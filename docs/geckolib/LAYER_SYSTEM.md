# Layer System and Runtime Assembly

## Overview

This document describes the modular layer system that allows runtime assembly of characters from multiple geometric and texture layers, enabling dynamic customization of appearance and equipment.

## Layer Architecture

### Layer Types

```
┌─────────────────────────────────────────────────┐
│            Character Visual Layers               │
├─────────────────────────────────────────────────┤
│  Layer 0: Base Body (always visible)            │
│  Layer 1: Skin/Texture Overlay                  │
│  Layer 2: Undergarments                         │
│  Layer 3: Clothing/Outfit                       │
│  Layer 4: Armor (Chest)                         │
│  Layer 5: Armor (Legs)                          │
│  Layer 6: Armor (Arms)                          │
│  Layer 7: Armor (Feet)                          │
│  Layer 8: Armor (Head)                          │
│  Layer 9: Accessories (Cape, Belt)              │
│  Layer 10: Held Items (Weapons, Shields)        │
│  Layer 11: Effects (Particles, Auras)           │
└─────────────────────────────────────────────────┘
```

### Layer Properties

Each layer has:
1. **Geometry**: 3D mesh (can be null for texture-only layers)
2. **Texture**: Material/skin
3. **Visibility**: Can be hidden by other layers
4. **Parent Bone**: Which bone to attach to
5. **Render Priority**: Draw order
6. **Transparency**: Alpha blending mode

## Base Body System

### Body Part Variants

Each race has modular body parts with multiple variants:

```java
public class BodyPartConfiguration {
    // Base geometry selections (per race)
    public int headVariant = 0;      // 0-6+ options
    public int bodyVariant = 0;      // 0-3+ options
    public int armsVariant = 0;      // 0-3+ options
    public int legsVariant = 0;      // 0-3+ options

    // Overlay options (can stack with base)
    public int hairStyle = 0;        // 0-10+ options, null = bald
    public int facialHair = -1;      // -1 = none, 0-5+ options
    public int scars = -1;           // -1 = none, 0-3+ options
    public int tattoos = -1;         // -1 = none, 0-5+ options

    // Material/texture selections
    public String skinTexture = "default";
    public String hairColor = "brown";
    public String eyeColor = "brown";
}
```

### Base Body Rendering

```java
public class BaseBodyRenderer {

    public void renderBaseBody(
        CharacterEntity entity,
        MatrixStack matrices,
        VertexConsumerProvider vertices,
        int light,
        int overlay
    ) {
        BodyPartConfiguration config = entity.getBodyConfiguration();
        SkeletonProfile profile = entity.getRace().getSkeletonProfile();

        // Load geometry for each body part
        GeoModel headModel = loadBodyPartModel("head", config.headVariant, entity.getRace());
        GeoModel bodyModel = loadBodyPartModel("body", config.bodyVariant, entity.getRace());
        GeoModel armsModel = loadBodyPartModel("arms", config.armsVariant, entity.getRace());
        GeoModel legsModel = loadBodyPartModel("legs", config.legsVariant, entity.getRace());

        // Render each part with appropriate texture
        renderBodyPart(headModel, config.skinTexture, matrices, vertices, light, overlay);
        renderBodyPart(bodyModel, config.skinTexture, matrices, vertices, light, overlay);
        renderBodyPart(armsModel, config.skinTexture, matrices, vertices, light, overlay);
        renderBodyPart(legsModel, config.skinTexture, matrices, vertices, light, overlay);

        // Render overlays (hair, facial hair, etc.)
        if (config.hairStyle >= 0) {
            renderOverlay("hair", config.hairStyle, config.hairColor, matrices, vertices, light, overlay);
        }

        if (config.facialHair >= 0) {
            renderOverlay("facial_hair", config.facialHair, config.hairColor, matrices, vertices, light, overlay);
        }
    }

    private void renderBodyPart(
        GeoModel model,
        String textureName,
        MatrixStack matrices,
        VertexConsumerProvider vertices,
        int light,
        int overlay
    ) {
        // Get texture resource
        Identifier texture = getTextureIdentifier(textureName);

        // Get vertex consumer with texture
        VertexConsumer consumer = vertices.getBuffer(RenderLayer.getEntityCutout(texture));

        // Render model geometry
        model.renderToBuffer(matrices, consumer, light, overlay, 1.0f, 1.0f, 1.0f, 1.0f);
    }
}
```

