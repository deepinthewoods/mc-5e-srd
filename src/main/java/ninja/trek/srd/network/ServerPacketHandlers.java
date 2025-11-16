package ninja.trek.srd.network;

import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.entity.Entity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import ninja.trek.srd.FiveESrdMod;
import ninja.trek.srd.character.entity.CharacterEntity;
import ninja.trek.srd.combat.*;
import ninja.trek.srd.network.payloads.CreateCharacterPayload;
import ninja.trek.srd.network.payloads.EndTurnPayload;
import ninja.trek.srd.network.payloads.SyncCombatStatePayload;
import ninja.trek.srd.network.payloads.TurnEndPayload;
import ninja.trek.srd.network.payloads.TurnStartPayload;
import ninja.trek.srd.network.payloads.UseActionPayload;
import ninja.trek.srd.util.DiceRoller;

import java.util.UUID;

/**
 * Handles incoming packets from clients on the server side.
 */
public class ServerPacketHandlers {

    /**
     * Register all server-side packet receivers.
     */
    public static void register() {
        FiveESrdMod.LOGGER.info("Registering server packet handlers...");

        // Handle UseAction packets
        ServerPlayNetworking.registerGlobalReceiver(UseActionPayload.ID, ServerPacketHandlers::handleUseAction);

        // Handle EndTurn packets
        ServerPlayNetworking.registerGlobalReceiver(EndTurnPayload.ID, ServerPacketHandlers::handleEndTurn);

        // Handle CreateCharacter packets
        ServerPlayNetworking.registerGlobalReceiver(CreateCharacterPayload.ID, ServerPacketHandlers::handleCreateCharacter);

        // Handle TogglePlayerMode packets
        ServerPlayNetworking.registerGlobalReceiver(TogglePlayerModePayload.ID, ServerPacketHandlers::handleTogglePlayerMode);

        // Handle SelectCharacter packets
        ServerPlayNetworking.registerGlobalReceiver(SelectCharacterPayload.ID, ServerPacketHandlers::handleSelectCharacter);

        // Handle DeleteCharacter packets
        ServerPlayNetworking.registerGlobalReceiver(DeleteCharacterPayload.ID, ServerPacketHandlers::handleDeleteCharacter);

        FiveESrdMod.LOGGER.info("Server packet handlers registered successfully!");
    }

    /**
     * Handle UseAction packet from client.
     */
    private static void handleUseAction(UseActionPayload payload, ServerPlayNetworking.Context context) {
        ServerPlayerEntity player = context.player();

        // Execute on server thread
        context.server().execute(() -> {
            // Validate that it's the player's turn
            EncounterManager manager = EncounterManager.getInstance(context.server());
            EncounterState encounter = manager.getEncounterForEntity(player.getUuid());
            if (encounter == null) {
                FiveESrdMod.LOGGER.warn("Player {} tried to use action but is not in combat", player.getName().getString());
                return;
            }

            UUID currentTurnEntity = encounter.getCurrentTurnEntity();
            if (currentTurnEntity == null || !currentTurnEntity.equals(player.getUuid())) {
                FiveESrdMod.LOGGER.warn("Player {} tried to use action but it's not their turn", player.getName().getString());
                return;
            }

            // Get the acting entity (could be player or a character entity)
            Entity actingEntity = context.server().getOverworld().getEntity(player.getUuid());
            if (actingEntity == null) {
                FiveESrdMod.LOGGER.error("Could not find acting entity for player {}", player.getName().getString());
                return;
            }

            // Execute action based on type
            switch (payload.actionType()) {
                case ATTACK -> handleAttackAction(payload, actingEntity, encounter, manager, context.server());
                case DASH -> handleDashAction(actingEntity, encounter, manager, context.server());
                case DISENGAGE -> handleDisengageAction(actingEntity, encounter, manager, context.server());
                case DODGE -> handleDodgeAction(actingEntity, encounter, manager, context.server());
                default -> FiveESrdMod.LOGGER.warn("Unimplemented action type: {}", payload.actionType());
            }
        });
    }

