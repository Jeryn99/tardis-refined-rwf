package dev.jeryn.mc.rwf.client;

import dev.jeryn.frame.tardis.Frame;
import dev.jeryn.mc.rwf.RealWorldFlight;
import net.minecraft.client.Minecraft;
import net.minecraft.client.animation.AnimationDefinition;
import net.minecraft.client.model.HierarchicalModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.AnimationState;
import net.minecraft.world.entity.LivingEntity;
import whocraft.tardis_refined.client.TardisClientData;

import java.util.Random;

public class ConsolePilotModel<T extends LivingEntity> extends HierarchicalModel<T> {

    // ── Animation definitions ─────────────────────────────────────────────────
    public static final AnimationDefinition PILOT_1 = Frame.loadAnimation(
            new ResourceLocation(RealWorldFlight.MOD_ID, "frame/player/pilot_1.json"));

    public static final AnimationDefinition PILOT_2 = Frame.loadAnimation(
            new ResourceLocation(RealWorldFlight.MOD_ID, "frame/player/pilot_2.json"));

    public static final AnimationDefinition PILOT_3 = Frame.loadAnimation(
            new ResourceLocation(RealWorldFlight.MOD_ID, "frame/player/pilot_3.json")); // fixed typo (was pilot_4.json)

    public static final AnimationDefinition PILOT_4 = Frame.loadAnimation(
            new ResourceLocation(RealWorldFlight.MOD_ID, "frame/player/pilot_4.json"));

    public static final AnimationDefinition PANIC = Frame.loadAnimation(
            new ResourceLocation(RealWorldFlight.MOD_ID, "frame/player/pilot_4.json"));

    private static final AnimationDefinition[] PILOTING_ANIMATIONS = {
            PILOT_1, PILOT_2, PILOT_3, PILOT_4
    };

    // ── Model ─────────────────────────────────────────────────────────────────
    private final ModelPart root;

    // ── Single active state — hard cuts, no blending ────────────────────────────
    private final AnimationState state = new AnimationState();
    private AnimationDefinition currentDef = null;

    private final Random random = new Random();
    private int lastPilotIndex = -1;

    // Tick (not frame) at which the current animation started.
    private int switchTick = -1;

    public ConsolePilotModel(ModelPart modelPart) {
        super();
        this.root = modelPart;
        this.young = false;
    }

    // ─────────────────────────────────────────────────────────────────────────

    private static int holdTicksFor(AnimationDefinition def) {
        return Math.max(1, Math.round(def.lengthInSeconds() * 20f));
    }

    private AnimationDefinition pickNextPilotAnimation() {
        if (PILOTING_ANIMATIONS.length == 1) {
            return PILOTING_ANIMATIONS[0];
        }
        int idx = 0;
        do {
            System.out.println(idx);
            idx = random.nextInt(PILOTING_ANIMATIONS.length);
        } while (idx == lastPilotIndex);
        lastPilotIndex = idx;
        return PILOTING_ANIMATIONS[idx];
    }

    private void switchTo(AnimationDefinition def, int tickCount) {
        currentDef = def;
        switchTick = tickCount;
   /*     state.stop();
        state.start(tickCount);*/
    }

    // ─────────────────────────────────────────────────────────────────────────

    @Override
    public void setupAnim(T entity, float limbSwing, float limbSwingAmount,
                          float ageInTicks, float netHeadYaw, float headPitch) {

        young = false;

        int tickCount = Minecraft.getInstance().player.tickCount;
        TardisClientData tardisClientData =
                TardisClientData.getInstance(Minecraft.getInstance().level.dimension());
        boolean hasFuel = tardisClientData.getFuel() > 0;

        // ── First-time init ────────────────────────────────────────────────────
        if (currentDef == null) {
            switchTo(hasFuel ? pickNextPilotAnimation() : PANIC, tickCount);
        }

        // ── Instant switch into PANIC the moment fuel runs out ─────────────────
        if (!hasFuel && currentDef != PANIC) {
            switchTo(PANIC, tickCount);
        }
        // ── Instant switch out of PANIC the moment fuel returns ────────────────
        else if (hasFuel && currentDef == PANIC) {
            switchTo(pickNextPilotAnimation(), tickCount);
        }

        if (!state.isStarted()) {
            state.start(tickCount);
        }

        // ── Rotate to a new random pilot clip once the current one finishes ────
        // Compare against real game ticks, not render frames, so this only
        // fires once the animation has actually had time to play out.
        if (currentDef != PANIC && (tickCount - switchTick) >= holdTicksFor(currentDef)) {
            switchTo(pickNextPilotAnimation(), tickCount);
        }

        animate(state, currentDef, tickCount, 1f);
    }

    // ─────────────────────────────────────────────────────────────────────────

    @Override
    public ModelPart root() {
        return root;
    }
}