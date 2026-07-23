package com.darkifov.thaumcraft.block;

import com.darkifov.thaumcraft.ThaumcraftMod;
import com.darkifov.thaumcraft.blockentity.ArcanePressurePlateBlockEntity;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.material.PushReaction;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

import javax.annotation.Nullable;

/** TC4 warded Arcane Pressure Plate with owner-aware trigger modes and key access. */
public final class ArcanePressurePlateBlock extends BaseEntityBlock {
    public static final BooleanProperty POWERED = BlockStateProperties.POWERED;
    public static final IntegerProperty SETTING = IntegerProperty.create("setting", 0,
            TC4ArcanePressurePlateParity.MAX_SETTING);

    private static final VoxelShape UNPRESSED_SHAPE = Block.box(
            TC4ArcanePressurePlateParity.OUTLINE_MIN_XZ * 16.0D, 0.0D,
            TC4ArcanePressurePlateParity.OUTLINE_MIN_XZ * 16.0D,
            TC4ArcanePressurePlateParity.OUTLINE_MAX_XZ * 16.0D,
            TC4ArcanePressurePlateParity.OUTLINE_UNPRESSED_HEIGHT * 16.0D,
            TC4ArcanePressurePlateParity.OUTLINE_MAX_XZ * 16.0D);
    private static final VoxelShape PRESSED_SHAPE = Block.box(
            TC4ArcanePressurePlateParity.OUTLINE_MIN_XZ * 16.0D, 0.0D,
            TC4ArcanePressurePlateParity.OUTLINE_MIN_XZ * 16.0D,
            TC4ArcanePressurePlateParity.OUTLINE_MAX_XZ * 16.0D,
            TC4ArcanePressurePlateParity.OUTLINE_PRESSED_HEIGHT * 16.0D,
            TC4ArcanePressurePlateParity.OUTLINE_MAX_XZ * 16.0D);
    private static final AABB TOUCH_AABB = new AABB(
            TC4ArcanePressurePlateParity.SCAN_MIN_XZ, 0.0D,
            TC4ArcanePressurePlateParity.SCAN_MIN_XZ,
            TC4ArcanePressurePlateParity.SCAN_MAX_XZ,
            TC4ArcanePressurePlateParity.SCAN_MAX_Y,
            TC4ArcanePressurePlateParity.SCAN_MAX_XZ);

    public ArcanePressurePlateBlock(Properties properties) {
        super(properties);
        registerDefaultState(stateDefinition.any().setValue(POWERED, false).setValue(SETTING, 0));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(POWERED, SETTING);
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new ArcanePressurePlateBlockEntity(pos, state);
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return defaultBlockState();
    }

    @Override
    public void setPlacedBy(Level level, BlockPos pos, BlockState state,
                            @Nullable LivingEntity placer, ItemStack stack) {
        super.setPlacedBy(level, pos, state, placer, stack);
        if (placer instanceof Player player
                && level.getBlockEntity(pos) instanceof ArcanePressurePlateBlockEntity plate) {
            plate.initializeOwner(player);
        }
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player,
                                 InteractionHand hand, BlockHitResult hit) {
        if (!(level.getBlockEntity(pos) instanceof ArcanePressurePlateBlockEntity plate)) {
            return InteractionResult.PASS;
        }
        if (level.isClientSide) return InteractionResult.SUCCESS;
        plate.initializeOwner(player);
        int setting = plate.cycleSetting(player);
        if (setting < 0) {
            player.displayClientMessage(Component.translatable("message.thaumcraft.arcane_pressure_plate.protected")
                    .withStyle(ChatFormatting.BLUE), true);
            return InteractionResult.CONSUME;
        }
        level.playSound(null, pos, SoundEvents.WOODEN_PRESSURE_PLATE_CLICK_ON,
                SoundSource.BLOCKS, 0.1F, 0.9F);
        player.displayClientMessage(Component.translatable(
                "message.thaumcraft.arcane_pressure_plate.setting_" + setting)
                .withStyle(ChatFormatting.DARK_PURPLE, ChatFormatting.ITALIC), false);
        updatePlate(level, pos, level.getBlockState(pos));
        return InteractionResult.CONSUME;
    }

