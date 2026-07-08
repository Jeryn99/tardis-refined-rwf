package dev.jeryn.mc.rwf.client;

import com.bulletphysics.linearmath.Transform;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import dev.jeryn.mc.rwf.common.TardisPhysics;
import dev.jeryn.mc.rwf.common.entity.TardisEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix3f;
import org.joml.Matrix4f;
import whocraft.tardis_refined.client.model.blockentity.shell.ShellEntry;
import whocraft.tardis_refined.client.model.blockentity.shell.ShellModel;
import whocraft.tardis_refined.client.model.blockentity.shell.ShellModelCollection;
import whocraft.tardis_refined.client.screen.screens.ShellSelectionScreen;
import whocraft.tardis_refined.common.blockentity.shell.GlobalShellBlockEntity;
import whocraft.tardis_refined.common.tardis.themes.ShellTheme;
import whocraft.tardis_refined.patterns.ShellPattern;
import whocraft.tardis_refined.patterns.ShellPatterns;

import javax.vecmath.Quat4f;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static net.minecraft.client.renderer.texture.OverlayTexture.NO_OVERLAY;
import static whocraft.tardis_refined.client.screen.main.MonitorOS.MonitorOSExtension.generateDummyGlobalShell;

public class RenderTardis extends EntityRenderer<TardisEntity> {

    private static final Map<ResourceLocation, Float> SEAT_CACHE = new HashMap<>();

    public RenderTardis(EntityRendererProvider.Context context) {
        super(context);
    }

    private static float seatFor(ResourceLocation themeId, ShellModel shell) {
        Float cached = SEAT_CACHE.get(themeId);
        if (cached != null) return cached;

        float[] maxY = {Float.NEGATIVE_INFINITY};
        shell.root().visit(new PoseStack(), (pose, path, index, cube) -> {
            Matrix4f m = pose.pose();
            float[] xs = {cube.minX / 16F, cube.maxX / 16F};
            float[] ys = {cube.minY / 16F, cube.maxY / 16F};
            float[] zs = {cube.minZ / 16F, cube.maxZ / 16F};
            for (float cx : xs)
                for (float cy : ys)
                    for (float cz : zs) {
                        org.joml.Vector3f v = new org.joml.Vector3f(cx, cy, cz).mulPosition(m);
                        if (v.y > maxY[0]) maxY[0] = v.y;
                    }
        });

        float seat = maxY[0] == Float.NEGATIVE_INFINITY ? 1.5F : maxY[0];
        SEAT_CACHE.put(themeId, seat);
        return seat;
    }


    private static float[] getLocalMatrix() {
        Transform t = new Transform();
        TardisPhysics.tardis_rigid_body.getWorldTransform(t);
        float[] m = new float[16];
        t.getOpenGLMatrix(m);
        return m;
    }

    private static void renderParticles(TardisEntity entity) {
        Entity controllingPlayer = entity.getVehicle();
        if(controllingPlayer == null) return;
        BlockPos start = controllingPlayer.blockPosition();
        BlockState ground = null;
        double surfaceY = 0;

        BlockPos.MutableBlockPos cursor = start.mutable();

        for (int i = 0; i < 3; i++) {
            cursor.move(Direction.DOWN);
            BlockState state = entity.level().getBlockState(cursor);

            if (!state.isAir() && !state.canBeReplaced()) {
                ground = state;
                surfaceY = cursor.getY() + 1.0D;
                break;
            }
        }

        if (ground != null) {

            double gap = controllingPlayer.getY() - surfaceY;
            if (gap < 0.2D || gap > 2.5D) return;

            double cx = entity.getX();
            double cy = surfaceY + 0.1D;
            double cz = entity.getZ();

            double radius = 1.5;

            for (int i = 0; i < 20; i++) {

                double angle = (i / 20.0) * Math.PI * 2.0;

                double ox = Math.cos(angle) * radius;
                double oz = Math.sin(angle) * radius;

                double vx = ox * 0.03;
                double vz = oz * 0.03;

                double vy = 0.02;

                entity.level().addParticle(
                        new net.minecraft.core.particles.BlockParticleOption(
                                net.minecraft.core.particles.ParticleTypes.BLOCK, ground
                        ),
                        cx + ox,
                        cy,
                        cz + oz,
                        vx, vy, vz
                );
            }
        }
    }

