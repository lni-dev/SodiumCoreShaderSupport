package de.linusdev.sodiumcoreshadersupport;

import com.mojang.blaze3d.buffers.GpuBuffer;

public interface GpuBufferExtension {
    void sodiumCoreShaderSupport$setUniforms(GpuBuffer buffer);
    GpuBuffer sodiumCoreShaderSupport$getUniforms();
}