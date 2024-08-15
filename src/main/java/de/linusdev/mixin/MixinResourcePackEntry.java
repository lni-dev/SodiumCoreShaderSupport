package de.linusdev.mixin;

import com.google.common.collect.ImmutableList;
import de.linusdev.SodiumCoreShaderSupport;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ConfirmScreen;
import net.minecraft.client.gui.screen.DialogScreen;
import net.minecraft.client.gui.screen.pack.PackListWidget;
import net.minecraft.client.gui.screen.pack.ResourcePackOrganizer;
import net.minecraft.resource.ResourcePackProfile;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Mixin(PackListWidget.ResourcePackEntry.class)
public abstract class MixinResourcePackEntry {


    @Shadow @Final private ResourcePackOrganizer.Pack pack;
    @Shadow @Final protected MinecraftClient client;
    @Shadow @Final private PackListWidget widget;

    @Inject(at = @At("HEAD"), method = "enable", cancellable = true)
    private void enable(CallbackInfoReturnable<Boolean> cir) {

        ResourcePackProfile resProfile = ((MixinAbstractPack) pack).getProfile();

        var ret = SodiumCoreShaderSupport.isResourcePackCompatible(resProfile);
        switch (ret.compatibility()) {
            case COMPATIBLE -> {
                // Do nothing, pack can be enabled.
            }
            case NOT_COMPATIBLE -> {
                String title = "Try these minecraft versions:";
                assert ret.minecraftVersions() != null;
                String msg = ret.minecraftVersions().stream().reduce((string, string2) -> string + ", " + string2).orElse("none");

                if(ret.correctMcVersionIndex() != null) {
                    title = "Try these sodium versions:";
                    assert ret.sodiumVersions() != null;
                    var sMsg = ret.sodiumVersions().stream().reduce((string, string2) -> string + ", " + string2);
                    msg = sMsg.orElseGet(() ->
                            ret.minecraftVersions().stream()
                                    .filter(string -> !string.equals(ret.minecraftVersions().get(ret.correctMcVersionIndex())))
                                    .reduce((string, string2) -> string + ", " + string2)
                                    .orElse("none")
                    );
                }

                this.client.setScreen(new ConfirmScreen(
                        t -> this.client.setScreen(((MixinPackListWidget) this.widget).getScreen()),
                        Text.of("Resourcepack not compatible with current sodium or minecraft version."),
                        Text.of(title + " " + msg), Text.of("OK"), Text.of("OK")
                ));

                cir.setReturnValue(false);
                cir.cancel();
            }
            case MISSING_INFORMATION -> {
                this.client.setScreen(new ConfirmScreen(
                        confirmed -> {
                            this.client.setScreen(((MixinPackListWidget) this.widget).getScreen());
                            if (confirmed) {
                                this.pack.enable();
                            }
                        },
                        Text.of("Warning"),
                        Text.of("This resourcepack overwrites shaders, but does not specify compatibility with sodium core shaders. It is most likely not compatible. Do you want to enable it anyway?")

                ));

                cir.setReturnValue(false);
                cir.cancel();
            }
            case MALFORMED_INFORMATION -> {
                this.client.setScreen(new ConfirmScreen(
                        confirmed -> {
                            this.client.setScreen(((MixinPackListWidget) this.widget).getScreen());
                            if (confirmed) {
                                this.pack.enable();
                            }
                        },
                        Text.of("Warning"),
                        Text.of("This resourcepack contains malformed information about its compatibility (see log). Do you want to enable it anyway?")
                ));

                cir.setReturnValue(false);
                cir.cancel();
            }
        }
    }


}
