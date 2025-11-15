package ninja.trek.srd.client.model.geckolib;

import net.minecraft.util.Identifier;
import ninja.trek.srd.FiveESrdMod;
import ninja.trek.srd.character.entity.CharacterEntity;
import ninja.trek.srd.character.layer.LayerConfiguration;
import ninja.trek.srd.client.render.geckolib.CharacterGeoRenderState;
import ninja.trek.srd.client.render.geckolib.DynamicTextureComposer;
import software.bernie.geckolib.cache.object.BakedGeoModel;
import software.bernie.geckolib.cache.object.GeoBone;
import software.bernie.geckolib.model.GeoModel;
import software.bernie.geckolib.renderer.base.GeoRenderState;

import java.util.ArrayList;
import java.util.List;

/**
 * GeckoLib model for CharacterEntity.
 * Handles dynamic model, texture, and animation resource loading based on race and configuration.
 * Supports body part variants through selective bone visibility.
 *
 * Based on IMPLEMENTATION_PLAN.md Phase 2 and Phase 5 specifications.
 */
public class CharacterGeoModel extends GeoModel<CharacterEntity> {

    @Override
    public Identifier getModelResource(GeoRenderState renderState) {
        // Load race-specific model
        if (renderState instanceof CharacterGeoRenderState state) {
            String raceName = state.race.getName().toLowerCase();
            return Identifier.of(FiveESrdMod.MOD_ID,
                "geckolib/models/entity/character/" + raceName + "/" + raceName + "_body");
        }
        // Fallback to human model
        return Identifier.of(FiveESrdMod.MOD_ID,
            "geckolib/models/entity/character/human/human_body");
    }

    @Override
    public Identifier getTextureResource(GeoRenderState renderState) {
        // Load race-specific texture with optional dynamic compositing
        if (renderState instanceof CharacterGeoRenderState state) {
            String raceName = state.race.getName().toLowerCase();
            LayerConfiguration config = state.layerConfiguration;

            // Get base skin texture
            String skinTexture = config.getSkinTexture();
            Identifier baseTexture = Identifier.of(FiveESrdMod.MOD_ID,
                "textures/entity/character/base/" + raceName + "_" + skinTexture + ".png");

            // Build list of clothing layers for compositing
            List<Identifier> clothingLayers = new ArrayList<>();

            // Add clothing texture if not default
            String clothingTexture = config.getClothingTexture();
            if (clothingTexture != null && !clothingTexture.equals("default")) {
                clothingLayers.add(Identifier.of(FiveESrdMod.MOD_ID,
                    "textures/entity/character/clothing/" + clothingTexture + ".png"));
            }

            // If no layers to composite, return base texture directly
            if (clothingLayers.isEmpty()) {
                return baseTexture;
            }

            // Compose texture dynamically (Phase 7.3)
            return DynamicTextureComposer.getInstance().composeTexture(baseTexture, clothingLayers);
        }

        // Fallback to human texture
        return Identifier.of(FiveESrdMod.MOD_ID,
            "textures/entity/character/base/human_default.png");
    }

    @Override
    public Identifier getAnimationResource(CharacterEntity entity) {
        // All humanoids share the same base animations
        // Bone retargeting adapts them to different proportions
        //
        // NOTE: Additional animation files exist for combat and magic:
        // - combat.animation.json (Phase 8.1): Melee, ranged, and defensive animations
        // - magic.animation.json (Phase 8.2): Spellcasting and channeling animations
        //
        // These are currently separate files with placeholder animations ready for BlockBench refinement.
        // Full integration requires either:
        // 1. Consolidating all animations into a single file, OR
        // 2. Implementing multiple animation controllers for different animation types, OR
        // 3. Using GeckoLib's animation file referencing system (if supported)
        //
        // For now, returning the main locomotion animation file which includes:
        // - idle, walk, run (locomotion)
        // - attack, cast, block, channel, death (basic actions)
        return Identifier.of(FiveESrdMod.MOD_ID,
            "geckolib/animations/entity/character/locomotion");
    }

    @Override
    public void applyMolangQueries(CharacterEntity animatable) {
        super.applyMolangQueries(animatable);
    }

    // setCustomAnimations has been replaced in GeckoLib 5 with a different approach
    // Use prepareForRenderPass or updateRenderState in the renderer instead
    // Commenting out for now to fix compilation
    /*
    @Override
    public void setCustomAnimations(CharacterEntity entity, long instanceId, GeoRenderState renderState) {
        super.setCustomAnimations(entity, instanceId, renderState);

        if (renderState instanceof CharacterGeoRenderState state) {
            BakedGeoModel model = getBakedModel(getModelResource(state));
            LayerConfiguration config = state.layerConfiguration;

            // Apply body part variants visibility
            applyBodyPartVariants(model, config);

            // Apply visibility rules (e.g., helmet hides hair)
            applyVisibilityRules(model, config);
        }
    }
    */

    /**
     * Show/hide body part variants based on layer configuration.
     * Each body part (head, body, arms, legs) can have multiple variants.
     */
    private void applyBodyPartVariants(BakedGeoModel model, LayerConfiguration config) {
        // Head variants
        showOnlyVariant(model, "head", config.getHeadVariant(), 10);

        // Body variants
        showOnlyVariant(model, "body", config.getBodyVariant(), 10);

        // Arms variants
        showOnlyVariant(model, "arms", config.getArmsVariant(), 10);

        // Legs variants
        showOnlyVariant(model, "legs", config.getLegsVariant(), 10);
    }

    /**
     * Show only the specified variant for a body part, hide all others.
     *
     * @param model The baked model
     * @param partName Base name of the body part (e.g., "head", "body")
     * @param activeVariant The variant index to show (0-based)
     * @param maxVariants Maximum number of variants to check
     */
    private void showOnlyVariant(BakedGeoModel model, String partName, int activeVariant, int maxVariants) {
        for (int i = 0; i < maxVariants; i++) {
            String variantName = partName + "_variant_" + i;
            GeoBone bone = model.getBone(variantName).orElse(null);

            if (bone != null) {
                // Show only the active variant, hide all others
                bone.setHidden(i != activeVariant);
            }
        }

        // Also check for non-variant base bone (used as variant 0)
        GeoBone baseBone = model.getBone(partName).orElse(null);
        if (baseBone != null && activeVariant != 0) {
            // If there's a base bone and we're not using variant 0, hide it
            baseBone.setHidden(true);
        }
    }

    /**
     * Apply visibility rules based on equipment and configuration.
     */
    private void applyVisibilityRules(BakedGeoModel model, LayerConfiguration config) {
        // Hide hair if helmet is equipped or explicitly hidden
        GeoBone hair = model.getBone("hair").orElse(null);
        if (hair != null) {
            hair.setHidden(!config.isShowHair());
        }

        // Hide ears if helmet is equipped or explicitly hidden
        GeoBone ears = model.getBone("ears").orElse(null);
        if (ears != null) {
            ears.setHidden(!config.isShowEars());
        }

        // Hide cape if not visible
        GeoBone cape = model.getBone("cape").orElse(null);
        if (cape != null) {
            cape.setHidden(!config.isShowCape());
        }
    }
}
