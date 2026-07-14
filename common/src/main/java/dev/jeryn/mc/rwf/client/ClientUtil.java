package dev.jeryn.mc.rwf.client;

import dev.jeryn.mc.rwf.network.LocalHopMessage;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.Input;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

public class ClientUtil {
    public static void handleInput(LocalPlayer localPlayer, Input input) {
        if (ClientFlightTracker.getForPlayer(localPlayer.getUUID()).isEmpty()) return;

        if (localPlayer.onGround()) {
            input.down = false;
            input.up = false;
            input.forwardImpulse = 0;
            input.left = false;
            input.right = false;
            input.leftImpulse = 0;
        }
    }

    private static final double LOCAL_HOP_MAX_DISTANCE = 64.0;

    public static void attemptLocalHop() {
        Minecraft mc = Minecraft.getInstance();
        LocalPlayer player = mc.player;
        if (player == null || mc.level == null) return;
        if (ClientFlightTracker.getForPlayer(player.getUUID()).isEmpty()) return;

        Vec3 eye = player.getEyePosition(1.0F);
        Vec3 look = player.getViewVector(1.0F);
        Vec3 end = eye.add(look.scale(LOCAL_HOP_MAX_DISTANCE));

        ClipContext ctx = new ClipContext(eye, end, ClipContext.Block.OUTLINE, ClipContext.Fluid.NONE, player);
        BlockHitResult hit = mc.level.clip(ctx);

        if (hit.getType() != HitResult.Type.BLOCK) return;

        new LocalHopMessage(hit.getBlockPos(), hit.getDirection()).send();
    }
}
