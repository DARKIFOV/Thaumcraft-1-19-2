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
import com.darkifov.thaumcraft.essentia.TC4EssentiaTubeParity;
import com.darkifov.thaumcraft.ThaumcraftMod;
import com.darkifov.thaumcraft.blockentity.EssentiaTubeBlockEntity;
import net.minecraft.core.BlockPos;
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
import net.minecraft.world.item.ItemStack;
import net.minecraft.sounds.SoundSource;
import com.darkifov.thaumcraft.porting.TC4Sounds;


public class EssentiaTubeBlock extends BaseEntityBlock {
    private final EssentiaTubeSubtype subtype;
    public static final BooleanProperty NORTH = BooleanProperty.create("north");
    public static final BooleanProperty SOUTH = BooleanProperty.create("south");
    public static final BooleanProperty WEST = BooleanProperty.create("west");
    public static final BooleanProperty EAST = BooleanProperty.create("east");
    public static final BooleanProperty UP = BooleanProperty.create("up");
    public static final BooleanProperty DOWN = BooleanProperty.create("down");

    private static final VoxelShape CORE_SHAPE = Block.box(TC4EssentiaTubeParity.CORE_MIN * 16.0D, TC4EssentiaTubeParity.CORE_MIN * 16.0D, TC4EssentiaTubeParity.CORE_MIN * 16.0D, TC4EssentiaTubeParity.CORE_MAX * 16.0D, TC4EssentiaTubeParity.CORE_MAX * 16.0D, TC4EssentiaTubeParity.CORE_MAX * 16.0D);
    private static final VoxelShape NORTH_SHAPE = Block.box(6.72D, 6.72D, 0.0D, 9.28D, 9.28D, 8.0D);
    private static final VoxelShape SOUTH_SHAPE = Block.box(6.72D, 6.72D, 8.0D, 9.28D, 9.28D, 16.0D);
    private static final VoxelShape WEST_SHAPE = Block.box(0.0D, 6.72D, 6.72D, 8.0D, 9.28D, 9.28D);
    private static final VoxelShape EAST_SHAPE = Block.box(8.0D, 6.72D, 6.72D, 16.0D, 9.28D, 9.28D);
    private static final VoxelShape UP_SHAPE = Block.box(6.72D, 8.0D, 6.72D, 9.28D, 16.0D, 9.28D);
    private static final VoxelShape DOWN_SHAPE = Block.box(6.72D, 0.0D, 6.72D, 9.28D, 8.0D, 9.28D);

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

        BlockEntity blockEntity = concreteLevel.getBlockEntity(pos);
        EssentiaTubeBlockEntity tube = blockEntity instanceof EssentiaTubeBlockEntity existing ? existing : null;
        return state
                .setValue(NORTH, EssentiaTubeConnections.canConnect(concreteLevel, pos, Direction.NORTH) && (tube == null || tube.isSideOpen(Direction.NORTH)))
                .setValue(SOUTH, EssentiaTubeConnections.canConnect(concreteLevel, pos, Direction.SOUTH) && (tube == null || tube.isSideOpen(Direction.SOUTH)))
                .setValue(WEST, EssentiaTubeConnections.canConnect(concreteLevel, pos, Direction.WEST) && (tube == null || tube.isSideOpen(Direction.WEST)))
                .setValue(EAST, EssentiaTubeConnections.canConnect(concreteLevel, pos, Direction.EAST) && (tube == null || tube.isSideOpen(Direction.EAST)))
                .setValue(UP, EssentiaTubeConnections.canConnect(concreteLevel, pos, Direction.UP) && (tube == null || tube.isSideOpen(Direction.UP)))
                .setValue(DOWN, EssentiaTubeConnections.canConnect(concreteLevel, pos, Direction.DOWN) && (tube == null || tube.isSideOpen(Direction.DOWN)));
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

