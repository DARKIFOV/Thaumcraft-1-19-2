package com.darkifov.thaumcraft.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

/**
 * Legacy discriminator retained so packet ids from earlier rebuild stages do
 * not shift. Research tables are container-backed and must be opened with
 * NetworkHooks.openScreen from ResearchTableBlock; opening a plain client
 * Screen here produced the duplicate, unusable research GUI reported in
 * v11.62.31-v11.62.37.
 */
@Deprecated(forRemoval = false)
public final class OpenResearchTablePacket {
    public static void encode(OpenResearchTablePacket packet, FriendlyByteBuf buffer) {
    }

    public static OpenResearchTablePacket decode(FriendlyByteBuf buffer) {
        return new OpenResearchTablePacket();
    }

    public static void handle(OpenResearchTablePacket packet, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        // Deliberate no-op. A menu cannot be opened safely from a client-only
        // packet because it has no server menu id, inventory or block position.
        context.setPacketHandled(true);
    }
}
