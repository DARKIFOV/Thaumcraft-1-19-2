package com.darkifov.thaumcraft.block;

import com.darkifov.thaumcraft.ThaumcraftMod;
import com.darkifov.thaumcraft.blockentity.ArcaneEarBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.BlockHitResult;

import javax.annotation.Nullable;

/**
 * TC4 Arcane Ear (BlockWoodenDevice metadata 1) port.
 *
 * <p>The block state carries only the visible/redstone powered flag. The
 * selected note, material-derived instrument and ten-tick pulse timer live in
 * {@link ArcaneEarBlockEntity}.</p>
 */
public final class ArcaneEarBlock extends BaseEntityBlock {
    public static final BooleanProperty POWERED = BlockStateProperties.POWERED;
    /** Modern block-event packets use an unsigned byte, so 5 is the silent particle event. */
    public static final int SILENT_NOTE_EVENT = TC4ArcaneEarParity.SILENT_NOTE_EVENT;

    public ArcaneEarBlock(Properties properties) {
        super(properties);
        registerDefaultState(stateDefinition.any().setValue(POWERED, false));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<net.minecraft.world.level.block.Block, BlockState> builder) {
        builder.add(POWERED);
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new ArcaneEarBlockEntity(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state,
                                                                  BlockEntityType<T> type) {
        return level.isClientSide ? null : createTickerHelper(type,
                ThaumcraftMod.ARCANE_EAR_BLOCK_ENTITY.get(), ArcaneEarBlockEntity::serverTick);
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player,
                                 InteractionHand hand, BlockHitResult hit) {
        if (!level.isClientSide && level.getBlockEntity(pos) instanceof ArcaneEarBlockEntity ear) {
            ear.changePitch();
            ear.emitConfiguredNote(true);
        }
        return InteractionResult.sidedSuccess(level.isClientSide);
    }

    @Override
    public void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean moving) {
        super.onPlace(state, level, pos, oldState, moving);
        if (!oldState.is(state.getBlock()) && level.getBlockEntity(pos) instanceof ArcaneEarBlockEntity ear) {
            ear.updateToneFromSupport();
        }
    }

    @Override
    public void neighborChanged(BlockState state, Level level, BlockPos pos,
                                net.minecraft.world.level.block.Block neighborBlock, BlockPos neighborPos,
                                boolean moving) {
        super.neighborChanged(state, level, pos, neighborBlock, neighborPos, moving);
        if (level.getBlockEntity(pos) instanceof ArcaneEarBlockEntity ear) {
            ear.updateToneFromSupport();
        }
    }

    @Override
    public boolean isSignalSource(BlockState state) {
        return true;
    }

    @Override
    public int getSignal(BlockState state, BlockGetter level, BlockPos pos, net.minecraft.core.Direction side) {
        return state.getValue(POWERED) ? 15 : 0;
    }

    @Override
    public int getDirectSignal(BlockState state, BlockGetter level, BlockPos pos, net.minecraft.core.Direction side) {
        return state.getValue(POWERED) ? 15 : 0;
    }

    @Override
    public boolean canConnectRedstone(BlockState state, BlockGetter level, BlockPos pos,
                                      @Nullable net.minecraft.core.Direction direction) {
        return true;
    }

    @Override
    public boolean triggerEvent(BlockState state, Level level, BlockPos pos, int eventId, int eventParam) {
        if (eventId < 0 || eventId > SILENT_NOTE_EVENT) {
            return super.triggerEvent(state, level, pos, eventId, eventParam);
        }
        if (!level.isClientSide) {
            return true;
        }

        int note = TC4ArcaneEarParity.clampNote(eventParam);
        if (eventId < SILENT_NOTE_EVENT) {
            SoundEvent sound = soundForTone(eventId);
            float pitch = TC4ArcaneEarParity.notePitch(note);
            level.playLocalSound(pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D,
                    sound, SoundSource.RECORDS, TC4ArcaneEarParity.NOTE_SOUND_VOLUME, pitch, false);
        }
        level.addParticle(ParticleTypes.NOTE,
                pos.getX() + 0.5D, pos.getY() + TC4ArcaneEarParity.NOTE_PARTICLE_Y, pos.getZ() + 0.5D,
                note / 24.0D, 0.0D, 0.0D);
        return true;
    }

    private static SoundEvent soundForTone(int tone) {
        return switch (tone) {
            case 1 -> SoundEvents.NOTE_BLOCK_BASEDRUM;
            case 2 -> SoundEvents.NOTE_BLOCK_SNARE;
            case 3 -> SoundEvents.NOTE_BLOCK_HAT;
            case 4 -> SoundEvents.NOTE_BLOCK_BASS;
            default -> SoundEvents.NOTE_BLOCK_HARP;
        };
    }
}
