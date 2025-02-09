package me.panhaskins.cmdlimit.utils.command;

import me.clip.placeholderapi.PlaceholderAPI;
import me.panhaskins.cmdlimit.api.APIColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;

import java.util.List;

public abstract class Commander implements TabExecutor {
    private final String name;

    public Commander(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    @Override
    public abstract boolean onCommand(CommandSender sender, Command command, String label, String[] args);

    @Override
    public abstract List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args);
}