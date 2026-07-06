package com.darkifov.thaumcraft.infusion;

import com.darkifov.thaumcraft.ThaumcraftMod;
import com.darkifov.thaumcraft.data.PlayerThaumData;
import com.darkifov.thaumcraft.blockentity.ArcanePedestalBlockEntity;
import com.darkifov.thaumcraft.network.ThaumcraftNetwork;
import com.darkifov.thaumcraft.porting.TC4Sounds;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.phys.AABB;

import java.util.List;

/**
 * Stage123: TC4-weighted infusion instability events, ported from
 * thaumcraft.common.tiles.TileInfusionMatrix#craftCycle in TC4 1.7.10.
 *
 * The original matrix rolls nextInt(500) <= instability, then chooses one of
 * 21 weighted event slots. This class keeps that weighting and adapts each
 * event to safe Forge 1.19.2 runtime objects.
 */
public final class InfusionInstabilityEvents {
    private static final int EVENT_ROLL_BOUND = 21;
    private static final double EVENT_RADIUS = 10.0D;

    private InfusionInstabilityEvents() {
    }

    public static boolean maybeTrigger(Level level, BlockPos matrixPos, Player player, InfusionRecipe recipe, InfusionStructureReport report, int instability) {
        int clamped = TC4InfusionRuntime.clampInstability(instability);

        if (clamped <= 0 || level.random.nextInt(TC4InfusionRuntime.VALIDITY_INSTABILITY_ROLL) > clamped) {
            return false;
        }

        int roll = level.random.nextInt(EVENT_ROLL_BOUND);

        switch (roll) {
            case 0, 2, 10, 13 -> ejectItem(level, matrixPos, player, report, 0);
            case 6, 17 -> ejectItem(level, matrixPos, player, report, 1);
            case 1, 11 -> ejectItem(level, matrixPos, player, report, 2);
            case 3, 8, 14 -> zap(level, matrixPos, player, false);
            case 5, 16 -> harm(level, matrixPos, player, false);
            case 12 -> zap(level, matrixPos, player, true);
            case 19 -> ejectItem(level, matrixPos, player, report, 3);
            case 7 -> ejectItem(level, matrixPos, player, report, 4);
            case 4, 15 -> ejectItem(level, matrixPos, player, report, 5);
            case 18 -> harm(level, matrixPos, player, true);
            case 9 -> explosion(level, matrixPos, player);
            case 20 -> warp(level, matrixPos, player);
            default -> surge(level, matrixPos, player);
        }

        if (level instanceof ServerLevel serverLevel) {
            serverLevel.sendParticles(ParticleTypes.REVERSE_PORTAL,
                    matrixPos.getX() + 0.5D, matrixPos.getY() + 0.65D, matrixPos.getZ() + 0.5D,
                    24, 1.25D, 0.45D, 1.25D, 0.04D);
        }

        level.playSound(null, matrixPos, TC4Sounds.event("shock"), SoundSource.BLOCKS, 0.55F, 0.75F + level.random.nextFloat() * 0.35F);
        return true;
    }

    private static void ejectItem(Level level, BlockPos matrixPos, Player player, InfusionStructureReport report, int type) {
        if (report.componentPedestals().isEmpty()) {
            surge(level, matrixPos, player);
            return;
        }

        ArcanePedestalBlockEntity pedestal = randomFilledPedestal(level, report.componentPedestals());

        if (pedestal == null) {
            surge(level, matrixPos, player);
            return;
        }

        BlockPos p = pedestal.getBlockPos();
        ItemStack stack = pedestal.stored();

        // TC4 type 0/1/2/5 drops the item. Type 3/4 only deletes it and leaves flux.
        if (type < 3 || type == 5) {
            pedestal.setStored(ItemStack.EMPTY);
            ItemEntity entity = new ItemEntity(level, p.getX() + 0.5D, p.getY() + 1.05D, p.getZ() + 0.5D, stack.copy());
            entity.setDeltaMovement((level.random.nextDouble() - 0.5D) * 0.45D, 0.35D + level.random.nextDouble() * 0.25D, (level.random.nextDouble() - 0.5D) * 0.45D);
            level.addFreshEntity(entity);
        } else {
            pedestal.setStored(ItemStack.EMPTY);
        }

        if (type == 1 || type == 3) {
            placeFluxLikeGoo(level, p.above());
        } else if (type == 2 || type == 4) {
            gasBurst(level, p.above());
        } else if (type == 5) {
            level.explode(null, p.getX() + 0.5D, p.getY() + 0.5D, p.getZ() + 0.5D, 1.0F, Explosion.BlockInteraction.NONE);
        }

        if (level instanceof ServerLevel serverLevel) {
            InfusionProcessHelper.spawnSourceParticles(serverLevel, p, matrixPos, true);
            serverLevel.sendParticles(ParticleTypes.SMOKE, p.getX() + 0.5D, p.getY() + 1.25D, p.getZ() + 0.5D, 18, 0.25D, 0.25D, 0.25D, 0.03D);
        }

        message(player, switch (type) {
            case 1, 3 -> "Infusion instability! Flux goo vents from a pedestal.";
            case 2, 4 -> "Infusion instability! Flux gas erupts from a pedestal.";
            case 5 -> "Infusion instability! A pedestal detonates.";
            default -> "Infusion instability! A component was ejected.";
        });
    }

