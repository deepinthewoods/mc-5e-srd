package ninja.trek.srd.combat;

/**
 * Represents the different types of damage in D&D 5e SRD.
 * These determine resistance, vulnerability, and immunity interactions.
 */
public enum DamageType {
    // Physical damage types
    SLASHING("Slashing"),
    PIERCING("Piercing"),
    BLUDGEONING("Bludgeoning"),

    // Elemental damage types
    FIRE("Fire"),
    COLD("Cold"),
    LIGHTNING("Lightning"),
    THUNDER("Thunder"),
    ACID("Acid"),
    POISON("Poison"),

    // Magical damage types
    FORCE("Force"),
    PSYCHIC("Psychic"),
    RADIANT("Radiant"),
    NECROTIC("Necrotic");

    private final String displayName;

    DamageType(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    /**
     * Physical damage types that can be reduced by resistances or increased by vulnerabilities.
     */
    public boolean isPhysical() {
        return this == SLASHING || this == PIERCING || this == BLUDGEONING;
    }

    /**
     * Elemental damage types for environmental effects and spells.
     */
    public boolean isElemental() {
        return this == FIRE || this == COLD || this == LIGHTNING ||
               this == THUNDER || this == ACID || this == POISON;
    }
}
