package com.darkifov.thaumcraft.wand;

import com.darkifov.thaumcraft.Aspect;

/**
 * v11.63.71 source-linked parity contract for the original TC4 wand component
 * catalogue, cap vis modifiers and WandRodPrimalOnUpdate regeneration rules.
 */
public final class TC4WandParity {
    public static final String CONTRACT_VERSION = "11.64.32";

    /** 18 craftable rod/staff entries plus the creative adapter. */
    public static final int ROD_TYPE_COUNT = 19;
    public static final int CRAFTABLE_ROD_COUNT = 18;
    /** 6 original craftable caps plus the creative infinity adapter. */
    public static final int CAP_TYPE_COUNT = 7;
    public static final int CRAFTABLE_CAP_COUNT = 6;

    private TC4WandParity() {}

    public static boolean rodCatalogueMatchesTc4() {
        return WandRodType.values().length == ROD_TYPE_COUNT
                && WandVariantRuntime.CRAFTABLE_ROD_COUNT == CRAFTABLE_ROD_COUNT
                && WandRodType.WOOD.baseCapacity() == 25 && WandRodType.WOOD.craftCost() == 1
                && WandRodType.GREATWOOD.baseCapacity() == 50 && WandRodType.GREATWOOD.craftCost() == 3
                && WandRodType.OBSIDIAN.baseCapacity() == 75 && WandRodType.OBSIDIAN.craftCost() == 6
                && WandRodType.SILVERWOOD.baseCapacity() == 100 && WandRodType.SILVERWOOD.craftCost() == 9
                && WandRodType.GREATWOOD_STAFF.baseCapacity() == 125 && WandRodType.GREATWOOD_STAFF.craftCost() == 8
                && WandRodType.OBSIDIAN_STAFF.baseCapacity() == 175 && WandRodType.OBSIDIAN_STAFF.craftCost() == 14
                && WandRodType.SILVERWOOD_STAFF.baseCapacity() == 250 && WandRodType.SILVERWOOD_STAFF.craftCost() == 24
                && WandRodType.PRIMAL_STAFF.baseCapacity() == 250 && WandRodType.PRIMAL_STAFF.craftCost() == 32
                && !WandRodType.WOOD.staff() && WandRodType.GREATWOOD_STAFF.staff()
                && WandRodType.PRIMAL_STAFF.staff();
    }

    public static boolean capCatalogueAndModifiersMatchTc4() {
        return WandCapType.values().length == CAP_TYPE_COUNT
                && WandVariantRuntime.CRAFTABLE_CAP_COUNT == CRAFTABLE_CAP_COUNT
                && close(WandCapType.IRON.visCostModifier(), 1.1F)
                && close(WandCapType.GOLD.visCostModifier(), 1.0F)
                && close(WandCapType.THAUMIUM.visCostModifier(), 0.9F)
                && close(WandCapType.VOID.visCostModifier(), 0.8F)
                && close(WandCapType.COPPER.visCostModifier(Aspect.ORDO), 1.0F)
                && close(WandCapType.COPPER.visCostModifier(Aspect.PERDITIO), 1.0F)
                && close(WandCapType.COPPER.visCostModifier(Aspect.AER), 1.1F)
                && close(WandCapType.SILVER.visCostModifier(Aspect.AER), 0.95F)
                && close(WandCapType.SILVER.visCostModifier(Aspect.TERRA), 0.95F)
                && close(WandCapType.SILVER.visCostModifier(Aspect.ORDO), 1.0F);
    }

    public static boolean elementalRegenerationMatchesTc4() {
        return WandRodType.ELEMENTAL_REGEN_INTERVAL_TICKS == 200
                && WandRodType.PRIMAL_STAFF_REGEN_INTERVAL_TICKS == 50
                && WandRodType.REGEN_THRESHOLD_DIVISOR == 10
                && WandRodType.REGEN_AMOUNT_VIS == 1
                && WandRodType.OBSIDIAN.regeneratedAspect() == Aspect.TERRA
                && WandRodType.BLAZE.regeneratedAspect() == Aspect.IGNIS
                && WandRodType.ICE.regeneratedAspect() == Aspect.AQUA
                && WandRodType.QUARTZ.regeneratedAspect() == Aspect.ORDO
                && WandRodType.BONE.regeneratedAspect() == Aspect.PERDITIO
                && WandRodType.REED.regeneratedAspect() == Aspect.AER
                && WandRodType.PRIMAL_STAFF.regeneratesAllPrimals()
                && !WandRodType.SILVERWOOD.hasRodRegen();
    }

    public static boolean focusCatalogueAndUpgradeIdsMatchTc4() {
        if (WandFocusType.values().length != 10 || FocusUpgradeType.values().length != 21) {
            return false;
        }
        for (short id = 0; id < 21; id++) {
            FocusUpgradeType upgrade = FocusUpgradeType.byId(id);
            if (upgrade == null || upgrade.id() != id) {
                return false;
            }
        }
        return WandFocusType.FIRE.id().equals("fire")
                && WandFocusType.PORTABLE_HOLE.id().equals("portable_hole")
                && WandFocusType.EQUAL_TRADE.id().equals("equal_trade")
                && WandFocusType.PECH_CURSE.id().equals("pech_curse")
                && WandFocusType.PRIMAL.id().equals("primal");
    }

    private static boolean close(float actual, float expected) {
        return Math.abs(actual - expected) < 0.0001F;
    }
}
