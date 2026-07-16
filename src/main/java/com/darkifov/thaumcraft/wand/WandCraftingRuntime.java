package com.darkifov.thaumcraft.wand;

import com.darkifov.thaumcraft.Aspect;
import com.darkifov.thaumcraft.ThaumcraftMod;
import com.darkifov.thaumcraft.arcane.ArcaneWorkbenchRecipe;
import com.darkifov.thaumcraft.block.WandItem;
import com.darkifov.thaumcraft.blockentity.ArcaneWorkbenchBlockEntity;
import com.darkifov.thaumcraft.data.PlayerThaumData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.ArrayList;
import java.util.List;

/**
 * Stage187 strict adapter for original TC4 ArcaneWandRecipe and ArcaneSceptreRecipe.
 *
 * Original source of truth:
 * - thaumcraft.common.lib.crafting.ArcaneWandRecipe
 *   cap slots (0,2) + (2,0), rod slot (1,1), empty all other slots,
 *   cost = cap craftCost * rod craftCost, all primal aspects.
 * - thaumcraft.common.lib.crafting.ArcaneSceptreRecipe
 *   cap slots (1,0) + (2,1) + (0,2), rod slot (1,1), charm slot (2,0),
 *   empty (0,0),(0,1),(1,2),(2,2), cost = cap craftCost * rod craftCost * 1.5,
 *   and root byte NBT "sceptre".
 */
public final class WandCraftingRuntime {
    public static final String TC4_KIND_WAND_ASSEMBLY = "TC4_ARCANE_WAND_RECIPE";
    public static final String TC4_KIND_SCEPTRE_ASSEMBLY = "TC4_ARCANE_SCEPTRE_RECIPE";
    private static final ResourceLocation RESULT_WAND_ITEM = new ResourceLocation(ThaumcraftMod.MOD_ID, "iron_capped_wooden_wand");
    private static final ResourceLocation CHARM_ITEM = new ResourceLocation(ThaumcraftMod.MOD_ID, "tc4_charm");

    private WandCraftingRuntime() {}

    public static List<ArcaneWorkbenchRecipe> generatedWandAssemblyRecipes() {
        List<ArcaneWorkbenchRecipe> recipes = new ArrayList<>();
        for (WandRodType rod : WandRodType.values()) {
            if (rod == WandRodType.CREATIVE) continue;
            ResourceLocation rodItem = rodItemId(rod);
            for (WandCapType cap : WandCapType.values()) {
                if (cap == WandCapType.INFINITY) continue;
                ResourceLocation capItem = capItemId(cap);
                if (capItem == null || rodItem == null) continue;
                if (rod == WandRodType.WOOD && cap == WandCapType.IRON) continue;

                ArcaneWorkbenchRecipe wand = ArcaneWorkbenchRecipe.tc4Adapter(
                        new ResourceLocation(ThaumcraftMod.MOD_ID, "tc4_arcane_wand_" + cap.id() + "_" + rod.id()),
                        rodItem,
                        RESULT_WAND_ITEM,
                        1,
                        wandResearch(rod, cap),
                        "ArcaneWandRecipe:" + cap.originalTag() + ":" + rod.originalTag(),
                        TC4_KIND_WAND_ASSEMBLY
                );
                wand.patternRow("  C").patternRow(" R ").patternRow("C  ")
                        .patternKey('C', capItem).patternKey('R', rodItem);
                addAllPrimalCost(wand, cap.craftCost() * rod.craftCost());
                recipes.add(wand);

                ArcaneWorkbenchRecipe sceptre = ArcaneWorkbenchRecipe.tc4Adapter(
                        new ResourceLocation(ThaumcraftMod.MOD_ID, "tc4_arcane_sceptre_" + cap.id() + "_" + rod.id()),
                        rodItem,
                        RESULT_WAND_ITEM,
                        1,
                        "SCEPTRE",
                        "ArcaneSceptreRecipe:" + cap.originalTag() + ":" + rod.originalTag(),
                        TC4_KIND_SCEPTRE_ASSEMBLY
                );
                // Original ArcaneSceptreRecipe coordinates (x,y):
                // cap (1,0), cap (2,1), cap (0,2), rod (1,1), charm (2,0).
                sceptre.patternRow(" CH").patternRow(" RC").patternRow("C  ")
                        .patternKey('C', capItem).patternKey('R', rodItem).patternKey('H', CHARM_ITEM);
                addAllPrimalCost(sceptre, (int)(cap.craftCost() * rod.craftCost() * 1.5F));
                recipes.add(sceptre);
            }
        }
        return recipes;
    }

