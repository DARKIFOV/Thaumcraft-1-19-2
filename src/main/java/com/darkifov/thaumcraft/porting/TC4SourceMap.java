package com.darkifov.thaumcraft.porting;

import java.util.List;

/**
 * Human-readable strict-port roadmap from the uploaded TC4 source tree to the Forge 1.19.2 rebuild.
 * This is intentionally small and stable: detailed generated maps live in docs/porting and data/thaumcraft.
 */
public final class TC4SourceMap {
    public record SourceTarget(String tc4Source, String currentTarget, String priority, String notes) {
    }

    public static final String TC4_ROOT = "Thaumcraft4-1.7.10-master";
    public static final String CURRENT_ROOT = "thaumcraft_legacy_rebuild_STAGE110_COMPILE_API_FIX_CLEAN_UPLOAD_1_19_2";

    private static final List<SourceTarget> CORE_TARGETS = List.of(
            new SourceTarget("thaumcraft/api/aspects/Aspect.java", "com.darkifov.thaumcraft.Aspect + porting.TC4AspectBridge", "P0", "Exact original tags, colors, components, blend values and texture ids"),
            new SourceTarget("thaumcraft/api/aspects/AspectList.java", "com.darkifov.thaumcraft.AspectList", "P0", "List semantics, sorting, merge/reduce and NBT compatibility"),
            new SourceTarget("thaumcraft/common/config/ConfigBlocks.java", "ThaumcraftMod registry + block package", "P0", "Block ids, metadata/subtype split and block entity mapping"),
            new SourceTarget("thaumcraft/common/config/ConfigItems.java", "ThaumcraftMod registry + item/block package", "P0", "Item ids, wand caps/rods/foci, thaumonomicon and tools"),
            new SourceTarget("thaumcraft/common/config/ConfigRecipes.java", "data/thaumcraft/recipes + recipe managers", "P0", "Arcane, crucible and infusion recipes"),
            new SourceTarget("thaumcraft/common/config/ConfigResearch.java", "data/thaumcraft/research + research package", "P0", "Research keys, categories, pages, parents and unlock rules"),
            new SourceTarget("thaumcraft/common/blocks", "com.darkifov.thaumcraft.block", "P0", "Strict behavior ports for jars, tubes, tables, devices, taint and eldritch blocks"),
            new SourceTarget("thaumcraft/common/tiles", "com.darkifov.thaumcraft.blockentity", "P0", "TileEntity logic moved to BlockEntity equivalents"),
            new SourceTarget("thaumcraft/common/items/wands", "com.darkifov.thaumcraft.wand + WandItem", "P0", "Wand storage, vis, cap discounts, rods and focus trigger behavior"),
            new SourceTarget("thaumcraft/client/gui", "com.darkifov.thaumcraft.client.screen", "P0", "Original GUI layout and interaction behavior"),
            new SourceTarget("thaumcraft/client/renderers", "com.darkifov.thaumcraft.client.render", "P1", "Particles, special renderers and item/block visuals"),
            new SourceTarget("assets/thaumcraft", "src/main/resources/assets/thaumcraft", "P0", "Original assets, language keys, sounds, particles and GUI textures")
    );

    private TC4SourceMap() {
    }

    public static List<SourceTarget> coreTargets() {
        return CORE_TARGETS;
    }
}
