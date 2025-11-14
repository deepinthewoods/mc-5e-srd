# Network Synchronization - Phase 7.4

This document describes the network synchronization system for GeckoLib layer configurations implemented in Phase 7.4.

## Overview

The network synchronization system ensures that character rendering configurations (body variants, equipment, textures, visibility flags) are properly synchronized from the server to all clients. This is essential for multiplayer gameplay where multiple players need to see the same character appearance.

## Architecture

### Packet Types

#### 1. SyncLayerConfigPayload (Server → Client)

**Purpose**: Full synchronization of all layer configuration data.

**When to use**:
- Initial sync when a player first sees an entity (via `onStartedTrackingBy`)
- Player reconnects or enters render distance
- Major configuration changes (complete outfit change)

**Data synchronized**:
- Body part variants (body, legs, arms, head) - 4 integers
- Equipment models (helmet, chest, legs, boots, cape, main hand, off hand) - 7 nullable strings
- Textures (skin, clothing) - 2 strings
- Armor texture map - Map<String, String>
- Visibility flags (showHair, showEars, showCape) - 3 booleans

**Size**: ~200-500 bytes depending on string lengths and armor texture count

**File**: `/home/user/mc-5e-srd/src/main/java/ninja/trek/srd/network/payloads/SyncLayerConfigPayload.java`

#### 2. UpdateLayerConfigPayload (Server → Client)

**Purpose**: Incremental update of a single configuration field.

**When to use**:
- Equipment change (e.g., equipping a helmet)
- Texture swap (e.g., changing skin color)
- Visibility toggle (e.g., hiding hair when helmet equipped)
- Any single-field modification

**Update types**:
- `BODY_VARIANT`, `LEGS_VARIANT`, `ARMS_VARIANT`, `HEAD_VARIANT`
- `HELMET_MODEL`, `CHEST_ARMOR_MODEL`, `LEG_ARMOR_MODEL`, `BOOT_ARMOR_MODEL`
- `CAPE_MODEL`, `MAIN_HAND_MODEL`, `OFF_HAND_MODEL`
- `SKIN_TEXTURE`, `CLOTHING_TEXTURE`, `ARMOR_TEXTURE`
- `SHOW_HAIR`, `SHOW_EARS`, `SHOW_CAPE`

**Size**: ~30-100 bytes (much more efficient than full sync)

**File**: `/home/user/mc-5e-srd/src/main/java/ninja/trek/srd/network/payloads/UpdateLayerConfigPayload.java`

### Server-Side Components

#### LayerConfigSyncManager

**Purpose**: Central utility class for sending layer configuration updates from server to clients.

**Key methods**:

1. **Full Sync Methods**:
   ```java
   LayerConfigSyncManager.syncFullConfiguration(entity, server);
   LayerConfigSyncManager.syncFullConfigurationToPlayer(entity, player);
   ```

2. **Incremental Update Method**:
   ```java
   LayerConfigSyncManager.sendUpdate(entity, update, server);
   ```

3. **Convenience Methods** (apply change + sync in one call):
   ```java
   LayerConfigSyncManager.equipHelmet(entity, "helmet_iron", server);
   LayerConfigSyncManager.updateBodyVariant(entity, 2, server);
   LayerConfigSyncManager.toggleHair(entity, false, server);
   // ... and many more
   ```

**Target players**: Uses `PlayerLookup.tracking()` to send updates only to players who can see the entity.

**File**: `/home/user/mc-5e-srd/src/main/java/ninja/trek/srd/network/LayerConfigSyncManager.java`

#### CharacterEntity Integration

**Automatic initial sync**:
```java
@Override
public void onStartedTrackingBy(ServerPlayerEntity player) {
    super.onStartedTrackingBy(player);
    LayerConfigSyncManager.syncFullConfigurationToPlayer(this, player);
}
```

When a player enters render distance of a CharacterEntity, the full configuration is automatically sent.

**Dirty flag tracking**:
```java
private boolean layerConfigDirty = true;

public void markLayerConfigDirty() {
    this.layerConfigDirty = true;
}
```

The dirty flag can be used for batching updates or periodic syncing if needed.

**File**: `/home/user/mc-5e-srd/src/main/java/ninja/trek/srd/character/entity/CharacterEntity.java`

### Client-Side Components

#### ClientPacketHandlers

**Handlers**:

1. **handleSyncLayerConfig**: Receives full configuration and applies to entity
   ```java
   private static void handleSyncLayerConfig(SyncLayerConfigPayload payload, ...)
   ```

