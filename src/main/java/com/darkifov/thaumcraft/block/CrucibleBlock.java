package com.darkifov.thaumcraft.block;

import com.darkifov.thaumcraft.Aspect;
import com.darkifov.thaumcraft.AspectDatabase;
import com.darkifov.thaumcraft.AspectList;
import com.darkifov.thaumcraft.ThaumcraftMod;
import com.darkifov.thaumcraft.alchemy.AlchemyRecipe;
import com.darkifov.thaumcraft.alchemy.AlchemyRecipes;
import com.darkifov.thaumcraft.blockentity.CrucibleBlockEntity;
import com.darkifov.thaumcraft.data.PlayerThaumData;
import com.darkifov.thaumcraft.recipe.TC4RecipeRequirementIndex;
import com.darkifov.thaumcraft.porting.TC4Sounds;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
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
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return level.isClientSide ? null : createTickerHelper(type, ThaumcraftMod.CRUCIBLE_BLOCK_ENTITY.get(), CrucibleBlockEntity::serverTick);
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
                    Component.literal("Crucible | " + crucible.statusText())
                            .append(crucible.aspects().toComponent()),
                    false
            );
            return InteractionResult.CONSUME;
        }

        if (held.getItem() == Items.WATER_BUCKET) {
            if (!crucible.hasWater()) {
                crucible.fillWater();
                level.playSound(null, pos, TC4Sounds.event("spill"), SoundSource.BLOCKS, 0.45F, 1.1F);

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
                crucible.drainWater();
                crucible.clearAspects();
                crucible.clearFlux();
                level.playSound(null, pos, TC4Sounds.event("spill"), SoundSource.BLOCKS, 0.55F, 0.8F);

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

        if (!crucible.isBoiling()) {
            player.displayClientMessage(Component.literal("The crucible needs boiling water. Put fire, lava, magma or a lit campfire below it.").withStyle(ChatFormatting.GOLD), false);
            return InteractionResult.CONSUME;
        }

        AlchemyRecipe catalystRecipe = AlchemyRecipes.findByCatalyst(held);

        if (catalystRecipe != null) {
            String requiredResearch = TC4RecipeRequirementIndex.requiredResearchForRuntimeRecipe(catalystRecipe.tc4Key(), catalystRecipe.research());
            if (!requiredResearch.isBlank() && !PlayerThaumData.hasResearch(player, requiredResearch)) {
                player.displayClientMessage(Component.literal("Research locked: " + requiredResearch).withStyle(ChatFormatting.RED), false);
                return InteractionResult.CONSUME;
            }

            if (catalystRecipe.canCraft(held, crucible.aspects())) {
                ItemStack result = catalystRecipe.craft(held, crucible.aspects());

                if (!player.getAbilities().instabuild) {
                    held.shrink(1);
                }

                crucible.consumeWater(CrucibleBlockEntity.WATER_PER_CRAFT);
                giveOrDrop(level, pos, player, result);
                crucible.setChangedAndSync();
                level.playSound(null, pos, TC4Sounds.event("craftstart"), SoundSource.BLOCKS, 0.65F, 1.0F);
                if (level instanceof ServerLevel serverLevel) {
                    serverLevel.sendParticles(ParticleTypes.ENCHANT, pos.getX() + 0.5D, pos.getY() + 0.85D, pos.getZ() + 0.5D, 40, 0.35D, 0.25D, 0.35D, 0.06D);
                }
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
        crucible.consumeWater(CrucibleBlockEntity.WATER_PER_DISSOLVE);
        level.playSound(null, pos, TC4Sounds.event("bubble"), SoundSource.BLOCKS, 0.35F, 0.95F + level.random.nextFloat() * 0.2F);
        if (level instanceof ServerLevel serverLevel) {
            serverLevel.sendParticles(ParticleTypes.BUBBLE, pos.getX() + 0.5D, pos.getY() + 0.88D, pos.getZ() + 0.5D, 10, 0.22D, 0.05D, 0.22D, 0.02D);
            serverLevel.sendParticles(ParticleTypes.WITCH, pos.getX() + 0.5D, pos.getY() + 0.90D, pos.getZ() + 0.5D, 5, 0.20D, 0.08D, 0.20D, 0.01D);
        }
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
            level.setBlock(target, ThaumcraftMod.FLUX_GOO.get().defaultBlockState(), 3);
            crucible.addFlux(-12);
            crucible.maybeSpillFlux(true);
            player.displayClientMessage(Component.literal("Flux burst! Flux goo spilled nearby.").withStyle(ChatFormatting.DARK_PURPLE), false);
        }
    }

    private void giveOrDrop(Level level, BlockPos pos, Player player, ItemStack stack) {
        if (!player.getInventory().add(stack)) {
            Containers.dropItemStack(level, pos.getX() + 0.5D, pos.getY() + 1.0D, pos.getZ() + 0.5D, stack);
        }
    }
}
