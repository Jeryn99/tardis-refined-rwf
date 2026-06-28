package dev.jeryn.mc.rwf.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import whocraft.tardis_refined.common.blockentity.console.GlobalConsoleBlockEntity;

public class ConsolePilotRendererOld {

    // Tweak this per console theme if needed
    private static final float PLAYER_Y_OFFSET = -1.5F;
    private static final float PLAYER_Z_OFFSET = -0.6F;

    public static void render(
            GlobalConsoleBlockEntity blockEntity,
            float partialTick,
            PoseStack poseStack,
            MultiBufferSource bufferSource,
            int packedLight
    ) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null) return;

        Level level = blockEntity.getLevel();
        if (level == null) return;

        EntityRenderDispatcher dispatcher = mc.getEntityRenderDispatcher();

        renderPilot(Minecraft.getInstance().player, partialTick, poseStack, bufferSource, packedLight, dispatcher);

}

private static void renderPilot(
        Player pilot,
        float partialTick,
        PoseStack poseStack,
        MultiBufferSource bufferSource,
        int packedLight,
        EntityRenderDispatcher dispatcher
) {
    poseStack.pushPose();

    poseStack.translate(0.0F, 1.5F, -2.5F);
    poseStack.mulPose(Axis.XP.rotationDegrees(180.0F));
    poseStack.mulPose(Axis.YP.rotationDegrees(180.0F));

    dispatcher.render(
            pilot,
            0.0, 0.0, 0.0,
            0,
            partialTick,
            poseStack,
            bufferSource,
            packedLight
    );

    poseStack.popPose();
}
}