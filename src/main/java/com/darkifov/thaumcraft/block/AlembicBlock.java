package com.darkifov.thaumcraft.block;

import com.darkifov.thaumcraft.Aspect;
import com.darkifov.thaumcraft.blockentity.AlembicBlockEntity;
import com.darkifov.thaumcraft.blockentity.CrucibleBlockEntity;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

public class AlembicBlock extends BaseEntityBlock {
    public static final int TRANSFER_AMOUNT = 8;

    public AlembicBlock(Properties properties) {
        super(properties);
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new AlembicBlockEntity(pos, state);
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player,
                                 InteractionHand hand, BlockHitResult hit) {
        ItemStack held = player.getItemInHand(hand);

        if (level.isClientSide) {
            return InteractionResult.SUCCESS;
        }

        BlockEntity blockEntity = level.getBlockEntity(pos);

        if (!(blockEntity instanceof AlembicBlockEntity alembic)) {
            return InteractionResult.PASS;
        }

        if (held.isEmpty()) {
            player.displayClientMessage(
                    Component.literal("Alembic | Capacity: " + alembic.aspects().totalAmount() + "/" + AlembicBlockEntity.CAPACITY + " | Aspects: ")
                            .append(alembic.aspects().toComponent()),
                    false
            );
            return InteractionResult.CONSUME;
        }

        if (held.getItem() instanceof EssentiaPhialItem) {
            return handlePhial(player, held, alembic);
        }

        if (held.getItem() == Items.BLAZE_POWDER) {
            BlockEntity below = level.getBlockEntity(pos.below());

            if (!(below instanceof CrucibleBlockEntity crucible)) {
                player.displayClientMessage(Component.literal("Place the alembic above a crucible.").withStyle(ChatFormatting.GRAY), false);
                return InteractionResult.CONSUME;
            }

            Aspect first = crucible.aspects().firstAspect();

            if (first == null) {
                player.displayClientMessage(Component.literal("The crucible has no essentia to distill.").withStyle(ChatFormatting.GRAY), false);
                return InteractionResult.CONSUME;
            }

            if (!alembic.canAccept(first)) {
                player.displayClientMessage(Component.literal("The alembic already contains another aspect.").withStyle(ChatFormatting.RED), false);
                return InteractionResult.CONSUME;
            }

            int removed = crucible.aspects().removeUpTo(first, Math.min(TRANSFER_AMOUNT, alembic.spaceLeft()));

            if (removed <= 0) {
                player.displayClientMessage(Component.literal("The alembic is full.").withStyle(ChatFormatting.RED), false);
                return InteractionResult.CONSUME;
            }

            alembic.addEssentia(first, removed);
            crucible.setChangedAndSync();

            if (!player.getAbilities().instabuild) {
                held.shrink(1);
            }

            player.displayClientMessage(
                    Component.literal("Distilled ").append(Component.literal(first.displayName() + " x" + removed).withStyle(style -> style.withColor(first.textColor())))
                            .append(Component.literal(" from crucible.")),
                    false
            );

            return InteractionResult.CONSUME;
        }

        return InteractionResult.PASS;
    }

    private InteractionResult handlePhial(Player player, ItemStack held, AlembicBlockEntity alembic) {
        Aspect heldAspect = EssentiaPhialItem.getAspect(held);
        int heldAmount = EssentiaPhialItem.getAmount(held);

        if (heldAspect != null && heldAmount > 0) {
            if (!alembic.canAccept(heldAspect)) {
                player.displayClientMessage(Component.literal("The alembic already contains another aspect.").withStyle(ChatFormatting.RED), false);
                return InteractionResult.CONSUME;
            }

            int added = alembic.addEssentia(heldAspect, heldAmount);

            if (added <= 0) {
                player.displayClientMessage(Component.literal("The alembic is full.").withStyle(ChatFormatting.RED), false);
                return InteractionResult.CONSUME;
            }

            EssentiaPhialItem.clear(held);

            player.displayClientMessage(
                    Component.literal("Poured ").append(Component.literal(heldAspect.displayName() + " x" + added).withStyle(style -> style.withColor(heldAspect.textColor())))
                            .append(Component.literal(" into alembic.")),
                    false
            );

            return InteractionResult.CONSUME;
        }

        Aspect first = alembic.aspects().firstAspect();

        if (first == null) {
            player.displayClientMessage(Component.literal("The alembic is empty.").withStyle(ChatFormatting.GRAY), false);
            return InteractionResult.CONSUME;
        }

        int removed = alembic.removeEssentia(first, TRANSFER_AMOUNT);
        EssentiaPhialItem.setEssentia(held, first, removed);

        player.displayClientMessage(
                Component.literal("Filled phial with ").append(Component.literal(first.displayName() + " x" + removed).withStyle(style -> style.withColor(first.textColor()))),
                false
        );

        return InteractionResult.CONSUME;
    }
}
