package ninja.trek.srd.equipment;

import ninja.trek.srd.combat.Weapon;

/**
 * Represents a character's equipped items.
 *
 * Tracks armor, weapons, and other equipment slots.
 */
public class Equipment {
    private ArmorType armor;
    private ArmorType shield;
    private Weapon mainHandWeapon;
    private Weapon offHandWeapon;

    public Equipment() {
        this.armor = null;
        this.shield = null;
        this.mainHandWeapon = null;
        this.offHandWeapon = null;
    }

    // Armor
    public ArmorType getArmor() { return armor; }
    public void setArmor(ArmorType armor) { this.armor = armor; }

    // Shield
    public ArmorType getShield() { return shield; }
    public void setShield(ArmorType shield) {
        if (shield != null && shield.getCategory() != ArmorType.ArmorCategory.SHIELD) {
            throw new IllegalArgumentException("Only shields can be equipped in shield slot");
        }
        this.shield = shield;
    }

    // Weapons
    public Weapon getMainHandWeapon() { return mainHandWeapon; }
    public void setMainHandWeapon(Weapon weapon) { this.mainHandWeapon = weapon; }

    public Weapon getOffHandWeapon() { return offHandWeapon; }
    public void setOffHandWeapon(Weapon weapon) { this.offHandWeapon = weapon; }

    /**
     * Calculate total AC from equipped armor and shield.
     *
     * @param dexModifier The character's DEX modifier
     * @return Total AC
     */
    public int calculateAC(int dexModifier) {
        int ac = 10 + dexModifier; // Base AC with no armor

        if (armor != null) {
            ac = armor.calculateAC(dexModifier);
        }

        if (shield != null) {
            ac += shield.getBaseAC(); // Shields always add +2
        }

        return ac;
    }

    /**
     * Check if the character has stealth disadvantage from armor.
     */
    public boolean hasStealthDisadvantage() {
        return (armor != null && armor.hasStealthDisadvantage());
    }

    /**
     * Check if the character is wielding a shield.
     */
    public boolean hasShield() {
        return shield != null;
    }

    /**
     * Check if the character is dual wielding.
     */
    public boolean isDualWielding() {
        return mainHandWeapon != null && offHandWeapon != null;
    }
}
