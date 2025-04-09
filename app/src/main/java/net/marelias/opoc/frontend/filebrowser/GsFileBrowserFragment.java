/*#######################################################
 *
 * SPDX-FileCopyrightText: 2017-2024 Gregor Santner <gsantner AT mailbox DOT org>
 * SPDX-License-Identifier: Unlicense OR CC0-1.0
 *
 * Written 2018-2024 by Gregor Santner <gsantner AT mailbox DOT org>
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * You should have received a copy of the CC0 Public Domain Dedication along with this software. If not, see <http://creativecommons.org/publicdomain/zero/1.0/>.
#########################################################*/

/*
 * Revision 001 of FilesystemViewerCreator
 * A simple filesystem dialog with file, folder and multiple selection
 * most bits (color, text, images) can be controller using FilesystemViewerData.
 * The data container contains a listener callback for results.
 * Most features are usable without any additional project files and resources
 */
package net.marelias.opoc.frontend.filebrowser;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import net.marelias.notepad.ApplicationObject;
import net.marelias.notepad.R;
import net.marelias.notepad.format.FormatRegistry;
import net.marelias.notepad.frontend.MarkorDialogFactory;
import net.marelias.notepad.frontend.filebrowser.MarkorFileBrowserFactory;
import net.marelias.notepad.frontend.filesearch.FileSearchEngine;
import net.marelias.notepad.model.AppSettings;
import net.marelias.notepad.util.MarkorContextUtils;
import net.marelias.opoc.frontend.base.GsFragmentBase;
import net.marelias.opoc.opoc.GsSharedPreferencesPropertyBackend;
import net.marelias.opoc.util.GsCollectionUtils;
import net.marelias.opoc.util.GsContextUtils;
import net.marelias.opoc.util.GsFileUtils;

import java.io.File;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import other.writeily.write.WrMarkorSingleton;
import other.writeily.write.WrConfirmDialog;
import other.writeily.write.WrRenameDialog;

public class GsFileBrowserFragment extends GsFragmentBase<GsSharedPreferencesPropertyBackend, GsContextUtils> implements GsFileBrowserOptions.SelectionListener {
    //########################
    //## Static
    //########################
    public static GsFileBrowserFragment newInstance() {
        return new GsFileBrowserFragment();
    }

    //########################
    //## Member
    //########################

    private RecyclerView _recyclerList;
    private SwipeRefreshLayout _swipe;

    private GsFileBrowserListAdapter _filesystemViewerAdapter;
    private GsFileBrowserOptions.Options _dopt;
    private GsFileBrowserOptions.SelectionListener _callback;
    private AppSettings _appSettings;
    private Menu _fragmentMenu;
    private MarkorContextUtils _cu;
    private Toolbar _toolbar;

    //########################
    //## Methods
    //########################

    public interface FilesystemFragmentOptionsListener {
        GsFileBrowserOptions.Options getFilesystemFragmentOptions(GsFileBrowserOptions.Options existingOptions);
    }

    @Override
    public void onViewCreated(@NonNull View root, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(root, savedInstanceState);
        Context context = getContext();
        _recyclerList = root.findViewById(R.id.ui__filesystem_dialog__list);
        _swipe = root.findViewById(R.id.pull_to_refresh);

        _appSettings = ApplicationObject.settings();
        _cu = new MarkorContextUtils(root.getContext());
        final Activity activity = getActivity();

        if (!(getActivity() instanceof FilesystemFragmentOptionsListener)) {
            throw new RuntimeException("Error: " + activity.getClass().getName() + " doesn't implement FilesystemFragmentOptionsListener");
        }
        setDialogOptions(((FilesystemFragmentOptionsListener) activity).getFilesystemFragmentOptions(_dopt));

        LinearLayoutManager lam = (LinearLayoutManager) _recyclerList.getLayoutManager();
        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(activity, lam.getOrientation());
        _recyclerList.addItemDecoration(dividerItemDecoration);

        _filesystemViewerAdapter = new GsFileBrowserListAdapter(_dopt, context);
        _recyclerList.setAdapter(_filesystemViewerAdapter);
        _filesystemViewerAdapter.getFilter().filter("");
        onFsViewerDoUiUpdate(_filesystemViewerAdapter);

        _swipe.setOnRefreshListener(() -> {
            _filesystemViewerAdapter.reloadCurrentFolder();
            _swipe.setRefreshing(false);
        });

        if (FileSearchEngine.isSearchExecuting.get()) {
            FileSearchEngine.activity.set(new WeakReference<>(activity));
        }

        _toolbar = activity.findViewById(R.id.toolbar);
    }

