package dev.jeryn.mc.rwf.fabric;

import dev.jeryn.mc.rwf.common.entity.FlightTracker;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.event.player.AttackBlockCallback;
import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.fabricmc.fabric.api.event.player.UseItemCallback;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;

public class RWFCommonEvents {

    public static void init() {

        ServerPlayConnectionEvents.DISCONNECT.register((handler, server) -> {
            try {
                if (!FlightTracker.isPlayerFlying(handler.getPlayer().getUUID())) return;

                FlightTracker.IN_FLIGHT.values().stream()
                        .filter(data -> data.player().getUUID().equals(handler.getPlayer().getUUID()))
                        .findFirst()
                        .ifPresent(FlightTracker::loggedOut);
            } catch (Throwable t) {
                System.out.println("[RWF] DISCONNECT handler failed " + t);
            }
        });

        ServerTickEvents.END_SERVER_TICK.register(FlightTracker::serverTick);

        UseItemCallback.EVENT.register((player, world, hand) -> {
            if (FlightTracker.isFlying(player)) {
                return InteractionResultHolder.fail(player.getItemInHand(hand));
            }
            return InteractionResultHolder.pass(player.getItemInHand(hand));
        });

        UseBlockCallback.EVENT.register((player, world, hand, hitResult) -> {
            if (!player.getItemInHand(hand).isEmpty() && FlightTracker.isFlying(player)) {
                return InteractionResult.FAIL;
            }
            return InteractionResult.PASS;
        });

        PlayerBlockBreakEvents.BEFORE.register((world, player, pos, state, blockEntity) -> !FlightTracker.isFlying(player));

        AttackBlockCallback.EVENT.register((player, world, hand, pos, direction) -> {
            if (FlightTracker.isFlying(player)) {
                return InteractionResult.FAIL;
            }
            return InteractionResult.PASS;
        });
    }
}