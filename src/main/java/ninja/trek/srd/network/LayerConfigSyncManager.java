package ninja.trek.srd.network;

import net.fabricmc.fabric.api.networking.v1.PlayerLookup;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import ninja.trek.srd.FiveESrdMod;
import ninja.trek.srd.character.entity.CharacterEntity;
import ninja.trek.srd.character.layer.LayerConfiguration;
import ninja.trek.srd.network.payloads.SyncLayerConfigPayload;
import ninja.trek.srd.network.payloads.UpdateLayerConfigPayload;

import java.util.Collection;
import java.util.UUID;

/**
 * Server-side manager for synchronizing layer configurations to clients.
 * Handles both full syncs and incremental updates.
 *
 * This is part of GeckoLib Phase 7.4: Network Synchronization.
 */
public class LayerConfigSyncManager {

    /**
     * Send a full layer configuration sync to all players tracking this entity.
     * Use this when:
     * - A player first sees the entity (initial sync)
     * - Major configuration changes occur (e.g., complete outfit change)
     * - Reconnecting players need to catch up
     *
     * @param entity The CharacterEntity whose configuration to sync
     * @param server The server instance
     */
    public static void syncFullConfiguration(CharacterEntity entity, MinecraftServer server) {
        if (entity.getEntityWorld().isClient()) {
            FiveESrdMod.LOGGER.warn("Attempted to sync layer config from client side");
            return;
        }

        LayerConfiguration config = entity.getLayerConfiguration();
        SyncLayerConfigPayload payload = SyncLayerConfigPayload.fromLayerConfig(entity.getUuid(), config);

        // Send to all players tracking this entity
        Collection<ServerPlayerEntity> trackingPlayers = getTrackingPlayers(entity, server);
        for (ServerPlayerEntity player : trackingPlayers) {
            ServerPlayNetworking.send(player, payload);
        }

        FiveESrdMod.LOGGER.debug("Sent full layer config sync for entity {} to {} players",
            entity.getUuid(), trackingPlayers.size());
    }

    /**
     * Send a full layer configuration sync to a specific player.
     * Use this when a player joins or enters render distance of an entity.
     *
     * @param entity The CharacterEntity whose configuration to sync
     * @param player The player to send the sync to
     */
    public static void syncFullConfigurationToPlayer(CharacterEntity entity, ServerPlayerEntity player) {
        if (entity.getEntityWorld().isClient()) {
            FiveESrdMod.LOGGER.warn("Attempted to sync layer config from client side");
            return;
        }

        LayerConfiguration config = entity.getLayerConfiguration();
        SyncLayerConfigPayload payload = SyncLayerConfigPayload.fromLayerConfig(entity.getUuid(), config);

        ServerPlayNetworking.send(player, payload);

        FiveESrdMod.LOGGER.debug("Sent full layer config sync for entity {} to player {}",
            entity.getUuid(), player.getName().getString());
    }

    /**
     * Send an incremental configuration update to all players tracking this entity.
     * Use this when a single field changes (e.g., equipping a helmet).
     *
     * This is more efficient than a full sync for small changes.
     *
     * @param entity The CharacterEntity whose configuration changed
     * @param update The specific update to send
     * @param server The server instance
     */
    public static void sendUpdate(CharacterEntity entity, UpdateLayerConfigPayload update, MinecraftServer server) {
        if (entity.getEntityWorld().isClient()) {
            FiveESrdMod.LOGGER.warn("Attempted to send layer config update from client side");
            return;
        }

        // Send to all players tracking this entity
        Collection<ServerPlayerEntity> trackingPlayers = getTrackingPlayers(entity, server);
        for (ServerPlayerEntity player : trackingPlayers) {
            ServerPlayNetworking.send(player, update);
        }

        FiveESrdMod.LOGGER.debug("Sent layer config update ({}) for entity {} to {} players",
            update.updateType(), entity.getUuid(), trackingPlayers.size());
    }

    /**
     * Convenience method: Update body variant and sync to clients.
     */
    public static void updateBodyVariant(CharacterEntity entity, int variant, MinecraftServer server) {
        entity.getLayerConfiguration().setBodyVariant(variant);
        sendUpdate(entity, UpdateLayerConfigPayload.bodyVariant(entity.getUuid(), variant), server);
    }

    /**
     * Convenience method: Update legs variant and sync to clients.
     */
    public static void updateLegsVariant(CharacterEntity entity, int variant, MinecraftServer server) {
        entity.getLayerConfiguration().setLegsVariant(variant);
        sendUpdate(entity, UpdateLayerConfigPayload.legsVariant(entity.getUuid(), variant), server);
    }

    /**
     * Convenience method: Update arms variant and sync to clients.
     */
    public static void updateArmsVariant(CharacterEntity entity, int variant, MinecraftServer server) {
        entity.getLayerConfiguration().setArmsVariant(variant);
        sendUpdate(entity, UpdateLayerConfigPayload.armsVariant(entity.getUuid(), variant), server);
    }

    /**
     * Convenience method: Update head variant and sync to clients.
     */
    public static void updateHeadVariant(CharacterEntity entity, int variant, MinecraftServer server) {
        entity.getLayerConfiguration().setHeadVariant(variant);
        sendUpdate(entity, UpdateLayerConfigPayload.headVariant(entity.getUuid(), variant), server);
    }

