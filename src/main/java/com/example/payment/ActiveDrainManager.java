package com.example.payment;

import com.example.config.ModConfig;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BeaconBlockEntity;
import net.minecraft.world.phys.AABB;

import java.util.List;

public class ActiveDrainManager {

    private int tickCounter = 0;
    private boolean powerOutage = false;

    public boolean isPowerOutage() {
        return powerOutage;
    }

    /**
     * Executes tick-based upkeep drain for active beacons if enabled in configuration.
     */
    public void tickDrain(Level level, BlockPos pos, BeaconBlockEntity beacon, int beaconTier, Container tributeContainer) {
        if (level == null || level.isClientSide() || beacon == null) return;

        // Check if active drain is enabled in config
        if (!ModConfig.get().enableActiveDrain) {
            powerOutage = false;
            return;
        }

        tickCounter++;
        int intervalTicks = Math.max(200, ModConfig.get().activeDrainIntervalMinutes * 1200); // 20 tps * 60s

        if (tickCounter >= intervalTicks) {
            tickCounter = 0;
            boolean paymentSuccess = tryConsumeTributeItem(tributeContainer);

            if (!paymentSuccess) {
                // Alternative upkeep drain: Try draining player XP / Hunger within AoE
                paymentSuccess = tryDrainNearbyPlayers(level, pos, beaconTier);
            }

            // Power Outage Fallback: If no payment or player drain succeeded, trigger outage
            powerOutage = !paymentSuccess;
        }
    }

    private boolean tryConsumeTributeItem(Container tributeContainer) {
        if (tributeContainer == null || tributeContainer.isEmpty()) return false;

        for (int slot = 0; slot < tributeContainer.getContainerSize(); slot++) {
            ItemStack stack = tributeContainer.getItem(slot);
            if (!stack.isEmpty() && PaymentCalculator.isPaymentValid(stack)) {
                stack.shrink(1);
                tributeContainer.setChanged();
                return true;
            }
        }
        return false;
    }

    private boolean tryDrainNearbyPlayers(Level level, BlockPos pos, int beaconTier) {
        double radius = (beaconTier * 10.0) + 10.0;
        AABB aabb = new AABB(pos).inflate(radius);
        List<ServerPlayer> players = level.getEntitiesOfClass(ServerPlayer.class, aabb);

        if (players.isEmpty()) return false;

        boolean drainedAny = false;
        for (ServerPlayer player : players) {
            if (player == null || player.isSpectator()) continue;

            // Drain XP or Hunger
            if (player.experienceLevel > 0 || player.experienceProgress > 0.0f) {
                player.giveExperiencePoints(-5);
                drainedAny = true;
            } else if (player.getFoodData().getFoodLevel() > 0) {
                player.getFoodData().addExhaustion(3.0f);
                drainedAny = true;
            }
        }
        return drainedAny;
    }
}
