package ninja.trek.srd.client.render.geckolib;

import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.util.math.MatrixStack;
import ninja.trek.srd.character.entity.CharacterEntity;
import ninja.trek.srd.client.model.geckolib.CharacterGeoModel;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

/**
 * GeckoLib renderer for CharacterEntity.
 * Handles rendering of character models with proper scaling and transformations.
 *
 * Based on IMPLEMENTATION_PLAN.md Phase 2 specification.
 */
public class CharacterGeoRenderer extends GeoEntityRenderer<CharacterEntity, CharacterGeoRenderState> {

    public CharacterGeoRenderer(EntityRendererFactory.Context context) {
        super(context, new CharacterGeoModel());
        this.shadowRadius = 0.5f;
    }

    @Override
    public CharacterGeoRenderState createRenderState() {
        return new CharacterGeoRenderState();
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
    public void preRender(MatrixStack poseStack, CharacterGeoRenderState state, int packedLight) {
        super.preRender(poseStack, state, packedLight);

        // Apply size scaling
        float scale = state.sizeScale;
        poseStack.scale(scale, scale, scale);
    }
}
