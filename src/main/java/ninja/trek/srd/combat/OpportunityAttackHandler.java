package ninja.trek.srd.combat;

import net.minecraft.entity.Entity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.math.Vec3d;
import ninja.trek.srd.FiveESrdMod;
import ninja.trek.srd.character.entity.CharacterEntity;
import ninja.trek.srd.util.DiceRoller;

import java.util.*;

/**
 * Handles opportunity attack detection and execution.
 * Opportunity attacks occur when an entity leaves another entity's reach without disengaging.
 */
public class OpportunityAttackHandler {

    // Track previous positions for movement detection
    private static final Map<UUID, Vec3d> previousPositions = new HashMap<>();

    /**
     * Check if movement should trigger opportunity attacks.
     * Called from CharacterEntity.tick() to monitor movement during combat.
     *
     * @param movingEntity The entity that moved
     * @param oldPosition The position before movement
     * @param newPosition The position after movement
     * @param server The server instance
     */
    public static void checkForOpportunityAttacks(CharacterEntity movingEntity, Vec3d oldPosition,
                                                  Vec3d newPosition, MinecraftServer server) {
        // Only check during combat
        if (!movingEntity.getCombatState().inCombat()) {
            return;
        }

        // Don't trigger opportunity attacks if entity used Disengage action
        // TODO: Add disengaged flag to CombatState
        // For now, we'll implement basic opportunity attacks

        // Get the encounter
        EncounterState encounter = EncounterManager.getInstance()
            .getEncounterForEntity(movingEntity.getUuid());

        if (encounter == null) {
            return;
        }

        // Check all other participants for opportunity attacks
        for (var tracker : encounter.getTurnOrder()) {
            if (tracker.entityId().equals(movingEntity.getUuid())) {
                continue; // Skip self
            }

            Entity entity = server.getOverworld().getEntity(tracker.entityId());
            if (!(entity instanceof CharacterEntity attacker)) {
                continue;
            }

            // Check if this entity gets an opportunity attack
            if (shouldTriggerOpportunityAttack(attacker, movingEntity, oldPosition, newPosition)) {
                performOpportunityAttack(attacker, movingEntity, server, encounter);
            }
        }
    }

    /**
     * Determine if an opportunity attack should be triggered.
     * Conditions:
     * 1. Attacker has reaction available
     * 2. Moving entity was within attacker's reach at old position
     * 3. Moving entity is outside attacker's reach at new position
     *
     * @param attacker The entity that might get an opportunity attack
     * @param movingEntity The entity that is moving
     * @param oldPosition Position before movement
     * @param newPosition Position after movement
     * @return true if opportunity attack should trigger
     */
    private static boolean shouldTriggerOpportunityAttack(CharacterEntity attacker, CharacterEntity movingEntity,
                                                          Vec3d oldPosition, Vec3d newPosition) {
        // Check if attacker has reaction
        if (!attacker.getCombatState().hasReaction()) {
            return false;
        }

        // Get attacker's weapon reach
        Weapon weapon = attacker.getEquippedWeapon();
        double reach = weapon.getReach() + 0.5; // Add 0.5 for entity width

        Vec3d attackerPos = new Vec3d(attacker.getX(), attacker.getY(), attacker.getZ());

        // Check if moving entity was in reach before
        double oldDistance = attackerPos.distanceTo(oldPosition);
        boolean wasInReach = oldDistance <= reach;

        // Check if moving entity is out of reach now
        double newDistance = attackerPos.distanceTo(newPosition);
        boolean isOutOfReach = newDistance > reach;

        // Trigger if entity left reach
        return wasInReach && isOutOfReach;
    }

    /**
     * Execute an opportunity attack.
     *
     * @param attacker The entity performing the opportunity attack
     * @param target The entity being attacked
     * @param server The server instance
     * @param encounter The current encounter
     */
    private static void performOpportunityAttack(CharacterEntity attacker, CharacterEntity target,
                                                  MinecraftServer server, EncounterState encounter) {
        // Check reaction one more time (should be redundant but safe)
        CombatState attackerState = attacker.getCombatState();
        if (!attackerState.hasReaction()) {
            return;
        }

        FiveESrdMod.LOGGER.info("{} gets an opportunity attack against {}!",
            attacker.getName().getString(), target.getName().getString());

        // Perform the attack
        DiceRoller roller = new DiceRoller(attacker.getEntityWorld().getRandom());
        CombatResolver resolver = new CombatResolver(roller);

        Weapon weapon = attacker.getEquippedWeapon();
        AttackResult result = resolver.performOpportunityAttack(
            weapon,
            attacker.getStats(),
            attacker.getLevel(),
            target.getCombatState().armorClass(),
            true  // Assumed proficient
        );

        FiveESrdMod.LOGGER.info("Opportunity attack result: {}", result.description());

        // Consume the reaction
        attacker.setCombatState(attackerState.useReaction());

        // Apply damage if hit
        if (result.isHit()) {
            CombatState targetState = target.getCombatState();
            CombatState newTargetState = targetState.takeDamage(result.damageDealt());
            target.setCombatState(newTargetState);

            // Sync to clients
            EncounterManager.getInstance().syncCombatState(server, target.getUuid(), newTargetState);

            // Check for death
            if (newTargetState.currentHitPoints() <= 0) {
                handleOpportunityAttackDeath(target, encounter, server);
            }
        }

        // Broadcast opportunity attack to all participants
        Text opportunityMessage = Text.literal(
            "⚔ OPPORTUNITY ATTACK! " + attacker.getName().getString() +
            " attacks " + target.getName().getString() + " as they flee! " +
            result.description()
        );

        for (var tracker : encounter.getTurnOrder()) {
            Entity participant = server.getOverworld().getEntity(tracker.entityId());
            if (participant instanceof ServerPlayerEntity playerEntity) {
                playerEntity.sendMessage(opportunityMessage, false);
            }
        }
    }

    /**
     * Handle target death from opportunity attack.
     */
    private static void handleOpportunityAttackDeath(CharacterEntity deadEntity, EncounterState encounter,
                                                     MinecraftServer server) {
        FiveESrdMod.LOGGER.info("Character {} was killed by an opportunity attack!", deadEntity.getName().getString());

        // Remove from encounter
        EncounterManager.getInstance().removeCombatant(deadEntity.getUuid());

        // Broadcast death message
        Text deathMessage = Text.literal(
            deadEntity.getName().getString() + " was struck down while fleeing!"
        );

        for (var tracker : encounter.getTurnOrder()) {
            Entity participant = server.getOverworld().getEntity(tracker.entityId());
            if (participant instanceof ServerPlayerEntity playerEntity) {
                playerEntity.sendMessage(deathMessage, false);
            }
        }
    }

    /**
     * Update previous position tracking for an entity.
     * Call this at the end of tick after opportunity attack checks.
     */
    public static void updatePosition(UUID entityId, Vec3d position) {
        previousPositions.put(entityId, position);
    }

    /**
     * Get the previous position of an entity.
     */
    public static Vec3d getPreviousPosition(UUID entityId) {
        return previousPositions.getOrDefault(entityId, Vec3d.ZERO);
    }

    /**
     * Clear position tracking (call when entity leaves combat).
     */
    public static void clearPosition(UUID entityId) {
        previousPositions.remove(entityId);
    }
}
