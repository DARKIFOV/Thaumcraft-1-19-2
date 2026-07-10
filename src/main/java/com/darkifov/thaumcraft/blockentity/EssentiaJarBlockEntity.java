package com.darkifov.thaumcraft.blockentity;

import com.darkifov.thaumcraft.Aspect;
import com.darkifov.thaumcraft.AspectList;
import com.darkifov.thaumcraft.ThaumcraftMod;
import com.darkifov.thaumcraft.block.EssentiaJarBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.Level;

public class EssentiaJarBlockEntity extends BlockEntity {
    private final AspectList aspects = new AspectList();
    private Aspect filterAspect = null;
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
        setChangedAndSync();
    }

    public void clearFilter() {
        filterAspect = null;
        setChangedAndSync();
    }

    public boolean canAcceptAspect(Aspect aspect) {
        return doesContainerAcceptOriginal(aspect) && (storedAspect() == null || storedAspect() == aspect);
    }

    /** Stage204: original TileJarFillable.doesContainerAccept parity. */
    public boolean doesContainerAcceptOriginal(Aspect aspect) {
        if (aspect == null) {
            return false;
        }
        return filterAspect == null || filterAspect == aspect;
    }

    /** Stage204: original TileJarFillable.addToContainer parity. Returns the remainder. */
    public int addToContainerOriginal(Aspect aspect, int amount, boolean voidJar) {
        if (amount <= 0) {
            return amount;
        }
        if (!doesContainerAcceptOriginal(aspect)) {
            return amount;
        }
        Aspect current = storedAspect();
        if (current != null && current != aspect) {
            return amount;
        }

        if (filterAspect == null && getBlockState().is(ThaumcraftMod.FILTERED_ESSENTIA_JAR.get())) {
            filterAspect = aspect;
        }

        int currentAmount = aspects.totalAmount();
        int space = Math.max(0, EssentiaJarBlock.CAPACITY - currentAmount);
        if (!voidJar && space <= 0) {
            return amount;
        }

        int stored = voidJar ? Math.min(space, amount) : Math.min(space, amount);
        if (stored > 0) {
            aspects.add(aspect, stored);
        }

        int remainder = voidJar ? 0 : amount - stored;
        if (stored > 0 || voidJar) {
            setChangedAndSync();
        }
        return remainder;
    }

    /** Stage204: original TileJarFillable.takeFromContainer parity. */
    public boolean takeFromContainerOriginal(Aspect aspect, int amount) {
        if (aspect == null || amount <= 0 || aspects.get(aspect) < amount) {
            return false;
        }
        aspects.remove(aspect, amount);
        if (aspects.totalAmount() <= 0) {
            aspects.clear();
        }
        setChangedAndSync();
        return true;
    }

    public int acceptFromTube(Aspect aspect, int amount, boolean voidOverflow) {
        if (!canAcceptAspect(aspect) || amount <= 0) {
            return 0;
        }
        int remainder = addToContainerOriginal(aspect, amount, voidOverflow);
        return Math.max(0, amount - remainder);
    }

    public int originalMinimumSuction(boolean voidJar) {
        if (voidJar) {
            return filterAspect != null ? 48 : 32;
        }
        return filterAspect != null ? 64 : 32;
    }

    public int originalSuctionAmount(boolean voidJar) {
        // v11.62.8 jar runtime parity: a void jar must keep suction even when
        // visually capped at 64 essentia. Earlier builds stopped pulling from
        // tubes once the display amount reached capacity, so a void jar behaved
        // like a normal full jar instead of consuming overflow like TC4.
        if (voidJar) {
            return filterAspect != null ? 48 : 32;
        }
        if (amount() < capacity()) {
            return filterAspect != null ? 64 : 32;
        }
        return 0;
    }



    /** v9.22: original TileJarFillable.fillJar lifecycle.
     * TC4 jars are active every 5 ticks and pull from the connectable transport above
     * when the jar suction is stronger than the neighbour suction. Earlier builds only
     * let the tube network push into jars, so isolated buffered-tube-over-jar setups
     * missed the original self-pull path. */
    public static void serverTick(Level level, BlockPos pos, BlockState state, EssentiaJarBlockEntity jar) {
        boolean voidJar = state.is(ThaumcraftMod.VOID_ESSENTIA_JAR.get());
        if (level.isClientSide || level.getGameTime() % 5L != 0L || (!voidJar && jar.amount() >= jar.capacity())) {
            return;
        }
        jar.fillJarFromAboveLikeTC4(voidJar);
    }

    private void fillJarFromAboveLikeTC4(boolean voidJar) {
        if (level == null) {
            return;
        }
        BlockEntity above = level.getBlockEntity(worldPosition.above());
        if (!(above instanceof EssentiaTubeBlockEntity tube) || !tube.allowsOutputTo(Direction.DOWN)) {
            return;
        }
        Aspect target = null;
        if (filterAspect != null) {
            target = filterAspect;
        } else if (storedAspect() != null && amount() > 0) {
            target = storedAspect();
        } else if (tube.getTransportEssentiaAmount(Direction.DOWN) > 0
                && tube.getSuctionAmount(Direction.DOWN) < originalSuctionAmount(voidJar)
                && originalSuctionAmount(voidJar) >= tube.getMinimumSuction()) {
            target = tube.getTransportEssentiaType(Direction.DOWN);
        }

        if (target != null && tube.getSuctionAmount(Direction.DOWN) < originalSuctionAmount(voidJar)) {
            int taken = tube.takeEssentiaOriginal(target, 1, Direction.DOWN);
            if (taken > 0) {
                addToContainerOriginal(target, taken, voidJar);
            }
        }
    }

    public Aspect storedAspect() {
        return aspects.firstAspect();
    }

    public int amount() {
        return aspects.totalAmount();
    }

    public int capacity() {
        return EssentiaJarBlock.CAPACITY;
    }

    public float fillRatio() {
        return Math.max(0.0F, Math.min(1.0F, aspects.totalAmount() / (float) EssentiaJarBlock.CAPACITY));
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
        tag.put("Aspects", aspects.save());

        Aspect stored = storedAspect();
        if (stored != null && amount() > 0) {
            tag.putString("Aspect", stored.id());
        }
        tag.putShort("Amount", (short) amount());
        tag.putByte("facing", (byte) labelFacing.get3DDataValue());

        if (filterAspect != null) {
            tag.putString("FilterAspect", filterAspect.name());
            tag.putString("AspectFilter", filterAspect.id());
        }
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);

        if (tag.contains("Aspects")) {
            aspects.load(tag.getCompound("Aspects"));
        }

        if (!tag.contains("Aspects") && tag.contains("Aspect")) {
            Aspect originalAspect = Aspect.byId(tag.getString("Aspect"));
            int originalAmount = Math.max(0, tag.getShort("Amount"));
            if (originalAspect != null && originalAmount > 0) {
                aspects.add(originalAspect, originalAmount);
            }
        }

        filterAspect = null;
        if (tag.contains("facing")) {
            try {
                Direction loadedFacing = Direction.from3DDataValue(tag.getByte("facing"));
                if (loadedFacing.getAxis().isHorizontal()) {
                    labelFacing = loadedFacing;
                }
            } catch (IllegalArgumentException ignored) {
                labelFacing = Direction.NORTH;
            }
        }

        if (tag.contains("FilterAspect")) {
            try {
                filterAspect = Aspect.valueOf(tag.getString("FilterAspect"));
            } catch (IllegalArgumentException ignored) {
                filterAspect = Aspect.byId(tag.getString("FilterAspect"));
            }
        }
        if (filterAspect == null && tag.contains("AspectFilter")) {
            filterAspect = Aspect.byId(tag.getString("AspectFilter"));
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
        load(packet.getTag());
    }
}
