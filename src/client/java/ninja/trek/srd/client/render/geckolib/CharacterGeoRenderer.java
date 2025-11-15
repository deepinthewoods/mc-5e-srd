package ninja.trek.srd.client.render.geckolib;

import net.minecraft.client.render.command.OrderedRenderCommandQueue;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.state.CameraRenderState;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.EntityType;
import ninja.trek.srd.character.entity.CharacterEntity;
import ninja.trek.srd.character.skeleton.SkeletonProfile;
import ninja.trek.srd.client.model.geckolib.CharacterGeoModel;
import ninja.trek.srd.client.render.animation.BoneRetargetingController;
import ninja.trek.srd.client.render.geckolib.layer.EquipmentLayerRenderer;
import ninja.trek.srd.client.render.geckolib.layer.HeldItemLayerRenderer;
import software.bernie.geckolib.cache.object.BakedGeoModel;
import software.bernie.geckolib.cache.object.GeoBone;
import software.bernie.geckolib.model.GeoModel;
import software.bernie.geckolib.renderer.GeoEntityRenderer;
import org.joml.Vector3f;

/**
 * GeckoLib renderer for CharacterEntity.
 * Handles rendering of character models with proper scaling, transformations, and bone retargeting.
 * Supports multiple render layers for equipment and held items.
 *
 * Based on IMPLEMENTATION_PLAN.md Phase 2-6 specification.
 */
public class CharacterGeoRenderer extends GeoEntityRenderer<CharacterEntity, CharacterGeoRenderState> {

    private final BoneRetargetingController retargeting = new BoneRetargetingController(SkeletonProfile.BASE);

    public CharacterGeoRenderer(EntityRendererFactory.Context context) {
        super(context, new CharacterGeoModel());
        this.shadowRadius = 0.5f;

        // TODO: Re-enable equipment layer renderer once GeckoLib 5 API is fully documented
        // The addRenderLayer method signature has changed in GeckoLib 5
        // Equipment layers will be added in a future update
        // addRenderLayer(new EquipmentLayerRenderer(this));

        // TODO: Re-enable held item layer renderer once API is confirmed
        // context.getItemInHandRenderer() is not available in current API
        // addRenderLayer(new HeldItemLayerRenderer(this, itemRenderer));
    }

    @Override
    public CharacterGeoRenderState createRenderState(CharacterEntity entity, Void relatedObject) {
        return new CharacterGeoRenderState();
    }

    @Override
    public GeoModel<CharacterEntity> getGeoModel() {
        return (GeoModel<CharacterEntity>) super.getGeoModel();
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

        // Held items (Phase 7.1)
        state.mainHandStack = entity.getMainHandStack();
        state.offHandStack = entity.getOffHandStack();
    }

    @Override
    public void scaleModelForRender(CharacterGeoRenderState state, float widthScale, float heightScale,
                                     MatrixStack poseStack, BakedGeoModel model, CameraRenderState cameraState) {
        super.scaleModelForRender(state, widthScale, heightScale, poseStack, model, cameraState);

        // Apply custom size scaling
        float scale = state.sizeScale;
        poseStack.scale(scale, scale, scale);
    }

    @Override
    public void preApplyRenderLayers(CharacterGeoRenderState renderState, MatrixStack poseStack,
                                     BakedGeoModel model, OrderedRenderCommandQueue renderQueue,
                                     CameraRenderState cameraState, int packedLight, int packedOverlay,
                                     int renderColor, boolean reRender) {
        super.preApplyRenderLayers(renderState, poseStack, model, renderQueue, cameraState,
                                   packedLight, packedOverlay, renderColor, reRender);

        // TODO: Fix bone retargeting - currently causing rendering issues
        // The bone position modification needs to be done differently to avoid
        // breaking the model structure
        // applyBoneRetargeting(renderState, model);
    }

    /**
     * Apply bone retargeting to adapt animations to different race proportions.
     * This modifies bone positions based on the skeleton profile ratios.
     */
    private void applyBoneRetargeting(CharacterGeoRenderState renderState, BakedGeoModel model) {
        SkeletonProfile targetProfile = SkeletonProfile.getByRaceName(renderState.race.getName());
        float sizeScale = renderState.sizeScale;

        // Apply retargeting to all bones
        for (GeoBone bone : model.topLevelBones()) {
            applyBoneRetargetingRecursive(bone, targetProfile, sizeScale);
        }
    }

    /**
     * Recursively apply bone retargeting to a bone and its children.
     */
    private void applyBoneRetargetingRecursive(GeoBone bone, SkeletonProfile targetProfile, float sizeScale) {
        // Get the bone's current position (using getModelPosition for local position)
        // Note: getModelPosition returns Vector3d, need to convert to Vector3f
        org.joml.Vector3d positionD = bone.getModelPosition();
        Vector3f position = new Vector3f((float)positionD.x, (float)positionD.y, (float)positionD.z);

        // Retarget the position based on skeleton profile
        Vector3f retargetedPosition = retargeting.retargetPosition(
            bone.getName(),
            new Vector3f(position),
            targetProfile,
            sizeScale
        );

        // Apply the retargeted position
        bone.updatePosition(retargetedPosition.x, retargetedPosition.y, retargetedPosition.z);

        // Apply to children recursively
        for (GeoBone child : bone.getChildBones()) {
            applyBoneRetargetingRecursive(child, targetProfile, sizeScale);
        }
    }
}
