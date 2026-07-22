package com.example.mixin.client;

import com.example.client.gui.BlueprintPanelWidget;
import com.example.client.gui.ScrollableEffectWidget;
import com.example.network.BeaconSyncPayload;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.BeaconScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.BeaconMenu;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(BeaconScreen.class)
public abstract class BeaconScreenMixin extends AbstractContainerScreen<BeaconMenu> {

    private BlueprintPanelWidget blueprintPanel;
    private ScrollableEffectWidget scrollableEffects;
    private String selectedEffect = "minecraft:haste";
    private int selectedLevel = 1;

    public BeaconScreenMixin(BeaconMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
    }

    @Inject(method = "init", at = @At("TAIL"))
    private void onBeaconScreenInit(CallbackInfo ci) {
        // Wipe Vanilla Beacon Effect Buttons to replace with custom UI
        this.clearWidgets();

        int left = (this.width - this.imageWidth) / 2;
        int top = (this.height - this.imageHeight) / 2;

        // 1. Blueprint Helper Panel on the left side
        this.blueprintPanel = new BlueprintPanelWidget(left - 125, top, 120, 168);
        this.addRenderableWidget(this.blueprintPanel);

        // 2. Interactive Scrollable Grid of Effect Buttons on the right
        this.scrollableEffects = new ScrollableEffectWidget(left + 175, top, 120, 168, (effectId, level) -> {
            this.selectedEffect = effectId;
            this.selectedLevel = level;

            if (this.blueprintPanel != null) {
                this.blueprintPanel.updateSelection(level, effectId);
            }
        });
        this.addRenderableWidget(this.scrollableEffects);

        // 3. Custom Confirm Checkmark Button
        Button confirmButton = Button.builder(Component.literal("✔ Confirm"), b -> {
            // Send selection C2S payload to server
            ClientPlayNetworking.send(new BeaconSyncPayload.BeaconSelectionPayload(
                net.minecraft.core.BlockPos.ZERO,
                this.selectedEffect,
                "minecraft:none",
                this.selectedLevel
            ));
            this.onClose();
        }).bounds(left + 50, top + 140, 70, 20).build();

        this.addRenderableWidget(confirmButton);
    }
}
