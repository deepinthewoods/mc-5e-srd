package ninja.trek.srd.combat;

import net.fabricmc.fabric.api.networking.v1.PlayerLookup;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.Vec3d;

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
     * Get the singleton instance.
     * Note: MinecraftServer parameter is for API compatibility but not currently used.
     */
    public static EncounterManager getInstance(MinecraftServer server) {
        return INSTANCE;
    }

    /**
     * Start a new encounter at the given position.
     */
    public UUID startEncounter(Vec3d centerPosition, List<Entity> participants) {
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

    /**
     * Broadcast a payload to all players involved in an encounter.
     */
    public void broadcastToEncounter(MinecraftServer server, UUID encounterId, CustomPayload payload) {
        EncounterState encounter = activeEncounters.get(encounterId);
        if (encounter == null) {
            return;
        }

        // Get all entity IDs in the encounter
        Set<UUID> entityIds = new HashSet<>();
        encounter.getTurnOrder().forEach(tracker -> entityIds.add(tracker.entityId()));

        // Find all online players in the encounter and send them the payload
        for (ServerPlayerEntity player : PlayerLookup.all(server)) {
            if (entityIds.contains(player.getUuid())) {
                ServerPlayNetworking.send(player, payload);
            }
        }
    }
}
