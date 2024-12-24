/*#######################################################
 *
 *   Maintained 2017-2024 by Gregor Santner <gsantner AT mailbox DOT org>
 *   License of this file: Apache 2.0
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
#########################################################*/
package net.marelias.notepad.activity;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Toast;

import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceScreen;

import net.marelias.notepad.ApplicationObject;
import net.marelias.notepad.R;
import net.marelias.notepad.frontend.filebrowser.MarkorFileBrowserFactory;
import net.marelias.notepad.model.AppSettings;
import net.marelias.opoc.frontend.base.GsActivityBase;
import net.marelias.opoc.frontend.base.GsPreferenceFragmentBase;
import net.marelias.opoc.frontend.filebrowser.GsFileBrowserOptions;
import net.marelias.opoc.frontend.settings.GsFontPreferenceCompat;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Locale;

public class SettingsActivity extends MarkorBaseActivity {

    @SuppressWarnings("WeakerAccess")
    public static class RESULT {
        public static final int NOCHANGE = -1;
        public static final int CHANGED = 1;
        public static final int RESTART_REQ = 2;
    }

    public static int activityRetVal = RESULT.NOCHANGE;
    private static int iconColor = Color.WHITE;

    protected Toolbar toolbar;

    public void onCreate(Bundle b) {
        // Must be applied before setContentView
        super.onCreate(b);

        // Load UI
        setContentView(R.layout.settings__activity);
        toolbar = findViewById(R.id.toolbar);

        // Custom code
        GsFontPreferenceCompat.additionalyCheckedFolder = new File(_appSettings.getNotebookDirectory(), ".app/fonts");
        iconColor = _cu.rcolor(this, R.color.primary_text);
        toolbar.setTitle(R.string.settings);
        setSupportActionBar(findViewById(R.id.toolbar));
        toolbar.setNavigationIcon(getResources().getDrawable(R.drawable.ic_arrow_back_white_24dp));
        toolbar.setNavigationOnClickListener(view -> SettingsActivity.this.onBackPressed());
        showFragment(SettingsFragmentMaster.TAG, false);
    }

    protected void showFragment(String tag, boolean addToBackStack) {
        String toolbarTitle = getString(R.string.settings);
        GsPreferenceFragmentBase prefFrag = (GsPreferenceFragmentBase) getSupportFragmentManager().findFragmentByTag(tag);
        if (prefFrag == null) {
            switch (tag) {
                case SettingsFragmentMaster.TAG:
                default: {
                    prefFrag = new SettingsFragmentMaster();
                    toolbarTitle = prefFrag.getTitleOrDefault(toolbarTitle);
                    break;
                }
            }
        }
        toolbar.setTitle(toolbarTitle);
        FragmentTransaction t = getSupportFragmentManager().beginTransaction();
        if (addToBackStack) {
            t.addToBackStack(tag);
        }
        t.replace(R.id.settings__activity__fragment_placeholder, prefFrag, tag).commit();
    }

    @Override
    protected void onStop() {
        setResult(activityRetVal);
        super.onStop();
    }

    public static abstract class MarkorSettingsFragment extends GsPreferenceFragmentBase<AppSettings> {
        @Override
        protected AppSettings getAppSettings(Context context) {
            return ApplicationObject.settings();
        }

        @Override
        protected void onPreferenceChanged(SharedPreferences prefs, String key) {
            activityRetVal = RESULT.CHANGED;
        }

        @Override
        @SuppressWarnings("rawtypes")
        protected void onPreferenceScreenChanged(PreferenceFragmentCompat preferenceFragmentCompat, PreferenceScreen preferenceScreen) {
            super.onPreferenceScreenChanged(preferenceFragmentCompat, preferenceScreen);
            if (!TextUtils.isEmpty(preferenceScreen.getTitle())) {
                if (getActivity() instanceof GsActivityBase && ((GsActivityBase) getActivity()).getToolbar() != null) {
                    ((GsActivityBase) getActivity()).getToolbar().setTitle(preferenceScreen.getTitle());
                }
            }
        }
    }

