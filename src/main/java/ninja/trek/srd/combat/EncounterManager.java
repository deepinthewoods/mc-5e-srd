package ninja.trek.srd.combat;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Server-side singleton managing all active combat encounters.
 */
public class EncounterManager {
    private static final EncounterManager INSTANCE = new EncounterManager();

    private final Map<UUID, EncounterState> activeEncounters = new ConcurrentHashMap<>();
    private final Map<UUID, UUID> entityToEncounter = new ConcurrentHashMap<>();

    private EncounterManager() {}

    public static EncounterManager getInstance() {
        return INSTANCE;
    }

    /**
     * Start a new encounter at the given position.
     */
    public UUID startEncounter(Vec3 centerPosition, List<Entity> participants) {
        UUID encounterId = UUID.randomUUID();
        EncounterState encounter = new EncounterState(encounterId, centerPosition);

        activeEncounters.put(encounterId, encounter);

        return encounterId;
    }

    /**
     * Add a combatant to an existing encounter.
     */
    public void addCombatant(UUID encounterId, InitiativeTracker tracker) {
        EncounterState encounter = activeEncounters.get(encounterId);
        if (encounter != null && !encounter.isEnded()) {
            encounter.addCombatant(tracker);
            entityToEncounter.put(tracker.entityId(), encounterId);
        }
    }

    /**
     * Advance the turn in the specified encounter.
     */
    public void advanceTurn(UUID encounterId) {
        EncounterState encounter = activeEncounters.get(encounterId);
        if (encounter != null && !encounter.isEnded()) {
            encounter.nextTurn();
        }
    }

    /**
     * End an encounter and clean up.
     */
    public void endEncounter(UUID encounterId) {
        EncounterState encounter = activeEncounters.get(encounterId);
        if (encounter != null) {
            encounter.end();
            // Remove entity mappings
            encounter.getTurnOrder().forEach(tracker ->
                entityToEncounter.remove(tracker.entityId())
            );
            activeEncounters.remove(encounterId);
        }
    }

    /**
     * Get the encounter for a specific entity.
     */
    public EncounterState getEncounterForEntity(UUID entityId) {
        UUID encounterId = entityToEncounter.get(entityId);
        return encounterId != null ? activeEncounters.get(encounterId) : null;
    }

    /**
     * Get an encounter by ID.
     */
    public EncounterState getEncounter(UUID encounterId) {
        return activeEncounters.get(encounterId);
    }

    /**
     * Remove a combatant from their encounter (e.g., when defeated or disconnected).
     */
    public void removeCombatant(UUID entityId) {
        UUID encounterId = entityToEncounter.get(entityId);
        if (encounterId != null) {
            EncounterState encounter = activeEncounters.get(encounterId);
            if (encounter != null) {
                encounter.removeCombatant(entityId);
                entityToEncounter.remove(entityId);

                // End encounter if no combatants remain
                if (encounter.getTurnOrder().isEmpty()) {
                    endEncounter(encounterId);
                }
            }
        }
    }

    /**
     * Check if an entity is in combat.
     */
    public boolean isInCombat(UUID entityId) {
        return entityToEncounter.containsKey(entityId);
    }

    /**
     * Get all active encounters.
     */
    public Collection<EncounterState> getAllEncounters() {
        return Collections.unmodifiableCollection(activeEncounters.values());
    }
}