    private static boolean isCoreHit(BlockHitResult hit, BlockPos pos) {
        double lx = hit.getLocation().x - pos.getX();
        double ly = hit.getLocation().y - pos.getY();
        double lz = hit.getLocation().z - pos.getZ();
        // TC4 raytracer subHit == 6 is the central 0.34375..0.65625 core cuboid.
        return lx >= 0.34375D && lx <= 0.65625D
                && ly >= 0.34375D && ly <= 0.65625D
                && lz >= 0.34375D && lz <= 0.65625D;
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return level.isClientSide
                ? createTickerHelper(type, ThaumcraftMod.ESSENTIA_TUBE_BLOCK_ENTITY.get(), EssentiaTubeBlockEntity::clientTick)
                : createTickerHelper(type, ThaumcraftMod.ESSENTIA_TUBE_BLOCK_ENTITY.get(), EssentiaTubeBlockEntity::serverTick);
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player,
                                 InteractionHand hand, BlockHitResult hit) {
        if (hand != InteractionHand.MAIN_HAND) {
            return InteractionResult.PASS;
        }
        BlockEntity blockEntity = level.getBlockEntity(pos);
        if (!(blockEntity instanceof EssentiaTubeBlockEntity tube)) {
            return InteractionResult.PASS;
        }

        ItemStack held = player.getItemInHand(hand);

        // Original BlockTube metadata 3: shift removes an installed filter regardless of held item.
        if (tube.subtype().usesAspectFilter() && player.isShiftKeyDown() && tube.aspectFilter() != null) {
            if (!level.isClientSide) {
                tube.setAspectFilter(null);
                popResource(level, pos.relative(hit.getDirection()), new ItemStack(ThaumcraftMod.JAR_LABEL.get()));
                level.playSound(null, pos, TC4Sounds.event("page"), SoundSource.BLOCKS, 1.0F, 1.0F);
            }
            return InteractionResult.sidedSuccess(level.isClientSide);
        }

        // Original filtered tube accepts only a typed jar label, never a phial.
        if (tube.subtype().usesAspectFilter() && tube.aspectFilter() == null
                && held.getItem() instanceof JarLabelItem) {
            var aspect = JarLabelItem.getAspect(held);
            if (aspect != null && !level.isClientSide) {
                tube.setAspectFilter(aspect);
                if (!player.getAbilities().instabuild) {
                    held.shrink(1);
                }
                level.playSound(null, pos, TC4Sounds.event("page"), SoundSource.BLOCKS, 1.0F, 1.0F);
            }
            return InteractionResult.sidedSuccess(level.isClientSide);
        }

        if (held.getItem() instanceof WandItem) {
            if (!level.isClientSide) {
                if (isCoreHit(hit, pos) && !tube.subtype().storesBufferEssentia()) {
                    tube.cycleFacingCoreLikeTC4();
                } else if (tube.subtype().storesBufferEssentia() && player.isShiftKeyDown()) {
                    tube.cycleChoke(hit.getDirection());
                    level.playSound(null, pos, TC4Sounds.event("squeek"), SoundSource.BLOCKS, 0.6F,
                            1.1F + level.random.nextFloat() * 0.2F);
                } else {
                    tube.toggleSideWithNeighbour(hit.getDirection());
                    level.playSound(null, pos, TC4Sounds.event("tool"), SoundSource.BLOCKS, 0.5F,
                            0.9F + level.random.nextFloat() * 0.2F);
                }
            }
            return InteractionResult.sidedSuccess(level.isClientSide);
        }

        return InteractionResult.PASS;
    }

    @Override
    public boolean hasAnalogOutputSignal(BlockState state) {
        return true;
    }

    @Override
    public int getAnalogOutputSignal(BlockState state, Level level, BlockPos pos) {
        BlockEntity blockEntity = level.getBlockEntity(pos);
        if (blockEntity instanceof EssentiaTubeBlockEntity tube && tube.subtype().storesBufferEssentia()) {
            int amount = tube.bufferAmount();
            return TC4EssentiaTubeParity.bufferComparator(amount);
        }
        return 0;
    }

    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean moving) {
        if (!state.is(newState.getBlock())) {
            BlockEntity blockEntity = level.getBlockEntity(pos);
            if (!level.isClientSide && blockEntity instanceof EssentiaTubeBlockEntity tube
                    && tube.subtype().usesAspectFilter() && tube.aspectFilter() != null) {
                popResource(level, pos, new ItemStack(ThaumcraftMod.JAR_LABEL.get()));
            }
        }
        super.onRemove(state, level, pos, newState, moving);
    }

}
