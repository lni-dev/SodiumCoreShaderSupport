package de.linusdev.sodiumcoreshadersupport;


import net.minecraft.server.packs.resources.PreparableReloadListener;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.Unit;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.event.AddClientReloadListenersEvent;
import net.neoforged.neoforge.client.event.RegisterClientReloadListenersEvent;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

import static de.linusdev.sodiumcoreshadersupport.CommonClass.reloadShaders;

@Mod(Constants.MOD_ID)
public class SodiumCoreShaderSupport implements PreparableReloadListener {

    private static SodiumCoreShaderSupport INSTANCE = null;

    public SodiumCoreShaderSupport(IEventBus eventBus) {
        INSTANCE = this;
        CommonClass.init();
        eventBus.addListener(SodiumCoreShaderSupport::onRegisterClientReloadListeners);
    }

    private static void onRegisterClientReloadListeners(AddClientReloadListenersEvent event) {
        event.registerReloadListener(INSTANCE);

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