package ninja.trek.srd.network.payloads;

import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;
import net.minecraft.util.Uuids;
import ninja.trek.srd.FiveESrdMod;

import java.util.UUID;

/**
 * Client-to-Server packet requesting to delete a character.
 */
public record DeleteCharacterPayload(
    UUID characterUUID
) implements CustomPayload {

    public static final Identifier ID_CONSTANT = Identifier.of(FiveESrdMod.MOD_ID, "delete_character");
    public static final CustomPayload.Id<DeleteCharacterPayload> ID = new CustomPayload.Id<>(ID_CONSTANT);

    public static final PacketCodec<RegistryByteBuf, DeleteCharacterPayload> CODEC = PacketCodec.tuple(
        Uuids.PACKET_CODEC, DeleteCharacterPayload::characterUUID,
        DeleteCharacterPayload::new
    );

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
}
