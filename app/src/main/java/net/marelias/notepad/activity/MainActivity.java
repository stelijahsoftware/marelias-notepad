/*#######################################################
 *
 *
 *   Maintained 2017-2024 by Gregor Santner <gsantner AT mailbox DOT org>
 *   License of this file: Apache 2.0
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
#########################################################*/
package net.marelias.notepad.activity;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.util.Pair;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.preference.PreferenceManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import net.marelias.notepad.R;
import net.marelias.notepad.format.FormatRegistry;
import net.marelias.notepad.frontend.NewFileDialog;
import net.marelias.notepad.frontend.filebrowser.MarkorFileBrowserFactory;
import net.marelias.notepad.frontend.textview.TextViewUtils;
import net.marelias.notepad.model.Document;
import net.marelias.notepad.util.MarkorContextUtils;
import net.marelias.opoc.frontend.base.GsFragmentBase;
import net.marelias.opoc.frontend.filebrowser.GsFileBrowserFragment;
import net.marelias.opoc.frontend.filebrowser.GsFileBrowserListAdapter;
import net.marelias.opoc.frontend.filebrowser.GsFileBrowserOptions;
import net.marelias.opoc.util.GsContextUtils;
import net.marelias.opoc.util.GsFileUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Field;

public class MainActivity extends MarkorBaseActivity implements GsFileBrowserFragment.FilesystemFragmentOptionsListener {
    private ViewPager2 _viewPager;
    private GsFileBrowserFragment _notebook;
    private FloatingActionButton _fab;
    private FloatingActionButton _fab2;

    private MarkorContextUtils _cu;
    private File _quickSwitchPrevFolder = null;

    @SuppressLint("SdCardPath")
    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try {
            _appSettings.getNotebookDirectory().mkdirs();
        } catch (Exception ignored) {
        }


        _cu = new MarkorContextUtils(this);
        setContentView(R.layout.main__activity);
        _viewPager = findViewById(R.id.main__view_pager_container);

        _fab = findViewById(R.id.fab_add_new_item);
        _fab2 = findViewById(R.id.fab_add_new_item_top);

        _fab.setOnClickListener(this::onClickFab);
        _fab2.setOnClickListener(this::onClickFab);

