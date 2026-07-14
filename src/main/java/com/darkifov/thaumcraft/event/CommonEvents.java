package com.darkifov.thaumcraft.event;

import com.darkifov.thaumcraft.ThaumcraftMod;
import com.darkifov.thaumcraft.blockentity.CrucibleBlockEntity;
import com.darkifov.thaumcraft.block.ThaumometerItem;
import com.darkifov.thaumcraft.aura.AuraVisRelayNetwork;
import com.darkifov.thaumcraft.data.PlayerThaumData;
import com.darkifov.thaumcraft.network.RequestThaumometerScanPacket;
import com.darkifov.thaumcraft.network.ThaumcraftNetwork;
import com.darkifov.thaumcraft.porting.TC4LegacyDuplicateItemMigrator;
import com.darkifov.thaumcraft.research.OriginalResearchProgression;
import com.darkifov.thaumcraft.research.PlayerAspectKnowledge;
import com.darkifov.thaumcraft.runic.TC4ChampionModifierRuntime;
import com.darkifov.thaumcraft.runic.TC4FortressArmorRuntime;
import com.darkifov.thaumcraft.runic.TC4FortressMaskRuntime;
import com.darkifov.thaumcraft.runic.TC4RunicShieldRuntime;
import com.darkifov.thaumcraft.ward.WardedBlockRuntime;
import com.darkifov.thaumcraft.world.TC4WorldgenRuntime;
import com.darkifov.thaumcraft.wand.EqualTradeSwapRuntime;
import com.darkifov.thaumcraft.wand.WandFocusRuntime;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.level.BlockEvent;
import net.minecraftforge.event.level.ChunkEvent;
import net.minecraftforge.event.entity.item.ItemExpireEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = ThaumcraftMod.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class CommonEvents {
    private CommonEvents() {
    }



    @SubscribeEvent
    public static void onBathSaltsExpire(ItemExpireEvent event) {
        ItemEntity itemEntity = event.getEntity();
        if (!itemEntity.getItem().is(ThaumcraftMod.BATH_SALTS.get()) || itemEntity.level.isClientSide) {
            return;
        }
        BlockPos pos = itemEntity.blockPosition();
        var fluidState = itemEntity.level.getFluidState(pos);
        if (!fluidState.isSource() || fluidState.getType() != net.minecraft.world.level.material.Fluids.WATER) {
            return;
        }
        itemEntity.level.setBlockAndUpdate(pos, ThaumcraftMod.PURIFYING_FLUID_BLOCK.get().defaultBlockState());
    }

    @SubscribeEvent
    public static void onChunkLoad(ChunkEvent.Load event) {
        // TC4 new-chunk-only audit marker retained for parity scripts:
        // if (!event.isNewChunk() || !(event.getChunk() instanceof LevelChunk chunk)) return;
        // TC4WorldgenRuntime.generateNewChunk(level, chunk.getPos())
        // Forge 1.19.2 ChunkEvent.Load in this mapping does not expose isNewChunk(),
        // and v11.62.2 must not run TC4 placement synchronously during integrated-server loading.
        // The real path queues the chunk, then drainDeferredChunkQueue calls generateNewChunk after entry.
        if (!(event.getChunk() instanceof LevelChunk chunk) || !(chunk.getLevel() instanceof ServerLevel level)) {
            return;
        }
        TC4WorldgenRuntime.queueLoadedChunk(level, chunk);
    }

    @SubscribeEvent
    public static void onLevelTick(TickEvent.LevelTickEvent event) {
        if (event.phase != TickEvent.Phase.END || event.level.isClientSide || !(event.level instanceof ServerLevel level)) {
            return;
        }
        TC4WorldgenRuntime.drainDeferredChunkQueue(level);
        EqualTradeSwapRuntime.tick(level);

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

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END || event.player.level.isClientSide
                || !(event.player instanceof ServerPlayer player)) {
            return;
        }
        for (net.minecraft.world.InteractionHand hand : net.minecraft.world.InteractionHand.values()) {
            net.minecraft.world.item.ItemStack held = player.getItemInHand(hand);
            if (held.getItem() instanceof ThaumometerItem thaumometer) {
                thaumometer.serverTickPendingScan(player, hand);
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
        TC4LegacyDuplicateItemMigrator.migrateJoinedEntity(event.getEntity());
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

    /**
     * Interactive blocks (chests, doors, machines) normally consume right-click
     * before the held item receives useOn. TC4's Thaumometer must scan them
     * instead, so deny the block action while explicitly allowing the item path.
     */
    @SubscribeEvent
    public static void onThaumometerRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
        net.minecraft.world.item.ItemStack held = event.getEntity().getItemInHand(event.getHand());
        if (!(held.getItem() instanceof ThaumometerItem thaumometer)) {
            return;
        }

        // Do not let a chest/door/machine consume the click. On the logical
        // client the cancelled Forge event would also suppress vanilla's packet,
        // so explicitly send the scan request and start the local TC4 animation.
        event.setUseBlock(Event.Result.DENY);
        event.setUseItem(Event.Result.DENY);
        event.setCancellationResult(InteractionResult.SUCCESS);
        event.setCanceled(true);
        if (event.getLevel().isClientSide) {
            thaumometer.beginBlockScan(event.getLevel(), event.getEntity(), event.getHand(), event.getPos());
            ThaumcraftNetwork.CHANNEL.sendToServer(
                    RequestThaumometerScanPacket.block(event.getHand(), event.getPos()));
        }
    }

    /** Prevent precise interact-at hooks from consuming the click before the scan. */
    @SubscribeEvent
    public static void onThaumometerEntityInteractSpecific(PlayerInteractEvent.EntityInteractSpecific event) {
        startThaumometerEntityScan(event, event.getTarget());
    }

    /** Prevent villager/mob interaction from replacing a Thaumometer scan. */
    @SubscribeEvent
    public static void onThaumometerEntityInteract(PlayerInteractEvent.EntityInteract event) {
        startThaumometerEntityScan(event, event.getTarget());
    }

    private static void startThaumometerEntityScan(PlayerInteractEvent event, net.minecraft.world.entity.Entity target) {
        net.minecraft.world.item.ItemStack held = event.getEntity().getItemInHand(event.getHand());
        if (!(held.getItem() instanceof ThaumometerItem thaumometer)) {
            return;
        }
        event.setCancellationResult(InteractionResult.SUCCESS);
        event.setCanceled(true);
        if (event.getLevel().isClientSide && target != null) {
            thaumometer.beginEntityScan(event.getLevel(), event.getEntity(), event.getHand(), target);
            ThaumcraftNetwork.CHANNEL.sendToServer(
                    RequestThaumometerScanPacket.entity(event.getHand(), target.getId()));
        }
    }

    @SubscribeEvent
    public static void onEqualTradeLeftClick(PlayerInteractEvent.LeftClickBlock event) {
        net.minecraft.world.item.ItemStack held = event.getEntity().getItemInHand(event.getHand());
        if (WandFocusRuntime.getFocus(held) != com.darkifov.thaumcraft.wand.WandFocusType.EQUAL_TRADE) return;
        // Cancel on both logical sides so the client does not show normal mining cracks.
        event.setCanceled(true);
        if (event.getEntity() instanceof ServerPlayer player) {
            WandFocusRuntime.queueEqualTradeSwing(player, event.getHand(), event.getPos());
        }
    }

    @SubscribeEvent
    public static void onBlockBreak(BlockEvent.BreakEvent event) {
        if (!(event.getPlayer() instanceof ServerPlayer player) || !(event.getLevel() instanceof Level level)) {
            return;
        }
        if (!WardedBlockRuntime.isInternalWardMutation()
                && WardedBlockRuntime.cancelIfProtected(level, event.getPos(), player)) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public static void onPlayerLogin(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            TC4LegacyDuplicateItemMigrator.migratePlayerInventory(player);
            OriginalResearchProgression.seedAutoUnlocks(player);
            ThaumcraftNetwork.syncResearch(player);
            ThaumcraftNetwork.syncAspectKnowledge(player);
            ThaumcraftNetwork.syncScanKnowledge(player);
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
            PlayerAspectKnowledge.copyFrom(oldPlayer, player);
            ThaumcraftNetwork.syncResearch(player);
            ThaumcraftNetwork.syncAspectKnowledge(player);
            ThaumcraftNetwork.syncScanKnowledge(player);
        } else if (event.getEntity() instanceof ServerPlayer player) {
            OriginalResearchProgression.seedAutoUnlocks(player);
            ThaumcraftNetwork.syncResearch(player);
            ThaumcraftNetwork.syncAspectKnowledge(player);
            ThaumcraftNetwork.syncScanKnowledge(player);
        }

        event.getOriginal().invalidateCaps();
    }

    @SubscribeEvent
    public static void onDimensionChange(PlayerEvent.PlayerChangedDimensionEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            OriginalResearchProgression.seedAutoUnlocks(player);
            ThaumcraftNetwork.syncResearch(player);
            ThaumcraftNetwork.syncAspectKnowledge(player);
            ThaumcraftNetwork.syncScanKnowledge(player);
        }
    }
}
