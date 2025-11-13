package ninja.trek.srd.item;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.text.Text;
import ninja.trek.srd.combat.Weapon;
import ninja.trek.srd.combat.WeaponProperty;

import java.util.function.Consumer;

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
    public void appendTooltip(ItemStack stack, TooltipContext context, net.minecraft.component.type.TooltipDisplayComponent displayComponent, Consumer<Text> tooltip, TooltipType type) {
        super.appendTooltip(stack, context, displayComponent, tooltip, type);

        // Add weapon information
        tooltip.accept(Text.literal("Damage: " + weapon.damageDice() + "d" + weapon.damageDie()));
        tooltip.accept(Text.literal("Type: " + weapon.damageType().name()));

        // Add properties
        if (!weapon.properties().isEmpty()) {
            StringBuilder props = new StringBuilder("Properties: ");
            for (WeaponProperty prop : weapon.properties()) {
                props.append(prop.name()).append(" ");
            }
            tooltip.accept(Text.literal(props.toString().trim()));
        }

        // Add range for ranged weapons
        if (weapon.normalRange() > 0) {
            tooltip.accept(Text.literal("Range: " + weapon.normalRange() + "/" + weapon.longRange()));
        }
    }
}
