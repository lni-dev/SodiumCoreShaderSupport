package de.linusdev.sodiumcoreshadersupport;


import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.PreparableReloadListener;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.Unit;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.event.AddClientReloadListenersEvent;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

import static de.linusdev.sodiumcoreshadersupport.CommonClass.reloadShaders;
import static de.linusdev.sodiumcoreshadersupport.Constants.RELOAD_LISTENER_ID;

@Mod(Constants.MOD_ID)
public class SodiumCoreShaderSupport implements PreparableReloadListener {

    private static SodiumCoreShaderSupport INSTANCE = null;

    public SodiumCoreShaderSupport(IEventBus eventBus) {
        INSTANCE = this;
        CommonClass.init();
        eventBus.addListener(SodiumCoreShaderSupport::onRegisterClientReloadListeners);
    }

    private static void onRegisterClientReloadListeners(AddClientReloadListenersEvent event) {
        event.addListener(RELOAD_LISTENER_ID, INSTANCE);
    }

    @Override
    public @NotNull CompletableFuture<Void> reload(
            @NotNull PreparationBarrier barrier,
            @NotNull ResourceManager manager,
            @NotNull Executor backgroundExecutor,
            @NotNull Executor gameExecutor
    ) {
        return barrier.wait(Unit.INSTANCE).thenRunAsync(() -> {
            reloadShaders(manager);
        }, gameExecutor);
    }
}