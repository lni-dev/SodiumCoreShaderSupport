package de.linusdev.sodiumcoreshadersupport;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.fabricmc.fabric.api.resource.SimpleResourceReloadListener;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.profiling.ProfilerFiller;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

import static de.linusdev.sodiumcoreshadersupport.CommonClass.reloadShaders;
import static de.linusdev.sodiumcoreshadersupport.Constants.RELOAD_LISTENER_ID;

public class SodiumCoreShaderSupport implements ClientModInitializer {


    @Override
    public void onInitializeClient() {
        CommonClass.init();

        ResourceManagerHelper
                .get(PackType.CLIENT_RESOURCES)
                .registerReloadListener(
                        new SimpleResourceReloadListener<Void>() {
                            @Override
                            public CompletableFuture<Void> load(ResourceManager manager, ProfilerFiller profiler, Executor executor) {
                                return CompletableFuture.supplyAsync(() -> {
                                    reloadShaders(manager);
                                    return null;
                                }, executor);
                            }

                            @Override
                            public CompletableFuture<Void> apply(Void data, ResourceManager manager, ProfilerFiller profiler, Executor executor) {
                                return CompletableFuture.runAsync(() -> {}, executor);
                            }

                            @Override
                            public ResourceLocation getFabricId() {
                                return RELOAD_LISTENER_ID;
                            }

                        });
    }
}
