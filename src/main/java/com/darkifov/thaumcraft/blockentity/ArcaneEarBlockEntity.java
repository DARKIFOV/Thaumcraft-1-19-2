package com.darkifov.thaumcraft.blockentity;

import com.darkifov.thaumcraft.ThaumcraftMod;
import com.darkifov.thaumcraft.block.ArcaneEarBlock;
import com.darkifov.thaumcraft.block.TC4ArcaneEarParity;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.NoteBlockInstrument;
import net.minecraft.world.level.material.Material;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;

/** Direct behavioral port of TC4 TileSensor. */
public final class ArcaneEarBlockEntity extends BlockEntity {
    private static final Map<ServerLevel, List<PlayedNote>> NOTE_EVENTS = new WeakHashMap<>();

    private byte note;
    private byte tone;
    private int signalTicks;

    public ArcaneEarBlockEntity(BlockPos pos, BlockState state) {
        super(ThaumcraftMod.ARCANE_EAR_BLOCK_ENTITY.get(), pos, state);
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, ArcaneEarBlockEntity ear) {
        int next = TC4ArcaneEarParity.pulseAfterTick(ear.signalTicks);
        if (next != ear.signalTicks) {
            ear.signalTicks = next;
            if (ear.signalTicks == 0) {
                ear.setPowered(false);
                ear.setChanged();
            }
        }
        if (!(level instanceof ServerLevel serverLevel)) {
            return;
        }
        List<PlayedNote> events = NOTE_EVENTS.get(serverLevel);
        if (events == null || events.isEmpty()) {
            return;
        }
        for (PlayedNote event : events) {
            if (TC4ArcaneEarParity.matchesPlayedNote(Byte.toUnsignedInt(ear.tone),
                    Byte.toUnsignedInt(ear.note), event.tone(), event.note(), pos.distSqr(event.pos()))) {
                // TileSensor consumed only the first matching buffered event in its tick.
                ear.emitConfiguredNote(false);
                ear.signalTicks = TC4ArcaneEarParity.REDSTONE_PULSE_TICKS;
                ear.setPowered(true);
                ear.setChanged();
                break;
            }
        }
    }

    /** Buffer a server note event exactly like TC4 EventHandlerWorld.noteEvent. */
    public static void onNotePlayed(ServerLevel level, BlockPos source,
                                    NoteBlockInstrument instrument, int playedNote) {
        int playedTone = toneForInstrument(instrument);
        if (playedTone < TC4ArcaneEarParity.MIN_TONE
                || playedTone > TC4ArcaneEarParity.MAX_TONE
                || playedNote < TC4ArcaneEarParity.MIN_NOTE
                || playedNote > TC4ArcaneEarParity.MAX_NOTE) {
            return;
        }
        NOTE_EVENTS.computeIfAbsent(level, ignored -> new ArrayList<>())
                .add(new PlayedNote(source.immutable(), playedTone, playedNote));
    }

    /** TC4 cleared the per-world note-event list at WorldTickEvent phase END. */
    public static void clearNoteEvents(ServerLevel level) {
        List<PlayedNote> events = NOTE_EVENTS.get(level);
        if (events != null) {
            events.clear();
        }
    }

    public void changePitch() {
        note = (byte) TC4ArcaneEarParity.nextNote(Byte.toUnsignedInt(note));
        setChanged();
    }

    public void updateToneFromSupport() {
        if (level == null) {
            return;
        }
        byte updated = toneForMaterial(level.getBlockState(worldPosition.below()).getMaterial());
        if (updated != tone) {
            tone = updated;
            setChanged();
        }
    }

    /** Emit the configured note/particle when the space above is air. */
    public boolean emitConfiguredNote(boolean withSound) {
        if (level == null || !level.isEmptyBlock(worldPosition.above())) {
            return false;
        }
        int eventId = withSound ? Byte.toUnsignedInt(tone) : ArcaneEarBlock.SILENT_NOTE_EVENT;
        level.blockEvent(worldPosition, getBlockState().getBlock(), eventId, Byte.toUnsignedInt(note));
        return true;
    }

    private void setPowered(boolean powered) {
        if (level == null) {
            return;
        }
        BlockState state = getBlockState();
        if (state.hasProperty(ArcaneEarBlock.POWERED) && state.getValue(ArcaneEarBlock.POWERED) != powered) {
            level.setBlock(worldPosition, state.setValue(ArcaneEarBlock.POWERED, powered), 3);
        }
        level.updateNeighborsAt(worldPosition, state.getBlock());
        level.updateNeighborsAt(worldPosition.below(), state.getBlock());
    }

    @Override
    public void onLoad() {
        super.onLoad();
        updateToneFromSupport();
        if (level instanceof ServerLevel) {
            signalTicks = 0;
            if (getBlockState().hasProperty(ArcaneEarBlock.POWERED)
                    && getBlockState().getValue(ArcaneEarBlock.POWERED)) {
                setPowered(false);
            }
        }
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.putByte(TC4ArcaneEarParity.NBT_NOTE, note);
        tag.putByte(TC4ArcaneEarParity.NBT_TONE, tone);
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        note = (byte) TC4ArcaneEarParity.clampNote(tag.getByte(TC4ArcaneEarParity.NBT_NOTE));
        tone = (byte) TC4ArcaneEarParity.clampTone(tag.getByte(TC4ArcaneEarParity.NBT_TONE));
        // TileSensor persisted only note/tone. Redstone pulse and render state are transient.
        signalTicks = 0;
    }

    public int note() {
        return Byte.toUnsignedInt(note);
    }

    public int tone() {
        return Byte.toUnsignedInt(tone);
    }

    public int signalTicks() {
        return signalTicks;
    }

    private static byte toneForMaterial(Material material) {
        if (material == Material.STONE) {
            return 1;
        }
        if (material == Material.SAND) {
            return 2;
        }
        if (material == Material.GLASS) {
            return 3;
        }
        if (material == Material.WOOD) {
            return 4;
        }
        return 0;
    }

    private static int toneForInstrument(NoteBlockInstrument instrument) {
        return switch (instrument) {
            case HARP -> 0;
            case BASEDRUM -> 1;
            case SNARE -> 2;
            case HAT -> 3;
            case BASS -> 4;
            default -> -1;
        };
    }

    private record PlayedNote(BlockPos pos, int tone, int note) {
    }
}
