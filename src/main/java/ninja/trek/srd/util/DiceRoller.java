package ninja.trek.srd.util;

import net.minecraft.util.math.random.Random;

/**
 * Utility class for rolling dice according to D&D 5e rules.
 */
public class DiceRoller {
    private final Random random;

    public DiceRoller(Random random) {
        this.random = random;
    }

    /**
     * Roll a single die with the specified number of sides.
     * @param sides Number of sides on the die (e.g., 20 for d20)
     * @return Result between 1 and sides (inclusive)
     */
    public int roll(int sides) {
        return random.nextInt(sides) + 1;
    }

    /**
     * Roll multiple dice and sum the results.
     * @param count Number of dice to roll
     * @param sides Number of sides on each die
     * @return Sum of all rolls
     */
    public int rollMultiple(int count, int sides) {
        int total = 0;
        for (int i = 0; i < count; i++) {
            total += roll(sides);
        }
        return total;
    }

    /**
     * Roll with advantage (roll twice, take higher).
     * @param sides Number of sides on the die
     * @return The higher of two rolls
     */
    public int rollWithAdvantage(int sides) {
        return Math.max(roll(sides), roll(sides));
    }

    /**
     * Roll with disadvantage (roll twice, take lower).
     * @param sides Number of sides on the die
     * @return The lower of two rolls
     */
    public int rollWithDisadvantage(int sides) {
        return Math.min(roll(sides), roll(sides));
    }

    /**
     * Roll a d20 (most common roll in 5e).
     */
    public int rollD20() {
        return roll(20);
    }

    /**
     * Roll for initiative (d20 + modifier).
     */
    public int rollInitiative(int dexModifier) {
        return rollD20() + dexModifier;
    }

    /**
     * Roll for ability scores using 4d6 drop lowest method.
     */
    public int rollAbilityScore() {
        int[] rolls = new int[4];
        for (int i = 0; i < 4; i++) {
            rolls[i] = roll(6);
        }

        // Find and drop the lowest
        int lowest = Integer.MAX_VALUE;
        int sum = 0;
        for (int roll : rolls) {
            sum += roll;
            if (roll < lowest) {
                lowest = roll;
            }
        }

        return sum - lowest;
    }

    /**
     * Roll for hit points (uses hit die + constitution modifier).
     */
    public int rollHitPoints(int hitDie, int conModifier) {
        return Math.max(1, roll(hitDie) + conModifier);
    }
}
