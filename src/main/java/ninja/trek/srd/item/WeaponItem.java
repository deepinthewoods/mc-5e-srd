package ninja.trek.srd.item;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.text.Text;
import ninja.trek.srd.combat.Weapon;
import ninja.trek.srd.combat.WeaponProperty;

import java.util.List;

/**
 * Item representing a weapon from the 5e SRD.
 */
public class WeaponItem extends Item {
    private final Weapon weapon;

    public WeaponItem(Settings settings, Weapon weapon) {
        super(settings);
        this.weapon = weapon;
    }

    public Weapon getWeapon() {
        return weapon;
    }

    @Override
    public void appendTooltip(ItemStack stack, TooltipContext context, List<Text> tooltip, TooltipType type) {
        super.appendTooltip(stack, context, tooltip, type);

        // Add weapon information
        tooltip.add(Text.literal("Damage: " + weapon.getDamageDice() + "d" + weapon.getDamageDie()));
        tooltip.add(Text.literal("Type: " + weapon.getDamageType().name()));

        // Add properties
        if (!weapon.getProperties().isEmpty()) {
            StringBuilder props = new StringBuilder("Properties: ");
            for (WeaponProperty prop : weapon.getProperties()) {
                props.append(prop.name()).append(" ");
            }
            tooltip.add(Text.literal(props.toString().trim()));
        }

        // Add range for ranged weapons
        if (weapon.getNormalRange() > 0) {
            tooltip.add(Text.literal("Range: " + weapon.getNormalRange() + "/" + weapon.getLongRange()));
        }
    }
}
