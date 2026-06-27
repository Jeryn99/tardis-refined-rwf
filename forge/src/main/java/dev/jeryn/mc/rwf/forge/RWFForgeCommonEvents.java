package dev.jeryn.mc.rwf.forge;

import dev.jeryn.mc.rwf.RealWorldFlight;
import dev.jeryn.mc.rwf.common.entity.TardisEntity;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = RealWorldFlight.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class RWFForgeCommonEvents {

    @SubscribeEvent
    public static void onLoggedOut(PlayerEvent.PlayerLoggedOutEvent event) {
        if (event.getEntity() instanceof ServerPlayer serverPlayer && serverPlayer.getFirstPassenger() instanceof TardisEntity tardis) {
            tardis.finishFlight(serverPlayer.serverLevel(), false);
        }
    }

}
