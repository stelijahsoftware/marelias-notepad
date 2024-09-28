package net.marelias.notepad.activity.openeditor;

import android.content.Intent;
import android.os.Bundle;

import net.marelias.notepad.activity.DocumentActivity;
import net.marelias.notepad.activity.MainActivity;
import net.marelias.notepad.activity.MarkorBaseActivity;
import net.marelias.notepad.util.MarkorContextUtils;
import net.marelias.opoc.frontend.filebrowser.GsFileBrowserListAdapter;

import java.io.File;


/**
 * This Activity exists solely to launch DocumentActivity with the correct intent
 * it is necessary as widget and shortcut intents do not respect MultipleTask etc
 */
public class OpenFromShortcutOrWidgetActivity extends MarkorBaseActivity {

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        launchActivityAndFinish(getIntent());
    }

    @Override
    protected void onNewIntent(final Intent intent) {
        super.onNewIntent(intent);
        launchActivityAndFinish(intent);
    }

    private void launchActivityAndFinish(Intent intent) {
        DocumentActivity.launch(this, intent);
        finish();
    }
}
