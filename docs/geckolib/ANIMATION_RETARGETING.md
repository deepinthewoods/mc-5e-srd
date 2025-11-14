# Animation Retargeting Technical Specification

## Overview

This document provides the mathematical foundations and implementation details for bone retargeting animations across different creature proportions while maintaining natural motion.

## Core Concept

**Animation Retargeting** adapts keyframe animations created for one skeleton to work on another skeleton with the same bone hierarchy but different bone lengths.

### Example Scenario

```
Animation created for Human (1.8m tall):
- Walk cycle with 0.8m stride length
- Arms swing with 0.4m total range

Applied to Dwarf (1.3m tall):
- Automatically scales to 0.58m stride length
- Arms swing with 0.29m total range
- SAME joint rotations (angles preserved)
- Natural motion maintained
```

## Mathematical Foundation

### Bone Length Ratio

The fundamental retargeting value is the bone length ratio:

```
ratio = target_bone_length / source_bone_length
```

**Example**:
```java
// Human forearm: 0.35m
// Dwarf forearm: 0.30m
float ratio = 0.30f / 0.35f = 0.857f

// Translation offset of 0.10m in human animation
// Becomes: 0.10m * 0.857 = 0.0857m in dwarf animation
```

### Transform Components

Each keyframe contains three transform components:

1. **Translation (Position)** - Scales with bone length ratio
2. **Rotation (Angles)** - Preserved across all skeletons
3. **Scale** - Usually preserved, optional global scaling

## Retargeting Algorithms

### Algorithm 1: Basic Position Retargeting

Used for most bones (arms, legs, torso).

```java
public class BasicBoneRetargeting {

    public Vector3f retargetPosition(
        Vector3f sourcePosition,
        String boneName,
        SkeletonProfile targetProfile
    ) {
        // Get bone length ratio
        float ratio = getBoneLengthRatio(boneName, targetProfile);

        // Scale position by ratio
        return new Vector3f(
            sourcePosition.x * ratio,
            sourcePosition.y * ratio,
            sourcePosition.z * ratio
        );
    }

    private float getBoneLengthRatio(String boneName, SkeletonProfile target) {
        BoneDefinition sourceBone = SOURCE_SKELETON.getBone(boneName);
        BoneDefinition targetBone = target.getBone(boneName);

        return targetBone.length / sourceBone.length;
    }
}
```

**When to use**: All translational movements (step forward, arm reach, body sway)

---

### Algorithm 2: Rotation Preservation

Rotations are NOT scaled - they're copied directly.

```java
public Quaternion retargetRotation(
    Quaternion sourceRotation,
    String boneName,
    SkeletonProfile targetProfile
) {
    // Rotations are preserved exactly
    return new Quaternion(sourceRotation);
}
```

**Why?**: A 45-degree elbow bend looks natural at any arm length. Scaling rotations would create unnatural poses.

---

### Algorithm 3: Chain Length Compensation

For bones in a chain (shoulder → elbow → wrist), compensate for cumulative length differences.

```java
public class ChainRetargeting {

    public Vector3f retargetInChain(
        Vector3f sourcePosition,
        String boneName,
        SkeletonProfile targetProfile,
        List<String> chainBones
    ) {
        // Calculate cumulative ratio from root to this bone
        float cumulativeRatio = 1.0f;

        for (String chainBone : chainBones) {
            if (chainBone.equals(boneName)) break;
            cumulativeRatio *= getBoneLengthRatio(chainBone, targetProfile);
        }

        // Apply local bone ratio
        float localRatio = getBoneLengthRatio(boneName, targetProfile);

        // Combine ratios
        return new Vector3f(
            sourcePosition.x * cumulativeRatio * localRatio,
            sourcePosition.y * cumulativeRatio * localRatio,
            sourcePosition.z * cumulativeRatio * localRatio
        );
    }
}
```

**Example Chain**: `arm_right → forearm_right → hand_right`
- If arm is 90% of source and forearm is 85% of source
- Hand position scales by: 0.90 × 0.85 = 0.765

---

### Algorithm 4: Locomotion Stride Scaling

Walk and run animations need special handling for stride length.

