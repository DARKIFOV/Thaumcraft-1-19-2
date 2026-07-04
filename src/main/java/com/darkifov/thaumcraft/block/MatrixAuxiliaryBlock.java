package com.darkifov.thaumcraft.block;

import com.darkifov.thaumcraft.blockentity.ArcanePedestalBlockEntity;
import com.darkifov.thaumcraft.infusion.InfusionAltarStructure;
import com.darkifov.thaumcraft.infusion.InfusionMatrixAuxiliaryHelper;
import com.darkifov.thaumcraft.infusion.MatrixAuxiliaryReport;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

public class MatrixAuxiliaryBlock extends Block {
    private final Mode mode;

    public MatrixAuxiliaryBlock(Properties properties, Mode mode) {
        super(properties);
        this.mode = mode;
    }

    public Mode mode() {
        return mode;
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, net.minecraft.world.entity.player.Player player, net.minecraft.world.InteractionHand hand, net.minecraft.world.phys.BlockHitResult hit) {
        if (!level.isClientSide) {
            player.displayClientMessage(Component.literal(mode.displayName()).withStyle(mode.color()), false);
            player.displayClientMessage(Component.literal(mode.description()).withStyle(ChatFormatting.GRAY), false);
        }

        return InteractionResult.SUCCESS;
    }

    public enum Mode {
        ACCELERATOR("Infusion Matrix Accelerator", ChatFormatting.AQUA,
                "Speeds up active infusion. One accelerator gives x2, four nearby accelerators cap at x5."),
        STABILIZER("Matrix Stabilization Pylon", ChatFormatting.LIGHT_PURPLE,
                "Stabilizes active infusion by 25% per symmetric powered pylon, up to 100% with four.");

        private final String displayName;
        private final ChatFormatting color;
        private final String description;

        Mode(String displayName, ChatFormatting color, String description) {
            this.displayName = displayName;
            this.color = color;
            this.description = description;
        }

        public String displayName() {
            return displayName;
        }

        public ChatFormatting color() {
            return color;
        }

        public String description() {
            return description;
        }
    }
}
