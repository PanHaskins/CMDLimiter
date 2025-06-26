package me.panhaskins.cmdlimit.utils;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.bungeecord.BungeeComponentSerializer;
import net.md_5.bungee.api.chat.BaseComponent;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import me.clip.placeholderapi.PlaceholderAPI;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Messager – stateless utility for translating chat strings.
 *
 * Supported input:
 *  • Legacy colours (&0-&f / §0-§f) + &r reset
 *  • Legacy decorations (&l &o &m &n &k) – auto-closed on next colour / reset
 *  • Hex colours (&#RRGGBB, §#RRGGBB, &x&R&R&G&G&B&B, §x&R&R&G&G&B&B)
 *  • Fancy gradients {#ff0000>} … {#00ff00<>} … {#0000ff<}
 *  • PlaceholderAPI (optional)
 *  • MiniMessage formatting
 *
 * Public API:
 *  translate(...) → Adventure Component
 *  translateToBaseComponent(...) → Spigot BaseComponent[]
 */
public final class Messager {

    // MiniMessage instance used for all deserialization.
    private static final MiniMessage MINIMESSAGE = MiniMessage.miniMessage();

    // Flag for PlaceholderAPI presence.
    private static final boolean PLACEHOLDER_API =
            Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI");

    // Precompiled regex patterns for color and formatting.
    private static final Pattern LEGACY_PATTERN =
            Pattern.compile("(?i)[&§]([0-9A-FK-OR])");
    private static final Pattern SIMPLE_HEX_PATTERN =
            Pattern.compile("(?i)(?:&|§)#([0-9A-F]{6})");
    private static final Pattern BRACE_HEX_PATTERN =
            Pattern.compile("\\{#([0-9A-Fa-f]{6})}");
    private static final Pattern GRADIENT_TOKEN =
            Pattern.compile("\\{#([0-9A-Fa-f]{6})(>|<>|<)}");
    private static final Pattern TAG_PATTERN =
            Pattern.compile("<(/?)([^>]+?)>");

    // Maps legacy color codes to MiniMessage tags.
    private static final Map<Character, String> LEGACY_MAP = Map.ofEntries(
            Map.entry('0', "<black>"),          Map.entry('8', "<dark_gray>"),
            Map.entry('1', "<dark_blue>"),      Map.entry('9', "<blue>"),
            Map.entry('2', "<dark_green>"),     Map.entry('a', "<green>"),
            Map.entry('3', "<dark_aqua>"),      Map.entry('b', "<aqua>"),
            Map.entry('4', "<dark_red>"),       Map.entry('c', "<red>"),
            Map.entry('5', "<dark_purple>"),    Map.entry('d', "<light_purple>"),
            Map.entry('6', "<gold>"),           Map.entry('e', "<yellow>"),
            Map.entry('7', "<gray>"),           Map.entry('f', "<white>"),
            Map.entry('k', "<obfuscated>"),     Map.entry('l', "<bold>"),
            Map.entry('m', "<strikethrough>"),  Map.entry('n', "<underlined>"),
            Map.entry('o', "<italic>"),         Map.entry('r', "<reset>")
    );

    // Set of decoration tags used for auto-closing decorations.
    private static final Set<String> DECORATION_TAGS = Set.of(
            "bold", "italic", "obfuscated", "strikethrough", "underlined"
    );

    // Set of MiniMessage color names for decoration closing logic.
    private static final Set<String> COLOUR_NAMES = Set.of(
            "black","dark_blue","dark_green","dark_aqua","dark_red","dark_purple",
            "gold","gray","dark_gray","blue","green","aqua","red",
            "light_purple","yellow","white"
    );

    /**
     * Translates a raw chat string to a MiniMessage Component.
     * Does not use PlaceholderAPI.
     */
    public static Component translate(String message) {
        return internalTranslate(message, null);
    }

    /**
     * Translates a chat string and applies PlaceholderAPI for a Player.
     */
    public static Component translate(String message, Player player) {
        return internalTranslate(message, player);
    }

    /**
     * Translates a chat string and applies PlaceholderAPI for an OfflinePlayer.
     */
    public static Component translate(String message, OfflinePlayer offlinePlayer) {
        return internalTranslate(message, offlinePlayer);
    }

    /**
     * Translates a list of chat strings to a list of Components.
     * Does not use PlaceholderAPI.
     */
    public static List<Component> translate(List<String> lines) {
        return translate(lines, null);
    }

    /**
     * Translates a list of chat strings and applies PlaceholderAPI for an OfflinePlayer.
     */
    public static List<Component> translate(List<String> lines, OfflinePlayer offlinePlayer) {
        List<Component> list = new ArrayList<>(lines.size());
        for (String s : lines) list.add(internalTranslate(s, offlinePlayer));
        return list;
    }

    /**
     * Translates a chat string and returns a Spigot BaseComponent array.
     * Does not use PlaceholderAPI.
     */
    public static BaseComponent[] translateToBaseComponents(String message) {
        return BungeeComponentSerializer.get().serialize(translate(message));
    }

    /**
     * Translates a chat string and returns a Spigot BaseComponent array with PlaceholderAPI for OfflinePlayer.
     */
    public static BaseComponent[] translateToBaseComponents(String message, OfflinePlayer offlinePlayer) {
        return BungeeComponentSerializer.get().serialize(translate(message, offlinePlayer));
    }

    /**
     * Translates a list of chat strings to a list of BaseComponent arrays.
     * Does not use PlaceholderAPI.
     */
    public static List<BaseComponent[]> translateToBaseComponents(List<String> lines) {
        return translateToBaseComponents(lines, null);
    }

    /**
     * Translates a list of chat strings to BaseComponent arrays with PlaceholderAPI for OfflinePlayer.
     */
    public static List<BaseComponent[]> translateToBaseComponents(List<String> lines, OfflinePlayer offlinePlayer) {
        List<BaseComponent[]> list = new ArrayList<>(lines.size());
        for (String s : lines) list.add(translateToBaseComponents(s, offlinePlayer));
        return list;
    }

    /**
     * Main translation pipeline.
     * Applies PlaceholderAPI, handles legacy, gradient and hex color translation,
     * closes decorations, then deserializes using MiniMessage.
     */
    private static Component internalTranslate(String message, OfflinePlayer offlinePlayer) {
        if (message == null || message.isEmpty()) return Component.empty();

        String text = applyPlaceholders(message, offlinePlayer);

        boolean legacy   = text.indexOf('&') >= 0 || text.indexOf('§') >= 0;
        boolean gradient = text.contains("{#") && (text.contains(">}") || text.contains("<}"));
        boolean hash     = text.indexOf('#') >= 0;

        if (legacy)   text = translateLegacy(text);
        if (gradient) text = parseGradients(text);
        if (hash)     text = translateHex(text);

        text = closeDecorations(text);
        return MINIMESSAGE.deserialize(text);
    }

    /**
     * Replaces all placeholders if PlaceholderAPI is present and viewer is not null.
     */
    private static String applyPlaceholders(String input, OfflinePlayer offlinePlayer) {
        if (offlinePlayer != null && PLACEHOLDER_API)
            return PlaceholderAPI.setPlaceholders(offlinePlayer, input);
        return input;
    }

    /**
     * Converts legacy color codes (& or §) to MiniMessage tags.
     */
    private static String translateLegacy(String input) {
        Matcher legacyMatcher = LEGACY_PATTERN.matcher(input);
        StringBuffer output = new StringBuffer();
        while (legacyMatcher.find()) {
            char code = Character.toLowerCase(legacyMatcher.group(1).charAt(0));
            legacyMatcher.appendReplacement(output, LEGACY_MAP.getOrDefault(code, ""));
        }
        return legacyMatcher.appendTail(output).toString();
    }

    /**
     * Converts hex codes (including §x§F§F§F§F§F§F and &#abcdef, {#abcdef}) to MiniMessage tags.
     */
    private static String translateHex(String input) {
        input = translateXHex(input);
        input = SIMPLE_HEX_PATTERN.matcher(input).replaceAll("<#$1>");
        return BRACE_HEX_PATTERN.matcher(input).replaceAll("<#$1>");
    }

    /**
     * Converts Bukkit hex notation (&x&F&F&F&F&F&F) to MiniMessage hex tags.
     */
    private static String translateXHex(String input) {
        StringBuilder out = new StringBuilder(input.length());
        for (int i = 0; i < input.length(); ) {
            char c = input.charAt(i);
            if ((c == '&' || c == '§') &&
                    i + 13 < input.length() &&
                    (input.charAt(i + 1) == 'x' || input.charAt(i + 1) == 'X')) {

                StringBuilder hex = new StringBuilder(6);
                boolean ok = true;
                for (int j = 0; j < 6; j++) {
                    char pref = input.charAt(i + 2 + j * 2);
                    char dig  = input.charAt(i + 3 + j * 2);
                    if ((pref != '&' && pref != '§') || !isHex(dig)) { ok = false; break; }
                    hex.append(dig);
                }
                if (ok) { out.append("<#").append(hex).append('>'); i += 14; continue; }
            }
            out.append(c); i++;
        }
        return out.toString();
    }

    /**
     * Checks if a character is a hexadecimal digit.
     */
    private static boolean isHex(char ch) {
        return (ch >= '0' && ch <= '9') ||
                (ch >= 'a' && ch <= 'f') ||
                (ch >= 'A' && ch <= 'F');
    }

    /**
     * Converts gradient notations to MiniMessage gradient tags.
     * Supports format: {#FF00FF>} ... {#FFFF00<>} ... {#CCCCCC<}
     */
    private static String parseGradients(String input) {
        Matcher gradientMatcher = GRADIENT_TOKEN.matcher(input);

        StringBuilder output      = new StringBuilder();
        StringBuilder innerBuffer = new StringBuilder();
        List<String> colours      = new ArrayList<>(4);

        int last = 0;
        boolean open = false;

        while (gradientMatcher.find()) {
            if (open) innerBuffer.append(input, last, gradientMatcher.start());
            else      output.append(input, last, gradientMatcher.start());

            String colour = gradientMatcher.group(1).toLowerCase(Locale.ROOT);
            String ctl    = gradientMatcher.group(2);

            switch (ctl) {
                case ">"  -> { open = true; colours.clear(); colours.add(colour); innerBuffer.setLength(0); }
                case "<>" -> { if (open) colours.add(colour); else output.append(gradientMatcher.group()); }
                case "<"  -> {
                    if (open) {
                        colours.add(colour);
                        output.append("<gradient:#")
                                .append(String.join(":#", colours))
                                .append('>')
                                .append(innerBuffer)
                                .append("</gradient>");
                        open = false;
                    } else output.append(gradientMatcher.group());
                }
            }
            last = gradientMatcher.end();
        }

        if (open) {
            output.append("{#").append(colours.getFirst()).append('>');
            output.append(innerBuffer);
            output.append(input.substring(last));
        } else output.append(input.substring(last));
        return output.toString();
    }

    /**
     * Closes all open decorations (bold, italic, etc.) before a color/gradient/reset tag.
     * Matches vanilla Minecraft formatting reset logic.
     */
    private static String closeDecorations(String input) {
        Matcher tagMatcher = TAG_PATTERN.matcher(input);
        Deque<String> stack = new ArrayDeque<>();
        StringBuilder out   = new StringBuilder();
        int last = 0;

        while (tagMatcher.find()) {
            out.append(input, last, tagMatcher.start());

            String slash = tagMatcher.group(1);
            String name  = tagMatcher.group(2).toLowerCase(Locale.ROOT);
            boolean open = slash.isEmpty();

            if (open && DECORATION_TAGS.contains(name)) {
                stack.push(name); out.append(tagMatcher.group());
            } else {
                if (isColour(name) || "reset".equals(name)) {
                    while (!stack.isEmpty()) out.append("</").append(stack.pop()).append('>');
                }
                out.append(tagMatcher.group());
                if (!open && DECORATION_TAGS.contains(name))
                    stack.removeFirstOccurrence(name);
            }
            last = tagMatcher.end();
        }
        out.append(input.substring(last));
        while (!stack.isEmpty()) out.append("</").append(stack.pop()).append('>');
        return out.toString();
    }

    /**
     * Checks if the tag is a color or gradient.
     */
    private static boolean isColour(String tag) {
        return tag.startsWith("#") || tag.startsWith("gradient") || COLOUR_NAMES.contains(tag);
    }
}