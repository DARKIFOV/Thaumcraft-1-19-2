package com.darkifov.thaumcraft.entity;

import com.darkifov.thaumcraft.ThaumcraftMod;
import com.darkifov.thaumcraft.porting.TC4Sounds;
import com.darkifov.thaumcraft.taint.TaintSpreadRuntime;
import com.mojang.math.Vector3f;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.network.NetworkHooks;

/**
 * Forge 1.19.2 adapter for TC4 {@code EntityFallingTaint}.
 *
 * <p>Unlike vanilla {@code FallingBlockEntity}, TC4 keeps the original source
 * coordinate until the first server tick. This matters for lateral crust
 * collapse: the visual entity starts in the neighbouring air column while the
 * source crust is removed atomically only after the entity is alive. Landing
 * restores the taint block only when the target is replaceable and the block
 * below is no longer a valid falling path.</p>
 */
public final class FallingTaintEntity extends Entity {
    private static final EntityDataAccessor<Integer> DATA_BLOCK_STATE =
            SynchedEntityData.defineId(FallingTaintEntity.class, EntityDataSerializers.INT);

    private BlockPos sourcePos = BlockPos.ZERO;
    private int fallTime;

    public FallingTaintEntity(EntityType<? extends FallingTaintEntity> type, Level level) {
        super(type, level);
        blocksBuilding = true;
    }

    public FallingTaintEntity(Level level, BlockPos visualPos, BlockState state, BlockPos sourcePos) {
        this(ThaumcraftMod.FALLING_TAINT.get(), level);
        setBlockState(state);
        this.sourcePos = sourcePos.immutable();
        setPos(visualPos.getX() + 0.5D, visualPos.getY(), visualPos.getZ() + 0.5D);
        setDeltaMovement(Vec3.ZERO);
        xo = getX();
        yo = getY();
        zo = getZ();
    }

    @Override
    protected void defineSynchedData() {
        entityData.define(DATA_BLOCK_STATE, Block.getId(Blocks.AIR.defaultBlockState()));
    }

    public BlockState getBlockState() {
        BlockState state = Block.stateById(entityData.get(DATA_BLOCK_STATE));
        return state == null ? Blocks.AIR.defaultBlockState() : state;
    }

    public void setBlockState(BlockState state) {
        entityData.set(DATA_BLOCK_STATE, Block.getId(state));
    }

    public int getFallTime() {
        return fallTime;
    }

    public BlockPos getSourcePos() {
        return sourcePos;
    }

    @Override
    public void tick() {
        BlockState carried = getBlockState();
        if (carried.isAir()) {
            discard();
            return;
        }

        super.tick();
        fallTime++;

        if (!level.isClientSide && fallTime == 1) {
            BlockState source = level.getBlockState(sourcePos);
            if (!source.is(carried.getBlock())) {
                discard();
                return;
            }
            level.removeBlock(sourcePos, false);
        }

        setDeltaMovement(getDeltaMovement().add(0.0D, -0.04D, 0.0D));
        move(MoverType.SELF, getDeltaMovement());
        setDeltaMovement(getDeltaMovement().multiply(0.98D, 0.98D, 0.98D));

        if (level.isClientSide) {
            if (fallTime == 1 || isOnGround()) {
                spawnTaintLandingParticles();
            }
            return;
        }

        BlockPos landingPos = blockPosition();
        boolean thickFluxGoo = level.getBlockState(landingPos.below()).is(ThaumcraftMod.FLUX_GOO.get());
        if (isOnGround() || thickFluxGoo) {
            Vec3 velocity = getDeltaMovement();
            setDeltaMovement(velocity.x * 0.7D, velocity.y * -0.5D, velocity.z * 0.7D);

            BlockState landingState = level.getBlockState(landingPos);
            if (!isFireOrLava(landingState)) {
                level.playSound(null, landingPos, TC4Sounds.event("gore"), SoundSource.BLOCKS,
                        0.5F, (random.nextFloat() - random.nextFloat()) * 0.16F + 0.8F);
                if (canPlaceAt(landingPos) && !TaintSpreadRuntime.canTaintFallBelow(level, landingPos.below())) {
                    level.setBlock(landingPos, carried, 3);
                    if (level instanceof net.minecraft.server.level.ServerLevel server) {
                        TaintSpreadRuntime.markTaintedColumn(server, landingPos);
                    }
                }
                discard();
            }
        } else if (((fallTime > 100) && (landingPos.getY() < level.getMinBuildHeight()
                || landingPos.getY() > level.getMaxBuildHeight())) || fallTime > 600) {
            discard();
        }
    }

    private boolean canPlaceAt(BlockPos pos) {
        BlockState state = level.getBlockState(pos);
        return state.is(ThaumcraftMod.TAINT_FIBRES.get())
                || state.is(ThaumcraftMod.FLUX_GOO.get())
                || state.isAir()
                || state.getMaterial().isReplaceable();
    }

    private static boolean isFireOrLava(BlockState state) {
        return state.is(Blocks.FIRE) || state.is(Blocks.SOUL_FIRE)
                || state.getFluidState().is(FluidTags.LAVA);
    }

    private void spawnTaintLandingParticles() {
        DustParticleOptions purple = new DustParticleOptions(new Vector3f(0.3F, 0.1F, 0.8F), 0.9F);
        for (int i = 0; i < 10; i++) {
            level.addParticle(purple,
                    getX() + (random.nextDouble() - 0.5D) * getBbWidth(),
                    getY() + random.nextDouble() * getBbHeight(),
                    getZ() + (random.nextDouble() - 0.5D) * getBbWidth(),
                    (random.nextDouble() - 0.5D) * 0.06D,
                    random.nextDouble() * 0.04D,
                    (random.nextDouble() - 0.5D) * 0.06D);
        }
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag tag) {
        tag.putInt("BlockState", entityData.get(DATA_BLOCK_STATE));
        tag.putInt("Time", fallTime);
        tag.putInt("OldX", sourcePos.getX());
        tag.putInt("OldY", sourcePos.getY());
        tag.putInt("OldZ", sourcePos.getZ());
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag tag) {
        entityData.set(DATA_BLOCK_STATE, tag.getInt("BlockState"));
        fallTime = tag.getInt("Time");
        sourcePos = new BlockPos(tag.getInt("OldX"), tag.getInt("OldY"), tag.getInt("OldZ"));
        if (getBlockState().isAir()) {
            setBlockState(ThaumcraftMod.TAINT_CRUST.get().defaultBlockState());
        }
    }

    @Override
    public boolean isPickable() {
        return !isRemoved();
    }

    @Override
    public boolean isAttackable() {
        return false;
    }

    @Override
    public boolean displayFireAnimation() {
        return false;
    }

    @Override
    public Packet<?> getAddEntityPacket() {
        return NetworkHooks.getEntitySpawningPacket(this);
    }
}