    /**
     * Handle an attack action.
     */
    private static void handleAttackAction(UseActionPayload payload, Entity attacker, EncounterState encounter,
                                           EncounterManager manager, net.minecraft.server.MinecraftServer server) {
        // Only CharacterEntity can perform attacks (for now)
        if (!(attacker instanceof CharacterEntity attackerCharacter)) {
            FiveESrdMod.LOGGER.warn("Non-character entity tried to attack: {}", attacker.getType());
            return;
        }

        // Check if attacker has an action available
        CombatState attackerState = attackerCharacter.getCombatState();
        if (!attackerState.hasAction()) {
            FiveESrdMod.LOGGER.warn("Character {} tried to attack but has no action available", attackerCharacter.getName().getString());
            return;
        }

        // Get target entity
        if (payload.targetEntityId().isEmpty()) {
            FiveESrdMod.LOGGER.warn("Attack action requires a target");
            return;
        }

        UUID targetId = payload.targetEntityId().get();
        Entity targetEntity = server.getOverworld().getEntity(targetId);
        if (!(targetEntity instanceof CharacterEntity target)) {
            FiveESrdMod.LOGGER.warn("Invalid attack target: {}", targetId);
            return;
        }

        // Perform the attack using 5e rules
        DiceRoller roller = new DiceRoller(attackerCharacter.getEntityWorld().getRandom());
        CombatResolver resolver = new CombatResolver(roller);

        Weapon weapon = attackerCharacter.getEquippedWeapon();
        CombatState targetState = target.getCombatState();

        // Check if target is dodging (gives disadvantage to attackers)
        boolean disadvantage = targetState.isDodging();

        AttackResult result = resolver.performAttack(
            weapon,
            attackerCharacter.getStats(),
            attackerCharacter.getLevel(),
            targetState.armorClass(),
            true,  // TODO: Check weapon proficiency based on class
            false, // TODO: Check for advantage
            disadvantage
        );

        FiveESrdMod.LOGGER.info("Attack result: {}", result.description());

        // Consume the action
        attackerCharacter.setCombatState(attackerState.useAction());

        // Apply damage if hit
        if (result.isHit()) {
            CombatState newTargetState = targetState.takeDamage(result.damageDealt(), result.isCritical());
            target.setCombatState(newTargetState);

            // Broadcast damage to all clients
            manager.syncCombatState(server, targetId, newTargetState);

            // Check for death
            if (newTargetState.currentHitPoints() <= 0) {
                handleEntityDeath(target, encounter, manager, server);
            }
        }

        // Broadcast attack result as a chat message to encounter participants
        Text attackMessage = Text.literal(result.description());
        for (var tracker : encounter.getTurnOrder()) {
            Entity participant = server.getOverworld().getEntity(tracker.entityId());
            if (participant instanceof ServerPlayerEntity playerEntity) {
                playerEntity.sendMessage(attackMessage, false);
            }
        }
    }

    /**
     * Handle Dash action (double movement for this turn).
     */
    private static void handleDashAction(Entity entity, EncounterState encounter,
                                         EncounterManager manager, net.minecraft.server.MinecraftServer server) {
        if (!(entity instanceof CharacterEntity character)) {
            return;
        }

        CombatState state = character.getCombatState();
        if (!state.hasAction()) {
            FiveESrdMod.LOGGER.warn("Character {} tried to Dash but has no action available", character.getName().getString());
            return;
        }

        // Double remaining movement
        int currentMovement = state.remainingMovement();
        CombatState newState = state.useAction().withRemainingMovement(currentMovement * 2);
        character.setCombatState(newState);

        FiveESrdMod.LOGGER.info("Character {} used Dash action", character.getName().getString());
    }

    /**
     * Handle Disengage action (movement doesn't provoke opportunity attacks).
     */
    private static void handleDisengageAction(Entity entity, EncounterState encounter,
                                              EncounterManager manager, net.minecraft.server.MinecraftServer server) {
        if (!(entity instanceof CharacterEntity character)) {
            return;
        }

        CombatState state = character.getCombatState();
        if (!state.hasAction()) {
            FiveESrdMod.LOGGER.warn("Character {} tried to Disengage but has no action available", character.getName().getString());
            return;
        }

        // Set disengaged flag and consume action
        CombatState newState = state.useAction().setDisengaged(true);
        character.setCombatState(newState);
        manager.syncCombatState(server, character.getUuid(), newState);

        FiveESrdMod.LOGGER.info("Character {} used Disengage action", character.getName().getString());

        // Broadcast to encounter participants
        Text message = Text.literal(character.getName().getString() + " takes the Disengage action!");
        for (var tracker : encounter.getTurnOrder()) {
            Entity participant = server.getOverworld().getEntity(tracker.entityId());
            if (participant instanceof ServerPlayerEntity playerEntity) {
                playerEntity.sendMessage(message, false);
            }
        }
    }

