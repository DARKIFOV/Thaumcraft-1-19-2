package com.darkifov.thaumcraft.block;

import com.darkifov.thaumcraft.blockentity.EssentiaTubeBlockEntity;
import com.darkifov.thaumcraft.porting.TC4Sounds;
import com.darkifov.thaumcraft.item.TC4ResonatorItem;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

public class EssentiaValveBlock extends EssentiaTubeBlock {
    public EssentiaValveBlock(Properties properties) {
        super(properties, com.darkifov.thaumcraft.essentia.EssentiaTubeSubtype.VALVE);
    }

    public static boolean isOpen(Level level, BlockPos pos) {
        BlockEntity blockEntity = level.getBlockEntity(pos);
        return blockEntity instanceof EssentiaTubeBlockEntity tube && tube.isFlowAllowed();
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player,
                                 InteractionHand hand, BlockHitResult hit) {
        if (hand != InteractionHand.MAIN_HAND) {
            return InteractionResult.PASS;
        }
        if (player.getItemInHand(hand).getItem() instanceof WandItem) {
            return super.use(state, level, pos, player, hand, hit);
        }
        if (player.getItemInHand(hand).getItem() instanceof TC4ResonatorItem
                || player.getItemInHand(hand).is(asItem())) {
            return InteractionResult.PASS;
        }
        if (!level.isClientSide && level.getBlockEntity(pos) instanceof EssentiaTubeBlockEntity tube) {
            tube.toggleManualFlowLikeTC4();
            level.playSound(null, pos, TC4Sounds.event("squeek"), SoundSource.BLOCKS, 0.7F,
                    0.9F + level.random.nextFloat() * 0.2F);
        }
        return InteractionResult.sidedSuccess(level.isClientSide);
    }
}
