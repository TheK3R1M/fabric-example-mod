package com.example.mixin;

import com.example.payment.PaymentCalculator;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.BeaconMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(BeaconMenu.class)
public abstract class BeaconMenuMixin extends AbstractContainerMenu {

    protected BeaconMenuMixin(MenuType<?> menuType, int containerId) {
        super(menuType, containerId);
    }

    @Inject(method = "quickMoveStack", at = @At("HEAD"), cancellable = true)
    private void customQuickMoveStack(Player player, int index, CallbackInfoReturnable<ItemStack> cir) {
        ItemStack resultStack = ItemStack.EMPTY;
        Slot slot = this.slots.get(index);

        if (slot != null && slot.hasItem()) {
            ItemStack stackInSlot = slot.getItem();
            resultStack = stackInSlot.copy();

            // Payment Slot Index is 0
            if (index == 0) {
                // Moving out of tribute slot to player inventory (slots 1 to 37)
                if (!this.moveItemStackTo(stackInSlot, 1, 37, true)) {
                    cir.setReturnValue(ItemStack.EMPTY);
                    return;
                }
                slot.onQuickCraft(stackInSlot, resultStack);
            } else {
                // Shift clicking from player inventory into payment slot (index 0)
                if (PaymentCalculator.isPaymentValid(stackInSlot)) {
                    if (!this.moveItemStackTo(stackInSlot, 0, 1, false)) {
                        cir.setReturnValue(ItemStack.EMPTY);
                        return;
                    }
                } else if (index >= 1 && index < 28) {
                    // Inventory to hotbar
                    if (!this.moveItemStackTo(stackInSlot, 28, 37, false)) {
                        cir.setReturnValue(ItemStack.EMPTY);
                        return;
                    }
                } else if (index >= 28 && index < 37) {
                    // Hotbar to inventory
                    if (!this.moveItemStackTo(stackInSlot, 1, 28, false)) {
                        cir.setReturnValue(ItemStack.EMPTY);
                        return;
                    }
                }
            }

            if (stackInSlot.isEmpty()) {
                slot.setByPlayer(ItemStack.EMPTY);
            } else {
                slot.setChanged();
            }

            if (stackInSlot.getCount() == resultStack.getCount()) {
                cir.setReturnValue(ItemStack.EMPTY);
                return;
            }

            slot.onTake(player, stackInSlot);
            cir.setReturnValue(resultStack);
        } else {
            cir.setReturnValue(ItemStack.EMPTY);
        }
    }
}