    /**
     * Handle Dodge action (attacks against you have disadvantage).
     */
    private static void handleDodgeAction(Entity entity, EncounterState encounter,
                                          EncounterManager manager, net.minecraft.server.MinecraftServer server) {
        if (!(entity instanceof CharacterEntity character)) {
            return;
        }

        CombatState state = character.getCombatState();
        if (!state.hasAction()) {
            FiveESrdMod.LOGGER.warn("Character {} tried to Dodge but has no action available", character.getName().getString());
            return;
        }

        // Set dodging flag and consume action
        CombatState newState = state.useAction().setDodging(true);
        character.setCombatState(newState);
        manager.syncCombatState(server, character.getUuid(), newState);

        FiveESrdMod.LOGGER.info("Character {} used Dodge action", character.getName().getString());

        // Broadcast to encounter participants
        Text message = Text.literal(character.getName().getString() + " takes the Dodge action!");
        for (var tracker : encounter.getTurnOrder()) {
            Entity participant = server.getOverworld().getEntity(tracker.entityId());
            if (participant instanceof ServerPlayerEntity playerEntity) {
                playerEntity.sendMessage(message, false);
            }
        }
    }

    /**
     * Handle entity death in combat.
     */
    private static void handleEntityDeath(CharacterEntity deadEntity, EncounterState encounter,
                                          EncounterManager manager, net.minecraft.server.MinecraftServer server) {
        FiveESrdMod.LOGGER.info("Character {} has fallen unconscious/died", deadEntity.getName().getString());

        // TODO: Implement proper death saving throws and unconscious state
        // For now, just remove from encounter and kill the entity
        manager.removeCombatant(deadEntity.getUuid());

        // Broadcast death message
        Text deathMessage = Text.literal(deadEntity.getName().getString() + " has fallen!");
        for (var tracker : encounter.getTurnOrder()) {
            Entity participant = server.getOverworld().getEntity(tracker.entityId());
            if (participant instanceof ServerPlayerEntity playerEntity) {
                playerEntity.sendMessage(deathMessage, false);
            }
        }

        // Check if encounter should end
        // TODO: Implement proper encounter end conditions
    }

    /**
     * Handle EndTurn packet from client.
     */
    private static void handleEndTurn(EndTurnPayload payload, ServerPlayNetworking.Context context) {
        ServerPlayerEntity player = context.player();

        // Execute on server thread
        context.server().execute(() -> {
            EncounterManager manager = EncounterManager.getInstance(context.server());
            EncounterState encounter = manager.getEncounterForEntity(player.getUuid());

            if (encounter == null) {
                FiveESrdMod.LOGGER.warn("Player {} tried to end turn but is not in combat", player.getName().getString());
                return;
            }

            UUID currentTurnEntity = encounter.getCurrentTurnEntity();
            if (currentTurnEntity == null || !currentTurnEntity.equals(player.getUuid())) {
                FiveESrdMod.LOGGER.warn("Player {} tried to end turn but it's not their turn", player.getName().getString());
                return;
            }

            FiveESrdMod.LOGGER.info("Player {} ended their turn", player.getName().getString());

            // Send turn end notification
            TurnEndPayload turnEndPayload = new TurnEndPayload(encounter.getEncounterId(), player.getUuid());
            manager.broadcastToEncounter(context.server(), encounter.getEncounterId(), turnEndPayload);

            // Advance to next turn
            manager.advanceTurn(context.server(), encounter.getEncounterId());

            // Send turn start notification for next entity
            UUID nextEntityId = encounter.getCurrentTurnEntity();
            if (nextEntityId != null) {
                TurnStartPayload turnStartPayload = new TurnStartPayload(encounter.getEncounterId(), nextEntityId);
                manager.broadcastToEncounter(context.server(), encounter.getEncounterId(), turnStartPayload);

                // Handle turn start for the next entity
                Entity nextEntity = context.server().getOverworld().getEntity(nextEntityId);
                if (nextEntity instanceof CharacterEntity character) {
                    if (character.isPlayerControlled()) {
                        // Auto-possess player-owned character on their turn
                        UUID ownerUUID = character.getOwnerUUID();
                        if (ownerUUID != null) {
                            ServerPlayerEntity owner = context.server().getPlayerManager().getPlayer(ownerUUID);
                            if (owner != null) {
                                ninja.trek.srd.character.management.PossessionManager.getInstance()
                                    .possessCharacter(context.server(), owner, character);
                            }
                        }
                    } else {
                        // Schedule AI turn to execute after a short delay
                        context.server().execute(() -> {
                            character.executeAITurn(context.server(), encounter);
                        });
                    }
                }
            }
        });
    }

