package com.darkifov.thaumcraft.block;

import com.darkifov.thaumcraft.Aspect;
import com.darkifov.thaumcraft.ThaumcraftMod;
import com.darkifov.thaumcraft.blockentity.EssentiaJarBlockEntity;
import com.darkifov.thaumcraft.jar.JarTubeInteractionRuntime;
import com.darkifov.thaumcraft.jar.TC4EssentiaJarParity;
import com.darkifov.thaumcraft.porting.TC4Sounds;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

import javax.annotation.Nullable;
import java.util.List;

public class EssentiaJarBlock extends BaseEntityBlock {
    private static final VoxelShape JAR_SHAPE = Block.box(3.0D, 0.0D, 3.0D, 13.0D, 12.0D, 13.0D);
    public static final int TRANSFER_AMOUNT = TC4EssentiaJarParity.PHIAL_TRANSFER;
    public static final int CAPACITY = TC4EssentiaJarParity.CAPACITY;

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
        return level.isClientSide ? null
                : createTickerHelper(type, ThaumcraftMod.ESSENTIA_JAR_BLOCK_ENTITY.get(), EssentiaJarBlockEntity::serverTick);
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
        // Original BlockJar restores full block bounds for collision collection.
        return Shapes.block();
    }

    @Override
    public void setPlacedBy(Level level, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack stack) {
        super.setPlacedBy(level, pos, state, placer, stack);
        if (!level.isClientSide && level.getBlockEntity(pos) instanceof EssentiaJarBlockEntity jar) {
            jar.load(EssentiaJarBlockItem.readJarData(stack));
            if (placer != null) {
                jar.setLabelFacing(net.minecraft.core.Direction.from3DDataValue(
                        TC4EssentiaJarParity.labelFacingDataValue(placer.getYRot())));
            }
            jar.setChangedAndSync();
        }
    }

    @Override
    public List<ItemStack> getDrops(BlockState state, LootContext.Builder builder) {
        // filtered_essentia_jar is a migration alias; original TC4 has only normal and void fillable jars.
        ItemStack stack = new ItemStack(state.is(ThaumcraftMod.FILTERED_ESSENTIA_JAR.get())
                ? ThaumcraftMod.ESSENTIA_JAR.get() : state.getBlock());
        BlockEntity blockEntity = builder.getOptionalParameter(LootContextParams.BLOCK_ENTITY);
        if (blockEntity instanceof EssentiaJarBlockEntity jar) EssentiaJarBlockItem.writeJarData(stack, jar);
        return List.of(stack);
    }

    @Override
    public ItemStack getCloneItemStack(BlockGetter level, BlockPos pos, BlockState state) {
        ItemStack stack = new ItemStack(state.is(ThaumcraftMod.FILTERED_ESSENTIA_JAR.get())
                ? ThaumcraftMod.ESSENTIA_JAR.get() : state.getBlock());
        if (level.getBlockEntity(pos) instanceof EssentiaJarBlockEntity jar) EssentiaJarBlockItem.writeJarData(stack, jar);
        return stack;
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player,
                                 InteractionHand hand, BlockHitResult hit) {
        if (!(level.getBlockEntity(pos) instanceof EssentiaJarBlockEntity jar)) return InteractionResult.PASS;
        ItemStack held = player.getItemInHand(hand);

        if (level.isClientSide) return InteractionResult.SUCCESS;

        if (JarTubeInteractionRuntime.removeLabelFromJar(jar, player, hit.getDirection())) {
            return InteractionResult.CONSUME;
        }

        if (held.getItem() instanceof JarLabelItem) {
            JarTubeInteractionRuntime.applyLabelToJar(jar, player, held, hit.getDirection());
            return InteractionResult.CONSUME;
        }

        if (player.isShiftKeyDown() && held.isEmpty()) {
            jar.clearContentsLikeTC4();
            level.playSound(null, pos, TC4Sounds.event("jar"), SoundSource.BLOCKS, 0.4F, 1.0F);
            level.playSound(null, pos, SoundEvents.PLAYER_SPLASH, SoundSource.BLOCKS, 0.5F,
                    1.0F + (level.random.nextFloat() - level.random.nextFloat()) * 0.3F);
            return InteractionResult.CONSUME;
        }

        if (held.getItem() instanceof EssentiaPhialItem) {
            return usePhial(state, level, pos, player, held, jar);
        }

        // Original BlockJar consumes the interaction even when no branch changes state.
        return InteractionResult.CONSUME;
    }

    private InteractionResult usePhial(BlockState state, Level level, BlockPos pos, Player player,
                                       ItemStack held, EssentiaJarBlockEntity jar) {
        Aspect phialAspect = EssentiaPhialItem.getAspect(held);
        int phialAmount = EssentiaPhialItem.getAmount(held);

        if (phialAspect == null || phialAmount <= 0) {
            Aspect stored = jar.storedAspect();
            if (stored == null || !TC4EssentiaJarParity.canFillEmptyPhial(jar.amount())) {
                return InteractionResult.CONSUME;
            }
            if (jar.takeFromContainerOriginal(stored, TRANSFER_AMOUNT)) {
                ItemStack filled = new ItemStack(held.getItem());
                EssentiaPhialItem.setEssentia(filled, stored, TRANSFER_AMOUNT);
                consumeAndGive(player, held, filled);
                level.playSound(null, pos, SoundEvents.PLAYER_SPLASH, SoundSource.BLOCKS, 0.25F, 1.0F);
            }
            return InteractionResult.CONSUME;
        }

        boolean accepts = jar.doesContainerAcceptOriginal(phialAspect)
                && (jar.amount() == 0 || jar.storedAspect() == phialAspect);
        // ItemEssence requires a complete eight-point transfer even for void jars.
        if (!TC4EssentiaJarParity.canEmptyFilledPhial(jar.amount(), accepts)) {
            return InteractionResult.CONSUME;
        }
        if (jar.addToContainerOriginal(phialAspect, TRANSFER_AMOUNT, isVoidJar(state)) == 0) {
            consumeAndGive(player, held, new ItemStack(held.getItem()));
            level.playSound(null, pos, SoundEvents.PLAYER_SPLASH, SoundSource.BLOCKS, 0.25F, 1.0F);
        }
        return InteractionResult.CONSUME;
    }

    private static void consumeAndGive(Player player, ItemStack input, ItemStack output) {
        if (!player.getAbilities().instabuild) input.shrink(1);
        if (!player.getInventory().add(output)) player.drop(output, false);
        player.inventoryMenu.broadcastChanges();
    }
}
