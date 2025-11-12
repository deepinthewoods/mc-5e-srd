package ninja.trek.srd.network.payloads;

import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;
import net.minecraft.util.Uuids;
import net.minecraft.util.math.Vec3d;
import ninja.trek.srd.FiveESrdMod;

import java.util.UUID;

/**
 * Server-to-Client packet that synchronizes an entity's combat state.
 * Sent when combat state changes (action used, HP changed, etc.).
 */
public record SyncCombatStatePayload(
    UUID entityId,
    boolean inCombat,
    int initiative,
    Vec3d turnStartPosition,
    int remainingMovement,
    boolean hasAction,
    boolean hasBonusAction,
    boolean hasReaction,
    int armorClass,
    int currentHitPoints,
    int maxHitPoints
) implements CustomPayload {

    public static final Identifier ID_CONSTANT = Identifier.of(FiveESrdMod.MOD_ID, "sync_combat_state");
    public static final CustomPayload.Id<SyncCombatStatePayload> ID = new CustomPayload.Id<>(ID_CONSTANT);

    // Custom codec for Vec3d since there's no built-in one
    public static final PacketCodec<RegistryByteBuf, Vec3d> VEC3_CODEC = new PacketCodec<>() {
        @Override
        public Vec3d decode(RegistryByteBuf buf) {
            double x = buf.readDouble();
            double y = buf.readDouble();
            double z = buf.readDouble();
            return new Vec3d(x, y, z);
        }

        @Override
        public void encode(RegistryByteBuf buf, Vec3d vec) {
            buf.writeDouble(vec.x);
            buf.writeDouble(vec.y);
            buf.writeDouble(vec.z);
        }
    };

    public static final PacketCodec<RegistryByteBuf, SyncCombatStatePayload> CODEC = PacketCodec.tuple(
        Uuids.PACKET_CODEC, SyncCombatStatePayload::entityId,
        PacketCodecs.BOOLEAN, SyncCombatStatePayload::inCombat,
        PacketCodecs.VAR_INT, SyncCombatStatePayload::initiative,
        VEC3_CODEC, SyncCombatStatePayload::turnStartPosition,
        PacketCodecs.VAR_INT, SyncCombatStatePayload::remainingMovement,
        PacketCodecs.BOOLEAN, SyncCombatStatePayload::hasAction,
        PacketCodecs.BOOLEAN, SyncCombatStatePayload::hasBonusAction,
        PacketCodecs.BOOLEAN, SyncCombatStatePayload::hasReaction,
        PacketCodecs.VAR_INT, SyncCombatStatePayload::armorClass,
        PacketCodecs.VAR_INT, SyncCombatStatePayload::currentHitPoints,
        PacketCodecs.VAR_INT, SyncCombatStatePayload::maxHitPoints,
        SyncCombatStatePayload::new
    );

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
}