## Equipment Layer System

### Equipment Slots

```java
public enum EquipmentSlot {
    // Armor slots
    HEAD(LayerPriority.ARMOR_HEAD, "head"),
    CHEST(LayerPriority.ARMOR_CHEST, "torso_upper"),
    LEGS(LayerPriority.ARMOR_LEGS, "torso_lower"),
    FEET(LayerPriority.ARMOR_FEET, "foot_right"),  // Mirrors to left

    // Accessory slots
    CAPE(LayerPriority.ACCESSORY, "cape_attach"),
    BELT(LayerPriority.ACCESSORY, "belt_attach"),
    NECKLACE(LayerPriority.ACCESSORY, "head"),
    RING_LEFT(LayerPriority.ACCESSORY, "hand_left"),
    RING_RIGHT(LayerPriority.ACCESSORY, "hand_right"),

    // Held items
    MAINHAND(LayerPriority.HELD_ITEM, "item_mainhand"),
    OFFHAND(LayerPriority.HELD_ITEM, "item_offhand");

    private final int priority;
    private final String parentBone;
}
```

### Equipment Layer Configuration

```java
public class EquipmentLayerConfiguration {
    private final Map<EquipmentSlot, EquipmentLayer> layers;

    public static class EquipmentLayer {
        public String modelId;           // e.g., "armor_leather_helmet"
        public String textureId;         // e.g., "leather_brown"
        public boolean visible;          // Can be toggled on/off
        public float inflationOffset;    // How far from body (for layering)
        public boolean hidesHair;        // Helmets hide hair
        public boolean hidesEars;        // Helmets hide elf ears
        public boolean hidesFacialHair;  // Some helmets hide beards
    }

    public void setEquipment(EquipmentSlot slot, String modelId, String textureId) {
        EquipmentLayer layer = new EquipmentLayer();
        layer.modelId = modelId;
        layer.textureId = textureId;
        layer.visible = true;
        layer.inflationOffset = getDefaultInflation(slot);
        layer.hidesHair = (slot == EquipmentSlot.HEAD);
        layer.hidesEars = (slot == EquipmentSlot.HEAD);
        layer.hidesFacialHair = false;  // Most helmets show beards

        layers.put(slot, layer);
    }

    public void removeEquipment(EquipmentSlot slot) {
        layers.remove(slot);
    }

    public boolean hasEquipment(EquipmentSlot slot) {
        return layers.containsKey(slot) && layers.get(slot).visible;
    }

    private float getDefaultInflation(EquipmentSlot slot) {
        return switch (slot) {
            case HEAD -> 0.5f;    // Helmets slightly larger than head
            case CHEST -> 0.75f;  // Chest armor needs room
            case LEGS -> 0.5f;    // Leg armor
            case FEET -> 0.25f;   // Boots tight fit
            case CAPE -> 1.0f;    // Cape hangs behind
            default -> 0.0f;
        };
    }
}
```

### Equipment Rendering

```java
public class EquipmentLayerRenderer {

    public void renderEquipment(
        CharacterEntity entity,
        MatrixStack matrices,
        VertexConsumerProvider vertices,
        int light,
        int overlay
    ) {
        EquipmentLayerConfiguration config = entity.getEquipmentConfiguration();

        // Render in priority order (back to front)
        List<EquipmentSlot> renderOrder = EquipmentSlot.getRenderOrder();

        for (EquipmentSlot slot : renderOrder) {
            if (config.hasEquipment(slot)) {
                renderEquipmentLayer(
                    entity,
                    config.getLayer(slot),
                    slot,
                    matrices,
                    vertices,
                    light,
                    overlay
                );
            }
        }
    }

    private void renderEquipmentLayer(
        CharacterEntity entity,
        EquipmentLayer layer,
        EquipmentSlot slot,
        MatrixStack matrices,
        VertexConsumerProvider vertices,
        int light,
        int overlay
    ) {
        if (!layer.visible) return;

        // Load equipment model
        GeoModel equipmentModel = loadEquipmentModel(layer.modelId);

        // Load texture
        Identifier texture = getEquipmentTexture(layer.textureId);

        // Apply inflation offset (make armor slightly larger than body)
        matrices.push();
        matrices.scale(
            1.0f + layer.inflationOffset * 0.01f,
            1.0f + layer.inflationOffset * 0.01f,
            1.0f + layer.inflationOffset * 0.01f
        );

        // Render equipment geometry
        VertexConsumer consumer = vertices.getBuffer(RenderLayer.getEntityCutoutNoCull(texture));
        equipmentModel.renderToBuffer(matrices, consumer, light, overlay, 1.0f, 1.0f, 1.0f, 1.0f);

        matrices.pop();
    }
}
```

