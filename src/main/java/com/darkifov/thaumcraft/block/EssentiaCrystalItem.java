package com.darkifov.thaumcraft.block;

import com.darkifov.thaumcraft.Aspect;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

import javax.annotation.Nullable;
import java.util.List;

/** Original TC4 ItemCrystalEssence: one stable essentia point stored in NBT. */
public class EssentiaCrystalItem extends Item {
    private static final String TAG_ASPECT = "Aspect";

    public EssentiaCrystalItem(Properties properties) {
        super(properties);
    }

    public static ItemStack create(Item item, Aspect aspect) {
        ItemStack stack = new ItemStack(item);
        if (aspect != null) {
            stack.getOrCreateTag().putString(TAG_ASPECT, aspect.id());
        }
        return stack;
    }

    public static Aspect aspect(ItemStack stack) {
        if (stack == null || stack.isEmpty() || stack.getTag() == null) {
            return null;
        }
        return Aspect.byId(stack.getTag().getString(TAG_ASPECT));
    }

    public static int tint(ItemStack stack) {
        Aspect aspect = aspect(stack);
        return aspect == null ? 0xFFFFFF : aspect.nativeColor();
    }

    @Override
    public Component getName(ItemStack stack) {
        Aspect aspect = aspect(stack);
        if (aspect == null) {
            return super.getName(stack);
        }
        return Component.translatable("item.thaumcraft.tc4_crystalessence")
                .append(Component.literal(" (" + aspect.displayName() + ")").withStyle(aspect.color()));
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        Aspect aspect = aspect(stack);
        if (aspect != null) {
            tooltip.add(Component.literal("1 " + aspect.displayName() + " essentia").withStyle(aspect.color()));
        } else {
            tooltip.add(Component.translatable("tooltip.thaumcraft.crystalessence.empty").withStyle(ChatFormatting.GRAY));
        }
    }
}
