package com.darkifov.thaumcraft.wand;

import com.darkifov.thaumcraft.Aspect;
import com.darkifov.thaumcraft.AspectList;
import com.darkifov.thaumcraft.ThaumcraftMod;
import com.darkifov.thaumcraft.block.WandItem;
import com.darkifov.thaumcraft.porting.TC4Sounds;
import com.darkifov.thaumcraft.porting.TC4ClientFocusFxBridge;
import com.darkifov.thaumcraft.entity.projectile.TC4EmberEntity;
import com.darkifov.thaumcraft.entity.projectile.TC4ExplosiveOrbEntity;
import com.darkifov.thaumcraft.entity.projectile.TC4FocusProjectileEntity;
import com.darkifov.thaumcraft.entity.projectile.TC4FrostShardEntity;
import com.darkifov.thaumcraft.entity.projectile.TC4PrimalOrbEntity;
import com.darkifov.thaumcraft.entity.projectile.TC4ShockOrbEntity;
import com.darkifov.thaumcraft.ward.WardedBlockRuntime;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.List;
import java.util.function.Predicate;

/**
 * 1.19.2 adapter for original TC4 wand foci.
 *
 * Stage171/172 keeps this class tied to the original focus classes, not to new
 * invented spell rules.  Projectile/entity classes from 1.7.10 cannot be dropped
 * directly into Forge 1.19.2, so the runtime below preserves the original base
 * costs, cooldowns, ray/beam style and server-side effects using modern APIs.
 */
public final class WandFocusRuntime {
    public static final String TAG_FOCUS = "Focus";
    public static final String TAG_ORIGINAL_FOCUS_STACK = "focus"; // original TC4 ItemWandCasting NBT key

    private static final Map<String, Long> FIRE_SOUND_DELAY = new HashMap<>();
    private static final Map<String, Long> EXCAVATION_SOUND_DELAY = new HashMap<>();
    private static final Map<String, Long> WARDING_DELAY = new HashMap<>();
    private static final Map<String, Float> EXCAVATION_BREAKCOUNT = new HashMap<>();
    private static final Map<String, BlockPos> EXCAVATION_LAST_BLOCK = new HashMap<>();

    private WandFocusRuntime() {
    }

    /** Original ItemFocusBasic.WandFocusAnimation states used by ItemWandRenderer. */
    public enum WandFocusAnimation {
        WAVE,
        CHARGE
    }

    public static WandFocusAnimation focusAnimation(ItemStack wandStack) {
        WandFocusType type = getFocus(wandStack);
        if (type == null) {
            return WandFocusAnimation.WAVE;
        }
        // Original ItemFocusBasic defaults to WAVE. Fire/Shock switch to CHARGE unless
        // their projectile upgrades are installed; Excavation is always CHARGE.
        if (type == WandFocusType.FIRE) {
            return focusHasUpgrade(wandStack, FocusUpgradeType.FIREBALL) ? WandFocusAnimation.WAVE : WandFocusAnimation.CHARGE;
        }
        if (type == WandFocusType.SHOCK) {
            return focusHasUpgrade(wandStack, FocusUpgradeType.EARTH_SHOCK) ? WandFocusAnimation.WAVE : WandFocusAnimation.CHARGE;
        }
        if (type == WandFocusType.EXCAVATION) {
            return WandFocusAnimation.CHARGE;
        }
        return WandFocusAnimation.WAVE;
    }

    public static WandFocusType getFocus(ItemStack wandStack) {
        if (!(wandStack.getItem() instanceof WandItem)) {
            return null;
        }
        return WandFocusType.byId(wandStack.getOrCreateTag().getString(TAG_FOCUS));
    }

    public static boolean hasFocus(ItemStack wandStack) {
        return getFocus(wandStack) != null;
    }

    public static void setFocus(ItemStack wandStack, WandFocusType type) {
        if (type == null) {
            wandStack.getOrCreateTag().remove(TAG_FOCUS);
            wandStack.getOrCreateTag().remove(TAG_ORIGINAL_FOCUS_STACK);
        } else {
            wandStack.getOrCreateTag().putString(TAG_FOCUS, type.id());
            ItemStack focus = focusStack(type);
            saveFocusStack(wandStack, focus);
        }
    }

    public static void setFocusStack(ItemStack wandStack, ItemStack focusStack) {
        if (focusStack.isEmpty() || !(focusStack.getItem() instanceof com.darkifov.thaumcraft.block.WandFocusItem focusItem)) {
            setFocus(wandStack, null);
            return;
        }
        ItemStack copy = focusStack.copy();
        copy.setCount(1);
        wandStack.getOrCreateTag().putString(TAG_FOCUS, focusItem.focusType().id());
        saveFocusStack(wandStack, copy);
    }

    public static ItemStack getFocusStack(ItemStack wandStack) {
        CompoundTag tag = wandStack.getTag();
        if (tag != null && tag.contains(TAG_ORIGINAL_FOCUS_STACK, 10)) {
            ItemStack stack = ItemStack.of(tag.getCompound(TAG_ORIGINAL_FOCUS_STACK));
            if (!stack.isEmpty()) {
                return stack;
            }
        }
        WandFocusType type = getFocus(wandStack);
        return type == null ? ItemStack.EMPTY : focusStack(type);
    }

    private static void saveFocusStack(ItemStack wandStack, ItemStack focusStack) {
        if (focusStack.isEmpty()) {
            wandStack.getOrCreateTag().remove(TAG_ORIGINAL_FOCUS_STACK);
            return;
        }
        wandStack.getOrCreateTag().put(TAG_ORIGINAL_FOCUS_STACK, focusStack.save(new CompoundTag()));
    }

    public static boolean consumeFocusVis(ItemStack wandStack, Player player, WandFocusType type) {
        return consumeFocusVis(wandStack, player, type, type == WandFocusType.PRIMAL ? primalCost(player.level.random) : type.visCost());
    }

