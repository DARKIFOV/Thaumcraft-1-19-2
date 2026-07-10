package com.darkifov.thaumcraft.wand;

import com.darkifov.thaumcraft.Aspect;
import com.darkifov.thaumcraft.ThaumcraftMod;
import com.darkifov.thaumcraft.arcane.ArcaneWorkbenchRecipe;
import net.minecraft.resources.ResourceLocation;

import java.util.ArrayList;
import java.util.List;

/**
 * Stage190 strict ConfigRecipes wand/staff component index.
 *
 * Source of truth: thaumcraft.common.config.ConfigRecipes lines 1058-1215,
 * plus ArcaneWandRecipe/ArcaneSceptreRecipe registered at lines 1051-1055.
 * This class materializes the original arcane component recipes that feed the
 * generated Stage187 ArcaneWandRecipe/ArcaneSceptreRecipe adapter.  Old
 * prebuilt items such as thaumcraft:greatwood_wand and thaumcraft:silverwood_wand
 * remain registered only as Forge 1.19.2 compatibility shortcuts; strict TC4
 * assembly returns one ItemWandCasting-style stack with root rod/cap/sceptre NBT.
 */
public final class TC4ConfigRecipesWandIndex {
    public static final String TC4_KIND_CONFIG_WAND_COMPONENT = "TC4_CONFIGRECIPES_WAND_COMPONENT";
    public static final String PREBUILT_WAND_SHORTCUTS_ARE_DRIFT_ADAPTER = "prebuilt greatwood/silverwood wand shortcuts are ignored by strict original recipes";

    private TC4ConfigRecipesWandIndex() {}

    public static List<ArcaneWorkbenchRecipe> generatedArcaneComponentRecipes() {
        List<ArcaneWorkbenchRecipe> recipes = new ArrayList<>();

        addCapRecipe(recipes, "gold", "WandCapGold", WandCapType.GOLD, item("minecraft", "gold_nugget"), item("thaumcraft", "tc4_wand_cap_gold"));
        addCapRecipe(recipes, "copper", "WandCapCopper", WandCapType.COPPER, item("thaumcraft", "tc4_nuggetcopper"), item("thaumcraft", "tc4_wand_cap_copper"));
        addCapRecipe(recipes, "silver", "WandCapSilverInert", WandCapType.SILVER, item("thaumcraft", "tc4_nuggetsilver"), item("thaumcraft", "tc4_wand_cap_silver_inert"));
        addCapRecipe(recipes, "thaumium", "WandCapThaumiumInert", WandCapType.THAUMIUM, item("thaumcraft", "tc4_nuggetthaumium"), item("thaumcraft", "tc4_wand_cap_thaumium_inert"));
        addVoidCapRecipe(recipes);

        addGreatwoodRodRecipe(recipes);
        addStaffRodRecipe(recipes, "greatwood", "WandRodGreatwoodStaff", WandRodType.GREATWOOD_STAFF, item("thaumcraft", "tc4_wand_rod_greatwood"));
        addStaffRodRecipe(recipes, "obsidian", "WandRodObsidianStaff", WandRodType.OBSIDIAN_STAFF, item("thaumcraft", "tc4_wand_rod_obsidian"));
        addStaffRodRecipe(recipes, "silverwood", "WandRodSilverwoodStaff", WandRodType.SILVERWOOD_STAFF, item("thaumcraft", "tc4_wand_rod_silverwood"));
        addStaffRodRecipe(recipes, "ice", "WandRodIceStaff", WandRodType.ICE_STAFF, item("thaumcraft", "tc4_wand_rod_ice"));
        addStaffRodRecipe(recipes, "quartz", "WandRodQuartzStaff", WandRodType.QUARTZ_STAFF, item("thaumcraft", "tc4_wand_rod_quartz"));
        addStaffRodRecipe(recipes, "reed", "WandRodReedStaff", WandRodType.REED_STAFF, item("thaumcraft", "tc4_wand_rod_reed"));
        addStaffRodRecipe(recipes, "blaze", "WandRodBlazeStaff", WandRodType.BLAZE_STAFF, item("thaumcraft", "tc4_wand_rod_blaze"));
        addStaffRodRecipe(recipes, "bone", "WandRodBoneStaff", WandRodType.BONE_STAFF, item("thaumcraft", "tc4_wand_rod_bone"));

        return recipes;
    }

