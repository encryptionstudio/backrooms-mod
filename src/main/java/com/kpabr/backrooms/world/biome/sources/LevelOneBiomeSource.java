package com.kpabr.backrooms.world.biome.sources;

import java.util.stream.Stream;

import com.kpabr.backrooms.BackroomsMod;
import com.kpabr.backrooms.init.BackroomsLevels;
import com.kpabr.backrooms.util.BiomeListBuilder;
import com.kpabr.backrooms.util.BiomeRegistryList;
import com.kpabr.backrooms.util.LevelParameters;
import com.mojang.serialization.Codec;
import com.mojang.serialization.Lifecycle;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.util.Identifier;
import net.minecraft.util.math.noise.SimplexNoiseSampler;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryCodecs;
import net.minecraft.registry.RegistryEntryLookup;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.RegistryOps;
import net.minecraft.registry.SimpleRegistry;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.source.BiomeSource;
import net.minecraft.world.biome.source.util.MultiNoiseUtil.MultiNoiseSampler;
import net.minecraft.util.math.random.ChunkRandom;

import net.minecraft.util.math.random.Random;

public class LevelOneBiomeSource extends BiomeSource{

    public static final Codec<LevelOneBiomeSource> CODEC = RecordCodecBuilder.create((instance) ->
			instance.group(RegistryOps.getEntryLookupCodec(RegistryKeys.BIOME))
					.apply(instance, instance.stable(LevelOneBiomeSource::new)));

    private final BiomeRegistryList biomeList;
    
    private SimplexNoiseSampler temperatureNoiseSampler;
    private SimplexNoiseSampler moistnessNoiseSampler;
    private SimplexNoiseSampler integrityNoiseSampler;
    private SimplexNoiseSampler purityNoiseSampler;
    private SimplexNoiseSampler toxicityNoiseSampler;
    private boolean isNoiseInitialized = false;

    private RegistryEntryLookup<Biome> BIOME_REGISTRY;

    public LevelOneBiomeSource(RegistryEntryLookup<Biome> biomeRegistry) {
        super();
        this.biomeList = BiomeRegistryList.from(new BiomeListBuilder()
        .addBiome(BackroomsLevels.WAREHOUSE_BIOME, new LevelParameters(0.45, 0.3, 0.8, 0.4, 0.05, 1))
        .addBiome(BackroomsLevels.PARKING_GARAGE_BIOME, new LevelParameters(0.4, 0.4, 0.65, 0.35, 0.05, 0.9))
        .addBiome(BackroomsLevels.CEMENT_WALLS_BIOME, new LevelParameters(0.35, 0.45, 0.75, 0.4, 0.05, 0.8)), biomeRegistry);  
        BIOME_REGISTRY = biomeRegistry;
    }

    @Override
    public RegistryEntry<Biome> getBiome(int x, int y, int z, MultiNoiseSampler noise) {
        if (!this.isNoiseInitialized) {

            long seed = BackroomsLevels.LEVEL_0_WORLD.getSeed();
            Random randomGenerator = Random.create(seed);
            
            // Generate five different random seeds based on the world seed
            long[] randomSeeds = new long[5];
            for (int i = 0; i < 5; i++) {
                randomSeeds[i] = randomGenerator.nextLong();
            }

            this.temperatureNoiseSampler = new SimplexNoiseSampler(new ChunkRandom(Random.create(randomSeeds[0])));
            
            this.moistnessNoiseSampler = new SimplexNoiseSampler(new ChunkRandom(Random.create(randomSeeds[1])));
            
            this.integrityNoiseSampler = new SimplexNoiseSampler(new ChunkRandom(Random.create(randomSeeds[2])));
            
            this.purityNoiseSampler = new SimplexNoiseSampler(new ChunkRandom(Random.create(randomSeeds[3])));
            
            this.toxicityNoiseSampler = new SimplexNoiseSampler(new ChunkRandom(Random.create(randomSeeds[4])));
            this.isNoiseInitialized = true;
        }
        
        double temperatureNoiseAt = getNoiseAt(this.temperatureNoiseSampler, x, y, z);
        double moistnessNoiseAt = getNoiseAt(this.moistnessNoiseSampler, x, y, z);
        double integrityNoiseAt = getNoiseAt(this.integrityNoiseSampler, x, y, z);
        double purityNoiseAt = getNoiseAt(this.purityNoiseSampler, x, y, z);
        double toxicityNoiseAt = getNoiseAt(this.toxicityNoiseSampler, x, y, z);
        
        return biomeList.findNearest(new LevelParameters(temperatureNoiseAt, moistnessNoiseAt, integrityNoiseAt, purityNoiseAt, toxicityNoiseAt, 0d));
    }

    public static double getNoiseAt(SimplexNoiseSampler perlinNoiseSampler, int x, int y, int z) {
        double n = perlinNoiseSampler.sample(x*0.01, y*0.01, z*0.01);

        //Transform the range to [0.0, 1.0], supposing that the range of Noise2D is [-1.0, 1.0]
        n += 1.0;
        n /= 2.0;
        
        return n;
    }

    @Override
    protected Codec<? extends BiomeSource> getCodec() {
        return CODEC;
    }

    @Override
    protected Stream<RegistryEntry<Biome>> biomeStream() {
        return Stream.of(this.BIOME_REGISTRY.getOrThrow(BackroomsLevels.PARKING_GARAGE_BIOME),
        this.BIOME_REGISTRY.getOrThrow(BackroomsLevels.CEMENT_WALLS_BIOME),
        this.BIOME_REGISTRY.getOrThrow(BackroomsLevels.WAREHOUSE_BIOME));
    }
}
