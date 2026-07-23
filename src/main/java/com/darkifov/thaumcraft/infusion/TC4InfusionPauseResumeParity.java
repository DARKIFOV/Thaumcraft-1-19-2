package com.darkifov.thaumcraft.infusion;

/**
 * Deterministic source contract for TC4 Infusion Matrix structure loss.
 *
 * <p>The original matrix periodically checks only the center pedestal and four
 * diagonal pillars.  If that structure is missing it clears {@code active} but
 * keeps the locked recipe, pending essentia/components and crafting flag.  A
 * later wand activation resumes the same craft.  Once a recipe has started,
 * component pedestal contents are not used to select a different recipe;
 * missing components simply leave the matrix waiting.</p>
 */
public final class TC4InfusionPauseResumeParity {
    private TC4InfusionPauseResumeParity() {
    }

    public static boolean structureLossClearsLockedRecipe() {
        return false;
    }

    public static boolean structureLossGrantsTerminalWarp() {
        return false;
    }

    public static boolean reactivationResumesLockedCraft() {
        return true;
    }

    public static boolean componentLayoutReselectsRecipeAfterStart() {
        return false;
    }
}