```java
public class LocomotionRetargeting {

    public Vector3f retargetLocomotion(
        Vector3f sourcePosition,
        String boneName,
        SkeletonProfile targetProfile,
        AnimationType animType
    ) {
        // Get leg length ratio (for stride)
        float legRatio = getLegLengthRatio(targetProfile);

        // Get overall height ratio (for vertical motion)
        float heightRatio = targetProfile.totalHeight / SOURCE_SKELETON.totalHeight;

        if (boneName.contains("leg") || boneName.contains("shin") || boneName.contains("foot")) {
            // Stride length scales with leg length
            return new Vector3f(
                sourcePosition.x * legRatio,  // Forward/backward stride
                sourcePosition.y * heightRatio, // Vertical bob
                sourcePosition.z * legRatio   // Side-to-side sway
            );
        } else if (boneName.contains("arm") || boneName.contains("hand")) {
            // Arm swing scales with arm length
            float armRatio = getArmLengthRatio(targetProfile);
            return new Vector3f(
                sourcePosition.x * armRatio,
                sourcePosition.y * armRatio,
                sourcePosition.z * armRatio
            );
        } else {
            // Body/head use standard retargeting
            return retargetPosition(sourcePosition, boneName, targetProfile);
        }
    }

    private float getLegLengthRatio(SkeletonProfile target) {
        float sourceLegLength = SOURCE_SKELETON.getBone("leg_right").length
                              + SOURCE_SKELETON.getBone("shin_right").length;
        float targetLegLength = target.getBone("leg_right").length
                              + target.getBone("shin_right").length;
        return targetLegLength / sourceLegLength;
    }
}
```

**Result**: Dwarves take shorter steps, elves take longer steps, but animation timing stays synchronized.

---

### Algorithm 5: Global Size Scaling

Apply additional scaling for size variants (0.5x to 3.0x).

```java
public class SizeScaledRetargeting {

    public Vector3f retargetWithSizeScale(
        Vector3f basePosition,
        String boneName,
        SkeletonProfile targetProfile,
        float sizeScale  // 0.5 to 3.0
    ) {
        // First apply bone retargeting
        Vector3f retargeted = retargetPosition(basePosition, boneName, targetProfile);

        // Then apply global size scale
        return retargeted.mul(sizeScale);
    }

    public float getAnimationSpeed(
        AnimationType animType,
        SkeletonProfile targetProfile,
        float sizeScale
    ) {
        float baseSpeed = animType.getBaseSpeed();

        if (animType.isLocomotion()) {
            // Walk/run speed scales with size
            float legRatio = getLegLengthRatio(targetProfile);
            return baseSpeed * legRatio * sizeScale;
        } else {
            // Attack/cast animations play at normal speed
            return baseSpeed;
        }
    }
}
```

**Example**:
- Tiny pixie (0.5x size): Walks at 0.5x speed (tiny steps)
- Huge ogre (3.0x size): Walks at 3.0x speed (giant strides)
- Both attack at normal speed (balance consideration)

---

## Skeleton Profile Data Structure

### Base Definition

```java
public class SkeletonProfile {
    public final String raceName;
    public final float totalHeight;
    public final Map<String, BoneDefinition> bones;

    public static class BoneDefinition {
        public final String name;
        public final float length;        // Bone length in meters
        public final Vector3f pivot;      // Pivot point in local space
        public final String parent;       // Parent bone name

        // Optional: bone width/thickness (for collision)
        public final float width;
        public final float depth;
    }
}
```

### Human Profile (Reference)

```java
public static final SkeletonProfile HUMAN = new SkeletonProfile.Builder()
    .raceName("human")
    .totalHeight(1.8f)
    .addBone("body", 0.6f, new Vector3f(0, 12, 0), null)
    .addBone("head", 0.3f, new Vector3f(0, 24, 0), "body")
    .addBone("torso_upper", 0.6f, new Vector3f(0, 18, 0), "body")
    .addBone("torso_lower", 0.4f, new Vector3f(0, 12, 0), "body")
    .addBone("arm_right", 0.4f, new Vector3f(-6, 22, 0), "torso_upper")
    .addBone("forearm_right", 0.35f, new Vector3f(-6, 14, 0), "arm_right")
    .addBone("hand_right", 0.15f, new Vector3f(-6, 8, 0), "forearm_right")
    .addBone("arm_left", 0.4f, new Vector3f(6, 22, 0), "torso_upper")
    .addBone("forearm_left", 0.35f, new Vector3f(6, 14, 0), "arm_left")
    .addBone("hand_left", 0.15f, new Vector3f(6, 8, 0), "forearm_left")
    .addBone("leg_right", 0.5f, new Vector3f(-2, 12, 0), "torso_lower")
    .addBone("shin_right", 0.45f, new Vector3f(-2, 6, 0), "leg_right")
    .addBone("foot_right", 0.1f, new Vector3f(-2, 0, 0), "shin_right")
    .addBone("leg_left", 0.5f, new Vector3f(2, 12, 0), "torso_lower")
    .addBone("shin_left", 0.45f, new Vector3f(2, 6, 0), "leg_left")
    .addBone("foot_left", 0.1f, new Vector3f(2, 0, 0), "shin_left")
    .build();
```

