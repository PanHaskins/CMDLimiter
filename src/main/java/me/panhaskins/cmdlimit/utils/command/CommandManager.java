package me.panhaskins.cmdlimit.utils.command;

import org.bukkit.Bukkit;
import org.bukkit.command.*;
import org.bukkit.plugin.SimplePluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

public class CommandManager {
    private static CommandMap commandMap;
    private static Map<String, Command> registeredCommands = new HashMap<>();

    static {
        try {
            Field f = SimplePluginManager.class.getDeclaredField("commandMap");
            f.setAccessible(true);
            commandMap = (CommandMap) f.get(Bukkit.getPluginManager());
        } catch (NoSuchFieldException | IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    public static void registerCommand(JavaPlugin plugin, Commander command) {
        PluginCommand pluginCommand = getCommand(command.getName(), plugin);

        pluginCommand.setExecutor(command);
        pluginCommand.setTabCompleter(command);

        commandMap.register(plugin.getName(), pluginCommand);
        registeredCommands.put(command.getName().toLowerCase(), pluginCommand);
    }

    public static void unregisterCommand(String command) {
        Command cmd = registeredCommands.remove(command.toLowerCase());
        if (cmd != null) {
            try {
                Field knownCommandsField = SimpleCommandMap.class.getDeclaredField("knownCommands");
                knownCommandsField.setAccessible(true);
                @SuppressWarnings("unchecked")
                Map<String, Command> knownCommands = (Map<String, Command>) knownCommandsField.get(commandMap);

                knownCommands.remove(command.toLowerCase());
                for (String alias : cmd.getAliases()) {
                    if (knownCommands.get(alias.toLowerCase()) == cmd) {
                        knownCommands.remove(alias.toLowerCase());
                    }
                }
            } catch (NoSuchFieldException | IllegalAccessException e) {
                e.printStackTrace();
            }
        }
    }

    private static PluginCommand getCommand(String name, JavaPlugin plugin) {
        PluginCommand command = null;

        try {
            Constructor<PluginCommand> c = PluginCommand.class.getDeclaredConstructor(String.class, org.bukkit.plugin.Plugin.class);
            c.setAccessible(true);

            command = c.newInstance(name, plugin);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return command;
    }
}

