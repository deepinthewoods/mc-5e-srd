package ninja.trek.srd.registry;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import ninja.trek.srd.FiveESrdMod;
import ninja.trek.srd.block.EncounterBlock;

/**
 * Registry for custom blocks.
 */
public class ModBlocks {

    public static final Block ENCOUNTER_BLOCK = registerWithItem(
        "encounter_block",
        new EncounterBlock(BlockBehaviour.Properties.of()
            .mapColor(MapColor.COLOR_RED)
            .strength(1.0f)
            .sound(SoundType.STONE)
            .requiresCorrectToolForDrops()
        )
    );

    /**
     * Register a block.
     */
    private static Block register(String name, Block block) {
        return Registry.register(
            BuiltInRegistries.BLOCK,
            ResourceLocation.fromNamespaceAndPath(FiveESrdMod.MOD_ID, name),
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
            BuiltInRegistries.ITEM,
            ResourceLocation.fromNamespaceAndPath(FiveESrdMod.MOD_ID, name),
            new BlockItem(registered, new Item.Properties())
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