### Dwarf Profile

```java
public static final SkeletonProfile DWARF = new SkeletonProfile.Builder()
    .raceName("dwarf")
    .totalHeight(1.3f)
    .addBone("body", 0.5f, new Vector3f(0, 10, 0), null)
    .addBone("head", 0.32f, new Vector3f(0, 18, 0), "body")
    .addBone("torso_upper", 0.5f, new Vector3f(0, 14, 0), "body")
    .addBone("torso_lower", 0.35f, new Vector3f(0, 10, 0), "body")
    .addBone("arm_right", 0.35f, new Vector3f(-7, 17, 0), "torso_upper")  // Wider shoulders
    .addBone("forearm_right", 0.3f, new Vector3f(-7, 11, 0), "arm_right")
    .addBone("hand_right", 0.12f, new Vector3f(-7, 6, 0), "forearm_right")
    .addBone("arm_left", 0.35f, new Vector3f(7, 17, 0), "torso_upper")
    .addBone("forearm_left", 0.3f, new Vector3f(7, 11, 0), "arm_left")
    .addBone("hand_left", 0.12f, new Vector3f(7, 6, 0), "forearm_left")
    .addBone("leg_right", 0.35f, new Vector3f(-2, 10, 0), "torso_lower")
    .addBone("shin_right", 0.3f, new Vector3f(-2, 5, 0), "leg_right")
    .addBone("foot_right", 0.08f, new Vector3f(-2, 0, 0), "shin_right")
    .addBone("leg_left", 0.35f, new Vector3f(2, 10, 0), "torso_lower")
    .addBone("shin_left", 0.3f, new Vector3f(2, 5, 0), "leg_left")
    .addBone("foot_left", 0.08f, new Vector3f(2, 0, 0), "shin_left")
    .build();
```

### Elf Profile

```java
public static final SkeletonProfile ELF = new SkeletonProfile.Builder()
    .raceName("elf")
    .totalHeight(2.0f)
    .addBone("body", 0.65f, new Vector3f(0, 13, 0), null)
    .addBone("head", 0.28f, new Vector3f(0, 26, 0), "body")
    .addBone("torso_upper", 0.65f, new Vector3f(0, 20, 0), "body")
    .addBone("torso_lower", 0.45f, new Vector3f(0, 13, 0), "body")
    .addBone("arm_right", 0.45f, new Vector3f(-5, 24, 0), "torso_upper")  // Narrower shoulders
    .addBone("forearm_right", 0.4f, new Vector3f(-5, 16, 0), "arm_right")
    .addBone("hand_right", 0.17f, new Vector3f(-5, 9, 0), "forearm_right")
    .addBone("arm_left", 0.45f, new Vector3f(5, 24, 0), "torso_upper")
    .addBone("forearm_left", 0.4f, new Vector3f(5, 16, 0), "arm_left")
    .addBone("hand_left", 0.17f, new Vector3f(5, 9, 0), "forearm_left")
    .addBone("leg_right", 0.6f, new Vector3f(-2, 13, 0), "torso_lower")
    .addBone("shin_right", 0.55f, new Vector3f(-2, 7, 0), "leg_right")
    .addBone("foot_right", 0.12f, new Vector3f(-2, 0, 0), "shin_right")
    .addBone("leg_left", 0.6f, new Vector3f(2, 13, 0), "torso_lower")
    .addBone("shin_left", 0.55f, new Vector3f(2, 7, 0), "leg_left")
    .addBone("foot_left", 0.12f, new Vector3f(2, 0, 0), "shin_left")
    .build();
```

### Halfling Profile