    @Override
    public void onBackPressed() {
        GsPreferenceFragmentBase prefFrag = (GsPreferenceFragmentBase) getSupportFragmentManager().findFragmentByTag(SettingsFragmentMaster.TAG);
        if (prefFrag != null && prefFrag.canGoBack()) {
            prefFrag.goBack();
            return;
        }
        super.onBackPressed();
    }

    public static class SettingsFragmentMaster extends MarkorSettingsFragment {
        public static final String TAG = "SettingsFragmentMaster";

        @Override
        public int getPreferenceResourceForInflation() {
            return R.xml.preferences_master;
        }

        @Override
        public String getFragmentTag() {
            return TAG;
        }

        @Override
        public void doUpdatePreferences() {
            String remove = "/storage/emulated/0/";
            updateSummary(R.string.pref_key__notebook_directory,
                    _cu.htmlToSpanned("<small><small>" + _appSettings.getNotebookDirectory().getAbsolutePath().replace(remove, "") + "</small></small>")
            );
//            updateSummary(R.string.pref_key__quicknote_filepath,
//                    _cu.htmlToSpanned("<small><small>" + _appSettings.getQuickNoteFile().getAbsolutePath().replace(remove, "") + "</small></small>")
//            );
//            updateSummary(R.string.pref_key__todo_filepath,
//                    _cu.htmlToSpanned("<small><small>" + _appSettings.getTodoFile().getAbsolutePath().replace(remove, "") + "</small></small>")
//            );
//            updatePreference(R.string.pref_key__is_launcher_for_special_files_enabled, null,
//                    ("Launcher (" + getString(R.string.special_documents) + ")"),
//                    getString(R.string.app_drawer_launcher_special_files_description), true
//            );
//            updateSummary(R.string.pref_key__exts_to_always_open_in_this_app, _appSettings.getString(R.string.pref_key__exts_to_always_open_in_this_app, ""));

//            updateSummary(R.string.pref_key__snippet_directory_path, _appSettings.getSnippetsDirectory().getAbsolutePath());

            final String fileDescFormat = _appSettings.getString(R.string.pref_key__file_description_format, "");
            if (fileDescFormat.equals("")) {
                updateSummary(R.string.pref_key__file_description_format, getString(R.string.default_));
            } else {
                updateSummary(R.string.pref_key__file_description_format, fileDescFormat);
            }

            // Display app version (elyahw)
            super.doUpdatePreferences();
            Context context = getContext();
            if (context == null) {
                return;
            }
            Locale locale = Locale.getDefault();
            Preference pref = findPreference(R.string.pref_key__more_info__source_code);

            pref.setTitle("About");
            pref.setSummary(String.format(locale,"Mar-Elias Notepad\n(%s)\nVersion v%s",
                                                    _cu.getAppIdUsedAtManifest(context),
                                                    //_cu.getAppIdFlavorSpecific(context),
                                                    _cu.getAppVersionName(context))); // BuildConfig.VERSION_NAME
        }

        @SuppressLint("ApplySharedPref")
        @Override
        protected void onPreferenceChanged(final SharedPreferences prefs, final String key) {
            super.onPreferenceChanged(prefs, key);
            final Context context = getContext();
            if (context == null) {
                return;
            }

            if (eq(key, R.string.pref_key__language)) {
                activityRetVal = RESULT.RESTART_REQ;
                _appSettings.setRecreateMainRequired(true);
            } else if (eq(key, R.string.pref_key__app_theme)) {
                _appSettings.applyAppTheme();
                getActivity().finish();
            }

//            else if (eq(key, R.string.pref_key__is_launcher_for_special_files_enabled)) {
//                boolean extraLaunchersEnabled = prefs.getBoolean(key, false);
//                new MarkorContextUtils(getActivity()).applySpecialLaunchersVisibility(getActivity(), extraLaunchersEnabled);
//            }
            else if (eq(key, R.string.pref_key__file_description_format)) {
                try {
                    new SimpleDateFormat(prefs.getString(key, ""), Locale.getDefault());
                } catch (IllegalArgumentException e) {
                    Toast.makeText(getContext(), e.getLocalizedMessage() + "\n\n" + getString(R.string.loading_default_value), Toast.LENGTH_SHORT).show();
                    prefs.edit().putString(key, "").commit();
                }
            }
        }

