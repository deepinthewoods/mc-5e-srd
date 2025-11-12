package ninja.trek.srd.character.entity;

import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.storage.ReadView;
import net.minecraft.storage.WriteView;
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
     * Write custom character data to storage using the ReadView/WriteView API.
     * This method uses Codecs for type-safe serialization.
     *
     * API: writeCustomData(WriteView) replaces addAdditionalSaveData(CompoundTag) in 1.21.6+
     */
    @Override
    protected void writeCustomData(WriteView view) {
        super.writeCustomData(view);

        // Write character stats using Codec
        view.put("stats", CharacterStats.CODEC, this.stats);

        // Write race using Codec
        view.put("race", Race.CODEC, this.race);

        // Write character class using Codec
        view.put("character_class", CharacterClass.CODEC, this.characterClass);

        // Write level as primitive
        view.putInt("level", this.level);

        // Write combat state using Codec
        view.put("combat_state", CombatState.CODEC, this.combatState);
    }

    /**
     * Read custom character data from storage using the ReadView/WriteView API.
     * This method uses Codecs for type-safe deserialization with fallback defaults.
     *
     * API: readCustomData(ReadView) replaces readAdditionalSaveData(CompoundTag) in 1.21.6+
     */
    @Override
    protected void readCustomData(ReadView view) {
        super.readCustomData(view);

        // Read character stats with fallback to default
        this.stats = view.read("stats", CharacterStats.CODEC)
            .orElse(CharacterStats.createDefault());

        // Read race with fallback to HUMAN
        this.race = view.read("race", Race.CODEC)
            .orElse(Race.HUMAN);

        // Read character class with fallback to FIGHTER
        this.characterClass = view.read("character_class", CharacterClass.CODEC)
            .orElse(CharacterClass.FIGHTER);

        // Read level with fallback to 1
        this.level = view.getOptionalInt("level")
            .orElse(1);

        // Read combat state with fallback to default
        int maxHp = calculateMaxHitPoints();
        int ac = calculateArmorClass();
        this.combatState = view.read("combat_state", CombatState.CODEC)
            .orElse(CombatState.createDefault(maxHp, ac));

        // Update Minecraft attributes after loading data
        updateMinecraftAttributes();
    }
}
