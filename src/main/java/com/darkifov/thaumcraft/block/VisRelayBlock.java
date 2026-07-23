package com.darkifov.thaumcraft.block;

import com.darkifov.thaumcraft.Aspect;
import com.darkifov.thaumcraft.ThaumcraftMod;
import com.darkifov.thaumcraft.blockentity.VisRelayBlockEntity;
import com.darkifov.thaumcraft.porting.TC4Sounds;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
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
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

/** TC4 energized-node relay with eight-block LoS graph and wand-cycled attunement. */
public class VisRelayBlock extends BaseEntityBlock {
    private static final VoxelShape SHAPE = Block.box(3.0D, 3.0D, 3.0D, 13.0D, 13.0D, 13.0D);

    public VisRelayBlock(BlockBehaviour.Properties properties) {
        super(properties);
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new VisRelayBlockEntity(pos, state);
    }

    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return level.isClientSide ? null
                : createTickerHelper(type, ThaumcraftMod.VIS_RELAY_BLOCK_ENTITY.get(), VisRelayBlockEntity::serverTick);
    }

    @Override public RenderShape getRenderShape(BlockState state) { return RenderShape.ENTITYBLOCK_ANIMATED; }
    @Override public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) { return SHAPE; }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        if (random.nextFloat() < 0.35F) {
            level.addParticle(ParticleTypes.ENCHANT,
                    pos.getX() + 0.5D + (random.nextDouble() - 0.5D) * 0.45D,
                    pos.getY() + 0.5D + (random.nextDouble() - 0.5D) * 0.45D,
                    pos.getZ() + 0.5D + (random.nextDouble() - 0.5D) * 0.45D,
                    0.0D, 0.01D, 0.0D);
        }
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player,
                                 InteractionHand hand, BlockHitResult hit) {
        ItemStack stack = player.getItemInHand(hand);
        if (!(stack.getItem() instanceof WandItem)) return InteractionResult.PASS;
        if (!level.isClientSide() && level.getBlockEntity(pos) instanceof VisRelayBlockEntity relay) {
            byte attunement = relay.cycleAttunement();
            relay.refreshParent((net.minecraft.server.level.ServerLevel)level);
            player.displayClientMessage(attunementMessage(attunement), true);
            level.playSound(null, pos, TC4Sounds.event("crystal"), SoundSource.BLOCKS, 0.2F, 1.0F);
        }
        return InteractionResult.sidedSuccess(level.isClientSide());
    }

    public static Component attunementMessage(byte attunement) {
        Aspect aspect = switch (attunement) {
            case 0 -> Aspect.AER;
            case 1 -> Aspect.IGNIS;
            case 2 -> Aspect.AQUA;
            case 3 -> Aspect.TERRA;
            case 4 -> Aspect.ORDO;
            case 5 -> Aspect.PERDITIO;
            default -> null;
        };
        return aspect == null
                ? Component.literal("Vis relay: unfiltered").withStyle(ChatFormatting.GRAY)
                : Component.literal("Vis relay: " + aspect.displayName()).withStyle(aspect.color());
    }
}
