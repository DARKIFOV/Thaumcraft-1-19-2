package com.darkifov.thaumcraft.network;

import com.darkifov.thaumcraft.client.screen.ResearchNoteScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
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
        context.enqueueWork(() -> Minecraft.getInstance().setScreen(new ResearchNoteScreen()));
        context.setPacketHandled(true);
    }
}
