package dev.redgamer6427a.core.minecraft.velocity.configuration;

import dev.dejvokep.boostedyaml.YamlDocument;
import dev.dejvokep.boostedyaml.dvs.versioning.BasicVersioning;
import dev.dejvokep.boostedyaml.settings.dumper.DumperSettings;
import dev.dejvokep.boostedyaml.settings.general.GeneralSettings;
import dev.dejvokep.boostedyaml.settings.loader.LoaderSettings;
import dev.dejvokep.boostedyaml.settings.updater.UpdaterSettings;

import lombok.Getter;


import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
@Getter
public abstract class Configuration extends AbstractConfigurationSection {

    private File file;
    private YamlDocument config;
    private String id;

    public static List<Configuration> configurations = new ArrayList<>();

    public Configuration(String fileName, File dataDirectory) {
        super("", null);
        id = fileName;
        try {

            // Ensure the "config" directory exists
            File configDir = new File(dataDirectory, "config");
            if (!configDir.exists()) {
                configDir.mkdirs();
            }

            config = YamlDocument.create(new File(configDir, fileName+".yml"),
                    getClass().getResourceAsStream("/config.yml"),
                    GeneralSettings.DEFAULT,
                    LoaderSettings.builder().setAutoUpdate(true).build(),
                    DumperSettings.DEFAULT,
                    UpdaterSettings.builder().setVersioning(new BasicVersioning("file-version")).setOptionSorting(UpdaterSettings.DEFAULT_OPTION_SORTING).build());
            file = config.getFile();
        } catch (Exception e){
            e.printStackTrace();
        }

        if (!file.exists()) {
            try {
                file.getParentFile().mkdirs();
                file.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        try {
            config.update();
            config.save();
        } catch (IOException ignored) {}
        configurations.add(this);
    }

    @Override
    public String toString() {
        return "crossbowConfiguration["+file.getName()+", "+ children.toString()+"]";
    }

    public void load(){

        try {
            config.reload();


        } catch (IOException e) {
            throw new RuntimeException(e);
        }


        preLoadHook();
        for(ConfigurationPart part : children){
            part.load();
        }
        save();
        postLoadHook();
    }

    public void save(){
        preSaveHook();
        for(ConfigurationPart part : children){
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
