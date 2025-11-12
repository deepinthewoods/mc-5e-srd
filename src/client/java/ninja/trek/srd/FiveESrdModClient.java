package ninja.trek.srd;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.fabricmc.fabric.api.event.player.UseItemCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.util.ActionResult;
import ninja.trek.srd.client.gui.CharacterCreationScreen;
import ninja.trek.srd.client.render.CharacterEntityRenderer;
import ninja.trek.srd.registry.ModEntities;
import ninja.trek.srd.registry.ModItems;

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

		// Register client-side packet receivers
		ninja.trek.srd.network.ClientPacketHandlers.register();

		// Register character creation item use callback
		UseItemCallback.EVENT.register((player, world, hand) -> {
			if (player.getStackInHand(hand).getItem() == ModItems.CHARACTER_CREATION_TOME) {
				// Open character creation screen on client side
				MinecraftClient.getInstance().setScreen(new CharacterCreationScreen());
				return ActionResult.SUCCESS;
			}
			return ActionResult.PASS;
		});

		// TODO: Register model loader for character parts
		// TODO: Register HUD overlays for combat UI
		// TODO: Register keybindings for actions

		FiveESrdMod.LOGGER.info("5E SRD client initialized successfully!");
	}
}
