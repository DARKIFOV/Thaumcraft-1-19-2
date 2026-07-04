package com.darkifov.thaumcraft.event;

import com.darkifov.thaumcraft.ThaumcraftMod;
import com.darkifov.thaumcraft.data.PlayerThaumData;
import com.darkifov.thaumcraft.network.ThaumcraftNetwork;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = ThaumcraftMod.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class CommonEvents {
    private CommonEvents() {
    }

    @SubscribeEvent
    public static void onPlayerLogin(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            ThaumcraftNetwork.syncResearch(player);
        }
    }

    @SubscribeEvent
    public static void onPlayerClone(PlayerEvent.Clone event) {
        if (!event.isWasDeath()) {
            return;
        }

        event.getOriginal().reviveCaps();

        if (event.getEntity() instanceof ServerPlayer player && event.getOriginal() instanceof ServerPlayer oldPlayer) {
            PlayerThaumData.copyFrom(oldPlayer, player);
            ThaumcraftNetwork.syncResearch(player);
        } else if (event.getEntity() instanceof ServerPlayer player) {
            ThaumcraftNetwork.syncResearch(player);
        }

        event.getOriginal().invalidateCaps();
    }

    @SubscribeEvent
    public static void onDimensionChange(PlayerEvent.PlayerChangedDimensionEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            ThaumcraftNetwork.syncResearch(player);
        }
    }
}
