package com.darkifov.thaumcraft.blockentity;

import com.darkifov.thaumcraft.Aspect;
import com.darkifov.thaumcraft.ThaumcraftMod;
import com.darkifov.thaumcraft.aura.AuraVisRelayNetwork;
import com.darkifov.thaumcraft.block.FumeDissipatorBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** Exact TC4 TileFluxScrubber gameplay adapter. */
public final class FumeDissipatorBlockEntity extends BlockEntity {
    public static final int FLUX_RADIUS = 16;
    public static final int FLUX_RADIUS_SQUARED = FLUX_RADIUS * FLUX_RADIUS;
    public static final int POSITIONS_PER_TICK = 16;
    public static final int VIS_REQUEST_CENTIVIS = 10;
    public static final int VIS_COST_PER_FLUX = 5;
    public static final int CHARGES_PER_CONVERSION = 4;
    public static final int ESSENTIA_CAPACITY = 4;

    private int essentia;
    private int charges;
    private int power;
    private int animationSeed;
    private final List<BlockPos> checklist = new ArrayList<>();

    public FumeDissipatorBlockEntity(BlockPos pos, BlockState state) {
        super(ThaumcraftMod.FUME_DISSIPATOR_BLOCK_ENTITY.get(), pos, state);
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, FumeDissipatorBlockEntity scrubber) {
        if (!(level instanceof ServerLevel serverLevel)) return;
        if (scrubber.animationSeed == 0) scrubber.animationSeed = serverLevel.random.nextInt(1000) + 1;

        if (scrubber.charges >= CHARGES_PER_CONVERSION) {
            scrubber.charges -= CHARGES_PER_CONVERSION;
            if (serverLevel.random.nextInt(4) == 0) {
                scrubber.essentia = Math.min(ESSENTIA_CAPACITY, scrubber.essentia + 1);
            }
            scrubber.setChangedAndSync();
        }

        if (scrubber.power < VIS_COST_PER_FLUX) {
            int drained = AuraVisRelayNetwork.drainMachineVis(serverLevel, pos, Aspect.AER, VIS_REQUEST_CENTIVIS);
            if (drained > 0) {
                scrubber.power += drained;
                scrubber.setChangedAndSync();
            }
        }
        if (scrubber.power >= VIS_COST_PER_FLUX) scrubber.checkFlux(serverLevel);
    }

    private void checkFlux(ServerLevel level) {
        if (checklist.isEmpty()) rebuildChecklist(level);
        int checked = 0;
        while (checked++ < POSITIONS_PER_TICK && !checklist.isEmpty()) {
            BlockPos target = checklist.remove(checklist.size() - 1);
            if (!level.hasChunkAt(target)) continue;
            if (target.distSqr(worldPosition) >= FLUX_RADIUS_SQUARED) continue;
            BlockState targetState = level.getBlockState(target);
            if (!isFlux(targetState)) continue;

            power = Math.max(0, power - VIS_COST_PER_FLUX);
            level.removeBlock(target, false);
            level.sendParticles(ParticleTypes.WITCH, target.getX() + 0.5D, target.getY() + 0.5D, target.getZ() + 0.5D,
                    8, 0.35D, 0.35D, 0.35D, 0.02D);
            charges++;
            setChangedAndSync();
            return;
        }
    }

    private void rebuildChecklist(ServerLevel level) {
        checklist.clear();
        BlockPos min = worldPosition.offset(-FLUX_RADIUS, -FLUX_RADIUS, -FLUX_RADIUS);
        BlockPos max = worldPosition.offset(FLUX_RADIUS, FLUX_RADIUS, FLUX_RADIUS);
        for (BlockPos mutable : BlockPos.betweenClosed(min, max)) checklist.add(mutable.immutable());
        Collections.shuffle(checklist, new java.util.Random(level.random.nextLong()));
    }

    private static boolean isFlux(BlockState state) {
        return state.is(ThaumcraftMod.FLUX_GOO.get()) || state.is(ThaumcraftMod.FLUX_GAS.get());
    }

    public Direction facing() {
        return getBlockState().hasProperty(FumeDissipatorBlock.FACING)
                ? getBlockState().getValue(FumeDissipatorBlock.FACING) : Direction.DOWN;
    }
    public boolean canOutputTo(Direction face) { return face == facing(); }
    public Aspect essentiaType(Direction face) { return canOutputTo(face) && essentia > 0 ? Aspect.PRAECANTATIO : null; }
    public int essentiaAmount(Direction face) { return canOutputTo(face) ? essentia : 0; }
    public int power() { return power; }
    public int charges() { return charges; }
    public int essentia() { return essentia; }
    public int animationSeed() { return animationSeed; }

    public int takeEssentia(Aspect aspect, int amount, Direction face) {
        if (aspect != Aspect.PRAECANTATIO || amount <= 0 || !canOutputTo(face)) return 0;
        int removed = Math.min(essentia, amount);
        if (removed > 0) { essentia -= removed; setChangedAndSync(); }
        return removed;
    }

    public void restoreEssentia(int amount) {
        if (amount <= 0) return;
        essentia = Math.min(ESSENTIA_CAPACITY, essentia + amount);
        setChangedAndSync();
    }

    /** GameTest hook: primes the exact post-cleanup counters without bypassing NBT rules. */
    public void setRuntimeStateForTest(int power, int charges, int essentia) {
        this.power = Math.max(0, power);
        this.charges = Math.max(0, charges);
        this.essentia = Math.max(0, Math.min(ESSENTIA_CAPACITY, essentia));
        setChangedAndSync();
    }

    private void setChangedAndSync() {
        setChanged();
        if (level != null && !level.isClientSide) level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), Block.UPDATE_CLIENTS);
    }

    @Override protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.putInt("charges", charges);
        tag.putInt("power", power);
        tag.putInt("essentia", essentia);
        tag.putInt("AnimationSeed", animationSeed);
        tag.putInt("facing", facing().get3DDataValue());
    }

    @Override public void load(CompoundTag tag) {
        super.load(tag);
        charges = Math.max(0, tag.getInt("charges"));
        power = Math.max(0, tag.getInt("power"));
        essentia = Math.max(0, Math.min(ESSENTIA_CAPACITY, tag.getInt("essentia")));
        animationSeed = tag.getInt("AnimationSeed");
    }

    @Override public CompoundTag getUpdateTag() { CompoundTag tag = new CompoundTag(); saveAdditional(tag); return tag; }
    @Override public ClientboundBlockEntityDataPacket getUpdatePacket() { return ClientboundBlockEntityDataPacket.create(this); }
    @Override public void onDataPacket(Connection connection, ClientboundBlockEntityDataPacket packet) { load(packet.getTag()); }
}
