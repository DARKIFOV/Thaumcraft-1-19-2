package com.darkifov.thaumcraft.block;

import com.darkifov.thaumcraft.blockentity.WardedBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.SpawnPlacements;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.PushReaction;
import net.minecraftforge.common.IPlantable;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * TC4 BlockWarded wrapper.  The real block is stored in WardedBlockEntity and
 * remains visible/collidable while its ticking and destruction are frozen.
 */
public class WardedBlock extends BaseEntityBlock {
    public WardedBlock(Properties properties) {
        super(properties);
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new WardedBlockEntity(pos, state);
    }

    public static BlockState remembered(BlockGetter level, BlockPos pos) {
        BlockEntity blockEntity = level.getBlockEntity(pos);
        return blockEntity instanceof WardedBlockEntity warded
                ? warded.rememberedState()
                : net.minecraft.world.level.block.Blocks.STONE.defaultBlockState();
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.INVISIBLE;
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        BlockState original = remembered(level, pos);
        return original.getShape(level, pos, context);
    }

    @Override
    public VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        BlockState original = remembered(level, pos);
        return original.getCollisionShape(level, pos, context);
    }

    @Override
    public VoxelShape getOcclusionShape(BlockState state, BlockGetter level, BlockPos pos) {
        BlockState original = remembered(level, pos);
        return original.getOcclusionShape(level, pos);
    }

    @Override
    public VoxelShape getVisualShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        BlockState original = remembered(level, pos);
        return original.getVisualShape(level, pos, context);
    }

    @Override
    public boolean propagatesSkylightDown(BlockState state, BlockGetter level, BlockPos pos) {
        return remembered(level, pos).propagatesSkylightDown(level, pos);
    }

    @Override
    public float getShadeBrightness(BlockState state, BlockGetter level, BlockPos pos) {
        return remembered(level, pos).getShadeBrightness(level, pos);
    }

    @Override
    public int getLightEmission(BlockState state, BlockGetter level, BlockPos pos) {
        return remembered(level, pos).getLightEmission(level, pos);
    }


    @Override
    public float getFriction(BlockState state, LevelReader level, BlockPos pos, Entity entity) {
        return remembered(level, pos).getFriction(level, pos, entity);
    }

    @Override
    public SoundType getSoundType(BlockState state, LevelReader level, BlockPos pos, Entity entity) {
        return remembered(level, pos).getSoundType(level, pos, entity);
    }

    @Override
    public boolean isLadder(BlockState state, LevelReader level, BlockPos pos, LivingEntity entity) {
        return remembered(level, pos).isLadder(level, pos, entity);
    }

    @Override
    public boolean canSustainPlant(BlockState state, BlockGetter level, BlockPos pos, Direction facing, IPlantable plantable) {
        return remembered(level, pos).canSustainPlant(level, pos, facing, plantable);
    }

    @Override
    public boolean isFertile(BlockState state, BlockGetter level, BlockPos pos) {
        return remembered(level, pos).isFertile(level, pos);
    }

    @Override
    public float getEnchantPowerBonus(BlockState state, LevelReader level, BlockPos pos) {
        return remembered(level, pos).getEnchantPowerBonus(level, pos);
    }

    @Override
    public boolean isValidSpawn(BlockState state, BlockGetter level, BlockPos pos,
                                SpawnPlacements.Type type, EntityType<?> entityType) {
        return false;
    }

    @Override
    public boolean canDropFromExplosion(BlockState state, BlockGetter level, BlockPos pos, Explosion explosion) {
        return false;
    }

    @Override
    public void onBlockExploded(BlockState state, Level level, BlockPos pos, Explosion explosion) {
        // TC4 wards are explosion-proof; the stored state must remain untouched.
    }

    @Override
    public boolean isSignalSource(BlockState state) {
        // The original state lives in the block entity, so this broad answer lets
        // vanilla query getSignal/getDirectSignal with world context below.
        return true;
    }

    @Override
    public int getSignal(BlockState state, BlockGetter level, BlockPos pos, Direction direction) {
        BlockState original = remembered(level, pos);
        return original.getSignal(level, pos, direction);
    }

    @Override
    public int getDirectSignal(BlockState state, BlockGetter level, BlockPos pos, Direction direction) {
        BlockState original = remembered(level, pos);
        return original.getDirectSignal(level, pos, direction);
    }

    @Override
    public boolean canConnectRedstone(BlockState state, BlockGetter level, BlockPos pos, Direction direction) {
        return remembered(level, pos).canRedstoneConnectTo(level, pos, direction);
    }

    @Override
    public boolean hasAnalogOutputSignal(BlockState state) {
        return true;
    }

    @Override
    public int getAnalogOutputSignal(BlockState state, Level level, BlockPos pos) {
        BlockState original = remembered(level, pos);
        return original.hasAnalogOutputSignal() ? original.getAnalogOutputSignal(level, pos) : 0;
    }

    @Override
    public ItemStack getCloneItemStack(BlockState state, HitResult target, BlockGetter level,
                                       BlockPos pos, Player player) {
        return remembered(level, pos).getCloneItemStack(target, level, pos, player);
    }

    @Override
    public PushReaction getPistonPushReaction(BlockState state) {
        return PushReaction.BLOCK;
    }

    @Override
    public boolean canEntityDestroy(BlockState state, BlockGetter level, BlockPos pos, Entity entity) {
        return false;
    }

    @Override
    public void stepOn(Level level, BlockPos pos, BlockState state, Entity entity) {
        BlockState original = remembered(level, pos);
        original.getBlock().stepOn(level, pos, original, entity);
    }

    @Override
    public void fallOn(Level level, BlockState state, BlockPos pos, Entity entity, float fallDistance) {
        BlockState original = remembered(level, pos);
        original.getBlock().fallOn(level, original, pos, entity, fallDistance);
    }

    @Override
    public void updateEntityAfterFallOn(BlockGetter level, Entity entity) {
        // The original block owns fall behaviour, but this callback has no position.
        super.updateEntityAfterFallOn(level, entity);
    }

    @Override
    public void attack(BlockState state, Level level, BlockPos pos, Player player) {
        // Intentionally do not forward block breaking; Focus Warding is the only removal path.
    }
}
