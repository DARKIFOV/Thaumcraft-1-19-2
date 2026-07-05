
package com.darkifov.thaumcraft.network;
import com.darkifov.thaumcraft.ThaumcraftMod;
import com.darkifov.thaumcraft.block.OsmoticEnchantmentHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;
import java.util.function.Supplier;

public class RequestOsmoticEnchantPacket {
    private final BlockPos pos; private final int choiceOrdinal;
    public RequestOsmoticEnchantPacket(BlockPos pos, int choiceOrdinal) { this.pos = pos; this.choiceOrdinal = choiceOrdinal; }
    public static void encode(RequestOsmoticEnchantPacket p, FriendlyByteBuf b) { b.writeBlockPos(p.pos); b.writeInt(p.choiceOrdinal); }
    public static RequestOsmoticEnchantPacket decode(FriendlyByteBuf b) { return new RequestOsmoticEnchantPacket(b.readBlockPos(), b.readInt()); }
    public static void handle(RequestOsmoticEnchantPacket packet, Supplier<NetworkEvent.Context> ctxSup) {
        NetworkEvent.Context ctx = ctxSup.get();
        ctx.enqueueWork(() -> {
            ServerPlayer player = ctx.getSender();
            if (player == null || player.distanceToSqr(packet.pos.getX()+0.5D, packet.pos.getY()+0.5D, packet.pos.getZ()+0.5D) > 64.0D) return;
            if (!player.level.getBlockState(packet.pos).is(ThaumcraftMod.OSMOTIC_ENCHANTER.get())) return;
            OsmoticEnchantmentHelper.Choice[] values = OsmoticEnchantmentHelper.Choice.values();
            OsmoticEnchantmentHelper.apply(player.level, packet.pos, player, values[Math.floorMod(packet.choiceOrdinal, values.length)]);
        });
        ctx.setPacketHandled(true);
    }
}
