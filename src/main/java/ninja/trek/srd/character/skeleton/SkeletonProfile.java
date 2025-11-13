package ninja.trek.srd.character.skeleton;

import java.util.HashMap;
import java.util.Map;

/**
 * Defines bone lengths and proportions for a specific race.
 * Used for animation retargeting to adapt animations across different body proportions.
 *
 * All measurements are in Minecraft units (1 unit = 1 meter).
 * Based on GECKOLIB_ARCHITECTURE.md and ANIMATION_RETARGETING.md specifications.
 */
public class SkeletonProfile {

    private final String name;
    private final Map<String, Float> boneLengths;
    private final float totalHeight;
    private final float legLength;

    // Standard profiles for each race
    public static final SkeletonProfile HUMAN = createHuman();
    public static final SkeletonProfile DWARF = createDwarf();
    public static final SkeletonProfile ELF = createElf();
    public static final SkeletonProfile HALFLING = createHalfling();

    // Base reference for retargeting (using human as base)
    public static final SkeletonProfile BASE = HUMAN;

    public SkeletonProfile(String name, Map<String, Float> boneLengths, float totalHeight) {
        this.name = name;
        this.boneLengths = new HashMap<>(boneLengths);
        this.totalHeight = totalHeight;

        // Calculate total leg length
        this.legLength = boneLengths.getOrDefault(HumanoidBones.LEG_RIGHT, 0.5f)
                + boneLengths.getOrDefault(HumanoidBones.SHIN_RIGHT, 0.45f);
    }

    /**
     * Get the length of a specific bone.
     */
    public float getBoneLength(String boneName) {
        return boneLengths.getOrDefault(boneName, 0.0f);
    }

    /**
     * Get the ratio of this bone length compared to the base skeleton.
     */
    public float getBoneLengthRatio(String boneName) {
        float thisLength = getBoneLength(boneName);
        float baseLength = BASE.getBoneLength(boneName);

        if (baseLength == 0.0f) {
            return 1.0f;
        }

        return thisLength / baseLength;
    }

    /**
     * Get total height of this skeleton.
     */
    public float getTotalHeight() {
        return totalHeight;
    }

    /**
     * Get total leg length (thigh + shin).
     */
    public float getLegLength() {
        return legLength;
    }

    /**
     * Get leg length ratio compared to base skeleton.
     */
    public float getLegLengthRatio() {
        return legLength / BASE.getLegLength();
    }

    /**
     * Get the skeleton name.
     */
    public String getName() {
        return name;
    }

    /**
     * Create human skeleton profile (base reference).
     */
    private static SkeletonProfile createHuman() {
        Map<String, Float> bones = new HashMap<>();

        // Torso
        bones.put(HumanoidBones.TORSO_UPPER, 0.6f);
        bones.put(HumanoidBones.TORSO_LOWER, 0.4f);

        // Head
        bones.put(HumanoidBones.HEAD, 0.3f);

        // Arms
        bones.put(HumanoidBones.ARM_RIGHT, 0.4f);
        bones.put(HumanoidBones.FOREARM_RIGHT, 0.35f);
        bones.put(HumanoidBones.ARM_LEFT, 0.4f);
        bones.put(HumanoidBones.FOREARM_LEFT, 0.35f);

        // Legs
        bones.put(HumanoidBones.LEG_RIGHT, 0.5f);
        bones.put(HumanoidBones.SHIN_RIGHT, 0.45f);
        bones.put(HumanoidBones.LEG_LEFT, 0.5f);
        bones.put(HumanoidBones.SHIN_LEFT, 0.45f);

        return new SkeletonProfile("human", bones, 1.8f);
    }

    /**
     * Create dwarf skeleton profile (shorter, stockier).
     */
    private static SkeletonProfile createDwarf() {
        Map<String, Float> bones = new HashMap<>();

        // Torso (proportionally larger)
        bones.put(HumanoidBones.TORSO_UPPER, 0.5f);
        bones.put(HumanoidBones.TORSO_LOWER, 0.35f);

        // Head (proportionally larger)
        bones.put(HumanoidBones.HEAD, 0.32f);

        // Arms (shorter)
        bones.put(HumanoidBones.ARM_RIGHT, 0.35f);
        bones.put(HumanoidBones.FOREARM_RIGHT, 0.3f);
        bones.put(HumanoidBones.ARM_LEFT, 0.35f);
        bones.put(HumanoidBones.FOREARM_LEFT, 0.3f);

        // Legs (much shorter)
        bones.put(HumanoidBones.LEG_RIGHT, 0.35f);
        bones.put(HumanoidBones.SHIN_RIGHT, 0.3f);
        bones.put(HumanoidBones.LEG_LEFT, 0.35f);
        bones.put(HumanoidBones.SHIN_LEFT, 0.3f);

        return new SkeletonProfile("dwarf", bones, 1.3f);
    }

    /**
     * Create elf skeleton profile (taller, slender).
     */
    private static SkeletonProfile createElf() {
        Map<String, Float> bones = new HashMap<>();

        // Torso (longer)
        bones.put(HumanoidBones.TORSO_UPPER, 0.65f);
        bones.put(HumanoidBones.TORSO_LOWER, 0.45f);

        // Head (proportionally smaller)
        bones.put(HumanoidBones.HEAD, 0.28f);

        // Arms (longer)
        bones.put(HumanoidBones.ARM_RIGHT, 0.45f);
        bones.put(HumanoidBones.FOREARM_RIGHT, 0.4f);
        bones.put(HumanoidBones.ARM_LEFT, 0.45f);
        bones.put(HumanoidBones.FOREARM_LEFT, 0.4f);

        // Legs (longer)
        bones.put(HumanoidBones.LEG_RIGHT, 0.6f);
        bones.put(HumanoidBones.SHIN_RIGHT, 0.55f);
        bones.put(HumanoidBones.LEG_LEFT, 0.6f);
        bones.put(HumanoidBones.SHIN_LEFT, 0.55f);

        return new SkeletonProfile("elf", bones, 2.0f);
    }

    /**
     * Create halfling skeleton profile (smallest).
     */
    private static SkeletonProfile createHalfling() {
        Map<String, Float> bones = new HashMap<>();

        // Torso (compact)
        bones.put(HumanoidBones.TORSO_UPPER, 0.4f);
        bones.put(HumanoidBones.TORSO_LOWER, 0.3f);

        // Head (proportionally large)
        bones.put(HumanoidBones.HEAD, 0.3f);

        // Arms (short)
        bones.put(HumanoidBones.ARM_RIGHT, 0.3f);
        bones.put(HumanoidBones.FOREARM_RIGHT, 0.25f);
        bones.put(HumanoidBones.ARM_LEFT, 0.3f);
        bones.put(HumanoidBones.FOREARM_LEFT, 0.25f);

        // Legs (very short)
        bones.put(HumanoidBones.LEG_RIGHT, 0.3f);
        bones.put(HumanoidBones.SHIN_RIGHT, 0.25f);
        bones.put(HumanoidBones.LEG_LEFT, 0.3f);
        bones.put(HumanoidBones.SHIN_LEFT, 0.25f);

        return new SkeletonProfile("halfling", bones, 1.0f);
    }

    /**
     * Get a skeleton profile by race name.
     */
    public static SkeletonProfile getByRaceName(String raceName) {
        return switch (raceName.toLowerCase()) {
            case "human" -> HUMAN;
            case "dwarf" -> DWARF;
            case "elf" -> ELF;
            case "halfling" -> HALFLING;
            default -> HUMAN; // Default to human
        };
    }
}
