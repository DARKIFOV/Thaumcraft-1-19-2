package com.darkifov.thaumcraft.block;

import com.darkifov.thaumcraft.Aspect;
import com.darkifov.thaumcraft.AspectDatabase;
import com.darkifov.thaumcraft.AspectList;
import com.darkifov.thaumcraft.aura.TC4AuraNodeScanParity;
import com.darkifov.thaumcraft.aura.TC4ThaumometerParity;
import com.darkifov.thaumcraft.aura.TC4ThaumometerScanKeys;
import com.darkifov.thaumcraft.aura.TC4ThaumometerTargeting;
import com.darkifov.thaumcraft.blockentity.AuraNodeBlockEntity;
import com.darkifov.thaumcraft.blockentity.ManaPodBlockEntity;
import com.darkifov.thaumcraft.client.render.ThaumometerItemRenderer;
import com.darkifov.thaumcraft.data.NodeScanData;
import com.darkifov.thaumcraft.data.PlayerThaumData;
import com.darkifov.thaumcraft.network.ThaumcraftNetwork;
import com.darkifov.thaumcraft.porting.TC4Sounds;
import com.darkifov.thaumcraft.research.OriginalResearchProgression;
import com.darkifov.thaumcraft.research.PlayerAspectKnowledge;
import com.darkifov.thaumcraft.research.ResearchTableFoundation;
import com.darkifov.thaumcraft.source.TC4EntityAspectRegistry;
import net.minecraft.ChatFormatting;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

/** Full TC4 ItemThaumometer/ScanManager handheld "@" adapter. */
public class ThaumometerItem extends Item {
    private static final String TAG_SCANNED = "ScannedBlocks";
    private static final String TAG_SCANNED_ENTITIES = "ScannedEntities";
    private static final String TAG_SCANNED_NODES = "ScannedAuraNodes";
    private static final String TAG_PENDING_BLOCK_SCAN = "TC4PendingBlockScan";
    private static final String TAG_PENDING_ENTITY_SCAN = "TC4PendingEntityScan";
    private static final String TAG_PENDING_SCAN_START = "TC4PendingScanStart";
    private static final String TAG_PENDING_SCAN_TICK = "TC4PendingScanTick";

    /** Original startScan is transient item-instance state, never ItemStack NBT. */
    private static final Map<UUID, PendingScan> PENDING_SCANS = new ConcurrentHashMap<>();

    private record PendingScan(InteractionHand hand, TC4ThaumometerTargeting.Kind kind, String stableKey,
                               BlockPos blockPos, UUID entityId, long startedTick, long lastProcessedTick) {
        PendingScan processedAt(long tick) {
            return new PendingScan(hand, kind, stableKey, blockPos, entityId, startedTick, tick);
        }
    }

    public ThaumometerItem(Properties properties) { super(properties); }

    @Override
    public void initializeClient(Consumer<IClientItemExtensions> consumer) {
        consumer.accept(new IClientItemExtensions() {
            @Override public BlockEntityWithoutLevelRenderer getCustomRenderer() { return ThaumometerItemRenderer.instance(); }
        });
    }

