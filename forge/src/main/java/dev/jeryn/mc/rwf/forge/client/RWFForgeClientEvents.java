package dev.jeryn.mc.rwf.forge.client;

import com.mojang.blaze3d.vertex.PoseStack;
import dev.jeryn.mc.rwf.RealWorldFlight;
import dev.jeryn.mc.rwf.client.ClientUtil;
import dev.jeryn.mc.rwf.client.FlightModeClient;
import dev.jeryn.mc.rwf.client.RWFKeyMappings;
import dev.jeryn.mc.rwf.client.model.RWFModelRegistry;
import dev.jeryn.mc.rwf.client.model.forge.RWFModelRegistryImpl;
import dev.jeryn.mc.rwf.client.ClientFlightTracker;
import dev.jeryn.mc.rwf.common.TardisPhysics;
import dev.jeryn.mc.rwf.common.entity.FlightTracker;
import dev.jeryn.mc.rwf.network.RWFOpenDoor;
import dev.jeryn.mc.rwf.network.StopRWFMessage;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = RealWorldFlight.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public class RWFForgeClientEvents {

    private static long lastOpened = System.currentTimeMillis();

    @SubscribeEvent
    public static void onRenderLevelLast(RenderLevelStageEvent event) {
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_ENTITIES) return;
        if (TardisPhysics.dynamicsWorld == null) return;

        PoseStack poseStack = event.getPoseStack();
        MultiBufferSource.BufferSource buffer = Minecraft.getInstance()
                .renderBuffers().bufferSource();

        // Translate to camera-relative space
        Vec3 cam = Minecraft.getInstance().gameRenderer.getMainCamera().getPosition();
        poseStack.pushPose();
        poseStack.translate(-cam.x, -cam.y, -cam.z);

        TardisPhysics.DEBUG_DRAW.begin(poseStack, buffer);
        TardisPhysics.dynamicsWorld.debugDrawWorld();
        TardisPhysics.DEBUG_DRAW.end();

        buffer.endBatch(RenderType.LINES);
        poseStack.popPose();
    }

    @SubscribeEvent
    public static void onRegisterLayerDefinitions(EntityRenderersEvent.RegisterLayerDefinitions event) {
        RWFModelRegistry.init();
        RWFModelRegistryImpl.register(event);
    }

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) {
            return;
        }

        // Client Effects
        FlightModeClient.tick();

        if (Minecraft.getInstance().player == null) {
            FlightTracker.IN_FLIGHT.clear();
        }

        // Handle Door Opening
        long currentTime = System.currentTimeMillis();
        if (Minecraft.getInstance().options.keyAttack.isDown() && currentTime - lastOpened >= 2000) {
            new RWFOpenDoor().send();
            lastOpened = currentTime;
        }

        while (RWFKeyMappings.EXIT_FLIGHT.consumeClick()) {
            if (Minecraft.getInstance().player != null
                    && ClientFlightTracker.getForPlayer(Minecraft.getInstance().player.getUUID()).isPresent()) {
                new StopRWFMessage(false).send();
            }
        }

        while (RWFKeyMappings.FREE_FALL.consumeClick()) {
            TardisPhysics.toggleFreefall();
        }

        while (RWFKeyMappings.LOCAL_HOP.consumeClick()) {
            ClientUtil.attemptLocalHop();
        }
    }

    @SubscribeEvent
    public static void onClientTick(TickEvent event) {
        if (event.type == TickEvent.Type.CLIENT && event.phase == TickEvent.Phase.END) {
            TardisPhysics.onClientTick();
        }
    }
}
