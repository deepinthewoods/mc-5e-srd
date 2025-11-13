package ninja.trek.srd.character.animation;

/**
 * Represents the current animation state of a character.
 * States are organized by priority with higher priority states able to interrupt lower priority ones.
 *
 * Priority levels:
 * - PRIORITY_ACTION (1): Action animations like attacks, casts, special abilities
 * - PRIORITY_LOCOMOTION (2): Movement animations like walk, run, jump
 * - PRIORITY_IDLE (3): Default idle state
 */
public enum AnimationState {
    // Priority 1: Action animations (highest priority)
    ATTACKING(AnimationPriority.ACTION, false, 0.5f),
    CASTING(AnimationPriority.ACTION, false, 1.0f),
    BLOCKING(AnimationPriority.ACTION, true, 0.0f),
    DODGING(AnimationPriority.ACTION, true, 0.0f),
    USING_ITEM(AnimationPriority.ACTION, false, 0.8f),
    CHANNELING(AnimationPriority.ACTION, true, 0.0f),  // Channeled spell
    DEATH(AnimationPriority.ACTION, false, 1.5f),

    // Priority 2: Locomotion animations
    RUNNING(AnimationPriority.LOCOMOTION, true, 0.2f),
    WALKING(AnimationPriority.LOCOMOTION, true, 0.2f),
    JUMPING(AnimationPriority.LOCOMOTION, false, 0.4f),
    FALLING(AnimationPriority.LOCOMOTION, true, 0.1f),
    SWIMMING(AnimationPriority.LOCOMOTION, true, 0.3f),

    // Priority 3: Idle animation (lowest priority)
    IDLE(AnimationPriority.IDLE, true, 0.3f);

    private final AnimationPriority priority;
    private final boolean canBeInterrupted;
    private final float defaultTransitionTime;  // in seconds

    AnimationState(AnimationPriority priority, boolean canBeInterrupted, float defaultTransitionTime) {
        this.priority = priority;
        this.canBeInterrupted = canBeInterrupted;
        this.defaultTransitionTime = defaultTransitionTime;
    }

    /**
     * Get the priority level of this animation state.
     */
    public AnimationPriority getPriority() {
        return priority;
    }

    /**
     * Check if this animation can be interrupted by another animation.
     * Some animations (like looping idle or walk) can be interrupted,
     * while others (like attacks) must complete.
     */
    public boolean canBeInterrupted() {
        return canBeInterrupted;
    }

    /**
     * Get the default transition time when transitioning to/from this state.
     */
    public float getDefaultTransitionTime() {
        return defaultTransitionTime;
    }

    /**
     * Check if this state can transition to another state.
     * Transitions are allowed if:
     * 1. The target state has higher or equal priority, OR
     * 2. This state can be interrupted, OR
     * 3. This state has completed (handled by controller)
     */
    public boolean canTransitionTo(AnimationState target) {
        // Higher priority can always interrupt
        if (target.priority.getValue() < this.priority.getValue()) {
            return true;
        }

        // Same priority can transition if current is interruptible
        if (target.priority.getValue() == this.priority.getValue() && this.canBeInterrupted) {
            return true;
        }

        // Lower priority can only transition if current is interruptible
        return this.canBeInterrupted;
    }

    /**
     * Get the animation name that GeckoLib should use for this state.
     */
    public String getAnimationName() {
        return this.name().toLowerCase();
    }

    /**
     * Check if this is a looping animation.
     */
    public boolean isLooping() {
        return switch (this) {
            case IDLE, WALKING, RUNNING, FALLING, SWIMMING, BLOCKING, CHANNELING, DODGING -> true;
            default -> false;
        };
    }

    /**
     * Check if this is a locomotion state.
     */
    public boolean isLocomotion() {
        return priority == AnimationPriority.LOCOMOTION;
    }

    /**
     * Check if this is an action state.
     */
    public boolean isAction() {
        return priority == AnimationPriority.ACTION;
    }
}
