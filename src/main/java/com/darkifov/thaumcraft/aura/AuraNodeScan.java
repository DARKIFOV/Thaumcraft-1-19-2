package com.darkifov.thaumcraft.aura;

import com.darkifov.thaumcraft.AspectStack;
import com.darkifov.thaumcraft.blockentity.AuraNodeBlockEntity;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public final class AuraNodeScan {
    private AuraNodeScan() {
    }

    public static void sendScan(Player player, AuraNodeBlockEntity node) {
        if (player == null || node == null) {
            return;
        }

        AuraNodeType type = AuraNodeType.fromName(node.nodeType());
        int total = node.aspects().totalAmount();

        player.displayClientMessage(Component.literal("Aura Node").withStyle(ChatFormatting.AQUA)
                .append(Component.literal(" [" + type.displayName() + "]").withStyle(ChatFormatting.LIGHT_PURPLE)), false);
        player.displayClientMessage(Component.literal("Vis: " + total).withStyle(ChatFormatting.GOLD), false);

        List<AspectStack> stacks = new ArrayList<>(node.aspects().all());
        stacks.sort(Comparator.comparing(stack -> stack.aspect().id()));

        if (stacks.isEmpty()) {
            player.displayClientMessage(Component.literal("Aspects: none").withStyle(ChatFormatting.GRAY), false);
            return;
        }

        StringBuilder builder = new StringBuilder();
        for (AspectStack stack : stacks) {
            if (!builder.isEmpty()) {
                builder.append(", ");
            }
            builder.append(stack.aspect().id()).append(" ").append(stack.amount());
        }

        player.displayClientMessage(Component.literal("Aspects: " + builder).withStyle(ChatFormatting.GRAY), false);
    }
}
