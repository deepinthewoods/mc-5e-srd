package ninja.trek.srd.client;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import ninja.trek.srd.network.payloads.TogglePlayerModePayload;
import org.lwjgl.glfw.GLFW;

/**
 * Manages keybindings for character mode switching.
 */
public class CharacterModeKeybindings {

    private static final String KEYBIND_CATEGORY = "key.categories.fiveesrd";

    // G key to toggle between Player Mode and Party Mode
    private static final KeyBinding TOGGLE_MODE_KEY = KeyBindingHelper.registerKeyBinding(new KeyBinding(
        "key.fiveesrd.toggle_mode",
        InputUtil.Type.KEYSYM,
        GLFW.GLFW_KEY_G,
        KEYBIND_CATEGORY
    ));

    /**
     * Register keybinding handlers.
     * Call this from client initialization.
     */
    public static void register() {
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            // Handle Toggle Mode key press
            while (TOGGLE_MODE_KEY.wasPressed()) {
                // Send packet to server to toggle mode
                ClientPlayNetworking.send(new TogglePlayerModePayload());
            }
        });
    }
}
