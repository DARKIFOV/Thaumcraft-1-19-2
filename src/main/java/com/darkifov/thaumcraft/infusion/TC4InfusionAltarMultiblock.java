package com.darkifov.thaumcraft.infusion;

import com.darkifov.thaumcraft.Aspect;
import com.darkifov.thaumcraft.ThaumcraftMod;
import com.darkifov.thaumcraft.block.InfusionPillarBlock;
import com.darkifov.thaumcraft.block.WandItem;
import com.darkifov.thaumcraft.blockentity.ArcanePedestalBlockEntity;
import com.darkifov.thaumcraft.blockentity.InfusionMatrixBlockEntity;
import com.darkifov.thaumcraft.data.PlayerThaumData;
import com.darkifov.thaumcraft.porting.TC4Sounds;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

/** Exact WandManager#createInfusionAltar / fitInfusionAltar adapter. */
public final class TC4InfusionAltarMultiblock {
    public static final int COST_PER_PRIMAL_CENTIVIS = TC4InfusionAltarFullClosureParity.ALTAR_VIS_COST_PER_PRIMAL_CENTIVIS;
    private static final Aspect[] COST_ASPECTS = {
            Aspect.IGNIS, Aspect.TERRA, Aspect.ORDO,
            Aspect.AER, Aspect.PERDITIO, Aspect.AQUA
    };

    private TC4InfusionAltarMultiblock() {
    }

    public static boolean tryCreate(Level level, BlockPos clicked, Player player,
                                    InteractionHand hand, ItemStack wand) {
        if (level == null || clicked == null || player == null || wand == null || wand.isEmpty()) {
            return false;
        }
        // WandManager#performTrigger event 3 is gated by the INFUSION research.
        if (!PlayerThaumData.hasResearch(player, TC4InfusionAltarFullClosureParity.RESEARCH_KEY)) {
            return false;
        }

        for (int ox = clicked.getX() + TC4InfusionAltarFullClosureParity.ALTAR_ORIGIN_SCAN_MIN;
             ox <= clicked.getX() + TC4InfusionAltarFullClosureParity.ALTAR_ORIGIN_SCAN_MAX; ox++) {
            for (int oy = clicked.getY() + TC4InfusionAltarFullClosureParity.ALTAR_ORIGIN_SCAN_MIN;
                 oy <= clicked.getY() + TC4InfusionAltarFullClosureParity.ALTAR_ORIGIN_SCAN_MAX; oy++) {
                for (int oz = clicked.getZ() + TC4InfusionAltarFullClosureParity.ALTAR_ORIGIN_SCAN_MIN;
                     oz <= clicked.getZ() + TC4InfusionAltarFullClosureParity.ALTAR_ORIGIN_SCAN_MAX; oz++) {
                    BlockPos origin = new BlockPos(ox, oy, oz);
                    if (!fits(level, origin)) {
                        continue;
                    }
                    if (!hasAllVis(wand, player)) {
                        return false;
                    }
                    if (level.isClientSide) {
                        return true;
                    }
                    if (!consumeAllVisAtomically(wand, player)) {
                        return false;
                    }
                    replace(level, origin);
                    player.swing(hand, true);
                    return true;
                }
            }
        }
        return false;
    }

