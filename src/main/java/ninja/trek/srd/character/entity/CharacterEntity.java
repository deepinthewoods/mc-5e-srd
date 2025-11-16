package ninja.trek.srd.character.entity;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.mob.PathAwareEntity;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.world.World;
import net.minecraft.util.math.Vec3d;
import ninja.trek.srd.character.data.*;
import ninja.trek.srd.character.layer.LayerConfiguration;
import ninja.trek.srd.combat.*;
import ninja.trek.srd.util.DiceRoller;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.animatable.manager.AnimatableManager;
import software.bernie.geckolib.animatable.processing.AnimationController;
import software.bernie.geckolib.animatable.processing.AnimationTest;
import software.bernie.geckolib.animation.PlayState;
import software.bernie.geckolib.animation.RawAnimation;
import software.bernie.geckolib.util.GeckoLibUtil;

/**
 * Base entity for D&D 5e characters with full character sheet data.
 * Implements GeoEntity for GeckoLib-based rendering and animation.
 */
public class CharacterEntity extends PathAwareEntity implements GeoEntity {

    // Synced entity data
    private static final TrackedData<Integer> DATA_BODY_INDEX =
        DataTracker.registerData(CharacterEntity.class, TrackedDataHandlerRegistry.INTEGER);
    private static final TrackedData<Integer> DATA_LEGS_INDEX =
        DataTracker.registerData(CharacterEntity.class, TrackedDataHandlerRegistry.INTEGER);
    private static final TrackedData<Integer> DATA_ARMS_INDEX =
        DataTracker.registerData(CharacterEntity.class, TrackedDataHandlerRegistry.INTEGER);
    private static final TrackedData<Integer> DATA_HEAD_INDEX =
        DataTracker.registerData(CharacterEntity.class, TrackedDataHandlerRegistry.INTEGER);

    // Character data (stored in NBT)
    private CharacterStats stats;
    private Race race;
    private CharacterClass characterClass;
    private int level;
    private CombatState combatState;
    private Vec3d lastCombatPosition = Vec3d.ZERO;
    private boolean playerControlled = false;
    private java.util.UUID ownerUUID; // Player who owns this character
    private ninja.trek.srd.character.ai.CombatAIController aiController;
    private Weapon equippedWeapon;

    // GeckoLib animation cache
    private final AnimatableInstanceCache animationCache = GeckoLibUtil.createInstanceCache(this);

    // Layer configuration for rendering
    private LayerConfiguration layerConfiguration = new LayerConfiguration();

    // Size scale for retargeting (0.5 - 3.0, default 1.0)
    private float sizeScale = 1.0f;

    // Track if layer configuration needs syncing (dirty flag)
    private boolean layerConfigDirty = true;

    // Animation state tracking (client-side)
    private boolean isAttacking = false;
    private boolean isCasting = false;
    private boolean isBlocking = false;
    private boolean isChanneling = false;
    private long attackStartTime = 0;
    private long castStartTime = 0;

    public CharacterEntity(EntityType<? extends PathAwareEntity> entityType, World level) {
        super(entityType, level);
        // Initialize with defaults
        this.stats = CharacterStats.createDefault();
        this.race = Race.HUMAN;
        this.characterClass = CharacterClass.FIGHTER;
        this.level = 1;
        this.equippedWeapon = Weapons.getStartingWeaponForFighter();

        // Calculate initial combat state
        int maxHp = calculateMaxHitPoints();
        int ac = calculateArmorClass();
        this.combatState = CombatState.createDefault(maxHp, ac);
        this.lastCombatPosition = Vec3d.ofBottomCenter(this.getBlockPos());

        // Register AI goals if on server
        if (!level.isClient()) {
            registerAIGoals();
        }
    }

    @Override
    public void tick() {
        super.tick();

        if (!this.getEntityWorld().isClient()) {
            // Store position before movement for opportunity attack detection
            Vec3d previousPosition = OpportunityAttackHandler.getPreviousPosition(this.getUuid());
            Vec3d currentPosition = currentPositionVector();

            // Check for opportunity attacks if in combat and moved
            if (this.combatState.inCombat() && !previousPosition.equals(Vec3d.ZERO)) {
                double distanceMoved = previousPosition.distanceTo(currentPosition);
                if (distanceMoved > 0.1) { // Threshold to avoid tiny movements
                    var server = this.getEntityWorld().getServer();
                    if (server != null) {
                        OpportunityAttackHandler.checkForOpportunityAttacks(
                            this,
                            previousPosition,
                            currentPosition,
                            server
                        );
                    }
                }
            }

            // Update movement constraints
            updateCombatMovementConstraints();

            // Update position tracking for next tick
            OpportunityAttackHandler.updatePosition(this.getUuid(), currentPosition);
        } else if (!this.combatState.inCombat()) {
            this.lastCombatPosition = currentPositionVector();
        }
    }

