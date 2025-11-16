package ninja.trek.srd.mixin.client;

import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;
import ninja.trek.srd.character.entity.CharacterEntity;
import ninja.trek.srd.client.ClientPossessionManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Mixin to override the camera entity when possessing a character.
 * When in party mode and possessing a character, the camera follows the character instead of the player.
 */
@Mixin(MinecraftClient.class)
public abstract class CameraMixin {

    @Shadow
    public abstract Entity getCameraEntity();

    /**
     * Override getCameraEntity to return the possessed character when in party mode.
     */
    @Inject(method = "getCameraEntity", at = @At("HEAD"), cancellable = true)
    private void onGetCameraEntity(CallbackInfoReturnable<Entity> cir) {
        ClientPossessionManager possessionManager = ClientPossessionManager.getInstance();

        // If we're in party mode and possessing a character, return the character entity
        if (possessionManager.isPossessing()) {
            CharacterEntity character = possessionManager.getPossessedCharacter();
            if (character != null) {
                cir.setReturnValue(character);
            }
        }
    }
}
