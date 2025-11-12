package ninja.trek.srd.network.payloads;

import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;
import ninja.trek.srd.FiveESrdMod;

/**
 * Client-to-Server packet requesting to end the current player's turn.
 * Sent when a player clicks the "End Turn" button.
 */
public record EndTurnPayload() implements CustomPayload {

    public static final Identifier ID_CONSTANT = Identifier.of(FiveESrdMod.MOD_ID, "end_turn");
    public static final CustomPayload.Id<EndTurnPayload> ID = new CustomPayload.Id<>(ID_CONSTANT);

    // Empty payload codec (no data to transmit)
    public static final PacketCodec<RegistryByteBuf, EndTurnPayload> CODEC = PacketCodec.unit(new EndTurnPayload());

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
}
