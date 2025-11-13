package ninja.trek.srd.network.payloads;

import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;
import net.minecraft.util.Uuids;
import ninja.trek.srd.FiveESrdMod;
import ninja.trek.srd.character.layer.LayerConfiguration;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Server-to-Client packet that synchronizes a character's complete layer configuration.
 * This includes body variants, equipment models, textures, and visibility flags.
 *
 * Used for:
 * - Initial sync when a player first sees an entity
 * - Full resync when requested or after major changes
 *
 * For incremental updates, see UpdateLayerConfigPayload.
 */
public record SyncLayerConfigPayload(
    UUID entityId,
    int bodyVariant,
    int legsVariant,
    int armsVariant,
    int headVariant,
    String helmetModel,
    String chestArmorModel,
    String legArmorModel,
    String bootArmorModel,
    String capeModel,
    String mainHandModel,
    String offHandModel,
    String skinTexture,
    String clothingTexture,
    Map<String, String> armorTextures,
    boolean showHair,
    boolean showEars,
    boolean showCape
) implements CustomPayload {

    public static final Identifier ID_CONSTANT = Identifier.of(FiveESrdMod.MOD_ID, "sync_layer_config");
    public static final CustomPayload.Id<SyncLayerConfigPayload> ID = new CustomPayload.Id<>(ID_CONSTANT);

    /**
     * Create a payload from a LayerConfiguration object.
     */
    public static SyncLayerConfigPayload fromLayerConfig(UUID entityId, LayerConfiguration config) {
        return new SyncLayerConfigPayload(
            entityId,
            config.getBodyVariant(),
            config.getLegsVariant(),
            config.getArmsVariant(),
            config.getHeadVariant(),
            config.getHelmetModel(),
            config.getChestArmorModel(),
            config.getLegArmorModel(),
            config.getBootArmorModel(),
            config.getCapeModel(),
            config.getMainHandModel(),
            config.getOffHandModel(),
            config.getSkinTexture(),
            config.getClothingTexture(),
            new HashMap<>(config.getArmorTextures()),
            config.isShowHair(),
            config.isShowEars(),
            config.isShowCape()
        );
    }

    /**
     * Apply this payload's data to a LayerConfiguration object.
     */
    public void applyToConfig(LayerConfiguration config) {
        config.setBodyVariant(bodyVariant);
        config.setLegsVariant(legsVariant);
        config.setArmsVariant(armsVariant);
        config.setHeadVariant(headVariant);
        config.setHelmetModel(helmetModel);
        config.setChestArmorModel(chestArmorModel);
        config.setLegArmorModel(legArmorModel);
        config.setBootArmorModel(bootArmorModel);
        config.setCapeModel(capeModel);
        config.setMainHandModel(mainHandModel);
        config.setOffHandModel(offHandModel);
        config.setSkinTexture(skinTexture);
        config.setClothingTexture(clothingTexture);
        config.setShowHair(showHair);
        config.setShowEars(showEars);
        config.setShowCape(showCape);

        // Clear and repopulate armor textures
        config.getArmorTextures().clear();
        config.getArmorTextures().putAll(armorTextures);
    }

    // Custom codec for nullable strings (equipment models can be null)
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

    // Custom codec for String maps (armor textures)
    private static final PacketCodec<RegistryByteBuf, Map<String, String>> STRING_MAP_CODEC = new PacketCodec<>() {
        @Override
        public Map<String, String> decode(RegistryByteBuf buf) {
            int size = buf.readVarInt();
            Map<String, String> map = new HashMap<>();
            for (int i = 0; i < size; i++) {
                String key = buf.readString();
                String value = buf.readString();
                map.put(key, value);
            }
            return map;
        }

        @Override
        public void encode(RegistryByteBuf buf, Map<String, String> map) {
            buf.writeVarInt(map.size());
            for (Map.Entry<String, String> entry : map.entrySet()) {
                buf.writeString(entry.getKey());
                buf.writeString(entry.getValue());
            }
        }
    };

    public static final PacketCodec<RegistryByteBuf, SyncLayerConfigPayload> CODEC = new PacketCodec<>() {
        @Override
        public SyncLayerConfigPayload decode(RegistryByteBuf buf) {
            UUID entityId = Uuids.PACKET_CODEC.decode(buf);
            int bodyVariant = PacketCodecs.VAR_INT.decode(buf);
            int legsVariant = PacketCodecs.VAR_INT.decode(buf);
            int armsVariant = PacketCodecs.VAR_INT.decode(buf);
            int headVariant = PacketCodecs.VAR_INT.decode(buf);
            String helmetModel = NULLABLE_STRING_CODEC.decode(buf);
            String chestArmorModel = NULLABLE_STRING_CODEC.decode(buf);
            String legArmorModel = NULLABLE_STRING_CODEC.decode(buf);
            String bootArmorModel = NULLABLE_STRING_CODEC.decode(buf);
            String capeModel = NULLABLE_STRING_CODEC.decode(buf);
            String mainHandModel = NULLABLE_STRING_CODEC.decode(buf);
            String offHandModel = NULLABLE_STRING_CODEC.decode(buf);
            String skinTexture = PacketCodecs.STRING.decode(buf);
            String clothingTexture = PacketCodecs.STRING.decode(buf);
            Map<String, String> armorTextures = STRING_MAP_CODEC.decode(buf);
            boolean showHair = PacketCodecs.BOOLEAN.decode(buf);
            boolean showEars = PacketCodecs.BOOLEAN.decode(buf);
            boolean showCape = PacketCodecs.BOOLEAN.decode(buf);

            return new SyncLayerConfigPayload(
                entityId, bodyVariant, legsVariant, armsVariant, headVariant,
                helmetModel, chestArmorModel, legArmorModel, bootArmorModel, capeModel,
                mainHandModel, offHandModel, skinTexture, clothingTexture, armorTextures,
                showHair, showEars, showCape
            );
        }

        @Override
        public void encode(RegistryByteBuf buf, SyncLayerConfigPayload payload) {
            Uuids.PACKET_CODEC.encode(buf, payload.entityId);
            PacketCodecs.VAR_INT.encode(buf, payload.bodyVariant);
            PacketCodecs.VAR_INT.encode(buf, payload.legsVariant);
            PacketCodecs.VAR_INT.encode(buf, payload.armsVariant);
            PacketCodecs.VAR_INT.encode(buf, payload.headVariant);
            NULLABLE_STRING_CODEC.encode(buf, payload.helmetModel);
            NULLABLE_STRING_CODEC.encode(buf, payload.chestArmorModel);
            NULLABLE_STRING_CODEC.encode(buf, payload.legArmorModel);
            NULLABLE_STRING_CODEC.encode(buf, payload.bootArmorModel);
            NULLABLE_STRING_CODEC.encode(buf, payload.capeModel);
            NULLABLE_STRING_CODEC.encode(buf, payload.mainHandModel);
            NULLABLE_STRING_CODEC.encode(buf, payload.offHandModel);
            PacketCodecs.STRING.encode(buf, payload.skinTexture);
            PacketCodecs.STRING.encode(buf, payload.clothingTexture);
            STRING_MAP_CODEC.encode(buf, payload.armorTextures);
            PacketCodecs.BOOLEAN.encode(buf, payload.showHair);
            PacketCodecs.BOOLEAN.encode(buf, payload.showEars);
            PacketCodecs.BOOLEAN.encode(buf, payload.showCape);
        }
    };

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
}
