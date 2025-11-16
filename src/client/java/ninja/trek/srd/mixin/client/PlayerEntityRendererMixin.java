package ninja.trek.srd.mixin.client;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.render.entity.PlayerEntityRenderer;
import ninja.trek.srd.client.ClientPossessionManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Mixin to make the player entity invisible when in party mode.
 */
@Mixin(PlayerEntityRenderer.class)
public class PlayerEntityRendererMixin {

    /**
     * Make the local player invisible when in party mode.
     */
    @Inject(method = "shouldRender", at = @At("HEAD"), cancellable = true)
    private void onShouldRender(AbstractClientPlayerEntity player, CallbackInfoReturnable<Boolean> cir) {
        MinecraftClient client = MinecraftClient.getInstance();

        // Check if this is the local player and they're in party mode
        if (player == client.player) {
            ClientPossessionManager possessionManager = ClientPossessionManager.getInstance();
            if (possessionManager.isPartyMode()) {
                // Don't render the player entity in party mode
                cir.setReturnValue(false);
            }
        }
    }
}