    public static boolean consumeFocusVis(ItemStack wandStack, Player player, WandFocusType type, AspectList cost) {
        if (player.getAbilities().instabuild || WandItem.hasInfiniteVis(wandStack)) {
            return true;
        }
        for (var entry : cost.entries().entrySet()) {
            int needed = focusModifiedVisCost(wandStack, entry.getKey(), entry.getValue());
            if (WandItem.getVis(wandStack, entry.getKey()) < needed) {
                player.displayClientMessage(Component.literal("Not enough " + entry.getKey().displayName() + " vis for " + type.displayName() + ": need " + needed + " after cap modifier.").withStyle(ChatFormatting.RED), true);
                return false;
            }
        }
        for (var entry : cost.entries().entrySet()) {
            WandItem.consumeVis(wandStack, entry.getKey(), focusModifiedVisCost(wandStack, entry.getKey(), entry.getValue()));
        }
        return true;
    }

    public static InteractionResult cast(ItemStack wandStack, Level level, Player player) {
        WandFocusType type = getFocus(wandStack);
        if (type == null) {
            return InteractionResult.PASS;
        }

        AspectList resolvedCost = focusVisCost(wandStack, type, level.random);
        if (!consumeFocusVis(wandStack, player, type, resolvedCost)) {
            level.playSound(null, player.blockPosition(), TC4Sounds.event("wandfail"), SoundSource.PLAYERS, 0.55F, 1.0F);
            return InteractionResult.CONSUME;
        }

        boolean success = switch (type) {
            case FIRE -> castFire(level, player);
            case FROST -> castFrost(level, player);
            case SHOCK -> castShock(level, player);
            case EXCAVATION -> castExcavation(level, player);
            case PORTABLE_HOLE -> castPortableHole(level, player);
            case EQUAL_TRADE -> castEqualTrade(level, player);
            case WARDING -> castWarding(level, player);
            case PRIMAL -> castPrimal(level, player);
        };

        int cooldown = activationCooldown(wandStack, type);
        if (success && cooldown > 0) {
            player.getCooldowns().addCooldown(wandStack.getItem(), cooldown);
        }
        if (!playsOwnActivationSound(wandStack, type)) {
            level.playSound(null, player.blockPosition(), soundFor(type), SoundSource.PLAYERS, 0.65F, pitchFor(type));
        }
        return InteractionResult.CONSUME;
    }

    private static boolean playsOwnActivationSound(ItemStack wandStack, WandFocusType type) {
        return switch (type) {
            case FROST, PORTABLE_HOLE, PRIMAL -> true;
            case FIRE -> focusHasUpgrade(wandStack, FocusUpgradeType.FIREBALL);
            case SHOCK -> focusHasUpgrade(wandStack, FocusUpgradeType.EARTH_SHOCK);
            default -> false;
        };
    }

    public static boolean shouldUseContinuously(ItemStack wandStack) {
        WandFocusType type = getFocus(wandStack);
        if (type == WandFocusType.FIRE) {
            return !focusHasUpgrade(wandStack, FocusUpgradeType.FIREBALL);
        }
        if (type == WandFocusType.SHOCK) {
            return !focusHasUpgrade(wandStack, FocusUpgradeType.EARTH_SHOCK);
        }
        return type == WandFocusType.EXCAVATION;
    }

    public static void beginContinuousUse(ItemStack wandStack, Level level, Player player) {
        WandFocusType type = getFocus(wandStack);
        if (type == WandFocusType.FIRE || type == WandFocusType.SHOCK || type == WandFocusType.EXCAVATION) {
            player.swing(player.getUsedItemHand(), true);
        }
    }

    public static void onUsingFocusTick(ItemStack wandStack, Level level, Player player, int count) {
        WandFocusType type = getFocus(wandStack);
        if (type == null) {
            player.stopUsingItem();
            return;
        }
        switch (type) {
            case FIRE -> onUsingFireFocusTick(wandStack, level, player, count);
            case SHOCK -> onUsingShockFocusTick(wandStack, level, player, count);
            case EXCAVATION -> onUsingExcavationFocusTick(wandStack, level, player, count);
            default -> { }
        }
    }

    public static void onPlayerStoppedUsingFocus(ItemStack wandStack, Level level, Player player, int count) {
        if (getFocus(wandStack) == WandFocusType.EXCAVATION) {
            String pp = focusUseKey(level, player);
            EXCAVATION_BREAKCOUNT.put(pp, 0.0F);
            EXCAVATION_LAST_BLOCK.put(pp, BlockPos.ZERO);
        }
    }

    public static int focusUpgradeLevel(ItemStack wandStack, FocusUpgradeType upgrade) {
        return FocusUpgradeRuntime.getUpgradeLevel(getFocusStack(wandStack), upgrade);
    }

    public static boolean focusHasUpgrade(ItemStack wandStack, FocusUpgradeType upgrade) {
        return focusUpgradeLevel(wandStack, upgrade) > 0;
    }


    public static AspectList focusVisCost(ItemStack wandStack, WandFocusType type, RandomSource random) {
        AspectList cost = switch (type) {
            case FIRE -> focusHasUpgrade(wandStack, FocusUpgradeType.FIREBALL)
                    ? cost(Aspect.IGNIS, 66, Aspect.PERDITIO, 33)
                    : focusHasUpgrade(wandStack, FocusUpgradeType.FIREBEAM)
                    ? cost(Aspect.IGNIS, 10, Aspect.ORDO, 3)
                    : cost(Aspect.IGNIS, 10);
            case FROST -> focusHasUpgrade(wandStack, FocusUpgradeType.ICE_BOULDER)
                    ? cost(Aspect.AQUA, 20, Aspect.IGNIS, 2, Aspect.PERDITIO, 2, Aspect.TERRA, 5)
                    : focusHasUpgrade(wandStack, FocusUpgradeType.SCATTERSHOT)
                    ? cost(Aspect.AQUA, 20, Aspect.IGNIS, 2, Aspect.PERDITIO, 2, Aspect.AER, 5)
                    : cost(Aspect.AQUA, 5, Aspect.IGNIS, 2, Aspect.PERDITIO, 2);
            case SHOCK -> focusHasUpgrade(wandStack, FocusUpgradeType.EARTH_SHOCK)
                    ? cost(Aspect.AER, 75, Aspect.TERRA, 25)
                    : focusHasUpgrade(wandStack, FocusUpgradeType.CHAIN_LIGHTNING)
                    ? cost(Aspect.AER, 40, Aspect.AQUA, 10)
                    : cost(Aspect.AER, 25);
            case EXCAVATION -> focusHasUpgrade(wandStack, FocusUpgradeType.SILK_TOUCH)
                    ? cost(Aspect.AER, 1, Aspect.IGNIS, 1, Aspect.TERRA, 16, Aspect.AQUA, 1, Aspect.ORDO, 1, Aspect.PERDITIO, 1)
                    : focusHasUpgrade(wandStack, FocusUpgradeType.DOWSING)
                    ? cost(Aspect.TERRA, 15, Aspect.IGNIS, 2, Aspect.ORDO, 2)
                    : cost(Aspect.TERRA, 15);
            case PORTABLE_HOLE -> cost(Aspect.PERDITIO, 10, Aspect.AER, 10);
            case EQUAL_TRADE -> focusHasUpgrade(wandStack, FocusUpgradeType.SILK_TOUCH)
                    ? cost(Aspect.PERDITIO, 6, Aspect.TERRA, 6, Aspect.ORDO, 6, Aspect.AER, 1, Aspect.IGNIS, 1, Aspect.AQUA, 1)
                    : cost(Aspect.PERDITIO, 5, Aspect.TERRA, 5, Aspect.ORDO, 5);
            case WARDING -> cost(Aspect.TERRA, 25, Aspect.ORDO, 25, Aspect.AQUA, 10);
            case PRIMAL -> primalCost(random);
        };
        return cost;
    }

