package com.darkifov.thaumcraft.blockentity;

import com.darkifov.thaumcraft.Aspect;
import com.darkifov.thaumcraft.ThaumcraftMod;
import com.darkifov.thaumcraft.aura.AuraVisRelayNetwork;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;

/**
 * TC4 TileVisRelay data model: attunement, parent link and five-tick consume pulse.
 * The graph itself is rebuilt from loaded block entities, so no global weak-reference
 * cache can survive dimension unloads or retain stale worlds.
 */
public class VisRelayBlockEntity extends BlockEntity {
    public static final int PULSE_TICKS = 5;
    private byte attunement = -1;
    private BlockPos parentPos;
    private Aspect pulseAspect;
    private long pulseStartGameTime = Long.MIN_VALUE;
    private long relayTick;

    public VisRelayBlockEntity(BlockPos pos, BlockState state) {
        this(ThaumcraftMod.VIS_RELAY_BLOCK_ENTITY.get(), pos, state);
    }

    protected VisRelayBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, VisRelayBlockEntity relay) {
        if (!(level instanceof ServerLevel serverLevel)) return;
        relay.relayTick++;
        if (relay.parentPos == null || relay.relayTick % 40L == 0L) {
            relay.refreshParent(serverLevel);
        }
    }

    public void refreshParent(ServerLevel level) {
        BlockPos next = AuraVisRelayNetwork.findConnection(level, worldPosition, attunement())
                .map(connection -> connection.nextParent(worldPosition))
                .orElse(null);
        setParentPos(next);
    }

    public byte attunement() {
        return attunement;
    }

    public byte cycleAttunement() {
        attunement++;
        if (attunement > 5) attunement = -1;
        parentPos = null;
        setChangedAndSync();
        return attunement;
    }

    public BlockPos parentPos() {
        return parentPos;
    }

    public void setParentPos(BlockPos parentPos) {
        BlockPos normalized = parentPos == null ? null : parentPos.immutable();
        if (java.util.Objects.equals(this.parentPos, normalized)) return;
        this.parentPos = normalized;
        setChangedAndSync();
    }

    public void triggerPulse(Aspect aspect) {
        if (level == null || level.isClientSide || aspect == null) return;
        long now = level.getGameTime();
        if (pulseStartGameTime != Long.MIN_VALUE && now - pulseStartGameTime < PULSE_TICKS) return;
        pulseAspect = aspect;
        pulseStartGameTime = now;
        setChangedAndSync();
    }

    public Aspect pulseAspect() {
        return pulseAspect;
    }

    public float pulseStrength(float partialTick) {
        if (level == null || pulseAspect == null || pulseStartGameTime == Long.MIN_VALUE) return 0.0F;
        float age = (level.getGameTime() + partialTick) - pulseStartGameTime;
        if (age < 0.0F || age >= PULSE_TICKS) return 0.0F;
        return 1.0F - age / PULSE_TICKS;
    }

    /** TC4 pRed/pGreen/pBlue returned gradually to white by 0.025 per tick. */
    public int beamColor(float partialTick) {
        if (level == null || pulseAspect == null || pulseStartGameTime == Long.MIN_VALUE) return 0xFFFFFF;
        float age = Math.max(0.0F, (level.getGameTime() + partialTick) - pulseStartGameTime);
        float fade = Math.min(1.0F, age * 0.025F);
        int source = relayColor(pulseAspect);
        int r = mix((source >> 16) & 255, 255, fade);
        int g = mix((source >> 8) & 255, 255, fade);
        int b = mix(source & 255, 255, fade);
        return (r << 16) | (g << 8) | b;
    }

    private static int mix(int from, int to, float amount) {
        return Math.max(0, Math.min(255, Math.round(from + (to - from) * amount)));
    }

    public static int relayColor(Aspect aspect) {
        if (aspect == null) return 0xFFFFFF;
        return switch (aspect) {
            case AER -> 0xFFFF7E;
            case IGNIS -> 0xFF3C01;
            case AQUA -> 0x0090FF;
            case TERRA -> 0x00A000;
            case ORDO -> 0xEECFFF;
            case PERDITIO -> 0x555577;
            default -> aspect.nativeColor();
        };
    }

    @Override
    public AABB getRenderBoundingBox() {
        AABB box = new AABB(worldPosition);
        return parentPos == null ? box : box.minmax(new AABB(parentPos)).inflate(1.0D);
    }

    protected void setChangedAndSync() {
        setChanged();
        if (level != null) level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.putByte("Attunement", attunement);
        if (parentPos != null) tag.putLong("ParentPos", parentPos.asLong());
        tag.putString("PulseAspect", pulseAspect == null ? "" : pulseAspect.id());
        tag.putLong("PulseStart", pulseStartGameTime);
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        attunement = tag.contains("Attunement") ? tag.getByte("Attunement") : -1;
        parentPos = tag.contains("ParentPos") ? BlockPos.of(tag.getLong("ParentPos")) : null;
        pulseAspect = Aspect.byId(tag.getString("PulseAspect"));
        pulseStartGameTime = tag.contains("PulseStart") ? tag.getLong("PulseStart") : Long.MIN_VALUE;
    }

    @Override
    public CompoundTag getUpdateTag() {
        return saveWithoutMetadata();
    }

    @Override
    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public void onDataPacket(Connection connection, ClientboundBlockEntityDataPacket packet) {
        CompoundTag tag = packet.getTag();
        if (tag != null) load(tag);
    }
}
