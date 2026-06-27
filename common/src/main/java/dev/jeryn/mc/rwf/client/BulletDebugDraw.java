package dev.jeryn.mc.rwf.client;

import com.bulletphysics.linearmath.DebugDrawModes;
import com.bulletphysics.linearmath.IDebugDraw;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.util.Mth;

import javax.vecmath.Vector3f;

public class BulletDebugDraw extends IDebugDraw {

    private VertexConsumer consumer;
    private PoseStack.Pose pose;
    private long startTime = System.currentTimeMillis();

    private int debugMode = DebugDrawModes.DRAW_WIREFRAME
            | DebugDrawModes.DRAW_AABB
            | DebugDrawModes.DRAW_CONTACT_POINTS;

    public void begin(PoseStack poseStack, MultiBufferSource buffer) {
        this.consumer = buffer.getBuffer(RenderType.LINES);
        this.pose = poseStack.last();
    }

    public void end() {
        this.consumer = null;
        this.pose = null;
    }

    // ── Time ──────────────────────────────────────────────────────────────────

    private float time() {
        return (System.currentTimeMillis() - startTime) / 1000.0f;
    }

    // ── Color helpers ─────────────────────────────────────────────────────────

    /**
     * Animated electric cyan/blue pulse based on line midpoint and time.
     */
    private float[] wireframeColor(Vector3f from, Vector3f to) {
        float t = time();
        float mid = (from.x + from.y + from.z + to.x + to.y + to.z) * 0.05f;
        float wave = Mth.sin(t * 3.0f + mid) * 0.5f + 0.5f;

        // Cycle between electric blue and cyan
        float r = 0.0f;
        float g = 0.6f + wave * 0.4f;
        float b = 1.0f;
        float a = 0.55f + wave * 0.35f;

        return new float[]{r, g, b, a};
    }

    /**
     * Contact points pulse hot magenta/white.
     */
    private float[] contactColor() {
        float t = time();
        float wave = Mth.sin(t * 12.0f) * 0.5f + 0.5f; // fast strobe
        return new float[]{1.0f, wave * 0.2f, 1.0f - wave * 0.5f, 1.0f};
    }

    /**
     * AABB lines — warm amber/orange tint.
     */
    private float[] aabbColor(Vector3f color) {

        // If bullet passes a non-white color use it, else apply our tint
        boolean isDefault = color.x > 0.9f && color.y > 0.9f && color.z > 0.9f;
        if (!isDefault) {
            // Brighten whatever bullet gives us
            return new float[]{
                    Math.min(1f, color.x * 1.4f),
                    Math.min(1f, color.y * 1.4f),
                    Math.min(1f, color.z * 1.4f),
                    0.8f
            };
        }
        float t = time();
        float wave = Mth.sin(t * 2.0f) * 0.5f + 0.5f;
        return new float[]{1.0f, 0.55f + wave * 0.3f, 0.05f, 0.75f};
    }

    // ── Core draw ─────────────────────────────────────────────────────────────

    private void line(Vector3f from, Vector3f to, float r, float g, float b, float a) {
        if (!Minecraft.getInstance().getEntityRenderDispatcher().shouldRenderHitBoxes()) return;

        if (consumer == null) return;

        // Draw the line twice offset slightly to fake thickness/glow
        for (int pass = 0; pass < 2; pass++) {
            float alpha = pass == 0 ? a : a * 0.35f;
            float offset = pass == 0 ? 0f : 0.015f;

            consumer.vertex(pose.pose(), from.x + offset, from.y + offset, from.z)
                    .color(r, g, b, alpha)
                    .normal(pose.normal(), 0, 1, 0)
                    .endVertex();

            consumer.vertex(pose.pose(), to.x + offset, to.y + offset, to.z)
                    .color(r, g, b, alpha)
                    .normal(pose.normal(), 0, 1, 0)
                    .endVertex();
        }
    }

    @Override
    public void drawLine(Vector3f from, Vector3f to, Vector3f color) {
        if (!Minecraft.getInstance().getEntityRenderDispatcher().shouldRenderHitBoxes()) return;

        float[] c;

        // Bullet uses specific colors to signal line type:
        // red = X axis / contact, green = Y axis, blue = Z axis
        // white/grey = generic wireframe
        boolean isRed = color.x > 0.8f && color.y < 0.3f && color.z < 0.3f;
        boolean isGreen = color.y > 0.8f && color.x < 0.3f && color.z < 0.3f;
        boolean isBlue = color.z > 0.8f && color.x < 0.3f && color.y < 0.3f;

        if (isRed) {
            // Remap X axis to hot pink
            float wave = Mth.sin(time() * 5f) * 0.5f + 0.5f;
            c = new float[]{1.0f, 0.1f + wave * 0.3f, 0.4f, 0.9f};
        } else if (isGreen) {
            // Remap Y axis to electric lime
            c = new float[]{0.3f, 1.0f, 0.2f, 0.85f};
        } else if (isBlue) {
            // Remap Z axis to violet
            c = new float[]{0.6f, 0.1f, 1.0f, 0.85f};
        } else {
            // Generic wireframe — animated cyan
            c = wireframeColor(from, to);
        }

        line(from, to, c[0], c[1], c[2], c[3]);
    }

    @Override
    public void drawContactPoint(Vector3f pointOnB, Vector3f normalOnB,
                                 float distance, int lifeTime, Vector3f color) {
        // Draw the contact normal
        Vector3f to = new Vector3f(normalOnB);
        to.scale(Math.max(distance, 0.3f)); // minimum visible length
        to.add(pointOnB);

        float[] c = contactColor();
        line(pointOnB, to, c[0], c[1], c[2], c[3]);

        // Draw a small cross at the contact point
        float s = 0.12f;
        line(
                new Vector3f(pointOnB.x - s, pointOnB.y, pointOnB.z),
                new Vector3f(pointOnB.x + s, pointOnB.y, pointOnB.z),
                c[0], c[1], c[2], 1.0f
        );
        line(
                new Vector3f(pointOnB.x, pointOnB.y - s, pointOnB.z),
                new Vector3f(pointOnB.x, pointOnB.y + s, pointOnB.z),
                c[0], c[1], c[2], 1.0f
        );
        line(
                new Vector3f(pointOnB.x, pointOnB.y, pointOnB.z - s),
                new Vector3f(pointOnB.x, pointOnB.y, pointOnB.z + s),
                c[0], c[1], c[2], 1.0f
        );
    }

    @Override
    public void reportErrorWarning(String warningString) {
        System.out.println("[Bullet] " + warningString);
    }

    @Override
    public void draw3dText(Vector3f location, String textString) {
    }

    @Override
    public int getDebugMode() {
        return debugMode;
    }

    @Override
    public void setDebugMode(int debugMode) {
        this.debugMode = debugMode;
    }
}