package com.darkifov.thaumcraft.world;

import com.darkifov.thaumcraft.ThaumcraftMod;
import net.minecraft.data.worldgen.BiomeDefaultFeatures;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeGenerationSettings;
import net.minecraft.world.level.biome.BiomeSpecialEffects;
import net.minecraft.world.level.biome.MobSpawnSettings;
import net.minecraftforge.common.BiomeManager;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

/** Registers TC4's Magical Forest, Eldritch biome and Forge compatibility metadata. */
public final class TC4Biomes {
    /** Config.biomeMagicalForestWeight default in TC4 4.2.3.5. */
    public static final int ORIGINAL_MAGICAL_FOREST_WEIGHT = 5;
    public static final DeferredRegister<Biome> BIOMES =
            DeferredRegister.create(ForgeRegistries.BIOMES, ThaumcraftMod.MOD_ID);
    public static final ResourceKey<Biome> MAGICAL_FOREST_KEY = ResourceKey.create(
            net.minecraft.core.Registry.BIOME_REGISTRY,
            new ResourceLocation(ThaumcraftMod.MOD_ID, "magical_forest"));
    public static final RegistryObject<Biome> MAGICAL_FOREST =
            BIOMES.register("magical_forest", TC4Biomes::createMagicalForest);
    public static final ResourceKey<Biome> ELDRITCH_KEY = ResourceKey.create(
            net.minecraft.core.Registry.BIOME_REGISTRY,
            new ResourceLocation(ThaumcraftMod.MOD_ID, "eldritch"));
    public static final RegistryObject<Biome> ELDRITCH =
            BIOMES.register("eldritch", TC4Biomes::createEldritch);

    private TC4Biomes() {
    }

    public static void register(IEventBus modBus) {
        BIOMES.register(modBus);
        modBus.addListener(TC4Biomes::commonSetup);
    }

    private static void commonSetup(FMLCommonSetupEvent event) {
        event.enqueueWork(() -> {
            // These are Forge compatibility/discovery lists. Vanilla's
            // already-created 1.19.2 MultiNoiseBiomeSource is patched at
            // server startup by MagicalForestWorldgenInstaller; the lists
            // alone are deliberately not treated as proof of generation.
            BiomeManager.addAdditionalOverworldBiomes(MAGICAL_FOREST_KEY);
            BiomeManager.addBiome(BiomeManager.BiomeType.WARM,
                    new BiomeManager.BiomeEntry(MAGICAL_FOREST_KEY, ORIGINAL_MAGICAL_FOREST_WEIGHT));
            // ThaumcraftWorldGenerator.initialize registers the biome in both
            // WARM and COOL lists with the same configured weight.
            BiomeManager.addBiome(BiomeManager.BiomeType.COOL,
                    new BiomeManager.BiomeEntry(MAGICAL_FOREST_KEY, ORIGINAL_MAGICAL_FOREST_WEIGHT));
        });
    }

    private static Biome createMagicalForest() {
        MobSpawnSettings.Builder spawns = new MobSpawnSettings.Builder();
        BiomeDefaultFeatures.farmAnimals(spawns);
        BiomeDefaultFeatures.commonSpawns(spawns);

        /*
         * v11.62.42 Forge 1.19.2 world-load hotfix:
         *
         * Do not assemble the custom biome from shared vanilla placed features here.
         * Cyanide correctly detected that the old hard-coded list introduced a
         * cross-biome placed-feature ordering cycle while new chunks were being
         * decorated. That cycle freezes world loading at 0% and ends with
         * FeatureCycleException.
         *
         * TC4 trees, ores, aura nodes and taint are already placed by the dedicated
         * Forge 1.19.2 TC4WorldgenRuntime new-chunk pass. Keeping this biome's
         * generation settings empty removes the illegal feature graph without
         * disabling the biome itself, its colours, mobs, /locate visibility or the
         * TC4 runtime generation bridge.
         */
        BiomeGenerationSettings.Builder generation = new BiomeGenerationSettings.Builder();

        BiomeSpecialEffects effects = new BiomeSpecialEffects.Builder()
                // BiomeGenMagicalForest does not override fog. Keep the
                // vanilla Overworld fog used by the original client.
                .fogColor(0xC0D8FF)
                // getWaterColorMultiplier() == 30702 in TC4 4.2.3.5.
                .waterColor(0x0077EE)
                .waterFogColor(0x050533)
                .skyColor(0x78A7FF)
                // BiomeGenMagicalForest#getBiomeGrassColor and
                // #getBiomeFoliageColor (blue_magical_forest=false).
                .grassColorOverride(0x55FF81)
                .foliageColorOverride(0x66FFC5)
                .ambientMoodSound(net.minecraft.world.level.biome.AmbientMoodSettings.LEGACY_CAVE_SETTINGS)
                .build();

        return new Biome.BiomeBuilder()
                .precipitation(Biome.Precipitation.RAIN)
                .temperature(0.7F)
                .downfall(0.6F)
                .specialEffects(effects)
                .mobSpawnSettings(spawns.build())
                .generationSettings(generation.build())
                .build();
    }
    private static Biome createEldritch() {
        MobSpawnSettings.Builder spawns = new MobSpawnSettings.Builder();
        BiomeGenerationSettings.Builder generation = new BiomeGenerationSettings.Builder();
        BiomeSpecialEffects effects = new BiomeSpecialEffects.Builder()
                // BiomeGenEldritch only overrides the sky colour to black.
                .fogColor(0xC0D8FF)
                .waterColor(0x3F76E4)
                .waterFogColor(0x050533)
                .skyColor(0x000000)
                .ambientMoodSound(net.minecraft.world.level.biome.AmbientMoodSettings.LEGACY_CAVE_SETTINGS)
                .build();
        return new Biome.BiomeBuilder()
                .precipitation(Biome.Precipitation.NONE)
                .temperature(0.5F)
                .downfall(0.0F)
                .specialEffects(effects)
                .mobSpawnSettings(spawns.build())
                .generationSettings(generation.build())
                .build();
    }

}
