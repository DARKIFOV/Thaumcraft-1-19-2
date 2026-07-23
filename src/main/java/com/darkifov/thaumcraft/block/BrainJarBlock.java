package com.darkifov.thaumcraft.block;

import com.darkifov.thaumcraft.ThaumcraftMod;
import com.darkifov.thaumcraft.blockentity.BrainJarBlockEntity;
import com.darkifov.thaumcraft.blockentity.TC4BrainJarParity;
import com.darkifov.thaumcraft.porting.TC4Sounds;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

/** Forge 1.19.2 port of TC4 BlockJar metadata 1 / TileJarBrain. */
public final class BrainJarBlock extends BaseEntityBlock {
    private static final VoxelShape SHAPE = Block.box(3.0D, 0.0D, 3.0D, 13.0D, 12.0D, 13.0D);

    public BrainJarBlock(Properties properties) {
        super(properties);
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new BrainJarBlockEntity(pos, state);
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Override
    public VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        // BlockJar.addCollisionBoxesToList temporarily restored full-block bounds in TC4.
        return Shapes.block();
    }

    @Override
    public SoundType getSoundType(BlockState state, LevelReader level, BlockPos pos, Entity entity) {
        SoundEvent jar = TC4Sounds.event("jar");
        // TC4 CustomStepSound overrides the break/place sound to thaumcraft:jar.
        return new SoundType(1.0F, 1.0F, jar, jar, jar, jar, jar);
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        if (level.getBlockEntity(pos) instanceof BrainJarBlockEntity brain
                && brain.storedExperience() >= TC4BrainJarParity.MAX_XP) {
            double x = pos.getX() + TC4BrainJarParity.FULL_PARTICLE_XZ_MIN
                    + random.nextFloat() * TC4BrainJarParity.FULL_PARTICLE_XZ_RANGE;
            double y = pos.getY() + TC4BrainJarParity.FULL_PARTICLE_Y;
            double z = pos.getZ() + TC4BrainJarParity.FULL_PARTICLE_XZ_MIN
                    + random.nextFloat() * TC4BrainJarParity.FULL_PARTICLE_XZ_RANGE;
            level.addParticle(ThaumcraftMod.BRAIN_JAR_FULL_PARTICLE.get(), x, y, z,
                    TC4BrainJarParity.FULL_PARTICLE_RED,
                    TC4BrainJarParity.fullParticleGreen(random.nextFloat()),
                    TC4BrainJarParity.fullParticleBlue(random.nextFloat()));
        }
    }

    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return createTickerHelper(type, ThaumcraftMod.BRAIN_JAR_BLOCK_ENTITY.get(),
                level.isClientSide ? BrainJarBlockEntity::clientTick : BrainJarBlockEntity::serverTick);
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player,
                                 InteractionHand hand, BlockHitResult hit) {
        if (!(level.getBlockEntity(pos) instanceof BrainJarBlockEntity brain)) {
            return InteractionResult.PASS;
        }
        if (!level.isClientSide && level instanceof ServerLevel serverLevel) {
            brain.releaseRandomExperience(serverLevel);
        } else if (level.isClientSide) {
            brain.playJarSound();
        }
        return InteractionResult.sidedSuccess(level.isClientSide);
    }

    @Override
    public void onRemove(BlockState oldState, Level level, BlockPos pos, BlockState newState, boolean moving) {
        if (!oldState.is(newState.getBlock())) {
            if (!level.isClientSide && level instanceof ServerLevel serverLevel
                    && level.getBlockEntity(pos) instanceof BrainJarBlockEntity brain) {
                brain.releaseAllExperience(serverLevel);
            }
            level.updateNeighbourForOutputSignal(pos, this);
        }
        super.onRemove(oldState, level, pos, newState, moving);
    }

    @Override
    public boolean hasAnalogOutputSignal(BlockState state) {
        return true;
    }

    @Override
    public int getAnalogOutputSignal(BlockState state, Level level, BlockPos pos) {
        return level.getBlockEntity(pos) instanceof BrainJarBlockEntity brain ? brain.comparatorOutput() : 0;
    }

    @Override
    public float getEnchantPowerBonus(BlockState state, LevelReader level, BlockPos pos) {
        return TC4BrainJarParity.ENCHANT_POWER_BONUS;
    }
}
