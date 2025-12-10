package de.linusdev.sodiumcoreshadersupport;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.resource.v1.ResourceLoader;
import net.fabricmc.fabric.api.resource.v1.reloader.SimpleResourceReloader;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.PackType;
import org.jetbrains.annotations.NotNull;

import static de.linusdev.sodiumcoreshadersupport.CommonClass.reloadShaders;
import static de.linusdev.sodiumcoreshadersupport.Constants.RELOAD_LISTENER_ID;

public class SodiumCoreShaderSupport implements ClientModInitializer {


    @Override
    public void onInitializeClient() {
        CommonClass.init();

        ResourceLoader.get(PackType.CLIENT_RESOURCES).registerReloader(
                Identifier.fromNamespaceAndPath(RELOAD_LISTENER_ID.getNamespace(), RELOAD_LISTENER_ID.getPath()),
                new SimpleResourceReloader<@NotNull String>() {
                    @Override
                    protected String prepare(@NotNull SharedState store) {
                        reloadShaders(store.resourceManager());
                        return "";
                    }

                    @Override
                    protected void apply(String prepared, @NotNull SharedState store) {

                    }
                }
        );
    }
}
