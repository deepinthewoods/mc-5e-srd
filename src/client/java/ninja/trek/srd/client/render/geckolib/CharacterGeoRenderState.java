package ninja.trek.srd.client.render.geckolib;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.client.render.entity.state.LivingEntityRenderState;
import ninja.trek.srd.character.data.CharacterAppearance;
import ninja.trek.srd.character.data.Race;
import ninja.trek.srd.character.layer.LayerConfiguration;
import software.bernie.geckolib.constant.dataticket.DataTicket;
import software.bernie.geckolib.renderer.base.GeoRenderState;

import java.util.Map;

/**
 * GeckoLib render state for character entities.
 * Contains all data needed for rendering including race, appearance, and layer configuration.
 */
public class CharacterGeoRenderState extends LivingEntityRenderState implements GeoRenderState {
    private final Map<DataTicket<?>, Object> geckolibDataMap = new Object2ObjectOpenHashMap<>();

    public Race race = Race.HUMAN;
    public CharacterAppearance appearance;
    public LayerConfiguration layerConfiguration;
    public float sizeScale = 1.0f;

    // Animation data
    public boolean isMoving;
    public boolean isSprinting;
    public boolean isAttacking;

    @Override
    public Map<DataTicket<?>, Object> getDataMap() {
        return geckolibDataMap;
    }

    @Override
    public <D> D getGeckolibData(DataTicket<D> dataTicket) {
        return (D) geckolibDataMap.get(dataTicket);
    }

    @Override
    public boolean hasGeckolibData(DataTicket<?> dataTicket) {
        return geckolibDataMap.containsKey(dataTicket);
    }

    @Override
    public <D> void addGeckolibData(DataTicket<D> dataTicket, D data) {
        geckolibDataMap.put(dataTicket, data);
    }
}
