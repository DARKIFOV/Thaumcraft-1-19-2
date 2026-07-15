package com.darkifov.thaumcraft.blockentity;

import com.darkifov.thaumcraft.Aspect;
import com.darkifov.thaumcraft.ThaumcraftMod;
import com.darkifov.thaumcraft.porting.TC4Sounds;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

/**
 * TC4 TileCentrifuge parity core.
 * Pulls one non-primal aspect from below at suction 64, processes it for 39 ticks,
 * and exposes exactly one randomly selected component through the top.
 */
public class AlchemicalCentrifugeBlockEntity extends BlockEntity {
    public static final int ORIGINAL_INPUT_SUCTION = 64;
    public static final int ORIGINAL_PROCESS_TICKS = 39;
    public static final int ORIGINAL_DRAW_INTERVAL_TICKS = 5;

    private Aspect aspectIn;
    private Aspect aspectOut;
    private int process;
    private int counter;
    private float rotation;
    private float rotationSpeed;

    public AlchemicalCentrifugeBlockEntity(BlockPos pos, BlockState state) {
        super(ThaumcraftMod.ALCHEMICAL_CENTRIFUGE_BLOCK_ENTITY.get(), pos, state);
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, AlchemicalCentrifugeBlockEntity tile) {
        if (level == null || level.isClientSide) {
            return;
        }
        tile.counter++;
        if (tile.isPowered()) {
            return;
        }
        if (tile.aspectOut == null && tile.aspectIn == null && tile.counter % ORIGINAL_DRAW_INTERVAL_TICKS == 0) {
            tile.drawEssentiaFromBelow();
        }
        if (tile.process > 0) {
            tile.process--;
            tile.setChangedAndSync();
        }
        if (tile.aspectOut == null && tile.aspectIn != null && tile.process <= 0) {
            tile.processEssentia();
        }
    }

    public static void clientTick(Level level, BlockPos pos, BlockState state, AlchemicalCentrifugeBlockEntity tile) {
        boolean spinning = tile.aspectIn != null && !tile.isPowered();
        if (spinning && tile.rotationSpeed < 20.0F) {
            tile.rotationSpeed += 2.0F;
        } else if (!spinning && tile.rotationSpeed > 0.0F) {
            tile.rotationSpeed = Math.max(0.0F, tile.rotationSpeed - 0.5F);
        }
        float previous = tile.rotation;
        tile.rotation += tile.rotationSpeed;
        if (tile.rotation >= 360.0F) {
            tile.rotation -= 360.0F;
        }
        if (level != null && previous % 180.0F >= 160.0F && tile.rotation % 180.0F <= 20.0F && tile.rotationSpeed > 0.0F) {
            level.playLocalSound(pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D,
                    TC4Sounds.event("pump"), SoundSource.BLOCKS, 1.0F, 1.0F, false);
        }
    }

    public Aspect inputAspect() {
        return aspectIn;
    }

    public Aspect outputAspect() {
        return aspectOut;
    }

    public int process() {
        return process;
    }

    public float rotation(float partialTick) {
        return rotation + rotationSpeed * partialTick;
    }

    public boolean isPowered() {
        return level != null && level.hasNeighborSignal(worldPosition);
    }

    public boolean canInputFrom(Direction face) {
        return face == Direction.DOWN;
    }

    public boolean canOutputTo(Direction face) {
        return face == Direction.UP;
    }

    public int suctionAmount(Direction face) {
        return face == Direction.DOWN ? ORIGINAL_INPUT_SUCTION : 0;
    }

    public int outputAmount(Direction face) {
        return canOutputTo(face) && aspectOut != null ? 1 : 0;
    }

    public Aspect outputType(Direction face) {
        return outputAmount(face) > 0 ? aspectOut : null;
    }

    public int takeOutput(Aspect aspect, int amount, Direction face) {
        if (!canOutputTo(face) || aspectOut == null || aspectOut != aspect || amount <= 0) {
            return 0;
        }
        aspectOut = null;
        setChangedAndSync();
        return 1;
    }

    public void restoreOutput(Aspect aspect, int amount) {
        if (aspectOut == null && aspect != null && amount > 0) {
            aspectOut = aspect;
            setChangedAndSync();
        }
    }

    public int addInput(Aspect aspect, int amount, Direction face) {
        if (!canInputFrom(face) || aspect == null || aspect.isPrimal() || amount <= 0 || aspectIn != null) {
            return 0;
        }
        aspectIn = aspect;
        process = ORIGINAL_PROCESS_TICKS;
        setChangedAndSync();
        return 1;
    }

    private void drawEssentiaFromBelow() {
        if (level == null) {
            return;
        }
        BlockEntity below = level.getBlockEntity(worldPosition.below());
        if (!(below instanceof EssentiaTubeBlockEntity tube)
                || !tube.allowsOutputTo(Direction.UP)
                || tube.getTransportEssentiaAmount(Direction.UP) <= 0
                || tube.getSuctionAmount(Direction.UP) >= ORIGINAL_INPUT_SUCTION
                || ORIGINAL_INPUT_SUCTION < tube.getMinimumSuction()) {
            return;
        }
        Aspect candidate = tube.getTransportEssentiaType(Direction.UP);
        if (candidate == null || candidate.isPrimal()) {
            return;
        }
        int taken = tube.takeEssentiaOriginal(candidate, 1, Direction.UP);
        if (taken == 1) {
            aspectIn = candidate;
            process = ORIGINAL_PROCESS_TICKS;
            setChangedAndSync();
        }
    }

    private void processEssentia() {
        Aspect first = aspectIn.firstComponent();
        Aspect second = aspectIn.secondComponent();
        if (first == null || second == null) {
            aspectIn = null;
            process = 0;
            setChangedAndSync();
            return;
        }
        aspectOut = level != null && level.random.nextBoolean() ? first : second;
        aspectIn = null;
        process = 0;
        setChangedAndSync();
        if (level instanceof ServerLevel serverLevel) {
            serverLevel.playSound(null, worldPosition, TC4Sounds.event("pump"), SoundSource.BLOCKS, 0.8F, 1.0F);
        }
    }

    private void setChangedAndSync() {
        setChanged();
        if (level != null && !level.isClientSide) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), Block.UPDATE_CLIENTS);
        }
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        if (aspectIn != null) {
            tag.putString("aspectIn", aspectIn.id());
        }
        if (aspectOut != null) {
            tag.putString("aspectOut", aspectOut.id());
        }
        tag.putInt("process", process);
        tag.putInt("counter", counter);
        tag.putFloat("rotation", rotation);
        tag.putFloat("rotationSpeed", rotationSpeed);
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        aspectIn = Aspect.byId(tag.getString("aspectIn"));
        aspectOut = Aspect.byId(tag.getString("aspectOut"));
        process = Math.max(0, tag.getInt("process"));
        counter = Math.max(0, tag.getInt("counter"));
        rotation = tag.getFloat("rotation");
        rotationSpeed = tag.getFloat("rotationSpeed");
    }

    @Override
    public CompoundTag getUpdateTag() {
        CompoundTag tag = new CompoundTag();
        saveAdditional(tag);
        return tag;
    }

    @Override
    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public void onDataPacket(Connection connection, ClientboundBlockEntityDataPacket packet) {
        load(packet.getTag());
    }
}
