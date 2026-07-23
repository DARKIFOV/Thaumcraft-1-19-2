package com.darkifov.thaumcraft.infusion;

/**
 * Dependency-free source contract for the original TC4 infusion altar shell,
 * pedestal and matrix scheduler.
 *
 * <p>Production code consumes these constants directly. The class therefore
 * remains independently testable with {@code javac --release 17} without
 * becoming a test-only mirror.</p>
 */
public final class TC4InfusionAltarFullClosureParity {
    public static final String CONTRACT_VERSION = "11.64.34";
    public static final String RESEARCH_KEY = "INFUSION";

    /** Six primal aspects, 25 vis each; the 1.19 port stores 100 centivis per vis. */
    public static final int PRIMAL_ASPECT_COUNT = 6;
    public static final int ALTAR_VIS_COST_PER_PRIMAL = 25;
    public static final int ALTAR_VIS_COST_PER_PRIMAL_CENTIVIS = 2500;

    /** WandManager searches every possible 3x3x3 origin from clicked-2 through clicked. */
    public static final int ALTAR_ORIGIN_SCAN_MIN = -2;
    public static final int ALTAR_ORIGIN_SCAN_MAX = 0;
    public static final int ALTAR_BLUEPRINT_SIZE = 3;
    public static final int ALTAR_MATRIX_LAYER_FROM_ORIGIN = 2;
    public static final int ALTAR_CENTER_INDEX = 1;
    public static final int ALTAR_BLUEPRINT_MATRIX_BLOCKS = 1;
    public static final int ALTAR_BLUEPRINT_CENTER_PEDESTALS = 1;
    public static final int ALTAR_BLUEPRINT_ARCANE_STONE_BLOCKS = 4;
    public static final int ALTAR_BLUEPRINT_ARCANE_STONE_BRICKS = 4;
    public static final int ALTAR_BLUEPRINT_AIR_BLOCKS = 17;
    public static final int[] ORIGINAL_PILLAR_ORIENTATIONS = {2, 3, 4, 5};

    public static final int MATRIX_LIGHT_LEVEL = 10;
    public static final float CRAFTING_BREAK_EXPLOSION_STRENGTH = 2.0F;

    public static final int MATRIX_VALIDITY_IDLE_INTERVAL = 100;
    public static final int MATRIX_VALIDITY_CRAFTING_INTERVAL = 20;
    public static final int CRAFT_CYCLE_INTERVAL = 10;
    public static final int ENCHANTMENT_XP_CYCLE_INTERVAL = 20;
    /** Original itemCount value: five later craftCycle passes, not five world ticks. */
    public static final int COMPONENT_TRAVEL_CYCLES = 5;

    public static final int PEDESTAL_HORIZONTAL_RADIUS = 8;
    public static final int PEDESTAL_TOP_OFFSET_FROM_MATRIX = -1;
    public static final int PEDESTAL_BOTTOM_OFFSET_FROM_MATRIX = -10;
    public static final int STABILIZER_HORIZONTAL_RADIUS = 12;
    public static final int STABILIZER_TOP_OFFSET_FROM_MATRIX = 5;
    public static final int STABILIZER_BOTTOM_OFFSET_FROM_MATRIX = -10;

    public static final int PEDESTAL_COMPARATOR_EMPTY = 0;
    public static final int PEDESTAL_COMPARATOR_OCCUPIED = 15;

    public static final double PEDESTAL_ITEM_Y = 1.15D;
    public static final float PEDESTAL_BOB_AMPLITUDE = 0.05F;
    public static final float PEDESTAL_BOB_DIVISOR = 16.0F;
    public static final float PEDESTAL_ROTATION_DEGREES_PER_TICK = 1.0F;
    public static final float PEDESTAL_ITEM_SCALE = 1.0F;
    public static final float PEDESTAL_BLOCK_SCALE = 2.0F;

    public static final String PEDESTAL_ITEMS_NBT = "Items";
    public static final String PEDESTAL_SLOT_NBT = "Slot";
    public static final String MATRIX_ASPECTS_NBT = "Aspects";
    public static final String MATRIX_ASPECT_KEY_NBT = "key";
    public static final String MATRIX_ASPECT_AMOUNT_NBT = "amount";
    public static final String MATRIX_RECIPE_INPUTS_NBT = "recipein";
    public static final String MATRIX_RECIPE_INPUT_INDEX_NBT = "item";
    public static final String MATRIX_RECIPE_OUTPUT_NBT = "recipeout";
    public static final String MATRIX_RECIPE_OUTPUT_TYPE_NBT = "rotype";
    public static final String MATRIX_CATALYST_NBT = "recipeinput";
    public static final String MATRIX_RECIPE_INSTABILITY_NBT = "recipeinst";
    public static final String MATRIX_RECIPE_TYPE_NBT = "recipetype";
    public static final String MATRIX_RECIPE_XP_NBT = "recipexp";
    public static final String MATRIX_RECIPE_PLAYER_NBT = "recipeplayer";
    public static final String PEDESTAL_CUSTOM_NAME_NBT = "CustomName";

    public static final String MATRIX_ACTIVE_NBT = "active";
    public static final String MATRIX_CRAFTING_NBT = "crafting";
    public static final String MATRIX_INSTABILITY_NBT = "instability";
    public static final String LOWER_PILLAR_DROP = "thaumcraft:arcane_stone_bricks";
    public static final String UPPER_PILLAR_DROP = "thaumcraft:arcane_stone";

    private TC4InfusionAltarFullClosureParity() {
    }

    public static int pedestalComparator(boolean occupied) {
        return occupied ? PEDESTAL_COMPARATOR_OCCUPIED : PEDESTAL_COMPARATOR_EMPTY;
    }

    public static int altarBlueprintVolume() {
        return ALTAR_BLUEPRINT_SIZE * ALTAR_BLUEPRINT_SIZE * ALTAR_BLUEPRINT_SIZE;
    }

    public static int altarBlueprintAccountedBlocks() {
        return ALTAR_BLUEPRINT_MATRIX_BLOCKS
                + ALTAR_BLUEPRINT_CENTER_PEDESTALS
                + ALTAR_BLUEPRINT_ARCANE_STONE_BLOCKS
                + ALTAR_BLUEPRINT_ARCANE_STONE_BRICKS
                + ALTAR_BLUEPRINT_AIR_BLOCKS;
    }

    public static boolean isBlueprintCorner(int x, int z) {
        return (x == 0 || x == ALTAR_BLUEPRINT_SIZE - 1)
                && (z == 0 || z == ALTAR_BLUEPRINT_SIZE - 1);
    }

    public static boolean isOriginalPillarOrientation(int orientation) {
        return orientation >= 2 && orientation <= 5;
    }

    /**
     * The first pedestal encountered while scanning downward is the only
     * pedestal used for a given x/z column by TC4 getSurroundings.
     */
    public static boolean isPedestalScanOffset(int dx, int dyFromMatrix, int dz) {
        return (dx != 0 || dz != 0)
                && Math.abs(dx) <= PEDESTAL_HORIZONTAL_RADIUS
                && Math.abs(dz) <= PEDESTAL_HORIZONTAL_RADIUS
                && dyFromMatrix <= PEDESTAL_TOP_OFFSET_FROM_MATRIX
                && dyFromMatrix >= PEDESTAL_BOTTOM_OFFSET_FROM_MATRIX;
    }

    /** Ticks are global tile ticks, matching count % countDelay == 0. */
    public static boolean shouldRunCraftCycle(int count, int countDelay) {
        return countDelay > 0 && count % countDelay == 0;
    }
}
