package com.darkifov.thaumcraft.compat;

import com.darkifov.thaumcraft.ThaumcraftMod;
import net.minecraft.resources.ResourceLocation;

public final class ThaumcraftCompatIds {
    public static final ResourceLocation TAG_SHARDS = id("shards");
    public static final ResourceLocation TAG_PRIMAL_SHARDS = id("primal_shards");
    public static final ResourceLocation TAG_WANDS = id("wands");
    public static final ResourceLocation TAG_WAND_CAPS = id("wand_caps");
    public static final ResourceLocation TAG_WAND_CORES = id("wand_cores");
    public static final ResourceLocation TAG_ESSENTIA_CONTAINERS = id("essentia_containers");
    public static final ResourceLocation TAG_ELDRITCH_MATERIALS = id("eldritch_materials");
    public static final ResourceLocation TAG_ADDON_MAGIC_CATALYSTS = id("addon_magic_catalysts");

    public static final ResourceLocation TAG_ARCANE_DEVICES = id("arcane_devices");
    public static final ResourceLocation TAG_ESSENTIA_DEVICES = id("essentia_devices");
    public static final ResourceLocation TAG_ELDRITCH_BLOCKS = id("eldritch_blocks");
    public static final ResourceLocation TAG_AURA_BLOCKS = id("aura_blocks");

    private ThaumcraftCompatIds() {
    }

    private static ResourceLocation id(String path) {
        return new ResourceLocation(ThaumcraftMod.MOD_ID, path);
    }
}
