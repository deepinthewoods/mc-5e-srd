package ninja.trek.srd.combat;

import java.util.Set;

/**
 * Represents a weapon with D&D 5e SRD stats.
 * Weapons define damage dice, damage type, properties, and magical bonuses.
 */
public record Weapon(
    String name,
    int damageDice,          // Number of dice (e.g., 1 for 1d8)
    int damageDie,           // Die size (e.g., 8 for 1d8)
    DamageType damageType,
    int magicBonus,          // +1, +2, +3 for magic weapons (0 for normal)
    Set<WeaponProperty> properties,
    int versatileDamageDice, // Number of dice when used two-handed (0 if not versatile)
    int versatileDamageDie,  // Die size when used two-handed
    int normalRange,         // Normal range in blocks (for ranged/thrown weapons)
    int longRange            // Long range in blocks (disadvantage beyond normal range)
) {

    /**
     * Standard weapon constructor for simple weapons without versatile damage.
     */
    public Weapon(String name, int damageDice, int damageDie, DamageType damageType,
                  Set<WeaponProperty> properties) {
        this(name, damageDice, damageDie, damageType, 0, properties, 0, 0, 0, 0);
    }

    /**
     * Magic weapon constructor.
     */
    public Weapon(String name, int damageDice, int damageDie, DamageType damageType,
                  int magicBonus, Set<WeaponProperty> properties) {
        this(name, damageDice, damageDie, damageType, magicBonus, properties, 0, 0, 0, 0);
    }

    /**
     * Returns true if this weapon has the specified property.
     */
    public boolean hasProperty(WeaponProperty property) {
        return properties.contains(property);
    }

    /**
     * Returns true if this weapon can be used for melee attacks.
     */
    public boolean isMelee() {
        return hasProperty(WeaponProperty.MELEE);
    }

    /**
     * Returns true if this weapon can be used for ranged attacks.
     */
    public boolean isRanged() {
        return hasProperty(WeaponProperty.RANGED) || hasProperty(WeaponProperty.THROWN);
    }

    /**
     * Returns true if the attacker can use DEX instead of STR for attack and damage rolls.
     */
    public boolean canUseDexterity() {
        return hasProperty(WeaponProperty.FINESSE) || hasProperty(WeaponProperty.RANGED);
    }

    /**
     * Returns the attack bonus for this weapon (primarily from magical enhancement).
     */
    public int getAttackBonus() {
        return magicBonus;
    }

    /**
     * Returns the damage bonus for this weapon (primarily from magical enhancement).
     */
    public int getDamageBonus() {
        return magicBonus;
    }

    /**
     * Returns the reach of this weapon in blocks.
     * Standard melee reach is 1 block, reach weapons have 2 blocks.
     */
    public int getReach() {
        if (hasProperty(WeaponProperty.REACH)) {
            return 2; // 10 feet = 2 blocks
        } else if (isMelee()) {
            return 1; // 5 feet = 1 block
        }
        return 0; // Ranged weapons have no melee reach
    }

    /**
     * Returns the effective range of this weapon in blocks.
     * For melee weapons, returns the reach.
     * For ranged/thrown weapons, returns the normal range.
     */
    public double getRange() {
        if (isRanged() && normalRange > 0) {
            return normalRange;
        }
        return getReach();
    }

    /**
     * Returns a display string for the damage (e.g., "1d8+1 slashing").
     */
    public String getDamageString(boolean twoHanded) {
        if (twoHanded && versatileDamageDice > 0) {
            return String.format("%dd%d%s %s",
                versatileDamageDice,
                versatileDamageDie,
                magicBonus > 0 ? "+" + magicBonus : "",
                damageType.getDisplayName());
        }
        return String.format("%dd%d%s %s",
            damageDice,
            damageDie,
            magicBonus > 0 ? "+" + magicBonus : "",
            damageType.getDisplayName());
    }
}
