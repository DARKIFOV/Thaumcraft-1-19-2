package com.darkifov.thaumcraft.network;

import com.darkifov.thaumcraft.client.ClientHooks;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class OpenResearchTablePacket {
    public OpenResearchTablePacket() {
    }

    public static void encode(OpenResearchTablePacket packet, FriendlyByteBuf buffer) {
    }

    public static OpenResearchTablePacket decode(FriendlyByteBuf buffer) {
        return new OpenResearchTablePacket();
    }

    public static void handle(OpenResearchTablePacket packet, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();

        context.enqueueWork(() ->
                DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> ClientHooks::openResearchTable)
        );
        context.setPacketHandled(true);
    }
}
