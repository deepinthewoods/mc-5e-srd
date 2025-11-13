package ninja.trek.srd.combat;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

/**
 * Tracks death saving throws for a character at 0 HP.
 * In D&D 5e, when a creature drops to 0 HP, they become unconscious and must make death saves.
 * - 3 successes: stabilize at 0 HP
 * - 3 failures: die
 * - Taking damage while unconscious: 1 automatic failure (2 if critical hit)
 */
public record DeathSaves(
    int successes,
    int failures,
    boolean isStabilized
) {
    public static final Codec<DeathSaves> CODEC = RecordCodecBuilder.create(instance ->
        instance.group(
            Codec.INT.fieldOf("successes").forGetter(DeathSaves::successes),
            Codec.INT.fieldOf("failures").forGetter(DeathSaves::failures),
            Codec.BOOL.fieldOf("is_stabilized").forGetter(DeathSaves::isStabilized)
        ).apply(instance, DeathSaves::new)
    );

    /**
     * Create default death saves (no successes or failures).
     */
    public static DeathSaves createDefault() {
        return new DeathSaves(0, 0, false);
    }

    /**
     * Add a successful death save.
     * @return new DeathSaves with success added (or stabilized if reached 3)
     */
    public DeathSaves addSuccess() {
        int newSuccesses = Math.min(3, successes + 1);
        boolean stabilized = newSuccesses >= 3;
        return new DeathSaves(newSuccesses, failures, stabilized);
    }

    /**
     * Add a failed death save.
     * @return new DeathSaves with failure added
     */
    public DeathSaves addFailure() {
        return new DeathSaves(successes, Math.min(3, failures + 1), isStabilized);
    }

    /**
     * Add multiple failures at once (for taking damage while unconscious).
     * @param count number of failures to add
     * @return new DeathSaves with failures added
     */
    public DeathSaves addFailures(int count) {
        return new DeathSaves(successes, Math.min(3, failures + count), isStabilized);
    }

    /**
     * Check if character is dead (3 failures).
     */
    public boolean isDead() {
        return failures >= 3;
    }

    /**
     * Check if character is stabilized (3 successes or stabilized by healing).
     */
    public boolean isStabilized() {
        return isStabilized || successes >= 3;
    }

    /**
     * Reset death saves (called when healing above 0 HP or on long rest).
     */
    public DeathSaves reset() {
        return createDefault();
    }

    /**
     * Stabilize the character (via Medicine check or Spare the Dying cantrip).
     */
    public DeathSaves stabilize() {
        return new DeathSaves(successes, failures, true);
    }

    /**
     * Get a display string for death saves.
     */
    public String getDisplayString() {
        if (isDead()) {
            return "DEAD";
        }
        if (isStabilized()) {
            return "STABILIZED";
        }
        return String.format("Death Saves: %d successes / %d failures", successes, failures);
    }
}
