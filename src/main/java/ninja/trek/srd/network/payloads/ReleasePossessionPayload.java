package ninja.trek.srd.network.payloads;

import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.s2c.common.CustomPayloadS2CPacket;
import net.minecraft.util.Identifier;
import ninja.trek.srd.FiveESrdMod;

/**
 * Server-to-Client packet instructing the client to release possession.
 * This switches control back to the player or releases camera control.
 */
public record ReleasePossessionPayload() implements CustomPayload {

    public static final Identifier ID_CONSTANT = Identifier.of(FiveESrdMod.MOD_ID, "release_possession");
    public static final CustomPayload.Id<ReleasePossessionPayload> ID = new CustomPayload.Id<>(ID_CONSTANT);

    public static final PacketCodec<RegistryByteBuf, ReleasePossessionPayload> CODEC = PacketCodec.of(
        (value, buf) -> {}, // No data to write
        buf -> new ReleasePossessionPayload() // No data to read
    );

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }

    /**
     * Create a packet from this payload.
     */
    public Packet<?> createPacket() {
        return new CustomPayloadS2CPacket(this);
    }
}
