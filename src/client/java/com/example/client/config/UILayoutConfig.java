package com.example.client.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.nio.file.Path;

public class UILayoutConfig {

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Path CONFIG_PATH = FabricLoader.getInstance().getConfigDir().resolve("ui_layout.json");

    public static class ElementBounds {
        public int x;
        public int y;
        public int width;
        public int height;

        public ElementBounds() {}

        public ElementBounds(int x, int y, int width, int height) {
            this.x = x;
            this.y = y;
            this.width = width;
            this.height = height;
        }
    }

    public static class LayoutData {
        public ElementBounds blueprintPanel = new ElementBounds(-160, 0, 125, 168);
        public ElementBounds scrollableGrid = new ElementBounds(200, 0, 135, 168);
        public ElementBounds confirmButton = new ElementBounds(50, 138, 72, 20);
    }

    public static LayoutData loadOrGenerate() {
        File configFile = CONFIG_PATH.toFile();
        if (configFile.exists()) {
            try (FileReader reader = new FileReader(configFile)) {
                LayoutData loaded = GSON.fromJson(reader, LayoutData.class);
                if (loaded != null) {
                    if (loaded.blueprintPanel == null) loaded.blueprintPanel = new ElementBounds(-160, 0, 125, 168);
                    if (loaded.scrollableGrid == null) loaded.scrollableGrid = new ElementBounds(200, 0, 135, 168);
                    if (loaded.confirmButton == null) loaded.confirmButton = new ElementBounds(50, 138, 72, 20);
                    return loaded;
                }
            } catch (Exception e) {
                System.err.println("[Fabric Mod] Failed to read ui_layout.json, regenerating defaults: " + e.getMessage());
            }
        }

        LayoutData defaultData = new LayoutData();
        saveConfig(defaultData);
        return defaultData;
    }

    public static void saveConfig(LayoutData data) {
        try {
            File configFile = CONFIG_PATH.toFile();
            if (configFile.getParentFile() != null && !configFile.getParentFile().exists()) {
                configFile.getParentFile().mkdirs();
            }
            try (FileWriter writer = new FileWriter(configFile)) {
                GSON.toJson(data, writer);
            }
        } catch (Exception e) {
            System.err.println("[Fabric Mod] Failed to save ui_layout.json: " + e.getMessage());
        }
    }
}
