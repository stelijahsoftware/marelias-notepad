/*#######################################################
 *
 *   Maintained 2017-2024 by Gregor Santner <gsantner AT mailbox DOT org>
 *   License of this file: Apache 2.0
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
#########################################################*/
package net.marelias.notepad.format.markdown;

//import android.annotation.SuppressLint;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.annotation.StringRes;

import net.marelias.notepad.R;
import net.marelias.notepad.format.ActionButtonBase;
import net.marelias.notepad.model.Document;

import java.util.Arrays;
import java.util.List;

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
    public List<ActionItem> getFormatActionList() {
        return Arrays.asList(
        );
    }

    @Override
    public boolean runTitleClick() {

        return true;
    }

}
