package com.darkifov.thaumcraft.network;

import com.darkifov.thaumcraft.blockentity.TransvectorInterfaceBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class RequestTransvectorInspectPacket {
    private final BlockPos pos;

    public RequestTransvectorInspectPacket(BlockPos pos) {
        this.pos = pos;
    }

    public static void encode(RequestTransvectorInspectPacket packet, FriendlyByteBuf buffer) {
        buffer.writeBlockPos(packet.pos);
    }

    public static RequestTransvectorInspectPacket decode(FriendlyByteBuf buffer) {
        return new RequestTransvectorInspectPacket(buffer.readBlockPos());
    }

    public static void handle(RequestTransvectorInspectPacket packet, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();

        context.enqueueWork(() -> {
            ServerPlayer player = context.getSender();

            if (player == null || player.distanceToSqr(packet.pos.getX() + 0.5D, packet.pos.getY() + 0.5D, packet.pos.getZ() + 0.5D) > 64.0D) {
                return;
            }

            BlockEntity blockEntity = player.level.getBlockEntity(packet.pos);

            if (blockEntity instanceof TransvectorInterfaceBlockEntity transvector) {
                transvector.inspectTarget(player);
            }
        });

        context.setPacketHandled(true);
    }
}
