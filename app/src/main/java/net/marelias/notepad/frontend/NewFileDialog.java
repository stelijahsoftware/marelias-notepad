/*#######################################################
 *
 *   Maintained 2018-2024 by Gregor Santner <gsantner AT mailbox DOT org>
 *
 *   License of this file: Apache 2.0
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
#########################################################*/
package net.marelias.notepad.frontend;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.text.InputFilter;
import android.util.AttributeSet;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListPopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.documentfile.provider.DocumentFile;
import androidx.fragment.app.DialogFragment;

import net.marelias.notepad.ApplicationObject;
import net.marelias.notepad.R;
import net.marelias.notepad.format.FormatRegistry;
import net.marelias.notepad.frontend.textview.HighlightingEditor;
import net.marelias.notepad.frontend.textview.TextViewUtils;
import net.marelias.notepad.model.AppSettings;
import net.marelias.notepad.model.Document;
import net.marelias.notepad.util.MarkorContextUtils;
import net.marelias.opoc.util.GsCollectionUtils;
import net.marelias.opoc.util.GsContextUtils;
import net.marelias.opoc.util.GsFileUtils;
import net.marelias.opoc.wrapper.GsCallback;

import java.io.File;
import java.util.Arrays;
import java.util.List;

public class NewFileDialog extends DialogFragment {
    public static final String FRAGMENT_TAG = NewFileDialog.class.getName();
    public static final String EXTRA_DIR = "EXTRA_DIR";
    public static final String EXTRA_ALLOW_CREATE_DIR = "EXTRA_ALLOW_CREATE_DIR";

    public static final int MAX_TITLE_FORMATS = 10;

    private static final List<Integer> NEW_FILE_FORMATS = Arrays.asList(
            FormatRegistry.FORMAT_PLAIN
    );

    private GsCallback.a1<File> callback;

    public static NewFileDialog newInstance(
            final File sourceFile,
            final boolean allowCreateDir,
            final GsCallback.a1<File> callback
    ) {
        NewFileDialog dialog = new NewFileDialog();
        Bundle args = new Bundle();
        args.putSerializable(EXTRA_DIR, sourceFile);
        args.putSerializable(EXTRA_ALLOW_CREATE_DIR, allowCreateDir);
        dialog.setArguments(args);
        dialog.callback = callback;
        return dialog;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        final File file = (File) getArguments().getSerializable(EXTRA_DIR);
        final boolean allowCreateDir = getArguments().getBoolean(EXTRA_ALLOW_CREATE_DIR);
        final LayoutInflater inflater = LayoutInflater.from(getActivity());
        return makeDialog(file, allowCreateDir, inflater);
    }

    @SuppressLint("SetTextI18n")
    private AlertDialog makeDialog(final File basedir, final boolean allowCreateDir, LayoutInflater inflater) {
        final Activity activity = getActivity();
        final AppSettings appSettings = ApplicationObject.settings();
        final AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(inflater.getContext(), R.style.Theme_AppCompat_DayNight_Dialog_Rounded);
        final View root = inflater.inflate(R.layout.new_file_dialog, null);

        final EditText titleEdit = root.findViewById(R.id.new_file_dialog__name);
        final EditText formatEdit = root.findViewById(R.id.new_file_dialog__name_format);
        final TextView formatSpinner = root.findViewById(R.id.new_file_dialog__name_format_spinner);

        titleEdit.requestFocus();
        new Handler().postDelayed(new GsContextUtils.DoTouchView(titleEdit), 200);

        titleEdit.setFilters(new InputFilter[]{GsContextUtils.instance.makeFilenameInputFilter()});

        // Build a list of available formats
        // -----------------------------------------------------------------------------------------
        final List<FormatRegistry.Format> formats = GsCollectionUtils.map(
                NEW_FILE_FORMATS, t -> GsCollectionUtils.selectFirst(FormatRegistry.FORMATS, f -> f.format == t));

        // Setup title format spinner and actions
        // -----------------------------------------------------------------------------------------
        final ArrayAdapter<String> formatAdapter = new ArrayAdapter<String>(activity, android.R.layout.simple_spinner_dropdown_item) {
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                View view = super.getView(position, convertView, parent);

                TextView textView = (TextView) view.findViewById(android.R.id.text1);

                String item = getItem(position);
                if (item != null && item.equals(formatEdit.getText().toString())) {
                    textView.setTypeface(null, Typeface.BOLD);
                } else {
                    textView.setTypeface(null, Typeface.NORMAL);
                }

                return view;
            }
        };

        formatAdapter.addAll(appSettings.getTitleFormats());

        final ListPopupWindow formatPopup = new ListPopupWindow(activity);
        formatPopup.setAdapter(formatAdapter);
        formatPopup.setAnchorView(formatEdit);

