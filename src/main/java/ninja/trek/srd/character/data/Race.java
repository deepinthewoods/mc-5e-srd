package ninja.trek.srd.character.data;

import com.mojang.serialization.Codec;
import net.minecraft.util.StringRepresentable;

/**
 * Represents playable races from the 5e SRD.
 * Initial implementation includes Human and Dwarf.
 */
public enum Race implements StringRepresentable {
    HUMAN(
        "human",
        6,  // 30 feet = 6 blocks
        0,  // default body index
        0,  // default legs index
        0,  // default arms index
        0,  // default head index
        false // no darkvision
    ),
    DWARF(
        "dwarf",
        5,  // 25 feet = 5 blocks (slow speed)
        1,  // dwarf body
        1,  // dwarf legs
        1,  // dwarf arms
        2,  // dwarf head
        true // darkvision 60ft
    );

    public static final Codec<Race> CODEC = StringRepresentable.fromEnum(Race::values);

    private final String name;
    private final int baseMovementSpeed;
    private final int defaultBodyIndex;
    private final int defaultLegsIndex;
    private final int defaultArmsIndex;
    private final int defaultHeadIndex;
    private final boolean hasDarkvision;

    Race(String name, int baseMovementSpeed, int defaultBodyIndex, int defaultLegsIndex,
         int defaultArmsIndex, int defaultHeadIndex, boolean hasDarkvision) {
        this.name = name;
        this.baseMovementSpeed = baseMovementSpeed;
        this.defaultBodyIndex = defaultBodyIndex;
        this.defaultLegsIndex = defaultLegsIndex;
        this.defaultArmsIndex = defaultArmsIndex;
        this.defaultHeadIndex = defaultHeadIndex;
        this.hasDarkvision = hasDarkvision;
    }

    @Override
    public String getSerializedName() {
        return name;
    }

    public String getName() {
        return name;
    }

    public int getBaseMovementSpeed() {
        return baseMovementSpeed;
    }

    public int getDefaultBodyIndex() {
        return defaultBodyIndex;
    }

    public int getDefaultLegsIndex() {
        return defaultLegsIndex;
    }

    public int getDefaultArmsIndex() {
        return defaultArmsIndex;
    }

    public int getDefaultHeadIndex() {
        return defaultHeadIndex;
    }

    public boolean hasDarkvision() {
        return hasDarkvision;
    }

    /**
     * Deserialize Race from string name.
     * API: Used by Entity.readAdditionalSaveData(CompoundTag) - Minecraft 1.21.10
     */
    public static Race fromSerializedName(String name) {
        for (Race race : values()) {
            if (race.getSerializedName().equals(name)) {
                return race;
            }
        }
        return HUMAN; // default fallback
    }
}
