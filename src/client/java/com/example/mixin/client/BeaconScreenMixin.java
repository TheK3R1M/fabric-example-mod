package com.example.mixin.client;

import com.example.client.config.UILayoutConfig;
import com.example.client.gui.BlueprintPanelWidget;
import com.example.client.gui.ScrollableEffectWidget;
import com.example.network.BeaconSyncPayload;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.gui.components.AbstractWidget;
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

    // 1. Wipe Vanilla Beacon Buttons: Block vanilla addBeaconButton calls
    @Inject(method = "addBeaconButton", at = @At("HEAD"), cancellable = true)
    private void onAddBeaconButton(AbstractWidget button, CallbackInfo ci) {
        ci.cancel();
    }

    // 2. Wipe Vanilla Beacon Buttons: Block vanilla button state updates
    @Inject(method = "updateButtons", at = @At("HEAD"), cancellable = true)
    private void onUpdateButtons(CallbackInfo ci) {
        ci.cancel();
    }

    @Inject(method = "init", at = @At("TAIL"))
    private void onBeaconScreenInit(CallbackInfo ci) {
        // Clear any residual widgets
        this.clearWidgets();

        // Load or auto-generate dynamic UI layout coordinates from config/ui_layout.json
        UILayoutConfig.LayoutData layout = UILayoutConfig.loadOrGenerate();

        int left = (this.width - this.imageWidth) / 2;
        int top = (this.height - this.imageHeight) / 2;

        // A. Blueprint Helper Panel
        this.blueprintPanel = new BlueprintPanelWidget(
            left + layout.blueprintPanel.x,
            top + layout.blueprintPanel.y,
            layout.blueprintPanel.width,
            layout.blueprintPanel.height
        );
        this.blueprintPanel.updateSelection(this.selectedLevel, this.selectedEffect);
        this.addRenderableWidget(this.blueprintPanel);

        // B. Interactive Scrollable Grid of Effect Buttons
        this.scrollableEffects = new ScrollableEffectWidget(
            left + layout.scrollableGrid.x,
            top + layout.scrollableGrid.y,
            layout.scrollableGrid.width,
            layout.scrollableGrid.height,
            (effectId, level) -> {
                this.selectedEffect = effectId;
                this.selectedLevel = level;

                // Instant cost & tier recalculation on left panel
                if (this.blueprintPanel != null) {
                    this.blueprintPanel.updateSelection(level, effectId);
                }
            }
        );
        this.addRenderableWidget(this.scrollableEffects);

        // C. Custom Confirm Checkmark Button
        Button confirmButton = Button.builder(Component.literal("✔ Confirm"), b -> {
            // Send selection C2S payload to server
            ClientPlayNetworking.send(new BeaconSyncPayload.BeaconSelectionPayload(
                net.minecraft.core.BlockPos.ZERO,
                this.selectedEffect,
                "minecraft:none",
                this.selectedLevel
            ));
            this.onClose();
        }).bounds(
            left + layout.confirmButton.x,
            top + layout.confirmButton.y,
            layout.confirmButton.width,
            layout.confirmButton.height
        ).build();

        this.addRenderableWidget(confirmButton);
    }
}
