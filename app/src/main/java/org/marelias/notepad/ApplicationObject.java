/*#######################################################
 *
 *   Maintained 2017-2024 by Gregor Santner <gsantner AT mailbox DOT org>
 *   License of this file: Apache 2.0
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
#########################################################*/
package org.marelias.notepad;

import android.webkit.WebView;

import androidx.multidex.MultiDexApplication;

import org.marelias.notepad.model.AppSettings;

public class ApplicationObject extends MultiDexApplication {
    private volatile static ApplicationObject _app;
    private volatile static AppSettings _appSettings;

    public static ApplicationObject get() {
        return _app;
    }

    public static AppSettings settings() {
        return _appSettings;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        _app = this;
        _appSettings = new AppSettings().init(getApplicationContext());

        // Load search query history from persistent storage
        org.marelias.notepad.frontend.FileSearchEngine.loadHistory(getApplicationContext());

        // Per https://stackoverflow.com/a/54191884/4717438
        try {
            new WebView(getApplicationContext());
        } catch (Exception ignored) {
        }
    }
}
