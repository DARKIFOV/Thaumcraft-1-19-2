package com.darkifov.thaumcraft.block;

import com.darkifov.thaumcraft.ThaumcraftMod;
import com.darkifov.thaumcraft.blockentity.ArcanePedestalBlockEntity;
import com.darkifov.thaumcraft.blockentity.InfusionMatrixBlockEntity;
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
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;


public class InfusionMatrixBlock extends BaseEntityBlock {
    public InfusionMatrixBlock(Properties properties) {
        super(properties);
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new InfusionMatrixBlockEntity(pos, state);
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

        return createTickerHelper(type, ThaumcraftMod.INFUSION_MATRIX_BLOCK_ENTITY.get(), InfusionMatrixBlockEntity::serverTick);
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        boolean active = false;

        if (level.getBlockEntity(pos) instanceof InfusionMatrixBlockEntity matrix) {
            active = matrix.active();
        }

        if (random.nextFloat() < (active ? 0.9F : 0.45F)) {
            double x = pos.getX() + 0.5D + (random.nextDouble() - 0.5D) * (active ? 2.0D : 1.4D);
            double y = pos.getY() + 0.5D + random.nextDouble() * 0.9D;
            double z = pos.getZ() + 0.5D + (random.nextDouble() - 0.5D) * (active ? 2.0D : 1.4D);
            level.addParticle(ParticleTypes.ENCHANT, x, y, z, 0.0D, active ? 0.06D : 0.03D, 0.0D);
        }

        if (random.nextFloat() < (active ? 0.45F : 0.18F)) {
            level.addParticle(ParticleTypes.END_ROD, pos.getX() + 0.5D, pos.getY() + 0.85D, pos.getZ() + 0.5D, 0.0D, 0.02D, 0.0D);
        }

        if (active && random.nextFloat() < 0.25F) {
            level.addParticle(ParticleTypes.WITCH, pos.getX() + 0.5D, pos.getY() + 0.55D, pos.getZ() + 0.5D, 0.0D, 0.01D, 0.0D);
        }
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player,
                                 InteractionHand hand, BlockHitResult hit) {
        ItemStack held = player.getItemInHand(hand);

        if (level.isClientSide) {
            return InteractionResult.SUCCESS;
        }

        if (!(level.getBlockEntity(pos) instanceof InfusionMatrixBlockEntity matrix)) {
            return InteractionResult.PASS;
        }

        if (held.isEmpty()) {
            if (matrix.active()) {
                player.displayClientMessage(matrix.statusComponent(), false);

                if (player.isShiftKeyDown()) {
                    matrix.cancelInfusion(player);
                }

                return InteractionResult.CONSUME;
            }

            ArcanePedestalBlockEntity catalystPedestal = InfusionProcessHelper.findCatalystPedestal(level, pos);
            InfusionStructureReport report = InfusionAltarStructure.analyze(level, pos, catalystPedestal);
            player.displayClientMessage(report.summary(), false);
            player.displayClientMessage(Component.literal("Start with wand. Shift + empty hand while active cancels.").withStyle(ChatFormatting.GRAY), false);
            return InteractionResult.CONSUME;
        }

        if (!(held.getItem() instanceof WandItem)) {
            player.displayClientMessage(Component.literal("Use empty hand to inspect or a wand to start infusion.").withStyle(ChatFormatting.LIGHT_PURPLE), false);
            return InteractionResult.CONSUME;
        }

        matrix.startInfusion(player);
        return InteractionResult.CONSUME;
    }
}
