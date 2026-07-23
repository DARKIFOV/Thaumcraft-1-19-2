package com.darkifov.thaumcraft.block;

import com.darkifov.thaumcraft.Aspect;
import com.darkifov.thaumcraft.ThaumcraftMod;
import com.darkifov.thaumcraft.blockentity.InfernalFurnaceBlockEntity;
import com.darkifov.thaumcraft.data.PlayerThaumData;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

import java.util.HashSet;
import java.util.Set;

/** Exact 3x3x3 WandManager#fit/replaceArcaneFurnace adapter. */
public final class InfernalFurnaceMultiblock {
    private static final ThreadLocal<Boolean> RESTORING = ThreadLocal.withInitial(() -> false);

    private InfernalFurnaceMultiblock() {
    }

    public static boolean isRestoring() {
        return RESTORING.get();
    }

    public static boolean tryCreate(Level level, BlockPos clicked, Player player, ItemStack wand) {
        BlockState clickedState = level.getBlockState(clicked);
        if (!clickedState.is(Blocks.OBSIDIAN) && !clickedState.is(Blocks.NETHER_BRICKS)
                && !clickedState.is(Blocks.NETHER_BRICK_FENCE)) {
            return false;
        }
        if (!PlayerThaumData.hasResearch(player, TC4InfernalFurnaceParity.RESEARCH)) {
            return false;
        }
        Match match = findMatch(level, clicked);
        if (match == null) return false;
        if (level.isClientSide) return true;

        int ignis = WandItem.modifiedVisCost(wand, player, Aspect.IGNIS,
                TC4InfernalFurnaceParity.FORMATION_IGNIS_CENTIVIS, true);
        int terra = WandItem.modifiedVisCost(wand, player, Aspect.TERRA,
                TC4InfernalFurnaceParity.FORMATION_TERRA_CENTIVIS, true);
        if (!player.getAbilities().instabuild
                && (!WandItem.hasVis(wand, Aspect.IGNIS, ignis) || !WandItem.hasVis(wand, Aspect.TERRA, terra))) {
            return true;
        }
        if (!player.getAbilities().instabuild) {
            WandItem.consumeVis(wand, Aspect.IGNIS, ignis);
            WandItem.consumeVis(wand, Aspect.TERRA, terra);
        }
        form((ServerLevel) level, match.lower(), match.nozzleFacing());
        return true;
    }

    private static Match findMatch(Level level, BlockPos clicked) {
        for (int dx = -2; dx <= 0; dx++) {
            for (int dy = -2; dy <= 0; dy++) {
                for (int dz = -2; dz <= 0; dz++) {
                    BlockPos lower = clicked.offset(dx, dy, dz);
                    Direction facing = fits(level, lower);
                    if (facing != null) return new Match(lower.immutable(), facing);
                }
            }
        }
        return null;
    }

    /** Returns core->fence direction, or null when the original blueprint does not fit. */
    public static Direction fits(Level level, BlockPos lower) {
        Direction nozzle = null;
        for (int y = 0; y < 3; y++) {
            for (int x = 0; x < 3; x++) {
                for (int z = 0; z < 3; z++) {
                    BlockPos pos = lower.offset(x, y, z);
                    BlockState state = level.getBlockState(pos);
                    boolean topCenter = y == 2 && x == 1 && z == 1;
                    boolean center = y == 1 && x == 1 && z == 1;
                    boolean cardinalMiddle = y == 1 && ((x == 1) ^ (z == 1));
                    Block expected = ((x + z) & 1) == 0 ? Blocks.NETHER_BRICKS : Blocks.OBSIDIAN;
                    if (topCenter) expected = Blocks.AIR;
                    if (center) expected = Blocks.LAVA;
                    if (state.is(expected)) continue;
                    if (cardinalMiddle && state.is(Blocks.NETHER_BRICK_FENCE) && nozzle == null) {
                        nozzle = directionFromLocal(x, z);
                        continue;
                    }
                    return null;
                }
            }
        }
        return nozzle;
    }

