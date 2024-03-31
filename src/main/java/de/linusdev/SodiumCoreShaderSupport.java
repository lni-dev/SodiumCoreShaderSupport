package de.linusdev;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.ModInitializer;

import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.fabricmc.fabric.api.resource.SimpleResourceReloadListener;
import net.minecraft.resource.Resource;
import net.minecraft.resource.ResourceManager;
import net.minecraft.resource.ResourceType;
import net.minecraft.util.Identifier;
import net.minecraft.util.profiler.Profiler;
import org.apache.commons.io.IOUtils;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.stream.Collectors;

public class SodiumCoreShaderSupport implements ClientModInitializer {

    public static final Logger LOGGER = LoggerFactory.getLogger("sodiumcoreshadersupport");

    public static Map<String, Map<String, Resource>> shaders;

    @Override
    public void onInitializeClient() {
        ResourceManagerHelper
                .get(ResourceType.CLIENT_RESOURCES)
                .registerReloadListener(
                        new SimpleResourceReloadListener<Void>() {

                            @Override
                            public Identifier getFabricId() {
                                return new Identifier("sodium", "shaders");
                            }

                            @Override
                            public CompletableFuture<Void> load(ResourceManager manager, Profiler profiler, Executor executor) {
                                return CompletableFuture.supplyAsync(() -> {
                                    LOGGER.info("Loading shaders...");
                                    shaders = new HashMap<>();

                                    manager.findAllResources("shaders", path -> true).forEach((identifier, resources) -> {
                                        Map<String, Resource> nameSpace = shaders.computeIfAbsent(
                                                identifier.getNamespace(),
                                                k -> new HashMap<>()
                                        );

                                        nameSpace.put(
                                                identifier.getPath().substring("shaders/".length()),
                                                resources.get(resources.size() - 1)
                                        );
                                    });

                                    shaders.forEach((nameSpace, map) -> {
                                        System.out.println("nameSpace: " + nameSpace);
                                        map.forEach((path, resource) -> {
                                            System.out.println("    " + path + ": " + resource.getResourcePackName());
                                        });
                                    });

                                    return null;
                                }, executor);
                            }

                            @Override
                            public CompletableFuture<Void> apply(Void data, ResourceManager manager, Profiler profiler, Executor executor) {
                                return CompletableFuture.runAsync(() -> {
                                }, executor);
                            }
                        });
    }
}