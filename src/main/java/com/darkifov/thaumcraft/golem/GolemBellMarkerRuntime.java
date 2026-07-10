package com.darkifov.thaumcraft.golem;

import com.darkifov.thaumcraft.entity.ThaumGolemEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Original TC4 ItemGolemBell marker storage with a modern persistence extension.
 * Legacy keys remain unchanged; UUID and dimension ResourceLocation keys are
 * added so a bell survives entity-id changes and does not alias modded dimensions.
 */
public final class GolemBellMarkerRuntime {
    public static final String NBT_GOLEM_ID = "golemid";
    public static final String NBT_GOLEM_UUID = "golemuuid";
    public static final String NBT_GOLEM_DIMENSION = "golemdimension";
    public static final String NBT_HOME_X = "golemhomex";
    public static final String NBT_HOME_Y = "golemhomey";
    public static final String NBT_HOME_Z = "golemhomez";
    public static final String NBT_HOME_FACE = "golemhomeface";
    public static final String NBT_MARKERS = "markers";
    public static final String NBT_MARKER_DIMENSION = "dimKey";

    private GolemBellMarkerRuntime() {
    }

    public static int getGolemId(ItemStack stack) {
        CompoundTag tag = stack.getTag();
        return tag != null && tag.contains(NBT_GOLEM_ID) ? tag.getInt(NBT_GOLEM_ID) : -1;
    }

    public static UUID getGolemUuid(ItemStack stack) {
        CompoundTag tag = stack.getTag();
        return tag != null && tag.hasUUID(NBT_GOLEM_UUID) ? tag.getUUID(NBT_GOLEM_UUID) : null;
    }

    public static BlockPos getGolemHomeCoords(ItemStack stack) {
        CompoundTag tag = stack.getTag();
        if (tag == null || !tag.contains(NBT_HOME_X)) {
            return null;
        }
        return new BlockPos(tag.getInt(NBT_HOME_X), tag.getInt(NBT_HOME_Y), tag.getInt(NBT_HOME_Z));
    }

    public static int getGolemHomeFace(ItemStack stack) {
        CompoundTag tag = stack.getTag();
        return tag != null && tag.contains(NBT_HOME_FACE) ? tag.getInt(NBT_HOME_FACE) : -1;
    }

    public static ListTag getMarkersTag(ItemStack stack) {
        CompoundTag tag = stack.getTag();
        return tag != null && tag.contains(NBT_MARKERS) ? tag.getList(NBT_MARKERS, 10).copy() : new ListTag();
    }

    public static List<Marker> getMarkers(ItemStack stack) {
        return readMarkers(getMarkersTag(stack));
    }

    public static void setMarkers(ItemStack stack, List<Marker> markers) {
        stack.getOrCreateTag().put(NBT_MARKERS, writeMarkers(markers));
    }

    public static void bindGolem(ItemStack stack, ThaumGolemEntity golem) {
        CompoundTag tag = stack.getOrCreateTag();
        tag.putInt(NBT_GOLEM_ID, golem.getId());
        tag.putUUID(NBT_GOLEM_UUID, golem.getUUID());
        tag.putString(NBT_GOLEM_DIMENSION, dimensionKey(golem.level));
        BlockPos home = golem.getHomePos();
        tag.putInt(NBT_HOME_X, home.getX());
        tag.putInt(NBT_HOME_Y, home.getY());
        tag.putInt(NBT_HOME_Z, home.getZ());
        tag.putInt(NBT_HOME_FACE, golem.getHomeFacing());
        tag.put(NBT_MARKERS, golem.originalMarkerListSnapshot());
    }

