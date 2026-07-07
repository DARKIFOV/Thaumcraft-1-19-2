package com.darkifov.thaumcraft.block;

import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.BooleanOp;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.StateDefinition;
import com.darkifov.thaumcraft.essentia.EssentiaTubeConnections;
import com.darkifov.thaumcraft.essentia.EssentiaTubeSubtype;
import com.darkifov.thaumcraft.ThaumcraftMod;
import com.darkifov.thaumcraft.jar.JarTubeInteractionRuntime;
import com.darkifov.thaumcraft.blockentity.EssentiaTubeBlockEntity;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;


public class EssentiaTubeBlock extends BaseEntityBlock {
    private final EssentiaTubeSubtype subtype;
    public static final BooleanProperty NORTH = BooleanProperty.create("north");
    public static final BooleanProperty SOUTH = BooleanProperty.create("south");
    public static final BooleanProperty WEST = BooleanProperty.create("west");
    public static final BooleanProperty EAST = BooleanProperty.create("east");
    public static final BooleanProperty UP = BooleanProperty.create("up");
    public static final BooleanProperty DOWN = BooleanProperty.create("down");

    private static final VoxelShape CORE_SHAPE = Block.box(6.0D, 6.0D, 6.0D, 10.0D, 10.0D, 10.0D);
    private static final VoxelShape NORTH_SHAPE = Block.box(6.0D, 6.0D, 0.0D, 10.0D, 10.0D, 6.0D);
    private static final VoxelShape SOUTH_SHAPE = Block.box(6.0D, 6.0D, 10.0D, 10.0D, 10.0D, 16.0D);
    private static final VoxelShape WEST_SHAPE = Block.box(0.0D, 6.0D, 6.0D, 6.0D, 10.0D, 10.0D);
    private static final VoxelShape EAST_SHAPE = Block.box(10.0D, 6.0D, 6.0D, 16.0D, 10.0D, 10.0D);
    private static final VoxelShape UP_SHAPE = Block.box(6.0D, 10.0D, 6.0D, 10.0D, 16.0D, 10.0D);
    private static final VoxelShape DOWN_SHAPE = Block.box(6.0D, 0.0D, 6.0D, 10.0D, 6.0D, 10.0D);

    public EssentiaTubeBlock(Properties properties) {
        this(properties, EssentiaTubeSubtype.NORMAL);
    }

