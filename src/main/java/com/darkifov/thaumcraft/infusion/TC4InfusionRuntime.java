package com.darkifov.thaumcraft.infusion;

import com.darkifov.thaumcraft.Aspect;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

/**
 * Runtime constants ported from TC4's TileInfusionMatrix craftCycle.
 *
 * The original 1.7.10 matrix does not consume everything at the end. It first drains
 * one point of essentia at a time, then pulls one pedestal component at a time after
 * a short five tick item travel delay. Stage122 mirrors that behaviour in the 1.19.2
 * block entity instead of using the earlier all-at-once finish consumption.
 */
public final class TC4InfusionRuntime {
    /** TC4 EssentiaHandler.drainEssentia range used by TileInfusionMatrix.craftCycle. */
    public static final int ESSENTIA_DRAIN_RANGE = 12;
    /** TC4 TileInfusionMatrix.countDelay default: craftCycle is evaluated every ten ticks. */
    public static final int CRAFT_CYCLE_DELAY = 10;
    /** TC4 itemCount value: five later craftCycle passes before consumption. */
    public static final int ITEM_PULL_DELAY = 5;
    public static final int MIN_INSTABILITY_ROLL = 1;
    public static final int MAX_INSTABILITY = 25;
    public static final int ESSENTIA_FAILURE_BASE_ROLL = 100;
    public static final int COMPONENT_FAILURE_BASE_ROLL = 50;
    public static final int VALIDITY_INSTABILITY_ROLL = 500;

    private TC4InfusionRuntime() {
    }

    public static int estimateDuration(InfusionRecipe recipe, InfusionStructureReport report) {
        return estimateDuration(recipe, report, recipe.aspectCost(), recipe.components(), recipe.instability());
    }

    public static List<ResourceLocation> orderedComponentPullList(InfusionRecipe recipe, ItemStack catalyst) {
        // Stage343-362 TC4 craftCycle adapter: keep the recipe's original
        // ConfigRecipes component order.  The matrix may animate/source-pull
        // one item at a time, but it must not sort, dedupe, or select by any
        // invented modern priority.
        return new ArrayList<>(recipe.componentsFor(catalyst));
    }

    public static List<InfusionRecipe.ComponentSpec> orderedComponentSpecList(InfusionRecipe recipe, ItemStack catalyst) {
        // v8.42: keep an ItemStack-like component ledger in recipe order so
        // same item ids with different damage/NBT cannot be coalesced by the
        // legacy ResourceLocation-only pending list.
        return new ArrayList<>(recipe.componentSpecsFor(catalyst));
    }

    public static int estimateDuration(InfusionRecipe recipe, InfusionStructureReport report, Map<Aspect, Integer> aspectCost, List<ResourceLocation> components, int recipeInstability) {
        int essentia = totalEssentia(aspectCost);
        int componentCount = components == null ? 0 : components.size();
        int componentTicks = componentCount * (ITEM_PULL_DELAY + 1) * CRAFT_CYCLE_DELAY;
        int base = 20 + essentia * CRAFT_CYCLE_DELAY + componentTicks + recipeInstability * 20;
        return InfusionProcessHelper.acceleratedDuration(Math.max(80, base), report);
    }

    public static int startingInstability(InfusionRecipe recipe, InfusionStructureReport report, int matrixStabilizers) {
        return clampInstability(InfusionProcessHelper.calculatedInstability(recipe, report, matrixStabilizers));
    }

    public static int failedEssentiaRollBound(int recipeInstability) {
        return Math.max(MIN_INSTABILITY_ROLL, ESSENTIA_FAILURE_BASE_ROLL - recipeInstability * 3);
    }

    public static int failedComponentRollBound(int recipeInstability) {
        return Math.max(MIN_INSTABILITY_ROLL, COMPONENT_FAILURE_BASE_ROLL - recipeInstability * 2);
    }

    /**
     * TC4 TileInfusionMatrix.craftCycle rolls the essentia-refund gate once per
     * unmatched ingredient index inside the recipeIngredients loop ("a"), using
     * nextInt(1 + a). The first unmatched ingredient in a cycle (a == 0) always
     * refunds one essentia point back onto the pending bill, and the odds shrink
     * for each later unmatched index reached within that same cycle.
     */
    public static int componentShortageEssentiaRefundRollBound(int ingredientIndex) {
        return Math.max(1, 1 + ingredientIndex);
    }

    /**
     * Original shortage paths only cap values above 25. They never floor a
     * stabilised negative starting value to zero.
     */
    public static int clampInstability(int value) {
        return Math.min(MAX_INSTABILITY, value);
    }

    /** Exact TileInfusionMatrix craftingStart rule: instability = symmetry + recipe instability. */
    public static int initialInstability(int symmetry, int recipeInstability) {
        return symmetry + recipeInstability;
    }

    /**
     * Surrounding rescans update symmetry for future diagnostics but do not
     * rewrite instability already locked by craftingStart.
     */
    public static int runningInstability(int current, int symmetry, int recipeInstability) {
        return current;
    }

    /**
     * TC4 TileInfusionMatrix keeps a concrete recipeInput ItemStack and validates
     * the catalyst against it during every craftCycle.  The comparison is strict
     * for the item id and NBT, while damage 32767 is still treated as wildcard
     * for legacy metadata recipes.
     */
    public static boolean sameCraftingCatalyst(ItemStack current, ItemStack recipeInput) {
        if (current == null || current.isEmpty() || recipeInput == null || recipeInput.isEmpty()) {
            return false;
        }
        if (!current.is(recipeInput.getItem())) {
            return false;
        }
        if (recipeInput.getDamageValue() != TC4InfusionItemMatcher.WILDCARD_DAMAGE
                && current.getDamageValue() != recipeInput.getDamageValue()) {
            return false;
        }
        if (recipeInput.hasTag()) {
            return current.hasTag() && current.getTag().equals(recipeInput.getTag());
        }
        return !current.hasTag();
    }

    public static int totalEssentia(Map<Aspect, Integer> aspects) {
        int total = 0;

        for (int amount : aspects.values()) {
            total += Math.max(0, amount);
        }

        return total;
    }

    public static int totalPendingEssentia(EnumMap<Aspect, Integer> aspects) {
        int total = 0;

        for (int amount : aspects.values()) {
            total += Math.max(0, amount);
        }

        return total;
    }

    public static String serializeComponents(List<ResourceLocation> components) {
        StringBuilder builder = new StringBuilder();

        for (ResourceLocation id : components) {
            if (builder.length() > 0) {
                builder.append('|');
            }

            builder.append(id);
        }

        return builder.toString();
    }

    public static String serializeComponentSpecs(List<InfusionRecipe.ComponentSpec> specs) {
        StringBuilder builder = new StringBuilder();
        for (InfusionRecipe.ComponentSpec spec : specs) {
            if (spec == null || spec.itemId() == null) {
                continue;
            }
            if (builder.length() > 0) {
                builder.append('|');
            }
            builder.append(spec.itemId()).append('@').append(spec.damage());
            if (spec.tag() != null && !spec.tag().isEmpty()) {
                builder.append('@').append(spec.tag().getAsString().replace('|', ' '));
            }
        }
        return builder.toString();
    }
}
