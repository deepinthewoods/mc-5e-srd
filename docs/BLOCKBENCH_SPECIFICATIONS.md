# BlockBench Model Specifications

## Overview

This document provides exact specifications for creating character models in BlockBench for the GeckoLib animation system. Follow these specifications precisely to ensure compatibility with the bone retargeting and layer systems.

## BlockBench Project Setup

### Initial Configuration

1. **Create New Project**:
   - File → New → Bedrock Model
   - Select "GeckoLib Animated Model"
   - Model Identifier: `geometry.character.<race>` (e.g., `geometry.character.dwarf`)

2. **Project Settings**:
   - Model Format: `GeckoLib Model`
   - Texture Size: `64x64` (standard) or `128x128` (detailed)
   - Box UV: **Disabled** (use per-face UV)
   - Export Version: `1.12.0`

3. **File Naming Convention**:
   - Race models: `<race>_<part>.bbmodel`
     - `dwarf_body.bbmodel`
     - `dwarf_heads.bbmodel`
     - `dwarf_limbs.bbmodel`
   - Equipment: `<type>_<category>.bbmodel`
     - `armor_leather.bbmodel`
     - `weapon_swords.bbmodel`

## Standard Humanoid Skeleton

### Bone Hierarchy (Mandatory)

All race models **MUST** use this exact bone structure and naming:

```
root (locator, no geometry)
└── body (main pivot, geometry attached)
    ├── head
    │   ├── head_top (locator for helmet pivot)
    │   ├── head_overlay (for hair/ears that armor can hide)
    │   └── jaw (optional, for facial animations)
    ├── torso_upper
    │   ├── arm_right
    │   │   ├── forearm_right
    │   │   │   ├── hand_right
    │   │   │   │   └── fingers_right (optional)
    │   │   │   └── item_mainhand (locator)
    │   │   └── shoulder_pad_right (locator for armor)
    │   ├── arm_left
    │   │   ├── forearm_left
    │   │   │   ├── hand_left
    │   │   │   │   └── fingers_left (optional)
    │   │   │   └── item_offhand (locator)
    │   │   └── shoulder_pad_left (locator for armor)
    │   └── cape_attach (locator on back)
    └── torso_lower
        ├── leg_right
        │   ├── shin_right
        │   │   └── foot_right
        │   │       └── toe_right (optional)
        │   └── thigh_armor_right (locator)
        ├── leg_left
        │   ├── shin_left
        │   │   └── foot_left
        │   │       └── toe_left (optional)
        │   └── thigh_armor_left (locator)
        └── belt_attach (locator for belts/accessories)
```

**Critical Rules**:
- ✅ Bone names **MUST** match exactly (case-sensitive)
- ✅ Hierarchy **MUST** match exactly
- ✅ Use `_right` and `_left` suffixes (not `_r`/`_l`)
- ✅ Locator bones (no geometry) for attachment points
- ❌ Do NOT add extra bones in the main chain
- ⚠️ Optional bones (jaw, fingers, toes) can be omitted if not needed

### Bone Pivot Points (Reference Positions)

These are reference values for **Human** proportions (1.8m tall). Scale proportionally for other races.

```
Bone Name          | Pivot Position (X, Y, Z) | Notes
-------------------|--------------------------|---------------------------
root               | (0, 0, 0)                | Origin point
body               | (0, 12, 0)               | Base of torso
head               | (0, 24, 0)               | Neck connection
head_top           | (0, 32, 0)               | Top of head
torso_upper        | (0, 18, 0)               | Mid-torso
torso_lower        | (0, 12, 0)               | Waist
arm_right          | (-6, 22, 0)              | Shoulder joint
forearm_right      | (-6, 14, 0)              | Elbow joint
hand_right         | (-6, 8, 0)               | Wrist joint
item_mainhand      | (-6, 8, -2)              | Weapon grip point
arm_left           | (6, 22, 0)               | Shoulder joint
forearm_left       | (6, 14, 0)               | Elbow joint
hand_left          | (6, 8, 0)                | Wrist joint
item_offhand       | (6, 8, -2)               | Shield/item grip
leg_right          | (-2, 12, 0)              | Hip joint
shin_right         | (-2, 6, 0)               | Knee joint
foot_right         | (-2, 0, 0)               | Ankle joint
leg_left           | (2, 12, 0)               | Hip joint
shin_left          | (2, 6, 0)                | Knee joint
foot_left          | (2, 0, 0)                | Ankle joint
```

