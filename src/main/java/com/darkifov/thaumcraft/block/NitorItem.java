package com.darkifov.thaumcraft.block;

import com.darkifov.thaumcraft.ThaumcraftMod;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;

/**
 * TC4 parity target: the resource item "Nitor" can be placed directly in the world.
 * The placed world block is internal/hidden and drops the same Nitor item again.
 */
public class NitorItem extends Item {
    public NitorItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        BlockPlaceContext placeContext = new BlockPlaceContext(context);
        BlockPos placePos = placeContext.getClickedPos();
        Player player = context.getPlayer();
        BlockState nitorState = ThaumcraftMod.NITOR_LIGHT.get().defaultBlockState();

        if (!level.getWorldBorder().isWithinBounds(placePos)) {
            return InteractionResult.FAIL;
        }
        if (player != null && !level.mayInteract(player, placePos)) {
            return InteractionResult.FAIL;
        }
        if (!level.getBlockState(placePos).canBeReplaced(placeContext)) {
            return InteractionResult.FAIL;
        }
        if (!nitorState.canSurvive(level, placePos)) {
            return InteractionResult.FAIL;
        }

        if (!level.isClientSide) {
            level.setBlock(placePos, nitorState, 11);
            level.gameEvent(player, GameEvent.BLOCK_PLACE, placePos);
            if (player == null || !player.getAbilities().instabuild) {
                context.getItemInHand().shrink(1);
            }
        }

        level.playSound(player, placePos, SoundEvents.FIRECHARGE_USE, SoundSource.BLOCKS, 0.55F, 1.15F);
        return InteractionResult.sidedSuccess(level.isClientSide);
    }
}
