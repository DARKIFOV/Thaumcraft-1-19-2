package com.darkifov.thaumcraft.network;

import com.darkifov.thaumcraft.blockentity.TransvectorInterfaceBlockEntity;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class RequestTransvectorClearPacket {
    private final BlockPos pos;

    public RequestTransvectorClearPacket(BlockPos pos) {
        this.pos = pos;
    }

    public static void encode(RequestTransvectorClearPacket packet, FriendlyByteBuf buffer) {
        buffer.writeBlockPos(packet.pos);
    }

    public static RequestTransvectorClearPacket decode(FriendlyByteBuf buffer) {
        return new RequestTransvectorClearPacket(buffer.readBlockPos());
    }

    public static void handle(RequestTransvectorClearPacket packet, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();

        context.enqueueWork(() -> {
            ServerPlayer player = context.getSender();

            if (player == null || player.distanceToSqr(packet.pos.getX() + 0.5D, packet.pos.getY() + 0.5D, packet.pos.getZ() + 0.5D) > 64.0D) {
                return;
            }

            BlockEntity blockEntity = player.level.getBlockEntity(packet.pos);

            if (blockEntity instanceof TransvectorInterfaceBlockEntity transvector) {
                transvector.clearTarget();
                player.displayClientMessage(Component.literal("Transvector Interface target cleared.").withStyle(ChatFormatting.GRAY), false);
            }
        });

        context.setPacketHandled(true);
    }
}