**Units**: BlockBench units (16 units = 1 Minecraft block = 1 meter)

### Race-Specific Proportions

#### Human (Base Reference)
- **Total Height**: 28.8 units (1.8 blocks)
- **Torso**: 12 units
- **Legs**: 12 units (6 upper, 6 lower)
- **Arms**: 14 units (6 upper, 8 lower)
- **Head**: 4.8 units

#### Dwarf (Shorter, Stocky)
- **Total Height**: 20.8 units (1.3 blocks)
- **Torso**: 10 units (wider geometry)
- **Legs**: 8 units (4 upper, 4 lower)
- **Arms**: 10 units (5 upper, 5 lower)
- **Head**: 5.2 units (proportionally larger)
- **Scaling**: 72% of human height, 120% width on torso

#### Elf (Tall, Slender)
- **Total Height**: 32 units (2.0 blocks)
- **Torso**: 13 units (narrower geometry)
- **Legs**: 14 units (7 upper, 7 lower)
- **Arms**: 15 units (7 upper, 8 lower)
- **Head**: 4.5 units (proportionally smaller)
- **Scaling**: 111% of human height, 80% width

#### Halfling (Smallest)
- **Total Height**: 16 units (1.0 block)
- **Torso**: 8 units
- **Legs**: 7 units (3.5 upper, 3.5 lower)
- **Arms**: 9 units (4 upper, 5 lower)
- **Head**: 4.8 units (same as human, large proportion)
- **Scaling**: 56% of human height

## Geometry Organization

### Variant System

Each body part should have multiple variants within the same file.

#### Example: `dwarf_heads.bbmodel`

```
Outliner Structure:
├── head (bone, shared by all variants)
│   ├── head_variant_0 (folder/group)
│   │   ├── skull_cube
│   │   ├── face_cube
│   │   └── beard_short_cube
│   ├── head_variant_1 (folder/group)
│   │   ├── skull_cube
│   │   ├── face_cube
│   │   └── beard_long_cube
│   ├── head_variant_2 (folder/group)
│   │   ├── skull_cube
│   │   ├── face_cube
│   │   ├── beard_braided_cube
│   │   └── helmet_horned_cube
│   └── head_overlay (bone, for hair/ears)
│       ├── hair_variant_0
│       ├── hair_variant_1
│       └── ears_variant_0
└── head_top (locator bone)
```

**Naming Convention**:
- Variants: `<part>_variant_<number>`
- Sub-elements: Descriptive names (e.g., `beard_long`, `skull_cube`)
- Overlays: `<part>_overlay` (for things armor can hide)

### Body Part Files

#### 1. `<race>_body.bbmodel`

Contains:
- Torso upper geometry variants (0-3+)
- Torso lower geometry variants (0-3+)
- Each variant = different physique (muscular, thin, heavy, etc.)

```
Variants:
- body_variant_0: Standard build
- body_variant_1: Athletic/muscular
- body_variant_2: Heavy/stocky
- body_variant_3: Thin/lean
```

#### 2. `<race>_heads.bbmodel`

Contains:
- Head geometry variants (0-6+)
- Facial features (beards, scars, etc.)
- Hair as separate overlay

```
Variants:
- head_variant_0: Young, clean-shaven
- head_variant_1: Bearded
- head_variant_2: Scarred veteran
- head_variant_3: Elder
- head_variant_4: Female variant 1
- head_variant_5: Female variant 2
- head_variant_6: Female variant 3
```

#### 3. `<race>_limbs.bbmodel`

Contains:
- Arm variants (0-3+)
- Leg variants (0-3+)
- Hand/foot variations

```
Arm Variants:
- arms_variant_0: Standard arms
- arms_variant_1: Muscular arms
- arms_variant_2: Thin arms
- arms_variant_3: Scarred/tattooed

Leg Variants:
- legs_variant_0: Standard legs
- legs_variant_1: Athletic legs
- legs_variant_2: Heavy legs
- legs_variant_3: Digitigrade (optional, for non-standard races)
```

## Equipment/Armor Models

### Armor Files

Each armor set should be a **separate BlockBench project** using the **same skeleton**.

#### Example: `armor_leather.bbmodel`

