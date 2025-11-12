package ninja.trek.srd.combat;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.util.math.Vec3d;

/**
 * Tracks an entity's current combat state and action economy.
 */
public record CombatState(
    boolean inCombat,
    int initiative,
    Vec3d turnStartPosition,
    int remainingMovement,
    boolean hasAction,
    boolean hasBonusAction,
    boolean hasReaction,
    int armorClass,
    int currentHitPoints,
    int maxHitPoints,
    boolean isUnconscious,
    DeathSaves deathSaves
) {
    public static final Codec<CombatState> CODEC = RecordCodecBuilder.create(instance ->
        instance.group(
            Codec.BOOL.fieldOf("in_combat").forGetter(CombatState::inCombat),
            Codec.INT.fieldOf("initiative").forGetter(CombatState::initiative),
            Vec3d.CODEC.fieldOf("turn_start_position").forGetter(CombatState::turnStartPosition),
            Codec.INT.fieldOf("remaining_movement").forGetter(CombatState::remainingMovement),
            Codec.BOOL.fieldOf("has_action").forGetter(CombatState::hasAction),
            Codec.BOOL.fieldOf("has_bonus_action").forGetter(CombatState::hasBonusAction),
            Codec.BOOL.fieldOf("has_reaction").forGetter(CombatState::hasReaction),
            Codec.INT.fieldOf("armor_class").forGetter(CombatState::armorClass),
            Codec.INT.fieldOf("current_hit_points").forGetter(CombatState::currentHitPoints),
            Codec.INT.fieldOf("max_hit_points").forGetter(CombatState::maxHitPoints),
            Codec.BOOL.fieldOf("is_unconscious").forGetter(CombatState::isUnconscious),
            DeathSaves.CODEC.fieldOf("death_saves").forGetter(CombatState::deathSaves)
        ).apply(instance, CombatState::new)
    );

    /**
     * Create a default non-combat state.
     */
    public static CombatState createDefault(int maxHp, int ac) {
        return new CombatState(
            false,
            0,
            Vec3d.ZERO,
            0,
            false,
            false,
            false,
            ac,
            maxHp,
            maxHp,
            false,
            DeathSaves.createDefault()
        );
    }

    /**
     * Start a new turn for this entity.
     */
    public CombatState startTurn(Vec3d currentPosition, int movementSpeed) {
        return new CombatState(
            true,
            this.initiative,
            currentPosition,
            movementSpeed,
            true,
            true,
            true,
            this.armorClass,
            this.currentHitPoints,
            this.maxHitPoints,
            this.isUnconscious,
            this.deathSaves
        );
    }

    /**
     * Use the entity's action.
     */
    public CombatState useAction() {
        return new CombatState(
            this.inCombat,
            this.initiative,
            this.turnStartPosition,
            this.remainingMovement,
            false,
            this.hasBonusAction,
            this.hasReaction,
            this.armorClass,
            this.currentHitPoints,
            this.maxHitPoints,
            this.isUnconscious,
            this.deathSaves
        );
    }

    /**
     * Use the entity's bonus action.
     */
    public CombatState useBonusAction() {
        return new CombatState(
            this.inCombat,
            this.initiative,
            this.turnStartPosition,
            this.remainingMovement,
            this.hasAction,
            false,
            this.hasReaction,
            this.armorClass,
            this.currentHitPoints,
            this.maxHitPoints,
            this.isUnconscious,
            this.deathSaves
        );
    }

    /**
     * Use the entity's reaction.
     */
    public CombatState useReaction() {
        return new CombatState(
            this.inCombat,
            this.initiative,
            this.turnStartPosition,
            this.remainingMovement,
            this.hasAction,
            this.hasBonusAction,
            false,
            this.armorClass,
            this.currentHitPoints,
            this.maxHitPoints,
            this.isUnconscious,
            this.deathSaves
        );
    }

    /**
     * Update remaining movement.
     */
    public CombatState withRemainingMovement(int movement) {
        return new CombatState(
            this.inCombat,
            this.initiative,
            this.turnStartPosition,
            movement,
            this.hasAction,
            this.hasBonusAction,
            this.hasReaction,
            this.armorClass,
            this.currentHitPoints,
            this.maxHitPoints,
            this.isUnconscious,
            this.deathSaves
        );
    }

    /**
     * Take damage.
     * If damage reduces HP to 0, entity becomes unconscious.
     * If already unconscious, each hit counts as a failed death save.
     */
    public CombatState takeDamage(int damage) {
        int newHp = Math.max(0, this.currentHitPoints - damage);
        boolean nowUnconscious = newHp == 0;

        // If already unconscious and taking damage, add death save failures
        DeathSaves newDeathSaves = this.deathSaves;
        if (this.isUnconscious && damage > 0) {
            // TODO: Check if damage was from a critical hit (2 failures instead of 1)
            newDeathSaves = newDeathSaves.addFailure();
        } else if (nowUnconscious && !this.isUnconscious) {
            // Just became unconscious, reset death saves
            newDeathSaves = DeathSaves.createDefault();
        }

        // Check for massive damage (instant death if damage >= max HP)
        if (this.currentHitPoints > 0 && damage >= this.maxHitPoints) {
            // Massive damage = instant death
            newDeathSaves = newDeathSaves.addFailures(3);
        }

        return new CombatState(
            this.inCombat,
            this.initiative,
            this.turnStartPosition,
            this.remainingMovement,
            this.hasAction,
            this.hasBonusAction,
            this.hasReaction,
            this.armorClass,
            newHp,
            this.maxHitPoints,
            nowUnconscious,
            newDeathSaves
        );
    }

    /**
     * Heal damage.
     * Healing from 0 HP wakes the entity and clears death saves.
     */
    public CombatState heal(int amount) {
        int newHp = Math.min(this.maxHitPoints, this.currentHitPoints + amount);
        boolean stillUnconscious = newHp == 0;
        DeathSaves newDeathSaves = stillUnconscious ? this.deathSaves : DeathSaves.createDefault();

        return new CombatState(
            this.inCombat,
            this.initiative,
            this.turnStartPosition,
            this.remainingMovement,
            this.hasAction,
            this.hasBonusAction,
            this.hasReaction,
            this.armorClass,
            newHp,
            this.maxHitPoints,
            stillUnconscious,
            newDeathSaves
        );
    }

    /**
     * Make a death saving throw.
     * @param rollResult The d20 roll result (1-20)
     * @return New combat state with updated death saves
     */
    public CombatState makeDeathSave(int rollResult) {
        if (!this.isUnconscious) {
            return this; // Not unconscious, no death save needed
        }

        DeathSaves newDeathSaves;
        if (rollResult == 1) {
            // Natural 1 = 2 failures
            newDeathSaves = this.deathSaves.addFailures(2);
        } else if (rollResult == 20) {
            // Natural 20 = regain 1 HP and wake up
            return this.heal(1);
        } else if (rollResult >= 10) {
            // Success
            newDeathSaves = this.deathSaves.addSuccess();
        } else {
            // Failure
            newDeathSaves = this.deathSaves.addFailure();
        }

        return new CombatState(
            this.inCombat,
            this.initiative,
            this.turnStartPosition,
            this.remainingMovement,
            this.hasAction,
            this.hasBonusAction,
            this.hasReaction,
            this.armorClass,
            this.currentHitPoints,
            this.maxHitPoints,
            this.isUnconscious,
            newDeathSaves
        );
    }

    /**
     * Stabilize the entity (via Medicine check or Spare the Dying).
     */
    public CombatState stabilize() {
        if (!this.isUnconscious) {
            return this;
        }

        return new CombatState(
            this.inCombat,
            this.initiative,
            this.turnStartPosition,
            this.remainingMovement,
            this.hasAction,
            this.hasBonusAction,
            this.hasReaction,
            this.armorClass,
            this.currentHitPoints,
            this.maxHitPoints,
            this.isUnconscious,
            this.deathSaves.stabilize()
        );
    }

    /**
     * Check if movement to target position is valid.
     */
    public boolean canMoveTo(Vec3d targetPos) {
        // If no action/bonus action used, can freely explore movement sphere
        if (this.hasAction && this.hasBonusAction) {
            return true;
        }
        // Otherwise, check if within remaining movement range
        double distanceFromStart = this.turnStartPosition.distanceTo(targetPos);
        return distanceFromStart <= this.remainingMovement;
    }
}
