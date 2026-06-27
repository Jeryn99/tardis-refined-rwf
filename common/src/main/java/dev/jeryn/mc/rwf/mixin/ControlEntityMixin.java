package dev.jeryn.mc.rwf.mixin;

import org.spongepowered.asm.mixin.Mixin;
import whocraft.tardis_refined.common.entity.ControlEntity;

@Mixin(value = ControlEntity.class, remap = false)
public class ControlEntityMixin {

   /* @Inject(at = @At("HEAD"), cancellable = true, method = "hurt(Lnet/minecraft/world/damagesource/DamageSource;F)Z", remap = true)
    private void stopInteractionOnHurt(DamageSource damageSource, float amount, CallbackInfoReturnable<Boolean> cir) {
        ControlEntity control = (ControlEntity) (Object) this;

        if (FlightTracker.isFlying(control.level().dimension())) {
            if (control.controlSpecification() != null && !control.controlSpecification().control().equals(TRControlRegistry.MONITOR.get()) && !control.controlSpecification().control().equals(TRControlRegistry.DOOR_TOGGLE.get())) {
                PlayerUtil.sendMessage((LivingEntity) damageSource.getDirectEntity(), "TARDIS in Flight", true);
                cir.setReturnValue(false);
            }
        }

    }


    @Inject(at = @At("HEAD"), cancellable = true, method = "interactAt(Lnet/minecraft/world/entity/player/Player;Lnet/minecraft/world/phys/Vec3;Lnet/minecraft/world/InteractionHand;)Lnet/minecraft/world/InteractionResult;", remap = true)
    private void stopInteractionOnInteract(Player player, Vec3 vec3, InteractionHand interactionHand, CallbackInfoReturnable<InteractionResult> cir) {
        ControlEntity control = (ControlEntity) (Object) this;
        if (FlightTracker.isFlying(control.level().dimension())) {

            if (control.controlSpecification() != null && !control.controlSpecification().control().equals(TRControlRegistry.MONITOR.get()) && !control.controlSpecification().control().equals(TRControlRegistry.DOOR_TOGGLE.get())) {
                PlayerUtil.sendMessage(player, "TARDIS in Flight", true);
                cir.setReturnValue(InteractionResult.CONSUME);
            }
        }
    }*/


}
