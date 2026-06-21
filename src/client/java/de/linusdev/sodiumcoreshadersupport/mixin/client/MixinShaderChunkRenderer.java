package de.linusdev.sodiumcoreshadersupport.mixin.client;

import com.mojang.blaze3d.pipeline.BindGroupLayout;
import com.mojang.blaze3d.shaders.UniformType;
import net.caffeinemc.mods.sodium.client.render.chunk.ShaderChunkRenderer;
import net.caffeinemc.mods.sodium.client.render.chunk.terrain.DefaultTerrainRenderPasses;
import net.caffeinemc.mods.sodium.client.render.chunk.terrain.TerrainRenderPass;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

@Mixin(ShaderChunkRenderer.class)
public abstract class MixinShaderChunkRenderer {

    @Redirect(
            method = "<clinit>",
            at = @At(
                    value = "INVOKE",
                    target = "Lcom/mojang/blaze3d/pipeline/BindGroupLayout$Builder;build()Lcom/mojang/blaze3d/pipeline/BindGroupLayout;"
            )
    )
    private static BindGroupLayout modifyBindGroup(
            BindGroupLayout.Builder builder
    ) {
        return builder
                .withUniform("u_SCSS", UniformType.UNIFORM_BUFFER)
                .build();
    }


    @Inject(
            method = "createShaderConstants",
            at = @At("RETURN")
    )
    private static void createShaderConstants(
            TerrainRenderPass pass, CallbackInfoReturnable<List<String>> cir
    ) {
        List<String> v = cir.getReturnValue();

        v.add("SODIUM_CORE_SHADER_SUPPORT");

        if (pass == DefaultTerrainRenderPasses.SOLID) {
            v.add("RENDER_PASS_SOLID");
        } else if (pass == DefaultTerrainRenderPasses.CUTOUT) {
            v.add("RENDER_PASS_CUTOUT");
        } else if (pass == DefaultTerrainRenderPasses.TRANSLUCENT) {
            v.add("RENDER_PASS_TRANSLUCENT");
        }
    }
}
