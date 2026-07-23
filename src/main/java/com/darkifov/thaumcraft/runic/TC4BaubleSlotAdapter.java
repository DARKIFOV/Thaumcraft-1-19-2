package com.darkifov.thaumcraft.runic;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Optional 1.19.2 Curios/Baubles bridge for TC4 runic baubles.
 * No hard dependency is declared: all access is reflective and silently falls
 * back to the Stage211 offhand/inventory mirror when no accessory API exists.
 */
public final class TC4BaubleSlotAdapter {
    public static final int TC4_BAUBLE_SLOT_LIMIT = 4;
    public static final String CURIOS_API_CLASS = "top.theillusivec4.curios.api.CuriosApi";
    public static final String LEGACY_BAUBLES_API_CLASS = "baubles.api.BaublesApi";

    private TC4BaubleSlotAdapter() {
    }

    public static List<ItemStack> findEquippedBaubles(Player player) {
        List<ItemStack> stacks = new ArrayList<>();
        stacks.addAll(readCurios(player));
        if (stacks.size() < TC4_BAUBLE_SLOT_LIMIT) {
            stacks.addAll(readLegacyBaubles(player));
        }
        if (stacks.size() > TC4_BAUBLE_SLOT_LIMIT) {
            return new ArrayList<>(stacks.subList(0, TC4_BAUBLE_SLOT_LIMIT));
        }
        return stacks;
    }

    private static List<ItemStack> readCurios(Player player) {
        List<ItemStack> stacks = new ArrayList<>();
        try {
            Class<?> curiosApi = Class.forName(CURIOS_API_CLASS);
            Method getCuriosInventory = curiosApi.getMethod("getCuriosInventory", net.minecraft.world.entity.LivingEntity.class);
            Object lazyOptional = getCuriosInventory.invoke(null, player);
            Optional<?> inventoryOpt = resolveLazyOptional(lazyOptional);
            if (inventoryOpt.isEmpty()) {
                return stacks;
            }
            Object curiosInventory = inventoryOpt.get();
            Method getCurios = curiosInventory.getClass().getMethod("getCurios");
            Object mapObject = getCurios.invoke(curiosInventory);
            if (!(mapObject instanceof Map<?, ?> map)) {
                return stacks;
            }
            for (Object handler : map.values()) {
                Object itemHandler = handler.getClass().getMethod("getStacks").invoke(handler);
                readItemHandler(itemHandler, stacks);
                if (stacks.size() >= TC4_BAUBLE_SLOT_LIMIT) {
                    break;
                }
            }
        } catch (ReflectiveOperationException | LinkageError ignored) {
            // Optional dependency absent or incompatible. The inventory mirror remains active.
        }
        return stacks;
    }

    private static Optional<?> resolveLazyOptional(Object lazyOptional) throws ReflectiveOperationException {
        if (lazyOptional == null) {
            return Optional.empty();
        }
        Object resolved = lazyOptional.getClass().getMethod("resolve").invoke(lazyOptional);
        return resolved instanceof Optional<?> opt ? opt : Optional.empty();
    }

    private static List<ItemStack> readLegacyBaubles(Player player) {
        List<ItemStack> stacks = new ArrayList<>();
        try {
            Class<?> baublesApi = Class.forName(LEGACY_BAUBLES_API_CLASS);
            Object inventory = baublesApi.getMethod("getBaubles", net.minecraft.world.entity.player.Player.class).invoke(null, player);
            readItemHandler(inventory, stacks);
        } catch (ReflectiveOperationException | LinkageError ignored) {
            // Baubles does not exist for normal Forge 1.19.2; this is for compatibility shims only.
        }
        return stacks;
    }

    private static void readItemHandler(Object itemHandler, List<ItemStack> out) throws ReflectiveOperationException {
        if (itemHandler == null) {
            return;
        }
        Method getSlots = itemHandler.getClass().getMethod("getSlots");
        Method getStackInSlot = itemHandler.getClass().getMethod("getStackInSlot", int.class);
        int slots = Math.min(TC4_BAUBLE_SLOT_LIMIT, Math.max(0, ((Number) getSlots.invoke(itemHandler)).intValue()));
        for (int i = 0; i < slots && out.size() < TC4_BAUBLE_SLOT_LIMIT; i++) {
            Object stack = getStackInSlot.invoke(itemHandler, i);
            if (stack instanceof ItemStack itemStack && !itemStack.isEmpty()) {
                out.add(itemStack);
            }
        }
    }
}
