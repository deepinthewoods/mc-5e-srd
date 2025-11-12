package ninja.trek.srd.combat;

import net.fabricmc.fabric.api.networking.v1.PlayerLookup;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.Vec3d;
import ninja.trek.srd.character.entity.CharacterEntity;
import ninja.trek.srd.network.payloads.SyncCombatStatePayload;
import ninja.trek.srd.network.payloads.SyncEncounterStatePayload;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * Server-side singleton managing all active combat encounters.
 */
public class EncounterManager {
    private static final EncounterManager INSTANCE = new EncounterManager();

    private final Map<UUID, EncounterState> activeEncounters = new ConcurrentHashMap<>();
    private final Map<UUID, UUID> entityToEncounter = new ConcurrentHashMap<>();
    private final Map<UUID, CombatState> trackedCombatStates = new ConcurrentHashMap<>();
    private final Map<UUID, Integer> baseMovementSpeeds = new ConcurrentHashMap<>();

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
     * Register the base movement speed (in blocks per turn) for an entity.
     */
    public void setBaseMovementSpeed(UUID entityId, int movementSpeed) {
        baseMovementSpeeds.put(entityId, movementSpeed);
    }

    /**
     * Add a combatant to an existing encounter.
     */
    public void addCombatant(MinecraftServer server, UUID encounterId, InitiativeTracker tracker) {
        EncounterState encounter = activeEncounters.get(encounterId);
        if (encounter != null && !encounter.isEnded()) {
            encounter.addCombatant(tracker);
            entityToEncounter.put(tracker.entityId(), encounterId);
            if (server != null) {
                syncEncounter(server, encounterId);
                ServerPlayerEntity player = server.getPlayerManager().getPlayer(tracker.entityId());
                if (player != null) {
                    syncEncounterToPlayer(server, encounterId, player);
                }
            }
        }
    }

    /**
     * Advance the turn in the specified encounter.
     */
    public void advanceTurn(UUID encounterId) {
        advanceTurn(null, encounterId);
    }

    public void advanceTurn(MinecraftServer server, UUID encounterId) {
        EncounterState encounter = activeEncounters.get(encounterId);
        if (encounter != null && !encounter.isEnded()) {
            encounter.nextTurn();
            if (server != null) {
                startTurnForCurrentEntity(server, encounterId);
            }
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
            encounter.getTurnOrder().forEach(tracker -> {
                entityToEncounter.remove(tracker.entityId());
                trackedCombatStates.remove(tracker.entityId());
                baseMovementSpeeds.remove(tracker.entityId());
            });
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
     * Get the latest combat state for an entity.
     */
    public CombatState getCombatState(MinecraftServer server, UUID entityId) {
        if (server != null) {
            CombatState state = resolveCombatState(server, entityId);
            if (state != null) {
                return state;
            }
        }
        return trackedCombatStates.get(entityId);
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
                trackedCombatStates.remove(entityId);
                baseMovementSpeeds.remove(entityId);

                // End encounter if no combatants remain
                if (encounter.getTurnOrder().isEmpty()) {
                    endEncounter(encounterId);
                }
            }
        }
    }

