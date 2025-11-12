package ninja.trek.srd.network;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import ninja.trek.srd.FiveESrdMod;
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

            // TODO: Update client-side encounter state cache
            // TODO: Update combat UI to show initiative order
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

            // TODO: Update entity's combat state on client
            // TODO: Update combat UI to show action availability
        });
    }

    /**
     * Handle TurnStart packet from server.
     */
    private static void handleTurnStart(TurnStartPayload payload, ClientPlayNetworking.Context context) {
        // Execute on client thread
        context.client().execute(() -> {
            FiveESrdMod.LOGGER.info("Turn started for entity: {}", payload.entityId());

            // TODO: Highlight current turn entity
            // TODO: Enable/disable action buttons based on whose turn it is
            // TODO: Play turn start sound/visual effect
        });
    }

    /**
     * Handle TurnEnd packet from server.
     */
    private static void handleTurnEnd(TurnEndPayload payload, ClientPlayNetworking.Context context) {
        // Execute on client thread
        context.client().execute(() -> {
            FiveESrdMod.LOGGER.info("Turn ended for entity: {}", payload.entityId());

            // TODO: Remove highlighting from entity
            // TODO: Disable action buttons
        });
    }
}
