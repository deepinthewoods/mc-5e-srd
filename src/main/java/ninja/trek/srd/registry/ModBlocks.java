package ninja.trek.srd.registry;

import net.minecraft.registry.Registry;
import net.minecraft.registry.Registries;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.util.Identifier;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.block.Block;
import net.minecraft.sound.BlockSoundGroup;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.MapColor;
import ninja.trek.srd.FiveESrdMod;
import ninja.trek.srd.block.EncounterBlock;

/**
 * Registry for custom blocks.
 */
public class ModBlocks {

    public static final Block ENCOUNTER_BLOCK = registerWithItem(
        "encounter_block",
        settings -> new EncounterBlock(settings),
        AbstractBlock.Settings.create()
            .mapColor(MapColor.RED)
            .strength(1.0f)
            .sounds(BlockSoundGroup.STONE)
            .requiresTool()
    );

    /**
     * Register a block.
     */
    private static Block register(String name, Block block, RegistryKey<Block> blockKey) {
        return Registry.register(Registries.BLOCK, blockKey, block);
    }

    /**
     * Register a block with its corresponding block item using a factory function.
     * This ensures the registry key is set in the settings before the block is constructed.
     */
    private static Block registerWithItem(String name, java.util.function.Function<AbstractBlock.Settings, Block> blockFactory, AbstractBlock.Settings settings) {
        // Create registry keys
        RegistryKey<Block> blockKey = RegistryKey.of(RegistryKeys.BLOCK, Identifier.of(FiveESrdMod.MOD_ID, name));
        RegistryKey<Item> itemKey = RegistryKey.of(RegistryKeys.ITEM, Identifier.of(FiveESrdMod.MOD_ID, name));

        // Set the registry key in the settings and create the block
        Block block = blockFactory.apply(settings.registryKey(blockKey));

        // Register the block
        Registry.register(Registries.BLOCK, blockKey, block);

        // Register corresponding item with its registry key
        Item.Settings itemSettings = new Item.Settings()
            .useBlockPrefixedTranslationKey()
            .registryKey(itemKey);
        Registry.register(Registries.ITEM, itemKey, new BlockItem(block, itemSettings));

        return block;
    }

    /**
     * Initialize all blocks.
     */
    public static void initialize() {
        FiveESrdMod.LOGGER.info("Registering blocks for " + FiveESrdMod.MOD_ID);
    }
}
