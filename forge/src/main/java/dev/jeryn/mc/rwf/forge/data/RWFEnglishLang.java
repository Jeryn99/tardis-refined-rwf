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
    }
}