    public static int activationCooldown(ItemStack wandStack, WandFocusType type) {
        return switch (type) {
            case FIRE -> focusHasUpgrade(wandStack, FocusUpgradeType.FIREBALL) ? 1000 : 0;
            case FROST -> focusHasUpgrade(wandStack, FocusUpgradeType.SCATTERSHOT) || focusHasUpgrade(wandStack, FocusUpgradeType.ICE_BOULDER) ? 500 : 200;
            case SHOCK -> focusHasUpgrade(wandStack, FocusUpgradeType.EARTH_SHOCK) ? 1000 : focusHasUpgrade(wandStack, FocusUpgradeType.CHAIN_LIGHTNING) ? 500 : 250;
            case PRIMAL -> 500;
            default -> type.cooldownTicks();
        };
    }

    private static AspectList cost(Object... pairs) {
        AspectList list = new AspectList();
        for (int i = 0; i + 1 < pairs.length; i += 2) {
            list.add((Aspect) pairs[i], (Integer) pairs[i + 1]);
        }
        return list;
    }

    private static int focusModifiedVisCost(ItemStack wandStack, Aspect aspect, int baseAmount) {
        if (baseAmount <= 0 || WandItem.hasInfiniteVis(wandStack)) {
            return 0;
        }
        float capModifier = WandComponentData.from(wandStack).visCostModifier(wandStack, aspect);
        float frugalModifier = focusUpgradeLevel(wandStack, FocusUpgradeType.FRUGAL) / 10.0F;
        return Math.max(1, (int) Math.ceil(baseAmount * Math.max(0.1F, capModifier - frugalModifier)));
    }

    public static ItemStack focusStack(WandFocusType type) {
        return switch (type) {
            case FIRE -> new ItemStack(ThaumcraftMod.FOCUS_FIRE.get());
            case FROST -> new ItemStack(ThaumcraftMod.FOCUS_FROST.get());
            case SHOCK -> new ItemStack(ThaumcraftMod.FOCUS_SHOCK.get());
            case EXCAVATION -> new ItemStack(ThaumcraftMod.FOCUS_EXCAVATION.get());
            case PORTABLE_HOLE -> new ItemStack(ThaumcraftMod.FOCUS_PORTABLE_HOLE.get());
            case EQUAL_TRADE -> new ItemStack(ThaumcraftMod.FOCUS_EQUAL_TRADE.get());
            case WARDING -> new ItemStack(ThaumcraftMod.FOCUS_WARDING.get());
            case PRIMAL -> new ItemStack(ThaumcraftMod.FOCUS_PRIMAL.get());
        };
    }

    private static boolean castFire(Level level, Player player) {
        ItemStack wandStack = player.getMainHandItem();
        if (focusHasUpgrade(wandStack, FocusUpgradeType.FIREBALL)) {
            if (!level.isClientSide) {
                TC4ExplosiveOrbEntity orb = new TC4ExplosiveOrbEntity(ThaumcraftMod.FOCUS_EXPLOSIVE_ORB.get(), level, player);
                orb.setStrength(1.0F + focusUpgradeLevel(wandStack, FocusUpgradeType.POTENCY) * 0.4F);
                orb.setAlchemistsFire(focusHasUpgrade(wandStack, FocusUpgradeType.ALCHEMISTS_FIRE));
                shootProjectile(orb, player, 1.15F, 0.0F);
                level.addFreshEntity(orb);
                level.levelEvent(null, 1009, player.blockPosition(), 0);
            }
            return true;
        }
        HitBundle hit = ray(level, player, 17.0D);
        beam(level, player.getEyePosition(), hit.end(), Aspect.IGNIS.argbColor(), ParticleTypes.FLAME);
        if (!level.isClientSide && hit.entityHit() != null && hit.entityHit().getEntity() instanceof LivingEntity living) {
            living.setSecondsOnFire(5);
            living.hurt(DamageSource.ON_FIRE, 4.0F);
            return true;
        }
        if (!level.isClientSide && hit.blockHit() != null) {
            BlockPos firePos = hit.blockHit().getBlockPos().relative(hit.blockHit().getDirection());
            if (level.getBlockState(firePos).isAir()) {
                level.setBlock(firePos, Blocks.FIRE.defaultBlockState(), 11);
                return true;
            }
        }
        return true;
    }

