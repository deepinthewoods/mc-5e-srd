package ninja.trek.srd.spell;

import net.minecraft.entity.LivingEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;

import java.util.Set;

/**
 * Represents a D&D 5e spell.
 *
 * This is the base class for all spells in the mod. Each spell defines its
 * properties (level, school, components, etc.) and implements the cast method
 * to handle its effects.
 */
public abstract class Spell {
    private final String id;
    private final String name;
    private final int level;
    private final SpellSchool school;
    private final CastingTime castingTime;
    private final Set<SpellComponent> components;
    private final int range; // Range in blocks
    private final String duration;
    private final boolean concentration;
    private final boolean ritual;

    protected Spell(String id, String name, int level, SpellSchool school,
                   CastingTime castingTime, Set<SpellComponent> components,
                   int range, String duration, boolean concentration, boolean ritual) {
        this.id = id;
        this.name = name;
        this.level = level;
        this.school = school;
        this.castingTime = castingTime;
        this.components = components;
        this.range = range;
        this.duration = duration;
        this.concentration = concentration;
        this.ritual = ritual;
    }

    /**
     * Cast this spell.
     *
     * @param world The world in which the spell is cast
     * @param caster The entity casting the spell
     * @param target The target entity (may be null for area spells)
     * @param spellLevel The level at which the spell is cast (for upcasting)
     * @return true if the spell was cast successfully
     */
    public abstract boolean cast(ServerWorld world, LivingEntity caster, LivingEntity target, int spellLevel);

    /**
     * Get the description of this spell.
     */
    public abstract Text getDescription();

    // Getters
    public String getId() { return id; }
    public String getName() { return name; }
    public int getLevel() { return level; }
    public SpellSchool getSchool() { return school; }
    public CastingTime getCastingTime() { return castingTime; }
    public Set<SpellComponent> getComponents() { return components; }
    public int getRange() { return range; }
    public String getDuration() { return duration; }
    public boolean requiresConcentration() { return concentration; }
    public boolean isRitual() { return ritual; }

    /**
     * Check if the spell requires a specific component.
     */
    public boolean requiresComponent(SpellComponent component) {
        return components.contains(component);
    }

    /**
     * Get the spell's display name with level.
     */
    public String getFullName() {
        if (level == 0) {
            return name + " (Cantrip)";
        }
        return name + " (Level " + level + ")";
    }
}
