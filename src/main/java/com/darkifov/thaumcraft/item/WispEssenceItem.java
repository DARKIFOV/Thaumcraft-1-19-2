package com.darkifov.thaumcraft.item;

import com.darkifov.thaumcraft.Aspect;
import net.minecraft.ChatFormatting;
import net.minecraft.core.NonNullList;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

import javax.annotation.Nullable;
import java.util.List;

/** NBT-bearing TC4 Wisp Essence: one aspect and its stored amount. */
public class WispEssenceItem extends TC4ResearchComponentItem {
    private static final String TAG_ASPECT = "Aspect";
    private static final String TAG_AMOUNT = "Amount";

    public WispEssenceItem(Properties properties, String originalSource, String legacyTexture) {
        super(properties, originalSource, legacyTexture);
    }

    public static ItemStack withAspect(ItemStack stack, Aspect aspect, int amount) {
        if (aspect != null) stack.getOrCreateTag().putString(TAG_ASPECT, aspect.id());
        stack.getOrCreateTag().putInt(TAG_AMOUNT, Math.max(1, amount));
        return stack;
    }

    public static Aspect aspect(ItemStack stack) {
        return stack.hasTag() ? Aspect.byId(stack.getTag().getString(TAG_ASPECT)) : null;
    }

    public static int amount(ItemStack stack) {
        return stack.hasTag() ? Math.max(1, stack.getTag().getInt(TAG_AMOUNT)) : 1;
    }

    public static int tint(ItemStack stack) {
        Aspect aspect = aspect(stack);
        return aspect == null ? 0xFFFFFF : aspect.nativeColor();
    }

    @Override
    public void fillItemCategory(CreativeModeTab tab, NonNullList<ItemStack> items) {
        if (!allowedIn(tab)) return;
        for (Aspect aspect : Aspect.values()) items.add(withAspect(new ItemStack(this), aspect, 2));
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        Aspect aspect = aspect(stack);
        if (aspect != null) {
            tooltip.add(Component.literal(aspect.displayName() + " × " + amount(stack))
                    .withStyle(style -> style.withColor(aspect.nativeColor())));
        } else {
            tooltip.add(Component.literal("Unattuned essence").withStyle(ChatFormatting.GRAY));
        }
        super.appendHoverText(stack, level, tooltip, flag);
    }
}
