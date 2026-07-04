
package com.darkifov.thaumcraft.network;
import com.darkifov.thaumcraft.ThaumcraftMod;
import com.darkifov.thaumcraft.block.OsmoticEnchantmentHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;
import java.util.function.Supplier;

public class RequestOsmoticStructureCheckPacket {
    private final BlockPos pos;
    public RequestOsmoticStructureCheckPacket(BlockPos pos) { this.pos = pos; }
    public static void encode(RequestOsmoticStructureCheckPacket p, FriendlyByteBuf b) { b.writeBlockPos(p.pos); }
    public static RequestOsmoticStructureCheckPacket decode(FriendlyByteBuf b) { return new RequestOsmoticStructureCheckPacket(b.readBlockPos()); }
    public static void handle(RequestOsmoticStructureCheckPacket packet, Supplier<NetworkEvent.Context> ctxSup) {
        NetworkEvent.Context ctx = ctxSup.get();
        ctx.enqueueWork(() -> {
            ServerPlayer player = ctx.getSender();
            if (player == null || player.distanceToSqr(packet.pos.getX()+0.5D, packet.pos.getY()+0.5D, packet.pos.getZ()+0.5D) > 64.0D) return;
            if (!player.level.getBlockState(packet.pos).is(ThaumcraftMod.OSMOTIC_ENCHANTER.get())) return;
            OsmoticEnchantmentHelper.showStructureStatus(player.level, packet.pos, player);
        });
        ctx.setPacketHandled(true);
    }
}
