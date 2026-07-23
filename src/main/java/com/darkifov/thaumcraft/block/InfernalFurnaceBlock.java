package com.darkifov.thaumcraft.block;

import com.darkifov.thaumcraft.ThaumcraftMod;
import com.darkifov.thaumcraft.blockentity.InfernalFurnaceBlockEntity;
import com.darkifov.thaumcraft.blockentity.InfernalFurnaceNozzleBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.Blaze;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

import javax.annotation.Nullable;
import net.minecraft.util.RandomSource;

/** TC4 BlockArcaneFurnace multiblock shell, core and essentia nozzle. */
public final class InfernalFurnaceBlock extends BaseEntityBlock {
    public static final IntegerProperty PART = IntegerProperty.create("part", 0, 10);
    public static final EnumProperty<InfernalFurnaceLayer> LAYER = EnumProperty.create("layer", InfernalFurnaceLayer.class);
    public static final DirectionProperty FACING = DirectionProperty.create("facing", Direction.Plane.HORIZONTAL);
    private static final VoxelShape CORE_SHAPE = Block.box(0.0D, 0.0D, 0.0D, 16.0D, 4.0D, 16.0D);

    public InfernalFurnaceBlock(Properties properties) {
        super(properties);
        registerDefaultState(stateDefinition.any()
                .setValue(PART, 1)
                .setValue(LAYER, InfernalFurnaceLayer.LOWER)
                .setValue(FACING, Direction.NORTH));
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        int part = state.getValue(PART);
        if (part == 0) return new InfernalFurnaceBlockEntity(pos, state);
        if (part == 10) return new InfernalFurnaceNozzleBlockEntity(pos, state);
        return null;
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state,
                                                                  BlockEntityType<T> type) {
        int part = state.getValue(PART);
        if (part == 0) {
            return createTickerHelper(type, ThaumcraftMod.INFERNAL_FURNACE_BLOCK_ENTITY.get(),
                    level.isClientSide ? InfernalFurnaceBlockEntity::clientTick : InfernalFurnaceBlockEntity::serverTick);
        }
        if (part == 10) {
            return createTickerHelper(type, ThaumcraftMod.INFERNAL_FURNACE_NOZZLE_BLOCK_ENTITY.get(),
                    InfernalFurnaceNozzleBlockEntity::serverTick);
        }
        return null;
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        int part = state.getValue(PART);
        if (part == 0) return CORE_SHAPE;
        if (part == 10) return nozzleShape(state.getValue(FACING));
        return Shapes.block();
    }

    @Override
    public VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return getShape(state, level, pos, context);
    }

    private static VoxelShape nozzleShape(Direction outward) {
        return switch (outward) {
            case EAST -> Block.box(0, 0, 0, 8, 16, 16);
            case WEST -> Block.box(8, 0, 0, 16, 16, 16);
            case SOUTH -> Block.box(0, 0, 0, 16, 16, 8);
            case NORTH -> Block.box(0, 0, 8, 16, 16, 16);
            default -> Shapes.block();
        };
    }

    @Override
    public boolean isSignalSource(BlockState state) {
        return false;
    }

    @Override
    public void entityInside(BlockState state, Level level, BlockPos pos, Entity entity) {
        if (state.getValue(PART) != 0) return;
        double dx = entity.getX() - (pos.getX() + 0.5D);
        double dz = entity.getZ() - (pos.getZ() + 0.5D);
        entity.setDeltaMovement(entity.getDeltaMovement().add(
                dx < -0.2D ? 0.0001D : dx > 0.2D ? -0.0001D : 0.0D,
                0.0D,
                dz < -0.2D ? 0.0001D : dz > 0.2D ? -0.0001D : 0.0D));
        if (entity instanceof ItemEntity item) {
            item.setDeltaMovement(item.getDeltaMovement().x, TC4InfernalFurnaceParity.ITEM_BOUNCE_Y,
                    item.getDeltaMovement().z);
            if (!level.isClientSide && item.isOnGround()
                    && level.getBlockEntity(pos) instanceof InfernalFurnaceBlockEntity furnace
                    && furnace.addItemsToInventory(item.getItem().copy())) {
                item.discard();
            }
        } else if (!level.isClientSide && entity instanceof LivingEntity living && !living.fireImmune()) {
            living.hurt(DamageSource.LAVA, TC4InfernalFurnaceParity.LIVING_DAMAGE);
            living.setSecondsOnFire(TC4InfernalFurnaceParity.LIVING_FIRE_SECONDS);
        }
    }

    @Override
    public void neighborChanged(BlockState state, Level level, BlockPos pos, Block block, BlockPos sourcePos,
                                boolean moving) {
        super.neighborChanged(state, level, pos, block, sourcePos, moving);
        if (level.isClientSide || InfernalFurnaceMultiblock.isRestoring()) return;
        BlockPos core = state.getValue(PART) == 0 ? pos : InfernalFurnaceMultiblock.findCore(level, pos);
        if (core != null && !structureIntact(level, core)) {
            InfernalFurnaceMultiblock.dismantle((ServerLevel) level, core, true);
        }
    }

    private static boolean structureIntact(Level level, BlockPos core) {
        for (int y = -1; y <= 1; y++) {
            for (int x = -1; x <= 1; x++) {
                for (int z = -1; z <= 1; z++) {
                    if (y == 1 && x == 0 && z == 0) continue; // original top-center opening
                    if (!level.getBlockState(core.offset(x, y, z)).is(ThaumcraftMod.INFERNAL_FURNACE.get())) {
                        return false;
                    }
                }
            }
        }
        return true;
    }

    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean moving) {
        if (!state.is(newState.getBlock()) && !level.isClientSide && !InfernalFurnaceMultiblock.isRestoring()) {
            BlockPos core = state.getValue(PART) == 0 ? pos : InfernalFurnaceMultiblock.findCore(level, pos);
            if (core != null) InfernalFurnaceMultiblock.dismantle((ServerLevel) level, core, true);
        }
        super.onRemove(state, level, pos, newState, moving);
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        if (state.getValue(PART) != 0) return;
        BlockState above = level.getBlockState(pos.above());
        if (!above.isAir() && above.canOcclude()) return;
        for (int i = 0; i < 3; i++) {
            level.addParticle(ParticleTypes.LARGE_SMOKE,
                    pos.getX() + random.nextFloat(), pos.getY() + 1.0D + random.nextFloat() * 0.5D,
                    pos.getZ() + random.nextFloat(), 0.0D, 0.0D, 0.0D);
        }
    }

    public static void spawnPunishmentBlaze(ServerLevel level, BlockPos core) {
        Blaze blaze = new Blaze(net.minecraft.world.entity.EntityType.BLAZE, level);
        blaze.moveTo(core.getX() + 0.5D, core.getY() + 1.0D, core.getZ() + 0.5D, 0.0F, 0.0F);
        blaze.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 6000, 2));
        blaze.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 12000, 0));
        level.addFreshEntity(blaze);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(PART, LAYER, FACING);
    }
}
