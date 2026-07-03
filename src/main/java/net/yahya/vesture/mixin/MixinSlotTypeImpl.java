package net.yahya.vesture.mixin;

import eu.pb4.trinkets.impl.SlotTypeImpl;
import net.yahya.vesture.VestureConfig;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = SlotTypeImpl.class, remap = false)
public abstract class MixinSlotTypeImpl {

    @Inject(method = "amount", at = @At("RETURN"), cancellable = true)
    private void vesture$overrideAmount(CallbackInfoReturnable<Integer> cir) {
        SlotTypeImpl self = (SlotTypeImpl) (Object) this;
        int configured = VestureConfig.getInstance().getAmount(self.group(), self.name());
        if (configured >= 0) {
            cir.setReturnValue(configured);
        }
    }
}
