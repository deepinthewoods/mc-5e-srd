package ninja.trek.srd.character.layer;

import ninja.trek.srd.equipment.ArmorType;

import java.util.HashMap;
import java.util.Map;

/**
 * Manages layer visibility rules for character rendering.
 * Implements smart visibility logic to optimize rendering by hiding layers
 * that are covered by equipment (helmets hide hair, armor hides clothing, etc.).
 *
 * Based on LAYER_SYSTEM.md Phase 7.2 specification.
 */
public class LayerVisibilityManager {

    /**
     * Visibility rules for different equipment types.
     */
    private static final Map<String, VisibilityRule> EQUIPMENT_RULES = new HashMap<>();

    static {
        // Helmet rules - most helmets hide hair and ears
        EQUIPMENT_RULES.put("helmet_default", new VisibilityRule(true, true, false));
        EQUIPMENT_RULES.put("helmet_open", new VisibilityRule(false, false, false));
        EQUIPMENT_RULES.put("helmet_full", new VisibilityRule(true, true, true));

        // Specific armor type rules
        EQUIPMENT_RULES.put("plate_helmet", new VisibilityRule(true, true, false));
        EQUIPMENT_RULES.put("chainmail_coif", new VisibilityRule(true, true, false));
        EQUIPMENT_RULES.put("leather_cap", new VisibilityRule(false, false, false));
        EQUIPMENT_RULES.put("wizard_hat", new VisibilityRule(false, false, false));

        // Robes hide underlying clothing
        EQUIPMENT_RULES.put("robe_default", new VisibilityRule(false, false, false));
    }

    /**
     * Defines what a piece of equipment hides.
     */
    private static class VisibilityRule {
        final boolean hidesHair;
        final boolean hidesEars;
        final boolean hidesFacialHair;
        final boolean hidesUnderClothing;

        VisibilityRule(boolean hidesHair, boolean hidesEars, boolean hidesFacialHair) {
            this(hidesHair, hidesEars, hidesFacialHair, false);
        }

        VisibilityRule(boolean hidesHair, boolean hidesEars, boolean hidesFacialHair, boolean hidesUnderClothing) {
            this.hidesHair = hidesHair;
            this.hidesEars = hidesEars;
            this.hidesFacialHair = hidesFacialHair;
            this.hidesUnderClothing = hidesUnderClothing;
        }
    }

    /**
     * Update visibility flags in the layer configuration based on equipped items.
     *
     * @param config The layer configuration to update
     */
    public void updateVisibility(LayerConfiguration config) {
        // Reset to default visibility
        config.setShowHair(true);
        config.setShowEars(true);
        config.setShowCape(true);

        // Apply helmet visibility rules
        if (config.hasEquipmentInSlot(EquipmentLayerSlot.HELMET)) {
            applyHelmetVisibility(config);
        }

        // Apply chest armor visibility rules
        if (config.hasEquipmentInSlot(EquipmentLayerSlot.CHEST)) {
            applyChestArmorVisibility(config);
        }
    }

    /**
     * Apply visibility rules for helmets.
     * Most helmets hide hair and ears.
     */
    private void applyHelmetVisibility(LayerConfiguration config) {
        String helmetModel = config.getHelmetModel();

        if (helmetModel == null) {
            return;
        }

        // Check for specific rules
        VisibilityRule rule = EQUIPMENT_RULES.get(helmetModel);

        if (rule == null) {
            // Default helmet behavior
            rule = EQUIPMENT_RULES.get("helmet_default");
        }

        // Apply the rule
        if (rule.hidesHair) {
            config.setShowHair(false);
        }

        if (rule.hidesEars) {
            config.setShowEars(false);
        }
    }

    /**
     * Apply visibility rules for chest armor.
     * Heavy armor and robes may hide underlying clothing.
     */
    private void applyChestArmorVisibility(LayerConfiguration config) {
        String chestModel = config.getChestArmorModel();

        if (chestModel == null) {
            return;
        }

        // Robes hide base clothing
        if (chestModel.contains("robe")) {
            // Mark that underlying clothing should not be rendered
            // This would be used by the renderer to skip clothing layers
        }
    }

