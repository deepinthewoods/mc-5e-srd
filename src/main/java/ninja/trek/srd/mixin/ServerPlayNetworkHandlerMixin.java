package ninja.trek.srd.mixin;

import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.Vec3d;
import ninja.trek.srd.combat.CombatState;
import ninja.trek.srd.combat.EncounterManager;
import ninja.trek.srd.combat.EncounterState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.UUID;

/**
 * Enforces turn-based combat movement for real players by validating
 * movement packets against the server-authoritative combat state.
 */
@Mixin(ServerPlayNetworkHandler.class)
public abstract class ServerPlayNetworkHandlerMixin {

    @Shadow public ServerPlayerEntity player;

    @Shadow public abstract void requestTeleport(double x, double y, double z, float yaw, float pitch);

    @Inject(method = "onPlayerMove", at = @At("HEAD"), cancellable = true)
    private void srd$validateCombatMovement(PlayerMoveC2SPacket packet, CallbackInfo ci) {
        if (player == null || player.isSpectator()) {
            return;
        }

        if (!(player.getEntityWorld() instanceof ServerWorld serverWorld)) {
            return;
        }
        MinecraftServer server = serverWorld.getServer();

        // Handle possession - redirect movement to possessed character
        ninja.trek.srd.character.management.PossessionManager possessionManager =
            ninja.trek.srd.character.management.PossessionManager.getInstance();
        UUID possessedCharacterUUID = possessionManager.getPossessedCharacter(player.getUuid());

        if (possessedCharacterUUID != null) {
            // Player is possessing a character - redirect movement to character
            net.minecraft.entity.Entity entity = server.getOverworld().getEntity(possessedCharacterUUID);
            if (entity instanceof ninja.trek.srd.character.entity.CharacterEntity character) {
                // Apply rotation from packet to character
                if (packet.changesLook()) {
                    character.setYaw(packet.getYaw(character.getYaw()));
                    character.setPitch(packet.getPitch(character.getPitch()));
                    character.headYaw = packet.getYaw(character.headYaw);
                }

                // Apply position from packet to character
                if (packet.changesPosition()) {
                    Vec3d currentPos = new Vec3d(character.getX(), character.getY(), character.getZ());
                    Vec3d targetPos = new Vec3d(
                        packet.getX(currentPos.x),
                        packet.getY(currentPos.y),
                        packet.getZ(currentPos.z)
                    );
                    character.setPosition(targetPos);
                }

                // Keep player frozen at character's position
                requestTeleport(character.getX(), character.getY(), character.getZ(),
                    character.getYaw(), character.getPitch());
                player.setVelocity(Vec3d.ZERO);

                ci.cancel();
                return;
            }
        }

        EncounterManager manager = EncounterManager.getInstance(server);
        EncounterState encounter = manager.getEncounterForEntity(player.getUuid());
        if (encounter == null) {
            return;
        }

        CombatState combatState = manager.getCombatState(server, player.getUuid());
        if (combatState == null || !combatState.inCombat()) {
            return;
        }

        UUID currentTurn = encounter.getCurrentTurnEntity();
        if (currentTurn == null || !currentTurn.equals(player.getUuid())) {
            revertPlayerPosition(packet);
            ci.cancel();
            return;
        }

        Vec3d currentPos = new Vec3d(player.getX(), player.getY(), player.getZ());
        Vec3d targetPos = new Vec3d(
            packet.getX(currentPos.x),
            packet.getY(currentPos.y),
            packet.getZ(currentPos.z)
        );

        double distance = targetPos.distanceTo(currentPos);
        if (distance < 1.0E-3) {
            return;
        }

        int remainingMovement = combatState.remainingMovement();
        if (remainingMovement <= 0) {
            revertPlayerPosition(packet);
            ci.cancel();
            return;
        }

        double allowedDistance = Math.min(distance, remainingMovement);
        int spentMovement = (int) Math.ceil(allowedDistance);
        if (spentMovement <= 0) {
            return;
        }

        CombatState updatedState = combatState.withRemainingMovement(Math.max(0, remainingMovement - spentMovement));
        manager.syncCombatState(server, player.getUuid(), updatedState);

        if (distance > remainingMovement + 1.0E-4) {
            Vec3d direction = targetPos.subtract(currentPos).normalize();
            Vec3d clampedPos = currentPos.add(direction.multiply(allowedDistance));
            forcePlayerPosition(packet, clampedPos);
            ci.cancel();
        }
    }

    private void revertPlayerPosition(PlayerMoveC2SPacket packet) {
        Vec3d pos = new Vec3d(player.getX(), player.getY(), player.getZ());
        float yaw = packet.getYaw(player.getYaw());
        float pitch = packet.getPitch(player.getPitch());
        requestTeleport(pos.x, pos.y, pos.z, yaw, pitch);
        player.setVelocity(Vec3d.ZERO);
    }

    private void forcePlayerPosition(PlayerMoveC2SPacket packet, Vec3d position) {
        float yaw = packet.getYaw(player.getYaw());
        float pitch = packet.getPitch(player.getPitch());
        requestTeleport(position.x, position.y, position.z, yaw, pitch);
        player.setVelocity(Vec3d.ZERO);
    }
}
