package com.darkifov.thaumcraft.block;

import com.darkifov.thaumcraft.Aspect;
import com.darkifov.thaumcraft.ThaumcraftMod;
import com.darkifov.thaumcraft.blockentity.AlembicBlockEntity;
import com.darkifov.thaumcraft.blockentity.AlchemicalFurnaceBlockEntity;
import com.darkifov.thaumcraft.block.WandItem;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.entity.player.Player;
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
        return RenderShape.ENTITYBLOCK_ANIMATED;
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
            if (player.isShiftKeyDown()) {
                if (alembic.aspectFilter() != null) {
                    alembic.clearAspectFilter();
                    ItemStack label = new ItemStack(ThaumcraftMod.JAR_LABEL.get());
                    if (!player.getInventory().add(label)) {
                        player.drop(label, false);
                    }
                    player.displayClientMessage(Component.literal("Alembic filter removed.").withStyle(ChatFormatting.GRAY), false);
                    return InteractionResult.CONSUME;
                }
                if (!alembic.aspects().isEmpty()) {
                    alembic.clearEssentia();
                    player.displayClientMessage(Component.literal("Alembic emptied.").withStyle(ChatFormatting.DARK_PURPLE), false);
                    return InteractionResult.CONSUME;
                }
            }
            player.displayClientMessage(
                    Component.literal("Alembic | Capacity: " + alembic.aspects().totalAmount() + "/" + AlembicBlockEntity.CAPACITY
                                    + " | Filter: " + (alembic.aspectFilter() == null ? "none" : alembic.aspectFilter().displayName())
                                    + " | Blocked face: " + alembic.facing().getName() + " | Aspects: ")
                            .append(alembic.aspects().toComponent()),
                    false
            );
            return InteractionResult.CONSUME;
        }

        if (held.getItem() instanceof WandItem && hit.getDirection().getAxis().isHorizontal()) {
            alembic.setFacing(hit.getDirection());
            player.swing(hand);
            player.displayClientMessage(Component.literal("Alembic output face blocked: " + hit.getDirection().getName()).withStyle(ChatFormatting.AQUA), false);
            return InteractionResult.CONSUME;
        }

        if (held.is(ThaumcraftMod.JAR_LABEL.get())) {
            Aspect filter = alembic.storedAspect();
            ItemStack other = player.getItemInHand(hand == InteractionHand.MAIN_HAND ? InteractionHand.OFF_HAND : InteractionHand.MAIN_HAND);
            if (filter == null && other.getItem() instanceof EssentiaPhialItem) {
                filter = EssentiaPhialItem.getAspect(other);
            }
            if (filter == null) {
                player.displayClientMessage(Component.literal("Fill the alembic, or hold a filled phial in the other hand, before applying a label.").withStyle(ChatFormatting.GRAY), false);
                return InteractionResult.CONSUME;
            }
            if (alembic.setAspectFilter(filter)) {
                if (!player.getAbilities().instabuild) {
                    held.shrink(1);
                }
                player.displayClientMessage(Component.literal("Alembic filtered to " + filter.displayName()).withStyle(filter.color()), false);
            }
            return InteractionResult.CONSUME;
        }

        if (held.getItem() instanceof EssentiaPhialItem) {
            return handlePhial(player, held, alembic);
        }

        if (level.getBlockEntity(pos.below()) instanceof AlchemicalFurnaceBlockEntity) {
            player.displayClientMessage(Component.literal("Alembic is mounted above an Alchemical Furnace; essentia distills into it automatically, as in TC4.").withStyle(ChatFormatting.GRAY), false);
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
