package com.darkifov.thaumcraft.network;

import com.darkifov.thaumcraft.client.ClientHooks;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class OpenArcaneWorkbenchPacket {
    public OpenArcaneWorkbenchPacket() {
    }

    public static void encode(OpenArcaneWorkbenchPacket packet, FriendlyByteBuf buffer) {
    }

    public static OpenArcaneWorkbenchPacket decode(FriendlyByteBuf buffer) {
        return new OpenArcaneWorkbenchPacket();
    }

    public static void handle(OpenArcaneWorkbenchPacket packet, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();

        context.enqueueWork(() ->
                DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> ClientHooks::openArcaneWorkbench)
        );

        context.setPacketHandled(true);
    }
}
