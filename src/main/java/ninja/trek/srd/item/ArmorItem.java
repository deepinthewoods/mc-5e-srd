package ninja.trek.srd.item;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.text.Text;
import ninja.trek.srd.equipment.ArmorType;

import java.util.List;

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
    public void appendTooltip(ItemStack stack, TooltipContext context, List<Text> tooltip, TooltipType type) {
        super.appendTooltip(stack, context, tooltip, type);

        // Add armor information
        tooltip.add(Text.literal("AC: " + armorType.getBaseAC()));
        tooltip.add(Text.literal("Type: " + armorType.getCategory().name()));

        if (armorType.hasStealthDisadvantage()) {
            tooltip.add(Text.literal("§cStealth Disadvantage"));
        }

        if (armorType.hasStrengthRequirement()) {
            tooltip.add(Text.literal("§eStrength Requirement"));
        }
    }
}
