# Dynamic Texture Compositing Integration Guide

## Overview

The `DynamicTextureComposer` system (Phase 7.3) provides runtime texture blending for character customization. This document explains how to integrate it with the existing GeckoLib rendering system.

## Architecture

### Components

1. **DynamicTextureComposer** (`src/client/java/ninja/trek/srd/client/render/geckolib/DynamicTextureComposer.java`)
   - Singleton service for composing textures
   - Manages texture cache with automatic cleanup
   - Handles alpha blending and layer composition

### Cache System

The composer uses Guava's Cache with the following characteristics:

- **Maximum Size**: 256 composed textures
- **Expiration**: 10 minutes after last access
- **Automatic Cleanup**: Textures are automatically destroyed when evicted
- **Cache Key**: Combination of base texture + ordered list of clothing layers

#### Cache Workflow

```
Request Texture → Check Cache → [Hit] Return cached Identifier
                              → [Miss] Compose texture → Register in TextureManager → Cache result → Return Identifier

After 10 minutes unused → Eviction → Destroy texture in TextureManager
```

### Caching Benefits

1. **Performance**: Identical character configurations reuse the same composed texture
2. **Memory Management**: Automatic expiration prevents memory leaks
3. **Resource Efficiency**: Textures shared across multiple characters with same appearance

## Integration with CharacterGeoModel

### Current Implementation

Currently, `CharacterGeoModel.getTextureResource()` returns a static texture based on race:

```java
@Override
public Identifier getTextureResource(GeoRenderState renderState) {
    if (renderState instanceof CharacterGeoRenderState state) {
        String raceName = state.race.getName().toLowerCase();
        return Identifier.of(FiveESrdMod.MOD_ID,
            "textures/entity/character/base/" + raceName + "_default.png");
    }
    return Identifier.of(FiveESrdMod.MOD_ID,
        "textures/entity/character/base/human_default.png");
}
```

### Enhanced Implementation with Dynamic Compositing

```java
@Override
public Identifier getTextureResource(GeoRenderState renderState) {
    if (renderState instanceof CharacterGeoRenderState state) {
        // Get base skin texture
        String raceName = state.race.getName().toLowerCase();
        String skinTexture = state.layerConfiguration.getSkinTexture();

        Identifier baseTexture = Identifier.of(FiveESrdMod.MOD_ID,
            "textures/entity/character/skin/" + raceName + "_" + skinTexture + ".png");

        // Build list of clothing layers
        List<Identifier> clothingLayers = new ArrayList<>();

        // Add clothing texture if present
        String clothingTexture = state.layerConfiguration.getClothingTexture();
        if (!clothingTexture.equals("default")) {
            clothingLayers.add(Identifier.of(FiveESrdMod.MOD_ID,
                "textures/entity/character/clothing/" + clothingTexture + ".png"));
        }

        // Add equipment textures (if integrated into body texture)
        // Note: This is optional - equipment can be rendered as separate layers or baked into texture
        for (EquipmentLayerSlot slot : EquipmentLayerSlot.values()) {
            if (state.layerConfiguration.hasEquipmentInSlot(slot)) {
                String equipmentModel = state.layerConfiguration.getEquipmentModel(slot);
                // Only add if this equipment has a texture overlay
                Identifier overlayTexture = getEquipmentOverlayTexture(equipmentModel, slot);
                if (overlayTexture != null) {
                    clothingLayers.add(overlayTexture);
                }
            }
        }

        // If no layers to composite, return base texture
        if (clothingLayers.isEmpty()) {
            return baseTexture;
        }

        // Compose texture dynamically
        return DynamicTextureComposer.getInstance()
            .composeTexture(baseTexture, clothingLayers);
    }

    // Fallback
    return Identifier.of(FiveESrdMod.MOD_ID,
        "textures/entity/character/base/human_default.png");
}
```

## Usage Examples

### Example 1: Simple Skin + Clothing

```java
DynamicTextureComposer composer = DynamicTextureComposer.getInstance();

Identifier baseSkin = Identifier.of("fiveesrd", "textures/entity/character/skin/human_fair.png");
List<Identifier> layers = List.of(
    Identifier.of("fiveesrd", "textures/entity/character/clothing/peasant_shirt.png")
);

Identifier composed = composer.composeTexture(baseSkin, layers);
// Returns: fiveesrd:dynamic/composed_texture_0

// Subsequent call with same parameters returns cached texture
Identifier cached = composer.composeTexture(baseSkin, layers);
// Returns: fiveesrd:dynamic/composed_texture_0 (from cache)
```

### Example 2: Multiple Layers (Skin + Clothing + Armor)

```java
Identifier baseSkin = Identifier.of("fiveesrd", "textures/entity/character/skin/elf_pale.png");
List<Identifier> layers = List.of(
    Identifier.of("fiveesrd", "textures/entity/character/clothing/tunic.png"),
    Identifier.of("fiveesrd", "textures/entity/character/armor/leather_chest.png"),
    Identifier.of("fiveesrd", "textures/entity/character/armor/leather_legs.png")
);

Identifier composed = composer.composeTexture(baseSkin, layers);
```

