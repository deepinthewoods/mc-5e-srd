package ninja.trek.srd.registry;

import com.mojang.serialization.Codec;
import net.minecraft.registry.Registry;
import net.minecraft.component.ComponentType;
import net.minecraft.registry.Registries;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.util.Identifier;
import ninja.trek.srd.FiveESrdMod;
import ninja.trek.srd.character.data.*;
import ninja.trek.srd.combat.CombatState;

/**
 * Registry for custom data component types used by the mod.
 */
public class ModDataComponents {

    // Character data components
    public static final ComponentType<CharacterStats> CHARACTER_STATS =
        register("character_stats", CharacterStats.CODEC);

    public static final ComponentType<Race> RACE =
        register("race", Race.CODEC);

    public static final ComponentType<CharacterClass> CHARACTER_CLASS =
        register("character_class", CharacterClass.CODEC);

    public static final ComponentType<CharacterAppearance> APPEARANCE =
        register("appearance", CharacterAppearance.CODEC);

    public static final ComponentType<Integer> LEVEL =
        register("level", Codec.INT);

    public static final ComponentType<Integer> PROFICIENCY_BONUS =
        register("proficiency_bonus", Codec.INT);

    // Combat data components
    public static final ComponentType<CombatState> COMBAT_STATE =
        register("combat_state", CombatState.CODEC);

    /**
     * Register a data component type with a codec.
     */
    private static <T> ComponentType<T> register(String name, Codec<T> codec) {
        return Registry.register(
            Registries.DATA_COMPONENT_TYPE,
            Identifier.of(FiveESrdMod.MOD_ID, name),
            ComponentType.<T>builder()
                .codec(codec)
                .cache()
                .build()
        );
    }

    /**
     * Initialize all data components. Call this during mod initialization.
     */
    public static void initialize() {
        FiveESrdMod.LOGGER.info("Registering data components for " + FiveESrdMod.MOD_ID);
    }
}
