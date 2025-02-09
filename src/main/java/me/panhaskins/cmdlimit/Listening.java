package me.panhaskins.cmdlimit;

import me.panhaskins.cmdlimit.utils.ConditionUtils;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;

public class Listening implements Listener {
    @EventHandler
    public void onCommand(PlayerCommandPreprocessEvent event) {
        Player player = event.getPlayer();

        for (String command : CMDLimiter.commandList) {
            ConfigurationSection commandSection = CMDLimiter.config.get().getConfigurationSection("commands." + command);
            if (commandSection == null || commandSection.getBoolean("isCustomCommand", true)) {
                continue;
            }

            if (event.getMessage().startsWith("/" + command.toLowerCase())) {

                if (CMDLimiter.dataManager.isOnCooldown(player, command)) {
                    CMDLimiter.messager.sendMessage(player, CMDLimiter.config.get().getString("cooldown")
                                    .replaceAll("%time%", String.valueOf(CMDLimiter.dataManager.getRemainingCooldown(player, command)))
                                    .replaceAll("%command%", command), player);
                    event.setCancelled(true);
                    return;
                }

                if (!commandSection.getBoolean("onlyWhenUsedAdminCommand", false)) {
                    if (ConditionUtils.checkRequirements(player, commandSection)) {
                        int globalMaxUse = commandSection.getInt("globalMaxUse");
                        int maxUse = commandSection.getInt("maxUse");
                        int globalUse = CMDLimiter.dataManager.getGlobal(command);
                        int playerUse = CMDLimiter.dataManager.getPlayer(player.getName(), command);

                        if (globalUse > globalMaxUse && globalMaxUse > 0) {
                            CMDLimiter.messager.sendMessage(player, commandSection.getString("globalUsed"), player);
                            event.setCancelled(true);
                            return;
                        }

                        if (playerUse > maxUse && maxUse > 0) {
                            CMDLimiter.messager.sendMessage(player, commandSection.getString("used"), player);
                            event.setCancelled(true);
                            return;
                        }

                        CMDLimiter.dataManager.setPlayer(player.getName(), command, playerUse + 1);
                        if (!commandSection.getStringList("console").isEmpty())
                            commandSection.getStringList("console").forEach(consoleCommand ->
                                    Bukkit.getServer().dispatchCommand(Bukkit.getConsoleSender(), consoleCommand.replace("%player%", player.getName())));
                        CMDLimiter.messager.sendMessage(player, commandSection.getString("use"), player);
                        int cooldown = commandSection.getInt("cooldown", 0);
                        if (cooldown > 0) CMDLimiter.dataManager.setCooldown(player, command, cooldown);
                    }
                }
            }
        }
    }
}