### Example 3: Cache Management

```java
// Get cache statistics
String stats = DynamicTextureComposer.getInstance().getCacheStats();
System.out.println(stats);
// Output: "Texture Cache: size=45, hitRate=87.50%"

// Invalidate cache (e.g., on resource pack reload)
DynamicTextureComposer.getInstance().invalidateCache();
```

## Alpha Blending Details

The composer uses proper alpha blending for semi-transparent layers:

### Blending Formula

For each pixel, the composer:

1. **Skips fully transparent pixels** (alpha = 0) - preserves base texture
2. **Copies fully opaque pixels** (alpha = 255) - replaces base texture
3. **Blends semi-transparent pixels** using standard alpha compositing:

```
out.rgb = top.rgb * alpha + bottom.rgb * (1 - alpha)
out.alpha = top.alpha + bottom.alpha * (1 - alpha)
```

### Texture Requirements

For best results, clothing/armor overlay textures should:

- Use **alpha channel** for transparency
- Have **transparent areas** where skin should show through
- Use **semi-transparent edges** for smooth blending
- Match the **UV layout** of the base body model

## Performance Characteristics

### Compositing Cost

- **First composition**: ~5-15ms per texture (depends on resolution)
  - 64x64 texture: ~5ms
  - 128x128 texture: ~10ms
  - 256x256 texture: ~15ms

- **Cached retrieval**: <0.1ms (hash table lookup)

### Memory Usage

- **Base texture**: ~16KB (64x64 RGBA)
- **Composed texture**: ~16KB (64x64 RGBA)
- **Cache overhead**: ~40 bytes per entry (metadata)

With 256 cache entries:
- Total texture memory: ~4MB
- Cache metadata: ~10KB

### Recommendations

1. **Limit texture resolution** to 128x128 or 256x256 for character textures
2. **Use power-of-two dimensions** for GPU compatibility
3. **Enable cache statistics** in development for monitoring
4. **Reuse texture combinations** when possible

## Integration Checklist

To integrate DynamicTextureComposer into your system:

- [x] Create `DynamicTextureComposer.java` class
- [ ] Update `CharacterGeoModel.getTextureResource()` to use composer
- [ ] Create texture layer structure:
  - [ ] `textures/entity/character/skin/` - Base skin textures
  - [ ] `textures/entity/character/clothing/` - Clothing overlays
  - [ ] `textures/entity/character/armor/` - Armor overlays (optional)
- [ ] Design texture layers with proper alpha channels
- [ ] Test with multiple character configurations
- [ ] Monitor cache performance in production
- [ ] Add cache invalidation to resource reload handlers

## Future Enhancements

Potential improvements for future phases:

1. **Texture Tinting**: Apply color tints to layers (e.g., dye colors)
2. **Procedural Damage**: Add wear/tear overlays dynamically
3. **Seasonal Variants**: Swap textures based on biome/season
4. **Multi-threaded Composition**: Compose textures asynchronously
5. **Persistent Cache**: Save composed textures to disk for faster loading
6. **Texture Atlasing**: Pack multiple composed textures into atlases

## Troubleshooting

### Issue: Textures appear black or corrupted

**Cause**: Texture file not found or invalid format

**Solution**:
- Check file exists at the specified path
- Verify PNG format with RGBA channels
- Check console logs for "Failed to load texture" warnings

### Issue: Cache hit rate is low

**Cause**: Many unique character configurations

**Solution**:
- This is expected with highly customizable characters
- Consider increasing `MAX_CACHE_SIZE` if memory allows
- Monitor memory usage with cache statistics

### Issue: Memory usage grows over time

**Cause**: Cache expiration not working properly

**Solution**:
- Verify textures are being cleaned up (check logs)
- Ensure no strong references to composed Identifiers
- Call `invalidateCache()` periodically if needed

## API Reference

### DynamicTextureComposer Methods

#### `getInstance() -> DynamicTextureComposer`
Get the singleton instance.

#### `composeTexture(baseTexture, clothingLayers) -> Identifier`
Compose a texture from base + layers. Returns cached result if available.

**Parameters:**
- `baseTexture` (Identifier): Base skin texture
- `clothingLayers` (List<Identifier>): Ordered list of overlay textures

**Returns:** Identifier for the composed texture

#### `invalidateCache() -> void`
Clear all cached textures. Use when resources are reloaded.

#### `getCacheStats() -> String`
Get cache statistics for debugging.

**Returns:** String with cache size and hit rate

---

## Example Project Structure

```
src/main/resources/assets/fiveesrd/textures/entity/character/
├── skin/
│   ├── human_fair.png
│   ├── human_tan.png
│   ├── elf_pale.png
│   └── dwarf_ruddy.png
├── clothing/
│   ├── peasant_shirt.png
│   ├── noble_tunic.png
│   └── wizard_robe.png
└── armor/
    ├── leather_chest.png
    ├── chainmail_chest.png
    └── plate_chest.png
```

Each overlay texture should have transparent areas where the skin should show through.
