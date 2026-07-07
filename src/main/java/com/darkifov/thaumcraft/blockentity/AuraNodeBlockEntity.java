package com.darkifov.thaumcraft.blockentity;

import com.darkifov.thaumcraft.Aspect;
import com.darkifov.thaumcraft.AspectDatabase;
import com.darkifov.thaumcraft.AspectList;
import com.darkifov.thaumcraft.ThaumcraftMod;
import com.darkifov.thaumcraft.aura.AuraNodeModifier;
import com.darkifov.thaumcraft.aura.AuraNodeProfile;
import com.darkifov.thaumcraft.aura.AuraNodeType;
import com.darkifov.thaumcraft.aura.AuraNodeWorldRuntime;
import com.darkifov.thaumcraft.block.NodeStabilizerBlock;
import com.darkifov.thaumcraft.block.NodeTransducerBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;

import java.util.List;

public class AuraNodeBlockEntity extends BlockEntity {
    private static final Aspect[] PRIMARY = new Aspect[]{
            Aspect.AER,
            Aspect.TERRA,
            Aspect.IGNIS,
            Aspect.AQUA,
            Aspect.ORDO,
            Aspect.PERDITIO
    };

    private final AspectList aspects = new AspectList();
    private final AspectList baseAspects = new AspectList();
    private boolean initialized = false;
    private String nodeType = AuraNodeType.NORMAL.name();
    private String nodeModifier = AuraNodeModifier.NORMAL.name();
    private int stability = 100;
    private boolean scanned;
    private boolean energized;
    private boolean jarred;
    private int energizedTicks;
    private int hungerCooldown;

    public AuraNodeBlockEntity(BlockPos pos, BlockState state) {
        super(ThaumcraftMod.AURA_NODE_BLOCK_ENTITY.get(), pos, state);
    }

    public AspectList aspects() {
        return aspects;
    }

    public AspectList baseAspects() {
        return baseAspects;
    }

    public boolean initialized() {
        return initialized;
    }

    public AuraNodeType typedNodeType() {
        return AuraNodeType.fromName(nodeType);
    }

    public AuraNodeModifier typedNodeModifier() {
        return AuraNodeModifier.fromName(nodeModifier);
    }

    public int stability() {
        return stability;
    }

    public boolean scanned() {
        return scanned;
    }

    public boolean isEnergized() {
        return energized;
    }

    public boolean isJarredNode() {
        return jarred;
    }

    public int energizedTicks() {
        return energizedTicks;
    }

    public void markScanned() {
        scanned = true;
        setChangedAndSync();
    }

    public int visualSize() {
        float modifierScale = typedNodeModifier().sizeScale();
        float typeScale = switch (typedNodeType()) {
            case HUNGRY -> 1.20F;
            case PURE -> 1.08F;
            case UNSTABLE -> 1.12F;
            case DARK, TAINTED -> 1.05F;
            default -> 1.0F;
        };
        return Math.max(22, Math.min(112, Math.round(aspects.totalAmount() * modifierScale * typeScale)));
    }

    public String nodeType() {
        return nodeType;
    }

    public String nodeModifier() {
        return nodeModifier;
    }

    public boolean isStabilized() {
        return level != null && NodeStabilizerBlock.hasStabilizerNearby(level, worldPosition);
    }

    public int stabilizerStrength() {
        return level == null ? 0 : NodeStabilizerBlock.stabilizerStrength(level, worldPosition);
    }

    public boolean hasActiveTransducer() {
        return level != null && NodeTransducerBlock.isActiveTransducerNearby(level, worldPosition);
    }

    public void initializeFromPosition() {
        if (initialized) {
            return;
        }
        AuraNodeProfile profile = AuraNodeWorldRuntime.createProfile(worldPosition);
        initializeAs(profile.type(), profile.modifier(), profile.aspects());
    }

    public void initializeAs(AuraNodeType type, AuraNodeModifier modifier, AspectList profileAspects) {
        nodeType = type == null ? AuraNodeType.NORMAL.name() : type.name();
        nodeModifier = modifier == null ? AuraNodeModifier.NORMAL.name() : modifier.name();
        aspects.clear();
        aspects.addAll(profileAspects);
        baseAspects.clear();
        baseAspects.addAll(profileAspects);
        stability = switch (typedNodeType()) {
            case UNSTABLE -> 62;
            case HUNGRY -> 72;
            case TAINTED, DARK -> 80;
            case PURE -> 100;
            default -> 92;
        };
        if (typedNodeModifier() == AuraNodeModifier.FADING) {
            stability = Math.min(stability, 55);
        } else if (typedNodeModifier() == AuraNodeModifier.BRIGHT) {
            stability = Math.min(100, stability + 8);
        }
        energized = false;
        jarred = false;
        energizedTicks = 0;
        initialized = true;
        setChangedAndSync();
    }

