/*#######################################################
 *
 *   Maintained 2017-2024 by Gregor Santner <gsantner AT mailbox DOT org>
 *   License of this file: Apache 2.0
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
#########################################################*/
package net.marelias.notepad.activity;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Rect;
import android.graphics.RectF;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.text.Html;
import android.view.KeyEvent;
import android.view.MotionEvent;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.FragmentManager;

import net.marelias.notepad.R;
import net.marelias.notepad.format.FormatRegistry;
import net.marelias.notepad.model.AppSettings;
import net.marelias.notepad.model.Document;
import net.marelias.notepad.util.MarkorContextUtils;
import net.marelias.opoc.format.GsTextUtils;
import net.marelias.opoc.frontend.GsFragmentBase;
import net.marelias.opoc.frontend.GsFileBrowserListAdapter;
import net.marelias.opoc.util.GsContextUtils;
import net.marelias.opoc.util.GsFileUtils;

import java.io.File;

//import other.so.AndroidBug5497Workaround;

public class DocumentActivity extends MarkorBaseActivity {

    private Toolbar _toolbar;
    private FragmentManager _fragManager;

    private static boolean nextLaunchTransparentBg = false;

    public static void launch(final Activity activity, final Intent intent) {
        final File file = MarkorContextUtils.getIntentFile(intent, null);
        final Integer lineNumber = intent.hasExtra(Document.EXTRA_FILE_LINE_NUMBER) ? intent.getIntExtra(Document.EXTRA_FILE_LINE_NUMBER, -1) : null;
        final Boolean doPreview = intent.hasExtra(Document.EXTRA_DO_PREVIEW) ? intent.getBooleanExtra(Document.EXTRA_DO_PREVIEW, false) : null;
        launch(activity, file, doPreview, lineNumber);
    }

    public static void launch(
            final Activity activity,
            final File file,
            final Boolean doPreview,
            final Integer lineNumber
    ) {
        launch(activity, file, doPreview, lineNumber, false);
    }

    private static void launch(
            final Activity activity,
            final File file,
            final Boolean doPreview,
            final Integer lineNumber,
            final boolean forceOpenInThisApp
    ) {
        if (activity == null || file == null) {
            return;
        }

        if (!forceOpenInThisApp && file.isFile() && !FormatRegistry.isFileSupported(file)) {
            askUserIfWantsToOpenFileInThisApp(activity, file);
            return;
        }

        final Intent intent;
        if (GsFileBrowserListAdapter.isVirtualFolder(file) || file.isDirectory()) {
            intent = new Intent(activity, MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT);
        } else {
            intent = new Intent(activity, DocumentActivity.class);

            if (lineNumber != null) {
                intent.putExtra(Document.EXTRA_FILE_LINE_NUMBER, lineNumber);
            }

            if (doPreview != null) {
                intent.putExtra(Document.EXTRA_DO_PREVIEW, doPreview);
            }
        }

        intent.putExtra(Document.EXTRA_FILE, file);

        nextLaunchTransparentBg = (activity instanceof MainActivity);
        GsContextUtils.instance.animateToActivity(activity, intent, false, null);
    }

