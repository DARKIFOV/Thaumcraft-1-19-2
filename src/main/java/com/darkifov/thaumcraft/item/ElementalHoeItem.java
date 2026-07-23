package com.darkifov.thaumcraft.item;

import com.darkifov.thaumcraft.ThaumcraftMod;
import com.darkifov.thaumcraft.item.gear.TC4ElementalToolTier;
import com.darkifov.thaumcraft.porting.TC4Sounds;
import com.darkifov.thaumcraft.world.TC4TreeGenerator;
import com.mojang.math.Vector3f;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.HoeItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BonemealableBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.player.BonemealEvent;
import net.minecraftforge.eventbus.api.Event;

/** Forge 1.19.2 port of TC4's Hoe of Growth. */
public final class ElementalHoeItem extends HoeItem {
    private static final DustParticleOptions TILL_PARTICLE =
            new DustParticleOptions(new Vector3f(0.50F, 0.28F, 0.08F), 0.7F);
    private static final DustParticleOptions GROW_PARTICLE =
            new DustParticleOptions(new Vector3f(0.18F, 0.85F, 0.18F), 0.75F);

    public ElementalHoeItem(Properties properties) {
        super(TC4ElementalToolTier.INSTANCE, -3, 0.0F, properties.stacksTo(1));
    }

    @Override
    public Rarity getRarity(ItemStack stack) {
        return Rarity.RARE;
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Player player = context.getPlayer();
        if (player == null || player.isShiftKeyDown()) {
            return super.useOn(context);
        }

        Level level = context.getLevel();
        BlockPos origin = context.getClickedPos();
        var face = context.getClickedFace();
        boolean didTill = false;

        for (int dx = -1; dx <= 1; dx++) {
            for (int dz = -1; dz <= 1; dz++) {
                BlockPos target = origin.offset(dx, 0, dz);
                Vec3 hitLocation = context.getClickLocation().add(dx, 0.0D, dz);
                BlockHitResult hit = new BlockHitResult(hitLocation, face, target, false);
                UseOnContext areaContext = new UseOnContext(player, context.getHand(), hit);
                InteractionResult result = super.useOn(areaContext);
                if (result.consumesAction()) {
                    didTill = true;
                    if (level instanceof ServerLevel serverLevel) {
                        serverLevel.sendParticles(TILL_PARTICLE,
                                target.getX() + 0.5D, target.getY() + 1.02D, target.getZ() + 0.5D,
                                5, 0.35D, 0.05D, 0.35D, 0.0D);
                    }
                }
            }
        }

        if (didTill) {
            return InteractionResult.sidedSuccess(level.isClientSide);
        }

        if (growLikeTC4(level, origin, player, context.getItemInHand(), context)) {
            return InteractionResult.sidedSuccess(level.isClientSide);
        }
        return InteractionResult.PASS;
    }

    private static boolean growLikeTC4(Level level, BlockPos pos, Player player,
                                       ItemStack stack, UseOnContext context) {
        BlockState state = level.getBlockState(pos);
        BonemealEvent event = new BonemealEvent(player, level, pos, state, stack);
        MinecraftForge.EVENT_BUS.post(event);
        if (event.isCanceled() || event.getResult() == Event.Result.DENY) {
            return false;
        }

        if (event.getResult() == Event.Result.ALLOW) {
            damageGrowthUse(level, player, stack, context, 1);
            finishGrowthEffect(level, pos);
            return true;
        }

        if (!(level instanceof ServerLevel serverLevel)) {
            return state.is(ThaumcraftMod.GREATWOOD_SAPLING.get())
                    || state.is(ThaumcraftMod.SILVERWOOD_SAPLING.get())
                    || state.getBlock() instanceof BonemealableBlock;
        }

        int remaining = stack.getMaxDamage() - stack.getDamageValue();
        if (state.is(ThaumcraftMod.GREATWOOD_SAPLING.get()) && remaining >= 20) {
            boolean grown = TC4TreeGenerator.growGreatwood(serverLevel, pos, level.random, false);
            if (grown) {
                damageGrowthUse(level, player, stack, context, 5);
                finishGrowthEffect(level, pos);
            }
            return grown;
        }
        if (state.is(ThaumcraftMod.SILVERWOOD_SAPLING.get()) && remaining >= 150) {
            boolean grown = TC4TreeGenerator.growSilverwood(serverLevel, pos, level.random, false);
            if (grown) {
                damageGrowthUse(level, player, stack, context, 25);
                finishGrowthEffect(level, pos);
            }
            return grown;
        }
        if (state.getBlock() instanceof BonemealableBlock growable
                && growable.isValidBonemealTarget(level, pos, state, false)
                && growable.isBonemealSuccess(level, level.random, pos, state)) {
            growable.performBonemeal(serverLevel, level.random, pos, state);
            damageGrowthUse(level, player, stack, context, 1);
            finishGrowthEffect(level, pos);
            return true;
        }
        return false;
    }

    private static void damageGrowthUse(Level level, Player player, ItemStack stack,
                                        UseOnContext context, int amount) {
        if (!level.isClientSide) {
            stack.hurtAndBreak(amount, player, p -> p.broadcastBreakEvent(context.getHand()));
        }
    }

    private static void finishGrowthEffect(Level level, BlockPos pos) {
        if (level instanceof ServerLevel serverLevel) {
            serverLevel.playSound(null, pos, TC4Sounds.event("wand"), SoundSource.PLAYERS,
                    0.75F, 0.9F + level.random.nextFloat() * 0.2F);
            serverLevel.sendParticles(GROW_PARTICLE,
                    pos.getX() + 0.5D, pos.getY() + 0.8D, pos.getZ() + 0.5D,
                    18, 0.35D, 0.45D, 0.35D, 0.02D);
        }
    }
}
