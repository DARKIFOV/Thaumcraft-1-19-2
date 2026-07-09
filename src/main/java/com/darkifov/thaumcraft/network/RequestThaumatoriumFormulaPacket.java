package com.darkifov.thaumcraft.network;

import com.darkifov.thaumcraft.blockentity.ThaumatoriumBlockEntity;
import com.darkifov.thaumcraft.menu.ThaumatoriumMenu;
import com.darkifov.thaumcraft.porting.TC4Sounds;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundSource;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

/**
 * Stage543-562 original GuiThaumatorium hotzone adapter.
 * TC4 used clickable formula icons inside the texture, not a modern button widget.
 */
public class RequestThaumatoriumFormulaPacket {
    private final BlockPos pos;
    private final int formulaIndex;

    public RequestThaumatoriumFormulaPacket(BlockPos pos, int formulaIndex) {
        this.pos = pos == null ? BlockPos.ZERO : pos;
        this.formulaIndex = formulaIndex;
    }

    public static void encode(RequestThaumatoriumFormulaPacket packet, FriendlyByteBuf buffer) {
        buffer.writeBlockPos(packet.pos);
        buffer.writeVarInt(packet.formulaIndex);
    }

    public static RequestThaumatoriumFormulaPacket decode(FriendlyByteBuf buffer) {
        return new RequestThaumatoriumFormulaPacket(buffer.readBlockPos(), buffer.readVarInt());
    }

    public static void handle(RequestThaumatoriumFormulaPacket packet, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> {
            ServerPlayer player = context.getSender();
            if (player == null) {
                return;
            }
            if (!(player.containerMenu instanceof ThaumatoriumMenu menu) || !menu.blockPos().equals(packet.pos)) {
                player.displayClientMessage(Component.literal("Thaumatorium formula selection rejected: wrong container.").withStyle(ChatFormatting.RED), false);
                return;
            }
            if (player.distanceToSqr(packet.pos.getX() + 0.5D, packet.pos.getY() + 0.5D, packet.pos.getZ() + 0.5D) > 64.0D) {
                return;
            }
            BlockEntity blockEntity = player.level.getBlockEntity(packet.pos);
            if (blockEntity instanceof ThaumatoriumBlockEntity thaumatorium) {
                if (thaumatorium.selectFormulaIndex(packet.formulaIndex)) {
                    player.level.playSound(null, packet.pos, TC4Sounds.event("brain"), SoundSource.BLOCKS, 0.35F, 1.05F);
                } else {
                    player.displayClientMessage(Component.literal("Thaumatorium formula slot is empty.").withStyle(ChatFormatting.RED), false);
                }
            }
        });
        context.setPacketHandled(true);
    }
}