    private static boolean castFrost(Level level, Player player) {
        ItemStack wandStack = player.getMainHandItem();
        int potency = focusUpgradeLevel(wandStack, FocusUpgradeType.POTENCY);
        int frosty = focusUpgradeLevel(wandStack, FocusUpgradeType.ALCHEMISTS_FROST);
        if (!level.isClientSide) {
            Entity soundSource = player;
            if (focusHasUpgrade(wandStack, FocusUpgradeType.SCATTERSHOT)) {
                int count = 5 + potency * 2;
                for (int i = 0; i < count; i++) {
                    TC4FrostShardEntity shard = new TC4FrostShardEntity(ThaumcraftMod.FOCUS_FROST_SHARD.get(), level, player);
                    shard.setDamage(1.0F);
                    shard.setFragile(true);
                    shard.setFrosty(frosty);
                    shootProjectile(shard, player, 1.1F, 8.0F);
                    level.addFreshEntity(shard);
                    soundSource = shard;
                }
            } else {
                TC4FrostShardEntity shard = new TC4FrostShardEntity(ThaumcraftMod.FOCUS_FROST_SHARD.get(), level, player);
                if (focusHasUpgrade(wandStack, FocusUpgradeType.ICE_BOULDER)) {
                    shard.setDamage(4.0F + potency * 2.0F);
                    shard.setBounce(0.8D, 6);
                    shootProjectile(shard, player, 0.75F, 1.0F);
                } else {
                    shard.setDamage((float) (3.0D + potency * 1.5D));
                    shootProjectile(shard, player, 1.15F, 1.0F);
                }
                shard.setFrosty(frosty);
                level.addFreshEntity(shard);
                soundSource = shard;
            }
            level.playSound(null, soundSource.getX(), soundSource.getY(), soundSource.getZ(), TC4Sounds.event("ice"), SoundSource.PLAYERS, 0.4F, 1.0F + level.random.nextFloat() * 0.1F);
        }
        return true;
    }

    private static boolean castShock(Level level, Player player) {
        ItemStack wandStack = player.getMainHandItem();
        int potency = focusUpgradeLevel(wandStack, FocusUpgradeType.POTENCY);
        if (focusHasUpgrade(wandStack, FocusUpgradeType.EARTH_SHOCK)) {
            if (!level.isClientSide) {
                TC4ShockOrbEntity orb = new TC4ShockOrbEntity(ThaumcraftMod.FOCUS_SHOCK_ORB.get(), level, player);
                orb.setArea(4.0F + focusUpgradeLevel(wandStack, FocusUpgradeType.ENLARGE) * 2.0F);
                orb.setDamage((int) (5 + potency * 1.33D));
                shootProjectile(orb, player, 0.85F, 0.0F);
                level.addFreshEntity(orb);
                level.playSound(null, player.blockPosition(), TC4Sounds.event("zap"), SoundSource.PLAYERS, 1.0F, 1.0F + (level.random.nextFloat() - level.random.nextFloat()) * 0.2F);
            }
            return true;
        }
        HitBundle hit = ray(level, player, 10.0D);
        beam(level, player.getEyePosition(), hit.end(), Aspect.AER.argbColor(), ParticleTypes.ELECTRIC_SPARK);
        if (!level.isClientSide && hit.entityHit() != null && hit.entityHit().getEntity() instanceof LivingEntity living) {
            int chainLevel = focusUpgradeLevel(wandStack, FocusUpgradeType.CHAIN_LIGHTNING);
            living.hurt(DamageSource.LIGHTNING_BOLT, (chainLevel > 0 ? 6.0F : 4.0F) + potency);
            if (chainLevel > 0) {
                chainShock(level, living, chainLevel * 2 + focusUpgradeLevel(wandStack, FocusUpgradeType.ENLARGE) * 2, potency, List.of(living.getId()));
            }
            return true;
        }
        return true;
    }

    private static boolean castExcavation(Level level, Player player) {
        ItemStack wandStack = player.getMainHandItem();
        HitBundle hit = ray(level, player, 8.0D);
        beam(level, player.getEyePosition(), hit.end(), Aspect.TERRA.argbColor(), ParticleTypes.CRIT);
        if (!level.isClientSide && hit.blockHit() != null) {
            BlockPos target = hit.blockHit().getBlockPos();
            return excavateBlock(level, player, target, wandStack);
        }
        return true;
    }

    private static boolean castPortableHole(Level level, Player player) {
        ItemStack wandStack = player.getMainHandItem();
        HitBundle hit = ray(level, player, 16.0D);
        beam(level, player.getEyePosition(), hit.end(), Aspect.PERDITIO.argbColor(), ParticleTypes.PORTAL);
        if (!level.isClientSide && hit.blockHit() != null) {
            Direction direction = hit.blockHit().getDirection();
            BlockPos cursor = hit.blockHit().getBlockPos();
            int maxDistance = 33 + focusUpgradeLevel(wandStack, FocusUpgradeType.ENLARGE) * 8;
            int duration = 120 + focusUpgradeLevel(wandStack, FocusUpgradeType.EXTEND) * 60;
            List<BlockPos> tunnel = new ArrayList<>();
            for (int i = 0; i < maxDistance; i++) {
                BlockPos p = cursor.relative(direction.getOpposite(), i);
                BlockState state = level.getBlockState(p);
                if (state.isAir() || state.getDestroySpeed(level, p) < 0.0F || state.getDestroySpeed(level, p) > 12.0F || !WardedBlockRuntime.mayEdit(level, p, player)) {
                    break;
                }
                tunnel.add(p);
            }
            int opened = tunnel.size();
            if (opened > 1) {
                // Original ItemFocusPortableHole multiplies the base vis list by tunnel distance after measuring the tunnel.
                // Forge 1.19.2 adapter: cast() has already paid one base cost, so only the measured extra distance is charged here before the tunnel is opened.
                AspectList extraCost = scaledCost(focusVisCost(wandStack, WandFocusType.PORTABLE_HOLE, level.random), opened - 1);
                if (!consumeFocusVis(wandStack, player, WandFocusType.PORTABLE_HOLE, extraCost)) {
                    return false;
                }
            }
            for (BlockPos p : tunnel) {
                level.setBlock(p, ThaumcraftMod.TEMPORARY_HOLE.get().defaultBlockState(), 3);
                if (level instanceof ServerLevel server) {
                    server.scheduleTick(p, ThaumcraftMod.TEMPORARY_HOLE.get(), duration);
                    server.sendParticles(ParticleTypes.PORTAL, p.getX() + 0.5D, p.getY() + 0.5D, p.getZ() + 0.5D, 6, 0.25D, 0.25D, 0.25D, 0.02D);
                }
            }
            if (opened > 0) {
                level.playSound(null, cursor, TC4Sounds.event("hhon"), SoundSource.BLOCKS, 1.0F, 1.0F);
            }
        }
        return true;
    }

