package com.darkifov.thaumcraft.block;

import com.darkifov.thaumcraft.Aspect;
import com.darkifov.thaumcraft.AspectList;
import com.darkifov.thaumcraft.ThaumcraftMod;
import com.darkifov.thaumcraft.blockentity.AlembicBlockEntity;
import com.darkifov.thaumcraft.porting.TC4Sounds;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

/** Original BlockMetalDevice metadata-1 interaction contract. */
public class AlembicBlock extends BaseEntityBlock {
    public AlembicBlock(Properties properties) { super(properties); }

    @Override public BlockEntity newBlockEntity(BlockPos pos, BlockState state) { return new AlembicBlockEntity(pos, state); }
    @Override public RenderShape getRenderShape(BlockState state) { return RenderShape.ENTITYBLOCK_ANIMATED; }
    @Override public boolean hasAnalogOutputSignal(BlockState state) { return true; }
    @Override public int getAnalogOutputSignal(BlockState state, Level level, BlockPos pos) {
        return level.getBlockEntity(pos) instanceof AlembicBlockEntity alembic ? alembic.comparatorOutput() : 0;
    }

    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean moving) {
        if (!state.is(newState.getBlock()) && level.getBlockEntity(pos) instanceof AlembicBlockEntity alembic
                && alembic.aspectFilter() != null) {
            Containers.dropItemStack(level, pos.getX() + .5D, pos.getY() + .5D, pos.getZ() + .5D,
                    new ItemStack(ThaumcraftMod.JAR_LABEL.get()));
        }
        super.onRemove(state, level, pos, newState, moving);
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player,
                                 InteractionHand hand, BlockHitResult hit) {
        if (!(level.getBlockEntity(pos) instanceof AlembicBlockEntity alembic)) return InteractionResult.PASS;
        ItemStack held = player.getItemInHand(hand);
        if (level.isClientSide) return InteractionResult.SUCCESS;

        if (held.getItem() instanceof WandItem && hit.getDirection().getAxis().isHorizontal()) {
            alembic.setFacing(hit.getDirection());
            player.swing(hand);
            return InteractionResult.CONSUME;
        }

        if (player.isShiftKeyDown() && alembic.aspectFilter() != null) {
            alembic.clearAspectFilter();
            Direction side = hit.getDirection();
            Containers.dropItemStack(level, pos.getX() + .5D + side.getStepX() / 3.0D,
                    pos.getY() + .5D, pos.getZ() + .5D + side.getStepZ() / 3.0D,
                    new ItemStack(ThaumcraftMod.JAR_LABEL.get()));
            level.playSound(null, pos, TC4Sounds.event("page"), SoundSource.BLOCKS, 1.0F, 1.1F);
            return InteractionResult.CONSUME;
        }

        if (player.isShiftKeyDown() && held.isEmpty()) {
            alembic.clearEssentia();
            level.playSound(null, pos, TC4Sounds.event("alembicknock"), SoundSource.BLOCKS, 0.2F, 1.0F);
            level.playSound(null, pos, SoundEvents.PLAYER_SWIM, SoundSource.BLOCKS, 0.5F,
                    1.0F + (level.random.nextFloat() - level.random.nextFloat()) * 0.3F);
            return InteractionResult.CONSUME;
        }

        if (!player.isShiftKeyDown() && held.isEmpty()) {
            player.displayClientMessage(Component.translatable("tile.alembic.msg." + alembic.fillMessageIndex()), true);
            level.playSound(null, pos, TC4Sounds.event("alembicknock"), SoundSource.BLOCKS, 0.2F, 1.0F);
            return InteractionResult.CONSUME;
        }

        if (held.getItem() instanceof JarLabelItem && alembic.aspectFilter() == null) {
            Aspect labelAspect = JarLabelItem.getAspect(held);
            if (alembic.amount() == 0 && labelAspect == null) return InteractionResult.CONSUME;
            Aspect target = alembic.amount() == 0 ? labelAspect : alembic.storedAspect();
            if (target != null && alembic.setAspectFilter(target)) {
                if (!player.getAbilities().instabuild) held.shrink(1);
                level.playSound(null, pos, TC4Sounds.event("page"), SoundSource.BLOCKS, 1.0F, 0.9F);
            }
            return InteractionResult.CONSUME;
        }

        if (held.getItem() instanceof EssentiaJarBlockItem jarItem && alembic.amount() > 0) {
            Aspect stored = alembic.storedAspect();
            AspectList jarAspects = EssentiaJarBlockItem.itemAspects(held);
            Aspect jarAspect = jarAspects.firstAspect();
            Aspect jarFilter = EssentiaJarBlockItem.itemFilter(held);
            if ((jarAspect == null || jarAspect == stored) && (jarFilter == null || jarFilter == stored)) {
                boolean voidJar = jarItem.getBlock() == ThaumcraftMod.VOID_ESSENTIA_JAR.get();
                int existing = jarAspect == null ? 0 : jarAspects.get(jarAspect);
                int moved = voidJar ? alembic.amount() : Math.min(64 - existing, alembic.amount());
                if (moved > 0) {
                    int retained = Math.min(64, existing + moved);
                    EssentiaJarBlockItem.writeItemData(held, stored, retained, jarFilter);
                    alembic.removeEssentia(stored, moved);
                    level.playSound(null, pos, SoundEvents.PLAYER_SWIM, SoundSource.BLOCKS, 0.5F,
                            1.0F + (level.random.nextFloat() - level.random.nextFloat()) * 0.3F);
                }
            }
            return InteractionResult.CONSUME;
        }
        return InteractionResult.PASS;
    }
}