    @Override
    public void entityInside(BlockState state, Level level, BlockPos pos, Entity entity) {
        if (!level.isClientSide && !state.getValue(POWERED) && !entity.isIgnoringBlockTriggers()) {
            updatePlate(level, pos, state);
        }
    }

    @Override
    public void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        if (state.getValue(POWERED)) updatePlate(level, pos, state);
    }

    private void updatePlate(Level level, BlockPos pos, BlockState state) {
        if (!(level.getBlockEntity(pos) instanceof ArcanePressurePlateBlockEntity plate)) return;
        boolean shouldPower = !level.getEntitiesOfClass(Entity.class, TOUCH_AABB.move(pos),
                plate::shouldTrigger).isEmpty();
        boolean powered = state.getValue(POWERED);
        if (shouldPower != powered) {
            BlockState updated = state.setValue(POWERED, shouldPower).setValue(SETTING, plate.setting());
            level.setBlock(pos, updated, Block.UPDATE_CLIENTS);
            level.updateNeighborsAt(pos, this);
            level.updateNeighborsAt(pos.below(), this);
            level.playSound(null, pos,
                    shouldPower ? SoundEvents.WOODEN_PRESSURE_PLATE_CLICK_ON
                            : SoundEvents.WOODEN_PRESSURE_PLATE_CLICK_OFF,
                    SoundSource.BLOCKS, 0.2F, shouldPower ? 0.6F : 0.5F);
        }
        if (shouldPower) level.scheduleTick(pos, this,
                TC4ArcanePressurePlateParity.CHECK_INTERVAL_TICKS);
    }

    /** Original warded removal path: only the owner may reclaim the block with a wand. */
    public static boolean removeWithOwnerWand(Level level, BlockPos pos, Player player) {
        BlockState state = level.getBlockState(pos);
        if (!(state.getBlock() instanceof ArcanePressurePlateBlock)
                || state.getValue(POWERED)
                || !(level.getBlockEntity(pos) instanceof ArcanePressurePlateBlockEntity plate)
                || !plate.isOwner(player)) {
            return false;
        }
        if (!level.isClientSide) {
            Block.popResource(level, pos, new ItemStack(ThaumcraftMod.ARCANE_PRESSURE_PLATE_ITEM.get()));
            level.levelEvent(player, 2001, pos, Block.getId(state));
            level.removeBlock(pos, false);
        }
        return true;
    }

    @Override
    public boolean isSignalSource(BlockState state) {
        return true;
    }

    @Override
    public int getSignal(BlockState state, BlockGetter level, BlockPos pos, Direction side) {
        return TC4ArcanePressurePlateParity.weakSignal(state.getValue(POWERED));
    }

    @Override
    public int getDirectSignal(BlockState state, BlockGetter level, BlockPos pos, Direction side) {
        return TC4ArcanePressurePlateParity.strongSignal(state.getValue(POWERED), side == Direction.UP);
    }

    @Override
    public boolean canConnectRedstone(BlockState state, BlockGetter level, BlockPos pos,
                                      @Nullable Direction direction) {
        return true;
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos,
                               CollisionContext context) {
        return state.getValue(POWERED) ? PRESSED_SHAPE : UNPRESSED_SHAPE;
    }

    @Override
    public VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos,
                                        CollisionContext context) {
        return Shapes.empty();
    }

    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean moving) {
        if (!state.is(newState.getBlock()) && state.getValue(POWERED)) {
            level.updateNeighborsAt(pos, this);
            level.updateNeighborsAt(pos.below(), this);
        }
        super.onRemove(state, level, pos, newState, moving);
    }

    @Override
    public boolean canDropFromExplosion(BlockState state, BlockGetter level, BlockPos pos, Explosion explosion) {
        return false;
    }

    @Override
    public void onBlockExploded(BlockState state, Level level, BlockPos pos, Explosion explosion) {
        // Original warded pressure plate ignores explosions entirely.
    }

    @Override
    public PushReaction getPistonPushReaction(BlockState state) {
        return PushReaction.BLOCK;
    }

    @Override
    public boolean canEntityDestroy(BlockState state, BlockGetter level, BlockPos pos, Entity entity) {
        return false;
    }
}
