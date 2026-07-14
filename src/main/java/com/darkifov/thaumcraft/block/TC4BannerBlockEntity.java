package com.darkifov.thaumcraft.block;

import com.darkifov.thaumcraft.Aspect;
import com.darkifov.thaumcraft.ThaumcraftMod;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

/** Persistent TC4 banner colour/aspect payload. Placement orientation lives in blockstate. */
public final class TC4BannerBlockEntity extends BlockEntity {
    public static final String TAG_COLOR = "color";
    public static final String TAG_ASPECT = "aspect";

    private int color = -1;
    private Aspect aspect;

    public TC4BannerBlockEntity(BlockPos pos, BlockState state) {
        super(ThaumcraftMod.TC4_BANNER_BLOCK_ENTITY.get(), pos, state);
    }

    public int color() {
        return color;
    }

    public void setColor(int color) {
        this.color = color >= 0 && color < 16 ? color : -1;
        sync();
    }

    public Aspect aspect() {
        return aspect;
    }

    public void setAspect(Aspect aspect) {
        this.aspect = aspect;
        sync();
    }

    private void sync() {
        setChanged();
        if (level != null) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
        }
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.putInt(TAG_COLOR, color);
        tag.putString(TAG_ASPECT, aspect == null ? "" : aspect.id());
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        color = tag.contains(TAG_COLOR) ? tag.getInt(TAG_COLOR) : -1;
        if (color < -1 || color > 15) {
            color = -1;
        }
        aspect = Aspect.byId(tag.getString(TAG_ASPECT));
    }

    @Override
    public CompoundTag getUpdateTag() {
        return saveWithoutMetadata();
    }

    @Override
    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public void onDataPacket(Connection net, ClientboundBlockEntityDataPacket packet) {
        CompoundTag tag = packet.getTag();
        if (tag != null) {
            load(tag);
        }
    }

}
