package ninja.trek.srd.network;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import ninja.trek.srd.FiveESrdMod;
import ninja.trek.srd.client.ClientEncounterState;
import ninja.trek.srd.client.gui.ActionHotbarOverlay;
import ninja.trek.srd.network.payloads.*;

/**
 * Handles incoming packets from the server on the client side.
 */
public class ClientPacketHandlers {

    /**
     * Register all client-side packet receivers.
     */
    public static void register() {
        FiveESrdMod.LOGGER.info("Registering client packet handlers...");

        // Handle SyncEncounterState packets
        ClientPlayNetworking.registerGlobalReceiver(
            SyncEncounterStatePayload.ID,
            ClientPacketHandlers::handleSyncEncounterState
        );

        // Handle SyncCombatState packets
        ClientPlayNetworking.registerGlobalReceiver(
            SyncCombatStatePayload.ID,
            ClientPacketHandlers::handleSyncCombatState
        );

        // Handle TurnStart packets
        ClientPlayNetworking.registerGlobalReceiver(
            TurnStartPayload.ID,
            ClientPacketHandlers::handleTurnStart
        );

        // Handle TurnEnd packets
        ClientPlayNetworking.registerGlobalReceiver(
            TurnEndPayload.ID,
            ClientPacketHandlers::handleTurnEnd
        );

        // Handle SyncLayerConfig packets (GeckoLib)
        ClientPlayNetworking.registerGlobalReceiver(
            SyncLayerConfigPayload.ID,
            ClientPacketHandlers::handleSyncLayerConfig
        );

        // Handle UpdateLayerConfig packets (GeckoLib)
        ClientPlayNetworking.registerGlobalReceiver(
            UpdateLayerConfigPayload.ID,
            ClientPacketHandlers::handleUpdateLayerConfig
        );

        // Handle PossessCharacter packets
        ClientPlayNetworking.registerGlobalReceiver(
            PossessCharacterPayload.ID,
            ClientPacketHandlers::handlePossessCharacter
        );

        // Handle ReleasePossession packets
        ClientPlayNetworking.registerGlobalReceiver(
            ReleasePossessionPayload.ID,
            ClientPacketHandlers::handleReleasePossession
        );

        FiveESrdMod.LOGGER.info("Client packet handlers registered successfully!");
    }

    /**
     * Handle SyncEncounterState packet from server.
     */
    private static void handleSyncEncounterState(SyncEncounterStatePayload payload, ClientPlayNetworking.Context context) {
        // Execute on client thread
        context.client().execute(() -> {
            FiveESrdMod.LOGGER.info("Received encounter state: {} combatants, round {}, turn {}",
                payload.turnOrder().size(),
                payload.roundNumber(),
                payload.currentTurnIndex() + 1
            );

            // Update client-side encounter state cache
            ClientEncounterState.getInstance().updateEncounter(payload);

            // TODO: Update combat UI to show initiative order (Phase 3)
        });
    }

    /**
     * Handle SyncCombatState packet from server.
     */
    private static void handleSyncCombatState(SyncCombatStatePayload payload, ClientPlayNetworking.Context context) {
        // Execute on client thread
        context.client().execute(() -> {
            FiveESrdMod.LOGGER.info("Received combat state for entity: {}, HP: {}/{}",
                payload.entityId(),
                payload.currentHitPoints(),
                payload.maxHitPoints()
            );

            // Update entity's combat state on client
            ClientEncounterState.getInstance().updateCombatState(
                payload.entityId(),
                payload.currentHitPoints(),
                payload.maxHitPoints(),
                payload.armorClass(),
                payload.hasAction(),
                payload.hasBonusAction(),
                payload.hasReaction(),
                payload.remainingMovement()
            );

            // TODO: Update combat UI to show action availability (Phase 3)
        });
    }

    /**
     * Handle TurnStart packet from server.
     */
    private static void handleTurnStart(TurnStartPayload payload, ClientPlayNetworking.Context context) {
        // Execute on client thread
        context.client().execute(() -> {
            FiveESrdMod.LOGGER.info("Turn started for entity: {}", payload.entityId());

            // Update client-side state
            ClientEncounterState.getInstance().startTurn(payload.encounterId(), payload.entityId());

            // TODO: Highlight current turn entity (Phase 3)
            // TODO: Enable/disable action buttons based on whose turn it is (Phase 3)
            // TODO: Play turn start sound/visual effect (Phase 5)

            ActionHotbarOverlay.notifyTurnStart(payload.encounterId(), payload.entityId());
        });
    }

