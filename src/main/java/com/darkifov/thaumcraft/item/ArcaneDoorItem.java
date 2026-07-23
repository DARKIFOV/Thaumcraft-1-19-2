package com.darkifov.thaumcraft.item;

import com.darkifov.thaumcraft.block.ArcaneDoorBlock;
import com.darkifov.thaumcraft.blockentity.ArcaneDoorBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

/** Places the two-block owner-bound TC4 Arcane Door. */
public final class ArcaneDoorItem extends Item {
    private final ArcaneDoorBlock block;

    public ArcaneDoorItem(ArcaneDoorBlock block, Properties properties) {
        super(properties.stacksTo(1));
        this.block = block;
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        if (context.getClickedFace() != Direction.UP) return InteractionResult.FAIL;
        Player player = context.getPlayer();
        if (player == null) return InteractionResult.PASS;
        Level level = context.getLevel();
        BlockPos lower = context.getClickedPos().above();
        BlockPos upper = lower.above();
        BlockPlaceContext placeContext = new BlockPlaceContext(context);
        if (!level.getBlockState(lower).canBeReplaced(placeContext)
                || !level.getBlockState(upper).canBeReplaced(placeContext)
                || !level.getBlockState(lower.below()).isFaceSturdy(level, lower.below(), Direction.UP)) {
            return InteractionResult.FAIL;
        }
        if (level.isClientSide) return InteractionResult.SUCCESS;

        Direction facing = player.getDirection();
        boolean hingeRight = determineHinge(level, lower, facing);
        BlockState lowerState = block.defaultBlockState().setValue(ArcaneDoorBlock.FACING, facing)
                .setValue(ArcaneDoorBlock.UPPER, false).setValue(ArcaneDoorBlock.HINGE_RIGHT, hingeRight);
        BlockState upperState = lowerState.setValue(ArcaneDoorBlock.UPPER, true);
        level.setBlock(lower, lowerState, net.minecraft.world.level.block.Block.UPDATE_CLIENTS);
        level.setBlock(upper, upperState, net.minecraft.world.level.block.Block.UPDATE_CLIENTS);
        level.updateNeighborsAt(lower, block);
        level.updateNeighborsAt(upper, block);
        if (level.getBlockEntity(lower) instanceof ArcaneDoorBlockEntity door) door.initializeOwner(player);
        if (level.getBlockEntity(upper) instanceof ArcaneDoorBlockEntity door) door.initializeOwner(player);
        level.playSound(null, lower, SoundEvents.WOOD_PLACE, SoundSource.BLOCKS, 1.0F, 0.9F);
        if (!player.getAbilities().instabuild) context.getItemInHand().shrink(1);
        return InteractionResult.CONSUME;
    }

    private boolean determineHinge(Level level, BlockPos pos, Direction facing) {
        Direction left = facing.getCounterClockWise();
        Direction right = facing.getClockWise();
        int leftSolid = solidScore(level, pos.relative(left));
        int rightSolid = solidScore(level, pos.relative(right));
        if (leftSolid != rightSolid) return rightSolid < leftSolid;
        boolean leftDoor = level.getBlockState(pos.relative(left)).getBlock() == block;
        boolean rightDoor = level.getBlockState(pos.relative(right)).getBlock() == block;
        return leftDoor && !rightDoor;
    }

    private static int solidScore(Level level, BlockPos pos) {
        int score = level.getBlockState(pos).isCollisionShapeFullBlock(level, pos) ? 1 : 0;
        return score + (level.getBlockState(pos.above()).isCollisionShapeFullBlock(level, pos.above()) ? 1 : 0);
    }
}
