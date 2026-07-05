package com.darkifov.thaumcraft.block;

import com.darkifov.thaumcraft.Aspect;
import com.darkifov.thaumcraft.ThaumcraftMod;
import com.darkifov.thaumcraft.blockentity.EssentiaJarBlockEntity;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

public class EssentiaJarBlock extends BaseEntityBlock {
    private static final VoxelShape JAR_SHAPE = Block.box(3.5D, 0.0D, 3.5D, 12.5D, 15.0D, 12.5D);

    public static final int TRANSFER_AMOUNT = 8;
    public static final int CAPACITY = 64;

    public EssentiaJarBlock(Properties properties) {
        super(properties);
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new EssentiaJarBlockEntity(pos, state);
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    protected boolean isFilteredJar(BlockState state) {
        return state.is(ThaumcraftMod.FILTERED_ESSENTIA_JAR.get());
    }

    protected boolean isVoidJar(BlockState state) {
        return state.is(ThaumcraftMod.VOID_ESSENTIA_JAR.get());
    }


    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return JAR_SHAPE;
    }

    @Override
    public VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return JAR_SHAPE;
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player,
                                 InteractionHand hand, BlockHitResult hit) {
        ItemStack held = player.getItemInHand(hand);

        if (level.isClientSide) {
            return InteractionResult.SUCCESS;
        }

        BlockEntity blockEntity = level.getBlockEntity(pos);

        if (!(blockEntity instanceof EssentiaJarBlockEntity jar)) {
            return InteractionResult.PASS;
        }

        if (held.isEmpty()) {
            String jarType = isVoidJar(state) ? "Void Jar" : isFilteredJar(state) ? "Filtered Jar" : "Essentia Jar";
            String filter = jar.filterAspect() == null ? "none" : jar.filterAspect().displayName();
            player.displayClientMessage(
                    Component.literal(jarType + " | Filter: " + filter + " | Fill: " + jar.amount() + "/" + jar.capacity() + " | Aspects: ")
                            .append(jar.aspects().toComponent()),
                    false
            );
            return InteractionResult.CONSUME;
        }

        if (held.getItem() instanceof EssentiaPhialItem) {
            Aspect heldAspect = EssentiaPhialItem.getAspect(held);
            int heldAmount = EssentiaPhialItem.getAmount(held);

            if (heldAspect == null || heldAmount <= 0) {
                Aspect first = jar.aspects().firstAspect();

                if (first == null) {
                    player.displayClientMessage(Component.literal("The jar is empty.").withStyle(ChatFormatting.GRAY), false);
                    return InteractionResult.CONSUME;
                }

                int removed = jar.aspects().removeUpTo(first, TRANSFER_AMOUNT);
                EssentiaPhialItem.setEssentia(held, first, removed);
                jar.setChangedAndSync();

                player.displayClientMessage(
                        Component.literal("Filled phial with ").append(Component.literal(first.displayName() + " x" + removed).withStyle(style -> style.withColor(first.textColor()))),
                        false
                );

                return InteractionResult.CONSUME;
            }

            if (isFilteredJar(state) && jar.filterAspect() == null) {
                jar.setFilterAspect(heldAspect);
                player.displayClientMessage(Component.literal("Filtered jar locked to ").append(Component.literal(heldAspect.displayName()).withStyle(style -> style.withColor(heldAspect.textColor()))), false);
            }

            if (!jar.canAcceptAspect(heldAspect)) {
                player.displayClientMessage(Component.literal("This jar rejects that aspect.").withStyle(ChatFormatting.RED), false);
                return InteractionResult.CONSUME;
            }

            int space = Math.max(0, CAPACITY - jar.aspects().totalAmount());
            int toAdd = Math.min(space, heldAmount);

            if (isVoidJar(state) && toAdd <= 0) {
                EssentiaPhialItem.clear(held);
                player.displayClientMessage(Component.literal("The void jar destroys excess ").append(Component.literal(heldAspect.displayName()).withStyle(style -> style.withColor(heldAspect.textColor()))), false);
                return InteractionResult.CONSUME;
            }

            if (toAdd <= 0) {
                player.displayClientMessage(Component.literal("The jar is full.").withStyle(ChatFormatting.RED), false);
                return InteractionResult.CONSUME;
            }

            jar.aspects().add(heldAspect, toAdd);

            if (toAdd >= heldAmount) {
                EssentiaPhialItem.clear(held);
            } else {
                EssentiaPhialItem.setEssentia(held, heldAspect, heldAmount - toAdd);
            }

            jar.setChangedAndSync();

            player.displayClientMessage(
                    Component.literal("Poured ").append(Component.literal(heldAspect.displayName() + " x" + toAdd).withStyle(style -> style.withColor(heldAspect.textColor())))
                            .append(Component.literal(" into jar.")),
                    false
            );

            return InteractionResult.CONSUME;
        }

        return InteractionResult.PASS;
    }
}
