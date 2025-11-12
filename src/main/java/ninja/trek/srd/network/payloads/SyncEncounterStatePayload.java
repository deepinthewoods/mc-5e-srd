package ninja.trek.srd.network.payloads;

import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Uuids;
import net.minecraft.util.Identifier;
import ninja.trek.srd.FiveESrdMod;
import ninja.trek.srd.combat.InitiativeTracker;

import java.util.List;
import java.util.UUID;

/**
 * Server-to-Client packet that synchronizes the full encounter state.
 * Sent when an encounter starts or when a player joins an ongoing encounter.
 */
public record SyncEncounterStatePayload(
    UUID encounterId,
    List<InitiativeEntry> turnOrder,
    int currentTurnIndex,
    int roundNumber,
    boolean ended
) implements CustomPayload {

    public static final Identifier ID_CONSTANT = Identifier.of(FiveESrdMod.MOD_ID, "sync_encounter_state");
    public static final CustomPayload.Id<SyncEncounterStatePayload> ID = new CustomPayload.Id<>(ID_CONSTANT);

    // Codec for a single initiative entry (entity ID, initiative roll, dex modifier)
    public static final PacketCodec<RegistryByteBuf, InitiativeEntry> INITIATIVE_ENTRY_CODEC = PacketCodec.tuple(
        Uuids.PACKET_CODEC, InitiativeEntry::entityId,
        PacketCodecs.VAR_INT, InitiativeEntry::initiativeRoll,
        PacketCodecs.VAR_INT, InitiativeEntry::dexModifier,
        InitiativeEntry::new
    );

    // Main codec for the payload
    public static final PacketCodec<RegistryByteBuf, SyncEncounterStatePayload> CODEC = PacketCodec.tuple(
        Uuids.PACKET_CODEC, SyncEncounterStatePayload::encounterId,
        INITIATIVE_ENTRY_CODEC.collect(PacketCodecs.toList()), SyncEncounterStatePayload::turnOrder,
        PacketCodecs.VAR_INT, SyncEncounterStatePayload::currentTurnIndex,
        PacketCodecs.VAR_INT, SyncEncounterStatePayload::roundNumber,
        PacketCodecs.BOOLEAN, SyncEncounterStatePayload::ended,
        SyncEncounterStatePayload::new
    );

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }

    /**
     * Creates a payload from an InitiativeTracker.
     */
    public static InitiativeEntry fromTracker(InitiativeTracker tracker) {
        return new InitiativeEntry(tracker.entityId(), tracker.initiativeRoll(), tracker.dexModifier());
    }

    /**
     * Converts an InitiativeEntry back to an InitiativeTracker.
     */
    public static InitiativeTracker toTracker(InitiativeEntry entry) {
        return new InitiativeTracker(entry.entityId(), entry.initiativeRoll(), entry.dexModifier());
    }

    /**
     * Simplified initiative entry for network transmission.
     */
    public record InitiativeEntry(UUID entityId, int initiativeRoll, int dexModifier) {}
}
