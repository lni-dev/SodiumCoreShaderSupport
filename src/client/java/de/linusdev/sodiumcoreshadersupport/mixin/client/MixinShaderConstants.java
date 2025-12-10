package de.linusdev.sodiumcoreshadersupport.mixin.client;

import net.caffeinemc.mods.sodium.client.gl.shader.ShaderConstants;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.HashMap;

@Mixin(ShaderConstants.Builder.class)
public abstract class MixinShaderConstants {

    @Shadow(remap = false) @Final private HashMap<String, String> constants;

    @Inject(at = @At("HEAD"), method = "build", remap = false)
    void build(CallbackInfoReturnable<ShaderConstants> cir) {
        constants.put("SODIUM_CORE_SHADER_SUPPORT", "");
    }
}