2. **handleUpdateLayerConfig**: Receives incremental update and applies change
   ```java
   private static void handleUpdateLayerConfig(UpdateLayerConfigPayload payload, ...)
   ```

**Entity lookup**: Uses `world.getEntityById()` to find the CharacterEntity and apply configuration.

**Thread safety**: All packet handling is executed on the client thread via `context.client().execute()`.

**File**: `/home/user/mc-5e-srd/src/client/java/ninja/trek/srd/network/ClientPacketHandlers.java`

### Registration

#### ModNetworking

**Payload registration**:
```java
PayloadTypeRegistry.playS2C().register(
    SyncLayerConfigPayload.ID,
    SyncLayerConfigPayload.CODEC
);

PayloadTypeRegistry.playS2C().register(
    UpdateLayerConfigPayload.ID,
    UpdateLayerConfigPayload.CODEC
);
```

**Handler registration** (in ClientPacketHandlers.register()):
```java
ClientPlayNetworking.registerGlobalReceiver(
    SyncLayerConfigPayload.ID,
    ClientPacketHandlers::handleSyncLayerConfig
);

ClientPlayNetworking.registerGlobalReceiver(
    UpdateLayerConfigPayload.ID,
    ClientPacketHandlers::handleUpdateLayerConfig
);
```

**Files**:
- `/home/user/mc-5e-srd/src/main/java/ninja/trek/srd/registry/ModNetworking.java`
- `/home/user/mc-5e-srd/src/client/java/ninja/trek/srd/network/ClientPacketHandlers.java`

## Usage Examples

### Example 1: Equipping Armor

```java
// Server-side code
CharacterEntity character = ...; // get the character entity
MinecraftServer server = character.getServer();

// Equip full set of iron armor (incremental updates)
LayerConfigSyncManager.equipHelmet(character, "helmet_iron", server);
LayerConfigSyncManager.equipChestArmor(character, "chestplate_iron", server);
LayerConfigSyncManager.equipLegArmor(character, "leggings_iron", server);
LayerConfigSyncManager.equipBoots(character, "boots_iron", server);

// Or modify directly and send update
character.getLayerConfiguration().setHelmetModel("helmet_iron");
LayerConfigSyncManager.sendUpdate(
    character,
    UpdateLayerConfigPayload.helmetModel(character.getUuid(), "helmet_iron"),
    server
);
```

### Example 2: Complete Outfit Change

```java
// For major changes, use full sync
LayerConfiguration config = character.getLayerConfiguration();
config.setBodyVariant(1);
config.setLegsVariant(2);
config.setArmsVariant(1);
config.setHeadVariant(0);
config.setHelmetModel("helmet_leather");
config.setChestArmorModel("chestplate_leather");
// ... more changes

// Send everything at once
LayerConfigSyncManager.syncFullConfiguration(character, server);
```

### Example 3: Texture Swap

```java
// Change skin texture (e.g., for racial variants)
LayerConfigSyncManager.updateSkinTexture(
    character,
    "elf_pale",
    server
);

// Change clothing texture
LayerConfigSyncManager.updateClothingTexture(
    character,
    "noble_outfit",
    server
);
```

### Example 4: Visibility Toggles

```java
// Hide hair when helmet is equipped
LayerConfigSyncManager.equipHelmet(character, "helmet_full", server);
LayerConfigSyncManager.toggleHair(character, false, server);
LayerConfigSyncManager.toggleEars(character, false, server);

// Show hair when helmet is removed
LayerConfigSyncManager.equipHelmet(character, null, server);
LayerConfigSyncManager.toggleHair(character, true, server);
LayerConfigSyncManager.toggleEars(character, true, server);
```

## Performance Considerations

### Bandwidth Optimization

1. **Incremental updates**: ~30-100 bytes vs ~200-500 bytes for full sync
   - Use `UpdateLayerConfigPayload` for single changes
   - Use `SyncLayerConfigPayload` only when necessary

2. **Player targeting**: Updates only sent to players tracking the entity
   - Uses `PlayerLookup.tracking()` to minimize network traffic
   - No packets sent to players who can't see the entity

3. **Batching**: For multiple changes, consider:
   ```java
   // Less efficient (multiple packets)
   LayerConfigSyncManager.equipHelmet(character, "helmet", server);
   LayerConfigSyncManager.equipChestArmor(character, "chest", server);
   LayerConfigSyncManager.equipLegArmor(character, "legs", server);

   // More efficient (one packet)
   config.setHelmetModel("helmet");
   config.setChestArmorModel("chest");
   config.setLegArmorModel("legs");
   LayerConfigSyncManager.syncFullConfiguration(character, server);
   ```

