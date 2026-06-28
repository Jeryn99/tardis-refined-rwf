package dev.jeryn.mc.rwf.client;

import dev.jeryn.frame.tardis.Frame;
import dev.jeryn.mc.rwf.RealWorldFlight;
import mc.craig.software.regen.util.AnimationUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.animation.AnimationDefinition;
import net.minecraft.client.model.HierarchicalModel;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.AnimationState;
import net.minecraft.world.entity.LivingEntity;

public class ConsolePilotModel<T extends LivingEntity> extends HierarchicalModel<T> {

    public static final AnimationDefinition PILOTING = Frame.loadAnimation(new ResourceLocation(RealWorldFlight.MOD_ID, "frame/player/pilot.json"));
    private static final AnimationState PILOTING_STATE = new AnimationState();
    private final ModelPart root;

    public ConsolePilotModel(ModelPart modelPart) {
        super();
        this.root = modelPart;
        young = false;
    }

    @Override
    public void setupAnim(T entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {

        if(!PILOTING_STATE.isStarted()){
            PILOTING_STATE.start(Minecraft.getInstance().player.tickCount);
        }

        young = false;
///*
//
//        // Reset body entirely — don't touch it
//        this.body.xRot = 0;
//        this.body.yRot = 0;
//        this.body.zRot = 0;
//
//        // Smooth sin waves at low frequency for fluid motion
//        float slowSin = (float) Math.sin(ageInTicks * 0.03F);
//        float slowCos = (float) Math.cos(ageInTicks * 0.025F);
//
//        // Arms gripping console — smooth subtle sway, no jitter
//        this.leftArm.xRot  = (float) Math.toRadians(-60) + slowSin * 0.03F;
//        this.leftArm.zRot  = (float) Math.toRadians(15)  + slowCos * 0.02F;
//
//        this.rightArm.xRot = (float) Math.toRadians(-60) + slowCos * 0.03F;
//        this.rightArm.zRot = (float) Math.toRadians(-15) - slowSin * 0.02F;
//
//        // Head slowly looking around the console, slight downward tilt
//        this.head.xRot = (float) Math.toRadians(20) + slowSin * 0.04F;
//        this.head.yRot = slowCos * 0.06F;
//
//        // Lock legs
//        this.leftLeg.xRot  = 0;
//        this.leftLeg.yRot  = 0;
//        this.leftLeg.zRot  = 0;
//        this.rightLeg.xRot = 0;
//        this.rightLeg.yRot = 0;
//        this.rightLeg.zRot = 0;
//
//        this.leftPants.copyFrom(this.leftLeg);
//        this.rightPants.copyFrom(this.rightLeg);
//        this.leftSleeve.copyFrom(this.leftArm);
//        this.rightSleeve.copyFrom(this.rightArm);
//        this.jacket.copyFrom(this.body);
//        this.hat.copyFrom(this.head);
//*/

        animate(PILOTING_STATE, PILOTING, Minecraft.getInstance().player.tickCount, 1);
    }


    @Override
    public ModelPart root() {
        return root;
    }
}