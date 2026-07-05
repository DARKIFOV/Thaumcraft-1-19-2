package com.darkifov.thaumcraft.network;

import com.darkifov.thaumcraft.Aspect;
import com.darkifov.thaumcraft.research.ResearchTableFoundation;
import net.minecraft.ChatFormatting;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class RequestCombineAspectsPacket {
    private final String firstId;
    private final String secondId;

    public RequestCombineAspectsPacket(String firstId, String secondId) {
        this.firstId = firstId == null ? "" : firstId;
        this.secondId = secondId == null ? "" : secondId;
    }

    public static void encode(RequestCombineAspectsPacket packet, FriendlyByteBuf buffer) {
        buffer.writeUtf(packet.firstId);
        buffer.writeUtf(packet.secondId);
    }

    public static RequestCombineAspectsPacket decode(FriendlyByteBuf buffer) {
        return new RequestCombineAspectsPacket(buffer.readUtf(), buffer.readUtf());
    }

    public static void handle(RequestCombineAspectsPacket packet, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();

        context.enqueueWork(() -> {
            ServerPlayer player = context.getSender();

            if (player == null) {
                return;
            }

            Aspect first = Aspect.byId(packet.firstId);
            Aspect second = Aspect.byId(packet.secondId);

            if (first == null || second == null) {
                player.displayClientMessage(Component.literal("Unknown aspect combination.").withStyle(ChatFormatting.RED), false);
                ThaumcraftNetwork.syncAspectKnowledge(player);
                return;
            }

            ResearchTableFoundation.combine(player, first, second);
            ThaumcraftNetwork.syncAspectKnowledge(player);
            ThaumcraftNetwork.syncResearch(player);
        });

        context.setPacketHandled(true);
    }
}
