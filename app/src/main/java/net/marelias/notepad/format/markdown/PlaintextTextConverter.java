/*#######################################################
 *
 *   Maintained 2018-2024 by Gregor Santner <gsantner AT mailbox DOT org>
 *   License of this file: Apache 2.0
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
#########################################################*/
package net.marelias.notepad.format.markdown;

import net.marelias.notepad.format.TextConverterBase;
import net.marelias.opoc.util.GsFileUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@SuppressWarnings("WeakerAccess")
public class PlaintextTextConverter extends TextConverterBase {
    private static final List<String> EXT_TEXT = Arrays.asList(".taskpaper", ".org", ".ldg", ".ledger", ".m3u", ".m3u8");
    private static final List<String> EXT_HTML = Arrays.asList(".html", ".htm");
    private static final List<String> EXT_CODE_HL = Arrays.asList(".py", ".cpp", ".h", ".c", ".js",
                                                                ".mjs", ".css", ".cs", ".kt",
                                                                ".lua", ".perl", ".java", ".qml",
                                                                ".diff", ".php", ".r", ".patch",
                                                                ".rs", ".swift", ".ts", ".mm", ".go",
                                                                ".sh", ".rb", ".tex", ".xml", ".xlf");
    private static final List<String> EXT = new ArrayList<>();

    static {
        EXT.addAll(EXT_TEXT);
        EXT.addAll(EXT_HTML);
        EXT.addAll(EXT_CODE_HL);
    }

    //########################
    //## Methods
    //########################

    @Override
    protected boolean isFileOutOfThisFormat(final File file, final String name, final String ext) {
        return EXT.contains(ext) || _appSettings.isExtOpenWithThisApp(ext) || GsFileUtils.isTextFile(file);
    }
}
