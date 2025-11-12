package ninja.trek.srd.network.payloads;

import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;
import ninja.trek.srd.FiveESrdMod;
import ninja.trek.srd.character.data.CharacterAppearance;
import ninja.trek.srd.character.data.CharacterClass;
import ninja.trek.srd.character.data.CharacterStats;
import ninja.trek.srd.character.data.Race;

/**
 * Client-to-Server packet requesting to create a new character entity.
 * Sent when a player completes the character creation screen.
 */
public record CreateCharacterPayload(
    String name,
    Race race,
    CharacterClass characterClass,
    CharacterStats stats,
    CharacterAppearance appearance
) implements CustomPayload {

    public static final Identifier ID_CONSTANT = Identifier.of(FiveESrdMod.MOD_ID, "create_character");
    public static final CustomPayload.Id<CreateCharacterPayload> ID = new CustomPayload.Id<>(ID_CONSTANT);

    // Codec for Race enum
    public static final PacketCodec<RegistryByteBuf, Race> RACE_CODEC =
        PacketCodecs.indexed(i -> Race.values()[i], Race::ordinal).cast();

    // Codec for CharacterClass enum
    public static final PacketCodec<RegistryByteBuf, CharacterClass> CLASS_CODEC =
        PacketCodecs.indexed(i -> CharacterClass.values()[i], CharacterClass::ordinal).cast();

    // Codec for CharacterStats
    public static final PacketCodec<RegistryByteBuf, CharacterStats> STATS_CODEC = PacketCodec.tuple(
        PacketCodecs.VAR_INT, CharacterStats::strength,
        PacketCodecs.VAR_INT, CharacterStats::dexterity,
        PacketCodecs.VAR_INT, CharacterStats::constitution,
        PacketCodecs.VAR_INT, CharacterStats::intelligence,
        PacketCodecs.VAR_INT, CharacterStats::wisdom,
        PacketCodecs.VAR_INT, CharacterStats::charisma,
        CharacterStats::new
    );

    // Codec for CharacterAppearance
    public static final PacketCodec<RegistryByteBuf, CharacterAppearance> APPEARANCE_CODEC = PacketCodec.tuple(
        PacketCodecs.VAR_INT, CharacterAppearance::bodyIndex,
        PacketCodecs.VAR_INT, CharacterAppearance::legsIndex,
        PacketCodecs.VAR_INT, CharacterAppearance::armsIndex,
        PacketCodecs.VAR_INT, CharacterAppearance::headIndex,
        CharacterAppearance::new
    );

    public static final PacketCodec<RegistryByteBuf, CreateCharacterPayload> CODEC = PacketCodec.tuple(
        PacketCodecs.STRING, CreateCharacterPayload::name,
        RACE_CODEC, CreateCharacterPayload::race,
        CLASS_CODEC, CreateCharacterPayload::characterClass,
        STATS_CODEC, CreateCharacterPayload::stats,
        APPEARANCE_CODEC, CreateCharacterPayload::appearance,
        CreateCharacterPayload::new
    );

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
}
