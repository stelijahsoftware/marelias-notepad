/*#######################################################
 *
 *   Maintained 2017-2024 by Gregor Santner <gsantner AT mailbox DOT org>
 *   License of this file: Apache 2.0
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
#########################################################*/
package org.marelias.notepad.frontend;

import android.content.Context;

import androidx.fragment.app.FragmentManager;

import org.marelias.notepad.ApplicationObject;
import org.marelias.notepad.R;
import org.marelias.notepad.model.AppSettings;
import org.marelias.notepad.util.MarkorContextUtils;
import org.marelias.opoc.frontend.GsFileBrowserDialog;
import org.marelias.opoc.frontend.GsFileBrowserOptions;

public class MarkorFileBrowserFactory {
    public static GsFileBrowserOptions.Options prepareFsViewerOpts(
            final Context context,
            final boolean doSelectFolder,
            final GsFileBrowserOptions.SelectionListener listener
    ) {
        final GsFileBrowserOptions.Options opts = new GsFileBrowserOptions.Options();
        final MarkorContextUtils cu = new MarkorContextUtils(context);
        final AppSettings appSettings = ApplicationObject.settings();

        if (listener != null) {
            opts.listener = listener;
        }
        opts.doSelectFolder = doSelectFolder;
        opts.doSelectFile = !doSelectFolder;

        opts.okButtonEnable = opts.doSelectFolder || opts.doSelectMultiple;

        opts.searchButtonImage = R.drawable.ic_search_black_24dp;
        opts.newDirButtonImage = R.drawable.baseline_create_new_folder_24;
        opts.homeButtonImage = R.drawable.ic_home_black_24dp;
        opts.newDirButtonText = R.string.create_folder;
        opts.upButtonEnable = true;
        opts.homeButtonEnable = true;
        opts.contentDescriptionFolder = R.string.folder;
        opts.contentDescriptionSelected = R.string.selected;
        opts.contentDescriptionFile = R.string.file;

        opts.accentColor = R.color.accent;
        opts.primaryColor = R.color.primary;
        opts.primaryTextColor = R.color.primary_text;
        opts.secondaryTextColor = R.color.secondary_text;
        opts.backgroundColor = R.color.background1;
        opts.titleTextColor = R.color.primary_text;
        opts.fileColor = R.color.file;
        opts.folderColor = R.color.folder;
        opts.fileImage = R.drawable.ic_file_white_24dp;
        opts.folderImage = R.drawable.ic_folder_gray_24dp;
        opts.descriptionFormat = appSettings.getString(R.string.pref_key__file_description_format, "");

        opts.titleText = R.string.select;

        opts.mountedStorageFolder = cu.getStorageAccessFolder(context);

        opts.refresh = () -> {
            opts.sortFolderFirst = appSettings.isFileBrowserSortFolderFirst();
            opts.sortByType = appSettings.getFileBrowserSortByType();
            opts.sortReverse = appSettings.isFileBrowserSortReverse();
            opts.filterShowDotFiles = true; // appSettings.isFileBrowserFilterShowDotFiles();
            opts.favouriteFiles = appSettings.getFavouriteFiles();
            opts.recentFiles = appSettings.getRecentFiles();
            opts.popularFiles = appSettings.getPopularFiles();
        };
        opts.refresh.callback();

        return opts;
    }

    private static GsFileBrowserDialog showDialog(final FragmentManager fm, final GsFileBrowserOptions.Options opts) {
        final GsFileBrowserDialog filesystemViewerDialog = GsFileBrowserDialog.newInstance(opts);
        filesystemViewerDialog.show(fm, GsFileBrowserDialog.FRAGMENT_TAG);
        return filesystemViewerDialog;
    }

    public static GsFileBrowserDialog showFolderDialog(
            final GsFileBrowserOptions.SelectionListener listener,
            final FragmentManager fm,
            final Context context
    ) {
        final GsFileBrowserOptions.Options opts = prepareFsViewerOpts(context, true, listener);
        opts.okButtonText = R.string.select_this_folder;
        opts.descModtimeInsteadOfParent = true;
        return showDialog(fm, opts);
    }
}
