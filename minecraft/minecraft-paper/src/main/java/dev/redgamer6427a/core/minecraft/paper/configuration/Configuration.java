package dev.redgamer6427a.admiral.paper.configuration;

import dev.redgamer6427a.admiral.paper.AdmiralPlugin;
import lombok.Getter;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

public abstract class Configuration extends AbstractConfigurationSection {

    public File file;
    public YamlConfiguration config;
    public final String id;

    @Getter
    private final static ArrayList<Configuration> configurations = new ArrayList<>();

    public Configuration(String fileName) {
        super("", null);
        id = fileName;
        // Ensure a "config" subdirectory exists
        File configDir = new File(AdmiralPlugin.getInstance().getDataFolder(), "config");
        if (!configDir.exists()) {
            configDir.mkdirs();
        }

        // Create the YAML file inside the "config" directory
        file = new File(configDir, fileName + ".yml");
        if (!file.exists()) {
            try {
                file.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        // Optionally save the default config
        try {
            AdmiralPlugin.getInstance().saveDefaultConfig();
        } catch (IllegalArgumentException e) {
            // Ignore if it fails
        }
        configurations.add(this);
    }


    @Override
    public String toString() {
        return "CrossbowConfiguration[" + file.getName() + ", " + children.toString() + "]";
    }

    public void load() {
        preLoadHook();
        config = YamlConfiguration.loadConfiguration(file);
        for (ConfigurationPart part : children) {
            part.load();
        }
        save();
        postLoadHook();
    }

    public void save() {
        preSaveHook();
        for (ConfigurationPart part : children) {
            part.save();
        }
        try {
            config.save(file);
        } catch (IOException e) {
            e.printStackTrace();
        }
        postSaveHook();
    }


}
