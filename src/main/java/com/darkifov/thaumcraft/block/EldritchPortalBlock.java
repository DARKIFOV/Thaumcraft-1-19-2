package com.darkifov.thaumcraft.block;

import com.darkifov.thaumcraft.ThaumcraftMod;
import com.darkifov.thaumcraft.blockentity.EldritchPortalBlockEntity;
import com.darkifov.thaumcraft.config.ThaumcraftConfig;
import com.darkifov.thaumcraft.data.PlayerThaumData;
import com.darkifov.thaumcraft.network.ThaumcraftNetwork;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;


public class EldritchPortalBlock extends BaseEntityBlock {
    public EldritchPortalBlock(Properties properties) {
        super(properties);
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new EldritchPortalBlockEntity(pos, state);
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        if (level.isClientSide) {
            return null;
        }

        return createTickerHelper(type, ThaumcraftMod.ELDRITCH_PORTAL_BLOCK_ENTITY.get(), EldritchPortalBlockEntity::serverTick);
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        boolean active = false;

        if (level.getBlockEntity(pos) instanceof EldritchPortalBlockEntity portal) {
            active = portal.encounterActive();
        }

        int count = active ? 4 : 2;

        for (int i = 0; i < count; i++) {
            level.addParticle(
                    random.nextBoolean() ? ParticleTypes.REVERSE_PORTAL : ParticleTypes.PORTAL,
                    pos.getX() + 0.5D + (random.nextDouble() - 0.5D) * (active ? 1.7D : 1.0D),
                    pos.getY() + 0.6D + random.nextDouble() * 0.7D,
                    pos.getZ() + 0.5D + (random.nextDouble() - 0.5D) * (active ? 1.7D : 1.0D),
                    0.0D,
                    active ? 0.08D : 0.03D,
                    0.0D
            );
        }
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player,
                                 InteractionHand hand, BlockHitResult hit) {
        if (level.isClientSide) {
            return InteractionResult.SUCCESS;
        }

        if (!(level.getBlockEntity(pos) instanceof EldritchPortalBlockEntity portal)) {
            return InteractionResult.PASS;
        }

        ItemStack held = player.getItemInHand(hand);

        if (held.isEmpty()) {
            int warp = PlayerThaumData.getWarp(player);
            int attunement = PlayerThaumData.getEldritchAttunement(player);
            player.displayClientMessage(portal.status(), false);
            player.displayClientMessage(Component.literal("Warp: " + warp + "/" + ThaumcraftConfig.ELDRITCH_PORTAL_REQUIRED_WARP.get() + " | Eldritch Attunement: " + attunement + "/" + ThaumcraftConfig.ELDRITCH_PORTAL_REQUIRED_ATTUNEMENT.get() + " | Use Crimson Key to start arena.").withStyle(ChatFormatting.DARK_PURPLE), false);
            return InteractionResult.CONSUME;
        }

        boolean crimson = held.is(ThaumcraftMod.CRIMSON_KEY.get());
        boolean awakened = held.is(ThaumcraftMod.AWAKENED_CRIMSON_KEY.get());

        if (!crimson && !awakened) {
            player.displayClientMessage(Component.literal("The portal waits for a Crimson Key.").withStyle(ChatFormatting.DARK_PURPLE), false);
            return InteractionResult.CONSUME;
        }

        if (!PlayerThaumData.hasResearch(player, "ELDRITCH_START")) {
            player.displayClientMessage(Component.literal("Research locked: ELDRITCH_START").withStyle(ChatFormatting.RED), false);
            return InteractionResult.CONSUME;
        }

        boolean started = portal.startEncounter(player, awakened);

        if (started && !player.getAbilities().instabuild) {
            held.shrink(1);
        }

        if (started) {
            player.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 20 * 4, 0));

            if (player instanceof ServerPlayer serverPlayer) {
                ThaumcraftNetwork.syncResearch(serverPlayer);
            }

            if (level instanceof ServerLevel serverLevel) {
                serverLevel.sendParticles(ParticleTypes.SOUL_FIRE_FLAME, pos.getX() + 0.5D, pos.getY() + 0.8D, pos.getZ() + 0.5D, 30, 0.8D, 0.5D, 0.8D, 0.04D);
            }
        }

        return InteractionResult.CONSUME;
    }
}
