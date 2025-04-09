/*#######################################################
 *
 *   Maintained 2017-2024 by Gregor Santner <gsantner AT mailbox DOT org>
 *   License of this file: Apache 2.0
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
#########################################################*/
package net.marelias.notepad.activity;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.SearchView;
import android.widget.Toast;
import android.content.ClipboardManager;
import android.content.ClipData;

import androidx.annotation.NonNull;

import net.marelias.notepad.ApplicationObject;
import net.marelias.notepad.R;
import net.marelias.notepad.format.ActionButtonBase;
import net.marelias.notepad.format.FormatRegistry;
import net.marelias.notepad.frontend.DraggableScrollbarScrollView;
import net.marelias.notepad.frontend.textview.HighlightingEditor;
import net.marelias.notepad.frontend.textview.TextViewUtils;
import net.marelias.notepad.model.Document;
import net.marelias.notepad.util.MarkorContextUtils;
import net.marelias.notepad.util.MarkorWebViewClient;
import net.marelias.opoc.frontend.GsFontPreferenceCompat;
import net.marelias.opoc.opoc.TextViewUndoRedo;
import net.marelias.opoc.util.GsContextUtils;
import net.marelias.opoc.util.GsWebViewChromeClient;
import net.marelias.opoc.opoc.GsTextWatcherAdapter;

import java.io.File;

@SuppressWarnings({"UnusedReturnValue"})
@SuppressLint("NonConstantResourceId")
public class DocumentEditAndViewFragment extends MarkorBaseFragment implements FormatRegistry.TextFormatApplier {
    public static final String FRAGMENT_TAG = "DocumentEditAndViewFragment";
    public static final String SAVESTATE_DOCUMENT = "DOCUMENT";
    public static final String START_PREVIEW = "START_PREVIEW";

    public static DocumentEditAndViewFragment newInstance(final @NonNull Document document, final Integer lineNumber, final Boolean preview) {
        DocumentEditAndViewFragment f = new DocumentEditAndViewFragment();
        Bundle args = new Bundle();
        args.putSerializable(Document.EXTRA_DOCUMENT, document);
        if (lineNumber != null) {
            args.putInt(Document.EXTRA_FILE_LINE_NUMBER, lineNumber);
        }
        if (preview != null) {
            args.putBoolean(START_PREVIEW, preview);
        }
        f.setArguments(args);
        return f;
    }

    private HighlightingEditor _hlEditor;
    private WebView _webView;
    private MarkorWebViewClient _webViewClient;

    private DraggableScrollbarScrollView _primaryScrollView; // elyahw: This is the view which can be scrolled, not the scrollbar itself
    //private HorizontalScrollView _hsView;
    private SearchView _menuSearchViewForViewMode;
    private Document _document;
    private FormatRegistry _format;
    private MarkorContextUtils _cu;
    private TextViewUndoRedo _editTextUndoRedoHelper;
    private MenuItem _saveMenuItem, _undoMenuItem, _redoMenuItem;
    private boolean _isPreviewVisible;

    public DocumentEditAndViewFragment() {
        super();
    }

