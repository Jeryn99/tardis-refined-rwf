package dev.jeryn.mc.rwf;

import net.minecraft.world.entity.AnimationState;

public interface RWFAnimationStateAccessor {
    AnimationState rwf$getAnimState();

    void rwf$setAnimState(AnimationState state);
}