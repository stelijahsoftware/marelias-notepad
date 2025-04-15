/*#######################################################
 *
 *   Maintained 2017-2024 by Gregor Santner <gsantner AT mailbox DOT org>
 *   License of this file: Apache 2.0
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
#########################################################*/
package net.marelias.notepad.model;

import android.content.Context;
import android.os.Environment;

import androidx.annotation.ColorRes;
import androidx.annotation.IdRes;
import androidx.annotation.StringRes;

import net.marelias.notepad.R;
import net.marelias.notepad.format.FormatRegistry;
import net.marelias.notepad.util.MarkorContextUtils;
import net.marelias.opoc.frontend.GsFileBrowserListAdapter;
import net.marelias.opoc.opoc.GsSharedPreferencesPropertyBackend;
import net.marelias.opoc.util.GsCollectionUtils;
import net.marelias.opoc.util.GsFileUtils;

import java.io.File;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

@SuppressWarnings({"SameParameterValue", "WeakerAccess", "FieldCanBeLocal"})
public class AppSettings extends GsSharedPreferencesPropertyBackend {
//    private SharedPreferences _prefCache;
//    private SharedPreferences _prefHistory;
    public static Boolean _isDeviceGoodHardware = null;
    private MarkorContextUtils _cu;

    @Override
    public AppSettings init(final Context context) {
        super.init(context);
//        _prefCache = context.getSharedPreferences("cache", Context.MODE_PRIVATE);
//        _prefHistory = context.getSharedPreferences("history", Context.MODE_PRIVATE);
        _cu = new MarkorContextUtils(context);
        _isDeviceGoodHardware = _cu.isDeviceGoodHardware(context);

        return this;
    }

    public boolean isPreferViewMode() {
        return getBool(R.string.pref_key__is_preview_first, false);
    }

    public void setNotebookDirectory(final File file) {
        setString(R.string.pref_key__notebook_directory, Document.getPath(file));
    }

    public File getNotebookDirectory() {
        return new File(getString(R.string.pref_key__notebook_directory, getDefaultNotebookFile().getAbsolutePath()));
    }

    public File getDefaultNotebookFile() {
        return GsFileUtils.join(
                Environment.getExternalStorageDirectory(),
                "Documents",
                rstr(R.string.app_name).toLowerCase(Locale.ROOT));
    }

    public String getFontFamily() {
        String str = "sans-serif-regular"; // elyahw font
        return str;
    }

    public int getFontSize() {
        return getInt(R.string.pref_key__editor_font_size, 15);
    }

    public int getViewFontSize() {
        int size = getInt(R.string.pref_key__view_font_size, -1);
        return size < 2 ? getFontSize() : size;
    }

    public boolean isHighlightingEnabled() {
        return true;
    }

    public boolean isDynamicHighlightingEnabled() {
        return getBool(R.string.pref_key__is_dynamic_highlighting_activated, true);
    }

    public String getLanguage() {
        return getString(R.string.pref_key__language, "");
    }

    public void setRecreateMainRequired(boolean value) {
        setBool(R.string.pref_key__is_main_recreate_required, value);
    }

    public boolean isRecreateMainRequired() {
        boolean ret = getBool(R.string.pref_key__is_main_recreate_required, false);
        setBool(R.string.pref_key__is_main_recreate_required, false);
        return ret;
    }

    public String setFileBrowserSortByType(String v) {
        setString(R.string.pref_key__file_browser__sort_by_type, v);
        return v;
    }

    public String getFileBrowserSortByType() {
        return getString(R.string.pref_key__file_browser__sort_by_type, GsFileUtils.SORT_BY_MTIME);
    }

    public boolean setFileBrowserSortReverse(boolean value) {
        setBool(R.string.pref_key__sort_reverse, value);
        return value;
    }

    // Elyahw: Initial state of reverse button
    public boolean isFileBrowserSortReverse() {
        return getBool(R.string.pref_key__sort_reverse, true);
    }

    public boolean isShowSettingsOptionInMainToolbar() {
        return false; // getBool(R.string.pref_key__show_settings_option_in_main_toolbar, true);
    }

    public boolean isEditorStartEditingInCenter() {
        return getBool(R.string.pref_key__editor_start_editing_in_center, false);
    }