    public void initializeFromJarTag(CompoundTag nodeTag) {
        if (nodeTag == null || nodeTag.isEmpty()) {
            initializeFromPosition();
            return;
        }
        nodeType = nodeTag.contains("NodeType") ? nodeTag.getString("NodeType") : AuraNodeType.NORMAL.name();
        nodeModifier = nodeTag.contains("NodeModifier") ? nodeTag.getString("NodeModifier") : AuraNodeModifier.PALE.name();
        stability = nodeTag.contains("Stability") ? nodeTag.getInt("Stability") : 45;
        scanned = nodeTag.getBoolean("Scanned");
        energized = nodeTag.getBoolean("Energized");
        jarred = true;
        energizedTicks = 0;
        aspects.clear();
        aspects.load(nodeTag.getCompound("Aspects"));
        baseAspects.clear();
        if (nodeTag.contains("BaseAspects")) {
            baseAspects.load(nodeTag.getCompound("BaseAspects"));
        } else {
            baseAspects.addAll(aspects);
        }
        initialized = true;
        setChangedAndSync();
    }

    public CompoundTag saveNodeJarTag() {
        CompoundTag tag = new CompoundTag();
        tag.putString("NodeType", nodeType);
        tag.putString("NodeModifier", nodeModifier);
        tag.putInt("Stability", stability);
        tag.putBoolean("Scanned", scanned);
        tag.putBoolean("Energized", energized);
        tag.putBoolean("Jarred", jarred);
        tag.put("Aspects", aspects.save());
        tag.put("BaseAspects", baseAspects.save());
        return tag;
    }

    public int drainToWand(Aspect aspect, int amount) {
        if (aspect == null || !aspect.isPrimal()) {
            return 0;
        }
        int drainCap = Math.max(1, Math.round(amount * (typedNodeModifier() == AuraNodeModifier.BRIGHT ? 1.15F : 1.0F)));
        int removed = aspects.removeUpTo(aspect, drainCap);

        if (removed > 0) {
            if (!isStabilized()) {
                stability = Math.max(0, stability - (typedNodeType() == AuraNodeType.UNSTABLE ? 2 : 1));
            }
            setChangedAndSync();
        }

        return removed;
    }

    public void regenerateSlowly() {
        if (baseAspects.isEmpty()) {
            baseAspects.addAll(aspects);
        }

        if (typedNodeModifier() == AuraNodeModifier.FADING && (level == null || level.getGameTime() % 1600L != 0L)) {
            return;
        }

        int regenAmount = typedNodeModifier() == AuraNodeModifier.BRIGHT ? 2 : 1;
        if (typedNodeType() == AuraNodeType.PURE) {
            regenAmount += 1;
        }
        if (energized) {
            regenAmount += 2;
        }
        if (stabilizerStrength() >= 2) {
            regenAmount += 1;
        } else if (isStabilized() && typedNodeType() == AuraNodeType.UNSTABLE) {
            regenAmount += 1;
        }

        int seed = Math.abs(worldPosition.getX() * 11 + worldPosition.getY() * 19 + worldPosition.getZ() * 23 + (int) (level == null ? 0 : level.getGameTime()));
        for (int offset = 0; offset < PRIMARY.length; offset++) {
            Aspect aspect = PRIMARY[(seed + offset) % PRIMARY.length];
            int max = Math.max(0, baseAspects.get(aspect));
            if (max > 0 && aspects.get(aspect) < max) {
                aspects.add(aspect, Math.min(regenAmount, max - aspects.get(aspect)));
                setChangedAndSync();
                return;
            }
        }

        if (typedNodeType() == AuraNodeType.PURE && baseAspects.get(Aspect.PRAECANTATIO) > 0 && aspects.get(Aspect.PRAECANTATIO) < baseAspects.get(Aspect.PRAECANTATIO)) {
            aspects.add(Aspect.PRAECANTATIO, 1);
            setChangedAndSync();
        }
    }

    public void tickNodeEffect(Level level) {
        boolean stabilized = isStabilized();
        AuraNodeType type = typedNodeType();

        if (type == AuraNodeType.TAINTED && !stabilized) {
            tickTainted(level);
        }

        if (type == AuraNodeType.PURE) {
            tickPure(level);
        }

        if (type == AuraNodeType.HUNGRY && !stabilized) {
            tickHungry(level);
        }

        if (type == AuraNodeType.DARK && !stabilized) {
            tickDark(level);
        }

        if (type == AuraNodeType.UNSTABLE) {
            tickUnstable(level, stabilized);
        }
    }