    public static boolean isGeneratedAssembly(ArcaneWorkbenchRecipe recipe) {
        return recipe != null && (TC4_KIND_WAND_ASSEMBLY.equals(recipe.tc4Kind()) || TC4_KIND_SCEPTRE_ASSEMBLY.equals(recipe.tc4Kind()));
    }

    public static boolean matchesGeneratedAssembly(ArcaneWorkbenchRecipe recipe, ArcaneWorkbenchBlockEntity table, Player player) {
        if (!isGeneratedAssembly(recipe)) {
            return false;
        }
        Assembly assembly = readAssembly(recipe);
        if (assembly == null) {
            return false;
        }
        if (!hasResearch(player, assembly.rod().researchKey()) || !hasResearch(player, assembly.cap().researchKey())) {
            return false;
        }
        if (TC4_KIND_SCEPTRE_ASSEMBLY.equals(recipe.tc4Kind()) && !hasResearch(player, "SCEPTRE")) {
            return false;
        }
        if (TC4_KIND_WAND_ASSEMBLY.equals(recipe.tc4Kind())) {
            return matchesWandPattern(table, assembly);
        }
        return matchesSceptrePattern(table, assembly);
    }

    /**
     * Produces the authoritative NBT result for runtime crafting and JEI.
     * ArcaneWorkbenchRecipe#result is necessarily the shared registry item and
     * has no rod/cap tags, so exposing it directly makes every recipe look like
     * and report the 25-vis wood/iron fallback.
     */
    public static ItemStack resultFor(ArcaneWorkbenchRecipe recipe) {
        if (!isGeneratedAssembly(recipe)) {
            return ItemStack.EMPTY;
        }
        Assembly assembly = readAssembly(recipe);
        if (assembly == null) {
            return ItemStack.EMPTY;
        }
        return WandVariantRuntime.create(
                assembly.rod(),
                assembly.cap(),
                TC4_KIND_SCEPTRE_ASSEMBLY.equals(recipe.tc4Kind()),
                false
        );
    }

    public static ItemStack resultFor(ArcaneWorkbenchRecipe recipe, ArcaneWorkbenchBlockEntity table) {
        return resultFor(recipe);
    }

    public static boolean consumeAssemblyItems(ArcaneWorkbenchRecipe recipe, ArcaneWorkbenchBlockEntity table) {
        if (!isGeneratedAssembly(recipe)) {
            return false;
        }
        if (TC4_KIND_WAND_ASSEMBLY.equals(recipe.tc4Kind())) {
            table.getItem(ArcaneWorkbenchBlockEntity.slotForGrid(0, 2)).shrink(1);
            table.getItem(ArcaneWorkbenchBlockEntity.slotForGrid(1, 1)).shrink(1);
            table.getItem(ArcaneWorkbenchBlockEntity.slotForGrid(2, 0)).shrink(1);
            return true;
        }
        // ArcaneSceptreRecipe exact original slot consumption.
        table.getItem(ArcaneWorkbenchBlockEntity.slotForGrid(0, 1)).shrink(1); // cap1 (x=1,y=0)
        table.getItem(ArcaneWorkbenchBlockEntity.slotForGrid(0, 2)).shrink(1); // charm (x=2,y=0)
        table.getItem(ArcaneWorkbenchBlockEntity.slotForGrid(1, 1)).shrink(1); // rod (x=1,y=1)
        table.getItem(ArcaneWorkbenchBlockEntity.slotForGrid(1, 2)).shrink(1); // cap2 (x=2,y=1)
        table.getItem(ArcaneWorkbenchBlockEntity.slotForGrid(2, 0)).shrink(1); // cap3 (x=0,y=2)
        return true;
    }

    private static boolean matchesWandPattern(ArcaneWorkbenchBlockEntity table, Assembly assembly) {
        return isEmpty(table, 0, 0) && isEmpty(table, 0, 1)
                && isCap(table, 0, 2, assembly.cap())
                && isEmpty(table, 1, 0) && isRod(table, 1, 1, assembly.rod()) && isEmpty(table, 1, 2)
                && isCap(table, 2, 0, assembly.cap())
                && isEmpty(table, 2, 1) && isEmpty(table, 2, 2);
    }

    private static boolean matchesSceptrePattern(ArcaneWorkbenchBlockEntity table, Assembly assembly) {
        return isEmpty(table, 0, 0) && isCap(table, 0, 1, assembly.cap()) && hasItem(table, 0, 2, CHARM_ITEM)
                && isEmpty(table, 1, 0) && isRod(table, 1, 1, assembly.rod()) && isCap(table, 1, 2, assembly.cap())
                && isCap(table, 2, 0, assembly.cap()) && isEmpty(table, 2, 1) && isEmpty(table, 2, 2);
    }

