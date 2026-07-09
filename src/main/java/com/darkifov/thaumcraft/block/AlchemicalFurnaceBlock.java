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
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
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

        if (held.isEmpty()) {
            int pct = furnace.burnDuration() <= 0 ? 0 : Math.min(100, furnace.burnProgress() * 100 / furnace.burnDuration());
            int alembics = TC4DistillationRuntime.countAlembicsAbove(level, pos);
            player.displayClientMessage(
                    Component.literal("Alchemical Furnace | Stored: " + furnace.aspects().totalAmount() + "/" + AlchemicalFurnaceBlockEntity.CAPACITY)
                            .append(Component.literal(" | Fuel: " + furnace.fuelTime()))
                            .append(Component.literal(" | Burn: " + pct + "%"))
                            .append(Component.literal(" | Alembics above: " + alembics))
                            .append(Component.literal(" | Aspects: "))
                            .append(furnace.aspects().toComponent()),
                    false
            );
            return InteractionResult.CONSUME;
        }

        int fuel = fuelValue(held);

        if (fuel > 0) {
            furnace.addFuel(fuel);

            if (!player.getAbilities().instabuild) {
                held.shrink(1);
            }

            player.displayClientMessage(Component.literal("Added alchemical fuel: +" + fuel).withStyle(ChatFormatting.GOLD), false);
            return InteractionResult.CONSUME;
        }

        if (furnace.active()) {
            player.displayClientMessage(Component.literal("The furnace is already processing an item.").withStyle(ChatFormatting.YELLOW), false);
            return InteractionResult.CONSUME;
        }

        if (furnace.fuelTime() <= 0) {
            player.displayClientMessage(Component.literal("The furnace needs fuel first. Use coal, charcoal, coal block or blaze powder.").withStyle(ChatFormatting.RED), false);
            return InteractionResult.CONSUME;
        }

        AspectList aspects = AspectDatabase.getAspectsForItem(held);

        if (aspects.isEmpty()) {
            player.displayClientMessage(Component.literal("No essentia can be extracted from this item.").withStyle(ChatFormatting.GRAY), false);
            return InteractionResult.CONSUME;
        }

        if (!furnace.canAccept(aspects)) {
            player.displayClientMessage(Component.literal("The furnace is too full. In original TC4, place alembics above it, then connect tubes from alembics to jars.").withStyle(ChatFormatting.RED), false);
            return InteractionResult.CONSUME;
        }

        furnace.startBurn(aspects);

        if (!player.getAbilities().instabuild) {
            held.shrink(1);
        }

        player.displayClientMessage(
                Component.literal("The furnace begins extracting essentia: ").append(aspects.toComponent()),
                false
        );

        return InteractionResult.CONSUME;
    }

    private int fuelValue(ItemStack stack) {
        if (stack.is(Items.COAL) || stack.is(Items.CHARCOAL)) {
            return 320;
        }

        if (stack.is(Items.COAL_BLOCK)) {
            return 3200;
        }

        if (stack.is(Items.BLAZE_POWDER)) {
            return 180;
        }

        if (stack.is(ThaumcraftMod.IGNIS_FUEL.get())) {
            return 640;
        }

        return 0;
    }
}
