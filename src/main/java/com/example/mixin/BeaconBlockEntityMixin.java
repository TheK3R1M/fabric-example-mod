package com.example.mixin;

import com.example.beacon.BeaconNetworkState;
import com.example.config.ModConfig;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BeaconBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(BeaconBlockEntity.class)
public abstract class BeaconBlockEntityMixin extends BlockEntity {

    @Shadow
    private int levels;

    public BeaconBlockEntityMixin(BlockPos pos, BlockState state) {
        super(null, pos, state);
    }

    @Inject(method = "updateBase", at = @At("HEAD"), cancellable = true)
    private static void customUpdateBase(Level level, int x, int y, int z, CallbackInfoReturnable<Integer> cir) {
        BlockPos currentPos = new BlockPos(x, y, z);
        BeaconNetworkState netState = BeaconNetworkState.getOrCreate(currentPos);

        // SLAVE Beacon Bypass: If linked to a valid Master, bypass base requirement
        if (netState.getRole() == BeaconNetworkState.Role.SLAVE && netState.isSlaveLinked()) {
            netState.setCachedTier(1);
            cir.setReturnValue(1);
            return;
        }

        // Algorithmic Tier Base Width = 2n + 1
        int calculatedTier = 0;
        double totalPowerWeight = 0.0;
        int maxAllowedTier = ModConfig.get().maxTier;

        for (int layer = 1; layer <= maxAllowedTier; layer++) {
            int currentY = y - layer;
            if (currentY < level.getMinY()) break;

            boolean layerValid = true;
            double layerWeight = 0.0;
            int radius = layer; // Base Width = 2*layer + 1 -> radius = layer

            for (int dx = -radius; dx <= radius; dx++) {
                for (int dz = -radius; dz <= radius; dz++) {
                    BlockPos blockPos = new BlockPos(x + dx, currentY, z + dz);
                    BlockState state = level.getBlockState(blockPos);
                    Block block = state.getBlock();
                    String blockId = BuiltInRegistries.BLOCK.getKey(block).toString();

                    double weight = ModConfig.get().getMineralWeight(blockId);
                    if (weight > 0.0) {
                        layerWeight += weight;
                    } else {
                        // Not a recognized mineral block -> break pyramid
                        layerValid = false;
                        break;
                    }
                }
                if (!layerValid) break;
            }

            if (layerValid) {
                calculatedTier++;
                totalPowerWeight += layerWeight;
            } else {
                break;
            }
        }

        netState.setCachedTier(calculatedTier);
        netState.setCalculatedPowerWeight(totalPowerWeight);
        cir.setReturnValue(calculatedTier);
    }

    @Inject(method = "tick", at = @At("HEAD"))
    private static void onBeaconTick(Level level, BlockPos pos, BlockState state, BeaconBlockEntity blockEntity, CallbackInfo ci) {
        if (level != null && !level.isClientSide()) {
            BeaconNetworkState netState = BeaconNetworkState.getOrCreate(pos);
            if (netState.shouldValidateThisTick()) {
                netState.validateNetwork(level, pos);
            }
        }
    }

    @Inject(method = "saveAdditional", at = @At("TAIL"))
    private void onSaveAdditional(ValueOutput output, CallbackInfo ci) {
        BeaconNetworkState netState = BeaconNetworkState.getOrCreate(this.worldPosition);
        netState.save(output);
    }

    @Inject(method = "loadAdditional", at = @At("TAIL"))
    private void onLoadAdditional(ValueInput input, CallbackInfo ci) {
        BeaconNetworkState netState = BeaconNetworkState.getOrCreate(this.worldPosition);
        netState.load(input);
    }

    @Override
    public void setRemoved() {
        BeaconNetworkState.remove(this.worldPosition);
        super.setRemoved();
    }
}
