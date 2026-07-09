package com.darkifov.thaumcraft.block;

import com.darkifov.thaumcraft.ThaumcraftMod;
import com.darkifov.thaumcraft.blockentity.ThaumatoriumBlockEntity;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraftforge.network.NetworkHooks;

/** Stage503-522 TileThaumatorium / TileThaumatoriumTop runtime bridge. */
public class ThaumatoriumBlock extends BaseEntityBlock {
    public static final DirectionProperty FACING = HorizontalDirectionalBlock.FACING;

    public ThaumatoriumBlock(Properties properties) {
        super(properties);
        registerDefaultState(defaultBlockState().setValue(FACING, Direction.NORTH));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<net.minecraft.world.level.block.Block, BlockState> builder) {
        builder.add(FACING);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return defaultBlockState().setValue(FACING, context.getHorizontalDirection().getOpposite());
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new ThaumatoriumBlockEntity(pos, state);
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
        return createTickerHelper(type, ThaumcraftMod.THAUMATORIUM_BLOCK_ENTITY.get(), ThaumatoriumBlockEntity::serverTick);
    }

    @Override
    public void onRemove(BlockState oldState, Level level, BlockPos pos, BlockState newState, boolean moving) {
        if (!oldState.is(newState.getBlock())) {
            BlockEntity blockEntity = level.getBlockEntity(pos);
            if (blockEntity instanceof ThaumatoriumBlockEntity thaumatorium) {
                ItemStack catalyst = thaumatorium.catalyst().copy();
                if (!catalyst.isEmpty()) {
                    Containers.dropItemStack(level, pos.getX() + 0.5D, pos.getY() + 0.75D, pos.getZ() + 0.5D, catalyst);
                }
            }
        }
        super.onRemove(oldState, level, pos, newState, moving);
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player,
                                 InteractionHand hand, BlockHitResult hit) {
        if (level.isClientSide) {
            return InteractionResult.SUCCESS;
        }
        BlockEntity blockEntity = level.getBlockEntity(pos);
        if (!(blockEntity instanceof ThaumatoriumBlockEntity thaumatorium)) {
            return InteractionResult.PASS;
        }
        ItemStack held = player.getItemInHand(hand);
        if (player.isShiftKeyDown() && held.isEmpty()) {
            ItemStack extracted = thaumatorium.extractCatalyst();
            if (!extracted.isEmpty()) {
                if (!player.addItem(extracted)) {
                    Containers.dropItemStack(level, pos.getX() + 0.5D, pos.getY() + 1.0D, pos.getZ() + 0.5D, extracted);
                }
                return InteractionResult.CONSUME;
            }
        }
        if (player.isShiftKeyDown() && held.isEmpty()) {
            thaumatorium.cycleFormula();
            player.displayClientMessage(Component.literal("Thaumatorium | formula cycled").withStyle(ChatFormatting.GOLD), false);
            return InteractionResult.CONSUME;
        }
        if (!held.isEmpty() && thaumatorium.catalyst().isEmpty()) {
            if (thaumatorium.insertCatalyst(held)) {
                if (!player.getAbilities().instabuild) {
                    held.shrink(1);
                }
                player.displayClientMessage(Component.literal("Thaumatorium | catalyst inserted: " + thaumatorium.catalyst().getHoverName().getString()).withStyle(ChatFormatting.GOLD), false);
                return InteractionResult.CONSUME;
            }
            player.displayClientMessage(Component.literal("Thaumatorium | no original alchemy formula for this catalyst yet.").withStyle(ChatFormatting.RED), false);
            return InteractionResult.CONSUME;
        }
        if (player instanceof ServerPlayer serverPlayer) {
            NetworkHooks.openScreen(serverPlayer, thaumatorium, buffer -> buffer.writeBlockPos(pos));
        } else {
            player.displayClientMessage(thaumatorium.statusComponent(), false);
        }
        return InteractionResult.CONSUME;
    }
}
