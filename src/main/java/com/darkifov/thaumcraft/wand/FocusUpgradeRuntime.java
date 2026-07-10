package com.darkifov.thaumcraft.wand;

import com.darkifov.thaumcraft.Aspect;
import com.darkifov.thaumcraft.AspectList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.entity.player.Player;
import com.darkifov.thaumcraft.data.PlayerThaumData;

import java.util.Arrays;
import java.util.Locale;

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


    /** Original TileFocalManipulator: first empty upgrade slot is the next rank (1..5). */
    public static int nextRank(ItemStack focusStack) {
        short[] upgrades = getAppliedUpgrades(focusStack);
        for (int i = 0; i < upgrades.length; i++) {
            if (upgrades[i] < 0) return i + 1;
        }
        return -1;
    }

    /** Compatibility name used by the real Focal Manipulator runtime. */
    public static int nextOpenRank(ItemStack focusStack) {
        return nextRank(focusStack);
    }

    /** Original TileFocalManipulator XP_MULT = 8. */
    public static int experienceCostForRank(int rank) {
        return rank < 1 || rank > MAX_RANK ? 0 : rank * 8;
    }

    /** Original TileFocalManipulator VIS_MULT = 200, doubled for every prior rank. */
    public static int compoundAspectAmountForRank(int rank) {
        if (rank < 1 || rank > MAX_RANK) return 0;
        return 200 << (rank - 1);
    }

    /**
     * Exact 1.7.10 focal-manipulator cost path: upgrade compound aspects are expanded
     * recursively to primal vis after applying 200 * 2^(rank-1).
     */
    public static AspectList primalVisCost(FocusUpgradeType type, int rank) {
        AspectList out = new AspectList();
        if (type == null) return out;
        int amount = compoundAspectAmountForRank(rank);
        if (amount <= 0) return out;
        for (String originalName : type.originalAspects().split(",")) {
            Aspect aspect = originalAspect(originalName.trim());
            if (aspect != null) reduceToPrimals(out, aspect, amount);
        }
        return out;
    }

    public static FocusUpgradeType[] validNextUpgrades(ItemStack focusStack) {
        if (focusStack.isEmpty() || !(focusStack.getItem() instanceof com.darkifov.thaumcraft.block.WandFocusItem item)) {
            return new FocusUpgradeType[0];
        }
        int rank = nextRank(focusStack);
        if (rank < 1) return new FocusUpgradeType[0];
        return Arrays.stream(possibleUpgrades(item.focusType(), rank))
                .filter(type -> canApplyUpgrade(focusStack, item.focusType(), type))
                .toArray(FocusUpgradeType[]::new);
    }

    /** ItemFocusBasic#getSortingHelper concatenates the five signed upgrade IDs. */
    public static String originalSortingHelper(ItemStack focusStack) {
        StringBuilder out = new StringBuilder();
        for (short id : getAppliedUpgrades(focusStack)) out.append(id);
        return out.toString();
    }

    /** Original ItemFocusBasic#getSortingHelper alias. */
    public static String sortingHelper(ItemStack focusStack) {
        return originalSortingHelper(focusStack);
    }

    private static void reduceToPrimals(AspectList out, Aspect aspect, int amount) {
        if (aspect == null || amount <= 0) return;
        if (aspect.isPrimal()) {
            out.add(aspect, amount);
            return;
        }
        reduceToPrimals(out, aspect.firstComponent(), amount);
        reduceToPrimals(out, aspect.secondComponent(), amount);
    }

    private static Aspect originalAspect(String name) {
        if (name == null || name.isBlank()) return null;
        return switch (name.toUpperCase(Locale.ROOT)) {
            case "AIR" -> Aspect.AER;
            case "EARTH" -> Aspect.TERRA;
            case "FIRE" -> Aspect.IGNIS;
            case "WATER" -> Aspect.AQUA;
            case "ORDER" -> Aspect.ORDO;
            case "ENTROPY" -> Aspect.PERDITIO;
            case "WEAPON" -> Aspect.TELUM;
            case "HUNGER" -> Aspect.FAMES;
            case "GREED" -> Aspect.LUCRUM;
            case "TRAVEL" -> Aspect.ITER;
            case "ENERGY" -> Aspect.POTENTIA;
            case "SLIME" -> Aspect.LIMUS;
            case "COLD" -> Aspect.GELUM;
            case "TRAP" -> Aspect.VINCULUM;
            case "CRAFT" -> Aspect.FABRICO;
            case "EXCHANGE" -> Aspect.PERMUTATIO;
            case "DARKNESS" -> Aspect.TENEBRAE;
            case "LIGHT" -> Aspect.LUX;
            case "CRYSTAL" -> Aspect.VITREUS;
            case "ARMOR" -> Aspect.TUTAMEN;
            case "LIFE" -> Aspect.VICTUS;
            case "POISON" -> Aspect.VENENUM;
            case "MAGIC" -> Aspect.PRAECANTATIO;
            case "SENSES" -> Aspect.SENSUS;
            case "MIND" -> Aspect.COGNITIO;
            case "WEATHER" -> Aspect.TEMPESTAS;
            case "MINE" -> Aspect.PERFODIO;
            default -> Aspect.byId(name);
        };
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
            case HELLBAT -> switch (rank) {
                case 1, 2, 4 -> a(FocusUpgradeType.FRUGAL, FocusUpgradeType.POTENCY);
                case 3 -> a(FocusUpgradeType.FRUGAL, FocusUpgradeType.POTENCY, FocusUpgradeType.BAT_BOMBS, FocusUpgradeType.DEVIL_BATS);
                case 5 -> a(FocusUpgradeType.FRUGAL, FocusUpgradeType.POTENCY, FocusUpgradeType.VAMPIRE_BATS);
                default -> new FocusUpgradeType[0];
            };
            case PECH_CURSE -> switch (rank) {
                case 1, 3 -> a(FocusUpgradeType.FRUGAL, FocusUpgradeType.POTENCY);
                case 2, 4 -> a(FocusUpgradeType.FRUGAL, FocusUpgradeType.POTENCY, FocusUpgradeType.EXTEND);
                case 5 -> a(FocusUpgradeType.FRUGAL, FocusUpgradeType.POTENCY, FocusUpgradeType.NIGHTSHADE);
                default -> new FocusUpgradeType[0];
            };
            case PRIMAL -> rank == 3 ? a(FocusUpgradeType.FRUGAL, FocusUpgradeType.SEEKER) : a(FocusUpgradeType.FRUGAL);
        };
    }

    public static boolean isPossible(WandFocusType focus, FocusUpgradeType type, int rank) {
        return Arrays.asList(possibleUpgrades(focus, rank)).contains(type);
    }

    /** Stage177: original canApplyUpgrade gates that affect architect/enlarge parity. */
    public static boolean canApplyUpgrade(ItemStack focusStack, WandFocusType focus, FocusUpgradeType type, int rank) {
        return rank >= 1 && rank <= MAX_RANK
                && isPossible(focus, type, rank)
                && canApplyUpgrade(focusStack, focus, type);
    }


    public static boolean canApplyUpgrade(ItemStack focusStack, WandFocusType focus, FocusUpgradeType type, int rank, Player player) {
        if (!canApplyUpgrade(focusStack, focus, type, rank)) return false;
        return focus != WandFocusType.HELLBAT || type != FocusUpgradeType.VAMPIRE_BATS
                || player == null || PlayerThaumData.hasResearch(player, "VAMPBAT");
    }

    public static boolean canApplyUpgrade(ItemStack focusStack, WandFocusType focus, FocusUpgradeType type) {
        if (focus == WandFocusType.WARDING && type == FocusUpgradeType.ENLARGE) {
            return isUpgradedWith(focusStack, FocusUpgradeType.ARCHITECT);
        }
        if (focus == WandFocusType.SHOCK && type == FocusUpgradeType.ENLARGE) {
            return isUpgradedWith(focusStack, FocusUpgradeType.CHAIN_LIGHTNING) || isUpgradedWith(focusStack, FocusUpgradeType.EARTH_SHOCK);
        }
        if (focus == WandFocusType.FIRE && type == FocusUpgradeType.ALCHEMISTS_FIRE) {
            // Original ItemFocusFire only blocks a second Alchemist's Fire rank
            // when Fireball is already installed. Rank two must remain selectable.
            return !(isUpgradedWith(focusStack, FocusUpgradeType.FIREBALL)
                    && isUpgradedWith(focusStack, FocusUpgradeType.ALCHEMISTS_FIRE));
        }
        return true;
    }

    private static FocusUpgradeType[] a(FocusUpgradeType... types) {
        return types;
    }
}
