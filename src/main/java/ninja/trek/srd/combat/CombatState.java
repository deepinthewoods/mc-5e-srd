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
    int maxHitPoints
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
            Codec.INT.fieldOf("max_hit_points").forGetter(CombatState::maxHitPoints)
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
            maxHp
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
            this.maxHitPoints
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
            this.maxHitPoints
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
            this.maxHitPoints
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
            this.maxHitPoints
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
            this.maxHitPoints
        );
    }

    /**
     * Take damage.
     */
    public CombatState takeDamage(int damage) {
        return new CombatState(
            this.inCombat,
            this.initiative,
            this.turnStartPosition,
            this.remainingMovement,
            this.hasAction,
            this.hasBonusAction,
            this.hasReaction,
            this.armorClass,
            Math.max(0, this.currentHitPoints - damage),
            this.maxHitPoints
        );
    }

    /**
     * Heal damage.
     */
    public CombatState heal(int amount) {
        return new CombatState(
            this.inCombat,
            this.initiative,
            this.turnStartPosition,
            this.remainingMovement,
            this.hasAction,
            this.hasBonusAction,
            this.hasReaction,
            this.armorClass,
            Math.min(this.maxHitPoints, this.currentHitPoints + amount),
            this.maxHitPoints
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
