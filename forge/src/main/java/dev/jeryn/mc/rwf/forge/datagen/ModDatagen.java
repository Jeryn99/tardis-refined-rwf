package dev.jeryn.mc.rwf.forge.datagen;

import dev.jeryn.mc.rwf.forge.data.RWFEnglishLang;
import net.minecraftforge.data.event.GatherDataEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD)
public class ModDatagen {

    @SubscribeEvent
    public static void gatherData(GatherDataEvent event) {
        var generator = event.getGenerator();
        var output = generator.getPackOutput();

        if (event.includeClient()) {
            generator.addProvider(true, new RWFEnglishLang(output));
        }
    }
}