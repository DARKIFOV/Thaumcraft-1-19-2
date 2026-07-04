package com.darkifov.thaumcraft.block;

import com.darkifov.thaumcraft.Aspect;
import com.darkifov.thaumcraft.AspectDatabase;
import com.darkifov.thaumcraft.AspectList;
import com.darkifov.thaumcraft.ThaumcraftMod;
import com.darkifov.thaumcraft.alchemy.AlchemyRecipe;
import com.darkifov.thaumcraft.alchemy.AlchemyRecipes;
import com.darkifov.thaumcraft.blockentity.CrucibleBlockEntity;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

public class CrucibleBlock extends BaseEntityBlock {
    public static final int PHIAL_TRANSFER_AMOUNT = 8;

    public CrucibleBlock(Properties properties) {
        super(properties);
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new CrucibleBlockEntity(pos, state);
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

        if (!(blockEntity instanceof CrucibleBlockEntity crucible)) {
            return InteractionResult.PASS;
        }

        if (held.isEmpty()) {
            player.displayClientMessage(
                    Component.literal("Crucible | Water: " + crucible.hasWater() + " | Flux: " + crucible.flux() + " | Aspects: ")
                            .append(crucible.aspects().toComponent()),
                    false
            );
            return InteractionResult.CONSUME;
        }

        if (held.getItem() == Items.WATER_BUCKET) {
            if (!crucible.hasWater()) {
                crucible.setWater(true);

                if (!player.getAbilities().instabuild) {
                    player.setItemInHand(hand, new ItemStack(Items.BUCKET));
                }

                player.displayClientMessage(Component.literal("The crucible is now filled with water.").withStyle(ChatFormatting.AQUA), false);
            } else {
                player.displayClientMessage(Component.literal("The crucible already has water.").withStyle(ChatFormatting.GRAY), false);
            }

            return InteractionResult.CONSUME;
        }

        if (held.getItem() == Items.BUCKET) {
            if (crucible.hasWater()) {
                crucible.setWater(false);
                crucible.clearAspects();
                crucible.clearFlux();

                if (!player.getAbilities().instabuild) {
                    player.setItemInHand(hand, new ItemStack(Items.WATER_BUCKET));
                }

                player.displayClientMessage(Component.literal("You emptied the crucible. All aspects and flux were lost.").withStyle(ChatFormatting.DARK_GRAY), false);
            }

            return InteractionResult.CONSUME;
        }

        if (held.getItem() instanceof EssentiaPhialItem) {
            return handlePhial(level, pos, player, held, crucible);
        }

        if (!crucible.hasWater()) {
            player.displayClientMessage(Component.literal("The crucible needs water first.").withStyle(ChatFormatting.BLUE), false);
            return InteractionResult.CONSUME;
        }

        AlchemyRecipe catalystRecipe = AlchemyRecipes.findByCatalyst(held);

        if (catalystRecipe != null) {
            if (catalystRecipe.canCraft(held, crucible.aspects())) {
                ItemStack result = catalystRecipe.craft(held, crucible.aspects());

                if (!player.getAbilities().instabuild) {
                    held.shrink(1);
                }

                giveOrDrop(level, pos, player, result);
                crucible.setChangedAndSync();
                maybeFluxBurst(level, pos, crucible, player);

                player.displayClientMessage(
                        Component.literal("Crucible alchemy complete: ")
                                .append(result.getHoverName())
                                .append(Component.literal(" | Used aspects: " + catalystRecipe.costText()).withStyle(ChatFormatting.DARK_AQUA)),
                        false
                );

                return InteractionResult.CONSUME;
            }

            player.displayClientMessage(
                    Component.literal("Catalyst recognized, but the crucible lacks aspects: " + catalystRecipe.costText()).withStyle(ChatFormatting.RED),
                    false
            );
            return InteractionResult.CONSUME;
        }

        AspectList aspects = AspectDatabase.getAspectsForItem(held);

        if (aspects.isEmpty()) {
            player.displayClientMessage(Component.literal("This item has no useful aspects.").withStyle(ChatFormatting.GRAY), false);
            return InteractionResult.CONSUME;
        }

        ItemStack one = held.copy();
        one.setCount(1);

        if (!player.getAbilities().instabuild) {
            held.shrink(1);
        }

        crucible.addAspects(aspects);
        maybeFluxBurst(level, pos, crucible, player);

        player.displayClientMessage(
                Component.literal("Dissolved ")
                        .append(one.getHoverName())
                        .append(Component.literal(" into: "))
                        .append(aspects.toComponent()),
                false
        );

        return InteractionResult.CONSUME;
    }

    private InteractionResult handlePhial(Level level, BlockPos pos, Player player, ItemStack held, CrucibleBlockEntity crucible) {
        if (!crucible.hasWater()) {
            player.displayClientMessage(Component.literal("The crucible needs water first.").withStyle(ChatFormatting.BLUE), false);
            return InteractionResult.CONSUME;
        }

        Aspect heldAspect = EssentiaPhialItem.getAspect(held);
        int heldAmount = EssentiaPhialItem.getAmount(held);

        if (heldAspect != null && heldAmount > 0) {
            crucible.aspects().add(heldAspect, heldAmount);
            EssentiaPhialItem.clear(held);
            crucible.setChangedAndSync();

            player.displayClientMessage(
                    Component.literal("Poured ").append(Component.literal(heldAspect.displayName() + " x" + heldAmount).withStyle(heldAspect.color()))
                            .append(Component.literal(" into crucible.")),
                    false
            );

            return InteractionResult.CONSUME;
        }

        Aspect first = crucible.aspects().firstAspect();

        if (first == null) {
            player.displayClientMessage(Component.literal("No essentia in crucible.").withStyle(ChatFormatting.GRAY), false);
            return InteractionResult.CONSUME;
        }

        int removed = crucible.aspects().removeUpTo(first, PHIAL_TRANSFER_AMOUNT);
        EssentiaPhialItem.setEssentia(held, first, removed);
        crucible.setChangedAndSync();

        player.displayClientMessage(
                Component.literal("Filled phial with ").append(Component.literal(first.displayName() + " x" + removed).withStyle(first.color())),
                false
        );

        return InteractionResult.CONSUME;
    }

    private void maybeFluxBurst(Level level, BlockPos pos, CrucibleBlockEntity crucible, Player player) {
        if (crucible.flux() < 24) {
            return;
        }

        BlockPos target = pos.offset(level.random.nextInt(5) - 2, -1, level.random.nextInt(5) - 2);

        if (!level.isOutsideBuildHeight(target) && !level.getBlockState(target).isAir()) {
            level.setBlock(target, ThaumcraftMod.TAINTED_SOIL.get().defaultBlockState(), 3);
            crucible.addFlux(-12);
            player.displayClientMessage(Component.literal("Flux burst! Nearby ground was tainted.").withStyle(ChatFormatting.DARK_PURPLE), false);
        }
    }

    private void giveOrDrop(Level level, BlockPos pos, Player player, ItemStack stack) {
        if (!player.getInventory().add(stack)) {
            Containers.dropItemStack(level, pos.getX() + 0.5D, pos.getY() + 1.0D, pos.getZ() + 0.5D, stack);
        }
    }
}