    public EssentiaTubeBlock(Properties properties, EssentiaTubeSubtype subtype) {
        super(properties);
        this.subtype = subtype == null ? EssentiaTubeSubtype.NORMAL : subtype;
        registerDefaultState(defaultBlockState()
                .setValue(NORTH, false)
                .setValue(SOUTH, false)
                .setValue(WEST, false)
                .setValue(EAST, false)
                .setValue(UP, false)
                .setValue(DOWN, false));
    }


    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(NORTH, SOUTH, WEST, EAST, UP, DOWN);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return updateConnections(defaultBlockState(), context.getLevel(), context.getClickedPos());
    }

    @Override
    public BlockState updateShape(BlockState state, Direction direction, BlockState neighborState,
                                  LevelAccessor level, BlockPos pos, BlockPos neighborPos) {
        return updateConnections(state, level, pos);
    }

    private BlockState updateConnections(BlockState state, LevelAccessor level, BlockPos pos) {
        if (!(level instanceof Level concreteLevel)) {
            return state;
        }

        return state
                .setValue(NORTH, EssentiaTubeConnections.canConnect(concreteLevel, pos, Direction.NORTH))
                .setValue(SOUTH, EssentiaTubeConnections.canConnect(concreteLevel, pos, Direction.SOUTH))
                .setValue(WEST, EssentiaTubeConnections.canConnect(concreteLevel, pos, Direction.WEST))
                .setValue(EAST, EssentiaTubeConnections.canConnect(concreteLevel, pos, Direction.EAST))
                .setValue(UP, EssentiaTubeConnections.canConnect(concreteLevel, pos, Direction.UP))
                .setValue(DOWN, EssentiaTubeConnections.canConnect(concreteLevel, pos, Direction.DOWN));
    }

    public static BooleanProperty connectionProperty(Direction direction) {
        return switch (direction) {
            case NORTH -> NORTH;
            case SOUTH -> SOUTH;
            case WEST -> WEST;
            case EAST -> EAST;
            case UP -> UP;
            case DOWN -> DOWN;
        };
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter getter, BlockPos pos, net.minecraft.world.phys.shapes.CollisionContext context) {
        VoxelShape shape = CORE_SHAPE;

        if (state.getValue(NORTH)) shape = Shapes.joinUnoptimized(shape, NORTH_SHAPE, BooleanOp.OR);
        if (state.getValue(SOUTH)) shape = Shapes.joinUnoptimized(shape, SOUTH_SHAPE, BooleanOp.OR);
        if (state.getValue(WEST)) shape = Shapes.joinUnoptimized(shape, WEST_SHAPE, BooleanOp.OR);
        if (state.getValue(EAST)) shape = Shapes.joinUnoptimized(shape, EAST_SHAPE, BooleanOp.OR);
        if (state.getValue(UP)) shape = Shapes.joinUnoptimized(shape, UP_SHAPE, BooleanOp.OR);
        if (state.getValue(DOWN)) shape = Shapes.joinUnoptimized(shape, DOWN_SHAPE, BooleanOp.OR);

        return shape;
    }


    public String connectedSidesDiagnostic(Level level, BlockPos pos) {
        return "Connected sides: " + EssentiaTubeConnections.summary(level, pos);
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        EssentiaTubeBlockEntity tube = new EssentiaTubeBlockEntity(pos, state);
        tube.setSubtype(subtype);
        return tube;
    }

    public EssentiaTubeSubtype subtype() {
        return subtype;
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

        return createTickerHelper(type, ThaumcraftMod.ESSENTIA_TUBE_BLOCK_ENTITY.get(), EssentiaTubeBlockEntity::serverTick);
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player,
                                 InteractionHand hand, BlockHitResult hit) {
        BlockEntity blockEntity = level.getBlockEntity(pos);
        if (!(blockEntity instanceof EssentiaTubeBlockEntity tube)) {
            if (!level.isClientSide) {
                player.displayClientMessage(Component.literal("Essentia Tube | No tube data.").withStyle(ChatFormatting.RED), false);
            }
            return InteractionResult.sidedSuccess(level.isClientSide);
        }

        if (!level.isClientSide && (player.getItemInHand(hand).getItem() instanceof JarLabelItem
                || player.getItemInHand(hand).getItem() instanceof EssentiaPhialItem)) {
            JarTubeInteractionRuntime.applyFilterToTube(tube, player, player.getItemInHand(hand),
                    player.getItemInHand(hand == InteractionHand.MAIN_HAND ? InteractionHand.OFF_HAND : InteractionHand.MAIN_HAND));
            return InteractionResult.CONSUME;
        }

        if (!level.isClientSide && player.getItemInHand(hand).getItem() instanceof WandItem) {
            if (player.isShiftKeyDown()) {
                tube.cycleChoke(hit.getDirection());
            } else {
                tube.toggleSideWithNeighbour(hit.getDirection());
            }
            player.displayClientMessage(Component.literal("Essentia Tube | " + tube.subtype().originalClassName()
                    + " | side " + hit.getDirection().getName()
                    + " open=" + tube.isSideOpen(hit.getDirection())
                    + " choke=" + tube.chokeState(hit.getDirection()))
                    .withStyle(ChatFormatting.GOLD), false);
            return InteractionResult.CONSUME;
        }

        if (!level.isClientSide) {
            player.displayClientMessage(
                    Component.literal("Essentia Tube | " + tube.subtype().originalClassName()
                            + " | Network: " + tube.networkSize()
                            + " | Sources: " + tube.lastSourceCount()
                            + " | Destinations: " + tube.lastDestinationCount()
                            + " | Last: " + (tube.lastMovedAspect().isBlank() ? "none" : tube.lastMovedAspect())
                            + " | " + tube.connectedSidesDiagnostic(level, pos)
                            + " | Winning suction: " + tube.lastWinningSuction()
                            + " | Source pressure: " + tube.lastSourcePressure()
                            + " | Conflicts: " + tube.lastConflictCount()
                            + " | Backflow: " + (tube.lastBackflowBlocked() ? "blocked" : "clear")
                            + " | Filter: " + tube.aspectFilterId()
                            + " | Suction: jar 32, labelled 64, void 32, labelled void 48.").withStyle(ChatFormatting.AQUA),
                    false
            );
        }

        return InteractionResult.sidedSuccess(level.isClientSide);
    }
}
