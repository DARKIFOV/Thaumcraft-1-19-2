package com.darkifov.thaumcraft.block;

import com.darkifov.thaumcraft.ThaumcraftMod;
import com.darkifov.thaumcraft.blockentity.ThaumatoriumBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.network.NetworkHooks;

/** Invisible upper half of the original two-block TileThaumatorium model. */
public final class ThaumatoriumUpperBlock extends Block {
    private static final VoxelShape SHAPE = Block.box(0.0D, -16.0D, 0.0D, 16.0D, 16.0D, 16.0D);

    public ThaumatoriumUpperBlock(Properties properties) {
        super(properties);
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player,
                                 InteractionHand hand, BlockHitResult hit) {
        ThaumatoriumBlockEntity tile = ThaumatoriumBlockEntity.resolveAt(level, pos);
        if (tile == null) {
            return InteractionResult.PASS;
        }
        if (!level.isClientSide && player instanceof ServerPlayer serverPlayer) {
            NetworkHooks.openScreen(serverPlayer, tile, buffer -> buffer.writeBlockPos(tile.getBlockPos()));
        }
        return InteractionResult.sidedSuccess(level.isClientSide);
    }

    @Override
    public void neighborChanged(BlockState state, Level level, BlockPos pos, Block block,
                                BlockPos fromPos, boolean moving) {
        if (!level.isClientSide && !level.getBlockState(pos.below()).is(ThaumcraftMod.THAUMATORIUM.get())) {
            level.setBlock(pos, ThaumcraftMod.TC4_ALCHEMICAL_CONSTRUCT.get().defaultBlockState(), Block.UPDATE_ALL);
        }
        super.neighborChanged(state, level, pos, block, fromPos, moving);
    }
}
