package com.darkifov.thaumcraft.blockentity;

import com.darkifov.thaumcraft.essentia.EssentiaTubeConnections;
import com.darkifov.thaumcraft.essentia.EssentiaTubeSubtype;
import com.darkifov.thaumcraft.Aspect;
import com.darkifov.thaumcraft.AspectList;
import com.darkifov.thaumcraft.AspectColor;
import com.darkifov.thaumcraft.ThaumcraftMod;
import com.darkifov.thaumcraft.block.EssentiaJarBlock;
import com.darkifov.thaumcraft.block.EssentiaValveBlock;
import com.darkifov.thaumcraft.config.ThaumcraftConfig;
import com.darkifov.thaumcraft.essentia.EssentiaSuction;
import com.darkifov.thaumcraft.essentia.EssentiaBackflowResult;
import com.darkifov.thaumcraft.essentia.EssentiaSuctionResolver;
import com.mojang.math.Vector3f;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.level.block.Block;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;

import java.util.ArrayDeque;
import java.util.HashSet;
import java.util.Set;

public class EssentiaTubeBlockEntity extends BlockEntity {
    // Directional tube pass: traversal is prepared to respect EssentiaTubeConnections side checks.
    private boolean connectedTransportNeighbor(BlockPos origin, net.minecraft.core.Direction direction) {
        if (level == null || !EssentiaSuctionResolver.sideAllows(level, origin, direction)) {
            return false;
        }
        BlockEntity current = level.getBlockEntity(origin);
        if (current instanceof EssentiaTubeBlockEntity tube && !tube.allowsNetworkTraversal(direction)) {
            return false;
        }
        BlockEntity neighbor = level.getBlockEntity(origin.relative(direction));
        return !(neighbor instanceof EssentiaTubeBlockEntity tube) || tube.allowsNetworkTraversal(direction.getOpposite());
    }

    public static final int MAX_NETWORK = 48;
    public static final int TRANSFER_AMOUNT = 1;

    public static final int SUCTION_NORMAL_JAR = EssentiaSuction.JAR_NORMAL;
    public static final int SUCTION_FILTERED_JAR = EssentiaSuction.JAR_FILTERED;
    public static final int SUCTION_VOID_JAR = EssentiaSuction.JAR_VOID;
    public static final int SUCTION_VOID_FILTERED_JAR = EssentiaSuction.JAR_VOID_FILTERED;

    // Original TileTube NBT/state names: facing, openSides, essentiaType/Amount, suctionType/suction, venting.
    private Direction facing = Direction.NORTH;
    private boolean[] openSides = new boolean[]{true, true, true, true, true, true};
    private Aspect essentiaType = null;
    private int essentiaAmount = 0;
    private Aspect suctionType = null;
    private int suction = 0;
    private int venting = 0;
    private EssentiaTubeSubtype subtype = EssentiaTubeSubtype.NORMAL;
    private Aspect aspectFilter = null;
    private byte[] chokedSides = new byte[]{0, 0, 0, 0, 0, 0};
    private final AspectList bufferAspects = new AspectList();
    private boolean allowFlow = true;
    private boolean wasPoweredLastTick = false;

    private int lastNetworkSize;
    private int lastSourceCount;
    private int lastDestinationCount;
    private String lastMovedAspect = "";
    private int lastConflictCount;
    private int lastWinningSuction;
    private int lastSourcePressure;
    private boolean lastBackflowBlocked;

    public EssentiaTubeBlockEntity(BlockPos pos, net.minecraft.world.level.block.state.BlockState state) {
        super(ThaumcraftMod.ESSENTIA_TUBE_BLOCK_ENTITY.get(), pos, state);
        if (state.getBlock() instanceof com.darkifov.thaumcraft.block.EssentiaTubeBlock tubeBlock) {
            this.subtype = tubeBlock.subtype();
        }
    }

    public EssentiaTubeSubtype subtype() {
        return subtype;
    }

    public void setSubtype(EssentiaTubeSubtype subtype) {
        this.subtype = subtype == null ? EssentiaTubeSubtype.NORMAL : subtype;
    }

    public String aspectFilterId() {
        return aspectFilter == null ? "none" : aspectFilter.id();
    }

