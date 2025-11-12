package ninja.trek.srd.client;

import ninja.trek.srd.combat.InitiativeTracker;
import ninja.trek.srd.network.payloads.SyncEncounterStatePayload;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Client-side cache of encounter states.
 * This is updated by network packets from the server.
 */
public class ClientEncounterState {
    private static final ClientEncounterState INSTANCE = new ClientEncounterState();

    private final Map<UUID, EncounterData> encounters = new HashMap<>();
    private final Map<UUID, CombatStateData> combatStates = new HashMap<>();

    private ClientEncounterState() {}

    public static ClientEncounterState getInstance() {
        return INSTANCE;
    }

    /**
     * Update encounter state from server sync packet.
     */
    public void updateEncounter(SyncEncounterStatePayload payload) {
        EncounterData data = new EncounterData(
            payload.encounterId(),
            payload.turnOrder(),
            payload.currentTurnIndex(),
            payload.roundNumber(),
            payload.ended()
        );
        encounters.put(payload.encounterId(), data);
    }

    /**
     * Update combat state for a specific entity.
     */
    public void updateCombatState(UUID entityId, int currentHp, int maxHp, int ac,
                                   boolean hasAction, boolean hasBonusAction,
                                   boolean hasReaction, int remainingMovement) {
        CombatStateData data = new CombatStateData(
            entityId, currentHp, maxHp, ac, hasAction,
            hasBonusAction, hasReaction, remainingMovement
        );
        combatStates.put(entityId, data);
    }

    /**
     * Mark the start of a turn for an entity.
     */
    public void startTurn(UUID encounterId, UUID entityId) {
        EncounterData encounter = encounters.get(encounterId);
        if (encounter != null) {
            // Update current turn entity
            List<SyncEncounterStatePayload.InitiativeEntry> turnOrder = encounter.turnOrder();
            for (int i = 0; i < turnOrder.size(); i++) {
                if (turnOrder.get(i).entityId().equals(entityId)) {
                    encounters.put(encounterId, new EncounterData(
                        encounter.encounterId(),
                        encounter.turnOrder(),
                        i,
                        encounter.roundNumber(),
                        encounter.ended()
                    ));
                    break;
                }
            }
        }
    }

    /**
     * Mark the end of a turn for an entity.
     */
    public void endTurn(UUID encounterId, UUID entityId) {
        // The actual turn advancement is handled by the server
        // This is just for client-side UI updates
    }

    /**
     * Get encounter data by ID.
     */
    public EncounterData getEncounter(UUID encounterId) {
        return encounters.get(encounterId);
    }

    /**
     * Get combat state for an entity.
     */
    public CombatStateData getCombatState(UUID entityId) {
        return combatStates.get(entityId);
    }

    /**
     * Find the encounter that contains a specific entity.
     */
    public UUID getEncounterForEntity(UUID entityId) {
        for (EncounterData encounter : encounters.values()) {
            for (SyncEncounterStatePayload.InitiativeEntry entry : encounter.turnOrder()) {
                if (entry.entityId().equals(entityId)) {
                    return encounter.encounterId();
                }
            }
        }
        return null;
    }

    /**
     * Clear all encounter data (e.g., when disconnecting from server).
     */
    public void clear() {
        encounters.clear();
        combatStates.clear();
    }

    /**
     * Immutable record for encounter data.
     */
    public record EncounterData(
        UUID encounterId,
        List<SyncEncounterStatePayload.InitiativeEntry> turnOrder,
        int currentTurnIndex,
        int roundNumber,
        boolean ended
    ) {
        public UUID getCurrentTurnEntity() {
            if (turnOrder.isEmpty() || ended) {
                return null;
            }
            return turnOrder.get(currentTurnIndex).entityId();
        }
    }

    /**
     * Immutable record for combat state data.
     */
    public record CombatStateData(
        UUID entityId,
        int currentHitPoints,
        int maxHitPoints,
        int armorClass,
        boolean hasAction,
        boolean hasBonusAction,
        boolean hasReaction,
        int remainingMovement
    ) {}
}
