package com.darkifov.thaumcraft.block;

import com.darkifov.thaumcraft.blockentity.AuraNodeBlockEntity;
import com.darkifov.thaumcraft.data.PlayerThaumData;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
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

public class HelmetOfRevealingItem extends ArmorItem {
    public HelmetOfRevealingItem(Properties properties) {
        super(ArmorMaterials.GOLD, EquipmentSlot.HEAD, properties.durability(880));
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);

        if (!level.isClientSide) {
            ItemStack head = player.getItemBySlot(EquipmentSlot.HEAD);

            if (head.isEmpty()) {
                player.setItemSlot(EquipmentSlot.HEAD, stack.copy());
                stack.setCount(0);
                player.playSound(SoundEvents.ARMOR_EQUIP_GOLD, 0.8F, 1.1F);
                player.displayClientMessage(Component.literal("Helmet of Revealing equipped.").withStyle(ChatFormatting.AQUA), false);
            } else {
                scanNearby(level, player);
            }
        }

        return InteractionResultHolder.success(stack);
    }

    private void scanNearby(Level level, Player player) {
        int nodes = 0;
        BlockPos base = player.blockPosition();

        for (BlockPos pos : BlockPos.betweenClosed(base.offset(-8, -4, -8), base.offset(8, 4, 8))) {
            if (level.getBlockEntity(pos) instanceof AuraNodeBlockEntity) {
                nodes++;
                player.displayClientMessage(Component.literal("Aura Node: " + pos.toShortString()).withStyle(ChatFormatting.AQUA), false);
            }
        }

        player.displayClientMessage(Component.literal("Revealing scan | nodes nearby: " + nodes).withStyle(ChatFormatting.LIGHT_PURPLE), false);
        player.displayClientMessage(Component.literal("Research unlocked: " + PlayerThaumData.researchCount(player) + " | Warp: " + PlayerThaumData.getWarp(player)).withStyle(ChatFormatting.GRAY), false);
    }

    @Override
    public void onArmorTick(ItemStack stack, Level level, Player player) {
        if (!level.isClientSide && player.tickCount % 240 == 0) {
            PlayerThaumData.unlockResearch(player, "HELMET_REVEALING_SCAN");
        }
    }

    @Override
    public void appendHoverText(ItemStack stack, Level level, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.literal("Reveals aura/research/warp information.").withStyle(ChatFormatting.AQUA));
        tooltip.add(Component.literal("Right-click while held: equip or scan.").withStyle(ChatFormatting.GRAY));
        tooltip.add(Component.literal("Client overlay enabled while worn.").withStyle(ChatFormatting.LIGHT_PURPLE));
    }
}
