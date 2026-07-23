package com.darkifov.thaumcraft.wand;

import com.darkifov.thaumcraft.Aspect;

/** Dependency-light v11.64.32 contract for original TC4 rods, caps and base wand container rules. */
public final class TC4WandComponentsFullClosureParity {
    public static final String CONTRACT_VERSION = "11.64.32";
    public static final int ORIGINAL_WAND_MAX_STACK = 1;
    public static final int ORIGINAL_WAND_CREATIVE_VARIANTS = 4;
    public static final int ORIGINAL_ROD_COMPONENT_VARIANTS = 17;
    public static final int ORIGINAL_CAP_COMPONENT_VARIANTS = 9;
    public static final int ORIGINAL_STAFF_ATTACK_DAMAGE = 6;
    public static final int ONE_VIS_CENTIVIS = 100;

    private TC4WandComponentsFullClosureParity() {}

    public static boolean capacitiesAndCraftCostsMatchOriginal() {
        return WandRodType.WOOD.baseCapacity() == 25 && WandRodType.WOOD.craftCost() == 1
                && WandRodType.GREATWOOD.baseCapacity() == 50 && WandRodType.GREATWOOD.craftCost() == 3
                && sixElementalRodsMatch(75, 6, false)
                && WandRodType.SILVERWOOD.baseCapacity() == 100 && WandRodType.SILVERWOOD.craftCost() == 9
                && WandRodType.GREATWOOD_STAFF.baseCapacity() == 125 && WandRodType.GREATWOOD_STAFF.craftCost() == 8
                && sixElementalRodsMatch(175, 14, true)
                && WandRodType.SILVERWOOD_STAFF.baseCapacity() == 250 && WandRodType.SILVERWOOD_STAFF.craftCost() == 24
                && WandRodType.PRIMAL_STAFF.baseCapacity() == 250 && WandRodType.PRIMAL_STAFF.craftCost() == 32;
    }

    private static boolean sixElementalRodsMatch(int capacity, int cost, boolean staff) {
        WandRodType[] values = staff
                ? new WandRodType[]{WandRodType.OBSIDIAN_STAFF, WandRodType.BLAZE_STAFF, WandRodType.ICE_STAFF,
                    WandRodType.QUARTZ_STAFF, WandRodType.BONE_STAFF, WandRodType.REED_STAFF}
                : new WandRodType[]{WandRodType.OBSIDIAN, WandRodType.BLAZE, WandRodType.ICE,
                    WandRodType.QUARTZ, WandRodType.BONE, WandRodType.REED};
        for (WandRodType value : values) {
            if (value.baseCapacity() != capacity || value.craftCost() != cost || value.staff() != staff) return false;
        }
        return true;
    }

    public static boolean capCostsAndModifiersMatchOriginal() {
        return cap(WandCapType.IRON, 1.1F, 1)
                && cap(WandCapType.GOLD, 1.0F, 3)
                && cap(WandCapType.THAUMIUM, 0.9F, 6)
                && cap(WandCapType.COPPER, 1.1F, 2)
                && cap(WandCapType.SILVER, 1.0F, 4)
                && cap(WandCapType.VOID, 0.8F, 9)
                && close(WandCapType.COPPER.visCostModifier(Aspect.ORDO), 1.0F)
                && close(WandCapType.COPPER.visCostModifier(Aspect.PERDITIO), 1.0F)
                && close(WandCapType.SILVER.visCostModifier(Aspect.AER), 0.95F)
                && close(WandCapType.SILVER.visCostModifier(Aspect.TERRA), 0.95F)
                && close(WandCapType.SILVER.visCostModifier(Aspect.IGNIS), 0.95F)
                && close(WandCapType.SILVER.visCostModifier(Aspect.AQUA), 0.95F);
    }

    private static boolean cap(WandCapType cap, float modifier, int cost) {
        return close(cap.visCostModifier(), modifier) && cap.craftCost() == cost;
    }

    public static boolean regenerationMatchesOriginal() {
        return WandRodType.ELEMENTAL_REGEN_INTERVAL_TICKS == 200
                && WandRodType.PRIMAL_STAFF_REGEN_INTERVAL_TICKS == 50
                && WandRodType.REGEN_THRESHOLD_DIVISOR == 10
                && WandRodType.REGEN_AMOUNT_VIS * ONE_VIS_CENTIVIS == 100
                && WandRodType.OBSIDIAN.regeneratedAspect() == Aspect.TERRA
                && WandRodType.BLAZE.regeneratedAspect() == Aspect.IGNIS
                && WandRodType.ICE.regeneratedAspect() == Aspect.AQUA
                && WandRodType.QUARTZ.regeneratedAspect() == Aspect.ORDO
                && WandRodType.BONE.regeneratedAspect() == Aspect.PERDITIO
                && WandRodType.REED.regeneratedAspect() == Aspect.AER
                && WandRodType.PRIMAL_STAFF.regeneratesAllPrimals();
    }

    public static boolean catalogueCountsMatchOriginal() {
        return WandVariantRuntime.CRAFTABLE_ROD_COUNT == 18
                && WandVariantRuntime.CRAFTABLE_CAP_COUNT == 6
                && WandVariantRuntime.ORIGINAL_CREATIVE_VARIANT_COUNT == ORIGINAL_WAND_CREATIVE_VARIANTS;
    }

    private static boolean close(float a, float b) { return Math.abs(a - b) < 0.0001F; }
}
