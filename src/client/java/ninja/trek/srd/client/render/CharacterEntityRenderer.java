package ninja.trek.srd.client.render;

import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.gl.RenderPipelines;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.RenderPhase;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.render.command.OrderedRenderCommandQueue;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.state.CameraRenderState;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RotationAxis;
import ninja.trek.srd.FiveESrdMod;
import ninja.trek.srd.character.entity.CharacterEntity;
import ninja.trek.srd.client.model.CharacterModelLoader;

import java.util.Map;

/**
 * Renderer for CharacterEntity with full GLTF-based modular rendering.
 *
 * Renders characters using modular mesh parts loaded from GLTF files.
 * Each character is composed of body, legs, arms, and head parts selected
 * based on their CharacterAppearance component.
 */
public class CharacterEntityRenderer extends EntityRenderer<CharacterEntity, CharacterRenderState> {

    private static final Identifier TEXTURE = Identifier.of(
        FiveESrdMod.MOD_ID,
        "textures/entity/character_atlas.png"
    );
    private static final RenderLayer CHARACTER_LAYER = createLayer();

    public CharacterEntityRenderer(EntityRendererFactory.Context context) {
        super(context);
        this.shadowRadius = 0.5f;
    }

    @Override
    public CharacterRenderState createRenderState() {
        return new CharacterRenderState();
    }

    @Override
    public void updateRenderState(CharacterEntity entity, CharacterRenderState state, float partialTick) {
        super.updateRenderState(entity, state, partialTick);

        // Extract appearance data
        state.appearance = entity.getAppearance();

        // Extract animation data
        state.limbAngle = entity.limbAngle;
        state.limbDistance = entity.limbDistance;
        state.handSwingProgress = entity.handSwingProgress;
        state.yaw = entity.getYaw();
        state.pitch = entity.getPitch();
        state.bodyYaw = entity.bodyYaw;
    }

    public void render(
            CharacterRenderState state,
            MatrixStack matrices,
            OrderedRenderCommandQueue queue,
            CameraRenderState cameraState
    ) {
        Map<String, CharacterModelLoader.MeshPart> meshParts = CharacterModelLoader.getMeshParts();
        if (meshParts.isEmpty()) {
            // No meshes loaded yet, skip rendering
            super.render(state, matrices, queue, cameraState);
            return;
        }

        matrices.push();

        // Rotate the entire mesh based on body yaw (movement direction)
        matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(180.0f - state.bodyYaw));

        var layer = CHARACTER_LAYER;
        int overlay = OverlayTexture.DEFAULT_UV;
        int light = state.light;

        // Calculate head rotation relative to body
        float headYawRotation = state.yaw - state.bodyYaw;
        float headPitchRotation = state.pitch;

        // Get mesh parts based on appearance
        CharacterModelLoader.MeshPart bodyMesh = meshParts.get(state.appearance.getBodyMesh());
        CharacterModelLoader.MeshPart legsMesh = meshParts.get(state.appearance.getLegsMesh());
        CharacterModelLoader.MeshPart armsMesh = meshParts.get(state.appearance.getArmsMesh());
        CharacterModelLoader.MeshPart headMesh = meshParts.get(state.appearance.getHeadMesh());

        // Render body (static)
        if (bodyMesh != null) {
            renderMesh(matrices, queue, layer, bodyMesh, overlay, light);
        }

        // Render legs with walk animation
        if (legsMesh != null) {
            renderAnimatedLegs(matrices, queue, layer, legsMesh, state.limbAngle, state.limbDistance, overlay, light);
        }

        // Render arms with swing animation
        if (armsMesh != null) {
            renderAnimatedArms(matrices, queue, layer, armsMesh, state.limbAngle, state.limbDistance,
                    state.handSwingProgress, overlay, light);
        }

        // Render head with look direction
        if (headMesh != null) {
            renderAnimatedHead(matrices, queue, layer, headMesh, headYawRotation, headPitchRotation, overlay, light);
        }

