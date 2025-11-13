package ninja.trek.srd.character.animation;

/**
 * Priority levels for animations.
 * Lower numeric values = higher priority.
 */
public enum AnimationPriority {
    ACTION(1),       // Attacks, casts, special abilities
    LOCOMOTION(2),   // Walk, run, jump
    IDLE(3);         // Default idle state

    private final int value;

    AnimationPriority(int value) {
        this.value = value;
    }

    /**
     * Get the numeric priority value.
     * Lower values indicate higher priority.
     */
    public int getValue() {
        return value;
    }

    /**
     * Check if this priority is higher than another.
     */
    public boolean isHigherThan(AnimationPriority other) {
        return this.value < other.value;
    }

    /**
     * Check if this priority is lower than another.
     */
    public boolean isLowerThan(AnimationPriority other) {
        return this.value > other.value;
    }
}
