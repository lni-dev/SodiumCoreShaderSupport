package de.linusdev.sodiumcoreshadersupport;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import de.linusdev.sodiumcoreshadersupport.mixin.MixinPack;
import de.linusdev.sodiumcoreshadersupport.platform.Services;
import net.minecraft.SharedConstants;
import net.minecraft.WorldVersion;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.repository.Pack;
import net.minecraft.server.packs.resources.IoSupplier;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.world.item.Items;
import org.jetbrains.annotations.NotNull;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

import static de.linusdev.sodiumcoreshadersupport.Constants.LOG;

// This class is part of the common project meaning it is shared between all supported loaders. Code written here can only
// import and access the vanilla codebase, libraries used by vanilla, and optionally third party libraries that provide
// common compatible binaries. This means common code can not directly use loader specific concepts such as Forge events
// however it will be compatible with all supported mod loaders.
public class CommonClass {

    // The loader specific projects are able to import and use any code from the common project. This allows you to
    // write the majority of your code here and load it from your loader specific projects. This example has some
    // code that gets invoked by the entry point of the loader specific projects.
    public static void init() {

        LOG.info("Hello from Common init on {}! we are currently in a {} environment!", Services.PLATFORM.getPlatformName(), Services.PLATFORM.getEnvironmentName());
        LOG.info("The ID for diamonds is {}", BuiltInRegistries.ITEM.getKey(Items.DIAMOND));

        // It is common for all supported loaders to provide a similar feature that can not be used directly in the
        // common code. A popular way to get around this is using Java's built-in service loader feature to create
        // your own abstraction layer. You can learn more about this in our provided services class. In this example
        // we have an interface in the common code and use a loader specific implementation to delegate our call to
        // the platform specific approach.
        if (Services.PLATFORM.isModLoaded("sodium")) {

            LOG.info("Sodium version " + Services.PLATFORM.getModVersion("sodium"));
        }
    }

    public static Map<String, Map<String, Resource>> shaders;

    public static void reloadShaders(@NotNull ResourceManager manager) {
        LOG.info("Loading shaders...");
        shaders = new HashMap<>();

        manager.listResourceStacks("shaders", path -> true).forEach((identifier, resources) -> {
            Map<String, Resource> nameSpace = shaders.computeIfAbsent(
                    identifier.getNamespace(),
                    k -> new HashMap<>()
            );

            nameSpace.put(
                    identifier.getPath().substring("shaders/".length()),
                    resources.getLast()
            );
        });

        shaders.forEach((nameSpace, map) -> {
            System.out.println("nameSpace: " + nameSpace);
            map.forEach((path, resource) -> {
                //noinspection resource: This would close the pack which is not what we want.
                System.out.println("    " + path + ": " + resource.source().location().title().getString());
            });
        });
    }

    public static PackSodiumCompReturn isResourcePackCompatible(Pack resProfile) {
        LOG.info("Checking resourcepack compatibility to sodium");
        if(!Services.PLATFORM.isModLoaded(Constants.SODIUM_MOD_ID)) {
            LOG.info("Sodium not loaded -> COMPATIBLE");
            return new PackSodiumCompReturn(PackSodiumCompatibility.COMPATIBLE, null, null, null); // sodium is not installed
        }

        WorldVersion currentGameVersion = SharedConstants.getCurrentVersion();
        String sodiumVersion = Services.PLATFORM.getModVersion(Constants.SODIUM_MOD_ID);

        var resourceSupplier = ((MixinPack) resProfile).getResourceSupplier();

        List<String> sodiumVersions = new ArrayList<>();
        List<String> minecraftVersions = new ArrayList<>();
        Integer correctMcVersionIndex = null;
        int mcVersionIndex = 0;
        try (var res = resourceSupplier.openPrimary(resProfile.location())) {

            // Check if pack has shaders
            AtomicBoolean hasShaders = new AtomicBoolean(false);
            res.listResources(PackType.CLIENT_RESOURCES, "minecraft", "shaders", (identifier, inputStreamInputSupplier) -> {
                hasShaders.set(true);
            });

            if(!hasShaders.get()) {
                // No shaders in the pack, it is compatible
                LOG.info("Pack does not contain shaders -> COMPATIBLE");
                return new PackSodiumCompReturn(PackSodiumCompatibility.COMPATIBLE, null, null, null);
            }


            // Check if pack has versions info
            IoSupplier<InputStream> streamSup = res.getResource(PackType.CLIENT_RESOURCES, ResourceLocation.fromNamespaceAndPath("sodiumcoreshadersupport", "versions.json"));

            if(streamSup == null) {
                // No info, show warning
                LOG.info("Pack does not contain a versions.json -> MISSING_INFORMATION");
                return new PackSodiumCompReturn(PackSodiumCompatibility.MISSING_INFORMATION, null, null, null);
            }

            // read json
            try(BufferedReader reader = new BufferedReader(new InputStreamReader(streamSup.get(), StandardCharsets.UTF_8))) {
                JsonElement element = JsonParser.parseReader(reader);
                if (element == null || !element.isJsonObject()) {
                    LOG.warn("{} has an invalid versions.json: first element must be json object ({...}) -> MALFORMED_INFORMATION", resProfile.getId());
                    return new PackSodiumCompReturn(PackSodiumCompatibility.MALFORMED_INFORMATION, null, null, null);
                }

                element = element.getAsJsonObject().get("supported-versions");

                if (element == null || !element.isJsonObject()) {
                    LOG.warn("{} has an invalid versions.json: missing 'supported-versions' json element -> MALFORMED_INFORMATION", resProfile.getId());
                    return new PackSodiumCompReturn(PackSodiumCompatibility.MALFORMED_INFORMATION, null, null, null);
                }

                for (Map.Entry<String, JsonElement> entry : element.getAsJsonObject().asMap().entrySet()) {
                    if (!entry.getValue().isJsonArray()) {
                        LOG.warn("{} has an invalid versions.json: sodium versions must be specified as array -> MALFORMED_INFORMATION", resProfile.getId());
                        return new PackSodiumCompReturn(PackSodiumCompatibility.MALFORMED_INFORMATION, null, null, null);
                    }

                    minecraftVersions.add(entry.getKey());
                    if (entry.getKey().equals(currentGameVersion.name())) {
                        correctMcVersionIndex = mcVersionIndex;
                        for (JsonElement ele : entry.getValue().getAsJsonArray()) {
                            if (!ele.isJsonPrimitive() || !ele.getAsJsonPrimitive().isString()) {
                                LOG.warn("{} has an invalid versions.json: sodium versions array must contain strings -> MALFORMED_INFORMATION", resProfile.getId());
                                return new PackSodiumCompReturn(PackSodiumCompatibility.MALFORMED_INFORMATION, null, null, null);
                            }

                            sodiumVersions.add(ele.getAsJsonPrimitive().getAsString());
                            if (sodiumVersion.equals(ele.getAsJsonPrimitive().getAsString())) {
                                // match found
                                LOG.info("pack is COMPATIBLE!");
                                return new PackSodiumCompReturn(PackSodiumCompatibility.COMPATIBLE, null, null, null);
                            }
                        }
                        break;
                    }
                    mcVersionIndex++;
                }
            }

        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        LOG.info("No version match found -> NOT_COMPATIBLE!");
        return new PackSodiumCompReturn(PackSodiumCompatibility.NOT_COMPATIBLE, sodiumVersions, minecraftVersions, correctMcVersionIndex); // no match found

    }
}