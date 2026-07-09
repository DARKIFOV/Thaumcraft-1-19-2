package com.darkifov.thaumcraft.event;

import com.darkifov.thaumcraft.ThaumcraftMod;
import com.darkifov.thaumcraft.blockentity.CrucibleBlockEntity;
import com.darkifov.thaumcraft.aura.AuraVisRelayNetwork;
import com.darkifov.thaumcraft.data.PlayerThaumData;
import com.darkifov.thaumcraft.network.ThaumcraftNetwork;
import com.darkifov.thaumcraft.research.OriginalResearchProgression;
import com.darkifov.thaumcraft.runic.TC4ChampionModifierRuntime;
import com.darkifov.thaumcraft.runic.TC4FortressArmorRuntime;
import com.darkifov.thaumcraft.runic.TC4FortressMaskRuntime;
import com.darkifov.thaumcraft.runic.TC4RunicShieldRuntime;
import com.darkifov.thaumcraft.ward.WardedBlockRuntime;
import com.darkifov.thaumcraft.world.TC4WorldgenRuntime;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.level.BlockEvent;
import net.minecraftforge.event.level.ChunkEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = ThaumcraftMod.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class CommonEvents {
    private CommonEvents() {
    }



    @SubscribeEvent
    public static void onChunkLoad(ChunkEvent.Load event) {
        if (!event.isNewChunk() || !(event.getChunk() instanceof LevelChunk chunk) || !(chunk.getLevel() instanceof ServerLevel level)) {
            return;
        }
        TC4WorldgenRuntime.generateNewChunk(level, chunk.getPos());
    }

    @SubscribeEvent
    public static void onLevelTick(TickEvent.LevelTickEvent event) {
        if (event.phase != TickEvent.Phase.END || event.level.isClientSide || !(event.level instanceof ServerLevel level)) {
            return;
        }
        for (ServerPlayer player : level.players()) {
            TC4RunicShieldRuntime.tick(player);
        }

        if (level.getGameTime() % 5L != 0L) {
            return;
        }

        for (ServerPlayer player : level.players()) {
            AuraVisRelayNetwork.tickPlayerRecharge(level, player);
            AABB scan = player.getBoundingBox().inflate(24.0D);
            for (ItemEntity itemEntity : level.getEntitiesOfClass(ItemEntity.class, scan, ItemEntity::isAlive)) {
                tryFeedCrucible(level, itemEntity);
            }
        }
    }

    private static void tryFeedCrucible(ServerLevel level, ItemEntity itemEntity) {
        BlockPos itemPos = itemEntity.blockPosition();
        for (BlockPos candidate : new BlockPos[] { itemPos, itemPos.below(), itemPos.below(2) }) {
            BlockEntity blockEntity = level.getBlockEntity(candidate);
            if (blockEntity instanceof CrucibleBlockEntity crucible && crucible.tryAcceptThrownItem(itemEntity)) {
                return;
            }
        }
    }



    @SubscribeEvent
    public static void onEntityJoinLevel(EntityJoinLevelEvent event) {
        TC4ChampionModifierRuntime.maybeMakeSpawnChampion(event.getEntity());
    }

    @SubscribeEvent
    public static void onLivingHurt(LivingHurtEvent event) {
        TC4FortressMaskRuntime.handleHurt(event);
        TC4ChampionModifierRuntime.handleHurt(event);
        TC4RunicShieldRuntime.handleHurt(event);
        TC4FortressArmorRuntime.handleHurt(event);
    }

    @SubscribeEvent
    public static void onLivingTick(LivingEvent.LivingTickEvent event) {
        TC4ChampionModifierRuntime.tick(event.getEntity());
    }

    @SubscribeEvent
    public static void onBlockBreak(BlockEvent.BreakEvent event) {
        if (!(event.getPlayer() instanceof ServerPlayer player) || !(event.getLevel() instanceof Level level)) {
            return;
        }
        if (WardedBlockRuntime.cancelIfProtected(level, event.getPos(), player)) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public static void onPlayerLogin(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            OriginalResearchProgression.seedAutoUnlocks(player);
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
            OriginalResearchProgression.seedAutoUnlocks(player);
            ThaumcraftNetwork.syncResearch(player);
        }

        event.getOriginal().invalidateCaps();
    }

    @SubscribeEvent
    public static void onDimensionChange(PlayerEvent.PlayerChangedDimensionEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            OriginalResearchProgression.seedAutoUnlocks(player);
            ThaumcraftNetwork.syncResearch(player);
        }
    }
}
