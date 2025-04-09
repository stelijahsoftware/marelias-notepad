/*#######################################################
 *
 * SPDX-FileCopyrightText: 2019-2024 Gregor Santner <gsantner AT mailbox DOT org>
 * SPDX-License-Identifier: Unlicense OR CC0-1.0
 *
 * Written 2019-2024 by Gregor Santner <gsantner AT mailbox DOT org>
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * You should have received a copy of the CC0 Public Domain Dedication along with this software. If not, see <http://creativecommons.org/publicdomain/zero/1.0/>.
#########################################################*/
package net.marelias.opoc.opoc;

import android.util.Base64;

@SuppressWarnings({"unused", "SpellCheckingInspection"})
public class GsTextUtils {

    public static String toTitleCase(final String str) {
        final String delimiters = " '-/#.";
        final StringBuilder sb = new StringBuilder();

        boolean nextUppercase = true;
        for (char c : str.toCharArray()) {
            c = (nextUppercase) ? Character.toUpperCase(c) : Character.toLowerCase(c);
            sb.append(c);
            nextUppercase = (delimiters.indexOf(c) >= 0);
        }
        return sb.toString().replaceAll("\\s+", " ").trim();
    }

    public static String toBase64(final byte[] bytes) {
        try {
            return Base64.encodeToString(bytes, Base64.DEFAULT);
        } catch (Exception ignored) {
            return "";
        }
    }

    public static int tryParseInt(final String value, int defaultValue) {
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    // Not null, not empty, not spaces only
    public static boolean isNullOrEmpty(final CharSequence str) {
        return str == null || str.length() == 0 || str.toString().trim().isEmpty();
    }

    /**
     * Convert escape sequences in string to escaped special characters. For example, convert
     * A\tB -> A    B
     * --------------
     * A\nB -> A
     * B
     *
     * @param input Input string
     * @return String with escaped sequences converted
     */
    public static String unescapeString(final String input) {
        final StringBuilder builder = new StringBuilder();
        boolean isEscaped = false;
        for (int i = 0; i < input.length(); i++) {
            char current = input.charAt(i);
            if (isEscaped) {
                if (current == 't') {
                    builder.append('\t');
                } else if (current == 'b') {
                    builder.append('\b');
                } else if (current == 'r') {
                    builder.append('\r');
                } else if (current == 'n') {
                    builder.append('\n');
                } else if (current == 'f') {
                    builder.append('\f');
                } else {
                    // Replace anything else with the literal pattern
                    builder.append('\\');
                    builder.append(current);
                }
                isEscaped = false;
            } else if (current == '\\') {
                isEscaped = true;
            } else {
                builder.append(current);
            }
        }

        // Handle trailing slash
        if (isEscaped) {
            builder.append('\\');
        }
        return builder.toString();
    }

    /**
     * Count instances of chars between start and end
     *
     * @param s     Sequence to count in
     * @param start start of section to count within
     * @param end   end of section to count within
     * @param chars Array of chars to count
     * @return number of instances of each char in [start, end)
     */
    public static int[] countChars(final CharSequence s, int start, int end, final char... chars) {
        // Faster specialization for the common single case
        if (chars.length == 1) {
            return new int[]{countChar(s, start, end, chars[0])};
        }

        final int[] counts = new int[chars.length];
        start = Math.max(0, start);
        end = Math.min(end, s.length());
        for (int i = start; i < end; i++) {
            final char c = s.charAt(i);
            for (int j = 0; j < chars.length; j++) {
                if (c == chars[j]) {
                    counts[j]++;
                }
            }
        }
        return counts;
    }

    /**
     * Count instances of a single char in a charsequence
     */
    public static int countChar(final CharSequence s, int start, int end, final char c) {
        start = Math.max(0, start);
        end = Math.min(end, s.length());
        int count = 0;
        for (int i = start; i < end; i++) {
            if (s.charAt(i) == c) {
                count++;
            }
        }
        return count;
    }

    public static boolean isNewLine(CharSequence source, int start, int end) {
        return isValidIndex(source, start, end - 1) && (source.charAt(start) == '\n' || source.charAt(end - 1) == '\n');
    }

    public static boolean isValidIndex(final CharSequence s, final int... indices) {
        return s != null && indices != null && inRange(0, s.length() - 1, indices);
    }

    public static boolean isValidSelection(final CharSequence s, final int... indices) {
        return s != null && indices != null && inRange(0, s.length(), indices);
    }

    // Checks if all values are in [min, max] _inclusive_
    public static boolean inRange(final int min, final int max, final int... values) {
        for (final int i : values) {
            if (i < min || i > max) {
                return false;
            }
        }
        return true;
    }
}
