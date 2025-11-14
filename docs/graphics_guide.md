# Graphics Creation Guide for 5e SRD Mod

## Table of Contents

1. [Introduction](#introduction)
2. [Understanding the Workflow](#understanding-the-workflow)
3. [Tools Setup](#tools-setup)
4. [Creating Models in Blockbench](#creating-models-in-blockbench)
5. [Creating Textures](#creating-textures)
6. [Creating Animations](#creating-animations)
7. [Exporting from Blockbench](#exporting-from-blockbench)
8. [File Organization](#file-organization)
9. [Testing Your Work](#testing-your-work)
10. [Tips & Troubleshooting](#tips--troubleshooting)

---

## Introduction

This guide explains how to create all the graphics assets for the 5e SRD mod, including:
- **Character models** (different races with body variants)
- **Equipment models** (armor, weapons, accessories)
- **Textures** (skins, clothing, armor materials)
- **Animations** (walk, run, combat, magic)

### What are these JSON files?

The `.geo.json` and `.animation.json` files you see in the project are **exported from Blockbench**. They contain:
- **Geometry data** (.geo.json): 3D model structure, vertices, faces, UV mapping
- **Animation data** (.animation.json): Keyframe animations with bone rotations and positions

You **don't edit these JSON files directly**. Instead, you create and edit models in Blockbench, then export them to JSON format.

---

## Understanding the Workflow

### The Graphics Pipeline

```
┌──────────────┐      ┌──────────────┐      ┌──────────────┐
│  Blockbench  │ ───> │  Export to   │ ───> │  Minecraft   │
│  (.bbmodel)  │      │    JSON      │      │   Runtime    │
│              │      │              │      │              │
│ - Create     │      │ - .geo.json  │      │ - GeckoLib   │
│   geometry   │      │ - .animation │      │   renders    │
│ - Animate    │      │   .json      │      │   models     │
│ - Texture    │      │              │      │              │
└──────────────┘      └──────────────┘      └──────────────┘
```

### Key Concepts

1. **Source Files (.bbmodel)**: Editable Blockbench projects (stored in project root)
2. **Exported Files (.geo.json)**: Generated geometry files (stored in `src/main/resources/assets/fiveesrd/geo/`)
3. **Textures (.png)**: Texture images (stored in `src/main/resources/assets/fiveesrd/textures/`)
4. **Animations (.animation.json)**: Exported animation data (stored in `src/main/resources/assets/fiveesrd/animations/`)

**Important**: Always keep both the .bbmodel source files AND the exported JSON files in version control!

---

## Tools Setup

### Required Software

1. **Blockbench** (Free)
   - Download: https://www.blockbench.net/
   - Version: Latest (4.5+)
   - Platform: Windows, Mac, Linux, or Web

2. **GeckoLib Plugin for Blockbench**
   - Required for proper export format
   - Install within Blockbench

3. **Texture Editor** (Choose one)
   - GIMP (Free): https://www.gimp.org/
   - Aseprite (Paid, pixel art focused): https://www.aseprite.org/
   - Photoshop (Paid)
   - Paint.NET (Free, Windows)

### Installing GeckoLib Plugin

1. Open Blockbench
2. Go to **File → Plugins**
3. Search for **"GeckoLib Animation Utils"**
4. Click **Install**
5. Restart Blockbench

You should now see "GeckoLib Model" as an option when creating new projects.

---

## Creating Models in Blockbench

### Starting a New Character Model

#### Step 1: Create New Project

1. **File → New → Bedrock Model**
2. In the dialog:
   - **Model Format**: Select "Bedrock Model" or "GeckoLib Animated Model"
   - **Model Identifier**: `geometry.character.human` (or dwarf/elf/halfling)
   - **Texture Width**: 64
   - **Texture Height**: 64
3. Click **Confirm**

#### Step 2: Create the Skeleton

**CRITICAL**: Use these exact bone names and hierarchy. The code expects these specific names!

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

**To create bones**:
1. Click the **"Add Bone"** button (or press B)
2. Name it exactly as shown above
3. Set the pivot point (see below)
4. Parent it correctly by dragging it onto the parent bone

#### Step 3: Set Pivot Points

Pivot points are where bones rotate from (like joints). Here are the reference positions for a **human** (1.8m tall):

| Bone Name | Position (X, Y, Z) | Description |
|-----------|-------------------|-------------|
| root | (0, 0, 0) | Ground level |
| body | (0, 12, 0) | Base of torso |
| head | (0, 24, 0) | Neck |
| torso_upper | (0, 18, 0) | Mid-torso |
| torso_lower | (0, 12, 0) | Waist |
| arm_right | (-6, 22, 0) | Right shoulder |
| forearm_right | (-6, 14, 0) | Right elbow |
| hand_right | (-6, 8, 0) | Right wrist |
| arm_left | (6, 22, 0) | Left shoulder |
| forearm_left | (6, 14, 0) | Left elbow |
| hand_left | (6, 8, 0) | Left wrist |
| leg_right | (-2, 12, 0) | Right hip |
| shin_right | (-2, 6, 0) | Right knee |
| foot_right | (-2, 0, 0) | Right ankle |
| leg_left | (2, 12, 0) | Left hip |
| shin_left | (2, 6, 0) | Left knee |
| foot_left | (2, 0, 0) | Left ankle |

**Note**: 16 Blockbench units = 1 Minecraft block = 1 meter

#### Step 4: Add Geometry

Now add cubes to your bones to create the visual model:

1. Select a bone in the outliner
2. Click **"Add Cube"** button (or press Ctrl+B)
3. Resize and position the cube using the 3D viewport
4. Use the UV editor to map textures

**Tips**:
- Start with simple shapes (one cube per body part)
- Refine with more cubes for detail
- Keep the poly count reasonable (<1000 triangles per variant)
- Use symmetry for left/right parts

#### Step 5: Create Variants (Optional)

For body part variations (different heads, body types, etc.):

1. Create a **folder/group** in the outliner
2. Name it `head_variant_0`
3. Add cubes inside this folder
4. Duplicate folder for more variants: `head_variant_1`, `head_variant_2`, etc.
5. Toggle visibility to work on one variant at a time

### Creating Equipment/Armor Models

Armor uses the **same skeleton** as character models but adds geometry only where armor pieces go.

#### Step 1: Start with Character Template

1. Open an existing character model (or use template)
2. **File → Save As**: `leather_helmet.bbmodel`
3. Delete all existing body geometry (keep only skeleton)

#### Step 2: Add Armor Geometry

1. Select the bone where armor attaches (e.g., `head` for helmet)
2. Add cubes for the armor piece
3. Make armor slightly **larger** than the body (add 0.5-1 unit padding) to prevent Z-fighting
4. Use Blockbench's "Inflate" tool to expand geometry

#### Step 3: Name Appropriately

Rename your geometry groups:
- `helmet_leather_hood` for leather helmet
- `chestplate_leather` for leather chestplate
- etc.

### Race-Specific Proportions

When creating models for different races, adjust the bone positions and geometry:

| Race | Height | Key Differences |
|------|--------|----------------|
| **Human** | 28.8 units (1.8 blocks) | Base reference |
| **Dwarf** | 20.8 units (1.3 blocks) | Stockier, shorter legs, wider torso, larger head |
| **Elf** | 32 units (2.0 blocks) | Taller, longer legs, slender, pointed ears |
| **Halfling** | 16 units (1.0 block) | Smallest, childlike proportions, large head |

**See BLOCKBENCH_SPECIFICATIONS.md for exact measurements.**

---

## Creating Textures

### Texture Basics

- **Size**: 64x64 pixels (standard) or 128x128 (detailed)
- **Format**: PNG with transparency (RGBA)
- **UV Mapping**: Done in Blockbench's UV editor

### Creating a Skin Texture

#### Method 1: Auto-Generate Template

1. In Blockbench, select all cubes
2. Right-click → **"Auto UV"**
3. Go to **Tools → Create Texture**
4. Choose **"Blank"** or **"Template"**
5. Click **"Create"**
6. Export texture: Right-click texture in texture panel → **"Save As"**

#### Method 2: Paint in External Editor

1. Export the UV template from Blockbench
2. Open in your texture editor (GIMP, Photoshop, etc.)
3. Paint each face:
   - Head: Front, back, sides, top, bottom
   - Body: Front, back, sides
   - Arms/Legs: Outer, inner sides
4. Save as PNG
5. Import back into Blockbench: Texture panel → **"Import Texture"**

### Texture Guidelines

**Character Skins**:
- Use natural skin tones
- Add details: shadows, highlights, wrinkles
- Keep style consistent across all races
- Create variants for different skin tones

**Armor Textures**:
- Add material details: metal shine, leather grain, cloth weave
- Use alpha channel for transparency (chainmail gaps, cloth edges)
- Consider color variants (brown leather, black leather, etc.)

**Naming Convention**:
```
{race}_skin_{variant}.png       Example: dwarf_skin_0.png
{armor}_{material}.png           Example: leather_brown.png
{armor}_dyed_{hexcolor}.png      Example: leather_dyed_FF0000.png
```

### Creating Texture Variants

For different skin tones, hair colors, etc.:

1. Create base texture: `human_skin_0.png`
2. Duplicate and modify: `human_skin_1.png` (darker tone)
3. Duplicate and modify: `human_skin_2.png` (lighter tone)
4. Create overlays for hair, tattoos: `hair_overlay_brown.png`

---

## Creating Animations

### Animation Basics

Animations are keyframe-based:
- **Keyframe**: A snapshot of bone rotations/positions at a specific time
- **Interpolation**: Smooth transitions between keyframes
- **Loop**: Animation repeats seamlessly

### Required Animations

#### Locomotion (locomotion.animation.json)

1. **idle**
   - Length: 2 seconds (loop)
   - Subtle breathing motion
   - Slight head bob

2. **walk**
   - Length: 1 second (loop)
   - Walking cycle (2 steps)
   - Arms swing opposite to legs

3. **run**
   - Length: 0.6 seconds (loop)
   - Faster, more exaggerated motions

#### Combat (combat.animation.json)

4. **attack_melee**
   - Length: 0.5 seconds
   - Sword swing from right to left
   - Torso rotation

5. **block**
   - Length: Hold pose
   - Shield raised, body braced

#### Magic (magic.animation.json)

6. **cast_spell**
   - Length: 0.8 seconds
   - Arms extended, magical gesture

### Creating an Animation

#### Step 1: Set Up Animation

1. Click **"Animation"** tab in Blockbench
2. Click **"Add Animation"**
3. Name it: `idle` (just the name, not full path)
4. Set properties:
   - **Loop**: On (for idle, walk, run)
   - **Length**: 2.0 seconds (for idle)

#### Step 2: Add Keyframes

1. Move the timeline scrubber to time 0.0s
2. Select a bone (e.g., `body`)
3. Rotate or move the bone
4. Click **"Add Keyframe"** button (or press K)
5. Choose what to keyframe: **Rotation**, **Position**, or **Scale**

**Best Practice**: Animate **rotations only** for most bones. Let the retargeting system handle positions.

#### Step 3: Add More Keyframes

1. Move timeline to 1.0s
2. Rotate the bone in opposite direction
3. Add keyframe
4. Move to 2.0s
5. Return bone to starting position (same as 0.0s for seamless loop)
6. Add keyframe

#### Step 4: Preview

1. Click the **"Play"** button in animation timeline
2. Adjust keyframes as needed
3. Use easing curves for smooth motion (right-click keyframe → "Easing")

### Animation Tips

**Rotation Limits** (for natural motion):
- Head Yaw: ±80 degrees
- Head Pitch: ±45 degrees
- Arm Swing: ±60 degrees
- Leg Swing: ±45 degrees
- Torso Rotation: ±20 degrees

**Keyframe Spacing**:
- Idle: Every 0.5-1 second (subtle)
- Walk/Run: Every 0.1-0.2 seconds (detailed)
- Combat: Every 0.05-0.1 seconds (snappy)

**For detailed animation specs, see:**
- BLOCKBENCH_SPECIFICATIONS.md (animation keyframe guidelines)
- COMBAT_ANIMATIONS_SPEC.md (combat animation details)
- MAGIC_ANIMATIONS_SPEC.md (magic animation details)

---

## Exporting from Blockbench

### Exporting Geometry (.geo.json)

1. **File → Export → Export GeckoLib Model**
2. In the dialog:
   - ☑ Export Geometry
   - ☐ Export Animations (uncheck if exporting separately)
   - ☐ Minify JSON (uncheck for easier debugging)
3. **Save to**: `src/main/resources/assets/fiveesrd/geo/entity/character/{race}/`
4. **Filename**: `{race}_body.geo.json` (e.g., `human_body.geo.json`)

### Exporting Animations (.animation.json)

1. **File → Export → Export GeckoLib Animations**
2. Select which animations to export:
   - Export all locomotion animations together
   - Export all combat animations together
   - Export all magic animations together
3. **Save to**: `src/main/resources/assets/fiveesrd/animations/entity/character/`
4. **Filename**:
   - `locomotion.animation.json` (idle, walk, run)
   - `combat.animation.json` (attacks, blocks)
   - `magic.animation.json` (spell casting)

### Exporting Textures

Textures are saved separately:

1. In texture panel, right-click texture
2. **"Save As"**
3. **Save to**: `src/main/resources/assets/fiveesrd/textures/entity/character/base/`
4. **Filename**: `{race}_default.png` or `{race}_skin_0.png`

### Pre-Export Checklist

Before exporting, verify:

- ✅ All bone names match the standard skeleton exactly
- ✅ Bone hierarchy is correct
- ✅ Pivot points are set correctly
- ✅ All cubes are parented to correct bones
- ✅ Textures are assigned and UV mapped
- ✅ Animations play smoothly in preview
- ✅ Model is centered at origin (0, 0, 0)

---

## File Organization

### Directory Structure

Here's where everything goes:

```
mc-5e-srd/
├── Blockbench source files (at project root)
│   ├── human_basic.bbmodel
│   ├── leather_helmet.bbmodel
│   ├── leather_chestplate.bbmodel
│   └── ...
│
└── src/main/resources/assets/fiveesrd/
    ├── geo/                                    # Exported geometry
    │   └── entity/
    │       ├── character/
    │       │   ├── human/
    │       │   │   ├── human_body.geo.json
    │       │   │   ├── human_heads.geo.json    (future: variants)
    │       │   │   └── human_limbs.geo.json    (future: variants)
    │       │   ├── dwarf/
    │       │   │   └── dwarf_body.geo.json
    │       │   ├── elf/
    │       │   │   └── elf_body.geo.json
    │       │   └── halfling/
    │       │       └── halfling_body.geo.json
    │       │
    │       └── equipment/
    │           └── armor/
    │               ├── leather_helmet.geo.json
    │               ├── leather_chestplate.geo.json
    │               ├── leather_leggings.geo.json
    │               └── leather_boots.geo.json
    │
    ├── animations/                             # Exported animations
    │   └── entity/
    │       └── character/
    │           ├── locomotion.animation.json   (idle, walk, run)
    │           ├── combat.animation.json       (attacks, blocks)
    │           └── magic.animation.json        (spell casting)
    │
    └── textures/                               # Texture images
        └── entity/
            └── character/
                ├── base/                       # Body/skin textures
                │   ├── human_default.png
                │   ├── dwarf_default.png
                │   ├── elf_default.png
                │   └── halfling_default.png
                │
                └── equipment/                  # Armor textures
                    └── armor/
                        ├── leather_brown.png
                        ├── chainmail_steel.png
                        └── plate_iron.png
```

### Naming Conventions

**Blockbench Files (.bbmodel)**:
```
{race}_{part}.bbmodel           Example: human_body.bbmodel
{armor}_{piece}.bbmodel          Example: leather_helmet.bbmodel
```

**Geometry Files (.geo.json)**:
```
{race}_{part}.geo.json          Example: human_body.geo.json
{armor}_{piece}.geo.json         Example: leather_helmet.geo.json
```

**Animation Files (.animation.json)**:
```
{category}.animation.json        Example: locomotion.animation.json
```

**Texture Files (.png)**:
```
{race}_{variant}.png            Example: human_skin_0.png
{armor}_{material}.png           Example: leather_brown.png
```

---

## Testing Your Work

### Building and Running

1. **Build the mod**:
   ```bash
   ./gradlew build
   ```

2. **Run the client**:
   ```bash
   ./gradlew runClient
   ```

3. **Wait for Minecraft to launch**

### Spawning Test Character

In-game:

1. Press **T** to open chat
2. Type: `/summon fiveesrd:character ~ ~ ~`
3. Press **Enter**

You should see your character spawn at your location.

### Verification Checklist

✅ **Model appears**: Not invisible, not error texture
✅ **Textures look correct**: No missing/stretched textures
✅ **Bones in right positions**: No floating/disconnected limbs
✅ **Animations play**: Character animates when moving
✅ **No console errors**: Check logs for missing files

### Common Issues and Fixes

**Issue: Model doesn't appear (invisible)**
- Check console for errors
- Verify file paths match exactly
- Ensure geometry file exists at expected location
- Check model identifier matches: `geometry.character.{race}`

**Issue: Texture is black/white checkerboard**
- Texture file doesn't exist or wrong path
- Check texture UV mapping in Blockbench
- Verify texture size (64x64 or 128x128)

**Issue: Animations look wrong/jerky**
- Check bone pivot points in Blockbench
- Verify parent-child bone relationships
- Make sure animations use rotations, not positions

**Issue: Limbs disconnect during animation**
- Incorrect pivot points
- Check that pivots are at joint locations (shoulder, elbow, wrist)

**Issue: Model exports but won't load**
- JSON syntax error in exported file
- Bone names don't match expected names
- Model identifier mismatch

### Debugging Tips

1. **Check Console Output**: Look for errors mentioning your model/texture paths
2. **Test Incrementally**: Export and test after each major change
3. **Start Simple**: Create simple placeholder geometry first, then refine
4. **Compare to Working Models**: Look at the existing placeholder models

---

## Tips & Troubleshooting

### Modeling Tips

**Keep It Simple**:
- Start with basic shapes (box models)
- Add detail gradually
- Test frequently

**Optimize Performance**:
- Keep poly count under 1000 triangles per model
- Reuse textures where possible
- Use texture atlasing for variants

**Maintain Consistency**:
- Keep similar style across all models
- Use same texture resolution (64x64)
- Follow same proportions for body parts

### Animation Tips

**Natural Motion**:
- Study real walking/running references
- Use easing curves for smooth motion
- Animate weight shifts (hips, torso)

**Combat Feel**:
- Add anticipation before strikes
- Include follow-through after impacts
- Make blocking look defensive (body braced)

**Loop Seamlessly**:
- First and last keyframes should match exactly
- Use "Loop" mode in Blockbench to verify
- Maintain consistent timing

### Texture Tips

**Resolution**:
- 64x64 for most models (standard)
- 128x128 for highly detailed models
- Keep consistent across similar items

**Style**:
- Match Minecraft's blocky aesthetic
- Use pixel art techniques
- Avoid overly realistic details

**Performance**:
- Use texture atlases for variants
- Reuse textures where possible
- Compress PNGs (use tools like TinyPNG)

### Workflow Tips

**Version Control**:
- Commit both .bbmodel AND .geo.json files
- Write clear commit messages: "Add dwarf head variant 2"
- Tag releases when models are stable

**Backup Frequently**:
- Blockbench can crash
- Save often (Ctrl+S)
- Use auto-save feature: File → Preferences → Save

**Organize Assets**:
- Keep .bbmodel files in project root (easy access)
- Keep exported files in proper directories
- Name files consistently

---

## Quick Reference

### Essential Blockbench Shortcuts

| Shortcut | Action |
|----------|--------|
| **Ctrl+S** | Save project |
| **Ctrl+Z** | Undo |
| **Ctrl+Y** | Redo |
| **B** | Add bone |
| **Ctrl+B** | Add cube |
| **K** | Add keyframe |
| **Space** | Play/pause animation |
| **G** | Move/grab |
| **R** | Rotate |
| **S** | Scale |

### File Paths Quick Copy

**Geometry export path**:
```
src/main/resources/assets/fiveesrd/geo/entity/character/{race}/
```

**Animation export path**:
```
src/main/resources/assets/fiveesrd/animations/entity/character/
```

**Texture path**:
```
src/main/resources/assets/fiveesrd/textures/entity/character/base/
```

---

## Additional Resources

### Project Documentation

- **BLOCKBENCH_SPECIFICATIONS.md** - Detailed bone structure and proportions
- **BLOCKBENCH_EXPORT_GUIDE.md** - Complete export workflow
- **LAYER_SYSTEM.md** - How layers and equipment rendering works
- **ANIMATION_RETARGETING.md** - How animations adapt to different races
- **FILE_STRUCTURE.md** - Complete file organization reference

### External Resources

**Blockbench**:
- Official Site: https://www.blockbench.net/
- Wiki: https://www.blockbench.net/wiki/
- Discord: https://discord.gg/fZQbxbg

**GeckoLib**:
- Documentation: https://docs.geckolib.com/
- Discord: https://discord.gg/MNQcKxB
- GitHub: https://github.com/bernie-g/geckolib

**Minecraft Modding**:
- Fabric Wiki: https://fabricmc.net/wiki/
- Fabric Discord: https://discord.gg/v6v4pMv

---

## Getting Help

If you're stuck:

1. **Check the console** for error messages
2. **Compare to existing models** (human_basic.bbmodel)
3. **Read error messages carefully** (they usually tell you what's wrong)
4. **Ask in Discord** (GeckoLib or Fabric communities)
5. **Check the documentation** (this guide and others in /docs)

---

**Last Updated**: 2025-11-14
**Version**: 1.0

Happy modeling! 🎨
