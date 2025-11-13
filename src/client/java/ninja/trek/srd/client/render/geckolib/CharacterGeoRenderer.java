package ninja.trek.srd.client.render.geckolib;

import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.state.LivingEntityRenderState;
import ninja.trek.srd.character.entity.CharacterEntity;
import ninja.trek.srd.client.model.geckolib.CharacterGeoModel;
import software.bernie.geckolib.renderer.GeoEntityRenderer;
import software.bernie.geckolib.renderer.base.GeoRenderState;

/**
 * GeckoLib renderer for CharacterEntity.
 * Handles rendering of character models with proper scaling and transformations.
 *
 * Based on IMPLEMENTATION_PLAN.md Phase 2 specification.
 */
public class CharacterGeoRenderer<R extends LivingEntityRenderState & GeoRenderState>
        extends GeoEntityRenderer<CharacterEntity, R> {

    public CharacterGeoRenderer(EntityRendererFactory.Context context) {
        super(context, new CharacterGeoModel());
    }
}
