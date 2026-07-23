package com.darkifov.thaumcraft.block;

import com.darkifov.thaumcraft.ThaumcraftMod;
import com.darkifov.thaumcraft.taint.TaintSpreadRuntime;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.shapes.BooleanOp;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

/** TC4 metadata 0..4 fibres, grass and spore stalk states. */
public class TaintFibresBlock extends Block {
    public static final IntegerProperty AGE = IntegerProperty.create("age", 0, 4);
    public static final BooleanProperty DOWN = BooleanProperty.create("down");
    public static final BooleanProperty UP = BooleanProperty.create("up");
    public static final BooleanProperty NORTH = BooleanProperty.create("north");
    public static final BooleanProperty SOUTH = BooleanProperty.create("south");
    public static final BooleanProperty WEST = BooleanProperty.create("west");
    public static final BooleanProperty EAST = BooleanProperty.create("east");
    private static final VoxelShape GROWTH = Block.box(3, 0, 3, 13, 13, 13);

    public TaintFibresBlock(Properties properties) {
        super(properties);
        registerDefaultState(stateDefinition.any().setValue(AGE, 0)
                .setValue(DOWN, false).setValue(UP, false).setValue(NORTH, false)
                .setValue(SOUTH, false).setValue(WEST, false).setValue(EAST, false));
    }

    @Override protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(AGE, DOWN, UP, NORTH, SOUTH, WEST, EAST);
    }

    @Override public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        if (state.getValue(AGE) > 0) return GROWTH;
        VoxelShape shape = Shapes.empty();
        if (state.getValue(DOWN)) shape = Shapes.joinUnoptimized(shape, Block.box(0,0,0,16,1,16), BooleanOp.OR);
        if (state.getValue(UP)) shape = Shapes.joinUnoptimized(shape, Block.box(0,15,0,16,16,16), BooleanOp.OR);
        if (state.getValue(NORTH)) shape = Shapes.joinUnoptimized(shape, Block.box(0,0,0,16,16,1), BooleanOp.OR);
        if (state.getValue(SOUTH)) shape = Shapes.joinUnoptimized(shape, Block.box(0,0,15,16,16,16), BooleanOp.OR);
        if (state.getValue(WEST)) shape = Shapes.joinUnoptimized(shape, Block.box(0,0,0,1,16,16), BooleanOp.OR);
        if (state.getValue(EAST)) shape = Shapes.joinUnoptimized(shape, Block.box(15,0,0,16,16,16), BooleanOp.OR);
        return shape.isEmpty() ? Block.box(0,0,0,16,1,16) : shape;
    }

    @Override public BlockState getStateForPlacement(BlockPlaceContext context) {
        return withAttachments(context.getLevel(), context.getClickedPos(), defaultBlockState());
    }

    @Override public boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
        if (state.getValue(AGE) > 0) return level.getBlockState(pos.below()).isFaceSturdy(level, pos.below(), Direction.UP);
        return hasAttachment(withAttachments(level, pos, state));
    }

    @Override public BlockState updateShape(BlockState state, Direction direction, BlockState neighborState,
                                             LevelAccessor level, BlockPos pos, BlockPos neighborPos) {
        BlockState updated = withAttachments(level, pos, state);
        return updated.canSurvive(level, pos) ? updated : Blocks.AIR.defaultBlockState();
    }

    @Override public void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        TaintSpreadRuntime.randomTickFibres(level, pos, state, random);
    }

    @Override public void entityInside(BlockState state, Level level, BlockPos pos, Entity entity) {
        if (!level.isClientSide && entity instanceof LivingEntity living && !living.isInvertedHealAndHarm()) {
            int chance = living instanceof Player ? 1000 : 500;
            int duration = living instanceof Player ? 80 : 160;
            if (level.random.nextInt(chance) == 0) living.addEffect(new MobEffectInstance(ThaumcraftMod.TAINT_POISON.get(), duration, 0, false, false));
        }
    }

    public static BlockState withAttachments(LevelReader level, BlockPos pos, BlockState state) {
        if (state.getValue(AGE) > 0) return state;
        return state.setValue(DOWN, supports(level, pos, Direction.DOWN))
                .setValue(UP, supports(level, pos, Direction.UP))
                .setValue(NORTH, supports(level, pos, Direction.NORTH))
                .setValue(SOUTH, supports(level, pos, Direction.SOUTH))
                .setValue(WEST, supports(level, pos, Direction.WEST))
                .setValue(EAST, supports(level, pos, Direction.EAST));
    }

    public static boolean hasAttachment(BlockState state) {
        return state.getValue(DOWN) || state.getValue(UP) || state.getValue(NORTH)
                || state.getValue(SOUTH) || state.getValue(WEST) || state.getValue(EAST);
    }

    private static boolean supports(LevelReader level, BlockPos pos, Direction face) {
        BlockPos support = pos.relative(face);
        BlockState state = level.getBlockState(support);
        return !TaintSpreadRuntime.isTaint(state) && state.isFaceSturdy(level, support, face.getOpposite());
    }
}
