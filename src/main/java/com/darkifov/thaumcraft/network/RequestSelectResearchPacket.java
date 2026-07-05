package com.darkifov.thaumcraft.network;

import com.darkifov.thaumcraft.research.OriginalResearchBridge;
import com.darkifov.thaumcraft.research.ResearchEntry;
import net.minecraft.ChatFormatting;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.Optional;
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

            boolean selected = OriginalResearchBridge.select(player, packet.researchKey);
            Optional<ResearchEntry> entry = OriginalResearchBridge.byKey(packet.researchKey);

            if (selected && entry.isPresent()) {
                player.displayClientMessage(
                        Component.literal("Selected research: ")
                                .withStyle(ChatFormatting.DARK_PURPLE)
                                .append(Component.literal(entry.get().title()).withStyle(ChatFormatting.LIGHT_PURPLE)),
                        true
                );
            } else {
                player.displayClientMessage(Component.literal("Unknown research key: " + packet.researchKey).withStyle(ChatFormatting.RED), true);
            }

            ThaumcraftNetwork.syncResearch(player);
        });

        context.setPacketHandled(true);
    }
}
