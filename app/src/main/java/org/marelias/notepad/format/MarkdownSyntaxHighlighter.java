/*#######################################################
 *
 *   Maintained 2018-2024 by Gregor Santner <gsantner AT mailbox DOT org>
 *   License of this file: Apache 2.0
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
#########################################################*/
package org.marelias.notepad.format;

import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;

import org.marelias.notepad.frontend.textview.SyntaxHighlighterBase;
import org.marelias.notepad.model.AppSettings;

import java.util.regex.Pattern;

public class MarkdownSyntaxHighlighter extends SyntaxHighlighterBase {

    /// *******************************
    // 1. REGULAR EXPRESSIONS/PATTERNS:
    /// *******************************

//    public final static Pattern BOLD = Pattern.compile("(?<=(\\n|^|\\s|\\[|\\{|\\())(([*_]){2,3})(?=\\S)(.*?)\\S\\2(?=(\\n|$|\\s|\\.|,|:|;|-|\\]|\\}|\\)))");
    public final static Pattern BOLD = Pattern.compile("\\*(?!\\s)([0-9a-zA-Z ]+?)(?<!\\s)\\*");
//    public final static Pattern ITALICS = Pattern.compile("(?<=(\\n|^|\\s|\\[|\\{|\\())([*_])(?=((?!\\2)|\\2{2,}))(?=\\S)(.*?)\\S\\2(?=(\\n|$|\\s|\\.|,|:|;|-|\\]|\\}|\\)))");
    public final static Pattern ITALICS = Pattern.compile("\\_(?!\\s)([0-9a-zA-Z ]+?)(?<!\\s)\\_");
    // public final static Pattern HEADING = Pattern.compile("(?m)((^#{1,6}[^\\S\\n][^\\n]+)|((\\n|^)[^\\s]+.*?\\n(-{2,}|={2,})[^\\S\\n]*$))");
    // public final static Pattern HEADING_SIMPLE = Pattern.compile("(?m)^(#{1,6}\\s.*$)");
    public final static Pattern LINK = Pattern.compile("\\[([^\\[]+)\\]\\(([^\\)]+)\\)");
    // public final static Pattern LIST_UNORDERED = Pattern.compile("(\\n|^)\\s{0,16}([*+-])( \\[[ xX]\\])?(?= )");
    // public final static Pattern LIST_ORDERED = Pattern.compile("(?m)^\\s{0,16}(\\d+)(:?\\.|\\))\\s");
    public final static Pattern STRIKETHROUGH = Pattern.compile("~{1}(.*?)\\S~{1}");
    public final static Pattern QUOTATION = Pattern.compile("(\\n|^)>");
    public final static Pattern QUOTATION_after_the_greaterthansymbol = Pattern.compile("(\\n|^)>.+");

    public final static Pattern CODE = Pattern.compile("(?m)(`(?!`)(.*?)`)");
    //public final static Pattern CODE = Pattern.compile("(?m)(`(?!`)(.*?)`)|(^[^\\S\\n]{4}(?![0-9\\-*+]).*$)"); // includes any code tabbed with 4 spaces
    public final static Pattern CODE_big = Pattern.compile("(?s)`{3}.*`{3}"); // (?s) lets it span multiple lines
    public final static Pattern CODE_dollar = Pattern.compile("^\\$\\ .+$", Pattern.MULTILINE);

    public final static Pattern DOUBLESPACE_LINE_ENDING = Pattern.compile("(?m)(?<=\\S)([^\\S\\n]{2,})\\n");
    // public final static Pattern ACTION_LINK_PATTERN = Pattern.compile("(?m)\\[(.*?)\\]\\((.*?)\\)");

