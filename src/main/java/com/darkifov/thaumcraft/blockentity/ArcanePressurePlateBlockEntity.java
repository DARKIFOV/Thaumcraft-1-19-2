package com.darkifov.thaumcraft.blockentity;

import com.darkifov.thaumcraft.ThaumcraftMod;
import com.darkifov.thaumcraft.block.ArcanePressurePlateBlock;
import com.darkifov.thaumcraft.block.TC4ArcanePressurePlateParity;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import java.util.LinkedHashSet;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

/** Persistent TC4 owner/access state for the warded Arcane Pressure Plate. */
public final class ArcanePressurePlateBlockEntity extends BlockEntity implements ArcaneAccessTarget {
    private static final UUID NIL_UUID = new UUID(0L, 0L);
    public static final String LEGACY_OWNER_TAG = "owner";
    public static final String LEGACY_ACCESS_TAG = "access";
    public static final String LEGACY_ACCESS_NAME_TAG = "name";

    private UUID owner = NIL_UUID;
    private String ownerName = "";
    private final Set<UUID> standardAccess = new LinkedHashSet<>();
    private final Set<UUID> fullAccess = new LinkedHashSet<>();
    private final Set<String> standardAccessNames = new LinkedHashSet<>();
    private final Set<String> fullAccessNames = new LinkedHashSet<>();
    private byte setting;

    public ArcanePressurePlateBlockEntity(BlockPos pos, BlockState state) {
        super(ThaumcraftMod.ARCANE_PRESSURE_PLATE_BLOCK_ENTITY.get(), pos, state);
        if (state.hasProperty(ArcanePressurePlateBlock.SETTING)) {
            setting = state.getValue(ArcanePressurePlateBlock.SETTING).byteValue();
        }
    }

    public void initializeOwner(Player player) {
        if (player == null || !owner.equals(NIL_UUID) || !ownerName.isEmpty()) return;
        owner = player.getUUID();
        ownerName = playerName(player);
        setChangedAndSync();
    }

    @Override
    public UUID owner() {
        return owner;
    }

    public String ownerName() {
        return ownerName;
    }

    @Override
    public BlockPos keyBindingPos() {
        return worldPosition;
    }

    @Override
    public byte keyTargetType() {
        return TC4ArcanePressurePlateParity.KEY_TARGET_PLATE;
    }

    @Override
    public Set<UUID> authorizedUsers() {
        Set<UUID> result = new LinkedHashSet<>();
        if (!owner.equals(NIL_UUID)) result.add(owner);
        result.addAll(standardAccess);
        result.addAll(fullAccess);
        return Set.copyOf(result);
    }

    @Override
    public boolean isOwner(Player player) {
        if (player == null) return false;
        return owner.equals(player.getUUID()) || nameMatches(ownerName, playerName(player));
    }

    @Override
    public boolean hasAccess(Player player) {
        if (player == null) return false;
        UUID id = player.getUUID();
        String name = normalizeName(playerName(player));
        return isOwner(player) || standardAccess.contains(id) || fullAccess.contains(id)
                || standardAccessNames.contains(name) || fullAccessNames.contains(name);
    }

    @Override
    public boolean hasFullAccess(Player player) {
        if (player == null) return false;
        String name = normalizeName(playerName(player));
        return isOwner(player) || fullAccess.contains(player.getUUID()) || fullAccessNames.contains(name);
    }

    /** Owner may bind either key; a full-access holder may duplicate iron keys only. */
    @Override
    public boolean mayBindKey(Player player, boolean goldKey) {
        return TC4ArcanePressurePlateParity.mayBindKey(isOwner(player), hasFullAccess(player), goldKey);
    }

    public boolean mayConfigure(Player player) {
        return hasFullAccess(player);
    }

    @Override
    public boolean addAccess(Player player, boolean full) {
        if (player == null || isOwner(player) || hasAccess(player)) return false;
        UUID id = player.getUUID();
        String name = normalizeName(playerName(player));
        boolean changed;
        if (full) {
            standardAccess.remove(id);
            standardAccessNames.remove(name);
            changed = fullAccess.add(id) | fullAccessNames.add(name);
        } else {
            changed = standardAccess.add(id) | standardAccessNames.add(name);
        }
        if (changed) setChangedAndSync();
        return changed;
    }

    public int setting() {
        return Byte.toUnsignedInt(setting);
    }

