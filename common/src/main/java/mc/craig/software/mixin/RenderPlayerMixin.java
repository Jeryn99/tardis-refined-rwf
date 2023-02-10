package mc.craig.software.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import mc.craig.software.client.overlay.RenderGallifreyanOverlay;
import mc.craig.software.common.entity.TardisEntity;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PlayerRenderer.class)
public class RenderPlayerMixin {

    @Inject(at = @At("HEAD"), cancellable = true, method = "render(Lnet/minecraft/client/player/AbstractClientPlayer;FFLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;I)V")
    private void stopXpBar(AbstractClientPlayer abstractClientPlayer, float f, float g, PoseStack poseStack, MultiBufferSource multiBufferSource, int i, CallbackInfo ci) {
       if(abstractClientPlayer.getFirstPassenger() instanceof TardisEntity) {
           ci.cancel();
       }
    }
}
