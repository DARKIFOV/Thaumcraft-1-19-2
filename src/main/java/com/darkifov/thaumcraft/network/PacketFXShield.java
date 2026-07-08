package com.darkifov.thaumcraft.network;

import com.darkifov.thaumcraft.client.fx.TC4ClientRunicShieldFx;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

/** Stage211 client FX packet matching TC4 PacketFXShield source/target semantics. */
public class PacketFXShield {
    private final int source;
    private final int target;

    public PacketFXShield(int source, int target) {
        this.source = source;
        this.target = target;
    }

    public static void encode(PacketFXShield packet, FriendlyByteBuf buffer) {
        buffer.writeInt(packet.source);
        buffer.writeInt(packet.target);
    }

    public static PacketFXShield decode(FriendlyByteBuf buffer) {
        return new PacketFXShield(buffer.readInt(), buffer.readInt());
    }

    public static void handle(PacketFXShield packet, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () ->
                TC4ClientRunicShieldFx.shield(packet.source, packet.target)
        ));
        context.setPacketHandled(true);
    }
}
