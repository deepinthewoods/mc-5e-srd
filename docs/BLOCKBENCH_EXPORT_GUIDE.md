# BlockBench Export Guide for GeckoLib Models

This guide explains how to create and export proper BlockBench models to replace the placeholder models in this project.

## Current Status

The project currently contains **placeholder models** that were auto-generated to allow development to continue. These models have the correct bone structure but use simple cube geometry. You should replace them with properly designed models from BlockBench.

## Placeholder Models Location

```
src/main/resources/assets/fiveesrd/geo/entity/character/
├── human/human_body.geo.json          (PLACEHOLDER)
├── dwarf/dwarf_body.geo.json          (PLACEHOLDER)
├── elf/elf_body.geo.json              (PLACEHOLDER)
└── halfling/halfling_body.geo.json    (PLACEHOLDER)
```

## Prerequisites

1. **BlockBench** (latest version): https://www.blockbench.net/
2. **GeckoLib Plugin** for BlockBench
3. Read `BLOCKBENCH_SPECIFICATIONS.md` for detailed bone structure requirements

## Step 1: Setting Up BlockBench

### Install GeckoLib Plugin

1. Open BlockBench
2. Go to **File → Plugins**
3. Search for "GeckoLib Animation Utils"
4. Click **Install**
5. Restart BlockBench

### Create New Project

1. **File → New → Bedrock Model**
2. Set **Model Identifier**: `geometry.character.human` (or dwarf/elf/halfling)
3. Set **Texture Size**: 64x64
4. Click **Confirm**

## Step 2: Required Bone Structure

### CRITICAL: Use Exact Bone Names

All models MUST use these exact bone names (defined in `HumanoidBones.java`):

```
root
└── body
    ├── torso_lower
    │   └── torso_upper
    │       ├── head
    │       │   ├── head_top
    │       │   └── jaw
    │       ├── arm_right
    │       │   ├── forearm_right
    │       │   │   └── hand_right
    │       │   └── shoulder_armor_right
    │       └── arm_left
    │           ├── forearm_left
    │           │   └── hand_left
    │           └── shoulder_armor_left
    ├── leg_right
    │   ├── shin_right
    │   │   └── foot_right
    │   └── thigh_armor_right
    └── leg_left
        ├── shin_left
        │   └── foot_left
        └── thigh_armor_left
```

### Bone Pivot Points

Refer to `SkeletonProfile.java` for bone lengths. Use these as guidelines for pivot placement:

**Human (base reference):**
- torso_upper: 0.6m tall
- torso_lower: 0.4m tall
- head: 0.3m tall
- arm_right/left: 0.4m
- forearm_right/left: 0.35m
- leg_right/left: 0.5m
- shin_right/left: 0.45m

**Dwarf:** Shorter legs (0.35m/0.3m), stockier build

**Elf:** Longer legs (0.6m/0.55m), slender build

**Halfling:** Shortest (0.3m/0.25m legs), compact build

## Step 3: Creating Geometry

### Tips for Good Models

1. **Start Simple**: Begin with basic shapes, refine later
2. **Use Cubes Wisely**: Combine multiple cubes for detail
3. **Respect Proportions**: Match the race's skeletal profile
4. **Add Inflation for Armor**: Leave space for equipment layers
5. **Test Early**: Export and test in-game frequently

### Body Parts to Model

1. **Base Body** (required):
   - Torso (upper and lower)
   - Head
   - Arms and hands
   - Legs and feet

2. **Armor Attachment Points** (empty bones):
   - shoulder_armor_right/left
   - thigh_armor_right/left
   - These are used for equipment layers

### Variant Groups (Optional)

For body part variants (Phase 5), use folders in BlockBench:
```
head/
├── head_variant_0/
├── head_variant_1/
└── head_variant_2/
```

## Step 4: Creating Textures

1. **Size**: 64x64 pixels (PNG format)
2. **Name**: `{race}_default.png` (e.g., `human_default.png`)
3. **UV Mapping**: Use BlockBench's UV editor
4. **Export Location**: `src/main/resources/assets/fiveesrd/textures/entity/character/base/`

### Current Placeholder Textures

All races currently use copies of `human_default.png`. Replace with unique textures:
- `dwarf_default.png` - Stocky, bearded aesthetic
- `elf_default.png` - Elegant, refined features
- `halfling_default.png` - Smaller, cheerful appearance

## Step 5: Creating Animations

Animations are shared across all races via bone retargeting.

### Required Animations

