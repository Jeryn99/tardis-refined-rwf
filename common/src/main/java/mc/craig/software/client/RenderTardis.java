package mc.craig.software.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Vector3f;
import mc.craig.software.common.entity.TardisEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import whocraft.tardis_refined.client.model.blockentity.shell.ShellModel;
import whocraft.tardis_refined.client.model.blockentity.shell.ShellModelCollection;

import static net.minecraft.client.renderer.entity.LivingEntityRenderer.getOverlayCoords;
import static net.minecraft.client.renderer.texture.OverlayTexture.NO_OVERLAY;

public class RenderTardis extends EntityRenderer<TardisEntity> {

    public RenderTardis(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public void render(TardisEntity entity, float f, float g, PoseStack poseStack, MultiBufferSource multiBufferSource, int i) {
        super.render(entity, f, g, poseStack, multiBufferSource, i);

        poseStack.pushPose();
        poseStack.translate(0, 1.1F, 0);
        poseStack.mulPose(Vector3f.XP.rotationDegrees(180));
        poseStack.mulPose(Vector3f.XP.rotationDegrees(entity.getXRot()));

        if (!Minecraft.getInstance().player.isOnGround()) {
            poseStack.mulPose(Vector3f.YP.rotation(entity.tickCount / 10F));
            float floating = Mth.cos(entity.tickCount * 0.1F) * -0.09F;
            poseStack.translate(0, -floating, 0);
        } else {
            poseStack.mulPose(Vector3f.YP.rotation(entity.getYRot()));
        }

        ShellModel shell = ShellModelCollection.getInstance().getShellModel(entity.getShellTheme());

        shell.renderToBuffer(poseStack, multiBufferSource.getBuffer(RenderType.entityTranslucent(getTextureLocation(entity))), i, NO_OVERLAY, 1.0F, 1.0F, 1.0F, 1.0F);

        poseStack.pushPose();
        poseStack.translate(0,0.1, 0);
        if (shell.lightTexture() != null) {
            shell.renderToBuffer(poseStack, multiBufferSource.getBuffer(RenderType.entityTranslucentEmissive(shell.lightTexture())), LightTexture.pack(15, this.getSkyLightLevel(entity, entity.blockPosition())), NO_OVERLAY, 1.0F, 1.0F, 1.0F, 1.0F);
        }
        poseStack.popPose();

        poseStack.popPose();
    }

    @Override
    public ResourceLocation getTextureLocation(TardisEntity entity) {
        ShellModel shell = ShellModelCollection.getInstance().getShellModel(entity.getShellTheme());
        return shell.texture();
    }
}
