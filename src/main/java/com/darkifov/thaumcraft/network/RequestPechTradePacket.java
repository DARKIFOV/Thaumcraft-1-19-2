package com.darkifov.thaumcraft.network;

import com.darkifov.thaumcraft.entity.PechEntity;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class RequestPechTradePacket {
    private final int pechEntityId;
    private final int tier;

    public RequestPechTradePacket(int pechEntityId, int tier) {
        this.pechEntityId = pechEntityId;
        this.tier = tier;
    }

    public static void encode(RequestPechTradePacket packet, FriendlyByteBuf buffer) {
        buffer.writeInt(packet.pechEntityId);
        buffer.writeInt(packet.tier);
    }

    public static RequestPechTradePacket decode(FriendlyByteBuf buffer) {
        return new RequestPechTradePacket(buffer.readInt(), buffer.readInt());
    }

    public static void handle(RequestPechTradePacket packet, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();

        context.enqueueWork(() -> {
            ServerPlayer player = context.getSender();

            if (player == null) {
                return;
            }

            Entity entity = player.level.getEntity(packet.pechEntityId);

            if (entity instanceof PechEntity pech) {
                pech.tradeFromGui(player, packet.tier);
            }
        });

        context.setPacketHandled(true);
    }
}
