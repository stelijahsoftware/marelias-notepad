/*#######################################################
 *
 *   Maintained 2017-2024 by Gregor Santner <gsantner AT mailbox DOT org>
 *   License of this file: Apache 2.0
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
#########################################################*/
package org.marelias.notepad.format;

//import android.annotation.SuppressLint;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.annotation.StringRes;

import org.marelias.notepad.R;
import org.marelias.notepad.model.Document;

public class MarkdownActionButtons extends ActionButtonBase {

    public MarkdownActionButtons(@NonNull Context context, Document document) {
//        super(context, document);
    }

    @Override
    protected @StringRes
    int getFormatActionsKey() {
        return R.string.pref_key__markdown__action_keys;
    }

    @Override
    public boolean runTitleClick() {
        return true;
    }

}