    public void setFavouriteFiles(final Collection<File> files) {
        final Set<String> set = new LinkedHashSet<>();
        for (final File f : files) {
            if (f != null && (f.exists() || GsFileBrowserListAdapter.isVirtualFolder(f))) {
                set.add(Document.getPath(f));
            }
        }
        setStringList(R.string.pref_key__favourite_files, GsCollectionUtils.map(set, p -> p));
    }

    private static final String PREF_PREFIX_EDIT_POS_CHAR = "PREF_PREFIX_EDIT_POS_CHAR";
    private static final String PREF_PREFIX_HIGHLIGHT_STATE = "PREF_PREFIX_HIGHLIGHT_STATE";
    private static final String PREF_PREFIX_PREVIEW_STATE = "PREF_PREFIX_PREVIEW_STATE";
    private static final String PREF_PREFIX_FONT_SIZE = "PREF_PREFIX_FONT_SIZE";
    private static final String PREF_PREFIX_FILE_FORMAT = "PREF_PREFIX_FILE_FORMAT";
    private static final String PREF_PREFIX_AUTO_FORMAT = "PREF_PREFIX_AUTO_FORMAT";
    private static final String PREF_PREFIX_LINE_NUM_STATE = "PREF_PREFIX_LINE_NUM_STATE";

    public void setLastEditPosition(final String path, final int pos) {
        if (fexists(path)) {
            setInt(PREF_PREFIX_EDIT_POS_CHAR + path, pos);
        }
    }

    public int getLastEditPosition(final String path, final int def) {
        if (!fexists(path)) {
            return def;
        } else {
            return getInt(PREF_PREFIX_EDIT_POS_CHAR + path, def);
        }
    }

    public void setDocumentLineNumbersEnabled(final String path, final boolean enabled) {
        if (fexists(path)) {
            setBool(PREF_PREFIX_LINE_NUM_STATE + path, enabled);
        }
    }

    public boolean getDocumentLineNumbersEnabled(final String path) {
        final boolean _default = false;
        if (!fexists(path)) {
            return _default;
        } else {
            return getBool(PREF_PREFIX_LINE_NUM_STATE + path, _default);
        }
    }

    public void setDocumentFormat(final String path, @StringRes final int format) {
        if (fexists(path)) {
            setString(PREF_PREFIX_FILE_FORMAT + path, _context.getString(format));
        }
    }

    @StringRes
    public int getDocumentFormat(final String path, final int _default) {
        if (!fexists(path)) {
            return _default;
        } else {
            final String value = getString(PREF_PREFIX_FILE_FORMAT + path, null);
            if (value == null) {
                return _default;
            }
//            final int sid = _cu.getResId(_context, GsContextUtils.ResType.STRING, value);
            return FormatRegistry.FORMAT_PLAIN; // sid != FormatRegistry.FORMAT_PLAIN ? sid : _default;
        }
    }

    public boolean getDocumentAutoFormatEnabled(final String path) {
        final boolean _default = true;
        if (!fexists(path)) {
            return _default;
        } else {
            return getBool(PREF_PREFIX_AUTO_FORMAT + path, _default);
        }
    }

    public int getDocumentFontSize(final String path) {
        final int _default = getFontSize();
        if (!fexists(path)) {
            return _default;
        } else {
            return getInt(PREF_PREFIX_FONT_SIZE + path, _default);
        }
    }

    public void setDocumentPreviewState(final String path, final boolean isViewMode) {
        setBool(PREF_PREFIX_PREVIEW_STATE + path, isViewMode);
    }

    public boolean getDocumentPreviewState(final String path) {
        // Use global setting as default
        final boolean _default = isPreferViewMode();
        // Always open in preview state when prefer preview mode is enabled
        if (_default || !fexists(path)) {
            return _default;
        } else {
            return getBool(PREF_PREFIX_PREVIEW_STATE + path, _default);
        }
    }

    public boolean getDocumentHighlightState(final String path, final CharSequence chars) {
        final boolean lengthOk = chars != null && chars.length() < (_isDeviceGoodHardware ? 100000 : 35000);
        return getBool(PREF_PREFIX_HIGHLIGHT_STATE + path, lengthOk && isHighlightingEnabled());
    }

