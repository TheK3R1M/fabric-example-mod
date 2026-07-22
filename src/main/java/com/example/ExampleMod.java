package com.example;

import com.example.command.CustomBeaconCommand;
import com.example.config.ModConfig;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.resources.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ExampleMod implements ModInitializer {
	public static final String MOD_ID = "modid";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	@Override
	public void onInitialize() {
		LOGGER.info("Initializing Custom Beacon Overhaul Mod...");

		// Load configuration
		ModConfig.load();

		// Register /custombeacon command tree
		CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
			CustomBeaconCommand.register(dispatcher);
		});

		LOGGER.info("Custom Beacon Overhaul Mod Phase 1 initialized successfully.");
	}

	public static Identifier id(String path) {
		return Identifier.fromNamespaceAndPath(MOD_ID, path);
	}
}
