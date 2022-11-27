package com.kpabr.backrooms.init;
import static com.kpabr.backrooms.util.RegistryHelper.get;


import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Optional;
import java.util.OptionalLong;

import com.kpabr.backrooms.BackroomsMod;
import com.kpabr.backrooms.world.biome.*;
import com.kpabr.backrooms.world.chunk.LevelOneChunkGenerator;
import com.kpabr.backrooms.world.chunk.LevelZeroChunkGenerator;
import net.ludocrypt.limlib.api.LiminalEffects;
import net.ludocrypt.limlib.api.LiminalWorld;
import net.ludocrypt.limlib.api.render.LiminalBaseEffects;
import net.ludocrypt.limlib.api.sound.ReverbSettings;
import net.ludocrypt.limlib.api.world.AbstractNbtChunkGenerator;
import net.minecraft.tag.TagKey;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.registry.RegistryKey;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.source.BiomeSource;
import net.minecraft.world.dimension.DimensionOptions;
import net.minecraft.world.dimension.DimensionType;

public class BackroomsLevels {

    public static final RegistryKey<Biome> DECREPIT_BIOME = get("decrepit", CrimsonHallsBiome.create());
    public static final RegistryKey<Biome> LEVEL_ZERO_NORMAL_BIOME = get("normal_biome", LevelZeroNormalBiome.create());
    public static final RegistryKey<Biome> CRIMSON_WALLS_BIOME = get("crimson_walls", CrimsonHallsBiome.create());
    public static final RegistryKey<Biome> CEMENT_WALLS_BIOME = get("cement_walls", CementHallsBiome.create());
    public static final RegistryKey<Biome> PARKING_GARAGE_BIOME = get("parking_garage", ParkingGarageBiome.create());
    public static final RegistryKey<Biome> WAREHOUSE_BIOME = get("warehouse", WarehouseBiome.create());

    public static final LiminalEffects LEVEL_ZERO_EFFECTS = new LiminalEffects(Optional.of(new LiminalBaseEffects.SimpleBaseEffects(Optional.empty(), false, "NONE", true, false, true)), Optional.empty(), Optional.empty(), Optional.empty(), Optional.of(new ReverbSettings().setDecayTime(2.15F).setDensity(0.725F)));
    public static final LiminalWorld LEVEL_0 = registerLevel("level_0", LevelZeroChunkGenerator.class, Level0BiomeSource.class);
    public static final LiminalWorld LEVEL_1 = registerLevel("level_1", LevelOneChunkGenerator.class, Level1BiomeSource.class);


    public static void init() {
        Registry.register(Registry.BIOME_SOURCE, "level_0_biome_source", Level0BiomeSource.CODEC);
        Registry.register(Registry.BIOME_SOURCE, "level_1_biome_source", Level1BiomeSource.CODEC);
        get("level_0_chunk_generator", LevelZeroChunkGenerator.CODEC);
        get("level_1_chunk_generator", LevelOneChunkGenerator.CODEC);
    }

    public static<T extends AbstractNbtChunkGenerator, S extends BaseBiomeSource> LiminalWorld registerLevel(String name, Class<T> chunkGenerator, Class<S> biomeSource) {
        final Identifier levelId = new Identifier(BackroomsMod.ModId, name);
        // Messy wrapper
        return get(levelId.getPath(), new LiminalWorld(levelId, DimensionType.create(OptionalLong.of(23500), true, false, false, true, 1.0, false, false, true, false, false, 0, 128, 128, TagKey.of(Registry.BLOCK_KEY, levelId), levelId, 0.075F),
                (world, dimensionTypeRegistry, biomeRegistry, structureRegistry, chunkGeneratorSettingsRegistry, noiseSettingsRegistry, registryManager, seed) -> new DimensionOptions(
                        dimensionTypeRegistry.getOrCreateEntry(world.getDimensionTypeKey()),
                        createChunkGenerator(chunkGenerator, biomeSource, biomeRegistry, seed)
                ),
                LEVEL_ZERO_EFFECTS));
    }

    public static<T extends AbstractNbtChunkGenerator, S extends BaseBiomeSource> LiminalWorld registerLevelWithEffects(Identifier name, Class<T> chunkGenerator, Class<S> biomeSource, LiminalEffects effects) {
        return get(name.getPath(), new LiminalWorld(name, DimensionType.create(OptionalLong.of(23500), true, false, false, true, 1.0, false, false, true, false, false, 0, 128, 128, TagKey.of(Registry.BLOCK_KEY, name), name, 0.075F),
                (world, dimensionTypeRegistry, biomeRegistry, structureRegistry, chunkGeneratorSettingsRegistry, noiseSettingsRegistry, registryManager, seed) ->
                        new DimensionOptions(
                                dimensionTypeRegistry.getOrCreateEntry(world.getDimensionTypeKey()),
                                createChunkGenerator(chunkGenerator, biomeSource, biomeRegistry, seed)
                        ),
                effects));
    }

    // Creating chunk generator for registerLevel in runtime.
    private static<T extends AbstractNbtChunkGenerator, S extends BiomeSource> T createChunkGenerator(Class<T> chunkGeneratorClass, Class<S> biomeSourceClass, Registry<Biome> registry, long seed) {
        try {
            Constructor<T> chunkGeneratorConstructor = chunkGeneratorClass.getConstructor(BiomeSource.class, long.class);
            Constructor<S> biomeSourceConstructor = biomeSourceClass.getConstructor(Registry.class, long.class);
            return chunkGeneratorConstructor.newInstance(biomeSourceConstructor.newInstance(registry, seed), seed);
        } catch (NoSuchMethodException | InvocationTargetException | InstantiationException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }
}
