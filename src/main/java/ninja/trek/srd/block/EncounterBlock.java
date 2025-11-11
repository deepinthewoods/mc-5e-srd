package ninja.trek.srd.block;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
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

    public EncounterBlock(Properties properties) {
        super(properties);
    }

    @Override
    @SuppressWarnings("deprecation")
    public void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        super.tick(state, level, pos, random);
        checkForEncounterTrigger(level, pos);
    }

    /**
     * Check if any players or entities are within trigger radius.
     */
    private void checkForEncounterTrigger(ServerLevel level, BlockPos pos) {
        Vec3 centerPos = Vec3.atCenterOf(pos);
        AABB searchBox = new AABB(centerPos).inflate(DEFAULT_TRIGGER_RADIUS);

        // Find all entities in range
        List<Entity> nearbyEntities = level.getEntities(
            null,
            searchBox,
            entity -> entity instanceof CharacterEntity || entity.getType().getCategory().isFriendly()
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
    private void triggerEncounter(ServerLevel level, Vec3 centerPos, List<Entity> participants) {
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
                    character.getUUID(),
                    initiativeRoll,
                    dexMod
                );

                manager.addCombatant(encounterId, tracker);

                // Mark character as in combat
                var combatState = character.getCombatState();
                character.setCombatState(new ninja.trek.srd.combat.CombatState(
                    true,
                    initiativeRoll + dexMod,
                    character.position(),
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
