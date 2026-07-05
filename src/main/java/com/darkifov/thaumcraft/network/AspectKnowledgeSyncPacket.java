package com.darkifov.thaumcraft.network;

import com.darkifov.thaumcraft.client.ClientAspectData;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;

public class AspectKnowledgeSyncPacket {
    private final Set<String> known;
    private final Map<String, Integer> pool;

    public AspectKnowledgeSyncPacket(Set<String> known, Map<String, Integer> pool) {
        this.known = known == null ? new HashSet<>() : new HashSet<>(known);
        this.pool = pool == null ? new HashMap<>() : new HashMap<>(pool);
    }

    public static void encode(AspectKnowledgeSyncPacket packet, FriendlyByteBuf buffer) {
        buffer.writeVarInt(packet.known.size());

        for (String key : packet.known) {
            buffer.writeUtf(key);
        }

        buffer.writeVarInt(packet.pool.size());

        for (Map.Entry<String, Integer> entry : packet.pool.entrySet()) {
            buffer.writeUtf(entry.getKey());
            buffer.writeVarInt(entry.getValue());
        }
    }

    public static AspectKnowledgeSyncPacket decode(FriendlyByteBuf buffer) {
        int knownSize = buffer.readVarInt();
        Set<String> known = new HashSet<>();

        for (int i = 0; i < knownSize; i++) {
            known.add(buffer.readUtf());
        }

        int poolSize = buffer.readVarInt();
        Map<String, Integer> pool = new HashMap<>();

        for (int i = 0; i < poolSize; i++) {
            pool.put(buffer.readUtf(), buffer.readVarInt());
        }

        return new AspectKnowledgeSyncPacket(known, pool);
    }

    public static void handle(AspectKnowledgeSyncPacket packet, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();

        context.enqueueWork(() -> ClientAspectData.set(packet.known, packet.pool));
        context.setPacketHandled(true);
    }
}
