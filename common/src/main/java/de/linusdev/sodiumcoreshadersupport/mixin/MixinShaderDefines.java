package de.linusdev.sodiumcoreshadersupport.mixin;

import net.minecraft.client.renderer.ShaderDefines;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ShaderDefines.class)
public abstract class MixinShaderDefines {

    @Inject(at = @At("RETURN"), method = "asSourceDirectives", cancellable = true)
    public void asSourceDirectivesInject(CallbackInfoReturnable<String> cir) {
        cir.setReturnValue(cir.getReturnValue() + "#define SODIUM_CORE_SHADER_SUPPORT\n");
    }

}