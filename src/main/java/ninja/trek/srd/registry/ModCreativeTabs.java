package ninja.trek.srd.registry;

import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import ninja.trek.srd.FiveESrdMod;

/**
 * Creative mode tabs for the 5E SRD mod.
 */
public class ModCreativeTabs {

    public static final CreativeModeTab FIVE_E_SRD_TAB = Registry.register(
        BuiltInRegistries.CREATIVE_MODE_TAB,
        ResourceLocation.fromNamespaceAndPath(FiveESrdMod.MOD_ID, "5e_srd"),
        FabricItemGroup.builder()
            .title(Component.translatable("itemGroup." + FiveESrdMod.MOD_ID))
            .icon(() -> new ItemStack(ModBlocks.ENCOUNTER_BLOCK))
            .displayItems((parameters, output) -> {
                // Add all mod items to the creative tab
                output.accept(ModBlocks.ENCOUNTER_BLOCK);
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
