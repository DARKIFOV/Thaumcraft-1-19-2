package com.darkifov.thaumcraft.network;

import com.darkifov.thaumcraft.ThaumcraftMod;
import com.darkifov.thaumcraft.arcane.ArcaneWorkbenchRecipes;
import com.darkifov.thaumcraft.data.PlayerThaumData;
import com.darkifov.thaumcraft.research.PlayerAspectKnowledge;
import com.darkifov.thaumcraft.research.ResearchNoteState;
import com.darkifov.thaumcraft.Aspect;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.simple.SimpleChannel;

import java.util.LinkedHashMap;
import java.util.Map;

public final class ThaumcraftNetwork {
    private static final String PROTOCOL_VERSION = "1";
    private static int packetId = 0;

    public static final SimpleChannel CHANNEL = NetworkRegistry.newSimpleChannel(
            new ResourceLocation(ThaumcraftMod.MOD_ID, "main"),
            () -> PROTOCOL_VERSION,
            PROTOCOL_VERSION::equals,
            PROTOCOL_VERSION::equals
    );

    private ThaumcraftNetwork() {
    }

    public static void register() {
        CHANNEL.registerMessage(
                packetId++,
                ResearchSyncPacket.class,
                ResearchSyncPacket::encode,
                ResearchSyncPacket::decode,
                ResearchSyncPacket::handle
        );

        CHANNEL.registerMessage(
                packetId++,
                AspectKnowledgeSyncPacket.class,
                AspectKnowledgeSyncPacket::encode,
                AspectKnowledgeSyncPacket::decode,
                AspectKnowledgeSyncPacket::handle
        );

        CHANNEL.registerMessage(
                packetId++,
                RequestCombineAspectsPacket.class,
                RequestCombineAspectsPacket::encode,
                RequestCombineAspectsPacket::decode,
                RequestCombineAspectsPacket::handle
        );

        CHANNEL.registerMessage(
                packetId++,
                ResearchNoteSyncPacket.class,
                ResearchNoteSyncPacket::encode,
                ResearchNoteSyncPacket::decode,
                ResearchNoteSyncPacket::handle
        );

        CHANNEL.registerMessage(
                packetId++,
                OpenResearchNotePacket.class,
                OpenResearchNotePacket::encode,
                OpenResearchNotePacket::decode,
                OpenResearchNotePacket::handle
        );

        CHANNEL.registerMessage(
                packetId++,
                RequestPlaceResearchNoteAspectPacket.class,
                RequestPlaceResearchNoteAspectPacket::encode,
                RequestPlaceResearchNoteAspectPacket::decode,
                RequestPlaceResearchNoteAspectPacket::handle
        );

        CHANNEL.registerMessage(
                packetId++,
                RequestSolveResearchNotePacket.class,
                RequestSolveResearchNotePacket::encode,
                RequestSolveResearchNotePacket::decode,
                RequestSolveResearchNotePacket::handle
        );

        CHANNEL.registerMessage(
                packetId++,
                RequestClearResearchNoteSlotPacket.class,
                RequestClearResearchNoteSlotPacket::encode,
                RequestClearResearchNoteSlotPacket::decode,
                RequestClearResearchNoteSlotPacket::handle
        );

        CHANNEL.registerMessage(
                packetId++,
                OpenResearchTablePacket.class,
                OpenResearchTablePacket::encode,
                OpenResearchTablePacket::decode,
                OpenResearchTablePacket::handle
        );

        CHANNEL.registerMessage(
                packetId++,
                RequestResearchTableActionPacket.class,
                RequestResearchTableActionPacket::encode,
                RequestResearchTableActionPacket::decode,
                RequestResearchTableActionPacket::handle
        );

        CHANNEL.registerMessage(
                packetId++,
                RequestResearchUnlockPacket.class,
                RequestResearchUnlockPacket::encode,
                RequestResearchUnlockPacket::decode,
                RequestResearchUnlockPacket::handle
        );

        CHANNEL.registerMessage(
                packetId++,
                RequestSelectResearchPacket.class,
                RequestSelectResearchPacket::encode,
                RequestSelectResearchPacket::decode,
                RequestSelectResearchPacket::handle
        );

        CHANNEL.registerMessage(
                packetId++,
                RequestCompleteSelectedResearchPacket.class,
                RequestCompleteSelectedResearchPacket::encode,
                RequestCompleteSelectedResearchPacket::decode,
                RequestCompleteSelectedResearchPacket::handle
        );

        CHANNEL.registerMessage(
                packetId++,
                ArcaneRecipeSyncPacket.class,
                ArcaneRecipeSyncPacket::encode,
                ArcaneRecipeSyncPacket::decode,
                ArcaneRecipeSyncPacket::handle
        );

        CHANNEL.registerMessage(
                packetId++,
                RequestPechTradePacket.class,
                RequestPechTradePacket::encode,
                RequestPechTradePacket::decode,
                RequestPechTradePacket::handle
        );

        CHANNEL.registerMessage(
                packetId++,
                RequestPechGiftPacket.class,
                RequestPechGiftPacket::encode,
                RequestPechGiftPacket::decode,
                RequestPechGiftPacket::handle
        );

        CHANNEL.registerMessage(
                packetId++,
                RequestEssentiaTerminalScanPacket.class,
                RequestEssentiaTerminalScanPacket::encode,
                RequestEssentiaTerminalScanPacket::decode,
                RequestEssentiaTerminalScanPacket::handle
        );

        CHANNEL.registerMessage(
                packetId++,
                RequestEssentiaInventoryScanPacket.class,
                RequestEssentiaInventoryScanPacket::encode,
                RequestEssentiaInventoryScanPacket::decode,
                RequestEssentiaInventoryScanPacket::handle
        );

        CHANNEL.registerMessage(
                packetId++,
                RequestEssentiaDriveScanPacket.class,
                RequestEssentiaDriveScanPacket::encode,
                RequestEssentiaDriveScanPacket::decode,
                RequestEssentiaDriveScanPacket::handle
        );

        CHANNEL.registerMessage(
                packetId++,
                RequestEssentiaTerminalFilteredScanPacket.class,
                RequestEssentiaTerminalFilteredScanPacket::encode,
                RequestEssentiaTerminalFilteredScanPacket::decode,
                RequestEssentiaTerminalFilteredScanPacket::handle
        );

        CHANNEL.registerMessage(
                packetId++,
                RequestOsmoticEnchantPacket.class,
                RequestOsmoticEnchantPacket::encode,
                RequestOsmoticEnchantPacket::decode,
                RequestOsmoticEnchantPacket::handle
        );

        CHANNEL.registerMessage(
                packetId++,
                RequestOsmoticStructureCheckPacket.class,
                RequestOsmoticStructureCheckPacket::encode,
                RequestOsmoticStructureCheckPacket::decode,
                RequestOsmoticStructureCheckPacket::handle
        );

        CHANNEL.registerMessage(
                packetId++,
                RequestTransvectorStatusPacket.class,
                RequestTransvectorStatusPacket::encode,
                RequestTransvectorStatusPacket::decode,
                RequestTransvectorStatusPacket::handle
        );

        CHANNEL.registerMessage(
                packetId++,
                RequestTransvectorInspectPacket.class,
                RequestTransvectorInspectPacket::encode,
                RequestTransvectorInspectPacket::decode,
                RequestTransvectorInspectPacket::handle
        );

        CHANNEL.registerMessage(
                packetId++,
                RequestTransvectorClearPacket.class,
                RequestTransvectorClearPacket::encode,
                RequestTransvectorClearPacket::decode,
                RequestTransvectorClearPacket::handle
        );

        CHANNEL.registerMessage(
                packetId++,
                RequestTransvectorActionPacket.class,
                RequestTransvectorActionPacket::encode,
                RequestTransvectorActionPacket::decode,
                RequestTransvectorActionPacket::handle
        );

        CHANNEL.registerMessage(
                packetId++,
                RequestWandArchitectTogglePacket.class,
                RequestWandArchitectTogglePacket::encode,
                RequestWandArchitectTogglePacket::decode,
                RequestWandArchitectTogglePacket::handle
        );

        CHANNEL.registerMessage(
                packetId++,
                RequestFocusChangePacket.class,
                RequestFocusChangePacket::encode,
                RequestFocusChangePacket::decode,
                RequestFocusChangePacket::handle
        );
    }

