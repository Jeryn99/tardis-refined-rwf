package dev.jeryn.mc.rwf.forge.data;

import dev.jeryn.mc.rwf.RealWorldFlight;
import net.minecraft.data.PackOutput;
import net.minecraftforge.common.data.LanguageProvider;

public class RWFEnglishLang extends LanguageProvider {

    public RWFEnglishLang(PackOutput output) {
        super(output, RealWorldFlight.MOD_ID, "en_us");
    }

    @Override
    protected void addTranslations() {
        add("key.tardis_refined_rwf.free_fall", "Freefall Toggle");
        add("key.tardis_refined_rwf.exit_flight", "End Flight");
        add("key.categories.tardis_refined_rwf", "Tardis Refined - Real World Flight");

        add("upgrade.tardis_refined_rwf.flight_unlock", "Flight Control System");
        add("upgrade.tardis_refined_rwf.flight_unlock.description", "Unlocks TARDIS flight control systems.");

        add("message.tardis_refined_rwf.flight_locked", "You need the Flight Control System upgrade before you can fly!");

        add("upgrade.tardis_refined_rwf.flight_efficiency_1", "Fuel Efficiency I");
        add("upgrade.tardis_refined_rwf.flight_efficiency_1.description", "Slightly improves fuel consumption during flight.");

        add("upgrade.tardis_refined_rwf.flight_efficiency_2", "Fuel Efficiency II");
        add("upgrade.tardis_refined_rwf.flight_efficiency_2.description", "Moderately improves fuel consumption during flight.");

        add("upgrade.tardis_refined_rwf.flight_efficiency_3", "Fuel Efficiency III");
        add("upgrade.tardis_refined_rwf.flight_efficiency_3.description", "Significantly improves fuel consumption during flight.");

        add("upgrade.tardis_refined_rwf.flight_efficiency_4", "Fuel Efficiency IV");
        add("upgrade.tardis_refined_rwf.flight_efficiency_4.description", "Greatly improves fuel consumption during flight.");

        add("upgrade.tardis_refined_rwf.flight_efficiency_5", "Fuel Efficiency V");
        add("upgrade.tardis_refined_rwf.flight_efficiency_5.description", "Massively improves fuel consumption during flight.");
    }
}
