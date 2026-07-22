package com.example.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.example.ExampleMod;
import net.fabricmc.loader.api.FabricLoader;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

public class ModConfig {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Path CONFIG_PATH = FabricLoader.getInstance().getConfigDir().resolve("custombeacon.json");

    public int maxTier = 64;
    public int baseScanIntervalTicks = 40;
    public boolean enableActiveDrain = false;
    public int activeDrainIntervalMinutes = 5;

    public Map<String, Double> mineralPowerWeights = new HashMap<>();
    public Map<String, Integer> effectHardCaps = new HashMap<>();
    public Map<String, Double> effectCostMultipliers = new HashMap<>();

    public ModConfig() {
        // Default mineral power weights
        mineralPowerWeights.put("minecraft:iron_block", 1.0);
        mineralPowerWeights.put("minecraft:gold_block", 1.2);
        mineralPowerWeights.put("minecraft:emerald_block", 1.5);
        mineralPowerWeights.put("minecraft:diamond_block", 2.0);
        mineralPowerWeights.put("minecraft:netherite_block", 3.0);

        // Default anti-OP hard caps
        effectHardCaps.put("minecraft:strength", 2);
        effectHardCaps.put("minecraft:resistance", 2);
        effectHardCaps.put("minecraft:speed", 5);
        effectHardCaps.put("minecraft:haste", 5);

        // Default cost multipliers
        effectCostMultipliers.put("minecraft:strength", 2.5);
        effectCostMultipliers.put("minecraft:resistance", 2.5);
        effectCostMultipliers.put("minecraft:speed", 1.0);
        effectCostMultipliers.put("minecraft:haste", 1.0);
    }

    private static ModConfig INSTANCE = new ModConfig();

    public static ModConfig get() {
        return INSTANCE;
    }

    public static void load() {
        File file = CONFIG_PATH.toFile();
        if (file.exists()) {
            try (FileReader reader = new FileReader(file)) {
                ModConfig loaded = GSON.fromJson(reader, ModConfig.class);
                if (loaded != null) {
                    INSTANCE = loaded;
                    ExampleMod.LOGGER.info("Successfully loaded custombeacon.json config.");
                    return;
                }
            } catch (IOException e) {
                ExampleMod.LOGGER.error("Failed to read custombeacon.json config file", e);
            }
        }
        // Save default config if not found or on error
        save();
    }

    public static void save() {
        try {
            File file = CONFIG_PATH.toFile();
            file.getParentFile().mkdirs();
            try (FileWriter writer = new FileWriter(file)) {
                GSON.toJson(INSTANCE, writer);
                ExampleMod.LOGGER.info("Saved custombeacon.json config file.");
            }
        } catch (IOException e) {
            ExampleMod.LOGGER.error("Failed to save custombeacon.json config file", e);
        }
    }

    public double getMineralWeight(String blockId) {
        return mineralPowerWeights.getOrDefault(blockId, 1.0);
    }

    public int getEffectHardCap(String effectId) {
        return effectHardCaps.getOrDefault(effectId, 5);
    }

    public double getEffectCostMultiplier(String effectId) {
        return effectCostMultipliers.getOrDefault(effectId, 1.0);
    }
}
