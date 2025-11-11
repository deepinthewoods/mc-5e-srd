package ninja.trek.srd.registry;

import com.mojang.serialization.Codec;
import net.minecraft.core.Registry;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.resources.ResourceLocation;
import ninja.trek.srd.FiveESrdMod;
import ninja.trek.srd.character.data.*;
import ninja.trek.srd.combat.CombatState;

/**
 * Registry for custom data component types used by the mod.
 */
public class ModDataComponents {

    // Character data components
    public static final DataComponentType<CharacterStats> CHARACTER_STATS =
        register("character_stats", CharacterStats.CODEC);

    public static final DataComponentType<Race> RACE =
        register("race", Race.CODEC);

    public static final DataComponentType<CharacterClass> CHARACTER_CLASS =
        register("character_class", CharacterClass.CODEC);

    public static final DataComponentType<CharacterAppearance> APPEARANCE =
        register("appearance", CharacterAppearance.CODEC);

    public static final DataComponentType<Integer> LEVEL =
        register("level", Codec.INT);

    public static final DataComponentType<Integer> PROFICIENCY_BONUS =
        register("proficiency_bonus", Codec.INT);

    // Combat data components
    public static final DataComponentType<CombatState> COMBAT_STATE =
        register("combat_state", CombatState.CODEC);

    /**
     * Register a data component type with a codec.
     */
    private static <T> DataComponentType<T> register(String name, Codec<T> codec) {
        return Registry.register(
            BuiltInRegistries.DATA_COMPONENT_TYPE,
            ResourceLocation.fromNamespaceAndPath(FiveESrdMod.MOD_ID, name),
            DataComponentType.<T>builder()
                .persistent(codec)
                .networkSynchronized(ByteBufCodecs.fromCodec(codec))
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
