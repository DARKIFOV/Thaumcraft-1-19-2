package com.darkifov.thaumcraft.network;

import com.darkifov.thaumcraft.client.ClientScanData;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.LinkedHashSet;
import java.util.Set;
import java.util.function.Supplier;

/** Server-to-client mirror of TC4's per-player scanned target ledger. */
public final class ScanKnowledgeSyncPacket {
    private final Set<String> objects;
    private final Set<String> entities;
    private final Set<String> nodes;

    public ScanKnowledgeSyncPacket(Set<String> objects, Set<String> entities, Set<String> nodes) {
        this.objects = copy(objects);
        this.entities = copy(entities);
        this.nodes = copy(nodes);
    }

    private static Set<String> copy(Set<String> source) {
        return source == null ? new LinkedHashSet<>() : new LinkedHashSet<>(source);
    }

    public static void encode(ScanKnowledgeSyncPacket packet, FriendlyByteBuf buffer) {
        writeSet(buffer, packet.objects);
        writeSet(buffer, packet.entities);
        writeSet(buffer, packet.nodes);
    }

    public static ScanKnowledgeSyncPacket decode(FriendlyByteBuf buffer) {
        return new ScanKnowledgeSyncPacket(readSet(buffer), readSet(buffer), readSet(buffer));
    }

    private static void writeSet(FriendlyByteBuf buffer, Set<String> values) {
        buffer.writeVarInt(values.size());
        for (String value : values) {
            buffer.writeUtf(value);
        }
    }

    private static Set<String> readSet(FriendlyByteBuf buffer) {
        int count = buffer.readVarInt();
        Set<String> values = new LinkedHashSet<>();
        for (int i = 0; i < count; i++) {
            values.add(buffer.readUtf());
        }
        return values;
    }

    public static void handle(ScanKnowledgeSyncPacket packet, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> ClientScanData.set(packet.objects, packet.entities, packet.nodes));
        context.setPacketHandled(true);
    }
}