    @Override
    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        final Bundle args = getArguments();
        if (savedInstanceState != null && savedInstanceState.containsKey(SAVESTATE_DOCUMENT)) {
            _document = (Document) savedInstanceState.getSerializable(SAVESTATE_DOCUMENT);
        } else if (args != null && args.containsKey(Document.EXTRA_DOCUMENT)) {
            _document = (Document) args.get(Document.EXTRA_DOCUMENT);
        }
    }

    @Override
    protected int getLayoutResId() {
        return R.layout.document__fragment__edit;
    }

    @SuppressLint({"SetJavaScriptEnabled", "WrongConstant", "AddJavascriptInterface", "JavascriptInterface"})
    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        final Activity activity = getActivity();

        _hlEditor = view.findViewById(R.id.document__fragment__edit__highlighting_editor);
        // _textActionsBar = view.findViewById(R.id.document__fragment__edit__text_actions_bar);
        _webView = view.findViewById(R.id.document__fragment_view_webview);
        _primaryScrollView = view.findViewById(R.id.document__fragment__edit__content_editor__scrolling_parent);
        // _primaryScrollView.setScrollbarFadingEnabled(true); // set instead in DraggableScrollbarScrollView
        _primaryScrollView.setBackgroundColor(Color.BLACK); // elyahw: colour at bottom of screen when scrolling and releasing
        _cu = new MarkorContextUtils(activity);

        // Using `if (_document != null)` everywhere is dangerous
        // It may cause reads or writes to _silently fail_
        // Instead we try to create it, and exit if that isn't possible
        if (isStateBad()) {
            Toast.makeText(activity, R.string.error_could_not_open_file, Toast.LENGTH_LONG).show();
            if (activity != null) {
                activity.finish();
            }
            return;
        }

        if (_appSettings.getSetWebViewFulldrawing() && Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            WebView.enableSlowWholeDocumentDraw();
        }

        _webViewClient = new MarkorWebViewClient(_webView, activity);
        _webView.setWebChromeClient(new GsWebViewChromeClient(_webView, activity, view.findViewById(R.id.document__fragment_fullscreen_overlay)));
        _webView.setWebViewClient(_webViewClient);
        _webView.addJavascriptInterface(this, "Android");
        _webView.setBackgroundColor(Color.TRANSPARENT);
        WebSettings webSettings = _webView.getSettings();
        webSettings.setBuiltInZoomControls(true);
        webSettings.setDisplayZoomControls(false);
        webSettings.setTextZoom((int) (_appSettings.getViewFontSize() / 15.7f * 100f));
        webSettings.setCacheMode(WebSettings.LOAD_CACHE_ELSE_NETWORK);
        webSettings.setDatabaseEnabled(true);
        webSettings.setGeolocationEnabled(false);
        webSettings.setJavaScriptEnabled(true);
        webSettings.setDomStorageEnabled(true);
        webSettings.setAllowFileAccess(true);
        webSettings.setAllowContentAccess(true);
        webSettings.setAllowFileAccessFromFileURLs(true);
        webSettings.setAllowUniversalAccessFromFileURLs(false);
        webSettings.setMediaPlaybackRequiresUserGesture(false);

//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT && BuildConfig.DEBUG) { // && BuildConfig.IS_TEST_BUILD
//            WebView.setWebContentsDebuggingEnabled(true); // Inspect on computer chromium browser: chrome://inspect/#devices
//        }

        // Upon construction, the document format has been determined from extension etc
        // Here we replace it with the last saved format.
        applyTextFormat(_appSettings.getDocumentFormat(_document.path, _document.getFormat()));

        if (activity instanceof DocumentActivity) {
            ((DocumentActivity) activity).setDocumentTitle(_document.title);
        }

        // Preview mode set before loadDocument to prevent flicker
        final Bundle args = getArguments();
        final boolean startInPreview = _appSettings.getDocumentPreviewState(_document.path);

        _hlEditor.setSaveInstanceState(false); // We will reload from disk
        _document.resetChangeTracking(); // force next reload
        loadDocument();

        // If not set by loadDocument, se the undo-redo helper here
        if (_editTextUndoRedoHelper == null) {
            _editTextUndoRedoHelper = new TextViewUndoRedo(_hlEditor);
        }

        // Configure the editor. Doing so after load helps prevent some errors
        // ---------------------------------------------------------
        _hlEditor.setLineSpacing(0, 1);
        _hlEditor.setTextSize(TypedValue.COMPLEX_UNIT_SP, _appSettings.getDocumentFontSize(_document.path));
        _hlEditor.setTypeface(GsFontPreferenceCompat.typeface(getContext(), _appSettings.getFontFamily(), Typeface.NORMAL));
