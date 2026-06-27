package dev.jeryn.mc.rwf.mixin;

import dev.jeryn.mc.rwf.network.StartRWFMessage;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import whocraft.tardis_refined.client.screen.components.GenericMonitorSelectionList;
import whocraft.tardis_refined.client.screen.components.SelectionListEntry;
import whocraft.tardis_refined.client.screen.main.MonitorScreen;

@Mixin(value = MonitorScreen.class, remap = false)
public class MonitorScreenMixin {

    @Inject(method = "createSelectionList()Lwhocraft/tardis_refined/client/screen/components/GenericMonitorSelectionList;", at = @At("RETURN"), remap = false)
    private void rwf$addExteriorFlight(CallbackInfoReturnable<GenericMonitorSelectionList> cir) {
        GenericMonitorSelectionList list = cir.getReturnValue();
        if (list == null) {
            return;
        }
        int hPos = Minecraft.getInstance().getWindow().getGuiScaledWidth() / 2 - 20;
        list.children().add(new SelectionListEntry(Component.translatable("EXTERIOR FLIGHT"), (entry) -> {
            new StartRWFMessage().send();
        }, hPos));
    }

}
