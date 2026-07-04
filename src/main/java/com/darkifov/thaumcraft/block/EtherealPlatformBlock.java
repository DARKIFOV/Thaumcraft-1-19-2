package com.darkifov.thaumcraft.block;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public class EtherealPlatformBlock extends Block {
    public static final BooleanProperty SOLID = BooleanProperty.create("solid");
    public static final BooleanProperty REDSTONE_LOCKED = BooleanProperty.create("redstone_locked");

    private static final VoxelShape PLATFORM_SHAPE = Block.box(0.0D, 6.0D, 0.0D, 16.0D, 8.0D, 16.0D);

    public EtherealPlatformBlock(BlockBehaviour.Properties properties) {
        super(properties);
        registerDefaultState(stateDefinition.any()
                .setValue(SOLID, true)
                .setValue(REDSTONE_LOCKED, false));
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter getter, BlockPos pos, CollisionContext context) {
        return PLATFORM_SHAPE;
    }

    @Override
    public VoxelShape getCollisionShape(BlockState state, BlockGetter getter, BlockPos pos, CollisionContext context) {
        if (!state.getValue(SOLID)) {
            return Shapes.empty();
        }

        return PLATFORM_SHAPE;
    }

    @Override
    public VoxelShape getOcclusionShape(BlockState state, BlockGetter getter, BlockPos pos) {
        return Shapes.empty();
    }

    @Override
    public boolean skipRendering(BlockState state, BlockState adjacentState, Direction direction) {
        return adjacentState.is(this) || super.skipRendering(state, adjacentState, direction);
    }

    @Override
    public float getShadeBrightness(BlockState state, BlockGetter getter, BlockPos pos) {
        return 1.0F;
    }

    @Override
    public boolean propagatesSkylightDown(BlockState state, BlockGetter getter, BlockPos pos) {
        return true;
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player,
                                 InteractionHand hand, BlockHitResult hit) {
        if (level.isClientSide) {
            return InteractionResult.SUCCESS;
        }

        if (player.isShiftKeyDown()) {
            boolean locked = !state.getValue(REDSTONE_LOCKED);
            level.setBlock(pos, state.setValue(REDSTONE_LOCKED, locked), 3);
            player.displayClientMessage(Component.literal("Ethereal Platform redstone lock: " + locked).withStyle(ChatFormatting.LIGHT_PURPLE), false);
            player.displayClientMessage(Component.literal("Когда lock включён, redstone signal управляет solid/phase состоянием.").withStyle(ChatFormatting.GRAY), false);
        } else {
            boolean solid = !state.getValue(SOLID);
            level.setBlock(pos, state.setValue(SOLID, solid), 3);
            player.displayClientMessage(Component.literal("Ethereal Platform solid: " + solid).withStyle(solid ? ChatFormatting.AQUA : ChatFormatting.GRAY), false);
            player.displayClientMessage(Component.literal("Обычный ПКМ переключает solid/phase. Shift+ПКМ — redstone lock.").withStyle(ChatFormatting.GRAY), false);
        }

        return InteractionResult.CONSUME;
    }

    @Override
    public void entityInside(BlockState state, Level level, BlockPos pos, Entity entity) {
        if (!level.isClientSide && !state.getValue(SOLID)) {
            entity.resetFallDistance();
        }

        super.entityInside(state, level, pos, entity);
    }

    @Override
    public BlockState updateShape(BlockState state, Direction direction, BlockState neighborState,
                                  LevelAccessor level, BlockPos pos, BlockPos neighborPos) {
        if (!level.isClientSide() && state.getValue(REDSTONE_LOCKED) && level instanceof Level realLevel) {
            boolean powered = realLevel.hasNeighborSignal(pos);
            boolean shouldBeSolid = !powered;

            if (state.getValue(SOLID) != shouldBeSolid) {
                return state.setValue(SOLID, shouldBeSolid);
            }
        }

        return super.updateShape(state, direction, neighborState, level, pos, neighborPos);
    }

    @Override
    public void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean movedByPiston) {
        if (!level.isClientSide && state.getValue(REDSTONE_LOCKED)) {
            boolean powered = level.hasNeighborSignal(pos);
            level.setBlock(pos, state.setValue(SOLID, !powered), 3);
        }

        super.onPlace(state, level, pos, oldState, movedByPiston);
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        if (random.nextInt(state.getValue(SOLID) ? 7 : 3) != 0) {
            return;
        }

        double x = pos.getX() + 0.1D + random.nextDouble() * 0.8D;
        double y = pos.getY() + 0.42D + random.nextDouble() * 0.18D;
        double z = pos.getZ() + 0.1D + random.nextDouble() * 0.8D;

        level.addParticle(net.minecraft.core.particles.ParticleTypes.END_ROD, x, y, z, 0.0D, 0.015D, 0.0D);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(SOLID, REDSTONE_LOCKED);
    }
}
