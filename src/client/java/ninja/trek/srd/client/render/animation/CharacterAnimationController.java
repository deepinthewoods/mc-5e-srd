package ninja.trek.srd.client.render.animation;

import ninja.trek.srd.character.animation.AnimationAction;
import ninja.trek.srd.character.animation.AnimationState;
import ninja.trek.srd.character.animation.AnimationTransition;
import ninja.trek.srd.character.entity.CharacterEntity;
import ninja.trek.srd.character.skeleton.SkeletonProfile;
import software.bernie.geckolib.animatable.processing.AnimationTest;
import software.bernie.geckolib.animation.PlayState;
import software.bernie.geckolib.animation.RawAnimation;

import java.util.LinkedList;
import java.util.Queue;

/**
 * Advanced animation controller for character entities with state machine support.
 * Manages animation states, transitions, priorities, and queueing for GeckoLib-based character rendering.
 *
 * Features:
 * - Priority-based animation system (Action > Locomotion > Idle)
 * - Smooth transitions between states with configurable transition times
 * - Animation queueing for chaining actions
 * - Interrupt conditions based on priority levels
 * - Animation completion callbacks
 * - Blend support for complex behaviors
 *
 * Based on IMPLEMENTATION_PLAN.md Phase 8.3 specification.
 */
public class CharacterAnimationController {

    // Animation definitions mapped to states
    private static final RawAnimation IDLE = RawAnimation.begin().thenLoop("idle");
    private static final RawAnimation WALK = RawAnimation.begin().thenLoop("walk");
    private static final RawAnimation RUN = RawAnimation.begin().thenLoop("run");
    private static final RawAnimation ATTACK = RawAnimation.begin().thenPlay("attack");
    private static final RawAnimation CAST = RawAnimation.begin().thenPlay("cast");
    private static final RawAnimation BLOCK = RawAnimation.begin().thenLoop("block");
    private static final RawAnimation DODGE = RawAnimation.begin().thenPlay("dodge");
    private static final RawAnimation CHANNEL = RawAnimation.begin().thenLoop("channel");
    private static final RawAnimation USE_ITEM = RawAnimation.begin().thenPlay("use_item");
    private static final RawAnimation DEATH = RawAnimation.begin().thenPlay("death");
    private static final RawAnimation JUMP = RawAnimation.begin().thenPlay("jump");
    private static final RawAnimation FALL = RawAnimation.begin().thenLoop("fall");
    private static final RawAnimation SWIM = RawAnimation.begin().thenLoop("swim");

    private final BoneRetargetingController retargeting;

    // State machine state
    private AnimationState currentState = AnimationState.IDLE;
    private AnimationState targetState = AnimationState.IDLE;
    private AnimationTransition activeTransition = null;
    private float currentAnimationTime = 0;  // Time in current animation
    private AnimationAction currentAction = null;  // Current action being executed
    private final Queue<AnimationAction> actionQueue = new LinkedList<>();  // Queued actions

    // State tracking
    private boolean isInCombat = false;
    private float lastUpdateTime = 0;

    public CharacterAnimationController() {
        // Initialize with human skeleton as the source (base animations are created for humans)
        this.retargeting = new BoneRetargetingController(SkeletonProfile.HUMAN);
    }

    /**
     * Main animation predicate that determines which animation to play.
     * Called by GeckoLib every frame.
     */
    public <E extends CharacterEntity> PlayState predicate(AnimationTest<E> animTest) {
        // Calculate delta time (GeckoLib doesn't provide this directly)
        float currentTime = (float) (System.currentTimeMillis() / 1000.0);
        float deltaTime = currentTime - lastUpdateTime;
        if (lastUpdateTime == 0) {
            deltaTime = 0;
        }
        lastUpdateTime = currentTime;

        // Update state machine
        update(deltaTime);

        // Get the animation for current state
        RawAnimation animation = getAnimationForState(currentState);

        // Handle transitions with blending
        if (activeTransition != null && !activeTransition.isComplete()) {
            // During transition, we're still playing the animation but it might be blending
            // GeckoLib handles the actual blending based on transition time
            return animTest.setAndContinue(animation);
        }

        // Normal playback
        if (currentState.isLooping()) {
            return animTest.setAndContinue(animation);
        } else {
            return animTest.setAndContinue(animation);
        }
    }

    /**
     * Update the state machine.
     * Handles transitions, animation completion, and action queueing.
     */
    private void update(float deltaTime) {
        // Update transition if active
        if (activeTransition != null) {
            if (activeTransition.update(deltaTime)) {
                // Transition complete
                completeTransition();
            }
        }

        // Update animation time
        currentAnimationTime += deltaTime;

        // Check if current action has completed
        if (currentAction != null && !currentAction.isLooping()) {
            if (currentAnimationTime >= currentAction.getDuration()) {
                completeCurrentAction();
            }
        }

        // Process next action in queue if idle
        if (currentState == AnimationState.IDLE && !actionQueue.isEmpty()) {
            AnimationAction nextAction = actionQueue.poll();
            startAction(nextAction);
        }
    }

