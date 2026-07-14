package dev.jeryn.mc.rwf.mixin;

import dev.jeryn.mc.rwf.client.ClientFlightTracker;
import net.minecraft.client.gui.Gui;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Gui.class)
public class HudHideMixin {

    @Inject(method = "renderPlayerHealth(Lnet/minecraft/client/gui/GuiGraphics;)V", at = @At("HEAD"), cancellable = true)
    private void hideFood(CallbackInfo ci) {
        if (isInTardis()) ci.cancel();
    }

    @Inject(method = "renderHearts", at = @At("HEAD"), cancellable = true)
    private void hideHealth(CallbackInfo ci) {
        if (isInTardis()) ci.cancel();
    }

    @Inject(method = "renderHotbar", at = @At("HEAD"), cancellable = true)
    private void hideHotbar(CallbackInfo ci) {
        if (isInTardis()) ci.cancel();
    }

    private boolean isInTardis() {
        var mc = net.minecraft.client.Minecraft.getInstance();
        return mc.player != null
                && ClientFlightTracker.getForPlayer(mc.player.getUUID()).isPresent();
    }
}