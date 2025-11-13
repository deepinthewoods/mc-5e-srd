# BlockBench Export Instructions

## Overview

The `human_basic.bbmodel` file now contains:
- ✅ Complete humanoid skeleton with standard bone hierarchy
- ✅ Basic geometry for all body parts
- ✅ Three locomotion animations (idle, walk, run)
- ✅ Embedded texture reference

**Next Step**: Export the model and animations to GeckoLib format for use in-game.

---

## Prerequisites

1. **Download BlockBench**: https://www.blockbench.net/
2. **Install GeckoLib Plugin**:
   - Open BlockBench
   - Go to `File` → `Plugins` → `Available`
   - Search for "GeckoLib Animation Utils"
   - Click `Install`
   - Restart BlockBench

---

## Export Instructions

### Step 1: Open the Model

1. Launch BlockBench
2. `File` → `Open Model`
3. Navigate to: `/home/user/mc-5e-srd/human_basic.bbmodel`
4. The model should load with all bones, geometry, and animations visible

### Step 2: Verify the Model

Before exporting, verify:
- ✅ All bones are present in the outliner (root → body → head, torso_upper, torso_lower, etc.)
- ✅ Geometry cubes are visible for each body part
- ✅ Three animations appear in the animation timeline: `idle`, `walk`, `run`
- ✅ Click through each animation to preview them

### Step 3: Export Geometry (.geo.json)

1. `File` → `Export` → `Export GeckoLib Model`
2. **Save Location**:
   ```
   /home/user/mc-5e-srd/src/main/resources/assets/fiveesrd/geo/entity/character/human/human_body.geo.json
   ```
3. **File Name**: `human_body.geo.json`
4. Click `Save`

**Verify**: The file should be ~10-50 KB in size and contain JSON geometry data.

### Step 4: Export Animations (.animation.json)

1. `File` → `Export` → `Export GeckoLib Animations`
2. **Save Location**:
   ```
   /home/user/mc-5e-srd/src/main/resources/assets/fiveesrd/animations/entity/character/locomotion.animation.json
   ```
3. **File Name**: `locomotion.animation.json`
4. **Important**: Ensure ALL animations are selected for export (idle, walk, run)
5. Click `Save`

**Verify**: The file should contain animation data for all three animations.

---

## Verification Checklist

After exporting, verify the following files exist:

```
src/main/resources/assets/fiveesrd/
├── geo/
│   └── entity/
│       └── character/
│           └── human/
│               └── human_body.geo.json          ✅ Should exist
├── animations/
│   └── entity/
│       └── character/
│           └── locomotion.animation.json        ✅ Should exist
└── textures/
    └── entity/
        └── character/
            └── base/
                └── human_default.png            ✅ Already created
```

---

## Quick Verification

Run these commands to verify the files exist:

```bash
# Check geometry file
ls -lh src/main/resources/assets/fiveesrd/geo/entity/character/human/human_body.geo.json

# Check animation file
ls -lh src/main/resources/assets/fiveesrd/animations/entity/character/locomotion.animation.json

# Check texture file (already created)
ls -lh src/main/resources/assets/fiveesrd/textures/entity/character/base/human_default.png
```

All three files should exist.

---

## Troubleshooting

### Problem: "GeckoLib Export" option not available

**Solution**:
1. Make sure you installed the GeckoLib Animation Utils plugin
2. Restart BlockBench after installation
3. The export options should appear under `File` → `Export`

### Problem: Export fails with "No animations found"

**Solution**:
1. Click on the animation timeline at the bottom of BlockBench
2. Verify you can see `idle`, `walk`, and `run` animations
3. If not visible, the animations may not have loaded correctly from the .bbmodel file
4. Contact for assistance

### Problem: Exported geometry looks wrong in-game

**Solution**:
1. Re-export with these settings:
   - Format: GeckoLib Model
   - Model Identifier: `geometry.character.human`
2. Verify bone hierarchy matches the documentation
3. Check that all bone names are lowercase with underscores (e.g., `arm_right`, not `ArmRight`)

---

## After Export: Building and Testing

Once you've exported both files:

1. **Test Build**:
   ```bash
   ./gradlew build
   ```

2. **Run In-Game**:
   ```bash
   ./gradlew runClient
   ```

3. **Spawn Character**:
   ```
   /summon fiveesrd:character
   ```

4. **Expected Result**:
   - Character entity should appear with the humanoid model
   - Default texture should be applied (beige/tan colors)
   - Idle animation should play automatically
   - Walking should trigger walk animation

---

## Next Steps After Successful Export

Once the model is rendering correctly:

1. **Phase 2 Complete**: Basic model rendering is working
2. **Phase 3**: Animation system integration and testing
3. **Phase 4**: Create models for other races (dwarf, elf, halfling)
4. **Phase 5**: Add body part variants

See `docs/IMPLEMENTATION_PLAN.md` for the full roadmap.

---

## Need Help?

If you encounter issues during export:
1. Check the BlockBench console for error messages (`Help` → `Developer` → `Toggle Developer Tools`)
2. Verify the GeckoLib plugin version is compatible with BlockBench
3. Make sure you're using the latest version of BlockBench (4.x or higher)
4. Check that the .bbmodel file is valid JSON

---

**Last Updated**: 2025-11-13
**Status**: Ready to Export
