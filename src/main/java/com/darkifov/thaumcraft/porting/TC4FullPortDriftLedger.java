package com.darkifov.thaumcraft.porting;

import java.util.List;

/**
 * Stage194 consolidated drift ledger.
 *
 * This is not a replacement for TC4 parity work. It is a guardrail that keeps
 * the known remaining differences explicit while future stages continue porting
 * directly from the original Thaumcraft 4 classes/assets.
 */
public final class TC4FullPortDriftLedger {
    private TC4FullPortDriftLedger() {}

    public record DriftEntry(String system, String status, List<String> tc4Source, List<String> parityLocked, List<String> remainingDrift, List<String> adapterNotes) {}

    public static final int STAGE = 194;
    public static final String VERSION = "1.96.0";
    public static final String POLICY = "Strict original TC4 parity; Forge 1.19.2 adapters must be explicit and must not invent mechanics.";

    public static final List<DriftEntry> ENTRIES = List.of(
            new DriftEntry("golems", "partial", List.of("EntityGolemBase", "ItemGolemBell", "ItemGolemCore"), List.of("items", "icons", "basic registrations", "research/recipe gates"), List.of("exact TC4 golem AI task scheduler", "core behavior matrix", "upgrade stacking behavior"), List.of("Forge 1.19.2 entity/data-sync adapter required")),
            new DriftEntry("wands_foci", "runtime-audited", List.of("ItemWandCasting", "WandManager", "ItemFocusBasic", "ItemFocusFire", "ItemFocusFrost", "ItemFocusShock", "ItemFocusExcavation", "ItemFocusTrade", "ItemFocusPortableHole", "ItemFocusWarding", "ItemFocusPrimal"), List.of("rod/cap/sceptre/focus NBT", "focus upgrade NBT", "pouch Inventory/Slot", "architect NBT", "continuous use", "projectiles/renderers"), List.of("final held visual tuning", "focus upgrade UI", "projectile edge cases"), List.of("Baubles flow represented by Forge 1.19.2 inventory/offhand adapter")),
            new DriftEntry("aura_nodes", "runtime", List.of("TileNode", "AuraManager", "NodeType", "NodeModifier"), List.of("node profiles", "scan runtime", "node jar", "stabilizer/transducer"), List.of("aura tick economics", "node merge/drain edge cases", "energized network parity"), List.of("world capability storage replaces 1.7.10 world hooks")),
            new DriftEntry("crucible", "runtime", List.of("TileCrucible", "BlockMetalDevice", "ConfigRecipes"), List.of("heat/boil", "thrown item alchemy", "aspect consumption", "flux spill", "recipe gates"), List.of("spill randomness", "particle timings", "container item edges"), List.of("1.19.2 item/entity hooks replace 1.7.10 collision hooks")),
            new DriftEntry("infusion", "runtime-audited", List.of("TileInfusionMatrix", "ConfigRecipes", "InfusionRecipe", "InfusionEnchantmentRecipe"), List.of("catalyst/components", "enchantment index", "instability", "unlock gates", "materialized recipes", "craftCycle source ordering"), List.of("instability micro-timings", "uncommon recipes"), List.of("block entity tick adapter")),
            new DriftEntry("taint", "runtime", List.of("BlockTaint", "BlockTaintFibres", "BiomeGenTaint", "EntityTaint*"), List.of("metadata split", "fibres age", "spread adjacency", "texture guard"), List.of("biome conversion", "taint mobs", "poison/flux interactions"), List.of("blockstate properties replace metadata")),
            new DriftEntry("eldritch", "partial", List.of("EldritchDimension", "TileEldritchAltar", "EntityCultist*"), List.of("blocks/items/entities", "warp hooks", "research pages"), List.of("dimension/maze", "rituals", "boss/cultist AI", "portal events"), List.of("dimension registration adapter")),
            new DriftEntry("worldgen", "partial-audited", List.of("WorldGenSilverwoodTrees", "WorldGenGreatwoodTrees", "WorldGenAuraNodes", "WorldGenEldritchRing"), List.of("resources", "sapling/tree bridge", "node data"), List.of("weighted placement", "biome filters", "structures/loot", "ore positions"), List.of("ConfiguredFeature/PlacedFeature adapter")),
            new DriftEntry("thaumonomicon_research", "runtime-audited", List.of("ResearchCategories", "ResearchItem", "ResearchPage", "GuiResearchBrowser"), List.of("keys", "categories", "parents", "coordinates", "icons", "warp", "pages", "progression", "gates"), List.of("item-stack render micro-layout", "addon page edge cases"), List.of("screen adapter preserves TC4 data/layout")),
            new DriftEntry("research_table", "runtime-audited", List.of("TileResearchTable", "ContainerResearchTable", "GuiResearchTable", "ItemResearchNotes"), List.of("slots", "bonusAspects", "copy behavior", "axial hex grid", "completion", "drag/drop"), List.of("pixel offsets", "sound timing edges"), List.of("menu/screen/data-slot adapter")),
            new DriftEntry("arcane_workbench", "runtime-audited", List.of("TileArcaneWorkbench", "ContainerArcaneWorkbench", "GuiArcaneWorkbench", "SlotCraftingArcaneWorkbench", "ConfigRecipes"), List.of("0-8/9/10 slot layout", "menu order", "vanilla fallback", "output take", "click restrictions", "component recipes"), List.of("uncommon ConfigRecipes combinations", "vis display polish", "migration slot 11 retirement"), List.of("NetworkHooks.openScreen replaces legacy standalone packet screen", "hidden slot 11 is migration-only"))
    );

    public static long partialCount() {
        return ENTRIES.stream().filter(entry -> entry.status().contains("partial")).count();
    }
}
