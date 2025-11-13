# Combat Animations Quick Reference

**For**: Artists creating animations in BlockBench
**See Full Spec**: `COMBAT_ANIMATIONS_SPEC.md`

## Animation List

| Animation Name | Duration | Loop | Purpose |
|----------------|----------|------|---------|
| `animation.character.combat.attack_melee_1h` | 0.5s | No | One-handed weapon swing |
| `animation.character.combat.attack_melee_2h` | 0.8s | No | Two-handed overhead strike |
| `animation.character.combat.attack_unarmed_punch` | 0.35s | No | Quick punch |
| `animation.character.combat.attack_unarmed_kick` | 0.5s | No | Front kick |
| `animation.character.combat.bow_draw` | 0.4s | No | Draw arrow and bowstring |
| `animation.character.combat.bow_hold` | 2.0s | Yes | Hold full draw while aiming |
| `animation.character.combat.bow_release` | 0.4s | No | Release arrow and recoil |
| `animation.character.combat.block_shield` | 2.0s | Yes | Raise and hold shield |
| `animation.character.combat.block_weapon` | 0.3s | No | Quick weapon parry |

## Key Principles

### ✅ DO
- Use **rotations only** (not position) for limb movements
- Follow bone naming from `BLOCKBENCH_SPECIFICATIONS.md`
- Create smooth, natural motion arcs
- Add anticipation (wind-up) and follow-through
- Test animations at 30 FPS
- Make sure loops are seamless (first frame = last frame)

### ❌ DON'T
- Don't use position keyframes on limbs (breaks retargeting)
- Don't exceed natural rotation limits (see spec)
- Don't make animations too slow (combat needs to feel responsive)
- Don't forget the impact frame (critical for hit detection)

## Critical Frames

Each animation has an **impact frame** where hit detection occurs in code:

- **attack_melee_1h**: 0.25s (mid-swing)
- **attack_melee_2h**: 0.45s (overhead slam)
- **attack_unarmed_punch**: 0.18s (fist extension)
- **attack_unarmed_kick**: 0.28s (leg extension)
- **bow_release**: 0.05s (arrow release)

## Bone Rotation Limits

Quick reference for natural motion:

```
Head:        ±45° pitch, ±80° yaw, ±20° roll
Torso:       ±45° twist, -20° to +30° bend
Arms:        Full rotation (±180°)
Forearms:    0° to -150° (elbow bend)
Legs:        ±90° swing
Shins:       0° to -150° (knee bend)
```

## Easing Curves

- **Wind-up**: `easeIn` (slow → fast)
- **Strike**: `linear` (constant speed)
- **Impact**: `easeOut` (fast → slow)
- **Recovery**: `easeInOut` (smooth)

## Export Checklist

Before exporting to `combat.animation.json`:

- [ ] Animation name matches exactly
- [ ] Duration is correct
- [ ] Loop setting is correct (true/false)
- [ ] All keyframes use rotations
- [ ] Easing curves applied
- [ ] Animation plays smoothly
- [ ] No bone popping/disconnections
- [ ] Impact frame is clearly defined

## File Location

Export to:
```
src/main/resources/assets/fiveesrd/animations/entity/character/combat.animation.json
```

## Need Help?

- Full specifications: `COMBAT_ANIMATIONS_SPEC.md`
- Bone structure: `BLOCKBENCH_SPECIFICATIONS.md`
- Retargeting info: `ANIMATION_RETARGETING.md`
- BlockBench docs: https://blockbench.net/wiki/

---

**Tip**: Start with one animation (e.g., 1h attack), get it approved, then use it as a template for others.
