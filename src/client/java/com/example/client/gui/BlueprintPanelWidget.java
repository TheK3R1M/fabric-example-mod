package com.example.client.gui;

import com.example.payment.PaymentCalculator;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public class BlueprintPanelWidget extends AbstractWidget {

    private int selectedLevel = 1;
    private String selectedEffectId = "minecraft:haste";
    private PaymentCalculator.PaymentRequirement currentPaymentReq;

    public BlueprintPanelWidget(int x, int y, int width, int height) {
        super(x, y, width, height, Component.literal("Blueprint Helper Panel"));
        recalculateCost();
    }

    public void updateSelection(int level, String effectId) {
        this.selectedLevel = level;
        this.selectedEffectId = effectId;
        recalculateCost();
    }

    private void recalculateCost() {
        this.currentPaymentReq = PaymentCalculator.calculateCost(selectedEffectId, selectedLevel);
    }

    @Override
    protected void extractWidgetRenderState(GuiGraphicsExtractor extractor, int mouseX, int mouseY, float partialTick) {
        // Render panel background
        extractor.fill(this.getX(), this.getY(), this.getX() + this.width, this.getY() + this.height, 0xD0101010);
        extractor.fill(this.getX() - 1, this.getY() - 1, this.getX() + this.width + 1, this.getY(), 0xFF444444);
        extractor.fill(this.getX() - 1, this.getY() + this.height, this.getX() + this.width + 1, this.getY() + this.height + 1, 0xFF444444);

        // Header
        extractor.text(Minecraft.getInstance().font, "BLUEPRINT HELPER", this.getX() + 6, this.getY() + 8, 0xFFFFD700);

        // Required Tier Level
        extractor.text(Minecraft.getInstance().font, "Req. Tier: Level " + selectedLevel, this.getX() + 6, this.getY() + 24, 0xFF55FF55);

        // Format cost requirement from PaymentCalculator
        if (currentPaymentReq != null) {
            String itemName = BuiltInRegistries.ITEM.getKey(currentPaymentReq.requiredItem()).getPath().replace("_", " ").toUpperCase();
            int count = currentPaymentReq.requiredCount();
            int stacks = count / 64;
            int remainder = count % 64;

            String costFormatted = stacks > 0 ? stacks + " Stacks + " + remainder : count + "x " + itemName;

            extractor.text(Minecraft.getInstance().font, "Required Tribute:", this.getX() + 6, this.getY() + 42, 0xFFAAAAAA);
            extractor.text(Minecraft.getInstance().font, costFormatted, this.getX() + 6, this.getY() + 54, 0xFF55FFFF);

            // Item preview icon
            extractor.item(new ItemStack(currentPaymentReq.requiredItem()), this.getX() + 6, this.getY() + 68);
        }

        // Max Tier Icon Indicator for Tier >= 9
        if (selectedLevel >= 9) {
            extractor.text(Minecraft.getInstance().font, "[MAX TIER BEACON]", this.getX() + 6, this.getY() + 92, 0xFFFF5555);
            extractor.item(new ItemStack(Items.BEACON), this.getX() + 6, this.getY() + 104);
        }
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {
        defaultButtonNarrationText(narrationElementOutput);
    }
}
