package ninja.trek.srd.registry;

import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import ninja.trek.srd.FiveESrdMod;
import ninja.trek.srd.network.payloads.*;

/**
 * Registers all custom network packets (payloads) for the mod.
 * Must be initialized on both client and server sides.
 */
public class ModNetworking {

    /**
     * Initialize all packet type registrations.
     * This must be called during mod initialization.
     */
    public static void initialize() {
        FiveESrdMod.LOGGER.info("Registering network payloads...");

        // Register Server-to-Client payloads
        registerS2CPayloads();

        // Register Client-to-Server payloads
        registerC2SPayloads();

        FiveESrdMod.LOGGER.info("Network payloads registered successfully!");
    }

    /**
     * Register all Server-to-Client payloads.
     */
    private static void registerS2CPayloads() {
        PayloadTypeRegistry.playS2C().register(
            SyncEncounterStatePayload.ID,
            SyncEncounterStatePayload.CODEC
        );

        PayloadTypeRegistry.playS2C().register(
            SyncCombatStatePayload.ID,
            SyncCombatStatePayload.CODEC
        );

        PayloadTypeRegistry.playS2C().register(
            TurnStartPayload.ID,
            TurnStartPayload.CODEC
        );

        PayloadTypeRegistry.playS2C().register(
            TurnEndPayload.ID,
            TurnEndPayload.CODEC
        );
    }

    /**
     * Register all Client-to-Server payloads.
     */
    private static void registerC2SPayloads() {
        PayloadTypeRegistry.playC2S().register(
            UseActionPayload.ID,
            UseActionPayload.CODEC
        );

        PayloadTypeRegistry.playC2S().register(
            EndTurnPayload.ID,
            EndTurnPayload.CODEC
        );

        PayloadTypeRegistry.playC2S().register(
            CreateCharacterPayload.ID,
            CreateCharacterPayload.CODEC
        );
    }
}
