package mc.craig.software;

import mc.craig.software.common.entity.TardisEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.Input;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.Entity;

public class ClientUtil {
    public static void handleInput(LocalPlayer localPlayer, Input input) {
        if(localPlayer.getFirstPassenger() instanceof TardisEntity tardis && localPlayer.isOnGround()){
            input.down = false;
            input.up = false;
            input.forwardImpulse = 0;
            input.left = false;
            input.right = false;
            input.leftImpulse = 0;
            input.shiftKeyDown = false;
        }
    }
}
