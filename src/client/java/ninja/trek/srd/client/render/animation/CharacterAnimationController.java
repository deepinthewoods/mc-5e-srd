package ninja.trek.srd.client.render.animation;

import ninja.trek.srd.character.entity.CharacterEntity;
import ninja.trek.srd.character.skeleton.SkeletonProfile;
import software.bernie.geckolib.animation.AnimationState;
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
    public PlayState predicate(AnimationState<CharacterEntity> state) {
        CharacterEntity entity = state.getAnimatable();

        // Get the target skeleton profile based on entity's race
        SkeletonProfile targetProfile = SkeletonProfile.getByRaceName(
            entity.getRace().getName()
        );

        // Calculate animation speed for retargeting
        boolean isMoving = state.isMoving();
        float animSpeed = retargeting.calculateAnimationSpeed(
            targetProfile,
            1.0f, // Size scale (TODO: add size scaling to CharacterEntity)
            isMoving
        );

        // Priority 1: Attack animations
        if (entity.handSwingProgress > 0) {
            state.getController().setAnimation(ATTACK);
            return PlayState.CONTINUE;
        }

        // Priority 2: Locomotion animations
        if (isMoving) {
            if (entity.isSprinting()) {
                state.getController().setAnimation(RUN);
                state.getController().setAnimationSpeed(animSpeed * 1.5); // Run is faster
            } else {
                state.getController().setAnimation(WALK);
                state.getController().setAnimationSpeed(animSpeed);
            }
            return PlayState.CONTINUE;
        }

        // Priority 3: Idle animation
        state.getController().setAnimation(IDLE);
        return PlayState.CONTINUE;
    }

    /**
     * Get the bone retargeting controller.
     */
    public BoneRetargetingController getRetargeting() {
        return retargeting;
    }
}
