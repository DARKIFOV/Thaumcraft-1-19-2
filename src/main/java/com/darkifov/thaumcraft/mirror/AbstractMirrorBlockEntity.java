package com.darkifov.thaumcraft.mirror;

import com.darkifov.thaumcraft.block.MirrorBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

import javax.annotation.Nullable;

/** Shared reciprocal-link lifecycle from TC4 TileMirror and TileMirrorEssentia. */
public abstract class AbstractMirrorBlockEntity extends BlockEntity {
    private static final int INITIAL_RETRY = 40;
    private static final int MAX_RETRY = 600;

    @Nullable
    protected MirrorLink link;
    @Nullable
    private MirrorLink dropLinkSnapshot;
    protected boolean linked;
    private int tickCount;
    private int retryInterval = INITIAL_RETRY;

    protected AbstractMirrorBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    protected abstract boolean acceptsPeer(AbstractMirrorBlockEntity peer);

    public final boolean isLinked() {
        return linked;
    }

    @Nullable
    public final MirrorLink link() {
        return link;
    }

    /**
     * Link written to the Forge loot result. During removal Forge may call onRemove before
     * evaluating drops, so the live link must be snapshotted before the peer is invalidated.
     */
    @Nullable
    public final MirrorLink linkForDrop() {
        if (dropLinkSnapshot != null) {
            return dropLinkSnapshot;
        }
        return linked ? link : null;
    }

    public final void setPendingLink(@Nullable MirrorLink pending) {
        dropLinkSnapshot = null;
        link = pending;
        linked = false;
        retryInterval = INITIAL_RETRY;
        setChangedAndSync();
        if (pending != null) {
            restoreLink();
        }
    }

    public final boolean isLinkValidSimple() {
        if (!linked || link == null || !(level instanceof ServerLevel serverLevel)) {
            return false;
        }
        AbstractMirrorBlockEntity peer = loadedPeer(serverLevel, link);
        return peer != null && acceptsPeer(peer) && peer.linked && peer.link != null
                && peer.link.dimension().equals(serverLevel.dimension())
                && peer.link.pos().equals(worldPosition);
    }

    public final boolean isLinkValid() {
        boolean valid = isLinkValidSimple();
        if (!valid && linked) {
            linked = false;
            updateLinkedState();
            setChangedAndSync();
        }
        return valid;
    }

    public final boolean restoreLink() {
        if (link == null || !(level instanceof ServerLevel serverLevel)) {
            return false;
        }
        AbstractMirrorBlockEntity peer = loadedPeer(serverLevel, link);
        if (peer == null || !acceptsPeer(peer) || peer.isLinkValidSimple()) {
            return false;
        }

        MirrorLink reciprocal = MirrorLink.at(serverLevel, worldPosition);
        peer.link = reciprocal;
        peer.linked = true;
        peer.retryInterval = INITIAL_RETRY;
        peer.updateLinkedState();
        peer.setChangedAndSync();

        dropLinkSnapshot = null;
        linked = true;
        retryInterval = INITIAL_RETRY;
        updateLinkedState();
        setChangedAndSync();
        return true;
    }

    public final void invalidateLink() {
        // TC4 creates the NBT-aware block drop before invalidating the remote endpoint.
        // Forge 1.19.2 may run onRemove before loot evaluation, so retain a transient copy.
        dropLinkSnapshot = linked && link != null ? link : null;
        if (!(level instanceof ServerLevel serverLevel)) {
            linked = false;
            return;
        }
        if (link != null) {
            AbstractMirrorBlockEntity peer = loadedPeer(serverLevel, link);
            if (peer != null && acceptsPeer(peer) && peer.link != null
                    && peer.link.dimension().equals(serverLevel.dimension())
                    && peer.link.pos().equals(worldPosition)) {
                peer.linked = false;
                // Preserve the peer coordinates like TC4. It can automatically restore if a
                // compatible mirror reappears at the old endpoint, without force-loading chunks.
                peer.retryInterval = INITIAL_RETRY;
                peer.updateLinkedState();
                peer.setChangedAndSync();
            }
        }
        linked = false;
        retryInterval = INITIAL_RETRY;
        updateLinkedState();
        setChangedAndSync();
    }

    protected final void tickLinkLifecycle() {
        if (!(level instanceof ServerLevel)) {
            return;
        }
        if (tickCount++ % retryInterval != 0) {
            return;
        }
        if (!isLinkValidSimple()) {
            retryInterval = Math.min(MAX_RETRY, retryInterval + 20);
            restoreLink();
        } else {
            retryInterval = INITIAL_RETRY;
        }
    }

    @Nullable
    protected final AbstractMirrorBlockEntity loadedPeer(ServerLevel origin) {
        return link == null ? null : loadedPeer(origin, link);
    }

    @Nullable
    private static AbstractMirrorBlockEntity loadedPeer(ServerLevel origin, MirrorLink target) {
        ServerLevel targetLevel = target.resolveLevel(origin);
        if (targetLevel == null || !targetLevel.hasChunkAt(target.pos())) {
            return null;
        }
        BlockEntity blockEntity = targetLevel.getBlockEntity(target.pos());
        return blockEntity instanceof AbstractMirrorBlockEntity mirror ? mirror : null;
    }

    protected final void setChangedAndSync() {
        setChanged();
        if (level != null && !level.isClientSide) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), Block.UPDATE_CLIENTS);
        }
    }

    private void updateLinkedState() {
        if (level == null || !getBlockState().hasProperty(MirrorBlock.LINKED)
                || getBlockState().getValue(MirrorBlock.LINKED) == linked) {
            return;
        }
        level.setBlock(worldPosition, getBlockState().setValue(MirrorBlock.LINKED, linked), Block.UPDATE_CLIENTS);
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.putBoolean("linked", linked);
        if (link != null) {
            link.write(tag);
        }
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        linked = tag.getBoolean("linked");
        link = MirrorLink.read(tag);
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
