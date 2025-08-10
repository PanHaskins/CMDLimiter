package me.panhaskins.cmdlimit.utils;

import me.clip.placeholderapi.PlaceholderAPI;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.bungeecord.BungeeComponentSerializer;
import net.md_5.bungee.api.chat.BaseComponent;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Utility class for translating legacy and modern formatting codes into Adventure Components.
 * Supports:
 * - Legacy color codes (&0-9a-f, §0-9a-f)
 * - Decorations (&l, &o, &m, &n, &k), auto-closed on next color or reset (&r)
 * - Reset (&r, §r)
 * - Hex colors (&x&F&F&8&8&0&0, &#ff8800, {#ff8800})
 * - Gradient shortcodes ({#ff0000>}, {#00ff00<>}, {#0000ff<})
 * - MiniMessage tags (passed through unchanged)
 * - PlaceholderAPI (if available and viewer is provided)
 */
public final class Messager {

    private static final MiniMessage MINI_MESSAGE = MiniMessage.miniMessage();
    private static final boolean PLACEHOLDER_API =
            Bukkit.getServer() != null && Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI");

    private static final Pattern SIMPLE_HEX_PATTERN = Pattern.compile("(?i)(?:&|§)#([0-9A-F]{6})");
    private static final Pattern BRACE_HEX_PATTERN = Pattern.compile("\\{#([0-9A-Fa-f]{6})}");
    private static final Pattern GRADIENT_TOKEN_PATTERN = Pattern.compile("\\{#([0-9A-Fa-f]{6})(>|<>|<)}");
    private static final String HOVER_TEXT_PREFIX = "<hover:show_text:";

    private static final Map<Character, String> COLOUR_MAP = Map.ofEntries(
            Map.entry('0', "<black>"),          Map.entry('8', "<dark_gray>"),
            Map.entry('1', "<dark_blue>"),      Map.entry('9', "<blue>"),
            Map.entry('2', "<dark_green>"),     Map.entry('a', "<green>"),
            Map.entry('3', "<dark_aqua>"),      Map.entry('b', "<aqua>"),
            Map.entry('4', "<dark_red>"),       Map.entry('c', "<red>"),
            Map.entry('5', "<dark_purple>"),    Map.entry('d', "<light_purple>"),
            Map.entry('6', "<gold>"),           Map.entry('e', "<yellow>"),
            Map.entry('7', "<gray>"),           Map.entry('f', "<white>")
    );

    private static final Map<Character, String> DECORATION_MAP = Map.ofEntries(
            Map.entry('k', "obfuscated"),
            Map.entry('l', "bold"),
            Map.entry('m', "strikethrough"),
            Map.entry('n', "underlined"),
            Map.entry('o', "italic")
    );

    /**
     * Translates a single message into an Adventure Component.
     *
     * @param message message to translate
     * @return translated Component
     */
    public static Component translate(String message) {
        return internalTranslate(message, null);
    }

    /**
     * Translates a single message into an Adventure Component with placeholder support.
     *
     * @param message message to translate
     * @param viewer  player for placeholders
     * @return translated Component
     */
    public static Component translate(String message, Player viewer) {
        return internalTranslate(message, viewer);
    }

    /**
     * Translates a single message into an Adventure Component with placeholder support.
     *
     * @param message message to translate
     * @param viewer  offline player for placeholders
     * @return translated Component
     */
    public static Component translate(String message, OfflinePlayer viewer) {
        return internalTranslate(message, viewer);
    }

    /**
     * Translates a list of messages into a list of Components.
     *
     * @param lines list of messages to translate
     * @return list of translated Components
     */
    public static List<Component> translate(List<String> lines) {
        return translate(lines, null);
    }

    /**
     * Translates a list of messages into a list of Components with placeholder support.
     *
     * @param lines  list of messages to translate
     * @param viewer player for placeholders
     * @return list of translated Components
     */
    public static List<Component> translate(List<String> lines, OfflinePlayer viewer) {
        Objects.requireNonNull(lines, "lines");
        List<Component> result = new ArrayList<>(lines.size());
        for (String line : lines) {
            result.add(internalTranslate(line, viewer));
        }
        return result;
    }

    /**
     * Translates a single message into an array of BaseComponents.
     *
     * @param message message to translate
     * @return BaseComponent array
     */
    public static BaseComponent[] translateToBaseComponents(String message) {
        return BungeeComponentSerializer.get().serialize(translate(message));
    }

    /**
     * Translates a single message into an array of BaseComponents with placeholder support.
     *
     * @param message message to translate
     * @param viewer  player for placeholders
     * @return BaseComponent array
     */
    public static BaseComponent[] translateToBaseComponents(String message, OfflinePlayer viewer) {
        return BungeeComponentSerializer.get().serialize(translate(message, viewer));
    }

    /**
     * Translates a list of messages into a list of BaseComponent arrays.
     *
     * @param lines list of messages to translate
     * @return list of BaseComponent arrays
     */
    public static List<BaseComponent[]> translateToBaseComponents(List<String> lines) {
        return translateToBaseComponents(lines, null);
    }

    /**
     * Translates a list of messages into a list of BaseComponent arrays with placeholder support.
     *
     * @param lines  list of messages to translate
     * @param viewer player for placeholders
     * @return list of BaseComponent arrays
     */
    public static List<BaseComponent[]> translateToBaseComponents(List<String> lines, OfflinePlayer viewer) {
        Objects.requireNonNull(lines, "lines");
        List<BaseComponent[]> result = new ArrayList<>(lines.size());
        for (String line : lines) {
            result.add(translateToBaseComponents(line, viewer));
        }
        return result;
    }

    /**
     * Internal pipeline for processing messages.
     *
     * @param message message to process
     * @param viewer  viewer for placeholders
     * @return translated Component
     */
    private static Component internalTranslate(String message, OfflinePlayer viewer) {
        if (message == null || message.isEmpty()) return Component.empty();

        String processed = applyPlaceholders(message, viewer);
        processed = parseGradients(processed);
        processed = translateLegacyCodes(processed);
        if (processed.indexOf('#') >= 0) {
            processed = SIMPLE_HEX_PATTERN.matcher(processed).replaceAll("<#$1>");
            processed = BRACE_HEX_PATTERN.matcher(processed).replaceAll("<#$1>");
        }

        return MINI_MESSAGE.deserialize(processed);
    }

    /**
     * Applies PlaceholderAPI placeholders if available and viewer is set.
     *
     * @param input  input message
     * @param viewer viewer for placeholders
     * @return message with placeholders applied
     */
    private static String applyPlaceholders(String input, OfflinePlayer viewer) {
        return viewer != null && PLACEHOLDER_API
                ? PlaceholderAPI.setPlaceholders(viewer, input)
                : input;
    }

    /**
     * Parses gradient shorthands into MiniMessage gradient tags.
     *
     * @param input input string
     * @return string with gradients replaced
     */
    private static String parseGradients(String input) {
        if (input.indexOf('{') < 0) {
            return input;
        }

        Matcher matcher = GRADIENT_TOKEN_PATTERN.matcher(input);
        if (!matcher.find()) {
            return input;
        }
        matcher.reset();

        StringBuilder output = new StringBuilder(input.length());
        StringBuilder buffer = new StringBuilder();
        List<String> colours = new ArrayList<>(4);

        int pos = 0;
        boolean open = false;

        while (matcher.find()) {
            if (open) buffer.append(input, pos, matcher.start());
            else      output.append(input, pos, matcher.start());

            String colour = matcher.group(1).toLowerCase(Locale.ROOT);
            String control = matcher.group(2);

            switch (control) {
                case ">"  -> { open = true; colours.clear(); colours.add(colour); buffer.setLength(0); }
                case "<>" -> { if (open) colours.add(colour); else output.append(matcher.group()); }
                case "<"  -> {
                    if (open) {
                        colours.add(colour);
                        output.append("<gradient:#")
                                .append(String.join(":#", colours))
                                .append('>')
                                .append(buffer)
                                .append("</gradient>");
                        open = false;
                    } else output.append(matcher.group());
                }
            }
            pos = matcher.end();
        }

        if (open) {
            output.append("{#").append(colours.getFirst()).append('>').append(buffer);
        }
        output.append(input.substring(pos));
        return output.toString();
    }

    /**
     * Translates legacy color and decoration codes and automatically closes decorations.
     * &x hex code is converted to MiniMessage hex tag.
     * Decorations are closed after the next color or reset code.
     *
     * @param input input string with legacy codes
     * @return string with MiniMessage tags
     */
    private static String translateLegacyCodes(String input) {
        if (input.indexOf('&') < 0 && input.indexOf('§') < 0) {
            return input;
        }

        StringBuilder output = new StringBuilder(input.length());
        Deque<String> decorationStack = new ArrayDeque<>();

        char[] characters = input.toCharArray();
        boolean insideHoverText = false;
        char hoverDelimiter = '\'';

        for (int i = 0; i < characters.length; i++) {
            char currentChar = characters[i];

            if (!insideHoverText && currentChar == '<'
                    && i + HOVER_TEXT_PREFIX.length() < characters.length
                    && input.startsWith(HOVER_TEXT_PREFIX, i)) {
                output.append(HOVER_TEXT_PREFIX);
                i += HOVER_TEXT_PREFIX.length();
                currentChar = characters[i];
                if (currentChar == '\'' || currentChar == '"') {
                    hoverDelimiter = currentChar;
                    insideHoverText = true;
                }
                output.append(currentChar);
                continue;
            }

            if (insideHoverText && currentChar == hoverDelimiter) {
                closeDecorations(decorationStack, output);
                insideHoverText = false;
                output.append(currentChar);
                continue;
            }

            if (currentChar == '<' && i + 1 < characters.length && characters[i + 1] == '/') {
                closeDecorations(decorationStack, output);
            }

            if ((currentChar == '&' || currentChar == '§') && i + 1 < characters.length) {
                char codeChar = Character.toLowerCase(characters[i + 1]);

                // &x hex colour code (&x&R&R&G&G&B&B)
                if (codeChar == 'x' && i + 13 < characters.length) {
                    StringBuilder hexDigits = new StringBuilder(6);
                    boolean valid = true;
                    for (int j = 0; j < 6; j++) {
                        char prefix = characters[i + 2 + j * 2];
                        char digit = characters[i + 3 + j * 2];
                        if ((prefix != '&' && prefix != '§') || Character.digit(digit, 16) < 0) {
                            valid = false;
                            break;
                        }
                        hexDigits.append(digit);
                    }
                    if (valid) {
                        closeDecorations(decorationStack, output);
                        output.append("<#").append(hexDigits).append('>');
                        i += 13;
                        continue;
                    }
                }

                String colourTag = COLOUR_MAP.get(codeChar);
                if (colourTag != null) {
                    closeDecorations(decorationStack, output);
                    output.append(colourTag);
                    i++;
                    continue;
                }

                String decoration = DECORATION_MAP.get(codeChar);
                if (decoration != null) {
                    output.append('<').append(decoration).append('>');
                    decorationStack.push(decoration);
                    i++;
                    continue;
                }

                if (codeChar == 'r') {
                    closeDecorations(decorationStack, output);
                    output.append("<reset>");
                    i++;
                    continue;
                }
            }
            output.append(currentChar);
        }
        closeDecorations(decorationStack, output);
        return output.toString();
    }

    /**
     * Closes all open decoration tags in the stack.
     *
     * @param decorations stack containing opened decorations
     * @param builder     target builder for the closing tags
     */
    private static void closeDecorations(Deque<String> decorations, StringBuilder builder) {
        while (!decorations.isEmpty()) {
            String name = decorations.pop();
            builder.append("</").append(name).append('>');
        }
    }
}
