package ninja.trek.srd.network.payloads;

import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;
import net.minecraft.util.Uuids;
import ninja.trek.srd.FiveESrdMod;

import java.util.UUID;

/**
 * Client-to-Server packet requesting to possess a specific character.
 * Sent when the player clicks on a character in the character list.
 */
public record SelectCharacterPayload(
    UUID characterUUID
) implements CustomPayload {

    public static final Identifier ID_CONSTANT = Identifier.of(FiveESrdMod.MOD_ID, "select_character");
    public static final CustomPayload.Id<SelectCharacterPayload> ID = new CustomPayload.Id<>(ID_CONSTANT);

    public static final PacketCodec<RegistryByteBuf, SelectCharacterPayload> CODEC = PacketCodec.tuple(
        Uuids.PACKET_CODEC, SelectCharacterPayload::characterUUID,
        SelectCharacterPayload::new
    );

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
}
