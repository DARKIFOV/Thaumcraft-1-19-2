package com.darkifov.thaumcraft.block;

import com.darkifov.thaumcraft.ThaumcraftMod;
import com.darkifov.thaumcraft.blockentity.ArcaneDoorBlockEntity;
import com.darkifov.thaumcraft.blockentity.ArcanePressurePlateBlockEntity;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.material.PushReaction;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

/** Owner-locked TC4 Arcane Door. Ordinary redstone is ignored; matching warded plates may operate it. */
public final class ArcaneDoorBlock extends BaseEntityBlock {
    public static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;
    public static final BooleanProperty OPEN = BlockStateProperties.OPEN;
    public static final BooleanProperty UPPER = BooleanProperty.create("upper");
    public static final BooleanProperty HINGE_RIGHT = BooleanProperty.create("hinge_right");

    private static final VoxelShape NORTH = Block.box(0, 0, 13, 16, 16, 16);
    private static final VoxelShape SOUTH = Block.box(0, 0, 0, 16, 16, 3);
    private static final VoxelShape WEST = Block.box(13, 0, 0, 16, 16, 16);
    private static final VoxelShape EAST = Block.box(0, 0, 0, 3, 16, 16);

    public ArcaneDoorBlock(Properties properties) {
        super(properties);
        registerDefaultState(stateDefinition.any().setValue(FACING, Direction.NORTH)
                .setValue(OPEN, false).setValue(UPPER, false).setValue(HINGE_RIGHT, false));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING, OPEN, UPPER, HINGE_RIGHT);
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new ArcaneDoorBlockEntity(pos, state);
    }

    @Override
    public RenderShape getRenderShape(BlockState state) { return RenderShape.MODEL; }

    public static BlockPos lowerPos(BlockState state, BlockPos pos) {
        return state.hasProperty(UPPER) && state.getValue(UPPER) ? pos.below() : pos;
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player,
                                 InteractionHand hand, BlockHitResult hit) {
        BlockPos lower = lowerPos(state, pos);
        if (!(level.getBlockEntity(lower) instanceof ArcaneDoorBlockEntity door)) return InteractionResult.PASS;
        if (level.isClientSide) return InteractionResult.SUCCESS;
        door.initializeOwner(player);
        if (!door.hasAccess(player) && !player.getAbilities().instabuild) {
            player.displayClientMessage(Component.translatable("message.thaumcraft.arcane_door.protected")
                    .withStyle(ChatFormatting.BLUE), true);
            level.playSound(null, lower, SoundEvents.IRON_DOOR_CLOSE, SoundSource.BLOCKS, 0.25F, 0.8F);
            return InteractionResult.CONSUME;
        }
        setOpen(level, lower, !level.getBlockState(lower).getValue(OPEN));
        return InteractionResult.CONSUME;
    }

    public static void setOpen(Level level, BlockPos lower, boolean open) {
        BlockState lowerState = level.getBlockState(lower);
        if (!(lowerState.getBlock() instanceof ArcaneDoorBlock) || lowerState.getValue(OPEN) == open) return;
        level.setBlock(lower, lowerState.setValue(OPEN, open), Block.UPDATE_CLIENTS);
        BlockPos upper = lower.above();
        BlockState upperState = level.getBlockState(upper);
        if (upperState.getBlock() instanceof ArcaneDoorBlock) {
            level.setBlock(upper, upperState.setValue(OPEN, open), Block.UPDATE_CLIENTS);
        }
        level.playSound(null, lower, open ? SoundEvents.IRON_DOOR_OPEN : SoundEvents.IRON_DOOR_CLOSE,
                SoundSource.BLOCKS, 1.0F, 0.9F + level.random.nextFloat() * 0.1F);
        level.updateNeighborsAt(lower, lowerState.getBlock());
        level.updateNeighborsAt(upper, upperState.getBlock());
    }

    @Override
    public void neighborChanged(BlockState state, Level level, BlockPos pos, Block block, BlockPos fromPos, boolean moving) {
        if (level.isClientSide) return;
        BlockPos lower = lowerPos(state, pos);
        if (!(level.getBlockEntity(lower) instanceof ArcaneDoorBlockEntity door)) return;
        boolean shouldOpen = false;
        for (Direction direction : Direction.Plane.HORIZONTAL) {
            BlockPos platePos = lower.relative(direction);
            BlockState neighbour = level.getBlockState(platePos);
            if (neighbour.getBlock() instanceof ArcanePressurePlateBlock
                    && neighbour.getValue(ArcanePressurePlateBlock.POWERED)
                    && level.getBlockEntity(platePos) instanceof ArcanePressurePlateBlockEntity plate
                    && door.sharesAuthorization(plate)) {
                shouldOpen = true;
                break;
            }
        }
        setOpen(level, lower, shouldOpen);
    }

    @Override
    public BlockState updateShape(BlockState state, Direction direction, BlockState neighbour,
                                  LevelAccessor level, BlockPos pos, BlockPos neighbourPos) {
        if (direction == (state.getValue(UPPER) ? Direction.DOWN : Direction.UP)) {
            boolean counterpart = neighbour.getBlock() instanceof ArcaneDoorBlock
                    && neighbour.getValue(UPPER) != state.getValue(UPPER);
            if (!counterpart) return net.minecraft.world.level.block.Blocks.AIR.defaultBlockState();
        }
        if (!state.getValue(UPPER) && direction == Direction.DOWN
                && !neighbour.isFaceSturdy(level, neighbourPos, Direction.UP)) {
            return net.minecraft.world.level.block.Blocks.AIR.defaultBlockState();
        }
        return super.updateShape(state, direction, neighbour, level, pos, neighbourPos);
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        Direction facing = state.getValue(FACING);
        boolean open = state.getValue(OPEN);
        boolean right = state.getValue(HINGE_RIGHT);
        Direction plane = open ? (right ? facing.getCounterClockWise() : facing.getClockWise()) : facing;
        return switch (plane) {
            case NORTH -> NORTH;
            case SOUTH -> SOUTH;
            case WEST -> WEST;
            default -> EAST;
        };
    }

    @Override
    public void playerWillDestroy(Level level, BlockPos pos, BlockState state, Player player) {
        if (!level.isClientSide) {
            BlockPos counterpart = state.getValue(UPPER) ? pos.below() : pos.above();
            BlockState counterpartState = level.getBlockState(counterpart);
            if (counterpartState.getBlock() instanceof ArcaneDoorBlock
                    && counterpartState.getValue(UPPER) != state.getValue(UPPER)) {
                if (state.getValue(UPPER) && !player.getAbilities().instabuild) {
                    Block.popResource(level, pos, new ItemStack(ThaumcraftMod.ARCANE_DOOR_ITEM.get()));
                }
                level.setBlock(counterpart, net.minecraft.world.level.block.Blocks.AIR.defaultBlockState(),
                        Block.UPDATE_ALL | Block.UPDATE_SUPPRESS_DROPS);
            }
        }
        super.playerWillDestroy(level, pos, state, player);
    }

    @Override
    public PushReaction getPistonPushReaction(BlockState state) { return PushReaction.BLOCK; }

    @Override
    public boolean canEntityDestroy(BlockState state, BlockGetter level, BlockPos pos, Entity entity) { return false; }
}
