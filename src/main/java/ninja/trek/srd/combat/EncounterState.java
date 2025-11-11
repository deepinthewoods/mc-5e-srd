package ninja.trek.srd.combat;

import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

/**
 * Manages the state of an active combat encounter.
 */
public class EncounterState {
    private final UUID encounterId;
    private final List<InitiativeTracker> turnOrder;
    private int currentTurnIndex;
    private int roundNumber;
    private final Vec3 centerPosition;
    private boolean ended;

    public EncounterState(UUID encounterId, Vec3 centerPosition) {
        this.encounterId = encounterId;
        this.centerPosition = centerPosition;
        this.turnOrder = new ArrayList<>();
        this.currentTurnIndex = 0;
        this.roundNumber = 1;
        this.ended = false;
    }

    /**
     * Add a combatant to the encounter and sort by initiative.
     */
    public void addCombatant(InitiativeTracker tracker) {
        turnOrder.add(tracker);
        Collections.sort(turnOrder);
    }

    /**
     * Get the entity ID whose turn it currently is.
     */
    public UUID getCurrentTurnEntity() {
        if (turnOrder.isEmpty() || ended) {
            return null;
        }
        return turnOrder.get(currentTurnIndex).entityId();
    }

    /**
     * Advance to the next turn.
     */
    public void nextTurn() {
        if (turnOrder.isEmpty() || ended) {
            return;
        }

        currentTurnIndex++;
        if (currentTurnIndex >= turnOrder.size()) {
            currentTurnIndex = 0;
            roundNumber++;
        }
    }

    /**
     * Remove a combatant from the encounter (e.g., when defeated).
     */
    public void removeCombatant(UUID entityId) {
        for (int i = 0; i < turnOrder.size(); i++) {
            if (turnOrder.get(i).entityId().equals(entityId)) {
                turnOrder.remove(i);
                // Adjust current turn index if needed
                if (i < currentTurnIndex) {
                    currentTurnIndex--;
                } else if (currentTurnIndex >= turnOrder.size() && !turnOrder.isEmpty()) {
                    currentTurnIndex = 0;
                    roundNumber++;
                }
                break;
            }
        }
    }

    /**
     * End the encounter.
     */
    public void end() {
        this.ended = true;
    }

    // Getters
    public UUID getEncounterId() {
        return encounterId;
    }

    public List<InitiativeTracker> getTurnOrder() {
        return Collections.unmodifiableList(turnOrder);
    }

    public int getCurrentTurnIndex() {
        return currentTurnIndex;
    }

    public int getRoundNumber() {
        return roundNumber;
    }

    public Vec3 getCenterPosition() {
        return centerPosition;
    }

    public boolean isEnded() {
        return ended;
    }
}
