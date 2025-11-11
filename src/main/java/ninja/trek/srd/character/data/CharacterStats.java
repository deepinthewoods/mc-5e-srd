package ninja.trek.srd.character.data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

/**
 * Represents the six core ability scores in D&D 5e.
 * Standard range is 1-20, with 10 being average.
 */
public record CharacterStats(
    int strength,
    int dexterity,
    int constitution,
    int intelligence,
    int wisdom,
    int charisma
) {
    public static final Codec<CharacterStats> CODEC = RecordCodecBuilder.create(instance ->
        instance.group(
            Codec.INT.fieldOf("strength").forGetter(CharacterStats::strength),
            Codec.INT.fieldOf("dexterity").forGetter(CharacterStats::dexterity),
            Codec.INT.fieldOf("constitution").forGetter(CharacterStats::constitution),
            Codec.INT.fieldOf("intelligence").forGetter(CharacterStats::intelligence),
            Codec.INT.fieldOf("wisdom").forGetter(CharacterStats::wisdom),
            Codec.INT.fieldOf("charisma").forGetter(CharacterStats::charisma)
        ).apply(instance, CharacterStats::new)
    );

    /**
     * Standard array for ability scores as defined in 5e SRD.
     */
    public static final int[] STANDARD_ARRAY = {15, 14, 13, 12, 10, 8};

    /**
     * Calculate the ability modifier for a given score.
     * Formula: (score - 10) / 2, rounded down.
     */
    public static int getModifier(int score) {
        return Math.floorDiv(score - 10, 2);
    }

    public int getStrengthModifier() {
        return getModifier(strength);
    }

    public int getDexterityModifier() {
        return getModifier(dexterity);
    }

    public int getConstitutionModifier() {
        return getModifier(constitution);
    }

    public int getIntelligenceModifier() {
        return getModifier(intelligence);
    }

    public int getWisdomModifier() {
        return getModifier(wisdom);
    }

    public int getCharismaModifier() {
        return getModifier(charisma);
    }

    /**
     * Create default character stats (all 10s).
     */
    public static CharacterStats createDefault() {
        return new CharacterStats(10, 10, 10, 10, 10, 10);
    }

    /**
     * Apply racial bonuses to ability scores.
     */
    public CharacterStats applyRacialBonuses(Race race) {
        return switch (race) {
            case HUMAN -> new CharacterStats(
                strength + 1,
                dexterity + 1,
                constitution + 1,
                intelligence + 1,
                wisdom + 1,
                charisma + 1
            );
            case DWARF -> new CharacterStats(
                strength,
                dexterity,
                constitution + 2,
                intelligence,
                wisdom,
                charisma
            );
        };
    }
}
