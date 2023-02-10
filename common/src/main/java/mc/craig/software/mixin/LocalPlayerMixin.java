package mc.craig.software.mixin;

import mc.craig.software.ClientUtil;
import mc.craig.software.common.entity.TardisEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.Input;
import net.minecraft.client.player.LocalPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LocalPlayer.class)
public class LocalPlayerMixin {

    @Shadow
    public Input input;

    @Inject(method = "aiStep()V", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/Minecraft;getTutorial()Lnet/minecraft/client/tutorial/Tutorial;"))
    private void inputEdit(CallbackInfo ci) {
        LocalPlayer localPlayer = (LocalPlayer) (Object) this;
        ClientUtil.handleInput(localPlayer, input);
    }

    @Inject(at = @At("HEAD"), cancellable = true, method = "isHandsBusy()Z")
    private void handsBusy(CallbackInfoReturnable<Boolean> cir) {
        LocalPlayer localPlayer = (LocalPlayer) (Object) this;
        if(Minecraft.getInstance().getCameraEntity() instanceof TardisEntity){
            cir.setReturnValue(true);
        }
    }

}