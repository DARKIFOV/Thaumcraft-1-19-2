package com.darkifov.thaumcraft.block;

import com.darkifov.thaumcraft.ThaumcraftMod;
import com.darkifov.thaumcraft.blockentity.AuraNodeBlockEntity;
import com.darkifov.thaumcraft.data.NodeScanData;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
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


public class AuraNodeBlock extends BaseEntityBlock {
    public AuraNodeBlock(Properties properties) {
        super(properties);
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new AuraNodeBlockEntity(pos, state);
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

        return createTickerHelper(type, ThaumcraftMod.AURA_NODE_BLOCK_ENTITY.get(), AuraNodeBlockEntity::serverTick);
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        if (random.nextFloat() < 0.65F) {
            double x = pos.getX() + 0.5D + (random.nextDouble() - 0.5D) * 0.85D;
            double y = pos.getY() + 0.5D + (random.nextDouble() - 0.5D) * 0.85D;
            double z = pos.getZ() + 0.5D + (random.nextDouble() - 0.5D) * 0.85D;
            level.addParticle(ParticleTypes.ENCHANT, x, y, z, 0.0D, 0.02D, 0.0D);
        }

        if (random.nextFloat() < 0.18F) {
            level.addParticle(ParticleTypes.END_ROD, pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D, 0.0D, 0.01D, 0.0D);
        }
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player,
                                 InteractionHand hand, BlockHitResult hit) {
        BlockEntity blockEntity = level.getBlockEntity(pos);

        if (!(blockEntity instanceof AuraNodeBlockEntity node)) {
            return InteractionResult.PASS;
        }

        ItemStack held = player.getItemInHand(hand);

        if (held.getItem() instanceof WandItem wand) {
            if (!level.isClientSide) {
                int moved = wand.chargeFromNode(held, node);

                if (moved > 0) {
                    player.displayClientMessage(Component.literal("Wand charged from aura node: +" + moved + " vis").withStyle(ChatFormatting.AQUA), false);
                } else {
                    player.displayClientMessage(Component.literal("The wand is full or this node has no usable vis.").withStyle(ChatFormatting.GRAY), false);
                }
            }

            return InteractionResult.sidedSuccess(level.isClientSide);
        }

        if (!level.isClientSide) {
            if (!node.initialized()) {
                node.initializeFromPosition();
            }

            if (!NodeScanData.hasScanned(player, pos) && !player.getAbilities().instabuild) {
                player.displayClientMessage(Component.literal("Unknown Aura Node. Scan it with a Thaumometer to reveal details.").withStyle(ChatFormatting.DARK_PURPLE), false);
                return InteractionResult.CONSUME;
            }

            String stableText = node.isStabilized() ? " | Stabilized" : "";
            player.displayClientMessage(Component.literal("Aura Node [" + node.nodeType() + stableText + "] aspects: ").append(node.aspects().toComponent()), false);
        }

        return InteractionResult.sidedSuccess(level.isClientSide);
    }
}
