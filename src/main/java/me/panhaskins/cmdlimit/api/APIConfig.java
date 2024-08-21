package me.panhaskins.cmdlimit.api;

import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

public class APIConfig {

    private final JavaPlugin plugin;

    private final String configPath;

    private File file;
    private YamlConfiguration cfg;

    public APIConfig(JavaPlugin plugin, String configPath) {
        this.plugin = plugin;
        this.configPath = configPath;
        create();
    }

    private void create() {
        if (!plugin.getDataFolder().exists()) plugin.getDataFolder().mkdir();

        file = new File(plugin.getDataFolder() + File.separator + configPath);
        plugin.getResource(configPath);
        if (!file.exists()) {
            try {
                Files.copy(plugin.getResource(configPath), file.toPath());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        cfg = YamlConfiguration.loadConfiguration(file);
    }

    public YamlConfiguration get() {
        return cfg;
    }

    public void save() {
        try {
            cfg.save(file);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void reload() {
        this.cfg = YamlConfiguration.loadConfiguration(file);
    }
}
