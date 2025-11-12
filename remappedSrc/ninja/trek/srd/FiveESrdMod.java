package ninja.trek.srd;

import net.fabricmc.api.ModInitializer;
import ninja.trek.srd.registry.ModBlocks;
import ninja.trek.srd.registry.ModDataComponents;
import ninja.trek.srd.registry.ModEntities;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Main mod class for the 5E SRD Fabric mod.
 * Implements D&D 5e SRD rules with turn-based combat and modular character rendering.
 */
public class FiveESrdMod implements ModInitializer {
	public static final String MOD_ID = "5e-srd";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	@Override
	public void onInitialize() {
		LOGGER.info("Initializing 5E SRD mod...");

		// Register all custom content
		ModDataComponents.initialize();
		ModBlocks.initialize();
		ModEntities.initialize();
		ninja.trek.srd.registry.ModCreativeTabs.initialize();
		ninja.trek.srd.registry.ModNetworking.initialize();

		// Register server-side packet receivers
		ninja.trek.srd.network.ServerPacketHandlers.register();

		LOGGER.info("5E SRD mod initialized successfully!");
	}
}