    public int cycleSetting(Player player) {
        if (!mayConfigure(player)) return -1;
        setting = (byte) TC4ArcanePressurePlateParity.nextSetting(setting());
        syncSettingToBlock();
        setChangedAndSync();
        return setting();
    }

    public boolean shouldTrigger(Entity entity) {
        boolean isPlayer = entity instanceof Player;
        boolean authorized = isPlayer && hasAccess((Player) entity);
        return TC4ArcanePressurePlateParity.shouldTrigger(setting(), isPlayer,
                entity == null || entity.isIgnoringBlockTriggers(), authorized);
    }

    private void syncSettingToBlock() {
        if (level == null) return;
        BlockState state = getBlockState();
        if (state.hasProperty(ArcanePressurePlateBlock.SETTING)
                && state.getValue(ArcanePressurePlateBlock.SETTING) != setting()) {
            level.setBlock(worldPosition, state.setValue(ArcanePressurePlateBlock.SETTING, setting()),
                    Block.UPDATE_CLIENTS);
        }
    }

    private void setChangedAndSync() {
        setChanged();
        if (level != null && !level.isClientSide) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), Block.UPDATE_CLIENTS);
        }
    }

    @Override
    public void onLoad() {
        super.onLoad();
        syncSettingToBlock();
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        if (!owner.equals(NIL_UUID)) tag.putUUID("Owner", owner);
        tag.putString(LEGACY_OWNER_TAG, ownerName);
        tag.putByte("setting", setting);
        tag.put("StandardAccess", writeUuidList(standardAccess));
        tag.put("FullAccess", writeUuidList(fullAccess));

        ListTag legacy = new ListTag();
        for (String name : standardAccessNames) legacy.add(legacyAccessEntry("0" + name));
        for (String name : fullAccessNames) legacy.add(legacyAccessEntry("1" + name));
        tag.put(LEGACY_ACCESS_TAG, legacy);
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        owner = tag.hasUUID("Owner") ? tag.getUUID("Owner") : NIL_UUID;
        ownerName = tag.getString(LEGACY_OWNER_TAG);
        setting = (byte) TC4ArcanePressurePlateParity.clampSetting(tag.getByte("setting"));

        standardAccess.clear();
        standardAccess.addAll(readUuidList(tag.getList("StandardAccess", Tag.TAG_INT_ARRAY)));
        fullAccess.clear();
        fullAccess.addAll(readUuidList(tag.getList("FullAccess", Tag.TAG_INT_ARRAY)));
        standardAccessNames.clear();
        fullAccessNames.clear();
        ListTag legacy = tag.getList(LEGACY_ACCESS_TAG, Tag.TAG_COMPOUND);
        for (Tag value : legacy) {
            String encoded = ((CompoundTag) value).getString(LEGACY_ACCESS_NAME_TAG);
            if (encoded.length() < 2) continue;
            String name = normalizeName(encoded.substring(1));
            if (encoded.charAt(0) == '1') fullAccessNames.add(name);
            else if (encoded.charAt(0) == '0') standardAccessNames.add(name);
        }
        standardAccess.removeAll(fullAccess);
        standardAccess.remove(owner);
        fullAccess.remove(owner);
        standardAccessNames.removeAll(fullAccessNames);
        standardAccessNames.remove(normalizeName(ownerName));
        fullAccessNames.remove(normalizeName(ownerName));
    }

    private static CompoundTag legacyAccessEntry(String encodedName) {
        CompoundTag value = new CompoundTag();
        value.putString(LEGACY_ACCESS_NAME_TAG, encodedName);
        return value;
    }

    private static ListTag writeUuidList(Set<UUID> uuids) {
        ListTag list = new ListTag();
        for (UUID uuid : uuids) list.add(net.minecraft.nbt.NbtUtils.createUUID(uuid));
        return list;
    }

    private static Set<UUID> readUuidList(ListTag list) {
        Set<UUID> result = new LinkedHashSet<>();
        for (Tag value : list) {
            try {
                result.add(net.minecraft.nbt.NbtUtils.loadUUID(value));
            } catch (IllegalArgumentException ignored) {
                // Malformed migration data must not make the block entity unloadable.
            }
        }
        return result;
    }

    private static String playerName(Player player) {
        return player.getGameProfile().getName();
    }

    private static String normalizeName(String name) {
        return name == null ? "" : name.toLowerCase(Locale.ROOT);
    }

    private static boolean nameMatches(String stored, String actual) {
        return !stored.isEmpty() && normalizeName(stored).equals(normalizeName(actual));
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
        if (tag != null) load(tag);
    }
}
