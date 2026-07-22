package com.example.client;

import com.example.beacon.BeaconNetworkState;
import com.example.client.render.SlaveBeaconRenderer;
import com.example.network.BeaconSyncPayload;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderers;
import net.minecraft.world.level.block.entity.BlockEntityType;

@Environment(EnvType.CLIENT)
public class ExampleModClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        // Register custom BlockEntityRenderer for Slave beacons
        BlockEntityRenderers.register(BlockEntityType.BEACON, ctx -> new SlaveBeaconRenderer());

        // Register S2C Global Network Receiver
        ClientPlayNetworking.registerGlobalReceiver(BeaconSyncPayload.BeaconStateSyncPayload.TYPE, (payload, context) -> {
            context.client().execute(() -> {
                BeaconNetworkState netState = BeaconNetworkState.getOrCreate(payload.masterPos());
                netState.setCachedTier(payload.tier());
                netState.setCalculatedPowerWeight(payload.powerWeight());
            });
        });
    }
}