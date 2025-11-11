package ninja.trek.srd;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import ninja.trek.srd.client.render.CharacterEntityRenderer;
import ninja.trek.srd.registry.ModEntities;

/**
 * Client-side initialization for the 5E SRD mod.
 * Handles rendering registration and client-only setup.
 */
public class FiveESrdModClient implements ClientModInitializer {
	@Override
	public void onInitializeClient() {
		FiveESrdMod.LOGGER.info("Initializing 5E SRD client...");

		// Register entity renderers
		EntityRendererRegistry.register(ModEntities.CHARACTER, CharacterEntityRenderer::new);

		// TODO: Register model loader for character parts
		// TODO: Register HUD overlays for combat UI
		// TODO: Register keybindings for actions

		FiveESrdMod.LOGGER.info("5E SRD client initialized successfully!");
	}
}
