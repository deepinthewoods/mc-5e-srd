# Combat Animations Specification - Phase 8.1

**Document Version**: 1.0
**Last Updated**: 2025-11-13
**Status**: Specification Complete - Ready for BlockBench Implementation

## Overview

This document provides detailed specifications for creating combat animations in BlockBench for the GeckoLib animation system. These specifications are designed to be followed by artists/animators who will create the actual animations in BlockBench.

**Important**: This document describes the target animations. The actual keyframe data will be created in BlockBench and exported to the `.animation.json` files.

## Table of Contents

1. [General Animation Guidelines](#general-animation-guidelines)
2. [Melee Combat Animations](#melee-combat-animations)
3. [Ranged Combat Animations](#ranged-combat-animations)
4. [Defensive Animations](#defensive-animations)
5. [Animation Integration](#animation-integration)
6. [Keyframe Reference Tables](#keyframe-reference-tables)
7. [Export Instructions](#export-instructions)

---

## General Animation Guidelines

### Animation Principles

All combat animations should follow these principles:

1. **Anticipation**: Wind-up before the action
2. **Follow-through**: Recovery after the action
3. **Weight**: Actions should feel like they have physical weight
4. **Timing**: Fast attacks are snappy, heavy attacks are deliberate
5. **Exaggeration**: Slightly exaggerate movements for readability

### Technical Specifications

- **Frame Rate**: 30 FPS (GeckoLib standard)
- **Rotation Units**: Degrees (BlockBench native)
- **Bone Naming**: Use exact names from `BLOCKBENCH_SPECIFICATIONS.md`
- **Easing Functions**:
  - `linear`: Smooth, constant speed
  - `easeInOut`: Smooth acceleration and deceleration
  - `easeOut`: Fast start, slow end (for impacts)
  - `easeIn`: Slow start, fast end (for wind-ups)

### Bone Rotation Limits (for Natural Motion)

```
Bone              | Rotation Axis | Min    | Max    | Notes
------------------|---------------|--------|--------|------------------
head              | X (pitch)     | -45°   | +45°   | Look up/down
head              | Y (yaw)       | -80°   | +80°   | Look left/right
head              | Z (roll)      | -20°   | +20°   | Head tilt
torso_upper       | Y (twist)     | -45°   | +45°   | Torso rotation
torso_upper       | X (bend)      | -20°   | +30°   | Forward/back bend
arm_right/left    | X (swing)     | -180°  | +180°  | Full rotation
arm_right/left    | Z (out/in)    | -90°   | +45°   | Arm raise/lower
forearm_right/left| X (bend)      | 0°     | -150°  | Elbow bend
hand_right/left   | X (wrist)     | -45°   | +45°   | Wrist articulation
leg_right/left    | X (swing)     | -90°   | +90°   | Hip movement
shin_right/left   | X (bend)      | 0°     | -150°  | Knee bend
foot_right/left   | X (ankle)     | -30°   | +30°   | Foot articulation
```

### Animation Naming Convention

All combat animations follow this pattern:
```
animation.character.combat.<type>_<variant>_<style>

Examples:
- animation.character.combat.attack_melee_1h
- animation.character.combat.attack_melee_2h
- animation.character.combat.attack_unarmed_punch
- animation.character.combat.bow_draw
- animation.character.combat.bow_hold
- animation.character.combat.bow_release
- animation.character.combat.block_shield
- animation.character.combat.block_weapon
```

---

## Melee Combat Animations

### 1. One-Handed Weapon Attack (attack_melee_1h)

**Animation ID**: `animation.character.combat.attack_melee_1h`
**Duration**: 0.5 seconds (15 frames @ 30 FPS)
**Loop**: false (one-shot)
**Purpose**: Standard sword/axe/mace attack from right side

#### Animation Flow

```
Phase 1: Anticipation (0.0s - 0.15s) → Wind-up
Phase 2: Strike (0.15s - 0.25s) → Fast forward motion
Phase 3: Impact (0.25s) → Contact frame
Phase 4: Follow-through (0.25s - 0.4s) → Swing continuation
Phase 5: Recovery (0.4s - 0.5s) → Return to idle
```

#### Detailed Keyframes

**Timestamp: 0.0s (Start - Idle Pose)**
```
body:           rotation [0, 0, 0]
torso_upper:    rotation [0, 0, 0]
head:           rotation [0, 0, 0]
arm_right:      rotation [0, 0, 0]
forearm_right:  rotation [0, 0, 0]
hand_right:     rotation [0, 0, 0]
arm_left:       rotation [0, 0, 0]
```

**Timestamp: 0.15s (Wind-up Peak)**
```
body:           rotation [0, 0, 0]
torso_upper:    rotation [0, -35, -15]    // Twist back and right
head:           rotation [0, 15, 0]        // Look toward target
arm_right:      rotation [-110, 0, -60]    // Raise arm back
forearm_right:  rotation [-25, 0, 0]       // Slight elbow bend
hand_right:     rotation [0, 0, -20]       // Grip rotation
arm_left:       rotation [10, 0, 15]       // Counterbalance
leg_right:      rotation [5, 0, 0]         // Slight weight shift
leg_left:       rotation [-5, 0, 0]        // Brace
```
**Easing from 0.0s → 0.15s**: `easeIn` (accelerate into wind-up)

**Timestamp: 0.25s (Impact Frame - Critical!)**
```
body:           rotation [0, 0, 0]
torso_upper:    rotation [0, 25, 10]       // Twist forward explosively
head:           rotation [0, -10, 0]       // Follow through
arm_right:      rotation [15, 0, 30]       // Swing across body
forearm_right:  rotation [-45, 0, 0]       // Elbow extends
hand_right:     rotation [10, 0, 15]       // Snap through
arm_left:       rotation [-15, 0, -20]     // Pull back for balance
leg_right:      rotation [-8, 0, 0]        // Push off
leg_left:       rotation [8, 0, 0]         // Pivot
foot_right:     rotation [10, 0, 0]        // Toe push
```
**Easing from 0.15s → 0.25s**: `linear` (fast, constant speed strike)

**Timestamp: 0.4s (Follow-through)**
```
body:           rotation [0, 0, 0]
torso_upper:    rotation [0, 35, 15]       // Continue rotation
head:           rotation [0, -15, 0]       // Look at impact
arm_right:      rotation [25, 0, 50]       // Arm extends fully
forearm_right:  rotation [-20, 0, 0]       // Straighten
hand_right:     rotation [15, 0, 20]       // Release grip tension
arm_left:       rotation [-20, 0, -25]     // Full counterbalance
```
**Easing from 0.25s → 0.4s**: `easeOut` (decelerate after impact)

**Timestamp: 0.5s (Recovery - Return to Idle)**
```
body:           rotation [0, 0, 0]
torso_upper:    rotation [0, 5, 0]         // Nearly neutral
head:           rotation [0, 0, 0]
arm_right:      rotation [0, 0, 5]         // Return to guard
forearm_right:  rotation [-5, 0, 0]
hand_right:     rotation [0, 0, 0]
arm_left:       rotation [0, 0, 0]
leg_right:      rotation [0, 0, 0]
leg_left:       rotation [0, 0, 0]
foot_right:     rotation [0, 0, 0]
```
**Easing from 0.4s → 0.5s**: `easeInOut` (smooth return to idle)

#### Additional Notes

- **Weight Distribution**: Character should shift weight from right foot to left foot during swing
- **Shoulder Movement**: shoulder_pad_right should follow arm_right naturally (parented)
- **Held Item**: item_mainhand bone will hold the weapon model (attachment point)
- **Impact Frame**: Frame at 0.25s is the "hit detection" frame in code
- **Retargeting**: This animation uses only rotations, allowing proper retargeting to all races

---

### 2. Two-Handed Weapon Attack (attack_melee_2h)

**Animation ID**: `animation.character.combat.attack_melee_2h`
**Duration**: 0.8 seconds (24 frames @ 30 FPS)
**Loop**: false (one-shot)
**Purpose**: Heavy overhead strike with greatsword/greataxe/maul

#### Animation Flow

```
Phase 1: Anticipation (0.0s - 0.25s) → Big wind-up
Phase 2: Strike (0.25s - 0.45s) → Overhead swing
Phase 3: Impact (0.45s) → Contact frame
Phase 4: Follow-through (0.45s - 0.65s) → Weapon continues down
Phase 5: Recovery (0.65s - 0.8s) → Return to guard
```

#### Detailed Keyframes

**Timestamp: 0.0s (Start)**
```
All bones at idle/neutral position
```

**Timestamp: 0.25s (Wind-up Peak)**
```
body:           rotation [0, 0, 0]
torso_upper:    rotation [-15, 0, 0]       // Lean back
head:           rotation [10, 0, 0]        // Look up slightly
arm_right:      rotation [-150, 0, -35]    // Raise high
forearm_right:  rotation [-45, 0, 0]       // Bend elbow back
hand_right:     rotation [-15, 0, -10]     // Grip adjustment
arm_left:       rotation [-140, 0, 35]     // Mirror right arm
forearm_left:   rotation [-50, 0, 0]       // Both hands on weapon
hand_left:      rotation [-15, 0, 10]      // Grip from other side
leg_right:      rotation [5, 0, 0]         // Slight crouch
leg_left:       rotation [5, 0, 0]         // Balance
shin_right:     rotation [-10, 0, 0]       // Knee bend
shin_left:      rotation [-10, 0, 0]       // Squat preparation
```
**Easing from 0.0s → 0.25s**: `easeIn` (slow, deliberate wind-up)

**Timestamp: 0.45s (Impact Frame)**
```
body:           rotation [10, 0, 0]        // Lean forward
torso_upper:    rotation [25, 0, 0]        // Explosive forward bend
head:           rotation [-5, 0, 0]        // Look down at target
arm_right:      rotation [40, 0, -20]      // Swing down
forearm_right:  rotation [-75, 0, 0]       // Extend through target
hand_right:     rotation [10, 0, -5]       // Grip tight
arm_left:       rotation [35, 0, 20]       // Symmetric swing
forearm_left:   rotation [-80, 0, 0]       // Full extension
hand_left:      rotation [10, 0, 5]        // Maintain grip
leg_right:      rotation [-10, 0, 0]       // Weight forward
leg_left:       rotation [5, 0, 0]         // Rear leg push
shin_right:     rotation [15, 0, 0]        // Extend knee
shin_left:      rotation [-5, 0, 0]        // Push through
foot_right:     rotation [0, 0, 0]         // Plant
foot_left:      rotation [15, 0, 0]        // Toe push
```
**Easing from 0.25s → 0.45s**: `linear` (constant speed through strike)

**Timestamp: 0.65s (Follow-through)**
```
body:           rotation [15, 0, 0]        // Lean further
torso_upper:    rotation [35, 0, 0]        // Full forward bend
arm_right:      rotation [65, 0, -10]      // Arms extended low
forearm_right:  rotation [-40, 0, 0]       // Slight rebound
hand_right:     rotation [20, 0, 0]        // Relax grip slightly
arm_left:       rotation [60, 0, 10]       // Follow through
forearm_left:   rotation [-45, 0, 0]       // Natural bend
hand_left:      rotation [20, 0, 0]        // Release tension
```
**Easing from 0.45s → 0.65s**: `easeOut` (deceleration after impact)

**Timestamp: 0.8s (Recovery)**
```
body:           rotation [0, 0, 0]
torso_upper:    rotation [5, 0, 0]         // Nearly upright
head:           rotation [0, 0, 0]
arm_right:      rotation [-15, 0, -10]     // Guard position
forearm_right:  rotation [-25, 0, 0]       // Ready stance
hand_right:     rotation [0, 0, 0]
arm_left:       rotation [-15, 0, 10]      // Mirror guard
forearm_left:   rotation [-25, 0, 0]
hand_left:      rotation [0, 0, 0]
leg_right:      rotation [0, 0, 0]
leg_left:       rotation [0, 0, 0]
```
**Easing from 0.65s → 0.8s**: `easeInOut` (smooth recovery)

#### Additional Notes

- **Power**: This animation should feel HEAVY - long wind-up, devastating strike
- **Both Hands**: Both arms must move symmetrically, gripping same weapon
- **item_mainhand**: Weapon attached here, both hands touch this attachment point
- **Ground Impact**: Weapon should feel like it slams into the ground
- **Stamina Cost**: In gameplay, this would be a high-stamina attack
- **Impact Frame**: 0.45s is the hit detection frame

---

### 3. Unarmed Attack - Punch (attack_unarmed_punch)

**Animation ID**: `animation.character.combat.attack_unarmed_punch`
**Duration**: 0.35 seconds (10.5 frames @ 30 FPS)
**Loop**: false (one-shot)
**Purpose**: Fast, snappy punch with right fist

#### Animation Flow

```
Phase 1: Wind-up (0.0s - 0.1s) → Pull back fist
Phase 2: Strike (0.1s - 0.18s) → Explosive forward punch
Phase 3: Impact (0.18s) → Contact frame
Phase 4: Recovery (0.18s - 0.35s) → Return to guard
```

#### Detailed Keyframes

**Timestamp: 0.0s (Start)**
```
All bones at idle/combat stance
```

**Timestamp: 0.1s (Wind-up)**
```
body:           rotation [0, 0, 0]
torso_upper:    rotation [0, -25, 0]       // Twist right (wind-up)
head:           rotation [0, 10, 0]        // Look at target
arm_right:      rotation [-15, 0, -45]     // Pull fist back to hip
forearm_right:  rotation [-90, 0, 0]       // Elbow bent 90°
hand_right:     rotation [0, 0, -30]       // Fist clenched
arm_left:       rotation [-30, 0, 20]      // Guard up (protect face)
forearm_left:   rotation [-100, 0, 0]      // High guard
hand_left:      rotation [0, 0, 0]         // Open or fist
leg_right:      rotation [5, -10, 0]       // Rear leg loaded
leg_left:       rotation [-3, 5, 0]        // Front leg braced
```
**Easing from 0.0s → 0.1s**: `easeIn` (coil up)

**Timestamp: 0.18s (Impact Frame)**
```
body:           rotation [0, 0, 0]
torso_upper:    rotation [0, 20, 5]        // Twist left explosively
head:           rotation [0, -5, 0]        // Eyes on target
arm_right:      rotation [10, 0, 15]       // Punch extends forward
forearm_right:  rotation [-25, 0, 0]       // Arm nearly straight
hand_right:     rotation [0, 0, 0]         // Fist aligned
arm_left:       rotation [-35, 0, 25]      // Pull back for power
forearm_left:   rotation [-105, 0, 0]      // Maintain guard
hand_left:      rotation [0, 0, 0]
leg_right:      rotation [-10, 5, 0]       // Push from rear leg
leg_left:       rotation [3, -3, 0]        // Pivot front leg
foot_right:     rotation [15, 0, 0]        // Push through ball of foot
shin_right:     rotation [0, 0, 0]         // Extend leg
```
**Easing from 0.1s → 0.18s**: `linear` (explosive punch speed)

**Timestamp: 0.35s (Recovery)**
```
body:           rotation [0, 0, 0]
torso_upper:    rotation [0, 0, 0]
head:           rotation [0, 0, 0]
arm_right:      rotation [-25, 0, 15]      // Return to guard
forearm_right:  rotation [-95, 0, 0]       // Guard position
hand_right:     rotation [0, 0, 0]
arm_left:       rotation [-30, 0, 20]      // Maintain guard
forearm_left:   rotation [-100, 0, 0]
leg_right:      rotation [0, 0, 0]
leg_left:       rotation [0, 0, 0]
```
**Easing from 0.18s → 0.35s**: `easeOut` (quick recovery)

#### Variant: Unarmed Attack - Kick (attack_unarmed_kick)

**Animation ID**: `animation.character.combat.attack_unarmed_kick`
**Duration**: 0.5 seconds
**Loop**: false

Quick overview (detailed keyframes similar to punch):
- Wind-up: 0.0s - 0.15s (lift knee, balance on standing leg)
- Strike: 0.15s - 0.28s (extend kicking leg forward)
- Impact: 0.28s (leg fully extended)
- Recovery: 0.28s - 0.5s (retract leg, regain balance)

**Key Differences**:
- Primary motion in leg_right/shin_right/foot_right
- torso_upper leans back for balance
- arm_left and arm_right spread for balance
- Standing leg (left) supports full weight

---

## Ranged Combat Animations

### 4. Bow Draw (bow_draw)

**Animation ID**: `animation.character.combat.bow_draw`
**Duration**: 0.4 seconds (12 frames @ 30 FPS)
**Loop**: false (transitions to bow_hold)
**Purpose**: Draw arrow and pull bowstring back

#### Animation Flow

```
Phase 1: Nock Arrow (0.0s - 0.15s) → Bring arrow to bow
Phase 2: Draw (0.15s - 0.4s) → Pull string back
End State: Hold at full draw (transition to bow_hold)
```

#### Detailed Keyframes

**Timestamp: 0.0s (Start - Bow Equipped)**
```
body:           rotation [0, 0, 0]
torso_upper:    rotation [0, -15, 0]       // Slight turn to target
head:           rotation [0, 10, 0]        // Look at target
arm_left:       rotation [-10, 0, 85]      // Hold bow forward
forearm_left:   rotation [-5, 0, 0]        // Arm extended
hand_left:      rotation [0, 0, 45]        // Grip bow handle
arm_right:      rotation [-15, 0, -30]     // Hand near quiver
forearm_right:  rotation [-80, 0, 0]       // Reach for arrow
hand_right:     rotation [0, 0, 0]         // Grabbing arrow
```

**Timestamp: 0.15s (Arrow Nocked)**
```
body:           rotation [0, 0, 0]
torso_upper:    rotation [0, -20, 0]       // Turn more to side
head:           rotation [0, 15, 0]        // Align with target
arm_left:       rotation [-10, 0, 90]      // Bow arm steady
forearm_left:   rotation [0, 0, 0]         // Locked straight
hand_left:      rotation [0, 0, 45]        // Firm grip
arm_right:      rotation [-15, 0, -25]     // Hand touches string
forearm_right:  rotation [-90, 0, 0]       // Elbow up
hand_right:     rotation [0, -15, 0]       // Pinch arrow/string
```
**Easing from 0.0s → 0.15s**: `easeInOut` (smooth preparation)

**Timestamp: 0.4s (Full Draw - Hold)**
```
body:           rotation [0, 0, 0]
torso_upper:    rotation [0, -25, 0]       // Full side turn
head:           rotation [0, 20, -3]       // Eye line to target
arm_left:       rotation [-8, 0, 92]       // Push bow forward
forearm_left:   rotation [0, 0, 0]         // Locked arm
hand_left:      rotation [0, 0, 45]        // Push grip
arm_right:      rotation [-25, 0, -20]     // Pull back to ear/cheek
forearm_right:  rotation [-100, 0, 0]      // Elbow high and back
hand_right:     rotation [0, -20, 5]       // Anchor point at cheek
shoulder_pad_right: rotation [0, 0, -5]    // Natural follow
leg_left:       rotation [0, 5, 0]         // Slight stance adjustment
leg_right:      rotation [0, -5, 0]        // Brace rear leg
```
**Easing from 0.15s → 0.4s**: `easeOut` (increasing tension as draw completes)

#### Additional Notes

- **Bow Attachment**: Bow model attached to item_offhand (left hand)
- **Arrow Visibility**: Arrow should be visible from 0.15s onward
- **String Tension**: Hand_right should anchor at same point (cheek/ear)
- **Shoulder Alignment**: Right shoulder should pull back naturally
- **Eye Line**: Head rotation should aim along arrow shaft
- **Breathing**: Subtle body motion can be added at hold phase

---

### 5. Bow Hold (bow_hold)

**Animation ID**: `animation.character.combat.bow_hold`
**Duration**: 2.0 seconds (60 frames @ 30 FPS)
**Loop**: true (looping hold animation)
**Purpose**: Maintain full draw while aiming

#### Animation Flow

```
Continuous subtle breathing and micro-adjustments
Holds the exact pose from bow_draw at 0.4s with minor variations
```

#### Detailed Keyframes

**Timestamp: 0.0s, 1.0s, 2.0s (Stable Aim)**
```
Same exact pose as bow_draw at 0.4s
```

**Timestamp: 0.5s, 1.5s (Breath In - Slight Variation)**
```
body:           rotation [0, 0, 0]
torso_upper:    rotation [0, -25, 0]       // Same as hold
head:           rotation [0, 20, -3]
arm_left:       rotation [-8, 0, 92]
forearm_left:   rotation [0, 0, 0]
hand_left:      rotation [0, 0, 45]
arm_right:      rotation [-25, 0, -19.5]   // Micro-adjustment (0.5° difference)
forearm_right:  rotation [-100, 0, 0]
hand_right:     rotation [0, -20, 5]
```
**Note**: Only tiny variations (±0.5° to ±1°) to show breathing, very subtle

**Easing**: `linear` (smooth, constant subtle motion)

#### Additional Notes

- **Stamina**: In game logic, stamina drains while holding
- **Aim Stability**: Very minimal movement (archer is focused)
- **Loop Seamlessly**: Must transition smoothly back to start
- **Cancel-able**: Can transition instantly to bow_release or back to idle

---

### 6. Bow Release (bow_release)

**Animation ID**: `animation.character.combat.bow_release`
**Duration**: 0.4 seconds (12 frames @ 30 FPS)
**Loop**: false (one-shot)
**Purpose**: Release arrow and recoil

#### Animation Flow

```
Phase 1: Release (0.0s - 0.05s) → Hand releases string
Phase 2: Recoil (0.05s - 0.2s) → Right hand snaps back
Phase 3: Follow-through (0.2s - 0.3s) → Bow arm holds steady
Phase 4: Recovery (0.3s - 0.4s) → Return to neutral
```

#### Detailed Keyframes

**Timestamp: 0.0s (Start - Held Pose)**
```
Exact same as bow_hold stable position
```

**Timestamp: 0.05s (Release Frame - Arrow Launched)**
```
body:           rotation [0, 0, 0]
torso_upper:    rotation [0, -25, 0]
head:           rotation [0, 20, -3]
arm_left:       rotation [-8, 0, 92]       // Bow arm stays steady
forearm_left:   rotation [0, 0, 0]
hand_left:      rotation [0, 0, 45]
arm_right:      rotation [-30, 0, -15]     // Snap back from string release
forearm_right:  rotation [-95, 0, 0]       // Elbow drops slightly
hand_right:     rotation [0, -10, 10]      // Open hand (release)
```
**Easing from 0.0s → 0.05s**: `linear` (instant release)

**Timestamp: 0.2s (Recoil Peak)**
```
body:           rotation [0, 0, 0]
torso_upper:    rotation [0, -20, 0]       // Slight rotation back
head:           rotation [0, 18, -2]       // Still watching target
arm_left:       rotation [-12, 0, 90]      // Bow arm drifts slightly
forearm_left:   rotation [0, 0, 0]
hand_left:      rotation [0, 0, 45]
arm_right:      rotation [-35, 0, -30]     // Right hand further back
forearm_right:  rotation [-80, 0, 0]       // Natural recoil position
hand_right:     rotation [0, 0, 15]        // Relaxed hand
shoulder_pad_right: rotation [0, 0, 5]     // Follow recoil
```
**Easing from 0.05s → 0.2s**: `easeOut` (decelerate recoil)

**Timestamp: 0.4s (Recovery - Ready)**
```
body:           rotation [0, 0, 0]
torso_upper:    rotation [0, -10, 0]       // Return toward neutral
head:           rotation [0, 10, 0]
arm_left:       rotation [-10, 0, 85]      // Lower bow slightly
forearm_left:   rotation [0, 0, 0]
hand_left:      rotation [0, 0, 45]
arm_right:      rotation [-15, 0, -25]     // Return to nock position
forearm_right:  rotation [-85, 0, 0]
hand_right:     rotation [0, 0, 0]
```
**Easing from 0.2s → 0.4s**: `easeInOut` (smooth recovery)

#### Additional Notes

- **Arrow Release**: At frame 0.05s, arrow entity spawns in game code
- **String Snap**: Bowstring should visually snap forward (if animated)
- **Bow Arm Stability**: Left arm should remain very steady (archer's discipline)
- **Hand Release**: Right hand opens and snaps back
- **Can Chain**: Can immediately transition back to bow_draw for rapid fire

---

## Defensive Animations

### 7. Shield Block (block_shield)

**Animation ID**: `animation.character.combat.block_shield`
**Duration**: Hold pose (0.5s to establish, can hold indefinitely)
**Loop**: true (holding block)
**Purpose**: Raise shield to block incoming attacks

#### Animation Flow

```
Phase 1: Raise Shield (0.0s - 0.2s) → Bring shield up
Phase 2: Brace (0.2s - 0.5s) → Set defensive stance
Phase 3: Hold (0.5s+) → Maintain block (looping)
```

#### Detailed Keyframes

**Timestamp: 0.0s (Start - Neutral/Idle)**
```
All bones at idle position
Shield in item_offhand (left hand)
```

**Timestamp: 0.2s (Shield Rising)**
```
body:           rotation [0, 0, 0]
torso_upper:    rotation [0, 5, -5]        // Slight turn toward shield side
head:           rotation [0, -5, 3]        // Peek over shield
arm_left:       rotation [-45, 0, 45]      // Raise shield
forearm_left:   rotation [-80, 0, 0]       // Bend elbow for shield
hand_left:      rotation [0, 0, 30]        // Grip shield handle
arm_right:      rotation [-10, 0, -15]     // Weapon ready at side
forearm_right:  rotation [-45, 0, 0]
hand_right:     rotation [0, 0, 0]
leg_left:       rotation [0, 5, 0]         // Shift weight
leg_right:      rotation [0, -5, 0]        // Brace rear leg
```
**Easing from 0.0s → 0.2s**: `easeOut` (quick raise, then settle)

**Timestamp: 0.5s (Full Block Pose - Hold)**
```
body:           rotation [-3, 0, 0]        // Lean slightly forward
torso_upper:    rotation [0, 10, -8]       // Turn into shield
head:           rotation [0, -8, 5]        // Look over shield rim
arm_left:       rotation [-55, 0, 55]      // Shield high and forward
forearm_left:   rotation [-90, 0, 0]       // Protect torso
hand_left:      rotation [0, 0, 35]        // Firm grip
shoulder_pad_left: rotation [0, 0, 10]     // Natural follow
arm_right:      rotation [-15, 0, -20]     // Weapon ready to counter
forearm_right:  rotation [-55, 0, 0]       // Guard position
hand_right:     rotation [0, 0, -10]       // Ready grip
leg_left:       rotation [0, 8, 0]         // Front leg bent
leg_right:      rotation [3, -8, 0]        // Rear leg braced
shin_left:      rotation [-12, 0, 0]       // Crouch slightly
shin_right:     rotation [-5, 0, 0]        // Stable stance
foot_left:      rotation [0, 0, 0]         // Plant
foot_right:     rotation [0, 0, 0]         // Plant
```
**Easing from 0.2s → 0.5s**: `easeInOut` (settle into stance)

**Timestamp: 1.0s, 1.5s, 2.0s (Breathing Variation)**
```
Same as 0.5s with very subtle variations:
- body rotation: -3° to -2° (micro-shift)
- No other major changes
This creates a living, breathing block stance
```

#### Additional Notes

- **Shield Model**: Attached to item_offhand
- **Coverage**: Shield should cover torso and part of head
- **Peek Over**: Character's eyes should look over shield top
- **Stamina**: Holding block drains stamina in gameplay
- **Impact Response**: When hit, play separate impact animation (phase 8.2)
- **Can Cancel**: Can instantly transition to attack or lower shield
- **Weight**: Should feel solid and planted

---

### 8. Weapon Parry (block_weapon)

**Animation ID**: `animation.character.combat.block_weapon`
**Duration**: 0.3 seconds (9 frames @ 30 FPS)
**Loop**: false (one-shot, can hold end pose)
**Purpose**: Quick parry with weapon when no shield equipped

#### Animation Flow

```
Phase 1: Raise Guard (0.0s - 0.15s) → Bring weapon up to intercept
Phase 2: Hold Parry (0.15s - 0.3s) → Deflect angle
```

#### Detailed Keyframes

**Timestamp: 0.0s (Start)**
```
Neutral guard position
```

**Timestamp: 0.15s (Parry Active)**
```
body:           rotation [0, 0, 0]
torso_upper:    rotation [0, -15, -10]     // Turn and tilt to deflect
head:           rotation [0, 10, 5]        // Watch incoming attack
arm_right:      rotation [-80, 0, -40]     // Raise weapon across body
forearm_right:  rotation [-70, 0, 0]       // Strong defensive angle
hand_right:     rotation [0, 0, -25]       // Angle blade to deflect
arm_left:       rotation [-20, 0, 35]      // Support/balance
forearm_left:   rotation [-60, 0, 0]
hand_left:      rotation [0, 0, 0]
leg_left:       rotation [0, 3, 0]         // Slight stance shift
leg_right:      rotation [0, -3, 0]
```
**Easing from 0.0s → 0.15s**: `easeOut` (fast raise, then firm)

**Timestamp: 0.3s (Hold Parry)**
```
Same as 0.15s, maintaining defensive position
Character can hold this briefly, then return to guard
```

#### Additional Notes

- **Timing Critical**: Short duration, must be timed with incoming attack
- **Deflection Angle**: Weapon angled to deflect, not block head-on
- **Skillful Defense**: Should look practiced and precise
- **Stamina**: Lower stamina cost than shield block
- **Follow-up**: Can quickly transition to counter-attack
- **One-Handed or Two-Handed**: Works for both (adjust with both arms for 2h)

---

## Animation Integration

### State Machine Integration

Combat animations integrate with the existing animation controller. Here's the priority order:

```
Priority 1: Death/Stun (not covered in this spec)
Priority 2: Combat Actions
  - attack_melee_1h
  - attack_melee_2h
  - attack_unarmed_punch
  - attack_unarmed_kick
  - bow_draw → bow_hold → bow_release
  - block_shield
  - block_weapon
Priority 3: Locomotion (walk, run, sneak)
Priority 4: Idle
```

### Transition Rules

**From Idle to Combat**:
- Immediate transition (0 frame blend)
- Animation plays from start

**From Walk/Run to Combat**:
- Interrupt locomotion immediately
- Combat animation overrides

**Between Combat Animations**:
- Attack animations must finish before next attack
- Block can interrupt charge-up of attack
- Release/recovery can be cancelled into block

**From Combat back to Idle/Walk**:
- Smooth blend over 0.15s (4-5 frames)
- Return to appropriate locomotion state

### Animation Controller Pseudocode

```java
private PlayState combatPredicate(AnimationState<CharacterEntity> state) {
    CharacterEntity entity = state.getAnimatable();

    // Priority 1: Active attack
    if (entity.isAttacking()) {
        WeaponType weapon = entity.getMainHandWeapon();
        if (weapon == WeaponType.BOW) {
            return handleBowAnimation(state, entity);
        } else if (weapon == WeaponType.TWO_HANDED) {
            state.getController().setAnimation(RawAnimation.begin()
                .thenPlay("combat.attack_melee_2h"));
        } else if (weapon == WeaponType.ONE_HANDED) {
            state.getController().setAnimation(RawAnimation.begin()
                .thenPlay("combat.attack_melee_1h"));
        } else {
            // Unarmed
            state.getController().setAnimation(RawAnimation.begin()
                .thenPlay("combat.attack_unarmed_punch"));
        }
        return PlayState.CONTINUE;
    }

    // Priority 2: Blocking
    if (entity.isBlocking()) {
        if (entity.hasShield()) {
            state.getController().setAnimation(RawAnimation.begin()
                .thenLoop("combat.block_shield"));
        } else {
            state.getController().setAnimation(RawAnimation.begin()
                .thenPlay("combat.block_weapon"));
        }
        return PlayState.CONTINUE;
    }

    // Fall back to locomotion controller
    return PlayState.STOP;
}

private PlayState handleBowAnimation(AnimationState<CharacterEntity> state, CharacterEntity entity) {
    BowState bowState = entity.getBowState();

    switch (bowState) {
        case DRAWING:
            state.getController().setAnimation(RawAnimation.begin()
                .thenPlay("combat.bow_draw")
                .transitionLength(0)); // No blend
            break;
        case HOLDING:
            state.getController().setAnimation(RawAnimation.begin()
                .thenLoop("combat.bow_hold"));
            break;
        case RELEASING:
            state.getController().setAnimation(RawAnimation.begin()
                .thenPlay("combat.bow_release"));
            break;
    }

    return PlayState.CONTINUE;
}
```

### Retargeting Compatibility

All combat animations MUST:
- Use **rotations only** for primary motion (no position keyframes on limbs)
- Position keyframes allowed only on root/body for balance shifts
- This ensures bone retargeting works correctly for all races
- Dwarf will have shorter reach, Elf longer reach, but motion looks natural

---

## Keyframe Reference Tables

### Standard Combat Stance (Reference Pose)

Use this as the starting point for guard/ready positions:

```
Bone            | Rotation [X, Y, Z] | Notes
----------------|--------------------|-----------------------
body            | [0, 0, 0]          | Upright
torso_upper     | [0, 0, 0]          | Neutral
head            | [0, 0, 0]          | Looking forward
arm_right       | [-20, 0, -10]      | Weapon at side, ready
forearm_right   | [-45, 0, 0]        | Natural guard bend
hand_right      | [0, 0, 0]          | Gripping weapon
arm_left        | [-15, 0, 10]       | Guard or balance
forearm_left    | [-50, 0, 0]        | Bent, ready
hand_left       | [0, 0, 0]          | Open or shield grip
leg_right       | [0, 0, 0]          | Neutral stance
leg_left        | [0, 0, 0]          | Neutral stance
shin_right      | [0, 0, 0]          | Straight
shin_left       | [0, 0, 0]          | Straight
```

### Easing Function Quick Reference

```
Function    | Graph Shape      | Use Case
------------|------------------|----------------------------------
linear      | Straight line    | Fast strikes, constant motion
easeIn      | Slow → Fast      | Wind-ups, anticipation
easeOut     | Fast → Slow      | Impacts, follow-through
easeInOut   | Slow→Fast→Slow   | Smooth transitions, returns
```

### Timing Guidelines

```
Action Type          | Duration Range | Notes
---------------------|----------------|-------------------------------
Light attack (1h)    | 0.4s - 0.6s    | Fast, responsive
Heavy attack (2h)    | 0.7s - 1.0s    | Powerful, commitment
Unarmed strike       | 0.3s - 0.5s    | Very fast
Bow draw             | 0.3s - 0.5s    | Skill-based
Bow release          | 0.3s - 0.4s    | Recoil and recovery
Shield block raise   | 0.2s - 0.3s    | Quick defense
Block hold           | Indefinite     | Stamina-limited
Parry window         | 0.1s - 0.2s    | Timing-critical
```

---

## Export Instructions

### File Locations

Combat animations must be exported to:
```
src/main/resources/assets/fiveesrd/animations/entity/character/combat.animation.json
```

### BlockBench Export Steps

1. **Open BlockBench** with your character model (any race, preferably human as reference)

2. **Create Animation**:
   - Click "Animation" tab
   - Click "+" to add new animation
   - Name it exactly as specified (e.g., `animation.character.combat.attack_melee_1h`)
   - Set duration (in seconds)
   - Set loop: true or false as specified

3. **Create Keyframes**:
   - Follow keyframe tables in this document
   - Select bone → Move timeline cursor → Press "+" for keyframe
   - Use rotation values from specifications
   - Set easing curves as specified

4. **Preview Animation**:
   - Play animation in BlockBench
   - Verify smooth motion
   - Check for bone disconnections
   - Adjust as needed (artists have creative freedom for polish)

5. **Export**:
   - File → Export → Export GeckoLib Animations
   - Save as `combat.animation.json`
   - Place in correct directory

### Quality Checklist

Before exporting, verify:
- [ ] Animation name matches specification exactly
- [ ] Duration is correct
- [ ] Loop setting is correct
- [ ] All keyframes use rotations (no position on limbs)
- [ ] Easing curves applied appropriately
- [ ] Animation plays smoothly
- [ ] No bone popping or disconnections
- [ ] Motion feels weighty and natural
- [ ] Timing matches gameplay needs (fast enough, not too slow)

---

## Implementation Checklist

### Phase 8.1 Combat Animations - Tasks

**Melee Animations**:
- [ ] attack_melee_1h - One-handed weapon swing
- [ ] attack_melee_2h - Two-handed overhead strike
- [ ] attack_unarmed_punch - Quick punch
- [ ] attack_unarmed_kick - Front kick (optional)

**Ranged Animations**:
- [ ] bow_draw - Draw arrow and bowstring
- [ ] bow_hold - Maintain full draw while aiming
- [ ] bow_release - Release arrow and recoil

**Defensive Animations**:
- [ ] block_shield - Raise shield to block
- [ ] block_weapon - Quick weapon parry

**Code Integration**:
- [ ] Update CharacterEntity with combat state tracking
- [ ] Add animation controller for combat priority
- [ ] Implement bow state machine (draw → hold → release)
- [ ] Add blocking state management
- [ ] Test all animations with retargeting on all 4 races

**Testing**:
- [ ] Verify animations play correctly
- [ ] Test animation interrupts and transitions
- [ ] Verify retargeting works (Dwarf, Elf, Halfling)
- [ ] Test held item rendering during combat
- [ ] Performance test with multiple entities

---

## Additional Notes for Artists

### Creative Freedom

While this specification provides exact keyframe timings and rotations, **artists should feel empowered to**:
- Add additional in-between keyframes for polish
- Adjust timing slightly if it improves feel (±0.05s)
- Add secondary motion (head turn, finger flex, etc.)
- Experiment with easing curves
- Add personality to animations

### What Must Stay Exact

- Animation names (code depends on these)
- Loop settings (true/false)
- Approximate duration (±0.1s acceptable)
- Impact frame timing (hit detection relies on this)
- Use rotations, not positions (for retargeting)

### Reference Materials

Good reference for combat animations:
- Medieval fighting manuals (real sword techniques)
- Archery instructional videos (proper form)
- Martial arts demonstrations (unarmed combat)
- Other games with good combat feel (Dark Souls, Monster Hunter)

### Questions?

Refer to:
- `BLOCKBENCH_SPECIFICATIONS.md` - Bone structure and naming
- `ANIMATION_RETARGETING.md` - Why rotations matter
- BlockBench documentation: https://blockbench.net/wiki/

---

**Document Version**: 1.0
**Author**: Implementation Team
**Status**: Ready for Artist Implementation
**Last Updated**: 2025-11-13

**Next Steps**: Create these animations in BlockBench and export to `combat.animation.json`
