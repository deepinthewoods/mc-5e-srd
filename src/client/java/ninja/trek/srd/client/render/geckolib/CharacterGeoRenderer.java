package ninja.trek.srd.client.render.geckolib;

import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.state.CameraRenderState;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.EntityType;
import ninja.trek.srd.character.entity.CharacterEntity;
import ninja.trek.srd.client.model.geckolib.CharacterGeoModel;
import software.bernie.geckolib.cache.object.BakedGeoModel;
import software.bernie.geckolib.model.GeoModel;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

/**
 * GeckoLib renderer for CharacterEntity.
 * Handles rendering of character models with proper scaling and transformations.
 *
 * Based on IMPLEMENTATION_PLAN.md Phase 2 specification.
 */
public class CharacterGeoRenderer extends GeoEntityRenderer<CharacterEntity, CharacterGeoRenderState> {

    private final CharacterGeoModel model = new CharacterGeoModel();

    public CharacterGeoRenderer(EntityRendererFactory.Context context, EntityType<? extends CharacterEntity> entityType) {
        super(context, entityType);
        this.shadowRadius = 0.5f;
    }

    @Override
    public GeoModel<CharacterEntity> getGeoModel() {
        return model;
    }

    @Override
    public void updateRenderState(CharacterEntity entity, CharacterGeoRenderState state, float tickDelta) {
        super.updateRenderState(entity, state, tickDelta);

        // Copy entity data to render state
        state.race = entity.getRace();
        state.appearance = entity.getAppearance();
        state.layerConfiguration = entity.getLayerConfiguration();
        state.sizeScale = entity.getSizeScale();

        // Animation data
        state.isMoving = entity.limbAnimator.isLimbMoving();
        state.isSprinting = entity.isSprinting();
        state.isAttacking = entity.handSwingProgress > 0;
    }

    @Override
    public void scaleModelForRender(CharacterGeoRenderState state, float widthScale, float heightScale,
                                     MatrixStack poseStack, BakedGeoModel model, CameraRenderState cameraState) {
        super.scaleModelForRender(state, widthScale, heightScale, poseStack, model, cameraState);

        // Apply custom size scaling
        float scale = state.sizeScale;
        poseStack.scale(scale, scale, scale);
    }
}
