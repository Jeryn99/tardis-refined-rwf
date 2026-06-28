package dev.jeryn.mc.rwf.mixin;

import dev.jeryn.mc.rwf.client.ClientFlightTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.world.level.Level;
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
        Level level = Minecraft.getInstance().level;
        if (level == null) return;

        boolean rwfFlying = ClientFlightTracker.isFlying(level.dimension());

        // Only act if TR's own isFlying() disagrees with our tracker,
        // so we don't double-start or double-stop anything TR already handled.
        if (rwfFlying && !tardisClientData.isFlying()) {
            if (!tardisClientData.ROTOR_ANIMATION.isStarted()) {
                tardisClientData.ROTOR_ANIMATION.start(0);
            }
        } else if (!rwfFlying && tardisClientData.isFlying()) {
            // TR thinks it's flying but we don't — leave TR in control, do nothing
        }
    }
}