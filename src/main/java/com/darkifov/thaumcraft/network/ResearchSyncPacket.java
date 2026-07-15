package com.darkifov.thaumcraft.network;

import com.darkifov.thaumcraft.client.ClientResearchData;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.HashSet;
import java.util.Set;
import java.util.function.Supplier;

/**
 * Synchronizes research and the complete TC4 warp state. Earlier rebuild
 * versions sent only total warp, which made client diagnostics and future
 * overlays unable to distinguish permanent, sticky and temporary buckets.
 */
public class ResearchSyncPacket {
    private final Set<String> research;
    private final int totalWarp;
    private final int permanentWarp;
    private final int stickyWarp;
    private final int temporaryWarp;
    private final int warpCounter;

    public ResearchSyncPacket(Set<String> research, int totalWarp, int permanentWarp,
                              int stickyWarp, int temporaryWarp, int warpCounter) {
        this.research = research == null ? new HashSet<>() : new HashSet<>(research);
        this.totalWarp = Math.max(0, totalWarp);
        this.permanentWarp = Math.max(0, permanentWarp);
        this.stickyWarp = Math.max(0, stickyWarp);
        this.temporaryWarp = Math.max(0, temporaryWarp);
        this.warpCounter = Math.max(0, warpCounter);
    }

    public static void encode(ResearchSyncPacket packet, FriendlyByteBuf buffer) {
        buffer.writeVarInt(packet.research.size());

        for (String key : packet.research) {
            buffer.writeUtf(key);
        }

        buffer.writeVarInt(packet.totalWarp);
        buffer.writeVarInt(packet.permanentWarp);
        buffer.writeVarInt(packet.stickyWarp);
        buffer.writeVarInt(packet.temporaryWarp);
        buffer.writeVarInt(packet.warpCounter);
    }

    public static ResearchSyncPacket decode(FriendlyByteBuf buffer) {
        int size = buffer.readVarInt();
        Set<String> research = new HashSet<>();

        for (int i = 0; i < size; i++) {
            research.add(buffer.readUtf());
        }

        int totalWarp = buffer.readVarInt();
        int permanentWarp = buffer.readVarInt();
        int stickyWarp = buffer.readVarInt();
        int temporaryWarp = buffer.readVarInt();
        int warpCounter = buffer.readVarInt();
        return new ResearchSyncPacket(research, totalWarp, permanentWarp, stickyWarp, temporaryWarp, warpCounter);
    }

    public static void handle(ResearchSyncPacket packet, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();

        context.enqueueWork(() -> ClientResearchData.setResearch(
                packet.research,
                packet.totalWarp,
                packet.permanentWarp,
                packet.stickyWarp,
                packet.temporaryWarp,
                packet.warpCounter
        ));
        context.setPacketHandled(true);
    }
}
