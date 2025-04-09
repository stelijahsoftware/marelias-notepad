/*#######################################################
 *
 * SPDX-FileCopyrightText: 2017-2024 Gregor Santner <gsantner AT mailbox DOT org>
 * SPDX-License-Identifier: Unlicense OR CC0-1.0
 *
 * Written 2017-2024 by Gregor Santner <gsantner AT mailbox DOT org>
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * You should have received a copy of the CC0 Public Domain Dedication along with this software. If not, see <http://creativecommons.org/publicdomain/zero/1.0/>.
#########################################################*/

/*
 * Parses most common markdown tags. Only inline tags are supported, multiline/block syntax
 * is not supported (citation, multiline code, ..). This is intended to stay as easy as possible.
 *
 * You can e.g. apply a accent color by replacing #000001 with your accentColor string.
 *
 * FILTER_ANDROID_TEXTVIEW output is intended to be used at simple Android TextViews,
 * were a limited set of _html tags is supported. This allow to still display e.g. a simple
 * CHANGELOG.md file without including a WebView for showing HTML, or other additional UI-libraries.
 *
 * FILTER_WEB is intended to be used at engines understanding most common HTML tags.
 */

package net.marelias.opoc.opoc;

/**
 * Simple Markdown Parser
 */
@SuppressWarnings({"WeakerAccess", "CaughtExceptionImmediatelyRethrown", "SameParameterValue", "unused", "SpellCheckingInspection", "RepeatedSpace", "SingleCharAlternation", "Convert2Lambda"})
public class GsSimpleMarkdownParser {
    //########################
    //## Statics
    //########################
    public interface SmpFilter {
        String filter(String text);
    }

    public final static SmpFilter FILTER_WEB = new SmpFilter() {
        @Override
        public String filter(String text) {
            // Don't start new line if 2 empty lines and heading
            while (text.contains("\n\n#")) {
                text = text.replace("\n\n#", "\n#");
            }

            text = text
                    .replaceAll("(?s)<!--.*?-->", "")  // HTML comments
                    .replace("\n\n", "\n<br/>\n") // Start new line if 2 empty lines
                    .replaceAll("~°", "&nbsp;&nbsp;") // double space/half tab
                    .replaceAll("(?m)^### (.*)$", "<h3>$1</h3>") // h3
                    .replaceAll("(?m)^## (.*)$", "<h2>$1</h2>") /// h2 (DEP: h3)
                    .replaceAll("(?m)^# (.*)$", "<h1>$1</h1>") // h1 (DEP: h2,h3)
                    .replaceAll("!\\[(.*?)\\]\\((.*?)\\)", "<img src=\\'$2\\' alt='$1' />") // img
                    .replaceAll("<(http|https):\\/\\/(.*)>", "<a href='$1://$2'>$1://$2</a>") // a href (DEP: img)
                    .replaceAll("\\[(.*?)\\]\\((.*?)\\)", "<a href=\\'$2\\'>$1</a>") // a href (DEP: img)
                    .replaceAll("(?m)^[-*] (.*)$", "<font color='#000001'>&#8226;</font> $1  ") // unordered list + end line
                    .replaceAll("(?m)^  [-*] (.*)$", "&nbsp;&nbsp;<font color='#000001'>&#8226;</font> $1  ") // unordered list2 + end line
                    .replaceAll("`([^<]*)`", "<code>$1</code>") // code
                    .replace("\\*", "●") // temporary replace escaped star symbol
                    .replaceAll("(?m)\\*\\*(.*)\\*\\*", "<b>$1</b>") // bold (DEP: temp star)
                    .replaceAll("(?m)\\*(.*)\\*", "<i>$1</i>") // italic (DEP: temp star code)
                    .replace("●", "*") // restore escaped star symbol (DEP: b,i)
                    .replaceAll("(?m)  $", "<br/>") // new line (DEP: ul)
            ;
            return text;
        }
    };

    //########################
    //## Singleton
    //########################
    private static GsSimpleMarkdownParser __instance;

    public static GsSimpleMarkdownParser get() {
        if (__instance == null) {
            __instance = new GsSimpleMarkdownParser();
        }
        return __instance;
    }

    //########################
    //## Members, Constructors
    //########################
    private SmpFilter _defaultSmpFilter;
    private String _html;

    public GsSimpleMarkdownParser() {
        setDefaultSmpFilter(FILTER_WEB);
    }

    //########################
    //## Methods
    //########################
    public GsSimpleMarkdownParser setDefaultSmpFilter(SmpFilter defaultSmpFilter) {
        _defaultSmpFilter = defaultSmpFilter;
        return this;
    }

    public String getHtml() {
        return _html;
    }

    public GsSimpleMarkdownParser setHtml(String html) {
        _html = html;
        return this;
    }

    @Override
    public String toString() {
        return _html != null ? _html : "";
    }
}