```
Structure:
├── root (same as character skeleton)
└── body
    ├── head
    │   └── helmet_leather_hood (geometry)
    ├── torso_upper
    │   ├── chest_leather_tunic (geometry)
    │   └── arm_right
    │       └── shoulder_pad_right
    │           └── shoulder_leather_pad (geometry)
    ├── torso_lower
    │   └── belt_leather (geometry)
    └── leg_right
        └── thigh_armor_right
            └── thigh_leather_guard (geometry)
```

**Key Points**:
- ✅ Use **exact same skeleton** as character models
- ✅ Add geometry only to bones that need armor
- ✅ Leave other bones empty (no geometry)
- ✅ Armor should slightly inflate around base body (add 0.5-1 unit padding)
- ✅ Use transparency for partial coverage (leather straps, chainmail gaps)

### Armor Types to Create

1. **Leather Armor** (`armor_leather.bbmodel`)
   - Light coverage, straps and padding
   - Helmet: Hood or cap
   - Chest: Tunic with belt
   - Legs: Thigh guards
   - Boots: Simple leather boots

2. **Chainmail Armor** (`armor_chainmail.bbmodel`)
   - Medium coverage, mail texture
   - Helmet: Coif
   - Chest: Hauberk (long mail shirt)
   - Legs: Mail leggings
   - Boots: Mail-covered boots

3. **Plate Armor** (`armor_plate.bbmodel`)
   - Full coverage, solid plates
   - Helmet: Full helm with visor
   - Chest: Breastplate with pauldrons
   - Legs: Cuisses and greaves
   - Boots: Sabatons (armored boots)

4. **Robes** (`armor_robes.bbmodel`)
   - Cloth coverage, flowing
   - Helmet: Wizard hat or circlet
   - Chest: Long robe with wide sleeves
   - Legs: Robe skirt (covers legs)
   - Boots: Simple shoes (barely visible)

### Weapon Models

Weapons attach to `item_mainhand` or `item_offhand` locators.

#### Example: `weapon_swords.bbmodel`

```
Structure:
├── root
└── body
    └── torso_upper
        └── arm_right
            └── forearm_right
                └── hand_right
                    └── item_mainhand (locator)
                        ├── sword_shortsword (geometry)
                        ├── sword_longsword (geometry)
                        ├── sword_greatsword (geometry)
                        └── sword_rapier (geometry)
```

**Weapon Pivot Points**:
- Sword/Axe: Pommel at origin, blade points forward (+Z)
- Bow: Grip at origin, limbs extend up (+Y)
- Staff: Center grip, ends extend up/down (±Y)
- Shield: Grip at origin, face points outward (+Z)

## Texturing Guidelines

### Texture Layout

#### Character Skin Texture (64x64)

```
UV Layout:
┌─────────────────────────────────────────────────┐
│  Head Front (8x8)    │  Head Right (8x8)        │
│  Head Back (8x8)     │  Head Left (8x8)         │
│  Head Top (8x8)      │  Head Bottom (8x8)       │
├──────────────────────┼──────────────────────────┤
│  Torso Front (8x12)  │  Arm Right Out (4x12)    │
│  Torso Back (8x12)   │  Arm Right In (4x12)     │
├──────────────────────┼──────────────────────────┤
│  Leg Right Out (4x12)│  Leg Right In (4x12)     │
│  Leg Left Out (4x12) │  Leg Left In (4x12)      │
└──────────────────────┴──────────────────────────┘
```

**Or use BlockBench's automatic UV unwrapping:**
- Right-click on a cube → "Auto UV"
- Tools → "Create Texture" → "Blank Template"

### Texture Variants

Each race should have multiple skin textures:

```
textures/entity/character/dwarf/
├── dwarf_skin_0.png    # Pale, light hair
├── dwarf_skin_1.png    # Fair, red hair
├── dwarf_skin_2.png    # Tan, brown hair
├── dwarf_skin_3.png    # Dark, black hair
└── dwarf_overlay.png   # Hair/beard color overlays (optional)
```

### Armor Textures

Armor textures should be separate from skin:

```
textures/entity/equipment/armor/
├── leather_brown.png
├── leather_black.png
├── chainmail_steel.png
├── chainmail_bronze.png
├── plate_steel.png
├── plate_gold.png
└── robes_wizard_blue.png
```

**Transparency**: Use PNG alpha channel for:
- Chainmail gaps (50% transparency)
- Cloth edges
- Straps and buckles

## Animation Setup