## Armor System

### Armor Types and Coverage

```java
public enum ArmorType {
    LEATHER("leather", ArmorMaterial.LEATHER, CoverageLevel.LIGHT),
    CHAINMAIL("chainmail", ArmorMaterial.CHAIN, CoverageLevel.MEDIUM),
    IRON("iron", ArmorMaterial.IRON, CoverageLevel.MEDIUM),
    GOLD("gold", ArmorMaterial.GOLD, CoverageLevel.MEDIUM),
    DIAMOND("diamond", ArmorMaterial.DIAMOND, CoverageLevel.HEAVY),
    NETHERITE("netherite", ArmorMaterial.NETHERITE, CoverageLevel.HEAVY),
    ROBES("robes", null, CoverageLevel.CLOTH);

    public enum CoverageLevel {
        CLOTH,    // Minimal coverage, mostly texture
        LIGHT,    // Partial geometric coverage
        MEDIUM,   // Moderate geometric coverage
        HEAVY     // Full geometric coverage
    }
}
```

### Armor Piece Models

Each armor type should have separate geometry for each slot:

```
models/entity/equipment/armor/
├── leather/
│   ├── leather_helmet.geo.json
│   ├── leather_chestplate.geo.json
│   ├── leather_leggings.geo.json
│   └── leather_boots.geo.json
├── chainmail/
│   ├── chainmail_coif.geo.json
│   ├── chainmail_hauberk.geo.json
│   ├── chainmail_chausses.geo.json
│   └── chainmail_boots.geo.json
├── plate/
│   ├── plate_helmet.geo.json
│   ├── plate_chestplate.geo.json
│   ├── plate_greaves.geo.json
│   └── plate_sabatons.geo.json
└── robes/
    ├── wizard_hat.geo.json
    ├── wizard_robe.geo.json  (covers chest + legs)
    └── wizard_shoes.geo.json
```

### Armor Layer Manager

```java
public class ArmorLayerManager {

    public void updateArmorLayers(CharacterEntity entity) {
        EquipmentLayerConfiguration config = entity.getEquipmentConfiguration();

        // Read armor from entity's equipment
        ItemStack helmet = entity.getEquippedStack(EquipmentSlot.HEAD);
        ItemStack chestplate = entity.getEquippedStack(EquipmentSlot.CHEST);
        ItemStack leggings = entity.getEquippedStack(EquipmentSlot.LEGS);
        ItemStack boots = entity.getEquippedStack(EquipmentSlot.FEET);

        // Update layer configuration
        updateArmorSlot(config, EquipmentSlot.HEAD, helmet);
        updateArmorSlot(config, EquipmentSlot.CHEST, chestplate);
        updateArmorSlot(config, EquipmentSlot.LEGS, leggings);
        updateArmorSlot(config, EquipmentSlot.FEET, boots);
    }

    private void updateArmorSlot(
        EquipmentLayerConfiguration config,
        EquipmentSlot slot,
        ItemStack armorItem
    ) {
        if (armorItem.isEmpty()) {
            config.removeEquipment(slot);
            return;
        }

        // Get armor type from item
        ArmorType armorType = getArmorType(armorItem);

        // Construct model ID
        String modelId = String.format("%s_%s",
            armorType.getId(),
            getSlotName(slot)
        );

        // Get texture ID (may be dyeable)
        String textureId = getArmorTexture(armorItem, armorType);

        // Set equipment layer
        config.setEquipment(slot, modelId, textureId);
    }

    private String getArmorTexture(ItemStack armorItem, ArmorType type) {
        // Check for custom dye (leather armor)
        if (armorItem.hasNbt() && armorItem.getNbt().contains("display")) {
            NbtCompound display = armorItem.getNbt().getCompound("display");
            if (display.contains("color")) {
                int color = display.getInt("color");
                return String.format("%s_dyed_%06x", type.getId(), color);
            }
        }

        // Default texture
        return type.getId() + "_default";
    }
}
```

