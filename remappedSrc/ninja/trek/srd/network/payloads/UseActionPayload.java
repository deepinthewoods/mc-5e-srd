package ninja.trek.srd.network.payloads;

import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;
import net.minecraft.util.Uuids;
import ninja.trek.srd.FiveESrdMod;

import java.util.Optional;
import java.util.UUID;

/**
 * Client-to-Server packet requesting to use an action.
 * Sent when a player clicks an action button in the combat UI.
 */
public record UseActionPayload(
    ActionType actionType,
    Optional<UUID> targetEntityId
) implements CustomPayload {

    public static final Identifier ID_CONSTANT = Identifier.of(FiveESrdMod.MOD_ID, "use_action");
    public static final CustomPayload.Id<UseActionPayload> ID = new CustomPayload.Id<>(ID_CONSTANT);

    // Codec for ActionType enum
    public static final PacketCodec<RegistryByteBuf, ActionType> ACTION_TYPE_CODEC =
        PacketCodecs.indexed(ActionType::values, ActionType::ordinal);

    // Codec for Optional<UUID>
    public static final PacketCodec<RegistryByteBuf, Optional<UUID>> OPTIONAL_UUID_CODEC =
        Uuids.PACKET_CODEC.collect(PacketCodecs::optional);

    public static final PacketCodec<RegistryByteBuf, UseActionPayload> CODEC = PacketCodec.tuple(
        ACTION_TYPE_CODEC, UseActionPayload::actionType,
        OPTIONAL_UUID_CODEC, UseActionPayload::targetEntityId,
        UseActionPayload::new
    );

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }

    /**
     * Types of actions that can be performed.
     */
    public enum ActionType {
        // Standard Actions
        ATTACK,
        DASH,
        DISENGAGE,
        DODGE,
        HELP,
        HIDE,
        READY,
        SEARCH,
        USE_OBJECT,

        // Bonus Actions
        OFFHAND_ATTACK,

        // Reactions
        OPPORTUNITY_ATTACK
    }
}
