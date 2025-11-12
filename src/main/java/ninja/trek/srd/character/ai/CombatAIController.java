package ninja.trek.srd.character.ai;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.entity.ai.goal.ActiveTargetGoal;
import net.minecraft.entity.player.PlayerEntity;
import ninja.trek.srd.character.entity.CharacterEntity;
import ninja.trek.srd.combat.EncounterManager;

import java.util.EnumSet;

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
     * Perform a basic melee attack.
     */
    private void performAttack(LivingEntity target) {
        // TODO: Implement proper 5e attack roll and damage calculation
        if (character.getEntityWorld() instanceof net.minecraft.server.world.ServerWorld serverWorld) {
            character.tryAttack(serverWorld, target);
        }

        // Use action
        var combatState = character.getCombatState();
        character.setCombatState(combatState.useAction());
    }

    /**
     * End this entity's turn.
     */
    private void endTurn() {
        var encounter = EncounterManager.getInstance()
            .getEncounterForEntity(character.getUuid());

        if (encounter != null) {
            EncounterManager.getInstance().advanceTurn(encounter.getEncounterId());
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
