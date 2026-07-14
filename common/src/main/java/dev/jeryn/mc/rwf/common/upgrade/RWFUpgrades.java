package dev.jeryn.mc.rwf.common.upgrade;

import dev.jeryn.mc.rwf.RealWorldFlight;
import net.minecraft.world.item.Items;
import whocraft.tardis_refined.common.capability.tardis.upgrades.Upgrade;
import whocraft.tardis_refined.common.capability.tardis.upgrades.Upgrade.UpgradeType;
import whocraft.tardis_refined.common.util.RegistryHelper;
import whocraft.tardis_refined.registry.DeferredRegistry;
import whocraft.tardis_refined.registry.RegistrySupplier;
import whocraft.tardis_refined.registry.TRUpgrades;

public class RWFUpgrades {

    public static final DeferredRegistry<Upgrade> RWF_UPGRADES =
            DeferredRegistry.create(RealWorldFlight.MOD_ID, TRUpgrades.UPGRADE_REGISTRY_KEY);

    // ----------------------------
    // ROOT
    // ----------------------------
    public static final RegistrySupplier<Upgrade> FLIGHT_UNLOCK;

    // ----------------------------
    // EFFICIENCY (MPG) CHAIN
    // ----------------------------
    public static final RegistrySupplier<Upgrade> FLIGHT_EFFICIENCY_1;
    public static final RegistrySupplier<Upgrade> FLIGHT_EFFICIENCY_2;
    public static final RegistrySupplier<Upgrade> FLIGHT_EFFICIENCY_3;
    public static final RegistrySupplier<Upgrade> FLIGHT_EFFICIENCY_4;
    public static final RegistrySupplier<Upgrade> FLIGHT_EFFICIENCY_5;

    static {

        // Unlock flight entirely
        FLIGHT_UNLOCK = RWF_UPGRADES.register("flight_unlock", () ->
                new Upgrade(
                        Items.ELYTRA::getDefaultInstance,
                        RegistryHelper.makeKey("flight_unlock"),
                        UpgradeType.MAIN_UPGRADE
                )
                        .setSkillPointsRequired(1)
                        .setPosition(9.0, 1.0)
        );

        // MPG / efficiency tiers
        FLIGHT_EFFICIENCY_1 = RWF_UPGRADES.register("flight_efficiency_1", () ->
                new Upgrade(
                        Items.CLOCK::getDefaultInstance,
                        FLIGHT_UNLOCK,
                        RegistryHelper.makeKey("flight_efficiency_1"),
                        UpgradeType.SUB_UPGRADE
                )
                        .setSkillPointsRequired(5)
                        .setPosition(8.0, 2.0)
        );

        FLIGHT_EFFICIENCY_2 = RWF_UPGRADES.register("flight_efficiency_2", () ->
                new Upgrade(
                        Items.CLOCK::getDefaultInstance,
                        FLIGHT_EFFICIENCY_1,
                        RegistryHelper.makeKey("flight_efficiency_2"),
                        UpgradeType.SUB_UPGRADE
                )
                        .setSkillPointsRequired(8)
                        .setPosition(8.0, 3.0)
        );

        FLIGHT_EFFICIENCY_3 = RWF_UPGRADES.register("flight_efficiency_3", () ->
                new Upgrade(
                        Items.REDSTONE::getDefaultInstance,
                        FLIGHT_EFFICIENCY_2,
                        RegistryHelper.makeKey("flight_efficiency_3"),
                        UpgradeType.SUB_UPGRADE
                )
                        .setSkillPointsRequired(12)
                        .setPosition(8.0, 4.0)
        );

        FLIGHT_EFFICIENCY_4 = RWF_UPGRADES.register("flight_efficiency_4", () ->
                new Upgrade(
                        Items.REDSTONE::getDefaultInstance,
                        FLIGHT_EFFICIENCY_3,
                        RegistryHelper.makeKey("flight_efficiency_4"),
                        UpgradeType.SUB_UPGRADE
                )
                        .setSkillPointsRequired(18)
                        .setPosition(8.0, 5.0)
        );

        FLIGHT_EFFICIENCY_5 = RWF_UPGRADES.register("flight_efficiency_5", () ->
                new Upgrade(
                        Items.RECOVERY_COMPASS::getDefaultInstance,
                        FLIGHT_EFFICIENCY_4,
                        RegistryHelper.makeKey("flight_efficiency_5"),
                        UpgradeType.SUB_UPGRADE
                )
                        .setSkillPointsRequired(25)
                        .setPosition(8.0, 6.0)
        );
    }
}