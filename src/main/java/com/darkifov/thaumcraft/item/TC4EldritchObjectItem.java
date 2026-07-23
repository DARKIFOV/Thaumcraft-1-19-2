package com.darkifov.thaumcraft.item;

import com.darkifov.thaumcraft.eldritch.TC4EldritchProgression;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

import javax.annotation.Nullable;
import java.util.List;

/** Explicit carriers for the simple TC4 ItemEldritchObject variants. */
public final class TC4EldritchObjectItem extends Item {
    public enum Variant { ELDRITCH_EYE, RUNED_TABLET }

    private final Variant variant;

    public TC4EldritchObjectItem(Properties properties, Variant variant) {
        super(properties.stacksTo(1).rarity(variant == Variant.RUNED_TABLET ? Rarity.RARE : Rarity.UNCOMMON));
        this.variant = variant;
    }

    public Variant variant() {
        return variant;
    }

    public static boolean isVariant(ItemStack stack, Variant expected) {
        return stack != null && stack.getItem() instanceof TC4EldritchObjectItem item && item.variant == expected;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (variant != Variant.ELDRITCH_EYE) {
            return InteractionResultHolder.pass(stack);
        }
        if (!level.isClientSide) {
            TC4EldritchProgression.attuneWithEldritchEye(player, false);
        }
        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide);
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        String key = variant == Variant.ELDRITCH_EYE
                ? "tooltip.thaumcraft.eldritch_eye.watching"
                : "tooltip.thaumcraft.runed_tablet.fragment";
        tooltip.add(Component.translatable(key).withStyle(ChatFormatting.DARK_PURPLE));
        super.appendHoverText(stack, level, tooltip, flag);
    }
}
