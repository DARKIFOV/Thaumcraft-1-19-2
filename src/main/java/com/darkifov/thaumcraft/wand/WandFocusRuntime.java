package com.darkifov.thaumcraft.wand;

import com.darkifov.thaumcraft.Aspect;
import com.darkifov.thaumcraft.AspectList;
import com.darkifov.thaumcraft.ThaumcraftMod;
import com.darkifov.thaumcraft.block.WandItem;
import com.darkifov.thaumcraft.porting.TC4Sounds;
import com.darkifov.thaumcraft.ward.WardedBlockRuntime;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.entity.projectile.ProjectileUtil;

import java.util.function.Predicate;

/**
 * Stage129: 1.19.2 runtime adaptation of original TC4 wand foci.
 *
 * The old TC4 focus classes cannot be dropped into 1.19.2 because they depend on
 * FML/MCP classes, but this runtime follows their key behaviour: each focus is a
 * wand attachment, consumes the original vis-cost AspectList, fires along the
 * player's look vector and gives TC4-like sound/particle feedback.
 */
public final class WandFocusRuntime {
    public static final String TAG_FOCUS = "Focus";

    private WandFocusRuntime() {
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
        } else {
            wandStack.getOrCreateTag().putString(TAG_FOCUS, type.id());
        }
    }

    public static boolean consumeFocusVis(ItemStack wandStack, Player player, WandFocusType type) {
        if (player.getAbilities().instabuild || WandItem.hasInfiniteVis(wandStack)) {
            return true;
        }
        for (var entry : type.visCost().entries().entrySet()) {
            int needed = WandItem.modifiedVisCost(wandStack, entry.getKey(), entry.getValue());
            if (WandItem.getVis(wandStack, entry.getKey()) < needed) {
                player.displayClientMessage(Component.literal("Not enough " + entry.getKey().displayName() + " vis for " + type.displayName() + ": need " + needed + " after cap modifier.").withStyle(ChatFormatting.RED), true);
                return false;
            }
        }
        for (var entry : type.visCost().entries().entrySet()) {
            WandItem.consumeVis(wandStack, entry.getKey(), WandItem.modifiedVisCost(wandStack, entry.getKey(), entry.getValue()));
        }
        return true;
    }

    public static InteractionResult cast(ItemStack wandStack, Level level, Player player) {
        WandFocusType type = getFocus(wandStack);
        if (type == null) {
            return InteractionResult.PASS;
        }

        if (!consumeFocusVis(wandStack, player, type)) {
            level.playSound(null, player.blockPosition(), TC4Sounds.event("wandfail"), SoundSource.PLAYERS, 0.55F, 1.0F);
            return InteractionResult.CONSUME;
        }

        switch (type) {
            case FIRE -> castFire(level, player);
            case FROST -> castFrost(level, player);
            case SHOCK -> castShock(level, player);
            case EXCAVATION -> castExcavation(level, player);
            case PORTABLE_HOLE -> castPortableHole(level, player);
            case EQUAL_TRADE -> castEqualTrade(level, player);
            case WARDING -> castWarding(level, player);
            case PRIMAL -> castPrimal(level, player);
        }

        level.playSound(null, player.blockPosition(), soundFor(type), SoundSource.PLAYERS, 0.65F, pitchFor(type));
        return InteractionResult.CONSUME;
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

    private static void castFire(Level level, Player player) {
        HitBundle hit = ray(level, player, 24.0D);
        beam(level, player.getEyePosition(), hit.end(), Aspect.IGNIS.argbColor(), ParticleTypes.FLAME);
        if (!level.isClientSide && hit.entityHit() != null && hit.entityHit().getEntity() instanceof LivingEntity living) {
            living.setSecondsOnFire(6);
            living.hurt(DamageSource.ON_FIRE, 6.0F);
        } else if (!level.isClientSide && hit.blockHit() != null) {
            BlockPos firePos = hit.blockHit().getBlockPos().relative(hit.blockHit().getDirection());
            if (level.getBlockState(firePos).isAir()) {
                level.setBlock(firePos, Blocks.FIRE.defaultBlockState(), 11);
            }
        }
    }

    private static void castFrost(Level level, Player player) {
        HitBundle hit = ray(level, player, 20.0D);
        beam(level, player.getEyePosition(), hit.end(), Aspect.AQUA.argbColor(), ParticleTypes.POOF);
        if (!level.isClientSide && hit.entityHit() != null && hit.entityHit().getEntity() instanceof LivingEntity living) {
            living.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 120, 2));
            living.hurt(DamageSource.FREEZE, 3.0F);
        } else if (!level.isClientSide && hit.blockHit() != null) {
            BlockPos target = hit.blockHit().getBlockPos().relative(hit.blockHit().getDirection());
            if (level.getBlockState(target).isAir()) {
                level.setBlock(target, Blocks.SNOW.defaultBlockState(), 3);
            }
        }
    }

    private static void castShock(Level level, Player player) {
        HitBundle hit = ray(level, player, 28.0D);
        beam(level, player.getEyePosition(), hit.end(), Aspect.AER.argbColor(), ParticleTypes.CRIT);
        if (!level.isClientSide && hit.entityHit() != null && hit.entityHit().getEntity() instanceof LivingEntity living) {
            living.hurt(DamageSource.MAGIC, 7.0F);
            living.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 80, 0));
            chainShock(level, living, 2);
        }
    }

    private static void castExcavation(Level level, Player player) {
        HitBundle hit = ray(level, player, 8.0D);
        beam(level, player.getEyePosition(), hit.end(), Aspect.TERRA.argbColor(), ParticleTypes.CRIT);
        if (!level.isClientSide && hit.blockHit() != null) {
            BlockPos target = hit.blockHit().getBlockPos();
            BlockState state = level.getBlockState(target);
            if (!state.isAir() && state.getDestroySpeed(level, target) >= 0.0F && state.getDestroySpeed(level, target) <= 20.0F && WardedBlockRuntime.mayEdit(level, target, player)) {
                level.destroyBlock(target, true, player);
            }
        }
    }

    private static void castPortableHole(Level level, Player player) {
        HitBundle hit = ray(level, player, 16.0D);
        beam(level, player.getEyePosition(), hit.end(), Aspect.PERDITIO.argbColor(), ParticleTypes.PORTAL);
        if (!level.isClientSide && hit.blockHit() != null) {
            Direction direction = hit.blockHit().getDirection();
            BlockPos cursor = hit.blockHit().getBlockPos();
            for (int i = 0; i < 6; i++) {
                BlockPos p = cursor.relative(direction.getOpposite(), i);
                BlockState state = level.getBlockState(p);
                if (!state.isAir() && state.getDestroySpeed(level, p) >= 0.0F && state.getDestroySpeed(level, p) <= 12.0F && WardedBlockRuntime.mayEdit(level, p, player)) {
                    level.setBlock(p, ThaumcraftMod.TEMPORARY_HOLE.get().defaultBlockState(), 3);
                    if (level instanceof ServerLevel server) {
                        server.scheduleTick(p, ThaumcraftMod.TEMPORARY_HOLE.get(), 160);
                    }
                }
            }
        }
    }

    private static void castEqualTrade(Level level, Player player) {
        HitBundle hit = ray(level, player, 12.0D);
        beam(level, player.getEyePosition(), hit.end(), Aspect.PERMUTATIO.argbColor(), ParticleTypes.ENCHANT);
        if (!level.isClientSide && hit.blockHit() != null) {
            ItemStack offhand = player.getOffhandItem();
            if (!offhand.isEmpty() && offhand.getItem() instanceof net.minecraft.world.item.BlockItem blockItem) {
                BlockPos target = hit.blockHit().getBlockPos();
                BlockState old = level.getBlockState(target);
                if (!old.isAir() && old.getDestroySpeed(level, target) >= 0.0F && WardedBlockRuntime.mayEdit(level, target, player)) {
                    level.destroyBlock(target, true, player);
                    level.setBlock(target, blockItem.getBlock().defaultBlockState(), 3);
                    if (!player.getAbilities().instabuild) {
                        offhand.shrink(1);
                    }
                }
            } else {
                player.displayClientMessage(Component.literal("Equal Trade needs a block in off-hand.").withStyle(ChatFormatting.GRAY), true);
            }
        }
    }

    private static void castWarding(Level level, Player player) {
        HitBundle hit = ray(level, player, 10.0D);
        beam(level, player.getEyePosition(), hit.end(), Aspect.ORDO.argbColor(), ParticleTypes.ENCHANT);
        if (!level.isClientSide && hit.blockHit() != null) {
            BlockPos target = hit.blockHit().getBlockPos();
            if (player.isShiftKeyDown()) {
                if (WardedBlockRuntime.unward(level, target, player)) {
                    player.displayClientMessage(Component.literal("The ward fades from " + level.getBlockState(target).getBlock().getName().getString() + ".").withStyle(ChatFormatting.BLUE), true);
                } else {
                    player.displayClientMessage(Component.literal("Another thaumaturge owns this ward.").withStyle(ChatFormatting.RED), true);
                }
                return;
            }
            WardedBlockRuntime.ward(level, target, player);
            player.displayClientMessage(Component.literal("Warding binds " + level.getBlockState(target).getBlock().getName().getString() + " to your aura.").withStyle(ChatFormatting.BLUE), true);
        }
    }

    private static void castPrimal(Level level, Player player) {
        HitBundle hit = ray(level, player, 28.0D);
        beam(level, player.getEyePosition(), hit.end(), Aspect.PRAECANTATIO.argbColor(), ParticleTypes.ENCHANT);
        if (!level.isClientSide && hit.entityHit() != null && hit.entityHit().getEntity() instanceof LivingEntity living) {
            living.hurt(DamageSource.MAGIC, 12.0F);
            living.setSecondsOnFire(3);
            living.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 80, 1));
        } else if (!level.isClientSide && hit.blockHit() != null) {
            BlockPos target = hit.blockHit().getBlockPos().relative(hit.blockHit().getDirection());
            if (level.getBlockState(target).isAir()) {
                level.setBlock(target, Blocks.LIGHT.defaultBlockState(), 3);
            }
        }
    }

    private static void chainShock(Level level, LivingEntity source, int remaining) {
        if (remaining <= 0 || level.isClientSide) {
            return;
        }
        AABB area = source.getBoundingBox().inflate(5.0D);
        for (LivingEntity nearby : level.getEntitiesOfClass(LivingEntity.class, area, e -> e.isAlive() && e != source)) {
            nearby.hurt(DamageSource.MAGIC, 3.0F);
            if (level instanceof ServerLevel server) {
                beam(server, source.getEyePosition(), nearby.getEyePosition(), Aspect.AER.argbColor(), ParticleTypes.CRIT);
            }
            break;
        }
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

    private static void beam(Level level, Vec3 start, Vec3 end, int color, net.minecraft.core.particles.ParticleOptions particle) {
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