    private static void addCapRecipe(List<ArcaneWorkbenchRecipe> recipes, String id, String configKey, WandCapType cap, ResourceLocation nugget, ResourceLocation result) {
        ArcaneWorkbenchRecipe recipe = recipe("tc4_configrecipes_cap_" + id, nugget, result, cap.researchKey(), "ConfigRecipes." + configKey)
                .patternRow("NNN").patternRow("N N").patternKey('N', nugget)
                .require(Aspect.ORDO, cap.craftCost())
                .require(Aspect.IGNIS, cap.craftCost())
                .require(Aspect.AER, cap.craftCost());
        recipes.add(recipe);
    }

    private static void addVoidCapRecipe(List<ArcaneWorkbenchRecipe> recipes) {
        ResourceLocation nugget = item("thaumcraft", "tc4_nuggetvoid");
        int cost = WandCapType.VOID.craftCost();
        ArcaneWorkbenchRecipe recipe = recipe("tc4_configrecipes_cap_void", nugget, item("thaumcraft", "tc4_wand_cap_void_inert"), "CAP_void", "ConfigRecipes.WandCapVoidInert")
                .patternRow("NNN").patternRow("N N").patternKey('N', nugget)
                .require(Aspect.PERDITIO, cost * 3)
                .require(Aspect.ORDO, cost * 3)
                .require(Aspect.IGNIS, cost * 2)
                .require(Aspect.AER, cost * 2);
        recipes.add(recipe);
    }

    private static void addGreatwoodRodRecipe(List<ArcaneWorkbenchRecipe> recipes) {
        // Original ConfigRecipes.WandRodGreatwood uses two real magical-log
        // block stacks (meta 0), not the old research-icon compatibility item.
        ResourceLocation log = item("thaumcraft", "greatwood_log");
        ArcaneWorkbenchRecipe recipe = recipe("tc4_configrecipes_rod_greatwood", log, item("thaumcraft", "tc4_wand_rod_greatwood"), "ROD_greatwood", "ConfigRecipes.WandRodGreatwood")
                .patternRow(" G").patternRow("G ").patternKey('G', log)
                .require(Aspect.PERDITIO, WandRodType.GREATWOOD.craftCost());
        recipes.add(recipe);
    }

    private static void addStaffRodRecipe(List<ArcaneWorkbenchRecipe> recipes, String id, String configKey, WandRodType staffRod, ResourceLocation baseRod) {
        ResourceLocation charm = item("thaumcraft", "tc4_charm");
        ResourceLocation result = item("thaumcraft", "tc4_staff_rod_" + id);
        ArcaneWorkbenchRecipe recipe = recipe("tc4_configrecipes_staff_rod_" + id, charm, result, staffRod.researchKey(), "ConfigRecipes." + configKey)
                .patternRow("  S").patternRow(" G ").patternRow("G  ")
                .patternKey('S', charm).patternKey('G', baseRod)
                .require(Aspect.ORDO, staffRod.craftCost());
        recipes.add(recipe);
    }

    private static ArcaneWorkbenchRecipe recipe(String id, ResourceLocation catalyst, ResourceLocation result, String research, String tc4Key) {
        return ArcaneWorkbenchRecipe.tc4Adapter(new ResourceLocation(ThaumcraftMod.MOD_ID, id), catalyst, result, 1, research, tc4Key, TC4_KIND_CONFIG_WAND_COMPONENT);
    }

    private static ResourceLocation item(String namespace, String path) {
        return new ResourceLocation(namespace, path);
    }
}
