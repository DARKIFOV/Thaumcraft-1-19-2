package com.darkifov.thaumcraft.network;

import com.darkifov.thaumcraft.Aspect;
import com.darkifov.thaumcraft.blockentity.EssentiaDriveBlockEntity;
import com.darkifov.thaumcraft.blockentity.EssentiaJarBlockEntity;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.network.NetworkEvent;

import java.util.EnumMap;
import java.util.Map;
import java.util.function.Supplier;

public class RequestEssentiaTerminalScanPacket {
    private final BlockPos pos;

    public RequestEssentiaTerminalScanPacket(BlockPos pos) {
        this.pos = pos;
    }

    public static void encode(RequestEssentiaTerminalScanPacket packet, FriendlyByteBuf buffer) {
        buffer.writeBlockPos(packet.pos);
    }

    public static RequestEssentiaTerminalScanPacket decode(FriendlyByteBuf buffer) {
        return new RequestEssentiaTerminalScanPacket(buffer.readBlockPos());
    }

    public static void handle(RequestEssentiaTerminalScanPacket packet, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();

        context.enqueueWork(() -> {
            ServerPlayer player = context.getSender();

            if (player == null || player.distanceToSqr(packet.pos.getX() + 0.5D, packet.pos.getY() + 0.5D, packet.pos.getZ() + 0.5D) > 64.0D) {
                return;
            }

            Map<Aspect, Integer> totals = new EnumMap<>(Aspect.class);
            int jars = 0;

            for (Direction direction : Direction.values()) {
                BlockEntity blockEntity = player.level.getBlockEntity(packet.pos.relative(direction));

                if (blockEntity instanceof EssentiaDriveBlockEntity drive) {
                    player.displayClientMessage(Component.literal("Соседний Essentia Drive найден:").withStyle(ChatFormatting.LIGHT_PURPLE), false);
                    drive.sendStatus(player);
                }

                if (blockEntity instanceof EssentiaJarBlockEntity jar) {
                    jars++;

                    for (Aspect aspect : Aspect.values()) {
                        int amount = jar.aspects().get(aspect);

                        if (amount > 0) {
                            totals.merge(aspect, amount, Integer::sum);
                        }
                    }
                }
            }

            player.displayClientMessage(Component.literal("Essentia Terminal | соседние jar: " + jars).withStyle(ChatFormatting.AQUA), false);

            if (totals.isEmpty()) {
                player.displayClientMessage(Component.literal("Рядом нет essentia.").withStyle(ChatFormatting.GRAY), false);
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
