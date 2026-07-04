package com.darkifov.thaumcraft.network;

import com.darkifov.thaumcraft.Aspect;
import com.darkifov.thaumcraft.block.EssentiaCellItem;
import com.darkifov.thaumcraft.blockentity.EssentiaDriveBlockEntity;
import com.darkifov.thaumcraft.blockentity.EssentiaJarBlockEntity;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class RequestEssentiaTerminalFilteredScanPacket {
    private final BlockPos pos;
    private final int aspectOrdinal;

    public RequestEssentiaTerminalFilteredScanPacket(BlockPos pos, int aspectOrdinal) {
        this.pos = pos;
        this.aspectOrdinal = aspectOrdinal;
    }

    public static void encode(RequestEssentiaTerminalFilteredScanPacket packet, FriendlyByteBuf buffer) {
        buffer.writeBlockPos(packet.pos);
        buffer.writeInt(packet.aspectOrdinal);
    }

    public static RequestEssentiaTerminalFilteredScanPacket decode(FriendlyByteBuf buffer) {
        return new RequestEssentiaTerminalFilteredScanPacket(buffer.readBlockPos(), buffer.readInt());
    }

    public static void handle(RequestEssentiaTerminalFilteredScanPacket packet, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();

        context.enqueueWork(() -> {
            ServerPlayer player = context.getSender();

            if (player == null || player.distanceToSqr(packet.pos.getX() + 0.5D, packet.pos.getY() + 0.5D, packet.pos.getZ() + 0.5D) > 64.0D) {
                return;
            }

            Aspect[] values = Aspect.values();
            Aspect aspect = values[Math.floorMod(packet.aspectOrdinal, values.length)];

            int jars = 0;
            int jarAmount = 0;
            int driveAmount = 0;
            int inventoryAmount = 0;
            int drives = 0;

            for (Direction direction : Direction.values()) {
                BlockEntity blockEntity = player.level.getBlockEntity(packet.pos.relative(direction));

                if (blockEntity instanceof EssentiaJarBlockEntity jar) {
                    jars++;
                    jarAmount += jar.aspects().get(aspect);
                }

                if (blockEntity instanceof EssentiaDriveBlockEntity drive) {
                    drives++;
                    driveAmount += drive.amountOf(aspect);
                }
            }

            for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
                ItemStack stack = player.getInventory().getItem(i);

                if (stack.getItem() instanceof EssentiaCellItem && EssentiaCellItem.getAspect(stack) == aspect) {
                    inventoryAmount += EssentiaCellItem.getAmount(stack);
                }
            }

            int total = jarAmount + driveAmount + inventoryAmount;

            player.displayClientMessage(Component.literal("Terminal Filter: ").append(Component.literal(aspect.displayName()).withStyle(aspect.color())), false);
            player.displayClientMessage(Component.literal("Jar: " + jarAmount + " / Drive: " + driveAmount + " / Inventory cells: " + inventoryAmount).withStyle(ChatFormatting.AQUA), false);
            player.displayClientMessage(Component.literal("Total: " + total + " | jars checked: " + jars + " | drives checked: " + drives).withStyle(ChatFormatting.LIGHT_PURPLE), false);
        });

        context.setPacketHandled(true);
    }
}
