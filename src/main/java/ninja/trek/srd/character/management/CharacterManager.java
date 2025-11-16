package ninja.trek.srd.character.management;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import ninja.trek.srd.character.entity.CharacterEntity;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Singleton service for managing player-character relationships.
 * Tracks which characters belong to which players.
 */
public class CharacterManager {
    private static CharacterManager instance;

    // Map of player UUID -> list of character UUIDs
    private final Map<UUID, List<UUID>> playerCharacters = new ConcurrentHashMap<>();

    // Map of character UUID -> owner player UUID for quick lookup
    private final Map<UUID, UUID> characterOwners = new ConcurrentHashMap<>();

    private CharacterManager() {
        // Private constructor for singleton
    }

    /**
     * Get the singleton instance.
     */
    public static CharacterManager getInstance() {
        if (instance == null) {
            instance = new CharacterManager();
        }
        return instance;
    }

    /**
     * Register a character as belonging to a player.
     */
    public void registerCharacter(UUID playerUUID, CharacterEntity character) {
        UUID characterUUID = character.getUuid();

        // Add to player's character list
        playerCharacters.computeIfAbsent(playerUUID, k -> new ArrayList<>()).add(characterUUID);

        // Add to character owners map
        characterOwners.put(characterUUID, playerUUID);
    }

    /**
     * Unregister a character (e.g., when deleted).
     */
    public void unregisterCharacter(UUID characterUUID) {
        UUID ownerUUID = characterOwners.remove(characterUUID);
        if (ownerUUID != null) {
            List<UUID> characters = playerCharacters.get(ownerUUID);
            if (characters != null) {
                characters.remove(characterUUID);
            }
        }
    }

    /**
     * Get all character UUIDs owned by a player.
     */
    public List<UUID> getPlayerCharacters(UUID playerUUID) {
        return new ArrayList<>(playerCharacters.getOrDefault(playerUUID, Collections.emptyList()));
    }

    /**
     * Get all character entities owned by a player.
     */
    public List<CharacterEntity> getPlayerCharacterEntities(MinecraftServer server, UUID playerUUID) {
        List<CharacterEntity> entities = new ArrayList<>();
        List<UUID> characterUUIDs = getPlayerCharacters(playerUUID);

        for (UUID characterUUID : characterUUIDs) {
            var entity = server.getOverworld().getEntity(characterUUID);
            if (entity instanceof CharacterEntity character) {
                entities.add(character);
            }
        }

        return entities;
    }

    /**
     * Get the owner UUID of a character.
     */
    @Nullable
    public UUID getOwner(UUID characterUUID) {
        return characterOwners.get(characterUUID);
    }

    /**
     * Check if a player owns a specific character.
     */
    public boolean ownsCharacter(UUID playerUUID, UUID characterUUID) {
        UUID owner = characterOwners.get(characterUUID);
        return owner != null && owner.equals(playerUUID);
    }

    /**
     * Get the number of characters owned by a player.
     */
    public int getCharacterCount(UUID playerUUID) {
        return playerCharacters.getOrDefault(playerUUID, Collections.emptyList()).size();
    }

    /**
     * Remove all characters for a player (e.g., when they disconnect).
     * This doesn't delete the entities, just clears the tracking.
     */
    public void clearPlayerCharacters(UUID playerUUID) {
        List<UUID> characters = playerCharacters.remove(playerUUID);
        if (characters != null) {
            for (UUID characterUUID : characters) {
                characterOwners.remove(characterUUID);
            }
        }
    }

    /**
     * Scan the world for existing characters and rebuild the mappings.
     * Useful for world load or after a crash.
     */
    public void rebuildMappings(MinecraftServer server) {
        playerCharacters.clear();
        characterOwners.clear();

        // Scan all entities in the overworld
        server.getOverworld().iterateEntities().forEach(entity -> {
            if (entity instanceof CharacterEntity character) {
                UUID ownerUUID = character.getOwnerUUID();
                if (ownerUUID != null) {
                    registerCharacter(ownerUUID, character);
                }
            }
        });
    }
}
