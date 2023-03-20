package me.panhaskins.cmdlimit;

import me.panhaskins.cmdlimit.api.APIColor;
import me.panhaskins.cmdlimit.api.APIConfig;
import me.panhaskins.cmdlimit.api.UpdateChecker;
import org.bukkit.Bukkit;
import org.bukkit.command.*;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.SimplePluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Set;


public final class CMDLimiter extends JavaPlugin implements Listener {

    public static APIConfig config, data;

    @Override
    public void onEnable() {

        // Plugin startup logic
        config = new APIConfig(this, getDataFolder() + File.separator + "config.yml" , "config.yml");
        data = new APIConfig(this, getDataFolder() + File.separator + "data.yml" , "data.yml");

        if(config.get().getBoolean("updateChecker")){
            new UpdateChecker(this, 100289).getLatestVersion(version -> {
                if (!this.getDescription().getVersion().equalsIgnoreCase(version)) {
                   Bukkit.getConsoleSender().sendMessage("");
                    Bukkit.getConsoleSender().sendMessage(APIColor.process("&8[&6WARNING&r&8] &f&eCMDLimiter &fPlugin"));
                    Bukkit.getConsoleSender().sendMessage(APIColor.process("&8[&6WARNING&r&8] &f&fYour plugin version is out of date."));
                    Bukkit.getConsoleSender().sendMessage(APIColor.process("&8[&6WARNING&r&8] &f&fI recommend updating it."));
                    Bukkit.getConsoleSender().sendMessage(APIColor.process("&8[&6WARNING&r&8] &fhttps://www.spigotmc.org/resources/%E2%8F%B2-cmd-limiter-%E2%8F%B2.100289/"));
                    Bukkit.getConsoleSender().sendMessage("");
                }
            });
        }
        registerCommands();

        this.getServer().getPluginManager().registerEvents(this, this);

        }

    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {

        if (cmd.getName().equalsIgnoreCase("clreload")) {
            if (sender.hasPermission("cl.reload")) {
                sender.sendMessage(APIColor.process(config.get().getString("reloading")));
                config.reload();
                data.reload();
                sender.sendMessage(APIColor.process(config.get().getString("reloadComplete")));
            }
            else
            {
                sender.sendMessage(APIColor.process(config.get().getString("noPermission").replaceAll("%command%", "clreload")));
            }
        }
        return true;
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent e) {
        Set<String> commandsList = config.get().getConfigurationSection("commands").getKeys(false);
        Player player = e.getPlayer();
        for (String cmdName : commandsList) {

            if (config.get().contains("commands." + cmdName + ".join.enabled")){
                ConfigurationSection dataSection = data.get().getConfigurationSection("Data." + cmdName);
                if (dataSection == null) {
                    dataSection = data.get().createSection("Data." + cmdName);
                    data.save();
                }

                if (!dataSection.getKeys(false).contains(player.getName())) {
                    for (String joinMessage : APIColor.process(config.get().getStringList("commands." + cmdName + ".join.message"))) {

                        player.sendMessage(joinMessage);

                    }
                }

            }

        }
    }

    private void registerCommands() {
        Set<String> commandsList = config.get().getConfigurationSection("commands").getKeys(false);
        for (String cmdName : commandsList) {

            registerCommand(new Commands(), config.get().getString(cmdName + ".description"), cmdName);
        }

    }

    private void registerCommand(CommandExecutor commandExecutor, String description, String name, String... aliases) { //to co znamenaj tie tri bodky. To som este nevidel uvidis
        try {
            Constructor<PluginCommand> constructor = PluginCommand.class.getDeclaredConstructor(String.class, Plugin.class);
            constructor.setAccessible(true);
            PluginCommand pluginCommand = constructor.newInstance(name, this);
            pluginCommand.setExecutor(commandExecutor);

            if (description != null) pluginCommand.setDescription(description);
            pluginCommand.setAliases(Arrays.asList(aliases));
            Field field = SimplePluginManager.class.getDeclaredField("commandMap");
            field.setAccessible(true);
            CommandMap commandMap = (CommandMap) field.get(this.getServer().getPluginManager());
            commandMap.register(this.getDescription().getName(), pluginCommand);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }

}