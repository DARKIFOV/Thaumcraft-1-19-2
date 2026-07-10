package com.darkifov.thaumcraft.network;

import com.darkifov.thaumcraft.block.WandItem;
import com.darkifov.thaumcraft.wand.WandComponentData;
import com.darkifov.thaumcraft.wand.WandManagerRuntime;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

/**
 * Stage188 server packet adapter for original PacketFocusChangeToServer.
 * Original TC4 sends dim/playerid/focus and then calls WandManager.changeFocus
 * only when the held ItemWandCasting is not a sceptre.  In 1.19.2 the server
 * player comes from the packet context, but the focus string, REMOVE sentinel,
 * sceptre guard and server-authoritative WandManager.changeFocus flow remain.
 */
public class RequestFocusChangePacket {
    private final String focus;

    public RequestFocusChangePacket(String focus) {
        this.focus = focus == null ? "" : focus;
    }

    public static void encode(RequestFocusChangePacket packet, FriendlyByteBuf buffer) {
        buffer.writeUtf(packet.focus, 128);
    }

    public static RequestFocusChangePacket decode(FriendlyByteBuf buffer) {
        return new RequestFocusChangePacket(buffer.readUtf(128));
    }

    public static void handle(RequestFocusChangePacket packet, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> {
            ServerPlayer player = context.getSender();
            if (player == null) {
                return;
            }
            ItemStack held = player.getMainHandItem();
            if (!(held.getItem() instanceof WandItem)) held = player.getOffhandItem();
            if (!(held.getItem() instanceof WandItem) || WandComponentData.isSceptre(held)) return;
            WandManagerRuntime.changeFocus(held, player.level, player, packet.focus);
        });
        context.setPacketHandled(true);
    }
}
