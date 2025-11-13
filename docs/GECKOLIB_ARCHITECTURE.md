# GeckoLib Animation & Bone Retargeting Architecture

## Overview

This document describes the complete architecture for migrating from the current custom GLTF system to a GeckoLib-based animation system with bone retargeting, modular layering, and runtime character assembly.

## Design Goals

1. **Bone Retargeting**: Share animations across creatures with same skeleton but different bone lengths (dwarf vs elf proportions)
2. **Modular Layers**: Mix and match geometric layers (heads, torsos, limbs) and texture layers (clothing, armor) at runtime
3. **Size Scaling**: Support 0.5x to 3x creature sizes with adaptive walk animations
4. **Animation Reuse**: Maximum sharing of keyframe animations across all humanoid creatures
5. **Maintainability**: Clean separation between models, animations, and runtime logic

## System Architecture

### High-Level Components

```
┌─────────────────────────────────────────────────────────────┐
│                     Character Entity                         │
│  - Race (Dwarf, Elf, Human, Halfling)                       │
│  - Size Scale (0.5x - 3.0x)                                  │
│  - Layer Configuration (body parts, equipment)               │
└──────────────────────┬──────────────────────────────────────┘
                       │
                       ▼
┌─────────────────────────────────────────────────────────────┐
│              GeckoLib Animation Controller                   │
│  - Animation State Machine                                   │
│  - Bone Retargeting Layer                                    │
│  - Layer Assembly & Rendering                                │
└──────────────────────┬──────────────────────────────────────┘
                       │
        ┌──────────────┼──────────────┐
        ▼              ▼              ▼
┌──────────────┐ ┌──────────┐ ┌─────────────┐
│  Base Model  │ │Equipment │ │ Animations  │
│  Geometry    │ │  Layers  │ │   (.json)   │
│  (.geo.json) │ │(.geo.json│ │             │
└──────────────┘ └──────────┘ └─────────────┘
```

### Core Technologies

- **GeckoLib 4.x**: Animation library for Fabric/Minecraft
- **BlockBench**: Model and animation creation tool
- **Custom Retargeting**: Bone length scaling algorithm
- **Layer System**: Runtime model composition

## Architecture Layers

### 1. Model Layer (BlockBench → .geo.json)

**Base Character Models**: One model per race with modular geometry

```
models/entity/characters/
├── base_humanoid.geo.json          # Shared skeleton definition
├── dwarf_body_variants.geo.json    # Dwarf body part geometries
├── elf_body_variants.geo.json      # Elf body part geometries
├── human_body_variants.geo.json    # Human body part geometries
└── halfling_body_variants.geo.json # Halfling body part geometries
```

**Equipment/Armor Models**: Separate layer geometries

```
models/entity/equipment/
├── armor_head.geo.json      # Helmets, hats, crowns
├── armor_chest.geo.json     # Chest plates, robes, tunics
├── armor_legs.geo.json      # Leg armor, pants
├── armor_feet.geo.json      # Boots, shoes
├── weapons_mainhand.geo.json
└── weapons_offhand.geo.json
```

### 2. Animation Layer (.animation.json)

**Shared Animations**: Created once, retargeted to all races

```
animations/entity/
├── humanoid_locomotion.animation.json
│   ├── idle
│   ├── walk
│   ├── run
│   ├── sneak
│   ├── jump
│   ├── fall
│   └── swim
├── humanoid_combat.animation.json
│   ├── attack_melee_1h
│   ├── attack_melee_2h
│   ├── attack_unarmed
│   ├── bow_draw
│   ├── bow_hold
│   ├── bow_release
│   ├── block_shield
│   └── hit_reaction
├── humanoid_magic.animation.json
│   ├── cast_channeling
│   ├── cast_instant
│   ├── cast_ground_target
│   └── cast_self_buff
├── humanoid_emotes.animation.json
│   ├── wave
│   ├── point
│   ├── cheer
│   ├── bow_greeting
│   └── shrug
└── race_specific/
    ├── dwarf_animations.animation.json
    └── elf_animations.animation.json
```

### 3. Bone Retargeting Layer (Java)

**Purpose**: Translate animations from base skeleton to race-specific proportions

**Key Classes**:
- `BoneRetargetingController`: Main retargeting logic
- `SkeletonProfile`: Defines bone lengths for each race
- `RetargetedAnimation`: Wrapper around GeckoLib animations with scaling

