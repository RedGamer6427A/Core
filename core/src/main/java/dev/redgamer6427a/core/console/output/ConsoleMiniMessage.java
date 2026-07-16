package dev.redgamer6427a.core.console.output;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import lombok.Getter;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * A class to implement the MiniMessage format for terminals
 * ----------------------------
 * Functionality:
 * - Hex and (changeable) predefined colors for fg, bg and underline.
 * - Default ansi colors and styling codes
 */

public class ConsoleMiniMessage {

    /**
     * Current colors
     */
    @Getter
    private static Map<String, Integer> colors;

    // PATTERNS
    /**
     * Implicit FG 3 and 6 letter hex codes
     */
    private static final Pattern hex6Pattern = Pattern.compile("<#([a-fA-F0-9]{6})>");
    private static final Pattern hex3Pattern = Pattern.compile("<#([a-fA-F0-9]{3})>");

    /**
     * Explicit FG 3 and 6 letter hex codes
     */
    private static final Pattern hex6PatternFG = Pattern.compile("<fg_#([a-fA-F0-9]{6})>");
    /**
     * Explicit FG 3 and 6 letter hex codes
     */
    private static final Pattern hex3PatternFG = Pattern.compile("<fg_#([a-fA-F0-9]{3})>");
    /**
     * Explicit BG 3 and 6 letter hex codes
     */
    private static final Pattern hex6PatternBG = Pattern.compile("<bg_#([a-fA-F0-9]{6})>");
    /**
     * Explicit BG 3 and 6 letter hex codes
     */
    private static final Pattern hex3PatternBG = Pattern.compile("<bg_#([a-fA-F0-9]{3})>");
    /**
     * Explicit Underline 3 and 6 letter hex codes
     */
    private static final Pattern hex6PatternUnderline = Pattern.compile("<ul_#([a-fA-F0-9]{6})>");
    /**
     * Explicit Underline 3 and 6 letter hex codes
     */
    private static final Pattern hex3PatternUnderline = Pattern.compile("<ul_#([a-fA-F0-9]{3})>");

    /**
     * A pattern for ansi codes that also need parameters
     */
    private static final Pattern ansiPattern = Pattern.compile("(?<!\\\\)<([^>]+)>");

    /**
     * The main function for this class
     * Functionality:
     * - Hex and (changeable) predefined colors for fg, bg and underline.
     * - Default ansi colors and styling codes
     * @param message the text to process
     * @param continueStyle whether to not append a reset tag. Defaults to false.
     * @return the processed message
     */
    public static String mm(String message, boolean continueStyle) {
        if (!continueStyle) {
            message += "<reset>";
        }
        
        message = processANSI(message);
        message = processOther(message);
        message = processColor(message);
        message = processHex(message);

        return message;
    }
    /**
     * The main function for this class
     * Functionality:
     * - Hex and (changeable) predefined colors for fg, bg and underline.
     * - Default ansi colors and styling codes
     * @param message the text to process
     * @return the processed message
     */
    public static String mm(String message) {
        return mm(message, false);
    }

    /**
     * Replace \033 with \\033
     * @param message The text to process
     * @return The processed text
     */

    public static String escapeEscapeCodes(String message) {
        return message.replace("\033", "\\033");
    }


    /**
     * Processes all hex tags
     * @param message The text to process
     * @return The processed text
     */
    private static String processHex(String message) {

        message = explicitFG(message);
        message = replaceHex(message, hex6PatternFG, true, false);
        message = replaceHex(message, hex3PatternFG, true, true);
        message = replaceHex(message, hex6PatternBG, false, false);
        message = replaceHex(message, hex3PatternBG, false, true);
        message = replaceUnderline(message);

        return message;
    }

    /**
     * Turns all implicit fg hex tags into explicit fg hex tags
     * @param message The text to process
     * @return The processed text
     */
    private static String explicitFG(String message) {
        Matcher matcher = hex6Pattern.matcher(message);

        while (matcher.find()) {
            String value = matcher.group(1);

            message = message.replace("<#" + value + ">", "<fg_#" + value + ">");

        }
        matcher = hex3Pattern.matcher(message);

        while (matcher.find()) {
            String value = matcher.group(1);

            message = message.replace("<#" + value + ">", "<fg_#" + value + ">");

        }
        return message;
    }

    private static String replaceUnderline(String message) {

        Matcher matcher = hex6PatternUnderline.matcher(message);

        while (matcher.find()) {
            String value = matcher.group(1);
            int hex = Integer.parseInt(value, 16);

            int r = (hex >> 16) & 0xFF;
            int g = (hex >> 8) & 0xFF;
            int b = hex & 0xFF;

            message = message.replace("<ul_#" + value + ">", ANSICode.UNDERLINE_COLOR_RGB.format(r, g, b));

        }

        matcher = hex3PatternUnderline.matcher(message);

        while (matcher.find()) {
            String value = matcher.group(1);

            int hex = convert3Hex6Hex(Integer.parseInt(value, 16));

            int r = (hex >> 16) & 0xFF;
            int g = (hex >> 8) & 0xFF;
            int b = hex & 0xFF;

            message = message.replace("<ul_#" + value + ">", ANSICode.UNDERLINE_COLOR_RGB.format(r, g, b));

        }

        return message;

    }

