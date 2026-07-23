package com.darkifov.thaumcraft.item.simple;

import com.darkifov.thaumcraft.Aspect;
import com.darkifov.thaumcraft.aura.AuraVisRelayNetwork;
import com.darkifov.thaumcraft.block.WandItem;
import com.darkifov.thaumcraft.runic.TC4BaubleSlotAdapter;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Set;

/** Server-authoritative replacement for ItemAmuletVis#onWornTick. */
public final class TC4VisAmuletRuntime {
    private TC4VisAmuletRuntime() {
    }

    public static void tick(ServerPlayer player) {
        if (player.tickCount % 5 != 0 || !(player.level instanceof ServerLevel level)) return;

        List<ItemStack> equipped = equippedAmulets(player);
        ItemStack wandStack = player.getMainHandItem();
        WandItem wand = wandStack.getItem() instanceof WandItem heldWand ? heldWand : null;

        for (ItemStack amuletStack : equipped) {
            if (!(amuletStack.getItem() instanceof TC4VisAmuletItem amulet)) continue;

            // TC4 first moved up to 5 cv/aspect from the amulet into the held wand.
            if (wand != null) {
                for (Aspect aspect : WandItem.primalVisAspects()) {
                    int stored = amulet.getVis(amuletStack, aspect);
                    int room = Math.max(0, wand.stackVisCapacity(wandStack) - WandItem.getVis(wandStack, aspect));
                    int moved = Math.min(5, Math.min(stored, room));
                    if (moved > 0) {
                        amulet.setVis(amuletStack, aspect, stored - moved);
                        WandItem.addRealVis(wandStack, aspect, moved);
                    }
                }
            }

            // Relay recharge is independent of holding a wand in the original item.
            AuraVisRelayNetwork.chargeAmuletFromNearestRelay(level, player, amuletStack, amulet);
        }
    }

    private static List<ItemStack> equippedAmulets(ServerPlayer player) {
        List<ItemStack> out = new ArrayList<>();
        Set<ItemStack> seen = java.util.Collections.newSetFromMap(new IdentityHashMap<>());
        for (ItemStack stack : TC4BaubleSlotAdapter.findEquippedBaubles(player)) {
            if (stack.getItem() instanceof TC4VisAmuletItem && seen.add(stack)) out.add(stack);
        }
        if (out.size() >= TC4BaubleSlotAdapter.TC4_BAUBLE_SLOT_LIMIT) return out;
        ItemStack offhand = player.getOffhandItem();
        if (offhand.getItem() instanceof TC4VisAmuletItem && seen.add(offhand)) out.add(offhand);
        for (ItemStack stack : player.getInventory().items) {
            if (out.size() >= TC4BaubleSlotAdapter.TC4_BAUBLE_SLOT_LIMIT) break;
            if (stack.getItem() instanceof TC4VisAmuletItem && seen.add(stack)) out.add(stack);
        }
        return out;
    }
}
