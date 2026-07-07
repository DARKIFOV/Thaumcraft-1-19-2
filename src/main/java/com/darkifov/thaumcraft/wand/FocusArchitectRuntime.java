package com.darkifov.thaumcraft.wand;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Stage177 strict adapter for original TC4 WandManager/IArchitect area state.
 *
 * Original TC4 stores architect dimensions directly on the wand stack with the
 * keys areax/areay/areaz/aread.  Forge 1.19.2 has no TC4 IArchitect overlay in
 * this port yet, but the runtime keys and block selection rules are preserved so
 * focus behaviour can use the same data and later GUI/overlay work can bind to
 * the same NBT without migration.
 */
public final class FocusArchitectRuntime {
    public static final String TAG_AREA_X = "areax";
    public static final String TAG_AREA_Y = "areay";
    public static final String TAG_AREA_Z = "areaz";
    public static final String TAG_AREA_DIM = "aread";
    public static final String TAG_PICKED_BLOCK = "picked";

    private FocusArchitectRuntime() {}

    public static int getAreaDim(ItemStack wandStack) {
        CompoundTag tag = wandStack.getTag();
        return tag != null && tag.contains(TAG_AREA_DIM) ? tag.getInt(TAG_AREA_DIM) : 0;
    }

    public static int getAreaX(ItemStack wandStack, WandFocusType focus) {
        return getArea(wandStack, focus, TAG_AREA_X);
    }

    public static int getAreaY(ItemStack wandStack, WandFocusType focus) {
        return getArea(wandStack, focus, TAG_AREA_Y);
    }

    public static int getAreaZ(ItemStack wandStack, WandFocusType focus) {
        return getArea(wandStack, focus, TAG_AREA_Z);
    }

    private static int getArea(ItemStack wandStack, WandFocusType focus, String key) {
        int max = getMaxAreaSize(wandStack, focus);
        CompoundTag tag = wandStack.getTag();
        int value = tag != null && tag.contains(key) ? tag.getInt(key) : max;
        return Math.max(0, Math.min(max, value));
    }

    public static void setAreaX(ItemStack wandStack, int area) {
        wandStack.getOrCreateTag().putInt(TAG_AREA_X, Math.max(0, area));
    }

    public static void setAreaY(ItemStack wandStack, int area) {
        wandStack.getOrCreateTag().putInt(TAG_AREA_Y, Math.max(0, area));
    }

    public static void setAreaZ(ItemStack wandStack, int area) {
        wandStack.getOrCreateTag().putInt(TAG_AREA_Z, Math.max(0, area));
    }

    public static void setAreaDim(ItemStack wandStack, int dim) {
        wandStack.getOrCreateTag().putInt(TAG_AREA_DIM, Math.max(0, dim));
    }

    public static int getMaxAreaSize(ItemStack wandStack, WandFocusType focus) {
        int enlarge = FocusUpgradeRuntime.getUpgradeLevel(WandFocusRuntime.getFocusStack(wandStack), FocusUpgradeType.ENLARGE);
        if (focus == WandFocusType.EQUAL_TRADE) {
            return 3 + enlarge * 2;
        }
        if (focus == WandFocusType.WARDING) {
            return 3 + enlarge;
        }
        return 1;
    }

    public static String architectStatusLine(ItemStack wandStack) {
        WandFocusType focus = WandFocusRuntime.getFocus(wandStack);
        if (focus == null) {
            return "No wand focus";
        }
        String dimName = switch (getAreaDim(wandStack)) {
            case 1 -> "X";
            case 2 -> "Z";
            case 3 -> "Y";
            default -> "XYZ";
        };
        return "Architect " + focus.displayName() + " area "
                + getAreaX(wandStack, focus) + "x"
                + getAreaY(wandStack, focus) + "x"
                + getAreaZ(wandStack, focus) + " dim " + dimName;
    }

    public static ItemStack pickedBlock(ItemStack wandStack) {
        CompoundTag tag = wandStack.getTag();
        if (tag != null && tag.contains(TAG_PICKED_BLOCK, 10)) {
            return ItemStack.of(tag.getCompound(TAG_PICKED_BLOCK));
        }
        return ItemStack.EMPTY;
    }

