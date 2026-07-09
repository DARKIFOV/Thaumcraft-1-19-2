package com.darkifov.thaumcraft.block;

import com.darkifov.thaumcraft.Aspect;
import com.darkifov.thaumcraft.AspectDatabase;
import com.darkifov.thaumcraft.AspectList;
import com.darkifov.thaumcraft.ThaumcraftMod;
import com.darkifov.thaumcraft.blockentity.AuraNodeBlockEntity;
import com.darkifov.thaumcraft.aura.TC4AuraNodeScanParity;
import com.darkifov.thaumcraft.client.render.ThaumometerItemRenderer;
import com.darkifov.thaumcraft.data.NodeScanData;
import com.darkifov.thaumcraft.data.PlayerThaumData;
import com.darkifov.thaumcraft.network.ThaumcraftNetwork;
import com.darkifov.thaumcraft.research.OriginalResearchProgression;
import com.darkifov.thaumcraft.research.PlayerAspectKnowledge;
import com.darkifov.thaumcraft.research.ResearchTableFoundation;
import com.darkifov.thaumcraft.source.TC4EntityAspectRegistry;
import net.minecraft.ChatFormatting;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Consumer;

public class ThaumometerItem extends Item {
    private static final String TAG_SCANNED = "ScannedBlocks";
    private static final String TAG_SCANNED_ENTITIES = "ScannedEntities";
    private static final String TAG_PENDING_BLOCK_SCAN = "TC4PendingBlockScan";
    private static final String TAG_PENDING_ENTITY_SCAN = "TC4PendingEntityScan";

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
        clearPendingScans(stack);
        if (!level.isClientSide) {
            Entity target = findScannableEntity(player);
            if (target != null) {
                setPendingEntityScan(stack, target);
            }
        }
        player.startUsingItem(hand);
        return InteractionResultHolder.consume(stack);
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

    private Entity findScannableEntity(Player player) {
        Vec3 eye = player.getEyePosition();
        Vec3 look = player.getLookAngle();
        double reach = TC4AuraNodeScanParity.THAUMOMETER_SCAN_RANGE;
        Vec3 end = eye.add(look.scale(reach));
        AABB searchBox = player.getBoundingBox().expandTowards(look.scale(reach)).inflate(TC4AuraNodeScanParity.THAUMOMETER_ENTITY_EXPAND);
        Entity best = null;
        double bestDistance = reach * reach;

        for (Entity entity : player.level.getEntities(player, searchBox, candidate -> candidate.isAlive() && candidate.isPickable())) {
            AABB hitBox = entity.getBoundingBox().inflate(entity.getPickRadius() + 0.25D);
            Optional<Vec3> hit = hitBox.clip(eye, end);
            if (hit.isPresent()) {
                double distance = eye.distanceToSqr(hit.get());
                if (distance < bestDistance) {
                    best = entity;
                    bestDistance = distance;
                }
            }
        }

        return best;
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        ItemStack stack = context.getItemInHand();
        Player player = context.getPlayer();
        if (player == null) {
            return InteractionResult.PASS;
        }

        BlockPos pos = context.getClickedPos();
        if (!TC4AuraNodeScanParity.isWithinScanRange(player, pos)) {
            return InteractionResult.PASS;
        }

        if (!context.getLevel().isClientSide) {
            clearPendingScans(stack);
            setPendingBlockScan(stack, pos);
        }
        player.startUsingItem(context.getHand());
        return InteractionResult.sidedSuccess(context.getLevel().isClientSide);
    }

