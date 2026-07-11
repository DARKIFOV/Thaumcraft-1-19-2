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

/** Registers TC4's Magical Forest and its Forge compatibility metadata. */
public final class TC4Biomes {
    public static final DeferredRegister<Biome> BIOMES =
            DeferredRegister.create(ForgeRegistries.BIOMES, ThaumcraftMod.MOD_ID);
    public static final ResourceKey<Biome> MAGICAL_FOREST_KEY = ResourceKey.create(
            net.minecraft.core.Registry.BIOME_REGISTRY,
            new ResourceLocation(ThaumcraftMod.MOD_ID, "magical_forest"));
    public static final RegistryObject<Biome> MAGICAL_FOREST =
            BIOMES.register("magical_forest", TC4Biomes::createMagicalForest);

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
                    new BiomeManager.BiomeEntry(MAGICAL_FOREST_KEY, 8));
        });
    }

    private static Biome createMagicalForest() {
        MobSpawnSettings.Builder spawns = new MobSpawnSettings.Builder();
        BiomeDefaultFeatures.farmAnimals(spawns);
        BiomeDefaultFeatures.commonSpawns(spawns);

        BiomeGenerationSettings.Builder generation = new BiomeGenerationSettings.Builder();
        BiomeDefaultFeatures.addDefaultCarversAndLakes(generation);
        BiomeDefaultFeatures.addDefaultMonsterRoom(generation);
        BiomeDefaultFeatures.addDefaultUndergroundVariety(generation);
        BiomeDefaultFeatures.addDefaultOres(generation);
        BiomeDefaultFeatures.addDefaultSoftDisks(generation);
        BiomeDefaultFeatures.addOtherBirchTrees(generation);
        BiomeDefaultFeatures.addForestFlowers(generation);
        BiomeDefaultFeatures.addForestGrass(generation);
        BiomeDefaultFeatures.addDefaultMushrooms(generation);
        BiomeDefaultFeatures.addDefaultExtraVegetation(generation);
        BiomeDefaultFeatures.addDefaultSprings(generation);

        BiomeSpecialEffects effects = new BiomeSpecialEffects.Builder()
                .fogColor(0xB7A7D9)
                .waterColor(0x3F76E4)
                .waterFogColor(0x050533)
                .skyColor(0x7AA7FF)
                .grassColorOverride(0x59C93C)
                .foliageColorOverride(0x34B94B)
                .ambientMoodSound(net.minecraft.world.level.biome.AmbientMoodSettings.LEGACY_CAVE_SETTINGS)
                .build();

        return new Biome.BiomeBuilder()
                .precipitation(Biome.Precipitation.RAIN)
                .temperature(0.7F)
                .downfall(0.8F)
                .specialEffects(effects)
                .mobSpawnSettings(spawns.build())
                .generationSettings(generation.build())
                .build();
    }
}
