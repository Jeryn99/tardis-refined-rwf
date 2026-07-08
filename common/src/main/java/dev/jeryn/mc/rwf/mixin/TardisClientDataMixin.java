package dev.jeryn.mc.rwf.mixin;

import dev.jeryn.mc.rwf.RWFAnimationStateAccessor;
import net.minecraft.world.entity.AnimationState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import whocraft.tardis_refined.client.TardisClientData;

@Mixin(TardisClientData.class)
public abstract class TardisClientDataMixin implements RWFAnimationStateAccessor {

    @Unique
    public AnimationState RWF_ANIM_STATE = new AnimationState();

    @Unique
    @Override
    public AnimationState rwf$getAnimState() {
        return this.RWF_ANIM_STATE;
    }

    @Unique
    @Override
    public void rwf$setAnimState(AnimationState state) {
        this.RWF_ANIM_STATE = state;
    }
}