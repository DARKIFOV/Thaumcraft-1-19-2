package com.darkifov.thaumcraft.world;

import com.darkifov.thaumcraft.ThaumcraftMod;
import com.mojang.datafixers.util.Pair;
import com.mojang.logging.LogUtils;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.biome.Climate;
import net.minecraft.world.level.biome.MultiNoiseBiomeSource;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraftforge.event.server.ServerStartedEvent;
import net.minecraftforge.event.server.ServerStartingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.List;

/** Installs Magical Forest through Minecraft's serializable multi-noise codec. */
@Mod.EventBusSubscriber(modid = ThaumcraftMod.MOD_ID)
public final class MagicalForestWorldgenInstaller {
    private static final Logger LOGGER = LogUtils.getLogger();

    private MagicalForestWorldgenInstaller() {
    }

    @SubscribeEvent
    public static void onServerStarting(ServerStartingEvent event) {
        ensureInstalled(event.getServer().getLevel(Level.OVERWORLD));
    }

    @SubscribeEvent
    public static void onServerStarted(ServerStartedEvent event) {
        ensureInstalled(event.getServer().getLevel(Level.OVERWORLD));
    }

    public static boolean ensureInstalled(ServerLevel overworld) {
        if (overworld == null) {
            return false;
        }

        ChunkGenerator generator = overworld.getChunkSource().getGenerator();
        if (!(generator.getBiomeSource() instanceof MultiNoiseBiomeSource source)) {
            LOGGER.error("[TC4 worldgen] Magical Forest requires the vanilla Overworld multi-noise source, found {}.",
                    generator.getBiomeSource().getClass().getName());
            return false;
        }

        Registry<Biome> registry = overworld.registryAccess().registryOrThrow(Registry.BIOME_REGISTRY);
        Holder<Biome> magicalForest = registry.getHolder(TC4Biomes.MAGICAL_FOREST_KEY).orElse(null);
        if (magicalForest == null) {
            LOGGER.error("[TC4 worldgen] Cannot install Magical Forest: biome holder is missing.");
            return false;
        }
        if (source.possibleBiomes().contains(magicalForest)) {
            return true;
        }

        List<Pair<Climate.ParameterPoint, Holder<Biome>>> parameters = new ArrayList<>();
        int replaced = 0;
        int preservedFlowerForest = 0;
        int flowerForestPoint = 0;
        for (Pair<Climate.ParameterPoint, Holder<Biome>> pair : source.parameters.values()) {
            if (pair.getSecond().is(Biomes.FLOWER_FOREST)) {
                // TC4 adds Magical Forest to weighted WARM and COOL pools; it
                // does not delete Flower Forest. MultiNoiseBiomeSource has no
                // public weighted-pool API in 1.19.2, so split its existing
                // Flower Forest climate points deterministically.
                if ((flowerForestPoint++ & 1) == 0) {
                    parameters.add(Pair.of(pair.getFirst(), magicalForest));
                    replaced++;
                } else {
                    parameters.add(pair);
                    preservedFlowerForest++;
                }
            } else {
                parameters.add(pair);
            }
        }
        if (replaced == 0) {
            LOGGER.error("[TC4 worldgen] No Flower Forest climate points were available for Magical Forest.");
            return false;
        }

        MultiNoiseBiomeSource replacement = new MultiNoiseBiomeSource(
                new Climate.ParameterList<>(parameters));
        generator.biomeSource = replacement;
        boolean installed = generator.getBiomeSource().possibleBiomes().contains(magicalForest);
        LOGGER.info("[TC4 worldgen] Installed serializable Magical Forest multi-noise source: magicalPoints={}, preservedFlowerForestPoints={}, locateVisible={}.",
                replaced, preservedFlowerForest, installed);
        return installed;
    }
}
