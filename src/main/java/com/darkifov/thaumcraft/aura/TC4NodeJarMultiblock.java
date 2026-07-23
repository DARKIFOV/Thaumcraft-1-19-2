package com.darkifov.thaumcraft.aura;

import com.darkifov.thaumcraft.Aspect;
import com.darkifov.thaumcraft.ThaumcraftMod;
import com.darkifov.thaumcraft.block.WandItem;
import com.darkifov.thaumcraft.blockentity.AuraNodeBlockEntity;
import com.darkifov.thaumcraft.blockentity.NodeJarBlockEntity;
import com.darkifov.thaumcraft.porting.TC4Sounds;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

/**
 * TC4 WandManager#createNodeJar / fitNodeJar / replaceNodeJar adapter.
 *
 * The original ritual is a 3x4x3 structure: nine wooden slabs on top and
 * three complete glass layers around a live aura node. Activating any part of
 * the structure with a wand consumes 70 centivis of every primal aspect and
 * collapses the structure into a portable Node in a Jar block.
 */
public final class TC4NodeJarMultiblock {
    public static final int PRIMAL_COST_CENTIVIS = 70;
    private static final Aspect[] COST = {
            Aspect.IGNIS, Aspect.TERRA, Aspect.ORDO,
            Aspect.AER, Aspect.PERDITIO, Aspect.AQUA
    };

    private TC4NodeJarMultiblock() {
    }

    /** Returns true when a complete structure was found, even if vis was insufficient. */
    public static boolean tryCreate(Level level, BlockPos clickedPos, Player player, InteractionHand hand, ItemStack wandStack) {
        Structure structure = find(level, clickedPos);
        if (structure == null) {
            return false;
        }
        if (level.isClientSide) {
            return true;
        }
        if (!(level instanceof ServerLevel serverLevel)) {
            return true;
        }
        if (!consumeCost(wandStack, player)) {
            return true;
        }

        CompoundTag captured = TC4NodeJarRuntime.capture(serverLevel, structure.node());
        for (BlockPos position : structure.materialBlocks()) {
            serverLevel.removeBlock(position, false);
        }
        BlockPos nodePos = structure.nodePos();
        serverLevel.setBlock(nodePos, ThaumcraftMod.NODE_JAR_BLOCK.get().defaultBlockState(), 3);
        if (serverLevel.getBlockEntity(nodePos) instanceof NodeJarBlockEntity jar) {
            jar.setNodeTag(captured);
            jar.startCaptureAnimation();
        }

        for (BlockPos position : structure.allPositions()) {
            serverLevel.sendParticles(ParticleTypes.END_ROD,
                    position.getX() + 0.5D, position.getY() + 0.5D, position.getZ() + 0.5D,
                    2, 0.28D, 0.28D, 0.28D, 0.01D);
        }
        serverLevel.playSound(null, nodePos, TC4Sounds.event("wand"), SoundSource.BLOCKS, 1.0F, 1.0F);
        player.swing(hand);
        player.displayClientMessage(Component.translatable("thaumcraft.nodejar.ritual_complete")
                .withStyle(ChatFormatting.AQUA), true);
        return true;
    }

    @Nullable
    static Structure find(Level level, BlockPos clickedPos) {
        // WandManager scans origins x-2..x, y-3..y, z-2..z. Scanning the
        // equivalent nearby node centers is clearer in the modern port.
        for (int dy = -3; dy <= 0; dy++) {
            for (int dx = -1; dx <= 1; dx++) {
                for (int dz = -1; dz <= 1; dz++) {
                    BlockPos nodePos = clickedPos.offset(dx, dy, dz);
                    if (level.getBlockEntity(nodePos) instanceof AuraNodeBlockEntity node) {
                        Structure candidate = validate(level, nodePos, node);
                        if (candidate != null) {
                            return candidate;
                        }
                    }
                }
            }
        }
        return null;
    }

    @Nullable
    private static Structure validate(Level level, BlockPos nodePos, AuraNodeBlockEntity node) {
        List<BlockPos> materials = new ArrayList<>(35);
        List<BlockPos> all = new ArrayList<>(36);

        for (int y = -1; y <= 2; y++) {
            for (int x = -1; x <= 1; x++) {
                for (int z = -1; z <= 1; z++) {
                    BlockPos pos = nodePos.offset(x, y, z);
                    all.add(pos.immutable());
                    if (x == 0 && y == 0 && z == 0) {
                        continue;
                    }
                    BlockState state = level.getBlockState(pos);
                    boolean matches = y == 2 ? state.is(BlockTags.WOODEN_SLABS) : state.is(Blocks.GLASS);
                    if (!matches) {
                        return null;
                    }
                    materials.add(pos.immutable());
                }
            }
        }
        return new Structure(nodePos.immutable(), node, List.copyOf(materials), List.copyOf(all));
    }

    private static boolean consumeCost(ItemStack wandStack, Player player) {
        if (player.getAbilities().instabuild || WandItem.hasInfiniteVis(wandStack)) {
            return true;
        }
        for (Aspect aspect : COST) {
            int cost = WandItem.modifiedVisCost(wandStack, player, aspect, PRIMAL_COST_CENTIVIS, true);
            if (!WandItem.hasVis(wandStack, aspect, cost)) {
                player.displayClientMessage(Component.translatable("thaumcraft.nodejar.insufficient_vis",
                        aspect.displayName(), WandItem.formatVis(cost)).withStyle(ChatFormatting.RED), false);
                return false;
            }
        }
        for (Aspect aspect : COST) {
            WandItem.consumeVisCost(wandStack, player, aspect, PRIMAL_COST_CENTIVIS, true);
        }
        return true;
    }

    record Structure(BlockPos nodePos, AuraNodeBlockEntity node,
                     List<BlockPos> materialBlocks, List<BlockPos> allPositions) {
    }
}
