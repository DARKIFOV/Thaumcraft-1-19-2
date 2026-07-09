package com.darkifov.thaumcraft.network;

import com.darkifov.thaumcraft.block.ResearchNoteItem;
import com.darkifov.thaumcraft.research.ResearchNoteSolver;
import com.darkifov.thaumcraft.research.ResearchTableInventoryRuntime;
import net.minecraft.ChatFormatting;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class RequestClearResearchNoteSlotPacket {
    private final int slot;

    public RequestClearResearchNoteSlotPacket(int slot) {
        this.slot = slot;
    }

    public static void encode(RequestClearResearchNoteSlotPacket packet, FriendlyByteBuf buffer) {
        buffer.writeVarInt(packet.slot);
    }

    public static RequestClearResearchNoteSlotPacket decode(FriendlyByteBuf buffer) {
        return new RequestClearResearchNoteSlotPacket(buffer.readVarInt());
    }

    public static void handle(RequestClearResearchNoteSlotPacket packet, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();

        context.enqueueWork(() -> {
            ServerPlayer player = context.getSender();

            if (player == null) {
                return;
            }

            ItemStack note = findNote(player);

            if (note.isEmpty()) {
                player.displayClientMessage(Component.literal("Open the Research Table and place a Research Note in slot 1.").withStyle(ChatFormatting.RED), false);
                return;
            }

            ResearchNoteSolver.clearSlot(player, note, packet.slot);
            ResearchTableInventoryRuntime.markOpenTableChanged(player);
            ThaumcraftNetwork.syncAspectKnowledge(player);
            ThaumcraftNetwork.syncResearchNote(player, note);
        });

        context.setPacketHandled(true);
    }

    private static ItemStack findNote(ServerPlayer player) {
        return ResearchTableInventoryRuntime.findOpenTableResearchNote(player).orElse(ItemStack.EMPTY);
    }
}
