package dev.jeryn.mc.rwf.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import dev.jeryn.mc.rwf.client.ConsolePilotRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import whocraft.tardis_refined.client.renderer.blockentity.console.GlobalConsoleRenderer;
import whocraft.tardis_refined.common.blockentity.console.GlobalConsoleBlockEntity;

@Mixin(GlobalConsoleRenderer.class)
public class GlobalConsoleRendererMixin {

    @Inject(
            method = "render(Lwhocraft/tardis_refined/common/blockentity/console/GlobalConsoleBlockEntity;FLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;II)V",
            at = @At(
                    value = "INVOKE",
                    target = "Lcom/mojang/blaze3d/vertex/PoseStack;popPose()V",
                    ordinal = 0
            ),
            remap = true
    )
    private void rwf$renderPilotAtConsole(
            GlobalConsoleBlockEntity blockEntity,
            float partialTick,
            PoseStack poseStack,
            MultiBufferSource bufferSource,
            int packedLight,
            int packedOverlay,
            CallbackInfo ci
    ) {
        ConsolePilotRenderer.render(blockEntity, partialTick, poseStack, bufferSource, packedLight);
    }
}