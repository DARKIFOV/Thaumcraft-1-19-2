package com.darkifov.thaumcraft.network;

import com.darkifov.thaumcraft.Aspect;
import com.darkifov.thaumcraft.block.EssentiaCellItem;
import com.darkifov.thaumcraft.block.EssentiaUpgradeCardItem;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.network.NetworkEvent;

import java.util.EnumMap;
import java.util.Map;
import java.util.function.Supplier;

public class RequestEssentiaInventoryScanPacket {
    private final BlockPos pos;

    public RequestEssentiaInventoryScanPacket(BlockPos pos) {
        this.pos = pos;
    }

    public static void encode(RequestEssentiaInventoryScanPacket packet, FriendlyByteBuf buffer) {
        buffer.writeBlockPos(packet.pos);
    }

    public static RequestEssentiaInventoryScanPacket decode(FriendlyByteBuf buffer) {
        return new RequestEssentiaInventoryScanPacket(buffer.readBlockPos());
    }

    public static void handle(RequestEssentiaInventoryScanPacket packet, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();

        context.enqueueWork(() -> {
            ServerPlayer player = context.getSender();

            if (player == null || player.distanceToSqr(packet.pos.getX() + 0.5D, packet.pos.getY() + 0.5D, packet.pos.getZ() + 0.5D) > 64.0D) {
                return;
            }

            Map<Aspect, Integer> totals = new EnumMap<>(Aspect.class);
            int cells = 0;
            int scanBonus = 1;
            int speedCards = 0;

            for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
                ItemStack stack = player.getInventory().getItem(i);

                if (stack.getItem() instanceof EssentiaCellItem) {
                    cells++;
                    Aspect aspect = EssentiaCellItem.getAspect(stack);
                    int amount = EssentiaCellItem.getAmount(stack);

                    if (aspect != null && amount > 0) {
                        totals.merge(aspect, amount, Integer::sum);
                    }
                }

                if (stack.getItem() instanceof EssentiaUpgradeCardItem card) {
                    speedCards += stack.getCount();
                    scanBonus += card.scanBonus() * stack.getCount();
                }
            }

            player.displayClientMessage(Component.literal("Digital Essentia Cells в инвентаре: " + cells + " | upgrade cards: " + speedCards + " | scan bonus: x" + Math.min(16, scanBonus)).withStyle(ChatFormatting.AQUA), false);

            if (totals.isEmpty()) {
                player.displayClientMessage(Component.literal("Ячейки пустые.").withStyle(ChatFormatting.GRAY), false);
                return;
            }

            for (Map.Entry<Aspect, Integer> entry : totals.entrySet()) {
                player.displayClientMessage(
                        Component.literal(entry.getKey().displayName() + ": " + entry.getValue()).withStyle(entry.getKey().color()),
                        false
                );
            }
        });

        context.setPacketHandled(true);
    }
}
