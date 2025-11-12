package ninja.trek.srd.combat;

import net.minecraft.text.Text;

import java.util.UUID;

/**
 * Tracks initiative for a single combatant in an encounter.
 */
public record InitiativeTracker(
    UUID entityId,
    int initiativeRoll,
    int dexModifier,
    Text displayName
) implements Comparable<InitiativeTracker> {

    /**
     * Get the total initiative value (roll + modifier).
     */
    public int getTotalInitiative() {
        return initiativeRoll + dexModifier;
    }

    @Override
    public int compareTo(InitiativeTracker other) {
        int thisTotal = this.getTotalInitiative();
        int otherTotal = other.getTotalInitiative();

        // Higher initiative goes first
        if (thisTotal != otherTotal) {
            return Integer.compare(otherTotal, thisTotal);
        }

        // Tie-breaker: higher dex modifier goes first
        return Integer.compare(other.dexModifier, this.dexModifier);
    }
}
