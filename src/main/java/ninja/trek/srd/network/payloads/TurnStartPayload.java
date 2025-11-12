package ninja.trek.srd.network.payloads;

import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;
import net.minecraft.util.Uuids;
import ninja.trek.srd.FiveESrdMod;

import java.util.UUID;

/**
 * Server-to-Client packet notifying that a turn has started for a specific entity.
 * Sent at the beginning of each entity's turn.
 */
public record TurnStartPayload(
    UUID encounterId,
    UUID entityId
) implements CustomPayload {

    public static final Identifier ID_CONSTANT = Identifier.of(FiveESrdMod.MOD_ID, "turn_start");
    public static final CustomPayload.Id<TurnStartPayload> ID = new CustomPayload.Id<>(ID_CONSTANT);

    public static final PacketCodec<RegistryByteBuf, TurnStartPayload> CODEC = PacketCodec.tuple(
        Uuids.PACKET_CODEC, TurnStartPayload::encounterId,
        Uuids.PACKET_CODEC, TurnStartPayload::entityId,
        TurnStartPayload::new
    );

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
}
