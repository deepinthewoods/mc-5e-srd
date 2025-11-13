package ninja.trek.srd.client.render.animation;

import ninja.trek.srd.character.skeleton.SkeletonProfile;
import org.joml.Vector3f;

/**
 * Handles bone retargeting for animations across different race skeletons.
 * Adapts animations created for the base skeleton to work with different proportions.
 *
 * Based on ANIMATION_RETARGETING.md specification.
 */
public class BoneRetargetingController {

    private final SkeletonProfile sourceProfile;

    public BoneRetargetingController(SkeletonProfile sourceProfile) {
        this.sourceProfile = sourceProfile;
    }

    /**
     * Retarget a position (translation) from source skeleton to target skeleton.
     * Scales the position proportionally based on bone length ratios.
     *
     * @param boneName The bone being animated
     * @param originalPosition The original position from the animation
     * @param targetProfile The target skeleton profile
     * @param sizeScale Global size scale (0.5 - 3.0)
     * @return The retargeted position
     */
    public Vector3f retargetPosition(String boneName, Vector3f originalPosition,
                                    SkeletonProfile targetProfile, float sizeScale) {
        // Get bone length ratio between target and source
        float lengthRatio = targetProfile.getBoneLengthRatio(boneName);

        // Scale the position by both the bone ratio and global scale
        float totalScale = lengthRatio * sizeScale;

        return new Vector3f(
            originalPosition.x * totalScale,
            originalPosition.y * totalScale,
            originalPosition.z * totalScale
        );
    }

    /**
     * Calculate animation speed multiplier for locomotion animations.
     * Longer legs move faster, shorter legs move slower.
     *
     * @param targetProfile The target skeleton profile
     * @param sizeScale Global size scale
     * @param isLocomotion Whether this is a walk/run animation
     * @return Speed multiplier for the animation
     */
    public float calculateAnimationSpeed(SkeletonProfile targetProfile, float sizeScale, boolean isLocomotion) {
        if (!isLocomotion) {
            // Non-locomotion animations play at normal speed
            return 1.0f;
        }

        // For walking/running, speed is based on leg length
        float legLengthRatio = targetProfile.getLegLengthRatio();

        // Combine leg length ratio with size scale
        // A 2x sized elf with long legs walks much faster than a 0.5x sized dwarf
        return legLengthRatio * sizeScale;
    }

    /**
     * Check if a bone name refers to a leg bone.
     */
    public static boolean isLegBone(String boneName) {
        return boneName.contains("leg") || boneName.contains("shin") || boneName.contains("foot");
    }

    /**
     * Check if a bone name refers to an arm bone.
     */
    public static boolean isArmBone(String boneName) {
        return boneName.contains("arm") || boneName.contains("hand");
    }
}
