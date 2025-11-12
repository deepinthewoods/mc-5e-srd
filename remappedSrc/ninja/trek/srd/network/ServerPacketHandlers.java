package ninja.trek.srd.network;

import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import ninja.trek.srd.FiveESrdMod;
import ninja.trek.srd.character.entity.CharacterEntity;
import ninja.trek.srd.combat.CombatState;
import ninja.trek.srd.combat.EncounterManager;
import ninja.trek.srd.combat.EncounterState;
import ninja.trek.srd.network.payloads.EndTurnPayload;
import ninja.trek.srd.network.payloads.SyncCombatStatePayload;
import ninja.trek.srd.network.payloads.TurnEndPayload;
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

        FiveESrdMod.LOGGER.info("Server packet handlers registered successfully!");
    }

    /**
     * Handle UseAction packet from client.
     */
    private static void handleUseAction(UseActionPayload payload, ServerPlayNetworking.Context context) {
        ServerPlayer player = context.player();

        // Execute on server thread
        context.server().execute(() -> {
            // Validate that it's the player's turn
            EncounterState encounter = EncounterManager.getInstance(player.server()).getEncounterForEntity(player.getUUID());
            if (encounter == null) {
                FiveESrdMod.LOGGER.warn("Player {} tried to use action but is not in combat", player.getName().getString());
                return;
            }

            UUID currentTurnEntity = encounter.getCurrentTurnEntity();
            if (currentTurnEntity == null || !currentTurnEntity.equals(player.getUUID())) {
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
        ServerPlayer player = context.player();

        // Execute on server thread
        context.server().execute(() -> {
            EncounterManager manager = EncounterManager.getInstance(player.server());
            EncounterState encounter = manager.getEncounterForEntity(player.getUUID());

            if (encounter == null) {
                FiveESrdMod.LOGGER.warn("Player {} tried to end turn but is not in combat", player.getName().getString());
                return;
            }

            UUID currentTurnEntity = encounter.getCurrentTurnEntity();
            if (currentTurnEntity == null || !currentTurnEntity.equals(player.getUUID())) {
                FiveESrdMod.LOGGER.warn("Player {} tried to end turn but it's not their turn", player.getName().getString());
                return;
            }

            FiveESrdMod.LOGGER.info("Player {} ended their turn", player.getName().getString());

            // Send turn end notification
            TurnEndPayload turnEndPayload = new TurnEndPayload(encounter.getEncounterId(), player.getUUID());
            manager.broadcastToEncounter(encounter.getEncounterId(), turnEndPayload);

            // Advance to next turn
            encounter.nextTurn();

            // TODO: Send turn start notification for next entity
            // TODO: Handle AI turns if next entity is an NPC
        });
    }
}
