package dev.jeryn.mc.rwf.client;

import com.bulletphysics.linearmath.Transform;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import dev.jeryn.mc.rwf.common.TardisPhysics;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;
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
import java.util.*;

import static net.minecraft.client.renderer.texture.OverlayTexture.NO_OVERLAY;
import static whocraft.tardis_refined.client.screen.main.MonitorOS.MonitorOSExtension.generateDummyGlobalShell;

public class RenderTardis {

    private static final Map<ResourceLocation, Float> SEAT_CACHE = new HashMap<>();

    private static final Map<UUID, float[]> PHYSICS_MATRIX = new HashMap<>();

    private static final Map<UUID, ResourceKey<Level>> LAST_DIMENSION = new HashMap<>();
    private static final Map<UUID, Integer> TRANSITION_START_TICK = new HashMap<>();
    private static final int TRANSITION_DURATION_TICKS = 20;

    private static final Map<UUID, Integer> LAST_PARTICLE_TICK = new HashMap<>();

    private RenderTardis() {
    }

    private static float dimensionTransitionAlpha(AbstractClientPlayer player, float partialTick) {
        UUID uuid = player.getUUID();
        ResourceKey<Level> currentDim = player.level().dimension();
        ResourceKey<Level> lastDim = LAST_DIMENSION.put(uuid, currentDim);

        if (lastDim != null && !lastDim.equals(currentDim)) {
            TRANSITION_START_TICK.put(uuid, player.tickCount);
        }

        float dimensionAlpha = 1F;
        Integer startTick = TRANSITION_START_TICK.get(uuid);
        if (startTick != null) {
            float elapsed = (player.tickCount - startTick) + partialTick;
            if (elapsed >= TRANSITION_DURATION_TICKS) {
                TRANSITION_START_TICK.remove(uuid);
            } else {
                float half = TRANSITION_DURATION_TICKS / 2F;
                dimensionAlpha = Mth.clamp(
                        elapsed < half ? 1F - (elapsed / half) : (elapsed - half) / half,
                        0F, 1F);
            }
        }

        return dimensionAlpha;
    }

    public static void setPhysicsMatrix(UUID pilot, float[] matrix) {
        PHYSICS_MATRIX.put(pilot, matrix);
    }

