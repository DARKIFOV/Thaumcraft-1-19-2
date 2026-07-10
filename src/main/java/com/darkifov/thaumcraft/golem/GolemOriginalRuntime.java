package com.darkifov.thaumcraft.golem;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;

import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Map;
import java.util.Set;

/**
 * Direct Forge 1.19.2 bridge for the persistent values used by TC4
 * EntityGolemBase, ItemGolemCore, ItemGolemUpgrade and ItemGolemBell.
 *
 * v11.62.21 makes the original byte[] upgrade slots authoritative. This is
 * important because TC4 allows two copies of the same upgrade and their effects
 * stack. A Set cannot represent that and used to silently collapse duplicates.
 */
public final class GolemOriginalRuntime {
    public static final String NBT_HOME_X = "HomeX";
    public static final String NBT_HOME_Y = "HomeY";
    public static final String NBT_HOME_Z = "HomeZ";
    public static final String NBT_HOME_FACING = "HomeFacing";
    public static final String NBT_GOLEM_TYPE = "GolemType";
    public static final String NBT_CORE = "Core";
    public static final String NBT_DECORATION = "Decoration";
    public static final String NBT_TOGGLES = "toggles";
    public static final String NBT_ADVANCED = "advanced";
    public static final String NBT_COLORS = "colors";
    public static final String NBT_UPGRADES = "upgrades";
    public static final String NBT_MARKERS = "Markers";
    public static final String NBT_INVENTORY = "Inventory";
    public static final String NBT_ITEM_CARRIED = "ItemCarried";
    public static final String NBT_ESSENTIA = "essentia";
    public static final String NBT_ESSENTIA_AMOUNT = "essentiaAmount";

    /** Original EntityGolemBase limits duplicate upgrades to two. */
    public static final int MAX_SAME_UPGRADE = 2;
    /** Original unupgraded task range before water/glasses/advanced bonuses. */
    public static final float BASE_RANGE = 16.0F;

    private GolemOriginalRuntime() {
    }

    public static byte[] defaultUpgrades(GolemMaterial material, boolean advanced) {
        byte[] values = new byte[material.upgradeSlots(advanced)];
        java.util.Arrays.fill(values, (byte) -1);
        return values;
    }

    public static byte[] normalizeUpgradeSlots(byte[] slots, GolemMaterial material, boolean advanced) {
        byte[] normalized = defaultUpgrades(material, advanced);
        if (slots == null) {
            return normalized;
        }
        int[] counts = new int[GolemUpgradeType.values().length];
        int out = 0;
        for (byte raw : slots) {
            GolemUpgradeType type = GolemUpgradeType.byOriginalId(raw);
            if (type == null || counts[type.ordinal()] >= MAX_SAME_UPGRADE || out >= normalized.length) {
                continue;
            }
            normalized[out++] = type.originalId();
            counts[type.ordinal()]++;
        }
        return normalized;
    }

    public static boolean installUpgrade(byte[] slots, GolemUpgradeType type) {
        if (slots == null || type == null || upgradeAmount(slots, type) >= MAX_SAME_UPGRADE) {
            return false;
        }
        for (int i = 0; i < slots.length; i++) {
            if (slots[i] < 0) {
                slots[i] = type.originalId();
                return true;
            }
        }
        return false;
    }

    public static GolemUpgradeType removeUpgrade(byte[] slots, int slot) {
        if (slots == null || slot < 0 || slot >= slots.length) {
            return null;
        }
        GolemUpgradeType removed = GolemUpgradeType.byOriginalId(slots[slot]);
        slots[slot] = -1;
        return removed;
    }

    public static byte[] defaultColors(int inventorySlots) {
        byte[] values = new byte[Math.max(0, inventorySlots)];
        java.util.Arrays.fill(values, (byte) -1);
        return values;
    }

    public static int upgradeAmount(byte[] slots, GolemUpgradeType type) {
        if (slots == null || type == null) {
            return 0;
        }
        int count = 0;
        for (byte value : slots) {
            if (value == type.originalId()) {
                count++;
            }
        }
        return count;
    }

    public static boolean hasUpgrade(byte[] slots, GolemUpgradeType type) {
        return upgradeAmount(slots, type) > 0;
    }

    public static int occupiedUpgradeSlots(byte[] slots) {
        int count = 0;
        if (slots != null) {
            for (byte slot : slots) {
                if (GolemUpgradeType.byOriginalId(slot) != null) {
                    count++;
                }
            }
        }
        return count;
    }

    public static Map<GolemUpgradeType, Integer> upgradeCounts(byte[] slots) {
        Map<GolemUpgradeType, Integer> counts = new EnumMap<>(GolemUpgradeType.class);
        if (slots != null) {
            for (byte value : slots) {
                GolemUpgradeType type = GolemUpgradeType.byOriginalId(value);
                if (type != null) {
                    counts.put(type, counts.getOrDefault(type, 0) + 1);
                }
            }
        }
        return counts;
    }

    /** Compatibility view for old UI/render code; never use it for counting. */
    public static Set<GolemUpgradeType> upgradesFromSlots(byte[] slots) {
        Set<GolemUpgradeType> set = EnumSet.noneOf(GolemUpgradeType.class);
        set.addAll(upgradeCounts(slots).keySet());
        return set;
    }

