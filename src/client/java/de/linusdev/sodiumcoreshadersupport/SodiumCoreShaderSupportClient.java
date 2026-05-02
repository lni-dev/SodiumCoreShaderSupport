package de.linusdev.sodiumcoreshadersupport;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import de.linusdev.sodiumcoreshadersupport.mixin.client.MixinPack;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.resource.v1.ResourceLoader;
import net.fabricmc.fabric.api.resource.v1.reloader.SimpleReloadListener;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.SharedConstants;
import net.minecraft.WorldVersion;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.repository.Pack;
import net.minecraft.server.packs.resources.*;
import net.minecraft.util.profiling.ProfilerFiller;
import org.jetbrains.annotations.NotNull;
import org.jspecify.annotations.NonNull;

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

import static de.linusdev.sodiumcoreshadersupport.Constants.RELOAD_LISTENER_ID;

public class SodiumCoreShaderSupportClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        ResourceLoader.get(PackType.CLIENT_RESOURCES).registerReloadListener(
                Identifier.fromNamespaceAndPath(RELOAD_LISTENER_ID.getNamespace(), RELOAD_LISTENER_ID.getPath()),
                new SimplePreparableReloadListener<Void>() {
                    @Override
                    protected Void prepare(@NonNull ResourceManager resourceManager, @NonNull ProfilerFiller profilerFiller) {
                        reloadCullingConfig(resourceManager);
                        reloadShaders(resourceManager);
                        return null;
                    }

                    @Override
                    protected void apply(Void obj, ResourceManager resourceManager, ProfilerFiller profilerFiller) {
                    }
                }
        );
    }

    public static Map<String, Map<String, Resource>> shaders;

    public static void reloadShaders(@NotNull ResourceManager manager) {
        Constants.LOG.info("Loading shaders...");
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
        Constants.LOG.info("Checking resourcepack compatibility to sodium");
        if(!FabricLoader.getInstance().isModLoaded(Constants.SODIUM_MOD_ID)) {
            Constants.LOG.info("Sodium not loaded -> COMPATIBLE");
            return new PackSodiumCompReturn(PackSodiumCompatibility.COMPATIBLE, null, null, null); // sodium is not installed
        }

        WorldVersion currentGameVersion = SharedConstants.getCurrentVersion();
        String sodiumVersion = FabricLoader.getInstance().getModContainer(Constants.SODIUM_MOD_ID).orElseThrow().getMetadata().getVersion().toString();

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
                Constants.LOG.info("Pack does not contain shaders -> COMPATIBLE");
                return new PackSodiumCompReturn(PackSodiumCompatibility.COMPATIBLE, null, null, null);
            }


            // Check if pack has versions info
            IoSupplier<InputStream> streamSup = res.getResource(PackType.CLIENT_RESOURCES, Identifier.fromNamespaceAndPath("sodiumcoreshadersupport", "versions.json"));

            if(streamSup == null) {
                // No info, show warning
                Constants.LOG.info("Pack does not contain a versions.json -> MISSING_INFORMATION");
                return new PackSodiumCompReturn(PackSodiumCompatibility.MISSING_INFORMATION, null, null, null);
            }

            // read json
            try(BufferedReader reader = new BufferedReader(new InputStreamReader(streamSup.get(), StandardCharsets.UTF_8))) {
                JsonElement element = JsonParser.parseReader(reader);
                if (element == null || !element.isJsonObject()) {
                    Constants.LOG.warn("{} has an invalid versions.json: first element must be json object ({...}) -> MALFORMED_INFORMATION", resProfile.getId());
                    return new PackSodiumCompReturn(PackSodiumCompatibility.MALFORMED_INFORMATION, null, null, null);
                }

                element = element.getAsJsonObject().get("supported-versions");

                if (element == null || !element.isJsonObject()) {
                    Constants.LOG.warn("{} has an invalid versions.json: missing 'supported-versions' json element -> MALFORMED_INFORMATION", resProfile.getId());
                    return new PackSodiumCompReturn(PackSodiumCompatibility.MALFORMED_INFORMATION, null, null, null);
                }

                for (Map.Entry<String, JsonElement> entry : element.getAsJsonObject().asMap().entrySet()) {
                    if (!entry.getValue().isJsonArray()) {
                        Constants.LOG.warn("{} has an invalid versions.json: sodium versions must be specified as array -> MALFORMED_INFORMATION", resProfile.getId());
                        return new PackSodiumCompReturn(PackSodiumCompatibility.MALFORMED_INFORMATION, null, null, null);
                    }

                    minecraftVersions.add(entry.getKey());
                    if (entry.getKey().equals(currentGameVersion.name())) {
                        correctMcVersionIndex = mcVersionIndex;
                        for (JsonElement ele : entry.getValue().getAsJsonArray()) {
                            if (!ele.isJsonPrimitive() || !ele.getAsJsonPrimitive().isString()) {
                                Constants.LOG.warn("{} has an invalid versions.json: sodium versions array must contain strings -> MALFORMED_INFORMATION", resProfile.getId());
                                return new PackSodiumCompReturn(PackSodiumCompatibility.MALFORMED_INFORMATION, null, null, null);
                            }

                            sodiumVersions.add(ele.getAsJsonPrimitive().getAsString());
                            if (sodiumVersion.equals(ele.getAsJsonPrimitive().getAsString())) {
                                // match found
                                Constants.LOG.info("pack is COMPATIBLE!");
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

        Constants.LOG.info("No version match found -> NOT_COMPATIBLE!");
        return new PackSodiumCompReturn(PackSodiumCompatibility.NOT_COMPATIBLE, sodiumVersions, minecraftVersions, correctMcVersionIndex); // no match found

    }

    private static final Identifier CULLING_CONFIG_ID = Identifier.fromNamespaceAndPath(Constants.MOD_ID, "versions.json");

    public static void reloadCullingConfig(@NotNull ResourceManager manager) {
        boolean disableFrustum = false;
        boolean disableBackface = false;

        var resourceOpt = manager.getResource(CULLING_CONFIG_ID);
        if (resourceOpt.isPresent()) {
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(resourceOpt.get().open(), StandardCharsets.UTF_8))) {
                JsonElement parsed = JsonParser.parseReader(reader);
                if (parsed != null && parsed.isJsonObject()) {
                    JsonObject obj = parsed.getAsJsonObject();
                    disableFrustum = readBool(obj, "disable-frustum-culling", false);
                    disableBackface = readBool(obj, "disable-backface-culling", false);
                }
            } catch (Exception e) {
                Constants.LOG.warn("Failed to read culling flags from {}: {}", CULLING_CONFIG_ID, e.toString());
            }
        }

        CullingConfig.disableFrustumCulling = disableFrustum;
        CullingConfig.disableBackfaceCulling = disableBackface;
    }

    private static boolean readBool(JsonObject obj, String key, boolean fallback) {
        JsonElement el = obj.get(key);
        if (el != null && el.isJsonPrimitive() && el.getAsJsonPrimitive().isBoolean()) {
            return el.getAsBoolean();
        }
        return fallback;
    }
}