package de.linusdev.sodiumcoreshadersupport.mixin.client;

import de.linusdev.sodiumcoreshadersupport.CullingConfig;
import net.caffeinemc.mods.sodium.client.render.viewport.Viewport;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = Viewport.class, remap = false)
public class MixinViewport {

    @Inject(method = "isBoxVisible", at = @At("HEAD"), cancellable = true)
    private void shadowculling$disableFrustumCulling(CallbackInfoReturnable<Boolean> cir) {
        if (CullingConfig.disableFrustumCulling) {
            cir.setReturnValue(true);
        }
    }
}
