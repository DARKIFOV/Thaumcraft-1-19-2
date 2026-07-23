package com.darkifov.thaumcraft.blockentity;

import com.darkifov.thaumcraft.Aspect;
import com.darkifov.thaumcraft.ThaumcraftMod;
import com.darkifov.thaumcraft.block.TC4EssentiaLampBlock;
import com.darkifov.thaumcraft.block.TC4GrowthLampParity;
import com.darkifov.thaumcraft.block.TC4FertilityLampParity;
import com.mojang.math.Vector3f;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.TagKey;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.BonemealableBlock;
import net.minecraft.world.level.block.BushBlock;
import net.minecraft.world.level.block.CactusBlock;
import net.minecraft.world.level.block.CocoaBlock;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.NetherWartBlock;
import net.minecraft.world.level.block.StemBlock;
import net.minecraft.world.level.block.SugarCaneBlock;
import net.minecraft.world.level.block.VineBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.core.Registry;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** Direct server-authoritative port of TileArcaneLampGrowth/Fertility. */
public final class TC4EssentiaLampBlockEntity extends BlockEntity {
    public static final int SUCTION = TC4GrowthLampParity.SUCTION;
    public static final int GROWTH_RADIUS = TC4GrowthLampParity.RADIUS;
    public static final int FERTILITY_RADIUS = TC4FertilityLampParity.RADIUS;
    private static final TagKey<Block> GROWTH_BLACKLIST = TagKey.create(Registry.BLOCK_REGISTRY,
            new ResourceLocation(ThaumcraftMod.MOD_ID, "growth_lamp_blacklist"));

    private int charges;
    private boolean reserve;
    private int drawDelay;
    private int fertilityCounter;
    private final List<BlockPos> growthColumns = new ArrayList<>();

    // TC4 keeps these transient and compares the previously selected target each tick.
    private BlockPos lastGrowthTarget = BlockPos.ZERO;
    private BlockState lastGrowthState = net.minecraft.world.level.block.Blocks.AIR.defaultBlockState();
    private Direction loadedFacing = Direction.DOWN;
    private boolean hasLoadedFacing;

    public TC4EssentiaLampBlockEntity(BlockPos pos, BlockState state) {
        super(ThaumcraftMod.TC4_ESSENTIA_LAMP_BLOCK_ENTITY.get(), pos, state);
        charges = kind() == TC4EssentiaLampBlock.Kind.GROWTH ? -1 : 0;
    }

    public TC4EssentiaLampBlock.Kind kind() {
        return getBlockState().getBlock() instanceof TC4EssentiaLampBlock lamp
                ? lamp.kind() : TC4EssentiaLampBlock.Kind.GROWTH;
    }

    public Direction inputFace() {
        return getBlockState().hasProperty(TC4EssentiaLampBlock.FACING)
                ? getBlockState().getValue(TC4EssentiaLampBlock.FACING) : Direction.DOWN;
    }

    public Aspect suctionType(Direction face) {
        if (face != inputFace()) return null;
        return kind() == TC4EssentiaLampBlock.Kind.GROWTH ? Aspect.HERBA : Aspect.VICTUS;
    }

    public int suctionAmount(Direction face) {
        if (kind() == TC4EssentiaLampBlock.Kind.GROWTH) {
            return TC4GrowthLampParity.suction(reserve, charges, face == inputFace());
        }
        return TC4FertilityLampParity.suction(charges, face == inputFace());
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, TC4EssentiaLampBlockEntity lamp) {
        if (!(level instanceof ServerLevel serverLevel)) return;
        if (lamp.kind() == TC4EssentiaLampBlock.Kind.GROWTH) {
            lamp.tickGrowth(serverLevel);
        } else {
            lamp.tickFertility(serverLevel);
        }
    }

    private void tickGrowth(ServerLevel level) {
        if (charges <= 0) {
            if (reserve) {
                charges = TC4GrowthLampParity.CHARGES_PER_ESSENTIA;
                reserve = false;
                updateActiveState();
            } else if (drawEssentia(Aspect.HERBA)) {
                charges = TC4GrowthLampParity.CHARGES_PER_ESSENTIA;
                updateActiveState();
            }
        }

        if (!reserve && drawEssentia(Aspect.HERBA)) {
            reserve = true;
            setChanged();
        }

        if (charges == 0) {
            charges = -1;
            updateActiveState();
        }

        if (charges > 0) {
            updatePlant(level);
        }
    }

    private void tickFertility(ServerLevel level) {
        if (TC4FertilityLampParity.canDraw(charges) && drawEssentia(Aspect.VICTUS)) {
            charges++;
            updateActiveState();
        }
        if (TC4FertilityLampParity.canBreed(charges)
                && TC4FertilityLampParity.isBreedingTick(fertilityCounter++)) {
            breedOnePair(level);
        }
    }

