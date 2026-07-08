package com.darkifov.thaumcraft.infusion;

import com.darkifov.thaumcraft.Aspect;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;

/**
 * Port of TC4 1.7.10 InfusionRunicAugmentRecipe.
 *
 * Original formulas:
 *   components = iron ingot + salis mundus + salis mundus * finalRunicCharge
 *   aspects    = ARMOR vis/2 + MAGIC vis/2 + ENERGY vis, vis=32*2^finalRunicCharge
 *   instability= 5 + finalRunicCharge/2
 *   output     = central copy with byte tag RS.HARDEN incremented by one
 */
public final class TC4InfusionRunicAugmentAdapter {
    public static final ResourceLocation RECIPE_ID = new ResourceLocation("thaumcraft", "tc4_runic_augmentation");
    public static final ResourceLocation IRON_INGOT = new ResourceLocation("minecraft", "iron_ingot");
    public static final ResourceLocation SALIS_MUNDUS = new ResourceLocation("thaumcraft", "tc4_dust");
    public static final String TC4_KIND = "INFUSION_RUNIC_AUGMENT";
    public static final String RESEARCH = "RUNICAUGMENTATION";

    private TC4InfusionRunicAugmentAdapter() {
    }

    public static InfusionRecipe materializeRecipe() {
        InfusionRecipe recipe = InfusionRecipe.runicAugmentRecipe(RECIPE_ID);
        recipe.component(IRON_INGOT);
        recipe.component(SALIS_MUNDUS);
        return recipe;
    }

    public static boolean canApply(ItemStack central) {
        return TC4RunicArmorHelper.isRunicArmor(central);
    }

    public static List<ResourceLocation> componentsFor(ItemStack central) {
        List<ResourceLocation> out = new ArrayList<>();
        out.add(IRON_INGOT);
        out.add(SALIS_MUNDUS);
        int finalCharge = TC4RunicArmorHelper.getFinalCharge(central);
        for (int i = 0; i < finalCharge; i++) {
            out.add(SALIS_MUNDUS);
        }
        return out;
    }

    public static EnumMap<Aspect, Integer> aspectsFor(ItemStack central) {
        EnumMap<Aspect, Integer> out = new EnumMap<>(Aspect.class);
        int finalCharge = Math.max(0, TC4RunicArmorHelper.getFinalCharge(central));
        double raw = 32.0D * Math.pow(2.0D, finalCharge);
        int vis = raw > Integer.MAX_VALUE ? Integer.MAX_VALUE : Math.max(0, (int) raw);
        if (vis > 0) {
            out.put(Aspect.TUTAMEN, Math.max(1, vis / 2));
            out.put(Aspect.PRAECANTATIO, Math.max(1, vis / 2));
            out.put(Aspect.POTENTIA, vis);
        }
        return out;
    }

    public static int instabilityFor(ItemStack central) {
        return 5 + Math.max(0, TC4RunicArmorHelper.getFinalCharge(central)) / 2;
    }

    public static ItemStack outputFor(ItemStack central) {
        return TC4RunicArmorHelper.addHardening(central);
    }

    public static boolean applyOutput(ItemStack central) {
        if (!canApply(central)) {
            return false;
        }
        ItemStack out = outputFor(central);
        if (out.isEmpty()) {
            return false;
        }
        central.setTag(out.getTag() == null ? null : out.getTag().copy());
        return true;
    }
}