    @Override
    public String getFragmentTag() {
        return "FilesystemViewerFragment";
    }

    @Override
    protected int getLayoutResId() {
        return R.layout.opoc_filesystem_fragment;
    }

    private void setDialogOptions(GsFileBrowserOptions.Options options) {
        _dopt = options;
        _callback = _dopt.listener;
        if (_callback != null) {
            _callback.onFsViewerConfig(_dopt); // Configure every time
        }
        _dopt.listener = this;
        checkOptions();
    }

    private void checkOptions() {
        if (_dopt.doSelectFile && !_dopt.doSelectMultiple) {
            _dopt.okButtonEnable = false;
        }
    }


    @Override
    public void onFsViewerSelected(String request, File file, final Integer lineNumber) {
        if (_callback != null) {
            _filesystemViewerAdapter.showFileAfterNextLoad(file);
            _callback.onFsViewerSelected(_dopt.requestId, file, lineNumber);
        }
    }

    @Override
    public void onFsViewerMultiSelected(String request, File... files) {
        if (_callback != null) {
            _callback.onFsViewerMultiSelected(_dopt.requestId, files);
        }
    }

    @Override
    public void onFsViewerCancel(String request) {
        if (_callback != null) {
            _callback.onFsViewerCancel(_dopt.requestId);
        }
    }

    @Override
    public void onFsViewerConfig(GsFileBrowserOptions.Options dopt) {
        if (_callback != null) {
            _callback.onFsViewerConfig(dopt);
        }
    }

    @Override
    @SuppressLint("SetTextI18n")
    public void onFsViewerDoUiUpdate(GsFileBrowserListAdapter adapter) {
        if (_callback != null) {
            _callback.onFsViewerDoUiUpdate(adapter);
        }

        updateMenuItems();
        _recyclerList.postDelayed(this::updateMenuItems, 1000);
    }

