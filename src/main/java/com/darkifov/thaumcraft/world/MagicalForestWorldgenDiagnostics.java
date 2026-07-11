package com.darkifov.thaumcraft.world;

import com.darkifov.thaumcraft.ThaumcraftMod;
import com.mojang.logging.LogUtils;
import net.minecraft.core.Registry;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraftforge.common.BiomeManager;
import net.minecraftforge.event.server.ServerStartedEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.slf4j.Logger;

/** Runtime proof that Magical Forest is part of the active overworld source. */
@Mod.EventBusSubscriber(modid = ThaumcraftMod.MOD_ID)
public final class MagicalForestWorldgenDiagnostics {
    private static final Logger LOGGER = LogUtils.getLogger();

    private MagicalForestWorldgenDiagnostics() {
    }

    @SubscribeEvent
    public static void onServerStarted(ServerStartedEvent event) {
        ServerLevel overworld = event.getServer().getLevel(Level.OVERWORLD);
        if (overworld == null) {
            LOGGER.error("[TC4 worldgen] Overworld is unavailable after server start; Magical Forest validation could not run.");
            return;
        }

        boolean installed = MagicalForestWorldgenInstaller.ensureInstalled(overworld);
        Registry<Biome> registry = overworld.registryAccess().registryOrThrow(Registry.BIOME_REGISTRY);
        BiomeSource source = overworld.getChunkSource().getGenerator().getBiomeSource();
        boolean registered = registry.getHolder(TC4Biomes.MAGICAL_FOREST_KEY).isPresent();
        boolean additional = BiomeManager.getAdditionalOverworldBiomes().contains(TC4Biomes.MAGICAL_FOREST_KEY);
        boolean warm = BiomeManager.getBiomes(BiomeManager.BiomeType.WARM).stream()
                .map(BiomeManager.BiomeEntry::getKey)
                .anyMatch(TC4Biomes.MAGICAL_FOREST_KEY::equals);
        boolean present = source.possibleBiomes().stream()
                .anyMatch(holder -> holder.is(TC4Biomes.MAGICAL_FOREST_KEY));

        LOGGER.info("[TC4 worldgen] Magical Forest diagnostic: registry={}, forgeAdditional={}, forgeWarm={}, installer={}, sourcePossible={}, sourceClass={}, possibleBiomeCount={}.",
                registered, additional, warm, installed, present,
                source.getClass().getName(), source.possibleBiomes().size());

        if (registered && additional && warm && installed && present) {
            LOGGER.info("[TC4 worldgen] PASS: thaumcraft:magical_forest is installed in the active Overworld BiomeSource. /locate biome can search for it in newly generated terrain.");
        } else {
            LOGGER.error("[TC4 worldgen] FAIL: Magical Forest did not complete the Forge worldgen path. registry={}, forgeAdditional={}, forgeWarm={}, installer={}, sourcePossible={}. Test a newly created Forge world and attach this log.",
                    registered, additional, warm, installed, present);
        }
    }
}
