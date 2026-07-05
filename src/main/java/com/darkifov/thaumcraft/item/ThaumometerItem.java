package com.darkifov.thaumcraft.item;

import com.darkifov.thaumcraft.aura.AuraNodeScan;
import com.darkifov.thaumcraft.blockentity.AuraNodeBlockEntity;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.block.entity.BlockEntity;

public class ThaumometerItem extends Item {
    public ThaumometerItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        BlockPos pos = context.getClickedPos();
        BlockEntity blockEntity = context.getLevel().getBlockEntity(pos);

        if (blockEntity instanceof AuraNodeBlockEntity node && context.getPlayer() != null) {
            if (!context.getLevel().isClientSide()) {
                node.markScanned();
                AuraNodeScan.sendScan(context.getPlayer(), node);
            }

            return InteractionResult.sidedSuccess(context.getLevel().isClientSide());
        }

        if (context.getPlayer() != null && !context.getLevel().isClientSide()) {
            context.getPlayer().displayClientMessage(Component.literal("Nothing of thaumic interest was found.").withStyle(ChatFormatting.GRAY), true);
        }

        return InteractionResult.PASS;
    }
}
