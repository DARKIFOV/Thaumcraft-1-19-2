package com.darkifov.thaumcraft.wand;

import com.darkifov.thaumcraft.Aspect;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

/**
 * Forge 1.19.2 equivalent of TC4's IVisDiscountGear contract.
 *
 * <p>The returned value is an integer percentage. A value of {@code 5}
 * therefore subtracts {@code 0.05F} from the wand consumption modifier,
 * matching WandManager#getTotalVisDiscount in Thaumcraft 4.</p>
 */
public interface TC4VisDiscountGear {
    int getVisDiscount(ItemStack stack, LivingEntity wearer, Aspect aspect);
}