    private static boolean castEqualTrade(Level level, Player player) {
        ItemStack wandStack = player.getMainHandItem();
        HitBundle hit = ray(level, player, 12.0D);
        beam(level, player.getEyePosition(), hit.end(), Aspect.PERMUTATIO.argbColor(), ParticleTypes.ENCHANT);
        if (!level.isClientSide && hit.blockHit() != null) {
            BlockPos origin = hit.blockHit().getBlockPos();
            if (player.isShiftKeyDown()) {
                if (level.getBlockState(origin).hasBlockEntity()) {
                    player.displayClientMessage(Component.literal("Equal Trade cannot pick tile entities.").withStyle(ChatFormatting.GRAY), true);
                    return true;
                }
                storePickedBlock(wandStack, level.getBlockState(origin).getBlock().asItem().getDefaultInstance());
                player.displayClientMessage(Component.literal("Equal Trade picked " + level.getBlockState(origin).getBlock().getName().getString() + ".").withStyle(ChatFormatting.GRAY), true);
                return true;
            }
            ItemStack picked = getPickedBlock(wandStack);
            if (picked.isEmpty()) {
                picked = player.getOffhandItem(); // Forge 1.19.2 adapter until TC4 focus radial/pick UI is fully restored.
            }
            if (!picked.isEmpty() && picked.getItem() instanceof BlockItem blockItem) {
                List<BlockPos> targets = focusHasUpgrade(wandStack, FocusUpgradeType.ARCHITECT)
                        ? FocusArchitectRuntime.equalTradeArchitectBlocks(wandStack, level, hit.blockHit(), player)
                        : FocusArchitectRuntime.equalTradeLinkedBlocks(level, origin, 3 + focusUpgradeLevel(wandStack, FocusUpgradeType.ENLARGE) * 2, player);
                int changed = 0;
                for (BlockPos target : targets) {
                    if (changed > 0 && !consumeFocusVis(wandStack, player, WandFocusType.EQUAL_TRADE, focusVisCost(wandStack, WandFocusType.EQUAL_TRADE, level.random))) {
                        break;
                    }
                    if (replaceBlockWithEqualTrade(level, player, target, blockItem, picked, wandStack)) {
                        changed++;
                    }
                }
                return changed > 0;
            }
            player.displayClientMessage(Component.literal("Equal Trade needs a picked block; sneak-use a block first.").withStyle(ChatFormatting.GRAY), true);
        }
        return true;
    }

    private static boolean castWarding(Level level, Player player) {
        ItemStack wandStack = player.getMainHandItem();
        HitBundle hit = ray(level, player, 10.0D);
        beam(level, player.getEyePosition(), hit.end(), Aspect.ORDO.argbColor(), ParticleTypes.ENCHANT);
        if (!level.isClientSide && hit.blockHit() != null) {
            BlockPos origin = hit.blockHit().getBlockPos();
            String wardKey = origin.getX() + ":" + origin.getY() + ":" + origin.getZ() + ":" + level.dimension().location();
            long now = System.currentTimeMillis();
            if (WARDING_DELAY.getOrDefault(wardKey, 0L) > now) {
                return true;
            }
            WARDING_DELAY.put(wardKey, now + 500L);
            boolean removing = WardedBlockRuntime.isWarded(level, origin);
            List<BlockPos> targets = FocusArchitectRuntime.wardingArchitectBlocks(wandStack, level, hit.blockHit(), player, removing);
            int changed = 0;
            for (BlockPos target : targets) {
                if (removing) {
                    if (WardedBlockRuntime.unward(level, target, player)) {
                        changed++;
                        sparkleBlock(level, target, Aspect.ORDO.argbColor());
                    }
                } else {
                    if (changed > 0 && !consumeFocusVis(wandStack, player, WandFocusType.WARDING, focusVisCost(wandStack, WandFocusType.WARDING, level.random))) {
                        break;
                    }
                    if (WardedBlockRuntime.ward(level, target, player)) {
                        changed++;
                        sparkleBlock(level, target, Aspect.ORDO.argbColor());
                    }
                }
            }
            if (changed > 0) {
                level.playSound(null, origin, TC4Sounds.event("zap"), SoundSource.BLOCKS, 0.25F, 1.0F);
                player.displayClientMessage(Component.literal((removing ? "The ward fades from " : "Warding binds ") + changed + " block(s).").withStyle(ChatFormatting.BLUE), true);
            } else if (removing) {
                player.displayClientMessage(Component.literal("Another thaumaturge owns this ward.").withStyle(ChatFormatting.RED), true);
            }
        }
        return true;
    }

    private static boolean castPrimal(Level level, Player player) {
        ItemStack wandStack = player.getMainHandItem();
        if (!level.isClientSide) {
            TC4PrimalOrbEntity orb = new TC4PrimalOrbEntity(ThaumcraftMod.FOCUS_PRIMAL_ORB.get(), level, player);
            orb.setSeeker(focusHasUpgrade(wandStack, FocusUpgradeType.SEEKER));
            shootProjectile(orb, player, 1.0F, 0.0F);
            level.addFreshEntity(orb);
            level.playSound(null, orb.getX(), orb.getY(), orb.getZ(), TC4Sounds.event("ice"), SoundSource.PLAYERS, 0.3F, 0.8F + level.random.nextFloat() * 0.1F);
        }
        return true;
    }


    private static void onUsingFireFocusTick(ItemStack wandStack, Level level, Player player, int count) {
        AspectList cost = focusVisCost(wandStack, WandFocusType.FIRE, level.random);
        if (!hasFocusVis(wandStack, player, WandFocusType.FIRE, cost)) {
            player.stopUsingItem();
            return;
        }
        if (level.isClientSide) {
            return;
        }
        if (!consumeFocusVis(wandStack, player, WandFocusType.FIRE, cost)) {
            player.stopUsingItem();
            return;
        }
        String pp = focusUseKey(level, player);
        long now = System.currentTimeMillis();
        if (FIRE_SOUND_DELAY.getOrDefault(pp, 0L) < now) {
            level.playSound(null, player.getX(), player.getY(), player.getZ(), TC4Sounds.event("fireloop"), SoundSource.PLAYERS, 0.33F, 2.0F);
            FIRE_SOUND_DELAY.put(pp, now + 500L);
        }
        int potency = focusUpgradeLevel(wandStack, FocusUpgradeType.POTENCY);
        boolean fireBeam = focusHasUpgrade(wandStack, FocusUpgradeType.FIREBEAM);
        float scatter = fireBeam ? 0.25F : 15.0F;
        int firey = focusUpgradeLevel(wandStack, FocusUpgradeType.ALCHEMISTS_FIRE);
        for (int a = 0; a < 2 + potency; a++) {
            TC4EmberEntity ember = new TC4EmberEntity(ThaumcraftMod.FOCUS_EMBER.get(), level, player);
            float damage = 2.0F + potency;
            if (fireBeam) {
                damage = (damage + 0.5F) * 1.5F;
                ember.setDuration(30);
            }
            ember.setDamage(damage);
            ember.setFirey(firey);
            shootProjectile(ember, player, 1.0F, scatter);
            ember.setPos(ember.getX() + ember.getDeltaMovement().x, ember.getY() + ember.getDeltaMovement().y, ember.getZ() + ember.getDeltaMovement().z);
            level.addFreshEntity(ember);
        }
    }

