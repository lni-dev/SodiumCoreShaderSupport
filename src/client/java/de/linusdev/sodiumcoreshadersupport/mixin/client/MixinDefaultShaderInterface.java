package de.linusdev.sodiumcoreshadersupport.mixin.client;

import com.mojang.blaze3d.textures.GpuSampler;
import net.caffeinemc.mods.sodium.client.gl.shader.uniform.GlUniformFloat;
import net.caffeinemc.mods.sodium.client.render.chunk.shader.ChunkShaderOptions;
import net.caffeinemc.mods.sodium.client.render.chunk.shader.DefaultShaderInterface;
import net.caffeinemc.mods.sodium.client.render.chunk.shader.ShaderBindingContext;
import net.caffeinemc.mods.sodium.client.render.chunk.terrain.TerrainRenderPass;
import net.caffeinemc.mods.sodium.client.util.FogParameters;
import net.minecraft.client.Camera;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.world.attribute.EnvironmentAttributeProbe;
import net.minecraft.world.attribute.EnvironmentAttributes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(DefaultShaderInterface.class)
public abstract class MixinDefaultShaderInterface {

    @Unique
    private GlUniformFloat sodiumCoreShaderSupport$uniformGameTime = null;

    @Unique
    private GlUniformFloat sodiumCoreShaderSupport$uniformSunAngle = null;

    @Inject(
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/caffeinemc/mods/sodium/client/render/chunk/shader/ShaderBindingContext;bindUniform(Ljava/lang/String;Ljava/util/function/IntFunction;)Lnet/caffeinemc/mods/sodium/client/gl/shader/uniform/GlUniform;",
                    shift = At.Shift.AFTER
            ),
            method = "<init>",
            remap = false
    )
    private void injectConstructor(ShaderBindingContext context, ChunkShaderOptions options, CallbackInfo ci) {
        sodiumCoreShaderSupport$uniformGameTime = context.bindUniformOptional("u_GameTime", GlUniformFloat::new);
        sodiumCoreShaderSupport$uniformSunAngle = context.bindUniformOptional("u_SunAngle", GlUniformFloat::new);
    }

    @Inject(at = @At("RETURN"), method = "setupState", remap = false)
    private void injectSetupState(TerrainRenderPass pass, FogParameters parameters, GpuSampler terrainSampler, CallbackInfo ci) {
        Minecraft minecraft = Minecraft.getInstance();
        long time = minecraft.level == null ? 0L : minecraft.level.getGameTime();
        DeltaTracker deltaTracker = minecraft.getDeltaTracker();
        float partialTick = deltaTracker.getGameTimeDeltaPartialTick(false);
        if(sodiumCoreShaderSupport$uniformGameTime != null)
            sodiumCoreShaderSupport$uniformGameTime.set(
                    ((float)(time % 24000L) + partialTick) / 24000.0F
            );
        if(sodiumCoreShaderSupport$uniformSunAngle != null) {
            float sunAngle = 0.0F;
            Camera camera = minecraft.gameRenderer.getMainCamera();
            if(minecraft.level != null && camera != null && camera.isInitialized()) {
                EnvironmentAttributeProbe probe = camera.attributeProbe();
                if(probe != null) {
                    sunAngle = probe.getValue(EnvironmentAttributes.SUN_ANGLE, partialTick) * 0.017453292F;
                }
            }
            sodiumCoreShaderSupport$uniformSunAngle.set(sunAngle);
        }
    }

}