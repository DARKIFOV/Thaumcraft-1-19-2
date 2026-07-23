package com.darkifov.thaumcraft.blockentity;

import com.darkifov.thaumcraft.Aspect;
import com.darkifov.thaumcraft.ThaumcraftMod;
import com.darkifov.thaumcraft.alchemy.TC4AlchemicalCentrifugeParity;
import com.darkifov.thaumcraft.porting.TC4Sounds;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

/** Exact TC4 TileCentrifuge lifecycle: one compound input, 39 processing ticks and one random component output. */
public class AlchemicalCentrifugeBlockEntity extends BlockEntity {
    public static final int ORIGINAL_INPUT_SUCTION = TC4AlchemicalCentrifugeParity.INPUT_SUCTION;
    public static final int ORIGINAL_PROCESS_TICKS = TC4AlchemicalCentrifugeParity.PROCESS_START;
    public static final int ORIGINAL_DRAW_INTERVAL_TICKS = TC4AlchemicalCentrifugeParity.DRAW_INTERVAL_TICKS;

    private Aspect aspectIn;
    private Aspect aspectOut;
    private Direction facing = Direction.NORTH;
    /** Original temporary fields: intentionally absent from persistent NBT. */
    private int process;
    private int counter;
    private float rotation;
    private float rotationSpeed;

    public AlchemicalCentrifugeBlockEntity(BlockPos pos, BlockState state) {
        super(ThaumcraftMod.ALCHEMICAL_CENTRIFUGE_BLOCK_ENTITY.get(), pos, state);
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, AlchemicalCentrifugeBlockEntity tile) {
        if (level == null || level.isClientSide) return;
        tile.counter++;
        boolean powered = tile.isPowered();
        if (!powered && tile.aspectOut == null && tile.aspectIn == null
                && tile.counter % ORIGINAL_DRAW_INTERVAL_TICKS == 0) {
            tile.drawEssentiaFromBelow();
        }
        if (tile.process > 0) tile.process = TC4AlchemicalCentrifugeParity.tickProcess(tile.process, powered);
        if (TC4AlchemicalCentrifugeParity.shouldProcess(tile.process, powered, tile.aspectIn != null, tile.aspectOut != null)) {
            tile.processEssentia();
        }
    }

    public static void clientTick(Level level, BlockPos pos, BlockState state, AlchemicalCentrifugeBlockEntity tile) {
        boolean spinning = tile.aspectIn != null && !tile.isPowered();
        tile.rotationSpeed = TC4AlchemicalCentrifugeParity.nextRotationSpeed(tile.rotationSpeed, spinning);
        float previous = tile.rotation;
        tile.rotation += tile.rotationSpeed;
        if (level != null && TC4AlchemicalCentrifugeParity.shouldPlayPump(previous, tile.rotation, tile.rotationSpeed)) {
            level.playLocalSound(pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D,
                    TC4Sounds.event("pump"), SoundSource.BLOCKS, 1.0F, 1.0F, false);
        }
    }

    public Aspect inputAspect() { return aspectIn; }
    public Aspect outputAspect() { return aspectOut; }
    public int process() { return process; }
    public int counter() { return counter; }
    public Direction facing() { return facing; }
    /** Original renderer uses the current client-tick rotation without partial interpolation. */
    public float rotation() { return rotation; }

    public boolean isPowered() { return level != null && level.hasNeighborSignal(worldPosition); }
    public boolean isConnectable(Direction face) { return face == Direction.UP || face == Direction.DOWN; }
    public boolean canInputFrom(Direction face) { return face == Direction.DOWN; }
    public boolean canOutputTo(Direction face) { return face == Direction.UP; }
    public int minimumSuction() { return 0; }
    public Aspect suctionType(Direction face) { return null; }
    public int suctionAmount(Direction face) { return face == Direction.DOWN ? ORIGINAL_INPUT_SUCTION : 0; }
    public int outputAmount(Direction face) { return canOutputTo(face) && aspectOut != null ? 1 : 0; }
    public Aspect outputType(Direction face) { return outputAmount(face) > 0 ? aspectOut : null; }

    public int takeOutput(Aspect aspect, int amount, Direction face) {
        if (!canOutputTo(face) || aspectOut == null || aspectOut != aspect || amount <= 0) return 0;
        aspectOut = null;
        setChangedAndSync();
        return 1;
    }

    public void restoreOutput(Aspect aspect, int amount) {
        if (aspectOut == null && aspect != null && amount > 0) {
            aspectOut = aspect;
            setChangedAndSync();
        }
    }

    public int addInput(Aspect aspect, int amount, Direction face) {
        if (!canInputFrom(face) || aspect == null || aspect.isPrimal() || amount <= 0 || aspectIn != null) return 0;
        aspectIn = aspect;
        process = ORIGINAL_PROCESS_TICKS;
        setChangedAndSync();
        return 1;
    }

