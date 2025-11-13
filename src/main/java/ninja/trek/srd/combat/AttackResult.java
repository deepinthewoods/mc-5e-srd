package ninja.trek.srd.combat;

/**
 * Represents the result of an attack roll in D&D 5e combat.
 * Contains information about hit/miss, damage dealt, and critical hits.
 */
public record AttackResult(
    boolean isHit,
    boolean isCritical,
    int attackRoll,      // The d20 roll result (natural roll, not including modifiers)
    int totalAttackBonus, // Total attack modifier applied
    int targetAC,
    int damageDealt,
    DamageType damageType,
    String description   // Human-readable description of the attack
) {

    /**
     * Creates a miss result.
     */
    public static AttackResult miss(int attackRoll, int totalAttackBonus, int targetAC) {
        return new AttackResult(
            false,
            false,
            attackRoll,
            totalAttackBonus,
            targetAC,
            0,
            DamageType.BLUDGEONING,
            String.format("Attack miss! (Rolled %d + %d = %d vs AC %d)",
                attackRoll, totalAttackBonus, attackRoll + totalAttackBonus, targetAC)
        );
    }

    /**
     * Creates a hit result with damage.
     */
    public static AttackResult hit(int attackRoll, int totalAttackBonus, int targetAC,
                                    int damage, DamageType damageType, boolean critical) {
        String critText = critical ? " CRITICAL HIT!" : "";
        return new AttackResult(
            true,
            critical,
            attackRoll,
            totalAttackBonus,
            targetAC,
            damage,
            damageType,
            String.format("Attack hit%s! (Rolled %d + %d = %d vs AC %d) Dealt %d %s damage",
                critText, attackRoll, totalAttackBonus, attackRoll + totalAttackBonus,
                targetAC, damage, damageType.getDisplayName().toLowerCase())
        );
    }

    /**
     * Returns the total attack roll (d20 + modifiers).
     */
    public int getTotalAttack() {
        return attackRoll + totalAttackBonus;
    }
}
