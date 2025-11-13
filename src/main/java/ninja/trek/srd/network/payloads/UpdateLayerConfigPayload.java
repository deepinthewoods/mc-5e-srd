package ninja.trek.srd.network.payloads;

import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;
import net.minecraft.util.Uuids;
import ninja.trek.srd.FiveESrdMod;
import ninja.trek.srd.character.layer.LayerConfiguration;

import java.util.UUID;

/**
 * Server-to-Client packet for incremental layer configuration updates.
 * Only sends the specific field that changed, reducing bandwidth.
 *
 * This is used when equipment changes, a texture is swapped, or visibility toggles.
 * For initial sync or major changes, use SyncLayerConfigPayload instead.
 */
public record UpdateLayerConfigPayload(
    UUID entityId,
    UpdateType updateType,
    String stringValue,
    int intValue,
    boolean boolValue
) implements CustomPayload {

    public static final Identifier ID_CONSTANT = Identifier.of(FiveESrdMod.MOD_ID, "update_layer_config");
    public static final CustomPayload.Id<UpdateLayerConfigPayload> ID = new CustomPayload.Id<>(ID_CONSTANT);

    /**
     * Types of updates that can be sent incrementally.
     */
    public enum UpdateType {
        // Body variants (use intValue)
        BODY_VARIANT,
        LEGS_VARIANT,
        ARMS_VARIANT,
        HEAD_VARIANT,

        // Equipment models (use stringValue, null to remove)
        HELMET_MODEL,
        CHEST_ARMOR_MODEL,
        LEG_ARMOR_MODEL,
        BOOT_ARMOR_MODEL,
        CAPE_MODEL,
        MAIN_HAND_MODEL,
        OFF_HAND_MODEL,

        // Textures (use stringValue)
        SKIN_TEXTURE,
        CLOTHING_TEXTURE,

        // Armor texture slot (use stringValue as "slot:texture")
        ARMOR_TEXTURE,

        // Visibility flags (use boolValue)
        SHOW_HAIR,
        SHOW_EARS,
        SHOW_CAPE
    }

    /**
     * Factory methods for creating specific update types.
     */
    public static UpdateLayerConfigPayload bodyVariant(UUID entityId, int variant) {
        return new UpdateLayerConfigPayload(entityId, UpdateType.BODY_VARIANT, null, variant, false);
    }

    public static UpdateLayerConfigPayload legsVariant(UUID entityId, int variant) {
        return new UpdateLayerConfigPayload(entityId, UpdateType.LEGS_VARIANT, null, variant, false);
    }

    public static UpdateLayerConfigPayload armsVariant(UUID entityId, int variant) {
        return new UpdateLayerConfigPayload(entityId, UpdateType.ARMS_VARIANT, null, variant, false);
    }

    public static UpdateLayerConfigPayload headVariant(UUID entityId, int variant) {
        return new UpdateLayerConfigPayload(entityId, UpdateType.HEAD_VARIANT, null, variant, false);
    }

    public static UpdateLayerConfigPayload helmetModel(UUID entityId, String model) {
        return new UpdateLayerConfigPayload(entityId, UpdateType.HELMET_MODEL, model, 0, false);
    }

    public static UpdateLayerConfigPayload chestArmorModel(UUID entityId, String model) {
        return new UpdateLayerConfigPayload(entityId, UpdateType.CHEST_ARMOR_MODEL, model, 0, false);
    }

    public static UpdateLayerConfigPayload legArmorModel(UUID entityId, String model) {
        return new UpdateLayerConfigPayload(entityId, UpdateType.LEG_ARMOR_MODEL, model, 0, false);
    }

    public static UpdateLayerConfigPayload bootArmorModel(UUID entityId, String model) {
        return new UpdateLayerConfigPayload(entityId, UpdateType.BOOT_ARMOR_MODEL, model, 0, false);
    }

    public static UpdateLayerConfigPayload capeModel(UUID entityId, String model) {
        return new UpdateLayerConfigPayload(entityId, UpdateType.CAPE_MODEL, model, 0, false);
    }

    public static UpdateLayerConfigPayload mainHandModel(UUID entityId, String model) {
        return new UpdateLayerConfigPayload(entityId, UpdateType.MAIN_HAND_MODEL, model, 0, false);
    }

    public static UpdateLayerConfigPayload offHandModel(UUID entityId, String model) {
        return new UpdateLayerConfigPayload(entityId, UpdateType.OFF_HAND_MODEL, model, 0, false);
    }

    public static UpdateLayerConfigPayload skinTexture(UUID entityId, String texture) {
        return new UpdateLayerConfigPayload(entityId, UpdateType.SKIN_TEXTURE, texture, 0, false);
    }

    public static UpdateLayerConfigPayload clothingTexture(UUID entityId, String texture) {
        return new UpdateLayerConfigPayload(entityId, UpdateType.CLOTHING_TEXTURE, texture, 0, false);
    }

    public static UpdateLayerConfigPayload armorTexture(UUID entityId, String slot, String texture) {
        return new UpdateLayerConfigPayload(entityId, UpdateType.ARMOR_TEXTURE, slot + ":" + texture, 0, false);
    }

    public static UpdateLayerConfigPayload showHair(UUID entityId, boolean show) {
        return new UpdateLayerConfigPayload(entityId, UpdateType.SHOW_HAIR, null, 0, show);
    }

    public static UpdateLayerConfigPayload showEars(UUID entityId, boolean show) {
        return new UpdateLayerConfigPayload(entityId, UpdateType.SHOW_EARS, null, 0, show);
    }

    public static UpdateLayerConfigPayload showCape(UUID entityId, boolean show) {
        return new UpdateLayerConfigPayload(entityId, UpdateType.SHOW_CAPE, null, 0, show);
    }

    /**
     * Apply this update to a LayerConfiguration object.
     */
    public void applyToConfig(LayerConfiguration config) {
        switch (updateType) {
            case BODY_VARIANT -> config.setBodyVariant(intValue);
            case LEGS_VARIANT -> config.setLegsVariant(intValue);
            case ARMS_VARIANT -> config.setArmsVariant(intValue);
            case HEAD_VARIANT -> config.setHeadVariant(intValue);
            case HELMET_MODEL -> config.setHelmetModel(stringValue);
            case CHEST_ARMOR_MODEL -> config.setChestArmorModel(stringValue);
            case LEG_ARMOR_MODEL -> config.setLegArmorModel(stringValue);
            case BOOT_ARMOR_MODEL -> config.setBootArmorModel(stringValue);
            case CAPE_MODEL -> config.setCapeModel(stringValue);
            case MAIN_HAND_MODEL -> config.setMainHandModel(stringValue);
            case OFF_HAND_MODEL -> config.setOffHandModel(stringValue);
            case SKIN_TEXTURE -> config.setSkinTexture(stringValue);
            case CLOTHING_TEXTURE -> config.setClothingTexture(stringValue);
            case ARMOR_TEXTURE -> {
                if (stringValue != null && stringValue.contains(":")) {
                    String[] parts = stringValue.split(":", 2);
                    config.setArmorTexture(parts[0], parts[1]);
                }
            }
            case SHOW_HAIR -> config.setShowHair(boolValue);
            case SHOW_EARS -> config.setShowEars(boolValue);
            case SHOW_CAPE -> config.setShowCape(boolValue);
        }
    }

    // Custom codec for UpdateType enum
    private static final PacketCodec<RegistryByteBuf, UpdateType> UPDATE_TYPE_CODEC = new PacketCodec<>() {
        @Override
        public UpdateType decode(RegistryByteBuf buf) {
            int ordinal = buf.readVarInt();
            UpdateType[] values = UpdateType.values();
            return ordinal >= 0 && ordinal < values.length ? values[ordinal] : UpdateType.BODY_VARIANT;
        }

        @Override
        public void encode(RegistryByteBuf buf, UpdateType type) {
            buf.writeVarInt(type.ordinal());
        }
    };

    // Custom codec for nullable string
    private static final PacketCodec<RegistryByteBuf, String> NULLABLE_STRING_CODEC = new PacketCodec<>() {
        @Override
        public String decode(RegistryByteBuf buf) {
            boolean present = buf.readBoolean();
            return present ? buf.readString() : null;
        }

        @Override
        public void encode(RegistryByteBuf buf, String value) {
            buf.writeBoolean(value != null);
            if (value != null) {
                buf.writeString(value);
            }
        }
    };

    // Codec for the entire packet
    public static final PacketCodec<RegistryByteBuf, UpdateLayerConfigPayload> CODEC = new PacketCodec<>() {
        @Override
        public UpdateLayerConfigPayload decode(RegistryByteBuf buf) {
            UUID entityId = Uuids.PACKET_CODEC.decode(buf);
            UpdateType updateType = UPDATE_TYPE_CODEC.decode(buf);
            String stringValue = NULLABLE_STRING_CODEC.decode(buf);
            int intValue = buf.readVarInt();
            boolean boolValue = buf.readBoolean();
            return new UpdateLayerConfigPayload(entityId, updateType, stringValue, intValue, boolValue);
        }

        @Override
        public void encode(RegistryByteBuf buf, UpdateLayerConfigPayload payload) {
            Uuids.PACKET_CODEC.encode(buf, payload.entityId);
            UPDATE_TYPE_CODEC.encode(buf, payload.updateType);
            NULLABLE_STRING_CODEC.encode(buf, payload.stringValue);
            buf.writeVarInt(payload.intValue);
            buf.writeBoolean(payload.boolValue);
        }
    };

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
}
