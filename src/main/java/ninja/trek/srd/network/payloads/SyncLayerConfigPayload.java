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

    public static final PacketCodec<RegistryByteBuf, SyncLayerConfigPayload> CODEC = PacketCodec.tuple(
        Uuids.PACKET_CODEC, SyncLayerConfigPayload::entityId,
        PacketCodecs.VAR_INT, SyncLayerConfigPayload::bodyVariant,
        PacketCodecs.VAR_INT, SyncLayerConfigPayload::legsVariant,
        PacketCodecs.VAR_INT, SyncLayerConfigPayload::armsVariant,
        PacketCodecs.VAR_INT, SyncLayerConfigPayload::headVariant,
        NULLABLE_STRING_CODEC, SyncLayerConfigPayload::helmetModel,
        NULLABLE_STRING_CODEC, SyncLayerConfigPayload::chestArmorModel,
        NULLABLE_STRING_CODEC, SyncLayerConfigPayload::legArmorModel,
        NULLABLE_STRING_CODEC, SyncLayerConfigPayload::bootArmorModel,
        NULLABLE_STRING_CODEC, SyncLayerConfigPayload::capeModel,
        NULLABLE_STRING_CODEC, SyncLayerConfigPayload::mainHandModel,
        NULLABLE_STRING_CODEC, SyncLayerConfigPayload::offHandModel,
        PacketCodecs.STRING, SyncLayerConfigPayload::skinTexture,
        PacketCodecs.STRING, SyncLayerConfigPayload::clothingTexture,
        STRING_MAP_CODEC, SyncLayerConfigPayload::armorTextures,
        PacketCodecs.BOOLEAN, SyncLayerConfigPayload::showHair,
        PacketCodecs.BOOLEAN, SyncLayerConfigPayload::showEars,
        PacketCodecs.BOOLEAN, SyncLayerConfigPayload::showCape,
        SyncLayerConfigPayload::new
    );

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
}
