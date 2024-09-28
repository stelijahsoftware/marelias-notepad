/*#######################################################
 *
 *   Maintained 2017-2024 by Gregor Santner <gsantner AT mailbox DOT org>
 *   License of this file: Apache 2.0
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
#########################################################*/
package net.marelias.notepad.web;

import android.app.Activity;
import android.content.Context;
import android.webkit.WebView;

import net.marelias.notepad.ApplicationObject;
import net.marelias.notepad.activity.DocumentActivity;
import net.marelias.notepad.model.AppSettings;
import net.marelias.notepad.util.MarkorContextUtils;
import net.marelias.opoc.web.GsWebViewClient;

import java.io.File;
import java.net.URLDecoder;

public class MarkorWebViewClient extends GsWebViewClient {
    protected final Activity _activity;

    public MarkorWebViewClient(final WebView webView, final Activity activity) {
        super(webView);
        _activity = activity;
    }

//    @Override
//    public boolean shouldOverrideUrlLoading(WebView view, String url) {
//        try {
//            Context context = view.getContext();
//
//            if (url.equals("about:blank")) {
//                view.reload();
//                return true;
//            }
//            if (url.startsWith("file:///android_asset/")) {
//                return false;
//            } else if (url.startsWith("file://")) {
//                MarkorContextUtils su = new MarkorContextUtils(view.getContext());
//                File file = new File(URLDecoder.decode(url.replace("file://", "").replace("+", "%2B")));
//                for (String str : new String[]{file.getAbsolutePath(), file.getAbsolutePath().replaceFirst("[#].*$", ""), file.getAbsolutePath() + ".md", file.getAbsolutePath() + ".txt"}) {
//                    File f = new File(str);
//                    if (f.exists()) {
//                        file = f;
//                        break;
//                    }
//                }
//                DocumentActivity.handleFileClick(_activity, file, null);
//            }
//            else {
//                MarkorContextUtils su = new MarkorContextUtils(_activity);
//                AppSettings settings = ApplicationObject.settings();
//                if (!settings.isOpenLinksWithChromeCustomTabs() || (settings.isOpenLinksWithChromeCustomTabs() && !su.openWebpageInChromeCustomTab(context, url))) {
//                    su.openWebpageInExternalBrowser(context, url);
//                    return true;
//                }
//            }
//        } catch (Exception ignored) {
//        }
//        return true;
//    }
}
