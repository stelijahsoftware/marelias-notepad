/*#######################################################
 *
 *   Maintained 2018-2024 by Gregor Santner <gsantner AT mailbox DOT org>
 *   License of this file: Apache 2.0
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
#########################################################*/
package net.gsantner.notepad2.format.markdown;

import android.content.Context;
import android.text.TextUtils;

import com.vladsch.flexmark.ext.admonition.AdmonitionExtension;
import com.vladsch.flexmark.ext.anchorlink.AnchorLinkExtension;
import com.vladsch.flexmark.ext.autolink.AutolinkExtension;
import com.vladsch.flexmark.ext.emoji.EmojiExtension;
import com.vladsch.flexmark.ext.emoji.EmojiImageType;
import com.vladsch.flexmark.ext.footnotes.FootnoteExtension;
import com.vladsch.flexmark.ext.gfm.strikethrough.StrikethroughSubscriptExtension;
import com.vladsch.flexmark.ext.gfm.tasklist.TaskListExtension;
import com.vladsch.flexmark.ext.gitlab.GitLabExtension;
import com.vladsch.flexmark.ext.ins.InsExtension;
import com.vladsch.flexmark.ext.jekyll.front.matter.JekyllFrontMatterExtension;
import com.vladsch.flexmark.ext.jekyll.tag.JekyllTagExtension;
import com.vladsch.flexmark.ext.tables.TablesExtension;
import com.vladsch.flexmark.ext.toc.SimTocExtension;
import com.vladsch.flexmark.ext.toc.TocExtension;
import com.vladsch.flexmark.ext.toc.internal.TocOptions;
import com.vladsch.flexmark.ext.typographic.TypographicExtension;
import com.vladsch.flexmark.ext.wikilink.WikiLinkExtension;
import com.vladsch.flexmark.ext.yaml.front.matter.AbstractYamlFrontMatterVisitor;
import com.vladsch.flexmark.ext.yaml.front.matter.YamlFrontMatterExtension;
import com.vladsch.flexmark.html.AttributeProvider;
import com.vladsch.flexmark.html.AttributeProviderFactory;
import com.vladsch.flexmark.html.HtmlRenderer;
import com.vladsch.flexmark.html.renderer.AttributablePart;
import com.vladsch.flexmark.html.renderer.LinkResolverContext;
import com.vladsch.flexmark.parser.Parser;
import com.vladsch.flexmark.superscript.SuperscriptExtension;
import com.vladsch.flexmark.util.ast.Document;
import com.vladsch.flexmark.util.ast.Node;
import com.vladsch.flexmark.util.builder.Extension;
import com.vladsch.flexmark.util.html.Attributes;
import com.vladsch.flexmark.util.options.MutableDataHolder;
import com.vladsch.flexmark.util.options.MutableDataSet;

import net.gsantner.notepad2.R;
import net.gsantner.notepad2.format.TextConverterBase;
import net.gsantner.opoc.util.GsContextUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@SuppressWarnings({"unchecked", "WeakerAccess"})
public class MarkdownTextConverter extends TextConverterBase {
    //########################
    //## Extensions
    //########################
    public static final String EXT_MARKDOWN__TXT = ".txt";
    public static final String EXT_MARKDOWN__MD_TXT = ".md.txt";
    public static final String EXT_MARKDOWN__MD = ".md";
    public static final String EXT_MARKDOWN__MARKDOWN = ".markdown";
    public static final String EXT_MARKDOWN__MKD = ".mkd";
    public static final String EXT_MARKDOWN__MDOWN = ".mdown";
    public static final String EXT_MARKDOWN__MKDN = ".mkdn";
    public static final String EXT_MARKDOWN__MDWN = ".mdwn";
    public static final String EXT_MARKDOWN__MDX = ".mdx";
    public static final String EXT_MARKDOWN__TEXT = ".text";
    public static final String EXT_MARKDOWN__RMD = ".rmd";

    public static final String MD_EXTENSIONS_PATTERN_LIST = "((md)|(markdown)|(mkd)|(mdown)|(mkdn)|(txt)|(mdwn)|(mdx)|(text)|(rmd))";
    public static final Pattern PATTERN_HAS_FILE_EXTENSION_FOR_THIS_FORMAT = Pattern.compile("((?i).*\\." + MD_EXTENSIONS_PATTERN_LIST + "$)");
    public static final Pattern MD_EXTENSION_PATTERN = Pattern.compile("((?i)\\." + MD_EXTENSIONS_PATTERN_LIST + "$)");
    public static final String[] MD_EXTENSIONS = new String[]{
            EXT_MARKDOWN__MD, EXT_MARKDOWN__MARKDOWN, EXT_MARKDOWN__MKD, EXT_MARKDOWN__MDOWN,
            EXT_MARKDOWN__MKDN, EXT_MARKDOWN__TXT, EXT_MARKDOWN__MDWN, EXT_MARKDOWN__TEXT,
            EXT_MARKDOWN__RMD, EXT_MARKDOWN__MD_TXT, EXT_MARKDOWN__MDX
    };

    //########################
    //## Injected CSS / JS / HTML
    //########################
    public static final String HTML_TOKEN_ITEM_S = "<span class='{{ scope }}-item-{{ attrName }}'>";
    public static final String HTML_TOKEN_ITEM_E = "</span>";
    public static final String HTML_TOKEN_DELIMITER = "<span class='{{ scope }}-delimiter-{{ attrName }} delimiter'></span>";

    public static final String YAML_FRONTMATTER_SCOPES = "post"; //, page, site";


    //########################
    //## Methods
    //########################

    @Override

    protected boolean isFileOutOfThisFormat(final File file, final String name, final String ext) {
        return (MarkdownTextConverter.PATTERN_HAS_FILE_EXTENSION_FOR_THIS_FORMAT.matcher(name).matches() && !name.endsWith(".txt")) || name.endsWith(".md.txt");
    }


    // Extension to add line numbers to headings
    // ---------------------------------------------------------------------------------------------

    private static class LineNumberIdProvider implements AttributeProvider {
        @Override
        public void setAttributes(Node node, AttributablePart part, Attributes attributes) {
            final Document document = node.getDocument();
            final int lineNumber = document.getLineNumber(node.getStartOffset());
            attributes.addValue("line", "" + lineNumber);
        }
    }

    private static class LineNumberIdProviderFactory implements AttributeProviderFactory {

        @Override
        public Set<Class<? extends AttributeProviderFactory>> getAfterDependents() {
            return null;
        }

        @Override
        public Set<Class<? extends AttributeProviderFactory>> getBeforeDependents() {
            return null;
        }

        @Override
        public boolean affectsGlobalScope() {
            return false;
        }

        @Override
        public AttributeProvider create(LinkResolverContext context) {
            return new LineNumberIdProvider();
        }
    }

    private static class LineNumberIdExtension implements HtmlRenderer.HtmlRendererExtension {
        @Override
        public void rendererOptions(MutableDataHolder options) {
        }

        @Override
        public void extend(HtmlRenderer.Builder rendererBuilder, String rendererType) {
            rendererBuilder.attributeProviderFactory(new LineNumberIdProviderFactory());
        }

        public static HtmlRenderer.HtmlRendererExtension create() {
            return new LineNumberIdExtension();
        }
    }
}