    public static void toggleMisc(ItemStack wandStack, Player player) {
        WandFocusType focus = WandFocusRuntime.getFocus(wandStack);
        if ((focus != WandFocusType.EQUAL_TRADE && focus != WandFocusType.WARDING)
                || !WandFocusRuntime.focusHasUpgrade(wandStack, FocusUpgradeType.ARCHITECT)) {
            return;
        }
        if (player.isShiftKeyDown()) {
            int dim = getAreaDim(wandStack) + 1;
            int maxDim = focus == WandFocusType.EQUAL_TRADE ? 2 : 3;
            if (dim > maxDim) dim = 0;
            setAreaDim(wandStack, dim);
            return;
        }
        int dim = getAreaDim(wandStack);
        int x = getAreaX(wandStack, focus);
        int y = getAreaY(wandStack, focus);
        int z = getAreaZ(wandStack, focus);
        if (dim == 0) {
            x++;
            y++;
            z++;
        } else if (dim == 1) {
            x++;
        } else if (dim == 2) {
            z++;
        } else if (dim == 3) {
            y++;
        }
        int max = getMaxAreaSize(wandStack, focus);
        setAreaX(wandStack, x > max ? 0 : x);
        setAreaY(wandStack, y > max ? 0 : y);
        setAreaZ(wandStack, z > max ? 0 : z);
    }

    public static boolean showAxis(ItemStack wandStack, WandFocusType focus, Direction side, Direction.Axis axis) {
        int dim = getAreaDim(wandStack);
        if (focus == WandFocusType.EQUAL_TRADE) {
            return switch (side.getAxis()) {
                case Y -> (axis == Direction.Axis.X && (dim == 0 || dim == 1)) || (axis == Direction.Axis.Z && (dim == 0 || dim == 2));
                case Z -> (axis == Direction.Axis.Y && (dim == 0 || dim == 1)) || (axis == Direction.Axis.X && (dim == 0 || dim == 2));
                case X -> (axis == Direction.Axis.Y && (dim == 0 || dim == 1)) || (axis == Direction.Axis.Z && (dim == 0 || dim == 2));
            };
        }
        if (focus == WandFocusType.WARDING) {
            if (dim == 0) return true;
            return switch (side.getAxis()) {
                case Y -> (axis == Direction.Axis.X && dim == 1) || (axis == Direction.Axis.Z && dim == 2) || (axis == Direction.Axis.Y && dim == 3);
                case Z -> (axis == Direction.Axis.Y && dim == 1) || (axis == Direction.Axis.X && dim == 2) || (axis == Direction.Axis.Z && dim == 3);
                case X -> (axis == Direction.Axis.Y && dim == 1) || (axis == Direction.Axis.Z && dim == 2) || (axis == Direction.Axis.X && dim == 3);
            };
        }
        return false;
    }

    public static List<BlockPos> equalTradeArchitectBlocks(ItemStack wandStack, Level level, BlockHitResult hit, Player player) {
        List<BlockPos> out = new ArrayList<>();
        Set<BlockPos> checked = new HashSet<>();
        BlockPos origin = hit.getBlockPos();
        BlockState source = level.getBlockState(origin);
        int sizeX = getAreaX(wandStack, WandFocusType.EQUAL_TRADE);
        int sizeY = getAreaY(wandStack, WandFocusType.EQUAL_TRADE);
        int sizeZ = getAreaZ(wandStack, WandFocusType.EQUAL_TRADE);
        if (hit.getDirection().getAxis() == Direction.Axis.Z) {
            checkTrade(level, origin, origin, source, hit.getDirection(), sizeZ, sizeY, sizeX, out, checked, player);
        } else {
            checkTrade(level, origin, origin, source, hit.getDirection(), sizeX, sizeY, sizeZ, out, checked, player);
        }
        return out;
    }

    private static void checkTrade(Level level, BlockPos origin, BlockPos pos, BlockState source, Direction side, int sizeX, int sizeY, int sizeZ, List<BlockPos> out, Set<BlockPos> checked, Player player) {
        if (!checked.add(pos)) return;
        if (!insideOriginalBounds(origin, pos, side, sizeX, sizeY, sizeZ, false)) return;
        BlockState state = level.getBlockState(pos);
        if (!state.equals(source) || state.isAir() || state.hasBlockEntity() || state.getDestroySpeed(level, pos) < 0.0F || !isBlockExposed(level, pos) || !level.mayInteract(player, pos)) {
            return;
        }
        out.add(pos);
        for (Direction dir : Direction.values()) {
            if (dir != side && dir != side.getOpposite()) {
                checkTrade(level, origin, pos.relative(dir), source, side, sizeX, sizeY, sizeZ, out, checked, player);
            }
        }
    }

