package ninja.trek.srd.client;

import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;
import ninja.trek.srd.character.entity.CharacterEntity;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

/**
 * Client-side manager for tracking possession state.
 * Handles camera positioning and input redirection.
 */
public class ClientPossessionManager {
    private static ClientPossessionManager instance;

    // Currently possessed character UUID (null if not possessing)
    private UUID possessedCharacterUUID = null;

    // Player mode state (true = player mode, false = party mode)
    private boolean playerMode = true;

    private ClientPossessionManager() {
        // Private constructor for singleton
    }

    /**
     * Get the singleton instance.
     */
    public static ClientPossessionManager getInstance() {
        if (instance == null) {
            instance = new ClientPossessionManager();
        }
        return instance;
    }

    /**
     * Possess a character. Called when receiving PossessCharacterPayload.
     */
    public void possessCharacter(UUID characterUUID) {
        this.possessedCharacterUUID = characterUUID;
        this.playerMode = false;
    }

    /**
     * Release possession. Called when receiving ReleasePossessionPayload.
     */
    public void releasePossession() {
        this.possessedCharacterUUID = null;
        // Note: We don't automatically switch to player mode here
        // The server controls mode state
    }

    /**
     * Set player mode state.
     */
    public void setPlayerMode(boolean playerMode) {
        this.playerMode = playerMode;
        if (playerMode) {
            // In player mode, we're not possessing anyone
            this.possessedCharacterUUID = null;
        }
    }

    /**
     * Check if currently possessing a character.
     */
    public boolean isPossessing() {
        return possessedCharacterUUID != null && !playerMode;
    }

    /**
     * Get the currently possessed character UUID.
     */
    @Nullable
    public UUID getPossessedCharacterUUID() {
        return possessedCharacterUUID;
    }

    /**
     * Get the currently possessed character entity.
     */
    @Nullable
    public CharacterEntity getPossessedCharacter() {
        if (possessedCharacterUUID == null) {
            return null;
        }

        MinecraftClient client = MinecraftClient.getInstance();
        if (client.world == null) {
            return null;
        }

        // Iterate through all entities to find the one with matching UUID
        for (net.minecraft.entity.Entity entity : client.world.getEntities()) {
            if (entity instanceof CharacterEntity character && entity.getUuid().equals(possessedCharacterUUID)) {
                return character;
            }
        }

        return null;
    }

    /**
     * Check if in player mode.
     */
    public boolean isPlayerMode() {
        return playerMode;
    }

    /**
     * Check if in party mode.
     */
    public boolean isPartyMode() {
        return !playerMode;
    }

    /**
     * Reset state (e.g., when disconnecting).
     */
    public void reset() {
        this.possessedCharacterUUID = null;
        this.playerMode = true;
    }
}
