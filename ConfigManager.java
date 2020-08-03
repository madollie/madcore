package me.invakid.azran.discord.config;

import org.apache.commons.io.FileUtils;
import org.yaml.snakeyaml.Yaml;

import java.io.File;

public enum ConfigManager {

    INSTANCE;

    private final String configName = "bot-settings.yml";

    ConfigManager() {
    }

    public File createConfig() {
        try {
            File configFile = new File(configName);
            if (!configFile.exists()) {
                configFile.createNewFile();
                FileUtils.copyInputStreamToFile(this.getClass().getResourceAsStream("/" + configName), configFile);
            }

            return configFile;
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        return null;
    }

    public BotConfig loadConfig() {
        File f = createConfig();
        if (f == null) return null;

        try {
            return new Yaml().loadAs(FileUtils.openInputStream(f), BotConfig.class);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

}