    public static void syncResearch(ServerPlayer player) {
        CHANNEL.send(
                PacketDistributor.PLAYER.with(() -> player),
                new ResearchSyncPacket(PlayerThaumData.getResearchSet(player), PlayerThaumData.getWarp(player))
        );
    }

    public static void syncAspectKnowledge(ServerPlayer player) {
        PlayerAspectKnowledge.seedPrimals(player);
        CHANNEL.send(
                PacketDistributor.PLAYER.with(() -> player),
                new AspectKnowledgeSyncPacket(PlayerAspectKnowledge.knownAspectIds(player), PlayerAspectKnowledge.poolAmounts(player))
        );
    }

    public static void syncArcaneRecipes(ServerPlayer player) {
        CHANNEL.send(
                PacketDistributor.PLAYER.with(() -> player),
                ArcaneRecipeSyncPacket.fromRecipes(ArcaneWorkbenchRecipes.recipes())
        );
    }

    public static void openResearchTable(ServerPlayer player) {
        syncResearch(player);
        syncAspectKnowledge(player);
        CHANNEL.send(PacketDistributor.PLAYER.with(() -> player), new OpenResearchTablePacket());
    }

    public static void requestUnlockFromClient() {
        CHANNEL.sendToServer(new RequestResearchUnlockPacket());
    }

    public static void requestSelectResearchFromClient(String researchKey) {
        CHANNEL.sendToServer(new RequestSelectResearchPacket(researchKey));
    }

