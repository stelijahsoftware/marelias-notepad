/*#######################################################
 *
 *   Maintained 2017-2024 by Gregor Santner <gsantner AT mailbox DOT org>
 *   License of this file: Apache 2.0
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
#########################################################*/
package org.marelias.notepad.format;

import android.app.Activity;
import android.content.Context;
import android.webkit.WebView;

import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;

import org.marelias.notepad.frontend.MarkorDialogFactory;
import org.marelias.notepad.frontend.textview.HighlightingEditor;
import org.marelias.notepad.model.AppSettings;
import org.marelias.notepad.model.Document;
import org.marelias.notepad.util.MarkorContextUtils;

@SuppressWarnings({"WeakerAccess", "UnusedReturnValue"})
public class ActionButtonBase {
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

    public ActionButtonBase(@NonNull Context context, Document document) {
//        super(context, document);
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


    public static class ActionItem {
        public enum DisplayMode {EDIT, VIEW, ANY}
    }

}