    private void tickTainted(Level level) {
        if (level.getGameTime() % 160L == 0L) {
            BlockPos target = worldPosition.offset(level.random.nextInt(9) - 4, -1, level.random.nextInt(9) - 4);
            if (!level.isOutsideBuildHeight(target) && !level.getBlockState(target).isAir() && !level.getBlockState(target).is(Blocks.BEDROCK)) {
                level.setBlock(target, ThaumcraftMod.TAINT_SOIL.get().defaultBlockState(), 3);
            }
        }
    }

    private void tickPure(Level level) {
        if (level.getGameTime() % 120L == 0L) {
            BlockPos target = worldPosition.offset(level.random.nextInt(9) - 4, -1, level.random.nextInt(9) - 4);
            if (!level.isOutsideBuildHeight(target) && level.getBlockState(target).is(ThaumcraftMod.TAINTED_SOIL.get())) {
                level.setBlock(target, Blocks.DIRT.defaultBlockState(), 3);
            }
        }

        if (level.getGameTime() % 80L == 0L) {
            for (LivingEntity living : level.getEntitiesOfClass(LivingEntity.class, new AABB(worldPosition).inflate(5.5D), LivingEntity::isAlive)) {
                living.removeEffect(MobEffects.POISON);
                living.removeEffect(MobEffects.WITHER);
                living.removeEffect(MobEffects.CONFUSION);
            }
        }
    }

    private void tickHungry(Level level) {
        if (hungerCooldown > 0) {
            hungerCooldown--;
        }
        if (level.getGameTime() % 10L == 0L) {
            AABB area = new AABB(worldPosition).inflate(7.0D);
            List<ItemEntity> items = level.getEntitiesOfClass(ItemEntity.class, area, item -> item.isAlive() && !item.getItem().isEmpty());

            for (ItemEntity item : items) {
                double dx = worldPosition.getX() + 0.5D - item.getX();
                double dy = worldPosition.getY() + 0.5D - item.getY();
                double dz = worldPosition.getZ() + 0.5D - item.getZ();
                item.setDeltaMovement(item.getDeltaMovement().add(dx * 0.026D, dy * 0.022D, dz * 0.026D));
                if (item.distanceToSqr(worldPosition.getX() + 0.5D, worldPosition.getY() + 0.5D, worldPosition.getZ() + 0.5D) < 1.35D && hungerCooldown == 0) {
                    AspectList food = AspectDatabase.getAspectsForItem(item.getItem());
                    for (var entry : food.entries().entrySet()) {
                        aspects.add(entry.getKey(), Math.min(3, Math.max(1, entry.getValue())));
                    }
                    item.getItem().shrink(1);
                    if (item.getItem().isEmpty()) {
                        item.discard();
                    }
                    hungerCooldown = 20;
                    setChangedAndSync();
                    break;
                }
            }

            for (LivingEntity living : level.getEntitiesOfClass(LivingEntity.class, area, LivingEntity::isAlive)) {
                double dx = worldPosition.getX() + 0.5D - living.getX();
                double dy = worldPosition.getY() + 0.5D - living.getY();
                double dz = worldPosition.getZ() + 0.5D - living.getZ();
                living.setDeltaMovement(living.getDeltaMovement().add(dx * 0.010D, dy * 0.006D, dz * 0.010D));
                if (level.getGameTime() % 40L == 0L && living.distanceToSqr(worldPosition.getX() + 0.5D, worldPosition.getY() + 0.5D, worldPosition.getZ() + 0.5D) < 3.0D) {
                    living.addEffect(new MobEffectInstance(MobEffects.HUNGER, 80, 0));
                }
            }
        }
    }

