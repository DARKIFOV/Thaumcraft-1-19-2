package com.darkifov.thaumcraft.item;

import com.darkifov.thaumcraft.entity.FollowingItemEntity;
import com.darkifov.thaumcraft.item.gear.TC4ElementalToolTier;
import com.darkifov.thaumcraft.porting.TC4Sounds;
import com.mojang.math.Vector3f;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.ShovelItem;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraftforge.common.util.BlockSnapshot;
import net.minecraftforge.event.ForgeEventFactory;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/** Forge 1.19.2 port of TC4's Shovel of the Earthmover. */
public final class ElementalShovelItem extends ShovelItem {
    private static final String NBT_ORIENTATION = "TC4EarthmoverOrientation";
    private static final ThreadLocal<Boolean> INTERNAL_HARVEST = ThreadLocal.withInitial(() -> false);
    private static final DustParticleOptions EARTH_PARTICLE =
            new DustParticleOptions(new Vector3f(0.50F, 0.28F, 0.08F), 0.7F);

    public ElementalShovelItem(Properties properties) {
        super(TC4ElementalToolTier.INSTANCE, 1.5F, -3.0F, properties.stacksTo(1));
    }

    @Override
    public Rarity getRarity(ItemStack stack) {
        return Rarity.RARE;
    }

    public static int getOrientation(ItemStack stack) {
        return Math.floorMod(stack.getOrCreateTag().getInt(NBT_ORIENTATION), 3);
    }

    public static int cycleOrientation(ItemStack stack) {
        int next = (getOrientation(stack) + 1) % 3;
        stack.getOrCreateTag().putInt(NBT_ORIENTATION, next);
        return next;
    }