    public static void clearBinding(ItemStack stack) {
        CompoundTag tag = stack.getTag();
        if (tag == null) {
            return;
        }
        tag.remove(NBT_GOLEM_ID);
        tag.remove(NBT_GOLEM_UUID);
        tag.remove(NBT_GOLEM_DIMENSION);
        tag.remove(NBT_HOME_X);
        tag.remove(NBT_HOME_Y);
        tag.remove(NBT_HOME_Z);
        tag.remove(NBT_HOME_FACE);
        tag.remove(NBT_MARKERS);
    }

    public static ThaumGolemEntity boundGolem(ItemStack stack, Level level) {
        if (!(level instanceof ServerLevel serverLevel)) {
            return null;
        }
        CompoundTag tag = stack.getTag();
        if (tag != null && tag.contains(NBT_GOLEM_DIMENSION)
                && !dimensionKey(level).equals(tag.getString(NBT_GOLEM_DIMENSION))) {
            return null;
        }
        int id = getGolemId(stack);
        if (id >= 0) {
            Entity entity = serverLevel.getEntity(id);
            if (entity instanceof ThaumGolemEntity golem) {
                UUID expected = getGolemUuid(stack);
                if (expected == null || expected.equals(golem.getUUID())) {
                    return golem;
                }
            }
        }
        UUID uuid = getGolemUuid(stack);
        if (uuid != null) {
            Entity entity = serverLevel.getEntity(uuid);
            if (entity instanceof ThaumGolemEntity golem) {
                stack.getOrCreateTag().putInt(NBT_GOLEM_ID, golem.getId());
                return golem;
            }
        }
        return null;
    }

    public static ToggleResult changeMarkers(ItemStack stack, Player player, Level level, BlockPos pos,
                                             Direction side, boolean markMultipleColors) {
        List<Marker> markers = getMarkers(stack);
        int dim = legacyDimensionId(level);
        String dimKey = dimensionKey(level);
        byte sideByte = (byte) (side == null ? Direction.UP.ordinal() : side.ordinal());
        int index = -1;
        byte foundColor = -1;

        if (!markMultipleColors) {
            index = indexOf(markers, pos, dim, dimKey, sideByte, (byte) -1, true);
        } else {
            for (byte color = -1; color < 16; color++) {
                index = indexOf(markers, pos, dim, dimKey, sideByte, color, false);
                foundColor = color;
                if (index >= 0) {
                    break;
                }
            }
        }

        String action;
        if (index >= 0) {
            markers.remove(index);
            action = "removed";
            if (markMultipleColors && !player.isShiftKeyDown()) {
                byte next = (byte) (foundColor + 1);
                if (next <= 15) {
                    markers.add(new Marker(pos.getX(), pos.getY(), pos.getZ(), dim, dimKey, sideByte, next));
                    action = next < 0 ? "any" : "color " + next;
                }
            }
        } else {
            markers.add(new Marker(pos.getX(), pos.getY(), pos.getZ(), dim, dimKey, sideByte, (byte) -1));
            action = "added";
        }

        setMarkers(stack, markers);
        ThaumGolemEntity golem = boundGolem(stack, level);
        if (golem != null) {
            golem.applyOriginalMarkerList(getMarkersTag(stack));
        }
        return new ToggleResult(action, markers.size());
    }

    public static ListTag writeMarkers(List<Marker> markers) {
        ListTag list = new ListTag();
        for (Marker marker : markers) {
            CompoundTag tag = new CompoundTag();
            tag.putInt("x", marker.x());
            tag.putInt("y", marker.y());
            tag.putInt("z", marker.z());
            tag.putInt("dim", marker.dim());
            if (marker.dimensionKey() != null && !marker.dimensionKey().isBlank()) {
                tag.putString(NBT_MARKER_DIMENSION, marker.dimensionKey());
            }
            tag.putByte("side", marker.side());
            tag.putByte("color", marker.color());
            list.add(tag);
        }
        return list;
    }