    /**
     * Helper method for replacing fg and bg hex tags
     * @param message The text to process
     * @param pattern The pattern to use
     * @param foreground Whether this text is FG or BG
     * @param hex3 Whether this text uses 3 or 6 hex chars
     * @return The processed text
     */
    private static String replaceHex(String message, Pattern pattern, boolean foreground, boolean hex3) {
        Matcher matcher = pattern.matcher(message);

        while (matcher.find()) {
            String value = matcher.group(1);
            int hex;
            if (hex3) {
                hex = convert3Hex6Hex(Integer.parseInt(value, 16));
            } else {
                hex = Integer.parseInt(value, 16);
            }

            int r = (hex >> 16) & 0xFF;
            int g = (hex >> 8) & 0xFF;
            int b = hex & 0xFF;

            if (foreground) {
                message = message.replace("<fg_#" + value + ">", ANSICode.FG_RGB.format(r, g, b));
            } else {
                message = message.replace("<bg_#" + value + ">", ANSICode.BG_RGB.format(r, g, b));
            }

        }

        return message;
    }

    /**
     * Converts 3 long hex integers to 6 long hex integers
     * For example: A3C -> AA33CC
     * @param hex The 3 hex integer
     * @return The 6 hex equivalent
     */
    private static int convert3Hex6Hex(int hex) {
        int r = (hex >> 8) & 0xF;
        int g = (hex >> 4) & 0xF;
        int b = hex & 0xF;

        return ((r * 17) << 16) | ((g * 17) << 8) | (b * 17);
    }

    /**
     * Parses the default colors from resources/colors.json included in asphalt.
     */
    public static void initDefaultColors() {


        try (InputStream is = ConsoleMiniMessage.class.getResourceAsStream("/colors.json")) {
            if (is == null) {
                throw new IllegalStateException("Could not find colors.json");
            }

            Map<String, Integer> colorMap = new Gson().fromJson(
                    new InputStreamReader(is),
                    new TypeToken<Map<String, Integer>>() {}.getType()
            );

            useColors(colorMap);
        } catch (IOException e) {
            throw new RuntimeException("Failed loading colors.json", e);
        }


    }

    /**
     * Tells this class to use a different set of predefined colors
     * @param colorMap The colormap to use
     */
    public static void useColors(Map<String, Integer> colorMap) {
        colors = colorMap;
    }

    /**
     * Turns predefined color tags into hex tags.
     * @param message The text to process
     * @return The processed text
     * */
    private static String processColor(String message) {
        if (colors == null) {
            initDefaultColors();
        }

        for (Map.Entry<String, Integer> entry : colors.entrySet()) {

            message = message.replace("<" + entry.getKey() + ">", "<fg_#" + String.format("%06X", entry.getValue()) + ">");
            message = message.replace("<fg_" + entry.getKey() + ">", "<fg_#" + String.format("%06X", entry.getValue()) + ">");
            message = message.replace("<bg_" + entry.getKey() + ">", "<bg_#" + String.format("%06X", entry.getValue()) + ">");
            message = message.replace("<ul_" + entry.getKey() + ">", "<ul_#" + String.format("%06X", entry.getValue()) + ">");
        }
        return message;
    }

    /**
     * Turns tags defined by the ANSI standard into their terminal equivalents
     * @param message The text to process
     * @return The processed text
     */
    private static String processANSI(String message) {
        for (ANSIColor color : ANSIColor.values()) {

            message = message.replace("<" + color.getName() + ">", "\033[" + color.getAnsi() + "m");
            message = message.replace("<fg_" + color.getName() + ">", "\033[" + color.getAnsi() + "m");
            message = message.replace("<bg_" + color.getName() + ">", "\033[" + color.getAnsi() + 10 + "m");

        }

        message = parseANSIMM(message);

        return message;

    }

    private static String processOther(String message) {
        for (ASCIIChar charr : ASCIIChar.values()) {
            message = message.replace(charr.getName(), charr.getValue());
        }
        return message;
    }


    /**
     * Parses tags defined in ANSICode
     * @param input The text to process
     * @return The processed text
     */
    private static String parseANSIMM(String input) {
        Matcher m = ansiPattern.matcher(input);
        StringBuilder sb = new StringBuilder();
        while (m.find()) {
            String[] parts = m.group(1).split(":");
            String id = parts[0];
            ANSICode ansi = Arrays.stream(ANSICode.values())
                    .filter(e -> e.getId().equals(id))
                    .findFirst()
                    .orElse(null);
            if (ansi == null) {
                m.appendReplacement(sb, m.group());
                continue;
            }
            String[] args = Arrays.copyOfRange(parts, 1, parts.length);
            m.appendReplacement(sb, Matcher.quoteReplacement(ansi.format((Object[]) args)));
        }
        m.appendTail(sb);
        return sb.toString();
    }

    /**
     * Prints a message to System.out. Does not end in \n automatically
     * @param message the message to print.
     */
    public static void printMM(String message) {
        System.out.print(mm(message));
    }
}