### Animation Controller Setup

In BlockBench, create animations in the same project as the model.

#### Required Animations (Create in `locomotion.animation.json`)

1. **idle**
   - Length: 2 seconds (looping)
   - Subtle breathing motion
   - Slight head bob
   - Arms relaxed at sides

2. **walk**
   - Length: 1 second (looping)
   - Full walk cycle (2 steps)
   - Arms swing opposite to legs
   - Slight torso rotation
   - Head stable (minimal bob)

3. **run**
   - Length: 0.6 seconds (looping)
   - Faster walk with more exaggerated motions
   - Arms pump harder
   - Torso leans forward slightly

4. **jump**
   - Length: 0.8 seconds (non-looping)
   - Crouch → Launch → Air → Land
   - Arms swing up during launch
   - Legs tuck during air time

5. **sneak**
   - Length: 1.5 seconds (looping)
   - Crouched walk
   - Body lowered 30%
   - Slower, deliberate steps

#### Combat Animations (Create in `combat.animation.json`)

1. **attack_melee_1h**
   - Length: 0.5 seconds (non-looping)
   - Right arm swings from right to left
   - Torso rotates for power
   - Recovery to idle

2. **attack_melee_2h**
   - Length: 0.7 seconds (non-looping)
   - Both arms swing overhead
   - Big windup
   - Strong follow-through

3. **bow_draw**
   - Length: 0.4 seconds (hold last frame)
   - Left arm extends forward
   - Right arm pulls back to ear
   - Torso turns slightly left

4. **bow_release**
   - Length: 0.3 seconds
   - Right arm releases
   - Recoil/recovery
   - Transition to idle

5. **block_shield**
   - Length: Hold pose
   - Left arm raised
   - Shield covers torso
   - Body braced

#### Magic Animations (Create in `magic.animation.json`)

1. **cast_channeling**
   - Length: 2 seconds (looping)
   - Both arms extended forward
   - Hands gesture in circular motions
   - Subtle glow effect (particle system)

2. **cast_instant**
   - Length: 0.6 seconds
   - Quick gesture with main hand
   - Snap motion
   - Return to idle

3. **cast_ground_target**
   - Length: 0.8 seconds
   - Point down with staff/hand
   - Torso leans forward
   - Hold end pose briefly

### Animation Keyframe Guidelines

**Frame Rate**: 30 FPS (standard for Minecraft)

**Keyframe Spacing**:
- Idle: Keyframes every 0.5-1 second
- Walk/Run: Keyframes every 0.1-0.2 seconds
- Attacks: Keyframes every 0.05-0.1 seconds (more detailed)

**Easing**:
- Idle: Linear or ease-in-out
- Walk: Linear (smooth looping)
- Attacks: Ease-out for windup, ease-in for impact
- Magic: Ease-in-out (smooth gestures)

**Rotation Limits** (for natural motion):
- Head Yaw: ±80 degrees
- Head Pitch: ±45 degrees
- Arm Swing: ±60 degrees
- Leg Swing: ±45 degrees
- Torso Rotation: ±20 degrees

### Bone Animation Properties

For each keyframe, you can animate:
- **Rotation** (X, Y, Z) - Primary animation method
- **Position** (X, Y, Z) - Use sparingly (mainly for retargeting compensation)
- **Scale** (X, Y, Z) - Use for special effects only

**Best Practice**: Animate **rotations only** for most bones. Let the retargeting system handle position adjustments.

## Export Settings

### GeckoLib Export Configuration

1. **File → Export → Export GeckoLib Model**

2. **Export Settings**:
   ```
   ☑ Export Geometry (.geo.json)
   ☑ Export Animations (.animation.json)
   ☑ Minify JSON (production only)
   ☐ Export as Bedrock Model (unchecked, we want GeckoLib)
   ```

3. **File Naming**:
   - Geometry: `<race>_<part>.geo.json` or `<equipment>_<type>.geo.json`
   - Animations: `<category>.animation.json`

4. **Export Location**:
   - Geometry: `src/main/resources/assets/fiveesrd/geo/entity/character/<race>/`
   - Animations: `src/main/resources/assets/fiveesrd/animations/entity/character/`

### Pre-Export Checklist

Before exporting, verify:

- ✅ All bone names match standard skeleton exactly
- ✅ Bone hierarchy is correct
- ✅ Pivot points are set correctly
- ✅ All cubes are parented to correct bones
- ✅ Textures are assigned and UV mapped
- ✅ Animations play smoothly in preview
- ✅ No overlapping geometry (Z-fighting)
- ✅ Model is centered at origin (0, 0, 0)
- ✅ No unused bones or cubes
- ✅ Variant groups are properly named

### Testing in BlockBench

1. **Preview Animations**:
   - Play each animation in loop mode
   - Check for sudden jerks or pops
   - Verify smooth transitions
   - Test at different playback speeds

2. **Test Bone Hierarchy**:
   - Select child bones and move them
   - Verify parent bones move children correctly
   - Test rotation inheritance

3. **Check Variants**:
   - Show/hide variant groups
   - Ensure only one variant visible at a time
   - Verify all share same skeleton

## Common Issues and Solutions

### Issue: Animations look weird on different races

**Cause**: Position keyframes instead of rotation keyframes

**Solution**: Delete position keyframes, use rotations only. Let retargeting handle position scaling.

---

### Issue: Armor clips through body

**Cause**: Armor geometry too close to body geometry

**Solution**: Inflate armor by 0.5-1 units. Use "Inflate" tool in BlockBench.

---

### Issue: Limbs disconnect during animation

**Cause**: Incorrect pivot points

**Solution**: Set pivot to joint location (shoulder, elbow, wrist, etc.)

---

### Issue: Model exports but doesn't show in game

**Cause**: Model identifier mismatch

**Solution**: Check `geometry.character.<race>` matches Java code reference

---

### Issue: Textures appear stretched or wrong

**Cause**: UV mapping incorrect

**Solution**: Use BlockBench UV editor. Right-click cube → "Auto UV" → "Reset"

---

### Issue: Animations don't loop smoothly

**Cause**: First and last keyframes don't match

**Solution**: Copy first keyframe to end. Ensure 360-degree rotations complete.

---

## Quality Checklist

Before finalizing a model:

**Geometry**:
- [ ] Follows standard skeleton exactly
- [ ] All variants properly grouped
- [ ] No unused cubes or bones
- [ ] Reasonable poly count (< 1000 triangles per variant)
- [ ] Clean topology (no overlapping faces)

**Texturing**:
- [ ] All faces UV mapped
- [ ] Texture resolution appropriate (64x64 or 128x128)
- [ ] No stretching or distortion
- [ ] Consistent style across variants
- [ ] Alpha channel used correctly

**Animation**:
- [ ] All required animations present
- [ ] Smooth playback at 30 FPS
- [ ] Proper loop points
- [ ] Natural motion arcs
- [ ] Consistent timing with other races

**Export**:
- [ ] Files named correctly
- [ ] Exported to correct directories
- [ ] JSON files valid (no syntax errors)
- [ ] Model loads in game without errors
- [ ] Textures display correctly

---

## Example Workflow

### Creating a Dwarf Character Model

1. **Setup**:
   - Create new GeckoLib project: `dwarf_body.bbmodel`
   - Set texture size: 64x64
   - Import reference image (optional)

2. **Build Skeleton**:
   - Create bone hierarchy following standard skeleton
   - Set pivot points for dwarf proportions
   - Test bone rotations

3. **Model Body Variant 0**:
   - Create cubes for torso_upper
   - Parent to correct bones
   - Apply UV mapping
   - Create texture

4. **Create Additional Variants**:
   - Duplicate variant 0 group
   - Rename to variant 1
   - Modify geometry (muscular build)
   - Adjust UVs, create new texture region

5. **Create Animations**:
   - Animation → Add Animation: "idle"
   - Set length: 2 seconds, loop
   - Add keyframes for subtle breathing
   - Test preview

6. **Export**:
   - File → Export → Export GeckoLib Model
   - Save to: `assets/fiveesrd/geo/entity/character/dwarf/dwarf_body.geo.json`
   - Export animation to: `assets/fiveesrd/animations/entity/character/locomotion.animation.json`

7. **Repeat for Other Parts**:
   - Create `dwarf_heads.bbmodel`
   - Create `dwarf_limbs.bbmodel`
   - Reuse same skeleton and animations

8. **Test in Game**:
   - Load mod in Minecraft
   - Spawn character entity
   - Verify rendering and animations
   - Iterate as needed

---

**Document Version**: 1.0
**Last Updated**: 2025-11-13
**Status**: Ready for Model Creation
