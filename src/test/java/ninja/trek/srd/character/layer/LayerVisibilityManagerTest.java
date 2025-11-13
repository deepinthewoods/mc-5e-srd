package ninja.trek.srd.character.layer;

/**
 * Example usage and test scenarios for LayerVisibilityManager.
 * This demonstrates how the visibility system works in practice.
 */
public class LayerVisibilityManagerTest {

    /**
     * Example 1: Helmet hides hair and ears
     */
    public static void testHelmetVisibility() {
        LayerConfiguration config = new LayerConfiguration();
        LayerVisibilityManager manager = new LayerVisibilityManager();

        // Initially, hair and ears are visible
        assert config.isShowHair() == true;
        assert config.isShowEars() == true;

        // Equip a plate helmet
        config.setHelmetModel("plate_helmet");
        // Visibility is automatically updated by setHelmetModel()

        // Hair and ears should now be hidden
        assert config.isShowHair() == false;
        assert config.isShowEars() == false;

        System.out.println("✓ Helmet correctly hides hair and ears");
    }

    /**
     * Example 2: Open helmet preserves visibility
     */
    public static void testOpenHelmet() {
        LayerConfiguration config = new LayerConfiguration();
        LayerVisibilityManager manager = new LayerVisibilityManager();

        // Equip an open helmet
        config.setHelmetModel("helmet_open");

        // Hair and ears should still be visible
        assert config.isShowHair() == true;
        assert config.isShowEars() == true;

        System.out.println("✓ Open helmet preserves hair and ear visibility");
    }

    /**
     * Example 3: Robe hides underlying equipment
     */
    public static void testRobeVisibility() {
        LayerConfiguration config = new LayerConfiguration();
        LayerVisibilityManager manager = new LayerVisibilityManager();

        // Equip chest and leg armor
        config.setChestArmorModel("wizard_robe");
        config.setLegArmorModel("leather_leggings");

        // Robe should hide leg armor layer
        assert manager.shouldRenderEquipmentLayer(EquipmentLayerSlot.LEGS, config) == false;

        // But chest (the robe itself) should render
        assert manager.shouldRenderEquipmentLayer(EquipmentLayerSlot.CHEST, config) == true;

        System.out.println("✓ Robe correctly hides underlying equipment");
    }

    /**
     * Example 4: Coverage calculation
     */
    public static void testCoverage() {
        LayerConfiguration config = new LayerConfiguration();
        LayerVisibilityManager manager = new LayerVisibilityManager();

        // No equipment - no coverage
        assert manager.getBodyPartCoverage("torso", config) == 0.0f;

        // Equip leather armor (light coverage)
        config.setChestArmorModel("leather_chestplate");
        float lightCoverage = manager.getBodyPartCoverage("torso", config);
        assert lightCoverage == 0.25f; // 25% coverage

        // Equip plate armor (heavy coverage)
        config.setChestArmorModel("plate_chestplate");
        float heavyCoverage = manager.getBodyPartCoverage("torso", config);
        assert heavyCoverage == 0.75f; // 75% coverage

        System.out.println("✓ Coverage calculation works correctly");
        System.out.println("  Light armor: " + (lightCoverage * 100) + "% coverage");
        System.out.println("  Heavy armor: " + (heavyCoverage * 100) + "% coverage");
    }

    /**
     * Example 5: Bone visibility optimization
     */
    public static void testBoneVisibility() {
        LayerConfiguration config = new LayerConfiguration();
        LayerVisibilityManager manager = new LayerVisibilityManager();

        // Initially all bones should render
        assert manager.shouldRenderBone("hair", config) == true;
        assert manager.shouldRenderBone("ear_left", config) == true;

        // Equip helmet
        config.setHelmetModel("plate_helmet");

        // Hair and ear bones should not render (optimization)
        assert manager.shouldRenderBone("hair", config) == false;
        assert manager.shouldRenderBone("ear_left", config) == false;
        assert manager.shouldRenderBone("ear_right", config) == false;

        // Other bones should still render
        assert manager.shouldRenderBone("head", config) == true;
        assert manager.shouldRenderBone("torso", config) == true;

        System.out.println("✓ Bone visibility optimization working");
    }

    /**
     * Example 6: Custom visibility rules
     */
    public static void testCustomRules() {
        LayerVisibilityManager manager = new LayerVisibilityManager();

        // Add a custom rule for a modded helmet
        manager.addCustomRule("dragon_helmet", false, true, false);
        // Dragon helmet shows hair but hides ears

        LayerConfiguration config = new LayerConfiguration();
        config.setHelmetModel("dragon_helmet");

        assert config.isShowHair() == true;  // Hair visible
        assert config.isShowEars() == false; // Ears hidden

        System.out.println("✓ Custom visibility rules work correctly");
    }

    /**
     * Example 7: Visibility summary for debugging
     */
    public static void testVisibilitySummary() {
        LayerConfiguration config = new LayerConfiguration();
        LayerVisibilityManager manager = new LayerVisibilityManager();

        // Equip some armor
        config.setHelmetModel("plate_helmet");
        config.setChestArmorModel("chain_hauberk");
        config.setLegArmorModel("leather_leggings");
        config.setBootArmorModel("iron_boots");

        // Get visibility summary
        String summary = manager.getVisibilitySummary(config);

        System.out.println("✓ Visibility summary generated:");
        System.out.println(summary);
    }

    /**
     * Example 8: Equipment removal restores visibility
     */
    public static void testEquipmentRemoval() {
        LayerConfiguration config = new LayerConfiguration();
        LayerVisibilityManager manager = new LayerVisibilityManager();

        // Equip helmet
        config.setHelmetModel("plate_helmet");
        assert config.isShowHair() == false;

        // Remove helmet
        config.setHelmetModel(null);
        manager.updateVisibility(config);
        assert config.isShowHair() == true;

        System.out.println("✓ Equipment removal restores visibility");
    }

    /**
     * Run all test examples
     */
    public static void main(String[] args) {
        System.out.println("=== LayerVisibilityManager Test Examples ===\n");

        testHelmetVisibility();
        testOpenHelmet();
        testRobeVisibility();
        testCoverage();
        testBoneVisibility();
        testCustomRules();
        testVisibilitySummary();
        testEquipmentRemoval();

        System.out.println("\n=== All tests passed! ===");
    }
}
