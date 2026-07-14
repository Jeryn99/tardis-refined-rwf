package dev.jeryn.mc.rwf.mixin;

import dev.jeryn.mc.rwf.client.ClientFlightTracker;
import net.minecraft.client.MouseHandler;
import net.minecraft.client.player.LocalPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(MouseHandler.class)
public class MouseHandlerMixin {

    @Redirect(method = "onScroll(JDD)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/player/LocalPlayer;isSpectator()Z"))
    private boolean rwf$treatPilotingAsSpectatorForScroll(LocalPlayer player) {
        if (player.isSpectator()) return true;
        return ClientFlightTracker.getForPlayer(player.getUUID()).isPresent();
    }
}
