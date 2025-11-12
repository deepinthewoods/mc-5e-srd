package ninja.trek.srd.network.payloads;

import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;
import net.minecraft.util.Uuids;
import ninja.trek.srd.FiveESrdMod;

import java.util.UUID;

/**
 * Server-to-Client packet notifying that a turn has ended for a specific entity.
 * Sent when an entity's turn ends (either manually or automatically).
 */
public record TurnEndPayload(
    UUID encounterId,
    UUID entityId
) implements CustomPayload {

    public static final Identifier ID_CONSTANT = Identifier.of(FiveESrdMod.MOD_ID, "turn_end");
    public static final CustomPayload.Id<TurnEndPayload> ID = new CustomPayload.Id<>(ID_CONSTANT);

    public static final PacketCodec<RegistryByteBuf, TurnEndPayload> CODEC = PacketCodec.tuple(
        Uuids.PACKET_CODEC, TurnEndPayload::encounterId,
        Uuids.PACKET_CODEC, TurnEndPayload::entityId,
        TurnEndPayload::new
    );

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
}
