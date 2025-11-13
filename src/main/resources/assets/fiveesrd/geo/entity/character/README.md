# Character Geometry Files

This directory contains GeckoLib geometry files (.geo.json) for character models.

## Directory Structure

- **base/**: Base humanoid skeleton reference models
- **dwarf/**: Dwarf race models (shorter, stockier proportions)
- **elf/**: Elf race models (taller, slender proportions)
- **human/**: Human race models (baseline proportions)
- **halfling/**: Halfling race models (smallest proportions)

## File Naming Convention

For each race, create the following files:
- `{race}_body.geo.json` - Main body model with all bones and geometry

Example for human:
- `human/human_body.geo.json`

## Creating Models

1. Open BlockBench (https://www.blockbench.net/)
2. Create a new "Animated Entity" project with GeckoLib plugin
3. Follow the bone structure defined in `/docs/BLOCKBENCH_SPECIFICATIONS.md`
4. Use exact bone names from `HumanoidBones.java`:
   - root
   - body
   - head, torso_upper, torso_lower
   - arm_right, forearm_right, hand_right
   - arm_left, forearm_left, hand_left
   - leg_right, shin_right, foot_right
   - leg_left, shin_left, foot_left
5. Export as "GeckoLib Animated Model"
6. Save .geo.json file to appropriate race directory

## Skeleton Profiles

Each race has specific bone lengths defined in `SkeletonProfile.java`:

### Human (Base)
- Total Height: 1.8m
- Torso Upper: 0.6m, Lower: 0.4m
- Arms: 0.4m + 0.35m forearm
- Legs: 0.5m + 0.45m shin

### Dwarf
- Total Height: 1.3m
- Shorter, stockier limbs
- Proportionally larger head (0.32m)

### Elf
- Total Height: 2.0m
- Longer, slender limbs
- Proportionally smaller head (0.28m)

### Halfling
- Total Height: 1.0m
- Very short limbs
- Proportionally large head (0.3m)

## Important Notes

⚠️ **All races must use the EXACT same bone names and hierarchy**
⚠️ Only bone LENGTHS differ between races, not structure
⚠️ This allows animation retargeting to work properly

## Next Steps

Until models are created in BlockBench, the renderer will fail to find resources.
You can:

1. Create models yourself using BlockBench
2. Use placeholder cube models for testing
3. Reference the GeckoLib examples: https://github.com/bernie-g/geckolib-examples

For detailed specifications, see:
- `/docs/BLOCKBENCH_SPECIFICATIONS.md`
- `/docs/GECKOLIB_ARCHITECTURE.md`
