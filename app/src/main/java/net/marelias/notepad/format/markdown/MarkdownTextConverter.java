/*#######################################################
 *
 *   Maintained 2018-2024 by Gregor Santner <gsantner AT mailbox DOT org>
 *   License of this file: Apache 2.0
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
#########################################################*/
package net.marelias.notepad.format.markdown;

import net.marelias.notepad.format.TextConverterBase;
import java.io.File;
import java.util.regex.Pattern;

@SuppressWarnings({"unchecked", "WeakerAccess"})
public class MarkdownTextConverter extends TextConverterBase {

    public static final String MD_EXTENSIONS_PATTERN_LIST = "((md)|(markdown)|(mkd)|(mdown)|(mkdn)|(txt)|(mdwn)|(mdx)|(text)|(rmd))";
    public static final Pattern PATTERN_HAS_FILE_EXTENSION_FOR_THIS_FORMAT = Pattern.compile("((?i).*\\." + MD_EXTENSIONS_PATTERN_LIST + "$)");

    @Override
    protected boolean isFileOutOfThisFormat(final File file, final String name, final String ext) {
        return (MarkdownTextConverter.PATTERN_HAS_FILE_EXTENSION_FOR_THIS_FORMAT.matcher(name).matches() && !name.endsWith(".txt")) || name.endsWith(".md.txt");
    }

}
