package com.darkifov.thaumcraft.wand;

import com.darkifov.thaumcraft.block.WandItem;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;

public record WandComponentData(WandRodType rod, WandCapType cap) {
    private static final String TAG_WAND = "Wand";
    private static final String TAG_ROD = "Rod";
    private static final String TAG_CAP = "Cap";

    public static WandComponentData from(ItemStack stack) {
        if (stack.getItem() instanceof WandItem wandItem) {
            CompoundTag tag = stack.getTagElement(TAG_WAND);
            WandRodType rod = wandItem.defaultRod();
            WandCapType cap = wandItem.defaultCap();

            if (tag != null) {
                rod = WandRodType.fromId(tag.getString(TAG_ROD));
                cap = WandCapType.fromId(tag.getString(TAG_CAP));
            }

            return new WandComponentData(rod, cap);
        }

        return new WandComponentData(WandRodType.WOOD, WandCapType.IRON);
    }

    public static void write(ItemStack stack, WandRodType rod, WandCapType cap) {
        CompoundTag tag = stack.getOrCreateTagElement(TAG_WAND);
        tag.putString(TAG_ROD, rod.id());
        tag.putString(TAG_CAP, cap.id());
    }

    public int capacity() {
        return rod.baseCapacity();
    }

    public float visCostModifier() {
        return cap.visCostModifier();
    }

    public String displayName() {
        return cap.id() + "-capped " + rod.id() + " wand";
    }
}
