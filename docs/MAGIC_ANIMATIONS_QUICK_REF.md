# Magic Animations Quick Reference

**Document Version**: 1.0
**Last Updated**: 2025-11-13
**Companion to**: MAGIC_ANIMATIONS_SPEC.md

---

## Animation Summary Table

| Animation Name | Duration | Loop | Priority | Movement | Use Case |
|----------------|----------|------|----------|----------|----------|
| `cast_instant` | 0.5s | No | 80 | Yes (100%) | Quick spells (Magic Missile, Shield) |
| `channel_concentrated` | 2.0s | Yes | 85 | Yes (50%) | Sustained spells (Eldritch Blast, Healing) |
| `ground_target` | 1.2s | No | 85 | No | Area spells (Fireball, Ice Storm) |
| `touch` | 1.0s | No | 83 | No | Touch range (Cure Wounds, Shocking Grasp) |
| `concentration_prep` | 1.2s | No | 85 | No | Start concentration (Bless, Haste) |
| `concentration_hold` | 3.0s | Yes | 70 | Yes (100%) | Maintain concentration |
| `ritual_cast` | 4.0s | Yes | 90 | No | Long rituals (10+ minutes) |
| `somatic_gesture` | 0.3s | No | 75 | Yes (100%) | Quick hand gesture |

---

## Animation Transitions Flow

```
Idle (40)
  ↓
Walk (50) ←→ Instant Cast (80) [can walk while casting]
  ↓
Run (50) ←→ Instant Cast (80) [can run while casting]
  ↓
Stop Movement → Ground Target (85) [must stop to cast]
  ↓
Return to Idle/Walk
  ↓
Concentration Prep (85) [stops movement]
  ↓
Auto-transition (0.0s)
  ↓
Concentration Hold (70) ←→ Walk/Run [blended movement]
  ↓
Break Concentration → Return to Idle
```

---

## Priority Override Rules

```
Ritual (90) > Channeling/Ground/Prep (85) > Touch (83) > Instant (80) > Somatic (75) > Hold (70) > Combat (60) > Walk (50) > Idle (40)
```

**Key Rules**:
- Higher number always interrupts lower number
- Concentration Hold (70) is special: allows blending with Walk (50)
- Combat (60) cannot interrupt any magic (70+)
- Magic (80+) can interrupt Combat (60)

---

## Particle Timing Cheat Sheet

### Instant Cast (0.5s)
```
0.15s → Pre-cast glow at hand (5 particles)
0.20s → Main burst (15 particles)
0.25s → Trail particles
```

### Channeled (2.0s loop)
```
Every 0.1s → Continuous stream (2 particles)
0.5s, 1.5s → Pulse effect (8 particles)
```

### Ground Target (1.2s)
```
0.0s-0.5s → Energy gathering (growing intensity)
0.5s → Projectile launch
0.5s-0.8s → Ground explosion effect (20 particles)
```

### Touch (1.0s)
```
0.3s-0.5s → Energy at hand (gathering)
0.5s → Touch burst
0.5s-0.7s → Transfer effect
```

### Concentration Prep (1.2s)
```
0.0s-0.3s → Growing sphere around caster
0.3s-0.7s → Intensify
0.7s → Activation burst (30 particles, 1.5m radius)
0.7s-1.2s → Lingering aura
```

### Concentration Hold (3.0s loop)
```
Every 1.5s → Gentle pulse (5 particles)
Continuous → Soft aura around hands
```

### Ritual (4.0s loop)
```
Continuous → Runic symbols appear and fade
Every 1.0s → Energy pulse
Variable → Symbols trace hand movement
```

### Somatic (0.3s)
```
0.2s → Small sparkle at hand
```

---

## Movement Speed Table

| Animation | Walk Speed | Run Speed | Can Move? |
|-----------|------------|-----------|-----------|
| Instant Cast | 100% | 100% | ✅ Yes |
| Channeling | 50% | ❌ No | ⚠️ Slow walk only |
| Ground Target | 0% | 0% | ❌ No |
| Touch | 0% | 0% | ❌ No |
| Concentration Prep | 0% | 0% | ❌ No |
| Concentration Hold | 100% | 100% | ✅ Yes (blended) |
| Ritual | 0% | 0% | ❌ No |
| Somatic Gesture | 100% | 100% | ✅ Yes |

---

## Key Pose Times (for BlockBench reference)

### Instant Cast (0.5s)
- **0.0s**: Neutral rest
- **0.1s**: ⭐ Windup (gather energy)
- **0.2s**: ⭐ Cast moment (release)
- **0.3s**: Recoil
- **0.5s**: Return to rest

### Channeling (2.0s loop)
- **0.0s**: Start sustained pose
- **0.5s**: ⭐ Variation 1
- **1.0s**: Return to base
- **1.5s**: ⭐ Variation 2
- **2.0s**: Loop back to start

