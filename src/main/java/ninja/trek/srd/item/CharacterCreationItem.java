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
        if (world.isClient()) {
            // Open character creation screen on client side
            openCharacterCreationScreen();
        }
        return ActionResult.SUCCESS;
    }

    /**
     * Opens the character creation screen.
     * This method should only be called on the client side.
     */
    private void openCharacterCreationScreen() {
        net.minecraft.client.MinecraftClient.getInstance().setScreen(
            new ninja.trek.srd.client.gui.CharacterCreationScreen()
        );
    }
}
