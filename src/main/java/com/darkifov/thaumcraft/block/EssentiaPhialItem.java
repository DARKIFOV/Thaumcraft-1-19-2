package com.darkifov.thaumcraft.block;

import com.darkifov.thaumcraft.Aspect;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

import java.util.List;

public class EssentiaPhialItem extends Item {
    private static final String TAG_ASPECT = "Aspect";
    private static final String TAG_AMOUNT = "Amount";

    public EssentiaPhialItem(Properties properties) {
        super(properties);
    }

    public static Aspect getAspect(ItemStack stack) {
        CompoundTag tag = stack.getTag();

        if (tag == null || !tag.contains(TAG_ASPECT)) {
            return null;
        }

        try {
            return Aspect.valueOf(tag.getString(TAG_ASPECT));
        } catch (IllegalArgumentException ignored) {
            return null;
        }
    }

    public static int getAmount(ItemStack stack) {
        CompoundTag tag = stack.getTag();

        if (tag == null) {
            return 0;
        }

        return Math.max(0, tag.getInt(TAG_AMOUNT));
    }

    public static boolean isFilled(ItemStack stack) {
        return getAspect(stack) != null && getAmount(stack) > 0;
    }

    public static void setEssentia(ItemStack stack, Aspect aspect, int amount) {
        if (aspect == null || amount <= 0) {
            clear(stack);
            return;
        }

        CompoundTag tag = stack.getOrCreateTag();
        tag.putString(TAG_ASPECT, aspect.name());
        tag.putInt(TAG_AMOUNT, amount);
    }

    public static void clear(ItemStack stack) {
        CompoundTag tag = stack.getOrCreateTag();
        tag.remove(TAG_ASPECT);
        tag.remove(TAG_AMOUNT);

        if (tag.isEmpty()) {
            stack.setTag(null);
        }
    }

    @Override
    public void appendHoverText(ItemStack stack, Level level, List<Component> tooltip, TooltipFlag flag) {
        Aspect aspect = getAspect(stack);
        int amount = getAmount(stack);

        if (aspect == null || amount <= 0) {
            tooltip.add(Component.literal("Empty").withStyle(ChatFormatting.GRAY));
        } else {
            tooltip.add(Component.literal(aspect.displayName() + " x" + amount).withStyle(aspect.color()));
        }
    }
}
