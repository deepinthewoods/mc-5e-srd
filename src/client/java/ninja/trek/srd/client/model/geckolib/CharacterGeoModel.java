package ninja.trek.srd.client.model.geckolib;

import net.minecraft.util.Identifier;
import ninja.trek.srd.FiveESrdMod;
import ninja.trek.srd.character.entity.CharacterEntity;
import ninja.trek.srd.client.render.geckolib.CharacterGeoRenderState;
import software.bernie.geckolib.model.GeoModel;

/**
 * GeckoLib model for CharacterEntity.
 * Handles dynamic model, texture, and animation resource loading based on race and configuration.
 *
 * Based on IMPLEMENTATION_PLAN.md Phase 2 specification.
 */
public class CharacterGeoModel extends GeoModel<CharacterEntity, CharacterGeoRenderState> {

    @Override
    public Identifier getModelResource(CharacterGeoRenderState renderState) {
        // Load race-specific model
        String raceName = renderState.race.getName().toLowerCase();
        return Identifier.of(FiveESrdMod.MOD_ID,
            "geo/entity/character/" + raceName + "/" + raceName + "_body.geo.json");
    }

    @Override
    public Identifier getTextureResource(CharacterGeoRenderState renderState) {
        // Load race-specific default texture
        // TODO: Support custom textures based on appearance configuration
        String raceName = renderState.race.getName().toLowerCase();
        return Identifier.of(FiveESrdMod.MOD_ID,
            "textures/entity/character/base/" + raceName + "_default.png");
    }

    @Override
    public Identifier getAnimationResource(CharacterEntity entity) {
        // All humanoids share the same base animations
        // Bone retargeting adapts them to different proportions
        return Identifier.of(FiveESrdMod.MOD_ID,
            "animations/entity/character/locomotion.animation.json");
    }
}