    public static float[] getPhysicsMatrix(UUID pilot) {
        return PHYSICS_MATRIX.get(pilot);
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

    private static float[] getLocalMatrix(float partialTick) {
        Transform t = new Transform();
        TardisPhysics.tardis_rigid_body.getWorldTransform(t);
        t.setRotation(TardisPhysics.getInterpolatedRotation(partialTick));
        float[] m = new float[16];
        t.getOpenGLMatrix(m);
        return m;
    }

    private static void renderParticles(AbstractClientPlayer player) {
        // This is called from a per-frame render hook, not a tick hook - cap
        // it to once per game tick so a high framerate doesn't multiply the
        // particle count for the same visual.
        Integer lastTick = LAST_PARTICLE_TICK.put(player.getUUID(), player.tickCount);
        if (lastTick != null && lastTick == player.tickCount) return;

        BlockPos start = player.blockPosition();
        BlockState ground = null;
        double surfaceY = 0;

        BlockPos.MutableBlockPos cursor = start.mutable();

        for (int i = 0; i < 3; i++) {
            cursor.move(Direction.DOWN);
            BlockState state = player.level().getBlockState(cursor);

            if (!state.isAir() && !state.canBeReplaced()) {
                ground = state;
                surfaceY = cursor.getY() + 1.0D;
                break;
            }
        }

        if (ground != null) {

            double gap = player.getY() - surfaceY;
            if (gap < 0.2D || gap > 2.5D) return;

            double cx = player.getX();
            double cy = surfaceY + 0.1D;
            double cz = player.getZ();

            double radius = 1.5;

            for (int i = 0; i < 20; i++) {

                double angle = (i / 20.0) * Math.PI * 2.0;

                double ox = Math.cos(angle) * radius;
                double oz = Math.sin(angle) * radius;

                double vx = ox * 0.03;
                double vz = oz * 0.03;

                double vy = 0.02;

                player.level().addParticle(
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

    private static ShellPattern getPattern(ClientFlightData data) {
        return ShellPatterns.getPatternOrDefault(data.shellTheme(), data.shellPattern());
    }

    private static ShellModel getModel(ClientFlightData data, ShellPattern pattern) {
        ShellEntry entry = ShellModelCollection.getInstance().getShellEntry(data.shellTheme());
        return entry == null ? null : entry.getShellModel(pattern);
    }

    private static void renderDebugText(AbstractClientPlayer player, ClientFlightData data, boolean isFreefalling,
                                        float[] activeMatrix, PoseStack poseStack, MultiBufferSource buffer, int packedLight) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.getEntityRenderDispatcher().shouldRenderHitBoxes()) {

            List<String> lines = new ArrayList<>();
            lines.add(String.format("§eRECOVERING§f " + data.isRecovering()));

            Vec3 pos = player.position();
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

            if (activeMatrix != null && TardisPhysics.tardis_rigid_body == null) {
                float[] m = activeMatrix;
                lines.add(String.format("§eMATRIX(0-2) §f%.2f  %.2f  %.2f", m[0], m[1], m[2]));
                lines.add(String.format("§eMATRIX(4-6) §f%.2f  %.2f  %.2f", m[4], m[5], m[6]));
                lines.add(String.format("§eMATRIX(8-10)§f%.2f  %.2f  %.2f", m[8], m[9], m[10]));
            }

            lines.add(String.format("§eorigin §f%s  %.1f %.1f %.1f",
                    data.originDimension().location().getPath(),
                    data.originPos().x, data.originPos().y, data.originPos().z));

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

                font.drawInBatch(text, x, y, 0xFFFFFF, false, matrix, buffer, Font.DisplayMode.NORMAL, 0, packedLight);
            }

            poseStack.popPose();
        }
    }

    public static void renderShellForPlayer(AbstractClientPlayer player, float partialTick, PoseStack poseStack,
                                            MultiBufferSource multiBufferSource, int packedLight) {

        ClientFlightData data = ClientFlightTracker.getForPlayer(player.getUUID()).orElse(null);
        if (data == null) return;

        ShellPattern pattern = getPattern(data);
        ShellModel shell = getModel(data, pattern);
        if (shell == null) return;

        generateDummyGlobalShell();
        GlobalShellBlockEntity dummy = ShellSelectionScreen.GLOBALSHELL_BLOCKENTITY;
        dummy.setTardisId(data.tardisDimension());

        poseStack.pushPose();
        poseStack.translate(0, .5, 0);

        float age = player.tickCount + partialTick;

        seatFor(data.shellTheme(), shell);

        boolean isFreefalling = data.isFreefalling();

        if (!player.onGround() && !isFreefalling) {
            renderParticles(player);
        }

        float[] activeMatrix = (TardisPhysics.tardis_rigid_body != null)
                ? getLocalMatrix(partialTick)
                : getPhysicsMatrix(player.getUUID());

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

            if (player.onGround()) {
                poseStack.translate(0, 1, 0);

            } else {
                poseStack.translate(0, 0.5, 0);

                float angle = age * 4 + 13 * player.swingTime;

                Vec3 motion = new Vec3(player.getDeltaMovement().x, 0, player.getDeltaMovement().z);

                poseStack.mulPose(Axis.ZP.rotationDegrees((float) (9 * ((-motion.x * 2) * 1))));
                poseStack.mulPose(Axis.XP.rotationDegrees((float) (9 * ((motion.z * 2) * 1))));
                poseStack.mulPose(Axis.YP.rotationDegrees(angle));
                poseStack.translate(Math.sin(angle * 0.05) * motion.lengthSqr() * 0.05, 0.5, Math.cos(angle * 0.07) * motion.lengthSqr() * 0.05);
            }
        }

        poseStack.mulPose(Axis.XP.rotationDegrees(180));

        ResourceLocation texture = shell.getShellTexture(pattern, false);

        int block = 15;
        int sky = Math.max(8, player.level().getBrightness(LightLayer.SKY, player.blockPosition()));
        int shellLight = LightTexture.pack(block, sky);

        shell.setIgnoreAnmationAlpha(true);
        float wave = data.isRecovering() ? (float) ((Math.sin(System.nanoTime() * 0.000000005) + 1.0) * 0.5) : 1F;
        float transitionAlpha = dimensionTransitionAlpha(player, partialTick);
        shell.renderShell(dummy, data.doorOpen(), true, poseStack,
                multiBufferSource.getBuffer(RenderType.entityTranslucent(texture)),
                shellLight, NO_OVERLAY, 1.0F, 1.0F, 1.0F, wave * transitionAlpha);

        shell.setIgnoreAnmationAlpha(false);

        ShellTheme theme = ShellTheme.getShellTheme(data.shellTheme());

        ResourceLocation emissive = shell.getShellTexture(pattern, true);

        VertexConsumer buffer = multiBufferSource.getBuffer(
                RenderType.entityTranslucent(emissive)
        );

        if (theme.producesLight()) {
            shell.renderShell(
                    dummy,
                    data.doorOpen(),
                    false,
                    poseStack,
                    buffer,
                    shellLight,
                    NO_OVERLAY,
                    1.0F, 1.0F, 1.0F, transitionAlpha
            );
        }
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        poseStack.popPose();

        renderDebugText(player, data, isFreefalling, activeMatrix, poseStack, multiBufferSource, packedLight);
    }
}
