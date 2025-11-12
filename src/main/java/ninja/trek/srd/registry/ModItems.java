package ninja.trek.srd.registry;

import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.util.Identifier;
import ninja.trek.srd.FiveESrdMod;
import ninja.trek.srd.item.CharacterCreationItem;

/**
 * Registry for custom items.
 */
public class ModItems {

    public static final Item CHARACTER_CREATION_TOME = register(
        "character_creation_tome",
        settings -> new CharacterCreationItem(settings),
        new Item.Settings().maxCount(1)
    );

    /**
     * Register an item using a factory function.
     * This ensures the registry key is set in the settings before the item is constructed.
     */
    private static Item register(String name, java.util.function.Function<Item.Settings, Item> itemFactory, Item.Settings settings) {
        // Create registry key
        RegistryKey<Item> itemKey = RegistryKey.of(RegistryKeys.ITEM, Identifier.of(FiveESrdMod.MOD_ID, name));

        // Set the registry key in the settings and create the item
        Item item = itemFactory.apply(settings.registryKey(itemKey));

        // Register the item
        return Registry.register(Registries.ITEM, itemKey, item);
    }

    /**
     * Initialize all items.
     */
    public static void initialize() {
        FiveESrdMod.LOGGER.info("Registering items for " + FiveESrdMod.MOD_ID);
    }
}
