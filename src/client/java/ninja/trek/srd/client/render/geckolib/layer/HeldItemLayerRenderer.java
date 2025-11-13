package ninja.trek.srd.client.render.geckolib.layer;

import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.item.ItemRenderer;
import net.minecraft.client.render.model.json.ModelTransformationMode;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.RotationAxis;
import ninja.trek.srd.character.entity.CharacterEntity;
import ninja.trek.srd.character.skeleton.HumanoidBones;
import ninja.trek.srd.client.render.geckolib.CharacterGeoRenderState;
import software.bernie.geckolib.cache.object.BakedGeoModel;
import software.bernie.geckolib.cache.object.GeoBone;
import software.bernie.geckolib.renderer.GeoRenderer;
import software.bernie.geckolib.renderer.layer.GeoRenderLayer;
import org.joml.Vector3f;

/**
 * Held item layer renderer for character entities.
 * Renders items held in the character's hands, attached to the hand bones.
 *
 * Based on IMPLEMENTATION_PLAN.md Phase 7 specification.
 */
public class HeldItemLayerRenderer extends GeoRenderLayer<CharacterEntity, CharacterGeoRenderState> {

    private final ItemRenderer itemRenderer;

    public HeldItemLayerRenderer(GeoRenderer<CharacterEntity, CharacterGeoRenderState> renderer,
                                 ItemRenderer itemRenderer) {
        super(renderer);
        this.itemRenderer = itemRenderer;
    }

    @Override
    public void render(MatrixStack poseStack, VertexConsumerProvider bufferSource,
                       int packedLight, CharacterGeoRenderState renderState,
                       float partialTick, float limbSwing, float limbSwingAmount) {

        BakedGeoModel model = getRenderer().getGeoModel().getBakedModel(
            getRenderer().getGeoModel().getModelResource(renderState));

        // Render main hand item
        String mainHandModel = renderState.layerConfiguration.getMainHandModel();
        if (mainHandModel != null && !mainHandModel.isEmpty()) {
            renderHandItem(poseStack, bufferSource, packedLight, model,
                HumanoidBones.HAND_RIGHT, ItemStack.EMPTY, true);
        }

        // Render off hand item
        String offHandModel = renderState.layerConfiguration.getOffHandModel();
        if (offHandModel != null && !offHandModel.isEmpty()) {
            renderHandItem(poseStack, bufferSource, packedLight, model,
                HumanoidBones.HAND_LEFT, ItemStack.EMPTY, false);
        }
    }

    /**
     * Render an item attached to a hand bone.
     */
    private void renderHandItem(MatrixStack poseStack, VertexConsumerProvider bufferSource,
                                int packedLight, BakedGeoModel model, String boneName,
                                ItemStack itemStack, boolean isMainHand) {

        GeoBone handBone = model.getBone(boneName).orElse(null);
        if (handBone == null) {
            return;
        }

        poseStack.push();

        // Position at hand bone
        Vector3f bonePosition = handBone.getWorldSpacePosition();
        poseStack.translate(bonePosition.x, bonePosition.y, bonePosition.z);

        // Apply bone rotation
        // TODO: Apply proper bone rotation matrix

        // Apply item-specific transforms
        applyHandItemTransform(poseStack, isMainHand);

        // Render the item
        if (!itemStack.isEmpty()) {
            itemRenderer.renderItem(
                itemStack,
                isMainHand ? ModelTransformationMode.THIRD_PERSON_RIGHT_HAND
                          : ModelTransformationMode.THIRD_PERSON_LEFT_HAND,
                packedLight,
                0, // overlay
                poseStack,
                bufferSource,
                null, // world
                0 // seed
            );
        }

        poseStack.pop();
    }

    /**
     * Apply hand-specific transforms for held items.
     */
    private void applyHandItemTransform(MatrixStack poseStack, boolean isMainHand) {
        if (isMainHand) {
            // Main hand: rotate and position for weapon grip
            poseStack.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(-90));
            poseStack.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(90));
            poseStack.translate(0, -0.05, 0);
        } else {
            // Off hand: mirror for left hand
            poseStack.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(90));
            poseStack.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(-90));
            poseStack.translate(0, -0.05, 0);
        }
    }
}
