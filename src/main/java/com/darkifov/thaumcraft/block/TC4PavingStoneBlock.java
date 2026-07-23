package com.darkifov.thaumcraft.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;

import java.util.List;

/** TC4 paving stone metadata 2 (travel) and 3 (warding). */
public final class TC4PavingStoneBlock extends Block {
    public enum Kind {
        TRAVEL,
        WARDING
    }

    private final Kind kind;

    public TC4PavingStoneBlock(Properties properties, Kind kind) {
        super(properties);
        this.kind = kind == null ? Kind.TRAVEL : kind;
    }

    public Kind kind() {
        return kind;
    }

    @Override
    public void entityInside(BlockState state, Level level, BlockPos pos, Entity entity) {
        super.entityInside(state, level, pos, entity);
        if (kind == Kind.TRAVEL && entity instanceof LivingEntity living) {
            living.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 40, 1, true, false));
            living.addEffect(new MobEffectInstance(MobEffects.JUMP, 40, 0, true, false));
        }
    }

    @Override
    public void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean movedByPiston) {
        super.onPlace(state, level, pos, oldState, movedByPiston);
        if (!level.isClientSide && kind == Kind.WARDING && !state.is(oldState.getBlock())) {
            level.scheduleTick(pos, this, 5);
        }
    }

    @Override
    public void neighborChanged(BlockState state, Level level, BlockPos pos, Block neighborBlock,
                                BlockPos neighborPos, boolean movedByPiston) {
        super.neighborChanged(state, level, pos, neighborBlock, neighborPos, movedByPiston);
        if (!level.isClientSide && kind == Kind.WARDING) {
            level.scheduleTick(pos, this, 1);
        }
    }

    @Override
    public void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        if (kind != Kind.WARDING) return;

        if (!level.hasNeighborSignal(pos)) {
            AABB area = new AABB(pos).expandTowards(0.0D, 3.0D, 0.0D).inflate(0.1D);
            List<LivingEntity> targets = level.getEntitiesOfClass(LivingEntity.class, area,
                    living -> !(living instanceof Player) && !living.isOnGround());
            for (LivingEntity living : targets) {
                float angle = (living.getYRot() + 180.0F) * ((float) Math.PI / 180.0F);
                living.push(-Mth.sin(angle) * 0.2F, -0.1D, Mth.cos(angle) * 0.2F);
            }
        }

        level.scheduleTick(pos, this, 5);
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        if (kind == Kind.TRAVEL && random.nextInt(4) == 0) {
            level.addParticle(ParticleTypes.ENCHANT,
                    pos.getX() + random.nextDouble(), pos.getY() + 1.02D, pos.getZ() + random.nextDouble(),
                    0.0D, 0.02D, 0.0D);
        } else if (kind == Kind.WARDING && random.nextInt(3) == 0) {
            level.addParticle(level.hasNeighborSignal(pos) ? ParticleTypes.SMOKE : ParticleTypes.WITCH,
                    pos.getX() + random.nextDouble(), pos.getY() + 1.05D, pos.getZ() + random.nextDouble(),
                    0.0D, 0.02D, 0.0D);
        }
    }
}
