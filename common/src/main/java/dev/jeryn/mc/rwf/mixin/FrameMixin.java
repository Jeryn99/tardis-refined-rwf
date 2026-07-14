package dev.jeryn.mc.rwf.mixin;

import dev.jeryn.frame.tardis.Frame;
import net.minecraft.client.animation.AnimationChannel;
import net.minecraft.client.animation.AnimationChannel.Targets;
import net.minecraft.client.animation.KeyframeAnimations;
import org.joml.Vector3f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Frame.class)
public class FrameMixin {

    @Inject(
            method = "targetToVector",
            at = @At("HEAD"),
            cancellable = true
    )
    private static void negateRotation(AnimationChannel.Target target, Vector3f vector3f, CallbackInfoReturnable<Vector3f> cir) {
        if (target == Targets.ROTATION) {
            cir.setReturnValue(KeyframeAnimations.degreeVec(-vector3f.x, -vector3f.y, vector3f.z));
        }
    }
}