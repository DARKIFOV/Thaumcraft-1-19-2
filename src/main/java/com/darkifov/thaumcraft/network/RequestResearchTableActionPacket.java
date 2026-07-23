package com.darkifov.thaumcraft.network;

import com.darkifov.thaumcraft.blockentity.ResearchTableBlockEntity;
import com.darkifov.thaumcraft.research.TC4ResearchTableParity;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

/** Server-authoritative, silent Research Table actions matching TC4. */
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
            if (player == null || player.distanceToSqr(packet.pos.getX() + 0.5D,
                    packet.pos.getY() + 0.5D, packet.pos.getZ() + 0.5D) > 64.0D) {
                return;
            }
            BlockEntity blockEntity = player.level.getBlockEntity(packet.pos);
            if (!(blockEntity instanceof ResearchTableBlockEntity table)) {
                return;
            }

            // TC4 creates notes from a Thaumonomicon entry and learns a solved
            // discovery by using the removed note item. Those are not table buttons.
            if (packet.action == TC4ResearchTableParity.ACTION_OPEN_NOTE
                    || packet.action == TC4ResearchTableParity.ACTION_SYNC_NOTE) {
                table.syncResearchNote(player);
            } else if (packet.action == 3 || packet.action == 5
                    || TC4ResearchTableParity.isCopyAction(packet.action)) {
                table.copyCompletedResearchNote(player);
            }
        });
        context.setPacketHandled(true);
    }
}
