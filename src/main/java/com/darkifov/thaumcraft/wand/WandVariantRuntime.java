package com.darkifov.thaumcraft.wand;

import com.darkifov.thaumcraft.ThaumcraftMod;
import com.darkifov.thaumcraft.block.WandItem;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

/**
 * Original TC4 wand subtype catalogue adapted to Forge 1.19.2 NBT ItemStacks.
 *
 * One registry item represents all ordinary wand, staff and sceptre component
 * combinations. The rod/cap identity is stored in original root tags, so the
 * capacity, name and BEWLR texture all resolve from the same authoritative data.
 */
public final class WandVariantRuntime {
    public static final int CRAFTABLE_ROD_COUNT = 18;
    public static final int CRAFTABLE_CAP_COUNT = 6;
    public static final int WAND_AND_STAFF_VARIANT_COUNT = CRAFTABLE_ROD_COUNT * CRAFTABLE_CAP_COUNT;
    public static final int CREATIVE_VARIANT_COUNT = WAND_AND_STAFF_VARIANT_COUNT + 1;

    private WandVariantRuntime() {}

    public static List<ItemStack> creativeVariants() {
        List<ItemStack> variants = new ArrayList<>(CREATIVE_VARIANT_COUNT);
        for (WandRodType rod : WandRodType.values()) {
            if (rod == WandRodType.CREATIVE) {
                continue;
            }
            for (WandCapType cap : WandCapType.values()) {
                if (cap == WandCapType.INFINITY) {
                    continue;
                }
                variants.add(create(rod, cap, false, true));
            }
        }

        // Original ItemWandCasting#getSubItems adds one canonical sceptre after
        // the rod/cap matrix: silverwood rod, thaumium caps, fully charged.
        variants.add(create(WandRodType.SILVERWOOD, WandCapType.THAUMIUM, true, true));
        return List.copyOf(variants);
    }

    public static ItemStack create(WandRodType rod, WandCapType cap, boolean sceptre, boolean filled) {
        ItemStack stack = new ItemStack(ThaumcraftMod.IRON_CAPPED_WOODEN_WAND.get(), 1);
        WandComponentData.write(stack, rod, cap);
        WandComponentData.setSceptre(stack, sceptre);
        if (filled) {
            WandItem.fillToCapacity(stack);
        } else {
            WandItem.clampVisToCapacity(stack);
        }
        return stack;
    }

    /**
     * Stable JEI subtype identity. Vis values and focus NBT are intentionally
     * ignored: component geometry is determined only by rod, cap and sceptre.
     */
    public static String subtypeKey(ItemStack stack) {
        WandComponentData data = WandComponentData.from(stack);
        return data.rod().originalTag() + ":" + data.cap().originalTag() + ":"
                + (WandComponentData.isSceptre(stack) ? "sceptre" : "normal");
    }
}
