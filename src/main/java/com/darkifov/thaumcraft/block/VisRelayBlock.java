package com.darkifov.thaumcraft.block;

import com.darkifov.thaumcraft.aura.AuraVisRelayNetwork;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * TC4 energized-node relay hardware.
 *
 * A relay is intentionally a normal block, not a large machine. It finds an energized
 * Aura Node through relay adjacency and lets wands draw primal vis through the network.
 */
public class VisRelayBlock extends Block {
    private static final VoxelShape SHAPE = Block.box(3.0D, 3.0D, 3.0D, 13.0D, 13.0D, 13.0D);

    public VisRelayBlock(BlockBehaviour.Properties properties) {
        super(properties);
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        if (random.nextFloat() < 0.35F) {
            level.addParticle(ParticleTypes.ENCHANT,
                    pos.getX() + 0.5D + (random.nextDouble() - 0.5D) * 0.45D,
                    pos.getY() + 0.5D + (random.nextDouble() - 0.5D) * 0.45D,
                    pos.getZ() + 0.5D + (random.nextDouble() - 0.5D) * 0.45D,
                    0.0D, 0.01D, 0.0D);
        }
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        ItemStack stack = player.getItemInHand(hand);
        if (!(stack.getItem() instanceof WandItem)) {
            return InteractionResult.PASS;
        }

        if (!level.isClientSide() && level instanceof ServerLevel serverLevel) {
            int moved = AuraVisRelayNetwork.chargeWandFromRelay(stack, serverLevel, pos, player, true);
            if (moved > 0) {
                player.displayClientMessage(Component.literal("Relay moved " + moved + " primal vis into the wand.").withStyle(ChatFormatting.AQUA), true);
            }
        }

        return InteractionResult.sidedSuccess(level.isClientSide());
    }
}