    /**
     * Reset movement and action resources for the entity whose turn it currently is.
     */
    public void startTurnForCurrentEntity(MinecraftServer server, UUID encounterId) {
        EncounterState encounter = activeEncounters.get(encounterId);
        if (encounter == null) {
            return;
        }

        UUID entityId = encounter.getCurrentTurnEntity();
        if (entityId == null) {
            return;
        }

        Entity entity = findEntity(server, entityId);
        if (entity instanceof CharacterEntity character) {
            character.startTurn();
            return;
        }

        CombatState combatState = resolveCombatState(server, entityId);
        if (combatState == null) {
            return;
        }

        Vec3d startPos = entity != null
            ? Vec3d.ofBottomCenter(entity.getBlockPos())
            : combatState.turnStartPosition();
        int movementSpeed = baseMovementSpeeds.getOrDefault(entityId, combatState.remainingMovement());

        CombatState started = combatState.startTurn(startPos, movementSpeed);
        syncCombatState(server, entityId, started);
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

    /**
     * Broadcast the latest encounter state (turn order, round, etc.) to all participants.
     */
    public void syncEncounter(MinecraftServer server, UUID encounterId) {
        EncounterState encounter = activeEncounters.get(encounterId);
        if (encounter == null) {
            return;
        }

        SyncEncounterStatePayload payload = new SyncEncounterStatePayload(
            encounterId,
            encounter.getTurnOrder().stream()
                .map(SyncEncounterStatePayload::fromTracker)
                .collect(Collectors.toList()),
            encounter.getCurrentTurnIndex(),
            encounter.getRoundNumber(),
            encounter.isEnded()
        );

        broadcastToEncounter(server, encounterId, payload);
    }

    /**
     * Send encounter state (and current combatant snapshots) directly to one player.
     * Used when a player enters an encounter that is already in progress.
     */
    public void syncEncounterToPlayer(MinecraftServer server, UUID encounterId, ServerPlayerEntity player) {
        EncounterState encounter = activeEncounters.get(encounterId);
        if (encounter == null) {
            return;
        }

        SyncEncounterStatePayload encounterPayload = new SyncEncounterStatePayload(
            encounterId,
            encounter.getTurnOrder().stream()
                .map(SyncEncounterStatePayload::fromTracker)
                .collect(Collectors.toList()),
            encounter.getCurrentTurnIndex(),
            encounter.getRoundNumber(),
            encounter.isEnded()
        );

        ServerPlayNetworking.send(player, encounterPayload);

        for (InitiativeTracker tracker : encounter.getTurnOrder()) {
            CombatState combatState = resolveCombatState(server, tracker.entityId());
            if (combatState != null) {
                SyncCombatStatePayload payload = buildCombatStatePayload(tracker.entityId(), combatState);
                ServerPlayNetworking.send(player, payload);
            }
        }
    }

    /**
     * If the specified player belongs to an active encounter, send them the latest encounter snapshot.
     */
    public void syncPlayerEncounterState(MinecraftServer server, ServerPlayerEntity player) {
        if (server == null || player == null) {
            return;
        }

        EncounterState encounter = getEncounterForEntity(player.getUuid());
        if (encounter != null) {
            syncEncounterToPlayer(server, encounter.getEncounterId(), player);
        }
    }

    /**
     * Broadcast a combat state update for the specified entity to all players in the encounter.
     */
    public void syncCombatState(MinecraftServer server, UUID entityId, CombatState combatState) {
        if (combatState == null) {
            return;
        }

        UUID encounterId = entityToEncounter.get(entityId);
        if (encounterId == null) {
            return;
        }

        SyncCombatStatePayload payload = buildCombatStatePayload(entityId, combatState);
        trackedCombatStates.put(entityId, combatState);
        broadcastToEncounter(server, encounterId, payload);
    }

    private CombatState resolveCombatState(MinecraftServer server, UUID entityId) {
        Entity entity = findEntity(server, entityId);
        if (entity instanceof CharacterEntity character) {
            return character.getCombatState();
        }

        return trackedCombatStates.get(entityId);
    }

    private Entity findEntity(MinecraftServer server, UUID entityId) {
        for (var world : server.getWorlds()) {
            Entity entity = world.getEntity(entityId);
            if (entity != null) {
                return entity;
            }
        }
        return null;
    }

    private SyncCombatStatePayload buildCombatStatePayload(UUID entityId, CombatState combatState) {
        return new SyncCombatStatePayload(
            entityId,
            combatState.inCombat(),
            combatState.initiative(),
            combatState.turnStartPosition(),
            combatState.remainingMovement(),
            combatState.hasAction(),
            combatState.hasBonusAction(),
            combatState.hasReaction(),
            combatState.armorClass(),
            combatState.currentHitPoints(),
            combatState.maxHitPoints()
        );
    }
}
