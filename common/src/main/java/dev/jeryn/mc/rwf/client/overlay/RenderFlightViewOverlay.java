package dev.jeryn.mc.rwf.client.overlay;

import com.mojang.blaze3d.platform.Window;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import dev.jeryn.mc.rwf.common.TardisPhysics;
import dev.jeryn.mc.rwf.common.entity.TardisEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.phys.Vec3;
import whocraft.tardis_refined.client.TardisClientData;

public class RenderFlightViewOverlay {

    public static ResourceLocation LEFT, RIGHT;

    private static float danger = 0.0f;

    public static void renderAll(GuiGraphics gui) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || !(mc.player.getFirstPassenger() instanceof TardisEntity))
            return;

        LocalPlayer player = mc.player;

        if (!(player.getFirstPassenger() instanceof TardisEntity tardis)) return;

        TardisClientData tardisClientData = TardisClientData.getInstance(tardis.getTardisDimension());

        if (tardisClientData.isFlying()) return;

        updateDanger(player);

        Window win = mc.getWindow();
        int w = win.getGuiScaledWidth();
        int h = win.getGuiScaledHeight();
        int cx = w / 2;
        Font font = mc.font;

        renderScanlines(gui, w, h);
        renderCorners(gui, w, h);

        renderCoordPanel(gui, font);
        renderPilot(gui, font, w, h);
        renderFuel(gui, font, w, h);
        renderHeat(gui, font, w, h);
        renderHeading(gui, font, player, w);

        renderSymbol(gui, LEFT, cx - 240, h - 40, player.tickCount);
        renderSymbol(gui, RIGHT, cx + 220, h - 40, -player.tickCount);

        renderVortex(gui, w / 2, h);

        renderImpactFlash(gui, w, h);
    }

    static void updateDanger(LocalPlayer player) {
        float target = 0.0f;

        float hp = player.getHealth() / player.getMaxHealth();
        if (hp < 0.5f) target += (1.0f - hp);

        if (player.isOnFire()) target += 0.7f;

        if (player.level().getFluidState(player.blockPosition()).is(FluidTags.LAVA)) {
            target += 1.2f;
        }

        danger += (target - danger) * 0.08f;
    }


    static void renderSymbol(GuiGraphics gui, ResourceLocation tex, int x, int y, float rot) {
        if (tex == null) return;
        PoseStack p = gui.pose();
        p.pushPose();
        p.translate(x, y, 0);
        p.scale(8, 8, 8);
        p.mulPose(Axis.ZP.rotationDegrees(rot));
        gui.blit(tex, -7, -7, 0, 0, 15, 15, 15, 15);
        p.popPose();
    }

    static void renderCoordPanel(GuiGraphics gui, Font f) {
        LocalPlayer player = Minecraft.getInstance().player;
        if (player == null) return;

        int x = 20;
        int y = 20;

        int c = danger > 0.5f ? 0xFF6666 : 0x88FFCC;

        gui.drawString(f, "X " + Math.round(player.getX()), x, y, c);
        gui.drawString(f, "Y " + Math.round(player.getY()), x, y + 10, c);
        gui.drawString(f, "Z " + Math.round(player.getZ()), x, y + 20, c);

        Vec3 vel = player.getDeltaMovement();
        double speed = vel.length() * 20.0D; // blocks/tick -> approx blocks/sec

        gui.drawString(f, "SPD " + String.format("%.1f", speed) + " b/s", x, y + 30, c);
    }

    static void renderPilot(GuiGraphics gui, Font f, int w, int h) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) return;

        int x = 20;
        int y = h - 36;

        net.minecraft.client.gui.components.PlayerFaceRenderer.draw(
                gui,
                mc.player.getSkinTextureLocation(),
                x,
                y,
                16
        );

        gui.drawString(f, mc.player.getName().getString(), x + 20, y + 4, 0xFFFFFF);
    }

    static void renderFuel(GuiGraphics gui, Font f, int w, int h) {
        int x = w - 80;
        int y = h - 30;

        int color = danger > 0.5f ? 0xFF5555 : 0x66FFCC;

        LocalPlayer player = Minecraft.getInstance().player;
        if (player.getFirstPassenger() instanceof TardisEntity tardis) {
            TardisClientData tardisClientData = TardisClientData.getInstance(tardis.getTardisDimension());

            int fuel = (int) tardisClientData.getFuel();
            int percent = (int) ((fuel / 1000.0f) * 100.0f);

            boolean low = percent <= 20;
            boolean blink = (player.tickCount / 8) % 2 == 0;

            int fuelColor = (low && blink) ? 0xFF3333 : color;

            gui.drawString(f, "FUEL: " + percent + "%", x, y, fuelColor);

            if (low && blink) {
                gui.drawString(f, "LOW FUEL", x, y - 10, 0xFF3333);
            }
        }
    }

    /* ---------------- SYSTEMS PANEL (heat) ---------------- */

    static void renderHeat(GuiGraphics gui, Font f, int w, int h) {
        int barW = 70;
        int barH = 4;
        int x = w - 20 - barW;
        int y = 20;

        float heat = TardisPhysics.heat;
        int heatRgb = heat > 0.7F ? 0xFF4433 : (heat > 0.35F ? 0xFFAA33 : 0x88FFCC);

        gui.drawString(f, "ENGINE HEAT", x, y, heatRgb);
        gui.fill(x, y + 10, x + barW, y + 10 + barH, 0x55202020);
        gui.fill(x, y + 10, x + Math.max(1, (int) (barW * heat)), y + 10 + barH, 0xFF000000 | heatRgb);
    }

    /* ---------------- HEADING ---------------- */

    static void renderHeading(GuiGraphics gui, Font f, LocalPlayer player, int w) {
        float yaw = ((player.getYRot() % 360F) + 360F) % 360F;
        String[] dirs = {"S", "SW", "W", "NW", "N", "NE", "E", "SE"};
        int index = Math.round(yaw / 45F) % 8;

        String text = dirs[index] + "  " + Math.round(yaw) + "°";
        int tw = f.width(text);
        gui.drawString(f, text, w / 2 - tw / 2, 4, 0x88FFCC);
    }

    /* ---------------- IMPACT FLASH ---------------- */

    static void renderImpactFlash(GuiGraphics gui, int w, int h) {
        float flash = TardisPhysics.impactFlash;
        if (flash <= 0.02F) return;

        int alpha = (int) Math.min(140, flash * 160);
        int color = (alpha << 24) | 0xFF2222;

        int edge = 40;
        gui.fill(0, 0, w, edge, color);
        gui.fill(0, h - edge, w, h, color);
        gui.fill(0, 0, edge, h, color);
        gui.fill(w - edge, 0, w, h, color);
    }

    static void renderCorners(GuiGraphics gui, int w, int h) {
        int pulse = (int) (85 + danger * 140);
        int c = (pulse << 24) | 0xAAFFAA;
        int l = 18;

        gui.fill(16, 16, 16 + l, 17, c);
        gui.fill(16, 16, 17, 16 + l, c);
        gui.fill(w - 16 - l, 16, w - 16, 17, c);
        gui.fill(w - 17, 16, w - 16, 16 + l, c);
        gui.fill(16, h - 17, 16 + l, h - 16, c);
        gui.fill(16, h - 16 - l, 17, h - 16, c);
        gui.fill(w - 16 - l, h - 17, w - 16, h - 16, c);
        gui.fill(w - 17, h - 16 - l, w - 16, h - 16, c);
    }

    static void renderScanlines(GuiGraphics gui, int w, int h) {
        for (int y = 0; y < h; y += 3) {
            int alpha = (int) (0x14 + danger * 60);
            alpha = Math.min(alpha, 180);

            int col = (alpha << 24) | 0x00FFAA;
            gui.fill(0, y, w, y + 2, col);
        }
    }

    static void renderVortex(GuiGraphics gui, int cx, int h) {
        int t = Minecraft.getInstance().player.tickCount;

        for (int i = 0; i < 10; i++) {
            int r = (int) ((t * (2 + danger * 6) + i * 18) % 220);
            drawRing(gui, cx, h - 10, r, 0x2200FFAA);
        }
    }

    static void drawRing(GuiGraphics gui, int cx, int cy, int r, int color) {
        for (int a = 0; a < 360; a += 8) {
            double rad = Math.toRadians(a);
            int x = (int) (cx + Math.cos(rad) * r);
            int y = (int) (cy + Math.sin(rad) * r * 0.35);
            gui.fill(x, y, x + 1, y + 1, color);
        }
    }
}