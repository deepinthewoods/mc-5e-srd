package ninja.trek.srd.block;

import net.minecraft.util.math.BlockPos;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.random.Random;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.server.network.ServerPlayerEntity;
import ninja.trek.srd.character.entity.CharacterEntity;
import ninja.trek.srd.combat.EncounterManager;
import ninja.trek.srd.combat.InitiativeTracker;
import ninja.trek.srd.combat.DeathSaves;
import ninja.trek.srd.util.DiceRoller;

import java.util.List;
import java.util.UUID;

/**
 * A block that triggers combat encounters when a player gets near.
 * The block destroys itself immediately after triggering.
 */
public class EncounterBlock extends Block {
    private static final int DEFAULT_TRIGGER_RADIUS = 10;
    private static final int DEFAULT_PLAYER_MOVEMENT = 6;
    private static final int TICK_DELAY = 20; // Check every second (20 ticks)

    public EncounterBlock(Settings settings) {
        super(settings);
    }

    /**
     * Called when the block is placed. Schedule the first tick to start checking for entities.
     */
    @Override
    public void onPlaced(World world, BlockPos pos, BlockState state, LivingEntity placer, ItemStack itemStack) {
        super.onPlaced(world, pos, state, placer, itemStack);
        if (!world.isClient) {
            world.scheduleBlockTick(pos, this, TICK_DELAY);
        }
    }

    /**
     * Called when a scheduled tick occurs. Check for nearby entities and reschedule the next tick.
     */
    @Override
    public void scheduledTick(BlockState state, ServerWorld world, BlockPos pos, Random random) {
        checkForEncounterTrigger(world, pos);
        // Only reschedule if the block still exists (wasn't removed by trigger)
        if (world.getBlockState(pos).isOf(this)) {
            world.scheduleBlockTick(pos, this, TICK_DELAY);
        }
    }

    /**
     * Check if any players or entities are within trigger radius.
     * This is called via scheduledTick every second.
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

                manager.addCombatant(level.getServer(), encounterId, tracker);

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
                    combatState.maxHitPoints(),
                    false,
                    DeathSaves.createDefault(),
                    false,
                    false
                ));

                manager.syncCombatState(level.getServer(), character.getUuid(), character.getCombatState());
                manager.setBaseMovementSpeed(character.getUuid(), character.getRace().getBaseMovementSpeed());
            } else if (entity instanceof ServerPlayerEntity player && !player.isSpectator()) {
                int initiativeRoll = roller.rollInitiative(0);
                InitiativeTracker tracker = new InitiativeTracker(
                    player.getUuid(),
                    initiativeRoll,
                    0,
                    player.getDisplayName()
                );

                manager.addCombatant(level.getServer(), encounterId, tracker);

                ninja.trek.srd.combat.CombatState combatState = new ninja.trek.srd.combat.CombatState(
                    true,
                    initiativeRoll,
                    Vec3d.ofBottomCenter(player.getBlockPos()),
                    DEFAULT_PLAYER_MOVEMENT,
                    true,
                    true,
                    true,
                    10 + (int) Math.round(player.getArmor()),
                    (int) Math.round(player.getHealth()),
                    (int) Math.round(player.getMaxHealth()),
                    false,
                    DeathSaves.createDefault(),
                    false,
                    false
                );

                manager.syncCombatState(level.getServer(), player.getUuid(), combatState);
                manager.setBaseMovementSpeed(player.getUuid(), DEFAULT_PLAYER_MOVEMENT);
            }
        }

        manager.syncEncounter(level.getServer(), encounterId);
        // TODO: Spawn associated mobs from NBT data
    }
}