    /**
     * Determine if a body part should be visible based on equipment.
     *
     * @param bodyPart The name of the body part (e.g., "hair", "ears")
     * @param config The layer configuration
     * @return true if the body part should be rendered, false if it should be hidden
     */
    public boolean shouldRenderBodyPart(String bodyPart, LayerConfiguration config) {
        return switch (bodyPart.toLowerCase()) {
            case "hair" -> config.isShowHair();
            case "ears" -> config.isShowEars();
            case "cape" -> config.isShowCape();
            default -> true; // Unknown parts are visible by default
        };
    }

    /**
     * Determine if an equipment layer should be rendered based on coverage rules.
     * This optimization prevents rendering layers that are completely hidden.
     *
     * @param slot The equipment slot to check
     * @param config The layer configuration
     * @return true if the layer should be rendered, false if it can be skipped
     */
    public boolean shouldRenderEquipmentLayer(EquipmentLayerSlot slot, LayerConfiguration config) {
        // Always render if the slot has equipment
        if (!config.hasEquipmentInSlot(slot)) {
            return false;
        }

        // Check if this layer is covered by a higher layer
        return switch (slot) {
            case BOOTS -> true; // Always render boots
            case LEGS -> !isCoveredByRobe(config); // Hide if robe is equipped
            case CHEST -> !isCoveredByRobe(config); // Hide if robe is equipped
            case HELMET -> true; // Always render helmet
            case CAPE -> config.isShowCape(); // Respect cape visibility
            case MAIN_HAND, OFF_HAND -> true; // Always render held items
        };
    }

    /**
     * Check if body/leg armor is covered by a robe.
     */
    private boolean isCoveredByRobe(LayerConfiguration config) {
        String chestModel = config.getChestArmorModel();
        return chestModel != null && chestModel.contains("robe");
    }

    /**
     * Get the armor coverage level for visibility calculations.
     *
     * @param armorType The armor type
     * @return The coverage level (NONE, LIGHT, MEDIUM, HEAVY)
     */
    public CoverageLevel getCoverageLevel(ArmorType armorType) {
        if (armorType == null) {
            return CoverageLevel.NONE;
        }

        return switch (armorType.getCategory()) {
            case LIGHT -> CoverageLevel.LIGHT;
            case MEDIUM -> CoverageLevel.MEDIUM;
            case HEAVY -> CoverageLevel.HEAVY;
            case SHIELD -> CoverageLevel.NONE;
        };
    }

    /**
     * Get the armor coverage level from a model name.
     * This is useful when we only have the model ID.
     *
     * @param modelName The equipment model name
     * @return The estimated coverage level
     */
    public CoverageLevel getCoverageLevelFromModel(String modelName) {
        if (modelName == null) {
            return CoverageLevel.NONE;
        }

        String lower = modelName.toLowerCase();

        // Robes provide full coverage
        if (lower.contains("robe")) {
            return CoverageLevel.HEAVY;
        }

        // Heavy armor types
        if (lower.contains("plate") || lower.contains("splint")) {
            return CoverageLevel.HEAVY;
        }

        // Medium armor types
        if (lower.contains("chain") || lower.contains("scale") || lower.contains("breastplate")) {
            return CoverageLevel.MEDIUM;
        }

        // Light armor types
        if (lower.contains("leather") || lower.contains("padded") || lower.contains("studded")) {
            return CoverageLevel.LIGHT;
        }

        return CoverageLevel.LIGHT;
    }

    /**
     * Determines how much a piece of equipment covers the base body.
     * Used for optimization - higher coverage means more base geometry can be skipped.
     */
    public enum CoverageLevel {
        NONE(0.0f),      // No coverage (shields, accessories)
        LIGHT(0.25f),    // Minimal coverage (leather, padded)
        MEDIUM(0.5f),    // Moderate coverage (chain, scale)
        HEAVY(0.75f);    // Full coverage (plate, robes)

        private final float coverageRatio;

        CoverageLevel(float coverageRatio) {
            this.coverageRatio = coverageRatio;
        }

        public float getCoverageRatio() {
            return coverageRatio;
        }
    }