//        _hlEditor.setBackgroundColor(_appSettings.getEditorBackgroundColor());
        _hlEditor.setTextColor(_appSettings.getEditorForegroundColor());
        _hlEditor.setGravity(_appSettings.isEditorStartEditingInCenter() ? Gravity.CENTER : Gravity.NO_GRAVITY);
        _hlEditor.setHighlightingEnabled(_appSettings.getDocumentHighlightState(_document.path, _hlEditor.getText()));
        _hlEditor.setLineNumbersEnabled(_appSettings.getDocumentLineNumbersEnabled(_document.path));
        _hlEditor.setAutoFormatEnabled(_appSettings.getDocumentAutoFormatEnabled(_document.path));
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // Do not need to send contents to accessibility
            _hlEditor.setImportantForAccessibility(View.IMPORTANT_FOR_AUTOFILL_NO_EXCLUDE_DESCENDANTS);
        }

        // Various settings
        updateMenuToggleStates(0);
        // ---------------------------------------------------------

        final Runnable debounced = TextViewUtils.makeDebounced(500, () -> {
            checkTextChangeState();
            updateUndoRedoIconStates();
        });
        _hlEditor.addTextChangedListener(GsTextWatcherAdapter.after(s -> debounced.run()));

        // We set the keyboard to be hidden if it was hidden when we lost focus
        // This works well to preserve keyboard state.
        if (activity != null) {
            final Window window = activity.getWindow();
            final int adjustResize = WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE;
            final int unchanged = WindowManager.LayoutParams.SOFT_INPUT_STATE_UNCHANGED | adjustResize;
            final int hidden = WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN | adjustResize;
            final int shown = WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE | adjustResize;

            _hlEditor.getViewTreeObserver().addOnWindowFocusChangeListener(hasFocus -> {
                if (hasFocus) {
                    // Restore old state
                    _hlEditor.postDelayed(() -> window.setSoftInputMode(unchanged), 500);
                } else {
                    final Boolean isOpen = TextViewUtils.isImeOpen(_hlEditor);
                    if (isOpen != null) {
                        window.setSoftInputMode(isOpen ? shown : hidden);
                    }
                }
            });
        }
    }

    @Override
    protected void onFragmentFirstTimeVisible() {
        final Bundle args = getArguments();

        int startPos = _appSettings.getLastEditPosition(_document.path, _hlEditor.length());
        if (args != null && args.containsKey(Document.EXTRA_FILE_LINE_NUMBER)) {
            final int lno = args.getInt(Document.EXTRA_FILE_LINE_NUMBER);
            if (lno >= 0) {
                startPos = TextViewUtils.getIndexFromLineOffset(_hlEditor.getText(), lno, 0);
            } else if (lno == Document.EXTRA_FILE_LINE_NUMBER_LAST) {
                startPos = _hlEditor.length();
            }
        }

        _primaryScrollView.invalidate();
        // Can affect layout so run before setting scroll position
        _hlEditor.recomputeHighlighting();

        TextViewUtils.setSelectionAndShow(_hlEditor, startPos);
    }

    @Override
    public void onResume() {
        loadDocument();
        _webView.onResume();
        super.onResume();
    }

    @Override
    public void onPause() {
        saveDocument(false);
        _webView.onPause();
//        _appSettings.addRecentFile(_document.file);
        _appSettings.setDocumentPreviewState(_document.path, _isPreviewVisible);
        _appSettings.setLastEditPosition(_document.path, TextViewUtils.getSelection(_hlEditor)[0]);
        super.onPause();
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        outState.putSerializable(SAVESTATE_DOCUMENT, _document);
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.document__edit__menu, menu);
        _cu.tintMenuItems(menu, true, Color.WHITE);
        _cu.setSubMenuIconsVisibility(menu, true);

//        final boolean isExperimentalFeaturesEnabled = _appSettings.isExperimentalFeaturesEnabled();
        final boolean isText = true; //!_document.isBinaryFileNoTextLoading();

        menu.findItem(R.id.action_undo).setVisible(isText && _appSettings.isEditorHistoryEnabled());
        menu.findItem(R.id.action_redo).setVisible(isText && _appSettings.isEditorHistoryEnabled());

        // Undo / Redo / Save (keep visible, but deactivated and tinted grey if not executable)
        _undoMenuItem = menu.findItem(R.id.action_undo).setVisible(isText && !_isPreviewVisible);
        _redoMenuItem = menu.findItem(R.id.action_redo).setVisible(isText && !_isPreviewVisible);
        _saveMenuItem = menu.findItem(R.id.action_save).setVisible(isText && !_isPreviewVisible);

        menu.findItem(R.id.paste_text).setVisible(isText && !_isPreviewVisible);

        // Edit / Preview switch
        menu.findItem(R.id.action_search).setVisible(isText && !_isPreviewVisible);
        menu.findItem(R.id.action_search_view).setVisible(isText && _isPreviewVisible);
