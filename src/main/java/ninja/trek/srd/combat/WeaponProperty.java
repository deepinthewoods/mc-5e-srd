package ninja.trek.srd.combat;

/**
 * Represents special properties that weapons can have in D&D 5e SRD.
 * These properties modify how weapons can be used in combat.
 */
public enum WeaponProperty {
    /**
     * When making an attack with a finesse weapon, you use your choice of your
     * Strength or Dexterity modifier for the attack and damage rolls.
     */
    FINESSE("Finesse"),

    /**
     * A light weapon is small and easy to handle, making it ideal for use when
     * fighting with two weapons.
     */
    LIGHT("Light"),

    /**
     * Small creatures have disadvantage on attack rolls with heavy weapons.
     * A heavy weapon's size and bulk make it too large for a Small creature to use effectively.
     */
    HEAVY("Heavy"),

    /**
     * This weapon requires two hands to use.
     */
    TWO_HANDED("Two-Handed"),

    /**
     * This weapon can be used with one or two hands. A damage value in parentheses
     * appears with the property—the damage when the weapon is used with two hands to make a melee attack.
     */
    VERSATILE("Versatile"),

    /**
     * This weapon adds 5 feet (1 block in Minecraft) to your reach when you attack with it.
     */
    REACH("Reach"),

    /**
     * If a weapon has the thrown property, you can throw the weapon to make a ranged attack.
     */
    THROWN("Thrown"),

    /**
     * A weapon that can be used to make a ranged attack has a range.
     * The range lists two numbers (normal/long range).
     */
    RANGED("Ranged"),

    /**
     * Because of the time required to load this weapon, you can fire only one piece
     * of ammunition from it when you use an action, bonus action, or reaction to fire it,
     * regardless of the number of attacks you can normally make.
     */
    LOADING("Loading"),

    /**
     * This weapon can be used to make a melee attack.
     */
    MELEE("Melee");

    private final String displayName;

    WeaponProperty(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