**Algorithm**:
1. Load animation keyframes (rotation, translation, scale)
2. For each bone, look up target race's bone length vs base bone length
3. Scale translation keyframes proportionally
4. Preserve rotation keyframes (angles stay same)
5. Apply global scale multiplier for creature size
6. Special case: Walk/run animations scale stride length and speed

### 4. Layer Assembly Layer (Java)

**Purpose**: Combine multiple geometry layers at runtime

**Key Classes**:
- `LayerConfiguration`: Defines which meshes to render
- `EquipmentLayerRenderer`: Renders equipment over base model
- `CharacterModelAssembler`: Composites all layers

**Process**:
1. Render base body part (e.g., body_variant_2)
2. Apply base textures
3. Render equipment layers in order (chest armor, then cape, etc.)
4. Apply equipment textures with transparency
5. Handle visibility (helmets can hide hair layer)

### 5. Animation Controller (Java)

**Purpose**: State machine for animation transitions

**Key Classes**:
- `CharacterAnimationController`: Extends `GeoAnimatable`
- `AnimationState`: Enum of all animation states
- `TransitionHandler`: Smooth blending between animations

**States**:
- **Locomotion**: idle → walk → run (transition based on speed)
- **Combat**: idle → attack → idle (interruptible)
- **Action**: casting, using items (blocks other animations)
- **Reaction**: hit reactions, death (highest priority)

## Bone Retargeting Deep Dive

### Standard Humanoid Skeleton

All races share this bone hierarchy:

```
root
└── body
    ├── head
    │   ├── head_top (for helmets)
    │   └── jaw (optional, for talking)
    ├── torso_upper
    │   ├── arm_right
    │   │   ├── forearm_right
    │   │   │   └── hand_right
    │   │   └── shoulder_armor_right
    │   └── arm_left
    │       ├── forearm_left
    │       │   └── hand_left
    │       └── shoulder_armor_left
    └── torso_lower
        ├── leg_right
        │   ├── shin_right
        │   │   └── foot_right
        │   └── thigh_armor_right
        └── leg_left
            ├── shin_left
            │   └── foot_left
            └── thigh_armor_left
```

**Total Bones**: ~20-25 (depending on detail level)

### Race Skeleton Profiles

Each race defines bone lengths (in Minecraft units, 1 unit = 1 meter):

#### Human (Base Reference)
```java
{
  "torso_upper": 0.6,
  "torso_lower": 0.4,
  "arm_right": 0.4,
  "forearm_right": 0.35,
  "leg_right": 0.5,
  "shin_right": 0.45,
  "head": 0.3,
  "total_height": 1.8
}
```

#### Dwarf (Shorter, Stockier)
```java
{
  "torso_upper": 0.5,
  "torso_lower": 0.35,
  "arm_right": 0.35,
  "forearm_right": 0.3,
  "leg_right": 0.35,
  "shin_right": 0.3,
  "head": 0.32,  // Larger head proportionally
  "total_height": 1.3
}
```

#### Elf (Taller, Slender)
```java
{
  "torso_upper": 0.65,
  "torso_lower": 0.45,
  "arm_right": 0.45,
  "forearm_right": 0.4,
  "leg_right": 0.6,
  "shin_right": 0.55,
  "head": 0.28,
  "total_height": 2.0
}
```

#### Halfling (Smallest)
```java
{
  "torso_upper": 0.4,
  "torso_lower": 0.3,
  "arm_right": 0.3,
  "forearm_right": 0.25,
  "leg_right": 0.3,
  "shin_right": 0.25,
  "head": 0.3,  // Large head ratio
  "total_height": 1.0
}
```

### Retargeting Algorithm

```java
RetargetedKeyframe retarget(Keyframe original, Bone bone, SkeletonProfile targetRace) {
    // 1. Get bone length ratio
    float baseBoneLength = BASE_SKELETON_PROFILE.getBoneLength(bone.name);
    float targetBoneLength = targetRace.getBoneLength(bone.name);
    float lengthRatio = targetBoneLength / baseBoneLength;

    // 2. Scale translations (position changes)
    Vector3f newPosition = original.position.mul(lengthRatio);

    // 3. Keep rotations unchanged (angles work at any scale)
    Quaternion newRotation = original.rotation.copy();

    // 4. Keep scale unchanged (or apply global scale for size variant)
    Vector3f newScale = original.scale.copy();

    // 5. Special case: stride length for walk/run
    if (isLocomotionAnimation() && isLegBone(bone)) {
        // Longer legs = longer stride, but animation plays at same speed
        // This is already handled by position scaling above
    }

    return new RetargetedKeyframe(newPosition, newRotation, newScale, original.timestamp);
}
```

