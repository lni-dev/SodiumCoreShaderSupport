package de.linusdev.sodiumcoreshadersupport;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public record PackSodiumCompReturn(
            @NotNull PackSodiumCompatibility compatibility,
            @Nullable List<String> sodiumVersions,
            @Nullable List<String> minecraftVersions,
            @Nullable Integer correctMcVersionIndex
) { }