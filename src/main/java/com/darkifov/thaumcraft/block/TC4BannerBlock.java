package com.darkifov.thaumcraft.block;

import com.darkifov.thaumcraft.Aspect;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.material.PushReaction;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

import javax.annotation.Nullable;

/**
 * Functional port of TC4's metadata-8 wooden-device banner.
 *
 * <p>The block preserves the original 16-step floor rotation, wall mounting,
 * colour NBT, aspect application from filled essentia phials, sneaking erase,
 * NBT-preserving drops and non-solid collision.</p>
 */
public final class TC4BannerBlock extends BaseEntityBlock {
    public static final BooleanProperty WALL = BooleanProperty.create("wall");
    public static final IntegerProperty ROTATION = IntegerProperty.create("rotation", 0, 15);
    public static final DirectionProperty FACING = DirectionProperty.create("facing", Direction.Plane.HORIZONTAL);

    private static final VoxelShape FLOOR_SHAPE = Block.box(5.0D, 0.0D, 5.0D, 11.0D, 16.0D, 11.0D);
    private static final VoxelShape NORTH_SHAPE = Block.box(0.0D, 0.0D, 0.0D, 16.0D, 16.0D, 4.0D);
    private static final VoxelShape SOUTH_SHAPE = Block.box(0.0D, 0.0D, 12.0D, 16.0D, 16.0D, 16.0D);
    private static final VoxelShape WEST_SHAPE = Block.box(0.0D, 0.0D, 0.0D, 4.0D, 16.0D, 16.0D);
    private static final VoxelShape EAST_SHAPE = Block.box(12.0D, 0.0D, 0.0D, 16.0D, 16.0D, 16.0D);

    public TC4BannerBlock(Properties properties) {
        super(properties);
        registerDefaultState(stateDefinition.any()
                .setValue(WALL, false)
                .setValue(ROTATION, 0)
                .setValue(FACING, Direction.SOUTH));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(WALL, ROTATION, FACING);
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        Direction side = context.getClickedFace();
        if (side.getAxis().isHorizontal()) {
            return defaultBlockState()
                    .setValue(WALL, true)
                    .setValue(FACING, side)
                    .setValue(ROTATION, wallRotation(side));
        }
        int rotation = Mth.floor((180.0F + context.getRotation()) * 16.0F / 360.0F + 0.5D) & 15;
        return defaultBlockState()
                .setValue(WALL, false)
                .setValue(FACING, context.getHorizontalDirection().getOpposite())
                .setValue(ROTATION, rotation);
    }

    private static int wallRotation(Direction side) {
        return switch (side) {
            case NORTH -> 8;
            case WEST -> 4;
            case EAST -> 12;
            default -> 0;
        };
    }

    @Override
    public boolean canSurvive(BlockState state, net.minecraft.world.level.LevelReader level, BlockPos pos) {
        if (state.getValue(WALL)) {
            Direction facing = state.getValue(FACING);
            return level.getBlockState(pos.relative(facing.getOpposite())).isFaceSturdy(level,
                    pos.relative(facing.getOpposite()), facing);
        }
        return level.getBlockState(pos.below()).isFaceSturdy(level, pos.below(), Direction.UP);
    }

    @Override
    public void neighborChanged(BlockState state, Level level, BlockPos pos, Block block,
                                BlockPos fromPos, boolean moving) {
        if (!state.canSurvive(level, pos)) {
            level.destroyBlock(pos, true);
            return;
        }
        super.neighborChanged(state, level, pos, block, fromPos, moving);
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new TC4BannerBlockEntity(pos, state);
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.ENTITYBLOCK_ANIMATED;
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        if (!state.getValue(WALL)) {
            return FLOOR_SHAPE;
        }
        return switch (state.getValue(FACING)) {
            case NORTH -> NORTH_SHAPE;
            case SOUTH -> SOUTH_SHAPE;
            case WEST -> WEST_SHAPE;
            case EAST -> EAST_SHAPE;
            default -> FLOOR_SHAPE;
        };
    }

    @Override
    public VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos,
                                        CollisionContext context) {
        return Shapes.empty();
    }

    @Override
    public PushReaction getPistonPushReaction(BlockState state) {
        return PushReaction.DESTROY;
    }

    @Override
    public void setPlacedBy(Level level, BlockPos pos, BlockState state,
                            @Nullable LivingEntity placer, ItemStack stack) {
        super.setPlacedBy(level, pos, state, placer, stack);
        if (!level.isClientSide && level.getBlockEntity(pos) instanceof TC4BannerBlockEntity banner) {
            banner.setColor(TC4BannerBlockItem.getColor(stack));
            banner.setAspect(TC4BannerBlockItem.getAspect(stack));
        }
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player,
                                 InteractionHand hand, BlockHitResult hit) {
        if (!(level.getBlockEntity(pos) instanceof TC4BannerBlockEntity banner) || banner.color() < 0) {
            return InteractionResult.PASS;
        }

        if (player.isShiftKeyDown()) {
            if (!level.isClientSide && banner.aspect() != null) {
                banner.setAspect(null);
                level.playSound(null, pos, SoundEvents.WOOL_PLACE, SoundSource.BLOCKS, 1.0F, 1.0F);
            }
            return InteractionResult.sidedSuccess(level.isClientSide);
        }

        ItemStack held = player.getItemInHand(hand);
        if (!(held.getItem() instanceof EssentiaPhialItem) || !EssentiaPhialItem.isFilled(held)) {
            return InteractionResult.PASS;
        }

        Aspect aspect = EssentiaPhialItem.getAspect(held);
        if (aspect == null) {
            return InteractionResult.PASS;
        }
        if (!level.isClientSide) {
            banner.setAspect(aspect);
            if (!player.getAbilities().instabuild) {
                held.shrink(1);
            }
            level.playSound(null, pos, SoundEvents.WOOL_PLACE, SoundSource.BLOCKS, 1.0F, 1.0F);
        }
        return InteractionResult.sidedSuccess(level.isClientSide);
    }

    @Override
    public java.util.List<ItemStack> getDrops(BlockState state,
                                               net.minecraft.world.level.storage.loot.LootContext.Builder builder) {
        BlockEntity blockEntity = builder.getOptionalParameter(
                net.minecraft.world.level.storage.loot.parameters.LootContextParams.BLOCK_ENTITY);
        ItemStack stack = new ItemStack(this);
        if (blockEntity instanceof TC4BannerBlockEntity banner) {
            TC4BannerBlockItem.setColor(stack, banner.color());
            TC4BannerBlockItem.setAspect(stack, banner.aspect());
        }
        return java.util.List.of(stack);
    }

    @Override
    public ItemStack getCloneItemStack(BlockGetter level, BlockPos pos, BlockState state) {
        ItemStack stack = new ItemStack(this);
        if (level.getBlockEntity(pos) instanceof TC4BannerBlockEntity banner) {
            TC4BannerBlockItem.setColor(stack, banner.color());
            TC4BannerBlockItem.setAspect(stack, banner.aspect());
        }
        return stack;
    }
}
