package de.linusdev.sodiumcoreshadersupport.mixin;

import net.caffeinemc.mods.sodium.client.gl.shader.ShaderConstants;
import net.caffeinemc.mods.sodium.client.render.chunk.ShaderChunkRenderer;
import net.caffeinemc.mods.sodium.client.render.chunk.shader.ChunkFogMode;
import net.caffeinemc.mods.sodium.client.render.chunk.shader.ChunkShaderOptions;
import net.caffeinemc.mods.sodium.client.render.chunk.terrain.DefaultTerrainRenderPasses;
import net.caffeinemc.mods.sodium.client.render.chunk.terrain.TerrainRenderPass;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(ShaderChunkRenderer.class)
public abstract class MixinShaderChunkRenderer {

    /**
     * @author linusdev
     * @reason added defines for different render passes (Sodium 0.8.7 moved the
     *         constants assembly out of ChunkShaderOptions into this class)
     */
    @Overwrite(remap = false)
    private static ShaderConstants createShaderConstants(ChunkShaderOptions options) {
        ShaderConstants.Builder builder = ShaderConstants.builder();
        builder.addAll(options.fog().getDefines());

        builder.add("SODIUM_CORE_SHADER_SUPPORT");

        if (options.pass().supportsFragmentDiscard()) {
            builder.add("USE_FRAGMENT_DISCARD");
        }

        TerrainRenderPass pass = options.pass();
        if (pass == DefaultTerrainRenderPasses.SOLID) {
            builder.add("RENDER_PASS_SOLID");
        } else if (pass == DefaultTerrainRenderPasses.CUTOUT) {
            builder.add("RENDER_PASS_CUTOUT");
        } else if (pass == DefaultTerrainRenderPasses.TRANSLUCENT) {
            builder.add("RENDER_PASS_TRANSLUCENT");
        }

        builder.add("USE_VERTEX_COMPRESSION");

        return builder.build();
    }
}
