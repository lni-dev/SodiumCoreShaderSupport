package de.linusdev.sodiumcoreshadersupport.mixin.client;

import com.mojang.blaze3d.buffers.GpuBuffer;
import de.linusdev.sodiumcoreshadersupport.GpuBufferExtension;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(GpuBuffer.class)
public abstract class MixinGpuBuffer implements GpuBufferExtension {

    @Unique
    private GpuBuffer sodiumCoreShaderSupport$uniforms;

    @Override
    public void sodiumCoreShaderSupport$setUniforms(GpuBuffer buffer) {
        this.sodiumCoreShaderSupport$uniforms = buffer;
    }

    @Override
    public GpuBuffer sodiumCoreShaderSupport$getUniforms() {
        return this.sodiumCoreShaderSupport$uniforms;
    }
}