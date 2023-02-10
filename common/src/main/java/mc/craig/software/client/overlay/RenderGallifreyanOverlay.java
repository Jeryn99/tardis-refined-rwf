package mc.craig.software.client.overlay;

import com.mojang.blaze3d.platform.Window;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Vector3f;
import mc.craig.software.common.entity.TardisEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.resources.ResourceLocation;

import static net.minecraft.client.gui.GuiComponent.blit;

public class RenderGallifreyanOverlay {

    public static final ResourceLocation L_GOLD_SYM_01 = new ResourceLocation("tardis_refined:textures/particle/gold/l_gold_sym_01.png");
    public static final ResourceLocation L_GOLD_SYM_02 = new ResourceLocation("tardis_refined:textures/particle/gold/l_gold_sym_02.png");
    public static final ResourceLocation L_GOLD_SYM_03 = new ResourceLocation("tardis_refined:textures/particle/gold/l_gold_sym_03.png");
    public static final ResourceLocation L_GOLD_SYM_04 = new ResourceLocation("tardis_refined:textures/particle/gold/l_gold_sym_04.png");
    public static final ResourceLocation L_GOLD_SYM_05 = new ResourceLocation("tardis_refined:textures/particle/gold/l_gold_sym_05.png");
    public static final ResourceLocation L_GOLD_SYM_06 = new ResourceLocation("tardis_refined:textures/particle/gold/l_gold_sym_06.png");
    public static final ResourceLocation L_GOLD_SYM_07 = new ResourceLocation("tardis_refined:textures/particle/gold/l_gold_sym_07.png");
    public static final ResourceLocation L_GOLD_SYM_08 = new ResourceLocation("tardis_refined:textures/particle/gold/l_gold_sym_08.png");
    public static final ResourceLocation L_GOLD_SYM_09 = new ResourceLocation("tardis_refined:textures/particle/gold/l_gold_sym_09.png");
    public static final ResourceLocation L_GOLD_SYM_10 = new ResourceLocation("tardis_refined:textures/particle/gold/l_gold_sym_10.png");

    public static final ResourceLocation[] GOLD_SYMBOLS = new ResourceLocation[]{
            L_GOLD_SYM_01,
            L_GOLD_SYM_02,
            L_GOLD_SYM_03,
            L_GOLD_SYM_04,
            L_GOLD_SYM_05,
            L_GOLD_SYM_06,
            L_GOLD_SYM_07,
            L_GOLD_SYM_08,
            L_GOLD_SYM_09,
            L_GOLD_SYM_10
    };

    public static ResourceLocation LEFT = getRandomGoldSymbol(), RIGHT = getRandomGoldSymbol();

    public static ResourceLocation getRandomGoldSymbol() {
        int randomIndex = (int) (Math.random() * GOLD_SYMBOLS.length);
        return GOLD_SYMBOLS[randomIndex];
    }

    public static void renderAll(PoseStack poseStack) {
        if (!(Minecraft.getInstance().player.getFirstPassenger() instanceof TardisEntity)) return;

        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);

        Window window = Minecraft.getInstance().getWindow();
        int winWid = window.getGuiScaledWidth() / 2;
        int winHeight = window.getGuiScaledHeight();

        if (Minecraft.getInstance().level.getGameTime() % 200 == 0) {
            LEFT = getRandomGoldSymbol();
            RIGHT = getRandomGoldSymbol();
        }

        // Symbol
        poseStack.pushPose();
        RenderSystem.setShaderTexture(0, LEFT);
        poseStack.translate(winWid - 240, winHeight - 10, 0);
        poseStack.scale(8, 8, 8);
        poseStack.mulPose(Vector3f.ZN.rotationDegrees(Minecraft.getInstance().player.tickCount));
        blit(poseStack, (int) -7.5D, (int) -7.5D, 0, 0, 15, 15, 15, 15);
        poseStack.popPose();

        // Symbol
        poseStack.pushPose();
        RenderSystem.setShaderTexture(0, RIGHT);
        poseStack.translate(winWid + 220, winHeight, 0);
        poseStack.scale(8, 8, 8);
        poseStack.mulPose(Vector3f.ZP.rotationDegrees(Minecraft.getInstance().player.tickCount));
        blit(poseStack, (int) -7.5D, (int) -7.5D, 0, 0, 15, 15, 15, 15);
        poseStack.popPose();

    }

}