```java
public static final SkeletonProfile HALFLING = new SkeletonProfile.Builder()
    .raceName("halfling")
    .totalHeight(1.0f)
    .addBone("body", 0.4f, new Vector3f(0, 8, 0), null)
    .addBone("head", 0.3f, new Vector3f(0, 14, 0), "body")  // Proportionally large
    .addBone("torso_upper", 0.4f, new Vector3f(0, 11, 0), "body")
    .addBone("torso_lower", 0.3f, new Vector3f(0, 8, 0), "body")
    .addBone("arm_right", 0.3f, new Vector3f(-5, 13, 0), "torso_upper")
    .addBone("forearm_right", 0.25f, new Vector3f(-5, 8, 0), "arm_right")
    .addBone("hand_right", 0.1f, new Vector3f(-5, 5, 0), "forearm_right")
    .addBone("arm_left", 0.3f, new Vector3f(5, 13, 0), "torso_upper")
    .addBone("forearm_left", 0.25f, new Vector3f(5, 8, 0), "arm_left")
    .addBone("hand_left", 0.1f, new Vector3f(5, 5, 0), "forearm_left")
    .addBone("leg_right", 0.3f, new Vector3f(-2, 8, 0), "torso_lower")
    .addBone("shin_right", 0.25f, new Vector3f(-2, 4, 0), "leg_right")
    .addBone("foot_right", 0.07f, new Vector3f(-2, 0, 0), "shin_right")
    .addBone("leg_left", 0.3f, new Vector3f(2, 8, 0), "torso_lower")
    .addBone("shin_left", 0.25f, new Vector3f(2, 4, 0), "leg_left")
    .addBone("foot_left", 0.07f, new Vector3f(2, 0, 0), "shin_left")
    .build();
```

---

## Implementation Classes

### Core Retargeting Engine

```java
public class BoneRetargetingController {

    private final SkeletonProfile sourceProfile;
    private final Map<String, SkeletonProfile> targetProfiles;
    private final Map<String, RetargetingCache> cache;

    public BoneRetargetingController(SkeletonProfile source) {
        this.sourceProfile = source;
        this.targetProfiles = new HashMap<>();
        this.cache = new HashMap<>();

        // Register all race profiles
        registerProfile(HUMAN);
        registerProfile(DWARF);
        registerProfile(ELF);
        registerProfile(HALFLING);
    }

    /**
     * Retarget an entire animation to a target skeleton
     */
    public RetargetedAnimation retargetAnimation(
        Animation sourceAnimation,
        String targetRace,
        float sizeScale
    ) {
        SkeletonProfile targetProfile = targetProfiles.get(targetRace);

        // Create new animation with retargeted keyframes
        RetargetedAnimation result = new RetargetedAnimation(
            sourceAnimation.getName(),
            sourceAnimation.getLength()
        );

        // Process each bone track
        for (BoneAnimation boneAnim : sourceAnimation.getBoneAnimations()) {
            String boneName = boneAnim.getBoneName();

            // Retarget all keyframes for this bone
            List<RetargetedKeyframe> newKeyframes = new ArrayList<>();

            for (Keyframe kf : boneAnim.getKeyframes()) {
                newKeyframes.add(retargetKeyframe(
                    kf,
                    boneName,
                    targetProfile,
                    sizeScale,
                    sourceAnimation.getType()
                ));
            }

            result.addBoneAnimation(boneName, newKeyframes);
        }

        // Adjust animation speed for locomotion
        if (sourceAnimation.getType().isLocomotion()) {
            result.setSpeedMultiplier(
                calculateLocomotionSpeed(targetProfile, sizeScale)
            );
        }

        return result;
    }

    /**
     * Retarget a single keyframe
     */
    private RetargetedKeyframe retargetKeyframe(
        Keyframe source,
        String boneName,
        SkeletonProfile targetProfile,
        float sizeScale,
        AnimationType animType
    ) {
        // Get retargeting algorithm based on bone and animation type
        RetargetingStrategy strategy = selectStrategy(boneName, animType);

        // Apply retargeting
        Vector3f newPosition = strategy.retargetPosition(
            source.getPosition(),
            boneName,
            targetProfile,
            sizeScale
        );

        Quaternion newRotation = strategy.retargetRotation(
            source.getRotation(),
            boneName,
            targetProfile
        );

        Vector3f newScale = strategy.retargetScale(
            source.getScale(),
            boneName,
            sizeScale
        );

        return new RetargetedKeyframe(
            newPosition,
            newRotation,
            newScale,
            source.getTimestamp(),
            source.getEasing()
        );
    }

    /**
     * Select appropriate retargeting strategy
     */
    private RetargetingStrategy selectStrategy(String boneName, AnimationType animType) {
        if (animType.isLocomotion()) {
            return new LocomotionRetargetingStrategy();
        } else if (isChainBone(boneName)) {
            return new ChainRetargetingStrategy();
        } else {
            return new BasicRetargetingStrategy();
        }
    }

    /**
     * Calculate walk speed multiplier
     */
    private float calculateLocomotionSpeed(SkeletonProfile target, float sizeScale) {
        float legRatio = getLegLengthRatio(target);
        return legRatio * sizeScale;
    }
}
```

