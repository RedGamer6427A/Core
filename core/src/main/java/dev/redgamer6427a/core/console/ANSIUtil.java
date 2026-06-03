package dev.redgamer6427a.core.console;

import org.jline.utils.AttributedString;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ANSIUtil {
    public static String truncateAnsi(String s, int maxVisible, boolean front, boolean addElipsis) {
        if (!front) {
            // existing end truncation logic
            StringBuilder result = new StringBuilder();
            int visible = 0;
            int i = 0;

            while (i < s.length()) {
                if (s.charAt(i) == '\033' && i + 1 < s.length() && s.charAt(i + 1) == '[') {
                    int end = i + 2;
                    while (end < s.length() && !Character.isLetter(s.charAt(end))) end++;
                    result.append(s, i, end + 1);
                    i = end + 1;
                } else {
                    if (addElipsis) {
                        if (visible >= maxVisible - 3) {
                            result.append("...");
                            result.append("\033[0m");
                            return result.toString();
                        }
                    } else {
                        if (visible >= maxVisible) {
                            result.append("\033[0m");
                            return result.toString();
                        }
                    }
                    result.append(s.charAt(i));
                    visible++;
                    i++;
                }
            }
            return result.toString();
        } else {
            // front truncation — skip characters from the beginning
            String stripped = AttributedString.stripAnsi(s);
            int totalVisible = stripped.length();
            if (totalVisible <= maxVisible) return s;

            // how many visible chars to skip from front
            int toSkip;
            StringBuilder result;
            if (addElipsis) {
                toSkip = totalVisible - maxVisible + 3;
                result = new StringBuilder("...");
            } else {
                toSkip = totalVisible - maxVisible;
                result = new StringBuilder();
            }


            int skipped = 0;
            int i = 0;

            while (i < s.length()) {
                if (s.charAt(i) == '\033' && i + 1 < s.length() && s.charAt(i + 1) == '[') {
                    int end = i + 2;
                    while (end < s.length() && !Character.isLetter(s.charAt(end))) end++;
                    if (skipped >= toSkip) {
                        result.append(s, i, end + 1);
                    }
                    i = end + 1;
                } else {
                    if (skipped >= toSkip) {
                        result.append(s.charAt(i));
                    } else {
                        skipped++;
                    }
                    i++;
                }
            }
            return result.toString();
        }
    }

    public static String stripNonAnsi(String input) {
        // Match all ANSI escape sequences, concatenate them, drop rest
        StringBuilder sb = new StringBuilder();
        Matcher m = Pattern.compile("\u001B\\[[\\d;]*[A-Za-z]").matcher(input);
        while (m.find()) sb.append(m.group());
        return sb.toString();
    }


    public static List<Map.Entry<Integer, String>> extractAnsiCodes(String input) {
        List<Map.Entry<Integer, String>> result = new ArrayList<>();
        Matcher m = Pattern.compile("\u001B\\[[\\d;]*[A-Za-z]").matcher(input);

        int stripped = 0; // chars consumed that are plain text
        int lastEnd = 0;

        while (m.find()) {
            // plain text between last match and this one
            stripped += m.start() - lastEnd;
            result.add(Map.entry(stripped, m.group()));
            lastEnd = m.end();
        }

        return result;
    }

}
