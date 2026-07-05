package com.darkifov.thaumcraft.research;

import com.darkifov.thaumcraft.data.PlayerThaumData;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.HashMap;
import java.util.Map;

public final class ResearchLocks {
    private static final Map<String, String> ITEM_LOCKS = new HashMap<>();

    static {
        ITEM_LOCKS.put("thaumcraft:golem_core", "GOLEMS");
        ITEM_LOCKS.put("thaumcraft:golem_bell", "GOLEMS");
        ITEM_LOCKS.put("thaumcraft:golem_seal_collect", "GOLEMS");
        ITEM_LOCKS.put("thaumcraft:golem_seal_collect_block", "GOLEMS");
        ITEM_LOCKS.put("thaumcraft:taint_seed", "TAINT");
        ITEM_LOCKS.put("thaumcraft:warp_charm", "WARP");
        ITEM_LOCKS.put("thaumcraft:infusion_core", "INFUSION");
        ITEM_LOCKS.put("thaumcraft:unstable_singularity", "INFUSION");
        ITEM_LOCKS.put("thaumcraft:void_metal_ingot", "INFUSION");
    }

    private ResearchLocks() {
    }

    public static String requiredResearch(ItemStack stack) {
        if (stack == null || stack.isEmpty()) {
            return "";
        }

        var id = ForgeRegistries.ITEMS.getKey(stack.getItem());

        if (id == null) {
            return "";
        }

        return ITEM_LOCKS.getOrDefault(id.toString(), "");
    }

    public static boolean canUse(Player player, ItemStack stack) {
        String required = requiredResearch(stack);
        return required.isEmpty() || PlayerThaumData.hasResearch(player, required);
    }
}
