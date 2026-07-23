package com.darkifov.thaumcraft.item;

import com.darkifov.thaumcraft.entity.FollowingItemEntity;
import com.darkifov.thaumcraft.item.gear.TC4ElementalToolTier;
import com.darkifov.thaumcraft.porting.TC4Sounds;
import com.mojang.math.Vector3f;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.AxeItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * Forge 1.19.2 port of TC4 {@code ItemElementalAxe} (Axe of the Stream).
 *
 * <p>Breaking a log while not sneaking harvests one connected log from the
 * furthest reachable point, then turns the newborn drops into Following Item
 * entities aimed at the player. Holding use pulls nearby loose items toward
 * the wielder with the original acceleration and velocity cap.</p>
 */
public final class ElementalAxeItem extends AxeItem {
    private static final ThreadLocal<Boolean> INTERNAL_HARVEST = ThreadLocal.withInitial(() -> false);
    private static final DustParticleOptions STREAM_PARTICLE =
            new DustParticleOptions(new Vector3f(0.33F, 0.33F, 1.0F), 0.65F);

    public ElementalAxeItem(Properties properties) {
        // 1.7.10 ItemAxe contributes 3 base attack damage on top of the
        // elemental material's +3, for an effective six damage before enchants.
        super(TC4ElementalToolTier.INSTANCE, 3.0F, -3.0F, properties.stacksTo(1));
    }

    @Override
    public Rarity getRarity(ItemStack stack) {
        return Rarity.RARE;
    }

    @Override
    public UseAnim getUseAnimation(ItemStack stack) {
        return UseAnim.BOW;
    }

    @Override
    public int getUseDuration(ItemStack stack) {
        return 72_000;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        player.startUsingItem(hand);
        return InteractionResultHolder.consume(stack);
    }

    @Override
    public void onUseTick(Level level, LivingEntity livingEntity, ItemStack stack, int remainingUseDuration) {
        if (!(livingEntity instanceof Player player)) {
            return;
        }

        AABB range = player.getBoundingBox().inflate(10.0D);
        List<ItemEntity> items = level.getEntitiesOfClass(ItemEntity.class, range,
                item -> !item.isRemoved()
                        && !item.getItem().isEmpty()
                        && (!(item instanceof FollowingItemEntity following) || following.getTarget() == null));

        for (ItemEntity item : items) {
            Vec3 away = item.position().subtract(
                    player.getX(),
                    player.getY() + player.getBbHeight() * 0.5D,
                    player.getZ());
            double distance = away.length();
            if (distance < 1.0E-5D) {
                continue;
            }

            if (!level.isClientSide) {
                Vec3 next = item.getDeltaMovement().subtract(away.scale(0.3D / distance));
                item.setDeltaMovement(
                        Mth.clamp(next.x, -0.35D, 0.35D),
                        Mth.clamp(next.y, -0.35D, 0.35D),
                        Mth.clamp(next.z, -0.35D, 0.35D));
                item.hasImpulse = true;

                if (level instanceof ServerLevel serverLevel) {
                    serverLevel.sendParticles(STREAM_PARTICLE,
                            item.getX() + (level.random.nextFloat() - level.random.nextFloat()) * 0.125F,
                            item.getY() + item.getBbHeight() * 0.5D
                                    + (level.random.nextFloat() - level.random.nextFloat()) * 0.125F,
                            item.getZ() + (level.random.nextFloat() - level.random.nextFloat()) * 0.125F,
                            1, 0.0D, 0.0D, 0.0D, 0.0D);
                }
            }
        }
    }

