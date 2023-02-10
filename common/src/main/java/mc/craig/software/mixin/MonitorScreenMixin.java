package mc.craig.software.mixin;

import mc.craig.software.network.StartRWFMessage;
import net.minecraft.client.gui.components.ObjectSelectionList;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import whocraft.tardis_refined.client.screen.components.GenericMonitorSelectionList;
import whocraft.tardis_refined.client.screen.selections.SelectionScreen;

@Mixin(value = SelectionScreen.class, remap = false)
public class MonitorScreenMixin {

    @Shadow
    private ObjectSelectionList list;

    @Inject(at = @At("RETURN"), cancellable = true, method = "init()V")
    protected void init(CallbackInfo ci) {
        list.children().add(new GenericMonitorSelectionList.Entry(Component.translatable("flight mung"), (entry) -> {
            new StartRWFMessage().send();
        }));
    }

}
