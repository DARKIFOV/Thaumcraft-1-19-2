package com.darkifov.thaumcraft.network;

import com.darkifov.thaumcraft.Aspect;
import com.darkifov.thaumcraft.block.ResearchNoteItem;
import com.darkifov.thaumcraft.research.ResearchNoteSolver;
import net.minecraft.ChatFormatting;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class RequestPlaceResearchNoteAspectPacket {
    private final int slot;
    private final String aspectId;

    public RequestPlaceResearchNoteAspectPacket(int slot, String aspectId) {
        this.slot = slot;
        this.aspectId = aspectId == null ? "" : aspectId;
    }

    public static void encode(RequestPlaceResearchNoteAspectPacket packet, FriendlyByteBuf buffer) {
        buffer.writeVarInt(packet.slot);
        buffer.writeUtf(packet.aspectId);
    }

    public static RequestPlaceResearchNoteAspectPacket decode(FriendlyByteBuf buffer) {
        return new RequestPlaceResearchNoteAspectPacket(buffer.readVarInt(), buffer.readUtf());
    }

    public static void handle(RequestPlaceResearchNoteAspectPacket packet, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();

        context.enqueueWork(() -> {
            ServerPlayer player = context.getSender();

            if (player == null) {
                return;
            }

            ItemStack note = findNote(player);

            if (note.isEmpty()) {
                player.displayClientMessage(Component.literal("Hold a Research Note to edit it.").withStyle(ChatFormatting.RED), false);
                return;
            }

            Aspect aspect = Aspect.byId(packet.aspectId);

            if (aspect == null) {
                player.displayClientMessage(Component.literal("Unknown aspect.").withStyle(ChatFormatting.RED), false);
                return;
            }

            ResearchNoteSolver.placeAspect(player, note, packet.slot, aspect);
            ThaumcraftNetwork.syncAspectKnowledge(player);
            ThaumcraftNetwork.syncResearchNote(player, note);
        });

        context.setPacketHandled(true);
    }

    private static ItemStack findNote(ServerPlayer player) {
        ItemStack main = player.getMainHandItem();

        if (main.getItem() instanceof ResearchNoteItem) {
            return main;
        }

        ItemStack off = player.getOffhandItem();

        if (off.getItem() instanceof ResearchNoteItem) {
            return off;
        }

        return ItemStack.EMPTY;
    }
}