    private void tickDark(Level level) {
        if (level.getGameTime() % 80L == 0L) {
            AABB area = new AABB(worldPosition).inflate(5.0D);
            for (LivingEntity living : level.getEntitiesOfClass(LivingEntity.class, area, LivingEntity::isAlive)) {
                living.addEffect(new MobEffectInstance(MobEffects.DARKNESS, 120, 0));
                living.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 120, 0));
            }
        }
    }

    private void tickUnstable(Level level, boolean stabilized) {
        long interval = stabilized ? 260L : 100L;
        if (level.getGameTime() % interval != 0L) {
            return;
        }
        Aspect from = null;
        for (Aspect aspect : PRIMARY) {
            if (aspects.get(aspect) > 1) {
                from = aspect;
                break;
            }
        }
        Aspect to = PRIMARY[Math.floorMod(worldPosition.getX() + worldPosition.getZ() + (int) level.getGameTime(), PRIMARY.length)];
        if (from != null && to != from && aspects.remove(from, 1)) {
            aspects.add(to, 1);
            if (!stabilized) {
                stability = Math.max(0, stability - 1);
            } else {
                stability = Math.min(100, stability + 1);
            }
            setChangedAndSync();
        }
    }

    public void setChangedAndSync() {
        setChanged();

        if (level != null) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
        }
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, AuraNodeBlockEntity node) {
        if (!node.initialized()) {
            node.initializeFromPosition();
        }

        if (level.getGameTime() % 320L == 0L) {
            node.regenerateSlowly();
        }

        node.tickNodeEffect(level);
        node.tickEnergizedState(level);
        pulseNode(node);
    }

    public void tickEnergizedState(Level level) {
        boolean transduced = hasActiveTransducer();
        if (transduced && isStabilized()) {
            energizedTicks = Math.min(400, energizedTicks + 4);
            if (energizedTicks >= 80 && !energized) {
                energized = true;
                stability = Math.min(100, stability + (stabilizerStrength() >= 2 ? 10 : 4));
                setChangedAndSync();
            }
        } else {
            if (energizedTicks > 0) {
                energizedTicks = Math.max(0, energizedTicks - 1);
            }
            if (energized && energizedTicks <= 0) {
                energized = false;
                setChangedAndSync();
            }
        }

        if (energized && level.getGameTime() % 100L == 0L) {
            for (Aspect aspect : PRIMARY) {
                int base = Math.max(1, baseAspects.get(aspect));
                if (aspects.get(aspect) < base + 8) {
                    aspects.add(aspect, 1);
                    break;
                }
            }
            stability = Math.min(100, stability + 1);
            setChangedAndSync();
        }
    }

    public static void pulseNode(AuraNodeBlockEntity node) {
        if (node == null || node.level == null || node.level.isClientSide()) {
            return;
        }

        long time = node.level.getGameTime();
        if (time % 200L == 0L) {
            int total = node.aspects.totalAmount();

            if (total <= 0) {
                node.stability = Math.max(0, node.stability - 2);
            } else if (node.typedNodeType() == AuraNodeType.PURE) {
                node.stability = Math.min(100, node.stability + 1);
            } else if (node.typedNodeType() == AuraNodeType.UNSTABLE || node.typedNodeType() == AuraNodeType.HUNGRY) {
                node.stability = Math.max(0, node.stability - (node.isStabilized() ? 0 : 1));
            } else if (node.stabilizerStrength() >= 2) {
                node.stability = Math.min(100, node.stability + 2);
            } else if (node.isStabilized()) {
                node.stability = Math.min(100, node.stability + 1);
            }

            if (node.level instanceof ServerLevel serverLevel && node.typedNodeType() == AuraNodeType.UNSTABLE && node.stability < 35 && !node.isStabilized()) {
                serverLevel.sendParticles(net.minecraft.core.particles.ParticleTypes.WITCH,
                        node.worldPosition.getX() + 0.5D, node.worldPosition.getY() + 0.5D, node.worldPosition.getZ() + 0.5D,
                        16, 0.55D, 0.55D, 0.55D, 0.02D);
            }

            node.setChangedAndSync();
        }
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.putBoolean("Initialized", initialized);
        tag.putString("NodeType", nodeType);
        tag.putString("NodeModifier", nodeModifier);
        tag.putInt("Stability", stability);
        tag.putInt("HungerCooldown", hungerCooldown);
        tag.putBoolean("Scanned", scanned);
        tag.putBoolean("Energized", energized);
        tag.putBoolean("Jarred", jarred);
        tag.putInt("EnergizedTicks", energizedTicks);
        tag.put("Aspects", aspects.save());
        tag.put("BaseAspects", baseAspects.save());
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        initialized = tag.getBoolean("Initialized");
        nodeType = tag.contains("NodeType") ? tag.getString("NodeType") : AuraNodeType.NORMAL.name();
        nodeModifier = tag.contains("NodeModifier") ? tag.getString("NodeModifier") : AuraNodeModifier.NORMAL.name();
        stability = tag.contains("Stability") ? tag.getInt("Stability") : 100;
        hungerCooldown = tag.contains("HungerCooldown") ? tag.getInt("HungerCooldown") : 0;
        scanned = tag.getBoolean("Scanned");
        energized = tag.getBoolean("Energized");
        jarred = tag.getBoolean("Jarred");
        energizedTicks = tag.contains("EnergizedTicks") ? tag.getInt("EnergizedTicks") : (energized ? 100 : 0);
        aspects.clear();
        aspects.load(tag.getCompound("Aspects"));
        baseAspects.clear();
        if (tag.contains("BaseAspects")) {
            baseAspects.load(tag.getCompound("BaseAspects"));
        } else {
            baseAspects.addAll(aspects);
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
}
