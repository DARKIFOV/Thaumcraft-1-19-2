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
import com.darkifov.thaumcraft.entity.AspectOrbEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;

import java.util.List;
import java.util.UUID;

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
    /** TC4 TileNodeEnergized.visBase: per-tick centivis capacity after primal reduction. */
    private final AspectList energizedVisBase = new AspectList();
    /** TC4 TileNodeEnergized.vis: transient pool refilled once per server game tick. */
    private final AspectList energizedVis = new AspectList();
    private boolean initialized = false;
    private String nodeId = "";
    private String nodeType = AuraNodeType.NORMAL.name();
    private String nodeModifier = AuraNodeModifier.NORMAL.name();
    private int stability = 100;
    private boolean scanned;
    private boolean energized;
    private boolean jarred;
    private int energizedTicks;
    private int hungerCooldown;
    private int lastDrainColor = 0xFFFFFF;
    private long lastDrainGameTime = Long.MIN_VALUE / 4L;
    private int lastDrainerEntityId = -1;
    private long lastActiveMillis = System.currentTimeMillis();
    private boolean catchUpPending;
    /** Per-node scheduler matching TileNode.count instead of world-global time. */
    private long nodeTick;
    private long energizedVisGameTime = Long.MIN_VALUE;

    public AuraNodeBlockEntity(BlockPos pos, BlockState state) {
        super(ThaumcraftMod.AURA_NODE_BLOCK_ENTITY.get(), pos, state);
    }

    public AspectList aspects() {
        return aspects;
    }

    public AspectList baseAspects() {
        return baseAspects;
    }

    public AspectList energizedVisBase() {
        return energizedVisBase;
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

    public String nodeId() {
        ensureNodeId();
        return nodeId;
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
        ensureNodeId();
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
        energizedVisBase.clear();
        energizedVis.clear();
        energizedVisGameTime = Long.MIN_VALUE;
        lastActiveMillis = System.currentTimeMillis();
        catchUpPending = false;
        nodeTick = 0L;
        initialized = true;
        setChangedAndSync();
    }

    public void initializeFromJarTag(CompoundTag nodeTag) {
        if (nodeTag == null || nodeTag.isEmpty()) {
            initializeFromPosition();
            return;
        }
        nodeId = nodeTag.contains("NodeId") ? nodeTag.getString("NodeId") : "";
        ensureNodeId();
        nodeType = nodeTag.contains("NodeType") ? nodeTag.getString("NodeType") : AuraNodeType.NORMAL.name();
        nodeModifier = nodeTag.contains("NodeModifier") ? nodeTag.getString("NodeModifier") : AuraNodeModifier.PALE.name();
        stability = nodeTag.contains("Stability") ? nodeTag.getInt("Stability") : 45;
        scanned = nodeTag.getBoolean("Scanned");
        energized = false;
        energizedVisBase.clear();
        energizedVis.clear();
        energizedVisGameTime = Long.MIN_VALUE;
        // TileJarNode releases a normal world node. Keeping this true made the
        // released node invisible to recharge pedestals and other live-node systems.
        jarred = false;
        energizedTicks = 0;
        aspects.clear();
        aspects.load(nodeTag.getCompound("Aspects"));
        baseAspects.clear();
        if (nodeTag.contains("BaseAspects")) {
            baseAspects.load(nodeTag.getCompound("BaseAspects"));
        } else {
            baseAspects.addAll(aspects);
        }
        lastActiveMillis = System.currentTimeMillis();
        catchUpPending = false;
        nodeTick = 0L;
        initialized = true;
        setChangedAndSync();
    }

    public CompoundTag saveNodeJarTag() {
        CompoundTag tag = new CompoundTag();
        tag.putString("NodeId", nodeId());
        tag.putString("NodeType", nodeType);
        tag.putString("NodeModifier", nodeModifier);
        tag.putInt("Stability", stability);
        tag.putBoolean("Scanned", scanned);
        tag.putBoolean("Energized", energized);
        tag.putBoolean("Jarred", jarred);
        tag.put("Aspects", aspects.save());
        tag.put("BaseAspects", baseAspects.save());
        tag.put("EnergizedVisBase", energizedVisBase.save());
        return tag;
    }

    private void ensureNodeId() {
        if (nodeId == null || nodeId.isBlank()) {
            nodeId = UUID.randomUUID().toString();
        }
    }

    /** Removes any stored aspect for the focused/unfocused TC4 wand recharge pedestal. */
    public int drainForPedestal(Aspect aspect, int amount) {
        if (aspect == null || amount <= 0 || jarred) return 0;
        int removed = aspects.removeUpTo(aspect, amount);
        if (removed > 0) setChangedAndSync();
        return removed;
    }

    public int drainToWand(Aspect aspect, int amount) {
        if (aspect == null || !aspect.isPrimal() || amount <= 0) {
            return 0;
        }

        // Original TileNode.takeFromContainer removes exactly the amount accepted by the wand.
        // Bright nodes regenerate faster; they do not multiply a single tap or damage an invented stability meter.
        int removed = aspects.removeUpTo(aspect, amount);
        if (removed > 0) {
            setChangedAndSync();
        }
        return removed;
    }

    /**
     * Exact TileNodeEnergized.consumeVis adapter. Values are centivis-sized units,
     * are replenished from visBase once per server tick, and never mutate the
     * permanent aura/base-aspect profile of the node.
     */
    public int consumeEnergizedVis(Aspect aspect, int requestedCentivis) {
        if (!energized || aspect == null || !aspect.isPrimal() || requestedCentivis <= 0 || level == null) {
            return 0;
        }
        refreshEnergizedVisForTick();
        return energizedVis.removeUpTo(aspect, requestedCentivis);
    }

    private void refreshEnergizedVisForTick() {
        if (level == null || !energized) return;
        long gameTime = level.getGameTime();
        if (energizedVisGameTime == gameTime) return;
        if (energizedVisBase.isEmpty()) {
            rebuildEnergizedVisBase(false);
        }
        energizedVis.clear();
        energizedVis.addAll(energizedVisBase);
        energizedVisGameTime = gameTime;
    }

    private void rebuildEnergizedVisBase(boolean synchronize) {
        energizedVisBase.clear();
        AspectList reduced = new AspectList();
        for (java.util.Map.Entry<Aspect, Integer> entry : baseAspects.entries().entrySet()) {
            reduceToPrimals(entry.getKey(), entry.getValue(), reduced);
        }
        float scale = switch (typedNodeModifier()) {
            case BRIGHT -> 1.20F;
            case PALE -> 0.80F;
            case FADING -> 0.50F;
            default -> 1.0F;
        };
        for (Aspect aspect : PRIMARY) {
            int amount = (int)Math.floor(Math.sqrt(Math.max(0, (int)(reduced.get(aspect) * scale))));
            if (typedNodeType() == AuraNodeType.UNSTABLE && level != null) {
                amount += level.getRandom().nextInt(5) - 2;
            }
            if (amount >= 1) energizedVisBase.add(aspect, amount);
        }
        energizedVis.clear();
        energizedVisGameTime = Long.MIN_VALUE;
        if (synchronize) setChangedAndSync();
    }

    private static void reduceToPrimals(Aspect aspect, int amount, AspectList out) {
        if (aspect == null || amount <= 0) return;
        if (aspect.isPrimal()) {
            out.add(aspect, amount);
            return;
        }
        reduceToPrimals(aspect.firstComponent(), amount, out);
        reduceToPrimals(aspect.secondComponent(), amount, out);
    }

    /**
     * Exact server-side aspect/modifier mutation performed by TC4's
     * ItemEldritchObject metadata 3 (Primordial Pearl).
     */
    public void applyPrimordialPearl(boolean researched, net.minecraft.util.RandomSource random) {
        if (random == null) {
            return;
        }
        for (Aspect aspect : new java.util.ArrayList<>(aspects.entries().keySet())) {
            int amount = baseAspects.get(aspect);
            if (!aspect.isPrimal()) {
                if (random.nextBoolean()) {
                    setAspectAmount(baseAspects, aspect, amount - 1);
                }
            } else {
                setAspectAmount(baseAspects, aspect,
                        amount - 2 + random.nextInt(researched ? 9 : 6));
            }
        }

        for (Aspect aspect : PRIMARY) {
            int amount = baseAspects.get(aspect);
            int rolled = random.nextInt(researched ? 4 : 3);
            if (rolled > 0 && rolled > amount) {
                setAspectAmount(baseAspects, aspect, rolled);
                aspects.add(aspect, 1);
            }
        }

        AuraNodeModifier modifier = typedNodeModifier();
        if (modifier == AuraNodeModifier.FADING && random.nextBoolean()) {
            nodeModifier = AuraNodeModifier.PALE.name();
        } else if (modifier == AuraNodeModifier.PALE && random.nextBoolean()) {
            nodeModifier = AuraNodeModifier.NORMAL.name();
        } else if (modifier == AuraNodeModifier.NORMAL && random.nextInt(5) == 0) {
            nodeModifier = AuraNodeModifier.BRIGHT.name();
        }
        setChangedAndSync();
    }

    private static void setAspectAmount(AspectList list, Aspect aspect, int amount) {
        int current = list.get(aspect);
        if (current > 0) {
            list.remove(aspect, current);
        }
        if (amount > 0) {
            list.add(aspect, amount);
        }
    }

    public void markWandDrain(Aspect aspect, Player drainer) {
        if (aspect == null) {
            return;
        }
        lastDrainColor = aspect.nativeColor();
        lastDrainGameTime = level == null ? 0L : level.getGameTime();
        lastDrainerEntityId = drainer == null ? -1 : drainer.getId();
        setChangedAndSync();
    }

    /**
     * Original TileNode clears drainEntity/drainCollision whenever the current
     * five-tick tap fails, the player looks away, or wand use stops. Keeping a
     * stale entity id for a fixed grace window made the rebuilt beam linger
     * after release and made a full wand look as if it was still draining.
     */
    public void clearWandDrain(Player drainer) {
        if (drainer != null && lastDrainerEntityId >= 0 && lastDrainerEntityId != drainer.getId()) {
            return;
        }
        if (lastDrainerEntityId < 0 && lastDrainGameTime <= Long.MIN_VALUE / 8L) {
            return;
        }
        lastDrainerEntityId = -1;
        lastDrainGameTime = Long.MIN_VALUE / 4L;
        setChangedAndSync();
    }

    public int lastDrainColor() {
        return lastDrainColor;
    }

    public int lastDrainerEntityId() {
        return lastDrainerEntityId;
    }

    public boolean isRecentlyDrained() {
        return level != null && level.getGameTime() - lastDrainGameTime <= 10L;
    }

    private int regenerationInterval() {
        int interval = switch (typedNodeModifier()) {
            case BRIGHT -> 400;
            case PALE -> 900;
            case FADING -> 0;
            default -> 600;
        };

        // TC4 node stabilizers protect nodes at the cost of slower natural recharge.
        int stabilizer = stabilizerStrength();
        if (interval > 0 && stabilizer >= 2) {
            interval *= 20;
        } else if (interval > 0 && stabilizer == 1) {
            interval *= 2;
        }
        return interval;
    }

    public void regenerateSlowly() {
        if (level == null) {
            return;
        }
        if (baseAspects.isEmpty()) {
            baseAspects.addAll(aspects);
        }

        if (regenerateOneMissingAspect()) {
            setChangedAndSync();
        }
        lastActiveMillis = System.currentTimeMillis();
    }

    private boolean regenerateOneMissingAspect() {
        if (level == null) {
            return false;
        }
        List<Aspect> candidates = new java.util.ArrayList<>();
        for (var entry : baseAspects.entries().entrySet()) {
            if (entry.getValue() > 0 && aspects.get(entry.getKey()) < entry.getValue()) {
                candidates.add(entry.getKey());
            }
        }
        if (candidates.isEmpty()) {
            return false;
        }
        Aspect aspect = candidates.get(level.random.nextInt(candidates.size()));
        aspects.add(aspect, 1);
        return true;
    }

    /**
     * TC4 catch-up recharge: after a world was closed, one missing aspect may be
     * restored per regeneration*75 ms, capped by the base vis size.
     */
    private void applyCatchUpRecharge(int regeneration) {
        if (!catchUpPending || level == null || level.isClientSide()) {
            return;
        }
        catchUpPending = false;
        long now = System.currentTimeMillis();
        if (regeneration <= 0 || lastActiveMillis <= 0L || now <= lastActiveMillis) {
            lastActiveMillis = now;
            return;
        }
        long intervalMillis = Math.max(1L, regeneration * 75L);
        int restores = (int) Math.min(baseAspects.totalAmount(), (now - lastActiveMillis) / intervalMillis);
        boolean changed = false;
        for (int index = 0; index < restores; index++) {
            if (!regenerateOneMissingAspect()) {
                break;
            }
            changed = true;
        }
        lastActiveMillis = now;
        if (changed) {
            setChangedAndSync();
        }
    }

    private void handleDepletedBaseAspect() {
        if (level == null || level.isClientSide()) {
            return;
        }

        // Original AspectList keeps zero-valued entries; this port removes them.
        // Iterating the base list therefore preserves the same depleted-aspect test.
        for (Aspect aspect : new java.util.ArrayList<>(baseAspects.entries().keySet())) {
            int base = baseAspects.get(aspect);
            if (base <= 0 || aspects.get(aspect) > 0) {
                continue;
            }

            baseAspects.remove(aspect, 1);
            int remainingBase = baseAspects.get(aspect);
            if (level.random.nextInt(20) == 0 || remainingBase <= 0) {
                // TileNode.handleRecharge uses separate if statements, allowing a
                // freshly pale node to become fading in the same rare decay event.
                if (level.random.nextInt(5) == 0) {
                    AuraNodeModifier modifier = typedNodeModifier();
                    if (modifier == AuraNodeModifier.BRIGHT) {
                        nodeModifier = AuraNodeModifier.NORMAL.name();
                    } else if (modifier == AuraNodeModifier.NORMAL) {
                        nodeModifier = AuraNodeModifier.PALE.name();
                    }
                    if (typedNodeModifier() == AuraNodeModifier.PALE && level.random.nextInt(5) == 0) {
                        nodeModifier = AuraNodeModifier.FADING.name();
                    }
                }

                if (aspects.isEmpty()) {
                    level.removeBlock(worldPosition, false);
                    return;
                }
            }

            setChangedAndSync();
            return;
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

        tickNodeStability(level);
    }

    private void tickTainted(Level level) {
        if (nodeTick % 160L == 0L) {
            BlockPos target = worldPosition.offset(level.random.nextInt(9) - 4, -1, level.random.nextInt(9) - 4);
            if (!level.isOutsideBuildHeight(target) && !level.getBlockState(target).isAir() && !level.getBlockState(target).is(Blocks.BEDROCK)) {
                level.setBlock(target, ThaumcraftMod.TAINT_SOIL.get().defaultBlockState(), 3);
            }
        }
    }

    private void tickPure(Level level) {
        if (nodeTick % 120L == 0L) {
            BlockPos target = worldPosition.offset(level.random.nextInt(9) - 4, -1, level.random.nextInt(9) - 4);
            if (!level.isOutsideBuildHeight(target) && level.getBlockState(target).is(ThaumcraftMod.TAINTED_SOIL.get())) {
                level.setBlock(target, Blocks.DIRT.defaultBlockState(), 3);
            }
        }

        if (nodeTick % 80L == 0L) {
            for (LivingEntity living : level.getEntitiesOfClass(LivingEntity.class, new AABB(worldPosition).inflate(5.5D), LivingEntity::isAlive)) {
                living.removeEffect(MobEffects.POISON);
                living.removeEffect(ThaumcraftMod.TAINT_POISON.get());
                living.removeEffect(MobEffects.WITHER);
                living.removeEffect(MobEffects.CONFUSION);
            }
        }
    }

    private void tickHungry(Level level) {
        if (hungerCooldown > 0) {
            hungerCooldown--;
        }
        if (nodeTick % 10L == 0L) {
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
                if (nodeTick % 40L == 0L && living.distanceToSqr(worldPosition.getX() + 0.5D, worldPosition.getY() + 0.5D, worldPosition.getZ() + 0.5D) < 3.0D) {
                    living.addEffect(new MobEffectInstance(MobEffects.HUNGER, 80, 0));
                }
            }
        }
    }

    private void tickDark(Level level) {
        if (nodeTick % 80L == 0L) {
            AABB area = new AABB(worldPosition).inflate(5.0D);
            for (LivingEntity living : level.getEntitiesOfClass(LivingEntity.class, area, LivingEntity::isAlive)) {
                living.addEffect(new MobEffectInstance(MobEffects.DARKNESS, 120, 0));
                living.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 120, 0));
            }
        }
    }

    /**
     * Original TileNode.handleNodeStability parity.
     * Unstable nodes eject one random primal aspect as a collectible orb rather
     * than converting it into another aspect. Stabilizers can very rarely calm
     * unstable nodes and can repair fading modifiers back to pale.
     */
    private void tickNodeStability(Level level) {
        if (nodeTick % 100L != 0L || level.isClientSide()) {
            return;
        }

        int lock = stabilizerStrength();
        boolean changed = false;
        if (typedNodeType() == AuraNodeType.UNSTABLE && level.random.nextBoolean()) {
            if (lock == 0) {
                List<Aspect> available = new java.util.ArrayList<>();
                for (Aspect aspect : PRIMARY) {
                    if (aspects.get(aspect) > 0) {
                        available.add(aspect);
                    }
                }
                if (!available.isEmpty()) {
                    Aspect emitted = available.get(level.random.nextInt(available.size()));
                    if (aspects.remove(emitted, 1) && level instanceof ServerLevel serverLevel) {
                        AspectOrbEntity orb = new AspectOrbEntity(
                                ThaumcraftMod.ASPECT_ORB.get(), serverLevel,
                                worldPosition.getX() + 0.5D,
                                worldPosition.getY() + 0.5D,
                                worldPosition.getZ() + 0.5D,
                                emitted, 1);
                        serverLevel.addFreshEntity(orb);
                        changed = true;
                    }
                }
            } else if (level.random.nextInt(Math.max(1, 10_000 / lock)) == 42) {
                nodeType = AuraNodeType.NORMAL.name();
                changed = true;
            }
        }

        if (typedNodeModifier() == AuraNodeModifier.FADING && lock > 0
                && level.random.nextInt(Math.max(1, 12_500 / lock)) == 69) {
            nodeModifier = AuraNodeModifier.PALE.name();
            changed = true;
        }

        if (changed) {
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

        // TileNode increments its own count before running node handlers.
        // A world-global modulo synchronized every node and changed the first
        // recharge delay after placement/chunk load.
        node.nodeTick++;
        int regeneration = node.regenerationInterval();
        node.applyCatchUpRecharge(regeneration);
        if (regeneration > 0 && node.nodeTick % regeneration == 0L) {
            node.regenerateSlowly();
        }
        if (node.nodeTick % 1200L == 0L) {
            node.handleDepletedBaseAspect();
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
                rebuildEnergizedVisBase(false);
                stability = Math.min(100, stability + (stabilizerStrength() >= 2 ? 10 : 4));
                setChangedAndSync();
            }
        } else {
            if (energizedTicks > 0) {
                energizedTicks = Math.max(0, energizedTicks - 1);
            }
            if (energized && energizedTicks <= 0) {
                energized = false;
                energizedVis.clear();
                energizedVisGameTime = Long.MIN_VALUE;
                setChangedAndSync();
            }
        }

        if (energized && typedNodeType() == AuraNodeType.UNSTABLE
                && !level.isClientSide() && level.getRandom().nextInt(500) == 1) {
            // TC4 periodically rerolled an unstable energized node's sqrt pool.
            rebuildEnergizedVisBase(true);
        }
    }

    public static void pulseNode(AuraNodeBlockEntity node) {
        if (node == null || node.level == null || node.level.isClientSide()) {
            return;
        }

        if (node.nodeTick % 200L == 0L) {
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
        tag.putString("NodeId", nodeId());
        tag.putString("NodeType", nodeType);
        tag.putString("NodeModifier", nodeModifier);
        tag.putInt("Stability", stability);
        tag.putInt("HungerCooldown", hungerCooldown);
        tag.putBoolean("Scanned", scanned);
        tag.putBoolean("Energized", energized);
        tag.putBoolean("Jarred", jarred);
        tag.putInt("EnergizedTicks", energizedTicks);
        tag.putInt("LastDrainColor", lastDrainColor);
        tag.putLong("LastDrainGameTime", lastDrainGameTime);
        tag.putInt("LastDrainerEntityId", lastDrainerEntityId);
        tag.putLong("LastActiveMillis", lastActiveMillis);
        tag.put("Aspects", aspects.save());
        tag.put("BaseAspects", baseAspects.save());
        tag.put("EnergizedVisBase", energizedVisBase.save());
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        initialized = tag.getBoolean("Initialized");
        nodeId = tag.contains("NodeId") ? tag.getString("NodeId") : "";
        ensureNodeId();
        nodeType = tag.contains("NodeType") ? tag.getString("NodeType") : AuraNodeType.NORMAL.name();
        nodeModifier = tag.contains("NodeModifier") ? tag.getString("NodeModifier") : AuraNodeModifier.NORMAL.name();
        stability = tag.contains("Stability") ? tag.getInt("Stability") : 100;
        hungerCooldown = tag.contains("HungerCooldown") ? tag.getInt("HungerCooldown") : 0;
        scanned = tag.getBoolean("Scanned");
        energized = tag.getBoolean("Energized");
        jarred = tag.getBoolean("Jarred");
        energizedTicks = tag.contains("EnergizedTicks") ? tag.getInt("EnergizedTicks") : (energized ? 100 : 0);
        lastDrainColor = tag.contains("LastDrainColor") ? tag.getInt("LastDrainColor") : 0xFFFFFF;
        lastDrainGameTime = tag.contains("LastDrainGameTime") ? tag.getLong("LastDrainGameTime") : Long.MIN_VALUE / 4L;
        lastDrainerEntityId = tag.contains("LastDrainerEntityId") ? tag.getInt("LastDrainerEntityId") : -1;
        lastActiveMillis = tag.contains("LastActiveMillis") ? tag.getLong("LastActiveMillis") : System.currentTimeMillis();
        catchUpPending = initialized;
        aspects.clear();
        aspects.load(tag.getCompound("Aspects"));
        baseAspects.clear();
        if (tag.contains("BaseAspects")) {
            baseAspects.load(tag.getCompound("BaseAspects"));
        } else {
            baseAspects.addAll(aspects);
        }
        energizedVisBase.clear();
        if (tag.contains("EnergizedVisBase")) {
            energizedVisBase.load(tag.getCompound("EnergizedVisBase"));
        }
        energizedVis.clear();
        energizedVisGameTime = Long.MIN_VALUE;
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
