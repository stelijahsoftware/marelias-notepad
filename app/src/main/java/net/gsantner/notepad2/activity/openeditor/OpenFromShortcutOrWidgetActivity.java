package net.gsantner.notepad2.activity.openeditor;

import android.content.Intent;
import android.os.Bundle;

import net.gsantner.notepad2.activity.DocumentActivity;
import net.gsantner.notepad2.activity.MainActivity;
import net.gsantner.notepad2.activity.MarkorBaseActivity;
import net.gsantner.notepad2.util.MarkorContextUtils;
import net.gsantner.opoc.frontend.filebrowser.GsFileBrowserListAdapter;

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
