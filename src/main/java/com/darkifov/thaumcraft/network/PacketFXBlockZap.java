package com.darkifov.thaumcraft.network;

import com.darkifov.thaumcraft.client.fx.TC4ClientInfusionFx;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

/** Stage209 client packet matching TC4 PacketFXBlockZap's six-float arc payload. */
public class PacketFXBlockZap {
    private final float sx;
    private final float sy;
    private final float sz;
    private final float ex;
    private final float ey;
    private final float ez;

    public PacketFXBlockZap(float sx, float sy, float sz, float ex, float ey, float ez) {
        this.sx = sx;
        this.sy = sy;
        this.sz = sz;
        this.ex = ex;
        this.ey = ey;
        this.ez = ez;
    }

    public static void encode(PacketFXBlockZap packet, FriendlyByteBuf buffer) {
        buffer.writeFloat(packet.sx);
        buffer.writeFloat(packet.sy);
        buffer.writeFloat(packet.sz);
        buffer.writeFloat(packet.ex);
        buffer.writeFloat(packet.ey);
        buffer.writeFloat(packet.ez);
    }

    public static PacketFXBlockZap decode(FriendlyByteBuf buffer) {
        return new PacketFXBlockZap(buffer.readFloat(), buffer.readFloat(), buffer.readFloat(), buffer.readFloat(), buffer.readFloat(), buffer.readFloat());
    }

    public static void handle(PacketFXBlockZap packet, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () ->
                TC4ClientInfusionFx.blockZap(packet.sx, packet.sy, packet.sz, packet.ex, packet.ey, packet.ez)
        ));
        context.setPacketHandled(true);
    }
}
