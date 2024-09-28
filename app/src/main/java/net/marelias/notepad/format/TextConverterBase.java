/*#######################################################
 *
 *   Maintained 2018-2024 by Gregor Santner <gsantner AT mailbox DOT org>
 *   License of this file: Apache 2.0
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
#########################################################*/
package net.marelias.notepad.format;

import android.app.Activity;
import android.content.Context;
import android.net.Uri;
import android.text.format.DateFormat;
import android.webkit.WebView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;

import net.marelias.notepad.ApplicationObject;
import net.marelias.notepad.R;
import net.marelias.notepad.model.AppSettings;
import net.marelias.notepad.model.Document;
import net.marelias.opoc.format.GsTextUtils;
import net.marelias.opoc.util.GsContextUtils;
import net.marelias.opoc.util.GsFileUtils;

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