### Walk Animation Speed Adaptation

For walking/running animations to look natural:

```java
float getAnimationSpeed(AnimationType type, SkeletonProfile race, float sizeScale) {
    float baseSpeed = type.getBaseSpeed();

    if (type == AnimationType.WALK || type == AnimationType.RUN) {
        // Longer legs = naturally faster walk cycle
        float legLengthRatio = race.getLegLength() / BASE_LEG_LENGTH;
        float scaleRatio = sizeScale; // 0.5x to 3.0x

        // Combined ratio (e.g., tall elf at 2x scale walks much faster)
        float speedMultiplier = legLengthRatio * scaleRatio;

        return baseSpeed * speedMultiplier;
    }

    // Other animations play at normal speed regardless of size
    return baseSpeed;
}
```

## Layer System Deep Dive

### Layer Types

#### 1. Base Geometry Layers
- **Purpose**: Core body shape for each race
- **Variants**: Multiple options per body part (different faces, body types, etc.)
- **Rendering**: Always visible (unless replaced by full-coverage armor)

#### 2. Clothing Layers
- **Purpose**: Base outfits, civilian clothes
- **Texture-based**: Often just texture swaps on base geometry
- **Examples**: Tunics, robes, peasant clothes

#### 3. Armor Layers
- **Purpose**: Protective equipment with geometric changes
- **Separate Meshes**: New geometry that overlays base model
- **Examples**: Plate armor chest, chainmail sleeves, leather boots

#### 4. Equipment Layers
- **Purpose**: Held items, weapons, shields
- **Parent Bone**: Attached to hand bones
- **Examples**: Swords, staves, torches, shields

### Layer Rendering Order

```
1. Base body geometry (torso, legs, arms)
2. Base head geometry
3. Underclothes layer (if any)
4. Main clothing layer
5. Armor layer (chest)
6. Armor layer (legs)
7. Armor layer (arms)
8. Armor layer (feet)
9. Armor layer (head) - may hide hair/ears
10. Cape/cloak layer (backside)
11. Equipment layer (weapons in hands)
12. Effects layer (magic auras, buffs)
```

### Layer Configuration Format

```java
public class LayerConfiguration {
    // Base body parts
    public int bodyVariant = 0;     // 0-3+
    public int legsVariant = 0;     // 0-3+
    public int armsVariant = 0;     // 0-3+
    public int headVariant = 0;     // 0-6+

    // Equipment slots
    public String helmetModel = null;  // null = none
    public String chestArmorModel = null;
    public String legArmorModel = null;
    public String bootArmorModel = null;
    public String capeModel = null;
    public String mainHandModel = null;
    public String offHandModel = null;

    // Texture overrides
    public String skinTexture = "default";
    public String clothingTexture = "default";
    public Map<String, String> armorTextures = new HashMap<>();

    // Visibility flags
    public boolean showHair = true;  // Hidden if helmet present
    public boolean showEars = true;  // Hidden if helmet present
    public boolean showCape = true;
}
```

## File Organization

### Recommended Structure

