package com.darkifov.thaumcraft.mirror;

import com.darkifov.thaumcraft.Aspect;
import com.darkifov.thaumcraft.ThaumcraftMod;
import com.darkifov.thaumcraft.block.MirrorBlock;
import com.darkifov.thaumcraft.blockentity.AlchemicalCentrifugeBlockEntity;
import com.darkifov.thaumcraft.blockentity.AlchemicalFurnaceBlockEntity;
import com.darkifov.thaumcraft.blockentity.AlembicBlockEntity;
import com.darkifov.thaumcraft.blockentity.EssentiaJarBlockEntity;
import com.darkifov.thaumcraft.blockentity.EssentiaReservoirBlockEntity;
import com.darkifov.thaumcraft.blockentity.EssentiaTubeBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import javax.annotation.Nullable;

/** Runtime port of TC4 TileMirrorEssentia's source-only remote drain. */
public final class EssentiaMirrorBlockEntity extends AbstractMirrorBlockEntity {
    public static final int RANGE = 8;
    @Nullable
    private RemoteSource lastDrainSource;

    public EssentiaMirrorBlockEntity(BlockPos pos, BlockState state) {
        super(ThaumcraftMod.ESSENTIA_MIRROR_BLOCK_ENTITY.get(), pos, state);
    }

    @Override
    protected boolean acceptsPeer(AbstractMirrorBlockEntity peer) {
        return peer instanceof EssentiaMirrorBlockEntity;
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, EssentiaMirrorBlockEntity mirror) {
        mirror.tickLinkLifecycle();
    }

    @Nullable
    public Aspect peekRemoteAspect() {
        RemoteSource source = findRemoteSource(null);
        return source == null ? null : source.aspect();
    }

    /** TC4 only accepted a one-point request from an essentia mirror. */
    public int takeRemoteEssentia(Aspect aspect, int amount) {
        if (aspect == null || amount != 1) {
            return 0;
        }
        RemoteSource source = findRemoteSource(aspect);
        if (source == null) {
            return 0;
        }
        int removed = source.remove(1);
        if (removed > 0) {
            lastDrainSource = source;
        }
        return removed;
    }

    /** Rollback hook used when the tube network selected a destination that became unavailable. */
    public void restoreRemoteEssentia(Aspect aspect, int amount) {
        if (amount <= 0 || aspect == null) {
            return;
        }
        RemoteSource source = lastDrainSource;
        lastDrainSource = null;
        if (source != null && source.aspect() == aspect && source.restore(amount)) {
            return;
        }
        RemoteSource fallback = findRemoteSource(aspect);
        if (fallback != null) {
            fallback.restore(amount);
        }
    }

    @Nullable
    private RemoteSource findRemoteSource(@Nullable Aspect required) {
        if (!(level instanceof ServerLevel origin) || !isLinkValid() || link == null) {
            return null;
        }
        ServerLevel targetLevel = link.resolveLevel(origin);
        if (targetLevel == null || !targetLevel.hasChunkAt(link.pos())) {
            return null;
        }
        BlockEntity target = targetLevel.getBlockEntity(link.pos());
        if (!(target instanceof EssentiaMirrorBlockEntity)) {
            return null;
        }
        BlockState targetState = targetLevel.getBlockState(link.pos());
        Direction facing = targetState.hasProperty(MirrorBlock.FACING)
                ? targetState.getValue(MirrorBlock.FACING) : Direction.UP;

        // Exact EssentiaHandler.getSources ordering: aa, bb, then the forward cc half-space [0, range).
        for (int aa = -RANGE; aa <= RANGE; aa++) {
            for (int bb = -RANGE; bb <= RANGE; bb++) {
                for (int cc = 0; cc < RANGE; cc++) {
                    if (aa == 0 && bb == 0 && cc == 0) {
                        continue;
                    }
                    BlockPos sourcePos = orientedOffset(link.pos(), facing, aa, bb, cc);
                    if (!targetLevel.hasChunkAt(sourcePos)) {
                        continue;
                    }
                    BlockEntity candidate = targetLevel.getBlockEntity(sourcePos);
                    if (candidate instanceof EssentiaMirrorBlockEntity) {
                        continue;
                    }
                    RemoteSource source = sourceFrom(candidate);
                    if (source != null && (required == null || source.aspect() == required)) {
                        return source;
                    }
                }
            }
        }
        return null;
    }

