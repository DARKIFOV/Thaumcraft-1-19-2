package com.darkifov.thaumcraft.item;

import com.darkifov.thaumcraft.eldritch.TC4EldritchProgression;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

import javax.annotation.Nullable;
import java.util.List;

/** Explicit TC4 ItemEldritchObject meta 1 implementation. */
public final class TC4CrimsonRitesItem extends Item {
    public TC4CrimsonRitesItem(Properties properties) {
        super(properties.stacksTo(1).rarity(net.minecraft.world.item.Rarity.UNCOMMON));
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (!level.isClientSide) {
            TC4EldritchProgression.readCrimsonRites(player);
        }
        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide);
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("tooltip.thaumcraft.crimson_rites.symbols").withStyle(ChatFormatting.DARK_PURPLE));
        tooltip.add(Component.translatable("tooltip.thaumcraft.crimson_rites.study").withStyle(ChatFormatting.DARK_BLUE));
        super.appendHoverText(stack, level, tooltip, flag);
    }
}