## Clothing System

### Clothing Layers

Unlike armor, clothing is primarily texture-based with minimal geometry changes:

```java
public class ClothingConfiguration {
    // Texture overlays
    public String shirtTexture = null;      // Overlays on body
    public String pantsTexture = null;      // Overlays on legs
    public String robeTexture = null;       // Full-body overlay (hides shirt+pants)

    // Geometric additions
    public String capeModel = null;         // Actual geometry
    public String hoodModel = null;         // Actual geometry
    public String skirtModel = null;        // Actual geometry

    // Colors/dyes
    public int shirtColor = 0xFFFFFF;       // White = no tint
    public int pantsColor = 0xFFFFFF;
    public int capeColor = 0xFFFFFF;
}
```

### Clothing Rendering

```java
public class ClothingRenderer {

    public void renderClothing(
        CharacterEntity entity,
        MatrixStack matrices,
        VertexConsumerProvider vertices,
        int light,
        int overlay
    ) {
        ClothingConfiguration config = entity.getClothingConfiguration();

        // Render texture overlays (shader-based)
        if (config.shirtTexture != null) {
            renderClothingOverlay(
                "torso_upper",
                config.shirtTexture,
                config.shirtColor,
                matrices,
                vertices,
                light,
                overlay
            );
        }

        if (config.pantsTexture != null) {
            renderClothingOverlay(
                "legs",
                config.pantsTexture,
                config.pantsColor,
                matrices,
                vertices,
                light,
                overlay
            );
        }

        // Render geometric clothing
        if (config.capeModel != null) {
            renderClothingGeometry(
                config.capeModel,
                "cape_attach",
                config.capeColor,
                matrices,
                vertices,
                light,
                overlay
            );
        }
    }

    private void renderClothingOverlay(
        String bodyPart,
        String textureId,
        int tintColor,
        MatrixStack matrices,
        VertexConsumerProvider vertices,
        int light,
        int overlay
    ) {
        // Load overlay texture
        Identifier texture = getClothingTexture(textureId);

        // Extract RGB from tint
        float r = ((tintColor >> 16) & 0xFF) / 255.0f;
        float g = ((tintColor >> 8) & 0xFF) / 255.0f;
        float b = (tintColor & 0xFF) / 255.0f;

        // Render with tint and slight offset (avoid Z-fighting)
        VertexConsumer consumer = vertices.getBuffer(
            RenderLayer.getEntityTranslucent(texture)
        );

        matrices.push();
        matrices.translate(0, 0, 0.001f);  // Offset slightly outward

        // Render overlay geometry with tint
        renderBodyPartOverlay(bodyPart, matrices, consumer, light, overlay, r, g, b, 1.0f);

        matrices.pop();
    }
}
```

## Layer Visibility Rules

### Hiding Overlays

Some equipment should hide base body overlays:

```java
public class LayerVisibilityManager {

    public void updateVisibility(CharacterEntity entity) {
        BodyPartConfiguration body = entity.getBodyConfiguration();
        EquipmentLayerConfiguration equipment = entity.getEquipmentConfiguration();

        // Check if helmet hides hair
        if (equipment.hasEquipment(EquipmentSlot.HEAD)) {
            EquipmentLayer helmet = equipment.getLayer(EquipmentSlot.HEAD);

            if (helmet.hidesHair) {
                body.hairVisible = false;
            }

            if (helmet.hidesEars) {
                body.earsVisible = false;
            }

            if (helmet.hidesFacialHair) {
                body.facialHairVisible = false;
            }
        } else {
            // No helmet, show all overlays
            body.hairVisible = true;
            body.earsVisible = true;
            body.facialHairVisible = true;
        }

        // Robe hides body clothing
        ClothingConfiguration clothing = entity.getClothingConfiguration();
        if (clothing.robeTexture != null) {
            clothing.shirtVisible = false;
            clothing.pantsVisible = false;
        }

        // Heavy armor hides clothing
        if (equipment.hasEquipment(EquipmentSlot.CHEST)) {
            ArmorType type = getArmorType(equipment.getLayer(EquipmentSlot.CHEST));
            if (type.getCoverageLevel() == CoverageLevel.HEAVY) {
                clothing.shirtVisible = false;
            }
        }
    }
}
```

