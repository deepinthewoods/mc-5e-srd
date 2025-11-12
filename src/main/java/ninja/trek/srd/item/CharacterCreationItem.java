package ninja.trek.srd.item;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.world.World;

/**
 * Item that opens the character creation screen when used.
 */
public class CharacterCreationItem extends Item {

    public CharacterCreationItem(Settings settings) {
        super(settings);
    }

    @Override
    public ActionResult use(World world, PlayerEntity user, Hand hand) {
        // Client-side screen opening is handled by the client module
        // to avoid mixing client and server code in the common source set
        return ActionResult.SUCCESS;
    }
}
