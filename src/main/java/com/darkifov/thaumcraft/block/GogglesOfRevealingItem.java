package com.darkifov.thaumcraft.block;

import com.darkifov.thaumcraft.blockentity.AuraNodeBlockEntity;
import com.darkifov.thaumcraft.data.PlayerThaumData;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterials;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

import java.util.List;

public class GogglesOfRevealingItem extends ArmorItem {
    public GogglesOfRevealingItem(Properties properties) {
        super(ArmorMaterials.GOLD, EquipmentSlot.HEAD, properties.durability(480));
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);

        if (!level.isClientSide) {
            ItemStack head = player.getItemBySlot(EquipmentSlot.HEAD);

            if (head.isEmpty()) {
                player.setItemSlot(EquipmentSlot.HEAD, stack.copy());
                stack.setCount(0);
                player.displayClientMessage(Component.literal("Goggles of Revealing equipped.").withStyle(ChatFormatting.AQUA), false);
            } else {
                scan(level, player);
            }
        }

        return InteractionResultHolder.success(stack);
    }

    private void scan(Level level, Player player) {
        int nodes = 0;
        BlockPos base = player.blockPosition();

        for (BlockPos pos : BlockPos.betweenClosed(base.offset(-6, -3, -6), base.offset(6, 3, 6))) {
            if (level.getBlockEntity(pos) instanceof AuraNodeBlockEntity) {
                nodes++;
            }
        }

        PlayerThaumData.unlockResearch(player, "GOGGLES_OF_REVEALING");
        player.displayClientMessage(Component.literal("Goggles scan | nearby aura nodes: " + nodes + " | warp: " + PlayerThaumData.getWarp(player)).withStyle(ChatFormatting.AQUA), false);
    }

    @Override
    public void onArmorTick(ItemStack stack, Level level, Player player) {
        if (!level.isClientSide && player.tickCount % 300 == 0) {
            PlayerThaumData.unlockResearch(player, "GOGGLES_OF_REVEALING");
        }
    }

    @Override
    public void appendHoverText(ItemStack stack, Level level, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.literal("Classic revealing headgear.").withStyle(ChatFormatting.AQUA));
        tooltip.add(Component.literal("Shows revealing overlay and scans aura nodes.").withStyle(ChatFormatting.GRAY));
    }
}