    public static void askUserIfWantsToOpenFileInThisApp(final Activity activity, final File file) {
        if (GsFileUtils.isContentsPlainText(file)) {
            new AlertDialog.Builder(activity, R.style.Theme_AppCompat_DayNight_Dialog_Rounded)
                    .setTitle(R.string.open_with)
                    .setMessage(R.string.selected_file_may_be_a_textfile_want_to_open_in_editor)
                    .setIcon(R.drawable.ic_open_in_browser_black_24dp)
                    .setPositiveButton("Open here", (dialog1, which) -> DocumentActivity.launch(activity, file, null, null, true))
                    .setNegativeButton("Other app", (dialog1, which) -> new MarkorContextUtils(activity).viewFileInOtherApp(activity, file, null))
                    .create()
                    .show();
        } else {
            new MarkorContextUtils(activity).viewFileInOtherApp(activity, file, null);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        StoragePermissionActivity.requestPermissions(this);
        AppSettings.clearDebugLog();
        if (nextLaunchTransparentBg) {
            //getWindow().getDecorView().setBackgroundColor(Color.TRANSPARENT);
            nextLaunchTransparentBg = false;
        }
        setContentView(R.layout.document__activity);
        _toolbar = findViewById(R.id.toolbar);

        setSupportActionBar(findViewById(R.id.toolbar));
        _fragManager = getSupportFragmentManager();

        handleLaunchingIntent(getIntent());
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        handleLaunchingIntent(intent);
    }

    private void handleLaunchingIntent(final Intent intent) {
        if (intent == null) return;

        final String intentAction = intent.getAction();
        final Uri intentData = intent.getData();

        // Pull the file from the intent
        // -----------------------------------------------------------------------
        File file = (File) intent.getSerializableExtra(Document.EXTRA_FILE);

        final boolean intentIsView = Intent.ACTION_VIEW.equals(intentAction);
        final boolean intentIsEdit = Intent.ACTION_EDIT.equals(intentAction);

        if (Intent.ACTION_PROCESS_TEXT.equals(intentAction) && intent.hasExtra(Intent.EXTRA_PROCESS_TEXT)) {
            intent.putExtra(Intent.EXTRA_TEXT, intent.getStringExtra("android.intent.extra.PROCESS_TEXT"));
            return;
        } else if (file == null && (intentIsView || intentIsEdit)) {
            file = _cu.extractFileFromIntent(this, intent);
            if (file == null) {
                // More permissive - file may not exist
                // Will be filtered out in next stage
                file = MarkorContextUtils.getIntentFile(intent, null);
            }
        }

        // Decide what to do with the file
        // -----------------------------------------------------------------------
        if (file == null || !_cu.canWriteFile(this, file, false, true)) {
            showNotSupportedMessage();
        } else {
            // Open in editor/viewer
            final Document doc = new Document(file);
            Integer startLine = null;
            if (intent.hasExtra(Document.EXTRA_FILE_LINE_NUMBER)) {
                startLine = intent.getIntExtra(Document.EXTRA_FILE_LINE_NUMBER, Document.EXTRA_FILE_LINE_NUMBER_LAST);
            } else if (intentData != null) {
                final String line = intentData.getQueryParameter("line");
                if (line != null) {
                    startLine = GsTextUtils.tryParseInt(line, -1);
                }
            }

            // Start in a specific mode if required. Otherwise let the fragment decide
            Boolean startInPreview = null;
            if (startLine != null) {
                // If a line is requested, open in edit mode so the line is shown
                startInPreview = false;
            } else if (intent.getBooleanExtra(Document.EXTRA_DO_PREVIEW, false) || file.getName().startsWith("index.")) {
                startInPreview = true;
            }

            showTextEditor(doc, startLine, startInPreview);
        }
    }

    private void showNotSupportedMessage() {
        final String notSupportedMessage = (getString(R.string.filemanager_doesnot_supply_required_data__appspecific) + "\n\n" + getString(R.string.sync_to_local_folder_notice)).replace("\n", "<br/>");
        new AlertDialog.Builder(this, R.style.Theme_AppCompat_DayNight_Dialog_Rounded)
                .setMessage(Html.fromHtml(notSupportedMessage))
                .setNegativeButton(R.string.more_info, (di, i) -> _cu.openWebpageInExternalBrowser(this, ""))
                .setPositiveButton(android.R.string.ok, null)
                .setOnDismissListener((dialogInterface) -> finish())
                .create().show();
    }

    private final RectF point = new RectF(0, 0, 0, 0);

    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        if (_appSettings.isSwipeToChangeMode() && _appSettings.isEditorLineBreakingEnabled() && getCurrentVisibleFragment() instanceof DocumentEditAndViewFragment) {
            try {
                Rect activityVisibleSize = new Rect();
                getWindow().getDecorView().getWindowVisibleDisplayFrame(activityVisibleSize);

                if (event.getAction() == MotionEvent.ACTION_DOWN && event.getY() > (_toolbar.getBottom() + _cu.convertDpToPx(this, 8)) & event.getY() < (activityVisibleSize.bottom - _cu.convertDpToPx(this, 52))) {
                    point.set(event.getX(), event.getY(), 0, 0);
                }
//                else if (event.getAction() == MotionEvent.ACTION_UP) {
//                    point.set(point.left, point.top, event.getX(), event.getY());
//                    if (Math.abs(point.width()) > SWIPE_MIN_DX && Math.abs(point.height()) < SWIPE_MAX_DY) {
//                        getCurrentVisibleFragment().getFragmentMenu().performIdentifierAction(R.id.action_preview_edit_toggle, 0);
//                    }
//                }
            } catch (Exception ignored) {
                // No fancy exception handling :P. Nothing to see here.
            }
        }
        try {
            return super.dispatchTouchEvent(event);
        } catch (IndexOutOfBoundsException ignored) {
            return false;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        _cu.extractResultFromActivityResult(this, requestCode, resultCode, data);
    }

    public void setTitle(final CharSequence title) {
        final ActionBar bar = getSupportActionBar();
        if (bar != null) {
            bar.setTitle(title);
        }
    }

    public void setDocumentTitle(final String title) {
        setTitle(title);
    }

    public void showTextEditor(final Document document, final Integer lineNumber, final Boolean startPreview) {
        final GsFragmentBase<?, ?> currentFragment = getCurrentVisibleFragment();

        final boolean sameDocumentRequested = (
                currentFragment instanceof DocumentEditAndViewFragment &&
                        document.path.equals(((DocumentEditAndViewFragment) currentFragment).getDocument().path));

        if (!sameDocumentRequested) {
            showFragment(DocumentEditAndViewFragment.newInstance(document, lineNumber, startPreview));
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
//        _cu.setKeepScreenOn(this, _appSettings.isKeepScreenOn());
    }

    @Override
    @SuppressWarnings("StatementWithEmptyBody")
    public void onBackPressed() {
        final int entryCount = _fragManager.getBackStackEntryCount();
        final GsFragmentBase<?, ?> top = getCurrentVisibleFragment();

        // We pop the stack to go back to the previous fragment
        // if the top fragment does not handle the back press
        // Doesn't actually get called as we have 1 fragment in the stack
        if (top != null && !top.onBackPressed() && entryCount > 1) {
            _fragManager.popBackStack();
            return;
        }

        // Handle in this activity
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            finishAndRemoveTask();
        } else {
            finish();
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        return super.onReceiveKeyPress(getCurrentVisibleFragment(), keyCode, event) || super.onKeyDown(keyCode, event);
    }

    public GsFragmentBase<?, ?> showFragment(GsFragmentBase<?, ?> fragment) {
        if (fragment != getCurrentVisibleFragment()) {
            _fragManager.beginTransaction()
                    .replace(R.id.document__placeholder_fragment, fragment, fragment.getFragmentTag())
                    .commit();

            supportInvalidateOptionsMenu();
        }
        return fragment;
    }

    private GsFragmentBase<?, ?> getCurrentVisibleFragment() {
        return (GsFragmentBase<?, ?>) getSupportFragmentManager().findFragmentById(R.id.document__placeholder_fragment);
    }
}
