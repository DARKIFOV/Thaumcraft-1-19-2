package com.darkifov.thaumcraft.golem;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;

import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Map;
import java.util.Set;

/**
 * Stage195 bridge for original EntityGolemBase / ItemGolemCore / ItemGolemUpgrade values.
 * It keeps TC4 metadata and NBT names explicit while the Forge 1.19.2 entity uses modern APIs.
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

    private GolemOriginalRuntime() {
    }

    public static byte[] defaultUpgrades(GolemMaterial material, boolean advanced) {
        byte[] values = new byte[material.upgradeSlots(advanced)];
        for (int i = 0; i < values.length; i++) {
            values[i] = -1;
        }
        return values;
    }

    public static byte[] defaultColors(int inventorySlots) {
        byte[] values = new byte[Math.max(0, inventorySlots)];
        for (int i = 0; i < values.length; i++) {
            values[i] = -1;
        }
        return values;
    }

    public static int upgradeAmount(Set<GolemUpgradeType> upgrades, GolemUpgradeType type) {
        return upgrades.contains(type) ? 1 : 0;
    }

    public static Map<GolemUpgradeType, Integer> upgradeCounts(byte[] originalUpgradeSlots) {
        Map<GolemUpgradeType, Integer> counts = new EnumMap<>(GolemUpgradeType.class);
        if (originalUpgradeSlots == null) {
            return counts;
        }
        for (byte value : originalUpgradeSlots) {
            GolemUpgradeType type = GolemUpgradeType.byOriginalId(value);
            if (type != null) {
                counts.put(type, counts.getOrDefault(type, 0) + 1);
            }
        }
        return counts;
    }

    public static Set<GolemUpgradeType> upgradesFromSlots(byte[] originalUpgradeSlots) {
        Set<GolemUpgradeType> set = EnumSet.noneOf(GolemUpgradeType.class);
        if (originalUpgradeSlots != null) {
            for (byte value : originalUpgradeSlots) {
                GolemUpgradeType type = GolemUpgradeType.byOriginalId(value);
                if (type != null) {
                    set.add(type);
                }
            }
        }
        return set;
    }

    public static byte[] slotsFromUpgrades(Set<GolemUpgradeType> upgrades, GolemMaterial material, boolean advanced) {
        byte[] values = defaultUpgrades(material, advanced);
        int index = 0;
        for (GolemUpgradeType type : GolemUpgradeType.values()) {
            if (upgrades.contains(type) && index < values.length) {
                values[index++] = type.originalId();
            }
        }
        return values;
    }

    public static int carryLimit(GolemMaterial material, Set<GolemUpgradeType> upgrades) {
        return material.carryLimit(upgradeAmount(upgrades, GolemUpgradeType.EARTH));
    }

    public static int inventorySlotCount(GolemMaterial material, GolemCoreType core, Set<GolemUpgradeType> upgrades) {
        if (!core.hasInventory()) {
            return 0;
        }
        int base = core.baseInventorySize();
        int earth = upgradeAmount(upgrades, GolemUpgradeType.EARTH);
        if (core == GolemCoreType.LIQUID) {
            return Math.max(1, base + earth);
        }
        return Math.max(0, base + earth * 6);
    }

    public static int strength(GolemMaterial material, Set<GolemUpgradeType> upgrades) {
        return material.strength() + upgradeAmount(upgrades, GolemUpgradeType.EARTH);
    }

    public static double attackDamage(GolemMaterial material, Set<GolemUpgradeType> upgrades, String decoration) {
        int damage = 2 + strength(material, upgrades) + upgradeAmount(upgrades, GolemUpgradeType.EARTH);
        if (decoration != null && decoration.contains("M")) {
            damage += 2;
        }
        return damage;
    }

    public static double movementSpeed(GolemMaterial material, Set<GolemUpgradeType> upgrades, String decoration, boolean advanced, boolean inWater) {
        float speed = material.speed();
        if (decoration != null && decoration.contains("B")) {
            speed *= 1.1F;
        }
        if (decoration != null && decoration.contains("P")) {
            speed *= 0.88F;
        }
        speed *= 1.0F + upgradeAmount(upgrades, GolemUpgradeType.AIR) * 0.15F;
        if (advanced) {
            speed *= 1.1F;
        }
        if (inWater && (material == GolemMaterial.STONE || material == GolemMaterial.IRON || material == GolemMaterial.THAUMIUM)) {
            speed *= 2.0F;
        }
        return speed;
    }

    public static String upgradeSlotString(byte[] slots) {
        StringBuilder builder = new StringBuilder();
        if (slots != null) {
            for (byte slot : slots) {
                builder.append(Integer.toHexString(slot));
            }
        }
        return builder.toString();
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
