# Character Textures

This directory contains texture files for character rendering.

## Directory Structure

- **base/**: Skin textures for each race
- **clothing/**: Clothing and outfit textures
- **armor/**: Armor piece textures

## File Naming Convention

### Base Skin Textures
Format: `{race}_{variant}.png`

Examples:
- `human_default.png` - Default human skin
- `human_pale.png` - Pale skin variant
- `dwarf_default.png` - Default dwarf skin
- `elf_default.png` - Default elf skin
- `halfling_default.png` - Default halfling skin

### Clothing Textures
Format: `{clothing_type}_{color}.png`

Examples:
- `tunic_red.png`
- `robe_blue.png`
- `peasant_brown.png`

### Armor Textures
Format: `{armor_type}_{material}.png`

Examples:
- `leather_brown.png`
- `chainmail_steel.png`
- `plate_gold.png`

## Texture Specifications

### Size
- Recommended: 64x64 pixels
- Maximum: 128x128 pixels
- Format: PNG with transparency

### UV Mapping
- Must match the geometry's UV coordinates from BlockBench
- Use BlockBench's texture painting tool for accuracy
- Export texture from BlockBench with model

### Color Guidelines
- Use realistic or stylized colors based on mod aesthetic
- Ensure good contrast for visibility
- Include proper shading and highlights
- Use alpha channel for transparency where needed

## Creating Textures

### Method 1: BlockBench Texture Painting
1. Open your model in BlockBench
2. Go to "Paint" tab
3. Paint directly on the model
4. File → Export → Export Texture
5. Save to appropriate directory

### Method 2: External Image Editor
1. Export UV template from BlockBench (File → Export → UV Mapping)
2. Open in GIMP, Photoshop, or Aseprite
3. Paint on the UV map
4. Save as PNG
5. Place in appropriate directory

## Texture Layers (Phase 7)

In advanced phases, textures can be composited at runtime:
- Base skin layer
- Clothing layer (with transparency)
- Armor layer (with transparency)
- Effect layers (glows, particles)

## Default Texture

If a texture file is not found, GeckoLib will use a missing texture (purple/black checkerboard).

To provide a fallback:
- Ensure at least one `{race}_default.png` exists for each race
- Use simple, solid colors for testing before creating detailed textures

## Minimum Viable Product (MVP)

For initial testing, create simple solid-color textures:
- `human_default.png` - Tan/beige solid color with basic face
- `dwarf_default.png` - Light brown solid color
- `elf_default.png` - Pale green or fair skin tone
- `halfling_default.png` - Warm tan color

## Resources

- BlockBench Texture Painting: https://www.blockbench.net/wiki/guides/texturing
- Minecraft Texture Guidelines: https://minecraft.wiki/w/Tutorials/Creating_a_resource_pack
- Color Palette Tools: https://coolors.co/

For detailed specifications, see:
- `/docs/BLOCKBENCH_SPECIFICATIONS.md`
- `/docs/LAYER_SYSTEM.md`