    private boolean drawEssentia(Aspect aspect) {
        if (++drawDelay % TC4GrowthLampParity.DRAW_INTERVAL_TICKS != 0 || level == null) return false;
        Direction support = inputFace();
        BlockEntity neighbour = level.getBlockEntity(worldPosition.relative(support));
        Direction sideFromNeighbour = support.getOpposite();
        int suction = suctionAmount(support);
        if (suction <= 0) return false;

        if (neighbour instanceof EssentiaJarBlockEntity jar
                && sideFromNeighbour == Direction.UP
                && jar.takeFromContainerOriginal(aspect, 1)) {
            return true;
        }
        if (neighbour instanceof EssentiaTubeBlockEntity tube
                && tube.allowsOutputTo(sideFromNeighbour)
                && tube.getSuctionAmount(sideFromNeighbour) < suction
                && tube.takeEssentiaOriginal(aspect, 1, sideFromNeighbour) == 1) {
            return true;
        }
        if (neighbour instanceof AlembicBlockEntity alembic
                && alembic.canOutputTo(sideFromNeighbour)
                && alembic.removeEssentia(aspect, 1) == 1) {
            return true;
        }
        if (neighbour instanceof EssentiaReservoirBlockEntity reservoir
                && reservoir.canAccessFrom(sideFromNeighbour)
                && reservoir.removeEssentia(aspect, 1) == 1) {
            return true;
        }
        if (neighbour instanceof AlchemicalCentrifugeBlockEntity centrifuge
                && centrifuge.canOutputTo(sideFromNeighbour)
                && centrifuge.takeOutput(aspect, 1, sideFromNeighbour) == 1) {
            return true;
        }
        return false;
    }

    private void updatePlant(ServerLevel level) {
        BlockState observed = level.getBlockState(lastGrowthTarget);
        if (!observed.equals(lastGrowthState)) {
            if (level.getNearestPlayer(lastGrowthTarget.getX() + 0.5D, lastGrowthTarget.getY() + 0.5D,
                    lastGrowthTarget.getZ() + 0.5D, TC4GrowthLampParity.SPARKLE_RANGE, false) != null) {
                int color = TC4GrowthLampParity.SPARKLE_COLOR;
                level.sendParticles(new DustParticleOptions(new Vector3f(
                                ((color >> 16) & 255) / 255.0F,
                                ((color >> 8) & 255) / 255.0F,
                                (color & 255) / 255.0F), 0.8F),
                        lastGrowthTarget.getX() + 0.5D, lastGrowthTarget.getY() + 0.5D,
                        lastGrowthTarget.getZ() + 0.5D, 7, 0.3D, 0.3D, 0.3D, 0.01D);
            }
            lastGrowthState = observed;
        }

        if (growthColumns.isEmpty()) {
            refillGrowthColumns(level.getRandom());
        }

        BlockPos top = growthColumns.remove(0);
        BlockPos.MutableBlockPos cursor = top.mutable();
        for (int y = worldPosition.getY() + GROWTH_RADIUS; y >= worldPosition.getY() - GROWTH_RADIUS; y--) {
            cursor.setY(y);
            int dx = cursor.getX() - worldPosition.getX();
            int dy = cursor.getY() - worldPosition.getY();
            int dz = cursor.getZ() - worldPosition.getZ();
            if (!TC4GrowthLampParity.insideSphere(dx, dy, dz)) continue;

            BlockState state = level.getBlockState(cursor);
            if (state.isAir() || !isPlant(state) || isGrownCrop(level, cursor, state) || state.is(GROWTH_BLACKLIST)) {
                continue;
            }

            charges--;
            lastGrowthTarget = cursor.immutable();
            lastGrowthState = state;
            setChanged();

            // 1.7.10 scheduled the target block's updateTick one tick later. Modern crops moved
            // their growth code to randomTick, so invoking one random tick preserves the original
            // single-natural-tick chance instead of the guaranteed bonemeal path used previously.
            state.randomTick(level, cursor, level.getRandom());
            return;
        }
    }

    private void refillGrowthColumns(RandomSource random) {
        for (int dx = -GROWTH_RADIUS; dx <= GROWTH_RADIUS; dx++) {
            for (int dz = -GROWTH_RADIUS; dz <= GROWTH_RADIUS; dz++) {
                growthColumns.add(worldPosition.offset(dx, GROWTH_RADIUS, dz));
            }
        }
        for (int index = growthColumns.size(); index > 1; index--) {
            Collections.swap(growthColumns, index - 1, random.nextInt(index));
        }
    }

    private static boolean isPlant(BlockState state) {
        Block block = state.getBlock();
        return block instanceof BonemealableBlock
                || block instanceof BushBlock
                || block instanceof VineBlock
                || block instanceof CactusBlock
                || block instanceof SugarCaneBlock;
    }

