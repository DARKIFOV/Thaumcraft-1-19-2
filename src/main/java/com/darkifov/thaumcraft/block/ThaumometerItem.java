package com.darkifov.thaumcraft.block;

import com.darkifov.thaumcraft.Aspect;
import com.darkifov.thaumcraft.AspectDatabase;
import com.darkifov.thaumcraft.AspectList;
import com.darkifov.thaumcraft.ThaumcraftMod;
import com.darkifov.thaumcraft.blockentity.AuraNodeBlockEntity;
import com.darkifov.thaumcraft.client.render.ThaumometerItemRenderer;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;
import com.darkifov.thaumcraft.aura.TC4AuraNodeScanParity;
import com.darkifov.thaumcraft.aura.TC4ThaumometerTargeting;
import com.darkifov.thaumcraft.data.NodeScanData;
import com.darkifov.thaumcraft.data.PlayerThaumData;
import com.darkifov.thaumcraft.network.ThaumcraftNetwork;
import com.darkifov.thaumcraft.research.OriginalResearchProgression;
import com.darkifov.thaumcraft.research.PlayerAspectKnowledge;
import com.darkifov.thaumcraft.research.ResearchTableFoundation;
import com.darkifov.thaumcraft.porting.TC4Sounds;
import com.darkifov.thaumcraft.source.TC4EntityAspectRegistry;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
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
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.Map;
import java.util.UUID;
import java.util.function.Consumer;

public class ThaumometerItem extends Item {
    private static final String TAG_SCANNED = "ScannedBlocks";
    private static final String TAG_SCANNED_ENTITIES = "ScannedEntities";
    private static final String TAG_SCANNED_NODES = "ScannedAuraNodes";
    private static final String TAG_PENDING_BLOCK_SCAN = "TC4PendingBlockScan";
    private static final String TAG_PENDING_ENTITY_SCAN = "TC4PendingEntityScan";
    private static final String TAG_PENDING_SCAN_START = "TC4PendingScanStart";
    private static final String TAG_PENDING_SCAN_TICK = "TC4PendingScanTick";
    private static final int REQUIRED_STABLE_TICKS = 20;

    public ThaumometerItem(Properties properties) {
        super(properties);
    }




    @Override
    public void initializeClient(Consumer<IClientItemExtensions> consumer) {
        consumer.accept(new IClientItemExtensions() {
            @Override
            public BlockEntityWithoutLevelRenderer getCustomRenderer() {
                return ThaumometerItemRenderer.instance();
            }
        });
    }

    @Override
    public int getUseDuration(ItemStack stack) {
        return TC4AuraNodeScanParity.THAUMOMETER_USE_DURATION_TICKS;
    }

    @Override
    public UseAnim getUseAnimation(ItemStack stack) {
        return UseAnim.NONE;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        TC4ThaumometerTargeting.ScanTarget target = TC4ThaumometerTargeting.find(player, 1.0F);
        return beginTargetScan(level, player, hand, stack, target);
    }

    /**
     * Forge 1.19.2 interactive blocks consume right-click before Item#useOn.
     * The common event bridge calls this method directly so chests, doors and
     * machines are scanned instead of opened. The target is stored server-side,
     * while both logical sides enter the normal 25 tick use animation.
     */
    public InteractionResultHolder<ItemStack> beginBlockScan(Level level, Player player,
                                                               InteractionHand hand, BlockPos pos) {
        ItemStack stack = player.getItemInHand(hand);
        if (pos == null || !TC4AuraNodeScanParity.isWithinScanRange(player, pos)) {
            return InteractionResultHolder.pass(stack);
        }
        TC4ThaumometerTargeting.ScanTarget target = TC4ThaumometerTargeting.forBlock(player, pos);
        return beginTargetScan(level, player, hand, stack, target);
    }

    /** Same direct bridge for villagers, mobs and dropped items. */
    public InteractionResultHolder<ItemStack> beginEntityScan(Level level, Player player,
                                                                InteractionHand hand, Entity entity) {
        ItemStack stack = player.getItemInHand(hand);
        if (!isValidEntityScanTarget(player, entity)) {
            return InteractionResultHolder.pass(stack);
        }
        TC4ThaumometerTargeting.ScanTarget target = TC4ThaumometerTargeting.forEntity(entity);
        return beginTargetScan(level, player, hand, stack, target);
    }