        formatEdit.setText(formatAdapter.getItem(0));
        formatPopup.setOnItemClickListener((parent, view, position, id) -> {
            String selectedItem = formatAdapter.getItem(position);
            formatEdit.setText(selectedItem);
            formatPopup.dismiss();
            formatAdapter.notifyDataSetChanged();
        });

        formatSpinner.setOnClickListener(v -> formatPopup.show());
        formatEdit.setOnClickListener(v -> formatPopup.show());

        // Setup template spinner and action
        // -----------------------------------------------------------------------------------------
        final List<Pair<String, String>> templates = appSettings.getBuiltinTemplates();

        dialogBuilder.setView(root);

        // Setup button click actions
        // -----------------------------------------------------------------------------------------

        final GsCallback.s0 getTitle = () -> {
            final String title = titleEdit.getText().toString().trim();

            // Elyahw: This is what is done on the press of the okay button in new file dialog. For options see [AppSettings.java]
            String format = formatEdit.getText().toString().trim();

            if (title.isEmpty())//format.isEmpty() &&
            {
                format = "`yyyyMMdd'_'hhMMss`"; // the rest of options are in AppSettings.java
            }
            else if (!title.isEmpty() && !format.contains("{{title}}"))
            {
                format += "_{{title}}";
            }

            return TextViewUtils.interpolateSnippet(format, title, "").trim();
        };

        final GsCallback.s0 getTitle_folder = () -> {
            final String title = titleEdit.getText().toString().trim();
            String format = formatEdit.getText().toString().trim();
            if (title.isEmpty())//format.isEmpty() &&
            {
                format = "`yyyyMMdd'_'hhMMss`"; // the rest of options are in AppSettings.java
            }
            else
            {
                format = "{{title}}";
            }
            return TextViewUtils.interpolateSnippet(format, title, "").trim();
        };

        final MarkorContextUtils cu = new MarkorContextUtils(getContext());
        dialogBuilder.setNegativeButton(R.string.cancel, (dialogInterface, i) -> dialogInterface.dismiss());
        dialogBuilder.setPositiveButton(getString(android.R.string.ok), (dialogInterface, i) -> {
            // final FormatRegistry.Format fmt = formats.get(typeSpinner.getSelectedItemPosition());

            final String title = getTitle.callback();
            String fileName = GsFileUtils.getFilteredFilenameWithoutDisallowedChars(title + ".txt");

            // Get template string
            // -------------------------------------------------------------------------------------
            final String template;
            //template = "";
            template = "-----\nNote Title\n-----\n"; // possible to enter a template here..

            final Pair<String, Integer> content = getTemplateContent(template, title);
            // -------------------------------------------------------------------------------------

            final File file = new File(basedir, fileName);

            // Most of the logic we want is in the document class so we just reuse it
            final Document document = new Document(file);

            // These are done even if the file isn't created
            final String titleFormat = formatEdit.getText().toString().trim();
            final FormatRegistry.Format fmt = formats.get(0);
            appSettings.setNewFileDialogLastUsedType(fmt.format);

            if (!file.exists() || file.length() <= GsContextUtils.TEXTFILE_OVERWRITE_MIN_TEXT_LENGTH) {
                document.saveContent(activity, content.first, cu, true);

                // We only make these changes if the file did not already exist
                appSettings.setDocumentFormat(document.path, fmt.format);
                appSettings.setLastEditPosition(document.path, content.second);
                appSettings.setNewFileDialogLastUsedExtension(".txt");

                callback(file);

            } else if (file.canWrite()) {
                callback(file);
            } else {
                Toast.makeText(activity, R.string.failed_to_create_backup, Toast.LENGTH_LONG).show();
            }

            dialogInterface.dismiss();
        });

        // Folder creation button:
        dialogBuilder.setNeutralButton(R.string.folder, (dialogInterface, i) -> {
            final String title = getTitle_folder.callback();
            final String dirName = GsFileUtils.getFilteredFilenameWithoutDisallowedChars(title);

            final File f = new File(basedir, dirName);

            final String titleFormat = formatEdit.getText().toString().trim();

            if (cu.isUnderStorageAccessFolder(getContext(), f, true)) {
                DocumentFile dof = cu.getDocumentFile(getContext(), f, true);
                if (dof != null && dof.exists()) {
                    callback(f);
                }
            } else if (f.isDirectory() || f.mkdirs()) {
                callback(f);
            }
            dialogInterface.dismiss();
        });

        if (!allowCreateDir) {
            dialogBuilder.setNeutralButton("", null);
        }

        // elyahw (makes no difference)
        // titleEdit.requestFocus();
        /////////////////
        // Initial creation - loop through and set type
        final int lastUsedType = appSettings.getNewFileDialogLastUsedType();
        final List<Integer> indices = GsCollectionUtils.indices(formats, f -> f.format == lastUsedType);

