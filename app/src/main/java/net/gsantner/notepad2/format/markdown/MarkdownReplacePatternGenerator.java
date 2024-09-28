/*#######################################################
 *
 *   Maintained 2018-2024 by Gregor Santner <gsantner AT mailbox DOT org>
 *   License of this file: Apache 2.0
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
#########################################################*/
package net.gsantner.notepad2.format.markdown;

import net.gsantner.notepad2.format.ActionButtonBase;
import net.gsantner.notepad2.frontend.textview.AutoTextFormatter;
import net.gsantner.notepad2.frontend.textview.ReplacePatternGeneratorHelper;
import net.gsantner.opoc.format.GsTextUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

public class MarkdownReplacePatternGenerator {

    // TODO: write tests

    public static final Pattern PREFIX_ORDERED_LIST = Pattern.compile("^(\\s*)((\\d+)(\\.|\\))(\\s))");
    public static final Pattern PREFIX_CHECKBOX_LIST = Pattern.compile("^(\\s*)(([-*+]\\s\\[)[\\sxX](]\\s))");
    public static final Pattern PREFIX_UNORDERED_LIST = Pattern.compile("^(\\s*)((-|\\*|\\+)\\s)");

    public static final AutoTextFormatter.FormatPatterns formatPatterns = new AutoTextFormatter.FormatPatterns(
            MarkdownReplacePatternGenerator.PREFIX_UNORDERED_LIST,
            MarkdownReplacePatternGenerator.PREFIX_CHECKBOX_LIST,
            MarkdownReplacePatternGenerator.PREFIX_ORDERED_LIST,
            2);
}