    public Aspect aspectFilter() {
        return aspectFilter;
    }

    public Aspect bufferAspect() {
        return bufferAspects.firstAspect();
    }

    public int bufferAmount() {
        return bufferAspects.totalAmount();
    }

    public float bufferFillRatio() {
        return Math.min(1.0F, bufferAspects.totalAmount() / 8.0F);
    }

    public boolean isFlowAllowed() {
        return allowFlow;
    }

    public boolean isVenting() {
        return venting > 0;
    }

    public byte[] chokedSidesSnapshot() {
        return chokedSides.clone();
    }

    public void setAspectFilter(Aspect filter) {
        this.aspectFilter = filter;
        setChangedAndSync();
    }

    public int chokeState(Direction direction) {
        return direction == null ? 0 : chokedSides[direction.ordinal()] & 255;
    }

    public void cycleChoke(Direction direction) {
        if (direction == null) {
            return;
        }
        int index = direction.ordinal();
        chokedSides[index] = (byte) ((chokedSides[index] + 1) % 3);
        setChangedAndSync();
    }

    public void toggleSideWithNeighbour(Direction direction) {
        if (direction == null || level == null) {
            return;
        }
        setSideOpen(direction, !isSideOpen(direction));
        BlockEntity neighbour = level.getBlockEntity(worldPosition.relative(direction));
        if (neighbour instanceof EssentiaTubeBlockEntity tube) {
            tube.setSideOpen(direction.getOpposite(), isSideOpen(direction));
        }
    }

    public boolean isSideOpen(Direction direction) {
        return direction != null && openSides[direction.ordinal()] && (!subtype.redstoneValve() || allowFlow);
    }

    /** Stage204 TileTubeOneway adapter: one side receives suction, the facing side outputs essentia. */
    public boolean allowsInputFrom(Direction direction) {
        return isSideOpen(direction) && (!subtype.directionalFlow() || direction == facing.getOpposite());
    }

    /** Stage204 TileTubeOneway adapter: movement leaves a one-way tube only through its facing side. */
    public boolean allowsOutputTo(Direction direction) {
        return isSideOpen(direction) && (!subtype.directionalFlow() || direction == facing);
    }

    public boolean allowsNetworkTraversal(Direction direction) {
        return isSideOpen(direction) && (!subtype.directionalFlow() || direction == facing || direction == facing.getOpposite());
    }

    public void setSideOpen(Direction direction, boolean open) {
        if (direction == null) {
            return;
        }
        openSides[direction.ordinal()] = open;
        setChangedAndSync();
    }

    public Direction facing() {
        return facing;
    }

    public void setFacing(Direction facing) {
        this.facing = facing == null ? Direction.NORTH : facing;
        setChangedAndSync();
    }

    public Aspect getEssentiaType(Direction side) {
        return essentiaType;
    }

    public int getEssentiaAmount(Direction side) {
        return essentiaAmount;
    }

    public Aspect getSuctionType(Direction side) {
        return suctionType;
    }

    public int getSuctionAmount(Direction side) {
        return suction;
    }

    public int getMinimumSuction() {
        return subtype.minimumSuction();
    }

    public int ventingTicks() {
        return venting;
    }

    public void setSuction(Aspect aspect, int amount) {
        this.suctionType = aspect;
        this.suction = Math.max(0, amount);
    }


    public static void serverTick(Level level, BlockPos pos, net.minecraft.world.level.block.state.BlockState state, EssentiaTubeBlockEntity tube) {
        if (tube.subtype.redstoneValve() && level.getGameTime() % 5L == 0L) {
            boolean gettingPower = level.hasNeighborSignal(pos);
            if (gettingPower != tube.wasPoweredLastTick) {
                tube.allowFlow = !gettingPower;
                tube.wasPoweredLastTick = gettingPower;
                tube.setChangedAndSync();
            }
        }
        if (tube.venting > 0) {
            tube.venting--;
        }
        // Original TileTube recalculates suction every 2 ticks and equalizes every 5 ticks when suction exists.
        if (level.getGameTime() % 2L == 0L) {
            tube.calculateSuctionSnapshot();
            tube.checkVentingSnapshot();
        }
        if (level.getGameTime() % Math.max(5, ThaumcraftConfig.ESSENTIA_TUBE_TRANSFER_INTERVAL_TICKS.get()) != 0L) {
            return;
        }

        tube.tryMoveEssentia();
        if (tube.subtype.storesBufferEssentia()) {
            tube.fillBufferSnapshot();
        }
    }