### Sync Frequency

- **Initial sync**: Once when player starts tracking (automatic)
- **Equipment changes**: Immediately (incremental)
- **Periodic sync**: Not implemented (not needed for typical use)
- **Reconnect sync**: Automatic via `onStartedTrackingBy`

## Integration Points

### With Equipment System

When implementing the equipment system, integrate as follows:

```java
public void equipItem(ItemStack item, EquipmentSlot slot) {
    // Apply equipment to entity
    String model = getModelForItem(item);

    // Sync to clients
    switch (slot) {
        case HEAD -> LayerConfigSyncManager.equipHelmet(this, model, server);
        case CHEST -> LayerConfigSyncManager.equipChestArmor(this, model, server);
        case LEGS -> LayerConfigSyncManager.equipLegArmor(this, model, server);
        case FEET -> LayerConfigSyncManager.equipBoots(this, model, server);
    }
}
```

### With Character Creation

When creating a character, set initial configuration then sync:

```java
public void createCharacter(...) {
    CharacterEntity character = new CharacterEntity(...);

    // Set initial appearance
    character.getLayerConfiguration().setBodyVariant(appearance.bodyIndex());
    character.getLayerConfiguration().setLegsVariant(appearance.legsIndex());
    // ... more configuration

    // Spawn entity (onStartedTrackingBy will auto-sync)
    world.spawnEntity(character);
}
```

### With Animation System

The renderer reads `LayerConfiguration` directly:

```java
public class CharacterEntityRenderer extends GeoEntityRenderer<CharacterEntity> {
    @Override
    public void render(...) {
        LayerConfiguration config = entity.getLayerConfiguration();

        // Use config.getBodyVariant(), config.getHelmetModel(), etc.
        // to determine which models/textures to render
    }
}
```

No additional syncing needed - renderer uses the already-synced configuration.

## Troubleshooting

### Configuration Not Syncing

**Problem**: Client sees default appearance instead of configured appearance.

**Solutions**:
1. Verify payloads are registered in `ModNetworking.initialize()`
2. Verify handlers are registered in `ClientPacketHandlers.register()`
3. Check server logs for "Sent full layer config sync" messages
4. Check client logs for "Received full layer config sync" messages
5. Ensure `onStartedTrackingBy` is being called

### Entity Not Found on Client

**Problem**: "Entity not found or not a CharacterEntity" in client logs.

**Solutions**:
1. Entity ID mismatch - verify UUID is being sent correctly
2. Entity not yet loaded on client - sync happens before entity spawns
3. Use entity's actual ID, not hash code: `world.getEntityById(entity.getId())`

### Desync Between Players

**Problem**: Different players see different configurations for the same entity.

**Solutions**:
1. Verify `PlayerLookup.tracking()` is working correctly
2. Ensure updates are sent to all tracking players
3. Check that full sync is sent when players start tracking
4. Look for race conditions in configuration updates

## Future Enhancements

### Potential Improvements

1. **Compression**: For large armor texture maps, consider compression
2. **Batching**: Implement update batching for rapid changes
3. **Caching**: Client-side caching of configurations for respawned entities
4. **Delta encoding**: Even more efficient updates by sending only changed bytes
5. **Request-based sync**: Let clients request resync if desynced

### Extension Points

1. **Custom update types**: Add new `UpdateType` enum values for new fields
2. **Validation**: Add server-side validation of configuration values
3. **Permissions**: Check player permissions before applying changes
4. **Events**: Fire events when configuration changes for mod integration

## Testing Checklist

- [ ] Single player can see their character's configuration
- [ ] Multiple players see the same configuration for an entity
- [ ] Configuration persists when player moves away and returns
- [ ] Reconnecting players receive current configuration
- [ ] Incremental updates apply correctly
- [ ] Full sync applies correctly
- [ ] Equipment changes reflect immediately
- [ ] Texture changes reflect immediately
- [ ] Visibility toggles work correctly
- [ ] No memory leaks from packet handlers
- [ ] Performance acceptable with many entities

## Summary

The network synchronization system provides:

✅ **Automatic initial sync** when players see entities
✅ **Efficient incremental updates** for single changes
✅ **Full sync capability** for major changes
✅ **Convenient helper methods** for common operations
✅ **Type-safe payloads** using Java records
✅ **Proper threading** with client thread execution
✅ **Minimal bandwidth** through delta updates
✅ **Clean integration** with existing systems

This implementation completes Phase 7.4 of the GeckoLib animation system.
