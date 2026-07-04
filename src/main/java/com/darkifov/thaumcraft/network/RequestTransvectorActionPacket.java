package com.darkifov.thaumcraft.network;

import com.darkifov.thaumcraft.blockentity.TransvectorInterfaceBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class RequestTransvectorActionPacket {
    private final BlockPos pos;
    private final int action;

    public RequestTransvectorActionPacket(BlockPos pos, int action) {
        this.pos = pos;
        this.action = action;
    }

    public static void encode(RequestTransvectorActionPacket packet, FriendlyByteBuf buffer) {
        buffer.writeBlockPos(packet.pos);
        buffer.writeInt(packet.action);
    }

    public static RequestTransvectorActionPacket decode(FriendlyByteBuf buffer) {
        return new RequestTransvectorActionPacket(buffer.readBlockPos(), buffer.readInt());
    }

    public static void handle(RequestTransvectorActionPacket packet, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();

        context.enqueueWork(() -> {
            ServerPlayer player = context.getSender();

            if (player == null || player.distanceToSqr(packet.pos.getX() + 0.5D, packet.pos.getY() + 0.5D, packet.pos.getZ() + 0.5D) > 64.0D) {
                return;
            }

            BlockEntity blockEntity = player.level.getBlockEntity(packet.pos);

            if (blockEntity instanceof TransvectorInterfaceBlockEntity transvector) {
                transvector.performAction(player, packet.action);
            }
        });

        context.setPacketHandled(true);
    }
}
