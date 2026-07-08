package com.darkifov.thaumcraft.network;

import com.darkifov.thaumcraft.client.fx.TC4ClientEldritchBossFx;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

/** Stage216 packet replacing TC4 entity-status bytes 15/16/17/18/19 for eldritch bosses and orbs. */
public class PacketFXEldritchBoss {
    private final int type;
    private final int entityId;
    private final double x;
    private final double y;
    private final double z;
    private final int bx;
    private final int by;
    private final int bz;

    public PacketFXEldritchBoss(int type, int entityId, double x, double y, double z, int bx, int by, int bz) {
        this.type = type;
        this.entityId = entityId;
        this.x = x;
        this.y = y;
        this.z = z;
        this.bx = bx;
        this.by = by;
        this.bz = bz;
    }

    public static void encode(PacketFXEldritchBoss packet, FriendlyByteBuf buffer) {
        buffer.writeInt(packet.type);
        buffer.writeInt(packet.entityId);
        buffer.writeDouble(packet.x);
        buffer.writeDouble(packet.y);
        buffer.writeDouble(packet.z);
        buffer.writeInt(packet.bx);
        buffer.writeInt(packet.by);
        buffer.writeInt(packet.bz);
    }

    public static PacketFXEldritchBoss decode(FriendlyByteBuf buffer) {
        return new PacketFXEldritchBoss(buffer.readInt(), buffer.readInt(), buffer.readDouble(), buffer.readDouble(), buffer.readDouble(), buffer.readInt(), buffer.readInt(), buffer.readInt());
    }

    public static void handle(PacketFXEldritchBoss packet, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () ->
                TC4ClientEldritchBossFx.show(packet.type, packet.entityId, packet.x, packet.y, packet.z, packet.bx, packet.by, packet.bz)
        ));
        context.setPacketHandled(true);
    }
}
