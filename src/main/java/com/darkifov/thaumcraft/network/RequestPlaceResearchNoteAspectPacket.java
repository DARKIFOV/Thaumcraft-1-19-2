package com.darkifov.thaumcraft.network;

import com.darkifov.thaumcraft.Aspect;
import com.darkifov.thaumcraft.block.ResearchNoteItem;
import com.darkifov.thaumcraft.research.ResearchNoteSolver;
import com.darkifov.thaumcraft.research.ResearchTableInventoryRuntime;
import net.minecraft.network.FriendlyByteBuf;
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
                return;
            }

            Aspect aspect = Aspect.byId(packet.aspectId);

            if (aspect == null) {
                return;
            }

            if (ResearchNoteSolver.placeAspect(player, note, packet.slot, aspect)) {
                ResearchTableInventoryRuntime.markOpenTableChanged(player);
                ThaumcraftNetwork.syncAspectKnowledge(player);
                ThaumcraftNetwork.syncResearchNote(player, note);
            }
        });

        context.setPacketHandled(true);
    }

    private static ItemStack findNote(ServerPlayer player) {
        return ResearchTableInventoryRuntime.findOpenTableResearchNote(player).orElse(ItemStack.EMPTY);
    }
}