    public int networkSize() {
        if (level == null) {
            return 0;
        }

        return collectTubeNetwork(level, worldPosition).size();
    }

    public int lastNetworkSize() {
        return lastNetworkSize;
    }

    public int lastSourceCount() {
        return lastSourceCount;
    }

    public int lastDestinationCount() {
        return lastDestinationCount;
    }

    public String lastMovedAspect() {
        return lastMovedAspect;
    }

    public String connectedSidesDiagnostic(Level level, BlockPos pos) {
        return "Connected sides: " + EssentiaTubeConnections.summary(level, pos);
    }

    public int lastConflictCount() {
        return lastConflictCount;
    }

    public int lastWinningSuction() {
        return lastWinningSuction;
    }

    public int lastSourcePressure() {
        return lastSourcePressure;
    }

    public boolean lastBackflowBlocked() {
        return lastBackflowBlocked;
    }

    private void calculateSuctionSnapshot() {
        if (level == null || level.isClientSide) {
            return;
        }
        Set<BlockPos> network = collectTubeNetwork(level, worldPosition);
        Source source = findBestSource(level, network);
        if (source == null || source.aspect() == null) {
            setSuction(null, 0);
            return;
        }
        Destination destination = findBestDestinationJar(level, network, source.aspect());
        if (destination == null) {
            setSuction(null, 0);
            return;
        }
        // TileTube.calculateSuction stores neighbour suction - 1; restrict/filter/oneway subclasses transform this input.
        if (!subtype.allowsAspect(aspectFilter, source.aspect())) {
            setSuction(null, 0);
            return;
        }
        int neighbourSuction = destination.suction();
        if (subtype.restrictsSuction()) {
            // TileTubeRestrict deliberately chokes incoming suction before the shared TileTube transfer step.
            neighbourSuction = Math.max(0, neighbourSuction / 2);
        }
        setSuction(source.aspect(), subtype.transformNeighbourSuction(neighbourSuction));
    }

    private void checkVentingSnapshot() {
        if (level == null || level.isClientSide || suction <= 0 || suctionType == null) {
            return;
        }
        Set<BlockPos> network = collectTubeNetwork(level, worldPosition);
        int conflicts = EssentiaSuctionResolver.competingDestinations(level, network, suctionType, null);
        if (conflicts > 1) {
            venting = 40;
        }
    }

    private void tryMoveEssentia() {
        if (level == null || level.isClientSide) {
            return;
        }

        Set<BlockPos> network = collectTubeNetwork(level, worldPosition);
        lastNetworkSize = network.size();

        Source source = findBestSource(level, network);
        lastSourceCount = countSources(level, network);

        if (source == null || source.aspect() == null) {
            lastMovedAspect = "";
            lastWinningSuction = 0;
            lastSourcePressure = 0;
            lastConflictCount = 0;
            lastBackflowBlocked = false;
            return;
        }

        Destination destination = findBestDestinationJar(level, network, source.aspect());
        lastDestinationCount = countDestinations(level, network, source.aspect());

        if (destination == null || destination.suction() <= EssentiaSuction.SOURCE_NONE) {
            lastMovedAspect = "";
            lastWinningSuction = 0;
            lastSourcePressure = source.priority();
            lastConflictCount = 0;
            lastBackflowBlocked = true;
            return;
        }

        lastWinningSuction = destination.suction();
        lastSourcePressure = source.priority();
        lastConflictCount = EssentiaSuctionResolver.competingDestinations(level, network, source.aspect(), destination.jar().getBlockPos());
        lastBackflowBlocked = destination.suction() <= source.priority();

        EssentiaBackflowResult backflowResult = new EssentiaBackflowResult(
                source.aspect(),
                worldPosition,
                Direction.NORTH,
                destination.jar().getBlockPos(),
                Direction.SOUTH,
                source.priority(),
                destination.suction(),
                lastConflictCount,
                lastBackflowBlocked
        );

        if (!backflowResult.canMove()) {
            venting = 40;
            lastMovedAspect = "";
            setChangedAndSync();
            return;
        }

        int removed = source.remove(TRANSFER_AMOUNT);

        if (removed <= 0) {
            lastMovedAspect = "";
            return;
        }

        int accepted = destination.jar().acceptFromTube(source.aspect(), removed, destination.voidJar());

        if (accepted <= 0) {
            source.restore(removed);
            lastMovedAspect = "";
            return;
        }

        essentiaType = source.aspect();
        essentiaAmount = Math.max(0, accepted);
        lastMovedAspect = source.aspect().id();
        renderTransferParticles(source.aspect(), destination.jar().getBlockPos(), destination.voidJar());
        essentiaAmount = 0;
        essentiaType = null;
        setChangedAndSync();
    }


