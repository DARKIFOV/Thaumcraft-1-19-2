package com.darkifov.thaumcraft.block;

import com.darkifov.thaumcraft.data.PlayerThaumData;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

import java.util.List;

public class ThaumicEnergisticsCardItem extends Item {
    private final String cardType;

    public ThaumicEnergisticsCardItem(Properties properties, String cardType) {
        super(properties.stacksTo(16));
        this.cardType = cardType;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);

        if (!level.isClientSide) {
            boolean active = !stack.getOrCreateTag().getBoolean("Active");
            stack.getOrCreateTag().putBoolean("Active", active);
            PlayerThaumData.unlockResearch(player, "THAUMIC_ENERGISTICS_CARDS");
            player.displayClientMessage(Component.literal(cardType + " card active marker: " + active).withStyle(ChatFormatting.AQUA), false);
        }

        return InteractionResultHolder.success(stack);
    }

    @Override
    public boolean isFoil(ItemStack stack) {
        return stack.hasTag() && stack.getTag().getBoolean("Active");
    }

    @Override
    public void appendHoverText(ItemStack stack, Level level, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.literal("Thaumic Energistics card: " + cardType).withStyle(ChatFormatting.AQUA));
        tooltip.add(Component.literal("Right-click toggles active marker for diagnostics.").withStyle(ChatFormatting.GRAY));
    }
}
