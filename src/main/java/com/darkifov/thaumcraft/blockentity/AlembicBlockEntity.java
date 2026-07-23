package com.darkifov.thaumcraft.blockentity;

import com.darkifov.thaumcraft.Aspect;
import com.darkifov.thaumcraft.AspectList;
import com.darkifov.thaumcraft.ThaumcraftMod;
import com.darkifov.thaumcraft.alchemy.TC4AlchemicalFurnaceParity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

/** Exact TileAlembic storage contract: one aspect, 32 capacity, label filter and one blocked horizontal face. */
public class AlembicBlockEntity extends BlockEntity {
    public static final int CAPACITY = TC4AlchemicalFurnaceParity.ALEMBIC_CAPACITY;

    private final AspectList aspects = new AspectList();
    /** TC4 may retain a typed aspect at amount 0 after a typed label is applied. */
    private Aspect emptyAspectType;
    private Aspect aspectFilter;
    private Direction facing = Direction.NORTH;

    public AlembicBlockEntity(BlockPos pos, BlockState state) {
        super(ThaumcraftMod.ALEMBIC_BLOCK_ENTITY.get(), pos, state);
    }

    public AspectList aspects() { return aspects; }
    public Aspect storedAspect() { return aspects.firstAspect() != null ? aspects.firstAspect() : emptyAspectType; }
    public int amount() { Aspect a = aspects.firstAspect(); return a == null ? 0 : aspects.get(a); }
    public Aspect aspectFilter() { return aspectFilter; }
    public Direction facing() { return facing; }
    public int spaceLeft() { return Math.max(0, CAPACITY - amount()); }
    public boolean aboveFurnace() { return level != null && level.getBlockEntity(worldPosition.below()) instanceof AlchemicalFurnaceBlockEntity; }
    public boolean aboveAlembic() { return level != null && level.getBlockEntity(worldPosition.below()) instanceof AlembicBlockEntity; }

    public boolean canAccept(Aspect aspect) {
        if (aspect == null || spaceLeft() <= 0) return false;
        if (aspectFilter != null && aspectFilter != aspect) return false;
        Aspect first = storedAspect();
        return first == null || first == aspect || amount() == 0;
    }

    public boolean canOutputTo(Direction face) {
        return face != null && face != Direction.DOWN && face != facing;
    }

    public void setFacing(Direction direction) {
        if (direction != null && direction.getAxis().isHorizontal()) {
            facing = direction;
            setChangedAndSync();
        }
    }

    public boolean setAspectFilter(Aspect aspect) {
        if (aspect == null || aspectFilter != null) return false;
        if (amount() > 0 && storedAspect() != aspect) return false;
        if (amount() == 0) emptyAspectType = aspect;
        aspectFilter = aspect;
        setChangedAndSync();
        return true;
    }

    public Aspect clearAspectFilter() {
        Aspect old = aspectFilter;
        aspectFilter = null;
        setChangedAndSync();
        return old;
    }

    public int addEssentia(Aspect aspect, int requested) {
        if (aspect == null || requested <= 0 || !canAccept(aspect)) return 0;
        int added = Math.min(requested, spaceLeft());
        if (added > 0) {
            emptyAspectType = null;
            aspects.add(aspect, added);
            setChangedAndSync();
        }
        return added;
    }

    public int removeEssentia(Aspect aspect, int requested) {
        if (aspect == null || requested <= 0) return 0;
        int removed = aspects.removeUpTo(aspect, requested);
        if (removed > 0) {
            if (amount() <= 0) emptyAspectType = null;
            setChangedAndSync();
        }
        return removed;
    }

    public void clearEssentia() {
        aspects.clear();
        emptyAspectType = null;
        setChangedAndSync();
    }

    public int comparatorOutput() { return TC4AlchemicalFurnaceParity.comparator(amount()); }
    public int fillMessageIndex() { return TC4AlchemicalFurnaceParity.alembicFillMessage(amount()); }

    public void setChangedAndSync() {
        setChanged();
        if (level != null && !level.isClientSide) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), Block.UPDATE_CLIENTS);
        }
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        Aspect stored = storedAspect();
        if (stored != null) tag.putString("aspect", stored.id());
        if (aspectFilter != null) tag.putString("AspectFilter", aspectFilter.id());
        tag.putShort("amount", (short) amount());
        tag.putByte("facing", (byte) facing.get3DDataValue());
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        aspects.clear();
        emptyAspectType = null;
        Aspect stored = Aspect.byId(tag.contains("aspect") ? tag.getString("aspect") : tag.getString("Aspect"));
        int loadedAmount = Math.max(0, Math.min(CAPACITY,
                tag.contains("amount") ? tag.getShort("amount") : tag.getShort("Amount")));
        if (stored == null && tag.contains("Aspects", Tag.TAG_COMPOUND)) {
            AspectList legacy = new AspectList();
            legacy.load(tag.getCompound("Aspects"));
            stored = legacy.firstAspect();
            loadedAmount = stored == null ? 0 : Math.min(CAPACITY, legacy.get(stored));
        }
        if (stored != null && loadedAmount > 0) aspects.add(stored, loadedAmount);
        else emptyAspectType = stored;
        aspectFilter = Aspect.byId(tag.getString("AspectFilter"));
        if (tag.contains("facing")) {
            Direction loaded = Direction.from3DDataValue(tag.getByte("facing"));
            if (loaded.getAxis().isHorizontal()) facing = loaded;
        }
    }

    @Override public CompoundTag getUpdateTag() { CompoundTag tag = new CompoundTag(); saveAdditional(tag); return tag; }
    @Override public ClientboundBlockEntityDataPacket getUpdatePacket() { return ClientboundBlockEntityDataPacket.create(this); }
    @Override public void onDataPacket(Connection connection, ClientboundBlockEntityDataPacket packet) { load(packet.getTag()); }
}
