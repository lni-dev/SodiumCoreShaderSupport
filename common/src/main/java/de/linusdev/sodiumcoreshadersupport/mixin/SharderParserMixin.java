package de.linusdev.sodiumcoreshadersupport.mixin;

import net.caffeinemc.mods.sodium.client.gl.shader.ShaderParser;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

import static de.linusdev.sodiumcoreshadersupport.Constants.LOG;

@Mixin(ShaderParser.class)
public class SharderParserMixin {
    @Shadow
    @Final
    private List<String> lines;

    @Inject(at = @At("HEAD"), method = "finish", remap = false)
    private void loadShaderInject(
            CallbackInfoReturnable<ShaderParser.ParsedShader> cir
    ) {
        LOG.debug("Parsed shader: \n{}", String.join("\n", this.lines));
    }

}
