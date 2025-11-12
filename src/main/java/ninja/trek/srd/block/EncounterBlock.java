package ninja.trek.srd.block;

import net.minecraft.util.math.BlockPos;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.random.Random;
import net.minecraft.entity.Entity;
import net.minecraft.world.World;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import ninja.trek.srd.character.entity.CharacterEntity;
import ninja.trek.srd.combat.EncounterManager;
import ninja.trek.srd.combat.InitiativeTracker;
import ninja.trek.srd.util.DiceRoller;

import java.util.List;
import java.util.UUID;

/**
 * A block that triggers combat encounters when a player gets near.
 * The block destroys itself immediately after triggering.
 */
public class EncounterBlock extends Block {
    private static final int DEFAULT_TRIGGER_RADIUS = 10;

    public EncounterBlock(Settings settings) {
        super(settings);
    }

    /**
     * Check if any players or entities are within trigger radius.
     * This should be called via scheduledTick or another mechanism.
     */
    private void checkForEncounterTrigger(ServerWorld level, BlockPos pos) {
        Vec3d centerPos = Vec3d.ofCenter(pos);
        double radius = DEFAULT_TRIGGER_RADIUS;
        Box searchBox = new Box(
            centerPos.x - radius, centerPos.y - radius, centerPos.z - radius,
            centerPos.x + radius, centerPos.y + radius, centerPos.z + radius
        );

        // Find all entities in range
        List<Entity> nearbyEntities = level.getEntitiesByClass(
            Entity.class,
            searchBox,
            entity -> entity instanceof CharacterEntity || entity.getType().getSpawnGroup().isPeaceful()
        );

        // If entities found, trigger encounter
        if (!nearbyEntities.isEmpty()) {
            triggerEncounter(level, centerPos, nearbyEntities);
            // Destroy this block
            level.removeBlock(pos, false);
        }
    }

    /**
     * Trigger a combat encounter for all nearby entities.
     */
    private void triggerEncounter(ServerWorld level, Vec3d centerPos, List<Entity> participants) {
        DiceRoller roller = new DiceRoller(level.random);
        EncounterManager manager = EncounterManager.getInstance();

        // Create new encounter
        UUID encounterId = manager.startEncounter(centerPos, participants);

        // Roll initiative for all participants and add them
        for (Entity entity : participants) {
            if (entity instanceof CharacterEntity character) {
                int initiativeRoll = character.rollInitiative(roller);
                int dexMod = character.getStats().getDexterityModifier();

                InitiativeTracker tracker = new InitiativeTracker(
                    character.getUuid(),
                    initiativeRoll,
                    dexMod,
                    character.getDisplayName()
                );

                manager.addCombatant(encounterId, tracker);

                // Mark character as in combat
                var combatState = character.getCombatState();
                character.setCombatState(new ninja.trek.srd.combat.CombatState(
                    true,
                    initiativeRoll + dexMod,
                    Vec3d.ofBottomCenter(character.getBlockPos()),
                    character.getRace().getBaseMovementSpeed(),
                    true,
                    true,
                    true,
                    combatState.armorClass(),
                    combatState.currentHitPoints(),
                    combatState.maxHitPoints()
                ));
            }
        }

        // TODO: Sync encounter state to clients
        // TODO: Spawn associated mobs from NBT data
    }
}
