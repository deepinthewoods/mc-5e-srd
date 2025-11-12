package ninja.trek.srd.combat;

import java.util.Set;

/**
 * Registry of standard D&D 5e SRD weapons.
 * This class contains predefined weapon configurations for use in combat.
 */
public class Weapons {

    // ============ Simple Melee Weapons ============

    public static final Weapon CLUB = new Weapon(
        "Club",
        1, 4, // 1d4
        DamageType.BLUDGEONING,
        Set.of(WeaponProperty.LIGHT, WeaponProperty.MELEE)
    );

    public static final Weapon DAGGER = new Weapon(
        "Dagger",
        1, 4, // 1d4
        DamageType.PIERCING,
        0,
        Set.of(WeaponProperty.FINESSE, WeaponProperty.LIGHT, WeaponProperty.THROWN, WeaponProperty.MELEE),
        0, 0, // Not versatile
        4, 12  // 20/60 feet = 4/12 blocks
    );

    public static final Weapon QUARTERSTAFF = new Weapon(
        "Quarterstaff",
        1, 6, // 1d6
        DamageType.BLUDGEONING,
        0,
        Set.of(WeaponProperty.VERSATILE, WeaponProperty.MELEE),
        1, 8, // 1d8 when two-handed
        0, 0
    );

    public static final Weapon MACE = new Weapon(
        "Mace",
        1, 6, // 1d6
        DamageType.BLUDGEONING,
        Set.of(WeaponProperty.MELEE)
    );

    public static final Weapon SPEAR = new Weapon(
        "Spear",
        1, 6, // 1d6
        DamageType.PIERCING,
        0,
        Set.of(WeaponProperty.THROWN, WeaponProperty.VERSATILE, WeaponProperty.MELEE),
        1, 8, // 1d8 when two-handed
        4, 12  // 20/60 feet
    );

    // ============ Simple Ranged Weapons ============

    public static final Weapon LIGHT_CROSSBOW = new Weapon(
        "Light Crossbow",
        1, 8, // 1d8
        DamageType.PIERCING,
        0,
        Set.of(WeaponProperty.RANGED, WeaponProperty.LOADING, WeaponProperty.TWO_HANDED),
        0, 0,
        16, 64 // 80/320 feet = 16/64 blocks
    );

    public static final Weapon SHORTBOW = new Weapon(
        "Shortbow",
        1, 6, // 1d6
        DamageType.PIERCING,
        0,
        Set.of(WeaponProperty.RANGED, WeaponProperty.TWO_HANDED),
        0, 0,
        16, 64 // 80/320 feet
    );

    // ============ Martial Melee Weapons ============

    public static final Weapon LONGSWORD = new Weapon(
        "Longsword",
        1, 8, // 1d8
        DamageType.SLASHING,
        0,
        Set.of(WeaponProperty.VERSATILE, WeaponProperty.MELEE),
        1, 10, // 1d10 when two-handed
        0, 0
    );

    public static final Weapon SHORTSWORD = new Weapon(
        "Shortsword",
        1, 6, // 1d6
        DamageType.PIERCING,
        Set.of(WeaponProperty.FINESSE, WeaponProperty.LIGHT, WeaponProperty.MELEE)
    );

    public static final Weapon GREATSWORD = new Weapon(
        "Greatsword",
        2, 6, // 2d6
        DamageType.SLASHING,
        Set.of(WeaponProperty.HEAVY, WeaponProperty.TWO_HANDED, WeaponProperty.MELEE)
    );

    public static final Weapon GREATAXE = new Weapon(
        "Greataxe",
        1, 12, // 1d12
        DamageType.SLASHING,
        Set.of(WeaponProperty.HEAVY, WeaponProperty.TWO_HANDED, WeaponProperty.MELEE)
    );

    public static final Weapon BATTLEAXE = new Weapon(
        "Battleaxe",
        1, 8, // 1d8
        DamageType.SLASHING,
        0,
        Set.of(WeaponProperty.VERSATILE, WeaponProperty.MELEE),
        1, 10, // 1d10 when two-handed
        0, 0
    );

    public static final Weapon RAPIER = new Weapon(
        "Rapier",
        1, 8, // 1d8
        DamageType.PIERCING,
        Set.of(WeaponProperty.FINESSE, WeaponProperty.MELEE)
    );

    public static final Weapon SCIMITAR = new Weapon(
        "Scimitar",
        1, 6, // 1d6
        DamageType.SLASHING,
        Set.of(WeaponProperty.FINESSE, WeaponProperty.LIGHT, WeaponProperty.MELEE)
    );

    public static final Weapon WARHAMMER = new Weapon(
        "Warhammer",
        1, 8, // 1d8
        DamageType.BLUDGEONING,
        0,
        Set.of(WeaponProperty.VERSATILE, WeaponProperty.MELEE),
        1, 10, // 1d10 when two-handed
        0, 0
    );

    public static final Weapon PIKE = new Weapon(
        "Pike",
        1, 10, // 1d10
        DamageType.PIERCING,
        Set.of(WeaponProperty.HEAVY, WeaponProperty.REACH, WeaponProperty.TWO_HANDED, WeaponProperty.MELEE)
    );

    public static final Weapon GLAIVE = new Weapon(
        "Glaive",
        1, 10, // 1d10
        DamageType.SLASHING,
        Set.of(WeaponProperty.HEAVY, WeaponProperty.REACH, WeaponProperty.TWO_HANDED, WeaponProperty.MELEE)
    );

    // ============ Martial Ranged Weapons ============

    public static final Weapon LONGBOW = new Weapon(
        "Longbow",
        1, 8, // 1d8
        DamageType.PIERCING,
        0,
        Set.of(WeaponProperty.RANGED, WeaponProperty.HEAVY, WeaponProperty.TWO_HANDED),
        0, 0,
        30, 120 // 150/600 feet = 30/120 blocks
    );

    public static final Weapon HEAVY_CROSSBOW = new Weapon(
        "Heavy Crossbow",
        1, 10, // 1d10
        DamageType.PIERCING,
        0,
        Set.of(WeaponProperty.RANGED, WeaponProperty.HEAVY, WeaponProperty.LOADING, WeaponProperty.TWO_HANDED),
        0, 0,
        20, 80 // 100/400 feet = 20/80 blocks
    );

    public static final Weapon HAND_CROSSBOW = new Weapon(
        "Hand Crossbow",
        1, 6, // 1d6
        DamageType.PIERCING,
        0,
        Set.of(WeaponProperty.RANGED, WeaponProperty.LIGHT, WeaponProperty.LOADING),
        0, 0,
        6, 24 // 30/120 feet = 6/24 blocks
    );

    // ============ Unarmed Strike ============

    public static final Weapon UNARMED_STRIKE = new Weapon(
        "Unarmed Strike",
        1, 1, // 1 + STR modifier
        DamageType.BLUDGEONING,
        Set.of(WeaponProperty.MELEE)
    );

    // ============ Helper Methods ============

    /**
     * Returns a default weapon for a character (unarmed strike).
     */
    public static Weapon getDefaultWeapon() {
        return UNARMED_STRIKE;
    }

    /**
     * Returns a basic starting weapon for Fighter class.
     */
    public static Weapon getStartingWeaponForFighter() {
        return LONGSWORD;
    }
}
