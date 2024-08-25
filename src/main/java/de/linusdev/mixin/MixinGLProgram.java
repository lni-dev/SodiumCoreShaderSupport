package de.linusdev.mixin;

import net.caffeinemc.mods.sodium.client.gl.GlObject;
import net.caffeinemc.mods.sodium.client.gl.shader.uniform.GlUniform;
import org.apache.logging.log4j.Logger;
import org.lwjgl.opengl.GL20C;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.function.IntFunction;

@Mixin(net.caffeinemc.mods.sodium.client.gl.shader.GlProgram.class)
public abstract class MixinGLProgram extends GlObject {

    @Accessor(value = "LOGGER", remap = false)
    abstract Logger getLOGGER();

    /**
     * @author LinusDev
     * @reason Making binding an inactive uniform not a runtime exception.
     * 
     */
    @Overwrite(remap = false)
    public <U extends GlUniform<?>> U bindUniform(String name, IntFunction<U> factory) {
        int index = GL20C.glGetUniformLocation(this.handle(), name);

        if(index < 0) {
            int error = GL20C.glGetError();
            if (error == GL20C.GL_INVALID_OPERATION)
                getLOGGER().warn("Error while binding uniform: GL_INVALID_OPERATION");
            else if (error == GL20C.GL_INVALID_VALUE)
                getLOGGER().warn("Error while binding uniform: GL_INVALID_VALUE");
            else
                getLOGGER().warn("Error while binding uniform: Unknown Error, code: " + error);
        }

        return factory.apply(index);
    }
}