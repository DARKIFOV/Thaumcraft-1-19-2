package com.darkifov.thaumcraft.network;

import com.darkifov.thaumcraft.blockentity.ResearchTableBlockEntity;
import com.darkifov.thaumcraft.research.TC4ResearchTableParity;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class RequestResearchTableActionPacket {
    private final BlockPos pos;
    private final int action;

    public RequestResearchTableActionPacket(BlockPos pos, int action) {
        this.pos = pos == null ? BlockPos.ZERO : pos;
        this.action = action;
    }

    public static void encode(RequestResearchTableActionPacket packet, FriendlyByteBuf buffer) {
        buffer.writeBlockPos(packet.pos);
        buffer.writeVarInt(packet.action);
    }

    public static RequestResearchTableActionPacket decode(FriendlyByteBuf buffer) {
        return new RequestResearchTableActionPacket(buffer.readBlockPos(), buffer.readVarInt());
    }

    public static void handle(RequestResearchTableActionPacket packet, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> {
            ServerPlayer player = context.getSender();
            if (player == null) {
                return;
            }
            if (player.distanceToSqr(packet.pos.getX() + 0.5D, packet.pos.getY() + 0.5D, packet.pos.getZ() + 0.5D) > 64.0D) {
                return;
            }
            BlockEntity blockEntity = player.level.getBlockEntity(packet.pos);
            if (!(blockEntity instanceof ResearchTableBlockEntity table)) {
                player.displayClientMessage(Component.literal("Research Table is missing.").withStyle(ChatFormatting.RED), false);
                return;
            }
            if (packet.action == TC4ResearchTableParity.ACTION_CREATE_NOTE) {
                player.displayClientMessage(Component.literal("Create research notes from an available Thaumonomicon entry.")
                        .withStyle(ChatFormatting.GRAY), false);
            } else if (packet.action == TC4ResearchTableParity.ACTION_OPEN_NOTE) {
                table.syncResearchNote(player);
            } else if (packet.action == TC4ResearchTableParity.ACTION_COMPLETE_SOLVED_NOTE) {
                player.displayClientMessage(Component.literal("Take the completed discovery and use it to learn the research.")
                        .withStyle(ChatFormatting.GRAY), false);
            } else if (packet.action == TC4ResearchTableParity.ACTION_SYNC_NOTE) {
                table.syncResearchNote(player);
            } else if (packet.action == 3 || packet.action == 5 || TC4ResearchTableParity.isCopyAction(packet.action)) {
                table.copyCompletedResearchNote(player);
            }
        });
        context.setPacketHandled(true);
    }
}