    /**
     * Create default attribute supplier for character entities.
     */
    public static DefaultAttributeContainer.Builder createAttributes() {
        return PathAwareEntity.createMobAttributes()
            .add(EntityAttributes.MAX_HEALTH, 10.0)
            .add(EntityAttributes.MOVEMENT_SPEED, 0.25)
            .add(EntityAttributes.ATTACK_DAMAGE, 2.0)
            .add(EntityAttributes.ARMOR, 0.0);
    }

    @Override
    protected void initDataTracker(DataTracker.Builder builder) {
        super.initDataTracker(builder);
        builder.add(DATA_BODY_INDEX, 0);
        builder.add(DATA_LEGS_INDEX, 0);
        builder.add(DATA_ARMS_INDEX, 0);
        builder.add(DATA_HEAD_INDEX, 0);
    }

    /**
     * Register AI goals for this character entity.
     */
    private void registerAIGoals() {
        this.aiController = new ninja.trek.srd.character.ai.CombatAIController(this);
        this.goalSelector.add(1, this.aiController);
        this.targetSelector.add(1, new net.minecraft.entity.ai.goal.ActiveTargetGoal<>(
            this,
            net.minecraft.entity.player.PlayerEntity.class,
            false
        ));
    }

    /**
     * Initialize character from creation data.
     */
    public void initializeCharacter(Race race, CharacterClass characterClass, CharacterStats baseStats, CharacterAppearance appearance) {
        this.race = race;
        this.characterClass = characterClass;
        this.level = 1;

        // Apply racial bonuses to stats
        this.stats = baseStats.applyRacialBonuses(race);

        // Set appearance
        setAppearance(appearance);

        // Equip starting weapon based on class
        this.equippedWeapon = Weapons.getStartingWeaponForFighter();

        // Calculate and set attributes
        int maxHp = calculateMaxHitPoints();
        int ac = calculateArmorClass();

        this.combatState = CombatState.createDefault(maxHp, ac);

        // Update Minecraft attributes
        updateMinecraftAttributes();
    }

    /**
     * Set character appearance (mesh indices).
     */
    public void setAppearance(CharacterAppearance appearance) {
        this.dataTracker.set(DATA_BODY_INDEX, appearance.bodyIndex());
        this.dataTracker.set(DATA_LEGS_INDEX, appearance.legsIndex());
        this.dataTracker.set(DATA_ARMS_INDEX, appearance.armsIndex());
        this.dataTracker.set(DATA_HEAD_INDEX, appearance.headIndex());
    }

    /**
     * Get character appearance.
     */
    public CharacterAppearance getAppearance() {
        return new CharacterAppearance(
            this.dataTracker.get(DATA_BODY_INDEX),
            this.dataTracker.get(DATA_LEGS_INDEX),
            this.dataTracker.get(DATA_ARMS_INDEX),
            this.dataTracker.get(DATA_HEAD_INDEX)
        );
    }

    /**
     * Calculate maximum hit points based on class, level, and CON modifier.
     */
    private int calculateMaxHitPoints() {
        int hitDie = characterClass.getHitDie();
        int conMod = stats.getConstitutionModifier();

        // First level: max hit die + CON mod
        int maxHp = hitDie + conMod;

        // Additional levels: average of hit die + CON mod per level
        for (int i = 2; i <= level; i++) {
            maxHp += (hitDie / 2 + 1) + conMod;
        }

        return Math.max(1, maxHp);
    }

    /**
     * Calculate armor class (base 10 + DEX modifier).
     * TODO: Add armor bonuses when equipment system is implemented.
     */
    private int calculateArmorClass() {
        return 10 + stats.getDexterityModifier();
    }

