/*#######################################################
 *
 *   Maintained 2017-2024 by Gregor Santner <gsantner AT mailbox DOT org>
 *   License of this file: Apache 2.0
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
#########################################################*/
package net.gsantner.notepad2.format;

import static android.util.Patterns.WEB_URL;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Handler;
import android.text.Editable;
import android.text.Selection;
import android.text.Spannable;
import android.text.TextUtils;
import android.view.HapticFeedbackConstants;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.widget.EditText;

import androidx.annotation.DrawableRes;
//import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
//import androidx.appcompat.widget.TooltipCompat;

//import com.flask.colorpicker.ColorPickerView;
//import com.flask.colorpicker.Utils;
//import com.flask.colorpicker.builder.ColorPickerClickListener;
//import com.flask.colorpicker.builder.ColorPickerDialogBuilder;

//import net.gsantner.notepad2.ApplicationObject;
import net.gsantner.notepad2.R;
import net.gsantner.notepad2.activity.DocumentActivity;
//import net.gsantner.notepad2.frontend.AttachLinkOrFileDialog;
import net.gsantner.notepad2.frontend.DatetimeFormatDialog;
import net.gsantner.notepad2.frontend.MarkorDialogFactory;
import net.gsantner.notepad2.frontend.textview.HighlightingEditor;
import net.gsantner.notepad2.frontend.textview.TextViewUtils;
import net.gsantner.notepad2.model.AppSettings;
import net.gsantner.notepad2.model.Document;
import net.gsantner.notepad2.util.MarkorContextUtils;

import net.gsantner.opoc.format.GsTextUtils;
import net.gsantner.opoc.frontend.GsSearchOrCustomTextDialog;
import net.gsantner.opoc.util.GsCollectionUtils;
//import net.gsantner.opoc.util.GsContextUtils;
import net.gsantner.opoc.util.GsFileUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@SuppressWarnings({"WeakerAccess", "UnusedReturnValue"})
public abstract class ActionButtonBase {
    private Activity _activity;
    private MarkorContextUtils _cu;

    protected HighlightingEditor _hlEditor;
    protected WebView _webView;
    protected Document _document;
    protected AppSettings _appSettings;

    private final GsSearchOrCustomTextDialog.DialogState _specialKeyDialogState = new GsSearchOrCustomTextDialog.DialogState();

    public static final String ACTION_ORDER_PREF_NAME = "action_order";

    // Override to implement custom search action
    public boolean onSearch() {
        MarkorDialogFactory.showSearchDialog(_activity, _hlEditor);
        return true;
    }

    // Override to implement custom title action
    public boolean runTitleClick() {
        return false;
    }

    /**
     * Derived classes must return a unique StringRes id.
     * This is used to extract the appropriate action order preference.
     *
     * @return StringRes preference key
     */
    @StringRes
    protected abstract int getFormatActionsKey();

    /**
     * Derived classes must return a List of ActionItem. One for each action they want to implement.
     *
     * @return List of ActionItems
     */
    protected abstract List<ActionItem> getFormatActionList();

