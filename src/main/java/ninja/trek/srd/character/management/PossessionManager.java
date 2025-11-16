package ninja.trek.srd.character.management;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import ninja.trek.srd.character.entity.CharacterEntity;
import ninja.trek.srd.network.payloads.PossessCharacterPayload;
import ninja.trek.srd.network.payloads.ReleasePossessionPayload;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Server-side manager for player possession of characters.
 * Tracks which player is currently possessing which character.
 */
public class PossessionManager {
    private static PossessionManager instance;

    // Map of player UUID -> currently possessed character UUID
    private final Map<UUID, UUID> playerToPossessedCharacter = new HashMap<>();

    // Map of character UUID -> player UUID (for quick reverse lookup)
    private final Map<UUID, UUID> characterToPossessingPlayer = new HashMap<>();

    // Map of player UUID -> player mode state (true = player mode, false = party mode)
    private final Map<UUID, Boolean> playerModeState = new HashMap<>();

    private PossessionManager() {
        // Private constructor for singleton
    }

    /**
     * Get the singleton instance.
     */
    public static PossessionManager getInstance() {
        if (instance == null) {
            instance = new PossessionManager();
        }
        return instance;
    }

    /**
     * Possess a character as a player.
     * Sends network packet to client to switch camera and input.
     */
    public void possessCharacter(MinecraftServer server, ServerPlayerEntity player, CharacterEntity character) {
        UUID playerUUID = player.getUuid();
        UUID characterUUID = character.getUuid();

        // Release any currently possessed character
        releasePossession(server, player);

        // Update mappings
        playerToPossessedCharacter.put(playerUUID, characterUUID);
        characterToPossessingPlayer.put(characterUUID, playerUUID);

        // Ensure player is in party mode
        playerModeState.put(playerUUID, false);

        // Send packet to client
        player.networkHandler.sendPacket(new PossessCharacterPayload(characterUUID).createPacket());
    }

    /**
     * Release possession of current character.
     * This does NOT return to player mode - just releases the character.
     */
    public void releasePossession(MinecraftServer server, ServerPlayerEntity player) {
        UUID playerUUID = player.getUuid();
        UUID previousCharacter = playerToPossessedCharacter.remove(playerUUID);

        if (previousCharacter != null) {
            characterToPossessingPlayer.remove(previousCharacter);
        }

        // Send packet to client
        player.networkHandler.sendPacket(new ReleasePossessionPayload().createPacket());
    }

    /**
     * Get the character UUID that a player is currently possessing.
     */
    @Nullable
    public UUID getPossessedCharacter(UUID playerUUID) {
        return playerToPossessedCharacter.get(playerUUID);
    }

    /**
     * Get the player UUID that is currently possessing a character.
     */
    @Nullable
    public UUID getPossessingPlayer(UUID characterUUID) {
        return characterToPossessingPlayer.get(characterUUID);
    }

    /**
     * Check if a player is currently possessing any character.
     */
    public boolean isPossessing(UUID playerUUID) {
        return playerToPossessedCharacter.containsKey(playerUUID);
    }

    /**
     * Check if a player is in player mode (true) or party mode (false).
     */
    public boolean isPlayerMode(UUID playerUUID) {
        return playerModeState.getOrDefault(playerUUID, true); // Default to player mode
    }

    /**
     * Set player mode state.
     */
    public void setPlayerMode(MinecraftServer server, ServerPlayerEntity player, boolean playerMode) {
        UUID playerUUID = player.getUuid();
        playerModeState.put(playerUUID, playerMode);

        if (playerMode) {
            // Switching to player mode - release possession
            releasePossession(server, player);
        } else {
            // Switching to party mode - possess first available character
            var characters = CharacterManager.getInstance().getPlayerCharacterEntities(server, playerUUID);
            if (!characters.isEmpty()) {
                possessCharacter(server, player, characters.get(0));
            }
        }
    }

    /**
     * Toggle between player mode and party mode.
     */
    public void toggleMode(MinecraftServer server, ServerPlayerEntity player) {
        boolean currentMode = isPlayerMode(player.getUuid());
        setPlayerMode(server, player, !currentMode);
    }

    /**
     * Clean up possession state when a player disconnects.
     */
    public void onPlayerDisconnect(UUID playerUUID) {
        UUID characterUUID = playerToPossessedCharacter.remove(playerUUID);
        if (characterUUID != null) {
            characterToPossessingPlayer.remove(characterUUID);
        }
        playerModeState.remove(playerUUID);
    }

    /**
     * Clean up possession state when a character is removed.
     */
    public void onCharacterRemoved(MinecraftServer server, UUID characterUUID) {
        UUID playerUUID = characterToPossessingPlayer.remove(characterUUID);
        if (playerUUID != null) {
            playerToPossessedCharacter.remove(playerUUID);

            // Notify client to release possession
            ServerPlayerEntity player = server.getPlayerManager().getPlayer(playerUUID);
            if (player != null) {
                player.networkHandler.sendPacket(new ReleasePossessionPayload().createPacket());

                // Try to possess another character
                var characters = CharacterManager.getInstance().getPlayerCharacterEntities(server, playerUUID);
                if (!characters.isEmpty()) {
                    possessCharacter(server, player, characters.get(0));
                } else {
                    // No more characters - switch to player mode
                    setPlayerMode(server, player, true);
                }
            }
        }
    }
}
