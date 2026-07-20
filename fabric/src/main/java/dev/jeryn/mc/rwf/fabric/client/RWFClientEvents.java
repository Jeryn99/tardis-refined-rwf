package dev.jeryn.mc.rwf.fabric.client;

import com.mojang.blaze3d.vertex.PoseStack;
import dev.jeryn.mc.rwf.common.TardisPhysics;
import dev.jeryn.mc.rwf.client.ClientUtil;
import dev.jeryn.mc.rwf.client.FlightModeClient;
import dev.jeryn.mc.rwf.fabric.client.RWFKeyMappingsFabric;
import dev.jeryn.mc.rwf.client.ClientFlightTracker;
import dev.jeryn.mc.rwf.network.RWFOpenDoor;
import dev.jeryn.mc.rwf.network.StopRWFMessage;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.world.phys.Vec3;

public class RWFClientEvents {

    private static long lastOpened = System.currentTimeMillis();

    public static void init() {

        WorldRenderEvents.AFTER_ENTITIES.register(context -> {
            if (TardisPhysics.dynamicsWorld == null) return;

            PoseStack poseStack = context.matrixStack();
            MultiBufferSource.BufferSource buffer = Minecraft.getInstance()
                    .renderBuffers().bufferSource();

            Vec3 cam = Minecraft.getInstance().gameRenderer.getMainCamera().getPosition();

            poseStack.pushPose();
            poseStack.translate(-cam.x, -cam.y, -cam.z);

            TardisPhysics.DEBUG_DRAW.begin(poseStack, buffer);
            TardisPhysics.dynamicsWorld.debugDrawWorld();
            TardisPhysics.DEBUG_DRAW.end();

            buffer.endBatch(RenderType.LINES);
            poseStack.popPose();
        });

        ClientTickEvents.END_CLIENT_TICK.register(client -> {

            FlightModeClient.tick();

            if (client.player == null) {
                ClientFlightTracker.clear();
            }

            long currentTime = System.currentTimeMillis();

            if (client.options.keyAttack.isDown() && currentTime - lastOpened >= 2000) {
                new RWFOpenDoor().send();
                lastOpened = currentTime;
            }

            while (RWFKeyMappingsFabric.EXIT_FLIGHT.consumeClick()) {
                if (client.player != null
                        && ClientFlightTracker.getForPlayer(client.player.getUUID()).isPresent()) {
                    new StopRWFMessage(false).send();
                }
            }

            while (RWFKeyMappingsFabric.FREE_FALL.consumeClick()) {
                TardisPhysics.toggleFreefall();
            }

            while (RWFKeyMappingsFabric.LOCAL_HOP.consumeClick()) {
                ClientUtil.attemptLocalHop();
            }

            TardisPhysics.onClientTick();
        });
    }
}