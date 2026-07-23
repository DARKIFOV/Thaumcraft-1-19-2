package com.darkifov.thaumcraft.client;

import com.darkifov.thaumcraft.ThaumcraftMod;
import com.darkifov.thaumcraft.item.gear.HoverHarnessItem;
import com.darkifov.thaumcraft.network.ThaumcraftNetwork;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/** Sends the H-key request only when the local player is actually wearing a harness. */
@Mod.EventBusSubscriber(modid = ThaumcraftMod.MOD_ID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class ClientHoverEvents {
    private ClientHoverEvents() {
    }

    @SubscribeEvent
    public static void onKey(InputEvent.Key event) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player == null || minecraft.screen != null) {
            return;
        }
        while (ClientHoverKeybinds.KEY_TOGGLE_HOVER.consumeClick()) {
            if (minecraft.player.getItemBySlot(EquipmentSlot.CHEST).getItem() instanceof HoverHarnessItem) {
                ThaumcraftNetwork.requestHoverToggleFromClient();
            }
        }
    }
}
