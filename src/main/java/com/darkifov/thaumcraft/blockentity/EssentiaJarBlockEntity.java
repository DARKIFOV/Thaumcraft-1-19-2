package com.darkifov.thaumcraft.blockentity;

import com.darkifov.thaumcraft.Aspect;
import com.darkifov.thaumcraft.AspectList;
import com.darkifov.thaumcraft.ThaumcraftMod;
import com.darkifov.thaumcraft.jar.TC4EssentiaJarParity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public class EssentiaJarBlockEntity extends BlockEntity {
    public static final int ORIGINAL_FILL_INTERVAL_TICKS = TC4EssentiaJarParity.FILL_INTERVAL_TICKS;

    private final AspectList aspects = new AspectList();
    private Aspect retainedAspect;
    private Aspect filterAspect;
    private Direction labelFacing = Direction.NORTH;

    public EssentiaJarBlockEntity(BlockPos pos, BlockState state) {
        super(ThaumcraftMod.ESSENTIA_JAR_BLOCK_ENTITY.get(), pos, state);
    }

    public AspectList aspects() {
        return aspects;
    }

    public Aspect filterAspect() {
        return filterAspect;
    }

    public boolean hasFilter() {
        return filterAspect != null;
    }

    public Direction labelFacing() {
        return labelFacing;
    }

    public void setLabelFacing(Direction direction) {
        if (direction != null && direction.getAxis().isHorizontal()) {
            labelFacing = direction;
            setChangedAndSync();
        }
    }

    public void setFilterAspect(Aspect aspect) {
        filterAspect = aspect;
        if (amount() <= 0 && aspect != null) {
            retainedAspect = aspect;
        }
        setChangedAndSync();
    }

    /** TC4 removes the label but does not forcibly erase the retained zero-amount aspect hint. */
    public void clearFilter() {
        filterAspect = null;
        setChangedAndSync();
    }

    public void clearContentsLikeTC4() {
        aspects.clear();
        if (filterAspect == null) {
            retainedAspect = null;
        } else {
            retainedAspect = filterAspect;
        }
        setChangedAndSync();
    }

    public boolean canAcceptAspect(Aspect aspect) {
        if (!doesContainerAcceptOriginal(aspect)) {
            return false;
        }
        Aspect current = amount() > 0 ? retainedAspect : null;
        return current == null || current == aspect;
    }

    public boolean doesContainerAcceptOriginal(Aspect aspect) {
        return aspect != null && (filterAspect == null || filterAspect == aspect);
    }

    /** TileJarFillable / TileJarFillableVoid addToContainer. Returns remainder. */
    public int addToContainerOriginal(Aspect aspect, int incoming, boolean voidJar) {
        if (incoming <= 0) return incoming;
        if (!doesContainerAcceptOriginal(aspect)) return incoming;

        boolean sameAspectOrEmpty = amount() == 0 || retainedAspect == aspect;
        int remainder = TC4EssentiaJarParity.remainderAfterInsert(voidJar, sameAspectOrEmpty, amount(), incoming);
        if (!sameAspectOrEmpty) return remainder;

        retainedAspect = aspect;
        int acceptedForDisplay = Math.min(Math.max(0, TC4EssentiaJarParity.CAPACITY - amount()), incoming);
        if (acceptedForDisplay > 0) {
            aspects.add(aspect, acceptedForDisplay);
        }
        if (acceptedForDisplay > 0 || (voidJar && remainder == 0)) {
            setChangedAndSync();
        }
        return remainder;
    }

    public boolean takeFromContainerOriginal(Aspect aspect, int requested) {
        if (aspect == null || requested <= 0 || retainedAspect != aspect || amount() < requested) {
            return false;
        }
        aspects.remove(aspect, requested);
        if (amount() <= 0) {
            aspects.clear();
            retainedAspect = filterAspect == null ? null : filterAspect;
        }
        setChangedAndSync();
        return true;
    }

    public int acceptFromTube(Aspect aspect, int incoming, boolean voidOverflow) {
        if (incoming <= 0 || !canAcceptAspect(aspect)) return 0;
        return incoming - addToContainerOriginal(aspect, incoming, voidOverflow);
    }

    public int originalMinimumSuction(boolean voidJar) {
        return TC4EssentiaJarParity.minimumSuction(voidJar, hasFilter());
    }

    public int originalSuctionAmount(boolean voidJar) {
        return TC4EssentiaJarParity.suctionAmount(voidJar, hasFilter(), amount());
    }

    public Aspect suctionType() {
        return filterAspect != null ? filterAspect : retainedAspect;
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, EssentiaJarBlockEntity jar) {
        boolean voidJar = state.is(ThaumcraftMod.VOID_ESSENTIA_JAR.get());
        if (level.isClientSide || level.getGameTime() % ORIGINAL_FILL_INTERVAL_TICKS != 0L
                || (!voidJar && jar.amount() >= jar.capacity())) {
            return;
        }
        jar.fillJarFromAboveLikeTC4(voidJar);
    }

    private void fillJarFromAboveLikeTC4(boolean voidJar) {
        if (level == null) return;
        BlockEntity above = level.getBlockEntity(worldPosition.above());
        if (!(above instanceof EssentiaTubeBlockEntity tube) || !tube.allowsOutputTo(Direction.DOWN)) return;

        Aspect target = null;
        if (filterAspect != null) {
            target = filterAspect;
        } else if (retainedAspect != null && amount() > 0) {
            target = retainedAspect;
        } else if (tube.getTransportEssentiaAmount(Direction.DOWN) > 0
                && tube.getSuctionAmount(Direction.DOWN) < originalSuctionAmount(voidJar)
                && originalSuctionAmount(voidJar) >= tube.getMinimumSuction()) {
            target = tube.getTransportEssentiaType(Direction.DOWN);
        }

        if (target != null && tube.getSuctionAmount(Direction.DOWN) < originalSuctionAmount(voidJar)) {
            int taken = tube.takeEssentiaOriginal(target, 1, Direction.DOWN);
            if (taken > 0) addToContainerOriginal(target, taken, voidJar);
        }
    }

    public Aspect storedAspect() {
        return retainedAspect;
    }

    public int amount() {
        return aspects.totalAmount();
    }

    public int capacity() {
        return TC4EssentiaJarParity.CAPACITY;
    }

    public float fillRatio() {
        return Math.max(0.0F, Math.min(1.0F, amount() / (float) capacity()));
    }

    public void setChangedAndSync() {
        setChanged();
        if (level != null && !level.isClientSide) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), Block.UPDATE_CLIENTS);
        }
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        if (retainedAspect != null) tag.putString("Aspect", retainedAspect.id());
        if (filterAspect != null) tag.putString("AspectFilter", filterAspect.id());
        tag.putShort("Amount", (short) amount());
        tag.putByte("facing", (byte) labelFacing.get3DDataValue());
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        aspects.clear();
        retainedAspect = null;
        filterAspect = null;
        labelFacing = Direction.NORTH;

        Aspect canonicalAspect = Aspect.byId(tag.getString("Aspect"));
        int canonicalAmount = Math.max(0, tag.getShort("Amount"));
        if (canonicalAspect != null) {
            retainedAspect = canonicalAspect;
            if (canonicalAmount > 0) aspects.add(canonicalAspect, Math.min(capacity(), canonicalAmount));
        } else if (tag.contains("Aspects", Tag.TAG_COMPOUND)) {
            // One-time migration from pre-11.64.35 port NBT.
            AspectList migrated = new AspectList();
            migrated.load(tag.getCompound("Aspects"));
            Aspect first = migrated.firstAspect();
            int amount = first == null ? 0 : Math.min(capacity(), migrated.get(first));
            if (first != null) {
                retainedAspect = first;
                if (amount > 0) aspects.add(first, amount);
            }
        }

        filterAspect = Aspect.byId(tag.getString("AspectFilter"));
        if (filterAspect == null) filterAspect = Aspect.byId(tag.getString("FilterAspect"));
        if (retainedAspect == null && filterAspect != null) retainedAspect = filterAspect;

        if (tag.contains("facing")) {
            Direction loaded = Direction.from3DDataValue(tag.getByte("facing"));
            if (loaded.getAxis().isHorizontal()) labelFacing = loaded;
        }
    }

    @Override
    public CompoundTag getUpdateTag() {
        CompoundTag tag = new CompoundTag();
        saveAdditional(tag);
        return tag;
    }

    @Override
    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public void onDataPacket(Connection connection, ClientboundBlockEntityDataPacket packet) {
        CompoundTag tag = packet.getTag();
        if (tag != null) load(tag);
    }
}
