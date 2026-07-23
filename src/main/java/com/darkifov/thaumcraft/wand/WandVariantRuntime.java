package com.darkifov.thaumcraft.wand;

import com.darkifov.thaumcraft.ThaumcraftMod;
import com.darkifov.thaumcraft.block.WandItem;
import net.minecraft.world.item.ItemStack;

import java.util.List;

/**
 * Original TC4 ItemWandCasting subtype catalogue adapted to one Forge 1.19.2 NBT item.
 * The workbench may craft every registered rod/cap pair, but the original creative tab
 * exposes exactly four filled representative stacks.
 */
public final class WandVariantRuntime {
    public static final int CRAFTABLE_ROD_COUNT = 18;
    public static final int CRAFTABLE_CAP_COUNT = 6;
    public static final int ORIGINAL_CREATIVE_VARIANT_COUNT = 4;

    private WandVariantRuntime() {}

    public static List<ItemStack> creativeVariants() {
        return List.of(
                create(WandRodType.WOOD, WandCapType.IRON, false, true),
                create(WandRodType.GREATWOOD, WandCapType.GOLD, false, true),
                create(WandRodType.SILVERWOOD, WandCapType.THAUMIUM, false, true),
                create(WandRodType.SILVERWOOD, WandCapType.THAUMIUM, true, true)
        );
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
