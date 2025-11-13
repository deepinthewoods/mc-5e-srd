# Character Animations

This directory contains GeckoLib animation files (.animation.json) for character animations.

## Required Animation Files

### locomotion.animation.json
Basic movement animations:
- `idle` - Standing still (2 second loop)
- `walk` - Walking animation (1 second loop)
- `run` - Running animation (0.6 second loop)

### combat.animation.json (Phase 8)
Combat animations:
- `attack_melee_1h` - One-handed weapon attack
- `attack_melee_2h` - Two-handed weapon attack
- `attack_unarmed` - Unarmed attack
- `bow_draw` - Drawing a bow
- `bow_hold` - Holding drawn bow
- `bow_release` - Releasing arrow
- `block_shield` - Shield blocking

### magic.animation.json (Phase 8)
Spell casting animations:
- `cast_instant` - Quick spell cast
- `cast_channeling` - Channeled spell
- `cast_ground_target` - Ground-targeted spell
- `cast_self_buff` - Self-buff animation

## Creating Animations

1. Open your character model in BlockBench
2. Switch to "Animate" tab
3. Create new animation (e.g., "walk")
4. Set loop mode and duration
5. Add keyframes for bones
6. Follow guidelines in `/docs/BLOCKBENCH_SPECIFICATIONS.md`

### Animation Guidelines

**DO:**
- ✅ Animate primarily using ROTATIONS
- ✅ Keep root bone stationary (except for height changes)
- ✅ Use smooth easing for natural motion
- ✅ Test animations at different speeds

**DON'T:**
- ❌ Animate positions heavily (breaks retargeting)
- ❌ Use different bone names than specification
- ❌ Make animations too fast or too slow
- ❌ Forget to set loop mode correctly

### Bone Retargeting

All animations are created for the HUMAN skeleton (base), then automatically retargeted to other races using `BoneRetargetingController`.

The retargeting system:
- Preserves rotation keyframes (angles work at any scale)
- Scales translation keyframes based on bone length ratios
- Adjusts walk/run speed based on leg length

Example: A human walk animation with 0.8m stride automatically becomes 0.58m stride for a dwarf.

## Export from BlockBench

1. File → Export → Export GeckoLib Animations
2. Save as `locomotion.animation.json` (or appropriate name)
3. Place in this directory
4. The game will automatically load animations

## Minimum Viable Product (MVP)

For initial testing, you only need `locomotion.animation.json` with:
- idle
- walk
- run

Other animations can be added in later phases.

## Animation Testing

To test animations in-game:
1. Build the mod: `./gradlew build`
2. Run the client: `./gradlew runClient`
3. Spawn a character: `/summon fiveesrd:character`
4. Push the entity to see walk animation
5. Check console for any animation errors

## Resources

- GeckoLib Documentation: https://docs.geckolib.com/
- BlockBench Tutorials: https://www.blockbench.net/wiki/
- Example Animations: https://github.com/bernie-g/geckolib-examples

For detailed specifications, see:
- `/docs/BLOCKBENCH_SPECIFICATIONS.md`
- `/docs/ANIMATION_RETARGETING.md`
