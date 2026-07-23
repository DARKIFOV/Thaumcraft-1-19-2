package com.darkifov.thaumcraft.network;

import com.darkifov.thaumcraft.client.ClientScanData;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.LinkedHashSet;
import java.util.Set;
import java.util.function.Supplier;

/** Server-to-client mirror of TC4's per-player scanned target ledger. */
public final class ScanKnowledgeSyncPacket {
    private final Set<String> objects, entities, nodes, phenomena;

    public ScanKnowledgeSyncPacket(Set<String> objects, Set<String> entities, Set<String> nodes, Set<String> phenomena) {
        this.objects=copy(objects); this.entities=copy(entities); this.nodes=copy(nodes); this.phenomena=copy(phenomena);
    }
    private static Set<String> copy(Set<String> s){return s==null?new LinkedHashSet<>():new LinkedHashSet<>(s);}
    public static void encode(ScanKnowledgeSyncPacket p, FriendlyByteBuf b){writeSet(b,p.objects);writeSet(b,p.entities);writeSet(b,p.nodes);writeSet(b,p.phenomena);}
    public static ScanKnowledgeSyncPacket decode(FriendlyByteBuf b){return new ScanKnowledgeSyncPacket(readSet(b),readSet(b),readSet(b),readSet(b));}
    private static void writeSet(FriendlyByteBuf b,Set<String> v){b.writeVarInt(v.size());for(String s:v)b.writeUtf(s);}
    private static Set<String> readSet(FriendlyByteBuf b){int n=b.readVarInt();Set<String> v=new LinkedHashSet<>();for(int i=0;i<n;i++)v.add(b.readUtf());return v;}
    public static void handle(ScanKnowledgeSyncPacket p,Supplier<NetworkEvent.Context> c){NetworkEvent.Context x=c.get();x.enqueueWork(()->ClientScanData.set(p.objects,p.entities,p.nodes,p.phenomena));x.setPacketHandled(true);}
}