    private boolean performEntityScan(Player player, ItemStack stack, UUID targetId) {
        if (!(player.level instanceof ServerLevel serverLevel) || targetId == null) {
            return false;
        }
        Entity target = serverLevel.getEntity(targetId);
        if (!isStableEntityScanTarget(player, target)) {
            return false;
        }
        ResourceLocation id = ForgeRegistries.ENTITY_TYPES.getKey(target.getType());
        String modernId = id == null ? "unknown" : id.toString();
        AspectList aspects = TC4EntityAspectRegistry.getAspectsForEntity(target);
        if (aspects.isEmpty()) {
            return false;
        }

        boolean firstPlayerScan = PlayerThaumData.markScannedEntity(player, modernId);
        addScannedEntity(stack, modernId); // Stage159: legacy per-thaumometer compatibility only.
        int learnedAspects = absorbScannedAspects(player, aspects);

        player.displayClientMessage(
                Component.literal(firstPlayerScan ? "New entity scan: " : "Already scanned entity: ")
                        .append(Component.literal(target.getName().getString()).withStyle(ChatFormatting.AQUA))
                        .append(Component.literal(" | Aspects: "))
                        .append(aspects.toComponent())
                        .append(Component.literal(" | Player scans: " + PlayerThaumData.getScanKnowledgeCount(player)).withStyle(ChatFormatting.GOLD)),
                false
        );

        if (firstPlayerScan) {
            String legacyTrigger = TC4EntityAspectRegistry.legacyScanTriggerId(target);
            int discovered = OriginalResearchProgression.applyScanTriggers(player, modernId, aspects.entries().keySet(), legacyTrigger.isBlank() ? null : legacyTrigger);
            if (discovered > 0) {
                player.displayClientMessage(Component.literal("Thaumometer revealed " + discovered + " TC4 research trigger(s).").withStyle(ChatFormatting.GOLD), false);
            }
        }

        if (learnedAspects > 0) {
            player.displayClientMessage(Component.literal("Aspect knowledge recorded: " + learnedAspects).withStyle(ChatFormatting.LIGHT_PURPLE), false);
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

            boolean firstNodeScan = NodeScanData.markScanned(player, pos);
            boolean firstPlayerScan = PlayerThaumData.markScannedObject(player, TC4AuraNodeScanParity.LEGACY_OBJECT_ID);
            int learnedAspects = absorbScannedAspects(player, node.aspects());

            player.displayClientMessage(TC4AuraNodeScanParity.header(node, firstNodeScan), false);
            player.displayClientMessage(TC4AuraNodeScanParity.visLine(node), false);
            player.displayClientMessage(TC4AuraNodeScanParity.aspectLine(node), false);

            if (firstNodeScan || firstPlayerScan) {
                int discovered = OriginalResearchProgression.applyScanTriggers(player, TC4AuraNodeScanParity.ORIGINAL_AURA_NODE_SCAN_KEY, node.aspects().entries().keySet(), null);
                if (discovered > 0) {
                    player.displayClientMessage(Component.literal("Thaumometer revealed " + discovered + " TC4 research trigger(s).").withStyle(ChatFormatting.GOLD), false);
                }
                ItemStack reward = new ItemStack(ThaumcraftMod.RESEARCH_POINT.get());

                if (!player.getInventory().add(reward)) {
                    Containers.dropItemStack(level, pos.getX() + 0.5D, pos.getY() + 1.0D, pos.getZ() + 0.5D, reward);
                }

                player.displayClientMessage(Component.literal("Aura insight gained! You received a Research Point.").withStyle(ChatFormatting.GOLD), false);
            }

            if (learnedAspects > 0) {
                player.displayClientMessage(Component.literal("Aspect knowledge recorded: " + learnedAspects).withStyle(ChatFormatting.LIGHT_PURPLE), false);
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

        ResourceLocation id = ForgeRegistries.BLOCKS.getKey(state.getBlock());
        String key = id == null ? "unknown" : id.toString();

        boolean firstPlayerScan = PlayerThaumData.markScannedObject(player, key);
        addScannedBlock(stack, key); // Stage159: keep old per-item NBT as a compatibility mirror.
        int scanCount = PlayerThaumData.getScanKnowledgeCount(player);

        int learnedAspects = absorbScannedAspects(player, aspects);

        player.displayClientMessage(
                Component.literal(firstPlayerScan ? "New scan: " : "Already scanned: ")
                        .append(Component.literal(state.getBlock().getName().getString()).withStyle(ChatFormatting.AQUA))
                        .append(Component.literal(" | Aspects: "))
                        .append(aspects.toComponent())
                        .append(Component.literal(" | Player scans: " + scanCount).withStyle(ChatFormatting.GOLD)),
                false
        );

        if (firstPlayerScan) {
            int discovered = OriginalResearchProgression.applyScanTriggers(player, key, aspects.entries().keySet(), null);
            if (discovered > 0) {
                player.displayClientMessage(Component.literal("Thaumometer revealed " + discovered + " TC4 research trigger(s).").withStyle(ChatFormatting.GOLD), false);
            }
        }

        if (firstPlayerScan && scanCount > 0 && scanCount % 5 == 0) {
            ItemStack reward = new ItemStack(ThaumcraftMod.RESEARCH_POINT.get());

            if (!player.getInventory().add(reward)) {
                Containers.dropItemStack(level, pos.getX() + 0.5D, pos.getY() + 1.0D, pos.getZ() + 0.5D, reward);
            }

            player.displayClientMessage(
                    Component.literal("Research insight gained! You received a Research Point.").withStyle(ChatFormatting.LIGHT_PURPLE),
                    false
            );
        }

        if (learnedAspects > 0) {
            player.displayClientMessage(Component.literal("Aspect knowledge recorded: " + learnedAspects).withStyle(ChatFormatting.LIGHT_PURPLE), false);
        }
        syncKnowledge(player);
        return true;
    }

    private boolean isStableEntityScanTarget(Player player, Entity target) {
        if (target == null || !target.isAlive() || !target.isPickable()) {
            return false;
        }
        if (player.distanceToSqr(target) > TC4AuraNodeScanParity.THAUMOMETER_SCAN_RANGE * TC4AuraNodeScanParity.THAUMOMETER_SCAN_RANGE) {
            return false;
        }
        // v8.62: validate against the entity currently under the ray, but never retarget.
        // Moving the crosshair to a different entity during the 25 tick hold cancels this scan.
        return findScannableEntity(player) == target;
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
        }
    }

    private int absorbScannedAspects(Player player, AspectList aspects) {
        PlayerThaumData.recordScannedAspects(player, aspects);
        ResearchTableFoundation.seed(player);
        int learned = 0;

        for (Map.Entry<Aspect, Integer> entry : aspects.entries().entrySet()) {
            Aspect aspect = entry.getKey();
            int amount = Math.max(1, entry.getValue());
            PlayerAspectKnowledge.addPool(player, aspect, amount);

            if (aspect.isPrimal() || ResearchTableFoundation.componentsKnown(player, aspect)) {
                if (PlayerAspectKnowledge.discover(player, aspect)) {
                    learned++;
                }
            }
        }

        return learned;
    }

    private void syncKnowledge(Player player) {
        if (player instanceof ServerPlayer serverPlayer) {
            ThaumcraftNetwork.syncAspectKnowledge(serverPlayer);
            ThaumcraftNetwork.syncResearch(serverPlayer);
        }
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
