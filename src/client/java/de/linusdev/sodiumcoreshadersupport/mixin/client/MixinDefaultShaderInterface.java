package de.linusdev.sodiumcoreshadersupport.mixin.client;

import com.mojang.blaze3d.textures.GpuSampler;
import net.caffeinemc.mods.sodium.client.gl.shader.uniform.GlUniformFloat;
import net.caffeinemc.mods.sodium.client.render.chunk.shader.ChunkShaderOptions;
import net.caffeinemc.mods.sodium.client.render.chunk.shader.DefaultShaderInterface;
import net.caffeinemc.mods.sodium.client.render.chunk.shader.ShaderBindingContext;
import net.caffeinemc.mods.sodium.client.render.chunk.terrain.TerrainRenderPass;
import net.caffeinemc.mods.sodium.client.util.FogParameters;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(DefaultShaderInterface.class)
public abstract class MixinDefaultShaderInterface {

    @Unique
    private GlUniformFloat sodiumCoreShaderSupport$uniformGameTime = null;

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
    }

    @Inject(at = @At("RETURN"), method = "setupState", remap = false)
    private void injectSetupState(TerrainRenderPass pass, FogParameters parameters, GpuSampler terrainSampler, CallbackInfo ci) {
        Minecraft minecraft = Minecraft.getInstance();
        long time = minecraft.level == null ? 0L : minecraft.level.getGameTime();
        DeltaTracker deltaTracker = minecraft.getDeltaTracker();
        if(sodiumCoreShaderSupport$uniformGameTime != null)
            sodiumCoreShaderSupport$uniformGameTime.set(
                    ((float)(time % 24000L) + deltaTracker.getGameTimeDeltaPartialTick(false)) / 24000.0F
            );
    }

}