    /**
     * Calculate how much of a body part is covered by equipment.
     * This can be used to optimize rendering by skipping fully covered geometry.
     *
     * @param bodyPart The body part name (e.g., "torso", "legs")
     * @param config The layer configuration
     * @return Coverage ratio from 0.0 (uncovered) to 1.0 (fully covered)
     */
    public float getBodyPartCoverage(String bodyPart, LayerConfiguration config) {
        return switch (bodyPart.toLowerCase()) {
            case "torso", "chest", "body" -> {
                if (config.hasEquipmentInSlot(EquipmentLayerSlot.CHEST)) {
                    String model = config.getChestArmorModel();
                    CoverageLevel level = getCoverageLevelFromModel(model);
                    yield level.getCoverageRatio();
                }
                yield 0.0f;
            }
            case "legs" -> {
                if (config.hasEquipmentInSlot(EquipmentLayerSlot.LEGS)) {
                    String model = config.getLegArmorModel();
                    CoverageLevel level = getCoverageLevelFromModel(model);
                    yield level.getCoverageRatio();
                }
                yield 0.0f;
            }
            case "head" -> {
                if (config.hasEquipmentInSlot(EquipmentLayerSlot.HELMET)) {
                    String model = config.getHelmetModel();
                    CoverageLevel level = getCoverageLevelFromModel(model);
                    yield level.getCoverageRatio();
                }
                yield 0.0f;
            }
            case "feet" -> {
                if (config.hasEquipmentInSlot(EquipmentLayerSlot.BOOTS)) {
                    yield 0.5f; // Boots typically cover 50% of feet
                }
                yield 0.0f;
            }
            default -> 0.0f; // Unknown parts are uncovered
        };
    }

    /**
     * Add a custom visibility rule for a specific equipment model.
     *
     * @param modelName The equipment model name
     * @param hidesHair Whether this equipment hides hair
     * @param hidesEars Whether this equipment hides ears
     * @param hidesFacialHair Whether this equipment hides facial hair
     */
    public void addCustomRule(String modelName, boolean hidesHair, boolean hidesEars, boolean hidesFacialHair) {
        EQUIPMENT_RULES.put(modelName, new VisibilityRule(hidesHair, hidesEars, hidesFacialHair));
    }

    /**
     * Check if rendering a specific bone should be skipped for optimization.
     * This is the main optimization method that renderers should use.
     *
     * @param boneName The name of the bone to check
     * @param config The layer configuration
     * @return true if the bone should be rendered, false if it can be skipped
     */
    public boolean shouldRenderBone(String boneName, LayerConfiguration config) {
        String lower = boneName.toLowerCase();

        // Hair bones
        if (lower.contains("hair")) {
            return config.isShowHair();
        }

        // Ear bones
        if (lower.contains("ear")) {
            return config.isShowEars();
        }

        // Cape bones
        if (lower.contains("cape")) {
            return config.isShowCape();
        }

        // Check body coverage for optimization
        // If a body part is 75%+ covered, we could potentially skip rendering it
        if (lower.contains("torso") || lower.contains("chest")) {
            float coverage = getBodyPartCoverage("torso", config);
            // Only skip if completely covered (1.0) - partial coverage still needs base
            return coverage < 1.0f;
        }

        // Default: render the bone
        return true;
    }

    /**
     * Generate a summary of visibility state for debugging.
     *
     * @param config The layer configuration
     * @return A human-readable summary of what's visible/hidden
     */
    public String getVisibilitySummary(LayerConfiguration config) {
        StringBuilder summary = new StringBuilder();
        summary.append("Visibility State:\n");
        summary.append("  Hair: ").append(config.isShowHair() ? "visible" : "hidden").append("\n");
        summary.append("  Ears: ").append(config.isShowEars() ? "visible" : "hidden").append("\n");
        summary.append("  Cape: ").append(config.isShowCape() ? "visible" : "hidden").append("\n");

        summary.append("\nEquipment Coverage:\n");
        summary.append("  Head: ").append(String.format("%.0f%%", getBodyPartCoverage("head", config) * 100)).append("\n");
        summary.append("  Chest: ").append(String.format("%.0f%%", getBodyPartCoverage("torso", config) * 100)).append("\n");
        summary.append("  Legs: ").append(String.format("%.0f%%", getBodyPartCoverage("legs", config) * 100)).append("\n");
        summary.append("  Feet: ").append(String.format("%.0f%%", getBodyPartCoverage("feet", config) * 100)).append("\n");

        return summary.toString();
    }
}
