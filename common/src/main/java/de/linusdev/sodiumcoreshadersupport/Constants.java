package de.linusdev.sodiumcoreshadersupport;

import net.minecraft.resources.ResourceLocation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Constants {

	public static final String MOD_ID = "sodiumcoreshadersupport";
	public static final String MOD_NAME = "Sodium Core Shader Support";
	public static final Logger LOG = LoggerFactory.getLogger(MOD_NAME);

	public static final String SODIUM_MOD_ID = "sodium";
	public static final ResourceLocation RELOAD_LISTENER_ID = ResourceLocation.fromNamespaceAndPath("sodiumcoreshadersupport", "shaderloader");
}