package com.darkifov.thaumcraft.item;

import com.darkifov.thaumcraft.Aspect;
import com.darkifov.thaumcraft.block.EssentiaPhialItem;
import net.minecraft.core.NonNullList;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;

/** TC4 ItemEssence: empty phials and one 8-unit filled variant per aspect. */
public final class TC4EssenceItem extends EssentiaPhialItem {
    public static final int TRANSFER_AMOUNT = 8;

    public TC4EssenceItem(Properties properties) {
        super(properties.stacksTo(64));
    }

    @Override
    public void fillItemCategory(CreativeModeTab tab, NonNullList<ItemStack> items) {
        if (!allowedIn(tab)) {
            return;
        }
        items.add(new ItemStack(this));
        for (Aspect aspect : Aspect.values()) {
            ItemStack filled = new ItemStack(this);
            setEssentia(filled, aspect, TRANSFER_AMOUNT);
            items.add(filled);
        }
    }

    public static int tint(ItemStack stack, int tintIndex) {
        if (tintIndex != 1) {
            return 0xFFFFFF;
        }
        Aspect aspect = getAspect(stack);
        return aspect == null ? 0xFFFFFF : aspect.nativeColor();
    }
}
