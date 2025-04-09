/*#######################################################
 *
 *   Maintained 2017-2024 by Gregor Santner <gsantner AT mailbox DOT org>
 *   License of this file: Apache 2.0
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
#########################################################*/
package net.marelias.notepad.frontend;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.os.Build;
import android.text.Editable;
import android.view.WindowManager;
import android.widget.EditText;

import androidx.annotation.RequiresApi;
import androidx.core.content.ContextCompat;

import net.marelias.notepad.ApplicationObject;
import net.marelias.notepad.R;
import net.marelias.notepad.frontend.textview.TextViewUtils;
import net.marelias.notepad.model.AppSettings;
import net.marelias.opoc.frontend.GsSearchOrCustomTextDialog;
import net.marelias.opoc.frontend.GsSearchOrCustomTextDialog.DialogOptions;
import net.marelias.opoc.util.GsCollectionUtils;
import net.marelias.opoc.util.GsFileUtils;
import net.marelias.opoc.opoc.GsCallback;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MarkorDialogFactory {
    public static AppSettings as() {
        return ApplicationObject.settings();
    }

    public static void showSearchFilesDialog(
            final Activity activity,
            final File searchDir,
            final GsCallback.a3<String, Integer, Boolean> callback
    ) {
        if (activity == null || searchDir == null || !searchDir.canRead()) {
            return;
        }

        if (!FileSearchEngine.isSearchExecuting.get()) {
            FileSearchDialog.showDialog(activity, searchOptions -> {
                searchOptions.rootSearchDir = searchDir;
                FileSearchEngine.queueFileSearch(activity, searchOptions, searchResults ->
                        FileSearchResultSelectorDialog.showDialog(activity, searchResults, callback));
            });
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    public static void showGlobFilesDialog(
            final Activity activity,
            final File searchDir,
            final GsCallback.a1<File> callback
    ) {
        GsSearchOrCustomTextDialog.DialogOptions dopt = new GsSearchOrCustomTextDialog.DialogOptions();
        baseConf(activity, dopt);
        dopt.titleText = R.string.search_documents;
        dopt.isSearchEnabled = true;
        dopt.defaultText = "**/[!.]*.*";
        dopt.callback = (query) -> {
            final List<File> found = GsFileUtils.searchFiles(searchDir, query);
            GsSearchOrCustomTextDialog.DialogOptions dopt2 = new GsSearchOrCustomTextDialog.DialogOptions();
            baseConf(activity, dopt2);
            dopt2.titleText = R.string.select;
            dopt2.isSearchEnabled = true;
            dopt2.data = GsCollectionUtils.map(found, File::getPath);
            dopt2.positionCallback = (result) -> callback.callback(found.get(result.get(0)));
            dopt2.neutralButtonText = R.string.search;
            dopt2.neutralButtonCallback = dialog2 -> {
                dialog2.dismiss();
                showGlobFilesDialog(activity, searchDir, callback);
            };
            GsSearchOrCustomTextDialog.showMultiChoiceDialogWithSearchFilterUI(activity, dopt2);
        };
        GsSearchOrCustomTextDialog.showMultiChoiceDialogWithSearchFilterUI(activity, dopt);
    }

    // Basic search dialog
    public static void showSearchDialog(final Activity activity, final EditText text) {
        final DialogOptions dopt = new DialogOptions();
        baseConf(activity, dopt);
        final Editable edit = text.getText();
        dopt.data = Arrays.asList(edit.toString().split("\n", -1)); // Do not ignore empty lines
        dopt.extraFilter = "[^\\s]+"; // Line must have one or more non-whitespace to display
        dopt.titleText = R.string.search_documents;
        dopt.searchHintText = R.string.search;
        dopt.neutralButtonCallback = (dialog) -> {
            dialog.dismiss();
            SearchAndReplaceTextDialog.showSearchReplaceDialog(activity, edit, TextViewUtils.getSelection(text));
        };
        dopt.neutralButtonText = R.string.search_and_replace;
        dopt.positionCallback = (result) -> TextViewUtils.selectLines(text, result);
        GsSearchOrCustomTextDialog.showMultiChoiceDialogWithSearchFilterUI(activity, dopt);
    }

    @SuppressLint("StringFormatMatches")
    public static void showCopyMoveConflictDialog(final Activity activity, final String fileName, final String destName, final boolean multiple, final GsCallback.a1<Integer> callback) {
        final DialogOptions dopt = new DialogOptions();
        baseConf(activity, dopt);
        dopt.positionCallback = (result) -> callback.callback(result.get(0));
        final List<String> data = new ArrayList<>();
        // Order of options here should be synchronized with WrMarkorSingleton._moveOrCopySelected
        data.add(activity.getString(R.string.keep_both));
        data.add(activity.getString(R.string.overwrite));
        data.add(activity.getString(R.string.skip));
        if (multiple) {
            data.add(activity.getString(R.string.keep_both_all));
            data.add(activity.getString(R.string.overwrite_all));
            data.add(activity.getString(R.string.skip_all));
        }
        dopt.data = data;
        dopt.isSearchEnabled = false;
        dopt.messageText = activity.getString(R.string.copy_move_conflict_message, fileName, destName);
        dopt.dialogWidthDp = WindowManager.LayoutParams.WRAP_CONTENT;
        GsSearchOrCustomTextDialog.showMultiChoiceDialogWithSearchFilterUI(activity, dopt);
    }

    public static void baseConf(Activity activity, DialogOptions dopt) {
        dopt.clearInputIcon = R.drawable.ic_baseline_clear_24;
        dopt.textColor = ContextCompat.getColor(activity, R.color.primary_text);
        dopt.highlightColor = ContextCompat.getColor(activity, R.color.accent);
        dopt.dialogStyle = R.style.Theme_AppCompat_DayNight_Dialog_Rounded;
    }
}