## Held Item System

### Weapon Attachment

Weapons attach to hand bones using locator bones:

```java
public class HeldItemRenderer {

    public void renderHeldItems(
        CharacterEntity entity,
        MatrixStack matrices,
        VertexConsumerProvider vertices,
        int light,
        int overlay
    ) {
        ItemStack mainHand = entity.getMainHandStack();
        ItemStack offHand = entity.getOffHandStack();

        // Render main hand item
        if (!mainHand.isEmpty()) {
            renderItemInHand(
                mainHand,
                "item_mainhand",
                Hand.MAIN_HAND,
                matrices,
                vertices,
                light,
                overlay
            );
        }

        // Render off hand item
        if (!offHand.isEmpty()) {
            renderItemInHand(
                offHand,
                "item_offhand",
                Hand.OFF_HAND,
                matrices,
                vertices,
                light,
                overlay
            );
        }
    }

    private void renderItemInHand(
        ItemStack stack,
        String boneName,
        Hand hand,
        MatrixStack matrices,
        VertexConsumerProvider vertices,
        int light,
        int overlay
    ) {
        matrices.push();

        // Get bone transform
        IBone bone = getBone(boneName);
        applyBoneTransform(matrices, bone);

        // Apply item-specific rotation/translation
        applyItemTransform(matrices, stack, hand);

        // Render item (delegate to Minecraft's item renderer)
        itemRenderer.renderItem(
            stack,
            ModelTransformationMode.THIRD_PERSON_RIGHT_HAND,
            light,
            overlay,
            matrices,
            vertices,
            entity.getWorld(),
            entity.getId()
        );

        matrices.pop();
    }

    private void applyItemTransform(MatrixStack matrices, ItemStack stack, Hand hand) {
        Item item = stack.getItem();

        if (item instanceof SwordItem) {
            // Sword grip
            matrices.translate(0, 0, -0.05);
            matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(-90));
        } else if (item instanceof BowItem) {
            // Bow hold
            matrices.translate(0, 0.05, 0);
            matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(90));
        } else if (item instanceof StaffItem) {
            // Staff vertical
            matrices.translate(0, 0.2, 0);
        } else if (item instanceof ShieldItem) {
            // Shield on forearm
            matrices.translate(-0.1, 0, 0);
            matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(-90));
        }

        // Mirror for left hand
        if (hand == Hand.OFF_HAND) {
            matrices.scale(-1, 1, 1);
        }
    }
}
```

## Dynamic Texture System

### Texture Atlasing

Combine multiple texture layers at runtime:

