package org.marelias.notepad.frontend;

import android.app.Activity;
import android.content.Context;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.StringRes;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.AppCompatEditText;
import androidx.core.content.ContextCompat;

import org.marelias.notepad.ApplicationObject;
import org.marelias.notepad.R;
import org.marelias.notepad.model.AppSettings;
import org.marelias.opoc.opoc.GsCallback;
import org.marelias.opoc.util.GsContextUtils;

public class FileSearchDialog {
    public static final class Options {
        public boolean enableRegex = true;
        public boolean enableSearchInContent = true;
    }

    public static void showDialog(final Activity activity, final GsCallback.a1<FileSearchEngine.SearchOptions> dialogCallback) {
        showDialog(activity, new Options(), dialogCallback);
    }

    public static void showDialog(
            final Activity activity,
            final FileSearchDialog.Options options,
            final GsCallback.a1<FileSearchEngine.SearchOptions> dialogCallback
    ) {
        final AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(activity, R.style.Theme_AppCompat_Light_Dialog_Rounded);
        final AppSettings appSettings = ApplicationObject.settings();

        final ScrollView scrollView = new ScrollView(activity);
        final LinearLayout dialogLayout = new LinearLayout(activity);
        dialogLayout.setOrientation(LinearLayout.VERTICAL);
        final int dp4px = GsContextUtils.instance.convertDpToPx(activity, 4);
        final int textColor = ContextCompat.getColor(activity, R.color.primary_text);

        final LinearLayout.LayoutParams margins = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        margins.setMargins(dp4px * 5, dp4px, dp4px * 5, dp4px);

        final LinearLayout.LayoutParams subCheckBoxMargins = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        subCheckBoxMargins.setMargins(dp4px * 5 * 2, dp4px, dp4px * 5, dp4px);

//        final TextView messageTextView = new TextView(activity);
        final AppCompatEditText searchEditText = new AppCompatEditText(activity);
        final Spinner queryHistorySpinner = new Spinner(activity);
        final CheckBox regexCheckBox = new CheckBox(activity);
        final CheckBox searchInContentCheckBox = new CheckBox(activity);
//        final CheckBox onlyFirstContentMatchCheckBox = new CheckBox(activity);

        // TextView
//        final String loc = activity.getString(options.searchLocation != 0 ? options.searchLocation : R.string.directory);
//        messageTextView.setText("(case insensitive)");
//        dialogLayout.addView(messageTextView, margins);

        // EdiText: Search query input
        searchEditText.setHint("(Case insensitive)");
        searchEditText.setSingleLine(true);
        searchEditText.setMaxLines(1);
        searchEditText.setTextColor(textColor);
        searchEditText.setHintTextColor((textColor & 0x00FFFFFF) | 0x99000000);
        dialogLayout.addView(searchEditText, margins);

        // <<<<
        // Spinner: History
        if (FileSearchEngine.queryHistory.size() > 0) {
            ArrayAdapter<String> adapter = new ArrayAdapter<>(activity, R.layout.list_group_history_item, FileSearchEngine.queryHistory);
            queryHistorySpinner.setAdapter(adapter);

            queryHistorySpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    String query = (String) parent.getItemAtPosition(position);
                    searchEditText.setText(query);
                    searchEditText.selectAll();
                    searchEditText.requestFocus();
                }

                public void onNothingSelected(AdapterView<?> parent) {
                }
            });

            dialogLayout.addView(queryHistorySpinner);
        }

        // Checkbox: Search in content
        if (options.enableSearchInContent) {
            searchInContentCheckBox.setText("Search file contents");
            searchInContentCheckBox.setChecked(appSettings.isSearchInContent());
            dialogLayout.addView(searchInContentCheckBox, margins);
        }
        else
        {
            searchInContentCheckBox.setChecked(false);
            searchInContentCheckBox.setVisibility(View.GONE);
        }

        // Checkbox: Regex search
        if (options.enableRegex) {
            regexCheckBox.setText("Regex");
            regexCheckBox.setChecked(appSettings.isSearchQueryUseRegex());
            dialogLayout.addView(regexCheckBox, margins);
        } else {
            regexCheckBox.setChecked(false);
            regexCheckBox.setVisibility(View.GONE);
        }

        // ScrollView
        scrollView.addView(dialogLayout);
        scrollView.setScrollbarFadingEnabled(false);


        // Configure dialog
        final AlertDialog dialog = dialogBuilder
                .setTitle("Search for file")
                .setOnCancelListener(null)
                .setNegativeButton(android.R.string.cancel, (dialogInterface, i) -> dialogInterface.dismiss())
                .setView(scrollView)
                .create();

        final GsCallback.a0 submit = () -> {
            final String query = searchEditText.getText().toString();
            if (dialogCallback != null && !TextUtils.isEmpty(query)) {
                FileSearchEngine.SearchOptions opt = new FileSearchEngine.SearchOptions();
                opt.query = query;
                opt.isRegexQuery = regexCheckBox.isChecked();
                opt.isSearchInContent = searchInContentCheckBox.isChecked();
                opt.ignoredDirectories = appSettings.getFileSearchIgnorelist();
                opt.maxSearchDepth = appSettings.getSearchMaxDepth();
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                }
                appSettings.setSearchQueryRegexUsing(opt.isRegexQuery);
                appSettings.setSearchInContent(opt.isSearchInContent);

                dialog.dismiss();
                dialogCallback.callback(opt);
            }
        };

        dialog.setButton(AlertDialog.BUTTON_POSITIVE, activity.getString(android.R.string.ok), (di, i) -> submit.callback());

        // Enter button callback set after creation to get ref to dialog
        searchEditText.setOnKeyListener((keyView, keyCode, keyEvent) -> {
            if ((keyEvent.getAction() == KeyEvent.ACTION_DOWN) && (keyCode == KeyEvent.KEYCODE_ENTER)) {
                submit.callback();
                return true;
            }
            return false;
        });

        dialog.show();

        final Window window = dialog.getWindow();
        if (window != null) {
            window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE | WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
            window.setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT);
        }

        // Request focus and show keyboard after dialog is shown
        searchEditText.post(() -> {
            searchEditText.requestFocus();
            InputMethodManager imm = (InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);
            if (imm != null) {
                imm.showSoftInput(searchEditText, InputMethodManager.SHOW_IMPLICIT);
            }
        });
    }
}
