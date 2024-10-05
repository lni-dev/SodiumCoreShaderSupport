package de.linusdev.mixin;

import net.caffeinemc.mods.sodium.client.gl.shader.ShaderConstants;
import net.caffeinemc.mods.sodium.client.render.chunk.shader.ChunkFogMode;
import net.caffeinemc.mods.sodium.client.render.chunk.shader.ChunkShaderOptions;
import net.caffeinemc.mods.sodium.client.render.chunk.terrain.DefaultTerrainRenderPasses;
import net.caffeinemc.mods.sodium.client.render.chunk.terrain.TerrainRenderPass;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(ChunkShaderOptions.class)
public abstract class MixinChunkShaderOptions {

    @Shadow @Final private ChunkFogMode fog;
    @Shadow @Final private TerrainRenderPass pass;

    /**
     * @author linusdev
     * @reason added defines for different render passes
     */
    @Overwrite(remap = false)
    public ShaderConstants constants() {
        ShaderConstants.Builder constants = ShaderConstants.builder();
        constants.addAll(this.fog.getDefines());
        if (this.pass.supportsFragmentDiscard()) {
            constants.add("USE_FRAGMENT_DISCARD");
        }

        if(pass == DefaultTerrainRenderPasses.SOLID)
            constants.add("RENDER_PASS_SOLID");
        else if(pass == DefaultTerrainRenderPasses.CUTOUT)
            constants.add("RENDER_PASS_CUTOUT");
        else if(pass == DefaultTerrainRenderPasses.TRANSLUCENT)
            constants.add("RENDER_PASS_TRANSLUCENT");

        constants.add("USE_VERTEX_COMPRESSION");
        return constants.build();
    }
}
