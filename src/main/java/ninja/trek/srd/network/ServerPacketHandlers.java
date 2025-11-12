package ninja.trek.srd.network;

import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.entity.Entity;
import ninja.trek.srd.FiveESrdMod;
import ninja.trek.srd.character.entity.CharacterEntity;
import ninja.trek.srd.combat.CombatState;
import ninja.trek.srd.combat.EncounterManager;
import ninja.trek.srd.combat.EncounterState;
import ninja.trek.srd.network.payloads.CreateCharacterPayload;
import ninja.trek.srd.network.payloads.EndTurnPayload;
import ninja.trek.srd.network.payloads.SyncCombatStatePayload;
import ninja.trek.srd.network.payloads.TurnEndPayload;
import ninja.trek.srd.network.payloads.TurnStartPayload;
import ninja.trek.srd.network.payloads.UseActionPayload;

import java.util.UUID;

/**
 * Handles incoming packets from clients on the server side.
 */
public class ServerPacketHandlers {

    /**
     * Register all server-side packet receivers.
     */
    public static void register() {
        FiveESrdMod.LOGGER.info("Registering server packet handlers...");

        // Handle UseAction packets
        ServerPlayNetworking.registerGlobalReceiver(UseActionPayload.ID, ServerPacketHandlers::handleUseAction);

        // Handle EndTurn packets
        ServerPlayNetworking.registerGlobalReceiver(EndTurnPayload.ID, ServerPacketHandlers::handleEndTurn);

        // Handle CreateCharacter packets
        ServerPlayNetworking.registerGlobalReceiver(CreateCharacterPayload.ID, ServerPacketHandlers::handleCreateCharacter);

        FiveESrdMod.LOGGER.info("Server packet handlers registered successfully!");
    }

    /**
     * Handle UseAction packet from client.
     */
    private static void handleUseAction(UseActionPayload payload, ServerPlayNetworking.Context context) {
        ServerPlayerEntity player = context.player();

        // Execute on server thread
        context.server().execute(() -> {
            // Validate that it's the player's turn
            EncounterState encounter = EncounterManager.getInstance(context.server()).getEncounterForEntity(player.getUuid());
            if (encounter == null) {
                FiveESrdMod.LOGGER.warn("Player {} tried to use action but is not in combat", player.getName().getString());
                return;
            }

            UUID currentTurnEntity = encounter.getCurrentTurnEntity();
            if (currentTurnEntity == null || !currentTurnEntity.equals(player.getUuid())) {
                FiveESrdMod.LOGGER.warn("Player {} tried to use action but it's not their turn", player.getName().getString());
                return;
            }

            // TODO: Implement specific action logic based on payload.actionType()
            // For now, just log the action
            FiveESrdMod.LOGGER.info("Player {} used action: {}", player.getName().getString(), payload.actionType());

            // TODO: Update combat state and sync to clients
            // This will be implemented when we add the full action system
        });
    }

    /**
     * Handle EndTurn packet from client.
     */
    private static void handleEndTurn(EndTurnPayload payload, ServerPlayNetworking.Context context) {
        ServerPlayerEntity player = context.player();

        // Execute on server thread
        context.server().execute(() -> {
            EncounterManager manager = EncounterManager.getInstance(context.server());
            EncounterState encounter = manager.getEncounterForEntity(player.getUuid());

            if (encounter == null) {
                FiveESrdMod.LOGGER.warn("Player {} tried to end turn but is not in combat", player.getName().getString());
                return;
            }

            UUID currentTurnEntity = encounter.getCurrentTurnEntity();
            if (currentTurnEntity == null || !currentTurnEntity.equals(player.getUuid())) {
                FiveESrdMod.LOGGER.warn("Player {} tried to end turn but it's not their turn", player.getName().getString());
                return;
            }

            FiveESrdMod.LOGGER.info("Player {} ended their turn", player.getName().getString());

            // Send turn end notification
            TurnEndPayload turnEndPayload = new TurnEndPayload(encounter.getEncounterId(), player.getUuid());
            manager.broadcastToEncounter(context.server(), encounter.getEncounterId(), turnEndPayload);

            // Advance to next turn
            manager.advanceTurn(context.server(), encounter.getEncounterId());

            // Send turn start notification for next entity
            UUID nextEntityId = encounter.getCurrentTurnEntity();
            if (nextEntityId != null) {
                TurnStartPayload turnStartPayload = new TurnStartPayload(encounter.getEncounterId(), nextEntityId);
                manager.broadcastToEncounter(context.server(), encounter.getEncounterId(), turnStartPayload);

                // Handle AI turns if next entity is an NPC
                Entity nextEntity = context.server().getOverworld().getEntity(nextEntityId);
                if (nextEntity instanceof CharacterEntity character && !character.isPlayerControlled()) {
                    // Schedule AI turn to execute after a short delay
                    context.server().execute(() -> {
                        character.executeAITurn(context.server(), encounter);
                    });
                }
            }
        });
    }

    /**
     * Handle CreateCharacter packet from client.
     */
    private static void handleCreateCharacter(CreateCharacterPayload payload, ServerPlayNetworking.Context context) {
        ServerPlayerEntity player = context.player();

        // Execute on server thread
        context.server().execute(() -> {
            FiveESrdMod.LOGGER.info("Creating character '{}' for player {}", payload.name(), player.getName().getString());

            // Create character entity in the player's world
            CharacterEntity character = new CharacterEntity(
                ninja.trek.srd.registry.ModEntities.CHARACTER,
                player.getEntityWorld()
            );

            // Initialize character data
            character.initializeCharacter(
                payload.race(),
                payload.characterClass(),
                payload.stats(),
                payload.appearance()
            );

            // Set as player-controlled
            character.setPlayerControlled(true);

            // Set custom name
            character.setCustomName(net.minecraft.text.Text.literal(payload.name()));
            character.setCustomNameVisible(true);

            // Position the character near the player
            double offsetX = player.getX() + 2.0;
            double offsetZ = player.getZ();
            character.refreshPositionAndAngles(offsetX, player.getY(), offsetZ, player.getYaw(), 0);

            // Spawn the entity in the world
            if (player.getEntityWorld() instanceof net.minecraft.server.world.ServerWorld serverWorld) {
                serverWorld.spawnEntity(character);
            }

            FiveESrdMod.LOGGER.info("Character '{}' created successfully at ({}, {}, {})",
                payload.name(), offsetX, player.getY(), offsetZ);
        });
    }
}
