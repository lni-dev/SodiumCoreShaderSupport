package de.linusdev.sodiumcoreshadersupport.mixin.client;


import net.caffeinemc.mods.sodium.client.gl.shader.GlShader;
import net.caffeinemc.mods.sodium.client.gl.shader.ShaderConstants;
import net.caffeinemc.mods.sodium.client.gl.shader.ShaderLoader;
import net.caffeinemc.mods.sodium.client.gl.shader.ShaderType;
import net.minecraft.resources.Identifier;
import org.apache.commons.io.IOUtils;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import static de.linusdev.sodiumcoreshadersupport.Constants.LOG;
import static de.linusdev.sodiumcoreshadersupport.SodiumCoreShaderSupportClient.shaders;

@Mixin(ShaderLoader.class)
public class MixinShaderLoader {

    @Inject(at = @At("HEAD"), method = "loadShader")
    private static void loadShaderInject(
            ShaderType type, Identifier name, ShaderConstants constants, CallbackInfoReturnable<GlShader> cir
    ) {
        LOG.info("Start loading shader in namespace '"  + name.getNamespace() + "': " + name.getPath());
    }

    /**
     * @author LinusDev
     * @reason Load shaders from resources, loaded by then ResourceManager instead of reading them as java resource.
     */
    @Overwrite
    public static String getShaderSource(Identifier name) {

        if(shaders == null) {
            // fallback to default getShaderSource
            String path = String.format("/assets/%s/shaders/%s", name.getNamespace(), name.getPath());

            try (InputStream in = ShaderLoader.class.getResourceAsStream(path)) {
                if (in == null) {
                    throw new RuntimeException("Shader not found: " + path);
                } else {
                    return IOUtils.toString(in, StandardCharsets.UTF_8);
                }
            } catch (IOException e) {
                throw new RuntimeException("Failed to read shader source for " + path, e);
            }
        }

        var nameSpace = shaders.get(name.getNamespace());

        if(nameSpace == null)
            throw new RuntimeException("No Shaders available for namespace '" + name.getNamespace() + "'");

        var shaderResource = nameSpace.get(name.getPath());

        if(shaderResource == null)
            throw new RuntimeException("No Shader found in namespace '" + name.getNamespace()
                    + "' for shader '" + name.getPath() + "'");

        try {

            //noinspection resource: This would close the pack which is not what we want.
            LOG.info("Loaded Shader '{}:{}' from pack '{}'.", name.getNamespace(), name.getPath(), shaderResource.source().location().title().getString());


            return IOUtils.toString(shaderResource.open(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new RuntimeException("Exception while reading shader source in namespace '" + name.getNamespace()
                    + "' for shader '" + name.getPath() + "'", e);
        }
    }

}