package com.darkifov.thaumcraft.network;

import com.darkifov.thaumcraft.client.ClientResearchNoteData;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Supplier;

public class ResearchNoteSyncPacket {
    private final String target;
    private final int progress;
    private final boolean solved;
    private final Map<Integer, String> slots;

    public ResearchNoteSyncPacket(String target, int progress, boolean solved, Map<Integer, String> slots) {
        this.target = target == null ? "" : target;
        this.progress = progress;
        this.solved = solved;
        this.slots = slots == null ? new LinkedHashMap<>() : new LinkedHashMap<>(slots);
    }

    public static void encode(ResearchNoteSyncPacket packet, FriendlyByteBuf buffer) {
        buffer.writeUtf(packet.target);
        buffer.writeVarInt(packet.progress);
        buffer.writeBoolean(packet.solved);
        buffer.writeVarInt(packet.slots.size());

        for (Map.Entry<Integer, String> entry : packet.slots.entrySet()) {
            buffer.writeVarInt(entry.getKey());
            buffer.writeUtf(entry.getValue());
        }
    }

    public static ResearchNoteSyncPacket decode(FriendlyByteBuf buffer) {
        String target = buffer.readUtf();
        int progress = buffer.readVarInt();
        boolean solved = buffer.readBoolean();
        int size = buffer.readVarInt();
        Map<Integer, String> slots = new LinkedHashMap<>();

        for (int i = 0; i < size; i++) {
            slots.put(buffer.readVarInt(), buffer.readUtf());
        }

        return new ResearchNoteSyncPacket(target, progress, solved, slots);
    }

    public static void handle(ResearchNoteSyncPacket packet, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> ClientResearchNoteData.set(packet.target, packet.progress, packet.solved, packet.slots));
        context.setPacketHandled(true);
    }
}
