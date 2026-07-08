package com.darkifov.thaumcraft.network;

import com.darkifov.thaumcraft.client.fx.TC4ClientChampionFx;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

/** Stage214 client FX packet mirroring TC4 ChampionModifier.showFX dispatch. */
public class PacketFXChampion {
    private final int entityId;
    private final int mod;

    public PacketFXChampion(int entityId, int mod) {
        this.entityId = entityId;
        this.mod = mod;
    }

    public static void encode(PacketFXChampion packet, FriendlyByteBuf buffer) {
        buffer.writeInt(packet.entityId);
        buffer.writeInt(packet.mod);
    }

    public static PacketFXChampion decode(FriendlyByteBuf buffer) {
        return new PacketFXChampion(buffer.readInt(), buffer.readInt());
    }

    public static void handle(PacketFXChampion packet, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () ->
                TC4ClientChampionFx.showFX(packet.entityId, packet.mod)
        ));
        context.setPacketHandled(true);
    }
}