    private void updateMenuItems() {
        final String curFilepath = (getCurrentFolder() != null ? getCurrentFolder() : new File("/")).getAbsolutePath();
        final Set<File> selFiles = _filesystemViewerAdapter.getCurrentSelection();
        final int selCount = selFiles.size();
        final int totalCount = _filesystemViewerAdapter.getItemCount() - 1;   // Account for ".."
        final boolean selMulti1 = _dopt.doSelectMultiple && selCount == 1;
        final boolean selMultiMore = _dopt.doSelectMultiple && selCount > 1;
        final boolean selMultiAny = selMultiMore || selMulti1;
        final boolean selFilesOnly = _filesystemViewerAdapter.isFilesOnlySelected();
        final boolean selInVirtualDirectory = _filesystemViewerAdapter.isCurrentFolderVirtual();

        // Check if is a favourite
        boolean selTextFilesOnly = true;
        boolean selDirectoriesOnly = true;
        boolean selWritable = (!curFilepath.equals("/storage") && !curFilepath.equals("/storage/emulated"));
        boolean allSelectedFav = true;
        final Collection<File> favFiles = _dopt.favouriteFiles != null ? _dopt.favouriteFiles : Collections.emptySet();
        for (final File f : selFiles) {
            selTextFilesOnly &= FormatRegistry.isFileSupported(f, true);
            selWritable &= f.canWrite();
            selDirectoriesOnly &= f.isDirectory();
            allSelectedFav &= favFiles.contains(f);
        }

        if (_fragmentMenu != null && _fragmentMenu.findItem(R.id.action_delete_selected_items) != null) {
            _fragmentMenu.findItem(R.id.action_search).setVisible(selFiles.isEmpty() && !_filesystemViewerAdapter.isCurrentFolderVirtual());
            _fragmentMenu.findItem(R.id.action_delete_selected_items).setVisible((selMulti1 || selMultiMore) && selWritable);
            _fragmentMenu.findItem(R.id.action_rename_selected_item).setVisible(selMulti1 && selWritable & !selInVirtualDirectory);
            _fragmentMenu.findItem(R.id.action_move_selected_items).setVisible((selMulti1 || selMultiMore) && selWritable && !selInVirtualDirectory && !_cu.isUnderStorageAccessFolder(getContext(), getCurrentFolder(), true));
            _fragmentMenu.findItem(R.id.action_share_files).setVisible(selFilesOnly && (selMulti1 || selMultiMore) && !_cu.isUnderStorageAccessFolder(getContext(), getCurrentFolder(), true));
            _fragmentMenu.findItem(R.id.action_sort).setVisible(!_filesystemViewerAdapter.areItemsSelected());
            _fragmentMenu.findItem(R.id.action_settings).setVisible(!_filesystemViewerAdapter.areItemsSelected());
            _fragmentMenu.findItem(R.id.action_favourite).setVisible(selMultiAny && !allSelectedFav);
            _fragmentMenu.findItem(R.id.action_favourite_remove).setVisible(selMultiAny && allSelectedFav);
            _fragmentMenu.findItem(R.id.action_create_shortcut).setVisible(selMulti1 && (selFilesOnly || selDirectoriesOnly));
            _fragmentMenu.findItem(R.id.action_check_all).setVisible(_filesystemViewerAdapter.areItemsSelected() && selCount < totalCount);
            _fragmentMenu.findItem(R.id.action_clear_selection).setVisible(_filesystemViewerAdapter.areItemsSelected());
        }

//        if (_toolbar != null) {
//            _toolbar.setTitle(String.format("%s %d", _toolbar.getTitle(), totalCount));
//        }

        // TODO:
        // Update subtitle with count
        if (_toolbar != null) {
            if (_filesystemViewerAdapter.areItemsSelected()) {
                _toolbar.setTitle(String.format("(%d/%d)", selCount, totalCount));
            } else {
                _toolbar.setSubtitle("");
            }
        }
    }

    @Override
    public void onFsViewerItemLongPressed(File file, boolean doSelectMultiple) {
        if (_callback != null) {
            _callback.onFsViewerItemLongPressed(file, doSelectMultiple);
        }
    }

    @Override
    public boolean onBackPressed() {
        if (_filesystemViewerAdapter != null && _filesystemViewerAdapter.goBack()) {
            return true;
        }
        return super.onBackPressed();
    }

    public void reloadCurrentFolder() {
        _filesystemViewerAdapter.reloadCurrentFolder();
        onFsViewerDoUiUpdate(_filesystemViewerAdapter);
    }

    public File getCurrentFolder() {
        return _filesystemViewerAdapter != null ? _filesystemViewerAdapter.getCurrentFolder() : null;
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        _filesystemViewerAdapter.saveInstanceState(outState);
    }

    @Override
    public void onViewStateRestored(final Bundle savedInstanceState) {
        super.onViewStateRestored(savedInstanceState);
        _filesystemViewerAdapter.restoreSavedInstanceState(savedInstanceState);
    }

    @Override
    public void onResume() {
        super.onResume();
        if (_dopt.refresh != null) {
            _dopt.refresh.callback();
        }

        final File folder = getCurrentFolder();
        final Activity activity = getActivity();
        if (isVisible() && folder != null && activity != null) {
            activity.setTitle(folder.getName());
            reloadCurrentFolder();
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.filesystem__menu, menu);
        _cu.tintMenuItems(menu, true, Color.WHITE);
        _cu.setSubMenuIconsVisibility(menu, true);

        MenuItem item;
        if ((item = menu.findItem(R.id.action_folder_first)) != null) {
            item.setChecked(_dopt.sortFolderFirst);
        }
        if ((item = menu.findItem(R.id.action_sort_reverse)) != null) {
            item.setChecked(_dopt.sortReverse); // Elyahw
        }

        if ((item = menu.findItem(R.id.action_sort_by_name)) != null && GsFileUtils.SORT_BY_NAME.equals(_dopt.sortByType)) {
            item.setChecked(true);
        } else if ((item = menu.findItem(R.id.action_sort_by_date)) != null && GsFileUtils.SORT_BY_MTIME.equals(_dopt.sortByType)) {
            item.setChecked(true);
        } else if ((item = menu.findItem(R.id.action_sort_by_filesize)) != null && GsFileUtils.SORT_BY_FILESIZE.equals(_dopt.sortByType)) {
            item.setChecked(true);
        } else if ((item = menu.findItem(R.id.action_sort_by_mimetype)) != null && GsFileUtils.SORT_BY_MIMETYPE.equals(_dopt.sortByType)) {
            item.setChecked(true);
        }

        _fragmentMenu = menu;
        updateMenuItems();
    }

