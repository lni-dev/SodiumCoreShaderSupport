package de.linusdev.mixin;

import net.minecraft.resource.ResourcePackProfile;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(ResourcePackProfile.class)
public interface MixinResourcePackProfile {

    @Accessor(value = "packFactory")
    ResourcePackProfile.PackFactory getPackFactory();

}
