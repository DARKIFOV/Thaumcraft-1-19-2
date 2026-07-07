package com.darkifov.thaumcraft.block;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

public class EssentiaValveBlock extends EssentiaTubeBlock {
    public EssentiaValveBlock(Properties properties) {
        super(properties, com.darkifov.thaumcraft.essentia.EssentiaTubeSubtype.VALVE);
    }

    public static boolean isOpen(Level level, BlockPos pos) {
        return !level.hasNeighborSignal(pos);
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player,
                                 InteractionHand hand, BlockHitResult hit) {
        if (player.getItemInHand(hand).getItem() instanceof WandItem) {
            return super.use(state, level, pos, player, hand, hit);
        }
        if (!level.isClientSide) {
            player.displayClientMessage(
                    Component.literal("Essentia Valve | " + (isOpen(level, pos) ? "OPEN" : "CLOSED BY REDSTONE") + " | Redstone signal closes allowFlow like original TileTubeValve.").withStyle(ChatFormatting.AQUA),
                    false
            );
        }

        return InteractionResult.sidedSuccess(level.isClientSide);
    }
}
