package com.darkifov.thaumcraft.aura;

import com.darkifov.thaumcraft.blockentity.AuraNodeBlockEntity;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;


public final class AuraNodeScan {
    private AuraNodeScan() {
    }

    public static void sendScan(Player player, AuraNodeBlockEntity node) {
        if (player == null || node == null) {
            return;
        }

        player.displayClientMessage(TC4AuraNodeScanParity.header(node, false), false);
        player.displayClientMessage(TC4AuraNodeScanParity.visLine(node), false);
        player.displayClientMessage(TC4AuraNodeScanParity.aspectLine(node), false);
    }
}
