package com.darkifov.thaumcraft.network;

import com.darkifov.thaumcraft.client.fx.TC4ClientInfusionFx;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

/** Stage209 client packet matching TC4 PacketFXInfusionSource payload semantics. */
public class PacketFXInfusionSource {
    private final BlockPos matrixPos;
    private final byte dx;
    private final byte dy;
    private final byte dz;
    private final int entityId;

    public PacketFXInfusionSource(BlockPos matrixPos, byte dx, byte dy, byte dz, int entityId) {
        this.matrixPos = matrixPos;
        this.dx = dx;
        this.dy = dy;
        this.dz = dz;
        this.entityId = entityId;
    }

    public static void encode(PacketFXInfusionSource packet, FriendlyByteBuf buffer) {
        buffer.writeBlockPos(packet.matrixPos);
        buffer.writeInt(packet.entityId);
        buffer.writeByte(packet.dx);
        buffer.writeByte(packet.dy);
        buffer.writeByte(packet.dz);
    }

    public static PacketFXInfusionSource decode(FriendlyByteBuf buffer) {
        BlockPos matrixPos = buffer.readBlockPos();
        int entityId = buffer.readInt();
        byte dx = buffer.readByte();
        byte dy = buffer.readByte();
        byte dz = buffer.readByte();
        return new PacketFXInfusionSource(matrixPos, dx, dy, dz, entityId);
    }

    public static void handle(PacketFXInfusionSource packet, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () ->
                TC4ClientInfusionFx.infusionSource(packet.matrixPos, packet.dx, packet.dy, packet.dz, packet.entityId)
        ));
        context.setPacketHandled(true);
    }
}
