package com.darkifov.thaumcraft.blockentity;

import com.darkifov.thaumcraft.Aspect;
import com.darkifov.thaumcraft.AspectList;
import com.darkifov.thaumcraft.ThaumcraftMod;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

/** Direct TileAlembic parity: one aspect, 32 capacity, optional label filter and a wand-selected blocked face. */
public class AlembicBlockEntity extends BlockEntity {
    public static final int CAPACITY = 32;

    private final AspectList aspects = new AspectList();
    private Aspect aspectFilter;
    private Direction facing = Direction.NORTH;

    public AlembicBlockEntity(BlockPos pos, BlockState state) {
        super(ThaumcraftMod.ALEMBIC_BLOCK_ENTITY.get(), pos, state);
    }

    public AspectList aspects() {
        return aspects;
    }

    public Aspect storedAspect() {
        return aspects.firstAspect();
    }

    public Aspect aspectFilter() {
        return aspectFilter;
    }

    public Direction facing() {
        return facing;
    }

    public int spaceLeft() {
        return Math.max(0, CAPACITY - aspects.totalAmount());
    }

    public boolean canAccept(Aspect aspect) {
        if (aspect == null || spaceLeft() <= 0) {
            return false;
        }
        if (aspectFilter != null && aspectFilter != aspect) {
            return false;
        }
        Aspect first = aspects.firstAspect();
        return first == null || first == aspect;
    }

    public boolean canOutputTo(Direction face) {
        return face != null && face != Direction.DOWN && face != facing;
    }

    public void setFacing(Direction facing) {
        if (facing != null && facing.getAxis().isHorizontal()) {
            this.facing = facing;
            setChangedAndSync();
        }
    }

    public boolean setAspectFilter(Aspect aspect) {
        if (aspect == null) {
            return false;
        }
        if (!aspects.isEmpty() && storedAspect() != aspect) {
            return false;
        }
        this.aspectFilter = aspect;
        setChangedAndSync();
        return true;
    }

    public Aspect clearAspectFilter() {
        Aspect old = aspectFilter;
        aspectFilter = null;
        setChangedAndSync();
        return old;
    }

    public int addEssentia(Aspect aspect, int amount) {
        if (aspect == null || amount <= 0 || !canAccept(aspect)) {
            return 0;
        }
        int added = Math.min(amount, spaceLeft());
        if (added > 0) {
            aspects.add(aspect, added);
            setChangedAndSync();
        }
        return added;
    }

    public int removeEssentia(Aspect aspect, int amount) {
        int removed = aspects.removeUpTo(aspect, amount);
        if (removed > 0) {
            setChangedAndSync();
        }
        return removed;
    }

    public void clearEssentia() {
        if (!aspects.isEmpty()) {
            aspects.clear();
            setChangedAndSync();
        }
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
        Aspect stored = aspects.firstAspect();
        if (stored != null && aspects.totalAmount() > 0) {
            tag.putString("Aspect", stored.id());
            tag.putShort("Amount", (short) aspects.totalAmount());
        }
        if (aspectFilter != null) {
            tag.putString("AspectFilter", aspectFilter.id());
        }
        tag.putByte("facing", (byte) facing.get3DDataValue());
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        aspects.clear();
        if (tag.contains("Aspects")) {
            aspects.load(tag.getCompound("Aspects"));
        } else if (tag.contains("Aspect")) {
            Aspect originalAspect = Aspect.byId(tag.getString("Aspect"));
            int originalAmount = Math.max(0, tag.getShort("Amount"));
            if (originalAspect != null && originalAmount > 0) {
                aspects.add(originalAspect, Math.min(CAPACITY, originalAmount));
            }
        }
        aspectFilter = tag.contains("AspectFilter") ? Aspect.byId(tag.getString("AspectFilter")) : null;
        if (tag.contains("facing")) {
            Direction loaded = Direction.from3DDataValue(tag.getByte("facing"));
            if (loaded.getAxis().isHorizontal()) {
                facing = loaded;
            }
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
