package com.darkifov.thaumcraft.block;

import com.darkifov.thaumcraft.ThaumcraftMod;
import com.darkifov.thaumcraft.data.PlayerThaumData;
import com.darkifov.thaumcraft.network.ThaumcraftNetwork;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.monster.EnderMan;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

public class EldritchAltarBlock extends Block {
    public EldritchAltarBlock(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player,
                                 InteractionHand hand, BlockHitResult hit) {
        ItemStack held = player.getItemInHand(hand);

        if (level.isClientSide) {
            return InteractionResult.SUCCESS;
        }

        if (held.isEmpty()) {
            player.displayClientMessage(structureReport(level, pos), false);
            player.displayClientMessage(Component.literal("Use Eldritch Eye or Crimson Key to open the first portal.").withStyle(ChatFormatting.GRAY), false);
            return InteractionResult.CONSUME;
        }

        boolean validItem = held.is(ThaumcraftMod.ELDRITCH_EYE.get()) || held.is(ThaumcraftMod.CRIMSON_KEY.get());

        if (!validItem) {
            player.displayClientMessage(Component.literal("The altar waits for an Eldritch Eye or Crimson Key.").withStyle(ChatFormatting.DARK_PURPLE), false);
            return InteractionResult.CONSUME;
        }

        if (!validStructure(level, pos)) {
            player.displayClientMessage(Component.literal("The obelisk structure is incomplete.").withStyle(ChatFormatting.RED), false);
            player.displayClientMessage(structureReport(level, pos), false);
            return InteractionResult.CONSUME;
        }

        if (!PlayerThaumData.hasResearch(player, "ELDRITCH_WHISPERS")) {
            player.displayClientMessage(Component.literal("Research locked: ELDRITCH_WHISPERS").withStyle(ChatFormatting.RED), false);
            return InteractionResult.CONSUME;
        }

        if (PlayerThaumData.getWarp(player) < 12) {
            player.displayClientMessage(Component.literal("Your mind is not warped enough to open this door.").withStyle(ChatFormatting.RED), false);
            return InteractionResult.CONSUME;
        }

        openPortal(level, pos, player);

        if (!player.getAbilities().instabuild) {
            held.shrink(1);
        }

        PlayerThaumData.addWarp(player, 3);
        PlayerThaumData.addEldritchAttunement(player, 15);
        PlayerThaumData.unlockResearch(player, "ELDRITCH_START");

        if (player instanceof ServerPlayer serverPlayer) {
            ThaumcraftNetwork.syncResearch(serverPlayer);
        }

        return InteractionResult.CONSUME;
    }

    private Component structureReport(Level level, BlockPos pos) {
        int stones = 0;
        int obelisks = 0;

        for (BlockPos scan : BlockPos.betweenClosed(pos.offset(-2, -1, -2), pos.offset(2, 3, 2))) {
            BlockState state = level.getBlockState(scan);

            if (state.is(ThaumcraftMod.ELDRITCH_STONE.get())) {
                stones++;
            }

            if (state.is(ThaumcraftMod.ELDRITCH_OBELISK.get())) {
                obelisks++;
            }
        }

        return Component.literal("Eldritch Altar | Structure: stones " + stones + "/8, obelisks " + obelisks + "/4")
                .withStyle(stones >= 8 && obelisks >= 4 ? ChatFormatting.GREEN : ChatFormatting.RED);
    }

    private boolean validStructure(Level level, BlockPos pos) {
        int stones = 0;
        int obelisks = 0;

        for (BlockPos scan : BlockPos.betweenClosed(pos.offset(-2, -1, -2), pos.offset(2, 3, 2))) {
            BlockState state = level.getBlockState(scan);

            if (state.is(ThaumcraftMod.ELDRITCH_STONE.get())) {
                stones++;
            }

            if (state.is(ThaumcraftMod.ELDRITCH_OBELISK.get())) {
                obelisks++;
            }
        }

        return stones >= 8 && obelisks >= 4;
    }

    private void openPortal(Level level, BlockPos pos, Player player) {
        BlockPos portalPos = pos.above();

        level.setBlock(portalPos, ThaumcraftMod.ELDRITCH_PORTAL.get().defaultBlockState(), 3);

        if (level instanceof ServerLevel serverLevel) {
            serverLevel.sendParticles(ParticleTypes.REVERSE_PORTAL, portalPos.getX() + 0.5D, portalPos.getY() + 0.5D, portalPos.getZ() + 0.5D, 90, 1.2D, 0.8D, 1.2D, 0.1D);
            serverLevel.sendParticles(ParticleTypes.SOUL_FIRE_FLAME, portalPos.getX() + 0.5D, portalPos.getY() + 0.5D, portalPos.getZ() + 0.5D, 40, 0.7D, 0.5D, 0.7D, 0.03D);
            serverLevel.playSound(null, pos, SoundEvents.AMBIENT_CAVE, SoundSource.BLOCKS, 1.0F, 0.6F);

            EnderMan sentinel = EntityType.ENDERMAN.create(serverLevel);

            if (sentinel != null) {
                sentinel.moveTo(pos.getX() + 0.5D, pos.getY() + 1.0D, pos.getZ() + 2.5D, 180.0F, 0.0F);
                sentinel.setTarget(player);
                serverLevel.addFreshEntity(sentinel);
            }
        }

        player.displayClientMessage(Component.literal("The first Eldritch door opens.").withStyle(ChatFormatting.DARK_PURPLE), false);
    }
}
