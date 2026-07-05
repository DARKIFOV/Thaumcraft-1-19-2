package com.darkifov.thaumcraft.network;

import com.darkifov.thaumcraft.block.ResearchNoteItem;
import com.darkifov.thaumcraft.research.OriginalResearchBridge;
import com.darkifov.thaumcraft.research.ResearchEntry;
import com.darkifov.thaumcraft.research.ResearchNoteSolver;
import com.darkifov.thaumcraft.research.ResearchNoteState;
import net.minecraft.ChatFormatting;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.network.NetworkEvent;

import java.util.Optional;
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
                player.displayClientMessage(Component.literal("Hold a Research Note to solve it.").withStyle(ChatFormatting.RED), false);
                return;
            }

            boolean solved = ResearchNoteSolver.solve(player, note);

            if (solved) {
                Optional<ResearchEntry> target = targetResearch(player, note);

                if (target.isPresent()) {
                    OriginalResearchBridge.unlock(player, target.get());
                    player.displayClientMessage(Component.literal("Research unlocked from note: ")
                            .withStyle(ChatFormatting.GOLD)
                            .append(Component.literal(target.get().title()).withStyle(ChatFormatting.LIGHT_PURPLE)), false);
                }
            }

            ThaumcraftNetwork.syncAspectKnowledge(player);
            ThaumcraftNetwork.syncResearch(player);
            ThaumcraftNetwork.syncResearchNote(player, note);
        });

        context.setPacketHandled(true);
    }

    private static Optional<ResearchEntry> targetResearch(ServerPlayer player, ItemStack note) {
        String key = ResearchNoteState.target(note);

        if (key == null || key.isBlank()) {
            return OriginalResearchBridge.selectedOrFirstAvailable(player);
        }

        return OriginalResearchBridge.byKey(key);
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
