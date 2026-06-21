package de.linusdev.sodiumcoreshadersupport.mixin.client;

import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.blaze3d.buffers.GpuBuffer;
import com.mojang.blaze3d.systems.RenderPass;
import com.mojang.blaze3d.textures.GpuSampler;
import de.linusdev.sodiumcoreshadersupport.CullingConfig;
import de.linusdev.sodiumcoreshadersupport.GpuBufferExtension;
import net.caffeinemc.mods.sodium.client.render.chunk.ChunkRenderMatrices;
import net.caffeinemc.mods.sodium.client.render.chunk.DefaultChunkRenderer;
import net.caffeinemc.mods.sodium.client.render.chunk.lists.ChunkRenderListIterable;
import net.caffeinemc.mods.sodium.client.render.chunk.terrain.TerrainRenderPass;
import net.caffeinemc.mods.sodium.client.render.viewport.CameraTransform;
import net.caffeinemc.mods.sodium.client.util.FogParameters;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = DefaultChunkRenderer.class, remap = false)
public class MixinDefaultChunkRenderer {

    @ModifyVariable(method = "fillCommandBuffer", at = @At("HEAD"), argsOnly = true, ordinal = 0)
    private static boolean shadowculling$disableBlockFaceCulling(boolean useBlockFaceCulling) {
        return !CullingConfig.disableBackfaceCulling && useBlockFaceCulling;
    }

    @Inject(
            at = @At(value = "INVOKE", ordinal = 0, target = "Lcom/mojang/blaze3d/systems/RenderPass;setUniform(Ljava/lang/String;Lcom/mojang/blaze3d/buffers/GpuBuffer;)V"),
            method = "render"
    )
    public void render(
            ChunkRenderMatrices matrices,
            ChunkRenderListIterable renderLists,
            TerrainRenderPass renderPass,
            CameraTransform camera,
            FogParameters parameters,
            boolean indexedRenderingEnabled,
            GpuSampler terrainSampler,
            GpuBuffer uniformData,
            GpuBuffer sectionTimeInfo,
            CallbackInfo ci,
            @Local(name = "pass") RenderPass pass
    ) {
        pass.setUniform("u_SCCS", ((GpuBufferExtension)uniformData).sodiumCoreShaderSupport$getUniforms());
    }
}
