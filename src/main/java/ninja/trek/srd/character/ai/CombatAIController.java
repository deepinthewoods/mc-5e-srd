package ninja.trek.srd.character.ai;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.entity.ai.goal.ActiveTargetGoal;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.Text;
import ninja.trek.srd.FiveESrdMod;
import ninja.trek.srd.character.entity.CharacterEntity;
import ninja.trek.srd.combat.*;
import ninja.trek.srd.util.DiceRoller;

import java.util.EnumSet;
import java.util.UUID;

/**
 * AI controller for character entities during combat.
 * Implements basic AI behavior:
 * 1. If enemy in melee range -> Attack
 * 2. Else if can reach enemy with movement -> Move and Attack
 * 3. Else -> Move toward nearest enemy
 */
public class CombatAIController extends Goal {

    private final CharacterEntity character;
    private LivingEntity target;

    public CombatAIController(CharacterEntity character) {
        this.character = character;
        this.setControls(EnumSet.of(Goal.Control.MOVE, Goal.Control.LOOK));
    }

    @Override
    public boolean canStart() {
        // Only use AI when in combat and it's this entity's turn
        if (!character.getCombatState().inCombat()) {
            return false;
        }

        if (!character.isMyTurn()) {
            return false;
        }

        // Find nearest target
        this.target = findNearestTarget();
        return this.target != null;
    }

    @Override
    public boolean shouldContinue() {
        return canStart();
    }

    @Override
    public void tick() {
        if (target == null) {
            // No target, end turn
            endTurn();
            return;
        }

        double distanceToTarget = character.distanceTo(target);

        // Check if in melee range (approximately 1.5 blocks)
        if (distanceToTarget <= 2.0) {
            // Attack target
            performAttack(target);
            endTurn();
        } else {
            // Check if can reach with movement
            int remainingMovement = character.getCombatState().remainingMovement();
            if (distanceToTarget <= remainingMovement) {
                // Move to target and attack
                character.getNavigation().startMovingTo(target, 1.0);
                // Wait a tick for movement, then attack
                // TODO: Implement proper action sequencing
            } else {
                // Just move closer
                character.getNavigation().startMovingTo(target, 1.0);
                endTurn();
            }
        }
    }

    /**
     * Find the nearest hostile target.
     */
    private LivingEntity findNearestTarget() {
        // Simple implementation: find nearest player
        // TODO: Implement proper faction/hostility system
        return character.getEntityWorld()
            .getClosestPlayer(character, 20.0);
    }

    /**
     * Perform a basic melee attack using 5e rules.
     */
    private void performAttack(LivingEntity target) {
        // Only attack CharacterEntity targets (for now)
        if (!(target instanceof CharacterEntity targetCharacter)) {
            FiveESrdMod.LOGGER.warn("AI tried to attack non-character entity: {}", target.getType());
            return;
        }

        // Check if has action available
        CombatState combatState = character.getCombatState();
        if (!combatState.hasAction()) {
            FiveESrdMod.LOGGER.warn("AI {} tried to attack but has no action", character.getName().getString());
            return;
        }

        // Perform 5e attack
        DiceRoller roller = new DiceRoller(character.getEntityWorld().getRandom());
        CombatResolver resolver = new CombatResolver(roller);

        Weapon weapon = character.getEquippedWeapon();
        AttackResult result = resolver.performAttack(
            weapon,
            character.getStats(),
            character.getLevel(),
            targetCharacter.getCombatState().armorClass(),
            true,  // AI is assumed proficient with their weapon
            false, // No advantage
            false  // No disadvantage
        );

        FiveESrdMod.LOGGER.info("AI Attack result: {}", result.description());

        // Consume the action
        character.setCombatState(combatState.useAction());

        // Apply damage if hit
        if (result.isHit()) {
            CombatState targetState = targetCharacter.getCombatState();
            CombatState newTargetState = targetState.takeDamage(result.damageDealt());
            targetCharacter.setCombatState(newTargetState);

            // Check for death
            if (newTargetState.currentHitPoints() <= 0) {
                handleTargetDeath(targetCharacter);
            }
        }

        // Broadcast attack result
        broadcastAttackResult(result);
    }

    /**
     * Handle target death after AI attack.
     */
    private void handleTargetDeath(CharacterEntity deadTarget) {
        EncounterState encounter = EncounterManager.getInstance()
            .getEncounterForEntity(character.getUuid());

        if (encounter == null) {
            return;
        }

        var server = character.getEntityWorld().getServer();
        if (server != null) {
            EncounterManager.getInstance().removeCombatant(deadTarget.getUuid());

            // Broadcast death message
            Text deathMessage = Text.literal(deadTarget.getName().getString() + " has been defeated!");
            for (var tracker : encounter.getTurnOrder()) {
                var participant = server.getOverworld().getEntity(tracker.entityId());
                if (participant instanceof net.minecraft.server.network.ServerPlayerEntity playerEntity) {
                    playerEntity.sendMessage(deathMessage, false);
                }
            }
        }
    }

    /**
     * Broadcast attack result to all participants.
     */
    private void broadcastAttackResult(AttackResult result) {
        EncounterState encounter = EncounterManager.getInstance()
            .getEncounterForEntity(character.getUuid());

        if (encounter == null) {
            return;
        }

        var server = character.getEntityWorld().getServer();
        if (server != null) {
            Text attackMessage = Text.literal(
                character.getName().getString() + ": " + result.description()
            );

            for (var tracker : encounter.getTurnOrder()) {
                var participant = server.getOverworld().getEntity(tracker.entityId());
                if (participant instanceof net.minecraft.server.network.ServerPlayerEntity playerEntity) {
                    playerEntity.sendMessage(attackMessage, false);
                }
            }
        }
    }

    /**
     * End this entity's turn.
     */
    private void endTurn() {
        var encounter = EncounterManager.getInstance()
            .getEncounterForEntity(character.getUuid());

        if (encounter != null) {
            var server = character.getEntityWorld().getServer();
            if (server != null) {
                EncounterManager.getInstance().advanceTurn(server, encounter.getEncounterId());
            }
        }
    }

    /**
     * Register this AI goal to a character entity.
     * Note: This method should be called from within CharacterEntity where
     * goalSelector and targetSelector are accessible.
     */
    public static void registerGoals(CharacterEntity character) {
        // Access goals through public getter methods or make them accessible
        // For now, we'll add goals directly in CharacterEntity constructor
        // This method serves as documentation for what goals should be registered
    }
}
