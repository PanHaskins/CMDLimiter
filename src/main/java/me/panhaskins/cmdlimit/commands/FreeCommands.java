package me.panhaskins.cmdlimit.commands;

import me.panhaskins.cmdlimit.CMDLimiter;
import me.panhaskins.cmdlimit.utils.ConditionUtils;
import me.panhaskins.cmdlimit.utils.command.Commander;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;


public class FreeCommands extends Commander {


    private final ConfigurationSection commandSection;

    public FreeCommands(String name, ConfigurationSection commandSection) {
        super(name);
        this.commandSection = commandSection;
    }


    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        if (!(sender instanceof Player)) {
            CMDLimiter.messager.sendMessage((Player) sender, CMDLimiter.config.get().getString("playerOnly"));
            return true;
        }

        Player player = (Player) sender;
        String commandName = command.getName();
        if (CMDLimiter.dataManager.isOnCooldown(player, commandName)) {
            CMDLimiter.messager.sendMessage(player, CMDLimiter.config.get().getString("cooldown")
                    .replaceAll("%time%", String.valueOf(CMDLimiter.dataManager.getRemainingCooldown(player, commandName)))
                    .replaceAll("%command%", commandName), player);
            return false;
        }

        if (ConditionUtils.checkRequirements((Player) sender, commandSection)){
            executeCommandIfAllowed((Player) sender, getName(), commandSection);
                return true;
            }
        return false;
    }



    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> commandList = new ArrayList<>();
        for (String cmd : CMDLimiter.commandList) {
            if (CMDLimiter.config.get().getBoolean("commands." + cmd + ".isCustomCommand", true)) {
                commandList.add(cmd);
            }
        }

        return commandList;
    }

    private void executeCommandIfAllowed(Player player, String command, ConfigurationSection commandSection) {
        int maxUse = commandSection.getInt("maxUse");
        int playerUse = CMDLimiter.dataManager.getPlayer(player.getName(), command);

        if (playerUse < maxUse || maxUse <= 0) {
            CMDLimiter.dataManager.setPlayer(player.getName(), command, playerUse + 1);
            commandSection.getStringList("console").forEach(consoleCommand ->
                    Bukkit.getServer().dispatchCommand(Bukkit.getConsoleSender(), consoleCommand.replace("%player%", player.getName())));
            CMDLimiter.messager.sendMessage(player, commandSection.getString("use"), player);

            int cooldown = commandSection.getInt("cooldown", 0);
            if (cooldown > 0) CMDLimiter.dataManager.setCooldown(player, command, cooldown);
        } else{
            CMDLimiter.messager.sendMessage(player, commandSection.getString("used"), player);
        }

    }
}
