package com.darkifov.thaumcraft.network;

import com.darkifov.thaumcraft.client.ClientResearchData;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.HashSet;
import java.util.Set;
import java.util.function.Supplier;

public class ResearchSyncPacket {
    private final Set<String> research;
    private final int warp;

    public ResearchSyncPacket(Set<String> research, int warp) {
        this.research = research == null ? new HashSet<>() : new HashSet<>(research);
        this.warp = warp;
    }

    public static void encode(ResearchSyncPacket packet, FriendlyByteBuf buffer) {
        buffer.writeVarInt(packet.research.size());

        for (String key : packet.research) {
            buffer.writeUtf(key);
        }

        buffer.writeVarInt(packet.warp);
    }

    public static ResearchSyncPacket decode(FriendlyByteBuf buffer) {
        int size = buffer.readVarInt();
        Set<String> research = new HashSet<>();

        for (int i = 0; i < size; i++) {
            research.add(buffer.readUtf());
        }

        int warp = buffer.readVarInt();
        return new ResearchSyncPacket(research, warp);
    }

    public static void handle(ResearchSyncPacket packet, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();

        context.enqueueWork(() -> ClientResearchData.setResearch(packet.research, packet.warp));
        context.setPacketHandled(true);
    }
}