```
src/main/resources/assets/fiveesrd/
│
├── geo/                          # GeckoLib geometry files
│   ├── entity/
│   │   ├── character/
│   │   │   ├── base/
│   │   │   │   ├── humanoid_skeleton.geo.json
│   │   │   │   └── bone_reference.geo.json
│   │   │   ├── dwarf/
│   │   │   │   ├── dwarf_body.geo.json     # All body variants
│   │   │   │   ├── dwarf_heads.geo.json    # All head variants
│   │   │   │   └── dwarf_limbs.geo.json    # Arms/legs variants
│   │   │   ├── elf/
│   │   │   │   ├── elf_body.geo.json
│   │   │   │   ├── elf_heads.geo.json
│   │   │   │   └── elf_limbs.geo.json
│   │   │   ├── human/
│   │   │   │   ├── human_body.geo.json
│   │   │   │   ├── human_heads.geo.json
│   │   │   │   └── human_limbs.geo.json
│   │   │   └── halfling/
│   │   │       ├── halfling_body.geo.json
│   │   │       ├── halfling_heads.geo.json
│   │   │       └── halfling_limbs.geo.json
│   │   └── equipment/
│   │       ├── armor/
│   │       │   ├── leather_armor.geo.json
│   │       │   ├── chainmail_armor.geo.json
│   │       │   ├── plate_armor.geo.json
│   │       │   └── robes.geo.json
│   │       ├── weapons/
│   │       │   ├── swords.geo.json
│   │       │   ├── axes.geo.json
│   │       │   ├── bows.geo.json
│   │       │   └── staves.geo.json
│   │       └── accessories/
│   │           ├── capes.geo.json
│   │           ├── belts.geo.json
│   │           └── backpacks.geo.json
│
├── animations/                   # GeckoLib animation files
│   └── entity/
│       └── character/
│           ├── locomotion.animation.json
│           ├── combat.animation.json
│           ├── magic.animation.json
│           ├── emotes.animation.json
│           └── race_specific/
│               ├── dwarf_special.animation.json
│               └── elf_special.animation.json
│
└── textures/                     # Texture atlases
    └── entity/
        └── character/
            ├── base/
            │   ├── dwarf_skin_0.png
            │   ├── dwarf_skin_1.png
            │   ├── elf_skin_0.png
            │   ├── human_skin_0.png
            │   └── ...
            ├── clothing/
            │   ├── tunic_red.png
            │   ├── robe_blue.png
            │   └── ...
            └── armor/
                ├── leather_brown.png
                ├── chainmail_steel.png
                ├── plate_gold.png
                └── ...
```

### Java Source Structure

```
src/main/java/ninja/trek/srd/
│
├── client/
│   ├── model/
│   │   └── geckolib/
│   │       ├── CharacterGeoModel.java
│   │       ├── EquipmentGeoModel.java
│   │       └── LayerGeoModel.java
│   ├── render/
│   │   ├── geckolib/
│   │   │   ├── CharacterGeoRenderer.java
│   │   │   ├── EquipmentLayerRenderer.java
│   │   │   └── LayerRenderManager.java
│   │   └── animation/
│   │       ├── CharacterAnimationController.java
│   │       ├── BoneRetargetingController.java
│   │       └── AnimationStateManager.java
│   └── registry/
│       ├── ModGeoModels.java
│       └── ModAnimations.java
│
└── character/
    ├── skeleton/
    │   ├── SkeletonProfile.java
    │   ├── HumanoidBones.java
    │   └── BoneRetargeting.java
    ├── layer/
    │   ├── LayerConfiguration.java
    │   ├── LayerType.java
    │   └── EquipmentLayer.java
    └── animation/
        ├── AnimationType.java
        ├── AnimationSpeed.java
        └── RetargetedAnimation.java
```

## Implementation Considerations

### GeckoLib Integration Points

1. **Entity Implementation**:
   - `CharacterEntity` must implement `GeoEntity`
   - Override `registerControllers()` for animation controllers
   - Override `getAnimatableInstanceCache()` for animation caching

2. **Renderer Implementation**:
   - Extend `GeoEntityRenderer<CharacterEntity>`
   - Override `getTextureLocation()` for dynamic textures
   - Implement layer rendering in `preRender()` and `postRender()`

3. **Model Implementation**:
   - Extend `GeoModel<CharacterEntity>`
   - Override `getModelResource()` to select correct race model
   - Override `getTextureResource()` for layer textures
   - Override `getAnimationResource()` for animation selection

### Performance Optimizations (Future)

1. **Model Caching**: Pre-load all race models at startup
2. **Animation Baking**: Pre-calculate retargeted animations per race
3. **LOD System**: Simpler models/animations for distant entities
4. **Culling**: Don't render hidden layers (armor covering body)
5. **Batching**: Group entities with same model/texture for batch rendering

### Compatibility Notes

- **Minecraft Version**: 1.21+ (current codebase target)
- **GeckoLib Version**: 4.4+ (for Fabric 1.21)
- **Fabric API**: Required modules:
  - `fabric-rendering-v1`
  - `fabric-resource-loader-v0`
  - `fabric-networking-api-v1` (for layer sync)

## Next Steps

See the following documents for detailed specifications:

1. **BLOCKBENCH_SPECIFICATIONS.md**: Exact model structure, bone naming, export settings
2. **ANIMATION_RETARGETING.md**: Detailed retargeting algorithms and math
3. **LAYER_SYSTEM.md**: Runtime layer assembly, equipment system details
4. **IMPLEMENTATION_PLAN.md**: Step-by-step implementation phases

---

**Document Version**: 1.0
**Last Updated**: 2025-11-13
**Status**: Design Phase - Ready for Implementation
