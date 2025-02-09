package me.panhaskins.cmdlimit;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class CustomPlaceholders extends PlaceholderExpansion {

    @Override
    public @NotNull String getIdentifier() {
        return "cmdlimiter";
    }

    @Override
    public @NotNull String getAuthor() {
        return "panhaskins";
    }

    @Override
    public @NotNull String getVersion() {
        return "3.0";
    }

    @Override
    public String onPlaceholderRequest(Player player, String placeholder) {
        for (String command : CMDLimiter.commandList) {
            if (placeholder.equalsIgnoreCase("use_" + command)) {
                return String.valueOf(CMDLimiter.dataManager.getPlayer(player.getName(), command));
            }
        }
        return null;
    }
}
