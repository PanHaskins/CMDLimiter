package me.panhaskins.cmdlimit.utils;

import me.clip.placeholderapi.PlaceholderAPI;
import me.panhaskins.cmdlimit.api.ColorMessage;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import java.util.List;

public class Messager {
    public void sendMessage(Player player, String message, OfflinePlayer target) {
        if (message == null || message.isEmpty()) return;
        player.sendMessage(ColorMessage.toLegacy(ColorMessage.translate(PlaceholderAPI.setPlaceholders(target, message))));
    }

    public void sendMessage(Player player, String message){
        if (message == null || message.isEmpty()) return;
        player.sendMessage(ColorMessage.toLegacy(ColorMessage.translate(message)));
    }

    public void sendMessage(Player player, List<String> messages, OfflinePlayer target) {
        if (messages == null || messages.isEmpty()) return;
        PlaceholderAPI.setPlaceholders(target, messages)
                .stream()
                .map(ColorMessage::translate)
                .map(ColorMessage::toLegacy)
                .forEach(player::sendMessage);
    }

    public void sendMessage(Player player, List<String> messages) {
        if (messages == null || messages.isEmpty()) return;
        messages.stream()
                .map(ColorMessage::translate)
                .map(ColorMessage::toLegacy)
                .forEach(player::sendMessage);
    }
}