//        menu.findItem(R.id.submenu_per_file_settings).setVisible(isText);

        // SearchView (View Mode)
        _menuSearchViewForViewMode = (SearchView) menu.findItem(R.id.action_search_view).getActionView();
        if (_menuSearchViewForViewMode != null) {
            _menuSearchViewForViewMode.setSubmitButtonEnabled(true);
            _menuSearchViewForViewMode.setQueryHint(getString(R.string.search));
            _menuSearchViewForViewMode.setOnQueryTextFocusChangeListener((v, searchHasFocus) -> {
                if (!searchHasFocus) {
                    _menuSearchViewForViewMode.setQuery("", false);
                    _menuSearchViewForViewMode.setIconified(true);
                }
            });
            _menuSearchViewForViewMode.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
                @Override
                public boolean onQueryTextSubmit(String text) {
                    _webView.findNext(true);
                    return true;
                }

                @Override
                public boolean onQueryTextChange(String text) {
                    _webView.findAllAsync(text);
                    return true;
                }
            });
        }

        // Set various initial states
        updateMenuToggleStates(_document.getFormat());
        checkTextChangeState();
        updateUndoRedoIconStates();
    }

    @Override
    public boolean onReceiveKeyPress(int keyCode, KeyEvent event) {
        if (event.isCtrlPressed()) {
            if (event.isShiftPressed() && keyCode == KeyEvent.KEYCODE_Z) {
                if (_editTextUndoRedoHelper != null && _editTextUndoRedoHelper.getCanRedo()) {
                    _hlEditor.withAutoFormatDisabled(_editTextUndoRedoHelper::redo);
                    updateUndoRedoIconStates();
                }
                return true;
            } else if (keyCode == KeyEvent.KEYCODE_S) {
                saveDocument(true);
                return true;
            } else if (keyCode == KeyEvent.KEYCODE_Y) {
                if (_editTextUndoRedoHelper != null && _editTextUndoRedoHelper.getCanRedo()) {
                    _hlEditor.withAutoFormatDisabled(_editTextUndoRedoHelper::redo);
                    updateUndoRedoIconStates();
                }
                return true;
            } else if (keyCode == KeyEvent.KEYCODE_Z) {
                if (_editTextUndoRedoHelper != null && _editTextUndoRedoHelper.getCanUndo()) {
                    _hlEditor.withAutoFormatDisabled(_editTextUndoRedoHelper::undo);
                    updateUndoRedoIconStates();
                }
                return true;
            } else if (keyCode == KeyEvent.KEYCODE_SLASH) {
                return true;
            }
        }

        return false;
    }

    private void updateUndoRedoIconStates() {
        Drawable d;
        final boolean canUndo = _editTextUndoRedoHelper != null && _editTextUndoRedoHelper.getCanUndo();
        if (_undoMenuItem != null && _undoMenuItem.isEnabled() != canUndo && (d = _undoMenuItem.setEnabled(canUndo).getIcon()) != null) {
            d.mutate().setAlpha(canUndo ? 255 : 40);
        }

        final boolean canRedo = _editTextUndoRedoHelper != null && _editTextUndoRedoHelper.getCanRedo();
        if (_redoMenuItem != null && _redoMenuItem.isEnabled() != canRedo && (d = _redoMenuItem.setEnabled(canRedo).getIcon()) != null) {
            d.mutate().setAlpha(canRedo ? 255 : 40);
        }
    }

    public boolean loadDocument() {
        if (isSdStatusBad() || isStateBad()) {
            errorClipText();
            return false;
        }

        // Only trigger the load process if constructing or file updated or force reload
        if (_document.hasFileChangedSinceLastLoad()) {

            final String content = _document.loadContent(getContext());
            if (content == null) {
                errorClipText();
                return false;
            }

            if (!_document.isContentSame(_hlEditor.getText())) {

                final int[] sel = TextViewUtils.getSelection(_hlEditor);
                sel[0] = Math.min(sel[0], content.length());
                sel[1] = Math.min(sel[1], content.length());

                if (_editTextUndoRedoHelper != null) {
                    _editTextUndoRedoHelper.disconnect();
                    _editTextUndoRedoHelper.clearHistory();
                }

                _hlEditor.withAutoFormatDisabled(() -> _hlEditor.setText(content));

                if (_editTextUndoRedoHelper == null) {
                    _editTextUndoRedoHelper = new TextViewUndoRedo(_hlEditor);
                } else {
                    _editTextUndoRedoHelper.setTextView(_hlEditor);
                }

                TextViewUtils.setSelectionAndShow(_hlEditor, sel);
            }
            checkTextChangeState();

            return true;
        }
        return false;
    }

    private void pasteFromClipboard() {
        // Get the ClipboardManager
        ClipboardManager clipboard = (ClipboardManager) getActivity().getSystemService(Context.CLIPBOARD_SERVICE);
        if (clipboard == null || !clipboard.hasPrimaryClip()) {
            // No clipboard data
            return;
        }

        // Get the text from the clipboard
        ClipData clip = clipboard.getPrimaryClip();
        if (clip != null && clip.getItemCount() > 0) {
            ClipData.Item item = clip.getItemAt(0);
            String pasteText = item.getText().toString();

            // Insert the text into the EditText
            if (pasteText != null && _hlEditor != null) {
                int start = Math.max(_hlEditor.getSelectionStart(), 0);
                int end = Math.max(_hlEditor.getSelectionEnd(), 0);
                _hlEditor.getText().replace(Math.min(start, end), Math.max(start, end), pasteText);
            }
        }
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull final MenuItem item) {
        final Activity activity = getActivity();
        if (activity == null) {
            return true;
        }

        final int itemId = item.getItemId();
        switch (itemId) {
            case R.id.action_undo: {
                if (_editTextUndoRedoHelper != null && _editTextUndoRedoHelper.getCanUndo()) {
                    _hlEditor.withAutoFormatDisabled(_editTextUndoRedoHelper::undo);
                    updateUndoRedoIconStates();
                }
                return true;
            }
            case R.id.action_redo: {
                if (_editTextUndoRedoHelper != null && _editTextUndoRedoHelper.getCanRedo()) {
                    _hlEditor.withAutoFormatDisabled(_editTextUndoRedoHelper::redo);
                    updateUndoRedoIconStates();
                }
                return true;
            }
            case R.id.action_save: {
                saveDocument(true);
                // touch parent (elyahw)
                return true;
            }
            case R.id.paste_text: {
                pasteFromClipboard();
                return true;
            }

//            case R.id.action_preview_edit_toggle: {
//                setViewModeVisibility(!_isPreviewVisible);
//                return true;
//            }
            case R.string.action_format_plaintext:
            case R.string.action_format_markdown: {
                if (itemId != _document.getFormat()) {
                    _document.setFormat(itemId);
                    applyTextFormat(itemId);
                    _appSettings.setDocumentFormat(_document.path, _document.getFormat());
                }
                return true;
            }
            case R.id.action_search: {
                _format.getActions().onSearch();
                return true;
            }
            case R.id.action_line_numbers: {
                final boolean newState = !_hlEditor.getLineNumbersEnabled();
                _appSettings.setDocumentLineNumbersEnabled(_document.path, newState);
                _hlEditor.setLineNumbersEnabled(newState);
                updateMenuToggleStates(0);
                return true;
            }
            // Create custom font size for a specific file
//            case R.id.action_set_font_size: {
//                MarkorDialogFactory.showFontSizeDialog(activity, _appSettings.getDocumentFontSize(_document.path), (newSize) -> {
//                    _hlEditor.setTextSize(TypedValue.COMPLEX_UNIT_SP, (float) newSize);
//                    _appSettings.setDocumentFontSize(_document.path, newSize);
//                });
//                return true;
//            }

            default: {
                return super.onOptionsItemSelected(item);
            }
        }
    }

    public void checkTextChangeState() {
        final boolean isTextChanged = !_document.isContentSame(_hlEditor.getText());
        Drawable d;

        if (_saveMenuItem != null && _saveMenuItem.isEnabled() != isTextChanged && (d = _saveMenuItem.setEnabled(isTextChanged).getIcon()) != null) {
            d.mutate().setAlpha(isTextChanged ? 255 : 40);
        }
    }

    @Override
    public void applyTextFormat(final int textFormatId) {
        final Activity activity = getActivity();
        if (activity == null) {
            return;
        }
        _format = FormatRegistry.getFormat(textFormatId, activity, _document);
        _document.setFormat(_format.getFormatId());
        _hlEditor.setHighlighter(_format.getHighlighter());
        _hlEditor.setDynamicHighlightingEnabled(_appSettings.isDynamicHighlightingEnabled());
        _hlEditor.setAutoFormatters(_format.getAutoFormatInputFilter(), _format.getAutoFormatTextWatcher());
        _hlEditor.setAutoFormatEnabled(_appSettings.getDocumentAutoFormatEnabled(_document.path));
        _format.getActions()
                .setDocument(_document) // elyahw line added by merge request
                .setUiReferences(activity, _hlEditor, _webView);

        updateMenuToggleStates(_format.getFormatId());

    }

    private void updateMenuToggleStates(final int selectedFormatActionId) {
        MenuItem mi;
        if ((mi = _fragmentMenu.findItem(R.id.action_line_numbers)) != null) {
            mi.setChecked(_hlEditor.getLineNumbersEnabled());
        }
    }

    @Override
    public String getFragmentTag() {
        return FRAGMENT_TAG;
    }

    public void errorClipText() {
        final String text = getTextString();
        if (!TextUtils.isEmpty(text)) {
            Context context = getContext();
            context = context == null ? ApplicationObject.get().getApplicationContext() : context;
            new MarkorContextUtils(context).setClipboard(getContext(), text);
        }
        // Always show error message
        Toast.makeText(getContext(), R.string.error_could_not_open_file, Toast.LENGTH_LONG).show();
        Log.i(DocumentEditAndViewFragment.class.getName(), "Triggering error text clipping");
    }

    public boolean isSdStatusBad() {
        if (_cu.isUnderStorageAccessFolder(getContext(), _document.file, false) &&
                _cu.getStorageAccessFrameworkTreeUri(getContext()) == null) {
            _cu.showMountSdDialog(getActivity());
            return true;
        }
        return false;
    }

    // Checks document state if things aren't in a good state
    public boolean isStateBad() {
        return (_document == null ||
                _hlEditor == null ||
                _appSettings == null ||
                !_cu.canWriteFile(getContext(), _document.file, false, true));
    }

    // Save the file
    public boolean saveDocument(final boolean forceSaveEmpty) {
        final Activity activity = getActivity();
        if (activity == null || isSdStatusBad() || isStateBad()) {
            errorClipText();
            return false;
        }

        // Document is written iff writeable && content has changed
        final CharSequence text = _hlEditor.getText();
        if (!_document.isContentSame(text))
        {
            // Touch parent folder on edit (elyahw) ----------
            try {
                File parentFolder = _document.file.getParentFile();
                File rootFolder = new File(_appSettings.getNotebookDirectory().getPath());
                File homePath = new File("/storage/emulated/0/");

                //File ff = _document.file;
                //String ppath = "";
                //ppath = ff.getAbsolutePath();

                // no logging, just print to terminal:
                //System.out.println(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>> Touching parent folder.........................................\n");
                //System.out.println(parentFolder.getPath()); // "/storage/emulated/0/Documents/mar-elias notepad/aaa/abcd/fda"
                //System.out.println(_appSettings.getNotebookDirectory().getAbsolutePath()); // "/storage/emulated/0/Documents/mar-elias notepad"
                //System.out.println(_appSettings.getNotebookDirectory().getPath()); // same

                long currentTime = System.currentTimeMillis();

                // Traverse up the directory hierarchy and touch until root folder is reached
                while (parentFolder != null &&
                        !parentFolder.equals(rootFolder) &&
                        !parentFolder.equals(homePath)) {
                    parentFolder.setLastModified(currentTime);
                    parentFolder = parentFolder.getParentFile();
                }
            }
            catch (Exception ignored) {
                Log.i("Elyahw", "Exception touch parent folder.."); // Logcat
            }
            // ------------------------------------------------

            final int minLength = GsContextUtils.TEXTFILE_OVERWRITE_MIN_TEXT_LENGTH;
            if (!forceSaveEmpty && text != null && text.length() < minLength)
            {
                final String message = activity.getString(R.string.wont_save_min_length, minLength);
                Toast.makeText(activity, message, Toast.LENGTH_SHORT).show();

                return true;
            }
            if (_document.saveContent(getActivity(), text, _cu, forceSaveEmpty))
            {
                checkTextChangeState();
                return true;
            }
            else
            {
                errorClipText();
                return false; // Failure only if saveContent somehow fails
            }
        }
        else
        {
            return true; // Report success if text not changed
        }
    }

    @Override
    protected void onToolbarClicked(View v) {
        if (_format != null) {
            _format.getActions().runTitleClick();
        }
    }

    @Override
    protected boolean onToolbarLongClicked(View v) {
        if (isVisible() && isResumed()) {
            _format.getActions().runJumpBottomTopAction(_isPreviewVisible ? ActionButtonBase.ActionItem.DisplayMode.VIEW : ActionButtonBase.ActionItem.DisplayMode.EDIT);
            return true;
        }
        return false;
    }

    @Override
    public void onDestroy() {
        try {
            _webView.loadUrl("about:blank");
            _webView.destroy();
        } catch (Exception ignored) {
        }
        super.onDestroy();
    }

    public Document getDocument() {
        return _document;
    }

    public String getTextString() {
        final CharSequence text = _hlEditor != null ? _hlEditor.getText() : null;
        return text != null ? text.toString() : "";
    }
}
