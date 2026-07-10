package com.darkifov.thaumcraft.blockentity;

import com.darkifov.thaumcraft.Aspect;
import com.darkifov.thaumcraft.ThaumcraftMod;
import com.darkifov.thaumcraft.aura.AuraVisRelayNetwork;
import com.darkifov.thaumcraft.block.EssentiaCrystalItem;
import com.darkifov.thaumcraft.block.EssentiaCrystalizerBlock;
import com.darkifov.thaumcraft.porting.TC4Sounds;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.Connection;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemHandlerHelper;

/** Full TC4 TileEssentiaCrystalizer behavior with modern item/vis adapters. */
public class EssentiaCrystalizerBlockEntity extends BlockEntity {
    public static final int EMPTY_SUCTION = 128;
    public static final int HOLDING_SUCTION = 64;
    public static final int PROCESS_MAX = 200;
    public static final int DRAW_INTERVAL = 5;

    private Aspect aspect;
    private int progress;
    private int counter;
    private float spin;
    private float spinInc;
    private int venting;

    public EssentiaCrystalizerBlockEntity(BlockPos pos, BlockState state) {
        super(ThaumcraftMod.ESSENTIA_CRYSTALIZER_BLOCK_ENTITY.get(), pos, state);
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, EssentiaCrystalizerBlockEntity tile) {
        if (!(level instanceof ServerLevel server)) return;
        tile.counter++;
        if (!tile.isPowered() && tile.counter % DRAW_INTERVAL == 0) {
            if (tile.aspect == null) {
                tile.drawEssentia();
                tile.progress = 0;
            } else {
                int requestPoints = Math.min(20, Math.max(1, (PROCESS_MAX - tile.progress) / 2));
                int drainedCv = AuraVisRelayNetwork.drainMachineVis(server, pos, Aspect.TERRA, requestPoints * 100);
                int drainedPoints = drainedCv / 100;
                tile.progress += 1 + drainedPoints * 2;
                tile.setChangedAndSync();
            }
        }
        if (tile.aspect != null && tile.progress >= PROCESS_MAX) {
            tile.ejectCrystal();
        }
    }

    public static void clientTick(Level level, BlockPos pos, BlockState state, EssentiaCrystalizerBlockEntity tile) {
        boolean spinning = tile.aspect != null && !tile.isPowered();
        if (spinning) tile.spinInc = Math.min(20.0F, tile.spinInc + 0.1F);
        else tile.spinInc = Math.max(0.0F, tile.spinInc - 0.2F);
        tile.spin = (tile.spin + tile.spinInc) % 360.0F;
        if (tile.venting > 0) tile.venting--;
    }

    public Direction inputFace() {
        return getBlockState().hasProperty(EssentiaCrystalizerBlock.FACING)
                ? getBlockState().getValue(EssentiaCrystalizerBlock.FACING) : Direction.DOWN;
    }

    public Direction outputFace() { return inputFace().getOpposite(); }
    public boolean canInputFrom(Direction face) { return face == inputFace(); }
    public int suctionAmount(Direction face) { return canInputFrom(face) ? (aspect == null ? EMPTY_SUCTION : isPowered() ? 0 : HOLDING_SUCTION) : 0; }
    public Aspect heldAspect() { return aspect; }
    public int progress() { return progress; }
    public float spin(float partialTick) { return spin + spinInc * partialTick; }
    public boolean isPowered() { return level != null && level.hasNeighborSignal(worldPosition); }

    public int addInput(Aspect offered, int amount, Direction face) {
        if (!canInputFrom(face) || offered == null || amount <= 0 || aspect != null || isPowered()) return 0;
        aspect = offered;
        progress = 0;
        setChangedAndSync();
        return 1;
    }

