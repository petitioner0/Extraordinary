package com.petitoner0.extraordinary;

import com.petitoner0.extraordinary.finalritual.chokmah.Chokmah;
import com.petitoner0.extraordinary.finalritual.chokmah.ChokmahCameraPayload;
import com.petitoner0.extraordinary.finalritual.chokmah.ChokmahCameraStopPayload;
import com.petitoner0.extraordinary.finalritual.chokmah.ChokmahCommand;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;

import net.minecraft.resources.Identifier;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Extraordinary implements ModInitializer {
	public static final String MOD_ID = "extraordinary";

	// This logger is used to write text to the console and the log file.
	// It is considered best practice to use your mod id as the logger's name.
	// That way, it's clear which mod wrote info, warnings, and errors.
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	@Override
	public void onInitialize() {
		PayloadTypeRegistry.clientboundPlay().register(ChokmahCameraPayload.TYPE, ChokmahCameraPayload.CODEC);
		PayloadTypeRegistry.clientboundPlay().register(ChokmahCameraStopPayload.TYPE, ChokmahCameraStopPayload.CODEC);
		Chokmah.register();
		ChokmahCommand.register();
		// This code runs as soon as Minecraft is in a mod-load-ready state.
		// However, some things (like resources) may still be uninitialized.
		// Proceed with mild caution.

		LOGGER.info("Hello Fabric world!");
	}

	public static Identifier id(String path) {
		return Identifier.fromNamespaceAndPath(MOD_ID, path);
	}
}
