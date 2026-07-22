package com.example.mixin;

import com.example.payment.PaymentCalculator;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(targets = "net.minecraft.world.inventory.BeaconMenu$PaymentSlot")
public abstract class BeaconPaymentSlotMixin extends Slot {

    public BeaconPaymentSlotMixin() {
        super(null, 0, 0, 0);
    }

    @Inject(method = "mayPlace", at = @At("HEAD"), cancellable = true)
    private void customMayPlace(ItemStack stack, CallbackInfoReturnable<Boolean> cir) {
        cir.setReturnValue(PaymentCalculator.isPaymentValid(stack));
    }

    @Inject(method = "getMaxStackSize()I", at = @At("HEAD"), cancellable = true)
    private void customGetMaxStackSize(CallbackInfoReturnable<Integer> cir) {
        cir.setReturnValue(64);
    }

    @Inject(method = "getMaxStackSize(Lnet/minecraft/world/item/ItemStack;)I", at = @At("HEAD"), cancellable = true)
    private void customGetMaxStackSizeForStack(ItemStack stack, CallbackInfoReturnable<Integer> cir) {
        cir.setReturnValue(64);
    }
}