1. **idle** (2s loop): Breathing, slight sway
2. **walk** (1s loop): Walking cycle
3. **run** (0.6s loop): Running cycle
4. **attack** (0.5s): Melee attack swing

### Animation Best Practices

1. **Animate Rotations, Not Positions**: Retargeting works best with rotations
2. **Keyframe Timing**: Use smooth easing between keyframes
3. **Loop Seamlessly**: First and last keyframes should match
4. **Test All Races**: Verify animations work on all skeletal proportions

### Animation Export

- **Name**: `locomotion.animation.json`
- **Location**: `src/main/resources/assets/fiveesrd/animations/entity/character/`
- **Format**: GeckoLib format (automatic with plugin)

## Step 6: Exporting

### Export Model (.geo.json)

1. **File → Export → Export GeckoLib Model**
2. **Filename**: `{race}_body.geo.json`
3. **Save to**: `src/main/resources/assets/fiveesrd/geo/entity/character/{race}/`
4. Ensure "Export Animations" is **unchecked** (we do that separately)

### Export Animations (.animation.json)

1. **File → Export → Export GeckoLib Animations**
2. **Filename**: `locomotion.animation.json`
3. **Save to**: `src/main/resources/assets/fiveesrd/animations/entity/character/`
4. Include all locomotion animations in one file

## Step 7: Testing In-Game

### Build and Run

```bash
./gradlew build
./gradlew runClient
```

### Spawn Test Character

```
/summon fiveesrd:character ~ ~ ~
```

### Verify

- ✅ Model loads without errors
- ✅ Textures display correctly
- ✅ Bones are in correct positions
- ✅ Animations play smoothly
- ✅ No console errors about missing bones

### Common Issues

**Model doesn't appear:**
- Check console for missing texture/model errors
- Verify file paths match exactly
- Ensure bone names are correct

**Animations look wrong:**
- Check bone pivot points
- Verify parent-child relationships
- Test animation on base human model first

**Texture is black/white:**
- Ensure texture file exists in correct location
- Check UV mapping in BlockBench
- Verify texture size is 64x64

## Step 8: Creating Remaining Races

Once you have a working human model:

1. **Duplicate** the human BlockBench file
2. **Adjust proportions** based on `SkeletonProfile.java`
3. **Modify geometry** to match race aesthetics
4. **Create unique texture**
5. **Export** to appropriate directories
6. **Test in-game**

### Race-Specific Guidelines

**Dwarf:**
- Wider torso, broader shoulders
- Shorter arms and legs
- Larger head (proportionally)
- Add beard geometry

**Elf:**
- Taller, more slender
- Longer limbs
- Smaller head (proportionally)
- Add ear geometry

**Halfling:**
- Smallest overall
- Round, friendly proportions
- Large head (childlike proportions)
- Smaller feet

## Animation Retargeting Test

After creating all race models, test retargeting:

1. Spawn all 4 races together
2. Push them to make them walk
3. Verify each race:
   - Walks at appropriate speed
   - Limbs don't disconnect
   - Animations look natural
   - Proportions are maintained

## Quality Checklist

Before considering a model "complete":

- [ ] All required bones present with exact names
- [ ] Bone hierarchy matches specification
- [ ] Pivot points correctly positioned
- [ ] Geometry looks good from all angles
- [ ] UV mapping complete, no missing textures
- [ ] Texture exported and in correct location
- [ ] Model exports without errors
- [ ] Loads in-game without console errors
- [ ] Animations play correctly
- [ ] Bone retargeting works (if not base human)
- [ ] No visual glitches (Z-fighting, clipping)

## Advanced Features (Future)

Once basic models are complete, you can add:

1. **Facial Animations**: Blinking, expressions
2. **Body Variants**: Multiple heads, torsos, etc.
3. **Equipment Models**: Armor, weapons
4. **Particle Attachments**: Effects, glows
5. **LOD Models**: Lower-detail versions for distant entities

## Resources

- **BlockBench Wiki**: https://www.blockbench.net/wiki/
- **GeckoLib Docs**: https://docs.geckolib.com/
- **Project Specifications**: See `docs/BLOCKBENCH_SPECIFICATIONS.md`
- **Animation Reference**: See `docs/ANIMATION_RETARGETING.md`

## Getting Help

If you encounter issues:

1. Check console logs for error messages
2. Verify bone names match exactly
3. Test with simpler geometry first
4. Compare to placeholder models
5. Ask in GeckoLib Discord: https://discord.gg/MNQcKxB

---

**Remember**: The placeholder models are functional but basic. Proper BlockBench models will make your characters look professional and unique!

**Last Updated**: 2025-11-13