    @Override
    public boolean onBlockStartBreak(ItemStack stack, BlockPos pos, Player player) {
        if (INTERNAL_HARVEST.get()) {
            return false;
        }

        Level level = player.level;
        BlockState sourceState = level.getBlockState(pos);
        if (player.isShiftKeyDown() || !sourceState.is(BlockTags.LOGS)) {
            return false;
        }

        // Forge invokes this hook on both logical sides. Cancel the ordinary
        // base-log harvest on the client; the server performs the authoritative
        // farthest-log harvest below.
        if (level.isClientSide) {
            return true;
        }
        if (!(level instanceof ServerLevel serverLevel) || !(player instanceof ServerPlayer serverPlayer)) {
            return true;
        }

        BlockPos target = findFurthestConnectedLog(serverLevel, pos, sourceState.getBlock());
        AABB dropRange = new AABB(target).inflate(2.0D);
        Set<UUID> preExistingDrops = new HashSet<>();
        for (ItemEntity item : serverLevel.getEntitiesOfClass(ItemEntity.class, dropRange)) {
            preExistingDrops.add(item.getUUID());
        }

        boolean harvested;
        INTERNAL_HARVEST.set(true);
        try {
            harvested = serverPlayer.gameMode.destroyBlock(target);
        } finally {
            INTERNAL_HARVEST.remove();
        }

        if (harvested) {
            convertNewDrops(serverLevel, target, serverPlayer, preExistingDrops);
            scheduleNeighbourTicks(serverLevel, target);
            serverLevel.playSound(null, pos, TC4Sounds.event("bubble"), SoundSource.PLAYERS, 0.15F, 1.0F);
            serverLevel.sendParticles(STREAM_PARTICLE,
                    pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D,
                    12, 0.35D, 0.35D, 0.35D, 0.0D);
        }
        return true;
    }

    private static BlockPos findFurthestConnectedLog(ServerLevel level, BlockPos origin, Block sourceBlock) {
        BlockPos current = origin.immutable();
        double lastDistance = 0.0D;

        search:
        while (true) {
            for (int dx = -2; dx <= 2; dx++) {
                for (int dy = 2; dy >= -2; dy--) {
                    for (int dz = -2; dz <= 2; dz++) {
                        BlockPos candidate = current.offset(dx, dy, dz);
                        if (Math.abs(candidate.getX() - origin.getX()) > 24
                                || Math.abs(candidate.getY() - origin.getY()) > 48
                                || Math.abs(candidate.getZ() - origin.getZ()) > 24) {
                            return current;
                        }

                        BlockState state = level.getBlockState(candidate);
                        if (state.getBlock() != sourceBlock
                                || !state.is(BlockTags.LOGS)
                                || state.getDestroySpeed(level, candidate) < 0.0F) {
                            continue;
                        }

                        double distance = candidate.distSqr(origin);
                        if (distance > lastDistance) {
                            lastDistance = distance;
                            current = candidate.immutable();
                            continue search;
                        }
                    }
                }
            }
            return current;
        }
    }

    private static void convertNewDrops(ServerLevel level, BlockPos target, ServerPlayer player,
                                        Set<UUID> preExistingDrops) {
        AABB area = new AABB(target).inflate(2.0D);
        List<ItemEntity> drops = level.getEntitiesOfClass(ItemEntity.class, area,
                item -> !item.isRemoved()
                        && !preExistingDrops.contains(item.getUUID())
                        && !(item instanceof FollowingItemEntity)
                        && !item.getItem().isEmpty());

        for (ItemEntity drop : drops) {
            FollowingItemEntity following = new FollowingItemEntity(level,
                    drop.getX(), drop.getY(), drop.getZ(), drop.getItem().copy(), player, 10);
            following.setDeltaMovement(drop.getDeltaMovement());
            following.setPickUpDelay(10);
            if (level.addFreshEntity(following)) {
                drop.discard();
            }
        }
    }

    private static void scheduleNeighbourTicks(ServerLevel level, BlockPos center) {
        for (int dx = -3; dx <= 3; dx++) {
            for (int dy = -3; dy <= 3; dy++) {
                for (int dz = -3; dz <= 3; dz++) {
                    BlockPos pos = center.offset(dx, dy, dz);
                    BlockState state = level.getBlockState(pos);
                    if (!state.isAir()) {
                        level.scheduleTick(pos, state.getBlock(), 150 + level.random.nextInt(150));
                    }
                }
            }
        }
    }
}
