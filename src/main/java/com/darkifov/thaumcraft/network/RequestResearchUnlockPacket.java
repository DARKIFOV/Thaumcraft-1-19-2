package com.darkifov.thaumcraft.network;

import com.darkifov.thaumcraft.research.OriginalResearchBridge;
import com.darkifov.thaumcraft.research.ResearchEntry;
import com.darkifov.thaumcraft.research.ResearchRegistry;
import net.minecraft.ChatFormatting;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class RequestResearchUnlockPacket {
    public RequestResearchUnlockPacket() {
    }

    public static void encode(RequestResearchUnlockPacket packet, FriendlyByteBuf buffer) {
    }

    public static RequestResearchUnlockPacket decode(FriendlyByteBuf buffer) {
        return new RequestResearchUnlockPacket();
    }

    public static void handle(RequestResearchUnlockPacket packet, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();

        context.enqueueWork(() -> {
            ServerPlayer player = context.getSender();

            if (player == null) {
                return;
            }

            int pointSlot = findResearchPoint(player);

            if (pointSlot < 0 && !player.getAbilities().instabuild) {
                player.displayClientMessage(Component.literal("You need a Research Point.").withStyle(ChatFormatting.RED), false);
                return;
            }

            ResearchEntry target = OriginalResearchBridge.selectedOrFirstAvailable(player).orElse(null);

            if (target != null && OriginalResearchBridge.completeWithAspectCost(player, target)) {
                if (!player.getAbilities().instabuild) {
                    player.getInventory().getItem(pointSlot).shrink(1);
                }

                ThaumcraftNetwork.syncResearch(player);
                return;
            }

            player.displayClientMessage(Component.literal("No selected or available research to unlock right now.").withStyle(ChatFormatting.GRAY), false);
            ThaumcraftNetwork.syncResearch(player);
        });

        context.setPacketHandled(true);
    }

    private static int findResearchPoint(ServerPlayer player) {
        for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
            ItemStack stack = player.getInventory().getItem(i);

            if (stack.getItem().builtInRegistryHolder().key().location().toString().equals("thaumcraft:research_point")) {
                return i;
            }
        }

        return -1;
    }

}
