package com.darkifov.thaumcraft.wand;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.world.item.ItemStack;

import java.util.Arrays;

/** Stage173 original ItemFocusBasic upgrade NBT adapter. */
public final class FocusUpgradeRuntime {
    public static final String TAG_UPGRADE = "upgrade"; // original TC4 key
    public static final int MAX_RANK = 5;

    private FocusUpgradeRuntime() {}

    public static short[] getAppliedUpgrades(ItemStack focusStack) {
        short[] out = new short[]{-1, -1, -1, -1, -1};
        if (focusStack.isEmpty() || !focusStack.hasTag()) return out;
        ListTag list = focusStack.getOrCreateTag().getList(TAG_UPGRADE, 10);
        for (int i = 0; i < list.size() && i < MAX_RANK; i++) {
            out[i] = list.getCompound(i).getShort("id");
        }
        return out;
    }

    public static void setAppliedUpgrades(ItemStack focusStack, short[] upgrades) {
        ListTag list = new ListTag();
        for (int i = 0; i < MAX_RANK; i++) {
            CompoundTag entry = new CompoundTag();
            entry.putShort("id", i < upgrades.length ? upgrades[i] : -1);
            list.add(entry);
        }
        focusStack.getOrCreateTag().put(TAG_UPGRADE, list);
    }

    public static boolean applyUpgrade(ItemStack focusStack, FocusUpgradeType type, int rank) {
        if (focusStack.isEmpty() || type == null || rank < 1 || rank > MAX_RANK) return false;
        WandFocusType focus = focusStack.getItem() instanceof com.darkifov.thaumcraft.block.WandFocusItem focusItem ? focusItem.focusType() : null;
        if (focus != null && (!isPossible(focus, type, rank) || !canApplyUpgrade(focusStack, focus, type))) return false;
        short[] upgrades = getAppliedUpgrades(focusStack);
        if (upgrades[rank - 1] != -1) return false;
        upgrades[rank - 1] = type.id();
        setAppliedUpgrades(focusStack, upgrades);
        return true;
    }

    public static int getUpgradeLevel(ItemStack focusStack, FocusUpgradeType type) {
        if (type == null) return 0;
        int level = 0;
        for (short id : getAppliedUpgrades(focusStack)) {
            if (id == type.id()) level++;
        }
        return level;
    }

    public static boolean isUpgradedWith(ItemStack focusStack, FocusUpgradeType type) {
        return getUpgradeLevel(focusStack, type) > 0;
    }

