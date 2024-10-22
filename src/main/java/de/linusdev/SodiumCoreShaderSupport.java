package de.linusdev;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import de.linusdev.mixin.MixinResourcePackProfile;
import net.fabricmc.api.ClientModInitializer;

import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.fabricmc.fabric.api.resource.SimpleResourceReloadListener;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;
import net.fabricmc.loader.api.Version;
import net.fabricmc.loader.api.VersionParsingException;
import net.minecraft.GameVersion;
import net.minecraft.SharedConstants;
import net.minecraft.resource.*;
import net.minecraft.util.Identifier;
import net.minecraft.util.profiler.Profiler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.atomic.AtomicBoolean;

public class SodiumCoreShaderSupport implements ClientModInitializer {

    public static final Logger LOGGER = LoggerFactory.getLogger("sodiumcoreshadersupport");
    public static final ModContainer SODIUM = FabricLoader.getInstance().getModContainer("sodium").orElse(null);

    public static Map<String, Map<String, Resource>> shaders;

    @Override
    public void onInitializeClient() {
        ResourceManagerHelper
                .get(ResourceType.CLIENT_RESOURCES)
                .registerReloadListener(
                        new SimpleResourceReloadListener<Void>() {

                            @Override
                            public Identifier getFabricId() {
                                return Identifier.of("sodiumcoreshadersupport", "shaderloader");
                            }

                            @Override
                            public CompletableFuture<Void> load(ResourceManager manager, Executor executor) {
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
                                            System.out.println("    " + path + ": " + resource.getPack().getInfo().title().getString());
                                        });
                                    });

                                    return null;
                                }, executor);
                            }

                            @Override
                            public CompletableFuture<Void> apply(Void unused, ResourceManager resourceManager, Executor executor) {
                                return CompletableFuture.runAsync(() -> {}, executor);
                            }

                        });
    }

    public record PackSodiumCompReturn(
            @NotNull PackSodiumCompatibility compatibility,
            @Nullable List<String> sodiumVersions,
            @Nullable List<String> minecraftVersions,
            @Nullable Integer correctMcVersionIndex
    ) {
    }

    public static PackSodiumCompReturn isResourcePackCompatible(ResourcePackProfile resProfile) {
        if (SODIUM == null)
            return new PackSodiumCompReturn(PackSodiumCompatibility.COMPATIBLE, null, null, null); // sodium is not installed

        GameVersion currentGameVersion = SharedConstants.getGameVersion();
        Version sodiumVersion = SODIUM.getMetadata().getVersion();

        ResourcePackProfile.PackFactory packFactory = ((MixinResourcePackProfile) resProfile).getPackFactory();

        List<String> sodiumVersions = new ArrayList<>();
        List<String> minecraftVersions = new ArrayList<>();
        Integer correctMcVersionIndex = null;
        int mcVersionIndex = 0;
        try (var res = packFactory.open(resProfile.getInfo())) {

            // Check if pack has shaders
            AtomicBoolean hasShaders = new AtomicBoolean(false);
            res.findResources(ResourceType.CLIENT_RESOURCES, "minecraft", "shaders", (identifier, inputStreamInputSupplier) -> {
                hasShaders.set(true);
            });

            if(!hasShaders.get()) // No shaders in the pack, it is compatible
                return new PackSodiumCompReturn(PackSodiumCompatibility.COMPATIBLE, null, null, null);

            // Check if pack has versions info
            InputSupplier<InputStream> streamSup = res.open(ResourceType.CLIENT_RESOURCES, Identifier.of("sodiumcoreshadersupport", "versions.json"));

            if(streamSup == null) // No info, show warning
                return new PackSodiumCompReturn(PackSodiumCompatibility.MISSING_INFORMATION, null, null, null);

            // read json
            try(BufferedReader reader = new BufferedReader(new InputStreamReader(streamSup.get(), StandardCharsets.UTF_8))) {
                JsonElement element = JsonParser.parseReader(reader);
                if (element == null || !element.isJsonObject()) {
                    LOGGER.warn("{} has an invalid versions.json: first element must be json object ({...})", resProfile.getId());
                    return new PackSodiumCompReturn(PackSodiumCompatibility.MALFORMED_INFORMATION, null, null, null);
                }

                element = element.getAsJsonObject().get("supported-versions");

                if (element == null || !element.isJsonObject()) {
                    LOGGER.warn("{} has an invalid versions.json: missing 'supported-versions' json element", resProfile.getId());
                    return new PackSodiumCompReturn(PackSodiumCompatibility.MALFORMED_INFORMATION, null, null, null);
                }

                for (Map.Entry<String, JsonElement> entry : element.getAsJsonObject().asMap().entrySet()) {
                    if (!entry.getValue().isJsonArray()) {
                        LOGGER.warn("{} has an invalid versions.json: sodium versions must be specified as array", resProfile.getId());
                        return new PackSodiumCompReturn(PackSodiumCompatibility.MALFORMED_INFORMATION, null, null, null);
                    }

                    minecraftVersions.add(entry.getKey());
                    if (entry.getKey().equals(currentGameVersion.getName())) {
                        correctMcVersionIndex = mcVersionIndex;
                        for (JsonElement ele : entry.getValue().getAsJsonArray()) {
                            if (!ele.isJsonPrimitive() || !ele.getAsJsonPrimitive().isString()) {
                                LOGGER.warn("{} has an invalid versions.json: sodium versions array mus contain stringsb", resProfile.getId());
                                return new PackSodiumCompReturn(PackSodiumCompatibility.MALFORMED_INFORMATION, null, null, null);
                            }

                            sodiumVersions.add(ele.getAsJsonPrimitive().getAsString());
                            if (sodiumVersion.equals(Version.parse(ele.getAsJsonPrimitive().getAsString()))) {
                                // match found
                                return new PackSodiumCompReturn(PackSodiumCompatibility.COMPATIBLE, null, null, null);
                            }
                        }
                        break;
                    }
                    mcVersionIndex++;
                }
            }
        } catch (IOException | VersionParsingException e) {
            throw new RuntimeException(e);
        }

        return new PackSodiumCompReturn(PackSodiumCompatibility.NOT_COMPATIBLE, sodiumVersions, minecraftVersions, correctMcVersionIndex); // no match found

    }

}