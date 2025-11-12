package ninja.trek.srd.registry;

import net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry;
import net.minecraft.registry.Registry;
import net.minecraft.registry.Registries;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.util.Identifier;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnGroup;
import ninja.trek.srd.FiveESrdMod;
import ninja.trek.srd.character.entity.CharacterEntity;

/**
 * Registry for custom entity types.
 */
public class ModEntities {

    public static final EntityType<CharacterEntity> CHARACTER = register(
        "character",
        EntityType.Builder.create(CharacterEntity::new, SpawnGroup.CREATURE)
            .dimensions(0.6f, 1.8f) // Similar to player dimensions
    );

    /**
     * Register an entity type with its registry key.
     * This ensures the registry key is passed to build() as required by Minecraft 1.21.2+.
     */
    private static <T extends net.minecraft.entity.Entity> EntityType<T> register(
        String name,
        EntityType.Builder<T> builder
    ) {
        RegistryKey<EntityType<?>> entityKey = RegistryKey.of(
            RegistryKeys.ENTITY_TYPE,
            Identifier.of(FiveESrdMod.MOD_ID, name)
        );

        // Build with the registry key
        EntityType<T> entityType = builder.build(entityKey);

        // Register the entity type
        return Registry.register(Registries.ENTITY_TYPE, entityKey, entityType);
    }

    /**
     * Initialize entity types and register attributes.
     */
    public static void initialize() {
        FiveESrdMod.LOGGER.info("Registering entities for " + FiveESrdMod.MOD_ID);

        // Register attributes
        FabricDefaultAttributeRegistry.register(CHARACTER, CharacterEntity.createAttributes());
    }
}