    /**
     * Update Minecraft's internal attributes based on 5e stats.
     */
    private void updateMinecraftAttributes() {
        // Set max health
        if (this.getAttributeInstance(EntityAttributes.MAX_HEALTH) != null) {
            this.getAttributeInstance(EntityAttributes.MAX_HEALTH).setBaseValue(combatState.maxHitPoints());
            this.setHealth(combatState.currentHitPoints());
        }

        // Set movement speed based on race
        if (this.getAttributeInstance(EntityAttributes.MOVEMENT_SPEED) != null) {
            // Convert blocks per turn to blocks per tick
            // 6 blocks per turn / 20 ticks per second * some scaling factor
            double movementSpeed = race.getBaseMovementSpeed() * 0.05;
            this.getAttributeInstance(EntityAttributes.MOVEMENT_SPEED).setBaseValue(movementSpeed);
        }

        // Set armor
        if (this.getAttributeInstance(EntityAttributes.ARMOR) != null) {
            this.getAttributeInstance(EntityAttributes.ARMOR).setBaseValue(combatState.armorClass());
        }
    }

    /**
     * Start this entity's turn in combat.
     */
    public void startTurn() {
        if (!combatState.inCombat()) {
            return;
        }

        Vec3d currentPos = Vec3d.ofBottomCenter(this.getBlockPos());
        int movementSpeed = race.getBaseMovementSpeed();
        this.setCombatState(combatState.startTurn(currentPos, movementSpeed));
        this.lastCombatPosition = currentPos;
    }

    /**
     * Roll initiative for this character.
     */
    public int rollInitiative(DiceRoller roller) {
        return roller.rollInitiative(stats.getDexterityModifier());
    }

    /**
     * Check if it's currently this entity's turn.
     */
    public boolean isMyTurn() {
        EncounterState encounter = EncounterManager.getInstance().getEncounterForEntity(this.getUuid());
        if (encounter == null) {
            return false;
        }
        return this.getUuid().equals(encounter.getCurrentTurnEntity());
    }

    // Getters and setters
    public CharacterStats getStats() {
        return stats;
    }

    public void setStats(CharacterStats stats) {
        this.stats = stats;
        updateMinecraftAttributes();
    }

    public Race getRace() {
        return race;
    }

