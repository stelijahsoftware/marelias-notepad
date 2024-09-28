/*#######################################################
 *
 *   Maintained 2018-2024 by Gregor Santner <gsantner AT mailbox DOT org>
 *   License of this file: Apache 2.0
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
#########################################################*/
package net.gsantner.notepad2.format;

import android.content.Context;
import android.text.InputFilter;
import android.text.TextWatcher;

import androidx.annotation.NonNull;
import androidx.annotation.StringRes;

import net.gsantner.notepad2.ApplicationObject;
import net.gsantner.notepad2.R;
//import net.gsantner.notepad2.format.asciidoc.AsciidocActionButtons;
//import net.gsantner.notepad2.format.asciidoc.AsciidocSyntaxHighlighter;
//import net.gsantner.notepad2.format.asciidoc.AsciidocTextConverter;
//import net.gsantner.notepad2.format.binary.EmbedBinaryTextConverter;
//import net.gsantner.notepad2.format.csv.CsvSyntaxHighlighter;
//import net.gsantner.notepad2.format.csv.CsvTextConverter;
//import net.gsantner.notepad2.format.keyvalue.KeyValueSyntaxHighlighter;
//import net.gsantner.notepad2.format.keyvalue.KeyValueTextConverter;
import net.gsantner.notepad2.format.markdown.MarkdownActionButtons;
import net.gsantner.notepad2.format.markdown.MarkdownReplacePatternGenerator;
import net.gsantner.notepad2.format.markdown.MarkdownSyntaxHighlighter;
import net.gsantner.notepad2.format.markdown.MarkdownTextConverter;
//import net.gsantner.notepad2.format.orgmode.OrgmodeActionButtons;
//import net.gsantner.notepad2.format.orgmode.OrgmodeSyntaxHighlighter;
//import net.gsantner.notepad2.format.orgmode.OrgmodeTextConverter;
//import net.gsantner.notepad2.format.plaintext.PlaintextActionButtons;
//import net.gsantner.notepad2.format.plaintext.PlaintextSyntaxHighlighter;
import net.gsantner.notepad2.format.plaintext.PlaintextTextConverter;
//import net.gsantner.notepad2.format.todotxt.TodoTxtActionButtons;
//import net.gsantner.notepad2.format.todotxt.TodoTxtAutoTextFormatter;
//import net.gsantner.notepad2.format.todotxt.TodoTxtSyntaxHighlighter;
//import net.gsantner.notepad2.format.todotxt.TodoTxtTextConverter;
//import net.gsantner.notepad2.format.wikitext.WikitextActionButtons;
//import net.gsantner.notepad2.format.wikitext.WikitextReplacePatternGenerator;
//import net.gsantner.notepad2.format.wikitext.WikitextSyntaxHighlighter;
//import net.gsantner.notepad2.format.wikitext.WikitextTextConverter;
import net.gsantner.notepad2.frontend.textview.AutoTextFormatter;
import net.gsantner.notepad2.frontend.textview.ListHandler;
import net.gsantner.notepad2.frontend.textview.SyntaxHighlighterBase;
import net.gsantner.notepad2.model.AppSettings;
import net.gsantner.notepad2.model.Document;

import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

public class FormatRegistry {
    public static final int FORMAT_PLAIN = R.string.action_format_plaintext;


    public final static MarkdownTextConverter CONVERTER_MARKDOWN = new MarkdownTextConverter();
    public final static PlaintextTextConverter CONVERTER_PLAINTEXT = new PlaintextTextConverter();

    public static class Format {
        public final @StringRes int format, name;
        public final String defaultExtensionWithDot;
        public final TextConverterBase converter;

        public Format(@StringRes final int a_format, @StringRes final int a_name, final String a_defaultFileExtension, final TextConverterBase a_converter) {
            format = a_format;
            name = a_name;
            defaultExtensionWithDot = a_defaultFileExtension;
            converter = a_converter;
        }
    }

    // Order here is used to **determine** format by it's file extension and/or content heading
    public static final List<Format> FORMATS = Arrays.asList(
            new Format(FormatRegistry.FORMAT_PLAIN, R.string.plaintext, ".txt", CONVERTER_PLAINTEXT)
    );

    public static boolean isFileSupported(final File file, final boolean... textOnly) {
        final boolean textonly = textOnly != null && textOnly.length > 0 && textOnly[0];
        if (file != null) {
            final String filepath = file.getAbsolutePath().toLowerCase(Locale.ROOT);
            for (final Format format : FORMATS) {
                if (format.converter != null) {
                    return true;
                }
            }
        }
        return false;
    }

    public interface TextFormatApplier {
        void applyTextFormat(int textFormatId);
    }

    public static FormatRegistry getFormat(int formatId, @NonNull final Context context, final Document document) {
        final FormatRegistry format = new FormatRegistry();
        final AppSettings appSettings = ApplicationObject.settings();

        formatId = FORMAT_PLAIN;
        format._converter = CONVERTER_MARKDOWN;
        format._highlighter = new MarkdownSyntaxHighlighter(appSettings);
        format._textActions = new MarkdownActionButtons(context, document);
        format._autoFormatInputFilter = new AutoTextFormatter(MarkdownReplacePatternGenerator.formatPatterns);
        format._autoFormatTextWatcher = new ListHandler(MarkdownReplacePatternGenerator.formatPatterns);

        format._formatId = formatId;
        return format;
    }

    private ActionButtonBase _textActions;
    private SyntaxHighlighterBase _highlighter;
    private TextConverterBase _converter;
    private InputFilter _autoFormatInputFilter;
    private TextWatcher _autoFormatTextWatcher;
    private int _formatId;

    public ActionButtonBase getActions() {
        return _textActions;
    }

    public TextWatcher getAutoFormatTextWatcher() {
        return _autoFormatTextWatcher;
    }

    public InputFilter getAutoFormatInputFilter() {
        return _autoFormatInputFilter;
    }

    public SyntaxHighlighterBase getHighlighter() {
        return _highlighter;
    }

    public TextConverterBase getConverter() {
        return _converter;
    }

    public int getFormatId() {
        return _formatId;
    }
}