    private static Direction directionFromLocal(int x, int z) {
        if (x == 1 && z == 0) return Direction.NORTH;
        if (x == 1 && z == 2) return Direction.SOUTH;
        if (x == 0 && z == 1) return Direction.WEST;
        if (x == 2 && z == 1) return Direction.EAST;
        throw new IllegalArgumentException("Not a cardinal middle position");
    }

    private static void form(ServerLevel level, BlockPos lower, Direction nozzleFacing) {
        RESTORING.set(true);
        try {
            for (int y = 0; y < 3; y++) {
                InfernalFurnaceLayer layer = InfernalFurnaceLayer.values()[y];
                for (int z = 0; z < 3; z++) {
                    for (int x = 0; x < 3; x++) {
                        BlockPos pos = lower.offset(x, y, z);
                        BlockState original = level.getBlockState(pos);
                        if (original.isAir()) continue;
                        int part = TC4InfernalFurnaceParity.partForLocal(x, z);
                        if (original.is(Blocks.LAVA)) part = 0;
                        if (original.is(Blocks.NETHER_BRICK_FENCE)) part = 10;
                        BlockState formed = ThaumcraftMod.INFERNAL_FURNACE.get().defaultBlockState()
                                .setValue(InfernalFurnaceBlock.PART, part)
                                .setValue(InfernalFurnaceBlock.LAYER, layer)
                                .setValue(InfernalFurnaceBlock.FACING, nozzleFacing);
                        level.setBlock(pos, formed, Block.UPDATE_ALL);
                    }
                }
            }
        } finally {
            RESTORING.set(false);
        }
        BlockPos core = lower.offset(1, 1, 1);
        if (level.getBlockEntity(core) instanceof InfernalFurnaceBlockEntity furnace) {
            furnace.refreshFacing();
        }
        level.playSound(null, lower.getX() + 0.5D, lower.getY() + 0.5D, lower.getZ() + 0.5D,
                com.darkifov.thaumcraft.porting.TC4Sounds.event("wand"), SoundSource.BLOCKS, 1.0F, 1.0F);
    }

    public static BlockPos findCore(Level level, BlockPos around) {
        for (int dx = -2; dx <= 2; dx++) {
            for (int dy = -2; dy <= 2; dy++) {
                for (int dz = -2; dz <= 2; dz++) {
                    BlockPos pos = around.offset(dx, dy, dz);
                    BlockState state = level.getBlockState(pos);
                    if (state.is(ThaumcraftMod.INFERNAL_FURNACE.get())
                            && state.getValue(InfernalFurnaceBlock.PART) == 0) return pos.immutable();
                }
            }
        }
        return null;
    }

    public static void dismantle(ServerLevel level, BlockPos core, boolean spawnBlaze) {
        if (core == null || isRestoring()) return;
        BlockPos lower = core.offset(-1, -1, -1);
        Set<BlockPos> changed = new HashSet<>();
        RESTORING.set(true);
        try {
            for (int y = 0; y < 3; y++) {
                for (int z = 0; z < 3; z++) {
                    for (int x = 0; x < 3; x++) {
                        BlockPos pos = lower.offset(x, y, z);
                        BlockState state = level.getBlockState(pos);
                        if (!state.is(ThaumcraftMod.INFERNAL_FURNACE.get())) continue;
                        int part = state.getValue(InfernalFurnaceBlock.PART);
                        BlockState restored;
                        if (part == 0) restored = Blocks.AIR.defaultBlockState();
                        else if (part == 10) restored = Blocks.NETHER_BRICK_FENCE.defaultBlockState();
                        else if (TC4InfernalFurnaceParity.originalPartRestoresObsidian(part)) restored = Blocks.OBSIDIAN.defaultBlockState();
                        else restored = Blocks.NETHER_BRICKS.defaultBlockState();
                        level.setBlock(pos, restored, Block.UPDATE_ALL);
                        changed.add(pos.immutable());
                    }
                }
            }
        } finally {
            RESTORING.set(false);
        }
        if (spawnBlaze) InfernalFurnaceBlock.spawnPunishmentBlaze(level, core);
        for (BlockPos pos : changed) level.updateNeighborsAt(pos, level.getBlockState(pos).getBlock());
    }

    private record Match(BlockPos lower, Direction nozzleFacing) {
    }
}
