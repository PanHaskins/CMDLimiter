package me.panhaskins.cmdlimit.api;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.ChatColor;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * A utility class for handling color and formatting in Minecraft text.
 * Supports legacy color codes, RGB formats, gradients, and MiniMessage integration.
 * Designed to be universal and compatible with Paper API.
 */
public class ColorMessage {

    // Pattern for legacy Minecraft color codes (e.g., &a, &l, &r)
    private static final Pattern LEGACY_PATTERN = Pattern.compile("(?i)&([0-9a-fk-or])");

    // Pattern for RGB hex codes (e.g., &#RRGGBB, #RRGGBB, <#RRGGBB>, {#RRGGBB})
    private static final Pattern RGB_PATTERN = Pattern.compile("(?i)(&#|#|<#|\\{#)([0-9a-f]{6})(>|})?");

    // Pattern for gradient formats (e.g., {#RRGGBB>}text{#RRGGBB<})
    private static final Pattern GRADIENT_PATTERN = Pattern.compile("(?i)\\{#([0-9a-f]{6})>(.*?)\\{#([0-9a-f]{6})<}");

    // MiniMessage instance for modern formatting
    private static final MiniMessage MINI_MESSAGE = MiniMessage.miniMessage();

    /**
     * Translates a string with various color and formatting codes into a Component.
     * Supports legacy codes, RGB, gradients, and MiniMessage formats.
     *
     * @param input The raw input string containing color codes.
     * @return A formatted Component compatible with Paper API.
     */
    public static Component translate(String input) {
        if (input == null) return Component.empty();

        // Step 1: Handle legacy color codes first
        String legacyProcessed = translateLegacy(input);

        // Step 2: Handle RGB codes
        String rgbProcessed = translateRGB(legacyProcessed);

        // Step 3: Handle gradients
        String gradientProcessed = translateGradients(rgbProcessed);

        // Step 4: Parse the final string with MiniMessage for modern formatting
        return MINI_MESSAGE.deserialize(gradientProcessed);
    }

    /**
     * Translates legacy Minecraft color codes (e.g., &a, &l) into Bukkit ChatColor format.
     *
     * @param input The input string with legacy codes.
     * @return A string with translated legacy codes.
     */
    private static String translateLegacy(String input) {
        return ChatColor.translateAlternateColorCodes('&', input);
    }

    /**
     * Translates RGB hex codes into MiniMessage-compatible format.
     * Supports &#RRGGBB, #RRGGBB, <#RRGGBB>, {#RRGGBB}.
     *
     * @param input The input string with RGB codes.
     * @return A string with RGB codes converted to MiniMessage format.
     */
    private static String translateRGB(String input) {
        Matcher matcher = RGB_PATTERN.matcher(input);
        StringBuffer result = new StringBuffer();

        while (matcher.find()) {
            String hex = matcher.group(2); // Extract the RRGGBB part
            String replacement = "<#" + hex + ">"; // Convert to MiniMessage RGB format
            matcher.appendReplacement(result, replacement);
        }
        matcher.appendTail(result);
        return result.toString();
    }

    /**
     * Translates gradient formats into MiniMessage gradient syntax.
     * Supports {#RRGGBB>}text{#RRGGBB<} and similar patterns.
     *
     * @param input The input string with gradient codes.
     * @return A string with gradients converted to MiniMessage format.
     */
    private static String translateGradients(String input) {
        Matcher matcher = GRADIENT_PATTERN.matcher(input);
        StringBuffer result = new StringBuffer();

        while (matcher.find()) {
            String startColor = matcher.group(1); // Starting color
            String text = matcher.group(2);       // Text to apply gradient to
            String endColor = matcher.group(3);   // Ending color

            // Convert to MiniMessage gradient format
            String replacement = "<gradient:#" + startColor + ":#" + endColor + ">" + text + "</gradient>";
            matcher.appendReplacement(result, replacement);
        }
        matcher.appendTail(result);
        return result.toString();
    }

    /**
     * Converts a Component back to a legacy string with & color codes.
     * Useful for compatibility with older systems.
     *
     * @param component The Component to serialize.
     * @return A legacy-formatted string.
     */
    public static String toLegacy(Component component) {
        return LegacyComponentSerializer.legacyAmpersand().serialize(component);
    }
}