    public static FocusUpgradeType[] possibleUpgrades(WandFocusType focus, int rank) {
        if (focus == null || rank < 1 || rank > MAX_RANK) return new FocusUpgradeType[0];
        return switch (focus) {
            case FIRE -> switch (rank) {
                case 1, 5 -> a(FocusUpgradeType.FRUGAL, FocusUpgradeType.POTENCY);
                case 2, 4 -> a(FocusUpgradeType.FRUGAL, FocusUpgradeType.POTENCY, FocusUpgradeType.ALCHEMISTS_FIRE);
                case 3 -> a(FocusUpgradeType.FRUGAL, FocusUpgradeType.POTENCY, FocusUpgradeType.FIREBALL, FocusUpgradeType.FIREBEAM);
                default -> new FocusUpgradeType[0];
            };
            case FROST -> switch (rank) {
                case 1, 5 -> a(FocusUpgradeType.FRUGAL, FocusUpgradeType.POTENCY, FocusUpgradeType.ALCHEMISTS_FROST);
                case 2, 4 -> a(FocusUpgradeType.FRUGAL, FocusUpgradeType.POTENCY);
                case 3 -> a(FocusUpgradeType.FRUGAL, FocusUpgradeType.POTENCY, FocusUpgradeType.SCATTERSHOT, FocusUpgradeType.ICE_BOULDER, FocusUpgradeType.ALCHEMISTS_FROST);
                default -> new FocusUpgradeType[0];
            };
            case SHOCK -> switch (rank) {
                case 1, 2 -> a(FocusUpgradeType.FRUGAL, FocusUpgradeType.POTENCY);
                case 3 -> a(FocusUpgradeType.FRUGAL, FocusUpgradeType.POTENCY, FocusUpgradeType.CHAIN_LIGHTNING, FocusUpgradeType.EARTH_SHOCK);
                case 4, 5 -> a(FocusUpgradeType.FRUGAL, FocusUpgradeType.POTENCY, FocusUpgradeType.ENLARGE);
                default -> new FocusUpgradeType[0];
            };
            case EXCAVATION -> switch (rank) {
                case 1 -> a(FocusUpgradeType.FRUGAL, FocusUpgradeType.POTENCY, FocusUpgradeType.TREASURE);
                case 2, 4 -> a(FocusUpgradeType.FRUGAL, FocusUpgradeType.POTENCY, FocusUpgradeType.ENLARGE);
                case 3 -> a(FocusUpgradeType.FRUGAL, FocusUpgradeType.POTENCY, FocusUpgradeType.TREASURE, FocusUpgradeType.DOWSING);
                case 5 -> a(FocusUpgradeType.FRUGAL, FocusUpgradeType.POTENCY, FocusUpgradeType.TREASURE, FocusUpgradeType.SILK_TOUCH);
                default -> new FocusUpgradeType[0];
            };
            case EQUAL_TRADE -> switch (rank) {
                case 1, 2, 4 -> a(FocusUpgradeType.FRUGAL, FocusUpgradeType.ENLARGE);
                case 3 -> a(FocusUpgradeType.FRUGAL, FocusUpgradeType.ENLARGE, FocusUpgradeType.TREASURE, FocusUpgradeType.ARCHITECT);
                case 5 -> a(FocusUpgradeType.FRUGAL, FocusUpgradeType.ENLARGE, FocusUpgradeType.SILK_TOUCH);
                default -> new FocusUpgradeType[0];
            };
            case PORTABLE_HOLE -> a(FocusUpgradeType.FRUGAL, FocusUpgradeType.ENLARGE, FocusUpgradeType.EXTEND);
            case WARDING -> switch (rank) {
                case 1 -> a(FocusUpgradeType.FRUGAL);
                case 2 -> a(FocusUpgradeType.FRUGAL, FocusUpgradeType.ARCHITECT);
                case 3, 4, 5 -> a(FocusUpgradeType.FRUGAL, FocusUpgradeType.ENLARGE);
                default -> new FocusUpgradeType[0];
            };
            case PRIMAL -> rank == 3 ? a(FocusUpgradeType.FRUGAL, FocusUpgradeType.SEEKER) : a(FocusUpgradeType.FRUGAL);
        };
    }

    public static boolean isPossible(WandFocusType focus, FocusUpgradeType type, int rank) {
        return Arrays.asList(possibleUpgrades(focus, rank)).contains(type);
    }

    /** Stage177: original canApplyUpgrade gates that affect architect/enlarge parity. */
    public static boolean canApplyUpgrade(ItemStack focusStack, WandFocusType focus, FocusUpgradeType type) {
        if (focus == WandFocusType.WARDING && type == FocusUpgradeType.ENLARGE) {
            return isUpgradedWith(focusStack, FocusUpgradeType.ARCHITECT);
        }
        if (focus == WandFocusType.SHOCK && type == FocusUpgradeType.ENLARGE) {
            return isUpgradedWith(focusStack, FocusUpgradeType.CHAIN_LIGHTNING) || isUpgradedWith(focusStack, FocusUpgradeType.EARTH_SHOCK);
        }
        if (focus == WandFocusType.FIRE && type == FocusUpgradeType.ALCHEMISTS_FIRE) {
            return isUpgradedWith(focusStack, FocusUpgradeType.FIREBALL) || isUpgradedWith(focusStack, FocusUpgradeType.FIREBEAM);
        }
        if (focus == WandFocusType.FROST && type == FocusUpgradeType.ALCHEMISTS_FROST) {
            return isUpgradedWith(focusStack, FocusUpgradeType.SCATTERSHOT) || isUpgradedWith(focusStack, FocusUpgradeType.ICE_BOULDER);
        }
        return true;
    }

    private static FocusUpgradeType[] a(FocusUpgradeType... types) {
        return types;
    }
}
