package com.darkifov.thaumcraft.network;

import com.darkifov.thaumcraft.research.OriginalResearchBridge;
import com.darkifov.thaumcraft.research.ResearchEntry;
import net.minecraft.ChatFormatting;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.network.NetworkEvent;

import java.util.Optional;
import java.util.function.Supplier;

public class RequestCompleteSelectedResearchPacket {
    public RequestCompleteSelectedResearchPacket() {
    }

    public static void encode(RequestCompleteSelectedResearchPacket packet, FriendlyByteBuf buffer) {
    }

    public static RequestCompleteSelectedResearchPacket decode(FriendlyByteBuf buffer) {
        return new RequestCompleteSelectedResearchPacket();
    }

    public static void handle(RequestCompleteSelectedResearchPacket packet, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();

        context.enqueueWork(() -> {
            ServerPlayer player = context.getSender();

            if (player == null) {
                return;
            }

            Optional<ResearchEntry> target = OriginalResearchBridge.selectedOrFirstAvailable(player);

            if (target.isEmpty()) {
                player.displayClientMessage(Component.literal("No selected or available research.").withStyle(ChatFormatting.GRAY), false);
                ThaumcraftNetwork.syncResearch(player);
                return;
            }

            int pointSlot = findResearchPoint(player);

            if (pointSlot < 0 && !player.getAbilities().instabuild) {
                player.displayClientMessage(Component.literal("You need a Research Point.").withStyle(ChatFormatting.RED), false);
                ThaumcraftNetwork.syncResearch(player);
                return;
            }

            boolean completed = OriginalResearchBridge.completeWithAspectCost(player, target.get());

            if (completed && pointSlot >= 0 && !player.getAbilities().instabuild) {
                player.getInventory().getItem(pointSlot).shrink(1);
            }

            if (!completed) {
                player.displayClientMessage(Component.literal("Selected research cannot be completed yet.").withStyle(ChatFormatting.RED), false);
            }

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