    private void drawEssentiaFromBelow() {
        if (level == null) return;
        Source source = sourceBelow(level.getBlockEntity(worldPosition.below()));
        if (source == null || !source.canOutput() || source.amount() <= 0
                || source.suction() >= ORIGINAL_INPUT_SUCTION
                || ORIGINAL_INPUT_SUCTION < source.minimumSuction()) return;
        Aspect candidate = source.aspect();
        if (candidate == null || candidate.isPrimal()) return;
        if (source.take(candidate) == 1) {
            aspectIn = candidate;
            process = ORIGINAL_PROCESS_TICKS;
            setChangedAndSync();
        }
    }

    private Source sourceBelow(BlockEntity below) {
        if (below instanceof EssentiaTubeBlockEntity tube) {
            return new Source() {
                public boolean canOutput() { return tube.allowsOutputTo(Direction.UP); }
                public Aspect aspect() { return tube.getTransportEssentiaType(Direction.UP); }
                public int amount() { return tube.getTransportEssentiaAmount(Direction.UP); }
                public int suction() { return tube.getSuctionAmount(Direction.UP); }
                public int minimumSuction() { return tube.getMinimumSuction(); }
                public int take(Aspect aspect) { return tube.takeEssentiaOriginal(aspect, 1, Direction.UP); }
            };
        }
        if (below instanceof EssentiaJarBlockEntity jar) {
            boolean voidJar = below.getBlockState().is(ThaumcraftMod.VOID_ESSENTIA_JAR.get());
            return new Source() {
                public boolean canOutput() { return true; }
                public Aspect aspect() { return jar.storedAspect(); }
                public int amount() { return jar.amount(); }
                public int suction() { return jar.originalSuctionAmount(voidJar); }
                public int minimumSuction() { return jar.originalMinimumSuction(voidJar); }
                public int take(Aspect aspect) { return jar.takeFromContainerOriginal(aspect, 1) ? 1 : 0; }
            };
        }
        if (below instanceof AlembicBlockEntity alembic) {
            return new Source() {
                public boolean canOutput() { return alembic.canOutputTo(Direction.UP); }
                public Aspect aspect() { return alembic.storedAspect(); }
                public int amount() { return alembic.amount(); }
                public int suction() { return 0; }
                public int minimumSuction() { return 0; }
                public int take(Aspect aspect) { return alembic.removeEssentia(aspect, 1); }
            };
        }
        if (below instanceof AlchemicalCentrifugeBlockEntity centrifuge) {
            return new Source() {
                public boolean canOutput() { return centrifuge.canOutputTo(Direction.UP); }
                public Aspect aspect() { return centrifuge.outputType(Direction.UP); }
                public int amount() { return centrifuge.outputAmount(Direction.UP); }
                public int suction() { return centrifuge.suctionAmount(Direction.UP); }
                public int minimumSuction() { return centrifuge.minimumSuction(); }
                public int take(Aspect aspect) { return centrifuge.takeOutput(aspect, 1, Direction.UP); }
            };
        }
        return null;
    }

    private void processEssentia() {
        Aspect first = aspectIn == null ? null : aspectIn.firstComponent();
        Aspect second = aspectIn == null ? null : aspectIn.secondComponent();
        if (first == null || second == null) {
            aspectIn = null;
            process = 0;
            setChangedAndSync();
            return;
        }
        int selected = level == null ? 0 : level.random.nextInt(2);
        aspectOut = TC4AlchemicalCentrifugeParity.componentIndex(selected) == 0 ? first : second;
        aspectIn = null;
        process = 0;
        setChangedAndSync();
    }

    private void setChangedAndSync() {
        setChanged();
        if (level != null && !level.isClientSide) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), Block.UPDATE_CLIENTS);
        }
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        if (aspectIn != null) tag.putString("aspectIn", aspectIn.id());
        if (aspectOut != null) tag.putString("aspectOut", aspectOut.id());
        tag.putInt("facing", facing.get3DDataValue());
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        readOriginalNbt(tag);
        // These values were not serialized by TC4. A world reload restarts the transient cycle.
        process = 0;
        counter = 0;
        rotation = 0.0F;
        rotationSpeed = 0.0F;
    }

    private void readOriginalNbt(CompoundTag tag) {
        aspectIn = Aspect.byId(tag.getString("aspectIn"));
        aspectOut = Aspect.byId(tag.getString("aspectOut"));
        facing = tag.contains("facing") ? Direction.from3DDataValue(tag.getInt("facing")) : Direction.NORTH;
        if (facing == null) facing = Direction.NORTH;
    }

    @Override public CompoundTag getUpdateTag() { CompoundTag tag = new CompoundTag(); saveAdditional(tag); return tag; }
    @Override public void handleUpdateTag(CompoundTag tag) { readOriginalNbt(tag); }
    @Override public ClientboundBlockEntityDataPacket getUpdatePacket() { return ClientboundBlockEntityDataPacket.create(this); }
    @Override public void onDataPacket(Connection connection, ClientboundBlockEntityDataPacket packet) { readOriginalNbt(packet.getTag()); }

    private interface Source {
        boolean canOutput(); Aspect aspect(); int amount(); int suction(); int minimumSuction(); int take(Aspect aspect);
    }
}
