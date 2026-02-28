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
    public final static Pattern custom_comment_latex = Pattern.compile("^(\\% .+)$", Pattern.MULTILINE);
    public final static Pattern custom_comment_lua = Pattern.compile("^(\\-\\- .+)$", Pattern.MULTILINE);
    public final static Pattern custom_comment_cpp = Pattern.compile("^(\\/\\/.+)$", Pattern.MULTILINE);
    public final static Pattern custom_comment_python = Pattern.compile("^\\# .+$", Pattern.MULTILINE);
    public final static Pattern custom_comment_python_double = Pattern.compile("^\\#{2} .+$", Pattern.MULTILINE); // overriden by orange
    public final static Pattern custom_comment_python_triple = Pattern.compile("^\\#{3} .+$", Pattern.MULTILINE);
    // public final static Pattern custom_link = Pattern.compile("((h|H)ttps?):\\/\\/\\S+ ");
    public final static Pattern custom_numbers = Pattern.compile("(?<!((https?|Https?|HTTPS?):\\/\\/\\S{0,300})|(^\\/\\/.{0,300})|(^#.{0,300}))\\d+", Pattern.MULTILINE); // look behind to avoid highlighting numbers in urls

    public final static Pattern custom_priority_high = Pattern.compile("^\\[(h|H|high|HIGH|High)\\]", Pattern.MULTILINE);
    public final static Pattern custom_priority_med = Pattern.compile("^\\[(m|M|medium|MEDIUM|Medium)\\]", Pattern.MULTILINE);
    public final static Pattern custom_priority_low = Pattern.compile("^\\[(l|L|low|LOW|Low)\\]", Pattern.MULTILINE);

    public final static Pattern custom_heading_red = Pattern.compile("^\\[(r|R)\\ .+$", Pattern.MULTILINE);
    public final static Pattern custom_heading_orange = Pattern.compile("^\\[(o|O)\\ .+$", Pattern.MULTILINE);
    public final static Pattern custom_heading_blue = Pattern.compile("^\\[(b|B)\\ .+$", Pattern.MULTILINE);
    public final static Pattern custom_heading_green = Pattern.compile("^\\[(g|G)\\ .+$", Pattern.MULTILINE);
    public final static Pattern custom_heading_cyan = Pattern.compile("^\\[(c|C)\\ .+$", Pattern.MULTILINE);
    public final static Pattern custom_heading_purple = Pattern.compile("^\\[(p|P)\\ .+$", Pattern.MULTILINE);

    public final static Pattern custom_filter_allow = Pattern.compile("^\\✓.+$", Pattern.MULTILINE);
    public final static Pattern custom_filter_block = Pattern.compile("^\\✗.+$", Pattern.MULTILINE);

    public final static Pattern custom_chars = Pattern.compile("(^(\\-|\\*|=)+)|(\\s\\-\\s)", Pattern.MULTILINE);
    public final static Pattern custom_crossover = Pattern.compile("^((x|X) \\- ).+$", Pattern.MULTILINE);

    // TODO: how to avoid highlighting in the middle of another highlight?

    /// **********************
    // 2. COLOUR VALUES:
    /// **********************

    // private static final int MD_COLOR_HEADING = 0xffef6D00;
    private static final int MD_COLOR_LINK = 0xff1ea3fe;
    // private static final int MD_COLOR_LIST = 0xffdaa521;
    private static final int MD_COLOR_QUOTE_LIGHT = Color.parseColor("#707070");
    private static final int MD_COLOR_QUOTE_DARK = Color.parseColor("#c0c0c0");

    private static final int MD_COLOR_CODEBLOCK_LIGHT = Color.parseColor("#161828"); // was light grey e0e0e0 / black 161828
    private static final int MD_COLOR_CODEBLOCK_DARK = Color.parseColor("#aDaDaD");  // slightly lighter than editor bg in dark mode

    private static final int custom_colour_white = Color.parseColor("#ffffff");

    // Elyahw colours: (from Kate highlights)
    private static final int custom_colour_red = Color.parseColor("#de0303");
    private static final int custom_colour_red_bg = Color.parseColor("#fff2f2");
    private static final int custom_colour_red_bright = Color.parseColor("#ff0000");

    private static final int custom_colour_green = Color.parseColor("#009f00");
    private static final int custom_colour_green_light = Color.parseColor("#e1ffe3");

    //    private static final int custom_colour_blue = Color.parseColor("#0080ff");
    private static final int custom_colour_blue_kate = Color.parseColor("#1603ff");
    private static final int custom_colour_blue_light = Color.parseColor("#cadfff");
    private static final int custom_colour_blue_dark = Color.parseColor("#0000ff");

    private static final int custom_colour_orange = Color.parseColor("#ff8000");
    private static final int custom_colour_orange_bg = Color.parseColor("#fff9f2");
    private static final int custom_colour_yellow = Color.parseColor("#ffff00");
    private static final int custom_colour_purple_kate = Color.parseColor("#800080");
    private static final int custom_colour_purple_bg = Color.parseColor("#d4bed4");
    private static final int custom_colour_purple = Color.parseColor("#ff00ff");
    private static final int custom_colour_cyan = Color.parseColor("#00ffff");
    private static final int custom_colour_cyan_bg = Color.parseColor("#cffdfd");

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

        // Theme-aware colors: use editor foreground/secondary so text adapts to light/dark mode
        final boolean is_dark = _appSettings.isDarkModeEnabled();

        final int quoteColor = is_dark ? MD_COLOR_QUOTE_DARK : MD_COLOR_QUOTE_LIGHT;
        final int codeBlockBg = is_dark ? MD_COLOR_CODEBLOCK_DARK : MD_COLOR_CODEBLOCK_LIGHT;
        final int codeTextColor = is_dark ? custom_colour_white : custom_colour_white; // Color.BLACK

        //createTabSpans(_tabSize);
        //createUnderlineHexColorsSpans();

        // Highlighting urls:
        createSmallBlueLinkSpans();

        /// **********************
        // 3. TEXT STYLES (FONT/BOLD/ITALIC):
        /// **********************

        // Bold:
        //        createStyleSpanForMatches(custom_comment_python, Typeface.BOLD);
        //        createStyleSpanForMatches(custom_comment_python_double, Typeface.BOLD);
        //        createStyleSpanForMatches(custom_comment_python_triple, Typeface.BOLD);
        //        createStyleSpanForMatches(custom_comment_cpp, Typeface.BOLD);
        //        createStyleSpanForMatches(custom_comment_latex, Typeface.BOLD);
        //        createStyleSpanForMatches(custom_comment_lua, Typeface.BOLD);
        createStyleSpanForMatches(custom_heading_red, Typeface.BOLD);
        createStyleSpanForMatches(custom_heading_green, Typeface.BOLD);
        createStyleSpanForMatches(custom_heading_blue, Typeface.BOLD);
        createStyleSpanForMatches(custom_heading_cyan, Typeface.BOLD);
        createStyleSpanForMatches(custom_heading_purple, Typeface.BOLD);
        createStyleSpanForMatches(custom_heading_orange, Typeface.BOLD);
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
        //createColorSpanForMatches(custom_link, custom_colour_blue);
        //createStyleSpanForMatches(custom_link, Typeface.ITALIC);



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

        createColorBackgroundSpan(DOUBLESPACE_LINE_ENDING, codeBlockBg);

        createColorSpanForMatches(QUOTATION, quoteColor);

        createColorBackgroundSpan(CODE, codeBlockBg);
        createColorBackgroundSpan(CODE_dollar, codeBlockBg);
        createColorBackgroundSpan(CODE_big, codeBlockBg);

        createColorSpanForMatches(CODE, codeTextColor);
        createColorSpanForMatches(CODE_dollar, codeTextColor);
        createColorSpanForMatches(CODE_big, codeTextColor);

        //$ [l] [m] [h]
        createColorBackgroundSpan(custom_priority_high, custom_colour_red_bright);
        createStyleSpanForMatches(custom_priority_high, Typeface.BOLD);
        createColorBackgroundSpan(custom_priority_med, custom_colour_orange);
        createStyleSpanForMatches(custom_priority_med, Typeface.BOLD);
        createColorBackgroundSpan(custom_priority_low, custom_colour_yellow); // Colour the background
        createStyleSpanForMatches(custom_priority_low, Typeface.BOLD); // Make bold

        //$ [r [g [b..
        createColorSpanForMatches(custom_heading_red, custom_colour_white);
        createColorBackgroundSpan(custom_heading_red, custom_colour_red);
        createColorSpanForMatches(custom_heading_green, custom_colour_white);
        createColorBackgroundSpan(custom_heading_green, custom_colour_green);
        createColorSpanForMatches(custom_heading_blue, custom_colour_white);
        createColorBackgroundSpan(custom_heading_blue, custom_colour_blue_dark);
        createColorSpanForMatches(custom_heading_cyan, custom_colour_white);
        createColorBackgroundSpan(custom_heading_cyan, custom_colour_cyan);
        createColorSpanForMatches(custom_heading_purple, custom_colour_white);
        createColorBackgroundSpan(custom_heading_purple, custom_colour_purple_kate);
        createColorSpanForMatches(custom_heading_orange, custom_colour_white);
        createColorBackgroundSpan(custom_heading_orange, custom_colour_orange);

        // -- % //
        createColorSpanForMatches(custom_comment_cpp, custom_colour_red_bright);
        createColorBackgroundSpan(custom_comment_cpp, custom_colour_red_bg);
        createColorSpanForMatches(custom_comment_latex, custom_colour_cyan);
        createColorBackgroundSpan(custom_comment_latex, custom_colour_cyan_bg);
        createColorSpanForMatches(custom_comment_lua, custom_colour_purple_kate);
        createColorBackgroundSpan(custom_comment_lua, custom_colour_purple_bg);

        // # ## ### Python comments
        createColorSpanForMatches(custom_comment_python, custom_colour_green);
        createColorBackgroundSpan(custom_comment_python, custom_colour_green_light);
        createColorSpanForMatches(custom_comment_python_double, custom_colour_blue_kate);
        createColorBackgroundSpan(custom_comment_python_double, custom_colour_blue_light);
        createColorSpanForMatches(custom_comment_python_triple, custom_colour_orange);
        createColorBackgroundSpan(custom_comment_python_triple, custom_colour_orange_bg);

        // Filter ✓ ✗
        createColorBackgroundSpan(custom_filter_allow, custom_colour_green);
        createColorBackgroundSpan(custom_filter_block, custom_colour_red);

        // Numbers:
        createColorSpanForMatches(custom_numbers, custom_colour_orange);

        // Separate chars:
        createColorSpanForMatches(custom_chars, custom_colour_purple);

        // Crossover:
        createStrikeThroughSpanForMatches(custom_crossover);
        createStyleSpanForMatches(custom_crossover, Typeface.BOLD);
    }
}
