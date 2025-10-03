package de.linusdev.sodiumcoreshadersupport.mixin;

import net.caffeinemc.mods.sodium.client.gl.shader.uniform.GlUniformFloat;
import net.caffeinemc.mods.sodium.client.render.chunk.shader.ChunkShaderOptions;
import net.caffeinemc.mods.sodium.client.render.chunk.shader.DefaultShaderInterface;
import net.caffeinemc.mods.sodium.client.render.chunk.shader.ShaderBindingContext;
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

    @Inject(at = @At("RETURN"), method = "<init>", remap = false)
    private void injectConstructor(ShaderBindingContext context, ChunkShaderOptions options, CallbackInfo ci) {
        sodiumCoreShaderSupport$uniformGameTime = context.bindUniform("u_GameTime", GlUniformFloat::new);
    }

    @Inject(at = @At("RETURN"), method = "setupState", remap = false)
    private void injectSetupState(CallbackInfo ci) {
        Minecraft minecraft = Minecraft.getInstance();
        long time = minecraft.level == null ? 0L : minecraft.level.getGameTime();
        DeltaTracker deltaTracker = minecraft.getDeltaTracker();
        sodiumCoreShaderSupport$uniformGameTime.set(
                ((float)(time % 24000L) + deltaTracker.getGameTimeDeltaPartialTick(false)) / 24000.0F
        );
    }

}