    public GsFileBrowserListAdapter getAdapter() {
        return _filesystemViewerAdapter;
    }

    @Override
    public boolean onOptionsItemSelected(final MenuItem item) {
        final int _id = item.getItemId();
        final Set<File> currentSelection = _filesystemViewerAdapter.getCurrentSelection();

        switch (_id) {
            case R.id.action_create_shortcut: {
                final File file = currentSelection.iterator().next();
                _cu.createLauncherDesktopShortcut(getContext(), file);
                return true;
            }
            case R.id.action_sort_by_name: {
                item.setChecked(true);
                _dopt.sortByType = _appSettings.setFileBrowserSortByType(GsFileUtils.SORT_BY_NAME);
                reloadCurrentFolder();
                return true;
            }
            case R.id.action_sort_by_date: {
                item.setChecked(true);
                _dopt.sortByType = _appSettings.setFileBrowserSortByType(GsFileUtils.SORT_BY_MTIME);
                reloadCurrentFolder();
                return true;
            }
            case R.id.action_sort_by_filesize: {
                item.setChecked(true);
                _dopt.sortByType = _appSettings.setFileBrowserSortByType(GsFileUtils.SORT_BY_FILESIZE);
                reloadCurrentFolder();
                return true;
            }
            case R.id.action_sort_by_mimetype: {
                item.setChecked(true);
                _dopt.sortByType = _appSettings.setFileBrowserSortByType(GsFileUtils.SORT_BY_MIMETYPE);
                reloadCurrentFolder();
                return true;
            }
            case R.id.action_sort_reverse: {
                item.setChecked(!item.isChecked());
                _dopt.sortReverse = _appSettings.setFileBrowserSortReverse(item.isChecked());
                reloadCurrentFolder();
                return true;
            }
            case R.id.action_search: {
                executeSearchAction();
                return true;
            }
            case R.id.action_folder_first: {
                item.setChecked(!item.isChecked());
                _dopt.sortFolderFirst = _appSettings.setFileBrowserSortFolderFirst(item.isChecked());
                reloadCurrentFolder();
                return true;
            }
            case R.id.action_favourite: {
                if (_filesystemViewerAdapter.areItemsSelected()) {
                    _dopt.favouriteFiles = GsCollectionUtils.union(_dopt.favouriteFiles, currentSelection);
                    _appSettings.setFavouriteFiles(_dopt.favouriteFiles);
                    updateMenuItems();
                }
                return true;
            }
            case R.id.action_favourite_remove: {
                if (_filesystemViewerAdapter.areItemsSelected()) {
                    _dopt.favouriteFiles = GsCollectionUtils.setDiff(_dopt.favouriteFiles, currentSelection);
                    _appSettings.setFavouriteFiles(_dopt.favouriteFiles);
                    updateMenuItems();
                }
                return true;
            }
            case R.id.action_delete_selected_items: {
                askForDeletingFilesRecursive((confirmed, data) -> {
                    if (confirmed) {
                        Runnable deleter = () -> {
                            WrMarkorSingleton.getInstance().deleteSelectedItems(currentSelection, getContext());
                            _recyclerList.post(() -> _filesystemViewerAdapter.reloadCurrentFolder());
                        };
                        new Thread(deleter).start();
                    }
                });
                return true;
            }
            case R.id.action_move_selected_items:
            {
                askForMoveOrCopy(_id == R.id.action_move_selected_items);
                return true;
            }
            case R.id.action_check_all: {
                _filesystemViewerAdapter.selectAll();
                return true;
            }
            case R.id.action_clear_selection: {
                _filesystemViewerAdapter.unselectAll();
                return true;
            }
            case R.id.action_share_files: {
                MarkorContextUtils s = new MarkorContextUtils(getContext());
                s.shareStreamMultiple(getContext(), currentSelection, "*/*");
                _filesystemViewerAdapter.reloadCurrentFolder();
                return true;
            }
            case R.id.action_rename_selected_item: {
                if (_filesystemViewerAdapter.areItemsSelected()) {
                    final File file = currentSelection.iterator().next();
                    final WrRenameDialog renameDialog = WrRenameDialog.newInstance(file, renamedFile -> reloadCurrentFolder());
                    renameDialog.show(getChildFragmentManager(), WrRenameDialog.FRAGMENT_TAG);
                }
                return true;
            }
        }

        return false;
    }