    private InteractionResultHolder<ItemStack> beginTargetScan(Level level, Player player,
                                                                 InteractionHand hand, ItemStack stack,
                                                                 TC4ThaumometerTargeting.ScanTarget target) {
        if (target == null || !target.isPresent()) {
            if (!level.isClientSide) {
                clearPendingScans(stack);
            }
            return InteractionResultHolder.pass(stack);
        }

        // The client only chooses and animates the target. Aspect tables are
        // server-authoritative and may not yet be populated on the render side;
        // rejecting an empty client AspectList here was enough to prevent the
        // right-click packet from ever starting a scan. The server still refuses
        // truly aspectless targets before storing the pending scan.
        if (!level.isClientSide && !target.hasAspects()) {
            clearPendingScans(stack);
            return InteractionResultHolder.pass(stack);
        }
        if (!level.isClientSide) {
            clearPendingScans(stack);
            beginPendingScan(stack, target);
            stack.getOrCreateTag().putLong(TAG_PENDING_SCAN_START, level.getGameTime());
            stack.getOrCreateTag().putLong(TAG_PENDING_SCAN_TICK, Long.MIN_VALUE);
        }
        player.startUsingItem(hand);
        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide);
    }

    @Override
    public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity livingEntity) {
        if (!level.isClientSide && livingEntity instanceof Player player) {
            BlockPos pendingBlock = consumePendingBlockScan(stack);
            UUID pendingEntity = consumePendingEntityScan(stack);
            if (pendingBlock != null) {
                performBlockScan(level, player, stack, pendingBlock);
            } else if (pendingEntity != null) {
                performEntityScan(player, stack, pendingEntity);
            }
            clearPendingScans(stack);
        }
        return stack;
    }

    @Override
    public void releaseUsing(ItemStack stack, Level level, LivingEntity livingEntity, int timeLeft) {
        if (!level.isClientSide && timeLeft > 0) {
            // v8.62: releasing before the full TC4 hold duration cancels the pending target.
            // The next use must select a fresh block/entity instead of completing a stale scan.
            clearPendingScans(stack);
        }
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Player player = context.getPlayer();
        if (player == null) {
            return InteractionResult.PASS;
        }
        return beginBlockScan(context.getLevel(), player, context.getHand(), context.getClickedPos()).getResult();
    }

    @Override
    public void onUseTick(Level level, LivingEntity livingEntity, ItemStack stack, int remainingUseDuration) {
        if (!(livingEntity instanceof ServerPlayer player) || level.isClientSide) {
            return;
        }

        serverTickPendingScan(player, player.getUsedItemHand());
    }

    /**
     * Authoritative fallback for Forge interactions cancelled before vanilla's
     * normal item-use packet. CommonEvents calls this once per player tick, so
     * chests, mobs and modded machines still complete a stable TC4 scan.
     */
    public void serverTickPendingScan(ServerPlayer player, InteractionHand hand) {
        if (player == null || hand == null) {
            return;
        }
        ItemStack stack = player.getItemInHand(hand);
        if (stack.getItem() != this || !hasPendingScan(stack)) {
            return;
        }
        CompoundTag tag = stack.getOrCreateTag();
        long now = player.level.getGameTime();
        if (tag.getLong(TAG_PENDING_SCAN_TICK) == now) {
            return;
        }
        tag.putLong(TAG_PENDING_SCAN_TICK, now);

        // TC4 re-ran doScan every use tick and completed only while that result
        // remained equal to startScan. Merely keeping the original object alive and
        // in range let players look away and still finish a scan, which was not the
        // original mechanic. The server therefore validates the current ray target
        // against the stored block/entity every tick.
        if (!pendingTargetStillValid(player, stack)) {
            clearPendingScans(stack);
            player.stopUsingItem();
            return;
        }

        long elapsed = Math.max(0L, now - tag.getLong(TAG_PENDING_SCAN_START));
        if (elapsed % 2L == 0L) {
            player.level.playSound(null, player.blockPosition(), TC4Sounds.event("cameraticks"),
                    SoundSource.PLAYERS, 0.20F, 0.45F + player.level.random.nextFloat() * 0.10F);
        }

        if (elapsed >= REQUIRED_STABLE_TICKS) {
            BlockPos pendingBlock = consumePendingBlockScan(stack);
            UUID pendingEntity = consumePendingEntityScan(stack);
            if (pendingBlock != null) {
                performBlockScan(player.level, player, stack, pendingBlock);
            } else if (pendingEntity != null) {
                performEntityScan(player, stack, pendingEntity);
            }
            clearPendingScans(stack);
            player.stopUsingItem();
        }
    }

    private boolean performEntityScan(Player player, ItemStack stack, UUID targetId) {
        if (!(player.level instanceof ServerLevel serverLevel) || targetId == null) {
            return false;
        }
        Entity target = serverLevel.getEntity(targetId);
        if (!isValidEntityScanTarget(player, target)) {
            return false;
        }
        final boolean droppedItem = target instanceof ItemEntity;
        final ResourceLocation id;
        final String modernId;
        final AspectList aspects;
        final Component targetName;

        if (target instanceof ItemEntity itemEntity) {
            ItemStack scannedStack = itemEntity.getItem();
            id = ForgeRegistries.ITEMS.getKey(scannedStack.getItem());
            modernId = id == null ? "unknown" : id.toString();
            aspects = AspectDatabase.getAspectsForItem(scannedStack);
            targetName = scannedStack.getHoverName();
        } else {
            id = ForgeRegistries.ENTITY_TYPES.getKey(target.getType());
            modernId = id == null ? "unknown" : id.toString();
            aspects = TC4EntityAspectRegistry.getAspectsForEntity(target);
            targetName = target.getDisplayName();
        }
        if (aspects.isEmpty() || !canUnderstandScan(player, aspects)) {
            return false;
        }

        boolean firstPlayerScan;
        if (droppedItem) {
            firstPlayerScan = PlayerThaumData.markScannedObject(player, modernId);
            addScannedBlock(stack, modernId);
        } else {
            firstPlayerScan = PlayerThaumData.markScannedEntity(player, modernId);
            addScannedEntity(stack, modernId); // Legacy per-thaumometer compatibility mirror.
        }
        int learnedAspects = firstPlayerScan ? absorbScannedAspects(player, aspects) : 0;

        Component entityScan = Component.translatable(firstPlayerScan
                        ? (droppedItem ? "thaumcraft.scan.block.new" : "thaumcraft.scan.entity.new")
                        : (droppedItem ? "thaumcraft.scan.block.known" : "thaumcraft.scan.entity.known"), targetName)
                .withStyle(ChatFormatting.AQUA)
                .append(Component.literal(" | "))
                .append(Component.translatable("thaumcraft.scan.aspects"))
                .append(aspects.toComponent())
                .append(Component.literal(" | "))
                .append(Component.translatable("thaumcraft.scan.total", PlayerThaumData.getScanKnowledgeCount(player))
                        .withStyle(ChatFormatting.GOLD));
        player.displayClientMessage(entityScan, !firstPlayerScan);

        if (firstPlayerScan) {
            String legacyTrigger = droppedItem ? "" : TC4EntityAspectRegistry.legacyScanTriggerId(target);
            int discovered = OriginalResearchProgression.applyScanTriggers(player, modernId, aspects.entries().keySet(), legacyTrigger.isBlank() ? null : legacyTrigger);
            if (discovered > 0) {
                player.displayClientMessage(Component.translatable("thaumcraft.scan.research_revealed", discovered)
                        .withStyle(ChatFormatting.GOLD), false);
            }
        }

        if (learnedAspects > 0) {
            player.displayClientMessage(Component.translatable("thaumcraft.scan.aspect_knowledge", learnedAspects)
                    .withStyle(ChatFormatting.LIGHT_PURPLE), false);
        }

        syncKnowledge(player);
        return true;
    }

    private boolean performBlockScan(Level level, Player player, ItemStack stack, BlockPos pos) {
        if (!TC4AuraNodeScanParity.isWithinScanRange(player, pos)) {
            return false;
        }

        BlockEntity blockEntity = level.getBlockEntity(pos);

        if (blockEntity instanceof AuraNodeBlockEntity node) {
            if (!node.initialized()) {
                node.initializeFromPosition();
            }
            AspectList scanAspects = TC4AuraNodeScanParity.scanRewardAspects(node);
            if (!canUnderstandScan(player, scanAspects)) {
                return false;
            }

            boolean firstNodeScan = NodeScanData.markScanned(player, pos);
            boolean firstPlayerScan = PlayerThaumData.markScannedObject(player, TC4AuraNodeScanParity.LEGACY_OBJECT_ID);
            node.markScanned();
            addScannedNode(stack, pos);
            int learnedAspects = firstNodeScan ? absorbScannedAspects(player, scanAspects) : 0;

            if (firstNodeScan || firstPlayerScan) {
                int discovered = OriginalResearchProgression.applyScanTriggers(player, TC4AuraNodeScanParity.ORIGINAL_AURA_NODE_SCAN_KEY, scanAspects.entries().keySet(), null);
                if (discovered > 0) {
                    player.displayClientMessage(Component.translatable("thaumcraft.scan.research_revealed", discovered)
                        .withStyle(ChatFormatting.GOLD), false);
                }
            }

            if (learnedAspects > 0) {
                player.displayClientMessage(Component.translatable("thaumcraft.scan.aspect_knowledge", learnedAspects)
                    .withStyle(ChatFormatting.LIGHT_PURPLE), false);
            }
            syncKnowledge(player);
            return true;
        }

        BlockState state = level.getBlockState(pos);
        AspectList aspects = AspectDatabase.getAspectsForBlock(state);
        if (aspects.isEmpty()) {
            // v7.82: TC4 does not treat completely aspectless targets as
            // successful research scans.  Avoid polluting player scan data
            // and legacy thaumometer NBT with air/unknown/decor placeholders.
            return false;
        }
        if (!canUnderstandScan(player, aspects)) {
            return false;
        }

        ResourceLocation id = ForgeRegistries.BLOCKS.getKey(state.getBlock());
        String key = id == null ? "unknown" : id.toString();

        boolean firstPlayerScan = PlayerThaumData.markScannedObject(player, key);
        addScannedBlock(stack, key); // Stage159: keep old per-item NBT as a compatibility mirror.
        int scanCount = PlayerThaumData.getScanKnowledgeCount(player);

        int learnedAspects = firstPlayerScan ? absorbScannedAspects(player, aspects) : 0;

        Component blockScan = Component.translatable(firstPlayerScan
                        ? "thaumcraft.scan.block.new" : "thaumcraft.scan.block.known", state.getBlock().getName())
                .withStyle(ChatFormatting.AQUA)
                .append(Component.literal(" | "))
                .append(Component.translatable("thaumcraft.scan.aspects"))
                .append(aspects.toComponent())
                .append(Component.literal(" | "))
                .append(Component.translatable("thaumcraft.scan.total", scanCount).withStyle(ChatFormatting.GOLD));
        player.displayClientMessage(blockScan, !firstPlayerScan);

        if (firstPlayerScan) {
            int discovered = OriginalResearchProgression.applyScanTriggers(player, key, aspects.entries().keySet(), null);
            if (discovered > 0) {
                player.displayClientMessage(Component.translatable("thaumcraft.scan.research_revealed", discovered)
                        .withStyle(ChatFormatting.GOLD), false);
            }
        }

        if (learnedAspects > 0) {
            player.displayClientMessage(Component.translatable("thaumcraft.scan.aspect_knowledge", learnedAspects)
                    .withStyle(ChatFormatting.LIGHT_PURPLE), false);
        }
        syncKnowledge(player);
        return true;
    }

    private boolean isValidEntityScanTarget(Player player, Entity target) {
        // Dropped ItemEntity instances are intentionally not vanilla-pickable,
        // but TC4 ScanManager supports scanning them. The concrete entity id is
        // supplied by the client interaction packet, so validate existence,
        // range and line of sight without repeating a latency-sensitive exact ray.
        if (target == null || !target.isAlive()
                || (!(target instanceof ItemEntity) && !target.isPickable())) {
            return false;
        }
        if (player.distanceToSqr(target) > TC4AuraNodeScanParity.THAUMOMETER_SCAN_RANGE * TC4AuraNodeScanParity.THAUMOMETER_SCAN_RANGE) {
            return false;
        }
        return player.hasLineOfSight(target);
    }

    private void beginPendingScan(ItemStack stack, TC4ThaumometerTargeting.ScanTarget target) {
        if (target == null || !target.isPresent()) {
            return;
        }
        if ((target.kind() == TC4ThaumometerTargeting.Kind.ENTITY
                || target.kind() == TC4ThaumometerTargeting.Kind.ITEM) && target.entity() != null) {
            setPendingEntityScan(stack, target.entity());
        } else if ((target.kind() == TC4ThaumometerTargeting.Kind.BLOCK
                || target.kind() == TC4ThaumometerTargeting.Kind.NODE) && target.blockPos() != null) {
            setPendingBlockScan(stack, target.blockPos());
        }
    }

    private boolean pendingTargetStillValid(ServerPlayer player, ItemStack stack) {
        CompoundTag tag = stack.getTag();
        if (tag == null || player == null) {
            return false;
        }

        TC4ThaumometerTargeting.ScanTarget current = TC4ThaumometerTargeting.find(player, 1.0F);
        if (current == null || !current.isPresent() || !current.hasAspects()) {
            return false;
        }

        if (tag.contains(TAG_PENDING_BLOCK_SCAN)) {
            CompoundTag pending = tag.getCompound(TAG_PENDING_BLOCK_SCAN);
            BlockPos expected = new BlockPos(pending.getInt("x"), pending.getInt("y"), pending.getInt("z"));
            return current.blockPos() != null
                    && expected.equals(current.blockPos())
                    && (current.kind() == TC4ThaumometerTargeting.Kind.BLOCK
                    || current.kind() == TC4ThaumometerTargeting.Kind.NODE);
        }

        if (tag.contains(TAG_PENDING_ENTITY_SCAN)) {
            CompoundTag pending = tag.getCompound(TAG_PENDING_ENTITY_SCAN);
            return pending.hasUUID("uuid")
                    && current.entity() != null
                    && pending.getUUID("uuid").equals(current.entity().getUUID())
                    && (current.kind() == TC4ThaumometerTargeting.Kind.ENTITY
                    || current.kind() == TC4ThaumometerTargeting.Kind.ITEM);
        }
        return false;
    }

    private void setPendingBlockScan(ItemStack stack, BlockPos pos) {
        CompoundTag pending = new CompoundTag();
        pending.putInt("x", pos.getX());
        pending.putInt("y", pos.getY());
        pending.putInt("z", pos.getZ());
        stack.getOrCreateTag().put(TAG_PENDING_BLOCK_SCAN, pending);
    }

    private void setPendingEntityScan(ItemStack stack, Entity target) {
        CompoundTag pending = new CompoundTag();
        pending.putUUID("uuid", target.getUUID());
        pending.putInt("entityId", target.getId()); // legacy/debug mirror; UUID is authoritative.
        stack.getOrCreateTag().put(TAG_PENDING_ENTITY_SCAN, pending);
    }

    private BlockPos consumePendingBlockScan(ItemStack stack) {
        CompoundTag tag = stack.getTag();
        if (tag == null || !tag.contains(TAG_PENDING_BLOCK_SCAN)) {
            return null;
        }
        CompoundTag pending = tag.getCompound(TAG_PENDING_BLOCK_SCAN);
        BlockPos pos = new BlockPos(pending.getInt("x"), pending.getInt("y"), pending.getInt("z"));
        tag.remove(TAG_PENDING_BLOCK_SCAN);
        return pos;
    }

    private UUID consumePendingEntityScan(ItemStack stack) {
        CompoundTag tag = stack.getTag();
        if (tag == null || !tag.contains(TAG_PENDING_ENTITY_SCAN)) {
            return null;
        }
        CompoundTag pending = tag.getCompound(TAG_PENDING_ENTITY_SCAN);
        UUID id = pending.hasUUID("uuid") ? pending.getUUID("uuid") : null;
        tag.remove(TAG_PENDING_ENTITY_SCAN);
        return id;
    }

    private void clearPendingScans(ItemStack stack) {
        CompoundTag tag = stack.getTag();
        if (tag != null) {
            tag.remove(TAG_PENDING_BLOCK_SCAN);
            tag.remove(TAG_PENDING_ENTITY_SCAN);
            tag.remove(TAG_PENDING_SCAN_START);
            tag.remove(TAG_PENDING_SCAN_TICK);
        }
    }

    private boolean hasPendingScan(ItemStack stack) {
        CompoundTag tag = stack.getTag();
        return tag != null && tag.contains(TAG_PENDING_SCAN_START)
                && (tag.contains(TAG_PENDING_BLOCK_SCAN) || tag.contains(TAG_PENDING_ENTITY_SCAN));
    }

    private boolean canUnderstandScan(Player player, AspectList aspects) {
        PlayerAspectKnowledge.seedPrimals(player);
        for (Aspect aspect : aspects.entries().keySet()) {
            if (aspect == null || aspect.isPrimal() || ResearchTableFoundation.componentsKnown(player, aspect)) {
                continue;
            }
            Aspect missing = !PlayerAspectKnowledge.knows(player, aspect.firstComponent())
                    ? aspect.firstComponent() : aspect.secondComponent();
            Component missingName = missing == null
                    ? Component.translatable("thaumcraft.message.research.not_understood")
                    : Component.translatable("aspect.thaumcraft." + missing.id());
            player.displayClientMessage(Component.translatable("tc.discoveryerror", missingName)
                    .withStyle(ChatFormatting.RED), true);
            return false;
        }
        return true;
    }

    private int absorbScannedAspects(Player player, AspectList aspects) {
        PlayerThaumData.recordScannedAspects(player, aspects);
        ResearchTableFoundation.seed(player);
        int learned = 0;

        for (Map.Entry<Aspect, Integer> entry : aspects.entries().entrySet()) {
            Aspect aspect = entry.getKey();
            if (aspect == null || (!aspect.isPrimal() && !ResearchTableFoundation.componentsKnown(player, aspect))) {
                // TC4 ScanManager.validScan refuses to award an aspect until both
                // of its parent aspects are understood.  Recording the raw scan is
                // harmless, but the research pool must not bypass that gate.
                continue;
            }

            boolean discoveredNow = PlayerAspectKnowledge.discover(player, aspect);
            // The original ItemThaumometer completes scans with the "@" prefix.
            // ScanManager therefore keeps the object's aspect magnitude and
            // checkAndSyncAspectKnowledge adds two bonus points on first discovery.
            // The one-point rule belongs to the separate "#" scan path and must
            // not be applied to the handheld Thaumometer.
            int reward = Math.max(0, entry.getValue()) + (discoveredNow ? 2 : 0);
            if (reward > 0) {
                PlayerAspectKnowledge.addPool(player, aspect, reward);
            }
            if (discoveredNow) {
                learned++;
            }
        }

        return learned;
    }

    private void syncKnowledge(Player player) {
        if (player instanceof ServerPlayer serverPlayer) {
            ThaumcraftNetwork.syncAspectKnowledge(serverPlayer);
            ThaumcraftNetwork.syncResearch(serverPlayer);
            ThaumcraftNetwork.syncScanKnowledge(serverPlayer);
        }
    }

    public static boolean hasScannedBlock(ItemStack stack, String key) {
        return containsLegacyString(stack, TAG_SCANNED, key);
    }

    public static boolean hasScannedEntity(ItemStack stack, String key) {
        return containsLegacyString(stack, TAG_SCANNED_ENTITIES, key);
    }

    public static boolean hasScannedNode(ItemStack stack, BlockPos pos) {
        return pos != null && containsLegacyString(stack, TAG_SCANNED_NODES, NodeScanData.key(pos));
    }

    private static boolean containsLegacyString(ItemStack stack, String tagName, String key) {
        if (stack == null || key == null || key.isBlank()) {
            return false;
        }
        CompoundTag tag = stack.getTag();
        if (tag == null) {
            return false;
        }
        ListTag list = tag.getList(tagName, 8);
        for (int i = 0; i < list.size(); i++) {
            if (key.equals(list.getString(i))) {
                return true;
            }
        }
        return false;
    }

    private boolean addScannedNode(ItemStack stack, BlockPos pos) {
        return pos != null && addLegacyString(stack, TAG_SCANNED_NODES, NodeScanData.key(pos));
    }

    private boolean addScannedBlock(ItemStack stack, String key) {
        return addLegacyString(stack, TAG_SCANNED, key);
    }

    private boolean addScannedEntity(ItemStack stack, String key) {
        return addLegacyString(stack, TAG_SCANNED_ENTITIES, key);
    }

    private boolean addLegacyString(ItemStack stack, String tagName, String key) {
        if (key == null || key.isBlank()) {
            return false;
        }

        CompoundTag tag = stack.getOrCreateTag();
        ListTag list = tag.getList(tagName, 8);

        for (int i = 0; i < list.size(); i++) {
            if (list.getString(i).equals(key)) {
                tag.put(tagName, list);
                return false;
            }
        }

        list.add(StringTag.valueOf(key));
        tag.put(tagName, list);
        return true;
    }
}
