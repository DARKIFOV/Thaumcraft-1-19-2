package com.darkifov.thaumcraft.arcane;

/**
 * v11.64.31 exact source contract for TC4 Arcane Workbench inventory and vis units.
 * Empty AspectList recipes are free; there is no generic Ordo tax on the table.
 */
public final class TC4ArcaneWorkbenchVisCostParity {
    public static final String CONTRACT_VERSION = "11.64.31";
    public static final int INVENTORY_SIZE = 11;
    public static final int OUTPUT_SLOT = 9;
    public static final int WAND_SLOT = 10;
    public static final int AUTOMATION_SLOT = 10;
    public static final int EMPTY_ASPECT_LIST_COST = 0;
    public static final int CENTIVIS_MULTIPLIER = 100;

    private TC4ArcaneWorkbenchVisCostParity() {}

    public static boolean inventoryLayoutMatchesOriginal() {
        return INVENTORY_SIZE == 11 && OUTPUT_SLOT == 9 && WAND_SLOT == 10 && AUTOMATION_SLOT == 10;
    }

    public static boolean zeroAspectListIsFree() {
        return EMPTY_ASPECT_LIST_COST == 0;
    }

    public static boolean centivisScalingMatchesOriginal() {
        return CENTIVIS_MULTIPLIER == 100;
    }
}
