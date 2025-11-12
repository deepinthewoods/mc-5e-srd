package ninja.trek.srd.character.entity;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import ninja.trek.srd.character.data.*;
import ninja.trek.srd.combat.CombatState;
import ninja.trek.srd.combat.EncounterManager;
import ninja.trek.srd.combat.EncounterState;
import ninja.trek.srd.util.DiceRoller;

/**
 * Base entity for D&D 5e characters with full character sheet data.
 */
public class CharacterEntity extends PathfinderMob {

    // Synced entity data
    private static final EntityDataAccessor<Integer> DATA_BODY_INDEX =
        SynchedEntityData.defineId(CharacterEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> DATA_LEGS_INDEX =
        SynchedEntityData.defineId(CharacterEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> DATA_ARMS_INDEX =
        SynchedEntityData.defineId(CharacterEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> DATA_HEAD_INDEX =
        SynchedEntityData.defineId(CharacterEntity.class, EntityDataSerializers.INT);

    // Character data (stored in NBT)
    private CharacterStats stats;
    private Race race;
    private CharacterClass characterClass;
    private int level;
    private CombatState combatState;

    public CharacterEntity(EntityType<? extends PathfinderMob> entityType, Level level) {
        super(entityType, level);
        // Initialize with defaults
        this.stats = CharacterStats.createDefault();
        this.race = Race.HUMAN;
        this.characterClass = CharacterClass.FIGHTER;
        this.level = 1;

        // Calculate initial combat state
        int maxHp = calculateMaxHitPoints();
        int ac = calculateArmorClass();
        this.combatState = CombatState.createDefault(maxHp, ac);

        // Register AI goals if on server
        if (!level.isClientSide()) {
            registerAIGoals();
        }
    }

    /**
     * Create default attribute supplier for character entities.
     */
    public static AttributeSupplier.Builder createAttributes() {
        return PathfinderMob.createMobAttributes()
            .add(Attributes.MAX_HEALTH, 10.0)
            .add(Attributes.MOVEMENT_SPEED, 0.25)
            .add(Attributes.ATTACK_DAMAGE, 2.0)
            .add(Attributes.ARMOR, 0.0);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(DATA_BODY_INDEX, 0);
        builder.define(DATA_LEGS_INDEX, 0);
        builder.define(DATA_ARMS_INDEX, 0);
        builder.define(DATA_HEAD_INDEX, 0);
    }

    /**
     * Register AI goals for this character entity.
     */
    private void registerAIGoals() {
        this.goalSelector.addGoal(1, new ninja.trek.srd.character.ai.CombatAIController(this));
        this.targetSelector.addGoal(1, new net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal<>(
            this,
            net.minecraft.world.entity.player.Player.class,
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
        this.entityData.set(DATA_BODY_INDEX, appearance.bodyIndex());
        this.entityData.set(DATA_LEGS_INDEX, appearance.legsIndex());
        this.entityData.set(DATA_ARMS_INDEX, appearance.armsIndex());
        this.entityData.set(DATA_HEAD_INDEX, appearance.headIndex());
    }

    /**
     * Get character appearance.
     */
    public CharacterAppearance getAppearance() {
        return new CharacterAppearance(
            this.entityData.get(DATA_BODY_INDEX),
            this.entityData.get(DATA_LEGS_INDEX),
            this.entityData.get(DATA_ARMS_INDEX),
            this.entityData.get(DATA_HEAD_INDEX)
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
        if (this.getAttribute(Attributes.MAX_HEALTH) != null) {
            this.getAttribute(Attributes.MAX_HEALTH).setBaseValue(combatState.maxHitPoints());
            this.setHealth(combatState.currentHitPoints());
        }

        // Set movement speed based on race
        if (this.getAttribute(Attributes.MOVEMENT_SPEED) != null) {
            // Convert blocks per turn to blocks per tick
            // 6 blocks per turn / 20 ticks per second * some scaling factor
            double movementSpeed = race.getBaseMovementSpeed() * 0.05;
            this.getAttribute(Attributes.MOVEMENT_SPEED).setBaseValue(movementSpeed);
        }

        // Set armor
        if (this.getAttribute(Attributes.ARMOR) != null) {
            this.getAttribute(Attributes.ARMOR).setBaseValue(combatState.armorClass());
        }
    }

    /**
     * Start this entity's turn in combat.
     */
    public void startTurn() {
        if (!combatState.inCombat()) {
            return;
        }

        Vec3 currentPos = this.position();
        int movementSpeed = race.getBaseMovementSpeed();
        this.combatState = combatState.startTurn(currentPos, movementSpeed);
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
        EncounterState encounter = EncounterManager.getInstance().getEncounterForEntity(this.getUUID());
        if (encounter == null) {
            return false;
        }
        return this.getUUID().equals(encounter.getCurrentTurnEntity());
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
    }

    /**
     * Save character data to NBT.
     * API: Entity.addAdditionalSaveData(CompoundTag) - Minecraft 1.21.10
     */
    @Override
    protected void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);

        // Save character data
        tag.putString("Race", race.getSerializedName());
        tag.putString("Class", characterClass.getSerializedName());
        tag.putInt("Level", level);

        // Save stats
        CompoundTag statsTag = new CompoundTag();
        statsTag.putInt("STR", stats.strength());
        statsTag.putInt("DEX", stats.dexterity());
        statsTag.putInt("CON", stats.constitution());
        statsTag.putInt("INT", stats.intelligence());
        statsTag.putInt("WIS", stats.wisdom());
        statsTag.putInt("CHA", stats.charisma());
        tag.put("Stats", statsTag);

        // Save combat state
        CompoundTag combatTag = new CompoundTag();
        combatTag.putBoolean("InCombat", combatState.inCombat());
        combatTag.putInt("Initiative", combatState.initiative());
        combatTag.putInt("RemainingMovement", combatState.remainingMovement());
        combatTag.putBoolean("HasAction", combatState.hasAction());
        combatTag.putBoolean("HasBonusAction", combatState.hasBonusAction());
        combatTag.putBoolean("HasReaction", combatState.hasReaction());
        combatTag.putInt("ArmorClass", combatState.armorClass());
        combatTag.putInt("CurrentHP", combatState.currentHitPoints());
        combatTag.putInt("MaxHP", combatState.maxHitPoints());
        tag.put("Combat", combatTag);
    }

    /**
     * Load character data from NBT.
     * API: Entity.readAdditionalSaveData(CompoundTag) - Minecraft 1.21.10
     */
    @Override
    protected void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);

        // Load character data
        if (tag.contains("Race")) {
            this.race = Race.fromSerializedName(tag.getString("Race"));
        }
        if (tag.contains("Class")) {
            this.characterClass = CharacterClass.fromSerializedName(tag.getString("Class"));
        }
        if (tag.contains("Level")) {
            this.level = tag.getInt("Level");
        }

        // Load stats
        if (tag.contains("Stats")) {
            CompoundTag statsTag = tag.getCompound("Stats");
            this.stats = new CharacterStats(
                statsTag.getInt("STR"),
                statsTag.getInt("DEX"),
                statsTag.getInt("CON"),
                statsTag.getInt("INT"),
                statsTag.getInt("WIS"),
                statsTag.getInt("CHA")
            );
        }

        // Load combat state
        if (tag.contains("Combat")) {
            CompoundTag combatTag = tag.getCompound("Combat");
            this.combatState = new CombatState(
                combatTag.getBoolean("InCombat"),
                combatTag.getInt("Initiative"),
                combatTag.getInt("RemainingMovement"),
                combatTag.getBoolean("HasAction"),
                combatTag.getBoolean("HasBonusAction"),
                combatTag.getBoolean("HasReaction"),
                combatTag.getInt("ArmorClass"),
                combatTag.getInt("CurrentHP"),
                combatTag.getInt("MaxHP"),
                null  // turnStartPosition is not persisted
            );
        }

        // Update Minecraft attributes based on loaded data
        updateMinecraftAttributes();
    }
}