    /**
     * Convenience method: Equip helmet and sync to clients.
     */
    public static void equipHelmet(CharacterEntity entity, String model, MinecraftServer server) {
        entity.getLayerConfiguration().setHelmetModel(model);
        sendUpdate(entity, UpdateLayerConfigPayload.helmetModel(entity.getUuid(), model), server);
    }

    /**
     * Convenience method: Equip chest armor and sync to clients.
     */
    public static void equipChestArmor(CharacterEntity entity, String model, MinecraftServer server) {
        entity.getLayerConfiguration().setChestArmorModel(model);
        sendUpdate(entity, UpdateLayerConfigPayload.chestArmorModel(entity.getUuid(), model), server);
    }

    /**
     * Convenience method: Equip leg armor and sync to clients.
     */
    public static void equipLegArmor(CharacterEntity entity, String model, MinecraftServer server) {
        entity.getLayerConfiguration().setLegArmorModel(model);
        sendUpdate(entity, UpdateLayerConfigPayload.legArmorModel(entity.getUuid(), model), server);
    }

    /**
     * Convenience method: Equip boots and sync to clients.
     */
    public static void equipBoots(CharacterEntity entity, String model, MinecraftServer server) {
        entity.getLayerConfiguration().setBootArmorModel(model);
        sendUpdate(entity, UpdateLayerConfigPayload.bootArmorModel(entity.getUuid(), model), server);
    }

    /**
     * Convenience method: Equip cape and sync to clients.
     */
    public static void equipCape(CharacterEntity entity, String model, MinecraftServer server) {
        entity.getLayerConfiguration().setCapeModel(model);
        sendUpdate(entity, UpdateLayerConfigPayload.capeModel(entity.getUuid(), model), server);
    }

    /**
     * Convenience method: Equip main hand item and sync to clients.
     */
    public static void equipMainHand(CharacterEntity entity, String model, MinecraftServer server) {
        entity.getLayerConfiguration().setMainHandModel(model);
        sendUpdate(entity, UpdateLayerConfigPayload.mainHandModel(entity.getUuid(), model), server);
    }

    /**
     * Convenience method: Equip off hand item and sync to clients.
     */
    public static void equipOffHand(CharacterEntity entity, String model, MinecraftServer server) {
        entity.getLayerConfiguration().setOffHandModel(model);
        sendUpdate(entity, UpdateLayerConfigPayload.offHandModel(entity.getUuid(), model), server);
    }

    /**
     * Convenience method: Change skin texture and sync to clients.
     */
    public static void updateSkinTexture(CharacterEntity entity, String texture, MinecraftServer server) {
        entity.getLayerConfiguration().setSkinTexture(texture);
        sendUpdate(entity, UpdateLayerConfigPayload.skinTexture(entity.getUuid(), texture), server);
    }

    /**
     * Convenience method: Change clothing texture and sync to clients.
     */
    public static void updateClothingTexture(CharacterEntity entity, String texture, MinecraftServer server) {
        entity.getLayerConfiguration().setClothingTexture(texture);
        sendUpdate(entity, UpdateLayerConfigPayload.clothingTexture(entity.getUuid(), texture), server);
    }

    /**
     * Convenience method: Set armor texture for a slot and sync to clients.
     */
    public static void updateArmorTexture(CharacterEntity entity, String slot, String texture, MinecraftServer server) {
        entity.getLayerConfiguration().setArmorTexture(slot, texture);
        sendUpdate(entity, UpdateLayerConfigPayload.armorTexture(entity.getUuid(), slot, texture), server);
    }

    /**
     * Convenience method: Toggle hair visibility and sync to clients.
     */
    public static void toggleHair(CharacterEntity entity, boolean show, MinecraftServer server) {
        entity.getLayerConfiguration().setShowHair(show);
        sendUpdate(entity, UpdateLayerConfigPayload.showHair(entity.getUuid(), show), server);
    }

    /**
     * Convenience method: Toggle ears visibility and sync to clients.
     */
    public static void toggleEars(CharacterEntity entity, boolean show, MinecraftServer server) {
        entity.getLayerConfiguration().setShowEars(show);
        sendUpdate(entity, UpdateLayerConfigPayload.showEars(entity.getUuid(), show), server);
    }

    /**
     * Convenience method: Toggle cape visibility and sync to clients.
     */
    public static void toggleCape(CharacterEntity entity, boolean show, MinecraftServer server) {
        entity.getLayerConfiguration().setShowCape(show);
        sendUpdate(entity, UpdateLayerConfigPayload.showCape(entity.getUuid(), show), server);
    }

    /**
     * Get all players currently tracking this entity.
     * These are the players who can see the entity and need to receive updates.
     */
    private static Collection<ServerPlayerEntity> getTrackingPlayers(CharacterEntity entity, MinecraftServer server) {
        if (!(entity.getEntityWorld() instanceof ServerWorld serverWorld)) {
            return java.util.Collections.emptyList();
        }

        // Use Fabric's PlayerLookup to get all players tracking this entity
        return PlayerLookup.tracking(serverWorld, entity.getBlockPos());
    }
}