    @Override public int getUseDuration(ItemStack stack) { return TC4ThaumometerParity.USE_DURATION_TICKS; }
    @Override public UseAnim getUseAnimation(ItemStack stack) { return UseAnim.NONE; }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        return beginTargetScan(level, player, hand, player.getItemInHand(hand), TC4ThaumometerTargeting.find(player, 1.0F));
    }

    public InteractionResultHolder<ItemStack> beginBlockScan(Level level, Player player, InteractionHand hand, BlockPos pos) {
        ItemStack stack=player.getItemInHand(hand);
        if(pos==null || !TC4AuraNodeScanParity.isWithinScanRange(player,pos)) return InteractionResultHolder.pass(stack);
        return beginTargetScan(level,player,hand,stack,TC4ThaumometerTargeting.forBlock(player,pos));
    }

    public InteractionResultHolder<ItemStack> beginEntityScan(Level level, Player player, InteractionHand hand, Entity entity) {
        ItemStack stack=player.getItemInHand(hand);
        if(!isValidEntityScanTarget(player,entity)) return InteractionResultHolder.pass(stack);
        return beginTargetScan(level,player,hand,stack,TC4ThaumometerTargeting.forEntity(entity));
    }

    private InteractionResultHolder<ItemStack> beginTargetScan(Level level, Player player, InteractionHand hand,
                                                                ItemStack stack, TC4ThaumometerTargeting.ScanTarget target) {
        if(target==null || !target.isPresent()) {
            if(!level.isClientSide) PENDING_SCANS.remove(player.getUUID());
            return InteractionResultHolder.pass(stack);
        }
        if(!level.isClientSide) {
            migrateLegacyItemLedger(player,stack);
            if(!target.hasAspects() || !canUnderstandScan(player,target.aspects()) || !isNewScanTarget(player,target)) {
                PENDING_SCANS.remove(player.getUUID());
                return InteractionResultHolder.pass(stack);
            }
            PENDING_SCANS.put(player.getUUID(), new PendingScan(hand,target.kind(),target.stableKey(),
                    target.blockPos()==null?null:target.blockPos().immutable(),
                    target.entity()==null?null:target.entity().getUUID(),level.getGameTime(),Long.MIN_VALUE));
        }
        player.startUsingItem(hand);
        return InteractionResultHolder.sidedSuccess(stack,level.isClientSide);
    }

    @Override
    public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity livingEntity) {
        if(!level.isClientSide && livingEntity instanceof Player player) PENDING_SCANS.remove(player.getUUID());
        return stack;
    }

    @Override
    public void releaseUsing(ItemStack stack, Level level, LivingEntity livingEntity, int timeLeft) {
        if(!level.isClientSide && livingEntity instanceof Player player) PENDING_SCANS.remove(player.getUUID());
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Player player=context.getPlayer();
        return player==null?InteractionResult.PASS:beginBlockScan(context.getLevel(),player,context.getHand(),context.getClickedPos()).getResult();
    }

    @Override
    public void onUseTick(Level level, LivingEntity livingEntity, ItemStack stack, int remainingUseDuration) {
        if(!level.isClientSide && livingEntity instanceof ServerPlayer player) serverTickPendingScan(player,player.getUsedItemHand());
    }

    /** CommonEvents fallback for interactions consumed before vanilla Item#use. */
    public void serverTickPendingScan(ServerPlayer player, InteractionHand hand) {
        if(player==null || hand==null) return;
        PendingScan pending=PENDING_SCANS.get(player.getUUID());
        if(pending==null || pending.hand()!=hand || player.getItemInHand(hand).getItem()!=this) return;
        long now=player.level.getGameTime();
        if(pending.lastProcessedTick()==now) return;
        pending=pending.processedAt(now);
        PENDING_SCANS.put(player.getUUID(),pending);

        TC4ThaumometerTargeting.ScanTarget current=TC4ThaumometerTargeting.find(player,1.0F);
        if(!matchesPending(pending,current) || !current.hasAspects() || !isNewScanTarget(player,current)) {
            PENDING_SCANS.remove(player.getUUID());
            player.stopUsingItem();
            return;
        }

        long elapsed=Math.max(0L,now-pending.startedTick());
        if(TC4ThaumometerParity.shouldPlayCameraTickAfterElapsed(elapsed)) {
            player.level.playSound(null,player.blockPosition(),TC4Sounds.event("cameraticks"), SoundSource.PLAYERS,
                    TC4ThaumometerParity.CAMERA_TICK_VOLUME,
                    TC4ThaumometerParity.CAMERA_TICK_PITCH_BASE+player.level.random.nextFloat()*TC4ThaumometerParity.CAMERA_TICK_PITCH_RANDOM_SPAN);
        }
        if(TC4ThaumometerParity.shouldCompleteAfterElapsed(elapsed)) {
            completeScan(player,current);
            PENDING_SCANS.remove(player.getUUID());
            player.stopUsingItem();
        }
    }

    private boolean matchesPending(PendingScan pending, TC4ThaumometerTargeting.ScanTarget current) {
        if(current==null || !current.isPresent() || pending.kind()!=current.kind() || !pending.stableKey().equals(current.stableKey())) return false;
        if(pending.blockPos()!=null && !pending.blockPos().equals(current.blockPos())) return false;
        return pending.entityId()==null || (current.entity()!=null && pending.entityId().equals(current.entity().getUUID()));
    }

    private boolean completeScan(ServerPlayer player, TC4ThaumometerTargeting.ScanTarget target) {
        if(!canUnderstandScan(player,target.aspects())) return false;
        boolean first;
        String trigger=target.stableKey();
        String legacyEntityTrigger=null;
        switch(target.kind()) {
            case NODE -> {
                AuraNodeBlockEntity node=target.node();
                if(node==null || target.blockPos()==null) return false;
                NodeScanData.migrateLegacyPosition(player,target.blockPos(),target.stableKey());
                first=NodeScanData.markScanned(player,target.stableKey());
                PlayerThaumData.markScannedPhenomenon(player,target.stableKey());
                trigger=TC4AuraNodeScanParity.ORIGINAL_AURA_NODE_SCAN_KEY;
            }
            case ENTITY -> {
                if(target.entity()==null) return false;
                first=PlayerThaumData.markScannedEntity(player,target.stableKey());
                legacyEntityTrigger=TC4EntityAspectRegistry.legacyScanTriggerId(target.entity());
                trigger=TC4ThaumometerScanKeys.entityRegistryKey(target.entity());
            }
            case ITEM -> {
                if(!(target.entity() instanceof ItemEntity itemEntity)) return false;
                first=PlayerThaumData.markScannedObject(player,target.stableKey());
                trigger=TC4ThaumometerScanKeys.itemRegistryKey(itemEntity.getItem());
            }
            case BLOCK -> {
                if(target.blockPos()==null) return false;
                first=PlayerThaumData.markScannedObject(player,target.stableKey());
                trigger=TC4ThaumometerScanKeys.blockRegistryKey(player.level.getBlockState(target.blockPos()));
            }
            case PHENOMENON -> first=PlayerThaumData.markScannedPhenomenon(player,target.stableKey());
            default -> { return false; }
        }
        if(!first) return false;
        AspectList rewarded=absorbScannedAspects(player,target.aspects());
        OriginalResearchProgression.applyScanTriggers(player,trigger,rewarded.entries().keySet(),legacyEntityTrigger);
        syncKnowledge(player);
        return true;
    }

    private boolean isValidEntityScanTarget(Player player, Entity target) {
        if(target==null || !target.isAlive() || (!(target instanceof ItemEntity)&&!target.isPickable())) return false;
        if(player.distanceToSqr(target)>TC4ThaumometerParity.ENTITY_SCAN_RANGE*TC4ThaumometerParity.ENTITY_SCAN_RANGE) return false;
        return player.hasLineOfSight(target);
    }

    private boolean isNewScanTarget(Player player, TC4ThaumometerTargeting.ScanTarget target) {
        boolean scanned=switch(target.kind()) {
            case NODE -> NodeScanData.hasScanned(player,target.stableKey()) || PlayerThaumData.hasScannedPhenomenon(player,target.stableKey());
            case ENTITY -> PlayerThaumData.hasScannedEntity(player,target.stableKey()) ||
                    (target.entity()!=null && PlayerThaumData.hasScannedEntity(player,TC4ThaumometerScanKeys.entityRegistryKey(target.entity())));
            case ITEM -> PlayerThaumData.hasScannedObject(player,target.stableKey()) ||
                    (target.entity() instanceof ItemEntity ie && PlayerThaumData.hasScannedObject(player,TC4ThaumometerScanKeys.itemRegistryKey(ie.getItem())));
            case BLOCK -> PlayerThaumData.hasScannedObject(player,target.stableKey()) ||
                    (target.blockPos()!=null && PlayerThaumData.hasScannedObject(player,TC4ThaumometerScanKeys.blockRegistryKey(player.level.getBlockState(target.blockPos()))));
            case PHENOMENON -> PlayerThaumData.hasScannedPhenomenon(player,target.stableKey());
            default -> true;
        };
        return TC4ThaumometerParity.mayStartHandheldScan(scanned);
    }

    private boolean canUnderstandScan(Player player, AspectList aspects) {
        if(aspects==null || aspects.isEmpty()) return false;
        PlayerAspectKnowledge.seedPrimals(player);
        for(Aspect aspect:aspects.entries().keySet()) {
            if(aspect==null || aspect.isPrimal() || ResearchTableFoundation.componentsKnown(player,aspect)) continue;
            Aspect missing=!PlayerAspectKnowledge.knows(player,aspect.firstComponent())?aspect.firstComponent():aspect.secondComponent();
            Component missingName=missing==null?Component.translatable("thaumcraft.message.research.not_understood"):Component.translatable("aspect.thaumcraft."+missing.id());
            player.displayClientMessage(Component.translatable("tc.discoveryerror",missingName).withStyle(ChatFormatting.RED),true);
            return false;
        }
        return true;
    }

    /** Returns the post-cap aspect clue list used by ResearchManager.createClue. */
    private AspectList absorbScannedAspects(Player player, AspectList aspects) {
        PlayerThaumData.recordScannedAspects(player,aspects);
        ResearchTableFoundation.seed(player);
        AspectList rewarded=new AspectList();
        for(Map.Entry<Aspect,Integer> entry:aspects.entries().entrySet()) {
            Aspect aspect=entry.getKey();
            if(aspect==null || (!aspect.isPrimal()&&!ResearchTableFoundation.componentsKnown(player,aspect))) continue;
            boolean discoveredNow=PlayerAspectKnowledge.discover(player,aspect);
            int currentPool=PlayerAspectKnowledge.pool(player).get(aspect);
            int reward=TC4ThaumometerParity.cappedAspectReward(currentPool,entry.getValue(),discoveredNow);
            if(reward>0) { PlayerAspectKnowledge.addPool(player,aspect,reward); rewarded.add(aspect,reward); }
        }
        return rewarded;
    }

    private void syncKnowledge(Player player) {
        if(player instanceof ServerPlayer serverPlayer) {
            ThaumcraftNetwork.syncAspectKnowledge(serverPlayer);
            ThaumcraftNetwork.syncResearch(serverPlayer);
            ThaumcraftNetwork.syncScanKnowledge(serverPlayer);
        }
    }

    /** One-time migration of rebuild-only per-item scan ledgers into player authority. */
    private void migrateLegacyItemLedger(Player player, ItemStack stack) {
        CompoundTag tag=stack.getTag();
        if(tag==null) return;
        PlayerThaumData.importScannedObjects(player,readStrings(tag,TAG_SCANNED));
        PlayerThaumData.importScannedEntities(player,readStrings(tag,TAG_SCANNED_ENTITIES));
        NodeScanData.importLegacyKeys(player,readStrings(tag,TAG_SCANNED_NODES));
        for(String key:List.of(TAG_SCANNED,TAG_SCANNED_ENTITIES,TAG_SCANNED_NODES,TAG_PENDING_BLOCK_SCAN,
                TAG_PENDING_ENTITY_SCAN,TAG_PENDING_SCAN_START,TAG_PENDING_SCAN_TICK)) tag.remove(key);
        if(tag.isEmpty()) stack.setTag(null);
        syncKnowledge(player);
    }

    private static List<String> readStrings(CompoundTag tag,String key) {
        List<String> result=new ArrayList<>();
        ListTag list=tag.getList(key,8);
        for(int i=0;i<list.size();i++) result.add(list.getString(i));
        return result;
    }

    /** Legacy read-only probes retained for old save guards; new scans never write these tags. */
    public static boolean hasScannedBlock(ItemStack stack,String key){return containsLegacyString(stack,TAG_SCANNED,key);}
    public static boolean hasScannedEntity(ItemStack stack,String key){return containsLegacyString(stack,TAG_SCANNED_ENTITIES,key);}
    public static boolean hasScannedNode(ItemStack stack,BlockPos pos){return pos!=null&&containsLegacyString(stack,TAG_SCANNED_NODES,NodeScanData.key(pos));}
    private static boolean containsLegacyString(ItemStack stack,String tagName,String key){
        if(stack==null||key==null||key.isBlank()||stack.getTag()==null)return false;
        ListTag list=stack.getTag().getList(tagName,8);for(int i=0;i<list.size();i++)if(key.equals(list.getString(i)))return true;return false;
    }
}
