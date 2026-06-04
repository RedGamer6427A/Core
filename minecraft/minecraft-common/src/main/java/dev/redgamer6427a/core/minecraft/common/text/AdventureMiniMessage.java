package dev.redgamer6427a.core.minecraft.common.text;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import dev.redgamer6427a.core.console.output.ConsoleMiniMessage;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.Style;
import net.kyori.adventure.text.minimessage.MiniMessage;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class AdventureMiniMessage {

    private static final Map<String, Integer> TAGS;

    static {
        InputStream is = ConsoleMiniMessage.class.getResourceAsStream("/colors.json");
        if (is == null) {
            throw new IllegalStateException("Could not find colors.json");
        }
        try (
                InputStreamReader reader = new InputStreamReader(is)) {
            TAGS = new Gson().fromJson(reader,
                    new TypeToken<Map<String, Integer>>() {
                    }.getType());

        } catch (IOException e) {
            throw new IllegalStateException(e);
        }

    }

    public static String replaceColors(String text){
        if(text == null || text.isEmpty()) return text;
        String result = text;
        for(Map.Entry<String, Integer> entry : TAGS.entrySet()) {
            String hex = String.format("#%06X", entry.getValue());
            result = result.replaceAll("(?i)(?<!\\\\)<(" + entry.getKey() + ")>", "<" +hex + ">");
        }
        return result;

    }

    public static Component mm(String input) {
        if(input == null){
            return Component.empty();
        }
        return MiniMessage.miniMessage().deserialize(replaceColors(input));

    }

    public static String mmToConsole(String input) {
        String result = replaceColors(input); // first expand <RED> etc. to <#FF6600>

        // Pattern for hex colors: <#RRGGBB>
        Pattern hexPattern = Pattern.compile("(?i)(?<!\\\\)<#([0-9a-f]{6})>");
        Matcher hexMatcher = hexPattern.matcher(result);

        StringBuffer sb = new StringBuffer();
        while (hexMatcher.find()) {
            int hex = Integer.parseInt(hexMatcher.group(1), 16);
            String ansi = TerminalStyle.hexToAnsi(hex);
            hexMatcher.appendReplacement(sb, Matcher.quoteReplacement(ansi));
        }
        hexMatcher.appendTail(sb);

        // Now handle deco tags: <bold>, <italic>, <strikethrough>, etc.
        String styled = sb.toString();

        Map<String, TerminalStyle> styleMap = Map.ofEntries(
                Map.entry("bold", TerminalStyle.STYLE_BOLD),
                Map.entry("/bold", TerminalStyle.STYLE_BOLD_OFF),
                Map.entry("italic", TerminalStyle.STYLE_ITALIC),
                Map.entry("/italic", TerminalStyle.STYLE_ITALIC_OFF),
                Map.entry("underlined", TerminalStyle.STYLE_UNDERLINED),
                Map.entry("/underlined", TerminalStyle.STYLE_UNDERLINED_OFF),
                Map.entry("reset", TerminalStyle.RESET),
                Map.entry("b", TerminalStyle.STYLE_BOLD),
                Map.entry("/b", TerminalStyle.STYLE_BOLD_OFF),
                Map.entry("i", TerminalStyle.STYLE_ITALIC),
                Map.entry("/i", TerminalStyle.STYLE_ITALIC_OFF),
                Map.entry("u", TerminalStyle.STYLE_UNDERLINED),
                Map.entry("/u", TerminalStyle.STYLE_UNDERLINED_OFF)
        );

        // Regex for any <word> tag
        Pattern stylePattern = Pattern.compile("(?i)(?<!\\\\)<(/?[a-z_]+)>");
        Matcher styleMatcher = stylePattern.matcher(styled);

        sb = new StringBuffer();
        while (styleMatcher.find()) {
            String tag = styleMatcher.group(1).toLowerCase();
            TerminalStyle ts = styleMap.get(tag);
            if (ts != null) {
                styleMatcher.appendReplacement(sb, Matcher.quoteReplacement(ts.toString()));
            } else {
                // leave unknown tags unchanged
                styleMatcher.appendReplacement(sb, "<" + tag + ">");
            }
        }
        styleMatcher.appendTail(sb);

        return sb.toString();
    }


    public static String mmToConsole(Component input) {
        return mmToConsole(MiniMessage.miniMessage().serialize(mm(MiniMessage.miniMessage().serialize(input))));
    }

    public static Component stripAllStyles(Component component) {
        return component.style(Style.empty())
                .children(component.children().stream()
                        .map(AdventureMiniMessage::stripAllStyles)
                        .toList());
    }

    public static String serialize(Component component){
        String s = MiniMessage.miniMessage().serialize(component);

        Pattern pattern = Pattern.compile("(?i)(?<!\\\\)<#([0-9a-f]{6})>");
        Matcher matcher = pattern.matcher(s);

        StringBuffer sb = new StringBuffer();
        while (matcher.find()) {
            int hex = Integer.parseInt(matcher.group(1), 16);

            for (Tags color : Tags.values()) {
                if (color.getHex() == hex){
                    matcher.appendReplacement(sb, "<"+Matcher.quoteReplacement(color.getName().toLowerCase())+">");
                }
            }
        }
        matcher.appendTail(sb);
        return sb.toString();
    }


}
