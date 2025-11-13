package ninja.trek.srd.client.render.geckolib.layer;

import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.item.ItemRenderer;
import net.minecraft.client.render.model.json.ModelTransformationMode;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ShieldItem;
import net.minecraft.util.math.RotationAxis;
import ninja.trek.srd.character.entity.CharacterEntity;
import ninja.trek.srd.character.skeleton.HumanoidBones;
import ninja.trek.srd.client.render.geckolib.CharacterGeoRenderState;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import software.bernie.geckolib.cache.object.BakedGeoModel;
import software.bernie.geckolib.cache.object.GeoBone;
import software.bernie.geckolib.renderer.GeoRenderer;
import software.bernie.geckolib.renderer.layer.GeoRenderLayer;

/**
 * Held item layer renderer for character entities.
 * Renders items held in the character's hands, attached to the hand bones.
 *
 * Phase 7.1 Implementation:
 * - Attaches items to right_hand and left_hand bones
 * - Uses Minecraft's ItemRenderer for proper item display
 * - Applies bone transformations (position, rotation, scale)
 * - Supports different item types (weapons, shields, blocks, tools)
 *
 * Based on IMPLEMENTATION_PLAN.md Phase 7 specification.
 */
public class HeldItemLayerRenderer extends GeoRenderLayer<CharacterEntity, CharacterGeoRenderState> {

    private final ItemRenderer itemRenderer;

    // Item-specific offsets and rotations for better visual positioning
    private static final float ITEM_SCALE = 1.0f;
    private static final float BLOCK_ITEM_SCALE = 0.5f;

    // Pixel-to-block scale for GeckoLib models (16 pixels = 1 block)
    private static final float PIXEL_SCALE = 1.0f / 16.0f;

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

        // For Phase 7.1, we render based on the layer configuration's model names
        // In a future phase, this could be extended to read from entity inventory

        // Render main hand item
        String mainHandModel = renderState.layerConfiguration.getMainHandModel();
        if (mainHandModel != null && !mainHandModel.isEmpty()) {
            // For now, use empty ItemStack as placeholder
            // TODO Phase 7.2: Integrate with actual item inventory system
            ItemStack mainHandStack = renderState.mainHandStack;
            if (!mainHandStack.isEmpty()) {
                renderHandItem(poseStack, bufferSource, packedLight, model,
                    HumanoidBones.HAND_RIGHT, mainHandStack, true);
            }
        }

