package dev.jeryn.mc.rwf.mixin;

import dev.jeryn.mc.rwf.common.entity.FlightTracker;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import whocraft.tardis_refined.common.capability.tardis.TardisLevelOperator;

@Mixin(TardisLevelOperator.class)
public class TardisLevelOperatorMixin {

    @Inject(at = @At("HEAD"), cancellable = true,
            method = "exitTardis(Lnet/minecraft/world/entity/Entity;Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/core/BlockPos;Lnet/minecraft/core/Direction;Z)Z")
    private void rwf$blockExitWhileFlying(Entity entity, ServerLevel level, BlockPos pos, Direction direction, boolean force, CallbackInfoReturnable<Boolean> cir) {
        if (!(entity instanceof Player player)) return;

        TardisLevelOperator self = (TardisLevelOperator) (Object) this;
        if (FlightTracker.isFlying(self.getLevelKey())) {
            player.displayClientMessage(
                    Component.translatable("message.tardis_refined_rwf.exit_locked_flying"), true);
            cir.setReturnValue(false);
        }
    }
}
