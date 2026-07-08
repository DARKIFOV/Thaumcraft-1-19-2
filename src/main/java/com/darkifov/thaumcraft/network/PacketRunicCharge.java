package com.darkifov.thaumcraft.network;

import com.darkifov.thaumcraft.client.RunicShieldClientState;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

/** Stage211 1.19.2 SimpleChannel port of TC4 PacketRunicCharge. */
public class PacketRunicCharge {
    private final int entityId;
    private final short amount;
    private final short max;

    public PacketRunicCharge(int entityId, int amount, int max) {
        this.entityId = entityId;
        this.amount = (short) Math.max(0, Math.min(Short.MAX_VALUE, amount));
        this.max = (short) Math.max(0, Math.min(Short.MAX_VALUE, max));
    }

    public static void encode(PacketRunicCharge packet, FriendlyByteBuf buffer) {
        buffer.writeInt(packet.entityId);
        buffer.writeShort(packet.amount);
        buffer.writeShort(packet.max);
    }

    public static PacketRunicCharge decode(FriendlyByteBuf buffer) {
        return new PacketRunicCharge(buffer.readInt(), buffer.readShort(), buffer.readShort());
    }

    public static void handle(PacketRunicCharge packet, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () ->
                RunicShieldClientState.set(packet.entityId, packet.amount, packet.max)
        ));
        context.setPacketHandled(true);
    }
}
