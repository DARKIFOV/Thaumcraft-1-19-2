package com.darkifov.thaumcraft.source;

import com.darkifov.thaumcraft.Aspect;
import com.darkifov.thaumcraft.AspectList;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;

/**
 * Stage115 strict TC4 object-aspect bridge.
 * Values are generated from thaumcraft.common.config.ConfigAspects in TC4 1.7.10.
 * The old 1.7.10 classes are not loaded directly; only data-safe mappings are used at runtime.
 */
public final class TC4ObjectAspectRegistry {
    private static final Map<String, AspectList> EXACT = new LinkedHashMap<>();
    private static final Map<String, AspectList> LEGACY_OREDICT = new LinkedHashMap<>();

    static {
        legacy("stone", aspects(Aspect.TERRA, 2));
        legacy("cobblestone", aspects(Aspect.TERRA, 1, Aspect.PERDITIO, 1));
        legacy("logWood", aspects(Aspect.ARBOR, 4));
        legacy("plankWood", aspects(Aspect.ARBOR, 1));
        legacy("slabWood", aspects(Aspect.ARBOR, 1));
        legacy("stairWood", aspects(Aspect.ARBOR, 1));
        legacy("stickWood", aspects(Aspect.ARBOR, 1));
        legacy("treeSapling", aspects(Aspect.ARBOR, 1, Aspect.HERBA, 2));
        legacy("treeLeaves", aspects(Aspect.HERBA, 1));
        legacy("oreLapis", aspects(Aspect.TERRA, 1, Aspect.SENSUS, 3));
        legacy("oreDiamond", aspects(Aspect.TERRA, 1, Aspect.LUCRUM, 3, Aspect.VITREUS, 3));
        legacy("gemDiamond", aspects(Aspect.VITREUS, 4, Aspect.LUCRUM, 4));
        legacy("oreRedstone", aspects(Aspect.TERRA, 1, Aspect.POTENTIA, 2, Aspect.MACHINA, 2));
        legacy("oreEmerald", aspects(Aspect.TERRA, 1, Aspect.LUCRUM, 4, Aspect.VITREUS, 3));
        legacy("gemEmerald", aspects(Aspect.VITREUS, 4, Aspect.LUCRUM, 5));
        legacy("oreQuartz", aspects(Aspect.TERRA, 1, Aspect.VITREUS, 3));
        legacy("nuggetIron", aspects(Aspect.METALLUM, 1));
        legacy("oreIron", aspects(Aspect.TERRA, 1, Aspect.METALLUM, 3));
        legacy("dustIron", aspects(Aspect.METALLUM, 3, Aspect.PERDITIO, 1));
        legacy("oreGold", aspects(Aspect.TERRA, 1, Aspect.METALLUM, 2, Aspect.LUCRUM, 1));
        legacy("dustGold", aspects(Aspect.METALLUM, 2, Aspect.PERDITIO, 1, Aspect.LUCRUM, 1));
        legacy("dustRedstone", aspects(Aspect.POTENTIA, 2, Aspect.MACHINA, 1));
        legacy("dustGlowstone", aspects(Aspect.SENSUS, 1, Aspect.LUX, 2));
        legacy("glowstone", aspects(Aspect.SENSUS, 3, Aspect.LUX, 10));
        legacy("nuggetCopper", aspects(Aspect.METALLUM, 1));
        legacy("ingotCopper", aspects(Aspect.METALLUM, 3, Aspect.PERMUTATIO, 1));
        legacy("dustCopper", aspects(Aspect.METALLUM, 2, Aspect.PERDITIO, 1, Aspect.PERMUTATIO, 1));
        legacy("oreCopper", aspects(Aspect.METALLUM, 2, Aspect.TERRA, 1, Aspect.PERMUTATIO, 1));
        legacy("nuggetTin", aspects(Aspect.METALLUM, 1));
        legacy("ingotTin", aspects(Aspect.METALLUM, 3, Aspect.VITREUS, 1));
        legacy("dustTin", aspects(Aspect.METALLUM, 2, Aspect.PERDITIO, 1, Aspect.VITREUS, 1));
        legacy("oreTin", aspects(Aspect.METALLUM, 3, Aspect.PERDITIO, 1, Aspect.VITREUS, 1));
        legacy("nuggetSilver", aspects(Aspect.METALLUM, 1));
        legacy("ingotSilver", aspects(Aspect.METALLUM, 3, Aspect.LUCRUM, 1));
        legacy("dustSilver", aspects(Aspect.METALLUM, 2, Aspect.PERDITIO, 1, Aspect.LUCRUM, 1));
        legacy("oreSilver", aspects(Aspect.METALLUM, 3, Aspect.PERDITIO, 1, Aspect.LUCRUM, 1));
        legacy("nuggetLead", aspects(Aspect.METALLUM, 1));
        legacy("ingotLead", aspects(Aspect.METALLUM, 3, Aspect.ORDO, 1));
        legacy("dustLead", aspects(Aspect.METALLUM, 2, Aspect.PERDITIO, 1, Aspect.ORDO, 1));
        legacy("oreLead", aspects(Aspect.METALLUM, 3, Aspect.PERDITIO, 1, Aspect.ORDO, 1));

        exact("minecraft:stone", aspects(Aspect.TERRA, 2));
        exact("minecraft:granite", aspects(Aspect.TERRA, 2));
        exact("minecraft:diorite", aspects(Aspect.TERRA, 2));
        exact("minecraft:andesite", aspects(Aspect.TERRA, 2));
        exact("minecraft:deepslate", aspects(Aspect.TERRA, 2));
        exact("minecraft:cobblestone", aspects(Aspect.TERRA, 1, Aspect.PERDITIO, 1));
        exact("minecraft:mossy_cobblestone", aspects(Aspect.TERRA, 1, Aspect.PERDITIO, 1));
        exact("minecraft:cobbled_deepslate", aspects(Aspect.TERRA, 1, Aspect.PERDITIO, 1));
        exact("minecraft:stick", aspects(Aspect.ARBOR, 1));
        exact("minecraft:lapis_ore", aspects(Aspect.TERRA, 1, Aspect.SENSUS, 3));
        exact("minecraft:deepslate_lapis_ore", aspects(Aspect.TERRA, 1, Aspect.SENSUS, 3));
        exact("minecraft:diamond_ore", aspects(Aspect.TERRA, 1, Aspect.LUCRUM, 3, Aspect.VITREUS, 3));
        exact("minecraft:deepslate_diamond_ore", aspects(Aspect.TERRA, 1, Aspect.LUCRUM, 3, Aspect.VITREUS, 3));
        exact("minecraft:diamond", aspects(Aspect.VITREUS, 4, Aspect.LUCRUM, 4));
        exact("minecraft:redstone_ore", aspects(Aspect.TERRA, 1, Aspect.POTENTIA, 2, Aspect.MACHINA, 2));
        exact("minecraft:deepslate_redstone_ore", aspects(Aspect.TERRA, 1, Aspect.POTENTIA, 2, Aspect.MACHINA, 2));
        exact("minecraft:emerald_ore", aspects(Aspect.TERRA, 1, Aspect.LUCRUM, 4, Aspect.VITREUS, 3));
        exact("minecraft:deepslate_emerald_ore", aspects(Aspect.TERRA, 1, Aspect.LUCRUM, 4, Aspect.VITREUS, 3));
        exact("minecraft:emerald", aspects(Aspect.VITREUS, 4, Aspect.LUCRUM, 5));
        exact("minecraft:nether_quartz_ore", aspects(Aspect.TERRA, 1, Aspect.VITREUS, 3));
        exact("minecraft:iron_nugget", aspects(Aspect.METALLUM, 1));
        exact("minecraft:iron_ore", aspects(Aspect.TERRA, 1, Aspect.METALLUM, 3));
        exact("minecraft:deepslate_iron_ore", aspects(Aspect.TERRA, 1, Aspect.METALLUM, 3));
        exact("minecraft:gold_ore", aspects(Aspect.TERRA, 1, Aspect.METALLUM, 2, Aspect.LUCRUM, 1));
        exact("minecraft:deepslate_gold_ore", aspects(Aspect.TERRA, 1, Aspect.METALLUM, 2, Aspect.LUCRUM, 1));
        exact("minecraft:nether_gold_ore", aspects(Aspect.TERRA, 1, Aspect.METALLUM, 2, Aspect.LUCRUM, 1));
        exact("minecraft:redstone", aspects(Aspect.POTENTIA, 2, Aspect.MACHINA, 1));
        exact("minecraft:glowstone_dust", aspects(Aspect.SENSUS, 1, Aspect.LUX, 2));
        exact("minecraft:glowstone", aspects(Aspect.SENSUS, 3, Aspect.LUX, 10));
        exact("minecraft:copper_ingot", aspects(Aspect.METALLUM, 3, Aspect.PERMUTATIO, 1));
        exact("minecraft:copper_ore", aspects(Aspect.METALLUM, 2, Aspect.TERRA, 1, Aspect.PERMUTATIO, 1));
        exact("minecraft:deepslate_copper_ore", aspects(Aspect.METALLUM, 2, Aspect.TERRA, 1, Aspect.PERMUTATIO, 1));
        exact("minecraft:bedrock", aspects(Aspect.VACUOS, 16, Aspect.PERDITIO, 16, Aspect.TERRA, 16, Aspect.TENEBRAE, 16));
        exact("minecraft:dirt", aspects(Aspect.TERRA, 2));
        exact("minecraft:grass_block", aspects(Aspect.TERRA, 1, Aspect.HERBA, 1));
        exact("minecraft:farmland", aspects(Aspect.TERRA, 1, Aspect.METO, 2));
        exact("minecraft:sand", aspects(Aspect.TERRA, 1, Aspect.PERDITIO, 1));
        exact("minecraft:gravel", aspects(Aspect.TERRA, 2));
        exact("minecraft:clay", aspects(Aspect.TERRA, 1, Aspect.HERBA, 1));
        exact("minecraft:clay_ball", aspects(Aspect.AQUA, 1, Aspect.TERRA, 1));
        exact("minecraft:mycelium", aspects(Aspect.TERRA, 1, Aspect.TENEBRAE, 1));
        exact("minecraft:sponge", aspects(Aspect.TERRA, 3, Aspect.AQUA, 3));
        exact("minecraft:terracotta", aspects(Aspect.TERRA, 4, Aspect.IGNIS, 1));
        exact("minecraft:white_terracotta", aspects(Aspect.TERRA, 3, Aspect.IGNIS, 1, Aspect.SENSUS, 1));
        exact("minecraft:netherrack", aspects(Aspect.TERRA, 2, Aspect.IGNIS, 1));
        exact("minecraft:soul_sand", aspects(Aspect.TERRA, 1, Aspect.VINCULUM, 1, Aspect.SPIRITUS, 1));
        exact("minecraft:nether_bricks", aspects(Aspect.TERRA, 2, Aspect.IGNIS, 1));
        exact("minecraft:glass", aspects(Aspect.VITREUS, 1));
        exact("minecraft:white_stained_glass", aspects(Aspect.VITREUS, 1));
        exact("minecraft:bookshelf", aspects(Aspect.COGNITIO, 8));
        exact("minecraft:book", aspects(Aspect.COGNITIO, 4));
        exact("minecraft:writable_book", aspects(Aspect.COGNITIO, 5));
        exact("minecraft:paper", aspects(Aspect.ARBOR, 1, Aspect.COGNITIO, 1));
        exact("minecraft:leather", aspects(Aspect.BESTIA, 1, Aspect.PANNUS, 1));
        exact("minecraft:string", aspects(Aspect.PANNUS, 1));
        exact("minecraft:white_wool", aspects(Aspect.PANNUS, 4, Aspect.BESTIA, 1));
        exact("minecraft:feather", aspects(Aspect.VOLATUS, 2, Aspect.AER, 1));
        exact("minecraft:bone", aspects(Aspect.MORTUUS, 2));
        exact("minecraft:rotten_flesh", aspects(Aspect.CORPUS, 4, Aspect.MORTUUS, 1));
        exact("minecraft:spider_eye", aspects(Aspect.VENENUM, 2, Aspect.BESTIA, 1));
        exact("minecraft:gunpowder", aspects(Aspect.IGNIS, 2, Aspect.PERDITIO, 2));
        exact("minecraft:ender_pearl", aspects(Aspect.ALIENIS, 4, Aspect.ITER, 4));
        exact("minecraft:blaze_rod", aspects(Aspect.IGNIS, 4, Aspect.PRAECANTATIO, 1));
        exact("minecraft:blaze_powder", aspects(Aspect.IGNIS, 2, Aspect.POTENTIA, 2));
        exact("minecraft:ghast_tear", aspects(Aspect.SPIRITUS, 4, Aspect.SANO, 2));
        exact("minecraft:slime_ball", aspects(Aspect.LIMUS, 4));
        exact("minecraft:flint", aspects(Aspect.TELUM, 1, Aspect.TERRA, 1));
        exact("minecraft:coal", aspects(Aspect.IGNIS, 2, Aspect.POTENTIA, 2));
        exact("minecraft:charcoal", aspects(Aspect.IGNIS, 2, Aspect.POTENTIA, 2, Aspect.ARBOR, 1));
        exact("minecraft:iron_ingot", aspects(Aspect.METALLUM, 4));
        exact("minecraft:gold_ingot", aspects(Aspect.METALLUM, 3, Aspect.LUCRUM, 2));
        exact("minecraft:netherite_ingot", aspects(Aspect.METALLUM, 8, Aspect.PERDITIO, 4, Aspect.TENEBRAE, 2));
        exact("minecraft:quartz", aspects(Aspect.VITREUS, 3, Aspect.ORDO, 1));
        exact("minecraft:lapis_lazuli", aspects(Aspect.SENSUS, 3, Aspect.VITREUS, 1));
        exact("minecraft:amethyst_shard", aspects(Aspect.VITREUS, 3, Aspect.PRAECANTATIO, 1));
        exact("minecraft:apple", aspects(Aspect.VICTUS, 1, Aspect.FAMES, 2, Aspect.HERBA, 1));
        exact("minecraft:bread", aspects(Aspect.FAMES, 4, Aspect.MESSIS, 2));
        exact("minecraft:wheat", aspects(Aspect.MESSIS, 3, Aspect.HERBA, 1));
        exact("minecraft:carrot", aspects(Aspect.FAMES, 2, Aspect.HERBA, 1));
        exact("minecraft:potato", aspects(Aspect.FAMES, 2, Aspect.HERBA, 1));
        exact("minecraft:porkchop", aspects(Aspect.FAMES, 3, Aspect.BESTIA, 1));
        exact("minecraft:beef", aspects(Aspect.FAMES, 3, Aspect.BESTIA, 1));
        exact("minecraft:chicken", aspects(Aspect.FAMES, 3, Aspect.BESTIA, 1));
        exact("minecraft:cod", aspects(Aspect.FAMES, 2, Aspect.AQUA, 1, Aspect.BESTIA, 1));
        exact("minecraft:bow", aspects(Aspect.TELUM, 3, Aspect.ARBOR, 1, Aspect.PANNUS, 1));
        exact("minecraft:arrow", aspects(Aspect.TELUM, 2, Aspect.VOLATUS, 1));
        exact("minecraft:fishing_rod", aspects(Aspect.INSTRUMENTUM, 2, Aspect.AQUA, 1, Aspect.ARBOR, 1));
        exact("minecraft:shears", aspects(Aspect.INSTRUMENTUM, 2, Aspect.METALLUM, 2, Aspect.METO, 1));
        exact("minecraft:bucket", aspects(Aspect.VACUOS, 1, Aspect.METALLUM, 3));
        exact("minecraft:water_bucket", aspects(Aspect.AQUA, 8, Aspect.METALLUM, 3));
        exact("minecraft:lava_bucket", aspects(Aspect.IGNIS, 8, Aspect.METALLUM, 3));
        exact("minecraft:clock", aspects(Aspect.MACHINA, 2, Aspect.ORDO, 2, Aspect.LUCRUM, 1));
        exact("minecraft:compass", aspects(Aspect.MACHINA, 2, Aspect.ITER, 2, Aspect.METALLUM, 1));
        exact("minecraft:piston", aspects(Aspect.MACHINA, 4, Aspect.MOTUS, 2));
        exact("minecraft:sticky_piston", aspects(Aspect.MACHINA, 4, Aspect.MOTUS, 2, Aspect.LIMUS, 2));
        exact("minecraft:hopper", aspects(Aspect.MACHINA, 3, Aspect.PERMUTATIO, 2, Aspect.METALLUM, 2));
        exact("minecraft:tnt", aspects(Aspect.IGNIS, 4, Aspect.PERDITIO, 4, Aspect.VINCULUM, 1));
        exact("minecraft:minecart", aspects(Aspect.MACHINA, 3, Aspect.ITER, 2, Aspect.METALLUM, 3));
        exact("minecraft:chest_minecart", aspects(Aspect.MACHINA, 3, Aspect.ITER, 2, Aspect.VACUOS, 2));
        exact("minecraft:furnace_minecart", aspects(Aspect.MACHINA, 3, Aspect.ITER, 2, Aspect.IGNIS, 2));
        exact("minecraft:tnt_minecart", aspects(Aspect.MACHINA, 3, Aspect.ITER, 2, Aspect.IGNIS, 3, Aspect.PERDITIO, 3));
        exact("minecraft:hopper_minecart", aspects(Aspect.MACHINA, 3, Aspect.ITER, 2, Aspect.PERMUTATIO, 2));
        exact("minecraft:painting", aspects(Aspect.SENSUS, 5, Aspect.PANNUS, 3));
        exact("minecraft:item_frame", aspects(Aspect.SENSUS, 3, Aspect.PANNUS, 1));
        exact("minecraft:torch", aspects(Aspect.LUX, 2, Aspect.IGNIS, 1));
        exact("minecraft:lantern", aspects(Aspect.LUX, 3, Aspect.METALLUM, 1));
        exact("minecraft:ender_eye", aspects(Aspect.ALIENIS, 4, Aspect.SENSUS, 2, Aspect.ITER, 2));
        exact("thaumcraft:thaumonomicon", aspects(Aspect.COGNITIO, 10, Aspect.PRAECANTATIO, 2, Aspect.ARBOR, 1));
        exact("thaumcraft:research_note", aspects(Aspect.COGNITIO, 4, Aspect.PRAECANTATIO, 1));
        exact("thaumcraft:research_point", aspects(Aspect.COGNITIO, 1));
        exact("thaumcraft:scribing_tools", aspects(Aspect.INSTRUMENTUM, 1, Aspect.COGNITIO, 1));
        exact("thaumcraft:infused_scribing_tools", aspects(Aspect.INSTRUMENTUM, 2, Aspect.COGNITIO, 2, Aspect.PRAECANTATIO, 2));
        exact("thaumcraft:thaumometer", aspects(Aspect.SENSUS, 4, Aspect.VITREUS, 2, Aspect.PRAECANTATIO, 2, Aspect.METALLUM, 2));
        exact("thaumcraft:goggles_of_revealing", aspects(Aspect.SENSUS, 4, Aspect.VITREUS, 2, Aspect.PRAECANTATIO, 2, Aspect.METALLUM, 2));
        exact("thaumcraft:helmet_of_revealing", aspects(Aspect.SENSUS, 4, Aspect.TUTAMEN, 3, Aspect.METALLUM, 3, Aspect.PRAECANTATIO, 2));
        exact("thaumcraft:arcane_workbench", aspects(Aspect.FABRICO, 4, Aspect.PRAECANTATIO, 4, Aspect.ARBOR, 2));
        exact("thaumcraft:research_table", aspects(Aspect.COGNITIO, 4, Aspect.INSTRUMENTUM, 1, Aspect.ARBOR, 2));
        exact("thaumcraft:crucible", aspects(Aspect.AQUA, 8, Aspect.FABRICO, 4, Aspect.PRAECANTATIO, 4, Aspect.METALLUM, 4));
        exact("thaumcraft:alchemical_furnace", aspects(Aspect.PRAECANTATIO, 8, Aspect.AQUA, 8, Aspect.FABRICO, 8));
        exact("thaumcraft:arcane_pedestal", aspects(Aspect.ORDO, 4, Aspect.PRAECANTATIO, 2, Aspect.VITREUS, 2));
        exact("thaumcraft:infusion_matrix", aspects(Aspect.PRAECANTATIO, 16, Aspect.ORDO, 8, Aspect.ALIENIS, 4, Aspect.MACHINA, 4));
        exact("thaumcraft:essentia_jar", aspects(Aspect.VACUOS, 4, Aspect.VITREUS, 4, Aspect.PRAECANTATIO, 2));
        exact("thaumcraft:filtered_essentia_jar", aspects(Aspect.VACUOS, 4, Aspect.VITREUS, 4, Aspect.PRAECANTATIO, 3, Aspect.ORDO, 1));
        exact("thaumcraft:void_essentia_jar", aspects(Aspect.VACUOS, 8, Aspect.VITREUS, 4, Aspect.PRAECANTATIO, 4, Aspect.ALIENIS, 2));
        exact("thaumcraft:essentia_tube", aspects(Aspect.VACUOS, 2, Aspect.VITREUS, 2, Aspect.PRAECANTATIO, 1));
        exact("thaumcraft:essentia_valve", aspects(Aspect.VACUOS, 2, Aspect.VITREUS, 2, Aspect.PRAECANTATIO, 1, Aspect.MACHINA, 1));
        exact("thaumcraft:arcane_stone", aspects(Aspect.TERRA, 1, Aspect.PRAECANTATIO, 1));
        exact("thaumcraft:arcane_stone_bricks", aspects(Aspect.TERRA, 1, Aspect.PRAECANTATIO, 1, Aspect.ORDO, 1));
        exact("thaumcraft:obsidian_tile", aspects(Aspect.TERRA, 1, Aspect.ALIENIS, 1));
        exact("thaumcraft:obsidian_totem", aspects(Aspect.TERRA, 1, Aspect.ALIENIS, 1, Aspect.TENEBRAE, 1));
        exact("thaumcraft:eldritch_stone", aspects(Aspect.VACUOS, 8, Aspect.ALIENIS, 8, Aspect.SENSUS, 4));
        exact("thaumcraft:eldritch_portal", aspects(Aspect.VACUOS, 8, Aspect.ALIENIS, 8, Aspect.ITER, 8));
        exact("thaumcraft:eldritch_altar", aspects(Aspect.ALIENIS, 8, Aspect.PRAECANTATIO, 8, Aspect.SPIRITUS, 4));
        exact("thaumcraft:eldritch_obelisk", aspects(Aspect.VACUOS, 4, Aspect.ALIENIS, 4, Aspect.TERRA, 4));
        exact("thaumcraft:amber_ore", aspects(Aspect.TERRA, 1, Aspect.VINCULUM, 3, Aspect.VITREUS, 2));
        exact("thaumcraft:cinnabar_ore", aspects(Aspect.TERRA, 1, Aspect.METALLUM, 2, Aspect.PERMUTATIO, 2, Aspect.VENENUM, 1));
        exact("thaumcraft:tainted_soil", aspects(Aspect.TERRA, 1, Aspect.VITIUM, 3));
        exact("thaumcraft:taint_seed", aspects(Aspect.VICTUS, 1, Aspect.VITIUM, 3));
        exact("thaumcraft:greatwood_planks", aspects(Aspect.ARBOR, 1, Aspect.PRAECANTATIO, 1));
        exact("thaumcraft:silverwood_planks", aspects(Aspect.ARBOR, 1, Aspect.PRAECANTATIO, 1, Aspect.ORDO, 1));
        exact("thaumcraft:aer_shard", aspects(Aspect.PRAECANTATIO, 1, Aspect.AER, 2, Aspect.VITREUS, 1));
        exact("thaumcraft:ignis_shard", aspects(Aspect.PRAECANTATIO, 1, Aspect.IGNIS, 2, Aspect.VITREUS, 1));
        exact("thaumcraft:aqua_shard", aspects(Aspect.PRAECANTATIO, 1, Aspect.AQUA, 2, Aspect.VITREUS, 1));
        exact("thaumcraft:terra_shard", aspects(Aspect.PRAECANTATIO, 1, Aspect.TERRA, 2, Aspect.VITREUS, 1));
        exact("thaumcraft:ordo_shard", aspects(Aspect.PRAECANTATIO, 1, Aspect.ORDO, 2, Aspect.VITREUS, 1));
        exact("thaumcraft:perditio_shard", aspects(Aspect.PRAECANTATIO, 1, Aspect.PERDITIO, 2, Aspect.VITREUS, 1));
        exact("thaumcraft:balanced_shard", aspects(Aspect.AER, 1, Aspect.IGNIS, 1, Aspect.AQUA, 1, Aspect.TERRA, 1, Aspect.ORDO, 1, Aspect.PERDITIO, 1, Aspect.PRAECANTATIO, 2));
        exact("thaumcraft:vacuos_shard", aspects(Aspect.VACUOS, 2, Aspect.VITREUS, 1, Aspect.PRAECANTATIO, 1));
        exact("thaumcraft:vitreus_shard", aspects(Aspect.VITREUS, 3, Aspect.PRAECANTATIO, 1));
        exact("thaumcraft:metallum_shard", aspects(Aspect.METALLUM, 2, Aspect.VITREUS, 1, Aspect.PRAECANTATIO, 1));
        exact("thaumcraft:praecantatio_shard", aspects(Aspect.PRAECANTATIO, 3, Aspect.VITREUS, 1));
        exact("thaumcraft:herba_shard", aspects(Aspect.HERBA, 2, Aspect.VITREUS, 1, Aspect.PRAECANTATIO, 1));
        exact("thaumcraft:lux_shard", aspects(Aspect.LUX, 2, Aspect.VITREUS, 1, Aspect.PRAECANTATIO, 1));
        exact("thaumcraft:potentia_shard", aspects(Aspect.POTENTIA, 2, Aspect.VITREUS, 1, Aspect.PRAECANTATIO, 1));
        exact("thaumcraft:amber", aspects(Aspect.VINCULUM, 3, Aspect.VITREUS, 2));
        exact("thaumcraft:quicksilver_drop", aspects(Aspect.METALLUM, 3, Aspect.VENENUM, 1, Aspect.PERMUTATIO, 2));
        exact("thaumcraft:thaumium_ingot", aspects(Aspect.ORDO, 1, Aspect.METALLUM, 6, Aspect.TERRA, 1));
        exact("thaumcraft:thaumium_nugget", aspects(Aspect.METALLUM, 1));
        exact("thaumcraft:thaumium_plate", aspects(Aspect.ORDO, 1, Aspect.METALLUM, 6, Aspect.TERRA, 1));
        exact("thaumcraft:void_metal_ingot", aspects(Aspect.ORDO, 1, Aspect.METALLUM, 4, Aspect.TERRA, 1, Aspect.ALIENIS, 2));
        exact("thaumcraft:void_metal_nugget", aspects(Aspect.METALLUM, 1, Aspect.ALIENIS, 1));
        exact("thaumcraft:primordial_pearl", aspects(Aspect.AER, 16, Aspect.TERRA, 16, Aspect.IGNIS, 16, Aspect.AQUA, 16, Aspect.ORDO, 16, Aspect.PERDITIO, 16));
        exact("thaumcraft:nitor", aspects(Aspect.LUX, 2, Aspect.IGNIS, 1, Aspect.PRAECANTATIO, 1));
        exact("thaumcraft:alchemy_dust", aspects(Aspect.PRAECANTATIO, 2, Aspect.PERMUTATIO, 1));
        exact("thaumcraft:flux_crystal", aspects(Aspect.VITIUM, 3, Aspect.LIMUS, 1));
        exact("thaumcraft:tainted_slime", aspects(Aspect.VITIUM, 3, Aspect.LIMUS, 1));
        exact("thaumcraft:iron_wand_cap", aspects(Aspect.METALLUM, 3, Aspect.INSTRUMENTUM, 1));
        exact("thaumcraft:gold_wand_cap", aspects(Aspect.METALLUM, 3, Aspect.LUCRUM, 2, Aspect.INSTRUMENTUM, 1));
        exact("thaumcraft:thaumium_wand_cap", aspects(Aspect.METALLUM, 6, Aspect.ORDO, 1, Aspect.PRAECANTATIO, 2, Aspect.INSTRUMENTUM, 1));
        exact("thaumcraft:wooden_wand_core", aspects(Aspect.ARBOR, 2, Aspect.PRAECANTATIO, 1));
        exact("thaumcraft:greatwood_wand_core", aspects(Aspect.ARBOR, 3, Aspect.PRAECANTATIO, 2));
        exact("thaumcraft:silverwood_wand_core", aspects(Aspect.ARBOR, 3, Aspect.PRAECANTATIO, 2, Aspect.ORDO, 1));
        exact("thaumcraft:iron_capped_wooden_wand", aspects(Aspect.ARBOR, 2, Aspect.METALLUM, 3, Aspect.PRAECANTATIO, 2, Aspect.INSTRUMENTUM, 2));
        exact("thaumcraft:greatwood_wand", aspects(Aspect.ARBOR, 3, Aspect.METALLUM, 3, Aspect.PRAECANTATIO, 4, Aspect.INSTRUMENTUM, 2));
        exact("thaumcraft:silverwood_wand", aspects(Aspect.ARBOR, 3, Aspect.METALLUM, 6, Aspect.ORDO, 2, Aspect.PRAECANTATIO, 6, Aspect.INSTRUMENTUM, 2));
        exact("thaumcraft:focus_fire", aspects(Aspect.PRAECANTATIO, 5, Aspect.IGNIS, 5, Aspect.TELUM, 2));
        exact("thaumcraft:focus_frost", aspects(Aspect.PRAECANTATIO, 5, Aspect.GELUM, 5, Aspect.TELUM, 2));
        exact("thaumcraft:focus_shock", aspects(Aspect.PRAECANTATIO, 5, Aspect.POTENTIA, 5, Aspect.TELUM, 2));
        exact("thaumcraft:focus_exchange", aspects(Aspect.PRAECANTATIO, 5, Aspect.PERMUTATIO, 5, Aspect.INSTRUMENTUM, 2));
        exact("thaumcraft:focus_heal", aspects(Aspect.PRAECANTATIO, 5, Aspect.SANO, 5));
        exact("thaumcraft:focus_pech_summon", aspects(Aspect.PRAECANTATIO, 5, Aspect.VENENUM, 5, Aspect.PERDITIO, 5, Aspect.ALIENIS, 5, Aspect.TELUM, 5));
        exact("thaumcraft:golem_bell", aspects(Aspect.SENSUS, 2, Aspect.MACHINA, 2, Aspect.METALLUM, 2));
        exact("thaumcraft:golem_core", aspects(Aspect.MACHINA, 4, Aspect.COGNITIO, 2, Aspect.PRAECANTATIO, 2));
        exact("thaumcraft:eldritch_eye", aspects(Aspect.ALIENIS, 5, Aspect.AURAM, 3, Aspect.PRAECANTATIO, 3, Aspect.SENSUS, 3, Aspect.SPIRITUS, 3));
        exact("thaumcraft:crimson_key", aspects(Aspect.COGNITIO, 5, Aspect.PRAECANTATIO, 3, Aspect.ALIENIS, 3, Aspect.SPIRITUS, 3));
        exact("thaumcraft:awakened_crimson_key", aspects(Aspect.COGNITIO, 5, Aspect.PRAECANTATIO, 3, Aspect.ALIENIS, 5, Aspect.SPIRITUS, 5));
        exact("thaumcraft:bottomless_pouch", aspects(Aspect.VACUOS, 8, Aspect.PANNUS, 4, Aspect.PRAECANTATIO, 4));
        exact("thaumcraft:sanity_soap", aspects(Aspect.SANO, 2, Aspect.ORDO, 2, Aspect.PRAECANTATIO, 1));
        exact("thaumcraft:warp_charm", aspects(Aspect.ORDO, 4, Aspect.SANO, 2, Aspect.PRAECANTATIO, 4));
        exact("thaumcraft:warp_ward_talisman", aspects(Aspect.ORDO, 6, Aspect.SANO, 4, Aspect.PRAECANTATIO, 6));
    }

