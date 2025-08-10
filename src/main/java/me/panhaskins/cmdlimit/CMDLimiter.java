package me.panhaskins.cmdlimit;

import me.panhaskins.cmdlimit.utils.ConfigManager;
import me.panhaskins.cmdlimit.utils.UpdateChecker;
import me.panhaskins.cmdlimit.commands.AdminCommand;
import me.panhaskins.cmdlimit.commands.FreeCommands;
import me.panhaskins.cmdlimit.utils.DataManager;
import me.panhaskins.cmdlimit.utils.Messager;
import me.panhaskins.cmdlimit.utils.command.CommandManager;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashSet;
import java.util.Set;


public final class CMDLimiter extends JavaPlugin implements Listener {

    public static ConfigManager config;
    public static DataManager dataManager;
    private CustomPlaceholders customPlaceholders;
    public static Set<String> commandList = new HashSet<>();


    @Override
    public void onEnable() {

        // Plugin startup logic
        config = new ConfigManager(this, "config.yml");

        if(config.getConfig("config.yml").getBoolean("updateChecker")){
            new UpdateChecker(this, 100289).getLatestVersion(version -> {
                if (!this.getDescription().getVersion().equalsIgnoreCase(version)) {
                    Bukkit.getConsoleSender().sendMessage("");
                    Bukkit.getConsoleSender().spigot().sendMessage(Messager.translateToBaseComponents("&8[&6WARNING&r&8] &f&eCMDLimiter &fPlugin"));
                    Bukkit.getConsoleSender().spigot().sendMessage(Messager.translateToBaseComponents("&8[&6WARNING&r&8] &f&fYour plugin version is out of date."));
                    Bukkit.getConsoleSender().spigot().sendMessage(Messager.translateToBaseComponents("&8[&6WARNING&r&8] &f&fI recommend updating it."));
                    Bukkit.getConsoleSender().spigot().sendMessage(Messager.translateToBaseComponents("&8[&6WARNING&r&8] &fhttps://www.spigotmc.org/resources/%E2%8F%B2-cmd-limiter-%E2%8F%B2.100289/"));
                    Bukkit.getConsoleSender().sendMessage("");
                }
            });
        }

        commandList.addAll(config.getConfig("config.yml").getConfigurationSection("commands").getKeys(false));
        CommandManager.registerCommand(this, new AdminCommand(this));

        if (!commandList.isEmpty()) {
            for (String commandName : commandList) {
                if (config.getConfig("config.yml").getBoolean("commands." + commandName + ".isCustomCommand", true))
                    CommandManager.registerCommand(this, new FreeCommands(commandName, config.getConfig("config.yml").getConfigurationSection("commands." + commandName)));
            }
        }


        dataManager = new DataManager();
        dataManager.load();

        if (Bukkit.getServer().getPluginManager().isPluginEnabled("PlaceholderAPI")) {
            customPlaceholders = new CustomPlaceholders();
            customPlaceholders.register();
        }


        getServer().getPluginManager().registerEvents(new Listening(), this);

        dataManager.startDataSaveTask();
        dataManager.startCooldownCleanTask();
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent e) {
        Player player = e.getPlayer();

        for (String cmdName : commandList) {

            if (config.getConfig("config.yml").getBoolean("commands." + cmdName + ".join.enabled", false)) {
                if (dataManager.isOnCooldown(player, cmdName)
                        && dataManager.getPlayer(player.getName(), cmdName) > config.getConfig("config.yml").getInt("commands." + cmdName + ".maxUse")
                        && dataManager.getGlobal(cmdName) > config.getConfig("config.yml").getInt("commands." + cmdName + ".globalMaxUse")) {
                    player.spigot().sendMessage(Messager.translateToBaseComponents(config.getConfig("config.yml").getString("commands." + cmdName + ".join.message"), player));
                }


            }

        }

    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
        if(customPlaceholders != null || Bukkit.getServer().getPluginManager().isPluginEnabled("PlaceholderAPI"))
            customPlaceholders.unregister();
        if(dataManager != null) dataManager.save();
    }

}