/*#######################################################
 *
 *   Maintained 2018-2024 by Gregor Santner <gsantner AT mailbox DOT org>
 *   License of this file: Apache 2.0
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
#########################################################*/
package net.gsantner.notepad2.format.markdown;

import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;

import net.gsantner.notepad2.frontend.textview.SyntaxHighlighterBase;
import net.gsantner.notepad2.model.AppSettings;

import java.util.regex.Pattern;

import other.writeily.format.markdown.WrMarkdownHeaderSpanCreator;

public class MarkdownSyntaxHighlighter extends SyntaxHighlighterBase {

    public final static Pattern BOLD = Pattern.compile("(?<=(\\n|^|\\s|\\[|\\{|\\())(([*_]){2,3})(?=\\S)(.*?)\\S\\2(?=(\\n|$|\\s|\\.|,|:|;|-|\\]|\\}|\\)))");
    public final static Pattern ITALICS = Pattern.compile("(?<=(\\n|^|\\s|\\[|\\{|\\())([*_])(?=((?!\\2)|\\2{2,}))(?=\\S)(.*?)\\S\\2(?=(\\n|$|\\s|\\.|,|:|;|-|\\]|\\}|\\)))");
    public final static Pattern HEADING = Pattern.compile("(?m)((^#{1,6}[^\\S\\n][^\\n]+)|((\\n|^)[^\\s]+.*?\\n(-{2,}|={2,})[^\\S\\n]*$))");
    public final static Pattern HEADING_SIMPLE = Pattern.compile("(?m)^(#{1,6}\\s.*$)");
    public final static Pattern LINK = Pattern.compile("\\[([^\\[]+)\\]\\(([^\\)]+)\\)");
    public final static Pattern LIST_UNORDERED = Pattern.compile("(\\n|^)\\s{0,16}([*+-])( \\[[ xX]\\])?(?= )");
    public final static Pattern LIST_ORDERED = Pattern.compile("(?m)^\\s{0,16}(\\d+)(:?\\.|\\))\\s");
    public final static Pattern QUOTATION = Pattern.compile("(\\n|^)>");
    public final static Pattern STRIKETHROUGH = Pattern.compile("~{2}(.*?)\\S~{2}");
    public final static Pattern CODE = Pattern.compile("(?m)(`(?!`)(.*?)`)|(^[^\\S\\n]{4}(?![0-9\\-*+]).*$)");
    public final static Pattern DOUBLESPACE_LINE_ENDING = Pattern.compile("(?m)(?<=\\S)([^\\S\\n]{2,})\\n");
    public final static Pattern ACTION_LINK_PATTERN = Pattern.compile("(?m)\\[(.*?)\\]\\((.*?)\\)");

    private static final int MD_COLOR_HEADING = 0xffef6D00;
    private static final int MD_COLOR_LINK = 0xff1ea3fe;
    private static final int MD_COLOR_LIST = 0xffdaa521;
    private static final int MD_COLOR_QUOTE = 0xff88b04c;
    private static final int MD_COLOR_CODEBLOCK = 0x448c8c8c;

    // Elyahw colours: (from Kate highlights)
    private static final int elyahw_colour_red = Color.parseColor("#de0303");
    private static final int elyahw_colour_red_bright = Color.parseColor("#ff0000");
    private static final int elyahw_colour_green = Color.parseColor("#008f00");
    private static final int elyahw_colour_blue = Color.parseColor("#0080ff");
    private static final int elyahw_colour_blue_dark = Color.parseColor("#1603ff");
    private static final int elyahw_colour_blue_bright = Color.parseColor("#0080ff");
    private static final int elyahw_colour_orange = Color.parseColor("#ff8000");
    private static final int elyahw_colour_yellow = Color.parseColor("#ffff00");
    private static final int elyahw_colour_purple = Color.parseColor("#ff00ff");
    /*
    from Kate:
        <itemData name="TaskDone" strikeOut="1" bold="1" backgroundColor="#eeeeee" /> <!-- Note: strikeOut not strikeout -->
        <itemData name="CodeSegment" color='#000000' backgroundColor='#eaeaea' italic="1"/>
     */