```java
public class DynamicTextureComposer {

    public Identifier composeTexture(CharacterEntity entity) {
        String cacheKey = generateCacheKey(entity);

        // Check cache
        if (textureCache.containsKey(cacheKey)) {
            return textureCache.get(cacheKey);
        }

        // Compose new texture
        NativeImage composite = new NativeImage(64, 64, true);

        // Layer 1: Base skin
        blendTexture(composite, entity.getSkinTexture());

        // Layer 2: Clothing overlays
        if (entity.getClothingConfiguration().shirtTexture != null) {
            blendTexture(composite, entity.getClothingConfiguration().shirtTexture);
        }

        // Layer 3: Tattoos/scars
        if (entity.getBodyConfiguration().tattoos >= 0) {
            blendTexture(composite, getTattooTexture(entity.getBodyConfiguration().tattoos));
        }

        // Upload to GPU
        Identifier textureId = uploadTexture(composite, cacheKey);
        textureCache.put(cacheKey, textureId);

        return textureId;
    }

    private void blendTexture(NativeImage target, String sourceTexturePath) {
        NativeImage source = loadTexture(sourceTexturePath);

        for (int y = 0; y < 64; y++) {
            for (int x = 0; x < 64; x++) {
                int sourcePixel = source.getColor(x, y);
                int targetPixel = target.getColor(x, y);

                // Alpha blend
                int blended = alphaBlend(sourcePixel, targetPixel);
                target.setColor(x, y, blended);
            }
        }
    }

    private int alphaBlend(int top, int bottom) {
        int aTop = (top >> 24) & 0xFF;
        int rTop = (top >> 16) & 0xFF;
        int gTop = (top >> 8) & 0xFF;
        int bTop = top & 0xFF;

        int aBottom = (bottom >> 24) & 0xFF;
        int rBottom = (bottom >> 16) & 0xFF;
        int gBottom = (bottom >> 8) & 0xFF;
        int bBottom = bottom & 0xFF;

        float alpha = aTop / 255.0f;

        int r = (int) (rTop * alpha + rBottom * (1 - alpha));
        int g = (int) (gTop * alpha + gBottom * (1 - alpha));
        int b = (int) (bTop * alpha + bBottom * (1 - alpha));
        int a = Math.max(aTop, aBottom);

        return (a << 24) | (r << 16) | (g << 8) | b;
    }
}
```

## Runtime Assembly Pipeline

### Complete Rendering Order

```java
public class CharacterLayerAssembler {

    public void render(
        CharacterEntity entity,
        float tickDelta,
        MatrixStack matrices,
        VertexConsumerProvider vertices,
        int light
    ) {
        int overlay = getOverlay(entity, tickDelta);

        // 1. Update visibility rules
        layerVisibilityManager.updateVisibility(entity);

        // 2. Compose dynamic textures
        Identifier composedTexture = textureComposer.composeTexture(entity);

        // 3. Render base body
        baseBodyRenderer.render(entity, composedTexture, matrices, vertices, light, overlay);

        // 4. Render clothing layers
        clothingRenderer.renderClothing(entity, matrices, vertices, light, overlay);

        // 5. Render equipment layers (armor)
        equipmentRenderer.renderEquipment(entity, matrices, vertices, light, overlay);

        // 6. Render accessories (capes, belts)
        accessoryRenderer.renderAccessories(entity, matrices, vertices, light, overlay);

        // 7. Render held items
        heldItemRenderer.renderHeldItems(entity, matrices, vertices, light, overlay);

        // 8. Render effects (particles, glows)
        effectRenderer.renderEffects(entity, matrices, vertices, light, overlay);
    }
}
```

## Network Synchronization

### Syncing Layers to Clients

```java
public class LayerSyncHandler {

    // Server-side: Send layer updates
    public void syncLayers(CharacterEntity entity) {
        PacketByteBuf buf = PacketByteBufs.create();

        // Body configuration
        buf.writeInt(entity.getBodyConfiguration().headVariant);
        buf.writeInt(entity.getBodyConfiguration().bodyVariant);
        buf.writeInt(entity.getBodyConfiguration().armsVariant);
        buf.writeInt(entity.getBodyConfiguration().legsVariant);
        buf.writeString(entity.getBodyConfiguration().skinTexture);

        // Equipment configuration
        writeEquipmentLayer(buf, entity.getEquipmentConfiguration(), EquipmentSlot.HEAD);
        writeEquipmentLayer(buf, entity.getEquipmentConfiguration(), EquipmentSlot.CHEST);
        writeEquipmentLayer(buf, entity.getEquipmentConfiguration(), EquipmentSlot.LEGS);
        writeEquipmentLayer(buf, entity.getEquipmentConfiguration(), EquipmentSlot.FEET);

        // Send to all tracking clients
        ServerPlayNetworking.send(
            (ServerPlayerEntity) entity,
            ModNetworking.LAYER_SYNC_PACKET,
            buf
        );
    }

    // Client-side: Receive layer updates
    public void handleLayerSync(MinecraftClient client, PacketByteBuf buf) {
        int entityId = buf.readInt();

        ClientWorld world = client.world;
        if (world == null) return;

        Entity entity = world.getEntityById(entityId);
        if (!(entity instanceof CharacterEntity character)) return;

        // Read body configuration
        character.getBodyConfiguration().headVariant = buf.readInt();
        character.getBodyConfiguration().bodyVariant = buf.readInt();
        character.getBodyConfiguration().armsVariant = buf.readInt();
        character.getBodyConfiguration().legsVariant = buf.readInt();
        character.getBodyConfiguration().skinTexture = buf.readString();

        // Read equipment configuration
        readEquipmentLayer(buf, character.getEquipmentConfiguration(), EquipmentSlot.HEAD);
        readEquipmentLayer(buf, character.getEquipmentConfiguration(), EquipmentSlot.CHEST);
        readEquipmentLayer(buf, character.getEquipmentConfiguration(), EquipmentSlot.LEGS);
        readEquipmentLayer(buf, character.getEquipmentConfiguration(), EquipmentSlot.FEET);

        // Invalidate texture cache (force recompose)
        textureComposer.invalidateCache(character);
    }
}
```

