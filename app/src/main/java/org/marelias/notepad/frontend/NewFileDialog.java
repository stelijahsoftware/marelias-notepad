/*#######################################################
 *
 *   Maintained 2018-2024 by Gregor Santner <gsantner AT mailbox DOT org>
 *
 *   License of this file: Apache 2.0
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
#########################################################*/
package org.marelias.notepad.frontend;

import android.app.Activity;
import android.content.Context;
import android.util.AttributeSet;
import android.util.Pair;
import android.widget.Toast;

import androidx.documentfile.provider.DocumentFile;
import androidx.fragment.app.DialogFragment;

import org.marelias.notepad.ApplicationObject;
import org.marelias.notepad.R;
import org.marelias.notepad.format.FormatRegistry;
import org.marelias.notepad.frontend.textview.HighlightingEditor;
import org.marelias.notepad.frontend.textview.TextViewUtils;
import org.marelias.notepad.model.AppSettings;
import org.marelias.notepad.model.Document;
import org.marelias.notepad.util.MarkorContextUtils;
import org.marelias.opoc.opoc.GsCallback;
import org.marelias.opoc.util.GsCollectionUtils;
import org.marelias.opoc.util.GsContextUtils;
import org.marelias.opoc.util.GsFileUtils;

import java.io.File;
import java.util.Arrays;
import java.util.List;

public class NewFileDialog extends DialogFragment {
    public static final String FRAGMENT_TAG = NewFileDialog.class.getName();

    private static final List<Integer> NEW_FILE_FORMATS = Arrays.asList(
            FormatRegistry.FORMAT_PLAIN
    );

    private GsCallback.a1<File> callback;

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
            Toast.makeText(activity, "Failed to create backup", Toast.LENGTH_LONG).show();
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