    public static List<BlockPos> equalTradeLinkedBlocks(Level level, BlockPos origin, int lifespan, Player player) {
        List<BlockPos> out = new ArrayList<>();
        Set<BlockPos> checked = new HashSet<>();
        BlockState source = level.getBlockState(origin);
        checkTradeLinked(level, origin, source, lifespan, out, checked, player);
        return out;
    }

    private static void checkTradeLinked(Level level, BlockPos pos, BlockState source, int life, List<BlockPos> out, Set<BlockPos> checked, Player player) {
        if (life < 0 || !checked.add(pos)) return;
        BlockState state = level.getBlockState(pos);
        if (!state.equals(source) || state.isAir() || state.hasBlockEntity() || state.getDestroySpeed(level, pos) < 0.0F || !isBlockExposed(level, pos) || !level.mayInteract(player, pos)) {
            return;
        }
        out.add(pos);
        if (life == 0) return;
        for (int dx = -1; dx <= 1; dx++) {
            for (int dy = -1; dy <= 1; dy++) {
                for (int dz = -1; dz <= 1; dz++) {
                    if (dx != 0 || dy != 0 || dz != 0) {
                        checkTradeLinked(level, pos.offset(dx, dy, dz), source, life - 1, out, checked, player);
                    }
                }
            }
        }
    }

    public static List<BlockPos> wardingArchitectBlocks(ItemStack wandStack, Level level, BlockHitResult hit, Player player, boolean tiles) {
        List<BlockPos> out = new ArrayList<>();
        Set<BlockPos> checked = new HashSet<>();
        BlockPos origin = hit.getBlockPos();
        int sizeX = 0;
        int sizeY = 0;
        int sizeZ = 0;
        if (WandFocusRuntime.focusHasUpgrade(wandStack, FocusUpgradeType.ARCHITECT)) {
            sizeX = getAreaX(wandStack, WandFocusType.WARDING);
            sizeY = getAreaY(wandStack, WandFocusType.WARDING);
            sizeZ = getAreaZ(wandStack, WandFocusType.WARDING);
        }
        if (hit.getDirection().getAxis() == Direction.Axis.Z) {
            checkWarding(level, origin, origin, hit.getDirection(), sizeZ, sizeY, sizeX, out, checked, player, tiles);
        } else {
            checkWarding(level, origin, origin, hit.getDirection(), sizeX, sizeY, sizeZ, out, checked, player, tiles);
        }
        return out;
    }

    private static void checkWarding(Level level, BlockPos origin, BlockPos pos, Direction side, int sizeX, int sizeY, int sizeZ, List<BlockPos> out, Set<BlockPos> checked, Player player, boolean tiles) {
        if (!checked.add(pos)) return;
        if (!insideOriginalBounds(origin, pos, side, sizeX, sizeY, sizeZ, true)) return;
        BlockState state = level.getBlockState(pos);
        boolean warded = com.darkifov.thaumcraft.ward.WardedBlockRuntime.isWarded(level, pos);
        if (tiles) {
            if (!warded || !com.darkifov.thaumcraft.ward.WardedBlockRuntime.mayEdit(level, pos, player)) return;
        } else if (state.isAir() || state.hasBlockEntity() || !state.isSolidRender(level, pos)) {
            return;
        }
        if (!state.isAir()) {
            out.add(pos);
        } else {
            return;
        }
        for (Direction dir : Direction.values()) {
            checkWarding(level, origin, pos.relative(dir), side, sizeX, sizeY, sizeZ, out, checked, player, tiles);
        }
    }

    private static boolean insideOriginalBounds(BlockPos origin, BlockPos pos, Direction side, int sizeX, int sizeY, int sizeZ, boolean includePerpendicularDepth) {
        int dx = Math.abs(pos.getX() - origin.getX());
        int dy = Math.abs(pos.getY() - origin.getY());
        int dz = Math.abs(pos.getZ() - origin.getZ());
        return switch (side.getAxis()) {
            case Y -> dx <= sizeX && dz <= sizeZ && (!includePerpendicularDepth || dy <= sizeY);
            case Z -> dx <= sizeX && dy <= sizeZ && (!includePerpendicularDepth || dz <= sizeY);
            case X -> dy <= sizeX && dz <= sizeZ && (!includePerpendicularDepth || dx <= sizeY);
        };
    }

    private static boolean isBlockExposed(Level level, BlockPos pos) {
        for (Direction dir : Direction.values()) {
            if (!level.getBlockState(pos.relative(dir)).isSolidRender(level, pos.relative(dir))) {
                return true;
            }
        }
        return false;
    }
}
