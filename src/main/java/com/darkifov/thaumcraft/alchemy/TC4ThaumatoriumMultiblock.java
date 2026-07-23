package com.darkifov.thaumcraft.alchemy;

import com.darkifov.thaumcraft.Aspect;
import com.darkifov.thaumcraft.ThaumcraftMod;
import com.darkifov.thaumcraft.block.ThaumatoriumBlock;
import com.darkifov.thaumcraft.block.WandItem;
import com.darkifov.thaumcraft.data.PlayerThaumData;
import com.darkifov.thaumcraft.porting.TC4Sounds;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;

/** Exact modern adapter for WandManager#createThaumatorium. */
public final class TC4ThaumatoriumMultiblock {
    private static final Aspect[] VIS_ASPECTS = {Aspect.IGNIS, Aspect.ORDO, Aspect.AQUA};
    private static final int[] VIS_COSTS = {
            TC4ThaumatoriumParity.FIRE_VIS_CENTIVIS,
            TC4ThaumatoriumParity.ORDER_VIS_CENTIVIS,
            TC4ThaumatoriumParity.WATER_VIS_CENTIVIS
    };

    private TC4ThaumatoriumMultiblock() {
    }

    public static boolean tryCreate(Level level, BlockPos clicked, Player player, InteractionHand hand,
                                    ItemStack wand, Direction clickedFace) {
        if (level == null || clicked == null || player == null || wand == null || wand.isEmpty()
                || clickedFace == null || !PlayerThaumData.hasResearch(player, TC4ThaumatoriumParity.RESEARCH_KEY)) {
            return false;
        }
        BlockPos lower = normalizeLower(level, clicked);
        if (lower == null
                || !level.getBlockState(lower).is(ThaumcraftMod.TC4_ALCHEMICAL_CONSTRUCT.get())
                || !level.getBlockState(lower.above()).is(ThaumcraftMod.TC4_ALCHEMICAL_CONSTRUCT.get())
                || !level.getBlockState(lower.below()).is(ThaumcraftMod.CRUCIBLE.get())) {
            return false;
        }
        if (!hasAllVis(wand, player)) {
            return false;
        }
        if (level.isClientSide) {
            return true;
        }
        consumeAllVis(wand, player);
        Direction facing = clickedFace;
        level.setBlock(lower, ThaumcraftMod.THAUMATORIUM.get().defaultBlockState()
                .setValue(ThaumatoriumBlock.FACING, facing), Block.UPDATE_ALL);
        level.setBlock(lower.above(), ThaumcraftMod.THAUMATORIUM_UPPER.get().defaultBlockState(), Block.UPDATE_ALL);
        level.playSound(null, lower, TC4Sounds.event("wand"), SoundSource.BLOCKS, 1.0F, 1.0F);
        if (level instanceof ServerLevel serverLevel) {
            serverLevel.sendParticles(ParticleTypes.END_ROD,
                    lower.getX() + 0.5D, lower.getY() + 1.0D, lower.getZ() + 0.5D,
                    24, 0.45D, 0.85D, 0.45D, 0.02D);
        }
        player.swing(hand, true);
        return true;
    }

    private static BlockPos normalizeLower(Level level, BlockPos clicked) {
        if (level.getBlockState(clicked).is(ThaumcraftMod.TC4_ALCHEMICAL_CONSTRUCT.get())
                && level.getBlockState(clicked.below()).is(ThaumcraftMod.CRUCIBLE.get())) {
            return clicked;
        }
        BlockPos below = clicked.below();
        return level.getBlockState(below).is(ThaumcraftMod.TC4_ALCHEMICAL_CONSTRUCT.get())
                && level.getBlockState(below.below()).is(ThaumcraftMod.CRUCIBLE.get()) ? below : null;
    }

    private static boolean hasAllVis(ItemStack wand, Player player) {
        if (player.getAbilities().instabuild || WandItem.hasInfiniteVis(wand)) {
            return true;
        }
        for (int i = 0; i < VIS_ASPECTS.length; i++) {
            int cost = WandItem.modifiedVisCost(wand, player, VIS_ASPECTS[i], VIS_COSTS[i], true);
            if (WandItem.getVis(wand, VIS_ASPECTS[i]) < cost) {
                return false;
            }
        }
        return true;
    }

    private static void consumeAllVis(ItemStack wand, Player player) {
        if (player.getAbilities().instabuild || WandItem.hasInfiniteVis(wand)) {
            return;
        }
        for (int i = 0; i < VIS_ASPECTS.length; i++) {
            int cost = WandItem.modifiedVisCost(wand, player, VIS_ASPECTS[i], VIS_COSTS[i], true);
            if (!WandItem.consumeVis(wand, VIS_ASPECTS[i], cost)) {
                throw new IllegalStateException("Thaumatorium vis preflight diverged");
            }
        }
    }
}