    private static ArcanePedestalBlockEntity randomFilledPedestal(Level level, List<ArcanePedestalBlockEntity> pedestals) {
        for (int i = 0; i < 50 && !pedestals.isEmpty(); i++) {
            ArcanePedestalBlockEntity pedestal = pedestals.get(level.random.nextInt(pedestals.size()));
            if (!pedestal.stored().isEmpty()) {
                return pedestal;
            }
        }
        return null;
    }

    private static void zap(Level level, BlockPos matrixPos, Player player, boolean all) {
        List<LivingEntity> targets = livingTargets(level, matrixPos);

        for (LivingEntity target : targets) {
            target.hurt(DamageSource.MAGIC, 4.0F + level.random.nextInt(4));

            if (level instanceof ServerLevel serverLevel) {
                InfusionProcessHelper.spawnParticleBeam(serverLevel,
                        matrixPos.getX() + 0.5D, matrixPos.getY() + 0.5D, matrixPos.getZ() + 0.5D,
                        target.getX(), target.getY() + target.getBbHeight() * 0.5D, target.getZ(),
                        ParticleTypes.ELECTRIC_SPARK, 18);
            }

            if (!all) {
                break;
            }
        }

        message(player, all ? "Infusion instability! Arcs of energy lash every nearby creature." : "Infusion instability! A violent arc lashes out.");
    }

    private static void harm(Level level, BlockPos matrixPos, Player player, boolean all) {
        List<LivingEntity> targets = livingTargets(level, matrixPos);

        for (LivingEntity target : targets) {
            if (level.random.nextBoolean()) {
                target.addEffect(new MobEffectInstance(MobEffects.POISON, 120, 0, false, true));
            } else {
                target.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 2400, 0, true, true));
            }

            if (level instanceof ServerLevel serverLevel) {
                serverLevel.sendParticles(ParticleTypes.WITCH, target.getX(), target.getY() + 1.0D, target.getZ(), 16, 0.35D, 0.6D, 0.35D, 0.03D);
            }

            if (!all) {
                break;
            }
        }

        message(player, all ? "Infusion instability! Corruption washes over nearby creatures." : "Infusion instability! Corruption strikes a nearby creature.");
    }

    private static void warp(Level level, BlockPos matrixPos, Player fallbackPlayer) {
        List<Player> targets = level.getEntitiesOfClass(Player.class, area(matrixPos));
        Player target = targets.isEmpty() ? fallbackPlayer : targets.get(level.random.nextInt(targets.size()));

        if (target != null) {
            int warp = level.random.nextFloat() < 0.25F ? 1 : 1 + level.random.nextInt(5);
            PlayerThaumData.addWarp(target, warp);

            if (target instanceof ServerPlayer serverPlayer) {
                ThaumcraftNetwork.syncResearch(serverPlayer);
            }

            message(target, "Infusion instability! The altar scars your mind with warp." );
        }

        if (level instanceof ServerLevel serverLevel) {
            serverLevel.sendParticles(ParticleTypes.SOUL, matrixPos.getX() + 0.5D, matrixPos.getY() + 0.75D, matrixPos.getZ() + 0.5D, 36, 1.1D, 0.45D, 1.1D, 0.02D);
        }
    }

    private static void explosion(Level level, BlockPos matrixPos, Player player) {
        level.explode(null, matrixPos.getX() + 0.5D, matrixPos.getY() + 0.5D, matrixPos.getZ() + 0.5D, 1.5F + level.random.nextFloat(), Explosion.BlockInteraction.NONE);
        message(player, "Infusion instability! The altar releases a violent blast.");
    }

    private static void surge(Level level, BlockPos matrixPos, Player player) {
        if (level instanceof ServerLevel serverLevel) {
            serverLevel.sendParticles(ParticleTypes.SMOKE, matrixPos.getX() + 0.5D, matrixPos.getY() + 0.65D, matrixPos.getZ() + 0.5D, 20, 1.0D, 0.35D, 1.0D, 0.03D);
        }

        message(player, "Infusion instability surges around the altar.");
    }

    private static void placeFluxLikeGoo(Level level, BlockPos pos) {
        if (!level.isOutsideBuildHeight(pos) && level.getBlockState(pos).isAir()) {
            level.setBlock(pos, ThaumcraftMod.TAINTED_SOIL.get().defaultBlockState(), 3);
        }
    }

    private static void gasBurst(Level level, BlockPos pos) {
        if (level instanceof ServerLevel serverLevel) {
            serverLevel.sendParticles(ParticleTypes.CLOUD, pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D, 30, 0.45D, 0.45D, 0.45D, 0.02D);
            serverLevel.sendParticles(ParticleTypes.WITCH, pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D, 18, 0.35D, 0.35D, 0.35D, 0.02D);
        }
    }

    private static List<LivingEntity> livingTargets(Level level, BlockPos matrixPos) {
        return level.getEntitiesOfClass(LivingEntity.class, area(matrixPos), Entity::isAlive);
    }

    private static AABB area(BlockPos matrixPos) {
        return new AABB(matrixPos).inflate(EVENT_RADIUS, EVENT_RADIUS, EVENT_RADIUS);
    }

    private static void message(Player player, String text) {
        if (player != null) {
            player.displayClientMessage(Component.literal(text).withStyle(ChatFormatting.DARK_PURPLE), false);
        }
    }
}
