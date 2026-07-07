package com.darkifov.thaumcraft.block;

import com.darkifov.thaumcraft.taint.TaintSpreadRuntime;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

/**
 * TC4 blockTaint metadata bridge for Forge 1.19.2.
 *
 * Original TC4 stores three blockTaint variants as metadata:
 * 0 = taint_crust, 1 = taint_soil, 2 = fleshblock.
 * 1.19.2 no longer uses item/block metadata the same way, so Stage145 exposes
 * them as explicit registered blocks while preserving the original behavior and
 * source texture names.
 */
public class TaintBlock extends Block {
    public enum Variant {
        CRUST,
        SOIL,
        FLESH
    }

    private final Variant variant;

    public TaintBlock(Properties properties, Variant variant) {
        super(properties);
        this.variant = variant;
    }

    public Variant variant() {
        return variant;
    }

    @Override
    public void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        super.randomTick(state, level, pos, random);
        TaintSpreadRuntime.randomTick(level, pos, random, variant);
    }

    @Override
    public void entityInside(BlockState state, Level level, BlockPos pos, Entity entity) {
        super.entityInside(state, level, pos, entity);
        if (!level.isClientSide && entity instanceof LivingEntity living && !living.isInvertedHealAndHarm()) {
            int chance = living instanceof Player ? 1000 : 500;
            int duration = living instanceof Player ? 80 : 160;
            if (level.random.nextInt(chance) == 0) {
                living.addEffect(new MobEffectInstance(MobEffects.POISON, duration, 0, false, false));
            }
        }
    }
}
