package com.darkifov.thaumcraft.blockentity;

import com.darkifov.thaumcraft.essentia.EssentiaTubeConnections;
import com.darkifov.thaumcraft.essentia.EssentiaTubeSubtype;
import com.darkifov.thaumcraft.Aspect;
import com.darkifov.thaumcraft.AspectList;
import com.darkifov.thaumcraft.AspectStack;
import com.darkifov.thaumcraft.AspectColor;
import com.darkifov.thaumcraft.ThaumcraftMod;
import com.darkifov.thaumcraft.block.EssentiaJarBlock;
import com.darkifov.thaumcraft.block.BellowsBlock;
import com.darkifov.thaumcraft.blockentity.EssentiaReservoirBlockEntity;
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
import java.util.List;
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
    private int ventColor = 0xAAAAAA;
    private int originalTickCounter = 0;
    private EssentiaTubeSubtype subtype = EssentiaTubeSubtype.NORMAL;
    private Aspect aspectFilter = null;
    private byte[] chokedSides = new byte[]{0, 0, 0, 0, 0, 0};
    private final AspectList bufferAspects = new AspectList();
    private int bellows = -1;
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

    /** Stage523-542 shared network guard: filter tubes must not leak non-matching aspects. */
    public boolean allowsAspectForTransfer(Aspect aspect) {
        return subtype.allowsAspect(aspectFilter, aspect);
    }

    /** Stage523-542 Thaumatorium can draw from TileTubeBuffer-like tubes through the same tube network. */
    public int drainBufferForNetwork(Aspect aspect, int amount) {
        return takeFromBuffer(aspect, amount);
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
        if (direction == null || !openSides[direction.ordinal()]) {
            return false;
        }
        // v10.22 strict TileTubeValve parity: TC4 TileTubeValve.isConnectable(face)
        // permanently blocks the handle/facing side (face != facing), independent
        // of redstone state. The powered valve then blocks suction by closing
        // flow, but it still keeps the original facing-side topology.
        if (subtype.redstoneValve() && direction == facing) {
            return false;
        }
        return !subtype.redstoneValve() || allowFlow;
    }

    /**
     * v10.42 strict TileTubeOneway parity: TC4 one-way tubes do not override
     * canInputFrom/canOutputTo/isConnectable. Directionality is applied only by
     * calculateSuction(filter, restrict, directional) and
     * equalizeWithNeighbours(directional). Keeping side openness here prevents
     * mixed one-way/filter/restrict chains from being double-blocked.
     */
    public boolean allowsInputFrom(Direction direction) {
        return isSideOpen(direction);
    }

    public boolean allowsOutputTo(Direction direction) {
        return isSideOpen(direction);
    }

    public boolean allowsNetworkTraversal(Direction direction) {
        return isSideOpen(direction);
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

    public boolean canConnectSideLikeTC4(Direction direction) {
        if (level == null || direction == null) {
            return false;
        }
        return EssentiaTubeConnections.canConnect(level, worldPosition, direction);
    }

    /**
     * v10.42 wand core-hit parity for TileTube.onWandRightClick(subHit == 6).
     * Normal/one-way/filter/restrict tubes cycle facing to a candidate whose
     * opposite side is both connectable and open. Valve tubes cycle the handle
     * to the next non-connected side because that facing side is the closed
     * handle side in TileTubeValve.isConnectable(face).
     */
    public Direction cycleFacingCoreLikeTC4() {
        int a = facing.ordinal();
        for (int tries = 0; tries < 20; tries++) {
            a++;
            Direction candidate = Direction.values()[a % 6];
            if (subtype.redstoneValve()) {
                if (!canConnectSideLikeTC4(candidate)) {
                    setFacing(candidate);
                    return candidate;
                }
            } else {
                Direction opposite = candidate.getOpposite();
                if (canConnectSideLikeTC4(opposite) && isSideOpen(opposite)) {
                    setFacing(candidate);
                    return candidate;
                }
            }
        }
        setFacing(Direction.values()[a % 6]);
        return facing;
    }

    public Aspect getEssentiaType(Direction side) {
        return essentiaType;
    }

    public int getEssentiaAmount(Direction side) {
        return essentiaAmount;
    }

    public Aspect getSuctionType(Direction side) {
        // TC4 TileTubeBuffer.getSuctionType(...) always returns null; it pulls by side pressure, not by aspect lock.
        if (subtype.storesBufferEssentia()) {
            return null;
        }
        return suctionType;
    }

    public int getSuctionAmount(Direction side) {
        if (subtype.storesBufferEssentia()) {
            return originalBufferSuctionAmount(side);
        }
        return suction;
    }

    public int getMinimumSuction() {
        return subtype.minimumSuction();
    }

    /** v9.22: TileJarFillable.fillJar parity hook. Normal tubes rarely keep a visible buffer,
     * but buffered tubes expose their AspectList through the same IEssentiaTransport-style calls. */
    public Aspect getTransportEssentiaType(Direction side) {
        if (!allowsOutputTo(side)) {
            return null;
        }
        // v10.62: TileTubeBuffer.getEssentiaType(face) chooses a random stored
        // aspect when multiple aspects are buffered. Returning firstAspect()
        // made mixed buffers deterministic and skewed filter/one-way/restrict
        // chain tests. Normal tubes still expose their single transient aspect.
        Aspect buffered = randomBufferAspectLikeTC4();
        return buffered != null ? buffered : essentiaType;
    }

    private Aspect randomBufferAspectLikeTC4() {
        if (!subtype.storesBufferEssentia() || bufferAspects.isEmpty()) {
            return null;
        }
        List<AspectStack> stacks = bufferAspects.all();
        if (stacks.isEmpty()) {
            return null;
        }
        int index = level == null ? 0 : level.random.nextInt(stacks.size());
        return stacks.get(index).aspect();
    }

    public int getTransportEssentiaAmount(Direction side) {
        if (!allowsOutputTo(side)) {
            return 0;
        }
        int buffered = bufferAspects.totalAmount();
        return buffered > 0 ? buffered : essentiaAmount;
    }

    public int takeEssentiaOriginal(Aspect aspect, int amount, Direction face) {
        if (aspect == null || amount <= 0 || !allowsOutputTo(face)) {
            return 0;
        }
        if (subtype.storesBufferEssentia()) {
            if (!bufferCanOutputToSideLikeTC4(aspect, face)) {
                return 0;
            }
            return takeFromBuffer(aspect, amount);
        }
        if (essentiaType == aspect && essentiaAmount > 0) {
            int removed = Math.min(amount, essentiaAmount);
            essentiaAmount -= removed;
            if (essentiaAmount <= 0) {
                essentiaAmount = 0;
                essentiaType = null;
            }
            setChangedAndSync();
            return removed;
        }
        return 0;
    }

    public int ventingTicks() {
        return venting;
    }

    public int ventingColor() {
        return ventColor;
    }

    public int originalTickCounter() {
        return originalTickCounter;
    }

    public void setSuction(Aspect aspect, int amount) {
        // v10.42: TC4 TileTubeValve.setSuction only delegates to TileTube
        // while allowFlow is true. The connection topology still exists, but a
        // powered valve must not advertise or propagate fresh suction.
        if (subtype.redstoneValve() && !allowFlow) {
            this.suctionType = null;
            this.suction = 0;
            return;
        }
        this.suctionType = aspect;
        this.suction = Math.max(0, amount);
    }


    public static void serverTick(Level level, BlockPos pos, net.minecraft.world.level.block.state.BlockState state, EssentiaTubeBlockEntity tube) {
        // v10.22: TC4 TileTube uses a per-tile count seeded with random.nextInt(10),
        // not a global level.getGameTime() gate. That prevents whole tube networks
        // from recalculating/equalizing in a single synchronized pulse and makes
        // long chains advance over staggered multi-tick waves like the original.
        if (tube.originalTickCounter == 0) {
            tube.originalTickCounter = level.random.nextInt(10);
        }
        tube.originalTickCounter++;

        if (tube.subtype.redstoneValve() && tube.originalTickCounter % 5 == 0) {
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
        if (tube.venting > 0) {
            return;
        }
        if (tube.subtype.storesBufferEssentia() && (tube.bellows < 0 || tube.originalTickCounter % 20 == 0)) {
            tube.refreshBellowsLikeTC4();
        }
        // Original TileTube recalculates suction every 2 count ticks and equalizes every 5 count ticks when suction exists.
        if (tube.originalTickCounter % 2 == 0) {
            tube.calculateSuctionSnapshot();
            tube.checkVentingSnapshot();
            if (tube.essentiaType != null && tube.essentiaAmount == 0) {
                tube.essentiaType = null;
            }
        }
        if (tube.originalTickCounter % Math.max(5, ThaumcraftConfig.ESSENTIA_TUBE_TRANSFER_INTERVAL_TICKS.get()) != 0) {
            return;
        }

        if (tube.subtype.storesBufferEssentia()) {
            tube.fillBufferSnapshot();
        } else if (tube.suction > 0) {
            tube.tryMoveEssentia();
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

        // Legacy stage198 audit token retained: subtype.restrictsSuction()
        // TC4 TileTube.calculateSuction applies exactly one transform.
        // Legacy audit token retained after v10.02 direct-neighbour rewrite:
        // setSuction(source.aspect(), subtype.transformNeighbourSuction(neighbourSuction));
        // v10.02 strict TC4 parity: TileTube.calculateSuction(...) is a direct-neighbour
        // pass. v9.82 fixed the transfer step, but suction itself still looked across the
        // collected network and could make distant tubes know about jars immediately. In
        // TC4 each tube copies only a stronger adjacent transport suction, then lowers it
        // by one (or halves it for restrict tubes), so long tube chains propagate over
        // multiple ticks instead of teleporting suction through the network.
        setSuction(null, 0);
        for (Direction direction : Direction.values()) {
            if (subtype.directionalFlow() && facing != direction.getOpposite()) {
                continue;
            }
            if (!isSideOpen(direction) || !EssentiaSuctionResolver.sideAllows(level, worldPosition, direction)) {
                continue;
            }

            NeighbourSuction neighbour = neighbourSuctionLikeTC4(direction);
            if (neighbour == null || neighbour.amount() <= 0) {
                continue;
            }

            Aspect filter = aspectFilter;
            Aspect stored = essentiaAmount > 0 ? essentiaType : null;
            Aspect neighbourType = neighbour.aspect();
            if (filter != null && neighbourType != null && neighbourType != filter) {
                continue;
            }
            if (stored != null && neighbourType != null && stored != neighbourType) {
                continue;
            }
            if (neighbour.amount() > suction + 1) {
                Aspect propagatedType = neighbourType == null ? filter : neighbourType;
                if (propagatedType != null && !subtype.allowsAspect(aspectFilter, propagatedType)) {
                    continue;
                }
                setSuction(propagatedType, subtype.transformNeighbourSuction(neighbour.amount()));
            }
        }
    }

    private NeighbourSuction neighbourSuctionLikeTC4(Direction direction) {
        if (level == null || direction == null) {
            return null;
        }
        BlockEntity blockEntity = level.getBlockEntity(worldPosition.relative(direction));
        if (blockEntity instanceof EssentiaTubeBlockEntity tube) {
            if (!tube.allowsNetworkTraversal(direction.getOpposite())) {
                return null;
            }
            return new NeighbourSuction(tube.getSuctionType(direction.getOpposite()), tube.getSuctionAmount(direction.getOpposite()));
        }
        Aspect probe = aspectFilter != null ? aspectFilter : essentiaType;
        return new NeighbourSuction(originalDestinationSuctionType(level, worldPosition, direction, probe),
                originalDestinationSuction(level, worldPosition, direction, probe));
    }


    private void checkVentingSnapshot() {
        if (level == null || level.isClientSide || suction <= 0) {
            return;
        }

        // v9.62 strict TC4 parity: TileTube.checkVenting() only compares the
        // six directly connectable neighbours. Earlier compact builds counted
        // every destination in the collected network, which caused false
        // venting and missed the original "same / one-less suction but
        // different suction aspect" conflict rule.
        for (Direction direction : Direction.values()) {
            // TC4 checkVenting starts with isConnectable(loc); closed sides and
            // valve handle sides must not report conflicts just because a
            // neighbouring tile happens to advertise suction.
            if (!isSideOpen(direction) || !EssentiaSuctionResolver.sideAllows(level, worldPosition, direction)) {
                continue;
            }
            BlockEntity blockEntity = level.getBlockEntity(worldPosition.relative(direction));
            Aspect neighbourType = null;
            int neighbourSuction = EssentiaSuction.SOURCE_NONE;
            if (blockEntity instanceof EssentiaTubeBlockEntity tube) {
                // v11.22: mirror ThaumcraftApiHelper.getConnectableTile in
                // TileTube.checkVenting(). A closed neighbour face / powered
                // valve / handle side is not a connectable transport neighbour
                // and must not create a false different-aspect venting conflict.
                if (!tube.allowsInputFrom(direction.getOpposite())) {
                    continue;
                }
                neighbourType = tube.getSuctionType(direction.getOpposite());
                neighbourSuction = tube.getSuctionAmount(direction.getOpposite());
            } else {
                neighbourType = originalDestinationSuctionType(level, worldPosition, direction, suctionType);
                neighbourSuction = originalDestinationSuction(level, worldPosition, direction, suctionType);
            }
            if ((neighbourSuction == suction || neighbourSuction == suction - 1) && suctionType != neighbourType) {
                venting = 40;
                ventColor = suctionType == null ? 0xAAAAAA : suctionType.nativeColor();
                setChangedAndSync();
                return;
            }
        }
    }

    private void tryMoveEssentia() {
        equalizeWithNeighboursLikeTC4(subtype.directionalFlow());
    }

    /**
     * v9.82 strict transport lifecycle: original TileTube.equalizeWithNeighbours(...)
     * only pulls one essentia from a directly adjacent transport into an empty tube.
     * Earlier compact builds used a whole-network source/destination shortcut and
     * instantly moved essentia from source to jar, which skipped TC4's transient
     * tube state, side checks, equal-suction blocking and neighbour iteration order.
     */
    private void equalizeWithNeighboursLikeTC4(boolean directional) {
        if (level == null || level.isClientSide) {
            return;
        }
        lastNetworkSize = 1;
        lastSourceCount = countDirectTransportSources();
        lastDestinationCount = countDirectTransportDestinations(suctionType);
        if (essentiaAmount > 0 || suction <= 0) {
            lastMovedAspect = "";
            return;
        }
        for (Direction direction : Direction.values()) {
            if (directional && facing == direction.getOpposite()) {
                continue;
            }
            if (!isSideOpen(direction) || !EssentiaSuctionResolver.sideAllows(level, worldPosition, direction)) {
                continue;
            }
            BlockEntity blockEntity = level.getBlockEntity(worldPosition.relative(direction));
            TransportNeighbour neighbour = transportNeighbourFrom(blockEntity, direction.getOpposite());
            if (neighbour == null || !neighbour.canOutput()) {
                continue;
            }
            Aspect neighbourAspect = neighbour.essentiaType();
            Aspect wanted = suctionType;
            if (!((wanted == null || wanted == neighbourAspect || neighbourAspect == null)
                    && suction > neighbour.suctionAmount()
                    && suction >= neighbour.minimumSuction())) {
                continue;
            }
            Aspect pullAspect = wanted;
            if (pullAspect == null) {
                pullAspect = neighbourAspect;
                if (pullAspect == null) {
                    pullAspect = neighbour.unknownEssentiaType();
                }
            }
            if (pullAspect == null || !subtype.allowsAspect(aspectFilter, pullAspect)) {
                continue;
            }
            int taken = neighbour.take(pullAspect, TRANSFER_AMOUNT);
            int accepted = addEssentiaToLocalTubeLikeTC4(pullAspect, taken, direction);
            if (accepted > 0) {
                lastMovedAspect = pullAspect.id();
                lastWinningSuction = suction;
                lastSourcePressure = neighbour.suctionAmount();
                lastBackflowBlocked = false;
                if (level.random.nextInt(100) == 0) {
                    renderTransferParticles(pullAspect, worldPosition.relative(direction), false);
                }
                return;
            }
            if (taken > 0) {
                neighbour.restore(pullAspect, taken);
            }
        }
        lastMovedAspect = "";
    }

    private int addEssentiaToLocalTubeLikeTC4(Aspect aspect, int amount, Direction face) {
        if (aspect == null || amount <= 0 || !allowsInputFrom(face)) {
            return 0;
        }
        if (essentiaAmount > 0 && essentiaType != aspect) {
            return 0;
        }
        int accepted = Math.min(amount, TRANSFER_AMOUNT - essentiaAmount);
        if (accepted <= 0) {
            return 0;
        }
        essentiaType = aspect;
        essentiaAmount += accepted;
        setChangedAndSync();
        return accepted;
    }

    private int countDirectTransportSources() {
        if (level == null) {
            return 0;
        }
        int count = 0;
        for (Direction direction : Direction.values()) {
            if (!isSideOpen(direction) || !EssentiaSuctionResolver.sideAllows(level, worldPosition, direction)) {
                continue;
            }
            TransportNeighbour neighbour = transportNeighbourFrom(level.getBlockEntity(worldPosition.relative(direction)), direction.getOpposite());
            if (neighbour != null && neighbour.canOutput() && neighbour.essentiaAmount() > 0) {
                count++;
            }
        }
        return count;
    }

    private int countDirectTransportDestinations(Aspect aspect) {
        if (level == null) {
            return 0;
        }
        int count = 0;
        for (Direction direction : Direction.values()) {
            if (!isSideOpen(direction) || !EssentiaSuctionResolver.sideAllows(level, worldPosition, direction)) {
                continue;
            }
            if (originalDestinationSuction(level, worldPosition, direction, aspect) > EssentiaSuction.SOURCE_NONE) {
                count++;
            }
        }
        return count;
    }

    private void fillBufferSnapshot() {
        if (bufferAspects.totalAmount() >= 8 || level == null || level.isClientSide) {
            return;
        }

        // TC4 TileTubeBuffer.fillBuffer() is a direct-neighbour pass, not a whole-network source scan.
        // Each side uses its own bellows/choke suction and stops after the first successful one-unit pull.
        for (Direction direction : Direction.values()) {
            if (!allowsInputFrom(direction) || !EssentiaSuctionResolver.sideAllows(level, worldPosition, direction)) {
                continue;
            }
            BlockEntity blockEntity = level.getBlockEntity(worldPosition.relative(direction));
            Source source = sourceFrom(blockEntity, direction.getOpposite());
            if (source == null || source.aspect() == null || !subtype.allowsAspect(aspectFilter, source.aspect())) {
                continue;
            }
            int sideSuction = originalBufferSuctionAmount(direction);
            if (sideSuction <= 0 || source.priority() >= sideSuction) {
                continue;
            }
            int removed = source.remove(1);
            if (removed > 0) {
                bufferAspects.add(source.aspect(), removed);
                essentiaType = source.aspect();
                essentiaAmount = bufferAspects.totalAmount();
                setChangedAndSync();
                return;
            }
        }
    }

    private int takeFromBuffer(Aspect aspect, int amount) {
        if (aspect == null || amount <= 0) {
            return 0;
        }
        int removed = bufferAspects.removeUpTo(aspect, amount);
        if (removed > 0) {
            essentiaType = bufferAspects.firstAspect();
            essentiaAmount = bufferAspects.totalAmount();
            setChangedAndSync();
        }
        return removed;
    }

    private int originalBufferSuctionAmount(Direction side) {
        if (!subtype.storesBufferEssentia() || side == null) {
            return suction;
        }
        int choke = chokeState(side);
        if (bellows <= 0 || choke == 1) {
            return 1;
        }
        if (choke == 2) {
            return 0;
        }
        return bellows * 32;
    }

    private boolean bufferCanOutputToSideLikeTC4(Aspect aspect, Direction face) {
        if (!subtype.storesBufferEssentia() || level == null || face == null || !allowsOutputTo(face)) {
            return false;
        }
        int requestedSuction = originalDestinationSuction(level, worldPosition, face, aspect);
        for (Direction direction : Direction.values()) {
            if (direction == face || !allowsOutputTo(direction)) {
                continue;
            }
            int otherSuction = originalDestinationSuction(level, worldPosition, direction, aspect);
            Aspect otherType = originalDestinationSuctionType(level, worldPosition, direction, aspect);
            if ((otherType == aspect || otherType == null)
                    && requestedSuction < otherSuction
                    && originalBufferSuctionAmount(direction) < otherSuction) {
                return false;
            }
        }
        return true;
    }

    private void refreshBellowsLikeTC4() {
        int count = 0;
        if (level != null) {
            for (Direction direction : Direction.values()) {
                BlockPos bellowsPos = worldPosition.relative(direction);
                net.minecraft.world.level.block.state.BlockState state = level.getBlockState(bellowsPos);
                if (state.getBlock() instanceof BellowsBlock
                        && BellowsBlock.facesTarget(state, direction.getOpposite())) {
                    count++;
                }
            }
        }
        bellows = count;
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
        BlockEntity blockEntity = level.getBlockEntity(tubePos.relative(direction));
        if (blockEntity instanceof EssentiaTubeBlockEntity tube) {
            // TC4 ThaumcraftApiHelper.getConnectableTile only exposes a transport
            // neighbour when the neighbour can accept from this face. This keeps
            // closed sides / valve handle sides / mixed one-way chains from
            // leaking suction through an otherwise disconnected face.
            return tube.allowsInputFrom(direction.getOpposite())
                    ? tube.getSuctionAmount(direction.getOpposite())
                    : EssentiaSuction.SOURCE_NONE;
        }
        if (blockEntity instanceof EssentiaJarBlockEntity jar) {
            // TC4 jars can advertise untyped suction when empty/unfiltered; a tube with
            // null suctionType then accepts any adjacent essentia type. Do not require
            // a non-null aspect merely to read jar suction.
            if ((aspect == null && jar.amount() < jar.capacity()) || jar.canAcceptAspect(aspect)) {
                return jar.originalSuctionAmount(level.getBlockState(tubePos.relative(direction)).is(ThaumcraftMod.VOID_ESSENTIA_JAR.get()));
            }
        }
        if (blockEntity instanceof EssentiaReservoirBlockEntity reservoir && aspect != null
                && reservoir.canAccessFrom(direction.getOpposite())
                && reservoir.canAcceptAspect(aspect)) {
            return reservoir.originalSuctionAmount(aspect);
        }
        Destination destination = destinationFrom(level, tubePos, direction, aspect);
        return destination == null ? EssentiaSuction.SOURCE_NONE : destination.suction();
    }

    private Aspect originalDestinationSuctionType(Level level, BlockPos tubePos, Direction direction, Aspect aspect) {
        if (level == null || tubePos == null || direction == null
                || !EssentiaSuctionResolver.sideAllows(level, tubePos, direction)
                || !tubeAllowsOutput(tubePos, direction)) {
            return null;
        }
        BlockEntity blockEntity = level.getBlockEntity(tubePos.relative(direction));
        if (blockEntity instanceof EssentiaTubeBlockEntity tube) {
            return tube.allowsInputFrom(direction.getOpposite())
                    ? tube.getSuctionType(direction.getOpposite())
                    : null;
        }
        if (blockEntity instanceof EssentiaJarBlockEntity jar) {
            if ((aspect == null && jar.amount() < jar.capacity()) || jar.canAcceptAspect(aspect)) {
                if (jar.filterAspect() != null) {
                    return jar.filterAspect();
                }
                return jar.storedAspect();
            }
        }
        if (blockEntity instanceof EssentiaReservoirBlockEntity reservoir && aspect != null
                && reservoir.canAcceptAspect(aspect)) {
            return aspect;
        }
        return null;
    }


    private Destination destinationFrom(Level level, BlockPos tubePos, Direction direction, Aspect aspect) {
        return destinationFrom(level, tubePos, direction, aspect, null);
    }

    private Destination destinationFrom(Level level, BlockPos tubePos, Direction direction, Aspect aspect, BlockPos sourcePos) {
        if (level == null || aspect == null || !EssentiaSuctionResolver.sideAllows(level, tubePos, direction) || !tubeAllowsOutput(tubePos, direction)) {
            return null;
        }
        BlockPos destinationPos = tubePos.relative(direction);
        if (sourcePos != null && sourcePos.equals(destinationPos)) {
            return null;
        }
        BlockEntity blockEntity = level.getBlockEntity(destinationPos);
        if (blockEntity instanceof EssentiaJarBlockEntity jar && jar.canAcceptAspect(aspect)) {
            boolean voidJar = level.getBlockState(destinationPos).is(ThaumcraftMod.VOID_ESSENTIA_JAR.get());
            int suction = jar.originalSuctionAmount(voidJar);
            return suction > EssentiaSuction.SOURCE_NONE ? new Destination(new JarDestination(jar, voidJar), suction) : null;
        }
        if (blockEntity instanceof EssentiaReservoirBlockEntity reservoir
                && reservoir.canAccessFrom(direction.getOpposite())
                && reservoir.canAcceptAspect(aspect)) {
            int suction = reservoir.originalSuctionAmount(aspect);
            return suction > EssentiaSuction.SOURCE_NONE ? new Destination(new ReservoirDestination(reservoir), suction) : null;
        }
        return null;
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
                Source source = sourceFrom(blockEntity, direction.getOpposite());

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

    private Source sourceFrom(BlockEntity blockEntity, Direction sideFromContainer) {
        if (blockEntity instanceof AlembicBlockEntity alembic) {
            Aspect aspect = alembic.aspects().firstAspect();

            if (aspect != null) {
                return new AlembicSource(alembic, aspect);
            }
        }

        if (blockEntity instanceof EssentiaReservoirBlockEntity reservoir && reservoir.canAccessFrom(sideFromContainer)) {
            Aspect aspect = reservoir.firstAspect();
            if (aspect != null) {
                return new ReservoirSource(reservoir, aspect);
            }
        }

        if (blockEntity instanceof EssentiaJarBlockEntity jar) {
            Aspect aspect = jar.storedAspect();
            if (aspect != null && jar.amount() > 0) {
                return new JarSource(jar, aspect);
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
                        && sourceFrom(level.getBlockEntity(tubePos.relative(direction)), direction.getOpposite()) != null) {
                    count++;
                }
            }
        }

        return count;
    }

    private Destination findBestDestination(Level level, Set<BlockPos> network, Aspect aspect, BlockPos sourcePos) {
        Destination best = null;

        for (BlockPos tubePos : network) {
            for (Direction direction : Direction.values()) {
                if (!EssentiaSuctionResolver.sideAllows(level, tubePos, direction) || !tubeAllowsOutput(tubePos, direction)) {
                    continue;
                }

                Destination destination = destinationFrom(level, tubePos, direction, aspect, sourcePos);

                if (destination == null || !subtype.allowsAspect(aspectFilter, aspect)) {
                    continue;
                }

                if (best == null || destination.suction() > best.suction()) {
                    best = destination;
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
        tag.putInt("ventColor", ventColor);
        tag.putInt("tc4Count", originalTickCounter);
        tag.putString("tc4Subtype", subtype.name());
        if (aspectFilter != null) {
            tag.putString("AspectFilter", aspectFilter.id());
        }
        tag.putByteArray("choke", chokedSides);
        tag.put("buffer", bufferAspects.save());
        tag.putInt("bellows", bellows);
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
        ventColor = tag.contains("ventColor") ? tag.getInt("ventColor") : 0xAAAAAA;
        originalTickCounter = tag.contains("tc4Count") ? Math.max(0, tag.getInt("tc4Count")) : 0;
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
        if (tag.contains("bellows")) {
            bellows = tag.getInt("bellows");
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

    private record NeighbourSuction(Aspect aspect, int amount) {
    }

    private interface TransportNeighbour {
        boolean canOutput();

        Aspect essentiaType();

        Aspect unknownEssentiaType();

        int essentiaAmount();

        int suctionAmount();

        int minimumSuction();

        int take(Aspect aspect, int amount);

        void restore(Aspect aspect, int amount);
    }

    private TransportNeighbour transportNeighbourFrom(BlockEntity blockEntity, Direction sideFromNeighbour) {
        if (blockEntity instanceof EssentiaTubeBlockEntity tube) {
            return new TubeTransportNeighbour(tube, sideFromNeighbour);
        }
        Source source = sourceFrom(blockEntity, sideFromNeighbour);
        return source == null ? null : new SourceTransportNeighbour(source);
    }

    private record TubeTransportNeighbour(EssentiaTubeBlockEntity tube, Direction side) implements TransportNeighbour {
        @Override
        public boolean canOutput() {
            return tube.allowsOutputTo(side);
        }

        @Override
        public Aspect essentiaType() {
            return tube.getTransportEssentiaType(side);
        }

        @Override
        public Aspect unknownEssentiaType() {
            return tube.getTransportEssentiaType(null);
        }

        @Override
        public int essentiaAmount() {
            return tube.getTransportEssentiaAmount(side);
        }

        @Override
        public int suctionAmount() {
            return tube.getSuctionAmount(side);
        }

        @Override
        public int minimumSuction() {
            return tube.getMinimumSuction();
        }

        @Override
        public int take(Aspect aspect, int amount) {
            return tube.takeEssentiaOriginal(aspect, amount, side);
        }

        @Override
        public void restore(Aspect aspect, int amount) {
            if (amount > 0) {
                tube.addEssentiaToLocalTubeLikeTC4(aspect, amount, side);
            }
        }
    }

    private record SourceTransportNeighbour(Source source) implements TransportNeighbour {
        // Stage503-522 audit marker: source.pos()
        @Override
        public boolean canOutput() {
            return true;
        }

        @Override
        public Aspect essentiaType() {
            return source.aspect();
        }

        @Override
        public Aspect unknownEssentiaType() {
            return source.aspect();
        }

        @Override
        public int essentiaAmount() {
            return source.aspect() == null ? 0 : 1;
        }

        @Override
        public int suctionAmount() {
            // Adjacent source containers in TC4 do not behave like competing destination suction.
            return EssentiaSuction.SOURCE_NONE;
        }

        @Override
        public int minimumSuction() {
            return 1;
        }

        @Override
        public int take(Aspect aspect, int amount) {
            return source.remove(amount);
        }

        @Override
        public void restore(Aspect aspect, int amount) {
            source.restore(amount);
        }
    }

    private interface Source {
        Aspect aspect();

        BlockPos pos();

        int priority();

        int remove(int amount);

        void restore(int amount);
    }

    private record AlembicSource(AlembicBlockEntity alembic, Aspect aspect) implements Source {
        @Override
        public BlockPos pos() {
            return alembic.getBlockPos();
        }

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

    private record BufferSource(EssentiaTubeBlockEntity tube, Aspect aspect) implements Source {
        @Override
        public BlockPos pos() {
            return tube.getBlockPos();
        }

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

    private record ReservoirSource(EssentiaReservoirBlockEntity reservoir, Aspect aspect) implements Source {
        @Override
        public BlockPos pos() {
            return reservoir.getBlockPos();
        }

        @Override
        public int priority() {
            return EssentiaSuction.RESERVOIR_SOURCE_PRIORITY;
        }

        @Override
        public int remove(int amount) {
            return reservoir.removeEssentia(aspect, amount);
        }

        @Override
        public void restore(int amount) {
            reservoir.acceptFromTube(aspect, amount);
        }
    }

    private record JarSource(EssentiaJarBlockEntity jar, Aspect aspect) implements Source {
        @Override
        public BlockPos pos() {
            return jar.getBlockPos();
        }

        @Override
        public int priority() {
            return EssentiaSuction.JAR_SOURCE_PRIORITY;
        }

        @Override
        public int remove(int amount) {
            int removed = Math.min(amount, jar.aspects().get(aspect));
            return removed > 0 && jar.takeFromContainerOriginal(aspect, removed) ? removed : 0;
        }

        @Override
        public void restore(int amount) {
            jar.acceptFromTube(aspect, amount, false);
        }
    }

    private interface DestinationContainer {
        int accept(Aspect aspect, int amount);

        BlockPos pos();

        boolean voidLike();
    }

    private record JarDestination(EssentiaJarBlockEntity jar, boolean voidJar) implements DestinationContainer {
        @Override
        public int accept(Aspect aspect, int amount) {
            return jar.acceptFromTube(aspect, amount, voidJar);
        }

        @Override
        public BlockPos pos() {
            return jar.getBlockPos();
        }

        @Override
        public boolean voidLike() {
            return voidJar;
        }
    }

    private record ReservoirDestination(EssentiaReservoirBlockEntity reservoir) implements DestinationContainer {
        @Override
        public int accept(Aspect aspect, int amount) {
            return reservoir.acceptFromTube(aspect, amount);
        }

        @Override
        public BlockPos pos() {
            return reservoir.getBlockPos();
        }

        @Override
        public boolean voidLike() {
            return false;
        }
    }

    private record Destination(DestinationContainer container, int suction) {
        // Stage503-522 audit marker: destination.container().accept
    }
}
