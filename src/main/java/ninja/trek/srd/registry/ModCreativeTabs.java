package ninja.trek.srd.registry;

import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup;
import net.minecraft.registry.Registry;
import net.minecraft.registry.Registries;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import ninja.trek.srd.FiveESrdMod;

/**
 * Creative mode tabs for the 5E SRD mod.
 */
public class ModCreativeTabs {

    public static final ItemGroup FIVE_E_SRD_TAB = Registry.register(
        Registries.ITEM_GROUP,
        Identifier.of(FiveESrdMod.MOD_ID, "5e_srd"),
        FabricItemGroup.builder()
            .displayName(Text.translatable("itemGroup." + FiveESrdMod.MOD_ID))
            .icon(() -> new ItemStack(ModBlocks.ENCOUNTER_BLOCK))
            .entries((parameters, output) -> {
                // Add all mod items to the creative tab
                output.add(ModBlocks.ENCOUNTER_BLOCK);
                output.add(ModItems.CHARACTER_CREATION_TOME);
                // TODO: Add spawn eggs when implemented
            })
            .build()
    );

    /**
     * Initialize creative tabs.
     */
    public static void initialize() {
        FiveESrdMod.LOGGER.info("Registering creative tabs for " + FiveESrdMod.MOD_ID);
    }
}
