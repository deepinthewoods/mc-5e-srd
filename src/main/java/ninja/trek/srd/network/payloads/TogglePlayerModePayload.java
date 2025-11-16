package ninja.trek.srd.network.payloads;

import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;
import ninja.trek.srd.FiveESrdMod;

/**
 * Client-to-Server packet requesting to toggle between Player Mode and Party Mode.
 * Sent when the player presses the 'G' key.
 */
public record TogglePlayerModePayload() implements CustomPayload {

    public static final Identifier ID_CONSTANT = Identifier.of(FiveESrdMod.MOD_ID, "toggle_player_mode");
    public static final CustomPayload.Id<TogglePlayerModePayload> ID = new CustomPayload.Id<>(ID_CONSTANT);

    public static final PacketCodec<RegistryByteBuf, TogglePlayerModePayload> CODEC = PacketCodec.of(
        (value, buf) -> {}, // No data to write
        buf -> new TogglePlayerModePayload() // No data to read
    );

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
}
