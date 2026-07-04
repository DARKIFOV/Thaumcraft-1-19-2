package com.darkifov.thaumcraft.block;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

public class ThaumcraftExtrasElementalBlock extends Block {
    public enum Mode {
        FIRE,
        AIR,
        WATER,
        EARTH,
        LIGHT,
        ENDER,
        RESEARCH
    }

    private final Mode mode;

    public ThaumcraftExtrasElementalBlock(Properties properties, Mode mode) {
        super(properties);
        this.mode = mode;
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        if (random.nextFloat() > 0.35F) {
            return;
        }

        switch (mode) {
            case FIRE -> level.addParticle(ParticleTypes.FLAME, pos.getX() + 0.5D, pos.getY() + 1.05D, pos.getZ() + 0.5D, 0.0D, 0.02D, 0.0D);
            case AIR -> level.addParticle(ParticleTypes.CLOUD, pos.getX() + 0.5D, pos.getY() + 1.05D, pos.getZ() + 0.5D, 0.0D, 0.02D, 0.0D);
            case WATER -> level.addParticle(ParticleTypes.BUBBLE, pos.getX() + 0.5D, pos.getY() + 0.8D, pos.getZ() + 0.5D, 0.0D, 0.02D, 0.0D);
            case EARTH -> level.addParticle(ParticleTypes.COMPOSTER, pos.getX() + 0.5D, pos.getY() + 1.05D, pos.getZ() + 0.5D, 0.0D, 0.02D, 0.0D);
            case LIGHT -> level.addParticle(ParticleTypes.END_ROD, pos.getX() + 0.5D, pos.getY() + 1.05D, pos.getZ() + 0.5D, 0.0D, 0.02D, 0.0D);
            case ENDER -> level.addParticle(ParticleTypes.PORTAL, pos.getX() + 0.5D, pos.getY() + 0.8D, pos.getZ() + 0.5D, 0.0D, 0.04D, 0.0D);
            case RESEARCH -> level.addParticle(ParticleTypes.ENCHANT, pos.getX() + 0.5D, pos.getY() + 1.05D, pos.getZ() + 0.5D, 0.0D, 0.02D, 0.0D);
        }
    }

    @Override
    public void stepOn(Level level, BlockPos pos, BlockState state, Entity entity) {
        if (!level.isClientSide && entity instanceof LivingEntity living) {
            switch (mode) {
                case FIRE -> living.setSecondsOnFire(4);
                case AIR -> living.addEffect(new MobEffectInstance(MobEffects.LEVITATION, 20, 0));
                case WATER -> {
                    living.clearFire();
                    living.addEffect(new MobEffectInstance(MobEffects.WATER_BREATHING, 20 * 4, 0));
                }
                case EARTH -> living.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 20 * 2, 1));
                case LIGHT -> living.addEffect(new MobEffectInstance(MobEffects.GLOWING, 20 * 5, 0));
                default -> {
                }
            }
        }

        super.stepOn(level, pos, state, entity);
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player,
                                 InteractionHand hand, BlockHitResult hit) {
        if (level.isClientSide) {
            return InteractionResult.SUCCESS;
        }

        if (mode == Mode.ENDER) {
            double x = player.getX() + (level.random.nextDouble() - 0.5D) * 16.0D;
            double z = player.getZ() + (level.random.nextDouble() - 0.5D) * 16.0D;
            player.teleportTo(x, player.getY(), z);
            player.displayClientMessage(Component.literal("Блок Эндера случайно искажает твоё положение.").withStyle(ChatFormatting.DARK_PURPLE), false);
            return InteractionResult.CONSUME;
        }

        if (mode == Mode.RESEARCH) {
            player.displayClientMessage(Component.literal("Исследовательский кэш — творческий/тестовый блок. Полная выдача исследований будет позже.").withStyle(ChatFormatting.AQUA), false);
            return InteractionResult.CONSUME;
        }

        player.displayClientMessage(Component.literal("Элементальный блок Thaumcraft Extras: " + mode.name()).withStyle(ChatFormatting.GRAY), false);
        return InteractionResult.CONSUME;
    }
}
