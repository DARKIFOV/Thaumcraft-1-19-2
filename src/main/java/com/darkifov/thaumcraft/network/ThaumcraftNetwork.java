package com.darkifov.thaumcraft.network;

import com.darkifov.thaumcraft.ThaumcraftMod;
import com.darkifov.thaumcraft.arcane.ArcaneWorkbenchRecipes;
import com.darkifov.thaumcraft.data.PlayerThaumData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.simple.SimpleChannel;

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
                OpenResearchTablePacket.class,
                OpenResearchTablePacket::encode,
                OpenResearchTablePacket::decode,
                OpenResearchTablePacket::handle
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
                OpenArcaneWorkbenchPacket.class,
                OpenArcaneWorkbenchPacket::encode,
                OpenArcaneWorkbenchPacket::decode,
                OpenArcaneWorkbenchPacket::handle
        );

        CHANNEL.registerMessage(
                packetId++,
                RequestArcaneCraftPacket.class,
                RequestArcaneCraftPacket::encode,
                RequestArcaneCraftPacket::decode,
                RequestArcaneCraftPacket::handle
        );

        CHANNEL.registerMessage(
                packetId++,
                RequestArcaneMenuCraftPacket.class,
                RequestArcaneMenuCraftPacket::encode,
                RequestArcaneMenuCraftPacket::decode,
                RequestArcaneMenuCraftPacket::handle
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
    }

    public static void syncResearch(ServerPlayer player) {
        CHANNEL.send(
                PacketDistributor.PLAYER.with(() -> player),
                new ResearchSyncPacket(PlayerThaumData.getResearchSet(player), PlayerThaumData.getWarp(player))
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
        CHANNEL.send(PacketDistributor.PLAYER.with(() -> player), new OpenResearchTablePacket());
    }

    public static void openArcaneWorkbench(ServerPlayer player) {
        syncResearch(player);
        syncArcaneRecipes(player);
        CHANNEL.send(PacketDistributor.PLAYER.with(() -> player), new OpenArcaneWorkbenchPacket());
    }

    public static void requestUnlockFromClient() {
        CHANNEL.sendToServer(new RequestResearchUnlockPacket());
    }

    public static void requestArcaneCraftFromClient(ResourceLocation recipeId) {
        CHANNEL.sendToServer(new RequestArcaneCraftPacket(recipeId));
    }

    public static void requestArcaneMenuCraftFromClient(net.minecraft.core.BlockPos pos, ResourceLocation recipeId) {
        CHANNEL.sendToServer(new RequestArcaneMenuCraftPacket(pos, recipeId));
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
}
