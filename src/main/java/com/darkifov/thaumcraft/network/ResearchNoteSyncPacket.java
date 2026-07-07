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
    private final int radius;
    private final Map<Integer, String> slots;
    private final Map<Integer, Integer> types;

    public ResearchNoteSyncPacket(String target, int progress, boolean solved, int radius,
                                  Map<Integer, String> slots, Map<Integer, Integer> types) {
        this.target = target == null ? "" : target;
        this.progress = progress;
        this.solved = solved;
        this.radius = radius;
        this.slots = slots == null ? new LinkedHashMap<>() : new LinkedHashMap<>(slots);
        this.types = types == null ? new LinkedHashMap<>() : new LinkedHashMap<>(types);
    }

    public static void encode(ResearchNoteSyncPacket packet, FriendlyByteBuf buffer) {
        buffer.writeUtf(packet.target);
        buffer.writeVarInt(packet.progress);
        buffer.writeBoolean(packet.solved);
        buffer.writeVarInt(packet.radius);
        buffer.writeVarInt(packet.slots.size());
        for (Map.Entry<Integer, String> entry : packet.slots.entrySet()) {
            buffer.writeVarInt(entry.getKey());
            buffer.writeUtf(entry.getValue());
        }
        buffer.writeVarInt(packet.types.size());
        for (Map.Entry<Integer, Integer> entry : packet.types.entrySet()) {
            buffer.writeVarInt(entry.getKey());
            buffer.writeVarInt(entry.getValue());
        }
    }

    public static ResearchNoteSyncPacket decode(FriendlyByteBuf buffer) {
        String target = buffer.readUtf();
        int progress = buffer.readVarInt();
        boolean solved = buffer.readBoolean();
        int radius = buffer.readVarInt();
        int slotSize = buffer.readVarInt();
        Map<Integer, String> slots = new LinkedHashMap<>();
        for (int i = 0; i < slotSize; i++) {
            slots.put(buffer.readVarInt(), buffer.readUtf());
        }
        int typeSize = buffer.readVarInt();
        Map<Integer, Integer> types = new LinkedHashMap<>();
        for (int i = 0; i < typeSize; i++) {
            types.put(buffer.readVarInt(), buffer.readVarInt());
        }
        return new ResearchNoteSyncPacket(target, progress, solved, radius, slots, types);
    }

    public static void handle(ResearchNoteSyncPacket packet, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> ClientResearchNoteData.set(packet.target, packet.progress, packet.solved, packet.radius, packet.slots, packet.types));
        context.setPacketHandled(true);
    }
}
