package com.darkifov.thaumcraft.alchemy;

/**
 * Dependency-free contract extracted from TC4 4.2.3.5
 * TileThaumatorium, ContainerThaumatorium and WandManager#createThaumatorium.
 */
public final class TC4ThaumatoriumParity {
    public static final String CONTRACT_VERSION = "11.64.39";
    public static final String RESEARCH_KEY = "THAUMATORIUM";
    public static final int CRAFT_INTERVAL_TICKS = 5;
    public static final int HEAT_REFRESH_TICKS = 40;
    public static final int SUCTION = 128;
    public static final int BASE_RECIPE_MEMORY = 1;
    public static final int RECIPES_PER_MNEMONIC_MATRIX = 2;
    public static final int FIRE_VIS_CENTIVIS = 1500;
    public static final int ORDER_VIS_CENTIVIS = 3000;
    public static final int WATER_VIS_CENTIVIS = 3000;
    public static final int CLIENT_VENT_TICKS = 7;

    private TC4ThaumatoriumParity() {
    }

    public static int recipeCapacity(int orientedMatrices) {
        return BASE_RECIPE_MEMORY + Math.max(0, orientedMatrices) * RECIPES_PER_MNEMONIC_MATRIX;
    }

    public static int trimmedSize(int rememberedRecipes, int capacity) {
        return Math.min(Math.max(0, rememberedRecipes), Math.max(BASE_RECIPE_MEMORY, capacity));
    }

    public static boolean canRun(boolean heated, boolean powered, boolean hasCatalyst,
                                 int rememberedRecipes, int counter) {
        return heated && !powered && hasCatalyst && rememberedRecipes > 0
                && counter % CRAFT_INTERVAL_TICKS == 0;
    }
}
