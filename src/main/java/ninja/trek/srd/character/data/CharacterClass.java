package ninja.trek.srd.character.data;

import com.mojang.serialization.Codec;
import net.minecraft.util.StringIdentifiable;

/**
 * Represents character classes from the 5e SRD.
 * Initial implementation starts with Fighter only.
 */
public enum CharacterClass implements StringIdentifiable {
    FIGHTER(
        "fighter",
        10,  // hit die (d10)
        2    // proficiency bonus at level 1
    );

    public static final Codec<CharacterClass> CODEC = StringIdentifiable.createCodec(CharacterClass::values);

    private final String name;
    private final int hitDie;
    private final int baseProficiencyBonus;

    CharacterClass(String name, int hitDie, int baseProficiencyBonus) {
        this.name = name;
        this.hitDie = hitDie;
        this.baseProficiencyBonus = baseProficiencyBonus;
    }

    @Override
    public String asString() {
        return name;
    }

    public String getName() {
        return name;
    }

    public int getHitDie() {
        return hitDie;
    }

    /**
     * Calculate proficiency bonus for a given level.
     * Formula: floor((level - 1) / 4) + 2
     */
    public int getProficiencyBonus(int level) {
        return Math.floorDiv(level - 1, 4) + 2;
    }
}
