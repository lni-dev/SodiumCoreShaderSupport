package de.linusdev.sodiumcoreshadersupport.mixin;

import net.minecraft.server.packs.repository.Pack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(targets = "net.minecraft.client.gui.screens.packs.PackSelectionModel$EntryBase")
public interface MixinAbstractPack {

    @Accessor(value = "pack")
    Pack getPack();

}
