/*#######################################################
 *
 *   Maintained 2018-2024 by Gregor Santner <gsantner AT mailbox DOT org>
 *   License of this file: Apache 2.0
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
#########################################################*/
package net.gsantner.notepad2.format;

import android.app.Activity;
import android.content.Context;
import android.net.Uri;
import android.text.format.DateFormat;
import android.webkit.WebView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;

import net.gsantner.notepad2.ApplicationObject;
import net.gsantner.notepad2.R;
import net.gsantner.notepad2.model.AppSettings;
import net.gsantner.notepad2.model.Document;
import net.gsantner.opoc.format.GsTextUtils;
import net.gsantner.opoc.util.GsContextUtils;
import net.gsantner.opoc.util.GsFileUtils;

import java.io.File;
import java.util.Date;
import java.util.Locale;

//import other.de.stanetz.jpencconverter.JavaPasswordbasedCryption;

@SuppressWarnings("WeakerAccess")
public abstract class TextConverterBase {
    //########################
    //## HTML
    //########################
    protected static final String TOKEN_FONT = "{{ app.text_font }}";


    //########################
    //## Methods
    //########################
    protected final AppSettings _appSettings;

    public TextConverterBase() {
        _appSettings = ApplicationObject.settings();
    }

    public boolean isFileOutOfThisFormat(final @NonNull File file) {
        final String name = file.getName().toLowerCase();
        final String ext = name.replaceAll(".*\\.", ".");
        return isFileOutOfThisFormat(file, name, ext);
    }

    protected abstract boolean isFileOutOfThisFormat(final File file, final String name, final String ext);
}
