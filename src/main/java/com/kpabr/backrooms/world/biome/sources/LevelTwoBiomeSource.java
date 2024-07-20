package com.kpabr.backrooms.world.biome.sources;

import com.kpabr.backrooms.init.BackroomsLevels;
import com.kpabr.backrooms.world.chunk.LevelTwoChunkGenerator;
import com.mojang.serialization.Codec;
import com.mojang.serialization.Lifecycle;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryCodecs;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.source.BiomeSource;
import net.minecraft.world.biome.source.util.MultiNoiseUtil.MultiNoiseSampler;

import java.util.stream.Stream;

public class LevelTwoBiomeSource extends BiomeSource{
    
    public static final Codec<LevelTwoBiomeSource> CODEC = Codec.unit(new LevelTwoBiomeSource());
    private Registry<Biome> BIOME_REGISTRY;
    private final RegistryEntry<Biome> PIPES_BIOME;

    public LevelTwoBiomeSource() {
        super();
        PIPES_BIOME = BackroomsLevels.BIOME_REGISTRY.getEntry(BackroomsLevels.PIPES_BIOME).get();
        this.BIOME_REGISTRY = BackroomsLevels.BIOME_REGISTRY;
    }

    @Override
    public RegistryEntry<Biome> getBiome(int x, int y, int z, MultiNoiseSampler noise) {
        return PIPES_BIOME;
    }

    @Override
    protected Codec<LevelTwoBiomeSource> getCodec() {
        return CODEC;
    }

    @Override
    protected Stream<RegistryEntry<Biome>> biomeStream() {
        return BIOME_REGISTRY.stream()
            .map(BIOME_REGISTRY::getEntry);
    }
}
