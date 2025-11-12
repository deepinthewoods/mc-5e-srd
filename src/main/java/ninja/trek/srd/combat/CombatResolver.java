package ninja.trek.srd.combat;

import ninja.trek.srd.character.data.CharacterStats;
import ninja.trek.srd.util.DiceRoller;

/**
 * Handles combat resolution including attack rolls and damage calculation.
 * Implements D&D 5e SRD combat rules.
 */
public class CombatResolver {

    private final DiceRoller diceRoller;

    public CombatResolver(DiceRoller diceRoller) {
        this.diceRoller = diceRoller;
    }

    /**
     * Performs a complete attack from attacker to target.
     *
     * @param weapon The weapon being used
     * @param attackerStats The attacker's ability scores
     * @param attackerLevel The attacker's level (for proficiency bonus)
     * @param targetAC The target's armor class
     * @param isProficient Whether the attacker is proficient with the weapon
     * @param advantage Whether the attack has advantage
     * @param disadvantage Whether the attack has disadvantage
     * @return The result of the attack including damage
     */
    public AttackResult performAttack(
        Weapon weapon,
        CharacterStats attackerStats,
        int attackerLevel,
        int targetAC,
        boolean isProficient,
        boolean advantage,
        boolean disadvantage
    ) {
        // Step 1: Calculate attack bonus
        int attackBonus = calculateAttackBonus(
            weapon,
            attackerStats,
            attackerLevel,
            isProficient
        );

        // Step 2: Roll attack (d20)
        int attackRoll = rollAttack(advantage, disadvantage);
        boolean isCritical = attackRoll == 20; // Natural 20 is always a critical hit
        boolean isAutomaticMiss = attackRoll == 1; // Natural 1 is always a miss

        // Step 3: Check if attack hits
        int totalAttack = attackRoll + attackBonus;
        boolean hits = !isAutomaticMiss && (isCritical || totalAttack >= targetAC);

        if (!hits) {
            return AttackResult.miss(attackRoll, attackBonus, targetAC);
        }

        // Step 4: Roll damage
        int damage = rollDamage(weapon, attackerStats, isCritical, false);

        return AttackResult.hit(attackRoll, attackBonus, targetAC, damage, weapon.damageType(), isCritical);
    }

    /**
     * Calculates the total attack bonus for a weapon attack.
     * Attack Bonus = Ability Modifier + Proficiency Bonus (if proficient) + Weapon Magic Bonus
     */
    public int calculateAttackBonus(
        Weapon weapon,
        CharacterStats attackerStats,
        int attackerLevel,
        boolean isProficient
    ) {
        // Choose ability modifier (STR or DEX)
        int abilityModifier = getAttackAbilityModifier(weapon, attackerStats);

        // Calculate proficiency bonus based on level
        int proficiencyBonus = isProficient ? calculateProficiencyBonus(attackerLevel) : 0;

        // Add weapon's magical bonus
        int weaponBonus = weapon.getAttackBonus();

        return abilityModifier + proficiencyBonus + weaponBonus;
    }

    /**
     * Rolls damage for a weapon attack.
     * Damage = Weapon Dice + Ability Modifier + Weapon Magic Bonus
     * Critical hits double the weapon dice (not the modifiers).
     */
    public int rollDamage(
        Weapon weapon,
        CharacterStats attackerStats,
        boolean isCritical,
        boolean isTwoHanded
    ) {
        int damageDice = weapon.damageDice();
        int damageDie = weapon.damageDie();

        // Use versatile damage if two-handed and weapon has versatile property
        if (isTwoHanded && weapon.hasProperty(WeaponProperty.VERSATILE)) {
            damageDice = weapon.versatileDamageDice();
            damageDie = weapon.versatileDamageDie();
        }

        // Roll weapon damage dice
        int weaponDamage = diceRoller.rollMultiple(damageDice, damageDie);

        // Double dice on critical hit
        if (isCritical) {
            weaponDamage += diceRoller.rollMultiple(damageDice, damageDie);
        }

        // Add ability modifier to damage (not doubled on crit)
        int abilityModifier = getDamageAbilityModifier(weapon, attackerStats);

        // Add weapon's magical bonus to damage (not doubled on crit)
        int weaponBonus = weapon.getDamageBonus();

        // Total damage (minimum 0)
        return Math.max(0, weaponDamage + abilityModifier + weaponBonus);
    }

    /**
     * Rolls an attack with advantage or disadvantage.
     * Advantage: roll twice, take higher
     * Disadvantage: roll twice, take lower
     * Both cancel out: roll once
     */
    private int rollAttack(boolean advantage, boolean disadvantage) {
        // Advantage and disadvantage cancel out
        if (advantage && disadvantage) {
            return diceRoller.rollD20();
        }

        if (advantage) {
            return diceRoller.rollWithAdvantage(20);
        }

        if (disadvantage) {
            return diceRoller.rollWithDisadvantage(20);
        }

        return diceRoller.rollD20();
    }

    /**
     * Determines which ability modifier to use for attack rolls.
     * Finesse and ranged weapons can use DEX, otherwise STR.
     * For finesse weapons, choose the higher of STR or DEX.
     */
    private int getAttackAbilityModifier(Weapon weapon, CharacterStats stats) {
        if (weapon.canUseDexterity()) {
            // For finesse weapons, choose the better modifier
            if (weapon.hasProperty(WeaponProperty.FINESSE)) {
                return Math.max(stats.getStrengthModifier(), stats.getDexterityModifier());
            }
            // Ranged weapons always use DEX
            return stats.getDexterityModifier();
        }
        // Melee weapons without finesse use STR
        return stats.getStrengthModifier();
    }

    /**
     * Determines which ability modifier to use for damage rolls.
     * Same logic as attack rolls.
     */
    private int getDamageAbilityModifier(Weapon weapon, CharacterStats stats) {
        return getAttackAbilityModifier(weapon, stats);
    }

    /**
     * Calculates proficiency bonus based on character level.
     * Proficiency Bonus = 2 + floor((level - 1) / 4)
     * Level 1-4: +2, Level 5-8: +3, Level 9-12: +4, etc.
     */
    private int calculateProficiencyBonus(int level) {
        return 2 + (level - 1) / 4;
    }

    /**
     * Performs an opportunity attack (same as regular attack but uses reaction).
     */
    public AttackResult performOpportunityAttack(
        Weapon weapon,
        CharacterStats attackerStats,
        int attackerLevel,
        int targetAC,
        boolean isProficient
    ) {
        // Opportunity attacks are resolved the same as regular attacks
        // but they consume the reaction resource (handled by caller)
        return performAttack(weapon, attackerStats, attackerLevel, targetAC, isProficient, false, false);
    }
}