    /**
     * Start an animation action.
     * Handles priority checking, interrupts, and transition initiation.
     *
     * @param action The action to start
     * @return true if the action was started, false if it was queued or rejected
     */
    public boolean startAction(AnimationAction action) {
        AnimationState newState = action.getTargetState();

        // Check if we can transition to this state
        if (!canTransitionTo(newState)) {
            // Can't interrupt current animation
            if (action.canQueue() && !actionQueue.contains(action)) {
                // Queue the action
                actionQueue.offer(action);
                return false;
            }
            return false;
        }

        // Interrupt current action if one is playing
        if (currentAction != null && currentAction.getTargetState() != newState) {
            interruptCurrentAction();
        }

        // Start transition to new state
        startTransition(newState, action.getTransitionTime());

        // Set new action as current
        currentAction = action;
        currentAnimationTime = 0;

        return true;
    }

    /**
     * Queue an action to be played after the current animation completes.
     */
    public void queueAction(AnimationAction action) {
        if (action.canQueue() && !actionQueue.contains(action)) {
            actionQueue.offer(action);
        }
    }

    /**
     * Clear all queued actions.
     */
    public void clearQueue() {
        actionQueue.clear();
    }

    /**
     * Force transition to a specific state, interrupting current animation.
     */
    public void forceTransition(AnimationState newState, float transitionTime) {
        if (currentAction != null) {
            interruptCurrentAction();
        }
        startTransition(newState, transitionTime);
        currentAction = null;
    }

    /**
     * Check if we can transition to a new state.
     */
    private boolean canTransitionTo(AnimationState newState) {
        // Always allow same state
        if (currentState == newState) {
            return true;
        }

        // Check if current state allows transition
        return currentState.canTransitionTo(newState);
    }

    /**
     * Start a transition to a new state.
     */
    private void startTransition(AnimationState newState, float transitionTime) {
        if (currentState == newState) {
            // No transition needed
            return;
        }

        targetState = newState;
        activeTransition = new AnimationTransition(currentState, newState, transitionTime);
    }

    /**
     * Complete the current transition.
     */
    private void completeTransition() {
        currentState = targetState;
        activeTransition = null;
    }

    /**
     * Complete the current action and execute its callback.
     */
    private void completeCurrentAction() {
        if (currentAction != null) {
            currentAction.executeOnComplete(null);  // Pass entity if needed
            currentAction = null;
        }

        // Transition back to appropriate idle/locomotion state
        // This will be overridden by the main predicate based on movement
        if (currentState.isAction()) {
            forceTransition(AnimationState.IDLE, 0.3f);
        }
    }

    /**
     * Interrupt the current action and execute its interrupt callback.
     */
    private void interruptCurrentAction() {
        if (currentAction != null) {
            currentAction.executeOnInterrupt(null);  // Pass entity if needed
            currentAction = null;
        }
    }

    /**
     * Update locomotion state based on movement flags.
     * Should be called by the entity's tick method.
     */
    public void updateLocomotion(boolean isMoving, boolean isSprinting, boolean isJumping, boolean isFalling, boolean isSwimming) {
        // Don't update locomotion if we're in an action state
        if (currentState.isAction()) {
            // Unless the action allows movement
            if (currentAction != null && !currentAction.allowsMovement()) {
                return;
            }
        }

        // Determine target locomotion state
        AnimationState targetLocomotion;
        if (isSwimming) {
            targetLocomotion = AnimationState.SWIMMING;
        } else if (isJumping) {
            targetLocomotion = AnimationState.JUMPING;
        } else if (isFalling) {
            targetLocomotion = AnimationState.FALLING;
        } else if (isMoving) {
            targetLocomotion = isSprinting ? AnimationState.RUNNING : AnimationState.WALKING;
        } else {
            targetLocomotion = AnimationState.IDLE;
        }

        // Transition if needed
        if (targetLocomotion != currentState && currentState.canTransitionTo(targetLocomotion)) {
            startTransition(targetLocomotion, targetLocomotion.getDefaultTransitionTime());
            currentAction = null;  // Clear action when transitioning to locomotion
        }
    }

    /**
     * Get the RawAnimation for a given state.
     */
    private RawAnimation getAnimationForState(AnimationState state) {
        return switch (state) {
            case IDLE -> IDLE;
            case WALKING -> WALK;
            case RUNNING -> RUN;
            case ATTACKING -> ATTACK;
            case CASTING -> CAST;
            case BLOCKING -> BLOCK;
            case DODGING -> DODGE;
            case CHANNELING -> CHANNEL;
            case USING_ITEM -> USE_ITEM;
            case DEATH -> DEATH;
            case JUMPING -> JUMP;
            case FALLING -> FALL;
            case SWIMMING -> SWIM;
        };
    }

    // Getters

    public AnimationState getCurrentState() {
        return currentState;
    }

    public AnimationState getTargetState() {
        return targetState;
    }

    public AnimationTransition getActiveTransition() {
        return activeTransition;
    }

    public boolean isTransitioning() {
        return activeTransition != null && !activeTransition.isComplete();
    }

    public AnimationAction getCurrentAction() {
        return currentAction;
    }

    public int getQueuedActionCount() {
        return actionQueue.size();
    }

    public boolean isInCombat() {
        return isInCombat;
    }

    public void setInCombat(boolean inCombat) {
        this.isInCombat = inCombat;
    }

    /**
     * Get the bone retargeting controller.
     */
    public BoneRetargetingController getRetargeting() {
        return retargeting;
    }

    /**
     * Reset the controller to default state.
     */
    public void reset() {
        currentState = AnimationState.IDLE;
        targetState = AnimationState.IDLE;
        activeTransition = null;
        currentAnimationTime = 0;
        currentAction = null;
        actionQueue.clear();
        lastUpdateTime = 0;
    }
}
