package dev.jeryn.mc.rwf.mixin;

import org.spongepowered.asm.mixin.Mixin;
import whocraft.tardis_refined.common.tardis.manager.TardisPilotingManager;

@Mixin(value = TardisPilotingManager.class, remap = false)
public abstract class TardisPilotingManagerMixin {

  /*  @Final
    @Shadow
    private TardisLevelOperator operator;

    @Shadow
    private TardisNavLocation targetLocation;

    @Shadow
    private TardisNavLocation currentLocation;

    // Freefall trigger threshold
    private static final double FREEFALL_Y_VELOCITY_THRESHOLD = -0.25;

    @Inject(method = "tick(Lnet/minecraft/server/level/ServerLevel;)V", at = @At("HEAD"))
    private void rwf$onTick(ServerLevel level, CallbackInfo ci) {

        // ─────────────────────────────────────────────
        // 1. Resolve pilot
        // ─────────────────────────────────────────────
        ResourceKey<Level> interiorKey = operator.getLevel().dimension();
        FlightTracker.FlightData flightData = FlightTracker.IN_FLIGHT.get(interiorKey);

        boolean pilotIsFalling = false;

        if (flightData != null) {
            ServerPlayer pilot = flightData.player();

            if (pilot != null) {
                var vel = pilot.getDeltaMovement();

                pilotIsFalling =
                        !pilot.onGround()
                                && vel.y < FREEFALL_Y_VELOCITY_THRESHOLD;
            }
        }

        // ─────────────────────────────────────────────
        // 2. Current sim state
        // ─────────────────────────────────────────────
        boolean currentlyFreefalling =
                TardisFreefallManager.INSTANCE.isFreefalling(operator);
        // ─────────────────────────────────────────────
        // 3. Start freefall
        // ─────────────────────────────────────────────
        if (pilotIsFalling && !currentlyFreefalling) {

            if (targetLocation != null
                    && targetLocation.getLevel() != null
                    && targetLocation.getPosition() != null) {

                TardisFreefallManager.INSTANCE.startFreefall(
                        operator,
                        targetLocation.getLevel(),
                        targetLocation.getPosition()
                );
            }

            return;
        }

        // ─────────────────────────────────────────────
        // 4. Stop freefall
        // ─────────────────────────────────────────────
        if (!pilotIsFalling && currentlyFreefalling) {
            TardisFreefallManager.INSTANCE.cancel(operator);
            return;
        }

        // ─────────────────────────────────────────────
        // 5. Run simulation
        // ─────────────────────────────────────────────
        if (!currentlyFreefalling) return;

        TardisFreefallManager.INSTANCE.tickAll(0.05f);

        TardisFreefallPhysics sim =
                TardisFreefallManager.INSTANCE.getSim(operator);

        FreefallClientState state =
                sim != null ? sim.getClientState() : FreefallClientState.IDLE;

        rwf$syncClientState(state);

        // ─────────────────────────────────────────────
        // 6. Landing resolution
        // ─────────────────────────────────────────────
        var result = TardisFreefallManager.INSTANCE.pollLanding(operator);
        if (result == null) return;

        if (targetLocation != null && targetLocation.getPosition() != null) {

            BlockPos old = targetLocation.getPosition();
            BlockPos landed = new BlockPos(
                    old.getX(),
                    result.landedY(),
                    old.getZ()
            );

            targetLocation.setPosition(landed);

            if (currentLocation != null) {
                currentLocation.setPosition(landed);
            }

            rwf$applyRotationToShell(result.rotationXYZW(), targetLocation);
        }
    }

    // ─────────────────────────────────────────────
    // Packet sync (ONLY interior players)
    // ─────────────────────────────────────────────
    private void rwf$syncClientState(FreefallClientState state) {
        ServerLevel interior = (ServerLevel) operator.getLevel();
        System.out.printf("[Tardis Sync] interior=%s state=%s%n", interior, state);
        if (interior == null) return;

        var tardisId = interior.dimension().location();
        var players = Platform.getServer().getPlayerList().getPlayers();
        System.out.printf("[Tardis Sync] tardisId=%s playerCount=%d pitch=%.3f roll=%.3f fallSpeed=%.3f%n",
                tardisId, players.size(), state.pitch(), state.roll(), state.fallSpeed());

        for (ServerPlayer p : players) {
            new FreefallStatePacket(tardisId, state).send(p);
        }
    }

    // ─────────────────────────────────────────────
    // Optional shell rotation persistence hook
    // ─────────────────────────────────────────────
    private void rwf$applyRotationToShell(float[] quat, TardisNavLocation location) {
        // intentionally left for your shell renderer / NBT hook
        // keep if you wire BE rendering later
    }*/
}