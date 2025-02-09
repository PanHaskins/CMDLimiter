package me.panhaskins.cmdlimit.utils;

import me.clip.placeholderapi.PlaceholderAPI;
import me.panhaskins.cmdlimit.api.APIColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import java.util.List;

public class Messager {
    public void sendMessage(Player player, String message, OfflinePlayer target) {
        if (message == null || message.isEmpty()) return;
        player.sendMessage(PlaceholderAPI.setPlaceholders(target, APIColor.process(message)));
    }

    public void sendMessage(Player player, String message){
        if (message == null || message.isEmpty()) return;
        player.sendMessage(APIColor.process(message));
    }

    public void sendMessage(Player player, List<String> messages, OfflinePlayer target) {
        if (messages == null || messages.isEmpty()) return;
        PlaceholderAPI.setPlaceholders(target, APIColor.process(messages)).forEach(player::sendMessage);
    }

    public void sendMessage(Player player, List<String> messages) {
        if (messages == null || messages.isEmpty()) return;
        APIColor.process(messages).forEach(player::sendMessage);
    }
}