        final AlertDialog dialog = dialogBuilder.show();
        final Window win = dialog.getWindow();
        if (win != null) {
            win.setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT);
            win.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN | WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
        }
        titleEdit.post(titleEdit::requestFocus);
        ///////////////// elyahw
        return dialog;
    }

    private void callback(final File file) {
        try {
            callback.callback(file);
        } catch (Exception ignored) {
        }
    }

    public void setCallback(final GsCallback.a1<File> callback) {
        this.callback = callback;
    }

    private Pair<String, Integer> getTemplateContent(final String template, final String name) {
        String text = TextViewUtils.interpolateSnippet(template, name, "");

        final int startingIndex = text.indexOf(HighlightingEditor.PLACE_CURSOR_HERE_TOKEN);
        text = text.replaceAll(HighlightingEditor.PLACE_CURSOR_HERE_TOKEN, "");

        // Has no utility in a new file
        text = text.replaceAll(HighlightingEditor.INSERT_SELECTION_HERE_TOKEN, "");

        return Pair.create(text, startingIndex);
    }

    public static class ReselectSpinner extends androidx.appcompat.widget.AppCompatSpinner {

        public ReselectSpinner(Context context) {
            super(context);
        }
        public ReselectSpinner(Context context, AttributeSet attrs) {
            super(context, attrs);
        }
        public ReselectSpinner(Context context, AttributeSet attrs, int defStyleAttr) {
            super(context, attrs, defStyleAttr);
        }

        @Override
        public void setSelection(int position, boolean animate) {
            boolean sameSelected = position == getSelectedItemPosition();
            super.setSelection(position, animate);
            if (sameSelected) {
                getOnItemSelectedListener().onItemSelected(this, getSelectedView(), position, getSelectedItemId());
            }
        }

        @Override
        public void setSelection(int position) {
            boolean sameSelected = position == getSelectedItemPosition();
            super.setSelection(position);
            if (sameSelected) {
                getOnItemSelectedListener().onItemSelected(this, getSelectedView(), position, getSelectedItemId());
            }
        }
    }


    public void createNewFile(File basedir, String title, String format, String template) {
        final Activity activity = getActivity();
        final AppSettings appSettings = ApplicationObject.settings();
        final MarkorContextUtils cu = new MarkorContextUtils(getContext());

        title = TextViewUtils.interpolateSnippet(format, title, "").trim();

        String fileName = GsFileUtils.getFilteredFilenameWithoutDisallowedChars(title + ".txt");
        File file = new File(basedir, fileName);

        // Get template string
        final Pair<String, Integer> content = getTemplateContent(template, title);

        final Document document = new Document(file);

        final List<FormatRegistry.Format> formats = GsCollectionUtils.map(
                NEW_FILE_FORMATS, t -> GsCollectionUtils.selectFirst(FormatRegistry.FORMATS, f -> f.format == t));

        // These are done even if the file isn't created
        final FormatRegistry.Format fmt = formats.get(0);
        appSettings.setNewFileDialogLastUsedType(fmt.format);

        if (!file.exists() || file.length() <= GsContextUtils.TEXTFILE_OVERWRITE_MIN_TEXT_LENGTH) {
            document.saveContent(activity, content.first, cu, true);

            // We only make these changes if the file did not already exist
            appSettings.setDocumentFormat(document.path, fmt.format);
            appSettings.setLastEditPosition(document.path, content.second);
            appSettings.setNewFileDialogLastUsedExtension(".txt");

            callback(file);
        } else if (file.canWrite()) {
            callback(file);
        } else {
            Toast.makeText(activity, R.string.failed_to_create_backup, Toast.LENGTH_LONG).show();
        }
    }

    public void createNewFolder(File basedir, String title, String format) {
        final MarkorContextUtils cu = new MarkorContextUtils(getContext());

        title = TextViewUtils.interpolateSnippet(format, title, "").trim();

        final String dirName = GsFileUtils.getFilteredFilenameWithoutDisallowedChars(title);
        final File f = new File(basedir, dirName);

        if (cu.isUnderStorageAccessFolder(getContext(), f, true)) {
            DocumentFile dof = cu.getDocumentFile(getContext(), f, true);
            if (dof != null && dof.exists()) {
                callback(f);
            }
        } else if (f.isDirectory() || f.mkdirs()) {
            callback(f);
        }
    }

    public static void createNewFileCaller(File basedir, GsCallback.a1<File> callback, String title, String format, String template) {
        NewFileDialog dialog = new NewFileDialog();
        dialog.setCallback(callback);

        dialog.createNewFile(basedir, title, format, template);
    }

    public static void createNewFolderCaller(File basedir, GsCallback.a1<File> callback, String title, String format) {
        NewFileDialog dialog = new NewFileDialog();
        dialog.setCallback(callback);

        dialog.createNewFolder(basedir, title, format);
    }




}
