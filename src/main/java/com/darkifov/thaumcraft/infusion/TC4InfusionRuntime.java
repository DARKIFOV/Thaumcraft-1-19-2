package com.darkifov.thaumcraft.infusion;

import com.darkifov.thaumcraft.Aspect;
import net.minecraft.resources.ResourceLocation;

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
    public static final int ESSENTIA_DRAIN_RANGE = 12;
    public static final int ITEM_PULL_DELAY = 5;
    public static final int MIN_INSTABILITY_ROLL = 1;
    public static final int MAX_INSTABILITY = 25;
    public static final int ESSENTIA_FAILURE_BASE_ROLL = 100;
    public static final int COMPONENT_FAILURE_BASE_ROLL = 50;
    public static final int VALIDITY_INSTABILITY_ROLL = 500;

    private TC4InfusionRuntime() {
    }

    public static int estimateDuration(InfusionRecipe recipe, InfusionStructureReport report) {
        int essentia = totalEssentia(recipe.aspectCost());
        int components = recipe.components().size();
        int base = 20 + essentia * ESSENTIA_DRAIN_RANGE + components * (ITEM_PULL_DELAY + 2) + recipe.instability() * 20;
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

    public static int clampInstability(int value) {
        return Math.max(0, Math.min(MAX_INSTABILITY, value));
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
}