    // Elyahw regexes: reference: https://docs.oracle.com/javase/8/docs/api/java/util/regex/Pattern.html
    public final static Pattern elyahw_comment_latex = Pattern.compile("^(\\% .+)$", Pattern.MULTILINE);
    public final static Pattern elyahw_comment_lua = Pattern.compile("^(\\-\\- .+)$", Pattern.MULTILINE);
    public final static Pattern elyahw_comment_cpp = Pattern.compile("^(\\/\\/.+)$", Pattern.MULTILINE);
    public final static Pattern elyahw_comment_python = Pattern.compile("^\\# .+$", Pattern.MULTILINE);
    public final static Pattern elyahw_comment_python_double = Pattern.compile("^\\#{2} .+$", Pattern.MULTILINE); // overriden by orange
    public final static Pattern elyahw_comment_python_triple = Pattern.compile("^\\#{3} .+$", Pattern.MULTILINE);
    // public final static Pattern elyahw_link = Pattern.compile("((h|H)ttps?):\\/\\/\\S+ ");
    public final static Pattern elyahw_numbers = Pattern.compile("(?<!((https?|Https?|HTTPS?):\\/\\/\\S{0,300})|(^\\/\\/.{0,300})|(^#.{0,300}))\\d+", Pattern.MULTILINE); // look behind to avoid highlighting numbers in urls

    public final static Pattern elyahw_priority_high = Pattern.compile("^\\[(h|H|high|HIGH|High)\\]", Pattern.MULTILINE);
    public final static Pattern elyahw_priority_med = Pattern.compile("^\\[(m|M|medium|MEDIUM|Medium)\\]", Pattern.MULTILINE);
    public final static Pattern elyahw_priority_low = Pattern.compile("^\\[(l|L|low|LOW|Low)\\]", Pattern.MULTILINE);

    public final static Pattern elyahw_heading_red = Pattern.compile("^\\[(r|R)\\ .+$", Pattern.MULTILINE);
    public final static Pattern elyahw_heading_orange = Pattern.compile("^\\[(o|O)\\ .+$", Pattern.MULTILINE);
    public final static Pattern elyahw_heading_blue = Pattern.compile("^\\[(b|B)\\ .+$", Pattern.MULTILINE);
    public final static Pattern elyahw_heading_green = Pattern.compile("^\\[(g|G)\\ .+$", Pattern.MULTILINE);
    public final static Pattern elyahw_heading_cyan = Pattern.compile("^\\[(c|C)\\ .+$", Pattern.MULTILINE);
    public final static Pattern elyahw_heading_purple = Pattern.compile("^\\[(p|P)\\ .+$", Pattern.MULTILINE);

    public final static Pattern elyahw_filter_allow = Pattern.compile("^\\✓.+$", Pattern.MULTILINE);
    public final static Pattern elyahw_filter_block = Pattern.compile("^\\✗.+$", Pattern.MULTILINE);

    public final static Pattern elyahw_chars = Pattern.compile("(^(\\-|\\*|=)+)|(\\s\\-\\s)", Pattern.MULTILINE);
    public final static Pattern elyahw_crossover = Pattern.compile("^((x|X) \\- ).+$", Pattern.MULTILINE);

    // TODO: how to avoid highlighting in the middle of another highlight?

    /// **********************
    // 2. COLOUR VALUES:
    /// **********************

    // private static final int MD_COLOR_HEADING = 0xffef6D00;
    private static final int MD_COLOR_LINK = 0xff1ea3fe;
    // private static final int MD_COLOR_LIST = 0xffdaa521;
    private static final int MD_COLOR_QUOTE = Color.parseColor("#707070");
    private static final int MD_COLOR_CODEBLOCK = Color.parseColor("#161828"); // was light grey e0e0e0

    private static final int elyahw_colour_white = Color.parseColor("#ffffff");

    // Elyahw colours: (from Kate highlights)
    private static final int elyahw_colour_red = Color.parseColor("#de0303");
    private static final int elyahw_colour_red_bg = Color.parseColor("#fff2f2");
    private static final int elyahw_colour_red_bright = Color.parseColor("#ff0000");

    private static final int elyahw_colour_green = Color.parseColor("#009f00");
    private static final int elyahw_colour_green_light = Color.parseColor("#e1ffe3");

//    private static final int elyahw_colour_blue = Color.parseColor("#0080ff");
    private static final int elyahw_colour_blue_kate = Color.parseColor("#1603ff");
    private static final int elyahw_colour_blue_light = Color.parseColor("#cadfff");
    private static final int elyahw_colour_blue_dark = Color.parseColor("#0000ff");

