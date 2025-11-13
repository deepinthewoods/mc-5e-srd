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

        CombatState state = character.getCombatState();
        Weapon weapon = character.getEquippedWeapon();
        double distanceToTarget = character.distanceTo(target);
        double weaponRange = weapon.getRange();

        // Calculate HP percentage for tactical decisions
        float hpPercentage = (float) state.currentHitPoints() / state.maxHitPoints();
        boolean isLowHealth = hpPercentage < 0.3f;
        boolean isInMeleeRange = distanceToTarget <= 2.0;
        boolean isInWeaponRange = distanceToTarget <= weaponRange;

        // Advanced AI decision tree
        if (isLowHealth && isInMeleeRange) {
            // Low health and in danger - try to disengage and retreat
            if (state.hasAction()) {
                useDisengage();
                endTurn();
                return;
            } else if (state.hasAction()) {
                // If can't disengage, use Dodge
                useDodge();
                endTurn();
                return;
            }
        }

        // If in weapon range, attack
        if (isInWeaponRange) {
            if (state.hasAction()) {
                performAttack(target);
                endTurn();
            } else {
                endTurn();
            }
            return;
        }

        // Not in range - need to move closer
        int remainingMovement = state.remainingMovement();
        if (distanceToTarget <= remainingMovement) {
            // Can reach target with normal movement
            character.getNavigation().startMovingTo(target, 1.0);
            // Attack after moving (next tick)
            if (state.hasAction()) {
                performAttack(target);
            }
            endTurn();
        } else if (state.hasAction()) {
            // Too far - use Dash to close distance
            useDash();
            character.getNavigation().startMovingTo(target, 1.0);
            endTurn();
        } else {
            // Can't reach, just move closer
            character.getNavigation().startMovingTo(target, 1.0);
            endTurn();
        }
    }

    /**
     * Find the nearest hostile target.
     * Prioritizes low HP targets that are conscious.
     */
    private LivingEntity findNearestTarget() {
        EncounterState encounter = EncounterManager.getInstance()
            .getEncounterForEntity(character.getUuid());

        if (encounter == null) {
            return null;
        }

        var server = character.getEntityWorld().getServer();
        if (server == null) {
            return null;
        }

        LivingEntity bestTarget = null;
        double bestScore = Double.MAX_VALUE;

        for (var tracker : encounter.getTurnOrder()) {
            if (tracker.entityId().equals(character.getUuid())) {
                continue; // Skip self
            }

            var entity = server.getOverworld().getEntity(tracker.entityId());
            if (!(entity instanceof CharacterEntity potentialTarget)) {
                continue;
            }

            // Skip unconscious targets
            if (potentialTarget.getCombatState().isUnconscious()) {
                continue;
            }

            // Calculate target priority score (lower is better)
            double distance = character.distanceTo(potentialTarget);
            float hpPercentage = (float) potentialTarget.getCombatState().currentHitPoints() /
                                  potentialTarget.getCombatState().maxHitPoints();

            // Score = distance * HP percentage (prioritize close, low HP targets)
            double score = distance * (hpPercentage + 0.1); // +0.1 to avoid zero

            if (score < bestScore) {
                bestScore = score;
                bestTarget = potentialTarget;
            }
        }

        return bestTarget;
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
            CombatState newTargetState = targetState.takeDamage(result.damageDealt(), result.isCritical());
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
     * Use the Dash action to double movement speed.
     */
    private void useDash() {
        CombatState state = character.getCombatState();
        if (!state.hasAction()) {
            return;
        }

        int currentMovement = state.remainingMovement();
        CombatState newState = state.useAction().withRemainingMovement(currentMovement * 2);
        character.setCombatState(newState);

        FiveESrdMod.LOGGER.info("AI {} used Dash action", character.getName().getString());
        broadcastMessage(character.getName().getString() + " takes the Dash action!");
    }

    /**
     * Use the Disengage action to prevent opportunity attacks.
     */
    private void useDisengage() {
        CombatState state = character.getCombatState();
        if (!state.hasAction()) {
            return;
        }

        CombatState newState = state.useAction().setDisengaged(true);
        character.setCombatState(newState);

        FiveESrdMod.LOGGER.info("AI {} used Disengage action", character.getName().getString());
        broadcastMessage(character.getName().getString() + " takes the Disengage action!");
    }

    /**
     * Use the Dodge action to give attackers disadvantage.
     */
    private void useDodge() {
        CombatState state = character.getCombatState();
        if (!state.hasAction()) {
            return;
        }

        CombatState newState = state.useAction().setDodging(true);
        character.setCombatState(newState);

        FiveESrdMod.LOGGER.info("AI {} used Dodge action", character.getName().getString());
        broadcastMessage(character.getName().getString() + " takes the Dodge action!");
    }

    /**
     * Broadcast a message to all encounter participants.
     */
    private void broadcastMessage(String message) {
        EncounterState encounter = EncounterManager.getInstance()
            .getEncounterForEntity(character.getUuid());

        if (encounter == null) {
            return;
        }

        var server = character.getEntityWorld().getServer();
        if (server != null) {
            Text textMessage = Text.literal(message);
            for (var tracker : encounter.getTurnOrder()) {
                var participant = server.getOverworld().getEntity(tracker.entityId());
                if (participant instanceof net.minecraft.server.network.ServerPlayerEntity playerEntity) {
                    playerEntity.sendMessage(textMessage, false);
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
