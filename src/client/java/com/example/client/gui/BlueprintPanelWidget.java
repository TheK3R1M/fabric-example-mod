package com.example.client.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public class BlueprintPanelWidget extends AbstractWidget {

    private int selectedTier = 1;
    private String selectedEffectId = "minecraft:haste";

    public BlueprintPanelWidget(int x, int y, int width, int height) {
        super(x, y, width, height, Component.literal("Blueprint Helper Panel"));
    }

    public void updateSelection(int tier, String effectId) {
        this.selectedTier = tier;
        this.selectedEffectId = effectId;
    }

    @Override
    protected void extractWidgetRenderState(GuiGraphicsExtractor extractor, int mouseX, int mouseY, float partialTick) {
        // Draw side panel background box
        extractor.fill(this.getX(), this.getY(), this.getX() + this.width, this.getY() + this.height, 0xC0101010);

        // Header
        extractor.text(Minecraft.getInstance().font, "BLUEPRINT HELPER", this.getX() + 6, this.getY() + 8, 0xFFFFD700);

        // Required Tier
        extractor.text(Minecraft.getInstance().font, "Req. Tier: Level " + selectedTier, this.getX() + 6, this.getY() + 24, 0xFF55FF55);

        // Calculate total blocks required for Base Width = 2n + 1
        int totalBlocks = calculateTotalBlocks(selectedTier);
        int stacks = totalBlocks / 64;
        int remainder = totalBlocks % 64;
        String costFormatted = stacks > 0 ? stacks + " Stacks + " + remainder + " Blocks" : remainder + " Blocks";

        extractor.text(Minecraft.getInstance().font, "Total Base Cost:", this.getX() + 6, this.getY() + 40, 0xFFAAAAAA);
        extractor.text(Minecraft.getInstance().font, costFormatted, this.getX() + 6, this.getY() + 52, 0xFF55FFFF);

        // Max Vanilla Pyramid Icon for Tier >= 9
        if (selectedTier >= 9) {
            extractor.text(Minecraft.getInstance().font, "[MAX TIER BEACON]", this.getX() + 6, this.getY() + 72, 0xFFFF5555);
            extractor.item(new ItemStack(Items.BEACON), this.getX() + 6, this.getY() + 86);
        }
    }

    private int calculateTotalBlocks(int tier) {
        int total = 0;
        for (int layer = 1; layer <= tier; layer++) {
            int width = (2 * layer) + 1;
            total += (width * width);
        }
        return total;
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {
        defaultButtonNarrationText(narrationElementOutput);
    }
}