### Ground Target (1.2s)
- **0.0s**: Neutral
- **0.2s**: ⭐ Lean back (gather)
- **0.5s**: ⭐ Point down (cast)
- **0.8s**: Hold point
- **1.2s**: Return to neutral

### Touch (1.0s)
- **0.0s**: Neutral
- **0.2s**: Begin reach
- **0.5s**: ⭐ Full reach (contact)
- **0.7s**: Hold contact
- **1.0s**: Return

### Concentration Prep (1.2s)
- **0.0s**: Neutral
- **0.3s**: ⭐ Crouch and gather
- **0.7s**: ⭐ Rise and release
- **1.2s**: Settle into hold pose

### Concentration Hold (3.0s loop)
- **0.0s**: Base sustained pose
- **0.75s**: Drift variation 1
- **1.5s**: Return to base
- **2.25s**: Drift variation 2
- **3.0s**: Loop back

### Ritual (4.0s loop)
- **0.0s**: Start position
- **1.0s**: ⭐ Sweep/gesture 1
- **2.0s**: Return to start
- **3.0s**: ⭐ Sweep/gesture 2
- **4.0s**: Loop back

### Somatic Gesture (0.3s)
- **0.0s**: Rest
- **0.1s**: Quick raise
- **0.2s**: ⭐ Snap gesture
- **0.3s**: Return to rest

---

## Arm Rotation Quick Reference

### Right Arm Common Poses

```
Rest:          [0, 0, 0]
Extended Forward: [-90, 0, 0]
Extended Side:   [-90, 0, -15]
Point Down:     [45, 0, 0]
Point Up:       [-120, 0, 0]
Gather Energy:  [-45, 15, -20]
Touch Reach:    [-85, 0, -5]
```

### Hand Rotation Common Poses

```
Relaxed:       [0, 0, 0]
Fingers Curl:  [0, 0, 15]
Fingers Spread: [0, 0, -15]
Point:         [0, 0, -20]
Open Palm:     [0, 0, 15]
Grip:          [0, 0, -15]
```

### Head Rotation Common Poses

```
Forward:       [0, 0, 0]
Look Down:     [15, 0, 0]
Look Up:       [-10, 0, 0]
Look Right:    [0, 10, 0]
Look Left:     [0, -10, 0]
Focus Down (ground): [35, 0, 0]
Concentration: [-5, 0, 0]
```

---

## Easing Quick Reference

### When to Use Each Easing Type

**Linear**:
- Constant speed motion
- Mechanical movements
- Instant cast snap (0.1s → 0.2s)
- Holding positions

**Ease-Out** (slow start, fast end):
- Beginning of movements
- Windup phases
- Gathering energy
- Example: Instant cast 0.0s → 0.1s

**Ease-In** (fast start, slow end):
- End of movements
- Recovery phases
- Returning to rest
- Example: Ground target 0.8s → 1.2s

**Ease-In-Out** (smooth both ends):
- Natural motions
- Breathing
- Gentle transitions
- All Concentration Hold transitions
- Ritual movements

---

## Blending Weights Reference

### Concentration Hold + Walk Blend

```
Upper Body (uses Concentration Hold 100%):
- torso_upper
- arm_right, arm_left
- forearm_right, forearm_left
- hand_right, hand_left
- head (90% concentration, 10% walk)

Lower Body (uses Walk 100%):
- body (vertical bob only)
- leg_right, leg_left
- shin_right, shin_left
- foot_right, foot_left
```

### Channeling + Slow Walk Blend

```
Blend Ratio: 70% Channeling, 30% Walk
Walk Speed: 50% of normal
All bones blend proportionally
```

---

## Common Animation Combinations

### Spell Combat Sequence

```
1. Idle (40)
2. Instant Cast (80) - 0.5s
3. Return to Idle (40)
4. Walk toward target (50)
5. Ground Target (85) - 1.2s [walk stops]
6. Return to Walk (50)
```

### Concentration Spell Sequence

```
1. Walking (50)
2. Concentration Prep (85) - 1.2s [walk stops]
3. Auto-transition to Concentration Hold (70)
4. Resume Walking (50) [blended with hold]
   ... maintain concentration while moving ...
5. Break Concentration → Return to Walk (50)
```

### Ritual Casting Sequence

```
1. Idle (40)
2. Ritual Cast (90) - loops until complete [cannot be interrupted]
   ... 10 minutes later ...
3. Ritual Complete → Return to Idle (40)
```

### Quick Spellcasting Combo

```
1. Running (50)
2. Instant Cast (80) - 0.5s [keeps running]
3. Somatic Gesture (75) - 0.3s [keeps running]
4. Instant Cast (80) - 0.5s [keeps running]
5. Continue Running (50)
```

---

## Debugging Checklist

When an animation doesn't look right:

