package com.darkifov.thaumcraft.network;

import com.darkifov.thaumcraft.entity.PechEntity;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class RequestPechGiftPacket {
    private final int pechEntityId;

    public RequestPechGiftPacket(int pechEntityId) {
        this.pechEntityId = pechEntityId;
    }

    public static void encode(RequestPechGiftPacket packet, FriendlyByteBuf buffer) {
        buffer.writeInt(packet.pechEntityId);
    }

    public static RequestPechGiftPacket decode(FriendlyByteBuf buffer) {
        return new RequestPechGiftPacket(buffer.readInt());
    }

    public static void handle(RequestPechGiftPacket packet, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();

        context.enqueueWork(() -> {
            ServerPlayer player = context.getSender();

            if (player == null) {
                return;
            }

            Entity entity = player.level.getEntity(packet.pechEntityId);

            if (entity instanceof PechEntity pech) {
                pech.giftFromGui(player);
            }
        });

        context.setPacketHandled(true);
    }
}
