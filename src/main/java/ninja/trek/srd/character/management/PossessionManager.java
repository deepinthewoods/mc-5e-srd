package ninja.trek.srd.character.management;

import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.ItemStack;
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

    // Map of player UUID -> stored player inventory (when in party mode)
    private final Map<UUID, SimpleInventory> storedPlayerInventories = new HashMap<>();

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
     * Swaps player inventory with character inventory.
     */
    public void possessCharacter(MinecraftServer server, ServerPlayerEntity player, CharacterEntity character) {
        UUID playerUUID = player.getUuid();
        UUID characterUUID = character.getUuid();

        // Release any currently possessed character (this will save their inventory back)
        releasePossession(server, player);

        // Store player's inventory if not already stored (first time entering party mode)
        if (!storedPlayerInventories.containsKey(playerUUID)) {
            storedPlayerInventories.put(playerUUID, storePlayerInventory(player));
        }

        // Swap player inventory with character inventory
        swapInventories(player, character);

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
     * Saves character's current inventory back to the character.
     */
    public void releasePossession(MinecraftServer server, ServerPlayerEntity player) {
        UUID playerUUID = player.getUuid();
        UUID previousCharacterUUID = playerToPossessedCharacter.remove(playerUUID);

        if (previousCharacterUUID != null) {
            characterToPossessingPlayer.remove(previousCharacterUUID);

            // Save player's current inventory back to the character
            var entity = server.getOverworld().getEntity(previousCharacterUUID);
            if (entity instanceof CharacterEntity previousCharacter) {
                saveInventoryToCharacter(player, previousCharacter);
            }
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
     * Handles inventory swapping between player and party mode.
     */
    public void setPlayerMode(MinecraftServer server, ServerPlayerEntity player, boolean playerMode) {
        UUID playerUUID = player.getUuid();
        playerModeState.put(playerUUID, playerMode);

        if (playerMode) {
            // Switching to player mode - release possession and restore player's inventory
            releasePossession(server, player);

            // Restore player's original inventory
            SimpleInventory storedInventory = storedPlayerInventories.remove(playerUUID);
            if (storedInventory != null) {
                restorePlayerInventory(player, storedInventory);
            }
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
        storedPlayerInventories.remove(playerUUID);
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

    // Inventory Management Helper Methods

    /**
     * Store the player's current inventory.
     */
    private SimpleInventory storePlayerInventory(ServerPlayerEntity player) {
        SimpleInventory storage = new SimpleInventory(41);
        for (int i = 0; i < player.getInventory().size(); i++) {
            storage.setStack(i, player.getInventory().getStack(i).copy());
        }
        return storage;
    }

    /**
     * Restore a stored inventory to the player.
     */
    private void restorePlayerInventory(ServerPlayerEntity player, SimpleInventory storage) {
        player.getInventory().clear();
        for (int i = 0; i < Math.min(storage.size(), player.getInventory().size()); i++) {
            player.getInventory().setStack(i, storage.getStack(i).copy());
        }
        player.currentScreenHandler.sendContentUpdates();
        player.playerScreenHandler.onContentChanged(player.getInventory());
    }

    /**
     * Swap player inventory with character inventory.
     */
    private void swapInventories(ServerPlayerEntity player, CharacterEntity character) {
        // Save player's current inventory temporarily
        SimpleInventory tempStorage = new SimpleInventory(41);
        for (int i = 0; i < player.getInventory().size(); i++) {
            tempStorage.setStack(i, player.getInventory().getStack(i).copy());
        }

        // Clear player inventory and load character's inventory
        player.getInventory().clear();
        SimpleInventory charInventory = character.getInventory();
        for (int i = 0; i < Math.min(charInventory.size(), player.getInventory().size()); i++) {
            player.getInventory().setStack(i, charInventory.getStack(i).copy());
        }

        // Store temp inventory in character
        charInventory.clear();
        for (int i = 0; i < tempStorage.size(); i++) {
            charInventory.setStack(i, tempStorage.getStack(i).copy());
        }

        // Update client
        player.currentScreenHandler.sendContentUpdates();
        player.playerScreenHandler.onContentChanged(player.getInventory());
    }

    /**
     * Save player's current inventory back to character.
     */
    private void saveInventoryToCharacter(ServerPlayerEntity player, CharacterEntity character) {
        SimpleInventory charInventory = character.getInventory();
        charInventory.clear();
        for (int i = 0; i < Math.min(player.getInventory().size(), charInventory.size()); i++) {
            charInventory.setStack(i, player.getInventory().getStack(i).copy());
        }
    }
}