    public static Component orientationName(ItemStack stack) {
        return Component.translatable("tooltip.thaumcraft.elemental_shovel.orientation." + getOrientation(stack));
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("tooltip.thaumcraft.elemental_shovel.mode", orientationName(stack))
                .withStyle(ChatFormatting.GRAY));
        tooltip.add(Component.translatable("tooltip.thaumcraft.elemental_shovel.toggle").withStyle(ChatFormatting.DARK_GRAY));
    }

    @Override
    public boolean onBlockStartBreak(ItemStack stack, BlockPos origin, Player player) {
        if (INTERNAL_HARVEST.get() || player.isShiftKeyDown()) {
            return false;
        }
        Level level = player.level;
        if (!level.getBlockState(origin).is(BlockTags.MINEABLE_WITH_SHOVEL)) {
            return false;
        }
        if (level.isClientSide) {
            return true;
        }
        if (!(level instanceof ServerLevel serverLevel) || !(player instanceof ServerPlayer serverPlayer)) {
            return true;
        }

        Direction face = lookedFace(player, origin);
        INTERNAL_HARVEST.set(true);
        try {
            for (BlockPos target : miningPlane(origin, face)) {
                BlockState state = serverLevel.getBlockState(target);
                if (state.isAir() || !state.is(BlockTags.MINEABLE_WITH_SHOVEL)
                        || state.getDestroySpeed(serverLevel, target) < 0.0F) {
                    continue;
                }
                Set<UUID> existing = captureDrops(serverLevel, target);
                if (serverPlayer.gameMode.destroyBlock(target)) {
                    convertNewDrops(serverLevel, target, serverPlayer, existing);
                }
                if (stack.isEmpty()) {
                    break;
                }
            }
        } finally {
            INTERNAL_HARVEST.remove();
        }
        return true;
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Player player = context.getPlayer();
        if (player == null || player.isShiftKeyDown()) {
            return super.useOn(context);
        }
        Level level = context.getLevel();
        BlockPos sourcePos = context.getClickedPos();
        BlockState sourceState = level.getBlockState(sourcePos);
        if (level.getBlockEntity(sourcePos) != null || sourceState.isAir()) {
            return InteractionResult.PASS;
        }

        PlacementMaterial material = findPlacementMaterial(player, sourceState);
        if (material == null) {
            return InteractionResult.PASS;
        }

        List<BlockPos> targets = placementPlane(sourcePos, context.getClickedFace(), getOrientation(context.getItemInHand()), player.getYRot());
        if (level.isClientSide) {
            return targets.stream().anyMatch(pos -> canReplace(level, pos) && material.state.canSurvive(level, pos))
                    ? InteractionResult.SUCCESS : InteractionResult.PASS;
        }
        if (!(level instanceof ServerLevel serverLevel)) {
            return InteractionResult.PASS;
        }

        int placed = 0;
        for (BlockPos target : targets) {
            if (!canReplace(serverLevel, target) || !material.state.canSurvive(serverLevel, target)) {
                continue;
            }
            if (!player.getAbilities().instabuild && material.stack.isEmpty()) {
                break;
            }

            BlockSnapshot snapshot = BlockSnapshot.create(serverLevel.dimension(), serverLevel, target);
            if (!serverLevel.setBlock(target, material.state, Block.UPDATE_ALL)) {
                continue;
            }
            if (ForgeEventFactory.onBlockPlace(player, snapshot, context.getClickedFace())) {
                snapshot.restore(true, false);
                continue;
            }

            if (!player.getAbilities().instabuild) {
                material.stack.shrink(1);
            }
            context.getItemInHand().hurtAndBreak(1, player, p -> p.broadcastBreakEvent(context.getHand()));
            serverLevel.sendParticles(EARTH_PARTICLE,
                    target.getX() + 0.5D, target.getY() + 0.5D, target.getZ() + 0.5D,
                    7, 0.35D, 0.35D, 0.35D, 0.0D);
            placed++;
            if (context.getItemInHand().isEmpty()) {
                break;
            }
        }

        if (placed > 0) {
            serverLevel.playSound(null, sourcePos, TC4Sounds.event("wand"), SoundSource.PLAYERS,
                    0.35F, 0.9F + serverLevel.random.nextFloat() * 0.2F);
            return InteractionResult.CONSUME;
        }
        return InteractionResult.PASS;
    }

    public static List<BlockPos> placementPlane(BlockPos source, Direction face, int orientation, float playerYaw) {
        BlockPos anchor = source.relative(face);
        List<BlockPos> out = new ArrayList<>(9);
        boolean yawAlongX = Math.abs(net.minecraft.util.Mth.sin(playerYaw * ((float) Math.PI / 180.0F)))
                > Math.abs(net.minecraft.util.Mth.cos(playerYaw * ((float) Math.PI / 180.0F)));
        for (int a = -1; a <= 1; a++) {
            for (int b = -1; b <= 1; b++) {
                int x = 0, y = 0, z = 0;
                if (orientation == 0) {
                    if (face.getAxis() == Direction.Axis.Y) { x = a; z = b; }
                    else if (face.getAxis() == Direction.Axis.Z) { x = a; y = b; }
                    else { z = a; y = b; }
                } else if (orientation == 1) {
                    y = b;
                    if (face.getAxis() == Direction.Axis.Y) {
                        if (yawAlongX) x = a; else z = a;
                    } else if (face.getAxis() == Direction.Axis.Z) {
                        z = a;
                    } else {
                        x = a;
                    }
                } else {
                    if (face.getAxis() == Direction.Axis.Y) {
                        y = b;
                        if (yawAlongX) x = a; else z = a;
                    } else {
                        x = a;
                        z = b;
                    }
                }
                out.add(anchor.offset(x, y, z));
            }
        }
        return out;
    }

    public static List<BlockPos> previewPositions(Player player, BlockHitResult hit, ItemStack stack) {
        return placementPlane(hit.getBlockPos(), hit.getDirection(), getOrientation(stack), player.getYRot());
    }

    private static List<BlockPos> miningPlane(BlockPos origin, Direction face) {
        List<BlockPos> out = new ArrayList<>(9);
        for (int a = -1; a <= 1; a++) {
            for (int b = -1; b <= 1; b++) {
                if (face.getAxis() == Direction.Axis.Y) out.add(origin.offset(a, 0, b));
                else if (face.getAxis() == Direction.Axis.Z) out.add(origin.offset(a, b, 0));
                else out.add(origin.offset(0, b, a));
            }
        }
        return out;
    }

    private static Direction lookedFace(Player player, BlockPos expected) {
        HitResult result = player.pick(6.0D, 0.0F, false);
        if (result instanceof BlockHitResult blockHit && blockHit.getBlockPos().equals(expected)) {
            return blockHit.getDirection();
        }
        return Direction.UP;
    }

    private static boolean canReplace(Level level, BlockPos pos) {
        BlockState state = level.getBlockState(pos);
        if (state.getFluidState().is(FluidTags.LAVA)) {
            return false;
        }
        return state.isAir() || state.getFluidState().is(FluidTags.WATER) || state.getMaterial().isReplaceable();
    }

    @Nullable
    private static PlacementMaterial findPlacementMaterial(Player player, BlockState sourceState) {
        BlockState stateToPlace = sourceState;
        Item wanted = sourceState.getBlock().asItem();
        if (wanted == Items.AIR && !sourceState.is(Blocks.GRASS_BLOCK)) {
            return null;
        }
        ItemStack found = findStack(player.getInventory(), wanted);
        if (found.isEmpty() && sourceState.is(Blocks.GRASS_BLOCK)) {
            stateToPlace = Blocks.DIRT.defaultBlockState();
            found = findStack(player.getInventory(), Items.DIRT);
        }
        if (!player.getAbilities().instabuild && found.isEmpty()) {
            return null;
        }
        return new PlacementMaterial(stateToPlace, found);
    }

    private static ItemStack findStack(Inventory inventory, Item item) {
        if (item == Items.AIR) return ItemStack.EMPTY;
        for (ItemStack candidate : inventory.items) {
            if (!candidate.isEmpty() && candidate.is(item)) return candidate;
        }
        for (ItemStack candidate : inventory.offhand) {
            if (!candidate.isEmpty() && candidate.is(item)) return candidate;
        }
        return ItemStack.EMPTY;
    }

    private static Set<UUID> captureDrops(ServerLevel level, BlockPos pos) {
        Set<UUID> existing = new HashSet<>();
        for (ItemEntity item : level.getEntitiesOfClass(ItemEntity.class, new AABB(pos).inflate(2.0D))) {
            existing.add(item.getUUID());
        }
        return existing;
    }

    private static void convertNewDrops(ServerLevel level, BlockPos target, ServerPlayer player, Set<UUID> existing) {
        for (ItemEntity drop : level.getEntitiesOfClass(ItemEntity.class, new AABB(target).inflate(2.0D),
                item -> !item.isRemoved() && !existing.contains(item.getUUID())
                        && !(item instanceof FollowingItemEntity) && !item.getItem().isEmpty())) {
            FollowingItemEntity following = new FollowingItemEntity(level,
                    drop.getX(), drop.getY(), drop.getZ(), drop.getItem().copy(), player, 3);
            following.setDeltaMovement(drop.getDeltaMovement());
            following.setPickUpDelay(10);
            if (level.addFreshEntity(following)) {
                drop.discard();
            }
        }
    }

    private record PlacementMaterial(BlockState state, ItemStack stack) {}
}
