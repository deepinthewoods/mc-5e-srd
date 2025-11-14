# Magic/Spellcasting Animations Specification - Phase 8.2

**Document Version**: 1.0
**Last Updated**: 2025-11-13
**Status**: Ready for Implementation
**Phase**: 8.2 - Magic/Spellcasting Animations

---

## Table of Contents

1. [Overview](#overview)
2. [Animation Categories](#animation-categories)
3. [Detailed Animation Specifications](#detailed-animation-specifications)
4. [Keyframe Reference](#keyframe-reference)
5. [Animation Priority System](#animation-priority-system)
6. [Integration with Existing Animations](#integration-with-existing-animations)
7. [Locomotion During Casting](#locomotion-during-casting)
8. [BlockBench Implementation Guide](#blockbench-implementation-guide)
9. [Technical Integration](#technical-integration)
10. [Testing & Validation](#testing--validation)

---

## Overview

This document provides comprehensive specifications for magic and spellcasting animations in the GeckoLib animation system. These animations are designed to work with the bone retargeting system, ensuring natural motion across all character races (Human, Dwarf, Elf, Halfling).

### Design Philosophy

**Magic animations should:**
- Be visually distinct from combat animations
- Support different spell schools (evocation, conjuration, etc.)
- Allow for movement during certain cast types
- Integrate seamlessly with the animation priority system
- Work naturally with bone retargeting across all races

### Animation Naming Convention

All magic animations follow this pattern:
```
animation.character.spell.<type>_<variant>
```

Examples:
- `animation.character.spell.cast_instant`
- `animation.character.spell.channel_concentrated`
- `animation.character.spell.ground_target`

---

## Animation Categories

### 1. Instant Cast Spells
**Use Case**: Quick spells (Magic Missile, Shield, Counterspell)
**Duration**: 0.4-0.6 seconds
**Looping**: No
**Movement**: Allowed (walk speed)

### 2. Channeled Spells
**Use Case**: Sustained effects (Healing, Eldritch Blast, Ray of Frost)
**Duration**: 1.5-3.0 seconds
**Looping**: Yes
**Movement**: Limited (slow walk only)

### 3. Ground Target Spells
**Use Case**: Area spells (Fireball, Ice Storm, Moonbeam)
**Duration**: 1.0-1.5 seconds
**Looping**: No
**Movement**: Not allowed during cast

### 4. Touch Spells
**Use Case**: Melee range spells (Cure Wounds, Shocking Grasp)
**Duration**: 0.8-1.2 seconds
**Looping**: No
**Movement**: Not allowed during cast

### 5. Concentration Prep
**Use Case**: Preparing concentration spells (Bless, Haste)
**Duration**: 1.2 seconds
**Looping**: No, transitions to concentration_hold
**Movement**: Not allowed during cast

### 6. Concentration Hold
**Use Case**: Maintaining concentration (continuous effect)
**Duration**: 2.0 seconds
**Looping**: Yes
**Movement**: Allowed (normal speed)

---

## Detailed Animation Specifications

### Animation 1: Instant Cast (Single Hand)

**File**: `magic.animation.json`
**Animation Name**: `animation.character.spell.cast_instant`
**Duration**: 0.5 seconds
**Frame Rate**: 30 FPS
**Loop**: False
**Priority**: 80 (higher than combat, lower than critical actions)

#### Purpose
Quick gesture for instant spells (Magic Missile, Shield, Minor Illusion)

#### Keyframes

**Body** (subtle support movement):
```
0.0s  [0, 0, 0]       - Neutral
0.1s  [2, -10, 0]     - Slight lean and turn
0.3s  [1, -5, 0]      - Recovery starts
0.5s  [0, 0, 0]       - Return to neutral
```

**Torso Upper** (torso twist for power):
```
0.0s  [0, 0, 0]       - Neutral
0.1s  [0, -15, 0]     - Twist away (windup)
0.2s  [0, 5, 0]       - Snap forward (cast moment)
0.5s  [0, 0, 0]       - Return to neutral
```

**Arm Right** (main casting arm):
```
0.0s  [0, 0, 0]       - Relaxed at side
0.1s  [-45, 15, -20]  - Pull back and up (gather energy)
0.2s  [-90, 0, 0]     - Extended forward (release)
0.3s  [-70, 5, 5]     - Slight recoil
0.5s  [0, 0, 0]       - Return to rest
```

**Forearm Right** (hand gesture):
```
0.0s  [0, 0, 0]       - Neutral
0.1s  [-30, 0, 0]     - Bend at elbow (energy gathering)
0.2s  [10, 0, 0]      - Snap straight (release)
0.5s  [0, 0, 0]       - Return to neutral
```

**Hand Right** (finger gesture):
```
0.0s  [0, 0, 0]       - Open hand
0.1s  [0, 0, 10]      - Fingers curl (gather)
0.2s  [0, 0, -15]     - Fingers extend/spread (release)
0.5s  [0, 0, 0]       - Return to neutral
```

**Arm Left** (support/balance):
```
0.0s  [0, 0, 0]       - Relaxed
0.1s  [-20, -10, 10]  - Pull back for balance
0.2s  [-30, -5, 15]   - Extended slightly
0.5s  [0, 0, 0]       - Return to rest
```

**Head** (focus on target):
```
0.0s  [0, 0, 0]       - Looking forward
0.1s  [-2, -5, 0]     - Focus intensifies
0.2s  [2, 0, 0]       - Follow-through
0.5s  [0, 0, 0]       - Return to neutral
```

**Legs** (stable stance):
```
0.0s  [0, 0, 0]       - Neutral stance
0.1s  [2, 0, 0]       - Slight knee bend for stability
0.5s  [0, 0, 0]       - Return to neutral
```

#### Easing
- **0.0s → 0.1s**: Ease-out (slow start, quick finish for windup)
- **0.1s → 0.2s**: Linear (instant snap for cast)
- **0.2s → 0.5s**: Ease-in-out (smooth recovery)

#### Particle Timing
- **0.15s**: Begin particle effect (pre-cast glow)
- **0.2s**: Main particle burst (spell release)
- **0.25s**: Trail particles

---

### Animation 2: Channeled Spell (Both Hands)

**Animation Name**: `animation.character.spell.channel_concentrated`
**Duration**: 2.0 seconds
**Loop**: True
**Priority**: 85 (prevents interruption easily)

#### Purpose
Continuous channeling for sustained spells (Eldritch Blast, Healing, Ray of Frost)

#### Keyframes

**Body** (gentle sway):
```
0.0s  [0, 0, 0]       - Neutral
0.5s  [1, 0, 2]       - Slight lean right
1.0s  [0, 0, 0]       - Center
1.5s  [1, 0, -2]      - Slight lean left
2.0s  [0, 0, 0]       - Return to start (loop)
```

**Torso Upper** (breathing):
```
0.0s  [0, 0, 0]       - Neutral
0.5s  [1, 0, 0]       - Inhale (chest up)
1.0s  [0, 0, 0]       - Neutral
1.5s  [1, 0, 0]       - Inhale
2.0s  [0, 0, 0]       - Return to start
```

**Arm Right** (sustained position):
```
0.0s  [-80, 0, -10]   - Extended forward, slightly out
0.5s  [-82, 0, -12]   - Subtle variation (fatigue)
1.0s  [-80, 0, -10]   - Return to base
1.5s  [-82, 0, -8]    - Subtle variation
2.0s  [-80, 0, -10]   - Return to start
```

**Arm Left** (sustained position):
```
0.0s  [-80, 0, 10]    - Extended forward, slightly out
0.5s  [-82, 0, 8]     - Subtle variation
1.0s  [-80, 0, 10]    - Return to base
1.5s  [-82, 0, 12]    - Subtle variation
2.0s  [-80, 0, 10]    - Return to start
```

**Forearm Right** (subtle movement):
```
0.0s  [0, 0, 0]       - Straight
0.5s  [3, 0, 0]       - Micro-bend
1.0s  [0, 0, 0]       - Straight
1.5s  [3, 0, 0]       - Micro-bend
2.0s  [0, 0, 0]       - Return to start
```

**Forearm Left** (subtle movement):
```
0.0s  [0, 0, 0]       - Straight
0.5s  [3, 0, 0]       - Micro-bend
1.0s  [0, 0, 0]       - Straight
1.5s  [3, 0, 0]       - Micro-bend
2.0s  [0, 0, 0]       - Return to start
```

**Hand Right** (gentle pulse):
```
0.0s  [0, 0, 0]       - Neutral spread
0.5s  [0, 0, 5]       - Fingers extend
1.0s  [0, 0, 0]       - Neutral
1.5s  [0, 0, 5]       - Fingers extend
2.0s  [0, 0, 0]       - Return to start
```

**Hand Left** (gentle pulse):
```
0.0s  [0, 0, 0]       - Neutral spread
0.5s  [0, 0, -5]      - Fingers extend
1.0s  [0, 0, 0]       - Neutral
1.5s  [0, 0, -5]      - Fingers extend
2.0s  [0, 0, 0]       - Return to start
```

**Head** (focused concentration):
```
0.0s  [-5, 0, 0]      - Tilted forward slightly
0.5s  [-4, 1, 0]      - Micro-adjustment
1.0s  [-5, 0, 0]      - Return
1.5s  [-4, -1, 0]     - Micro-adjustment
2.0s  [-5, 0, 0]      - Return to start
```

**Legs** (grounded stance):
```
All keyframes: [0, 0, 0] - Stable, no movement
```

#### Easing
- All keyframes: Ease-in-out (smooth, continuous motion)

#### Particle Timing
- **Continuous**: Steady stream from hands
- **0.5s intervals**: Pulse effect synchronized with hand movements

---

### Animation 3: Ground Target Spell

**Animation Name**: `animation.character.spell.ground_target`
**Duration**: 1.2 seconds
**Loop**: False
**Priority**: 85

#### Purpose
Dramatic targeting for area spells (Fireball, Ice Storm, Call Lightning)

#### Keyframes

**Body** (dramatic lean):
```
0.0s  [0, 0, 0]       - Neutral
0.2s  [-15, 0, 0]     - Lean back (gather power)
0.5s  [20, 0, 0]      - Lean forward dramatically (point)
0.8s  [15, 0, 0]      - Hold position
1.2s  [0, 0, 0]       - Return to neutral
```

**Torso Upper** (upper body drive):
```
0.0s  [0, 0, 0]       - Neutral
0.2s  [-20, 0, 0]     - Lean back
0.5s  [25, 0, 0]      - Push forward
0.8s  [20, 0, 0]      - Hold
1.2s  [0, 0, 0]       - Return
```

**Arm Right** (main pointing arm):
```
0.0s  [0, 0, 0]       - Relaxed
0.2s  [-120, 20, -30] - Raised up and back (gather)
0.4s  [-90, 10, -20]  - Mid-transition
0.5s  [45, 0, 0]      - Point down at target
0.8s  [45, 0, 0]      - Hold point
1.2s  [0, 0, 0]       - Return to rest
```

**Forearm Right** (emphasis):
```
0.0s  [0, 0, 0]       - Neutral
0.2s  [-45, 0, 0]     - Bent back
0.5s  [15, 0, 0]      - Extend forward (emphasis)
0.8s  [15, 0, 0]      - Hold
1.2s  [0, 0, 0]       - Return
```

**Hand Right** (index finger point):
```
0.0s  [0, 0, 0]       - Open hand
0.2s  [0, 0, 20]      - Fingers curl (gather energy)
0.5s  [0, 0, -20]     - Index extended (point)
0.8s  [0, 0, -20]     - Hold point
1.2s  [0, 0, 0]       - Return
```

**Arm Left** (secondary support):
```
0.0s  [0, 0, 0]       - Relaxed
0.2s  [-90, 0, 20]    - Raised to side (balance)
0.5s  [-45, 0, 15]    - Lower slightly
0.8s  [-45, 0, 15]    - Hold
1.2s  [0, 0, 0]       - Return
```

**Head** (track target on ground):
```
0.0s  [0, 0, 0]       - Looking forward
0.2s  [-10, 0, 0]     - Look up (gathering)
0.5s  [35, 0, 0]      - Look down at target
0.8s  [35, 0, 0]      - Hold gaze
1.2s  [0, 0, 0]       - Return to neutral
```

**Leg Right** (stabilizing stance):
```
0.0s  [0, 0, 0]       - Neutral
0.2s  [-10, 0, 0]     - Slight bend
0.5s  [-15, 0, 5]     - Brace for power
0.8s  [-15, 0, 5]     - Hold
1.2s  [0, 0, 0]       - Return
```

**Leg Left** (forward step - optional):
```
0.0s  [0, 0, 0]       - Neutral
0.2s  [5, 0, 0]       - Prepare
0.5s  [10, 0, -5]     - Step forward slightly
0.8s  [10, 0, -5]     - Hold
1.2s  [0, 0, 0]       - Return
```

#### Easing
- **0.0s → 0.2s**: Ease-out (build up)
- **0.2s → 0.5s**: Ease-in (dramatic transition)
- **0.5s → 0.8s**: Linear (hold)
- **0.8s → 1.2s**: Ease-in-out (smooth return)

#### Particle Timing
- **0.0s-0.5s**: Energy gathering at hands (growing intensity)
- **0.5s**: Projectile or beam fires from hand to ground target
- **0.5s-0.8s**: Ground effect at target location

---

### Animation 4: Touch Spell

**Animation Name**: `animation.character.spell.touch`
**Duration**: 1.0 seconds
**Loop**: False
**Priority**: 83

#### Purpose
Reach out to touch target for healing or touch-range attack spells

#### Keyframes

**Body** (lean into touch):
```
0.0s  [0, 0, 0]       - Neutral
0.2s  [5, 0, 0]       - Lean forward slightly
0.5s  [8, 0, 0]       - Maximum reach
0.7s  [8, 0, 0]       - Hold contact
1.0s  [0, 0, 0]       - Return
```

**Torso Upper**:
```
0.0s  [0, 0, 0]       - Neutral
0.2s  [5, -10, 0]     - Rotate and lean
0.5s  [10, -15, 0]    - Full extension
0.7s  [10, -15, 0]    - Hold
1.0s  [0, 0, 0]       - Return
```

**Arm Right** (reaching arm):
```
0.0s  [0, 0, 0]       - At side
0.2s  [-45, 0, -15]   - Begin reach
0.5s  [-85, 0, -5]    - Full reach forward
0.7s  [-85, 0, -5]    - Hold contact
1.0s  [0, 0, 0]       - Return
```

**Forearm Right**:
```
0.0s  [0, 0, 0]       - Neutral
0.2s  [-15, 0, 0]     - Slight bend
0.5s  [5, 0, 0]       - Extend fully
0.7s  [5, 0, 0]       - Hold
1.0s  [0, 0, 0]       - Return
```

**Hand Right** (open palm):
```
0.0s  [0, 0, 0]       - Relaxed
0.2s  [0, 0, 10]      - Fingers spread slightly
0.5s  [0, 0, 15]      - Palm open fully (contact)
0.7s  [0, 0, 15]      - Hold contact
1.0s  [0, 0, 0]       - Return
```

**Arm Left** (balance):
```
0.0s  [0, 0, 0]       - At side
0.2s  [-20, 0, 10]    - Pull back for balance
0.5s  [-30, 0, 15]    - Extended for balance
0.7s  [-30, 0, 15]    - Hold
1.0s  [0, 0, 0]       - Return
```

**Head** (focus on touch point):
```
0.0s  [0, 0, 0]       - Forward
0.2s  [0, -10, 0]     - Turn toward target
0.5s  [5, -15, 0]     - Focus on hand/target
0.7s  [5, -15, 0]     - Hold focus
1.0s  [0, 0, 0]       - Return
```

**Leg Right** (step forward):
```
0.0s  [0, 0, 0]       - Neutral
0.2s  [10, 0, 0]      - Begin step
0.5s  [15, 0, 0]      - Step complete
0.7s  [15, 0, 0]      - Hold
1.0s  [0, 0, 0]       - Return
```

#### Easing
- **0.0s → 0.5s**: Ease-out (smooth reach)
- **0.5s → 0.7s**: Linear (hold)
- **0.7s → 1.0s**: Ease-in (return)

#### Particle Timing
- **0.3s-0.5s**: Energy gathering at hand
- **0.5s**: Touch effect (burst or gentle glow)
- **0.5s-0.7s**: Transfer effect

---

### Animation 5: Concentration Prep

**Animation Name**: `animation.character.spell.concentration_prep`
**Duration**: 1.2 seconds
**Loop**: False
**Priority**: 85
**Transition**: Blends into concentration_hold at 1.0s

#### Purpose
Initial casting of concentration spells (Bless, Haste, Spirit Guardians)

#### Keyframes

**Body** (gather focus):
```
0.0s  [0, 0, 0]       - Neutral
0.3s  [-5, 0, 0]      - Slight crouch (gather)
0.7s  [5, 0, 0]       - Rise up (release)
1.2s  [2, 0, 0]       - Settle into hold posture
```

**Torso Upper** (deep breath):
```
0.0s  [0, 0, 0]       - Neutral
0.3s  [-3, 0, 0]      - Crouch with body
0.7s  [8, 0, 0]       - Chest out (release)
1.2s  [3, 0, 0]       - Relaxed hold
```

**Arm Right** (mystic gesture):
```
0.0s  [0, 0, 0]       - At side
0.3s  [-60, 20, -30]  - Raise to side and back
0.7s  [-90, 0, -15]   - Extended to side
1.2s  [-85, 0, -10]   - Hold position (ready for hold animation)
```

**Arm Left** (mystic gesture):
```
0.0s  [0, 0, 0]       - At side
0.3s  [-60, -20, 30]  - Raise to side and back (mirror)
0.7s  [-90, 0, 15]    - Extended to side (mirror)
1.2s  [-85, 0, 10]    - Hold position
```

**Forearm Right**:
```
0.0s  [0, 0, 0]       - Neutral
0.3s  [-30, 0, 0]     - Bend inward
0.7s  [-10, 0, 0]     - Slightly bent
1.2s  [-5, 0, 0]      - Minimal bend (hold ready)
```

**Forearm Left**:
```
0.0s  [0, 0, 0]       - Neutral
0.3s  [-30, 0, 0]     - Bend inward
0.7s  [-10, 0, 0]     - Slightly bent
1.2s  [-5, 0, 0]      - Minimal bend
```

**Hand Right** (fingers position):
```
0.0s  [0, 0, 0]       - Relaxed
0.3s  [0, 0, 15]      - Fingers curl
0.7s  [0, 0, -10]     - Fingers spread (release energy)
1.2s  [0, 0, 5]       - Partial spread (hold ready)
```

**Hand Left** (fingers position):
```
0.0s  [0, 0, 0]       - Relaxed
0.3s  [0, 0, -15]     - Fingers curl (mirror)
0.7s  [0, 0, 10]      - Fingers spread
1.2s  [0, 0, -5]      - Partial spread
```

**Head** (concentration):
```
0.0s  [0, 0, 0]       - Neutral
0.3s  [-10, 0, 0]     - Look down (internal focus)
0.7s  [5, 0, 0]       - Look up (external release)
1.2s  [0, 0, 0]       - Forward (maintain concentration)
```

**Legs**:
```
0.0s  [0, 0, 0]       - Neutral
0.3s  [-10, 0, 0]     - Knees bend (gather)
0.7s  [0, 0, 0]       - Straighten
1.2s  [0, 0, 0]       - Stable
```

#### Easing
- **0.0s → 0.3s**: Ease-in (gradual gather)
- **0.3s → 0.7s**: Ease-out (dramatic release)
- **0.7s → 1.2s**: Ease-in-out (settle into hold)

#### Particle Timing
- **0.0s-0.3s**: Energy gathering around caster (growing sphere)
- **0.3s-0.7s**: Energy intensifies
- **0.7s**: Spell activates (burst effect)
- **0.7s-1.2s**: Lingering aura establishes

---

### Animation 6: Concentration Hold

**Animation Name**: `animation.character.spell.concentration_hold`
**Duration**: 3.0 seconds
**Loop**: True
**Priority**: 70 (allows walking, but lower priority than new actions)

#### Purpose
Maintain concentration on active spells - allows movement

#### Keyframes

**Body** (subtle breathing):
```
0.0s  [2, 0, 0]       - Base position
0.75s [3, 0, 1]       - Slight shift
1.5s  [2, 0, 0]       - Return to base
2.25s [3, 0, -1]      - Slight shift opposite
3.0s  [2, 0, 0]       - Return to start (loop)
```

**Torso Upper** (breathing):
```
0.0s  [3, 0, 0]       - Slightly elevated
0.75s [4, 0, 0]       - Inhale
1.5s  [3, 0, 0]       - Exhale
2.25s [4, 0, 0]       - Inhale
3.0s  [3, 0, 0]       - Return to start
```

**Arm Right** (gentle float):
```
0.0s  [-85, 0, -10]   - Extended to side
0.75s [-83, 2, -12]   - Drift slightly
1.5s  [-85, 0, -10]   - Return
2.25s [-87, -2, -8]   - Drift opposite
3.0s  [-85, 0, -10]   - Return to start
```

**Arm Left** (gentle float):
```
0.0s  [-85, 0, 10]    - Extended to side (mirror)
0.75s [-87, -2, 8]    - Drift
1.5s  [-85, 0, 10]    - Return
2.25s [-83, 2, 12]    - Drift opposite
3.0s  [-85, 0, 10]    - Return to start
```

**Forearm Right** (minimal movement):
```
0.0s  [-5, 0, 0]      - Slightly bent
0.75s [-3, 0, 0]      - Micro-adjustment
1.5s  [-5, 0, 0]      - Return
2.25s [-7, 0, 0]      - Micro-adjustment
3.0s  [-5, 0, 0]      - Return to start
```

**Forearm Left**:
```
0.0s  [-5, 0, 0]      - Slightly bent
0.75s [-7, 0, 0]      - Micro-adjustment
1.5s  [-5, 0, 0]      - Return
2.25s [-3, 0, 0]      - Micro-adjustment
3.0s  [-5, 0, 0]      - Return to start
```

**Hand Right** (energy flow):
```
0.0s  [0, 0, 5]       - Fingers partially spread
0.75s [0, 0, 8]       - Spread more
1.5s  [0, 0, 5]       - Return
2.25s [0, 0, 3]       - Curl slightly
3.0s  [0, 0, 5]       - Return to start
```

**Hand Left**:
```
0.0s  [0, 0, -5]      - Fingers partially spread
0.75s [0, 0, -3]      - Curl slightly
1.5s  [0, 0, -5]      - Return
2.25s [0, 0, -8]      - Spread more
3.0s  [0, 0, -5]      - Return to start
```

**Head** (alert concentration):
```
0.0s  [0, 0, 0]       - Forward
0.75s [1, 2, 0]       - Micro-adjustment
1.5s  [0, 0, 0]       - Center
2.25s [1, -2, 0]      - Micro-adjustment
3.0s  [0, 0, 0]       - Return to start
```

**Legs** (stable for movement):
```
All keyframes: [0, 0, 0] - No modification, allows locomotion to blend
```

#### Easing
- All transitions: Ease-in-out (very smooth, meditative)

#### Particle Timing
- **Continuous**: Soft aura around hands
- **Every 1.5s**: Gentle pulse synchronized with breathing

#### Special Note
This animation is designed to blend with walk/run animations. The leg and lower body remain neutral, allowing locomotion animations to control movement while the upper body maintains the concentration pose.

---

### Animation 7: Ritual Casting

**Animation Name**: `animation.character.spell.ritual_cast`
**Duration**: 4.0 seconds
**Loop**: True
**Priority**: 90 (cannot be interrupted)

#### Purpose
Long casting for ritual spells (10+ minutes in-game, but animated for visual feedback)

#### Keyframes

**Body** (swaying motion):
```
0.0s  [0, 0, 0]       - Center
1.0s  [3, 0, 5]       - Lean right and rotate
2.0s  [0, 0, 0]       - Center
3.0s  [3, 0, -5]      - Lean left and rotate
4.0s  [0, 0, 0]       - Return to start
```

**Torso Upper**:
```
0.0s  [0, 0, 0]       - Neutral
1.0s  [5, 0, 0]       - Lean forward
2.0s  [0, 0, 0]       - Center
3.0s  [5, 0, 0]       - Lean forward
4.0s  [0, 0, 0]       - Return
```

**Arm Right** (tracing symbols):
```
0.0s  [-90, 30, -20]  - Extended up and out
1.0s  [-90, -10, -15] - Sweep down
2.0s  [-90, 30, -20]  - Sweep up
3.0s  [-90, -10, -15] - Sweep down
4.0s  [-90, 30, -20]  - Return to start
```

**Arm Left** (holding component/focus):
```
0.0s  [-90, 0, 15]    - Extended forward
1.0s  [-85, 0, 18]    - Slight rise
2.0s  [-90, 0, 15]    - Lower
3.0s  [-85, 0, 18]    - Slight rise
4.0s  [-90, 0, 15]    - Return to start
```

**Forearm Right** (complex gesture):
```
0.0s  [-20, 0, 0]     - Bent
1.0s  [10, 0, 0]      - Extended
2.0s  [-20, 0, 0]     - Bent
3.0s  [10, 0, 0]      - Extended
4.0s  [-20, 0, 0]     - Return to start
```

**Forearm Left** (stable hold):
```
0.0s  [0, 0, 0]       - Straight
1.0s  [-5, 0, 0]      - Micro-bend
2.0s  [0, 0, 0]       - Straight
3.0s  [-5, 0, 0]      - Micro-bend
4.0s  [0, 0, 0]       - Return to start
```

**Hand Right** (tracing motions):
```
0.0s  [0, 0, 10]      - Fingers extended
1.0s  [0, 0, -5]      - Fingers curl
2.0s  [0, 0, 10]      - Fingers extended
3.0s  [0, 0, -5]      - Fingers curl
4.0s  [0, 0, 10]      - Return to start
```

**Hand Left** (holding focus):
```
0.0s  [0, 0, -15]     - Gripping
1.0s  [0, 0, -15]     - Hold
2.0s  [0, 0, -15]     - Hold
3.0s  [0, 0, -15]     - Hold
4.0s  [0, 0, -15]     - Return to start
```

**Head** (reading/reciting):
```
0.0s  [15, 0, 0]      - Looking down (at book/components)
1.0s  [10, 10, 0]     - Look to right
2.0s  [15, 0, 0]      - Look down
3.0s  [10, -10, 0]    - Look to left
4.0s  [15, 0, 0]      - Return to start
```

**Legs** (standing position):
```
All keyframes: [0, 0, 0] - Stable standing, no movement allowed
```

#### Easing
- All transitions: Ease-in-out (slow, deliberate)

#### Particle Timing
- **Continuous**: Complex runic symbols appear and fade
- **Every 1.0s**: Pulse of energy
- **Variable**: Symbols trace the path of the right hand

---

### Animation 8: Somatic Component (Quick Gesture)

**Animation Name**: `animation.character.spell.somatic_gesture`
**Duration**: 0.3 seconds
**Loop**: False
**Priority**: 75

#### Purpose
Very quick hand gesture for spells with somatic components but no flashy effects

#### Keyframes

**Body**:
```
All keyframes: [0, 0, 0] - Stable, no body movement
```

**Torso Upper**:
```
0.0s  [0, 0, 0]       - Neutral
0.15s [0, -5, 0]      - Slight twist
0.3s  [0, 0, 0]       - Return
```

**Arm Right** (quick flick):
```
0.0s  [0, 0, 0]       - At rest
0.1s  [-45, 10, -10]  - Quick raise
0.2s  [-60, 5, -5]    - Extension
0.3s  [0, 0, 0]       - Snap back to rest
```

**Forearm Right**:
```
0.0s  [0, 0, 0]       - Neutral
0.1s  [-20, 0, 0]     - Bend
0.2s  [5, 0, 0]       - Flick out
0.3s  [0, 0, 0]       - Return
```

**Hand Right** (finger snap or wave):
```
0.0s  [0, 0, 0]       - Relaxed
0.1s  [0, 0, 15]      - Curl
0.2s  [0, 0, -10]     - Snap/spread
0.3s  [0, 0, 0]       - Return
```

**All other bones**: [0, 0, 0] - Minimal movement

#### Easing
- **0.0s → 0.2s**: Linear (quick snap)
- **0.2s → 0.3s**: Ease-in (quick return)

#### Particle Timing
- **0.2s**: Small sparkle or flash at hand

---

## Animation Priority System

Magic animations integrate with the existing priority system:

### Priority Levels (0-100)

```
100 - Critical Actions (death, stunned)
 95 - Special Events
 90 - Ritual Casting (cannot interrupt)
 85 - Concentration Prep, Ground Target, Channeling
 83 - Touch Spell
 80 - Instant Cast
 75 - Somatic Gesture
 70 - Concentration Hold (allows blending with movement)
 60 - Combat Animations
 50 - Locomotion (walk, run)
 40 - Idle
```

### Priority Rules

1. **Higher priority animations always override lower priority**
2. **Equal priority animations**: Last triggered wins
3. **Concentration Hold (70)** is special:
   - Allows blending with walk (50) and idle (40)
   - Can be interrupted by instant cast (80+)
   - Interrupting concentration triggers "concentration broken" effect

4. **Movement Restrictions by Animation**:
   - **Full Movement**: Concentration Hold (can walk/run normally)
   - **Slow Movement**: Channeling (50% speed walk only)
   - **No Movement**: All other casting animations

---

## Integration with Existing Animations

### Blending Rules

#### From Idle → Magic Cast
```java
TransitionTime: 0.1 seconds
BlendType: Linear
Notes: Quick transition, idle is low priority
```

#### From Walk → Magic Cast
```java
TransitionTime: 0.15 seconds
BlendType: Ease-out
Notes: Smooth deceleration into cast
Special: Instant cast (80) allows walk to continue at 100% speed
        Channeling (85) reduces walk to 50% speed
        Other casts stop movement
```

#### From Combat → Magic Cast
```java
TransitionTime: 0.2 seconds
BlendType: Ease-in-out
Notes: Weapon-to-magic transition needs smoothness
Special: Combat (60) < Magic (80+), magic interrupts combat
```

#### From Magic Cast → Walk
```java
TransitionTime: 0.2 seconds
BlendType: Ease-in
Notes: Return to locomotion after cast completes
Special: Concentration Hold → Walk has 0.0s transition (seamless blend)
```

#### Between Magic Animations
```java
Instant → Instant: 0.1s (can chain quickly)
Instant → Channel: 0.15s (switch modes)
Channel → Instant: Interrupt (0.05s snap)
Concentration Prep → Concentration Hold: 0.0s (designed to flow)
```

### Locomotion During Casting

#### Concentration Hold Special Blending

The `concentration_hold` animation is designed to layer over locomotion:

```java
// Upper body: Uses concentration_hold keyframes
// - Torso Upper
// - Both Arms
// - Both Forearms
// - Both Hands
// - Head (partial, allows some looking)

// Lower body: Uses walk/run keyframes
// - Body (vertical bob only)
// - Both Legs
// - Both Shins
// - Both Feet

// Blend Method: Additive
concentration_final_pose = walk_animation + concentration_hold_animation
```

**Visual Result**: Character walks normally while maintaining concentration hand positions

#### Channeling Slow Walk

```java
// Walk animation plays at 50% speed
// Channeling animation takes priority on upper body
// Lower body uses slowed walk cycle
// Blend weight: 70% channeling, 30% walk
```

#### Instant Cast While Walking

```java
// Walk continues at 100% speed
// Upper body briefly performs cast gesture
// Lower body unaffected
// Animation plays on upper body only
// Duration: Short enough not to disrupt walk flow
```

---

## Keyframe Reference

### Rotation Conventions

**Format**: `[X, Y, Z]` in degrees

- **X-axis (Pitch)**:
  - Positive = Rotate forward/down
  - Negative = Rotate backward/up
  - Range: -90° to +90° (recommended)

- **Y-axis (Yaw)**:
  - Positive = Rotate right
  - Negative = Rotate left
  - Range: -180° to +180°

- **Z-axis (Roll)**:
  - Positive = Tilt clockwise (from viewer perspective)
  - Negative = Tilt counter-clockwise
  - Range: -45° to +45° (recommended)

### Safe Rotation Limits

To ensure natural-looking animations across all races:

```
Bone Name       | X (Pitch)    | Y (Yaw)      | Z (Roll)
----------------|--------------|--------------|-------------
body            | -10 to +20   | -15 to +15   | -10 to +10
torso_upper     | -20 to +30   | -25 to +25   | -15 to +15
head            | -45 to +45   | -80 to +80   | -30 to +30
arm_*           | -120 to +60  | -45 to +45   | -45 to +45
forearm_*       | -45 to +10   | -15 to +15   | -5 to +5
hand_*          | -30 to +30   | -20 to +20   | -30 to +30
leg_*           | -50 to +50   | -20 to +20   | -10 to +10
shin_*          | -25 to +15   | -10 to +10   | -5 to +5
foot_*          | -45 to +20   | -15 to +15   | -10 to +10
```

**Note**: These are safe ranges. Exceeding them may cause unnatural poses or bone intersection issues, especially on different race proportions.

---

## BlockBench Implementation Guide

### Creating Magic Animations in BlockBench

#### Step 1: Open Your Character Model
```
1. File → Open → Select character .bbmodel file
2. Ensure bone hierarchy matches standard skeleton
3. Verify all bones are present and properly named
```

#### Step 2: Create New Animation
```
1. Animation tab → Add Animation
2. Name: animation.character.spell.cast_instant (example)
3. Set animation length: 0.5 seconds (for instant cast)
4. Set loop: False (for one-shot animations)
5. Frame rate: 30 FPS (standard)
```

#### Step 3: Add Keyframes

**For each bone with movement:**

1. Select bone in outliner (e.g., "arm_right")
2. Move timeline cursor to keyframe time (e.g., 0.0s)
3. Click "Add Keyframe" button (or press K)
4. Select "Rotation" channel
5. In the rotation inputs, enter values from specifications:
   - Example: `[-45, 15, -20]` at 0.1s for instant cast arm_right
6. Repeat for each keyframe time

**Pro Tip**: Use the "Copy Keyframe" feature to duplicate and modify similar poses

#### Step 4: Set Easing/Interpolation

1. Click on keyframe diamond in timeline
2. Right-click → Interpolation
3. Select easing type:
   - **Linear**: Constant speed
   - **Ease-in**: Slow start, fast end
   - **Ease-out**: Fast start, slow end
   - **Ease-in-out**: Slow start, slow end (smooth)

#### Step 5: Preview Animation

```
1. Click "Play" button in animation panel
2. Watch animation loop
3. Verify smooth motion
4. Check for:
   - Bone disconnections
   - Unnatural rotations
   - Jerky movements
   - Proper loop points (for looping animations)
```

#### Step 6: Refine Timing

- Adjust keyframe positions by dragging in timeline
- Add additional keyframes for smoother motion
- Remove unnecessary keyframes (simplify)
- Ensure first and last keyframes match for loops

#### Step 7: Export

```
1. File → Export → Export GeckoLib Animation
2. Save to: src/main/resources/assets/fiveesrd/animations/entity/character/magic.animation.json
3. Ensure format_version: 1.8.0
4. Verify JSON is valid
```

### Multi-Animation File Structure

When exporting, all magic animations should be in one file: `magic.animation.json`

```json
{
  "format_version": "1.8.0",
  "animations": {
    "animation.character.spell.cast_instant": { ... },
    "animation.character.spell.channel_concentrated": { ... },
    "animation.character.spell.ground_target": { ... },
    "animation.character.spell.touch": { ... },
    "animation.character.spell.concentration_prep": { ... },
    "animation.character.spell.concentration_hold": { ... },
    "animation.character.spell.ritual_cast": { ... },
    "animation.character.spell.somatic_gesture": { ... }
  }
}
```

### Testing in BlockBench

1. **Smooth Transitions**:
   - Play animation at different speeds
   - Check for sudden pops or jerks
   - Verify easing feels natural

2. **Race Compatibility**:
   - Test animation on dwarf skeleton (shorter bones)
   - Test on elf skeleton (longer bones)
   - Verify no bone disconnections
   - Check that gestures look proportional

3. **Loop Points**:
   - For looping animations, first and last keyframes must match exactly
   - Play animation for multiple loops
   - Verify seamless transition at loop point

4. **Bone Hierarchy**:
   - Move parent bones to test child bone following
   - Verify rotation inheritance
   - Check for unexpected bone movements

---

## Technical Integration

### Java Animation Controller

#### Spell Animation Trigger Method

```java
public class CharacterEntity extends LivingEntity implements GeoEntity {

    private AnimationState<CharacterEntity> currentAnimationState;
    private int currentAnimationPriority = 0;

    /**
     * Trigger a spell animation
     * @param spellType The type of spell being cast
     * @param allowMovement Whether movement is allowed during cast
     */
    public void playSpellAnimation(SpellAnimationType spellType, boolean allowMovement) {
        String animationName = spellType.getAnimationName();
        int priority = spellType.getPriority();

        // Check if current animation can be interrupted
        if (priority >= currentAnimationPriority) {
            // Interrupt current animation if spell has higher priority
            triggerAnimation(animationName, priority);

            // Set movement restriction
            if (!allowMovement && spellType != SpellAnimationType.CONCENTRATION_HOLD) {
                this.setMovementSpeed(0.0f);
            } else if (spellType == SpellAnimationType.CHANNELED) {
                this.setMovementSpeed(this.getBaseMovementSpeed() * 0.5f);
            }

            // Schedule animation end callback
            scheduleAnimationEnd(spellType.getDuration(), () -> {
                this.setMovementSpeed(this.getBaseMovementSpeed());

                // Auto-transition for concentration spells
                if (spellType == SpellAnimationType.CONCENTRATION_PREP) {
                    playSpellAnimation(SpellAnimationType.CONCENTRATION_HOLD, true);
                }
            });
        }
    }

    /**
     * Stop concentration (if active)
     */
    public void breakConcentration() {
        if (currentAnimationState == SpellAnimationType.CONCENTRATION_HOLD) {
            // Trigger concentration break effect
            spawnConcentrationBreakParticles();

            // Return to idle
            triggerAnimation("animation.character.idle", 40);
        }
    }
}
```

#### Spell Animation Type Enum

```java
public enum SpellAnimationType {
    INSTANT_CAST("animation.character.spell.cast_instant", 80, 0.5f, true),
    CHANNELED("animation.character.spell.channel_concentrated", 85, 2.0f, true), // loops
    GROUND_TARGET("animation.character.spell.ground_target", 85, 1.2f, false),
    TOUCH("animation.character.spell.touch", 83, 1.0f, false),
    CONCENTRATION_PREP("animation.character.spell.concentration_prep", 85, 1.2f, false),
    CONCENTRATION_HOLD("animation.character.spell.concentration_hold", 70, -1f, true), // infinite loop
    RITUAL_CAST("animation.character.spell.ritual_cast", 90, -1f, false), // loops until complete
    SOMATIC_GESTURE("animation.character.spell.somatic_gesture", 75, 0.3f, true);

    private final String animationName;
    private final int priority;
    private final float duration; // -1 for looping
    private final boolean allowMovement;

    SpellAnimationType(String animationName, int priority, float duration, boolean allowMovement) {
        this.animationName = animationName;
        this.priority = priority;
        this.duration = duration;
        this.allowMovement = allowMovement;
    }

    public String getAnimationName() { return animationName; }
    public int getPriority() { return priority; }
    public float getDuration() { return duration; }
    public boolean allowsMovement() { return allowMovement; }
    public boolean isLooping() { return duration < 0; }
}
```

#### GeckoLib Animation Controller Integration

```java
@Override
public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
    controllers.add(new AnimationController<>(this, "main_controller", 0, this::animationPredicate));
}

private PlayState animationPredicate(AnimationState<CharacterEntity> state) {
    // Priority-based animation selection

    // Check for active spell animation
    if (isPerformingSpell()) {
        SpellAnimationType spellType = getCurrentSpellType();
        state.getController().setAnimation(RawAnimation.begin().then(
            spellType.getAnimationName(),
            spellType.isLooping() ? Animation.LoopType.LOOP : Animation.LoopType.PLAY_ONCE
        ));

        // Blend with locomotion for concentration hold
        if (spellType == SpellAnimationType.CONCENTRATION_HOLD && isMoving()) {
            // Apply upper body concentration, lower body locomotion
            return PlayState.CONTINUE; // Special blending handled in setCustomAnimations
        }

        return PlayState.CONTINUE;
    }

    // Fall back to locomotion/combat animations
    if (this.isAttacking()) {
        state.getController().setAnimation(RawAnimation.begin().then("animation.character.attack", Animation.LoopType.PLAY_ONCE));
    } else if (state.isMoving()) {
        if (this.isSprinting()) {
            state.getController().setAnimation(RawAnimation.begin().then("animation.character.run", Animation.LoopType.LOOP));
        } else {
            state.getController().setAnimation(RawAnimation.begin().then("animation.character.walk", Animation.LoopType.LOOP));
        }
    } else {
        state.getController().setAnimation(RawAnimation.begin().then("animation.character.idle", Animation.LoopType.LOOP));
    }

    return PlayState.CONTINUE;
}
```

#### Custom Bone Control for Concentration Hold + Walk Blend

```java
@Override
public void setCustomAnimations(CharacterEntity entity, long instanceId, AnimationState<CharacterEntity> state) {
    super.setCustomAnimations(entity, instanceId, state);

    if (entity.getCurrentSpellType() == SpellAnimationType.CONCENTRATION_HOLD && entity.isMoving()) {
        // Get bones
        IBone torsoUpper = getAnimationProcessor().getBone("torso_upper");
        IBone armRight = getAnimationProcessor().getBone("arm_right");
        IBone armLeft = getAnimationProcessor().getBone("arm_left");
        IBone forearmRight = getAnimationProcessor().getBone("forearm_right");
        IBone forearmLeft = getAnimationProcessor().getBone("forearm_left");
        IBone handRight = getAnimationProcessor().getBone("hand_right");
        IBone handLeft = getAnimationProcessor().getBone("hand_left");
        IBone head = getAnimationProcessor().getBone("head");

        // These bones use concentration_hold animation exclusively
        // Lower body bones (body, legs, shins, feet) use walk animation
        // GeckoLib handles this via animation layering

        // Optional: Add head tracking on top of concentration
        if (entity.getTarget() != null) {
            Entity target = entity.getTarget();
            float headYaw = calculateLookYaw(entity, target);
            float headPitch = calculateLookPitch(entity, target);

            // Blend with animation values
            head.setRotationY(head.getRotationY() + headYaw * 0.5f); // 50% blend
            head.setRotationX(head.getRotationX() + headPitch * 0.5f);
        }
    }
}
```

### Particle Effect Synchronization

```java
/**
 * Trigger particles at specific animation times
 */
public void spawnSpellParticles(SpellAnimationType spellType, float animationTime) {
    switch (spellType) {
        case INSTANT_CAST:
            if (animationTime >= 0.15f && animationTime < 0.17f) {
                // Pre-cast glow
                spawnParticle(ParticleTypes.ENCHANT, handPosition, 5);
            }
            if (animationTime >= 0.2f && animationTime < 0.22f) {
                // Main burst
                spawnParticle(ParticleTypes.SOUL_FIRE_FLAME, handPosition, 15);
            }
            break;

        case CHANNELED:
            // Continuous stream
            if (animationTime % 0.1f < 0.05f) { // Every 0.1s
                spawnParticle(ParticleTypes.SOUL, handPosition, 2);
            }
            // Pulse on hand movement
            if (Math.abs(animationTime - 0.5f) < 0.05f || Math.abs(animationTime - 1.5f) < 0.05f) {
                spawnParticle(ParticleTypes.ENCHANT, handPosition, 8);
            }
            break;

        case GROUND_TARGET:
            if (animationTime >= 0.0f && animationTime < 0.5f) {
                // Energy gathering
                float intensity = animationTime / 0.5f;
                spawnParticle(ParticleTypes.WITCH, handPosition, (int)(intensity * 10));
            }
            if (animationTime >= 0.5f && animationTime < 0.52f) {
                // Projectile launch
                spawnProjectileParticle(handPosition, targetPosition);
            }
            if (animationTime >= 0.5f && animationTime < 0.8f) {
                // Ground effect
                spawnParticle(ParticleTypes.FLAME, targetPosition, 20);
            }
            break;

        case CONCENTRATION_PREP:
            if (animationTime >= 0.7f && animationTime < 0.72f) {
                // Spell activate burst
                spawnSphereParticles(ParticleTypes.END_ROD, entityPosition, 1.5f, 30);
            }
            break;

        case CONCENTRATION_HOLD:
            // Subtle continuous aura
            if (animationTime % 1.5f < 0.05f) { // Every 1.5s pulse
                spawnParticle(ParticleTypes.ENCHANT, entityPosition, 5);
            }
            break;
    }
}
```

---

## Testing & Validation

### Visual Testing Checklist

#### Per-Animation Tests

For each magic animation, verify:

- [ ] Animation plays from start to finish without errors
- [ ] Keyframes transition smoothly (no pops or jerks)
- [ ] Bone rotations stay within safe limits
- [ ] No bone disconnections or stretching
- [ ] Easing curves feel natural
- [ ] Loop animations loop seamlessly (if applicable)
- [ ] Animation duration matches specification
- [ ] Particles sync with animation timing

#### Cross-Race Testing

Test each animation on all 4 races:

- [ ] **Human**: Animation looks natural at 1.8m height
- [ ] **Dwarf**: Shorter limbs don't cause issues, gestures proportional
- [ ] **Elf**: Longer limbs don't overextend, movements graceful
- [ ] **Halfling**: Smallest race maintains visual clarity of gestures

#### Integration Testing

- [ ] Idle → Instant Cast → Idle transition is smooth
- [ ] Walk → Instant Cast continues walking at full speed
- [ ] Walk → Channeling slows to 50% speed
- [ ] Walk → Ground Target stops movement
- [ ] Concentration Prep → Concentration Hold auto-transitions
- [ ] Concentration Hold + Walk blends properly (upper/lower body)
- [ ] Combat animation interrupted by magic cast (priority 60 < 80)
- [ ] Magic cast completed before starting locomotion
- [ ] Breaking concentration returns to appropriate idle/walk

#### Priority System Testing

Trigger animations in this order, verify correct behavior:

1. Start with Idle (priority 40)
2. Trigger Walk (priority 50) → Should override idle
3. Trigger Instant Cast (priority 80) → Should override walk
4. Instant Cast completes → Should return to walk
5. Trigger Concentration Prep (priority 85) → Should stop walk
6. Auto-transition to Concentration Hold (priority 70)
7. Start Walk → Should blend with concentration hold
8. Trigger Combat Attack (priority 60) → Should NOT interrupt concentration
9. Trigger Ground Target (priority 85) → Should interrupt concentration
10. Ground Target completes → Should return to idle

#### Performance Testing

- [ ] No FPS drops when playing magic animations
- [ ] Particle effects don't cause lag
- [ ] Multiple entities casting simultaneously (stress test)
- [ ] Animation transitions don't cause stuttering
- [ ] Bone retargeting calculations are performant

### Automated Testing (Optional)

```java
@Test
public void testMagicAnimationPriorities() {
    CharacterEntity entity = createTestEntity(Race.HUMAN);

    // Start idle
    entity.tick();
    assertEquals("animation.character.idle", entity.getCurrentAnimation());

    // Trigger instant cast
    entity.playSpellAnimation(SpellAnimationType.INSTANT_CAST, true);
    assertEquals("animation.character.spell.cast_instant", entity.getCurrentAnimation());
    assertEquals(80, entity.getCurrentAnimationPriority());

    // Try to interrupt with lower priority combat
    entity.attack(); // priority 60
    assertEquals("animation.character.spell.cast_instant", entity.getCurrentAnimation()); // Should not change

    // Wait for cast to complete
    waitForAnimationComplete(entity, 0.5f);

    // Should return to idle
    assertEquals("animation.character.idle", entity.getCurrentAnimation());
}

@Test
public void testConcentrationHoldMovementBlend() {
    CharacterEntity entity = createTestEntity(Race.HUMAN);

    // Start concentration hold
    entity.playSpellAnimation(SpellAnimationType.CONCENTRATION_HOLD, true);

    // Start walking
    entity.setMoving(true);
    entity.tick();

    // Verify upper body uses concentration animation
    IBone armRight = entity.getBone("arm_right");
    assertTrue(armRight.getRotationX() < -80); // Extended position from concentration

    // Verify lower body uses walk animation
    IBone legRight = entity.getBone("leg_right");
    assertTrue(Math.abs(legRight.getRotationX()) > 5); // Leg swinging from walk
}

@Test
public void testAnimationRetargetingAcrossRaces() {
    for (Race race : Race.values()) {
        CharacterEntity entity = createTestEntity(race);

        entity.playSpellAnimation(SpellAnimationType.INSTANT_CAST, true);
        entity.tick();

        // Verify no bone disconnections
        assertAllBonesConnected(entity);

        // Verify gestures are visible (hand reaches forward)
        IBone handRight = entity.getBone("hand_right");
        Vec3 handPos = handRight.getWorldPosition();
        Vec3 bodyPos = entity.getPosition();

        float reachDistance = handPos.subtract(bodyPos).length();
        assertTrue(reachDistance > entity.getHeight() * 0.3f); // Hand extends at least 30% of height
    }
}
```

### Debug Visualization

Add debug rendering to visualize animation states:

```java
// In development builds only
if (FiveESrdMod.DEBUG_MODE) {
    renderAnimationDebug(entity);
}

private void renderAnimationDebug(CharacterEntity entity) {
    // Render bone positions as colored spheres
    for (String boneName : HumanoidBones.ALL_BONES) {
        IBone bone = entity.getBone(boneName);
        Vec3 pos = bone.getWorldPosition();

        // Color by bone type
        int color = getBoneDebugColor(boneName);
        renderSphere(pos, 0.05f, color);

        // Render bone rotation axes
        renderRotationAxes(bone);
    }

    // Display current animation name and priority
    String debugText = String.format("%s (Priority: %d)",
        entity.getCurrentAnimation(),
        entity.getCurrentAnimationPriority());
    renderText(debugText, entity.getPosition().add(0, entity.getHeight() + 0.5, 0));

    // Display movement speed
    String speedText = String.format("Speed: %.2f", entity.getMovementSpeed());
    renderText(speedText, entity.getPosition().add(0, entity.getHeight() + 0.3, 0));
}
```

---

## Summary

This specification provides complete details for implementing 8 magic/spellcasting animations:

1. **Instant Cast** - Quick single-hand gesture (0.5s)
2. **Channeled Spell** - Sustained two-hand channeling (2.0s loop)
3. **Ground Target** - Dramatic pointing at ground (1.2s)
4. **Touch Spell** - Reaching touch gesture (1.0s)
5. **Concentration Prep** - Initial concentration cast (1.2s)
6. **Concentration Hold** - Maintaining concentration while moving (3.0s loop)
7. **Ritual Cast** - Long ritual casting (4.0s loop)
8. **Somatic Gesture** - Quick hand flick (0.3s)

### Key Features

- ✅ Fully compatible with bone retargeting system
- ✅ Works across all 4 races (Human, Dwarf, Elf, Halfling)
- ✅ Integrates with priority system (70-90 priority range)
- ✅ Supports movement during casting (concentration hold)
- ✅ Includes particle timing synchronization
- ✅ Smooth transitions with existing animations
- ✅ Detailed keyframe specifications for BlockBench

### Next Steps

1. Create animations in BlockBench following specifications
2. Export to `magic.animation.json`
3. Implement Java integration code
4. Add particle effects synchronized with animation timing
5. Test across all races
6. Validate priority system
7. Profile performance
8. Iterate and polish

---

**Ready for Phase 8.2 Implementation** ✨