    private static void addAllPrimalCost(ArcaneWorkbenchRecipe recipe, int cost) {
        int safe = Math.max(1, cost);
        recipe.require(Aspect.AER, safe).require(Aspect.TERRA, safe).require(Aspect.IGNIS, safe)
                .require(Aspect.AQUA, safe).require(Aspect.ORDO, safe).require(Aspect.PERDITIO, safe);
    }

    private static boolean hasResearch(Player player, String key) {
        return key == null || key.isBlank() || PlayerThaumData.hasResearch(player, key);
    }

    private static String wandResearch(WandRodType rod, WandCapType cap) {
        if (!rod.researchKey().isBlank()) return rod.researchKey();
        return cap.researchKey();
    }

    private static Assembly readAssembly(ArcaneWorkbenchRecipe recipe) {
        String[] split = recipe.tc4Key().split(":");
        if (split.length < 3) return null;
        WandCapType cap = WandCapType.fromOriginalTag(split[1]);
        WandRodType rod = WandRodType.fromOriginalTag(split[2]);
        return cap == null || rod == null ? null : new Assembly(rod, cap);
    }

    private static boolean isEmpty(ArcaneWorkbenchBlockEntity table, int row, int col) {
        return table.getItem(ArcaneWorkbenchBlockEntity.slotForGrid(row, col)).isEmpty();
    }

    private static boolean isCap(ArcaneWorkbenchBlockEntity table, int row, int col, WandCapType cap) {
        return hasItem(table, row, col, capItemId(cap));
    }

    private static boolean isRod(ArcaneWorkbenchBlockEntity table, int row, int col, WandRodType rod) {
        return hasItem(table, row, col, rodItemId(rod));
    }

    private static boolean hasItem(ArcaneWorkbenchBlockEntity table, int row, int col, ResourceLocation id) {
        if (id == null) return false;
        ResourceLocation actual = ForgeRegistries.ITEMS.getKey(table.getItem(ArcaneWorkbenchBlockEntity.slotForGrid(row, col)).getItem());
        return id.equals(actual);
    }

    public static ResourceLocation capItemId(WandCapType cap) {
        String id = switch (cap) {
            case IRON -> "tc4_wand_cap_iron";
            case GOLD -> "tc4_wand_cap_gold";
            case THAUMIUM -> "tc4_wand_cap_thaumium";
            case COPPER -> "tc4_wand_cap_copper";
            case SILVER -> "tc4_wand_cap_silver";
            case VOID -> "tc4_wand_cap_void";
            default -> null;
        };
        return id == null ? null : new ResourceLocation(ThaumcraftMod.MOD_ID, id);
    }

    public static ResourceLocation rodItemId(WandRodType rod) {
        String id = switch (rod) {
            // Original WandRod("wood") uses the vanilla stick directly.
            case WOOD -> "stick";
            case GREATWOOD -> "tc4_wand_rod_greatwood";
            case OBSIDIAN -> "tc4_wand_rod_obsidian";
            case BLAZE -> "tc4_wand_rod_blaze";
            case ICE -> "tc4_wand_rod_ice";
            case QUARTZ -> "tc4_wand_rod_quartz";
            case BONE -> "tc4_wand_rod_bone";
            case REED -> "tc4_wand_rod_reed";
            case SILVERWOOD -> "tc4_wand_rod_silverwood";
            case GREATWOOD_STAFF -> "tc4_staff_rod_greatwood";
            case OBSIDIAN_STAFF -> "tc4_staff_rod_obsidian";
            case BLAZE_STAFF -> "tc4_staff_rod_blaze";
            case ICE_STAFF -> "tc4_staff_rod_ice";
            case QUARTZ_STAFF -> "tc4_staff_rod_quartz";
            case BONE_STAFF -> "tc4_staff_rod_bone";
            case REED_STAFF -> "tc4_staff_rod_reed";
            case SILVERWOOD_STAFF -> "tc4_staff_rod_silverwood";
            case PRIMAL_STAFF -> "tc4_staff_rod_primal";
            default -> null;
        };
        if (id == null) return null;
        return rod == WandRodType.WOOD
                ? new ResourceLocation("minecraft", id)
                : new ResourceLocation(ThaumcraftMod.MOD_ID, id);
    }

    private record Assembly(WandRodType rod, WandCapType cap) {}
}
