package ninja.trek.srd.client.render.geckolib.layer;

import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;
import ninja.trek.srd.FiveESrdMod;
import ninja.trek.srd.character.entity.CharacterEntity;
import ninja.trek.srd.character.layer.EquipmentLayerSlot;
import ninja.trek.srd.character.layer.LayerConfiguration;
import ninja.trek.srd.client.render.geckolib.CharacterGeoRenderState;
import software.bernie.geckolib.cache.object.BakedGeoModel;
import software.bernie.geckolib.cache.object.GeoBone;
import software.bernie.geckolib.renderer.layer.GeoRenderLayer;
import software.bernie.geckolib.renderer.GeoRenderer;

/**
 * Equipment layer renderer for character entities.
 * Renders armor and equipment pieces as separate layers over the base body.
 *
 * Based on IMPLEMENTATION_PLAN.md Phase 6 specification.
 */
public class EquipmentLayerRenderer extends GeoRenderLayer<CharacterEntity, CharacterGeoRenderState> {

    // Inflation offset to prevent Z-fighting with base body
    private static final float ARMOR_INFLATION = 0.05f;

    public EquipmentLayerRenderer(GeoRenderer<CharacterEntity, CharacterGeoRenderState> renderer) {
        super(renderer);
    }

    @Override
    public void render(MatrixStack poseStack, VertexConsumerProvider bufferSource,
                       int packedLight, CharacterGeoRenderState renderState,
                       float partialTick, float limbSwing, float limbSwingAmount) {

        LayerConfiguration config = renderState.layerConfiguration;

        // Don't render if no equipment
        if (!config.hasAnyEquipment()) {
            return;
        }

        // Render each equipment piece
        for (EquipmentLayerSlot slot : EquipmentLayerSlot.values()) {
            if (config.hasEquipmentInSlot(slot)) {
                renderEquipmentPiece(poseStack, bufferSource, packedLight, renderState, slot, partialTick);
            }
        }
    }

    /**
     * Render a single equipment piece.
     */
    private void renderEquipmentPiece(MatrixStack poseStack, VertexConsumerProvider bufferSource,
                                       int packedLight, CharacterGeoRenderState renderState,
                                       EquipmentLayerSlot slot, float partialTick) {

        String equipmentModel = renderState.layerConfiguration.getEquipmentModel(slot);
        if (equipmentModel == null) {
            return;
        }

        // Get the equipment model identifier
        Identifier modelId = getEquipmentModelId(equipmentModel, slot);
        Identifier textureId = getEquipmentTextureId(equipmentModel, slot);

        // TODO: Load and render the equipment model
        // For now, this is a placeholder that will be completed when equipment models are created
        // The implementation will:
        // 1. Load the equipment geo.json model
        // 2. Apply the same bone transformations as the base model
        // 3. Apply inflation offset to prevent Z-fighting
        // 4. Render with the equipment texture
    }

    /**
     * Get the model identifier for an equipment piece.
     */
    private Identifier getEquipmentModelId(String modelName, EquipmentLayerSlot slot) {
        String slotPath = slot.name().toLowerCase();
        return Identifier.of(FiveESrdMod.MOD_ID,
            "geo/entity/character/equipment/" + slotPath + "/" + modelName + ".geo.json");
    }

    /**
     * Get the texture identifier for an equipment piece.
     */
    private Identifier getEquipmentTextureId(String modelName, EquipmentLayerSlot slot) {
        String slotPath = slot.name().toLowerCase();
        return Identifier.of(FiveESrdMod.MOD_ID,
            "textures/entity/character/equipment/" + slotPath + "/" + modelName + ".png");
    }

    /**
     * Apply inflation to a bone to prevent Z-fighting.
     */
    private void applyInflation(GeoBone bone, float inflation) {
        // Scale the bone slightly to create offset
        bone.updateScale(
            1.0f + inflation,
            1.0f + inflation,
            1.0f + inflation
        );
    }
}
