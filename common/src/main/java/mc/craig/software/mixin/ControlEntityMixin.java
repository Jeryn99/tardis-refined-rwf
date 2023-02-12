package mc.craig.software.mixin;

import mc.craig.software.common.entity.FlightTracker;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import whocraft.tardis_refined.common.entity.ControlEntity;
import whocraft.tardis_refined.common.tardis.control.ConsoleControl;
import whocraft.tardis_refined.common.util.PlayerUtil;

@Mixin(value = ControlEntity.class, remap = false)
public class ControlEntityMixin {

    @Inject(at = @At("HEAD"), cancellable = true, method = "hurt(Lnet/minecraft/world/damagesource/DamageSource;F)Z")
    private void stopInteractionOnHurt(DamageSource damageSource, float amount, CallbackInfoReturnable<Boolean> cir) {
        ControlEntity control = (ControlEntity) (Object) this;

        if (FlightTracker.isFlying(control.level.dimension())) {
            if (control.controlSpecification() != null && control.controlSpecification().control() != ConsoleControl.MONITOR && control.controlSpecification().control() != ConsoleControl.DOOR_TOGGLE) {
                PlayerUtil.sendMessage((LivingEntity) damageSource.getDirectEntity(), "TARDIS in Flight", true);
                cir.setReturnValue(false);
            }
        }

    }


    @Inject(at = @At("HEAD"), cancellable = true, method = "interactAt(Lnet/minecraft/world/entity/player/Player;Lnet/minecraft/world/phys/Vec3;Lnet/minecraft/world/InteractionHand;)Lnet/minecraft/world/InteractionResult;")
    private void stopInteractionOnInteract(Player player, Vec3 vec3, InteractionHand interactionHand, CallbackInfoReturnable<InteractionResult> cir) {
        ControlEntity control = (ControlEntity) (Object) this;
        if (FlightTracker.isFlying(control.level.dimension())) {

            if (control.controlSpecification() != null && control.controlSpecification().control() != ConsoleControl.MONITOR && control.controlSpecification().control() != ConsoleControl.DOOR_TOGGLE) {
                PlayerUtil.sendMessage(player, "TARDIS in Flight", true);
                cir.setReturnValue(InteractionResult.CONSUME);
            }
        }
    }


}
