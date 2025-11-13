package ninja.trek.srd.character.animation;

/**
 * Represents a smooth transition between animation states.
 * Tracks the transition progress and provides blending information.
 */
public class AnimationTransition {
    private final AnimationState fromState;
    private final AnimationState toState;
    private final float transitionDuration;  // Total transition time in seconds
    private float elapsed;  // Time elapsed since transition started

    public AnimationTransition(AnimationState fromState, AnimationState toState, float transitionDuration) {
        this.fromState = fromState;
        this.toState = toState;
        this.transitionDuration = Math.max(0.001f, transitionDuration);  // Minimum 1ms to avoid divide by zero
        this.elapsed = 0;
    }

    /**
     * Update the transition with delta time.
     * @param deltaTime Time in seconds since last update
     * @return true if transition is complete, false otherwise
     */
    public boolean update(float deltaTime) {
        elapsed += deltaTime;
        return elapsed >= transitionDuration;
    }

    /**
     * Get the current blend weight for the transition.
     * Returns a value from 0.0 (fully in fromState) to 1.0 (fully in toState).
     * Uses ease-in-out curve for smooth transitions.
     */
    public float getBlendWeight() {
        float t = Math.min(elapsed / transitionDuration, 1.0f);
        // Ease-in-out curve: smooth start and end
        return t < 0.5f
            ? 2 * t * t
            : 1 - (float)Math.pow(-2 * t + 2, 2) / 2;
    }

    /**
     * Get the linear blend weight without easing.
     */
    public float getLinearBlendWeight() {
        return Math.min(elapsed / transitionDuration, 1.0f);
    }

    /**
     * Check if the transition is complete.
     */
    public boolean isComplete() {
        return elapsed >= transitionDuration;
    }

    /**
     * Get the progress of the transition (0.0 to 1.0).
     */
    public float getProgress() {
        return Math.min(elapsed / transitionDuration, 1.0f);
    }

    public AnimationState getFromState() {
        return fromState;
    }

    public AnimationState getToState() {
        return toState;
    }

    public float getTransitionDuration() {
        return transitionDuration;
    }

    public float getElapsed() {
        return elapsed;
    }

    /**
     * Get remaining transition time in seconds.
     */
    public float getRemaining() {
        return Math.max(0, transitionDuration - elapsed);
    }

    @Override
    public String toString() {
        return String.format("Transition[%s -> %s, %.2f/%.2fs (%.0f%%)]",
            fromState, toState, elapsed, transitionDuration, getProgress() * 100);
    }
}
