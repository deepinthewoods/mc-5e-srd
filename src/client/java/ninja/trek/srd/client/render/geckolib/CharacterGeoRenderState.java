package ninja.trek.srd.client.render.geckolib;

import net.minecraft.client.render.entity.state.LivingEntityRenderState;
import ninja.trek.srd.character.data.CharacterAppearance;
import ninja.trek.srd.character.data.Race;
import ninja.trek.srd.character.layer.LayerConfiguration;
import software.bernie.geckolib.renderer.base.GeoRenderState;

/**
 * GeckoLib render state for character entities.
 * Contains all data needed for rendering including race, appearance, and layer configuration.
 */
public class CharacterGeoRenderState extends LivingEntityRenderState implements GeoRenderState {
    public Race race = Race.HUMAN;
    public CharacterAppearance appearance;
    public LayerConfiguration layerConfiguration;
    public float sizeScale = 1.0f;

    // Animation data
    public boolean isMoving;
    public boolean isSprinting;
    public boolean isAttacking;
}