    /** Legacy migration helper. New saves must preserve byte[] directly. */
    public static byte[] slotsFromUpgrades(Set<GolemUpgradeType> upgrades, GolemMaterial material, boolean advanced) {
        byte[] values = defaultUpgrades(material, advanced);
        int index = 0;
        if (upgrades != null) {
            for (GolemUpgradeType type : GolemUpgradeType.values()) {
                if (upgrades.contains(type) && index < values.length) {
                    values[index++] = type.originalId();
                }
            }
        }
        return values;
    }

    public static int carryLimit(GolemMaterial material, byte[] slots) {
        return material.carryLimit(upgradeAmount(slots, GolemUpgradeType.EARTH));
    }

    public static int fluidCarryLimit(GolemMaterial material, byte[] slots) {
        return (int) Math.floor(Math.sqrt(carryLimit(material, slots))) * 1000;
    }

    public static int inventorySlotCount(GolemMaterial material, GolemCoreType core, byte[] slots) {
        if (!core.hasInventory()) {
            return 0;
        }
        int earth = upgradeAmount(slots, GolemUpgradeType.EARTH);
        if (core == GolemCoreType.LIQUID) {
            return Math.max(1, 1 + earth);
        }
        return Math.max(0, 6 + earth * 6);
    }

    public static int strength(GolemMaterial material, byte[] slots) {
        return material.strength() + upgradeAmount(slots, GolemUpgradeType.EARTH);
    }

    /** TC4: 2 + getGolemStrength() + earth count, plus Mace decoration. */
    public static double attackDamage(GolemMaterial material, byte[] slots, String decoration) {
        int damage = 2 + strength(material, slots) + upgradeAmount(slots, GolemUpgradeType.EARTH);
        if (decoration != null && decoration.contains("M")) {
            damage += 2;
        }
        return damage;
    }

    public static double maxHealth(GolemMaterial material, String decoration) {
        return material.health() + (decoration != null && decoration.contains("H") ? 5 : 0);
    }

    public static double movementSpeed(GolemMaterial material, byte[] slots, String decoration, boolean advanced, boolean inWater) {
        float speed = material.speed();
        if (decoration != null && decoration.contains("B")) {
            speed *= 1.1F;
        }
        if (decoration != null && decoration.contains("P")) {
            speed *= 0.88F;
        }
        speed *= 1.0F + upgradeAmount(slots, GolemUpgradeType.AIR) * 0.15F;
        if (advanced) {
            speed *= 1.1F;
        }
        if (inWater && (material == GolemMaterial.STONE || material == GolemMaterial.IRON || material == GolemMaterial.THAUMIUM)) {
            speed *= 2.0F;
        }
        return speed;
    }

    /** TC4 EntityGolemBase#getRange. */
    public static float workRange(byte[] slots, String decoration, boolean advanced) {
        float range = BASE_RANGE + upgradeAmount(slots, GolemUpgradeType.WATER) * 4.0F;
        if (decoration != null && decoration.contains("G")) {
            range += Math.max(range * 0.1F, 1.0F);
        }
        if (advanced) {
            range += Math.max(range * 0.2F, 2.0F);
        }
        return range;
    }

    public static int regenDelay(GolemMaterial material, String decoration) {
        int delay = material.regenDelay();
        if (decoration != null && decoration.contains("F")) {
            delay = Math.max(1, (int) (delay * 0.66F));
        }
        return delay;
    }

    public static int attackFireSeconds(byte[] slots) {
        return upgradeAmount(slots, GolemUpgradeType.FIRE) * 4;
    }

    public static float entropyRetaliationDamage(byte[] slots, net.minecraft.util.RandomSource random) {
        int entropy = upgradeAmount(slots, GolemUpgradeType.ENTROPY);
        if (entropy <= 0) {
            return 0.0F;
        }
        return entropy * 2.0F + random.nextInt(2 * entropy);
    }

    public static String upgradeSlotString(byte[] slots) {
        StringBuilder builder = new StringBuilder();
        if (slots != null) {
            for (byte slot : slots) {
                builder.append(slot < 0 ? 'f' : Integer.toHexString(slot));
            }
        }
        return builder.toString();
    }

    public static String upgradeDescription(byte[] slots) {
        StringBuilder builder = new StringBuilder();
        for (Map.Entry<GolemUpgradeType, Integer> entry : upgradeCounts(slots).entrySet()) {
            if (builder.length() > 0) {
                builder.append(',');
            }
            builder.append(entry.getKey().id()).append('x').append(entry.getValue());
        }
        return builder.length() == 0 ? "none" : builder.toString();
    }

    public static String colorSlotString(byte[] colors) {
        StringBuilder builder = new StringBuilder();
        if (colors != null) {
            for (byte color : colors) {
                builder.append(color == -1 ? 'h' : Integer.toHexString(color));
            }
        }
        return builder.toString();
    }

    public static void writeOriginalMarker(CompoundTag markerTag, int x, int y, int z, int dim, byte side, byte color) {
        markerTag.putInt("x", x);
        markerTag.putInt("y", y);
        markerTag.putInt("z", z);
        markerTag.putInt("dim", dim);
        markerTag.putByte("side", side);
        markerTag.putByte("color", color);
    }

    public static ListTag emptyOriginalMarkerList() {
        return new ListTag();
    }
}
