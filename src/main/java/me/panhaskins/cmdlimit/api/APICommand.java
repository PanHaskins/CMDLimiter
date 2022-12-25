package me.panhaskins.cmdlimit.api;

import me.panhaskins.cmdlimit.CMDLimiter;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.PluginIdentifiableCommand;
import org.bukkit.plugin.Plugin;

public abstract class APICommand extends Command implements PluginIdentifiableCommand {
    CommandSender sender;
    CMDLimiter plugin = CMDLimiter.getInstance();

    protected APICommand(String cmdName) {
        super(cmdName);
    }

    @Override
    public Plugin getPlugin() {
        return plugin;
    }

    public abstract void run(CommandSender sender, String label, String[] args);

    @Override
    public boolean execute(CommandSender sender, String label, String[] args) {
        this.sender = sender;
        run(sender, label, args);
        return true;
    }
}