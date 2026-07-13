package dev.jeryn.mc.rwf.client;

import dev.jeryn.mc.rwf.common.entity.TardisEntity;
import net.minecraft.client.player.Input;
import net.minecraft.client.player.LocalPlayer;

public class ClientUtil {
    public static void handleInput(LocalPlayer localPlayer, Input input) {
        if (!(localPlayer.getFirstPassenger() instanceof TardisEntity)) return;

        if (localPlayer.onGround()) {
            input.down = false;
            input.up = false;
            input.forwardImpulse = 0;
            input.left = false;
            input.right = false;
            input.leftImpulse = 0;
        }
    }
}
