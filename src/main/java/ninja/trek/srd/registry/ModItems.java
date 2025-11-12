package ninja.trek.srd.registry;

import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;
import ninja.trek.srd.FiveESrdMod;
import ninja.trek.srd.item.CharacterCreationItem;

/**
 * Registry for custom items.
 */
public class ModItems {

    public static final Item CHARACTER_CREATION_TOME = register(
        "character_creation_tome",
        new CharacterCreationItem(new Item.Settings().maxCount(1))
    );

    /**
     * Register an item.
     */
    private static Item register(String name, Item item) {
        return Registry.register(
            Registries.ITEM,
            Identifier.of(FiveESrdMod.MOD_ID, name),
            item
        );
    }

    /**
     * Initialize all items.
     */
    public static void initialize() {
        FiveESrdMod.LOGGER.info("Registering items for " + FiveESrdMod.MOD_ID);
    }
}
