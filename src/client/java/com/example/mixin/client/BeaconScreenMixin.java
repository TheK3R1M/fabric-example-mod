package com.example.mixin.client;

import com.example.client.gui.BlueprintPanelWidget;
import com.example.client.gui.ScrollableEffectWidget;
import com.example.network.BeaconSyncPayload;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
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

    public BeaconScreenMixin(BeaconMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
    }

    @Inject(method = "init", at = @At("TAIL"))
    private void onBeaconScreenInit(CallbackInfo ci) {
        int left = (this.width - this.imageWidth) / 2;
        int top = (this.height - this.imageHeight) / 2;

        // Blueprint Panel on the left side of the screen
        this.blueprintPanel = new BlueprintPanelWidget(left - 125, top, 120, 168);
        this.addRenderableWidget(this.blueprintPanel);

        // Scrollable Effect Menu Widget inside screen bounds
        this.scrollableEffects = new ScrollableEffectWidget(left + 175, top, 110, 168, (effectId, level) -> {
            if (this.blueprintPanel != null) {
                this.blueprintPanel.updateSelection(level, effectId);
            }
            // Send selection C2S payload
            if (this.menu != null) {
                ClientPlayNetworking.send(new BeaconSyncPayload.BeaconSelectionPayload(
                    this.menu.getLevels() > 0 ? BlockPosAtMenu(top, left) : net.minecraft.core.BlockPos.ZERO,
                    effectId,
                    "minecraft:none",
                    level
                ));
            }
        });
        this.addRenderableWidget(this.scrollableEffects);
    }

    private net.minecraft.core.BlockPos BlockPosAtMenu(int top, int left) {
        return net.minecraft.core.BlockPos.ZERO;
    }
}