    private TC4ObjectAspectRegistry() {
    }

    public static AspectList getAspectsForItem(ItemStack stack) {
        if (stack == null || stack.isEmpty()) {
            return new AspectList();
        }
        ResourceLocation id = ForgeRegistries.ITEMS.getKey(stack.getItem());
        return byModernId(id);
    }

    public static AspectList getAspectsForBlock(BlockState state) {
        if (state == null) {
            return new AspectList();
        }
        ResourceLocation id = ForgeRegistries.BLOCKS.getKey(state.getBlock());
        return byModernId(id);
    }

    public static AspectList byModernId(ResourceLocation id) {
        if (id == null) {
            return new AspectList();
        }
        String key = id.toString().toLowerCase(Locale.ROOT);
        AspectList exact = EXACT.get(key);
        if (exact != null) {
            return copyOf(exact);
        }
        AspectList legacy = legacyAliasFor(key);
        return legacy == null ? new AspectList() : copyOf(legacy);
    }

    public static int exactRuntimeEntries() {
        return EXACT.size();
    }

    public static int legacyOreDictionaryEntries() {
        return LEGACY_OREDICT.size();
    }

    private static AspectList legacyAliasFor(String key) {
        if (key.equals("minecraft:stick")) return LEGACY_OREDICT.get("stickWood");
        if (key.endsWith("_log") || key.contains(":stripped_") && key.endsWith("_log") || key.endsWith("_stem") || key.endsWith("_hyphae")) return LEGACY_OREDICT.get("logWood");
        if (key.endsWith("_planks")) return LEGACY_OREDICT.get("plankWood");
        if (key.endsWith("_slab")) return LEGACY_OREDICT.get("slabWood");
        if (key.endsWith("_stairs")) return LEGACY_OREDICT.get("stairWood");
        if (key.endsWith("_sapling") || key.endsWith("_propagule") || key.endsWith("_fungus")) return LEGACY_OREDICT.get("treeSapling");
        if (key.endsWith("_leaves") || key.endsWith("_wart_block")) return LEGACY_OREDICT.get("treeLeaves");
        return null;
    }

    private static void exact(String id, AspectList aspects) {
        EXACT.put(id, aspects);
    }

    private static void legacy(String id, AspectList aspects) {
        LEGACY_OREDICT.put(id, aspects);
    }

    private static AspectList aspects(Object... data) {
        AspectList list = new AspectList();
        for (int i = 0; i + 1 < data.length; i += 2) {
            list.add((Aspect) data[i], (Integer) data[i + 1]);
        }
        return list;
    }

    private static AspectList copyOf(AspectList source) {
        AspectList copy = new AspectList();
        copy.addAll(source);
        return copy;
    }
}
