package com.example;

import com.example.command.CustomBeaconCommand;
import com.example.config.ModConfig;
import com.example.network.BeaconSyncPayload;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.resources.Identifier;
import net.minecraft.world.inventory.BeaconMenu;
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

		// Register Networking Payloads using modern Fabric API Methods
		PayloadTypeRegistry.serverboundPlay().register(BeaconSyncPayload.BeaconSelectionPayload.TYPE, BeaconSyncPayload.BeaconSelectionPayload.CODEC);
		PayloadTypeRegistry.clientboundPlay().register(BeaconSyncPayload.BeaconStateSyncPayload.TYPE, BeaconSyncPayload.BeaconStateSyncPayload.CODEC);

		// Handle C2S Selection Packets
		ServerPlayNetworking.registerGlobalReceiver(BeaconSyncPayload.BeaconSelectionPayload.TYPE, (payload, context) -> {
			context.server().execute(() -> {
				if (context.player().containerMenu instanceof BeaconMenu menu) {
					LOGGER.info("Received beacon effect selection from player: {} -> primary: {}, level: {}", 
						context.player().getName().getString(), payload.primaryEffect(), payload.requestedLevel());
				}
			});
		});

		// Register /custombeacon command tree
		CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
			CustomBeaconCommand.register(dispatcher);
		});

		LOGGER.info("Custom Beacon Overhaul Mod initialized successfully.");
	}

	public static Identifier id(String path) {
		return Identifier.fromNamespaceAndPath(MOD_ID, path);
	}
}