### Retargeting Strategy Interface

```java
public interface RetargetingStrategy {

    Vector3f retargetPosition(
        Vector3f sourcePosition,
        String boneName,
        SkeletonProfile targetProfile,
        float sizeScale
    );

    Quaternion retargetRotation(
        Quaternion sourceRotation,
        String boneName,
        SkeletonProfile targetProfile
    );

    Vector3f retargetScale(
        Vector3f sourceScale,
        String boneName,
        float sizeScale
    );
}
```

### Caching System

```java
public class RetargetingCache {

    private final Map<CacheKey, RetargetedAnimation> cache;

    private static class CacheKey {
        final String animationName;
        final String targetRace;
        final float sizeScale;

        // hashCode() and equals() implementation
    }

    public RetargetedAnimation getOrCompute(
        Animation source,
        String targetRace,
        float sizeScale,
        BoneRetargetingController controller
    ) {
        CacheKey key = new CacheKey(source.getName(), targetRace, sizeScale);

        return cache.computeIfAbsent(key, k ->
            controller.retargetAnimation(source, targetRace, sizeScale)
        );
    }

    public void invalidate() {
        cache.clear();
    }
}
```

---

## Integration with GeckoLib

### Custom Animation Controller

```java
public class CharacterAnimationController extends AnimationController<CharacterEntity> {

    private final BoneRetargetingController retargeting;

    public CharacterAnimationController(CharacterEntity entity) {
        super(entity, "controller", 0, CharacterAnimationController::predicate);
        this.retargeting = new BoneRetargetingController(SkeletonProfile.HUMAN);
    }

    private static PlayState predicate(AnimationState<CharacterEntity> state) {
        CharacterEntity entity = state.getAnimatable();

        // Get current animation based on entity state
        Animation sourceAnimation = selectAnimation(entity);

        // Retarget to entity's race and size
        RetargetedAnimation retargeted = retargeting.retargetAnimation(
            sourceAnimation,
            entity.getRace().getName(),
            entity.getSizeScale()
        );

        // Play retargeted animation
        state.getController().setAnimation(retargeted);

        return PlayState.CONTINUE;
    }
}
```

### GeckoLib Model Integration

```java
public class CharacterGeoModel extends GeoModel<CharacterEntity> {

    @Override
    public void setCustomAnimations(CharacterEntity entity, long instanceId, AnimationState<CharacterEntity> state) {
        super.setCustomAnimations(entity, instanceId, state);

        // GeckoLib will call this for each bone
        // Retargeting is already applied in the animation controller
        // This method can add runtime modifications (head tracking, etc.)

        IBone head = getAnimationProcessor().getBone("head");
        if (head != null) {
            // Add head tracking on top of retargeted animation
            float headYaw = entity.getHeadYaw() - entity.getBodyYaw();
            float headPitch = entity.getPitch();

            head.setRotationY(head.getRotationY() + headYaw * Mth.DEG_TO_RAD);
            head.setRotationX(head.getRotationX() + headPitch * Mth.DEG_TO_RAD);
        }
    }
}
```

---

## Testing and Validation

### Visual Comparison Test

Create a test scene with all races performing the same animation:

