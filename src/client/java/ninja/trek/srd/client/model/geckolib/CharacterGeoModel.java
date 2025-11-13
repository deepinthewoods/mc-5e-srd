package ninja.trek.srd.client.model.geckolib;

import net.minecraft.util.Identifier;
import ninja.trek.srd.FiveESrdMod;
import ninja.trek.srd.character.entity.CharacterEntity;
import ninja.trek.srd.character.layer.LayerConfiguration;
import software.bernie.geckolib.model.GeoModel;

/**
 * GeckoLib model for CharacterEntity.
 * Handles dynamic model, texture, and animation resource loading based on race and configuration.
 *
 * Based on IMPLEMENTATION_PLAN.md Phase 2 specification.
 */
public class CharacterGeoModel extends GeoModel<CharacterEntity> {

    @Override
    public Identifier getModelResource(CharacterEntity entity) {
        // Select model based on race
        String raceName = entity.getRace().getName().toLowerCase();

        // For now, use a single base model per race
        // In Phase 5, this will support body part variants
        return Identifier.of(FiveESrdMod.MOD_ID,
            "geo/entity/character/" + raceName + "/" + raceName + "_body.geo.json");
    }

    @Override
    public Identifier getTextureResource(CharacterEntity entity) {
        // Get skin texture from layer configuration
        LayerConfiguration config = entity.getLayerConfiguration();
        String skinTexture = config.getSkinTexture();
        String raceName = entity.getRace().getName().toLowerCase();

        // Path to race-specific skin texture
        return Identifier.of(FiveESrdMod.MOD_ID,
            "textures/entity/character/base/" + raceName + "_" + skinTexture + ".png");
    }

    @Override
    public Identifier getAnimationResource(CharacterEntity entity) {
        // All humanoids share the same base animations
        // Bone retargeting adapts them to different proportions
        return Identifier.of(FiveESrdMod.MOD_ID,
            "animations/entity/character/locomotion.animation.json");
    }
}
