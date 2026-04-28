package de.linusdev.sodiumcoreshadersupport;

public final class CullingConfig {
    public static volatile boolean disableFrustumCulling = false;
    public static volatile boolean disableBackfaceCulling = false;

    private CullingConfig() {}
}
