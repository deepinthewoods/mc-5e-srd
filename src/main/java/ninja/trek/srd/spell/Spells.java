package ninja.trek.srd.spell;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.damage.DamageTypes;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.registry.RegistryKeys;
import ninja.trek.srd.util.DiceRoller;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Registry of all spells in the mod.
 *
 * This class contains all spell implementations and provides
 * access to them by ID.
 */
public class Spells {
    private static final Map<String, Spell> SPELL_REGISTRY = new HashMap<>();

    // Cantrips (Level 0)
    public static final Spell FIRE_BOLT = register(new Spell(
            "fire_bolt",
            "Fire Bolt",
            0,
            SpellSchool.EVOCATION,
            CastingTime.ACTION,
            Set.of(SpellComponent.VERBAL, SpellComponent.SOMATIC),
            24, // 120 feet = 24 blocks
            "Instantaneous",
            false,
            false
    ) {
        @Override
        public boolean cast(ServerWorld world, LivingEntity caster, LivingEntity target, int spellLevel) {
            if (target == null) return false;

            // Fire Bolt: 1d10 fire damage
            int damage = DiceRoller.roll(10);

            // Create damage source
            DamageSource damageSource = new DamageSource(
                    world.getRegistryManager()
                            .get(RegistryKeys.DAMAGE_TYPE)
                            .entryOf(DamageTypes.ON_FIRE)
            );

            target.damage(damageSource, damage);
            return true;
        }

        @Override
        public Text getDescription() {
            return Text.literal("You hurl a mote of fire at a creature. Make a ranged spell attack. On a hit, the target takes 1d10 fire damage.");
        }
    });

    public static final Spell SHOCKING_GRASP = register(new Spell(
            "shocking_grasp",
            "Shocking Grasp",
            0,
            SpellSchool.EVOCATION,
            CastingTime.ACTION,
            Set.of(SpellComponent.VERBAL, SpellComponent.SOMATIC),
            1, // Touch = 1 block
            "Instantaneous",
            false,
            false
    ) {
        @Override
        public boolean cast(ServerWorld world, LivingEntity caster, LivingEntity target, int spellLevel) {
            if (target == null) return false;

            // Shocking Grasp: 1d8 lightning damage
            int damage = DiceRoller.roll(8);

            // Create damage source
            DamageSource damageSource = new DamageSource(
                    world.getRegistryManager()
                            .get(RegistryKeys.DAMAGE_TYPE)
                            .entryOf(DamageTypes.LIGHTNING_BOLT)
            );

            target.damage(damageSource, damage);
            // TODO: Target can't take reactions until start of next turn
            return true;
        }

        @Override
        public Text getDescription() {
            return Text.literal("Lightning springs from your hand to deliver a shock. Make a melee spell attack. On a hit, the target takes 1d8 lightning damage, and it can't take reactions until the start of its next turn.");
        }
    });

    // Level 1 Spells
    public static final Spell MAGIC_MISSILE = register(new Spell(
            "magic_missile",
            "Magic Missile",
            1,
            SpellSchool.EVOCATION,
            CastingTime.ACTION,
            Set.of(SpellComponent.VERBAL, SpellComponent.SOMATIC),
            24, // 120 feet = 24 blocks
            "Instantaneous",
            false,
            false
    ) {
        @Override
        public boolean cast(ServerWorld world, LivingEntity caster, LivingEntity target, int spellLevel) {
            if (target == null) return false;

            // Magic Missile: 3 darts at 1st level, +1 dart per spell level above 1st
            int darts = 3 + (spellLevel - 1);
            int totalDamage = 0;

            for (int i = 0; i < darts; i++) {
                // Each dart: 1d4 + 1 force damage
                totalDamage += DiceRoller.roll(4) + 1;
            }

            // Create damage source
            DamageSource damageSource = new DamageSource(
                    world.getRegistryManager()
                            .get(RegistryKeys.DAMAGE_TYPE)
                            .entryOf(DamageTypes.MAGIC)
            );

            target.damage(damageSource, totalDamage);
            return true;
        }

        @Override
        public Text getDescription() {
            return Text.literal("You create three glowing darts of magical force. Each dart hits a creature and deals 1d4 + 1 force damage. The darts all strike simultaneously.");
        }
    });