    private static BlockPos orientedOffset(BlockPos origin, Direction direction, int aa, int bb, int cc) {
        if (direction.getStepY() != 0) {
            return origin.offset(aa, cc * direction.getStepY(), bb);
        }
        if (direction.getStepX() == 0) {
            return origin.offset(aa, bb, cc * direction.getStepZ());
        }
        return origin.offset(cc * direction.getStepX(), aa, bb);
    }

    @Nullable
    private static RemoteSource sourceFrom(@Nullable BlockEntity blockEntity) {
        if (blockEntity instanceof EssentiaJarBlockEntity jar) {
            Aspect aspect = jar.storedAspect();
            return aspect != null && jar.amount() > 0 ? new JarSource(jar, aspect) : null;
        }
        if (blockEntity instanceof EssentiaReservoirBlockEntity reservoir) {
            Aspect aspect = reservoir.firstAspect();
            return aspect != null && reservoir.amount() > 0 ? new ReservoirSource(reservoir, aspect) : null;
        }
        if (blockEntity instanceof AlembicBlockEntity alembic) {
            Aspect aspect = alembic.storedAspect();
            return aspect != null ? new AlembicSource(alembic, aspect) : null;
        }
        if (blockEntity instanceof EssentiaTubeBlockEntity tube && tube.bufferAmount() > 0) {
            Aspect aspect = tube.bufferAspect();
            return aspect != null ? new BufferSource(tube, aspect) : null;
        }
        if (blockEntity instanceof AlchemicalFurnaceBlockEntity furnace && furnace.isAdvanced()) {
            Aspect aspect = furnace.firstAspect();
            return aspect != null ? new FurnaceSource(furnace, aspect) : null;
        }
        if (blockEntity instanceof AlchemicalCentrifugeBlockEntity centrifuge) {
            Aspect aspect = centrifuge.outputAspect();
            return aspect != null ? new CentrifugeSource(centrifuge, aspect) : null;
        }
        return null;
    }

    private interface RemoteSource {
        Aspect aspect();
        int remove(int amount);
        boolean restore(int amount);
    }

    private record JarSource(EssentiaJarBlockEntity jar, Aspect aspect) implements RemoteSource {
        @Override public int remove(int amount) { return jar.takeFromContainerOriginal(aspect, amount) ? amount : 0; }
        @Override public boolean restore(int amount) { return jar.acceptFromTube(aspect, amount, false) == amount; }
    }

    private record ReservoirSource(EssentiaReservoirBlockEntity reservoir, Aspect aspect) implements RemoteSource {
        @Override public int remove(int amount) { return reservoir.removeEssentia(aspect, amount); }
        @Override public boolean restore(int amount) { return reservoir.acceptFromTube(aspect, amount) == amount; }
    }

    private record AlembicSource(AlembicBlockEntity alembic, Aspect aspect) implements RemoteSource {
        @Override public int remove(int amount) { return alembic.removeEssentia(aspect, amount); }
        @Override public boolean restore(int amount) { return alembic.addEssentia(aspect, amount) == amount; }
    }

    private record BufferSource(EssentiaTubeBlockEntity tube, Aspect aspect) implements RemoteSource {
        @Override public int remove(int amount) { return tube.drainBufferForNetwork(aspect, amount); }
        @Override public boolean restore(int amount) { tube.restoreBufferForNetwork(aspect, amount); return true; }
    }

    private record FurnaceSource(AlchemicalFurnaceBlockEntity furnace, Aspect aspect) implements RemoteSource {
        @Override public int remove(int amount) { return furnace.removeUpTo(aspect, amount); }
        @Override public boolean restore(int amount) { furnace.restoreAdvancedOutput(aspect, amount); return true; }
    }

    private record CentrifugeSource(AlchemicalCentrifugeBlockEntity centrifuge, Aspect aspect) implements RemoteSource {
        @Override public int remove(int amount) {
            for (Direction direction : Direction.values()) {
                int taken = centrifuge.takeOutput(aspect, amount, direction);
                if (taken > 0) return taken;
            }
            return 0;
        }
        @Override public boolean restore(int amount) { centrifuge.restoreOutput(aspect, amount); return true; }
    }
}
