package dev.redgamer6427a.core.minecraft.common.text;


import dev.redgamer6427a.core.console.output.ConsoleMiniMessage;
import dev.redgamer6427a.core.logging.Logger;
import dev.redgamer6427a.core.processing.mm.MiniMessageParser;
import dev.redgamer6427a.core.processing.mm.MiniMessageTagProcessor;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.ObjectComponent;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.Style;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.Tag;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import net.kyori.adventure.text.object.ObjectContents;
import net.kyori.adventure.text.object.PlayerHeadObjectContents;
import org.jetbrains.annotations.NotNull;

import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class AdventureMM {

    public static MiniMessageParser parser = new MiniMessageParser();

    private static Map<String, Integer> getColors() {
        return ConsoleMiniMessage.getColors();
    }

    private static Map<String, String> textures = new ConcurrentHashMap<>(); // id -> base64 texture value

    // well-known Steve skin, safe fallback
    private static final String STEVE_TEXTURE_VALUE =
            "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMzFmNDc3ZWIxYTdiZWVlNjMxYzJjYTY0ZDA2ZjhmNjhmYTkzZmRiOGNjNmUwYjE0MDE2Yjk5NGQxYTRlZGUzZiJ9fX0=";

    private static TagResolver headTag() {
        return TagResolver.resolver("head_texture", (argumentQueue, context) -> {
            String id = argumentQueue.popOr("missing registered_id").value();
            String hash = textures.get(id);
            String value = (hash != null) ? buildTextureValue(hash) : STEVE_TEXTURE_VALUE;

            Component head = Component.object(b -> b.contents(
                    ObjectContents.playerHead(ph -> ph
                            .profileProperty(PlayerHeadObjectContents.property("textures", value)))
            ));

            return Tag.inserting(head);
        });
    }

    public static String serialize(Component component) {
        StringBuilder out = new StringBuilder();
        serializeNode(component, out);
        return out.toString();
    }

    public static String mmToConsole(Component component) {
        String mm = serialize(component);
        return ConsoleMiniMessage.mm(mm);
    }

    public static Component stripAllStyles(Component component) {
        return component.style(Style.empty())
                .children(component.children().stream()
                        .map(AdventureMM::stripAllStyles)
                        .toList());
    }

    private static TagResolver headTextureHashTag() {
        return TagResolver.resolver("head_texture_hash", (argumentQueue, context) -> {
            String hash = argumentQueue.popOr("missing hash").value();
            String value = buildTextureValue(hash);

            Component head = Component.object(b -> b.contents(
                    ObjectContents.playerHead(ph -> ph
                            .profileProperty(PlayerHeadObjectContents.property("textures", value)))
            ));

            return Tag.inserting(head);
        });
    }

    private static void serializeNode(Component component, StringBuilder out) {
        if (component instanceof ObjectComponent obj
                && obj.contents() instanceof PlayerHeadObjectContents phoc) {
            String value = phoc.profileProperties().stream()
                    .filter(p -> p.name().equals("textures"))
                    .map(PlayerHeadObjectContents.ProfileProperty::value)
                    .findFirst()
                    .orElse(null);

            if (value != null) {
                String hash = extractHash(value);
                Optional<String> knownId = findIdForHash(hash);

                if (knownId.isPresent()) {
                    out.append("<head_texture:").append(knownId.get()).append('>');
                } else {
                    out.append("<head_texture_hash:").append(hash).append('>');
                }
            }
            // fall through — do NOT return, children (trailing text) still need processing
        } else if (component instanceof TextComponent text && !text.content().isEmpty()) {
            out.append(MiniMessage.miniMessage().serialize(Component.text(text.content(), text.style())));
        }

        for (Component child : component.children()) {
            serializeNode(child, out);
        }
    }
    private static String extractHash(String base64Value) {
        String json = new String(Base64.getDecoder().decode(base64Value), StandardCharsets.UTF_8);
        int idx = json.indexOf("texture/");
        if (idx == -1) return base64Value;
        int start = idx + "texture/".length();
        int end = json.indexOf('"', start);
        return json.substring(start, end);
    }

    private static Optional<String> findIdForHash(String hash) {
        return textures.entrySet().stream()
                .filter(e -> e.getValue().equals(hash))
                .map(Map.Entry::getKey)
                .findFirst();
    }

    private static final MiniMessage MINI_MESSAGE = MiniMessage.builder()
            .tags(TagResolver.builder()
                    .resolver(TagResolver.standard())
                    .resolver(headTag())
                    .resolver(headTextureHashTag())
                    .build())
            .build();

    public static Component mm(String mm, MiniMessageParser parser) {
        String parsed = parser.parse(mm);

        return MINI_MESSAGE.deserialize(parsed);
    }

    public static String serializedMM(String mm, MiniMessageParser parser) {
        return parser.parse(mm);
    }

    public static void registerHead(String id, String textureHash) {
        textures.put(id, textureHash);
    }

    public static void unregisterHead(String id) {
        textures.remove(id);

    }

    public static String buildTextureValue(String mojangTextureHash) {
        String json = "{\"textures\":{\"SKIN\":{\"url\":\"%s\"}}}".formatted("http://textures.minecraft.net/texture/" + mojangTextureHash);
        return Base64.getEncoder().encodeToString(json.getBytes(StandardCharsets.UTF_8));
    }

    public static void buildParser() {

        // FIX UP THE VARS
        MiniMessageParser parser_ = new MiniMessageParser();

        MiniMessageTagProcessor placeholderProcessor = new MiniMessageTagProcessor();
        placeholderProcessor.parameterTag("%", 1, strings -> {
            System.out.println(strings);
            if (placeholdersById.get(strings.getFirst().toLowerCase()) == null) {
                System.out.println(placeholdersById.get(strings.getFirst().toLowerCase()) + " is null");
                return "<%:" + strings.getFirst() + ">";
            } else {
                System.out.println(placeholdersById.get(strings.getFirst().toLowerCase()) + " is not null");
                return placeholdersById.get(strings.getFirst().toLowerCase());
            }
        }, "<%>");
        parser_.addProcessor(placeholderProcessor);
        // <COLOR> tags

        MiniMessageTagProcessor predefinedProcessor = new MiniMessageTagProcessor();
        for (Map.Entry<String, Integer> entry : getColors().entrySet()) {
            predefinedProcessor.simpleTag(entry.getKey(), "#" + HexFormat.of().toHexDigits(entry.getValue()).substring(2));
        }
        parser_.addProcessor(predefinedProcessor);
        List<String> allowedDefaultColors = getDefaultColors();


        // <t_COLOR> tags
        MiniMessageTagProcessor predefinedProcessor_explicitText = new MiniMessageTagProcessor();
        for (Map.Entry<String, Integer> entry : getColors().entrySet()) {
            predefinedProcessor_explicitText.simpleTag("t:" + entry.getKey(), "#" + HexFormat.of().toHexDigits(entry.getValue()).substring(2));
        }
        parser_.addProcessor(predefinedProcessor_explicitText);


        // <s_COLOR> tags
        MiniMessageTagProcessor predefinedProcessor_explicitShadow = new MiniMessageTagProcessor();
        for (Map.Entry<String, Integer> entry : getColors().entrySet()) {
            predefinedProcessor_explicitShadow.simpleTag("s:" + entry.getKey(), "shadow:#" + HexFormat.of().toHexDigits(entry.getValue()).substring(2));
        }
        parser_.addProcessor(predefinedProcessor_explicitShadow);


        // <text_COLOR> tags
        MiniMessageTagProcessor predefinedProcessor_explicitTextLong = new MiniMessageTagProcessor();
        for (Map.Entry<String, Integer> entry : getColors().entrySet()) {
            predefinedProcessor_explicitTextLong.simpleTag("text:" + entry.getKey(), "#" + HexFormat.of().toHexDigits(entry.getValue()).substring(2));
        }
        parser_.addProcessor(predefinedProcessor_explicitTextLong);


        // <shadow_COLOR> tags
        MiniMessageTagProcessor predefinedProcessor_explicitShadowLong = new MiniMessageTagProcessor();
        for (Map.Entry<String, Integer> entry : getColors().entrySet()) {
            predefinedProcessor_explicitShadowLong.simpleTag("shadow:" + entry.getKey(), "shadow:#" + HexFormat.of().toHexDigits(entry.getValue()).substring(2));
        }
        parser_.addProcessor(predefinedProcessor_explicitShadowLong);

        // <&_DEFAULT_COLOR> tags
        MiniMessageTagProcessor defaultColorTagProcessor = new MiniMessageTagProcessor();

        for (String defaultColor : allowedDefaultColors) {
            defaultColorTagProcessor.simpleTag("&" + defaultColor, defaultColor);
        }
        parser_.addProcessor(defaultColorTagProcessor);
        // <t_&_DEFAULT_COLOR> tags
        MiniMessageTagProcessor defaultColorTagProcessor_explicitText = new MiniMessageTagProcessor();

        for (String defaultColor : allowedDefaultColors) {
            defaultColorTagProcessor_explicitText.simpleTag("t&:" + defaultColor, defaultColor);
        }
        parser_.addProcessor(defaultColorTagProcessor_explicitText);
        // <text_&_DEFAULT_COLOR> tags
        MiniMessageTagProcessor defaultColorTagProcessor_explicitTextLong = new MiniMessageTagProcessor();

        for (String defaultColor : allowedDefaultColors) {
            defaultColorTagProcessor_explicitTextLong.simpleTag("text_&:" + defaultColor, defaultColor);
        }
        parser_.addProcessor(defaultColorTagProcessor_explicitTextLong);
        // <s&:DEFAULT_COLOR> tags
        MiniMessageTagProcessor defaultColorTagProcessor_explicitShadow = new MiniMessageTagProcessor();

        for (String defaultColor : allowedDefaultColors) {
            defaultColorTagProcessor_explicitShadow.simpleTag("s&:" + defaultColor, "shadow:" + defaultColor);
        }

        parser_.addProcessor(defaultColorTagProcessor_explicitShadow);
        // <shadow_&_DEFAULT_COLOR> tags
        MiniMessageTagProcessor defaultColorTagProcessor_explicitShadowLong = new MiniMessageTagProcessor();

        for (String defaultColor : allowedDefaultColors) {
            defaultColorTagProcessor_explicitShadowLong.simpleTag("shadow_&:" + defaultColor, "shadow:" + defaultColor);
        }

        parser_.addProcessor(defaultColorTagProcessor_explicitShadowLong);

        MiniMessageTagProcessor predefinedGradientsProcessor = new MiniMessageTagProcessor();
        predefinedGradientsProcessor.simpleTag("rainbow", "gradient:red:yellow:green:blue:purple");
        parser_.addProcessor(predefinedGradientsProcessor);
        MiniMessageTagProcessor gradientProcessor = new MiniMessageTagProcessor();
        gradientProcessor.regexTag("gradient((?::[^:>]+)+)", groups -> {
            String[] tokens = groups.getFirst().substring(1).split(":"); // drop leading ':'
            StringBuilder out = new StringBuilder("gradient");
            for (String t : tokens) {
                out.append(':').append(toHex(t));
            }

            return out.toString();
        });

        parser_.addProcessor(gradientProcessor);


        parser = parser_;
    }


    private static final Map<String, String> placeholdersById = new ConcurrentHashMap<>();

    public static void setPlaceholder(String id, String value) {
        placeholdersById.put(id.toLowerCase(), value);
    }

    public static void deletePlaceholder(String id) {
        placeholdersById.remove(id.toLowerCase());
    }

    public static String getPlaceholder(String id) {
        return placeholdersById.get(id.toLowerCase());
    }

    private static final Logger logger = Logger.create();

    private static @NotNull List<String> getDefaultColors() {
        List<String> allowedDefaultColors = List.of(
                "black",
                "dark_blue",
                "dark_green",
                "dark_aqua",
                "dark_red",
                "dark_purple",
                "gold",
                "gray",
                "dark_gray",
                "blue",
                "green",
                "aqua",
                "red",
                "light_purple",
                "yellow",
                "white"
        );
        return allowedDefaultColors;
    }

    private static String toHex(String token) {
        if (token.startsWith("&") && getDefaultColors().contains(token.substring(1).toLowerCase())) {
            return token.substring(1);
        }
        if (token.startsWith("#")) return token.toLowerCase();
        Integer rgb = getColors().get(token.toLowerCase());
        if (rgb == null) return token;
        return String.format("#%06x", rgb);
    }

    public static Component mm(String mm) {
        return mm(mm, parser);
    }

    public static String serializedMM(String mm) {
        return serializedMM(mm, parser);
    }

}