    private void fillBufferSnapshot() {
        if (bufferAspects.totalAmount() >= 8 || level == null || level.isClientSide) {
            return;
        }
        Source source = findBestSource(level, collectTubeNetwork(level, worldPosition));
        if (source == null || source.aspect() == null || !subtype.allowsAspect(aspectFilter, source.aspect())) {
            return;
        }
        int removed = source.remove(1);
        if (removed > 0) {
            bufferAspects.add(source.aspect(), removed);
            essentiaType = source.aspect();
            essentiaAmount = bufferAspects.totalAmount();
            setChangedAndSync();
        }
    }

    private int takeFromBuffer(Aspect aspect, int amount) {
        if (aspect == null || amount <= 0) {
            return 0;
        }
        int removed = bufferAspects.removeUpTo(aspect, amount);
        if (removed > 0) {
            setChangedAndSync();
        }
        return removed;
    }

    private void renderTransferParticles(Aspect aspect, BlockPos destination, boolean voidJar) {
        if (!(level instanceof ServerLevel serverLevel)) {
            return;
        }

        int rgb = AspectColor.rgb(aspect);
        float r = ((rgb >> 16) & 255) / 255.0F;
        float g = ((rgb >> 8) & 255) / 255.0F;
        float b = (rgb & 255) / 255.0F;

        serverLevel.sendParticles(new DustParticleOptions(new Vector3f(r, g, b), 0.8F),
                worldPosition.getX() + 0.5D,
                worldPosition.getY() + 0.5D,
                worldPosition.getZ() + 0.5D,
                3,
                0.18D,
                0.18D,
                0.18D,
                0.01D);
        serverLevel.sendParticles(voidJar ? ParticleTypes.REVERSE_PORTAL : ParticleTypes.WITCH,
                destination.getX() + 0.5D,
                destination.getY() + 0.95D,
                destination.getZ() + 0.5D,
                2,
                0.12D,
                0.12D,
                0.12D,
                0.01D);
    }

    private Set<BlockPos> collectTubeNetwork(Level level, BlockPos start) {
        Set<BlockPos> visited = new HashSet<>();
        ArrayDeque<BlockPos> queue = new ArrayDeque<>();
        queue.add(start.immutable());
        visited.add(start.immutable());

        while (!queue.isEmpty() && visited.size() < ThaumcraftConfig.ESSENTIA_TUBE_MAX_NETWORK.get()) {
            BlockPos current = queue.removeFirst();

            for (Direction direction : Direction.values()) {
                BlockPos next = current.relative(direction);

                if (visited.contains(next)) {
                    continue;
                }

                if (connectedTransportNeighbor(current, direction) && isOpenTubeLike(level, next)) {
                    visited.add(next.immutable());
                    queue.add(next.immutable());
                }
            }
        }

        return visited;
    }

    private boolean isOpenTubeLike(Level level, BlockPos pos) {
        return EssentiaSuctionResolver.isTubeLike(level, pos);
    }

    private boolean tubeAllowsInput(BlockPos tubePos, Direction direction) {
        BlockEntity blockEntity = level == null ? null : level.getBlockEntity(tubePos);
        return !(blockEntity instanceof EssentiaTubeBlockEntity tube) || tube.allowsInputFrom(direction);
    }

    private boolean tubeAllowsOutput(BlockPos tubePos, Direction direction) {
        BlockEntity blockEntity = level == null ? null : level.getBlockEntity(tubePos);
        return !(blockEntity instanceof EssentiaTubeBlockEntity tube) || tube.allowsOutputTo(direction);
    }

