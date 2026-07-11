package com.darkifov.thaumcraft.block;

import com.darkifov.thaumcraft.blockentity.ArcanePedestalBlockEntity;
import com.darkifov.thaumcraft.infusion.InfusionAltarStructure;
import com.darkifov.thaumcraft.infusion.InfusionProcessHelper;
import com.darkifov.thaumcraft.infusion.InfusionStructureReport;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

public class InfusionMatrixAuxiliaryBlock extends Block {
    public enum Mode {
        ACCELERATOR,
        STABILIZER
    }

    private final Mode mode;

    public InfusionMatrixAuxiliaryBlock(Properties properties, Mode mode) {
        super(properties);
        this.mode = mode;
    }

    public Mode mode() {
        return mode;
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        if (!level.isClientSide) {
            BlockPos matrix = InfusionProcessHelper.findNearbyMatrix(level, pos, 6);

            if (matrix == null) {
                player.displayClientMessage(Component.translatable("thaumcraft.infusion.aux.missing", displayName())
                        .withStyle(ChatFormatting.GRAY), false);
                return InteractionResult.SUCCESS;
            }

            ArcanePedestalBlockEntity catalyst = InfusionProcessHelper.findCatalystPedestal(level, matrix);
            InfusionStructureReport report = InfusionAltarStructure.analyze(level, matrix, catalyst);

            if (mode == Mode.ACCELERATOR) {
                player.displayClientMessage(Component.translatable("thaumcraft.infusion.aux.accelerator",
                                report.matrixAccelerators(), report.speedMultiplier(), report.durationModifierPercent())
                        .withStyle(ChatFormatting.AQUA), false);
            } else {
                player.displayClientMessage(Component.translatable("thaumcraft.infusion.aux.stabilizer",
                                report.matrixStabilizers(), report.matrixStabilizationPercent())
                        .withStyle(ChatFormatting.GREEN), false);
            }

            player.displayClientMessage(report.summary(), false);
        }

        return InteractionResult.SUCCESS;
    }

    private Component displayName() {
        return Component.translatable(mode == Mode.ACCELERATOR
                ? "thaumcraft.infusion.aux.accelerator_name"
                : "thaumcraft.infusion.aux.stabilizer_name");
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        if (random.nextFloat() > 0.55F) {
            return;
        }

        double x = pos.getX() + 0.5D + (random.nextDouble() - 0.5D) * 0.55D;
        double y = pos.getY() + 0.75D + random.nextDouble() * 0.25D;
        double z = pos.getZ() + 0.5D + (random.nextDouble() - 0.5D) * 0.55D;

        if (mode == Mode.ACCELERATOR) {
            level.addParticle(ParticleTypes.ELECTRIC_SPARK, x, y, z, 0.0D, 0.02D, 0.0D);
        } else {
            level.addParticle(ParticleTypes.ENCHANT, x, y, z, 0.0D, 0.01D, 0.0D);
        }
    }
}