### Visual Issues
- [ ] Check all keyframes are present
- [ ] Verify rotation values are within safe limits
- [ ] Ensure first and last keyframes match (for loops)
- [ ] Check easing types are appropriate
- [ ] Test on all 4 races (Human, Dwarf, Elf, Halfling)

### Timing Issues
- [ ] Verify animation length matches spec
- [ ] Check particle timing aligns with key poses
- [ ] Ensure loop point is seamless
- [ ] Test transition times to/from other animations

### Integration Issues
- [ ] Verify priority is correct
- [ ] Check movement speed is set correctly
- [ ] Ensure blending works with locomotion
- [ ] Test auto-transitions (concentration prep → hold)

### Performance Issues
- [ ] Check for excessive keyframes
- [ ] Verify particle count isn't too high
- [ ] Test with multiple entities casting
- [ ] Profile bone retargeting calculations

---

## File Locations Reference

### Animation File
```
src/main/resources/assets/fiveesrd/animations/entity/character/magic.animation.json
```

### Integration Code
```
src/main/java/ninja/trek/srd/character/entity/CharacterEntity.java
src/client/java/ninja/trek/srd/client/model/geckolib/CharacterGeoModel.java
```

### Documentation
```
docs/MAGIC_ANIMATIONS_SPEC.md (detailed specifications)
docs/MAGIC_ANIMATIONS_QUICK_REF.md (this file)
docs/BLOCKBENCH_SPECIFICATIONS.md (bone structure reference)
docs/ANIMATION_RETARGETING.md (retargeting system)
```

---

## Spell School Animation Recommendations

### Evocation (Damage Spells)
- **Fire spells**: Use `ground_target` with aggressive pointing
- **Lightning**: Use `instant_cast` with sharp snap
- **Force**: Use `channel_concentrated` with both hands

### Conjuration (Summoning)
- **Summon creature**: Use `ritual_cast` or `concentration_prep`
- **Create object**: Use `instant_cast` or `somatic_gesture`

### Abjuration (Protection)
- **Shield spells**: Use `instant_cast` (quick defense)
- **Wards**: Use `concentration_prep` → `concentration_hold`

### Necromancy (Life/Death)
- **Healing**: Use `touch` or `channel_concentrated`
- **Damage undead**: Use `channel_concentrated` (beam)

### Enchantment (Mind)
- **Charm**: Use `somatic_gesture` (subtle)
- **Hold person**: Use `ground_target` (focus on target)

### Illusion (Deception)
- **Minor illusion**: Use `somatic_gesture` (quick and subtle)
- **Major image**: Use `concentration_prep` → `concentration_hold`

### Transmutation (Change)
- **Buff spells**: Use `concentration_prep` → `concentration_hold`
- **Polymorph**: Use `ritual_cast` (complex transformation)

### Divination (Knowledge)
- **Detect magic**: Use `concentration_prep` → `concentration_hold`
- **Scrying**: Use `ritual_cast` (long concentration)

---

## Animation Priority Override Examples

### Example 1: Combat to Magic
```
Current: Attack (60)
Trigger: Instant Cast (80)
Result: ✅ Instant Cast interrupts Attack (80 > 60)
```

### Example 2: Magic to Magic
```
Current: Channeling (85)
Trigger: Instant Cast (80)
Result: ❌ Channeling continues (85 > 80)
```

### Example 3: Concentration Hold to Magic
```
Current: Concentration Hold (70)
Trigger: Instant Cast (80)
Result: ✅ Instant Cast interrupts, breaks concentration (80 > 70)
```

### Example 4: Ritual Cannot Be Interrupted
```
Current: Ritual Cast (90)
Trigger: Ground Target (85)
Result: ❌ Ritual continues (90 > 85)
```

---

## Common Mistakes to Avoid

### ❌ Don't Do This

1. **Exceeding safe rotation limits**
   - Arm X rotation > 60° or < -120°
   - Results in unnatural poses

2. **Missing loop point keyframes**
   - First and last keyframes don't match
   - Results in jerky loop

3. **Too many keyframes**
   - Keyframe every 0.01s
   - Results in choppy animation and large file size

4. **Wrong easing on snap gestures**
   - Using ease-in-out on instant cast snap
   - Results in slow, mushy gesture

5. **Animating lower body on concentration hold**
   - Adding leg movements
   - Prevents blending with walk animation

### ✅ Do This Instead

1. **Stay within safe limits**
   - Reference safe rotation table
   - Test on all races

2. **Match loop points exactly**
   - Copy first keyframe to last keyframe
   - Verify seamless loop in BlockBench

3. **Use minimal keyframes**
   - Only add keyframes at key poses
   - Let easing handle in-between motion

4. **Use linear easing for snaps**
   - Instant, quick gestures use linear
   - Feels more responsive

5. **Keep lower body neutral on concentration hold**
   - Only animate upper body
   - Allows locomotion to control legs

---

**End of Quick Reference**

For detailed specifications, see **MAGIC_ANIMATIONS_SPEC.md**