    private static final int elyahw_colour_orange = Color.parseColor("#ff8000");
    private static final int elyahw_colour_orange_bg = Color.parseColor("#fff9f2");
    private static final int elyahw_colour_yellow = Color.parseColor("#ffff00");
    private static final int elyahw_colour_purple_kate = Color.parseColor("#800080");
    private static final int elyahw_colour_purple_bg = Color.parseColor("#d4bed4");
    private static final int elyahw_colour_purple = Color.parseColor("#ff00ff");
    private static final int elyahw_colour_cyan = Color.parseColor("#00ffff");
    private static final int elyahw_colour_cyan_bg = Color.parseColor("#cffdfd");

    /*
    from Kate:
        <itemData name="TaskDone" strikeOut="1" bold="1" backgroundColor="#eeeeee" /> <!-- Note: strikeOut not strikeout -->
        <itemData name="CodeSegment" color='#000000' backgroundColor='#eaeaea' italic="1"/>
     */

    public MarkdownSyntaxHighlighter(AppSettings as) {
        super(as);
    }

    private boolean _highlightBiggerHeadings;

    @Override
    public SyntaxHighlighterBase configure(Paint paint)
    {
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

        /// **********************
        // 3. TEXT STYLES (FONT/BOLD/ITALIC):
        /// **********************

        // Bold:
//        createStyleSpanForMatches(elyahw_comment_python, Typeface.BOLD);
//        createStyleSpanForMatches(elyahw_comment_python_double, Typeface.BOLD);
//        createStyleSpanForMatches(elyahw_comment_python_triple, Typeface.BOLD);
//        createStyleSpanForMatches(elyahw_comment_cpp, Typeface.BOLD);
//        createStyleSpanForMatches(elyahw_comment_latex, Typeface.BOLD);
//        createStyleSpanForMatches(elyahw_comment_lua, Typeface.BOLD);
        createStyleSpanForMatches(elyahw_heading_red, Typeface.BOLD);
        createStyleSpanForMatches(elyahw_heading_green, Typeface.BOLD);
        createStyleSpanForMatches(elyahw_heading_blue, Typeface.BOLD);
        createStyleSpanForMatches(elyahw_heading_cyan, Typeface.BOLD);
        createStyleSpanForMatches(elyahw_heading_purple, Typeface.BOLD);
        createStyleSpanForMatches(elyahw_heading_orange, Typeface.BOLD);
        createStyleSpanForMatches(BOLD, Typeface.BOLD);

        // Italic:
        createStyleSpanForMatches(QUOTATION_after_the_greaterthansymbol, Typeface.ITALIC);
        createStyleSpanForMatches(ITALICS, Typeface.ITALIC);

        // Strikethrough:
        createStrikeThroughSpanForMatches(STRIKETHROUGH);

        // Monospace font:
        createMonospaceSpanForMatches(CODE);
        createMonospaceSpanForMatches(CODE_dollar);
        createMonospaceSpanForMatches(CODE_big);

        // Elyahw custom: (order matters)
        //createColorSpanForMatches(elyahw_link, elyahw_colour_blue);
        //createStyleSpanForMatches(elyahw_link, Typeface.ITALIC);



        /// **********************
        // 4. COLOUR MAPPINGS:
        /// **********************

        // Causes strange highlighting above '=='
        //        if (_highlightBiggerHeadings)
        //        {
        //            createSpanForMatches(HEADING, new WrMarkdownHeaderSpanCreator(_spannable, MD_COLOR_HEADING));
        //        }
        //        else
        //        {
        //            createColorSpanForMatches(HEADING, MD_COLOR_HEADING);
        //        }
        // createColorSpanForMatches(HEADING_SIMPLE, MD_COLOR_HEADING);

        createColorSpanForMatches(LINK, MD_COLOR_LINK);

        // createColorSpanForMatches(LIST_UNORDERED, MD_COLOR_LIST);
        // createColorSpanForMatches(LIST_ORDERED, MD_COLOR_LIST);

        createColorBackgroundSpan(DOUBLESPACE_LINE_ENDING, MD_COLOR_CODEBLOCK);

        createColorSpanForMatches(QUOTATION, MD_COLOR_QUOTE);

        createColorBackgroundSpan(CODE, MD_COLOR_CODEBLOCK);
        createColorBackgroundSpan(CODE_dollar, MD_COLOR_CODEBLOCK);
        createColorBackgroundSpan(CODE_big, MD_COLOR_CODEBLOCK);

        createColorSpanForMatches(CODE, elyahw_colour_white);
        createColorSpanForMatches(CODE_dollar, elyahw_colour_white);
        createColorSpanForMatches(CODE_big, elyahw_colour_white);

        //$ [l] [m] [h]
        createColorBackgroundSpan(elyahw_priority_high, elyahw_colour_red_bright);
        createStyleSpanForMatches(elyahw_priority_high, Typeface.BOLD);
        createColorBackgroundSpan(elyahw_priority_med, elyahw_colour_orange);
        createStyleSpanForMatches(elyahw_priority_med, Typeface.BOLD);
        createColorBackgroundSpan(elyahw_priority_low, elyahw_colour_yellow); // Colour the background
        createStyleSpanForMatches(elyahw_priority_low, Typeface.BOLD); // Make bold

        //$ [r [g [b..
        createColorSpanForMatches(elyahw_heading_red, elyahw_colour_white);
        createColorBackgroundSpan(elyahw_heading_red, elyahw_colour_red);
        createColorSpanForMatches(elyahw_heading_green, elyahw_colour_white);
        createColorBackgroundSpan(elyahw_heading_green, elyahw_colour_green);
        createColorSpanForMatches(elyahw_heading_blue, elyahw_colour_white);
        createColorBackgroundSpan(elyahw_heading_blue, elyahw_colour_blue_dark);
        createColorSpanForMatches(elyahw_heading_cyan, elyahw_colour_white);
        createColorBackgroundSpan(elyahw_heading_cyan, elyahw_colour_cyan);
        createColorSpanForMatches(elyahw_heading_purple, elyahw_colour_white);
        createColorBackgroundSpan(elyahw_heading_purple, elyahw_colour_purple_kate);
        createColorSpanForMatches(elyahw_heading_orange, elyahw_colour_white);
        createColorBackgroundSpan(elyahw_heading_orange, elyahw_colour_orange);

        // -- % //
        createColorSpanForMatches(elyahw_comment_cpp, elyahw_colour_red_bright);
        createColorBackgroundSpan(elyahw_comment_cpp, elyahw_colour_red_bg);
        createColorSpanForMatches(elyahw_comment_latex, elyahw_colour_cyan);
        createColorBackgroundSpan(elyahw_comment_latex, elyahw_colour_cyan_bg);
        createColorSpanForMatches(elyahw_comment_lua, elyahw_colour_purple_kate);
        createColorBackgroundSpan(elyahw_comment_lua, elyahw_colour_purple_bg);

        // # ## ### Python comments
        createColorSpanForMatches(elyahw_comment_python, elyahw_colour_green);
        createColorBackgroundSpan(elyahw_comment_python, elyahw_colour_green_light);
        createColorSpanForMatches(elyahw_comment_python_double, elyahw_colour_blue_kate);
        createColorBackgroundSpan(elyahw_comment_python_double, elyahw_colour_blue_light);
        createColorSpanForMatches(elyahw_comment_python_triple, elyahw_colour_orange);
        createColorBackgroundSpan(elyahw_comment_python_triple, elyahw_colour_orange_bg);

        // Filter ✓ ✗
        createColorBackgroundSpan(elyahw_filter_allow, elyahw_colour_green);
        createColorBackgroundSpan(elyahw_filter_block, elyahw_colour_red);

        // Numbers:
        createColorSpanForMatches(elyahw_numbers, elyahw_colour_orange);

        // Separate chars:
        createColorSpanForMatches(elyahw_chars, elyahw_colour_purple);

        // Crossover:
        createStrikeThroughSpanForMatches(elyahw_crossover);
        createStyleSpanForMatches(elyahw_crossover, Typeface.BOLD);
    }
}
