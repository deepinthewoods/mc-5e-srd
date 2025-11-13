package ninja.trek.srd.client.model.geckolib;

import net.minecraft.util.Identifier;
import ninja.trek.srd.FiveESrdMod;
import ninja.trek.srd.character.entity.CharacterEntity;
import software.bernie.geckolib.model.GeoModel;
import software.bernie.geckolib.renderer.base.GeoRenderState;

/**
 * GeckoLib model for CharacterEntity.
 * Handles dynamic model, texture, and animation resource loading based on race and configuration.
 *
 * Based on IMPLEMENTATION_PLAN.md Phase 2 specification.
 */
public class CharacterGeoModel extends GeoModel<CharacterEntity> {

    @Override
    public Identifier getModelResource(GeoRenderState renderState) {
        // In GeckoLib 5, we can't access entity-specific data in model methods
        // Use a default model for all characters
        // TODO: Implement custom RenderState to store race information
        return Identifier.of(FiveESrdMod.MOD_ID,
            "geckolib/models/entity/character/human/human_body.geo.json");
    }

    @Override
    public Identifier getTextureResource(GeoRenderState renderState) {
        // In GeckoLib 5, we can't access entity-specific data here
        // Use a default texture for now - dynamic textures will need a different approach
        // TODO: Implement custom RenderState to store texture information
        return Identifier.of(FiveESrdMod.MOD_ID,
            "textures/entity/character/base/human_default.png");
    }

    @Override
    public Identifier getAnimationResource(CharacterEntity entity) {
        // All humanoids share the same base animations
        // Bone retargeting adapts them to different proportions
        return Identifier.of(FiveESrdMod.MOD_ID,
            "geckolib/animations/entity/character/locomotion.animation.json");
    }
}
