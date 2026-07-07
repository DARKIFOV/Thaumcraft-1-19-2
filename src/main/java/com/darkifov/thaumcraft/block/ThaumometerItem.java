package com.darkifov.thaumcraft.block;

import com.darkifov.thaumcraft.Aspect;
import com.darkifov.thaumcraft.AspectDatabase;
import com.darkifov.thaumcraft.AspectList;
import com.darkifov.thaumcraft.ThaumcraftMod;
import com.darkifov.thaumcraft.blockentity.AuraNodeBlockEntity;
import com.darkifov.thaumcraft.data.NodeScanData;
import com.darkifov.thaumcraft.data.PlayerThaumData;
import com.darkifov.thaumcraft.network.ThaumcraftNetwork;
import com.darkifov.thaumcraft.research.OriginalResearchProgression;
import com.darkifov.thaumcraft.research.PlayerAspectKnowledge;
import com.darkifov.thaumcraft.research.ResearchTableFoundation;
import com.darkifov.thaumcraft.source.TC4EntityAspectRegistry;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.Map;
import java.util.Optional;

public class ThaumometerItem extends Item {
    private static final String TAG_SCANNED = "ScannedBlocks";
    private static final String TAG_SCANNED_ENTITIES = "ScannedEntities";

    public ThaumometerItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(net.minecraft.world.level.Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);

        if (!level.isClientSide) {
            Entity target = findScannableEntity(player);
            if (target != null) {
                ResourceLocation id = ForgeRegistries.ENTITY_TYPES.getKey(target.getType());
                String modernId = id == null ? "unknown" : id.toString();
                AspectList aspects = TC4EntityAspectRegistry.getAspectsForEntity(target);
                if (aspects.isEmpty()) {
                    return InteractionResultHolder.pass(stack);
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
                return InteractionResultHolder.consume(stack);
            }
        }

        return InteractionResultHolder.pass(stack);
    }

    private Entity findScannableEntity(Player player) {
        Vec3 eye = player.getEyePosition();
        Vec3 look = player.getLookAngle();
        double reach = 16.0D;
        Vec3 end = eye.add(look.scale(reach));
        AABB searchBox = player.getBoundingBox().expandTowards(look.scale(reach)).inflate(1.0D);
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

        if (!context.getLevel().isClientSide && context.getPlayer() != null) {
            Player player = context.getPlayer();
            BlockPos pos = context.getClickedPos();
            BlockEntity blockEntity = context.getLevel().getBlockEntity(pos);

            if (blockEntity instanceof AuraNodeBlockEntity node) {
                if (!node.initialized()) {
                    node.initializeFromPosition();
                }

                boolean firstNodeScan = NodeScanData.markScanned(player, pos);
                boolean firstPlayerScan = PlayerThaumData.markScannedObject(player, "thaumcraft:aura_node");
                int learnedAspects = absorbScannedAspects(player, node.aspects());

                player.displayClientMessage(
                        Component.literal(firstNodeScan ? "New aura node scan: " : "Aura node already scanned: ")
                                .append(Component.literal(node.nodeType()).withStyle(ChatFormatting.LIGHT_PURPLE))
                                .append(Component.literal(" | Aspects: "))
                                .append(node.aspects().toComponent())
                                .append(Component.literal(node.isStabilized() ? " | Stabilized" : ""))
                                .append(Component.literal(node.isEnergized() ? " | Energized" : ""))
                                .append(Component.literal(" | Stability: " + node.stability() + "%"))
                                .append(Component.literal(" | Player scans: " + PlayerThaumData.getScanKnowledgeCount(player)).withStyle(ChatFormatting.GOLD)),
                        false
                );

                if (firstNodeScan || firstPlayerScan) {
                    int discovered = OriginalResearchProgression.applyScanTriggers(player, "ConfigBlocks.blockAiry, 1, 0", node.aspects().entries().keySet(), null);
                    if (discovered > 0) {
                        player.displayClientMessage(Component.literal("Thaumometer revealed " + discovered + " TC4 research trigger(s).").withStyle(ChatFormatting.GOLD), false);
                    }
                    ItemStack reward = new ItemStack(ThaumcraftMod.RESEARCH_POINT.get());

                    if (!player.getInventory().add(reward)) {
                        Containers.dropItemStack(context.getLevel(), pos.getX() + 0.5D, pos.getY() + 1.0D, pos.getZ() + 0.5D, reward);
                    }

                    player.displayClientMessage(Component.literal("Aura insight gained! You received a Research Point.").withStyle(ChatFormatting.GOLD), false);
                }

                if (learnedAspects > 0) {
                    player.displayClientMessage(Component.literal("Aspect knowledge recorded: " + learnedAspects).withStyle(ChatFormatting.LIGHT_PURPLE), false);
                }
                syncKnowledge(player);
                return InteractionResult.CONSUME;
            }

            BlockState state = context.getLevel().getBlockState(pos);
            ResourceLocation id = ForgeRegistries.BLOCKS.getKey(state.getBlock());
            String key = id == null ? "unknown" : id.toString();

            boolean firstPlayerScan = PlayerThaumData.markScannedObject(player, key);
            addScannedBlock(stack, key); // Stage159: keep old per-item NBT as a compatibility mirror.
            int scanCount = PlayerThaumData.getScanKnowledgeCount(player);

            AspectList aspects = AspectDatabase.getAspectsForBlock(state);
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
                    Containers.dropItemStack(context.getLevel(), pos.getX() + 0.5D, pos.getY() + 1.0D, pos.getZ() + 0.5D, reward);
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
        }

        return InteractionResult.sidedSuccess(context.getLevel().isClientSide);
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