    private void drawEssentia() {
        if (level == null) return;
        Direction face = inputFace();
        Direction sourceFace = face.getOpposite();
        BlockEntity neighbor = level.getBlockEntity(worldPosition.relative(face));
        Aspect candidate = null;
        int taken = 0;

        // TC4 ThaumcraftApiHelper.getConnectableTile accepts any directly
        // connectable IEssentiaTransport endpoint, not only a tube. Preserve the
        // tube suction comparison and support every modern endpoint that exposes
        // the same one-unit source contract.
        if (neighbor instanceof EssentiaTubeBlockEntity tube
                && tube.allowsOutputTo(sourceFace)
                && tube.getTransportEssentiaAmount(sourceFace) > 0
                && tube.getSuctionAmount(sourceFace) < EMPTY_SUCTION
                && EMPTY_SUCTION >= tube.getMinimumSuction()) {
            candidate = tube.getTransportEssentiaType(sourceFace);
            if (candidate != null) taken = tube.takeEssentiaOriginal(candidate, 1, sourceFace);
        } else if (neighbor instanceof EssentiaJarBlockEntity jar && jar.amount() > 0) {
            candidate = jar.storedAspect();
            if (candidate != null && jar.takeFromContainerOriginal(candidate, 1)) taken = 1;
        } else if (neighbor instanceof EssentiaReservoirBlockEntity reservoir
                && reservoir.canAccessFrom(sourceFace) && !reservoir.aspects().isEmpty()) {
            candidate = reservoir.aspects().firstAspect();
            if (candidate != null) taken = reservoir.removeEssentia(candidate, 1);
        } else if (neighbor instanceof AlembicBlockEntity alembic
                && alembic.canOutputTo(sourceFace) && !alembic.aspects().isEmpty()) {
            candidate = alembic.storedAspect();
            if (candidate != null) taken = alembic.removeEssentia(candidate, 1);
        } else if (neighbor instanceof AlchemicalCentrifugeBlockEntity centrifuge
                && centrifuge.canOutputTo(sourceFace)) {
            candidate = centrifuge.outputType(sourceFace);
            if (candidate != null) taken = centrifuge.takeOutput(candidate, 1, sourceFace);
        } else if (neighbor instanceof AlchemicalFurnaceBlockEntity furnace
                && furnace.isAdvanced() && furnace.canAdvancedOutputTo(sourceFace)) {
            candidate = furnace.advancedOutputType(sourceFace);
            if (candidate != null) taken = furnace.takeAdvancedOutput(candidate, 1, sourceFace);
        }

        if (taken == 1 && candidate != null) {
            aspect = candidate;
            progress = 0;
            setChangedAndSync();
        }
    }

    private void ejectCrystal() {
        if (level == null || aspect == null) return;
        ItemStack crystal = EssentiaCrystalItem.create(ThaumcraftMod.ESSENTIA_CRYSTAL.get(), aspect);
        Direction output = outputFace();
        BlockEntity target = level.getBlockEntity(worldPosition.relative(output));
        if (target != null) {
            IItemHandler handler = target.getCapability(ForgeCapabilities.ITEM_HANDLER, output.getOpposite()).orElse(null);
            if (handler != null) crystal = ItemHandlerHelper.insertItemStacked(handler, crystal, false);
        }
        if (!crystal.isEmpty()) {
            double x = worldPosition.getX() + 0.5D + output.getStepX() * 0.65D;
            double y = worldPosition.getY() + 0.5D + output.getStepY() * 0.65D;
            double z = worldPosition.getZ() + 0.5D + output.getStepZ() * 0.65D;
            ItemEntity entity = new ItemEntity(level, x, y, z, crystal);
            entity.setDeltaMovement(output.getStepX() * 0.04D, output.getStepY() * 0.04D, output.getStepZ() * 0.04D);
            level.addFreshEntity(entity);
        }
        level.playSound(null, worldPosition, SoundEvents.FIRE_EXTINGUISH, SoundSource.BLOCKS, 0.25F,
                2.6F + (level.random.nextFloat() - level.random.nextFloat()) * 0.8F);
        aspect = null;
        progress = 0;
        venting = 7;
        setChangedAndSync();
    }

    public Component statusComponent() {
        return Component.literal("Essentia Crystallizer | ")
                .append(Component.literal(aspect == null ? "empty" : aspect.displayName()).withStyle(aspect == null ? net.minecraft.ChatFormatting.GRAY : aspect.color()))
                .append(Component.literal(" | " + progress + "/" + PROCESS_MAX + " | suction " + suctionAmount(inputFace())));
    }

    private void setChangedAndSync() {
        setChanged();
        if (level != null && !level.isClientSide) level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), Block.UPDATE_CLIENTS);
    }

    @Override protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        if (aspect != null) tag.putString("Aspect", aspect.id());
        tag.putInt("Progress", progress);
        tag.putInt("Counter", counter);
        tag.putFloat("Spin", spin);
        tag.putFloat("SpinInc", spinInc);
        tag.putInt("Venting", venting);
    }

    @Override public void load(CompoundTag tag) {
        super.load(tag);
        aspect = Aspect.byId(tag.getString("Aspect"));
        progress = Math.max(0, tag.getInt("Progress"));
        counter = Math.max(0, tag.getInt("Counter"));
        spin = tag.getFloat("Spin");
        spinInc = tag.getFloat("SpinInc");
        venting = Math.max(0, tag.getInt("Venting"));
    }

    @Override public CompoundTag getUpdateTag() { CompoundTag tag = super.getUpdateTag(); saveAdditional(tag); return tag; }
    @Override public ClientboundBlockEntityDataPacket getUpdatePacket() { return ClientboundBlockEntityDataPacket.create(this); }
    @Override public void onDataPacket(Connection connection, ClientboundBlockEntityDataPacket packet) { load(packet.getTag()); }
}
