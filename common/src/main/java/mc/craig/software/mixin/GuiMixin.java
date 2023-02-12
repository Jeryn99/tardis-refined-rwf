package mc.craig.software.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import mc.craig.software.client.overlay.RenderGallifreyanOverlay;
import net.minecraft.client.gui.Gui;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Gui.class)
public class GuiMixin {

    @Inject(at = @At("HEAD"), cancellable = true, method = "renderHotbar(FLcom/mojang/blaze3d/vertex/PoseStack;)V")
    private void stopXpBar(float f, PoseStack poseStack, CallbackInfo ci) {
        RenderGallifreyanOverlay.renderAll(poseStack);
    }

}
