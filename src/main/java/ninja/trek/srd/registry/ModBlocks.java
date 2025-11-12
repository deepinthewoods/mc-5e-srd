package ninja.trek.srd.registry;

import net.minecraft.registry.Registry;
import net.minecraft.registry.Registries;
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
        new EncounterBlock(AbstractBlock.Settings.create()
            .mapColor(MapColor.RED)
            .strength(1.0f)
            .sounds(BlockSoundGroup.STONE)
            .requiresTool()
        )
    );

    /**
     * Register a block.
     */
    private static Block register(String name, Block block) {
        return Registry.register(
            Registries.BLOCK,
            Identifier.of(FiveESrdMod.MOD_ID, name),
            block
        );
    }

    /**
     * Register a block with its corresponding block item.
     */
    private static Block registerWithItem(String name, Block block) {
        Block registered = register(name, block);

        // Register corresponding item
        Registry.register(
            Registries.ITEM,
            Identifier.of(FiveESrdMod.MOD_ID, name),
            new BlockItem(registered, new Item.Settings())
        );

        return registered;
    }

    /**
     * Initialize all blocks.
     */
    public static void initialize() {
        FiveESrdMod.LOGGER.info("Registering blocks for " + FiveESrdMod.MOD_ID);
    }
}
