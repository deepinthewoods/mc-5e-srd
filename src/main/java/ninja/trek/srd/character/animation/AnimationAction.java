package ninja.trek.srd.character.animation;

import java.util.function.Consumer;

/**
 * Represents an animation action that can be triggered on a character.
 * Actions have associated animation states, durations, and callbacks.
 */
public class AnimationAction {
    private final AnimationState targetState;
    private final float duration;  // Duration in seconds (0 for looping animations)
    private final float transitionTime;  // Transition time in seconds
    private final boolean allowMovement;  // Can the character move while this animation plays?
    private final boolean canQueue;  // Can this action be queued after current animation?
    private final Consumer<Object> onComplete;  // Callback when animation completes
    private final Consumer<Object> onInterrupt;  // Callback when animation is interrupted

    private AnimationAction(Builder builder) {
        this.targetState = builder.targetState;
        this.duration = builder.duration;
        this.transitionTime = builder.transitionTime;
        this.allowMovement = builder.allowMovement;
        this.canQueue = builder.canQueue;
        this.onComplete = builder.onComplete;
        this.onInterrupt = builder.onInterrupt;
    }

    public AnimationState getTargetState() {
        return targetState;
    }

    public float getDuration() {
        return duration;
    }

    public float getTransitionTime() {
        return transitionTime;
    }

    public boolean allowsMovement() {
        return allowMovement;
    }

    public boolean canQueue() {
        return canQueue;
    }

    public void executeOnComplete(Object entity) {
        if (onComplete != null) {
            onComplete.accept(entity);
        }
    }

    public void executeOnInterrupt(Object entity) {
        if (onInterrupt != null) {
            onInterrupt.accept(entity);
        }
    }

    /**
     * Check if this animation is looping (duration = 0).
     */
    public boolean isLooping() {
        return duration <= 0 || targetState.isLooping();
    }

    /**
     * Create a builder for an animation action.
     */
    public static Builder builder(AnimationState targetState) {
        return new Builder(targetState);
    }

    /**
     * Builder for AnimationAction.
     */
    public static class Builder {
        private final AnimationState targetState;
        private float duration = 0;  // 0 = looping
        private float transitionTime;
        private boolean allowMovement = false;
        private boolean canQueue = true;
        private Consumer<Object> onComplete = null;
        private Consumer<Object> onInterrupt = null;

        private Builder(AnimationState targetState) {
            this.targetState = targetState;
            this.transitionTime = targetState.getDefaultTransitionTime();
        }

        /**
         * Set the duration of the animation in seconds.
         * Use 0 for looping animations.
         */
        public Builder duration(float duration) {
            this.duration = duration;
            return this;
        }

        /**
         * Set the transition time when starting this animation.
         */
        public Builder transitionTime(float transitionTime) {
            this.transitionTime = transitionTime;
            return this;
        }

        /**
         * Set whether the character can move while this animation plays.
         */
        public Builder allowMovement(boolean allowMovement) {
            this.allowMovement = allowMovement;
            return this;
        }

        /**
         * Set whether this action can be queued.
         */
        public Builder canQueue(boolean canQueue) {
            this.canQueue = canQueue;
            return this;
        }

        /**
         * Set the callback to execute when the animation completes.
         */
        public Builder onComplete(Consumer<Object> onComplete) {
            this.onComplete = onComplete;
            return this;
        }

        /**
         * Set the callback to execute when the animation is interrupted.
         */
        public Builder onInterrupt(Consumer<Object> onInterrupt) {
            this.onInterrupt = onInterrupt;
            return this;
        }

        public AnimationAction build() {
            return new AnimationAction(this);
        }
    }

    // Predefined actions for common animations

    /**
     * Create a standard melee attack action.
     */
    public static AnimationAction meleeAttack() {
        return builder(AnimationState.ATTACKING)
            .duration(0.6f)  // Attack animation duration
            .allowMovement(false)
            .canQueue(true)
            .build();
    }

    /**
     * Create a ranged attack action.
     */
    public static AnimationAction rangedAttack() {
        return builder(AnimationState.ATTACKING)
            .duration(0.5f)
            .allowMovement(false)
            .canQueue(true)
            .build();
    }

    /**
     * Create a spell casting action.
     */
    public static AnimationAction cast() {
        return builder(AnimationState.CASTING)
            .duration(1.0f)  // Cast animation duration
            .allowMovement(false)
            .canQueue(false)  // Can't queue casts
            .build();
    }

    /**
     * Create a channeled spell action (continuous).
     */
    public static AnimationAction channel() {
        return builder(AnimationState.CHANNELING)
            .duration(0)  // Looping
            .allowMovement(true)  // Can move while channeling
            .canQueue(false)
            .build();
    }

    /**
     * Create a blocking/defending action.
     */
    public static AnimationAction block() {
        return builder(AnimationState.BLOCKING)
            .duration(0)  // Looping
            .allowMovement(true)  // Can move while blocking
            .canQueue(false)
            .build();
    }

    /**
     * Create a dodge action.
     */
    public static AnimationAction dodge() {
        return builder(AnimationState.DODGING)
            .duration(0.4f)
            .allowMovement(true)
            .canQueue(false)
            .build();
    }

    /**
     * Create an item use action.
     */
    public static AnimationAction useItem() {
        return builder(AnimationState.USING_ITEM)
            .duration(0.8f)
            .allowMovement(false)
            .canQueue(true)
            .build();
    }

    /**
     * Create a death animation action.
     */
    public static AnimationAction death() {
        return builder(AnimationState.DEATH)
            .duration(1.5f)
            .allowMovement(false)
            .canQueue(false)
            .build();
    }
}
