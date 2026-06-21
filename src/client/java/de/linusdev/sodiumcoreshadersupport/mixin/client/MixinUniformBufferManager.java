package de.linusdev.sodiumcoreshadersupport.mixin.client;

import com.mojang.blaze3d.buffers.GpuBuffer;
import com.mojang.blaze3d.buffers.GpuBufferSlice;
import com.mojang.blaze3d.buffers.Std140Builder;
import de.linusdev.sodiumcoreshadersupport.GpuBufferExtension;
import net.caffeinemc.mods.sodium.client.render.chunk.ChunkRenderMatrices;
import net.caffeinemc.mods.sodium.client.render.chunk.UniformBufferManager;
import net.caffeinemc.mods.sodium.client.util.FogParameters;
import net.minecraft.client.Camera;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.MappableRingBuffer;
import net.minecraft.util.Mth;
import net.minecraft.world.attribute.EnvironmentAttributeProbe;
import net.minecraft.world.attribute.EnvironmentAttributes;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(UniformBufferManager.class)
public class MixinUniformBufferManager {
    @Shadow
    private boolean hasUpdatedThisFrame;
    @Shadow
    @Final
    private MappableRingBuffer uniformData;
    @Unique
    private MappableRingBuffer sodiumCoreShaderSupport$uniforms;


    @Inject(
            at = @At("RETURN"),
            method = "<init>",
            remap = false
    )
    public void init(ClientLevel level, int renderDistance, CallbackInfo ci) {
        sodiumCoreShaderSupport$uniforms = new MappableRingBuffer(() -> "Sodium Core Shader Support uniform buffer", GpuBuffer.USAGE_UNIFORM | GpuBuffer.USAGE_MAP_WRITE, 256);
    }

    @Inject(
            at = @At("HEAD"),
            method = "update"
    )
    public void update(ChunkRenderMatrices matrices, FogParameters fogParameters, CallbackInfo ci) {

        if(hasUpdatedThisFrame)
            return;

        this.sodiumCoreShaderSupport$uniforms.rotate();

        try (GpuBufferSlice.MappedView data = sodiumCoreShaderSupport$uniforms.currentBuffer().map(false, true)) {
            Std140Builder.intoBuffer(data.data())
                    .putFloat(calculateGameTime())
                    .putFloat(calculateSunAngle())
                    .get();
        }

    }

    @Inject(
            at = @At("RETURN"),
            method = "delete"
    )
    public void delete(CallbackInfo ci) {
        sodiumCoreShaderSupport$uniforms.close();
    }

    /**
     * @author LinusDev
     * @reason inject sodium core shader supports custom uniform buffer
     */
    @Overwrite
    public GpuBuffer getUniformBuffer() {
        GpuBuffer current = this.uniformData.currentBuffer();
        ((GpuBufferExtension) current).sodiumCoreShaderSupport$setUniforms(sodiumCoreShaderSupport$uniforms.currentBuffer());
        return current;
    }

    @Unique
    private static float calculateGameTime() {
        Minecraft minecraft = Minecraft.getInstance();
        long time = minecraft.level == null ? 0L : minecraft.level.getGameTime();
        DeltaTracker deltaTracker = minecraft.getDeltaTracker();
        float partialTick = deltaTracker.getGameTimeDeltaPartialTick(false);

        return ((float)(time % 24000L) + partialTick) / 24000.0F;
    }

    @Unique
    private static float calculateSunAngle() {
        Minecraft minecraft = Minecraft.getInstance();
        DeltaTracker deltaTracker = minecraft.getDeltaTracker();
        float partialTick = deltaTracker.getGameTimeDeltaPartialTick(false);

        float sunAngle = 0.0F;
        Camera camera = minecraft.gameRenderer.mainCamera();
        if(minecraft.level != null && camera != null && camera.isInitialized()) {
            EnvironmentAttributeProbe probe = camera.attributeProbe();
            if(probe != null) {
                sunAngle = probe.getValue(EnvironmentAttributes.SUN_ANGLE, partialTick) * Mth.DEG_TO_RAD;
            }
        }

        return sunAngle;
    }
}