    // Elyahw regexes: reference: https://docs.oracle.com/javase/8/docs/api/java/util/regex/Pattern.html
    public final static Pattern elyahw_comment_cpp = Pattern.compile("^(\\/\\/.+)$", Pattern.MULTILINE); // (?<!:) ignore :// for website links // you need multiline to match ^
    public final static Pattern elyahw_link = Pattern.compile("((h|H)ttps?):\\/\\/\\S+ ");
    public final static Pattern elyahw_numbers = Pattern.compile("[0-9]");
    public final static Pattern elyahw_comment_python = Pattern.compile("^\\#[^(\\s|#)].*$", Pattern.MULTILINE); // overriden by orange
    public final static Pattern elyahw_priority_high = Pattern.compile("^\\[(h|H)\\]", Pattern.MULTILINE);
    public final static Pattern elyahw_priority_med = Pattern.compile("^\\[(m|M)\\]", Pattern.MULTILINE);
    public final static Pattern elyahw_priority_low = Pattern.compile("^\\[(l|L)\\]", Pattern.MULTILINE);
    public final static Pattern elyahw_chars = Pattern.compile("(^(\\-|\\*|=)+)|(\\s\\-\\s)", Pattern.MULTILINE);


    // TODO: how to avoid highlighting in the middle of another highlight?


    public MarkdownSyntaxHighlighter(AppSettings as) {
        super(as);
    }

    private boolean _highlightLineEnding;
    private boolean _highlightCodeChangeFont;
    private boolean _highlightBiggerHeadings;


    @Override
    public SyntaxHighlighterBase configure(Paint paint)
    {
        _highlightLineEnding = false; // _appSettings.isMarkdownHighlightLineEnding();
        _highlightCodeChangeFont = false; // _appSettings.isHighlightCodeMonospaceFont();
        _highlightBiggerHeadings = false; // _appSettings.isHighlightBiggerHeadings();
        _delay = 200; // _appSettings.getMarkdownHighlightingDelay();
        return super.configure(paint);
    }

    @Override
    protected void generateSpans() {

        //createTabSpans(_tabSize);
        //createUnderlineHexColorsSpans();

        // Highlighting urls:
        createSmallBlueLinkSpans();

        // Causes strange highlighting above '=='
        //        if (_highlightBiggerHeadings)
        //        {
        //            createSpanForMatches(HEADING, new WrMarkdownHeaderSpanCreator(_spannable, MD_COLOR_HEADING));
        //        }
        //        else
        //        {
        //            createColorSpanForMatches(HEADING, MD_COLOR_HEADING);
        //        }
        createColorSpanForMatches(HEADING_SIMPLE, MD_COLOR_HEADING);

        createColorSpanForMatches(LINK, MD_COLOR_LINK);

        // createColorSpanForMatches(LIST_UNORDERED, MD_COLOR_LIST);
        // createColorSpanForMatches(LIST_ORDERED, MD_COLOR_LIST);

        if (_highlightLineEnding)
        {
            createColorBackgroundSpan(DOUBLESPACE_LINE_ENDING, MD_COLOR_CODEBLOCK);
        }

        createStyleSpanForMatches(BOLD, Typeface.BOLD);
        createStyleSpanForMatches(ITALICS, Typeface.ITALIC);
        createColorSpanForMatches(QUOTATION, MD_COLOR_QUOTE);
        createStrikeThroughSpanForMatches(STRIKETHROUGH);

        if (_highlightCodeChangeFont)
        {
            createMonospaceSpanForMatches(CODE);
        }

        createColorBackgroundSpan(CODE, MD_COLOR_CODEBLOCK);

        // Elyahw custom:
        //createColorSpanForMatches(elyahw_link, elyahw_colour_blue);
        //createStyleSpanForMatches(elyahw_link, Typeface.ITALIC);
        createColorBackgroundSpan(elyahw_priority_high, elyahw_colour_red_bright);
        createStyleSpanForMatches(elyahw_priority_high, Typeface.BOLD);
        createColorBackgroundSpan(elyahw_priority_med, elyahw_colour_orange);
        createStyleSpanForMatches(elyahw_priority_med, Typeface.BOLD);
        createColorBackgroundSpan(elyahw_priority_low, elyahw_colour_yellow); // Colour the background
        createStyleSpanForMatches(elyahw_priority_low, Typeface.BOLD); // Make bold
        createColorSpanForMatches(elyahw_comment_cpp, elyahw_colour_red);
        createColorSpanForMatches(elyahw_numbers, elyahw_colour_orange);
        createColorSpanForMatches(elyahw_comment_python, elyahw_colour_green);
        createColorSpanForMatches(elyahw_chars, elyahw_colour_purple);

    }
}