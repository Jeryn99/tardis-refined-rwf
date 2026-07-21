package dev.jeryn.mc.rwf.mixin;

import dev.jeryn.mc.rwf.common.entity.FlightTracker;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import whocraft.tardis_refined.common.capability.tardis.TardisLevelOperator;
import whocraft.tardis_refined.common.tardis.manager.TardisPilotingManager;

@Mixin(TardisPilotingManager.class)
public class TardisPilotingManagerMixin {

    @Shadow
    private TardisLevelOperator operator;

    @Inject(at = @At("RETURN"), cancellable = true, method = "canBeginFlight()Z", remap = false)
    private void rwf$blockBeginFlightWhileRWF(CallbackInfoReturnable<Boolean> cir) {
        if (FlightTracker.isFlying(operator.getLevelKey())) {
            cir.setReturnValue(false);
        }
    }
}
