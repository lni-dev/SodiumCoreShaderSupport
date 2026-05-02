package de.linusdev.sodiumcoreshadersupport.mixin.client;

import de.linusdev.sodiumcoreshadersupport.CullingConfig;
import net.caffeinemc.mods.sodium.client.render.chunk.DefaultChunkRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(value = DefaultChunkRenderer.class, remap = false)
public class MixinDefaultChunkRenderer {

    @ModifyVariable(method = "fillCommandBuffer", at = @At("HEAD"), argsOnly = true, ordinal = 0)
    private static boolean shadowculling$disableBlockFaceCulling(boolean useBlockFaceCulling) {
        return !CullingConfig.disableBackfaceCulling && useBlockFaceCulling;
    }
}
