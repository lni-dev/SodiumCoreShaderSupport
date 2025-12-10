package de.linusdev.sodiumcoreshadersupport.mixin.client;

import net.minecraft.server.packs.repository.Pack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(Pack.class)
public interface MixinPack {

    @Accessor(value = "resources")
    Pack.ResourcesSupplier getResourceSupplier();

}
