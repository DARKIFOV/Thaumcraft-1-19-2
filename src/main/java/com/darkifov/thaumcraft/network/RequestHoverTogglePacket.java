package com.darkifov.thaumcraft.network;

import com.darkifov.thaumcraft.item.gear.HoverHarnessRuntime;
import net.minecraft.ChatFormatting;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

/** Server-authoritative replacement for TC4 PacketFlyToServer / H key. */
public final class RequestHoverTogglePacket {
    public static void encode(RequestHoverTogglePacket packet, FriendlyByteBuf buffer) {
    }

    public static RequestHoverTogglePacket decode(FriendlyByteBuf buffer) {
        return new RequestHoverTogglePacket();
    }

    public static void handle(RequestHoverTogglePacket packet, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> {
            ServerPlayer player = context.getSender();
            if (player == null) {
                return;
            }
            boolean enabled = HoverHarnessRuntime.toggle(player);
            player.displayClientMessage(Component.translatable(enabled
                            ? "message.thaumcraft.hover.enabled"
                            : "message.thaumcraft.hover.disabled")
                    .withStyle(enabled ? ChatFormatting.AQUA : ChatFormatting.GRAY), true);
        });
        context.setPacketHandled(true);
    }
}