        matrices.pop();
        super.render(state, matrices, queue, cameraState);
    }

    /**
     * Render animated legs with walking motion.
     */
    private void renderAnimatedLegs(MatrixStack matrices, OrderedRenderCommandQueue queue, RenderLayer layer,
                                    CharacterModelLoader.MeshPart legs, float limbAngle, float limbDistance,
                                    int overlay, int light) {
        // Rotate legs back and forth based on walking (similar to player model)
        float legRotation = MathHelper.cos(limbAngle * 0.6662F) * 1.4F * limbDistance;

        matrices.push();
        matrices.translate(legs.pivotX(), legs.pivotY(), legs.pivotZ());
        matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(legRotation));
        matrices.translate(-legs.pivotX(), -legs.pivotY(), -legs.pivotZ());
        renderMesh(matrices, queue, layer, legs, overlay, light);
        matrices.pop();
    }

    /**
     * Render animated arms with walking and attack motion.
     */
    private void renderAnimatedArms(MatrixStack matrices, OrderedRenderCommandQueue queue, RenderLayer layer,
                                    CharacterModelLoader.MeshPart arms, float limbAngle, float limbDistance,
                                    float handSwingProgress, int overlay, int light) {
        // Combine walking arm swing with attack swing
        float armRotation = MathHelper.cos(limbAngle * 0.6662F + (float) Math.PI) * 2.0F * limbDistance * 0.5F;

        // Add attack swing if in progress
        if (handSwingProgress > 0) {
            float attackRotation = MathHelper.sin(MathHelper.sqrt(handSwingProgress) * (float) Math.PI * 2.0F) * 60.0F;
            armRotation += attackRotation;
        }

        matrices.push();
        matrices.translate(arms.pivotX(), arms.pivotY(), arms.pivotZ());
        matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(armRotation));
        matrices.translate(-arms.pivotX(), -arms.pivotY(), -arms.pivotZ());
        renderMesh(matrices, queue, layer, arms, overlay, light);
        matrices.pop();
    }

    /**
     * Render animated head with look direction.
     */
    private void renderAnimatedHead(MatrixStack matrices, OrderedRenderCommandQueue queue, RenderLayer layer,
                                    CharacterModelLoader.MeshPart head, float headYawRotation, float headPitchRotation,
                                    int overlay, int light) {
        matrices.push();
        matrices.translate(head.pivotX(), head.pivotY(), head.pivotZ());

        // Apply head rotation based on look direction
        matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(headYawRotation));
        matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(headPitchRotation));

        matrices.translate(-head.pivotX(), -head.pivotY(), -head.pivotZ());
        renderMesh(matrices, queue, layer, head, overlay, light);
        matrices.pop();
    }

    /**
     * Render a single mesh part.
     */
    private void renderMesh(MatrixStack matrices, OrderedRenderCommandQueue queue, RenderLayer layer,
                           CharacterModelLoader.MeshPart mesh, int overlay, int light) {
        queue.submitCustom(matrices, layer, (entry, consumer) -> {
            int[] indices = mesh.indices();
            if (indices.length < 3) return;
            for (int i = 0; i <= indices.length - 3; i += 3) {
                emitVertex(entry, consumer, mesh, indices[i], overlay, light);
                emitVertex(entry, consumer, mesh, indices[i + 1], overlay, light);
                emitVertex(entry, consumer, mesh, indices[i + 2], overlay, light);
            }
        });
    }

    /**
     * Create render layer for character rendering.
     */
    private static RenderLayer createLayer() {
        RenderPipeline pipeline = RenderPipeline.builder(RenderPipelines.ENTITY_SNIPPET)
                .withLocation(Identifier.of(FiveESrdMod.MOD_ID, "pipeline/character_triangles"))
                .withVertexFormat(VertexFormats.POSITION_COLOR_TEXTURE_OVERLAY_LIGHT_NORMAL, VertexFormat.DrawMode.TRIANGLES)
                .build();
        RenderLayer.MultiPhaseParameters params = RenderLayer.MultiPhaseParameters.builder()
                .texture(new RenderPhase.Texture(TEXTURE, false))
                .lightmap(RenderLayer.ENABLE_LIGHTMAP)
                .overlay(RenderLayer.ENABLE_OVERLAY_COLOR)
                .build(true);
        return RenderLayer.of("character_triangles", 1536, true, true, pipeline, params);
    }

    /**
     * Emit a single vertex to the consumer.
     */
    private static void emitVertex(MatrixStack.Entry entry, VertexConsumer consumer,
                                   CharacterModelLoader.MeshPart mesh, int vertexIndex, int overlay, int light) {
        float[] positions = mesh.positions();
        float[] normals = mesh.normals();
        float[] uvs = mesh.uvs();
        int posBase = vertexIndex * 3;
        int uvBase = vertexIndex * 2;
        float px = positions[posBase];
        float py = positions[posBase + 1];
        float pz = positions[posBase + 2];
        float nx = normals[posBase];
        float ny = normals[posBase + 1];
        float nz = normals[posBase + 2];
        float u = uvs[uvBase];
        float v = uvs[uvBase + 1];

        consumer.vertex(entry, px, py, pz)
                .color(255, 255, 255, 255)
                .texture(u, v)
                .overlay(overlay)
                .light(light)
                .normal(entry, nx, ny, nz);
    }
}