    private static boolean isGrownCrop(ServerLevel level, BlockPos pos, BlockState state) {
        Block block = state.getBlock();
        if (block instanceof BonemealableBlock growable && !(block instanceof StemBlock)
                && !growable.isValidBonemealTarget(level, pos, state, false)) {
            return true;
        }
        if (block instanceof CropBlock crop && crop.isMaxAge(state)) return true;
        // TC4 registered cocoa and nether wart as wildcard standard crops, making every metadata
        // state ineligible for the Growth Lamp even though normal world ticks can still grow them.
        if (block instanceof CocoaBlock || block instanceof NetherWartBlock) return true;
        if ((block instanceof CactusBlock || block instanceof SugarCaneBlock)
                && level.getBlockState(pos.below()).is(block)) {
            return true;
        }
        return block == ThaumcraftMod.TC4_MANA_POD.get()
                && state.hasProperty(com.darkifov.thaumcraft.block.ManaPodBlock.AGE)
                && state.getValue(com.darkifov.thaumcraft.block.ManaPodBlock.AGE) >= 7;
    }

    private void breedOnePair(ServerLevel level) {
        List<Animal> animals = level.getEntitiesOfClass(Animal.class,
                new AABB(worldPosition).inflate(TC4FertilityLampParity.RADIUS));
        for (Animal first : animals) {
            if (!TC4FertilityLampParity.eligibleAnimal(first.getAge(), first.isInLove())) continue;
            List<Animal> sameSpecies = new ArrayList<>();
            for (Animal animal : animals) {
                if (animal.getClass().equals(first.getClass())) sameSpecies.add(animal);
            }
            if (!TC4FertilityLampParity.populationAllowed(sameSpecies.size())) continue;
            Animal partner = null;
            for (Animal candidate : sameSpecies) {
                if (!TC4FertilityLampParity.eligibleAnimal(candidate.getAge(), candidate.isInLove())) continue;
                if (partner == null) {
                    partner = candidate;
                } else {
                    charges -= TC4FertilityLampParity.BREEDING_COST;
                    candidate.setInLove(null);
                    partner.setInLove(null);
                    updateActiveState();
                    return;
                }
            }
        }
    }

    public int charges() {
        return charges;
    }

    public boolean reserve() {
        return reserve;
    }

    private void updateActiveState() {
        setChanged();
        if (level == null) return;
        BlockState state = getBlockState();
        if (!state.hasProperty(TC4EssentiaLampBlock.ACTIVE)) return;
        boolean active = kind() == TC4EssentiaLampBlock.Kind.GROWTH
                ? TC4GrowthLampParity.isActive(charges)
                : TC4FertilityLampParity.isActive(charges);
        if (state.getValue(TC4EssentiaLampBlock.ACTIVE) != active) {
            level.setBlock(worldPosition, state.setValue(TC4EssentiaLampBlock.ACTIVE, active), 3);
        } else if (!level.isClientSide) {
            level.sendBlockUpdated(worldPosition, state, state, 3);
        }
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.putInt(TC4GrowthLampParity.NBT_ORIENTATION, inputFace().get3DDataValue());
        if (kind() == TC4EssentiaLampBlock.Kind.GROWTH) {
            tag.putBoolean(TC4GrowthLampParity.NBT_RESERVE, reserve);
        }
        tag.putInt(TC4GrowthLampParity.NBT_CHARGES, charges);
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        charges = tag.contains(TC4GrowthLampParity.NBT_CHARGES, Tag.TAG_INT) ? tag.getInt(TC4GrowthLampParity.NBT_CHARGES)
                : (kind() == TC4EssentiaLampBlock.Kind.GROWTH ? -1 : 0);
        reserve = kind() == TC4EssentiaLampBlock.Kind.GROWTH
                && tag.getBoolean(TC4GrowthLampParity.NBT_RESERVE);
        if (tag.contains(TC4GrowthLampParity.NBT_ORIENTATION, Tag.TAG_INT)) {
            loadedFacing = Direction.from3DDataValue(tag.getInt(TC4GrowthLampParity.NBT_ORIENTATION));
            hasLoadedFacing = true;
        }
    }

    @Override
    public void onLoad() {
        super.onLoad();
        if (level == null) return;
        BlockState state = getBlockState();
        if (hasLoadedFacing && state.hasProperty(TC4EssentiaLampBlock.FACING)
                && state.getValue(TC4EssentiaLampBlock.FACING) != loadedFacing) {
            state = state.setValue(TC4EssentiaLampBlock.FACING, loadedFacing);
        }
        if (state.hasProperty(TC4EssentiaLampBlock.ACTIVE)) {
            state = state.setValue(TC4EssentiaLampBlock.ACTIVE, kind() == TC4EssentiaLampBlock.Kind.GROWTH
                    ? TC4GrowthLampParity.isActive(charges)
                    : TC4FertilityLampParity.isActive(charges));
        }
        if (!state.equals(getBlockState())) {
            level.setBlock(worldPosition, state, 3);
        }
        hasLoadedFacing = false;
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
        if (packet.getTag() != null) load(packet.getTag());
    }
}
