package com.darkifov.thaumcraft.runic;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.List;
import java.util.Map;

/**
 * Stage213 1.19.2 adapter for original TC4 IWarpingGear semantics.
 *
 * TC4 exposed warp through item classes.  The 1.19.2 port has many flattened
 * legacy item ids, so this table keeps the original item-level warp values in a
 * source-compatible helper instead of reintroducing the 1.7.10 interface.
 */
public final class TC4WarpingGearAdapter {
    private static final Map<String, Integer> WARP_BY_ITEM = Map.ofEntries(
            Map.entry("thaumcraft:tc4_voidboots", 1),
            Map.entry("thaumcraft:tc4_voidchest", 1),
            Map.entry("thaumcraft:tc4_voidhelm", 1),
            Map.entry("thaumcraft:tc4_voidlegs", 1),
            Map.entry("thaumcraft:tc4_voidrobechest", 1),
            Map.entry("thaumcraft:tc4_voidrobechestover", 1),
            Map.entry("thaumcraft:tc4_voidrobehelm", 1),
            Map.entry("thaumcraft:tc4_voidrobelegs", 1),
            Map.entry("thaumcraft:tc4_voidrobelegsover", 1),
            Map.entry("thaumcraft:tc4_voidrobeboots", 1),
            Map.entry("thaumcraft:tc4_cultistboots", 1),
            Map.entry("thaumcraft:tc4_cultistrobechest", 1),
            Map.entry("thaumcraft:tc4_cultistrobehelm", 1),
            Map.entry("thaumcraft:tc4_cultistrobelegs", 1),
            Map.entry("thaumcraft:tc4_cultistplatechest", 1),
            Map.entry("thaumcraft:tc4_cultistplatehelm", 1),
            Map.entry("thaumcraft:tc4_cultistplatelegs", 1),
            Map.entry("thaumcraft:tc4_crimsonsword", 1),
            Map.entry("thaumcraft:tc4_primalcrusher", 2),
            Map.entry("thaumcraft:tc4_voidaxe", 1),
            Map.entry("thaumcraft:tc4_voidhoe", 1),
            Map.entry("thaumcraft:tc4_voidpick", 1),
            Map.entry("thaumcraft:tc4_voidshovel", 1),
            Map.entry("thaumcraft:tc4_voidsword", 1)
    );

    private TC4WarpingGearAdapter() {
    }

    public static int getWarp(ItemStack stack, Player player) {
        String id = registryId(stack);
        if (id == null) {
            return 0;
        }
        return WARP_BY_ITEM.getOrDefault(id, 0);
    }


    /**
     * Original WarpEvents#getWarpFromGear: held item + four armor slots + four
     * Baubles slots. Offhand is intentionally excluded because TC4 had no
     * offhand slot and only inspected getCurrentEquippedItem().
     */
    public static int getEquippedWarp(ServerPlayer player) {
        if (player == null) {
            return 0;
        }

        int warp = getWarp(player.getMainHandItem(), player);
        for (ItemStack armor : player.getArmorSlots()) {
            warp += getWarp(armor, player);
        }
        for (ItemStack bauble : TC4BaubleSlotAdapter.findEquippedBaubles(player)) {
            warp += getWarp(bauble, player);
        }
        return Math.max(0, warp);
    }

    public static void appendTooltip(ItemStack stack, Player player, List<Component> tooltip) {
        int warp = getWarp(stack, player);
        if (warp > 0) {
            tooltip.add(Component.literal("Warping " + warp).withStyle(ChatFormatting.DARK_PURPLE));
        }
    }

    private static String registryId(ItemStack stack) {
        if (stack == null || stack.isEmpty()) {
            return null;
        }
        ResourceLocation id = ForgeRegistries.ITEMS.getKey(stack.getItem());
        return id == null ? null : id.toString();
    }
}