        _viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                onViewPagerPageSelected(position);
            }
        });

        setSupportActionBar(findViewById(R.id.toolbar));

        // Setup viewpager
        _viewPager.setAdapter(new SectionsPagerAdapter(getSupportFragmentManager()));
        _viewPager.setOffscreenPageLimit(4);

        reduceViewpagerSwipeSensitivity();

        // Elyahw: Create default note:
        final SharedPreferences getPrefs = PreferenceManager.getDefaultSharedPreferences(this);
        if (!getPrefs.getBoolean("default_file_was_created", false)) {
            createDefaultFile();
        }
    }

    private void createDefaultFile() {
        // Get the internal storage directory
        File dir = _appSettings.getNotebookDirectory();
        System.out.println(">>>>>>> elyahw: writing default note to: " + dir);

        String path = dir.getAbsolutePath();
        String fileName = "Sample note.txt";
        File file = new File(path, fileName);

        // Check if the file exists, if not, create it
        if (!file.exists()) {
            try {
                System.out.println(">>>>>>> WRITING FILE NOW>>>: " + file);

                // Create a new file (it won't write anything if a file with similar name exists)
                FileOutputStream fos = new FileOutputStream(file);
                String defaultContent = "# This is a green line\n" +
                                        "## This is a blue line\n" +
                                        "### This is an orange line\n" +
                                        "\n" +
                                        "// Red line\n" +
                                        "\n" +
                                        "123456789543250 154223\n" +
                                        "\n" +
                                        "[R red line\n" +
                                        "[o orange line\n" +
                                        "[b blue line\n" +
                                        "[g green line\n" +
                                        "[c cyan line\n" +
                                        "[p purple line\n" +
                                        "\n" +
                                        "✓ filter allow rule\n" +
                                        "✗ filter block rule\n" +
                                        "\n" +
                                        "This is a list:\n" +
                                        "- element 1\n" +
                                        "- element 2\n" +
                                        "- element 3\n" +
                                        "X - this is a finished task\n" +
                                        "x - another finished task\n" +
                                        "\n" +
                                        "All numbers will be coloured orange, for example 2024-09-28\n" +
                                        "\n" +
                                        "Urls and websites will be highlighted:\n" +
                                        "https://www.github.com/gsantner/markor\n" +
                                        "\n" +
                                        "You can also add **bold**, another __bold__ and _italic_ or *italic* text; or ~~struck~~\n" +
                                        "\n" +
                                        "***** Asterisks will be coloured\n" +
                                        "----- Dashes will be coloured too\n" +
                                        "===== as well\n" +
                                        "\n" +
                                        "You can also add importance tags like:\n" +
                                        "[h] task 1\n" +
                                        "[m] task 2\n" +
                                        "[l] task 3\n" +
                                        "\n" +
                                        "[High] task a\n" +
                                        "[medium] task b\n" +
                                        "[LOW] task c\n" +
                                        "\n" +
                                        "`This is code, it has a different font`\n" +
                                        "\n" +
                                        "$ This is another line of code`\n" +
                                        "\n" +
                                        "```\n" +
                                        "Multiple lines\n" +
                                        "of code..\n" +
                                        "```\n" +
                                        "\n";

                fos.write(defaultContent.getBytes());
                fos.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        // Store a value in preferences:
        PreferenceManager.getDefaultSharedPreferences(getBaseContext()).edit().putBoolean("default_file_was_created", true).apply();
    }

    @Override
    public void onActivityFirstTimeVisible() {
        super.onActivityFirstTimeVisible();
        // Switch to tab if specific folder _not_ requested, and not recreating from saved instance
        final int startTab = _appSettings.getAppStartupTab();
        if (startTab != R.id.nav_notebook && MarkorContextUtils.getValidIntentFile(getIntent(), null) == null) {
            _viewPager.postDelayed(() -> _viewPager.setCurrentItem(tabIdToPos(startTab)), 100);
        }
    }


    @Override
    public void onSaveInstanceState(@NonNull final Bundle outState) {
        super.onSaveInstanceState(outState);

        // Save references to fragments
        try {
            final FragmentManager manager = getSupportFragmentManager();

            // Put and get notebook first. Most important for correct operation.
            manager.putFragment(outState, Integer.toString(R.id.nav_notebook), _notebook);

        } catch (NullPointerException | IllegalStateException ignored) {
            Log.d(MainActivity.class.getName(), "Child fragments null in onSaveInstanceState()");
        }
    }

    @Override
    public void onRestoreInstanceState(final Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);

        if (savedInstanceState == null) {
            return;
        }

        // Get back references to fragments
        try {
            final FragmentManager manager = getSupportFragmentManager();
            _notebook = (GsFileBrowserFragment) manager.getFragment(savedInstanceState, Integer.toString(R.id.nav_notebook));

            final NewFileDialog nf = (NewFileDialog) manager.findFragmentByTag(NewFileDialog.FRAGMENT_TAG);
            if (nf != null) {
                nf.setCallback(this::newItemCallback);
            }

        } catch (NullPointerException | IllegalStateException ignored) {
            Log.d(MainActivity.class.getName(), "Child fragment not found in onRestoreInstanceState()");
        }
    }

    // Reduces swipe sensitivity
    // Inspired by https://stackoverflow.com/a/72067439
    private void reduceViewpagerSwipeSensitivity() {
        final int SLOP_MULTIPLIER = 4;
        try {
            final Field ff = ViewPager2.class.getDeclaredField("mRecyclerView");
            ff.setAccessible(true);
            final RecyclerView recyclerView = (RecyclerView) ff.get(_viewPager);
            // Set a constant so we don't continuously reduce this value with every call
            recyclerView.setScrollingTouchSlop(RecyclerView.TOUCH_SLOP_PAGING);
            final Field touchSlopField = RecyclerView.class.getDeclaredField("mTouchSlop");
            touchSlopField.setAccessible(true);
            final int touchSlop = (int) touchSlopField.get(recyclerView);
            touchSlopField.set(recyclerView, touchSlop * SLOP_MULTIPLIER);
        } catch (Exception e) {
            Log.d(MainActivity.class.getName(), e.getMessage());
        }
    }

    @Override
    protected void onNewIntent(final Intent intent) {
        super.onNewIntent(intent);
        final File file = MarkorContextUtils.getValidIntentFile(intent, null);
        if (_notebook != null && file != null) {
            _viewPager.setCurrentItem(tabIdToPos(R.id.nav_notebook), false);
            if (file.isDirectory() || GsFileBrowserListAdapter.isVirtualFolder(file)) {
                _notebook.setCurrentFolder(file);
            } else {
                _notebook.getAdapter().showFile(file);
            }
        }
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        super.onOptionsItemSelected(item);
        if (item.getItemId() == R.id.action_settings) {
            _cu.animateToActivity(this, SettingsActivity.class, false, null);
            return true;
        }
        return false;
    }

    @Override
    public boolean onCreateOptionsMenu(final Menu menu) {
        getMenuInflater().inflate(R.menu.main__menu, menu);
        menu.findItem(R.id.action_settings).setVisible(_appSettings.isShowSettingsOptionInMainToolbar());

        _cu.tintMenuItems(menu, true, Color.WHITE);
        _cu.setSubMenuIconsVisibility(menu, true);
        return true;
    }

    @Override
    protected void onResume() {
        super.onResume();
//        if (!IntroActivity.isFirstStart(this)) {
            StoragePermissionActivity.requestPermissions(this);
//        }

        if (_appSettings.isRecreateMainRequired()) {
            // recreate(); // does not remake fragments
            final Intent intent = getIntent();
            overridePendingTransition(0, 0);
            intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
            finish();
            overridePendingTransition(0, 0);
            startActivity(intent);
        }

//        _cu.setKeepScreenOn(this, _appSettings.isKeepScreenOn());

    }

    public void onClickFab(final View view) {
        if (_notebook == null || _notebook.getAdapter() == null) {
            return;
        }

        if (_notebook.getAdapter().isCurrentFolderVirtual()) {
            _notebook.getAdapter().setCurrentFolder(_appSettings.getNotebookDirectory());
            return;
        }

        // This button creates a note only (rename it later by triggering the rename dialog)
        if (view.getId() == R.id.fab_add_new_item) {
            if (_cu.isUnderStorageAccessFolder(this, _notebook.getCurrentFolder(), true) && _cu.getStorageAccessFrameworkTreeUri(this) == null) {
                _cu.showMountSdDialog(this);
                return;
            }

            if (!_notebook.getAdapter().isCurrentFolderWriteable()) {
                return;
            }

            // Original file creation dialog:
            // NewFileDialog.newInstance(_notebook.getCurrentFolder(), true, this::newItemCallback)
            //         .show(getSupportFragmentManager(), NewFileDialog.FRAGMENT_TAG);

            String title = "";
            String format = "`yyyyMMdd HHmmss`"; // "`yyyyMMdd_HHmmss` {{title}}";
            final String template = ""; // "-----\nNote Title\n-----\n";
            NewFileDialog.createNewFileCaller(_notebook.getCurrentFolder(), this::newItemCallback, title, format, template);
        }

        // This button create a folder only (rename it later by triggering the rename dialog)
        if (view.getId() == R.id.fab_add_new_item_top) {
            if (_cu.isUnderStorageAccessFolder(this, _notebook.getCurrentFolder(), true) && _cu.getStorageAccessFrameworkTreeUri(this) == null) {
                _cu.showMountSdDialog(this);
                return;
            }

            if (!_notebook.getAdapter().isCurrentFolderWriteable()) {
                return;
            }

            // Original file creation dialog:
            // NewFileDialog.newInstance(_notebook.getCurrentFolder(), true, this::newItemCallback)
            //         .show(getSupportFragmentManager(), NewFileDialog.FRAGMENT_TAG);

            String title = "";
            String format = "`yyyyMMdd HHmmss`"; // "`yyyyMMdd_HHmmss` {{title}}";
            NewFileDialog.createNewFolderCaller(_notebook.getCurrentFolder(), this::newItemCallback, title, format);
        }

    }

    private void newItemCallback(final File file) {
        if (file.isFile()) {
            DocumentActivity.launch(MainActivity.this, file, false, null);
        }
        _notebook.getAdapter().showFile(file);
    }

    @Override
    public void onBackPressed() {
        // Check if fragment handled back press
        final GsFragmentBase<?, ?> frag = getPosFragment(getCurrentPos());
        if (frag == null || !frag.onBackPressed()) {
            super.onBackPressed();
        }
    }

    public String getFileBrowserTitle() {
        final File file = _notebook.getCurrentFolder();
        if (file != null && !_appSettings.getNotebookDirectory().equals(file)) {
            return "> " + file.getName();
        } else {
            return getString(R.string.app_name);
        }
    }

    public int tabIdToPos(final int id) {
        if (id == R.id.nav_notebook) return 0;
        //if (id == R.id.nav_todo) return 1;
        return 0;
    }

    public int tabIdFromPos(final int pos) { return 0;}//_bottomNav.getMenu().getItem(pos).getItemId();


    public int getCurrentPos() {
        return _viewPager.getCurrentItem();
    }

    public String getPosTitle(final int pos) {
        if (pos == 0) return getFileBrowserTitle();
        if (pos == 1) return getString(R.string.todo);
        if (pos == 2) return getString(R.string.quicknote);
        if (pos == 3) return getString(R.string.more);
        return "";
    }

    public GsFragmentBase<?, ?> getPosFragment(final int pos) {
        if (pos == 0) return _notebook;
        return null;
    }

    /**
     * Restores the default toolbar. Used when changing the tab or moving to another activity
     * while {@link GsFileBrowserFragment} action mode is active (e.g. when renaming a file)
     */
    private void restoreDefaultToolbar() {
        GsFileBrowserFragment wrFragment = getNotebook();
        if (wrFragment != null) {
            wrFragment.clearSelection();
        }
    }

    public void onViewPagerPageSelected(final int pos) {
        //_bottomNav.getMenu().getItem(pos).setChecked(true);

        if (pos == tabIdToPos(R.id.nav_notebook)) {
            _fab.show();
            _fab2.show();
            _cu.showSoftKeyboard(this, false, _notebook.getView());
        } else {
            _fab.hide();
            _fab2.hide();
            restoreDefaultToolbar();
        }

        setTitle(getPosTitle(pos));
    }

    private GsFileBrowserOptions.Options _filesystemDialogOptions = null;

    @Override
    public GsFileBrowserOptions.Options getFilesystemFragmentOptions(GsFileBrowserOptions.Options existingOptions) {
        if (_filesystemDialogOptions == null) {
            _filesystemDialogOptions = MarkorFileBrowserFactory.prepareFsViewerOpts(this, false, new GsFileBrowserOptions.SelectionListenerAdapter() {
                File toShow = null;

                @Override
                public void onFsViewerConfig(GsFileBrowserOptions.Options dopt) {
                    dopt.descModtimeInsteadOfParent = true;
                    dopt.rootFolder = _appSettings.getNotebookDirectory();
                    final File fallback = _appSettings.getFolderToLoadByMenuId(); // _appSettings.getAppStartupFolderMenuId()
                    final File file = MarkorContextUtils.getValidIntentFile(getIntent(), fallback);
                    if (!GsFileBrowserListAdapter.isVirtualFolder(file) && file.isFile()) {
                        dopt.startFolder = file.getParentFile();
                        toShow = file;
                    } else {
                        dopt.startFolder = file;
                    }
                    toShow = file.isFile() ? file : null;
                    dopt.doSelectMultiple = dopt.doSelectFolder = dopt.doSelectFile = true;
                    dopt.mountedStorageFolder = _cu.getStorageAccessFolder(MainActivity.this);
                }

                @Override
                public void onFsViewerDoUiUpdate(final GsFileBrowserListAdapter adapter) {
                    if (adapter != null && adapter.getCurrentFolder() != null && !TextUtils.isEmpty(adapter.getCurrentFolder().getName())) {
                        _appSettings.setFileBrowserLastBrowsedFolder(adapter.getCurrentFolder());
                        if (getCurrentPos() == tabIdToPos(R.id.nav_notebook)) {
                            setTitle(getFileBrowserTitle());
                        }
                        invalidateOptionsMenu();
                    }

                    if (toShow != null && adapter != null) {
                        adapter.showFile(toShow);
                        toShow = null;
                    }
                }

                @Override
                public void onFsViewerSelected(String request, File file, final Integer lineNumber) {
                    DocumentActivity.launch(MainActivity.this, file, null, lineNumber);
                }
            });
        }
        return _filesystemDialogOptions;
    }

    class SectionsPagerAdapter extends FragmentStateAdapter {

        SectionsPagerAdapter(FragmentManager fragMgr) {
            super(fragMgr, MainActivity.this.getLifecycle());
        }

        @NonNull
        @Override
        public Fragment createFragment(final int pos) {
            final GsFragmentBase<?, ?> frag;
            final int id = tabIdFromPos(pos);

            frag = _notebook = GsFileBrowserFragment.newInstance();

            frag.setMenuVisibility(false);
            return frag;
        }

        @Override
        public int getItemCount() {
            return 1;
        }
    }

    public GsFileBrowserFragment getNotebook() {
        return _notebook;
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onStop() {
        super.onStop();
        restoreDefaultToolbar();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        _cu.extractResultFromActivityResult(this, requestCode, resultCode, data);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        return super.onReceiveKeyPress(getPosFragment(getCurrentPos()), keyCode, event) ? true : super.onKeyDown(keyCode, event);
    }
}