    /**
     * Handle CreateCharacter packet from client.
     */
    private static void handleCreateCharacter(CreateCharacterPayload payload, ServerPlayNetworking.Context context) {
        ServerPlayerEntity player = context.player();

        // Execute on server thread
        context.server().execute(() -> {
            FiveESrdMod.LOGGER.info("Creating character '{}' for player {}", payload.name(), player.getName().getString());

            // Create character entity in the player's world
            CharacterEntity character = new CharacterEntity(
                ninja.trek.srd.registry.ModEntities.CHARACTER,
                player.getEntityWorld()
            );

            // Initialize character data
            character.initializeCharacter(
                payload.race(),
                payload.characterClass(),
                payload.stats(),
                payload.appearance()
            );

            // Set as player-controlled and assign owner
            character.setPlayerControlled(true);
            character.setOwnerUUID(player.getUuid());

            // Set custom name
            character.setCustomName(net.minecraft.text.Text.literal(payload.name()));
            character.setCustomNameVisible(true);

            // Position the character near the player
            double offsetX = player.getX() + 2.0;
            double offsetZ = player.getZ();
            character.refreshPositionAndAngles(offsetX, player.getY(), offsetZ, player.getYaw(), 0);

            // Spawn the entity in the world
            if (player.getEntityWorld() instanceof net.minecraft.server.world.ServerWorld serverWorld) {
                serverWorld.spawnEntity(character);
            }

            // Register character with CharacterManager
            ninja.trek.srd.character.management.CharacterManager.getInstance()
                .registerCharacter(player.getUuid(), character);

            FiveESrdMod.LOGGER.info("Character '{}' created successfully at ({}, {}, {})",
                payload.name(), offsetX, player.getY(), offsetZ);
        });
    }

    /**
     * Handle TogglePlayerMode packet from client.
     */
    private static void handleTogglePlayerMode(TogglePlayerModePayload payload, ServerPlayNetworking.Context context) {
        ServerPlayerEntity player = context.player();

        // Execute on server thread
        context.server().execute(() -> {
            FiveESrdMod.LOGGER.info("Player {} toggling player mode", player.getName().getString());

            ninja.trek.srd.character.management.PossessionManager.getInstance()
                .toggleMode(context.server(), player);
        });
    }

    /**
     * Handle SelectCharacter packet from client.
     */
    private static void handleSelectCharacter(SelectCharacterPayload payload, ServerPlayNetworking.Context context) {
        ServerPlayerEntity player = context.player();

        // Execute on server thread
        context.server().execute(() -> {
            // Verify that the player owns the character
            if (!ninja.trek.srd.character.management.CharacterManager.getInstance()
                .ownsCharacter(player.getUuid(), payload.characterUUID())) {
                FiveESrdMod.LOGGER.warn("Player {} tried to select character {} that they don't own",
                    player.getName().getString(), payload.characterUUID());
                return;
            }

            // Find the character entity
            Entity entity = context.server().getOverworld().getEntity(payload.characterUUID());
            if (!(entity instanceof CharacterEntity character)) {
                FiveESrdMod.LOGGER.warn("Character {} not found", payload.characterUUID());
                return;
            }

            FiveESrdMod.LOGGER.info("Player {} selecting character {}",
                player.getName().getString(), character.getName().getString());

            // Possess the character
            ninja.trek.srd.character.management.PossessionManager.getInstance()
                .possessCharacter(context.server(), player, character);
        });
    }

    /**
     * Handle DeleteCharacter packet from client.
     */
    private static void handleDeleteCharacter(DeleteCharacterPayload payload, ServerPlayNetworking.Context context) {
        ServerPlayerEntity player = context.player();

        // Execute on server thread
        context.server().execute(() -> {
            // Verify that the player owns the character
            if (!ninja.trek.srd.character.management.CharacterManager.getInstance()
                .ownsCharacter(player.getUuid(), payload.characterUUID())) {
                FiveESrdMod.LOGGER.warn("Player {} tried to delete character {} that they don't own",
                    player.getName().getString(), payload.characterUUID());
                return;
            }

            // Find the character entity
            Entity entity = context.server().getOverworld().getEntity(payload.characterUUID());
            if (!(entity instanceof CharacterEntity character)) {
                FiveESrdMod.LOGGER.warn("Character {} not found for deletion", payload.characterUUID());
                return;
            }

            FiveESrdMod.LOGGER.info("Player {} deleting character {}",
                player.getName().getString(), character.getName().getString());

            // Notify possession manager (will handle switching to another character if needed)
            ninja.trek.srd.character.management.PossessionManager.getInstance()
                .onCharacterRemoved(context.server(), character.getUuid());

            // Unregister from character manager
            ninja.trek.srd.character.management.CharacterManager.getInstance()
                .unregisterCharacter(character.getUuid());

            // Remove from world
            character.discard();
        });
    }
}
