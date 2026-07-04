package com.darkifov.thaumcraft.network;

import com.darkifov.thaumcraft.blockentity.EssentiaDriveBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class RequestEssentiaDriveScanPacket {
    private final BlockPos pos;

    public RequestEssentiaDriveScanPacket(BlockPos pos) {
        this.pos = pos;
    }

    public static void encode(RequestEssentiaDriveScanPacket packet, FriendlyByteBuf buffer) {
        buffer.writeBlockPos(packet.pos);
    }

    public static RequestEssentiaDriveScanPacket decode(FriendlyByteBuf buffer) {
        return new RequestEssentiaDriveScanPacket(buffer.readBlockPos());
    }

    public static void handle(RequestEssentiaDriveScanPacket packet, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();

        context.enqueueWork(() -> {
            ServerPlayer player = context.getSender();

            if (player == null || player.distanceToSqr(packet.pos.getX() + 0.5D, packet.pos.getY() + 0.5D, packet.pos.getZ() + 0.5D) > 64.0D) {
                return;
            }

            BlockEntity blockEntity = player.level.getBlockEntity(packet.pos);

            if (blockEntity instanceof EssentiaDriveBlockEntity drive) {
                drive.sendStatus(player);
            }
        });

        context.setPacketHandled(true);
    }
}
