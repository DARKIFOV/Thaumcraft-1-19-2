package com.darkifov.thaumcraft.network;

import com.darkifov.thaumcraft.research.OriginalResearchBridge;
import com.darkifov.thaumcraft.research.OriginalResearchSelection;
import com.darkifov.thaumcraft.research.ResearchEntry;
import com.darkifov.thaumcraft.research.TC4ResearchFlagPolicy;
import com.darkifov.thaumcraft.research.TC4ResearchNoteCreator;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
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
                ThaumcraftNetwork.syncResearch(player);
                return;
            }

            ResearchEntry entry = target.get();
            if (!OriginalResearchBridge.canUnlock(player, entry)) {
                ThaumcraftNetwork.syncResearch(player);
                return;
            }

            OriginalResearchSelection.set(player, entry.key());
            if (TC4ResearchFlagPolicy.isSecondary(entry)) {
                // TC4 secondary research is purchased directly with research aspects.
                OriginalResearchBridge.completeSecondaryFromThaumonomicon(player, entry);
            } else if (TC4ResearchFlagPolicy.canCreateNormalResearchNote(player, entry)) {
                TC4ResearchNoteCreator.create(player, entry);
            }

            ThaumcraftNetwork.syncResearch(player);
            ThaumcraftNetwork.syncAspectKnowledge(player);
            // TC4 creates the targeted note in the player inventory and shows
            // the research popup. Editing starts only after the note is placed
            // into a Research Table; do not open a rebuild-only freehand GUI.
        });

        context.setPacketHandled(true);
    }
}
