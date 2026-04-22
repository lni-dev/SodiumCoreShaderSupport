package de.linusdev.sodiumcoreshadersupport.mixin.client;

import net.caffeinemc.mods.sodium.client.gl.shader.ShaderConstants;
import net.caffeinemc.mods.sodium.client.render.chunk.ShaderChunkRenderer;
import net.caffeinemc.mods.sodium.client.render.chunk.shader.ChunkShaderOptions;
import net.caffeinemc.mods.sodium.client.render.chunk.terrain.DefaultTerrainRenderPasses;
import net.caffeinemc.mods.sodium.client.render.chunk.terrain.TerrainRenderPass;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

/*
 * As of Sodium mc1.21.11-0.8.7, ShaderChunkRenderer#createShader no longer calls
 * ChunkShaderOptions#constants() and instead builds defines through this private
 * static helper. The previous MixinChunkShaderOptions @Overwrite of
 * ChunkShaderOptions#constants() is dead code on this version — we have to
 * intercept here instead so RENDER_PASS_* defines actually reach the compiler.
 */
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