    /**
     * Get a combined action list - from derived format and the base actions
     */
    private List<ActionItem> getActionList() {
        final List<ActionItem> commonActions = Arrays.asList(
                new ActionItem(R.string.abid_common_delete_lines, R.drawable.ic_delete_black_24dp, R.string.delete_lines),
                new ActionItem(R.string.abid_common_duplicate_lines, R.drawable.ic_duplicate_lines_black_24dp, R.string.duplicate_lines),
                new ActionItem(R.string.abid_common_new_line_below, R.drawable.ic_baseline_keyboard_return_24, R.string.start_new_line_below),
                new ActionItem(R.string.abid_common_move_text_one_line_up, R.drawable.ic_baseline_arrow_upward_24, R.string.move_text_one_line_up).setRepeatable(true),
                new ActionItem(R.string.abid_common_move_text_one_line_down, R.drawable.ic_baseline_arrow_downward_24, R.string.move_text_one_line_down).setRepeatable(true),
                new ActionItem(R.string.abid_common_insert_snippet, R.drawable.ic_baseline_file_copy_24, R.string.insert_snippet),
                new ActionItem(R.string.abid_common_special_key, R.drawable.ic_keyboard_black_24dp, R.string.special_key),
                new ActionItem(R.string.abid_common_time, R.drawable.ic_access_time_black_24dp, R.string.date_and_time),
                new ActionItem(R.string.abid_common_open_link_browser, R.drawable.ic_open_in_browser_black_24dp, R.string.open_link),

                new ActionItem(R.string.abid_common_web_jump_to_very_top_or_bottom, R.drawable.ic_vertical_align_center_black_24dp, R.string.jump_to_bottom).setDisplayMode(ActionItem.DisplayMode.VIEW),
                new ActionItem(R.string.abid_common_view_file_in_other_app, R.drawable.ic_baseline_open_in_new_24, R.string.open_with).setDisplayMode(ActionItem.DisplayMode.VIEW),
                new ActionItem(R.string.abid_common_rotate_screen, R.drawable.ic_rotate_left_black_24dp, R.string.rotate).setDisplayMode(ActionItem.DisplayMode.ANY)
        );

        // Order is enforced separately
        final Map<Integer, ActionItem> unique = new HashMap<>();

        for (final ActionItem item : commonActions) {
            unique.put(item.keyId, item);
        }

        // Actions in the derived class override common actions if they share the same keyId
        for (final ActionItem item : getFormatActionList()) {
            unique.put(item.keyId, item);
        }

        return new ArrayList<>(unique.values());
    }


    public static class ReplacePattern {
        public final Matcher matcher;
        public final String replacePattern;
        public final boolean replaceAll;

        public boolean isSameReplace() {
            return replacePattern.equals("$0");
        }

        /**
         * Construct a ReplacePattern
         *
         * @param searchPattern  regex search pattern
         * @param replacePattern replace string
         * @param replaceAll     whether to replace all or just the first
         */
        public ReplacePattern(Pattern searchPattern, String replacePattern, boolean replaceAll) {
            this.matcher = searchPattern.matcher("");
            this.replacePattern = replacePattern;
            this.replaceAll = replaceAll;
        }

        public CharSequence replace() {
            return replaceAll ? matcher.replaceAll(replacePattern) : matcher.replaceFirst(replacePattern);
        }

        public ReplacePattern(Pattern searchPattern, String replacePattern) {
            this(searchPattern, replacePattern, false);
        }

    }

    public ActionButtonBase setUiReferences(@Nullable final Activity activity, @Nullable final HighlightingEditor hlEditor, @Nullable final WebView webview) {
        _activity = activity;
        _hlEditor = hlEditor;
        _webView = webview;
        _cu = new MarkorContextUtils(_activity);
        return this;
    }

    public Document getDocument() {
        return _document;
    }

    public ActionButtonBase setDocument(Document document) {
        _document = document;
        return this;
    }

    public Activity getActivity() {
        return _activity;
    }

    public Context getContext() {
        return _activity != null ? _activity : _appSettings.getContext();
    }

    public MarkorContextUtils getCu() {
        return _cu;
    }

    public static class ActionItem {
        @StringRes
        public int keyId;
        @DrawableRes
        public int iconId;
        @StringRes
        public int stringId;
        public DisplayMode displayMode = DisplayMode.EDIT;

        public boolean isRepeatable = false;

        public enum DisplayMode {EDIT, VIEW, ANY}

        public ActionItem(@StringRes int key, @DrawableRes int icon, @StringRes int string) {
            keyId = key;
            iconId = icon;
            stringId = string;
        }

        public ActionItem setDisplayMode(DisplayMode mode) {
            displayMode = mode;
            return this;
        }

        public ActionItem setRepeatable(boolean repeatable) {
            isRepeatable = repeatable;
            return this;
        }
    }

    public void runJumpBottomTopAction(ActionItem.DisplayMode displayMode) {
        if (displayMode == ActionItem.DisplayMode.EDIT) {
            int pos = _hlEditor.getSelectionStart();
            _hlEditor.setSelection(pos == 0 ? _hlEditor.getText().length() : 0);
        } else if (displayMode == ActionItem.DisplayMode.VIEW) {
            boolean top = _webView.getScrollY() > 100;
            _webView.scrollTo(0, top ? 0 : _webView.getContentHeight());
            if (!top) {
                _webView.scrollBy(0, 1000);
                _webView.scrollBy(0, 1000);
            }
        }
    }

}
