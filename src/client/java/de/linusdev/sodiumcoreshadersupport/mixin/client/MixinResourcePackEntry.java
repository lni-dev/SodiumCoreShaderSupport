package de.linusdev.sodiumcoreshadersupport.mixin.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.ConfirmScreen;
import net.minecraft.client.gui.screens.packs.PackSelectionModel;
import net.minecraft.client.gui.screens.packs.TransferableSelectionList;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.server.packs.repository.Pack;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static de.linusdev.sodiumcoreshadersupport.SodiumCoreShaderSupportClient.isResourcePackCompatible;


@Mixin(TransferableSelectionList.PackEntry.class)
public abstract class MixinResourcePackEntry {


    @Shadow @Final private PackSelectionModel.Entry pack;
    @Shadow @Final protected Minecraft minecraft;
    @Shadow @Final private TransferableSelectionList parent;

    @Inject(at = @At("HEAD"), method = "handlePackSelection", cancellable = true)
    private void enable(CallbackInfo ci) {

        Pack resProfile = ((MixinAbstractPack) pack).getPack();

        var ret = isResourcePackCompatible(resProfile);
        switch (ret.compatibility()) {
            case COMPATIBLE -> {
                // Do nothing, pack can be enabled.
            }
            case NOT_COMPATIBLE -> {
                String title = "Compatible minecraft versions:";
                assert ret.minecraftVersions() != null;
                String msg = ret.minecraftVersions().stream().reduce((string, string2) -> string + ", " + string2).orElse("none");

                if(ret.correctMcVersionIndex() != null) {
                    title = "Compatible sodium versions for current minecraft version:";
                    assert ret.sodiumVersions() != null;
                    var sMsg = ret.sodiumVersions().stream().reduce((string, string2) -> string + ", " + string2);
                    msg = sMsg.orElseGet(() ->
                            ret.minecraftVersions().stream()
                                    .filter(string -> !string.equals(ret.minecraftVersions().get(ret.correctMcVersionIndex())))
                                    .reduce((string, string2) -> string + ", " + string2)
                                    .orElse("none")
                    );
                }

                this.minecraft.setScreenAndShow(new ConfirmScreen(
                        confirmed -> {
                            this.minecraft.setScreenAndShow(((MixinPackListWidget) this.parent).getScreen());
                            if (confirmed) {
                                this.pack.select();
                            }
                        },
                        Component.nullToEmpty("Resourcepack not compatible with current sodium or minecraft version."),
                        Component.nullToEmpty(title + " " + msg),
                        Component.literal("Enable Anyway").setStyle(Style.EMPTY.withColor(0xff0000)),
                        Component.nullToEmpty("OK")
                ));

                ci.cancel();
            }
            case MISSING_INFORMATION -> {
                this.minecraft.setScreenAndShow(new ConfirmScreen(
                        confirmed -> {
                            this.minecraft.setScreenAndShow(((MixinPackListWidget) this.parent).getScreen());
                            if (confirmed) {
                                this.pack.select();
                            }
                        },
                        Component.nullToEmpty("Warning"),
                        Component.nullToEmpty("This resourcepack overwrites shaders, but does not specify compatibility with sodium core shaders. It is most likely not compatible. Do you want to enable it anyway?")

                ));

                ci.cancel();
            }
            case MALFORMED_INFORMATION -> {
                this.minecraft.setScreenAndShow(new ConfirmScreen(
                        confirmed -> {
                            this.minecraft.setScreenAndShow(((MixinPackListWidget) this.parent).getScreen());
                            if (confirmed) {
                                this.pack.select();
                            }
                        },
                        Component.nullToEmpty("Warning"),
                        Component.nullToEmpty("This resourcepack contains malformed information about its compatibility (see log). Do you want to enable it anyway?")
                ));

                ci.cancel();
            }
        }
    }


}
