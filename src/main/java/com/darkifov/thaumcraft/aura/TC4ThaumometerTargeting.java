package com.darkifov.thaumcraft.aura;

import com.darkifov.thaumcraft.AspectDatabase;
import com.darkifov.thaumcraft.AspectList;
import com.darkifov.thaumcraft.blockentity.AuraNodeBlockEntity;
import com.darkifov.thaumcraft.blockentity.ManaPodBlockEntity;
import com.darkifov.thaumcraft.source.TC4EntityAspectRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.registries.ForgeRegistries;

import javax.annotation.Nullable;
import java.util.Optional;

/**
 * Shared TC4-style Thaumometer ray target selection.
 *
 * <p>The original item and renderer both called the same scan routine every tick.
 * Earlier rebuild stages split that logic: the renderer only knew aura nodes,
 * block scans depended on useOn, and the server entity ray could pass through a
 * nearer wall. This adapter gives the item and the in-glass renderer one stable
 * 10-block target and rejects entities hidden behind the selected block.</p>
 */
public final class TC4ThaumometerTargeting {
    public enum Kind {
        NONE,
        BLOCK,
        ITEM,
        ENTITY,
        NODE,
        PHENOMENON
    }

    public record ScanTarget(
            Kind kind,
            @Nullable BlockPos blockPos,
            @Nullable Entity entity,
            Component displayName,
            AspectList aspects,
            String stableKey,
            @Nullable AuraNodeBlockEntity node
    ) {
        public static ScanTarget none() {
            return new ScanTarget(Kind.NONE, null, null, Component.empty(), new AspectList(), "", null);
        }

        public boolean isPresent() {
            return kind != Kind.NONE;
        }

        public boolean hasAspects() {
            return aspects != null && !aspects.isEmpty();
        }
    }

    private TC4ThaumometerTargeting() {
    }

    public static ScanTarget forEntity(Entity entity) {
        if (entity == null || !entity.isAlive()) {
            return ScanTarget.none();
        }
        if (entity instanceof ItemEntity itemEntity) {
            ItemStack stack = itemEntity.getItem();
            String key = TC4ThaumometerScanKeys.itemKey(stack);
            return new ScanTarget(Kind.ITEM, null, itemEntity, stack.getHoverName(),
                    AspectDatabase.getAspectsForItem(stack), key, null);
        }
        String key = TC4ThaumometerScanKeys.entityKey(entity);
        return new ScanTarget(Kind.ENTITY, null, entity, entity.getDisplayName(),
                TC4EntityAspectRegistry.getAspectsForEntity(entity), key, null);
    }

    public static ScanTarget forBlock(Player player, BlockPos pos) {
        if (player == null || player.level == null || pos == null || !player.level.isLoaded(pos)) {
            return ScanTarget.none();
        }
        BlockEntity blockEntity = player.level.getBlockEntity(pos);
        if (blockEntity instanceof AuraNodeBlockEntity node) {
            return new ScanTarget(Kind.NODE, pos.immutable(), null,
                    Component.translatable("block.thaumcraft.aura_node"), node.aspects(),
                    TC4ThaumometerScanKeys.nodeKey(node), node);
        }
        BlockState state = player.level.getBlockState(pos);
        if (blockEntity instanceof ManaPodBlockEntity pod && !pod.exposedAspects().isEmpty()) {
            String key = TC4ThaumometerScanKeys.blockKey(player, pos, state);
            return new ScanTarget(Kind.BLOCK, pos.immutable(), null, state.getBlock().getName(),
                    pod.exposedAspects(), key, null);
        }
        if (state.isAir()) {
            return ScanTarget.none();
        }
        String key = TC4ThaumometerScanKeys.blockKey(player, pos, state);
        return new ScanTarget(Kind.BLOCK, pos.immutable(), null, state.getBlock().getName(),
                AspectDatabase.getAspectsForBlock(state), key, null);
    }

    public static ScanTarget find(Player player, float partialTick) {
        if (player == null || player.level == null) {
            return ScanTarget.none();
        }

        double reach = TC4ThaumometerParity.ENTITY_SCAN_RANGE;
        Vec3 eye = player.getEyePosition(partialTick);
        Vec3 look = player.getViewVector(partialTick);
        Vec3 end = eye.add(look.scale(reach));

        HitResult blockHit = player.pick(reach, partialTick, true);
        double nearestBlockDistance = reach * reach;
        if (blockHit != null && blockHit.getType() == HitResult.Type.BLOCK) {
            nearestBlockDistance = eye.distanceToSqr(blockHit.getLocation());
        }

        Entity entity = findEntity(player, eye, end, look, nearestBlockDistance);
        if (entity != null) {
            return forEntity(entity);
        }

        if (blockHit instanceof BlockHitResult hit && blockHit.getType() == HitResult.Type.BLOCK) {
            ScanTarget blockTarget = forBlock(player, hit.getBlockPos());
            if (blockTarget.isPresent()) {
                return blockTarget;
            }
        }

        ItemStack scanner = player.isUsingItem() ? player.getUseItem() : player.getMainHandItem();
        ScanTarget phenomenon = TC4ThaumometerPhenomenaRegistry.find(player, scanner, partialTick);
        return phenomenon == null ? ScanTarget.none() : phenomenon;
    }

    @Nullable
    private static Entity findEntity(Player player, Vec3 eye, Vec3 end, Vec3 look, double maximumDistanceSq) {
        AABB searchBox = player.getBoundingBox()
                .expandTowards(look.scale(TC4ThaumometerParity.ENTITY_SCAN_RANGE))
                .inflate(TC4ThaumometerParity.ENTITY_TARGET_EXPAND);
        Entity best = null;
        double bestDistance = maximumDistanceSq;

        for (Entity candidate : player.level.getEntities(player, searchBox,
                entity -> entity.isAlive() && (entity instanceof ItemEntity || entity.isPickable()))) {
            AABB hitBox = candidate.getBoundingBox().inflate(
                    candidate.getPickRadius() + TC4ThaumometerParity.ENTITY_TARGET_EXPAND);
            double distance;
            if (hitBox.contains(eye)) {
                distance = 0.0D;
            } else {
                Optional<Vec3> hit = hitBox.clip(eye, end);
                if (hit.isEmpty()) {
                    continue;
                }
                distance = eye.distanceToSqr(hit.get());
            }
            if (distance < bestDistance) {
                best = candidate;
                bestDistance = distance;
            }
        }
        return best;
    }
}
