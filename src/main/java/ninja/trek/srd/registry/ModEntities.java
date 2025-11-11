package ninja.trek.srd.registry;

import net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import ninja.trek.srd.FiveESrdMod;
import ninja.trek.srd.character.entity.CharacterEntity;

/**
 * Registry for custom entity types.
 */
public class ModEntities {

    public static final EntityType<CharacterEntity> CHARACTER = Registry.register(
        BuiltInRegistries.ENTITY_TYPE,
        ResourceLocation.fromNamespaceAndPath(FiveESrdMod.MOD_ID, "character"),
        EntityType.Builder.of(CharacterEntity::new, MobCategory.CREATURE)
            .sized(0.6f, 1.8f) // Similar to player dimensions
            .clientTrackingRange(10)
            .build(null)
    );

    /**
     * Initialize entity types and register attributes.
     */
    public static void initialize() {
        FiveESrdMod.LOGGER.info("Registering entities for " + FiveESrdMod.MOD_ID);

        // Register attributes
        FabricDefaultAttributeRegistry.register(CHARACTER, CharacterEntity.createAttributes());
    }
}
