package ninja.trek.srd.client.render.animation;

import ninja.trek.srd.character.entity.CharacterEntity;
import ninja.trek.srd.character.skeleton.SkeletonProfile;
import software.bernie.geckolib.animatable.processing.AnimationTest;
import software.bernie.geckolib.animation.PlayState;
import software.bernie.geckolib.animation.RawAnimation;

/**
 * Animation controller for character entities.
 * Manages animation states and transitions for GeckoLib-based character rendering.
 *
 * Based on IMPLEMENTATION_PLAN.md Phase 3 specification.
 */
public class CharacterAnimationController {

    // Animation definitions
    private static final RawAnimation IDLE = RawAnimation.begin().thenLoop("idle");
    private static final RawAnimation WALK = RawAnimation.begin().thenLoop("walk");
    private static final RawAnimation RUN = RawAnimation.begin().thenLoop("run");
    private static final RawAnimation ATTACK = RawAnimation.begin().thenPlay("attack");
    private static final RawAnimation CAST = RawAnimation.begin().thenPlay("cast");

    private final BoneRetargetingController retargeting;

    public CharacterAnimationController() {
        // Initialize with human skeleton as the source (base animations are created for humans)
        this.retargeting = new BoneRetargetingController(SkeletonProfile.HUMAN);
    }

    /**
     * Main animation predicate that determines which animation to play.
     * Called by GeckoLib every frame.
     */
    public <E extends CharacterEntity> PlayState predicate(AnimationTest<E> animTest) {
        // In GeckoLib 5, AnimationTest provides movement data but not the entity itself
        // Simplified animation logic without entity-specific data

        // Priority 1: Locomotion animations based on movement
        if (animTest.isMoving()) {
            // TODO: Detect sprinting from AnimationTest if available
            return animTest.setAndContinue(WALK);
        }

        // Priority 2: Idle animation
        return animTest.setAndContinue(IDLE);
    }

    /**
     * Get the bone retargeting controller.
     */
    public BoneRetargetingController getRetargeting() {
        return retargeting;
    }
}