    private ShellPattern getPattern(TardisEntity entity) {
        return ShellPatterns.getPatternOrDefault(entity.getShellThemeId(), entity.getShellPatternId());
    }

    private ShellModel getModel(TardisEntity entity, ShellPattern pattern) {
        ShellEntry entry = ShellModelCollection.getInstance().getShellEntry(entity.getShellThemeId());
        return entry == null ? null : entry.getShellModel(pattern);
    }

    /* ---------------- SHIELD VFX ---------------- */

    private static final float SHIELD_SCALE = 1.08F;

    private void renderShield(TardisEntity entity, ShellPattern pattern, ShellModel shell,
                              GlobalShellBlockEntity dummy, float partialTick,
                              PoseStack poseStack, MultiBufferSource multiBufferSource, int packedLight) {

        float maxShield = entity.getMaxShield();
        if (maxShield <= 0F) return;

        float shieldPct = entity.getShield() / maxShield;
        if (shieldPct <= 0F) return;

        float age = entity.tickCount + partialTick;

        // layered sine flicker so it doesn't read as a simple pulse
        float flicker = 0.65F
                + 0.20F * (float) Math.sin(age * 0.5F)
                + 0.15F * (float) Math.sin(age * 1.7F + 1.3F);

        flicker = Math.max(0.0F, Math.min(1.0F, flicker));

        // shield gets noisier/dimmer as it depletes, brighter and steadier near full charge
        float alpha = (0.18F + 0.32F * flicker) * shieldPct;

        if (alpha <= 0.01F) return;

        float r = 0.15F;
        float g = 0.65F;
        float b = 1.0F;

        poseStack.pushPose();
        poseStack.scale(SHIELD_SCALE, SHIELD_SCALE, SHIELD_SCALE);

        ResourceLocation emissive = shell.getShellTexture(pattern, true);
        ResourceLocation base = shell.getShellTexture(pattern, false);
        ResourceLocation shieldTexture = emissive != null ? emissive : base;

        shell.setIgnoreAnmationAlpha(true);

        shell.renderShell(dummy, entity.isOpen(), false, poseStack,
                multiBufferSource.getBuffer(RenderType.entityTranslucentEmissive(base)),
                LightTexture.pack(15, 15),
                NO_OVERLAY, r, g, b, alpha);
        shell.setIgnoreAnmationAlpha(false);
        poseStack.popPose();
    }

