package com.example.payment;

import com.example.config.ModConfig;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.Container;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public class PaymentCalculator {

    public record PaymentRequirement(Item requiredItem, int requiredCount, String tierName) {
        public boolean isSatisfiedBy(ItemStack stack) {
            return !stack.isEmpty() && stack.is(requiredItem) && stack.getCount() >= requiredCount;
        }
    }

    /**
     * Calculates the payment requirement for a requested effect level.
     * Implements material evolution chain: Ingot -> Block -> Nether Star.
     */
    public static PaymentRequirement calculateCost(String effectId, int requestedLevel) {
        int hardCap = ModConfig.get().getEffectHardCap(effectId);
        int effectiveLevel = Math.min(requestedLevel, hardCap);
        double multiplier = ModConfig.get().getEffectCostMultiplier(effectId);

        // Exponential cost curve: Base = 2^(level - 1) * multiplier
        int rawItemCost = (int) Math.round(Math.pow(2.0, Math.max(0, effectiveLevel - 1)) * multiplier);

        // Standard material chain lookup
        Item baseItem = getBaseItemForEffect(effectId);
        Item blockItem = getBlockItemForBase(baseItem);

        // Stack Capping & Material Evolution Shift Logic
        if (rawItemCost <= 64) {
            return new PaymentRequirement(baseItem, Math.max(1, rawItemCost), "INGOT_TIER");
        }

        // Shift to Block Variant
        int blockCount = (rawItemCost + 8) / 9; // 9 ingots per block
        if (blockCount <= 64) {
            return new PaymentRequirement(blockItem, Math.max(1, blockCount), "BLOCK_TIER");
        }

        // Shift to Nether Star Tier
        int netherStarCount = Math.min(64, Math.max(1, blockCount / 4));
        return new PaymentRequirement(Items.NETHER_STAR, netherStarCount, "NETHER_STAR_TIER");
    }

    private static Item getBaseItemForEffect(String effectId) {
        if (effectId.contains("strength") || effectId.contains("resistance")) {
            return Items.DIAMOND;
        } else if (effectId.contains("haste") || effectId.contains("speed")) {
            return Items.GOLD_INGOT;
        }
        return Items.IRON_INGOT;
    }

    private static Item getBlockItemForBase(Item baseItem) {
        if (baseItem == Items.DIAMOND) return Items.DIAMOND_BLOCK;
        if (baseItem == Items.GOLD_INGOT) return Items.GOLD_BLOCK;
        if (baseItem == Items.EMERALD) return Items.EMERALD_BLOCK;
        if (baseItem == Items.NETHERITE_INGOT) return Items.NETHERITE_BLOCK;
        return Items.IRON_BLOCK;
    }

    public static boolean isPaymentValid(ItemStack stack) {
        if (stack.isEmpty()) return false;
        Item item = stack.getItem();
        return item == Items.IRON_INGOT || item == Items.GOLD_INGOT || item == Items.EMERALD ||
               item == Items.DIAMOND || item == Items.NETHERITE_INGOT || item == Items.IRON_BLOCK ||
               item == Items.GOLD_BLOCK || item == Items.EMERALD_BLOCK || item == Items.DIAMOND_BLOCK ||
               item == Items.NETHERITE_BLOCK || item == Items.NETHER_STAR;
    }
}