## Performance Optimization

### Layer Culling

Don't render hidden layers:

```java
public class LayerCullingOptimizer {

    public boolean shouldRenderLayer(EquipmentSlot slot, EquipmentLayerConfiguration config) {
        // Don't render shirt if heavy chest armor equipped
        if (slot == EquipmentSlot.CHEST) {
            if (config.hasEquipment(EquipmentSlot.CHEST)) {
                ArmorType type = getArmorType(config.getLayer(EquipmentSlot.CHEST));
                if (type.getCoverageLevel() == CoverageLevel.HEAVY) {
                    return false;  // Fully hidden, skip render
                }
            }
        }

        // Don't render hair if helmet equipped
        if (slot == EquipmentSlot.HEAD) {
            if (config.hasEquipment(EquipmentSlot.HEAD)) {
                if (config.getLayer(EquipmentSlot.HEAD).hidesHair) {
                    return false;
                }
            }
        }

        return true;
    }
}
```

### Texture Caching

Cache composed textures to avoid recomputing every frame:

```java
public class TextureCache {
    private final Map<String, CachedTexture> cache = new HashMap<>();

    private static class CachedTexture {
        Identifier textureId;
        long lastAccessTime;
        int accessCount;
    }

    public Identifier getOrCompose(CharacterEntity entity) {
        String key = generateKey(entity);

        CachedTexture cached = cache.get(key);
        if (cached != null) {
            cached.lastAccessTime = System.currentTimeMillis();
            cached.accessCount++;
            return cached.textureId;
        }

        // Compose new texture
        Identifier newTexture = composeTexture(entity);

        CachedTexture entry = new CachedTexture();
        entry.textureId = newTexture;
        entry.lastAccessTime = System.currentTimeMillis();
        entry.accessCount = 1;

        cache.put(key, entry);

        return newTexture;
    }

    // Cleanup old textures
    public void cleanup() {
        long now = System.currentTimeMillis();
        cache.entrySet().removeIf(entry -> {
            long age = now - entry.getValue().lastAccessTime;
            return age > 60000;  // Remove if not accessed in 60 seconds
        });
    }
}
```

---

## Testing Layer Assembly

### Test Scenarios

1. **Minimal Equipment**: Character with no armor, basic clothing
2. **Full Armor**: Character with complete plate armor set
3. **Mixed Equipment**: Leather helmet, chain chest, iron legs, cloth robe
4. **Held Items**: Sword in main hand, shield in off hand
5. **Visibility Rules**: Helmet hiding hair, robe hiding shirt
6. **Dynamic Changes**: Equip/unequip armor during gameplay

### Visual Validation

Create test GUI for previewing all layer combinations:

```java
public class LayerTestScreen extends Screen {

    public void renderPreview() {
        // Render character with current layer config
        renderCharacter(testEntity, matrices, tickDelta);

        // UI controls
        renderSlider("Head Variant", 0, 6, currentHeadVariant);
        renderSlider("Body Variant", 0, 3, currentBodyVariant);
        renderDropdown("Helmet", helmetOptions);
        renderDropdown("Chest Armor", chestOptions);
        renderCheckbox("Show Hair", showHair);

        // Update preview on change
        if (anyControlChanged()) {
            updateTestEntity();
        }
    }
}
```

---

**Document Version**: 1.0
**Last Updated**: 2025-11-13
**Status**: Ready for Implementation
