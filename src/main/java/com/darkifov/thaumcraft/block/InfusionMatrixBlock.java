package com.darkifov.thaumcraft.block;

import com.darkifov.thaumcraft.ThaumcraftMod;
import com.darkifov.thaumcraft.blockentity.InfusionMatrixBlockEntity;
import com.darkifov.thaumcraft.infusion.TC4InfusionAltarFullClosureParity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

/** Original TC4 runic matrix activation, destruction and visual shell. */
public class InfusionMatrixBlock extends BaseEntityBlock {
    public InfusionMatrixBlock(Properties properties) {
        super(properties);
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new InfusionMatrixBlockEntity(pos, state);
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.ENTITYBLOCK_ANIMATED;
    }

    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return createTickerHelper(type, ThaumcraftMod.INFUSION_MATRIX_BLOCK_ENTITY.get(),
                level.isClientSide ? InfusionMatrixBlockEntity::clientTick : InfusionMatrixBlockEntity::serverTick);
    }


    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player,
                                 InteractionHand hand, BlockHitResult hit) {
        if (hand != InteractionHand.MAIN_HAND) {
            return InteractionResult.PASS;
        }
        ItemStack held = player.getMainHandItem();
        if (!(held.getItem() instanceof WandItem)) {
            return InteractionResult.PASS;
        }
        if (level.isClientSide) {
            return InteractionResult.SUCCESS;
        }
        if (!(level.getBlockEntity(pos) instanceof InfusionMatrixBlockEntity matrix)) {
            return InteractionResult.PASS;
        }
        return matrix.onWandRightClick(player) ? InteractionResult.CONSUME : InteractionResult.PASS;
    }

    @Override
    public void onRemove(BlockState oldState, Level level, BlockPos pos, BlockState newState, boolean moving) {
        boolean explode = oldState.getBlock() != newState.getBlock()
                && !level.isClientSide
                && level.getBlockEntity(pos) instanceof InfusionMatrixBlockEntity matrix
                && matrix.crafting();
        super.onRemove(oldState, level, pos, newState, moving);
        if (explode) {
            level.explode(null, pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D,
                    TC4InfusionAltarFullClosureParity.CRAFTING_BREAK_EXPLOSION_STRENGTH,
                    Explosion.BlockInteraction.BREAK);
        }
    }
}
