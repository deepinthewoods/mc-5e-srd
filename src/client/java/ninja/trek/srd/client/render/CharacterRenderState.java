package ninja.trek.srd.client.render;

import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import ninja.trek.srd.character.data.CharacterAppearance;

/**
 * Render state for character entities, containing all data needed for rendering.
 */
public class CharacterRenderState extends LivingEntityRenderState {
    public CharacterAppearance appearance;

    // Animation data
    public float limbAngle;
    public float limbDistance;
    public float handSwingProgress;
    public float yaw;
    public float pitch;
}
