package com.darkifov.thaumcraft.network;

import com.darkifov.thaumcraft.research.OriginalResearchBridge;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class RequestSelectResearchPacket {
    private final String researchKey;

    public RequestSelectResearchPacket(String researchKey) {
        this.researchKey = researchKey == null ? "" : researchKey;
    }

    public static void encode(RequestSelectResearchPacket packet, FriendlyByteBuf buffer) {
        buffer.writeUtf(packet.researchKey);
    }

    public static RequestSelectResearchPacket decode(FriendlyByteBuf buffer) {
        return new RequestSelectResearchPacket(buffer.readUtf());
    }

    public static void handle(RequestSelectResearchPacket packet, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> {
            ServerPlayer player = context.getSender();
            if (player == null || packet.researchKey.isBlank()) {
                return;
            }
            OriginalResearchBridge.selectForResearchTable(player, packet.researchKey);
            ThaumcraftNetwork.syncResearch(player);
        });
        context.setPacketHandled(true);
    }
}
