package de.linusdev.mixin;

import net.minecraft.client.gui.screen.pack.PackListWidget;
import net.minecraft.client.gui.screen.pack.PackScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(PackListWidget.class)
public interface MixinPackListWidget {

    @Accessor(value = "screen")
    public PackScreen getScreen();
}
