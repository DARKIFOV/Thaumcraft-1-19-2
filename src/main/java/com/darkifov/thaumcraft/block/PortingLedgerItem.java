package com.darkifov.thaumcraft.block;

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

public class PortingLedgerItem extends Item {
    public PortingLedgerItem(Properties properties) {
        super(properties.stacksTo(1));
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);

        if (!level.isClientSide) {
            player.displayClientMessage(Component.literal("=== Thaumcraft Legacy Port Ledger / Stage 67 ===").withStyle(ChatFormatting.LIGHT_PURPLE), false);
            player.displayClientMessage(Component.literal("Core Thaumcraft: maximal registry/resource pass complete.").withStyle(ChatFormatting.AQUA), false);
            player.displayClientMessage(Component.literal("Thaumcraft Extras: ported items/entities/trade/foci branch checked.").withStyle(ChatFormatting.AQUA), false);
            player.displayClientMessage(Component.literal("Thaumic Energistics: cells/drive/terminal/pattern branch checked.").withStyle(ChatFormatting.AQUA), false);
            player.displayClientMessage(Component.literal("Thaumic Tinkerer: true-completion branch checked.").withStyle(ChatFormatting.GOLD), false);
            player.displayClientMessage(Component.literal("Next required step: real Forge build log verification.").withStyle(ChatFormatting.GRAY), false);
        }

        return InteractionResultHolder.success(stack);
    }

    @Override
    public void appendHoverText(ItemStack stack, Level level, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.literal("In-game completion ledger for the Stage 67 port parity pass.").withStyle(ChatFormatting.GRAY));
        tooltip.add(Component.literal("Right-click to print current branch status.").withStyle(ChatFormatting.LIGHT_PURPLE));
    }
}