    private void executeSearchAction() {
        final File currentFolder = getCurrentFolder();
        MarkorDialogFactory.showSearchFilesDialog(getActivity(), currentFolder, (relPath, lineNumber, longPress) -> {
            final File load = new File(currentFolder, relPath);
            if (!longPress) {
                if (load.isDirectory()) {
                    _filesystemViewerAdapter.setCurrentFolder(load);
                } else {
                    onFsViewerSelected("", load, lineNumber);
                }
            } else {
                _filesystemViewerAdapter.showFile(load);
            }
        });
    }

    public void clearSelection() {
        if (_filesystemViewerAdapter != null) { // Happens when restoring after rotation etc
            _filesystemViewerAdapter.unselectAll();
        }
    }


    ///////////////
    public void askForDeletingFilesRecursive(WrConfirmDialog.ConfirmDialogCallback confirmCallback) {
        final ArrayList<File> itemsToDelete = new ArrayList<>(_filesystemViewerAdapter.getCurrentSelection());
        final StringBuilder message = new StringBuilder(String.format(getString(R.string.do_you_really_want_to_delete_this_witharg), getResources().getQuantityString(R.plurals.documents, itemsToDelete.size())) + "\n\n");

        for (final File f : itemsToDelete) {
            message.append("\n").append(f.getName());
        }

        final WrConfirmDialog confirmDialog = WrConfirmDialog.newInstance(getString(R.string.confirm_delete), message.toString(), itemsToDelete, confirmCallback);
        confirmDialog.show(getChildFragmentManager(), WrConfirmDialog.FRAGMENT_TAG);
    }

    private void askForMoveOrCopy(final boolean isMove) {
        final List<File> files = new ArrayList<>(_filesystemViewerAdapter.getCurrentSelection());
        MarkorFileBrowserFactory.showFolderDialog(new GsFileBrowserOptions.SelectionListenerAdapter() {

            @Override
            public void onFsViewerSelected(String request, File file, Integer lineNumber) {
                super.onFsViewerSelected(request, file, null);
                WrMarkorSingleton.getInstance().moveOrCopySelected(files, file, getActivity(), isMove);
                _filesystemViewerAdapter.unselectAll();
                _filesystemViewerAdapter.reloadCurrentFolder();
            }

            @Override
            public void onFsViewerConfig(GsFileBrowserOptions.Options dopt) {
                dopt.titleText = isMove ? R.string.move : R.string.copy;
                dopt.rootFolder = _appSettings.getNotebookDirectory();
                dopt.startFolder = getCurrentFolder();
                // Directories cannot be moved into themselves. Don't give users the option
                final Set<String> selSet = new HashSet<>();
                for (final File f : files) {
                    selSet.add(f.getAbsolutePath());
                }
                dopt.fileOverallFilter = (context, test) -> !selSet.contains(test.getAbsolutePath());
            }

            @Override
            public void onFsViewerCancel(final String request) {
                super.onFsViewerCancel(request);
                _filesystemViewerAdapter.reloadCurrentFolder(); // May be new folders
            }
        }, getChildFragmentManager(), getActivity());
    }

    public void setCurrentFolder(final File folder) {
        if (folder != null && (folder.canRead() || GsFileBrowserListAdapter.isVirtualFolder(folder)) && _filesystemViewerAdapter != null) {
            _filesystemViewerAdapter.setCurrentFolder(folder);
        }
    }

    public GsFileBrowserOptions.Options getOptions() {
        return _dopt;
    }
}
