package com.darkifov.thaumcraft.block;

import com.darkifov.thaumcraft.Aspect;
import com.darkifov.thaumcraft.ThaumcraftMod;
import com.darkifov.thaumcraft.blockentity.EssentiaJarBlockEntity;
import com.darkifov.thaumcraft.jar.JarTubeInteractionRuntime;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;

import javax.annotation.Nullable;
import java.util.List;

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

    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        if (level.isClientSide) {
            return null;
        }
        return createTickerHelper(type, ThaumcraftMod.ESSENTIA_JAR_BLOCK_ENTITY.get(), EssentiaJarBlockEntity::serverTick);
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
    public void setPlacedBy(Level level, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack stack) {
        super.setPlacedBy(level, pos, state, placer, stack);
        // BlockItem applies BlockEntityTag after placement. This explicit legacy
        // fallback also accepts old TC4/root-NBT filled-jar stacks.
        if (!level.isClientSide && level.getBlockEntity(pos) instanceof EssentiaJarBlockEntity jar
                && stack.getItem() instanceof EssentiaJarBlockItem) {
            jar.load(EssentiaJarBlockItem.readJarData(stack));
            jar.setChangedAndSync();
        }
    }

    @Override
    public List<ItemStack> getDrops(BlockState state, LootContext.Builder builder) {
        ItemStack stack = new ItemStack(state.getBlock());
        BlockEntity blockEntity = builder.getOptionalParameter(LootContextParams.BLOCK_ENTITY);
        if (blockEntity instanceof EssentiaJarBlockEntity jar) {
            EssentiaJarBlockItem.writeJarData(stack, jar);
        }
        return List.of(stack);
    }

    @Override
    public ItemStack getCloneItemStack(BlockGetter level, BlockPos pos, BlockState state) {
        ItemStack stack = new ItemStack(state.getBlock());
        if (level.getBlockEntity(pos) instanceof EssentiaJarBlockEntity jar) {
            EssentiaJarBlockItem.writeJarData(stack, jar);
        }
        return stack;
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

        if (held.getItem() instanceof JarLabelItem) {
            JarTubeInteractionRuntime.applyLabelToJar(jar, player, held,
                    player.getItemInHand(hand == InteractionHand.MAIN_HAND ? InteractionHand.OFF_HAND : InteractionHand.MAIN_HAND),
                    hit.getDirection());
            return InteractionResult.CONSUME;
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

                int removed = Math.min(TRANSFER_AMOUNT, jar.aspects().get(first));
                if (jar.takeFromContainerOriginal(first, removed)) {
                    EssentiaPhialItem.setEssentia(held, first, removed);
                } else {
                    removed = 0;
                }

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

            boolean voidJar = isVoidJar(state);
            int remainder = jar.addToContainerOriginal(heldAspect, heldAmount, voidJar);
            int toAdd = Math.max(0, heldAmount - remainder);

            if (toAdd <= 0) {
                player.displayClientMessage(Component.literal(voidJar ? "The void jar rejects that aspect." : "The jar is full.").withStyle(ChatFormatting.RED), false);
                return InteractionResult.CONSUME;
            }

            if (remainder <= 0) {
                EssentiaPhialItem.clear(held);
            } else {
                EssentiaPhialItem.setEssentia(held, heldAspect, remainder);
            }

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