    private static void onUsingShockFocusTick(ItemStack wandStack, Level level, Player player, int count) {
        AspectList cost = focusVisCost(wandStack, WandFocusType.SHOCK, level.random);
        if (!hasFocusVis(wandStack, player, WandFocusType.SHOCK, cost)) {
            player.stopUsingItem();
            return;
        }
        HitBundle hit = ray(level, player, 20.0D);
        if (level.isClientSide) {
            float spread = hit.entityHit() != null ? 0.6F : 0.3F;
            TC4ClientFocusFxBridge.sparkleCloud(level, hit.end(), spread, 5, 2);
            TC4ClientFocusFxBridge.shockLightning(level, player, hit.end(), true);
            return;
        }
        beam(level, player.getEyePosition(), hit.end(), Aspect.AER.argbColor(), ParticleTypes.ELECTRIC_SPARK);
        if (!consumeFocusVis(wandStack, player, WandFocusType.SHOCK, cost)) {
            player.stopUsingItem();
            return;
        }
        level.playSound(null, player.getX(), player.getY(), player.getZ(), TC4Sounds.event("shock"), SoundSource.PLAYERS, 0.25F, 1.0F);
        if (hit.entityHit() != null && hit.entityHit().getEntity() instanceof LivingEntity living) {
            int potency = focusUpgradeLevel(wandStack, FocusUpgradeType.POTENCY);
            int chainLevel = focusUpgradeLevel(wandStack, FocusUpgradeType.CHAIN_LIGHTNING);
            living.hurt(DamageSource.LIGHTNING_BOLT, (chainLevel > 0 ? 6.0F : 4.0F) + potency);
            if (chainLevel > 0) {
                chainShock(level, living, chainLevel * 2 + focusUpgradeLevel(wandStack, FocusUpgradeType.ENLARGE) * 2, potency, List.of(living.getId()));
            }
        }
    }

    private static void onUsingExcavationFocusTick(ItemStack wandStack, Level level, Player player, int count) {
        AspectList cost = focusVisCost(wandStack, WandFocusType.EXCAVATION, level.random);
        if (!hasFocusVis(wandStack, player, WandFocusType.EXCAVATION, cost)) {
            player.stopUsingItem();
            return;
        }
        String pp = focusUseKey(level, player);
        HitBundle hit = ray(level, player, 10.0D);
        Vec3 end = hit.end();
        int impact = hit.blockHit() == null ? 0 : 5;
        if (level.isClientSide) {
            TC4ClientFocusFxBridge.beamCont(level, player, end, 2, 65382, false, impact > 0 ? 2.0F : 0.0F, impact);
        } else {
            beam(level, player.getEyePosition(), end, Aspect.TERRA.argbColor(), ParticleTypes.CRIT);
        }
        if (hit.blockHit() == null) {
            EXCAVATION_LAST_BLOCK.put(pp, BlockPos.ZERO);
            EXCAVATION_BREAKCOUNT.put(pp, 0.0F);
            EXCAVATION_SOUND_DELAY.put(pp, 0L);
            return;
        }
        BlockPos target = hit.blockHit().getBlockPos();
        BlockState state = level.getBlockState(target);
        float hardness = state.getDestroySpeed(level, target);
        if (state.isAir() || hardness < 0.0F || !WardedBlockRuntime.mayEdit(level, target, player)) {
            EXCAVATION_LAST_BLOCK.put(pp, BlockPos.ZERO);
            EXCAVATION_BREAKCOUNT.put(pp, 0.0F);
            return;
        }
        long now = System.currentTimeMillis();
        if (!level.isClientSide && EXCAVATION_SOUND_DELAY.getOrDefault(pp, 0L) < now) {
            level.playSound(null, target, TC4Sounds.event("rumble"), SoundSource.BLOCKS, 0.3F, 1.0F);
            EXCAVATION_SOUND_DELAY.put(pp, now + 1200L);
        }
        int potency = focusUpgradeLevel(wandStack, FocusUpgradeType.POTENCY);
        float speed = excavationSpeed(state, potency);
        BlockPos last = EXCAVATION_LAST_BLOCK.getOrDefault(pp, BlockPos.ZERO);
        if (!last.equals(target)) {
            EXCAVATION_LAST_BLOCK.put(pp, target);
            EXCAVATION_BREAKCOUNT.put(pp, 0.0F);
            return;
        }
        float bc = EXCAVATION_BREAKCOUNT.getOrDefault(pp, 0.0F);
        if (level.isClientSide && bc > 0.0F) {
            int progress = Math.min(9, Math.max(0, (int) (bc / hardness * 9.0F)));
            TC4ClientFocusFxBridge.excavateFX(level, target, player, progress);
        }
        if (bc >= hardness) {
            if (!level.isClientSide && consumeFocusVis(wandStack, player, WandFocusType.EXCAVATION, cost)) {
                excavateBlock(level, player, target, wandStack);
            }
            EXCAVATION_LAST_BLOCK.put(pp, BlockPos.ZERO);
            EXCAVATION_BREAKCOUNT.put(pp, 0.0F);
        } else {
            EXCAVATION_BREAKCOUNT.put(pp, bc + speed);
        }
    }

    private static void shootProjectile(TC4FocusProjectileEntity projectile, Player player, float velocity, float inaccuracy) {
        projectile.shootFromRotation(player, player.getXRot(), player.getYRot(), 0.0F, velocity, inaccuracy);
        projectile.setPos(player.getX(), player.getEyeY() - 0.1D, player.getZ());
    }