        @Override
        @SuppressWarnings({"ConstantConditions", "ConstantIfStatement", "StatementWithEmptyBody"})
        public Boolean onPreferenceClicked(Preference preference, String key, int keyResId) {
            final FragmentManager fragManager = getActivity().getSupportFragmentManager();
            switch (keyResId) {

                case R.string.pref_key__notebook_directory: {
                    MarkorFileBrowserFactory.showFolderDialog(new GsFileBrowserOptions.SelectionListenerAdapter() {
                        @Override
                        public void onFsViewerSelected(String request, File file, final Integer lineNumber) {
                            _appSettings.setNotebookDirectory(file);
                            _appSettings.setRecreateMainRequired(true);
                            doUpdatePreferences();
                        }

                        @Override
                        public void onFsViewerConfig(GsFileBrowserOptions.Options dopt) {
                            dopt.titleText = R.string.select_storage_folder;
                            dopt.rootFolder = _appSettings.getNotebookDirectory();
                        }
                    }, fragManager, getActivity());
                    return true;
                }
                case R.string.pref_key__more_info__source_code: {
                    // Either open a webpage:
                    // _cu.openWebpageInExternalBrowser(getContext(), "https://notepad.mar-elias.com/");

                    // Or display html text in a window:
                    String html_info = "<br>" + // + Add links
                                 "<font color='#000000'><b>Mar-Elias Notepad</b> is a fork of <b>Markor</b>, a simple text file editor with customised highlights</font><br>" +
                                 //"<font color='#276230'>Project website:</font><br>" +
                                 //"<font color='#0040ff'><a href='https://notepad.mar-elias.com/'>https://notepad.mar-elias.com/</a></font><br>" +
                                 "<br>" +
                                 "<font color='#000000'><b>Markor</b>, an open source notes project</font><br>" +
                                 //"Project name: Markor<br>" +
                                 "<font color='#276230'>Author: </font><font color='#000000'>Gregor Santner</font><br>" +
                                 "<font color='#276230'>Official project:</font>" +
                                 "<br><font color='#0040ff'><a href='https://github.com/gsantner/markor'>https://github.com/gsantner/markor</a></font><br>";// +
                                 //"<font color='#276230'>LICENSE: </font>" +
                                 //"<font color='#0040ff'><a href='https://github.com/writing-tools/marelias-notepad/blob/master/LICENSE.txt'>APACHE 2.0</a></font><br>";
                    // See:
                    // app/src/main/res/xml/preferences_master.xml

                    // This copies the content to the clipboard:
                    //_cu.setClipboard(getContext(), preference.getSummary());

                    // GsSimpleMarkdownParser smp = new GsSimpleMarkdownParser();
                    try {
                        // getResources().openRawResource(R.raw.license)
                        // String html = smp.parse(aaa, "", GsSimpleMarkdownParser.FILTER_CHANGELOG).getHtml();
                        _cu.showDialogWithHtmlTextView(getActivity(), R.string.about, html_info, true, null);
                    } catch (Exception ex) {

                    }
                    return true;
                }
            }

            if (key.startsWith("pref_key__editor_basic_color_scheme") && !key.contains("_fg_") && !key.contains("_bg_")) {
                _appSettings.setRecreateMainRequired(true);
                restartActivity();
            }
            return null;
        }

        @Override
        public boolean isDividerVisible() {
            return true;
        }
    }
}
