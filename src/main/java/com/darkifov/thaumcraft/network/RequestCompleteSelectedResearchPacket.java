package com.darkifov.thaumcraft.network;

import com.darkifov.thaumcraft.research.OriginalResearchBridge;
import com.darkifov.thaumcraft.research.ResearchEntry;
import com.darkifov.thaumcraft.research.TC4ResearchNoteCreator;
import net.minecraft.ChatFormatting;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.network.NetworkEvent;

import java.util.Optional;
import java.util.function.Supplier;

public class RequestCompleteSelectedResearchPacket {
    // Stage118 replaces the old findResearchPoint / completeWithAspectCost instant-unlock path with TC4 research-note creation.

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

            ItemStack note = TC4ResearchNoteCreator.create(player, target.get());
            ThaumcraftNetwork.syncResearch(player);
            ThaumcraftNetwork.syncAspectKnowledge(player);
            if (!note.isEmpty()) {
                ThaumcraftNetwork.openResearchNote(player, note);
            }
        });

        context.setPacketHandled(true);
    }
}