    /** Origin is the lower north-west corner of the original 3x3x3 blueprint. */
    public static boolean fits(Level level, BlockPos origin) {
        for (int layerFromTop = 0; layerFromTop < TC4InfusionAltarFullClosureParity.ALTAR_BLUEPRINT_SIZE; layerFromTop++) {
            int y = TC4InfusionAltarFullClosureParity.ALTAR_MATRIX_LAYER_FROM_ORIGIN - layerFromTop;
            for (int x = 0; x < TC4InfusionAltarFullClosureParity.ALTAR_BLUEPRINT_SIZE; x++) {
                for (int z = 0; z < TC4InfusionAltarFullClosureParity.ALTAR_BLUEPRINT_SIZE; z++) {
                    BlockPos pos = origin.offset(x, y, z);
                    if (layerFromTop == 0 && x == TC4InfusionAltarFullClosureParity.ALTAR_CENTER_INDEX && z == TC4InfusionAltarFullClosureParity.ALTAR_CENTER_INDEX) {
                        if (!level.getBlockState(pos).is(ThaumcraftMod.INFUSION_MATRIX.get())) return false;
                    } else if (layerFromTop == 1 && TC4InfusionAltarFullClosureParity.isBlueprintCorner(x, z)) {
                        if (!level.getBlockState(pos).is(ThaumcraftMod.ARCANE_STONE.get())) return false;
                    } else if (layerFromTop == 2 && TC4InfusionAltarFullClosureParity.isBlueprintCorner(x, z)) {
                        if (!level.getBlockState(pos).is(ThaumcraftMod.ARCANE_STONE_BRICKS.get())) return false;
                    } else if (layerFromTop == 2 && x == TC4InfusionAltarFullClosureParity.ALTAR_CENTER_INDEX && z == TC4InfusionAltarFullClosureParity.ALTAR_CENTER_INDEX) {
                        if (!(level.getBlockEntity(pos) instanceof ArcanePedestalBlockEntity)) return false;
                    } else if (!level.isEmptyBlock(pos)) {
                        return false;
                    }
                }
            }
        }
        return true;
    }

    private static boolean hasAllVis(ItemStack wand, Player player) {
        if (player.getAbilities().instabuild || WandItem.hasInfiniteVis(wand)) {
            return true;
        }
        for (Aspect aspect : COST_ASPECTS) {
            int cost = WandItem.modifiedVisCost(wand, player, aspect, COST_PER_PRIMAL_CENTIVIS, true);
            if (WandItem.getVis(wand, aspect) < cost) return false;
        }
        return true;
    }

    private static boolean consumeAllVisAtomically(ItemStack wand, Player player) {
        if (player.getAbilities().instabuild || WandItem.hasInfiniteVis(wand)) {
            return true;
        }
        int[] costs = new int[COST_ASPECTS.length];
        for (int i = 0; i < COST_ASPECTS.length; i++) {
            costs[i] = WandItem.modifiedVisCost(wand, player, COST_ASPECTS[i], COST_PER_PRIMAL_CENTIVIS, true);
            if (WandItem.getVis(wand, COST_ASPECTS[i]) < costs[i]) return false;
        }
        for (int i = 0; i < COST_ASPECTS.length; i++) {
            if (!WandItem.consumeVis(wand, COST_ASPECTS[i], costs[i])) {
                throw new IllegalStateException("Atomic infusion altar vis preflight diverged");
            }
        }
        return true;
    }

    private static void replace(Level level, BlockPos origin) {
        // Original metadata blueprint, in x/z row order: 2,3 / 4,5.
        setPillar(level, origin.offset(0, 0, 0), origin.offset(0, 1, 0), 2);
        setPillar(level, origin.offset(0, 0, 2), origin.offset(0, 1, 2), 3);
        setPillar(level, origin.offset(2, 0, 0), origin.offset(2, 1, 0), 4);
        setPillar(level, origin.offset(2, 0, 2), origin.offset(2, 1, 2), 5);

        BlockPos matrixPos = origin.offset(1, 2, 1);
        if (level.getBlockEntity(matrixPos) instanceof InfusionMatrixBlockEntity matrix) {
            matrix.activateFromMultiblock();
        }
        level.playSound(null, origin, TC4Sounds.event("wand"), SoundSource.BLOCKS, 1.0F, 1.0F);
    }

    private static void setPillar(Level level, BlockPos lower, BlockPos upper, int orientation) {
        net.minecraft.core.Direction facing = InfusionPillarBlock.facingForOriginalOrientation(orientation);
        net.minecraft.world.level.block.state.BlockState lowerState = ThaumcraftMod.INFUSION_PILLAR.get().defaultBlockState()
                .setValue(InfusionPillarBlock.HALF,
                        net.minecraft.world.level.block.state.properties.DoubleBlockHalf.LOWER)
                .setValue(InfusionPillarBlock.FACING, facing);
        level.setBlock(lower, lowerState, 3);
        level.setBlock(upper, lowerState.setValue(InfusionPillarBlock.HALF,
                net.minecraft.world.level.block.state.properties.DoubleBlockHalf.UPPER), 3);
    }
}