    private int originalDestinationSuction(Level level, BlockPos tubePos, Direction direction, Aspect aspect) {
        if (!EssentiaSuctionResolver.sideAllows(level, tubePos, direction) || !tubeAllowsOutput(tubePos, direction)) {
            return EssentiaSuction.SOURCE_NONE;
        }
        BlockPos destinationPos = tubePos.relative(direction);
        BlockEntity blockEntity = level.getBlockEntity(destinationPos);
        if (!(blockEntity instanceof EssentiaJarBlockEntity jar) || !jar.canAcceptAspect(aspect)) {
            return EssentiaSuction.SOURCE_NONE;
        }
        boolean voidJar = level.getBlockState(destinationPos).is(ThaumcraftMod.VOID_ESSENTIA_JAR.get());
        return jar.originalSuctionAmount(voidJar);
    }

    private Source findBestSource(Level level, Set<BlockPos> network) {
        Source best = null;

        for (BlockPos tubePos : network) {
            for (Direction direction : Direction.values()) {
                if (!EssentiaSuctionResolver.sideAllows(level, tubePos, direction) || !tubeAllowsInput(tubePos, direction)) {
                    continue;
                }

                BlockPos adjacent = tubePos.relative(direction);
                BlockEntity blockEntity = level.getBlockEntity(adjacent);
                Source source = sourceFrom(blockEntity);

                if (source == null) {
                    continue;
                }

                if (best == null || source.priority() > best.priority()) {
                    best = source;
                }
            }
        }

        return best;
    }

    private Source sourceFrom(BlockEntity blockEntity) {
        if (blockEntity instanceof AlembicBlockEntity alembic) {
            Aspect aspect = alembic.aspects().firstAspect();

            if (aspect != null) {
                return new AlembicSource(alembic, aspect);
            }
        }

        if (blockEntity instanceof AlchemicalFurnaceBlockEntity furnace) {
            Aspect aspect = furnace.firstAspect();

            if (aspect != null) {
                return new FurnaceSource(furnace, aspect);
            }
        }

        if (blockEntity instanceof EssentiaTubeBlockEntity tube && tube.subtype.storesBufferEssentia()) {
            Aspect aspect = tube.bufferAspects.firstAspect();
            if (aspect != null) {
                return new BufferSource(tube, aspect);
            }
        }

        return null;
    }

    private int countSources(Level level, Set<BlockPos> network) {
        int count = 0;

        for (BlockPos tubePos : network) {
            for (Direction direction : Direction.values()) {
                if (EssentiaSuctionResolver.sideAllows(level, tubePos, direction)
                        && tubeAllowsInput(tubePos, direction)
                        && sourceFrom(level.getBlockEntity(tubePos.relative(direction))) != null) {
                    count++;
                }
            }
        }

        return count;
    }

    private Destination findBestDestinationJar(Level level, Set<BlockPos> network, Aspect aspect) {
        Destination best = null;

        for (BlockPos tubePos : network) {
            for (Direction direction : Direction.values()) {
                if (!EssentiaSuctionResolver.sideAllows(level, tubePos, direction) || !tubeAllowsOutput(tubePos, direction)) {
                    continue;
                }

                BlockPos jarPos = tubePos.relative(direction);
                BlockEntity blockEntity = level.getBlockEntity(jarPos);

                if (!(blockEntity instanceof EssentiaJarBlockEntity jar) || !jar.canAcceptAspect(aspect) || !subtype.allowsAspect(aspectFilter, aspect)) {
                    continue;
                }

                boolean voidJar = level.getBlockState(jarPos).is(ThaumcraftMod.VOID_ESSENTIA_JAR.get());

                int suction = originalDestinationSuction(level, tubePos, direction, aspect);

                if (suction <= EssentiaSuction.SOURCE_NONE) {
                    continue;
                }

                if (best == null || suction > best.suction()) {
                    best = new Destination(jar, suction, voidJar);
                }
            }
        }

        return best;
    }

