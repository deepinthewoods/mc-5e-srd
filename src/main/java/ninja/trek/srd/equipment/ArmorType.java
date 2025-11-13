package ninja.trek.srd.equipment;

/**
 * Types of armor from the 5e SRD.
 */
public enum ArmorType {
    // Light Armor
    PADDED("Padded", ArmorCategory.LIGHT, 11, false, false),
    LEATHER("Leather", ArmorCategory.LIGHT, 11, false, false),
    STUDDED_LEATHER("Studded Leather", ArmorCategory.LIGHT, 12, false, false),

    // Medium Armor
    HIDE("Hide", ArmorCategory.MEDIUM, 12, false, false),
    CHAIN_SHIRT("Chain Shirt", ArmorCategory.MEDIUM, 13, false, false),
    SCALE_MAIL("Scale Mail", ArmorCategory.MEDIUM, 14, true, false),
    BREASTPLATE("Breastplate", ArmorCategory.MEDIUM, 14, false, false),
    HALF_PLATE("Half Plate", ArmorCategory.MEDIUM, 15, true, false),

    // Heavy Armor
    RING_MAIL("Ring Mail", ArmorCategory.HEAVY, 14, true, false),
    CHAIN_MAIL("Chain Mail", ArmorCategory.HEAVY, 16, true, true),
    SPLINT("Splint", ArmorCategory.HEAVY, 17, true, true),
    PLATE("Plate", ArmorCategory.HEAVY, 18, true, true),

    // Shield
    SHIELD("Shield", ArmorCategory.SHIELD, 2, false, false);

    private final String displayName;
    private final ArmorCategory category;
    private final int baseAC;
    private final boolean stealthDisadvantage;
    private final boolean strengthRequirement;

    ArmorType(String displayName, ArmorCategory category, int baseAC,
              boolean stealthDisadvantage, boolean strengthRequirement) {
        this.displayName = displayName;
        this.category = category;
        this.baseAC = baseAC;
        this.stealthDisadvantage = stealthDisadvantage;
        this.strengthRequirement = strengthRequirement;
    }

    public String getDisplayName() { return displayName; }
    public ArmorCategory getCategory() { return category; }
    public int getBaseAC() { return baseAC; }
    public boolean hasStealthDisadvantage() { return stealthDisadvantage; }
    public boolean hasStrengthRequirement() { return strengthRequirement; }

    /**
     * Calculate AC for this armor type with the given DEX modifier.
     */
    public int calculateAC(int dexModifier) {
        return switch (category) {
            case LIGHT -> baseAC + dexModifier;
            case MEDIUM -> baseAC + Math.min(dexModifier, 2); // Max +2 DEX
            case HEAVY -> baseAC; // No DEX bonus
            case SHIELD -> baseAC; // Shields are always +2
        };
    }

    public enum ArmorCategory {
        LIGHT,
        MEDIUM,
        HEAVY,
        SHIELD
    }
}