        // Render off hand item
        String offHandModel = renderState.layerConfiguration.getOffHandModel();
        if (offHandModel != null && !offHandModel.isEmpty()) {
            ItemStack offHandStack = renderState.offHandStack;
            if (!offHandStack.isEmpty()) {
                renderHandItem(poseStack, bufferSource, packedLight, model,
                    HumanoidBones.HAND_LEFT, offHandStack, false);
            }
        }
    }

    /**
     * Render an item attached to a hand bone.
     * Applies proper bone transformations and item-specific positioning.
     */
    private void renderHandItem(MatrixStack poseStack, VertexConsumerProvider bufferSource,
                                int packedLight, BakedGeoModel model, String boneName,
                                ItemStack itemStack, boolean isMainHand) {

        GeoBone handBone = model.getBone(boneName).orElse(null);
        if (handBone == null || handBone.isHidden()) {
            return;
        }

        poseStack.push();

        // Apply bone's world space transformation
        applyBoneTransform(poseStack, handBone);

        // Apply item-specific transforms based on item type
        applyItemTransform(poseStack, itemStack, isMainHand);

        // Render the item using Minecraft's item renderer
        itemRenderer.renderItem(
            itemStack,
            isMainHand ? ModelTransformationMode.THIRD_PERSON_RIGHT_HAND
                      : ModelTransformationMode.THIRD_PERSON_LEFT_HAND,
            packedLight,
            0, // overlay - no overlay for held items
            poseStack,
            bufferSource,
            null, // world - not needed for basic rendering
            0 // seed - for random texture variation
        );

        poseStack.pop();
    }

    /**
     * Apply the bone's world space transformation to the pose stack.
     * This ensures items follow the hand bone's position and rotation.
     */
    private void applyBoneTransform(MatrixStack poseStack, GeoBone bone) {
        // Get bone's world space position (already in block coordinates)
        Vector3f position = bone.getWorldSpacePosition();
        poseStack.translate(position.x * PIXEL_SCALE, position.y * PIXEL_SCALE, position.z * PIXEL_SCALE);

        // Apply bone's rotation using quaternion
        // GeckoLib bones store rotation as quaternions
        Quaternionf rotation = bone.getWorldSpaceRotation();
        poseStack.multiply(rotation);

        // Note: Scale is typically handled at the model level, not per-bone
        // If needed, bone scale can be retrieved via bone.getScaleX/Y/Z()
    }

    /**
     * Apply item-specific transforms based on the type of item being rendered.
     * Different item types (weapons, shields, blocks) need different positioning.
     */
    private void applyItemTransform(MatrixStack poseStack, ItemStack itemStack, boolean isMainHand) {
        // Special handling for different item types
        if (itemStack.getItem() instanceof ShieldItem) {
            applyShieldTransform(poseStack, isMainHand);
        } else if (itemStack.getItem() instanceof BlockItem) {
            applyBlockItemTransform(poseStack, isMainHand);
        } else {
            // Default transform for weapons and tools
            applyDefaultItemTransform(poseStack, isMainHand);
        }
    }

    /**
     * Apply transform for regular items (weapons, tools, etc.).
     */
    private void applyDefaultItemTransform(MatrixStack poseStack, boolean isMainHand) {
        if (isMainHand) {
            // Right hand: Rotate weapon into natural grip position
            poseStack.multiply(RotationAxis.POSITIVE_X.rotationDegrees(90));
            poseStack.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(0));
            poseStack.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(0));

            // Fine-tune position for better visual alignment
            poseStack.translate(0.0, 0.0, -0.1);

            // Apply standard item scale
            poseStack.scale(ITEM_SCALE, ITEM_SCALE, ITEM_SCALE);
        } else {
            // Left hand: Mirror the right hand transform
            poseStack.multiply(RotationAxis.POSITIVE_X.rotationDegrees(90));
            poseStack.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(0));
            poseStack.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(0));

            poseStack.translate(0.0, 0.0, -0.1);
            poseStack.scale(ITEM_SCALE, ITEM_SCALE, ITEM_SCALE);
        }
    }

    /**
     * Apply transform for shield items.
     * Shields need different positioning and rotation to look natural.
     */
    private void applyShieldTransform(MatrixStack poseStack, boolean isMainHand) {
        if (isMainHand) {
            // Right hand shield (less common, but supported)
            poseStack.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(-90));
            poseStack.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(90));
            poseStack.translate(0.0, -0.1, 0.0);
            poseStack.scale(ITEM_SCALE, ITEM_SCALE, ITEM_SCALE);
        } else {
            // Left hand shield (standard positioning)
            poseStack.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(90));
            poseStack.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(-90));
            poseStack.translate(0.0, -0.1, 0.0);
            poseStack.scale(ITEM_SCALE, ITEM_SCALE, ITEM_SCALE);
        }
    }

    /**
     * Apply transform for block items.
     * Blocks are rendered smaller and with different rotation.
     */
    private void applyBlockItemTransform(MatrixStack poseStack, boolean isMainHand) {
        if (isMainHand) {
            // Right hand block
            poseStack.multiply(RotationAxis.POSITIVE_X.rotationDegrees(90));
            poseStack.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(45));
            poseStack.translate(0.0, 0.0, -0.05);

            // Blocks are rendered smaller in hand
            poseStack.scale(BLOCK_ITEM_SCALE, BLOCK_ITEM_SCALE, BLOCK_ITEM_SCALE);
        } else {
            // Left hand block
            poseStack.multiply(RotationAxis.POSITIVE_X.rotationDegrees(90));
            poseStack.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(-45));
            poseStack.translate(0.0, 0.0, -0.05);
            poseStack.scale(BLOCK_ITEM_SCALE, BLOCK_ITEM_SCALE, BLOCK_ITEM_SCALE);
        }
    }
}
