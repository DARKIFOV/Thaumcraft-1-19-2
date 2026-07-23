package com.darkifov.thaumcraft.network;

import com.darkifov.thaumcraft.block.WandItem;
import com.darkifov.thaumcraft.item.ElementalShovelItem;
import com.darkifov.thaumcraft.porting.TC4Sounds;
import com.darkifov.thaumcraft.wand.FocusArchitectRuntime;
import net.minecraft.ChatFormatting;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

/**
 * Stage179 server packet adapter for original PacketItemKeyToServer key==1.
 *
 * TC4 sends the G key to the server and calls WandManager.toggleMisc on the
 * held ItemWandCasting.  This packet keeps the same server-authoritative flow
 * and mutates the original areax/areay/areaz/aread NBT keys through
 * FocusArchitectRuntime.
 */
public class RequestWandArchitectTogglePacket {
    private final byte key;

    public RequestWandArchitectTogglePacket(byte key) {
        this.key = key;
    }

    public static void encode(RequestWandArchitectTogglePacket packet, FriendlyByteBuf buffer) {
        buffer.writeByte(packet.key);
    }

    public static RequestWandArchitectTogglePacket decode(FriendlyByteBuf buffer) {
        return new RequestWandArchitectTogglePacket(buffer.readByte());
    }

    public static void handle(RequestWandArchitectTogglePacket packet, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> {
            ServerPlayer player = context.getSender();
            if (player == null || packet.key != 1) {
                return;
            }
            ItemStack held = player.getMainHandItem();
            if (held.getItem() instanceof ElementalShovelItem) {
                ElementalShovelItem.cycleOrientation(held);
                player.level.playSound(null, player.blockPosition(), TC4Sounds.event("wand"), SoundSource.PLAYERS, 0.35F, 1.0F);
                player.displayClientMessage(
                        Component.translatable("message.thaumcraft.elemental_shovel.mode", ElementalShovelItem.orientationName(held))
                                .withStyle(ChatFormatting.GRAY), true);
                return;
            }
            if (!(held.getItem() instanceof WandItem)) held = player.getOffhandItem();
            if (held.getItem() instanceof ElementalShovelItem) {
                ElementalShovelItem.cycleOrientation(held);
                player.level.playSound(null, player.blockPosition(), TC4Sounds.event("wand"), SoundSource.PLAYERS, 0.35F, 1.0F);
                player.displayClientMessage(
                        Component.translatable("message.thaumcraft.elemental_shovel.mode", ElementalShovelItem.orientationName(held))
                                .withStyle(ChatFormatting.GRAY), true);
                return;
            }
            if (!(held.getItem() instanceof WandItem)) return;
            FocusArchitectRuntime.toggleMisc(held, player);
            player.level.playSound(null, player.blockPosition(), TC4Sounds.event("wand"), SoundSource.PLAYERS, 0.35F, 1.0F);
            player.displayClientMessage(Component.literal(FocusArchitectRuntime.architectStatusLine(held)).withStyle(ChatFormatting.GRAY), true);
        });
        context.setPacketHandled(true);
    }
}