    private int countDestinations(Level level, Set<BlockPos> network, Aspect aspect) {
        int count = 0;

        for (BlockPos tubePos : network) {
            for (Direction direction : Direction.values()) {
                if (originalDestinationSuction(level, tubePos, direction, aspect) > EssentiaSuction.SOURCE_NONE) {
                    count++;
                }
            }
        }

        return count;
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
        if (essentiaType != null) {
            tag.putString("type", essentiaType.id());
        }
        tag.putInt("amount", essentiaAmount);
        tag.putInt("side", facing.ordinal());
        byte[] open = new byte[6];
        for (int i = 0; i < open.length; i++) {
            open[i] = (byte) (openSides[i] ? 1 : 0);
        }
        tag.putByteArray("open", open);
        if (suctionType != null) {
            tag.putString("stype", suctionType.id());
        }
        tag.putInt("samount", suction);
        tag.putInt("venting", venting);
        tag.putString("tc4Subtype", subtype.name());
        if (aspectFilter != null) {
            tag.putString("AspectFilter", aspectFilter.id());
        }
        tag.putByteArray("choke", chokedSides);
        tag.put("buffer", bufferAspects.save());
        tag.putBoolean("flow", allowFlow);
        tag.putBoolean("hadpower", wasPoweredLastTick);
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        essentiaType = Aspect.byId(tag.getString("type"));
        essentiaAmount = Math.max(0, tag.getInt("amount"));
        Direction[] directions = Direction.values();
        int side = tag.contains("side") ? tag.getInt("side") : Direction.NORTH.ordinal();
        facing = side >= 0 && side < directions.length ? directions[side] : Direction.NORTH;
        byte[] open = tag.getByteArray("open");
        if (open.length == 6) {
            for (int i = 0; i < openSides.length; i++) {
                openSides[i] = open[i] == 1;
            }
        }
        suctionType = Aspect.byId(tag.getString("stype"));
        suction = Math.max(0, tag.getInt("samount"));
        venting = Math.max(0, tag.getInt("venting"));
        if (tag.contains("tc4Subtype")) {
            subtype = EssentiaTubeSubtype.byName(tag.getString("tc4Subtype"));
        }
        aspectFilter = Aspect.byId(tag.getString("AspectFilter"));
        byte[] choke = tag.getByteArray("choke");
        if (choke.length == 6) {
            chokedSides = choke;
        }
        if (tag.contains("buffer")) {
            bufferAspects.load(tag.getCompound("buffer"));
        }
        if (tag.contains("flow")) {
            allowFlow = tag.getBoolean("flow");
        }
        if (tag.contains("hadpower")) {
            wasPoweredLastTick = tag.getBoolean("hadpower");
        }
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

    private interface Source {
        Aspect aspect();

        int priority();

        int remove(int amount);

        void restore(int amount);
    }

    private record AlembicSource(AlembicBlockEntity alembic, Aspect aspect) implements Source {
        @Override
        public int priority() {
            return EssentiaSuction.ALEMBIC_SOURCE_PRIORITY;
        }

        @Override
        public int remove(int amount) {
            return alembic.removeEssentia(aspect, amount);
        }

        @Override
        public void restore(int amount) {
            alembic.addEssentia(aspect, amount);
        }
    }

    private record FurnaceSource(AlchemicalFurnaceBlockEntity furnace, Aspect aspect) implements Source {
        @Override
        public int priority() {
            return EssentiaSuction.FURNACE_SOURCE_PRIORITY;
        }

        @Override
        public int remove(int amount) {
            return furnace.removeUpTo(aspect, amount);
        }

        @Override
        public void restore(int amount) {
            furnace.aspects().add(aspect, amount);
            furnace.setChangedAndSync();
        }
    }

    private record BufferSource(EssentiaTubeBlockEntity tube, Aspect aspect) implements Source {
        @Override
        public int priority() {
            int choke = tube.chokeState(tube.facing());
            if (choke == 2) {
                return 0;
            }
            return choke == 1 ? 1 : 32;
        }

        @Override
        public int remove(int amount) {
            return tube.takeFromBuffer(aspect, amount);
        }

        @Override
        public void restore(int amount) {
            tube.bufferAspects.add(aspect, amount);
            tube.setChangedAndSync();
        }
    }

    private record Destination(EssentiaJarBlockEntity jar, int suction, boolean voidJar) {
    }
}
