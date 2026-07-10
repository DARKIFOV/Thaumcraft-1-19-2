package com.darkifov.thaumcraft.block;

import com.darkifov.thaumcraft.ThaumcraftMod;
import com.darkifov.thaumcraft.blockentity.AlchemicalCentrifugeBlockEntity;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

public class AlchemicalCentrifugeBlock extends BaseEntityBlock {
    public AlchemicalCentrifugeBlock(Properties properties) {
        super(properties);
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new AlchemicalCentrifugeBlockEntity(pos, state);
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        if (level.isClientSide) {
            return createTickerHelper(type, ThaumcraftMod.ALCHEMICAL_CENTRIFUGE_BLOCK_ENTITY.get(), AlchemicalCentrifugeBlockEntity::clientTick);
        }
        return createTickerHelper(type, ThaumcraftMod.ALCHEMICAL_CENTRIFUGE_BLOCK_ENTITY.get(), AlchemicalCentrifugeBlockEntity::serverTick);
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player,
                                 InteractionHand hand, BlockHitResult hit) {
        if (level.isClientSide) {
            return InteractionResult.SUCCESS;
        }
        BlockEntity blockEntity = level.getBlockEntity(pos);
        if (!(blockEntity instanceof AlchemicalCentrifugeBlockEntity centrifuge)) {
            return InteractionResult.PASS;
        }
        player.displayClientMessage(Component.literal("Alchemical Centrifuge | input="
                + (centrifuge.inputAspect() == null ? "empty" : centrifuge.inputAspect().id())
                + " | output=" + (centrifuge.outputAspect() == null ? "empty" : centrifuge.outputAspect().id())
                + " | process=" + centrifuge.process() + "/" + AlchemicalCentrifugeBlockEntity.ORIGINAL_PROCESS_TICKS
                + " | powered=" + centrifuge.isPowered()).withStyle(ChatFormatting.AQUA), false);
        return InteractionResult.CONSUME;
    }
}
