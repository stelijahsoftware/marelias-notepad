/*#######################################################
 *
 *   Maintained 2017-2024 by Gregor Santner <gsantner AT mailbox DOT org>
 *   License of this file: Apache 2.0
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
#########################################################*/
package net.marelias.notepad.format;

import android.app.Activity;
import android.content.Context;
import android.webkit.WebView;

import androidx.annotation.DrawableRes;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;

import net.marelias.notepad.R;
import net.marelias.notepad.frontend.MarkorDialogFactory;
import net.marelias.notepad.frontend.textview.HighlightingEditor;
import net.marelias.notepad.model.AppSettings;
import net.marelias.notepad.model.Document;
import net.marelias.notepad.util.MarkorContextUtils;
import net.marelias.opoc.frontend.GsSearchOrCustomTextDialog;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