    public static final Spell CURE_WOUNDS = register(new Spell(
            "cure_wounds",
            "Cure Wounds",
            1,
            SpellSchool.EVOCATION,
            CastingTime.ACTION,
            Set.of(SpellComponent.VERBAL, SpellComponent.SOMATIC),
            1, // Touch = 1 block
            "Instantaneous",
            false,
            false
    ) {
        @Override
        public boolean cast(ServerWorld world, LivingEntity caster, LivingEntity target, int spellLevel) {
            if (target == null) return false;

            // Cure Wounds: 1d8 + spellcasting modifier per spell level
            int healing = DiceRoller.rollMultiple(spellLevel, 8);
            // TODO: Add spellcasting modifier (WIS/CHA depending on class)

            float currentHealth = target.getHealth();
            float maxHealth = target.getMaxHealth();
            target.setHealth(Math.min(currentHealth + healing, maxHealth));

            return true;
        }

        @Override
        public Text getDescription() {
            return Text.literal("A creature you touch regains a number of hit points equal to 1d8 + your spellcasting ability modifier. This spell has no effect on undead or constructs.");
        }
    });

    public static final Spell SHIELD = register(new Spell(
            "shield",
            "Shield",
            1,
            SpellSchool.ABJURATION,
            CastingTime.REACTION,
            Set.of(SpellComponent.VERBAL, SpellComponent.SOMATIC),
            0, // Self
            "1 round",
            false,
            false
    ) {
        @Override
        public boolean cast(ServerWorld world, LivingEntity caster, LivingEntity target, int spellLevel) {
            // Shield: +5 AC until start of next turn
            // TODO: Implement AC bonus effect
            return true;
        }

        @Override
        public Text getDescription() {
            return Text.literal("An invisible barrier of magical force appears and protects you. Until the start of your next turn, you have a +5 bonus to AC.");
        }
    });

    // Level 2 Spells
    public static final Spell SCORCHING_RAY = register(new Spell(
            "scorching_ray",
            "Scorching Ray",
            2,
            SpellSchool.EVOCATION,
            CastingTime.ACTION,
            Set.of(SpellComponent.VERBAL, SpellComponent.SOMATIC),
            24, // 120 feet = 24 blocks
            "Instantaneous",
            false,
            false
    ) {
        @Override
        public boolean cast(ServerWorld world, LivingEntity caster, LivingEntity target, int spellLevel) {
            if (target == null) return false;

            // Scorching Ray: 3 rays at 2nd level, +1 ray per spell level above 2nd
            int rays = 3 + (spellLevel - 2);
            int totalDamage = 0;

            for (int i = 0; i < rays; i++) {
                // Each ray: 2d6 fire damage
                totalDamage += DiceRoller.rollMultiple(2, 6);
            }

            // Create damage source
            DamageSource damageSource = new DamageSource(
                    world.getRegistryManager()
                            .get(RegistryKeys.DAMAGE_TYPE)
                            .entryOf(DamageTypes.ON_FIRE)
            );

            target.damage(damageSource, totalDamage);
            return true;
        }

        @Override
        public Text getDescription() {
            return Text.literal("You create three rays of fire and hurl them at targets. Make a ranged spell attack for each ray. On a hit, the target takes 2d6 fire damage.");
        }
    });

    /**
     * Register a spell and return it.
     */
    private static Spell register(Spell spell) {
        SPELL_REGISTRY.put(spell.getId(), spell);
        return spell;
    }

    /**
     * Get a spell by its ID.
     */
    public static Spell getSpell(String id) {
        return SPELL_REGISTRY.get(id);
    }

    /**
     * Get all registered spells.
     */
    public static Map<String, Spell> getAllSpells() {
        return Map.copyOf(SPELL_REGISTRY);
    }

    /**
     * Initialize the spell registry.
     * Called during mod initialization.
     */
    public static void init() {
        // Spells are registered via static initializers
        // This method just ensures the class is loaded
    }
}
