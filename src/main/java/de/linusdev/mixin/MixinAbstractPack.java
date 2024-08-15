package de.linusdev.mixin;

import net.minecraft.resource.ResourcePackProfile;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(targets = "net.minecraft.client.gui.screen.pack.ResourcePackOrganizer$AbstractPack")
public interface MixinAbstractPack {

    @Accessor(value = "profile")
    ResourcePackProfile getProfile();

}