    private static boolean excavateBlock(Level level, Player player, BlockPos target, ItemStack wandStack) {
        BlockState state = level.getBlockState(target);
        Block block = state.getBlock();
        float hardness = state.getDestroySpeed(level, target);
        if (state.isAir() || hardness < 0.0F || hardness > 20.0F || !WardedBlockRuntime.mayEdit(level, target, player)) {
            return false;
        }
        int treasure = focusUpgradeLevel(wandStack, FocusUpgradeType.TREASURE);
        boolean silkTouch = focusHasUpgrade(wandStack, FocusUpgradeType.SILK_TOUCH);
        if (silkTouch || treasure > 0) {
            // Original ItemFocusExcavation passes silkTouch/fortune into BlockUtils.harvestBlock.
            // Forge 1.19.2 adapter: use the vanilla loot-context path with an enchanted tool stack, then remove the block without duplicate drops.
            ItemStack lootTool = new ItemStack(Items.NETHERITE_PICKAXE);
            if (silkTouch) {
                EnchantmentHelper.setEnchantments(java.util.Map.of(Enchantments.SILK_TOUCH, 1), lootTool);
            } else {
                EnchantmentHelper.setEnchantments(java.util.Map.of(Enchantments.BLOCK_FORTUNE, treasure), lootTool);
            }
            Block.dropResources(state, level, target, state.hasBlockEntity() ? level.getBlockEntity(target) : null, player, lootTool);
            level.levelEvent(null, 2001, target, Block.getId(state));
            level.destroyBlock(target, false, player);
        } else {
            level.destroyBlock(target, true, player);
        }
        int enlarge = focusUpgradeLevel(wandStack, FocusUpgradeType.ENLARGE);
        for (int i = 0; i < enlarge; i++) {
            BlockPos n = matchingNeighbour(level, target, block);
            if (n == null || !consumeFocusVis(wandStack, player, WandFocusType.EXCAVATION, focusVisCost(wandStack, WandFocusType.EXCAVATION, level.random))) {
                break;
            }
            excavateBlock(level, player, n, wandStack);
        }
        return true;
    }

    private static BlockPos matchingNeighbour(Level level, BlockPos origin, Block block) {
        List<Direction> directions = new ArrayList<>(List.of(Direction.DOWN, Direction.UP, Direction.NORTH, Direction.SOUTH, Direction.EAST, Direction.WEST));
        java.util.Collections.shuffle(directions, new java.util.Random(level.random.nextLong()));
        for (Direction dir : directions) {
            BlockPos p = origin.relative(dir);
            if (level.getBlockState(p).getBlock() == block) {
                return p;
            }
        }
        return null;
    }

    private static void storePickedBlock(ItemStack wandStack, ItemStack picked) {
        if (picked.isEmpty()) {
            wandStack.getOrCreateTag().remove(FocusArchitectRuntime.TAG_PICKED_BLOCK);
            return;
        }
        ItemStack one = picked.copy();
        one.setCount(1);
        wandStack.getOrCreateTag().put(FocusArchitectRuntime.TAG_PICKED_BLOCK, one.save(new CompoundTag()));
    }

    private static ItemStack getPickedBlock(ItemStack wandStack) {
        CompoundTag tag = wandStack.getTag();
        if (tag != null && tag.contains(FocusArchitectRuntime.TAG_PICKED_BLOCK, 10)) {
            ItemStack picked = ItemStack.of(tag.getCompound(FocusArchitectRuntime.TAG_PICKED_BLOCK));
            if (!picked.isEmpty()) {
                return picked;
            }
        }
        return ItemStack.EMPTY;
    }

    private static boolean replaceBlockWithEqualTrade(Level level, Player player, BlockPos target, BlockItem blockItem, ItemStack picked, ItemStack wandStack) {
        BlockState old = level.getBlockState(target);
        if (old.isAir() || old.hasBlockEntity() || old.getDestroySpeed(level, target) < 0.0F || old.getBlock() == blockItem.getBlock() || !WardedBlockRuntime.mayEdit(level, target, player)) {
            return false;
        }
        if (!player.getAbilities().instabuild && !consumePickedBlock(player, picked)) {
            return false;
        }
        int treasure = focusUpgradeLevel(wandStack, FocusUpgradeType.TREASURE);
        boolean silkTouch = focusHasUpgrade(wandStack, FocusUpgradeType.SILK_TOUCH);
        if (silkTouch || treasure > 0) {
            ItemStack lootTool = new ItemStack(Items.NETHERITE_PICKAXE);
            if (silkTouch) {
                EnchantmentHelper.setEnchantments(java.util.Map.of(Enchantments.SILK_TOUCH, 1), lootTool);
            } else {
                EnchantmentHelper.setEnchantments(java.util.Map.of(Enchantments.BLOCK_FORTUNE, treasure), lootTool);
            }
            Block.dropResources(old, level, target, old.hasBlockEntity() ? level.getBlockEntity(target) : null, player, lootTool);
            level.levelEvent(null, 2001, target, Block.getId(old));
            level.destroyBlock(target, false, player);
        } else {
            level.destroyBlock(target, true, player);
        }
        level.setBlock(target, blockItem.getBlock().defaultBlockState(), 3);
        sparkleBlock(level, target, Aspect.PERMUTATIO.argbColor());
        return true;
    }

    private static boolean consumePickedBlock(Player player, ItemStack picked) {
        if (!picked.isEmpty() && picked == player.getOffhandItem()) {
            picked.shrink(1);
            return true;
        }
        for (int slot = 0; slot < player.getInventory().getContainerSize(); slot++) {
            ItemStack stack = player.getInventory().getItem(slot);
            if (!stack.isEmpty() && ItemStack.isSameItemSameTags(stack, picked)) {
                stack.shrink(1);
                return true;
            }
        }
        return false;
    }

    private static void sparkleBlock(Level level, BlockPos pos, int color) {
        if (level instanceof ServerLevel server) {
            double r = ((color >> 16) & 255) / 255.0D;
            double g = ((color >> 8) & 255) / 255.0D;
            double b = (color & 255) / 255.0D;
            server.sendParticles(ParticleTypes.ENCHANT, pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D, 8, r * 0.1D + 0.02D, g * 0.1D + 0.02D, b * 0.1D + 0.02D, 0.02D);
        }
    }

