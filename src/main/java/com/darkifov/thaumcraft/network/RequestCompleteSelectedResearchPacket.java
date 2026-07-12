package com.darkifov.thaumcraft.network;

import com.darkifov.thaumcraft.research.OriginalResearchBridge;
import com.darkifov.thaumcraft.research.OriginalResearchSelection;
import com.darkifov.thaumcraft.research.ResearchEntry;
import com.darkifov.thaumcraft.research.TC4ResearchFlagPolicy;
import com.darkifov.thaumcraft.research.TC4ResearchNoteCreator;
import net.minecraft.ChatFormatting;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.network.NetworkEvent;

import java.util.Optional;
import java.util.function.Supplier;

/**
 * Executes the exact Thaumonomicon research node clicked by the client.
 *
 * Older rebuild versions sent a separate selection packet followed by an empty
 * completion packet. If selection was rejected or delayed, the server silently
 * fell back to the first available entry and could create the wrong note. The
 * research key is now part of this single authoritative request.
 */
public class RequestCompleteSelectedResearchPacket {
    private final String researchKey;

    public RequestCompleteSelectedResearchPacket(String researchKey) {
        this.researchKey = researchKey == null ? "" : researchKey;
    }

    public static void encode(RequestCompleteSelectedResearchPacket packet, FriendlyByteBuf buffer) {
        buffer.writeUtf(packet.researchKey);
    }

    public static RequestCompleteSelectedResearchPacket decode(FriendlyByteBuf buffer) {
        return new RequestCompleteSelectedResearchPacket(buffer.readUtf());
    }

    public static void handle(RequestCompleteSelectedResearchPacket packet, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();

        context.enqueueWork(() -> {
            ServerPlayer player = context.getSender();
            if (player == null || packet.researchKey.isBlank()) {
                return;
            }

            Optional<ResearchEntry> target = OriginalResearchBridge.byKey(packet.researchKey);
            if (target.isEmpty()) {
                player.displayClientMessage(Component.literal("Unknown research key: " + packet.researchKey)
                        .withStyle(ChatFormatting.RED), false);
                ThaumcraftNetwork.syncResearch(player);
                return;
            }

            ResearchEntry entry = target.get();
            if (!OriginalResearchBridge.canUnlock(player, entry)) {
                player.displayClientMessage(Component.literal("That research is not available yet.")
                        .withStyle(ChatFormatting.GRAY), false);
                ThaumcraftNetwork.syncResearch(player);
                return;
            }

            OriginalResearchSelection.set(player, entry.key());
            ItemStack note = ItemStack.EMPTY;
            if (TC4ResearchFlagPolicy.isSecondary(entry)) {
                // TC4 secondary research is purchased directly with research aspects.
                OriginalResearchBridge.completeWithAspectCost(player, entry);
            } else if (TC4ResearchFlagPolicy.canCreateNormalResearchNote(player, entry)) {
                note = TC4ResearchNoteCreator.create(player, entry);
            } else {
                player.displayClientMessage(Component.literal("This entry is unlocked by its original trigger, not by a research note.")
                        .withStyle(ChatFormatting.GRAY), false);
            }

            ThaumcraftNetwork.syncResearch(player);
            ThaumcraftNetwork.syncAspectKnowledge(player);
            if (!note.isEmpty()) {
                ThaumcraftNetwork.openResearchNote(player, note);
            }
        });

        context.setPacketHandled(true);
    }
}
