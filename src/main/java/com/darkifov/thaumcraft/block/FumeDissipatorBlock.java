package com.darkifov.thaumcraft.block;

import com.darkifov.thaumcraft.blockentity.CrucibleBlockEntity;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;

import java.util.List;

public class FumeDissipatorBlock extends Block {
    public FumeDissipatorBlock(BlockBehaviour.Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player,
                                 InteractionHand hand, BlockHitResult hit) {
        if (level.isClientSide) {
            return InteractionResult.SUCCESS;
        }

        int radius = player.isShiftKeyDown() ? 8 : 4;
        int entities = cleanseEntities(level, pos, radius);
        int flux = cleanseCrucibles(level, pos, radius, player.isShiftKeyDown());

        player.clearFire();
        player.displayClientMessage(Component.literal("Fume Dissipator cleansed entities: " + entities + " | flux removed: " + flux + " | radius: " + radius)
                .withStyle(ChatFormatting.AQUA), false);

        level.playSound(null, pos, SoundEvents.BREWING_STAND_BREW, SoundSource.BLOCKS, 0.9F, 1.2F);
        return InteractionResult.CONSUME;
    }

    private int cleanseEntities(Level level, BlockPos pos, int radius) {
        AABB box = new AABB(pos).inflate(radius);
        List<LivingEntity> entities = level.getEntitiesOfClass(LivingEntity.class, box);
        int affected = 0;

        for (LivingEntity entity : entities) {
            boolean changed = false;
            entity.clearFire();

            changed |= entity.removeEffect(MobEffects.POISON);
            changed |= entity.removeEffect(MobEffects.WITHER);
            changed |= entity.removeEffect(MobEffects.BLINDNESS);
            changed |= entity.removeEffect(MobEffects.CONFUSION);
            changed |= entity.removeEffect(MobEffects.HUNGER);
            changed |= entity.removeEffect(MobEffects.WEAKNESS);
            changed |= entity.removeEffect(MobEffects.MOVEMENT_SLOWDOWN);

            if (changed) {
                affected++;
            }
        }

        return affected;
    }

    private int cleanseCrucibles(Level level, BlockPos pos, int radius, boolean strong) {
        int removed = 0;
        int max = strong ? 6 : 2;

        for (BlockPos target : BlockPos.betweenClosed(pos.offset(-radius, -radius, -radius), pos.offset(radius, radius, radius))) {
            if (level.getBlockEntity(target) instanceof CrucibleBlockEntity crucible && crucible.flux() > 0) {
                int before = crucible.flux();
                crucible.addFlux(-Math.min(max, before));
                removed += before - crucible.flux();
            }
        }

        return removed;
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        if (random.nextInt(3) != 0) {
            return;
        }

        double x = pos.getX() + 0.5D + (random.nextDouble() - 0.5D) * 0.8D;
        double y = pos.getY() + 0.85D + random.nextDouble() * 0.4D;
        double z = pos.getZ() + 0.5D + (random.nextDouble() - 0.5D) * 0.8D;
        level.addParticle(ParticleTypes.CLOUD, x, y, z, 0.0D, 0.025D, 0.0D);
        level.addParticle(ParticleTypes.END_ROD, x, y + 0.05D, z, 0.0D, 0.015D, 0.0D);
    }
}
