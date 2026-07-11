package com.darkifov.thaumcraft.network;

import com.darkifov.thaumcraft.client.ClientHooks;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class OpenResearchNotePacket {
    public OpenResearchNotePacket() {
    }

    public static void encode(OpenResearchNotePacket packet, FriendlyByteBuf buffer) {
    }

    public static OpenResearchNotePacket decode(FriendlyByteBuf buffer) {
        return new OpenResearchNotePacket();
    }

    public static void handle(OpenResearchNotePacket packet, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() ->
                DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> ClientHooks::openResearchNote)
        );
        context.setPacketHandled(true);
    }
}
