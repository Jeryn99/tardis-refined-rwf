package dev.jeryn.mc.rwf.mixin;

import dev.jeryn.mc.rwf.common.entity.FlightTracker;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LivingEntity.class)
public class LivingEntityMixin {

    @Inject(at = @At("HEAD"), cancellable = true, method = "jumpFromGround()V")
    private void jump(CallbackInfo ci) {
        LivingEntity livingEntity = (LivingEntity) (Object) this;
        if(FlightTracker.isFlyingEitherSide(livingEntity)) {
            double jumpPower = ((double) livingEntity.getJumpPower() + livingEntity.getJumpBoostPower()) * 2;
            Vec3 vec3 = livingEntity.getDeltaMovement();
            livingEntity.setDeltaMovement(vec3.x, jumpPower, vec3.z);
            ci.cancel();
        }
    }

    @Inject(at = @At("HEAD"), cancellable = true, method = "hurt(Lnet/minecraft/world/damagesource/DamageSource;F)Z")
    private void blockDamageWhileFlying(DamageSource source, float amount, CallbackInfoReturnable<Boolean> cir) {
        LivingEntity livingEntity = (LivingEntity) (Object) this;
        if (livingEntity instanceof Player && FlightTracker.isFlying(livingEntity)) {
            cir.setReturnValue(false);
        }
    }

    @Inject(at = @At("TAIL"), method = "travel(Lnet/minecraft/world/phys/Vec3;)V")
    private void rwf$spectatorPitchTilt(Vec3 travelVector, CallbackInfo ci) {
        LivingEntity livingEntity = (LivingEntity) (Object) this;
        if (!(livingEntity instanceof Player player)) return;
        if (!player.level().isClientSide()) return;
        dev.jeryn.mc.rwf.client.ClientFlightTracker.applySpectatorPitchTilt(player);
    }
}