    private void renderDebugText(TardisEntity entity, boolean isFreefalling, float[] activeMatrix,
                                 PoseStack poseStack, MultiBufferSource buffer, int packedLight) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.getEntityRenderDispatcher().shouldRenderHitBoxes()) {

            List<String> lines = new ArrayList<>();
            lines.add(String.format("§eRECOVERY§f " + entity.getRecoveryTicks()));

            Vec3 pos = entity.position();
            lines.add(String.format("§ePOS §f%.2f  %.2f  %.2f", pos.x, pos.y, pos.z));

            lines.add(String.format("§eFREEFALL §f%s  §eHEAT §f%.3f",
                    isFreefalling ? "§aON" : "§cOFF", TardisPhysics.heat));

            if (TardisPhysics.tardis_rigid_body != null) {
                javax.vecmath.Vector3f linVel = TardisPhysics.tardis_rigid_body.getLinearVelocity(new javax.vecmath.Vector3f());
                javax.vecmath.Vector3f angVel = TardisPhysics.tardis_rigid_body.getAngularVelocity(new javax.vecmath.Vector3f());

                float speed = (float) Math.sqrt(linVel.x * linVel.x + linVel.y * linVel.y + linVel.z * linVel.z);

                lines.add(String.format("§eLIN VEL §f%.2f  %.2f  %.2f  §e|v| §f%.2f",
                        linVel.x, linVel.y, linVel.z, speed));
                lines.add(String.format("§eANG VEL §f%.2f  %.2f  %.2f",
                        angVel.x, angVel.y, angVel.z));

                Transform wt = new Transform();
                TardisPhysics.tardis_rigid_body.getWorldTransform(wt);
                Quat4f q = new Quat4f();
                wt.getRotation(q);
                javax.vecmath.Vector3f euler = TardisPhysics.quatToEuler(q);
                lines.add(String.format("§eROT (deg) §fP:%.1f  Y:%.1f  R:%.1f",
                        Math.toDegrees(euler.x), Math.toDegrees(euler.y), Math.toDegrees(euler.z)));

                float linDamp = TardisPhysics.tardis_rigid_body.getLinearDamping();
                float angDamp = TardisPhysics.tardis_rigid_body.getAngularDamping();
                lines.add(String.format("§eDAMP §flin:%.3f  ang:%.3f", linDamp, angDamp));

            } else {
                lines.add("§eRIGID BODY §cnull §7(non-pilot or inactive)");
            }

            if (entity.physicsMatrix != null && TardisPhysics.tardis_rigid_body == null) {
                float[] m = entity.physicsMatrix;
                lines.add(String.format("§eMATRIX(0-2) §f%.2f  %.2f  %.2f", m[0], m[1], m[2]));
                lines.add(String.format("§eMATRIX(4-6) §f%.2f  %.2f  %.2f", m[4], m[5], m[6]));
                lines.add(String.format("§eMATRIX(8-10)§f%.2f  %.2f  %.2f", m[8], m[9], m[10]));
            }

            ClientFlightTracker.get(entity.getTardisDimension()).ifPresent(data -> {
                lines.add(String.format("§eorigin §f%s  %.1f %.1f %.1f",
                        data.originDimension().location().getPath(),
                        data.originPos().x, data.originPos().y, data.originPos().z));
            });


            float lineHeight = 0.25F;
            float totalHeight = 1.2F + lines.size() * lineHeight;

            poseStack.pushPose();
            poseStack.translate(0, totalHeight, 0);
            poseStack.mulPose(mc.getEntityRenderDispatcher().cameraOrientation());
            poseStack.scale(-0.025F, -0.025F, 0.025F);

            var font = mc.font;
            var matrix = poseStack.last().pose();

            for (int i = 0; i < lines.size(); i++) {
                Component text = Component.literal(lines.get(i));
                int w = font.width(text);
                float x = -w / 2.0F;
                float y = i * -(lineHeight / 0.025F);

                // Background
                buffer.getBuffer(RenderType.textBackgroundSeeThrough())
                        .vertex(matrix, x - 1, y - 1, 0)
                        .color(0, 0, 0, 80)
                        .uv2(packedLight)
                        .endVertex();
                buffer.getBuffer(RenderType.textBackgroundSeeThrough())
                        .vertex(matrix, x + w + 1, y - 1, 0)
                        .color(0, 0, 0, 80)
                        .uv2(packedLight)
                        .endVertex();
                buffer.getBuffer(RenderType.textBackgroundSeeThrough())
                        .vertex(matrix, x + w + 1, y + 9, 0)
                        .color(0, 0, 0, 80)
                        .uv2(packedLight)
                        .endVertex();
                buffer.getBuffer(RenderType.textBackgroundSeeThrough())
                        .vertex(matrix, x - 1, y + 9, 0)
                        .color(0, 0, 0, 80)
                        .uv2(packedLight)
                        .endVertex();

                entity.setCustomName(text);

                font.drawInBatch(text, x, y, 0xFFFFFF, false, matrix, buffer, Font.DisplayMode.NORMAL, 0, packedLight);
            }

            poseStack.popPose();
        }
    }

    @Override
    public void render(TardisEntity entity, float yaw, float partialTick, PoseStack poseStack,
                       MultiBufferSource multiBufferSource, int packedLight) {
        super.render(entity, yaw, partialTick, poseStack, multiBufferSource, packedLight);

        ShellPattern pattern = getPattern(entity);
        ShellModel shell = getModel(entity, pattern);
        if (shell == null) return;

        generateDummyGlobalShell();
        GlobalShellBlockEntity dummy = ShellSelectionScreen.GLOBALSHELL_BLOCKENTITY;
        dummy.setTardisId(entity.getTardisDimension());

        Entity controllingPlayer = entity.getVehicle();

        if (controllingPlayer == null) return;


        poseStack.pushPose();
        poseStack.translate(0, .5, 0);


        float age = entity.tickCount + partialTick;

        float seat = seatFor(entity.getShellThemeId(), shell);

        boolean isFreefalling = ClientFlightTracker.get(entity.getTardisDimension())
                .map(ClientFlightData::isFreefalling)
                .orElse(false);

        if (!entity.onGround() && !isFreefalling) {
            renderParticles(entity);
        }

        float[] activeMatrix = (TardisPhysics.tardis_rigid_body != null)
                ? getLocalMatrix()
                : entity.physicsMatrix;

        if (isFreefalling && activeMatrix != null) {
            Matrix4f rotOnly = new Matrix4f(
                    activeMatrix[0], activeMatrix[1], activeMatrix[2], 0,
                    activeMatrix[4], activeMatrix[5], activeMatrix[6], 0,
                    activeMatrix[8], activeMatrix[9], activeMatrix[10], 0,
                    0, 0, 0, 1
            );
            poseStack.last().pose().mul(rotOnly);
            poseStack.last().normal().mul(new Matrix3f(rotOnly));

        } else {

            if (controllingPlayer.onGround()) {
                poseStack.translate(0, 1, 0);

            } else {
                poseStack.translate(0, 0.5, 0);

                Vec3 playerPos = controllingPlayer.position();

                float speed = 3.0F;
                float radius = 0.5F;

                float angle = (entity.tickCount + partialTick) * speed * 0.05F;

                double offsetX = Math.cos(angle) * radius;
                double offsetZ = Math.sin(angle) * radius;

                float drift = (float)Math.sin(age * 0.03F) * 2F;
                poseStack.mulPose(Axis.ZP.rotationDegrees(drift));

                poseStack.translate(
                        playerPos.x + offsetX - entity.getX(),
                        playerPos.y + 0.2 - entity.getY(),
                        playerPos.z + offsetZ - entity.getZ()
                );

                float yaw2 = (float) (-Math.atan2(offsetX, offsetZ) * (180F / Math.PI));
                poseStack.mulPose(Axis.YP.rotationDegrees(yaw2));

                poseStack.mulPose(Axis.ZP.rotationDegrees((float) Math.sin(angle) * 6F));
            }
        }

        poseStack.mulPose(Axis.XP.rotationDegrees(180));

        ResourceLocation texture = shell.getShellTexture(pattern, false);

        shell.setIgnoreAnmationAlpha(true);
        float wave = entity.getRecoveryTicks() == 0 ? 1 :  (float) ((Math.sin(System.nanoTime() * 0.000000005) + 1.0) * 0.5);
        shell.renderShell(dummy, entity.isOpen(), true, poseStack,
                multiBufferSource.getBuffer(RenderType.entityTranslucent(texture)),
                packedLight, NO_OVERLAY, 1.0F, 1.0F, 1.0F, wave);

        shell.setIgnoreAnmationAlpha(false);

        ShellTheme theme = entity.getShellTheme();

        ResourceLocation emissive = shell.getShellTexture(pattern, true);

        VertexConsumer buffer = multiBufferSource.getBuffer(
                RenderType.entityTranslucent(emissive)
        );

// clamp lighting so it stays bright but still shaded
        int block = (int) Math.max(8, entity.getLightLevelDependentMagicValue());
        int sky = Math.max(8, entity.level().getBrightness(LightLayer.SKY, entity.blockPosition()));
        int light = LightTexture.pack(block, sky);

        shell.renderShell(
                dummy,
                entity.isOpen(),
                false,
                poseStack,
                buffer,
                light,
                NO_OVERLAY,
                1.0F, 1.0F, 1.0F, 1.0F
        );

  //      renderShield(entity, pattern, shell, dummy, partialTick, poseStack, multiBufferSource, packedLight);

        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        poseStack.popPose();

        renderDebugText(entity, isFreefalling, activeMatrix, poseStack, multiBufferSource, packedLight);
    }

    @Override
    protected int getBlockLightLevel(TardisEntity tardis, BlockPos blockPos) {
        return 15;
    }

    @Override
    public ResourceLocation getTextureLocation(TardisEntity entity) {
        ShellPattern pattern = getPattern(entity);
        ShellModel shell = getModel(entity, pattern);
        return shell == null
                ? ShellPatterns.DEFAULT.shellTexture().texture()
                : shell.getShellTexture(pattern, false);
    }
}