    public CharacterClass getCharacterClass() {
        return characterClass;
    }

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = level;
        updateMinecraftAttributes();
    }

    public CombatState getCombatState() {
        return combatState;
    }

    public void setCombatState(CombatState combatState) {
        this.combatState = combatState;
        if (!this.getEntityWorld().isClient()) {
            var server = this.getEntityWorld().getServer();
            if (server != null) {
                EncounterManager.getInstance().syncCombatState(server, this.getUuid(), combatState);
            }
        }
    }

    public boolean isPlayerControlled() {
        return playerControlled;
    }

    public void setPlayerControlled(boolean playerControlled) {
        this.playerControlled = playerControlled;
    }

    public java.util.UUID getOwnerUUID() {
        return ownerUUID;
    }

    public void setOwnerUUID(java.util.UUID ownerUUID) {
        this.ownerUUID = ownerUUID;
    }

    public ninja.trek.srd.character.ai.CombatAIController getAIController() {
        return aiController;
    }

    public Weapon getEquippedWeapon() {
        return equippedWeapon != null ? equippedWeapon : Weapons.getDefaultWeapon();
    }

    public void setEquippedWeapon(Weapon weapon) {
        this.equippedWeapon = weapon;
    }

    /**
     * Maintain server-authoritative combat movement constraints.
     */
    private void updateCombatMovementConstraints() {
        Vec3d currentPos = currentPositionVector();

        if (!this.combatState.inCombat()) {
            this.lastCombatPosition = currentPos;
            return;
        }

        if (this.lastCombatPosition == Vec3d.ZERO) {
            this.lastCombatPosition = currentPos;
        }

        // Freeze movement when it's not this entity's turn
        if (!isMyTurn()) {
            if (currentPos.squaredDistanceTo(this.lastCombatPosition) > 1.0E-4) {
                teleportToCombatAnchor(this.lastCombatPosition);
            }
            return;
        }

        double distanceMoved = currentPos.distanceTo(this.lastCombatPosition);
        if (distanceMoved < 1.0E-3) {
            return;
        }

        int remainingMovement = this.combatState.remainingMovement();
        if (remainingMovement <= 0) {
            teleportToCombatAnchor(this.lastCombatPosition);
            return;
        }

        double allowedDistance = Math.min(distanceMoved, remainingMovement);
        if (distanceMoved > remainingMovement) {
            Vec3d direction = currentPos.subtract(this.lastCombatPosition).normalize();
            Vec3d clampedPos = this.lastCombatPosition.add(direction.multiply(remainingMovement));
            teleportToCombatAnchor(clampedPos);
            allowedDistance = remainingMovement;
        }

        int spentMovement = (int) Math.ceil(allowedDistance);
        this.combatState = this.combatState.withRemainingMovement(Math.max(0, remainingMovement - spentMovement));
        this.lastCombatPosition = currentPositionVector();
    }

    private void teleportToCombatAnchor(Vec3d targetPos) {
        this.refreshPositionAndAngles(targetPos.x, targetPos.y, targetPos.z, this.getYaw(), this.getPitch());
        this.setVelocity(Vec3d.ZERO);
        this.lastCombatPosition = targetPos;
    }

    private Vec3d currentPositionVector() {
        return new Vec3d(this.getX(), this.getY(), this.getZ());
    }

    /**
     * Execute a turn for this AI-controlled character.
     * This is called when it's the AI's turn in combat.
     */
    public void executeAITurn(net.minecraft.server.MinecraftServer server, EncounterState encounter) {
        if (aiController != null && !playerControlled) {
            // The AI controller will handle the turn logic
            // and call the appropriate methods to advance the turn
            aiController.tick();
        }
    }

    // GeckoLib implementation

    // Animation definitions with priority-based state machine
    private static final RawAnimation IDLE = RawAnimation.begin().thenLoop("animation.character.idle");
    private static final RawAnimation WALK = RawAnimation.begin().thenLoop("animation.character.walk");
    private static final RawAnimation RUN = RawAnimation.begin().thenLoop("animation.character.run");
    private static final RawAnimation ATTACK = RawAnimation.begin().thenPlay("animation.character.attack");
    private static final RawAnimation CAST = RawAnimation.begin().thenPlay("animation.character.spell.cast_instant");
    private static final RawAnimation BLOCK = RawAnimation.begin().thenLoop("animation.character.combat.block_shield");
    private static final RawAnimation CHANNEL = RawAnimation.begin().thenLoop("animation.character.spell.channel_concentrated");
    private static final RawAnimation DEATH = RawAnimation.begin().thenPlay("animation.character.idle");

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>("movement", 0, this::animationController));
    }

    /**
     * Main animation controller with priority-based state machine.
     * Priority levels:
     * 1. Action animations (attacks, casts, special abilities) - cannot be interrupted
     * 2. Locomotion (walk, run, jump) - can be interrupted by actions
     * 3. Idle - can be interrupted by anything
     */
    private PlayState animationController(AnimationTest<?> animTest) {
        // Priority 1: Death animation (highest priority, cannot be interrupted)
        if (this.isDead() || this.combatState.isUnconscious()) {
            return animTest.setAndContinue(DEATH);
        }

        // Priority 2: Action animations (attacks, casts, blocks, channels)
        // These cannot be interrupted except by higher priority actions

        // Check for channeling (continuous spell)
        if (isChanneling) {
            return animTest.setAndContinue(CHANNEL);
        }

        // Check for blocking
        if (isBlocking) {
            return animTest.setAndContinue(BLOCK);
        }

        // Check for casting
        if (isCasting) {
            // Auto-complete cast after duration
            if (getCastElapsedTime() >= 1000) {  // 1 second cast time
                completeCast();
            } else {
                return animTest.setAndContinue(CAST);
            }
        }

        // Check for attacking
        if (isAttacking || this.handSwingProgress > 0) {
            // Auto-complete attack after duration
            if (getAttackElapsedTime() >= 600) {  // 0.6 second attack animation
                completeAttack();
            } else {
                return animTest.setAndContinue(ATTACK);
            }
        }

        // Priority 3: Locomotion animations
        // Can be interrupted by action animations
        if (animTest.isMoving()) {
            if (this.isSprinting()) {
                return animTest.setAndContinue(RUN);
            } else {
                return animTest.setAndContinue(WALK);
            }
        }

        // Priority 4: Idle animation (lowest priority)
        return animTest.setAndContinue(IDLE);
    }

    /**
     * Calculate animation speed based on race and size scale.
     * Used for locomotion animations to match stride length.
     */
    private double getAnimationSpeed(AnimationTest<?> animTest) {
        // Get the skeleton profile for this race
        ninja.trek.srd.character.skeleton.SkeletonProfile profile =
            ninja.trek.srd.character.skeleton.SkeletonProfile.getByRaceName(race.getName());

        // Use the leg length ratio to scale animation speed
        float legRatio = profile.getLegLengthRatio();

        // Combine with global size scale
        // Larger creatures walk faster, smaller creatures walk slower
        return legRatio * sizeScale;
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.animationCache;
    }

    // Layer configuration getters/setters

    public LayerConfiguration getLayerConfiguration() {
        return layerConfiguration;
    }

    public void setLayerConfiguration(LayerConfiguration layerConfiguration) {
        this.layerConfiguration = layerConfiguration;
        this.layerConfigDirty = true;
    }

    public float getSizeScale() {
        return sizeScale;
    }

    public void setSizeScale(float sizeScale) {
        this.sizeScale = Math.max(0.5f, Math.min(3.0f, sizeScale));
    }

    /**
     * Mark layer configuration as dirty, requiring a sync to clients.
     * Call this whenever you modify the layer configuration directly.
     */
    public void markLayerConfigDirty() {
        this.layerConfigDirty = true;
    }

    /**
     * Called when a player starts tracking this entity.
     * Send initial layer configuration sync to the player.
     */
    @Override
    public void onStartedTrackingBy(net.minecraft.server.network.ServerPlayerEntity player) {
        super.onStartedTrackingBy(player);

        // Send full layer configuration to the new tracking player
        if (!this.getEntityWorld().isClient()) {
            ninja.trek.srd.network.LayerConfigSyncManager.syncFullConfigurationToPlayer(this, player);
        }
    }

    // Animation action trigger methods

    /**
     * Start an attack animation.
     * @return true if attack was started, false if it couldn't be (e.g., already attacking)
     */
    public boolean startAttack() {
        if (isAttacking) {
            return false;
        }
        isAttacking = true;
        attackStartTime = System.currentTimeMillis();
        return true;
    }

    /**
     * Start a spell casting animation.
     * @return true if casting was started, false if it couldn't be
     */
    public boolean startCast() {
        if (isCasting) {
            return false;
        }
        isCasting = true;
        castStartTime = System.currentTimeMillis();
        return true;
    }

    /**
     * Start a channeled spell animation.
     * @return true if channeling was started, false if it couldn't be
     */
    public boolean startChannel() {
        if (isChanneling) {
            return false;
        }
        isChanneling = true;
        return true;
    }

    /**
     * Stop channeling.
     */
    public void stopChannel() {
        isChanneling = false;
    }

    /**
     * Start blocking.
     * @return true if blocking was started, false if it couldn't be
     */
    public boolean startBlock() {
        if (isBlocking) {
            return false;
        }
        isBlocking = true;
        return true;
    }

    /**
     * Stop blocking.
     */
    public void stopBlock() {
        isBlocking = false;
    }

    /**
     * Complete the current attack.
     */
    public void completeAttack() {
        isAttacking = false;
        attackStartTime = 0;
    }

    /**
     * Complete the current cast.
     */
    public void completeCast() {
        isCasting = false;
        castStartTime = 0;
    }

    // Animation state getters

    public boolean isAttacking() {
        return isAttacking;
    }

    public boolean isCasting() {
        return isCasting;
    }

    public boolean isBlocking() {
        return isBlocking;
    }

    public boolean isChanneling() {
        return isChanneling;
    }

    /**
     * Get the time elapsed since attack started (in milliseconds).
     */
    public long getAttackElapsedTime() {
        if (!isAttacking) {
            return 0;
        }
        return System.currentTimeMillis() - attackStartTime;
    }

    /**
     * Get the time elapsed since cast started (in milliseconds).
     */
    public long getCastElapsedTime() {
        if (!isCasting) {
            return 0;
        }
        return System.currentTimeMillis() - castStartTime;
    }

    // TODO: Implement entity persistence using Minecraft 1.21.10 API
    // The NBT save/load methods have changed significantly in 1.21.10
    // For now, entity data will not persist across world reloads
    // This will be implemented once the correct API is identified
}
