package com.darkifov.thaumcraft.network;

import com.darkifov.thaumcraft.block.ResearchNoteItem;
import com.darkifov.thaumcraft.research.ResearchNoteSolver;
import com.darkifov.thaumcraft.research.ResearchTableInventoryRuntime;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class RequestSolveResearchNotePacket {
    public RequestSolveResearchNotePacket() {
    }

    public static void encode(RequestSolveResearchNotePacket packet, FriendlyByteBuf buffer) {
    }

    public static RequestSolveResearchNotePacket decode(FriendlyByteBuf buffer) {
        return new RequestSolveResearchNotePacket();
    }

    public static void handle(RequestSolveResearchNotePacket packet, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();

        context.enqueueWork(() -> {
            ServerPlayer player = context.getSender();

            if (player == null) {
                return;
            }

            ItemStack note = findNote(player);

            if (note.isEmpty()) {
                return;
            }

            if (ResearchNoteSolver.solve(player, note)) {
                ResearchTableInventoryRuntime.markOpenTableChanged(player);
                ThaumcraftNetwork.syncResearchNote(player, note);
            }
        });

        context.setPacketHandled(true);
    }

    private static ItemStack findNote(ServerPlayer player) {
        return ResearchTableInventoryRuntime.findOpenTableResearchNote(player).orElse(ItemStack.EMPTY);
    }
}