    public static void requestCompleteSelectedResearchFromClient() {
        CHANNEL.sendToServer(new RequestCompleteSelectedResearchPacket());
    }

    public static void requestCombineAspectsFromClient(String firstId, String secondId) {
        CHANNEL.sendToServer(new RequestCombineAspectsPacket(firstId, secondId));
    }

    public static void requestResearchTableActionFromClient(net.minecraft.core.BlockPos pos, int action) {
        CHANNEL.sendToServer(new RequestResearchTableActionPacket(pos, action));
    }

    public static void syncResearchNote(ServerPlayer player, net.minecraft.world.item.ItemStack note) {
        ResearchNoteState.initialize(note, ResearchNoteState.target(note));
        Map<Integer, String> slots = new LinkedHashMap<>();
        Map<Integer, Integer> types = new LinkedHashMap<>();

        for (Map.Entry<Integer, Aspect> entry : ResearchNoteState.slots(note).entrySet()) {
            slots.put(entry.getKey(), entry.getValue().id());
        }
        types.putAll(ResearchNoteState.slotTypes(note));

        CHANNEL.send(
                PacketDistributor.PLAYER.with(() -> player),
                new ResearchNoteSyncPacket(
                        ResearchNoteState.target(note),
                        ResearchNoteState.progress(note),
                        ResearchNoteState.solved(note),
                        ResearchNoteState.radius(note),
                        slots,
                        types
                )
        );
    }

    public static void openResearchNote(ServerPlayer player, net.minecraft.world.item.ItemStack note) {
        syncAspectKnowledge(player);
        syncResearch(player);
        syncResearchNote(player, note);
        CHANNEL.send(PacketDistributor.PLAYER.with(() -> player), new OpenResearchNotePacket());
    }

    public static void requestPlaceResearchNoteAspectFromClient(int slot, String aspectId) {
        CHANNEL.sendToServer(new RequestPlaceResearchNoteAspectPacket(slot, aspectId));
    }

    public static void requestSolveResearchNoteFromClient() {
        CHANNEL.sendToServer(new RequestSolveResearchNotePacket());
    }

    public static void requestClearResearchNoteSlotFromClient(int slot) {
        CHANNEL.sendToServer(new RequestClearResearchNoteSlotPacket(slot));
    }

    public static void requestPechTradeFromClient(int pechEntityId, int tier) {
        CHANNEL.sendToServer(new RequestPechTradePacket(pechEntityId, tier));
    }

    public static void requestPechGiftFromClient(int pechEntityId) {
        CHANNEL.sendToServer(new RequestPechGiftPacket(pechEntityId));
    }

    public static void requestEssentiaTerminalScan(net.minecraft.core.BlockPos pos) {
        CHANNEL.sendToServer(new RequestEssentiaTerminalScanPacket(pos));
    }

    public static void requestEssentiaInventoryScan(net.minecraft.core.BlockPos pos) {
        CHANNEL.sendToServer(new RequestEssentiaInventoryScanPacket(pos));
    }

    public static void requestEssentiaDriveScan(net.minecraft.core.BlockPos pos) {
        CHANNEL.sendToServer(new RequestEssentiaDriveScanPacket(pos));
    }

    public static void requestEssentiaTerminalFilteredScan(net.minecraft.core.BlockPos pos, int aspectOrdinal) {
        CHANNEL.sendToServer(new RequestEssentiaTerminalFilteredScanPacket(pos, aspectOrdinal));
    }

    public static void requestOsmoticEnchant(net.minecraft.core.BlockPos pos, int choiceOrdinal) {
        CHANNEL.sendToServer(new RequestOsmoticEnchantPacket(pos, choiceOrdinal));
    }

    public static void requestOsmoticStructureCheck(net.minecraft.core.BlockPos pos) {
        CHANNEL.sendToServer(new RequestOsmoticStructureCheckPacket(pos));
    }

    public static void requestTransvectorStatus(net.minecraft.core.BlockPos pos) {
        CHANNEL.sendToServer(new RequestTransvectorStatusPacket(pos));
    }

    public static void requestTransvectorInspect(net.minecraft.core.BlockPos pos) {
        CHANNEL.sendToServer(new RequestTransvectorInspectPacket(pos));
    }

    public static void requestTransvectorClear(net.minecraft.core.BlockPos pos) {
        CHANNEL.sendToServer(new RequestTransvectorClearPacket(pos));
    }

    public static void requestTransvectorAction(net.minecraft.core.BlockPos pos, int action) {
        CHANNEL.sendToServer(new RequestTransvectorActionPacket(pos, action));
    }


    public static void requestWandArchitectToggleFromClient() {
        CHANNEL.sendToServer(new RequestWandArchitectTogglePacket((byte) 1));
    }

    public static void requestFocusChangeFromClient(String focus) {
        CHANNEL.sendToServer(new RequestFocusChangePacket(focus));
    }
}