    /**
     * Handle TurnEnd packet from server.
     */
    private static void handleTurnEnd(TurnEndPayload payload, ClientPlayNetworking.Context context) {
        // Execute on client thread
        context.client().execute(() -> {
            FiveESrdMod.LOGGER.info("Turn ended for entity: {}", payload.entityId());

            // Update client-side state
            ClientEncounterState.getInstance().endTurn(payload.encounterId(), payload.entityId());

            // TODO: Remove highlighting from entity (Phase 3)
            // TODO: Disable action buttons (Phase 3)

            ActionHotbarOverlay.notifyTurnEnd(payload.encounterId(), payload.entityId());
        });
    }

    /**
     * Handle SyncLayerConfig packet from server (GeckoLib Phase 7.4).
     * This performs a full sync of the entity's layer configuration.
     */
    private static void handleSyncLayerConfig(SyncLayerConfigPayload payload, ClientPlayNetworking.Context context) {
        // Execute on client thread
        context.client().execute(() -> {
            FiveESrdMod.LOGGER.debug("Received full layer config sync for entity: {}", payload.entityId());

            // Find the entity in the client world
            net.minecraft.client.world.ClientWorld world = context.client().world;
            if (world == null) {
                FiveESrdMod.LOGGER.warn("Cannot sync layer config: client world is null");
                return;
            }

            // Find entity by UUID
            ninja.trek.srd.character.entity.CharacterEntity character = findCharacterEntity(world, payload.entityId());
            if (character != null) {
                // Apply the configuration to the entity
                payload.applyToConfig(character.getLayerConfiguration());
                FiveESrdMod.LOGGER.debug("Applied full layer config to entity {}", payload.entityId());
            } else {
                FiveESrdMod.LOGGER.debug("Entity {} not found or not a CharacterEntity, config will be applied when entity loads", payload.entityId());
            }
        });
    }

    /**
     * Handle UpdateLayerConfig packet from server (GeckoLib Phase 7.4).
     * This performs an incremental update of a specific field.
     */
    private static void handleUpdateLayerConfig(UpdateLayerConfigPayload payload, ClientPlayNetworking.Context context) {
        // Execute on client thread
        context.client().execute(() -> {
            FiveESrdMod.LOGGER.debug("Received layer config update for entity: {} (type: {})",
                payload.entityId(), payload.updateType());

            // Find the entity in the client world
            net.minecraft.client.world.ClientWorld world = context.client().world;
            if (world == null) {
                FiveESrdMod.LOGGER.warn("Cannot update layer config: client world is null");
                return;
            }

            // Find entity by UUID
            ninja.trek.srd.character.entity.CharacterEntity character = findCharacterEntity(world, payload.entityId());
            if (character != null) {
                // Apply the update to the entity's configuration
                payload.applyToConfig(character.getLayerConfiguration());
                FiveESrdMod.LOGGER.debug("Applied layer config update to entity {}", payload.entityId());
            } else {
                FiveESrdMod.LOGGER.debug("Entity {} not found or not a CharacterEntity", payload.entityId());
            }
        });
    }

    /**
     * Handle PossessCharacter packet from server.
     */
    private static void handlePossessCharacter(PossessCharacterPayload payload, ClientPlayNetworking.Context context) {
        // Execute on client thread
        context.client().execute(() -> {
            FiveESrdMod.LOGGER.info("Possessing character: {}", payload.characterUUID());
            ninja.trek.srd.client.ClientPossessionManager.getInstance().possessCharacter(payload.characterUUID());
        });
    }

    /**
     * Handle ReleasePossession packet from server.
     */
    private static void handleReleasePossession(ReleasePossessionPayload payload, ClientPlayNetworking.Context context) {
        // Execute on client thread
        context.client().execute(() -> {
            FiveESrdMod.LOGGER.info("Releasing possession");
            ninja.trek.srd.client.ClientPossessionManager.getInstance().releasePossession();
        });
    }

    /**
     * Helper method to find a CharacterEntity by UUID in the client world.
     */
    private static ninja.trek.srd.character.entity.CharacterEntity findCharacterEntity(
        net.minecraft.client.world.ClientWorld world,
        java.util.UUID entityId
    ) {
        // Iterate through all entities in the world to find one with matching UUID
        for (net.minecraft.entity.Entity entity : world.getEntities()) {
            if (entity instanceof ninja.trek.srd.character.entity.CharacterEntity character) {
                if (character.getUuid().equals(entityId)) {
                    return character;
                }
            }
        }
        return null;
    }
}
