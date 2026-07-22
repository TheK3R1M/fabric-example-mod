package com.example.client;

import com.example.client.render.SlaveBeaconRenderer;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderers;
import net.minecraft.world.level.block.entity.BlockEntityType;

@Environment(EnvType.CLIENT)
public class ExampleModClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        // Register custom BlockEntityRenderer for Slave beacons
        BlockEntityRenderers.register(BlockEntityType.BEACON, ctx -> new SlaveBeaconRenderer());
    }
}