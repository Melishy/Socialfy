package uk.melishy.socialfy.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.gui.screens.Screen;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class Config {
    private static final Logger LOGGER = LoggerFactory.getLogger("Socialfy-Config");
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Path CONFIG_PATH = FabricLoader.getInstance().getConfigDir().resolve("socialfy.json");

    public boolean enabled = true;
    public boolean showState = true;
    public boolean showPlayerCount = true;
    public boolean showServerIp = false;
    public boolean debugMode = false;
    public int updateInterval = 5;

    public static Config initialize() {
        Config config = load();
        config.save();
        return config;
    }

    private static Config load() {
        try {
            if (Files.exists(CONFIG_PATH)) {
                String json = Files.readString(CONFIG_PATH);
                Config loadedConfig = GSON.fromJson(json, Config.class);
                if (loadedConfig != null) return loadedConfig;
            }
        } catch (IOException e) {
            LOGGER.error("[Socialfy] Failed to load config", e);
        } catch (Exception e) {
            LOGGER.error("[Socialfy] Invalid config file, using defaults", e);
        }
        return new Config();
    }

    public void save() {
        try {
            Files.createDirectories(CONFIG_PATH.getParent());
            Files.writeString(CONFIG_PATH, GSON.toJson(this));
        } catch (IOException e) {
            LOGGER.error("[Socialfy] Failed to save config", e);
        }
    }

    public Screen getConfigScreen(Screen parent) {
        boolean hasYACL = FabricLoader.getInstance().isModLoaded("yet_another_config_lib_v3")
                || FabricLoader.getInstance().isModLoaded("yacl");

        if (hasYACL) {
            return new YaclConfig().getConfigScreen(parent);
        } else {
            return null;
        }
    }
}
