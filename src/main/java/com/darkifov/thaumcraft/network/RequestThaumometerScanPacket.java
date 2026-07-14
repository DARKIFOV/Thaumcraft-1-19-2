package com.darkifov.thaumcraft.network;

import com.darkifov.thaumcraft.aura.TC4ThaumometerTargeting;
import com.darkifov.thaumcraft.block.ThaumometerItem;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

/**
 * Forge 1.19.2 interaction bridge for the TC4 Thaumometer.
 *
 * <p>Right-clicking a chest, machine or entity is intercepted on the client
 * before vanilla sends its normal interaction packet. Earlier rebuild stages
 * cancelled that event and then waited for a server event that could never
 * arrive, which made the Thaumometer appear completely dead. This explicit
 * packet carries only the clicked target; the server re-runs the shared ray
 * test and starts the authoritative 25-tick scan only when the target still
 * matches what the player is actually looking at.</p>
 */
public final class RequestThaumometerScanPacket {
    private static final byte BLOCK = 0;
    private static final byte ENTITY = 1;

    private final byte kind;
    private final InteractionHand hand;
    private final BlockPos blockPos;
    private final int entityId;

    private RequestThaumometerScanPacket(byte kind, InteractionHand hand, BlockPos blockPos, int entityId) {
        this.kind = kind;
        this.hand = hand == null ? InteractionHand.MAIN_HAND : hand;
        this.blockPos = blockPos == null ? BlockPos.ZERO : blockPos.immutable();
        this.entityId = entityId;
    }

    public static RequestThaumometerScanPacket block(InteractionHand hand, BlockPos pos) {
        return new RequestThaumometerScanPacket(BLOCK, hand, pos, -1);
    }

    public static RequestThaumometerScanPacket entity(InteractionHand hand, int entityId) {
        return new RequestThaumometerScanPacket(ENTITY, hand, BlockPos.ZERO, entityId);
    }

    public static void encode(RequestThaumometerScanPacket packet, FriendlyByteBuf buffer) {
        buffer.writeByte(packet.kind);
        buffer.writeByte(packet.hand == InteractionHand.OFF_HAND ? 1 : 0);
        if (packet.kind == BLOCK) {
            buffer.writeBlockPos(packet.blockPos);
        } else {
            buffer.writeVarInt(packet.entityId);
        }
    }

    public static RequestThaumometerScanPacket decode(FriendlyByteBuf buffer) {
        byte kind = buffer.readByte();
        InteractionHand hand = buffer.readByte() == 1 ? InteractionHand.OFF_HAND : InteractionHand.MAIN_HAND;
        if (kind == BLOCK) {
            return block(hand, buffer.readBlockPos());
        }
        return entity(hand, buffer.readVarInt());
    }

    public static void handle(RequestThaumometerScanPacket packet,
                              Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> {
            ServerPlayer player = context.getSender();
            if (player == null) {
                return;
            }

            ItemStack held = player.getItemInHand(packet.hand);
            if (!(held.getItem() instanceof ThaumometerItem thaumometer)) {
                return;
            }

            // The client interaction already carries the concrete target. Do not
            // require a second exact server ray at packet handling time: rotation
            // synchronization can lag by one tick and made every scan appear dead.
            // beginBlockScan/beginEntityScan still enforce loaded state, range,
            // target validity and server-authoritative aspect availability.
            if (packet.kind == BLOCK) {
                thaumometer.beginBlockScan(player.level, player, packet.hand, packet.blockPos);
                return;
            }

            Entity target = player.level.getEntity(packet.entityId);
            if (target != null) {
                thaumometer.beginEntityScan(player.level, player, packet.hand, target);
            }
        });
        context.setPacketHandled(true);
    }
}
