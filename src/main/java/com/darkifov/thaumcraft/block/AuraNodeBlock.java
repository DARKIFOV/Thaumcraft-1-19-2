package com.darkifov.thaumcraft.block;

import com.mojang.math.Vector3f;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.server.level.ServerLevel;
import com.darkifov.thaumcraft.aura.AuraNodeScan;
import com.darkifov.thaumcraft.ThaumcraftMod;
import com.darkifov.thaumcraft.blockentity.AuraNodeBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;


public class AuraNodeBlock extends BaseEntityBlock {
    private static final VoxelShape AURA_NODE_SELECTION_SHAPE = Block.box(4.0D, 4.0D, 4.0D, 12.0D, 12.0D, 12.0D);

    public AuraNodeBlock(Properties properties) {
        super(properties);
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new AuraNodeBlockEntity(pos, state);
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.INVISIBLE;
    }


    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return AURA_NODE_SELECTION_SHAPE;
    }

    @Override
    public VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return Shapes.empty();
    }

    @Override
    public boolean propagatesSkylightDown(BlockState state, BlockGetter level, BlockPos pos) {
        return true;
    }

    @Override
    public float getShadeBrightness(BlockState state, BlockGetter level, BlockPos pos) {
        return 1.0F;
    }

    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        if (level.isClientSide) {
            return null;
        }

        return createTickerHelper(type, ThaumcraftMod.AURA_NODE_BLOCK_ENTITY.get(), AuraNodeBlockEntity::serverTick);
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        if (random.nextFloat() < 0.65F) {
            double x = pos.getX() + 0.5D + (random.nextDouble() - 0.5D) * 0.85D;
            double y = pos.getY() + 0.5D + (random.nextDouble() - 0.5D) * 0.85D;
            double z = pos.getZ() + 0.5D + (random.nextDouble() - 0.5D) * 0.85D;
            level.addParticle(ParticleTypes.ENCHANT, x, y, z, 0.0D, 0.02D, 0.0D);
        }

        if (random.nextFloat() < 0.18F) {
            level.addParticle(ParticleTypes.END_ROD, pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D, 0.0D, 0.01D, 0.0D);
        }
    }


    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, net.minecraft.world.phys.BlockHitResult hit) {
        BlockEntity blockEntity = level.getBlockEntity(pos);

        if (!(blockEntity instanceof AuraNodeBlockEntity node)) {
            return InteractionResult.PASS;
        }

        ItemStack stack = player.getItemInHand(hand);

        if (stack.getItem() instanceof WandItem wandItem) {
            // TC4 TileNode starts an indefinite wand-use action here.  The actual random
            // primal transfer happens every five ticks while the player keeps looking at the node.
            wandItem.beginNodeUse(stack, level, pos);
            player.startUsingItem(hand);
            return InteractionResult.sidedSuccess(level.isClientSide());
        }

        if (!(stack.is(ThaumcraftMod.THAUMOMETER.get())
                || stack.is(ThaumcraftMod.GOGGLES_OF_REVEALING.get())
                || stack.is(ThaumcraftMod.HELMET_OF_REVEALING.get()))) {
            return InteractionResult.PASS;
        }

        if (!level.isClientSide()) {
            node.markScanned();
            AuraNodeScan.sendScan(player, node);

            if (level instanceof ServerLevel serverLevel) {
                serverLevel.sendParticles(new DustParticleOptions(new Vector3f(0.75F, 0.55F, 1.0F), 1.2F),
                        pos.getX() + 0.5D,
                        pos.getY() + 0.5D,
                        pos.getZ() + 0.5D,
                        12,
                        0.35D,
                        0.35D,
                        0.35D,
                        0.01D);
            }
        }

        return InteractionResult.sidedSuccess(level.isClientSide());
    }
}
