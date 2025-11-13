package ninja.trek.srd.client.render.geckolib;

import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.RotationAxis;
import ninja.trek.srd.character.entity.CharacterEntity;
import ninja.trek.srd.client.model.geckolib.CharacterGeoModel;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

/**
 * GeckoLib renderer for CharacterEntity.
 * Handles rendering of character models with proper scaling and transformations.
 *
 * Based on IMPLEMENTATION_PLAN.md Phase 2 specification.
 */
public class CharacterGeoRenderer extends GeoEntityRenderer<CharacterEntity> {

    public CharacterGeoRenderer(EntityRendererFactory.Context context) {
        super(context, new CharacterGeoModel());
        this.shadowRadius = 0.5f;
    }

    @Override
    public void render(CharacterEntity entity, float entityYaw, float partialTick,
                      MatrixStack poseStack, VertexConsumerProvider bufferSource, int packedLight) {

        // Apply size scaling
        float scale = entity.getSizeScale();
        poseStack.scale(scale, scale, scale);

        // Adjust shadow radius based on scale
        this.shadowRadius = 0.5f * scale;

        super.render(entity, entityYaw, partialTick, poseStack, bufferSource, packedLight);
    }

    @Override
    protected void applyRotations(CharacterEntity entity, MatrixStack poseStack, float ageInTicks,
                                 float rotationYaw, float partialTick, float nativeScale) {
        super.applyRotations(entity, poseStack, ageInTicks, rotationYaw, partialTick, nativeScale);

        // Additional rotations can be added here if needed
        // For example, leaning based on velocity, swimming rotation, etc.
    }
}
