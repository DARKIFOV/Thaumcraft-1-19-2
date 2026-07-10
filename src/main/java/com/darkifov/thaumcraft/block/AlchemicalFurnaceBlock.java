package com.darkifov.thaumcraft.block;

import com.darkifov.thaumcraft.AspectDatabase;
import com.darkifov.thaumcraft.AspectList;
import com.darkifov.thaumcraft.ThaumcraftMod;
import com.darkifov.thaumcraft.essentia.TC4DistillationRuntime;
import com.darkifov.thaumcraft.blockentity.AlchemicalFurnaceBlockEntity;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.level.Level;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;


public class AlchemicalFurnaceBlock extends BaseEntityBlock {
    public AlchemicalFurnaceBlock(Properties properties) {
        super(properties);
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new AlchemicalFurnaceBlockEntity(pos, state);
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

        return createTickerHelper(type, ThaumcraftMod.ALCHEMICAL_FURNACE_BLOCK_ENTITY.get(), AlchemicalFurnaceBlockEntity::serverTick);
    }

    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean moving) {
        if (!state.is(newState.getBlock())) {
            BlockEntity blockEntity = level.getBlockEntity(pos);
            if (blockEntity instanceof AlchemicalFurnaceBlockEntity furnace) {
                if (!furnace.inputStack().isEmpty()) {
                    Containers.dropItemStack(level, pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D, furnace.inputStack().copy());
                }
                if (!furnace.fuelStack().isEmpty()) {
                    Containers.dropItemStack(level, pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D, furnace.fuelStack().copy());
                }
            }
        }
        super.onRemove(state, level, pos, newState, moving);
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        if (random.nextFloat() < 0.35F) {
            level.addParticle(ParticleTypes.SMOKE, pos.getX() + 0.5D, pos.getY() + 1.05D, pos.getZ() + 0.5D, 0.0D, 0.02D, 0.0D);
        }

        if (random.nextFloat() < 0.18F) {
            level.addParticle(ParticleTypes.WITCH, pos.getX() + 0.5D, pos.getY() + 0.75D, pos.getZ() + 0.5D, 0.0D, 0.01D, 0.0D);
        }

        if (random.nextFloat() < 0.12F) {
            level.addParticle(ParticleTypes.FLAME, pos.getX() + 0.5D, pos.getY() + 0.3D, pos.getZ() + 0.5D, 0.0D, 0.01D, 0.0D);
        }
    }

    @Override
    public void entityInside(BlockState state, Level level, BlockPos pos, Entity entity) {
        if (!level.isClientSide && state.is(ThaumcraftMod.ADVANCED_ALCHEMICAL_FURNACE.get())
                && entity instanceof ItemEntity itemEntity
                && level.getBlockEntity(pos) instanceof AlchemicalFurnaceBlockEntity furnace) {
            ItemStack stack = itemEntity.getItem();
            if (!stack.isEmpty() && furnace.processAdvancedItem(stack)) {
                stack.shrink(1);
                if (stack.isEmpty()) {
                    itemEntity.discard();
                } else {
                    itemEntity.setItem(stack);
                }
                level.playSound(null, pos, SoundEvents.BUBBLE_COLUMN_BUBBLE_POP,
                        SoundSource.BLOCKS, 0.2F, 1.0F + level.random.nextFloat() * 0.4F);
            }
        }
        super.entityInside(state, level, pos, entity);
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player,
                                 InteractionHand hand, BlockHitResult hit) {
        if (level.isClientSide) {
            return InteractionResult.SUCCESS;
        }

        BlockEntity blockEntity = level.getBlockEntity(pos);
        if (!(blockEntity instanceof AlchemicalFurnaceBlockEntity furnace)) {
            return InteractionResult.PASS;
        }

        ItemStack held = player.getItemInHand(hand);
        if (furnace.isAdvanced()) {
            if (held.isEmpty()) {
                player.displayClientMessage(Component.literal("Advanced Alchemical Furnace | Essentia: "
                        + furnace.aspects().totalAmount() + "/" + furnace.capacity()
                        + " | Heat: " + furnace.advancedHeat() + "/500"
                        + " | Perditio: " + furnace.advancedEntropy() + "/500"
                        + " | Aqua: " + furnace.advancedWater() + "/500"
                        + " | Cooldown: " + furnace.advancedProcessedCooldown()), false);
                return InteractionResult.CONSUME;
            }
            AspectList advancedAspects = AspectDatabase.getAspectsForItem(held);
            if (advancedAspects == null || advancedAspects.isEmpty()) {
                player.displayClientMessage(Component.literal("No essentia can be extracted from this item.")
                        .withStyle(ChatFormatting.GRAY), false);
                return InteractionResult.CONSUME;
            }
            if (!furnace.processAdvancedItem(held)) {
                player.displayClientMessage(Component.literal("The advanced furnace is cooling down, full, or lacks Ignis/Perditio/Aqua power.")
                        .withStyle(ChatFormatting.YELLOW), false);
                return InteractionResult.CONSUME;
            }
            if (!player.getAbilities().instabuild) {
                held.shrink(1);
            }
            return InteractionResult.CONSUME;
        }
        if (held.isEmpty()) {
            if (player.isShiftKeyDown()) {
                ItemStack extracted = !furnace.inputStack().isEmpty() ? furnace.extractInput() : furnace.extractFuel();
                if (!extracted.isEmpty()) {
                    if (!player.getInventory().add(extracted)) {
                        player.drop(extracted, false);
                    }
                    return InteractionResult.CONSUME;
                }
            }
            int pct = furnace.burnDuration() <= 0 ? 0 : Math.min(100, furnace.burnProgress() * 100 / furnace.burnDuration());
            int alembics = TC4DistillationRuntime.countAlembicsAbove(level, pos);
            player.displayClientMessage(
                    Component.literal("Alchemical Furnace | Stored: " + furnace.aspects().totalAmount() + "/" + furnace.capacity())
                            .append(Component.literal(" | Fuel: " + furnace.fuelTime()))
                            .append(Component.literal(" | Burn: " + pct + "%"))
                            .append(Component.literal(" | Bellows: " + furnace.bellows()))
                            .append(Component.literal(" | Alembics: " + alembics))
                            .append(Component.literal(" | Input: " + (furnace.inputStack().isEmpty() ? "empty" : furnace.inputStack().getHoverName().getString())))
                            .append(Component.literal(" | Aspects: "))
                            .append(furnace.aspects().toComponent()),
                    false
            );
            return InteractionResult.CONSUME;
        }

        if (furnace.insertFuel(held)) {
            if (!player.getAbilities().instabuild) {
                held.shrink(1);
            }
            player.displayClientMessage(Component.literal("Fuel placed into the alchemical furnace.").withStyle(ChatFormatting.GOLD), false);
            return InteractionResult.CONSUME;
        }

        AspectList aspects = AspectDatabase.getAspectsForItem(held);
        if (aspects == null || aspects.isEmpty()) {
            player.displayClientMessage(Component.literal("No essentia can be extracted from this item.").withStyle(ChatFormatting.GRAY), false);
            return InteractionResult.CONSUME;
        }
        if (aspects.totalAmount() > furnace.space()) {
            player.displayClientMessage(Component.literal("The furnace cannot hold the essentia from this item.").withStyle(ChatFormatting.RED), false);
            return InteractionResult.CONSUME;
        }
        if (!furnace.insertInput(held)) {
            player.displayClientMessage(Component.literal("The input slot contains another item.").withStyle(ChatFormatting.YELLOW), false);
            return InteractionResult.CONSUME;
        }
        if (!player.getAbilities().instabuild) {
            held.shrink(1);
        }
        player.displayClientMessage(Component.literal("Item placed into the alchemical furnace: ").append(aspects.toComponent()), false);
        return InteractionResult.CONSUME;
    }

}