    public static List<Marker> readMarkers(ListTag list) {
        List<Marker> markers = new ArrayList<>();
        for (int i = 0; i < list.size(); i++) {
            CompoundTag tag = list.getCompound(i);
            int legacyDim = tag.getInt("dim");
            String key = tag.contains(NBT_MARKER_DIMENSION)
                    ? tag.getString(NBT_MARKER_DIMENSION)
                    : legacyDimensionKey(legacyDim);
            markers.add(new Marker(
                    tag.getInt("x"), tag.getInt("y"), tag.getInt("z"), legacyDim, key,
                    tag.getByte("side"), tag.getByte("color")
            ));
        }
        return markers;
    }

    public static ListTag markerListFromTaskPositions(BlockPos home, BlockPos input, BlockPos output,
                                                       BlockPos guard, BlockPos work, int homeFacing, Level level) {
        List<Marker> markers = new ArrayList<>();
        int dim = legacyDimensionId(level);
        String key = dimensionKey(level);
        addIfPresent(markers, work, dim, key, (byte) homeFacing, (byte) -1);
        addIfPresent(markers, input, dim, key, (byte) homeFacing, (byte) 0);
        addIfPresent(markers, output, dim, key, (byte) homeFacing, (byte) 1);
        addIfPresent(markers, guard, dim, key, (byte) homeFacing, (byte) 2);
        addIfPresent(markers, home, dim, key, (byte) homeFacing, (byte) 3);
        return writeMarkers(markers);
    }

    public static MutableComponent markerSummary(ItemStack stack) {
        List<Marker> markers = getMarkers(stack);
        BlockPos home = getGolemHomeCoords(stack);
        String homeText = home == null ? "unbound" : home.toShortString() + " face " + getGolemHomeFace(stack);
        String identity = getGolemUuid(stack) == null ? String.valueOf(getGolemId(stack)) : getGolemUuid(stack).toString().substring(0, 8);
        return Component.literal("TC4 bell golem=" + identity + " home=" + homeText + " markers=" + markers.size());
    }

    public static boolean markerMatchesLevel(Marker marker, Level level) {
        if (marker == null || level == null) {
            return false;
        }
        if (marker.dimensionKey() != null && !marker.dimensionKey().isBlank()) {
            return marker.dimensionKey().equals(dimensionKey(level));
        }
        return marker.dim() == legacyDimensionId(level);
    }

    private static void addIfPresent(List<Marker> markers, BlockPos pos, int dim, String dimensionKey,
                                     byte side, byte color) {
        if (pos != null && !pos.equals(BlockPos.ZERO)) {
            markers.add(new Marker(pos.getX(), pos.getY(), pos.getZ(), dim, dimensionKey, side, color));
        }
    }

    private static int indexOf(List<Marker> markers, BlockPos pos, int dim, String dimKey,
                               byte side, byte color, boolean ignoreColor) {
        for (int i = 0; i < markers.size(); i++) {
            Marker marker = markers.get(i);
            if (marker.x() == pos.getX() && marker.y() == pos.getY() && marker.z() == pos.getZ()
                    && marker.dim() == dim && dimKey.equals(marker.dimensionKey()) && marker.side() == side
                    && (ignoreColor || marker.color() == color)) {
                return i;
            }
        }
        return -1;
    }

    public static String dimensionKey(Level level) {
        return level == null ? "minecraft:overworld" : level.dimension().location().toString();
    }

    private static int legacyDimensionId(Level level) {
        String location = dimensionKey(level);
        if ("minecraft:the_nether".equals(location)) {
            return -1;
        }
        if ("minecraft:the_end".equals(location)) {
            return 1;
        }
        return 0;
    }

    private static String legacyDimensionKey(int legacyDimension) {
        return switch (legacyDimension) {
            case -1 -> "minecraft:the_nether";
            case 1 -> "minecraft:the_end";
            default -> "minecraft:overworld";
        };
    }

    public record Marker(int x, int y, int z, int dim, String dimensionKey, byte side, byte color) {
    }

    public record ToggleResult(String action, int count) {
    }
}
