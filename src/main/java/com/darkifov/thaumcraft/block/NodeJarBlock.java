package com.darkifov.thaumcraft.block;

import com.darkifov.thaumcraft.ThaumcraftMod;
import com.darkifov.thaumcraft.aura.TC4NodeJarRuntime;
import com.darkifov.thaumcraft.blockentity.AuraNodeBlockEntity;
import com.darkifov.thaumcraft.blockentity.NodeJarBlockEntity;
import com.darkifov.thaumcraft.porting.TC4Sounds;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
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
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.List;

/** World form of TC4 BlockJar metadata 2 / TileJarNode. */
public final class NodeJarBlock extends BaseEntityBlock {
    private static final VoxelShape SHAPE = Block.box(3.0D, 0.0D, 3.0D, 13.0D, 12.0D, 13.0D);

    public NodeJarBlock(Properties properties) {
        super(properties);
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new NodeJarBlockEntity(pos, state);
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Override
    public VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        // TC4 BlockJar temporarily restored full bounds for collisions.
        return Shapes.block();
    }

    @Override
    public void setPlacedBy(Level level, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack stack) {
        super.setPlacedBy(level, pos, state, placer, stack);
        CompoundTag root = stack.getTag();
        if (root != null && root.contains(TC4NodeJarRuntime.TAG_NODE_JAR)
                && level.getBlockEntity(pos) instanceof NodeJarBlockEntity jar) {
            jar.setNodeTag(root.getCompound(TC4NodeJarRuntime.TAG_NODE_JAR));
        }
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player,
                                 InteractionHand hand, BlockHitResult hit) {
        if (!(player.getItemInHand(hand).getItem() instanceof WandItem)) {
            return InteractionResult.PASS;
        }
        if (level.isClientSide) {
            return InteractionResult.SUCCESS;
        }
        if (!(level instanceof ServerLevel serverLevel)
                || !(level.getBlockEntity(pos) instanceof NodeJarBlockEntity jar)
                || !jar.hasNode()) {
            return InteractionResult.CONSUME;
        }

        CompoundTag nodeTag = jar.nodeTag();
        serverLevel.setBlock(pos, ThaumcraftMod.AURA_NODE.get().defaultBlockState(), 3);
        if (serverLevel.getBlockEntity(pos) instanceof AuraNodeBlockEntity node) {
            node.initializeFromJarTag(nodeTag);
        }
        serverLevel.sendParticles(ParticleTypes.END_ROD, pos.getX() + 0.5D, pos.getY() + 0.5D,
                pos.getZ() + 0.5D, 24, 0.4D, 0.5D, 0.4D, 0.03D);
        serverLevel.playSound(null, pos, SoundEvents.GLASS_BREAK, SoundSource.BLOCKS, 1.0F,
                0.9F + serverLevel.random.nextFloat() * 0.2F);
        serverLevel.playSound(null, pos, TC4Sounds.event("jar"), SoundSource.BLOCKS, 0.35F, 1.0F);
        player.swing(hand);
        return InteractionResult.CONSUME;
    }

    @Override
    public List<ItemStack> getDrops(BlockState state, LootContext.Builder builder) {
        BlockEntity blockEntity = builder.getOptionalParameter(LootContextParams.BLOCK_ENTITY);
        if (!(blockEntity instanceof NodeJarBlockEntity jar) || !jar.hasNode()) {
            return Collections.emptyList();
        }
        ItemStack drop = new ItemStack(ThaumcraftMod.NODE_JAR.get());
        drop.getOrCreateTag().put(TC4NodeJarRuntime.TAG_NODE_JAR, jar.nodeTag());
        return List.of(drop);
    }
}
