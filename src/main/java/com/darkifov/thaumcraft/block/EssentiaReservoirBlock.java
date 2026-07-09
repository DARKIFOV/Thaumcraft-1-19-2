package com.darkifov.thaumcraft.block;

import com.darkifov.thaumcraft.ThaumcraftMod;
import com.darkifov.thaumcraft.blockentity.EssentiaReservoirBlockEntity;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.phys.BlockHitResult;

/**
 * Stage503-522 TC4 BlockEssentiaReservoir bridge.
 *
 * Original 1.7.10 source-of-truth markers:
 * - thaumcraft.common.blocks.BlockEssentiaReservoir
 * - thaumcraft.common.tiles.TileEssentiaReservoir
 * - thaumcraft.client.renderers.tile.TileEssentiaReservoirRenderer
 *
 * The reservoir is not a creative storage block. It stores mixed essentia, keeps
 * original-facing NBT and exposes exactly one tube access side.
 */
public class EssentiaReservoirBlock extends BaseEntityBlock {
    public static final DirectionProperty FACING = HorizontalDirectionalBlock.FACING;

    public EssentiaReservoirBlock(Properties properties) {
        super(properties);
        registerDefaultState(defaultBlockState().setValue(FACING, Direction.NORTH));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<net.minecraft.world.level.block.Block, BlockState> builder) {
        builder.add(FACING);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return defaultBlockState().setValue(FACING, context.getHorizontalDirection().getOpposite());
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new EssentiaReservoirBlockEntity(pos, state);
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        if (level.isClientSide) {
            return null;
        }
        return createTickerHelper(type, ThaumcraftMod.ESSENTIA_RESERVOIR_BLOCK_ENTITY.get(), EssentiaReservoirBlockEntity::serverTick);
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player,
                                 InteractionHand hand, BlockHitResult hit) {
        if (level.isClientSide) {
            return InteractionResult.SUCCESS;
        }
        BlockEntity blockEntity = level.getBlockEntity(pos);
        if (!(blockEntity instanceof EssentiaReservoirBlockEntity reservoir)) {
            return InteractionResult.PASS;
        }
        if (player.getItemInHand(hand).getItem() instanceof WandItem && player.isShiftKeyDown()) {
            Direction next = state.getValue(FACING).getClockWise();
            level.setBlock(pos, state.setValue(FACING, next), 3);
            reservoir.setFacing(next);
            player.displayClientMessage(Component.literal("Essentia Reservoir | access side=" + next.getName()).withStyle(ChatFormatting.GOLD), false);
            return InteractionResult.CONSUME;
        }
        player.displayClientMessage(Component.literal("Essentia Reservoir | " + reservoir.amount() + "/" + EssentiaReservoirBlockEntity.CAPACITY
                        + " | access=" + reservoir.facing().getName()
                        + " | Aspects: ")
                        .append(reservoir.aspects().toComponent()),
                false);
        return InteractionResult.CONSUME;
    }
}
