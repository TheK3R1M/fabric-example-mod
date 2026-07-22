package com.example.beacon;

import com.example.config.ModConfig;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BeaconBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class BeaconNetworkState {

    public enum Role {
        MASTER,
        SLAVE,
        STANDALONE
    }

    private Role role = Role.STANDALONE;
    private BlockPos masterPos = null;
    private int tickCounter = 0;
    private double calculatedPowerWeight = 1.0;
    private int cachedTier = 0;
    private boolean slaveLinked = false;

    // Cache of active beacon positions on server
    private static final Map<BlockPos, BeaconNetworkState> ACTIVE_NETWORKS = new ConcurrentHashMap<>();

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }

    public BlockPos getMasterPos() {
        return masterPos;
    }

    public void setMasterPos(BlockPos masterPos) {
        this.masterPos = masterPos;
    }

    public double getCalculatedPowerWeight() {
        return calculatedPowerWeight;
    }

    public void setCalculatedPowerWeight(double calculatedPowerWeight) {
        this.calculatedPowerWeight = calculatedPowerWeight;
    }

    public int getCachedTier() {
        return cachedTier;
    }

    public void setCachedTier(int cachedTier) {
        this.cachedTier = cachedTier;
    }

    public boolean isSlaveLinked() {
        return slaveLinked;
    }

    /**
     * Ticks the network state. Validates Master-Slave links and pyramid structures 
     * ONLY once every 40-80 ticks (configured via baseScanIntervalTicks) to prevent tick lag.
     */
    public boolean shouldValidateThisTick() {
        tickCounter++;
        int interval = Math.max(20, ModConfig.get().baseScanIntervalTicks);
        if (tickCounter >= interval) {
            tickCounter = 0;
            return true;
        }
        return false;
    }

    public void validateNetwork(Level level, BlockPos currentPos) {
        if (level == null || level.isClientSide()) return;

        if (role == Role.SLAVE) {
            if (masterPos != null) {
                BlockEntity targetEntity = level.getBlockEntity(masterPos);
                if (targetEntity instanceof BeaconBlockEntity) {
                    // Check if Master is valid
                    BeaconNetworkState masterState = getOrCreate(masterPos);
                    if (masterState.getRole() == Role.MASTER && masterState.getCachedTier() > 0) {
                        this.slaveLinked = true;
                        return;
                    }
                }
            }
            // Master broken or unpowered -> slave becomes unlinked
            this.slaveLinked = false;
        } else {
            this.slaveLinked = false;
        }
    }

    public void save(ValueOutput output) {
        output.putString("BeaconRole", role.name());
        if (masterPos != null) {
            output.store("MasterPos", BlockPos.CODEC, masterPos);
        }
        output.putDouble("PowerWeight", calculatedPowerWeight);
        output.putInt("CachedTier", cachedTier);
    }

    public void load(ValueInput input) {
        String roleStr = input.getStringOr("BeaconRole", Role.STANDALONE.name());
        try {
            this.role = Role.valueOf(roleStr);
        } catch (IllegalArgumentException e) {
            this.role = Role.STANDALONE;
        }

        this.masterPos = input.read("MasterPos", BlockPos.CODEC).orElse(null);
        this.calculatedPowerWeight = input.getDoubleOr("PowerWeight", 1.0);
        this.cachedTier = input.getIntOr("CachedTier", 0);
    }

    public static BeaconNetworkState getOrCreate(BlockPos pos) {
        return ACTIVE_NETWORKS.computeIfAbsent(pos, p -> new BeaconNetworkState());
    }

    public static void remove(BlockPos pos) {
        ACTIVE_NETWORKS.remove(pos);
    }
}
