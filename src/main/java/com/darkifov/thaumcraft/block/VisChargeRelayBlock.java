package com.darkifov.thaumcraft.block;

import com.darkifov.thaumcraft.ThaumcraftMod;
import com.darkifov.thaumcraft.blockentity.VisChargeRelayBlockEntity;
import com.darkifov.thaumcraft.porting.TC4Sounds;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * TC4 TileMagicWorkbenchCharger adapter. Place directly above an Arcane
 * Workbench; it acts as a vis relay and feeds up to five centivis of every
 * primal aspect into the wand slot per tick when the network can supply it.
 */
public class VisChargeRelayBlock extends BaseEntityBlock {
    private static final VoxelShape SHAPE = Block.box(2.0D, 0.0D, 2.0D, 14.0D, 13.0D, 14.0D);

    public VisChargeRelayBlock(Properties properties) {
        super(properties);
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new VisChargeRelayBlockEntity(pos, state);
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.ENTITYBLOCK_ANIMATED;
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state,
                                                                  BlockEntityType<T> type) {
        return level.isClientSide ? null
                : createTickerHelper(type, ThaumcraftMod.VIS_CHARGE_RELAY_BLOCK_ENTITY.get(),
                VisChargeRelayBlockEntity::serverTick);
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player,
                                 InteractionHand hand, BlockHitResult hit) {
        ItemStack stack = player.getItemInHand(hand);
        if (!(stack.getItem() instanceof WandItem)) return InteractionResult.PASS;
        if (!level.isClientSide() && level.getBlockEntity(pos) instanceof VisChargeRelayBlockEntity relay) {
            byte attunement = relay.cycleAttunement();
            relay.refreshParent((net.minecraft.server.level.ServerLevel)level);
            player.displayClientMessage(VisRelayBlock.attunementMessage(attunement), true);
            level.playSound(null, pos, TC4Sounds.event("crystal"), SoundSource.BLOCKS, 0.2F, 1.0F);
        }
        return InteractionResult.sidedSuccess(level.isClientSide());
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        if (random.nextFloat() < 0.55F) {
            level.addParticle(ParticleTypes.ENCHANT,
                    pos.getX() + 0.5D + (random.nextDouble() - 0.5D) * 0.55D,
                    pos.getY() + 0.45D + random.nextDouble() * 0.55D,
                    pos.getZ() + 0.5D + (random.nextDouble() - 0.5D) * 0.55D,
                    0.0D, 0.015D, 0.0D);
        }
    }
}
