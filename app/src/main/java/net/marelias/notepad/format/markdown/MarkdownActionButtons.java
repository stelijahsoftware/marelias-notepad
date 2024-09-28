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
//import android.text.Editable;
//import android.view.KeyEvent;

import androidx.annotation.NonNull;
import androidx.annotation.StringRes;

import net.marelias.notepad.R;
//import net.marelias.notepad.activity.DocumentActivity;
import net.marelias.notepad.format.ActionButtonBase;
//import net.marelias.notepad.frontend.MarkorDialogFactory;
//import net.marelias.notepad.frontend.textview.AutoTextFormatter;
//import net.marelias.notepad.frontend.textview.TextViewUtils;
import net.marelias.notepad.model.Document;
//import net.marelias.opoc.util.GsContextUtils;
//import net.marelias.opoc.util.GsFileUtils;

//import java.io.File;
//import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
//import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
