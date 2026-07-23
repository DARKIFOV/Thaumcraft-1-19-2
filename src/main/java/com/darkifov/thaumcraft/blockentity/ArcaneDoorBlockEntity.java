package com.darkifov.thaumcraft.blockentity;

import com.darkifov.thaumcraft.ThaumcraftMod;
import com.darkifov.thaumcraft.block.ArcaneDoorBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import java.util.LinkedHashSet;
import java.util.Set;
import java.util.UUID;

/** Persistent UUID owner/access list for the two-block TC4 Arcane Door. */
public final class ArcaneDoorBlockEntity extends BlockEntity implements ArcaneAccessTarget {
    private static final UUID NIL_UUID = new UUID(0L, 0L);

    private UUID owner = NIL_UUID;
    private final Set<UUID> standardAccess = new LinkedHashSet<>();
    private final Set<UUID> fullAccess = new LinkedHashSet<>();

    public ArcaneDoorBlockEntity(BlockPos pos, BlockState state) {
        super(ThaumcraftMod.ARCANE_DOOR_BLOCK_ENTITY.get(), pos, state);
    }

    private ArcaneDoorBlockEntity master() {
        if (level == null || !getBlockState().hasProperty(ArcaneDoorBlock.UPPER)
                || !getBlockState().getValue(ArcaneDoorBlock.UPPER)) {
            return this;
        }
        BlockEntity lower = level.getBlockEntity(worldPosition.below());
        return lower instanceof ArcaneDoorBlockEntity door ? door : this;
    }

    @Override
    public BlockPos keyBindingPos() {
        ArcaneDoorBlockEntity master = master();
        return master == this ? worldPosition : master.worldPosition;
    }

    public void initializeOwner(Player player) {
        ArcaneDoorBlockEntity master = master();
        if (master != this) {
            master.initializeOwner(player);
            return;
        }
        if (player == null || !owner.equals(NIL_UUID)) return;
        owner = player.getUUID();
        setChangedAndSync();
    }

    @Override
    public UUID owner() {
        ArcaneDoorBlockEntity master = master();
        return master == this ? owner : master.owner();
    }

    @Override
    public boolean isOwner(Player player) {
        return player != null && owner().equals(player.getUUID());
    }

    @Override
    public boolean hasAccess(Player player) {
        if (player == null) return false;
        ArcaneDoorBlockEntity master = master();
        if (master != this) return master.hasAccess(player);
        UUID id = player.getUUID();
        return owner.equals(id) || standardAccess.contains(id) || fullAccess.contains(id);
    }

    @Override
    public boolean hasFullAccess(Player player) {
        if (player == null) return false;
        ArcaneDoorBlockEntity master = master();
        if (master != this) return master.hasFullAccess(player);
        UUID id = player.getUUID();
        return owner.equals(id) || fullAccess.contains(id);
    }

    @Override
    public boolean mayBindKey(Player player, boolean goldKey) {
        return isOwner(player) || (!goldKey && hasFullAccess(player));
    }

    @Override
    public boolean addAccess(Player player, boolean full) {
        ArcaneDoorBlockEntity master = master();
        if (master != this) return master.addAccess(player, full);
        if (player == null || isOwner(player)) return false;
        UUID id = player.getUUID();
        boolean changed;
        if (full) {
            standardAccess.remove(id);
            changed = fullAccess.add(id);
        } else if (fullAccess.contains(id)) {
            changed = false;
        } else {
            changed = standardAccess.add(id);
        }
        if (changed) setChangedAndSync();
        return changed;
    }

    @Override
    public Set<UUID> authorizedUsers() {
        ArcaneDoorBlockEntity master = master();
        if (master != this) return master.authorizedUsers();
        Set<UUID> result = new LinkedHashSet<>();
        if (!owner.equals(NIL_UUID)) result.add(owner);
        result.addAll(standardAccess);
        result.addAll(fullAccess);
        return Set.copyOf(result);
    }

    private void setChangedAndSync() {
        setChanged();
        if (level != null && !level.isClientSide) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), Block.UPDATE_CLIENTS);
            BlockPos upper = worldPosition.above();
            if (level.getBlockEntity(upper) instanceof ArcaneDoorBlockEntity) {
                level.sendBlockUpdated(upper, level.getBlockState(upper), level.getBlockState(upper), Block.UPDATE_CLIENTS);
            }
        }
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        ArcaneDoorBlockEntity master = master();
        if (master != this) return;
        if (!owner.equals(NIL_UUID)) tag.putUUID("Owner", owner);
        tag.put("StandardAccess", writeUuidList(standardAccess));
        tag.put("FullAccess", writeUuidList(fullAccess));
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        owner = tag.hasUUID("Owner") ? tag.getUUID("Owner") : NIL_UUID;
        standardAccess.clear();
        standardAccess.addAll(readUuidList(tag.getList("StandardAccess", Tag.TAG_INT_ARRAY)));
        fullAccess.clear();
        fullAccess.addAll(readUuidList(tag.getList("FullAccess", Tag.TAG_INT_ARRAY)));
        standardAccess.removeAll(fullAccess);
        standardAccess.remove(owner);
        fullAccess.remove(owner);
    }

    private static ListTag writeUuidList(Set<UUID> uuids) {
        ListTag list = new ListTag();
        for (UUID uuid : uuids) list.add(net.minecraft.nbt.NbtUtils.createUUID(uuid));
        return list;
    }

    private static Set<UUID> readUuidList(ListTag list) {
        Set<UUID> result = new LinkedHashSet<>();
        for (Tag value : list) {
            try { result.add(net.minecraft.nbt.NbtUtils.loadUUID(value)); }
            catch (IllegalArgumentException ignored) { }
        }
        return result;
    }

    @Override
    public CompoundTag getUpdateTag() { return master() == this ? saveWithoutMetadata() : new CompoundTag(); }

    @Override
    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public void onDataPacket(Connection net, ClientboundBlockEntityDataPacket packet) {
        CompoundTag tag = packet.getTag();
        if (tag != null) load(tag);
    }
}
