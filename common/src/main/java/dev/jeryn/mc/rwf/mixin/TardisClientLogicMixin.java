package dev.jeryn.mc.rwf.mixin;

import dev.jeryn.mc.rwf.RWFAnimationStateAccessor;
import dev.jeryn.mc.rwf.client.ClientFlightTracker;
import net.minecraft.world.entity.AnimationState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import whocraft.tardis_refined.client.TardisClientData;
import whocraft.tardis_refined.client.TardisClientLogic;

@Mixin(TardisClientLogic.class)
public class TardisClientLogicMixin {

    @Inject(
            method = "update",
            at = @At("TAIL"),
            remap = false
    )
    private static void rwf$onUpdate(TardisClientData tardisClientData, CallbackInfo ci) {
        boolean rwfFlying = ClientFlightTracker.isFlying(tardisClientData.getLevelKey());
        AnimationState animState = ((RWFAnimationStateAccessor) tardisClientData).rwf$getAnimState();

        if (rwfFlying) {
            if (!animState.isStarted()) {
                animState.start(0);
            }
        } else {
            animState.stop();
        }
    }
}