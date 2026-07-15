package com.darkifov.thaumcraft.wand;

import com.darkifov.thaumcraft.Aspect;
import com.darkifov.thaumcraft.AspectList;
import com.darkifov.thaumcraft.ThaumcraftMod;
import com.darkifov.thaumcraft.block.WandItem;
import com.darkifov.thaumcraft.porting.TC4Sounds;
import com.darkifov.thaumcraft.porting.TC4ResearchItems;
import com.darkifov.thaumcraft.porting.TC4ClientFocusFxBridge;
import com.darkifov.thaumcraft.entity.projectile.TC4EmberEntity;
import com.darkifov.thaumcraft.entity.projectile.TC4ExplosiveOrbEntity;
import com.darkifov.thaumcraft.entity.projectile.TC4FocusProjectileEntity;
import com.darkifov.thaumcraft.entity.projectile.TC4FrostShardEntity;
import com.darkifov.thaumcraft.entity.projectile.TC4PrimalOrbEntity;
import com.darkifov.thaumcraft.entity.projectile.TC4PechBlastEntity;
import com.darkifov.thaumcraft.entity.TC4FireBatEntity;
import com.darkifov.thaumcraft.entity.projectile.TC4ShockOrbEntity;
import com.darkifov.thaumcraft.eldritch.TC4OuterLandsDimensionAdapter;
import com.darkifov.thaumcraft.ward.WardedBlockRuntime;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ExperienceOrb;
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
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.level.BlockEvent;
import net.minecraftforge.registries.ForgeRegistries;

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
    private static final Map<String, Long> FOCUS_NEXT_CAST_TICK = new HashMap<>();

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
            int needed = modifiedFocusVisCost(wandStack, player, entry.getKey(), entry.getValue());
            if (WandItem.getVis(wandStack, entry.getKey()) < needed) {
                player.displayClientMessage(Component.literal("Not enough " + entry.getKey().displayName() + " vis for " + type.displayName() + ": need " + WandItem.formatVis(needed) + " after cap modifier.").withStyle(ChatFormatting.RED), true);
                return false;
            }
        }
        for (var entry : cost.entries().entrySet()) {
            WandItem.consumeVis(wandStack, entry.getKey(), modifiedFocusVisCost(wandStack, player, entry.getKey(), entry.getValue()));
        }
        return true;
    }

    public static InteractionResult cast(ItemStack wandStack, Level level, Player player) {
        WandFocusType type = getFocus(wandStack);
        if (type == null) {
            return InteractionResult.PASS;
        }

        // These foci price their actual successful server operations internally.
        // Charging here caused free secondary operations, paid failures and paid ward removal.
        boolean internallyPriced = type == WandFocusType.EXCAVATION
                || type == WandFocusType.PORTABLE_HOLE
                || type == WandFocusType.EQUAL_TRADE
                || type == WandFocusType.WARDING
                || type == WandFocusType.HELLBAT;
        if (!internallyPriced) {
            AspectList resolvedCost = focusVisCost(wandStack, type, level.random);
            if (!consumeFocusVis(wandStack, player, type, resolvedCost)) {
                level.playSound(null, player.blockPosition(), TC4Sounds.event("wandfail"), SoundSource.PLAYERS, 0.55F, 1.0F);
                return InteractionResult.CONSUME;
            }
        }

        boolean success = switch (type) {
            case FIRE -> castFire(wandStack, level, player);
            case FROST -> castFrost(wandStack, level, player);
            case SHOCK -> castShock(wandStack, level, player);
            case EXCAVATION -> castExcavation(wandStack, level, player);
            case PORTABLE_HOLE -> castPortableHole(wandStack, level, player);
            case EQUAL_TRADE -> castEqualTrade(wandStack, level, player);
            case WARDING -> castWarding(wandStack, level, player);
            case HELLBAT -> castHellbat(wandStack, level, player);
            case PECH_CURSE -> castPechCurse(wandStack, level, player);
            case PRIMAL -> castPrimal(wandStack, level, player);
        };

        int cooldown = activationCooldown(wandStack, type);
        if (success && cooldown > 0) {
            player.getCooldowns().addCooldown(wandStack.getItem(), cooldown);
        }
        if (!level.isClientSide && success && !playsOwnActivationSound(wandStack, type)) {
            level.playSound(null, player.blockPosition(), soundFor(type), SoundSource.PLAYERS, 0.65F, pitchFor(type));
        }
        return InteractionResult.CONSUME;
    }

    private static boolean playsOwnActivationSound(ItemStack wandStack, WandFocusType type) {
        return switch (type) {
            case FROST, PORTABLE_HOLE, WARDING, HELLBAT, PECH_CURSE, PRIMAL -> true;
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
        int cooldown = activationCooldown(wandStack, type);
        if (cooldown > 0) {
            String cooldownKey = focusUseKey(level, player) + ":" + type.id() + ":" + (level.isClientSide ? "client" : "server");
            long gameTime = level.getGameTime();
            if (FOCUS_NEXT_CAST_TICK.getOrDefault(cooldownKey, Long.MIN_VALUE) > gameTime) {
                return;
            }
            FOCUS_NEXT_CAST_TICK.put(cooldownKey, gameTime + cooldown);
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
            clearExcavationUse(level, player);
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
            case HELLBAT -> focusHasUpgrade(wandStack, FocusUpgradeType.DEVIL_BATS)
                    ? cost(Aspect.IGNIS, 100, Aspect.PERDITIO, 100, Aspect.AER, 100, Aspect.TERRA, 100)
                    : focusHasUpgrade(wandStack, FocusUpgradeType.BAT_BOMBS)
                    ? cost(Aspect.IGNIS, 100, Aspect.PERDITIO, 200, Aspect.AER, 100)
                    : cost(Aspect.IGNIS, 200, Aspect.PERDITIO, 100, Aspect.AER, 100);
            case PECH_CURSE -> focusHasUpgrade(wandStack, FocusUpgradeType.NIGHTSHADE)
                    ? cost(Aspect.AER, 10, Aspect.IGNIS, 10, Aspect.TERRA, 10, Aspect.ORDO, 10, Aspect.PERDITIO, 10, Aspect.AQUA, 10)
                    : cost(Aspect.TERRA, 10, Aspect.PERDITIO, 10, Aspect.AQUA, 10);
            case PRIMAL -> primalCost(random);
        };
        return cost;
    }

    public static int activationCooldown(ItemStack wandStack, WandFocusType type) {
        return switch (type) {
            case FIRE -> focusHasUpgrade(wandStack, FocusUpgradeType.FIREBALL) ? 20 : 0;
            case FROST -> focusHasUpgrade(wandStack, FocusUpgradeType.SCATTERSHOT) || focusHasUpgrade(wandStack, FocusUpgradeType.ICE_BOULDER) ? 10 : 4;
            case SHOCK -> focusHasUpgrade(wandStack, FocusUpgradeType.EARTH_SHOCK) ? 20 : focusHasUpgrade(wandStack, FocusUpgradeType.CHAIN_LIGHTNING) ? 10 : 5;
            case HELLBAT -> 20;
            case PECH_CURSE -> 5;
            case PRIMAL -> 10;
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

    public static int modifiedFocusVisCost(ItemStack wandStack, Aspect aspect, int baseAmount) {
        return modifiedFocusVisCost(wandStack, null, aspect, baseAmount);
    }

    public static int modifiedFocusVisCost(ItemStack wandStack, Player player, Aspect aspect, int baseAmount) {
        return WandItem.modifiedVisCost(wandStack, player, aspect, baseAmount, false);
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
            case HELLBAT -> TC4ResearchItems.registered("tc4_focus_hellbat").map(o -> new ItemStack(o.get())).orElse(ItemStack.EMPTY);
            case PECH_CURSE -> TC4ResearchItems.registered("tc4_focus_pech").map(o -> new ItemStack(o.get())).orElse(ItemStack.EMPTY);
            case PRIMAL -> new ItemStack(ThaumcraftMod.FOCUS_PRIMAL.get());
        };
    }

    private static boolean castFire(ItemStack wandStack, Level level, Player player) {
        if (!focusHasUpgrade(wandStack, FocusUpgradeType.FIREBALL)) {
            // Base fire and Firebeam are continuous-use effects and are handled by
            // onUsingFireFocusTick, matching ItemFocusFire#onFocusRightClick.
            return false;
        }
        if (!level.isClientSide) {
            TC4ExplosiveOrbEntity orb = new TC4ExplosiveOrbEntity(ThaumcraftMod.FOCUS_EXPLOSIVE_ORB.get(), level, player);
            orb.setStrength(1.0F + focusPotency(wandStack) * 0.4F);
            orb.setAlchemistsFire(focusHasUpgrade(wandStack, FocusUpgradeType.ALCHEMISTS_FIRE));
            shootProjectile(orb, player, 1.5F, 1.0F);
            level.addFreshEntity(orb);
            level.levelEvent(null, 1009, player.blockPosition(), 0);
        }
        return true;
    }

    /** Original ItemWandCasting#getFocusPotency includes one free potency for runed primal staffs. */
    private static int focusPotency(ItemStack wandStack) {
        int potency = focusUpgradeLevel(wandStack, FocusUpgradeType.POTENCY);
        if (WandComponentData.from(wandStack).hasRunes()) potency++;
        return potency;
    }

    private static boolean castFrost(ItemStack wandStack, Level level, Player player) {
        int potency = focusPotency(wandStack);
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
                    shootProjectile(shard, player, 1.5F, 8.0F);
                    level.addFreshEntity(shard);
                    soundSource = shard;
                }
            } else {
                TC4FrostShardEntity shard = new TC4FrostShardEntity(ThaumcraftMod.FOCUS_FROST_SHARD.get(), level, player);
                if (focusHasUpgrade(wandStack, FocusUpgradeType.ICE_BOULDER)) {
                    shard.setDamage(4.0F + potency * 2.0F);
                    shard.setBounce(0.8D, 6);
                    shootProjectile(shard, player, 1.5F, 1.0F);
                } else {
                    shard.setDamage((float) (3.0D + potency * 1.5D));
                    shootProjectile(shard, player, 1.5F, 1.0F);
                }
                shard.setFrosty(frosty);
                level.addFreshEntity(shard);
                soundSource = shard;
            }
            level.playSound(null, soundSource.getX(), soundSource.getY(), soundSource.getZ(), TC4Sounds.event("ice"), SoundSource.PLAYERS, 0.4F, 1.0F + level.random.nextFloat() * 0.1F);
        }
        InteractionHand hand = player.getOffhandItem() == wandStack ? InteractionHand.OFF_HAND : InteractionHand.MAIN_HAND;
        player.swing(hand, true);
        return true;
    }

    private static boolean castShock(ItemStack wandStack, Level level, Player player) {
        int potency = focusPotency(wandStack);
        if (focusHasUpgrade(wandStack, FocusUpgradeType.EARTH_SHOCK)) {
            if (!level.isClientSide) {
                TC4ShockOrbEntity orb = new TC4ShockOrbEntity(ThaumcraftMod.FOCUS_SHOCK_ORB.get(), level, player);
                orb.setArea(4.0F + focusUpgradeLevel(wandStack, FocusUpgradeType.ENLARGE) * 2.0F);
                orb.setDamage((int) (5 + potency * 1.33D));
                shootProjectile(orb, player, 1.5F, 1.0F);
                level.addFreshEntity(orb);
                level.playSound(null, player.blockPosition(), TC4Sounds.event("zap"), SoundSource.PLAYERS, 1.0F, 1.0F + (level.random.nextFloat() - level.random.nextFloat()) * 0.2F);
            }
            return true;
        }
        HitBundle hit = ray(level, player, 20.0D);
        beam(level, player.getEyePosition(), hit.end(), Aspect.AER.argbColor(), ParticleTypes.ELECTRIC_SPARK);
        if (!level.isClientSide && hit.entityHit() != null && hit.entityHit().getEntity() instanceof LivingEntity living) {
            int chainLevel = focusUpgradeLevel(wandStack, FocusUpgradeType.CHAIN_LIGHTNING);
            if (!canShockTarget(level, player, living)) {
                return true;
            }
            living.hurt(DamageSource.playerAttack(player), (chainLevel > 0 ? 6.0F : 4.0F) + potency);
            if (chainLevel > 0) {
                chainShock(level, player, living, chainLevel * 2 + focusUpgradeLevel(wandStack, FocusUpgradeType.ENLARGE) * 2, potency, List.of(living.getId()));
            }
            return true;
        }
        return true;
    }

    private static boolean castExcavation(ItemStack wandStack, Level level, Player player) {
        HitBundle hit = ray(level, player, 8.0D);
        beam(level, player.getEyePosition(), hit.end(), Aspect.TERRA.argbColor(), ParticleTypes.CRIT);
        if (!level.isClientSide && hit.blockHit() != null) {
            BlockPos target = hit.blockHit().getBlockPos();
            return excavateBlock(level, player, target, wandStack);
        }
        return true;
    }

    private static boolean castPortableHole(ItemStack wandStack, Level level, Player player) {
        HitBundle hit = ray(level, player, 16.0D);
        beam(level, player.getEyePosition(), hit.end(), Aspect.PERDITIO.argbColor(), ParticleTypes.PORTAL);
        if (level.isClientSide || hit.blockHit() == null || !(level instanceof ServerLevel server)) {
            return true;
        }
        Direction face = hit.blockHit().getDirection();
        BlockPos origin = hit.blockHit().getBlockPos();
        if (TC4OuterLandsDimensionAdapter.isOuterLands(level.dimension())) {
            level.playSound(null, origin, TC4Sounds.event("wandfail"), SoundSource.BLOCKS, 1.0F, 1.0F);
            return false;
        }
        int maxDistance = 33 + focusUpgradeLevel(wandStack, FocusUpgradeType.ENLARGE) * 8;
        int distance = 0;
        for (; distance < maxDistance; distance++) {
            BlockPos cursor = origin.relative(face.getOpposite(), distance);
            if (!com.darkifov.thaumcraft.block.TemporaryHoleBlock.canReplace(level, cursor, level.getBlockState(cursor), player)) {
                break;
            }
        }
        if (distance <= 0) {
            level.playSound(null, origin, TC4Sounds.event("wandfail"), SoundSource.BLOCKS, 0.7F, 1.0F);
            return false;
        }
        AspectList totalCost = scaledCost(focusVisCost(wandStack, WandFocusType.PORTABLE_HOLE, level.random), distance);
        if (!hasFocusVis(wandStack, player, WandFocusType.PORTABLE_HOLE, totalCost)) {
            level.playSound(null, origin, TC4Sounds.event("wandfail"), SoundSource.BLOCKS, 0.7F, 1.0F);
            return false;
        }
        int duration = 120 + focusUpgradeLevel(wandStack, FocusUpgradeType.EXTEND) * 60;
        BlockState rememberedOrigin = level.getBlockState(origin);
        boolean opened = com.darkifov.thaumcraft.block.TemporaryHoleBlock.createHole(
                server, origin, duration, distance + 1, face, player);
        if (!opened) {
            level.playSound(null, origin, TC4Sounds.event("wandfail"), SoundSource.BLOCKS, 0.7F, 1.0F);
            return false;
        }
        if (!consumeFocusVis(wandStack, player, WandFocusType.PORTABLE_HOLE, totalCost)) {
            // A protection event is allowed to mutate player inventory. Keep the
            // operation atomic if that invalidates the vis check after creation.
            level.setBlock(origin, rememberedOrigin, Block.UPDATE_ALL);
            level.playSound(null, origin, TC4Sounds.event("wandfail"), SoundSource.BLOCKS, 0.7F, 1.0F);
            return false;
        }
        level.playSound(null, origin, SoundEvents.ENDERMAN_TELEPORT, SoundSource.BLOCKS, 1.0F, 1.0F);
        return true;
    }

    private static boolean castEqualTrade(ItemStack wandStack, Level level, Player player) {
        HitBundle hit = ray(level, player, equalTradeReach(player));
        beam(level, player.getEyePosition(), hit.end(), 0x857B93, ParticleTypes.ENCHANT); // TC4 ItemFocusTrade#getFocusColor = 8747923
        if (level.isClientSide || hit.blockHit() == null || !(level instanceof ServerLevel server)
                || !(player instanceof ServerPlayer serverPlayer)) {
            return true;
        }
        BlockPos origin = hit.blockHit().getBlockPos();
        BlockState source = level.getBlockState(origin);
        if (player.isShiftKeyDown()) {
            if (source.isAir() || source.hasBlockEntity() || source.getBlock().asItem() == Items.AIR) {
                player.displayClientMessage(Component.translatable("message.thaumcraft.equal_trade.pick_failed").withStyle(ChatFormatting.GRAY), true);
                return false;
            }
            ItemStack picked = source.getBlock().asItem().getDefaultInstance();
            storePickedBlock(wandStack, picked, source);
            player.displayClientMessage(Component.translatable("message.thaumcraft.equal_trade.picked", source.getBlock().getName()).withStyle(ChatFormatting.GRAY), true);
            return true;
        }

        ItemStack picked = getPickedBlock(wandStack);
        BlockState targetState = getPickedState(wandStack);
        if (picked.isEmpty() || !(picked.getItem() instanceof BlockItem) || targetState.isAir()) {
            player.displayClientMessage(Component.translatable("message.thaumcraft.equal_trade.needs_pick").withStyle(ChatFormatting.GRAY), true);
            return false;
        }
        if (source.isAir() || source.hasBlockEntity() || source.getDestroySpeed(level, origin) < 0.0F
                || source.equals(targetState) || WardedBlockRuntime.isWarded(level, origin)) {
            return false;
        }

        int slot = wandInventorySlot(player, wandStack);
        if (focusHasUpgrade(wandStack, FocusUpgradeType.ARCHITECT)) {
            List<BlockPos> targets = FocusArchitectRuntime.equalTradeArchitectBlocks(wandStack, level, hit.blockHit(), player);
            for (BlockPos target : targets) {
                EqualTradeSwapRuntime.enqueue(server, serverPlayer, slot, target, source, targetState, picked, 0);
            }
            if (!targets.isEmpty()) {
                level.playSound(null, origin, TC4Sounds.event("wand"), SoundSource.PLAYERS, 0.25F, 1.0F);
            }
            return !targets.isEmpty();
        }
        EqualTradeSwapRuntime.enqueue(server, serverPlayer, slot, origin, source, targetState, picked,
                3 + focusUpgradeLevel(wandStack, FocusUpgradeType.ENLARGE));
        level.playSound(null, origin, TC4Sounds.event("wand"), SoundSource.PLAYERS, 0.25F, 1.0F);
        return true;
    }

    /** Original ItemFocusTrade#onEntitySwing: one queued replacement on left-click. */
    public static boolean queueEqualTradeSwing(ServerPlayer player, InteractionHand hand, BlockPos target) {
        ItemStack wandStack = player.getItemInHand(hand);
        if (!(wandStack.getItem() instanceof WandItem) || getFocus(wandStack) != WandFocusType.EQUAL_TRADE) return false;
        ItemStack picked = getPickedBlock(wandStack);
        BlockState targetState = getPickedState(wandStack);
        BlockState source = player.level.getBlockState(target);
        if (picked.isEmpty() || targetState.isAir() || source.isAir() || source.hasBlockEntity()
                || source.getDestroySpeed(player.level, target) < 0.0F || source.equals(targetState)
                || WardedBlockRuntime.isWarded(player.level, target)) return false;
        int slot = hand == InteractionHand.OFF_HAND ? -1 : player.getInventory().selected;
        EqualTradeSwapRuntime.enqueue((ServerLevel) player.level, player, slot, target, source, targetState, picked, 0);
        player.level.playSound(null, target, TC4Sounds.event("wand"), SoundSource.PLAYERS, 0.25F, 1.0F);
        return true;
    }

    private static boolean castWarding(ItemStack wandStack, Level level, Player player) {
        HitBundle hit = ray(level, player, 10.0D);
        beam(level, player.getEyePosition(), hit.end(), 0xFFE9CF, ParticleTypes.ENCHANT); // TC4 ItemFocusWarding#getFocusColor = 16771535
        if (level.isClientSide || hit.blockHit() == null) return true;

        BlockPos origin = hit.blockHit().getBlockPos();
        String wardKey = origin.getX() + ":" + origin.getY() + ":" + origin.getZ() + ":" + level.dimension().location();
        long now = System.currentTimeMillis();
        if (WARDING_DELAY.size() > 4096) {
            WARDING_DELAY.entrySet().removeIf(entry -> entry.getValue() <= now);
        }
        if (WARDING_DELAY.getOrDefault(wardKey, 0L) > now) return true;
        WARDING_DELAY.put(wardKey, now + 500L);

        boolean removing = WardedBlockRuntime.isWarded(level, origin);
        List<BlockPos> targets = FocusArchitectRuntime.wardingArchitectBlocks(wandStack, level, hit.blockHit(), player, removing);
        int changed = 0;
        for (BlockPos target : targets) {
            if (removing) {
                // Original removal is free and only succeeds for the ward owner.
                if (WardedBlockRuntime.unward(level, target, player)) {
                    changed++;
                    sparkleBlock(level, target, 0xFCA000); // TC4 PacketFXBlockSparkle = 16556032
                }
                continue;
            }
            AspectList cost = focusVisCost(wandStack, WandFocusType.WARDING, level.random);
            if (!hasFocusVis(wandStack, player, WandFocusType.WARDING, cost)) break;
            if (WardedBlockRuntime.ward(level, target, player, wandStack)) {
                if (!consumeFocusVis(wandStack, player, WandFocusType.WARDING, cost)) {
                    WardedBlockRuntime.rollbackWard(level, target, player);
                    break;
                }
                changed++;
                sparkleBlock(level, target, 0xFCA000); // TC4 PacketFXBlockSparkle = 16556032
            }
        }
        if (changed > 0) {
            level.playSound(null, origin, TC4Sounds.event("zap"), SoundSource.BLOCKS, 0.25F, 1.0F);
            player.displayClientMessage(Component.translatable(removing
                            ? "message.thaumcraft.warding.removed"
                            : "message.thaumcraft.warding.bound", changed)
                    .withStyle(ChatFormatting.BLUE), true);
            return true;
        }
        if (removing) {
            String message = WardedBlockRuntime.isOwner(level, origin, player)
                    ? "message.thaumcraft.warding.release_failed"
                    : "message.thaumcraft.warding.owned_other";
            player.displayClientMessage(Component.translatable(message).withStyle(ChatFormatting.RED), true);
        }
        return false;
    }

    private static boolean castHellbat(ItemStack wandStack, Level level, Player player) {
        HitBundle hit = ray(level, player, 32.0D);
        if (!(hit.entityHit() != null && hit.entityHit().getEntity() instanceof LivingEntity target)
                || target instanceof TC4FireBatEntity) {
            return false;
        }
        if (target instanceof Player && (level.getServer() == null || !level.getServer().isPvpAllowed())) {
            return false;
        }
        if (level.isClientSide) {
            player.swing(player.getOffhandItem() == wandStack ? InteractionHand.OFF_HAND : InteractionHand.MAIN_HAND, true);
            return true;
        }
        AspectList cost = focusVisCost(wandStack, WandFocusType.HELLBAT, level.random);
        if (!hasFocusVis(wandStack, player, WandFocusType.HELLBAT, cost)
                || !consumeFocusVis(wandStack, player, WandFocusType.HELLBAT, cost)) {
            level.playSound(null, player.blockPosition(), TC4Sounds.event("wandfail"), SoundSource.PLAYERS, 0.1F, 0.8F + level.random.nextFloat() * 0.1F);
            return false;
        }
        TC4FireBatEntity firebat = ThaumcraftMod.FIREBAT.get().create(level);
        if (firebat == null) return false;
        Vec3 look = player.getViewVector(1.0F);
        Vec3 spawn = player.getEyePosition().add(look.scale(0.5D)).add(0.0D, 0.25D, 0.0D);
        firebat.moveTo(spawn.x, spawn.y, spawn.z, player.getYRot(), 0.0F);
        firebat.configure(player, target, focusPotency(wandStack),
                focusHasUpgrade(wandStack, FocusUpgradeType.BAT_BOMBS),
                focusHasUpgrade(wandStack, FocusUpgradeType.DEVIL_BATS),
                focusHasUpgrade(wandStack, FocusUpgradeType.VAMPIRE_BATS));
        if (!level.addFreshEntity(firebat)) return false;
        level.levelEvent(null, 2004, firebat.blockPosition(), 0);
        level.playSound(null, firebat.getX(), firebat.getY(), firebat.getZ(), TC4Sounds.event("ice"), SoundSource.PLAYERS, 0.2F, 0.95F + level.random.nextFloat() * 0.1F);
        player.swing(player.getOffhandItem() == wandStack ? InteractionHand.OFF_HAND : InteractionHand.MAIN_HAND, true);
        return true;
    }

    private static boolean castPechCurse(ItemStack wandStack, Level level, Player player) {
        if (!level.isClientSide) {
            TC4PechBlastEntity blast = new TC4PechBlastEntity(ThaumcraftMod.FOCUS_PECH_BLAST.get(), level, player);
            blast.configure(focusPotency(wandStack), focusUpgradeLevel(wandStack, FocusUpgradeType.EXTEND),
                    focusHasUpgrade(wandStack, FocusUpgradeType.NIGHTSHADE));
            shootProjectile(blast, player, 1.5F, 1.0F);
            level.addFreshEntity(blast);
            level.playSound(null, blast.getX(), blast.getY(), blast.getZ(), TC4Sounds.event("ice"), SoundSource.PLAYERS, 0.4F, 1.0F + level.random.nextFloat() * 0.1F);
        }
        player.swing(player.getOffhandItem() == wandStack ? InteractionHand.OFF_HAND : InteractionHand.MAIN_HAND, true);
        return true;
    }

    private static boolean castPrimal(ItemStack wandStack, Level level, Player player) {
        if (!level.isClientSide) {
            TC4PrimalOrbEntity orb = new TC4PrimalOrbEntity(ThaumcraftMod.FOCUS_PRIMAL_ORB.get(), level, player);
            orb.setSeeker(focusHasUpgrade(wandStack, FocusUpgradeType.SEEKER));
            shootProjectile(orb, player, 0.5F, 1.0F);
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
        int potency = focusPotency(wandStack);
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
            int potency = focusPotency(wandStack);
            int chainLevel = focusUpgradeLevel(wandStack, FocusUpgradeType.CHAIN_LIGHTNING);
            if (!canShockTarget(level, player, living)) {
                return;
            }
            living.hurt(DamageSource.playerAttack(player), (chainLevel > 0 ? 6.0F : 4.0F) + potency);
            if (chainLevel > 0) {
                chainShock(level, player, living, chainLevel * 2 + focusUpgradeLevel(wandStack, FocusUpgradeType.ENLARGE) * 2, potency, List.of(living.getId()));
            }
        }
    }

    private static void onUsingExcavationFocusTick(ItemStack wandStack, Level level, Player player, int count) {
        AspectList cost = focusVisCost(wandStack, WandFocusType.EXCAVATION, level.random);
        if (!hasFocusVis(wandStack, player, WandFocusType.EXCAVATION, cost)) {
            clearExcavationUse(level, player);
            player.stopUsingItem();
            return;
        }

        String pp = focusUseKey(level, player);
        BlockHitResult blockHit = blockRay(level, player, 10.0D);
        Vec3 end = blockHit == null
                ? player.getEyePosition().add(player.getViewVector(1.0F).scale(10.0D))
                : blockHit.getLocation();
        int impact = blockHit == null ? 0 : 5;
        if (level.isClientSide) {
            TC4ClientFocusFxBridge.beamCont(level, player, end, 2, 65382, false, impact > 0 ? 2.0F : 0.0F, impact);
        }

        if (blockHit == null) {
            resetExcavationProgress(level, player, pp, true);
            return;
        }

        BlockPos target = blockHit.getBlockPos();
        BlockState state = level.getBlockState(target);
        float hardness = state.getDestroySpeed(level, target);
        if (state.isAir() || hardness < 0.0F || !mayExcavate(level, player, target, blockHit.getDirection())) {
            resetExcavationProgress(level, player, pp, false);
            return;
        }

        long now = System.currentTimeMillis();
        if (!level.isClientSide && EXCAVATION_SOUND_DELAY.getOrDefault(pp, 0L) < now) {
            level.playSound(null, target, TC4Sounds.event("rumble"), SoundSource.BLOCKS, 0.3F, 1.0F);
            EXCAVATION_SOUND_DELAY.put(pp, now + 1200L);
        }

        int potency = focusPotency(wandStack);
        float speed = excavationSpeed(state, potency);
        BlockPos last = EXCAVATION_LAST_BLOCK.get(pp);
        if (last == null || !last.equals(target)) {
            if (level.isClientSide && last != null) {
                TC4ClientFocusFxBridge.excavateFX(level, last, player, -1);
            }
            EXCAVATION_LAST_BLOCK.put(pp, target);
            EXCAVATION_BREAKCOUNT.put(pp, 0.0F);
            return;
        }

        float breakCount = EXCAVATION_BREAKCOUNT.getOrDefault(pp, 0.0F);
        if (level.isClientSide && breakCount > 0.0F) {
            int progress = Math.min(9, Math.max(0, (int) (breakCount / hardness * 9.0F)));
            TC4ClientFocusFxBridge.excavateFX(level, target, player, progress);
        }

        if (level.isClientSide) {
            EXCAVATION_BREAKCOUNT.put(pp, breakCount >= hardness ? 0.0F : breakCount + speed);
            return;
        }

        if (breakCount >= hardness) {
            if (!consumeFocusVis(wandStack, player, WandFocusType.EXCAVATION, cost)) {
                clearExcavationUse(level, player);
                player.stopUsingItem();
                return;
            }
            excavateBlock(level, player, target, wandStack);
            resetExcavationProgress(level, player, pp, false);
        } else {
            EXCAVATION_BREAKCOUNT.put(pp, breakCount + speed);
        }
    }

    private static void shootProjectile(TC4FocusProjectileEntity projectile, Player player, float velocity, float inaccuracy) {
        projectile.shootFromRotation(player, player.getXRot(), player.getYRot(), 0.0F, velocity, inaccuracy);
        projectile.setPos(player.getX(), player.getEyeY() - 0.1D, player.getZ());
    }

    private static boolean excavateBlock(Level level, Player player, BlockPos target, ItemStack wandStack) {
        BlockState originalState = level.getBlockState(target);
        if (!excavateSingleBlock(level, player, target, wandStack)) {
            return false;
        }

        int enlarge = focusUpgradeLevel(wandStack, FocusUpgradeType.ENLARGE);
        for (int attempt = 0; attempt < enlarge; attempt++) {
            BlockPos neighbour = matchingNeighbour(level, target, originalState);
            if (neighbour == null) {
                break;
            }
            AspectList neighbourCost = focusVisCost(wandStack, WandFocusType.EXCAVATION, level.random);
            if (!hasFocusVis(wandStack, player, WandFocusType.EXCAVATION, neighbourCost)) {
                break;
            }
            if (excavateSingleBlock(level, player, neighbour, wandStack)) {
                consumeFocusVis(wandStack, player, WandFocusType.EXCAVATION, neighbourCost);
            }
        }
        return true;
    }

    private static boolean excavateSingleBlock(Level level, Player player, BlockPos target, ItemStack wandStack) {
        if (!(level instanceof ServerLevel server) || !(player instanceof ServerPlayer serverPlayer)) {
            return false;
        }
        BlockState state = level.getBlockState(target);
        float hardness = state.getDestroySpeed(level, target);
        if (state.isAir() || hardness < 0.0F || !mayExcavate(level, player, target, Direction.UP)) {
            return false;
        }

        int experience = excavationBreakExperience(server, serverPlayer, target, state);
        if (experience < 0) {
            return false;
        }

        int treasure = focusUpgradeLevel(wandStack, FocusUpgradeType.TREASURE);
        boolean silkTouch = focusHasUpgrade(wandStack, FocusUpgradeType.SILK_TOUCH);
        boolean dowsing = focusHasUpgrade(wandStack, FocusUpgradeType.DOWSING);
        ItemStack lootTool = new ItemStack(Items.NETHERITE_PICKAXE);
        if (silkTouch) {
            EnchantmentHelper.setEnchantments(java.util.Map.of(Enchantments.SILK_TOUCH, 1), lootTool);
        } else if (treasure > 0) {
            EnchantmentHelper.setEnchantments(java.util.Map.of(Enchantments.BLOCK_FORTUNE, treasure), lootTool);
        }

        BlockEntity blockEntity = level.getBlockEntity(target);
        List<ItemStack> drops = Block.getDrops(state, server, target, blockEntity, player, lootTool);
        for (ItemStack drop : drops) {
            ItemStack resolved = dowsing ? applyDowsing(drop, treasure, level.random) : drop.copy();
            if (!resolved.isEmpty()) {
                if (dowsing && !ItemStack.isSameItemSameTags(drop, resolved)) {
                    level.playSound(null, target, SoundEvents.EXPERIENCE_ORB_PICKUP, SoundSource.BLOCKS, 0.2F, 0.7F + level.random.nextFloat() * 0.2F);
                }
                Block.popResource(level, target, resolved);
            }
        }
        if (experience > 0) {
            ExperienceOrb.award(server, Vec3.atCenterOf(target), experience);
        }

        level.removeBlock(target, false);
        level.levelEvent(null, 2001, target, Block.getId(state));
        return true;
    }

    private static int excavationBreakExperience(ServerLevel level, ServerPlayer player, BlockPos target, BlockState state) {
        if (player.getUsedItemHand() != InteractionHand.OFF_HAND) {
            return ForgeHooks.onBlockBreakEvent(level, player.gameMode.getGameModeForPlayer(), player, target);
        }
        // ForgeHooks checks the main-hand item before posting BreakEvent. TC4 had no offhand,
        // so the 1.19.2 adapter posts the same event directly when the wand is used offhand.
        BlockEvent.BreakEvent event = new BlockEvent.BreakEvent(level, target, state, player);
        MinecraftForge.EVENT_BUS.post(event);
        return event.isCanceled() ? -1 : event.getExpToDrop();
    }

    private static BlockPos matchingNeighbour(Level level, BlockPos origin, BlockState state) {
        List<Direction> directions = new ArrayList<>(List.of(Direction.DOWN, Direction.UP, Direction.NORTH, Direction.SOUTH, Direction.EAST, Direction.WEST));
        java.util.Collections.shuffle(directions, new java.util.Random(level.random.nextLong()));
        for (Direction direction : directions) {
            BlockPos candidate = origin.relative(direction);
            if (level.getBlockState(candidate).equals(state)) {
                return candidate;
            }
        }
        return null;
    }

    private static ItemStack applyDowsing(ItemStack drop, int treasure, RandomSource random) {
        if (drop.isEmpty()) {
            return ItemStack.EMPTY;
        }
        net.minecraft.resources.ResourceLocation key = ForgeRegistries.ITEMS.getKey(drop.getItem());
        String path = key == null ? "" : key.getPath();
        String clusterId = null;
        float specialChance = 1.0F;

        if (drop.is(Items.RAW_IRON) || isOreLike(path, "iron")) {
            clusterId = "tc4_clusteriron";
        } else if (drop.is(Items.RAW_GOLD) || isOreLike(path, "gold")) {
            clusterId = "tc4_clustergold";
            specialChance = 0.9F;
        } else if (drop.is(Items.RAW_COPPER) || isOreLike(path, "copper")) {
            clusterId = "tc4_clustercopper";
        } else if (isOreLike(path, "tin")) {
            clusterId = "tc4_clustertin";
        } else if (isOreLike(path, "silver")) {
            clusterId = "tc4_clustersilver";
        } else if (isOreLike(path, "lead")) {
            clusterId = "tc4_clusterlead";
        } else if (path.contains("cinnabar")) {
            clusterId = "tc4_clustercinnabar";
            specialChance = 0.9F;
        }

        float chance = (0.2F + treasure * 0.075F) * specialChance;
        if (clusterId == null || random.nextFloat() > chance) {
            return drop.copy();
        }
        ItemStack cluster = TC4ResearchItems.registered(clusterId)
                .map(holder -> new ItemStack(holder.get(), drop.getCount()))
                .orElse(ItemStack.EMPTY);
        return cluster.isEmpty() ? drop.copy() : cluster;
    }

    private static boolean isOreLike(String path, String metal) {
        return path.contains(metal) && (path.contains("ore") || path.startsWith("raw_") || path.endsWith("_raw"));
    }

    private static boolean mayExcavate(Level level, Player player, BlockPos target, Direction face) {
        return level.mayInteract(player, target)
                && player.mayUseItemAt(target, face, player.getUseItem())
                && WardedBlockRuntime.mayEdit(level, target, player);
    }

    private static BlockHitResult blockRay(Level level, Player player, double distance) {
        Vec3 start = player.getEyePosition();
        Vec3 end = start.add(player.getViewVector(1.0F).scale(distance));
        BlockHitResult hit = level.clip(new ClipContext(start, end, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, player));
        return hit.getType() == HitResult.Type.MISS ? null : hit;
    }

    private static void clearExcavationUse(Level level, Player player) {
        resetExcavationProgress(level, player, focusUseKey(level, player), true);
    }

    private static void resetExcavationProgress(Level level, Player player, String key, boolean resetSound) {
        BlockPos previous = EXCAVATION_LAST_BLOCK.remove(key);
        EXCAVATION_BREAKCOUNT.remove(key);
        if (resetSound) {
            EXCAVATION_SOUND_DELAY.put(key, 0L);
        }
        if (level.isClientSide && previous != null) {
            TC4ClientFocusFxBridge.excavateFX(level, previous, player, -1);
        }
    }

    private static void storePickedBlock(ItemStack wandStack, ItemStack picked, BlockState state) {
        ItemStack one = picked.copy();
        one.setCount(1);
        wandStack.getOrCreateTag().put(FocusArchitectRuntime.TAG_PICKED_BLOCK, one.save(new CompoundTag()));
        wandStack.getOrCreateTag().put(FocusArchitectRuntime.TAG_PICKED_STATE, NbtUtils.writeBlockState(state));
    }

    private static ItemStack getPickedBlock(ItemStack wandStack) {
        CompoundTag tag = wandStack.getTag();
        if (tag != null && tag.contains(FocusArchitectRuntime.TAG_PICKED_BLOCK, 10)) {
            ItemStack picked = ItemStack.of(tag.getCompound(FocusArchitectRuntime.TAG_PICKED_BLOCK));
            if (!picked.isEmpty()) return picked;
        }
        return ItemStack.EMPTY;
    }

    private static BlockState getPickedState(ItemStack wandStack) {
        CompoundTag tag = wandStack.getTag();
        if (tag != null && tag.contains(FocusArchitectRuntime.TAG_PICKED_STATE, 10)) {
            return NbtUtils.readBlockState(tag.getCompound(FocusArchitectRuntime.TAG_PICKED_STATE));
        }
        ItemStack picked = getPickedBlock(wandStack);
        return picked.getItem() instanceof BlockItem blockItem
                ? blockItem.getBlock().defaultBlockState()
                : Blocks.AIR.defaultBlockState();
    }

    private static int wandInventorySlot(Player player, ItemStack wandStack) {
        if (player.getOffhandItem() == wandStack) return -1;
        return player.getInventory().selected;
    }

    static void sparkleBlock(Level level, BlockPos pos, int color) {
        if (level instanceof ServerLevel server) {
            double r = ((color >> 16) & 255) / 255.0D;
            double g = ((color >> 8) & 255) / 255.0D;
            double b = (color & 255) / 255.0D;
            server.sendParticles(ParticleTypes.ENCHANT, pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D, 8, r * 0.1D + 0.02D, g * 0.1D + 0.02D, b * 0.1D + 0.02D, 0.02D);
        }
    }

    static boolean hasFocusVis(ItemStack wandStack, Player player, WandFocusType type, AspectList cost) {
        if (player.getAbilities().instabuild || WandItem.hasInfiniteVis(wandStack)) {
            return true;
        }
        for (var entry : cost.entries().entrySet()) {
            if (WandItem.getVis(wandStack, entry.getKey()) < modifiedFocusVisCost(wandStack, player, entry.getKey(), entry.getValue())) {
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
        net.minecraft.world.level.material.Material material = state.getMaterial();
        if (material == net.minecraft.world.level.material.Material.STONE
                || material == net.minecraft.world.level.material.Material.GRASS
                || material == net.minecraft.world.level.material.Material.DIRT
                || material == net.minecraft.world.level.material.Material.SAND) {
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

    private static void chainShock(Level level, Player caster, LivingEntity source, int remaining, int potency, List<Integer> alreadyHit) {
        if (remaining <= 0 || level.isClientSide) {
            return;
        }
        AABB area = source.getBoundingBox().inflate(8.0D);
        LivingEntity closest = null;
        double distance = Double.MAX_VALUE;
        for (LivingEntity nearby : level.getEntitiesOfClass(LivingEntity.class, area,
                e -> e.isAlive()
                        && e != caster
                        && !alreadyHit.contains(e.getId())
                        && source.distanceToSqr(e) <= 64.0D
                        && canShockTarget(level, caster, e))) {
            double d = source.distanceToSqr(nearby);
            if (d < distance) {
                closest = nearby;
                distance = d;
            }
        }
        if (closest != null) {
            closest.hurt(DamageSource.playerAttack(caster), 4.0F + potency);
            if (level instanceof ServerLevel server) {
                beam(server, source.getEyePosition(), closest.getEyePosition(), Aspect.AER.argbColor(), ParticleTypes.ELECTRIC_SPARK);
            }
            List<Integer> next = new ArrayList<>(alreadyHit);
            next.add(closest.getId());
            chainShock(level, caster, closest, remaining - 1, potency, next);
        }
    }

    private static boolean canShockTarget(Level level, Player caster, LivingEntity target) {
        if (!(target instanceof Player)) {
            return true;
        }
        return level.getServer() != null && level.getServer().isPvpAllowed();
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

    private static double equalTradeReach(Player player) {
        // ItemFocusTrade uses the server interaction manager's current block
        // reach instead of the longer generic focus ray. Resolve Forge's reach
        // attribute by registry id so altered reach values remain compatible.
        for (String id : new String[] {"block_reach", "reach_distance"}) {
            Attribute attribute = ForgeRegistries.ATTRIBUTES.getValue(new net.minecraft.resources.ResourceLocation("forge", id));
            if (attribute == null) continue;
            AttributeInstance instance = player.getAttribute(attribute);
            if (instance != null) return Math.max(1.0D, instance.getValue());
        }
        return 5.0D;
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
            case HELLBAT, PECH_CURSE -> TC4Sounds.event("ice");
            case PRIMAL -> TC4Sounds.event("zap");
        };
    }

    private static float pitchFor(WandFocusType type) {
        return type == WandFocusType.FROST ? 0.75F : type == WandFocusType.SHOCK ? 1.25F : 1.0F;
    }

    private record HitBundle(BlockHitResult blockHit, EntityHitResult entityHit, Vec3 end) {
    }
}