    private static boolean hasFocusVis(ItemStack wandStack, Player player, WandFocusType type, AspectList cost) {
        if (player.getAbilities().instabuild || WandItem.hasInfiniteVis(wandStack)) {
            return true;
        }
        for (var entry : cost.entries().entrySet()) {
            if (WandItem.getVis(wandStack, entry.getKey()) < focusModifiedVisCost(wandStack, entry.getKey(), entry.getValue())) {
                return false;
            }
        }
        return true;
    }

    private static String focusUseKey(Level level, Player player) {
        return (level.isClientSide ? "R" : "S") + player.getGameProfile().getName();
    }

    private static float excavationSpeed(BlockState state, int potency) {
        float speed = 0.05F + potency * 0.1F;
        if (state.getMaterial() == net.minecraft.world.level.material.Material.STONE
                || state.getMaterial() == net.minecraft.world.level.material.Material.METAL
                || state.getMaterial() == net.minecraft.world.level.material.Material.GLASS) {
            speed = 0.25F + potency * 0.25F;
        }
        if (state.is(Blocks.OBSIDIAN)) {
            speed *= 3.0F;
        }
        return speed;
    }

    private static AspectList scaledCost(AspectList cost, int multiplier) {
        AspectList out = new AspectList();
        for (var entry : cost.entries().entrySet()) {
            out.add(entry.getKey(), entry.getValue() * multiplier);
        }
        return out;
    }

    private static void chainShock(Level level, LivingEntity source, int remaining, int potency, List<Integer> alreadyHit) {
        if (remaining <= 0 || level.isClientSide) {
            return;
        }
        AABB area = source.getBoundingBox().inflate(7.0D);
        LivingEntity closest = null;
        double distance = Double.MAX_VALUE;
        for (LivingEntity nearby : level.getEntitiesOfClass(LivingEntity.class, area, e -> e.isAlive() && !alreadyHit.contains(e.getId()))) {
            double d = source.distanceToSqr(nearby);
            if (d < distance) {
                closest = nearby;
                distance = d;
            }
        }
        if (closest != null) {
            closest.hurt(DamageSource.LIGHTNING_BOLT, 4.0F + potency);
            if (level instanceof ServerLevel server) {
                beam(server, source.getEyePosition(), closest.getEyePosition(), Aspect.AER.argbColor(), ParticleTypes.ELECTRIC_SPARK);
            }
            List<Integer> next = new ArrayList<>(alreadyHit);
            next.add(closest.getId());
            chainShock(level, closest, remaining - 1, potency, next);
        }
    }

    private static AspectList primalCost(RandomSource random) {
        // Stage172 compatibility token: 50 + random.nextInt(5) * 50
        // Original ItemFocusPrimal uses new Random(System.currentTimeMillis() / 200L),
        // so all six primal costs are stable inside the same 200 ms window.
        java.util.Random tc4Random = new java.util.Random(System.currentTimeMillis() / 200L);
        AspectList cost = new AspectList();
        cost.add(Aspect.AQUA, 50 + tc4Random.nextInt(5) * 50);
        cost.add(Aspect.AER, 50 + tc4Random.nextInt(5) * 50);
        cost.add(Aspect.TERRA, 50 + tc4Random.nextInt(5) * 50);
        cost.add(Aspect.IGNIS, 50 + tc4Random.nextInt(5) * 50);
        cost.add(Aspect.ORDO, 50 + tc4Random.nextInt(5) * 50);
        cost.add(Aspect.PERDITIO, 50 + tc4Random.nextInt(5) * 50);
        return cost;
    }

    private static HitBundle ray(Level level, Player player, double distance) {
        Vec3 start = player.getEyePosition();
        Vec3 end = start.add(player.getViewVector(1.0F).scale(distance));
        BlockHitResult blockHit = level.clip(new ClipContext(start, end, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, player));
        Vec3 blockEnd = blockHit.getType() == HitResult.Type.MISS ? end : blockHit.getLocation();
        Predicate<Entity> predicate = entity -> entity.isAlive() && entity.isPickable() && entity != player;
        EntityHitResult entityHit = ProjectileUtil.getEntityHitResult(level, player, start, blockEnd, new AABB(start.x, start.y, start.z, blockEnd.x, blockEnd.y, blockEnd.z).inflate(1.0D), predicate);
        Vec3 finalEnd = entityHit == null ? blockEnd : entityHit.getLocation();
        return new HitBundle(blockHit.getType() == HitResult.Type.MISS ? null : blockHit, entityHit, finalEnd);
    }

    private static void beam(Level level, Vec3 start, Vec3 end, int color, ParticleOptions particle) {
        if (!(level instanceof ServerLevel server)) {
            return;
        }
        Vec3 delta = end.subtract(start);
        int steps = Math.max(4, (int) (delta.length() * 6.0D));
        double r = ((color >> 16) & 255) / 255.0D;
        double g = ((color >> 8) & 255) / 255.0D;
        double b = (color & 255) / 255.0D;
        for (int i = 0; i <= steps; i++) {
            Vec3 p = start.add(delta.scale(i / (double) steps));
            server.sendParticles(particle, p.x, p.y, p.z, 1, 0.01D, 0.01D, 0.01D, 0.0D);
            if (i % 3 == 0) {
                server.sendParticles(ParticleTypes.ENCHANT, p.x, p.y, p.z, 1, r * 0.03D, g * 0.03D, b * 0.03D, 0.0D);
            }
        }
    }

    private static net.minecraft.sounds.SoundEvent soundFor(WandFocusType type) {
        return switch (type) {
            case FIRE -> TC4Sounds.event("fireloop");
            case FROST -> TC4Sounds.event("ice");
            case SHOCK -> TC4Sounds.event("shock");
            case EXCAVATION -> TC4Sounds.event("tool");
            case PORTABLE_HOLE -> TC4Sounds.event("wind");
            case EQUAL_TRADE -> TC4Sounds.event("wand");
            case WARDING -> TC4Sounds.event("runicShieldEffect");
            case PRIMAL -> TC4Sounds.event("zap");
        };
    }

    private static float pitchFor(WandFocusType type) {
        return type == WandFocusType.FROST ? 0.75F : type == WandFocusType.SHOCK ? 1.25F : 1.0F;
    }

    private record HitBundle(BlockHitResult blockHit, EntityHitResult entityHit, Vec3 end) {
    }
}
