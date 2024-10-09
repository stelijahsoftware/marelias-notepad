/*#######################################################
 *
 *   Maintained 2018-2024 by Gregor Santner <gsantner AT mailbox DOT org>
 *   License of this file: Apache 2.0
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
#########################################################*/
package net.marelias.notepad.format;

import androidx.annotation.NonNull;

import net.marelias.notepad.ApplicationObject;
import net.marelias.notepad.model.AppSettings;

import java.io.File;

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