    public static Set<File> getFileSet(final List<String> paths) {
        final Set<File> set = new LinkedHashSet<>();
        for (final String fp : paths) {
            final File f = new File(fp);
            if (f.exists() || GsFileBrowserListAdapter.isVirtualFolder(f)) {
                set.add(f);
            }
        }
        return set;
    }

    public Set<File> getFavouriteFiles() {
        return getFileSet(getStringList(R.string.pref_key__favourite_files));
    }

    public Set<File> getRecentFiles() {
        return getFileSet(getStringList(R.string.pref_key__recent_documents));
    }

    public Set<File> getPopularFiles() {
        return getFileSet(getStringList(R.string.pref_key__popular_documents));
    }

    public boolean isEditorHistoryEnabled() {
        return true;//getBool(R.string.pref_key__editor_history_enabled3, true);
    }

    // Font color is defined here:
    public int getEditorForegroundColor() {
        return rcolor(R.color.primary_text);
    }

    public boolean isSearchQueryCaseSensitive() {
        return getBool(R.string.pref_key__is_search_query_case_sensitive, false);
    }

    public void setSearchQueryCaseSensitivity(final boolean isQuerySensitive) {
        setBool(R.string.pref_key__is_search_query_case_sensitive, isQuerySensitive);
    }

    public boolean isSearchQueryUseRegex() {
        return getBool(R.string.pref_key__is_search_query_use_regex, false);
    }

    public void setSearchQueryRegexUsing(final boolean isUseRegex) {
        setBool(R.string.pref_key__is_search_query_use_regex, isUseRegex);
    }

    public boolean isSearchInContent() {
        return getBool(R.string.pref_key__is_search_in_content, false);
    }

    public void setSearchInContent(final boolean isSearchInContent) {
        setBool(R.string.pref_key__is_search_in_content, isSearchInContent);
    }

    public int getSearchMaxDepth() {
        int depth = getIntOfStringPref(R.string.pref_key__max_search_depth, Integer.MAX_VALUE);

        if (depth == 0) {
            return Integer.MAX_VALUE;
        }

        return depth;
    }

    public List<String> getFileSearchIgnorelist() {
        String pref = getString(R.string.pref_key__filesearch_ignorelist, "");
        return Arrays.asList(pref.replace("\r", "").replace("\n\n", "\n").split("\n"));
    }

    public @IdRes
    int getAppStartupTab() {
        return R.id.nav_notebook;
    }

    public boolean isSwipeToChangeMode() {
        return getBool(R.string.pref_key__swipe_to_change_mode, false);
    }

    public boolean setFileBrowserSortFolderFirst(boolean v) {
        setBool(R.string.pref_key__filesystem_folder_first, v);
        return v;
    }

    public boolean isFileBrowserSortFolderFirst() {
        return getBool(R.string.pref_key__filesystem_folder_first, false);
    }

    public File getFolderToLoadByMenuId() {
        return getNotebookDirectory();
    }


    public boolean isEditorLineBreakingEnabled() {
        return getBool(R.string.pref_key__editor_enable_line_breaking, true);
    }

    private List<String> extSettingCache;

    public synchronized boolean isExtOpenWithThisApp(String ext) {
        if (ext.equals("")) {
            ext = "None";
        }
        if (extSettingCache == null) {
            String pref = getString(R.string.pref_key__exts_to_always_open_in_this_app, "");
            extSettingCache = Arrays.asList(pref.toLowerCase().replace(",,", ",None,").replace(" ", "").split(","));
        }
        return extSettingCache.contains(ext) || extSettingCache.contains(".*");
    }

    public void setNewFileDialogLastUsedExtension(String v) {
        setString(R.string.pref_key__new_file_dialog_lastused_extension, v);
    }

    public void setNewFileDialogLastUsedType(final int format) {
        setString(R.string.pref_key__new_file_dialog_lastused_type, _context.getString(format));
    }

    public void setFileBrowserLastBrowsedFolder(File f) {
        setString(R.string.pref_key__file_browser_last_browsed_folder, Document.getPath(f));
    }

    public boolean getSetWebViewFulldrawing(boolean... setValue) {
        final String k = "getSetWebViewFulldrawing";
        if (setValue != null && setValue.length == 1) {
            setBool(k, setValue[0]);
            return setValue[0];
        }
        return getBool(k, false);
    }

}
