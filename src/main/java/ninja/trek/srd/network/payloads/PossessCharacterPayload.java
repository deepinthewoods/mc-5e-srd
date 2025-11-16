package ninja.trek.srd.network.payloads;

import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.s2c.common.CustomPayloadS2CPacket;
import net.minecraft.util.Identifier;
import net.minecraft.util.Uuids;
import ninja.trek.srd.FiveESrdMod;

import java.util.UUID;

/**
 * Server-to-Client packet instructing the client to possess a character.
 * This switches the camera and input to the specified character.
 */
public record PossessCharacterPayload(
    UUID characterUUID
) implements CustomPayload {

    public static final Identifier ID_CONSTANT = Identifier.of(FiveESrdMod.MOD_ID, "possess_character");
    public static final CustomPayload.Id<PossessCharacterPayload> ID = new CustomPayload.Id<>(ID_CONSTANT);

    public static final PacketCodec<RegistryByteBuf, PossessCharacterPayload> CODEC = PacketCodec.tuple(
        Uuids.PACKET_CODEC, PossessCharacterPayload::characterUUID,
        PossessCharacterPayload::new
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
