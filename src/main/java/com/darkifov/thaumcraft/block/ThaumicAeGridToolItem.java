package com.darkifov.thaumcraft.block;

import com.darkifov.thaumcraft.data.PlayerThaumData;
import com.darkifov.thaumcraft.thaumicenergistics.ThaumicAeGrid;
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

public class ThaumicAeGridToolItem extends Item {
    public ThaumicAeGridToolItem(Properties properties) {
        super(properties.stacksTo(1));
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);

        if (!level.isClientSide) {
            PlayerThaumData.unlockResearch(player, "AE2_INTERNAL_PARITY");
            ThaumicAeGrid.sendStatus(level, player.blockPosition(), player);
            player.displayClientMessage(Component.literal("Grid Tool сканирует Thaumic AE сеть в радиусе 24 блоков.").withStyle(ChatFormatting.LIGHT_PURPLE), false);
        }

        return InteractionResultHolder.success(stack);
    }

    @Override
    public boolean isFoil(ItemStack stack) {
        return true;
    }

    @Override
    public void appendHoverText(ItemStack stack, Level level, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.literal("Stage 81 AE2 internal parity diagnostic tool.").withStyle(ChatFormatting.AQUA));
        tooltip.add(Component.literal("ПКМ: показать controller/cables/channels/energy/crafting CPU/essentia.").withStyle(ChatFormatting.GRAY));
    }
}
