package me.panhaskins.cmdlimit.utils;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;

import java.io.File;
import java.security.CodeSource;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.regex.Pattern;

public class ConfigManager {
    private final Plugin plugin;
    private final Map<String, FileConfiguration> configs = new HashMap<>();
    private final Map<String, File> files = new HashMap<>();

    /**
     * @param plugin  Instance of your plugin
     * @param patterns List of resource patterns, e.g. "functions/*.yml"
     */
    public ConfigManager(Plugin plugin, String... patterns) {
        this.plugin = plugin;
        for (String pattern : patterns) {
            registerPattern(pattern);
        }
    }

    // Register all configs matching given pattern (e.g. functions/*.yml)
    private void registerPattern(String pattern) {
        String folder = "";
        String filePattern = pattern;
        int slash = pattern.lastIndexOf('/');
        if (slash >= 0) {
            folder = pattern.substring(0, slash);
            filePattern = pattern.substring(slash + 1);
        }

        Pattern regex = Pattern.compile(filePattern.replace("*", ".*"));
        File dir = new File(plugin.getDataFolder(), folder);
        if (!dir.exists()) dir.mkdirs();

        // Load files bundled inside the plugin jar that match the pattern
        try {
            CodeSource codeSource = plugin.getClass().getProtectionDomain().getCodeSource();
            if (codeSource != null) {
                try (JarFile jar = new JarFile(new File(codeSource.getLocation().toURI()))) {
                    Enumeration<JarEntry> entries = jar.entries();
                    while (entries.hasMoreElements()) {
                        JarEntry entry = entries.nextElement();
                        if (entry.isDirectory()) continue;
                        String name = entry.getName();
                        if (!folder.isEmpty()) {
                            if (!name.startsWith(folder + "/")) continue;
                            name = name.substring(folder.length() + 1);
                        }
                        if (regex.matcher(name).matches()) {
                            String resourcePath = folder.isEmpty() ? name : folder + "/" + name;
                            File file = new File(dir, name);
                            if (!file.exists()) {
                                plugin.saveResource(resourcePath, false);
                            }
                            register(file, resourcePath);
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Load any existing files in the data folder that match the pattern
        File[] filesArr = dir.listFiles((d, name) -> regex.matcher(name).matches());
        if (filesArr != null) {
            for (File file : filesArr) {
                String key = folder.isEmpty() ? file.getName() : folder + "/" + file.getName();
                if (!configs.containsKey(key)) {
                    register(file, key);
                }
            }
        }
    }

    // Register a config file and load it
    private void register(File file, String key) {
        if (!file.exists()) {
            plugin.saveResource(key, false);
        }
        files.put(key, file);
        configs.put(key, YamlConfiguration.loadConfiguration(file));
    }

    /**
     * Get config by its relative path (e.g. "functions/something.yml")
     */
    public FileConfiguration getConfig(String key) {
        return configs.get(key);
    }

    /**
     * Reload single config file
     */
    public void reload(String key) {
        File file = files.get(key);
        if (file != null) {
            configs.put(key, YamlConfiguration.loadConfiguration(file));
        }
    }

    /**
     * Reload all registered configs
     */
    public void reloadAll() {
        for (String key : configs.keySet()) {
            reload(key);
        }
    }

    public void save(String key) {}

    public void saveAll() {}

    /**
     * Get all config keys (relative paths)
     */
    public Set<String> getConfigKeys() {
        return configs.keySet();
    }
}
