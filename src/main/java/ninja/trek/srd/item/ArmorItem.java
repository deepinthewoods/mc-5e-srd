package ninja.trek.srd.item;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.text.Text;
import ninja.trek.srd.equipment.ArmorType;

import java.util.function.Consumer;

/**
 * Item representing a piece of armor from the 5e SRD.
 */
public class ArmorItem extends Item {
    private final ArmorType armorType;

    public ArmorItem(Settings settings, ArmorType armorType) {
        super(settings);
        this.armorType = armorType;
    }

    public ArmorType getArmorType() {
        return armorType;
    }

    @Override
    public void appendTooltip(ItemStack stack, TooltipContext context, net.minecraft.component.type.TooltipDisplayComponent displayComponent, Consumer<Text> tooltip, TooltipType type) {
        super.appendTooltip(stack, context, displayComponent, tooltip, type);

        // Add armor information
        tooltip.accept(Text.literal("AC: " + armorType.getBaseAC()));
        tooltip.accept(Text.literal("Type: " + armorType.getCategory().name()));

        if (armorType.hasStealthDisadvantage()) {
            tooltip.accept(Text.literal("§cStealth Disadvantage"));
        }

        if (armorType.hasStrengthRequirement()) {
            tooltip.accept(Text.literal("§eStrength Requirement"));
        }
    }
}