```java
public class RetargetingTest {

    public void testWalkAnimation() {
        Animation walkAnim = loadAnimation("walk");

        // Spawn all races side by side
        CharacterEntity human = spawnCharacter(Race.HUMAN, new Vec3(0, 0, 0));
        CharacterEntity dwarf = spawnCharacter(Race.DWARF, new Vec3(2, 0, 0));
        CharacterEntity elf = spawnCharacter(Race.ELF, new Vec3(4, 0, 0));
        CharacterEntity halfling = spawnCharacter(Race.HALFLING, new Vec3(6, 0, 0));

        // All should walk in sync (relative to their size)
        // Verify:
        // - Stride lengths scale correctly
        // - No limb disconnection
        // - Natural motion for each race
        // - Animation timing synchronized
    }
}
```

### Numerical Validation

```java
public void validateRetargeting() {
    Animation testAnim = createTestAnimation();

    for (SkeletonProfile profile : getAllProfiles()) {
        RetargetedAnimation retargeted = retargetAnimation(testAnim, profile, 1.0f);

        // Validate keyframe count matches
        assertEquals(testAnim.getKeyframeCount(), retargeted.getKeyframeCount());

        // Validate rotations preserved
        for (BoneAnimation boneAnim : retargeted.getBoneAnimations()) {
            for (int i = 0; i < boneAnim.getKeyframes().size(); i++) {
                Quaternion sourceRot = testAnim.getKeyframe(i).getRotation();
                Quaternion targetRot = boneAnim.getKeyframe(i).getRotation();

                assertQuaternionEquals(sourceRot, targetRot, 0.001f);
            }
        }

        // Validate positions scaled
        float expectedRatio = profile.totalHeight / HUMAN.totalHeight;
        // ... validation logic
    }
}
```

---

## Performance Optimization

### Pre-Baking Animations

For production, pre-compute retargeted animations at startup:

```java
public class AnimationRegistry {

    private final Map<AnimationKey, RetargetedAnimation> bakedAnimations;

    public void bakeAllAnimations() {
        List<Animation> allAnimations = loadAllAnimations();
        List<SkeletonProfile> allProfiles = getAllProfiles();

        for (Animation anim : allAnimations) {
            for (SkeletonProfile profile : allProfiles) {
                // Bake at common size scales
                for (float scale : new float[]{0.5f, 1.0f, 1.5f, 2.0f, 3.0f}) {
                    AnimationKey key = new AnimationKey(anim.getName(), profile.raceName, scale);
                    RetargetedAnimation baked = retargetAnimation(anim, profile, scale);
                    bakedAnimations.put(key, baked);
                }
            }
        }

        System.out.println("Baked " + bakedAnimations.size() + " animation variants");
    }

    public RetargetedAnimation getAnimation(String name, String race, float scale) {
        // Round scale to nearest baked value
        float nearestScale = roundToNearest(scale, new float[]{0.5f, 1.0f, 1.5f, 2.0f, 3.0f});

        AnimationKey key = new AnimationKey(name, race, nearestScale);
        return bakedAnimations.get(key);
    }
}
```

**Memory Usage**: ~50-100 KB per animation variant (acceptable trade-off)

---

## Edge Cases and Limitations

### Issue 1: Very Small Creatures (< 0.5x)

**Problem**: Animations may look jittery at very small scales

**Solution**: Set minimum scale threshold or create specialized animations for tiny creatures

---

### Issue 2: Very Large Creatures (> 3.0x)

**Problem**: Walk speed becomes extremely fast, may look unrealistic

**Solution**: Cap walk speed multiplier at 2.0x, or create separate "giant" animations

---

### Issue 3: Non-Humanoid Creatures

**Problem**: Retargeting assumes humanoid structure

**Solution**: Create separate skeleton profiles for quadrupeds, flying creatures, etc. Use same retargeting principles but with different bone hierarchies.

---

## Future Enhancements

### Procedural Adjustments

Add subtle procedural modifications on top of retargeted animations:

- **Fatigue**: Slow down animations over time
- **Injury**: Limp on damaged leg
- **Mood**: Slump shoulders when low morale
- **Terrain**: Adapt stride to slopes

### IK (Inverse Kinematics)

Add foot IK for uneven terrain:
- Plant feet on ground regardless of slope
- Adjust leg rotations procedurally
- Works with any retargeted animation

### Blend Spaces

Interpolate between retargeted animations based on speed:
- 0-2 m/s: Walk animation
- 2-4 m/s: Blend walk → run
- 4+ m/s: Run animation
- Retargeting applies to all blend states

---

**Document Version**: 1.0
**Last Updated**: 2025-11-13
**Status**: Ready for Implementation
