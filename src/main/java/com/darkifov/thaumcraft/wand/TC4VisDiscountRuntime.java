package com.darkifov.thaumcraft.wand;

import com.darkifov.thaumcraft.Aspect;
import com.darkifov.thaumcraft.runic.TC4BaubleSlotAdapter;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

/**
 * Strict Forge 1.19.2 adapter for TC4 WandManager#getTotalVisDiscount.
 *
 * <p>TC4 sums all {@link TC4VisDiscountGear} in four armor slots and four
 * Baubles slots, then divides the integer percentage by 100. This port scans
 * vanilla armor on both logical sides and the optional reflective Curios /
 * legacy-Baubles bridge on the authoritative server. No hard accessory-mod
 * dependency is introduced.</p>
 */
public final class TC4VisDiscountRuntime {
    private TC4VisDiscountRuntime() {
    }

    public static float totalDiscount(Player player, Aspect aspect) {
        if (player == null) {
            return 0.0F;
        }

        int totalPercent = 0;
        for (ItemStack armor : player.getArmorSlots()) {
            totalPercent += discountPercent(armor, player, aspect);
        }

        if (player instanceof ServerPlayer serverPlayer) {
            for (ItemStack bauble : TC4BaubleSlotAdapter.findEquippedBaubles(serverPlayer)) {
                totalPercent += discountPercent(bauble, player, aspect);
            }
        }

        return Math.max(0, totalPercent) / 100.0F;
    }

    private static int discountPercent(ItemStack stack, Player player, Aspect aspect) {
        if (stack == null || stack.isEmpty() || !(stack.getItem() instanceof TC4VisDiscountGear gear)) {
            return 0;
        }
        return Math.max(0, gear.getVisDiscount(stack, player, aspect));
    }
}
