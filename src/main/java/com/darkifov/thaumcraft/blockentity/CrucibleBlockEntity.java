package com.darkifov.thaumcraft.blockentity;

import com.darkifov.thaumcraft.Aspect;
import com.darkifov.thaumcraft.AspectColor;
import com.darkifov.thaumcraft.AspectDatabase;
import com.darkifov.thaumcraft.AspectList;
import com.darkifov.thaumcraft.ThaumcraftMod;
import com.darkifov.thaumcraft.entity.SpecialItemEntity;
import com.darkifov.thaumcraft.alchemy.AlchemyRecipe;
import com.darkifov.thaumcraft.alchemy.AlchemyRecipes;
import com.darkifov.thaumcraft.block.BellowsBlock;
import com.darkifov.thaumcraft.block.TC4ArcaneBellowsParity;
import com.darkifov.thaumcraft.porting.TC4Sounds;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.Containers;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CampfireBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Stage125: TC4-like crucible heat, boil and flux state for Forge 1.19.2.
 */
public class CrucibleBlockEntity extends BlockEntity {
    public static final int MAX_WATER = 1000;
    public static final int BOIL_TEMPERATURE = 151;
    public static final int TC4_BOIL_TEMPERATURE = 150;
    public static final int MAX_TEMPERATURE = 200;
    public static final int FLUX_OVERLOAD_THRESHOLD = 64;
    public static final int WATER_PER_DISSOLVE = 25;
    public static final int WATER_PER_CRAFT = 50;
    public static final int EVAPORATE_INTERVAL_TICKS = 400;

    private final AspectList aspects = new AspectList();
    private int waterLevel = 0;
    private int temperature = 0;
    private int boilTicks = 0;
    private int flux = 0;
    private int bellowsCount = 0;
    private boolean lastHeated = false;

    public CrucibleBlockEntity(BlockPos pos, BlockState state) {
        super(ThaumcraftMod.CRUCIBLE_BLOCK_ENTITY.get(), pos, state);
    }

    public AspectList aspects() { return aspects; }
    public boolean hasWater() { return waterLevel > 0; }
    public int waterLevel() { return waterLevel; }
    public int temperature() { return temperature; }
    public int boilTicks() { return boilTicks; }
    public boolean lastHeated() { return lastHeated; }
    public int bellowsCount() { return bellowsCount; }
    public boolean isBoiling() { return hasWater() && temperature >= BOIL_TEMPERATURE; }

    /**
     * Stage127 renderer helper based on TC4 TileCrucible#getFluidHeight():
     * base = 0.3 + 0.5 * water ratio, then essentia overload raises the surface.
     */
    public float fluidHeight() {
        if (!hasWater()) {
            return 0.0F;
        }
        float waterRatio = Math.max(0.0F, Math.min(1.0F, waterLevel / (float) MAX_WATER));
        float base = 0.30F + 0.50F * waterRatio;
        float out = base + Math.min(1.0F, aspects.totalAmount() / 100.0F) * (1.0F - base);
        if (out > 1.0F) {
            return 1.001F;
        }
        if (Math.abs(out - 1.0F) < 0.0001F) {
            return 0.9999F;
        }
        return out;
    }

    public Aspect dominantAspect() {
        Aspect best = null;
        int bestAmount = 0;
        for (Map.Entry<Aspect, Integer> entry : aspects.entries().entrySet()) {
            if (entry.getValue() > bestAmount) {
                best = entry.getKey();
                bestAmount = entry.getValue();
            }
        }
        return best;
    }

    public int liquidColorArgb(int alpha) {
        Aspect dominant = dominantAspect();
        if (dominant == null || aspects.totalAmount() <= 0) {
            return (Math.max(0, Math.min(255, alpha)) << 24) | 0x3F76E4;
        }
        int tc4AspectTint = blendAspectRgb();
        int amount = Math.min(100, aspects.totalAmount());
        float weight = Math.min(0.75F, 0.25F + amount / 160.0F);
        int water = 0x3F76E4;
        int r = mix((water >> 16) & 255, (tc4AspectTint >> 16) & 255, weight);
        int g = mix((water >> 8) & 255, (tc4AspectTint >> 8) & 255, weight);
        int b = mix(water & 255, tc4AspectTint & 255, weight);
        return (Math.max(0, Math.min(255, alpha)) << 24) | (r << 16) | (g << 8) | b;
    }

    private int mix(int base, int tint, float weight) {
        return Math.max(0, Math.min(255, Math.round(base * (1.0F - weight) + tint * weight)));
    }

    private int blendAspectRgb() {
        int total = Math.max(1, aspects.totalAmount());
        int r = 0;
        int g = 0;
        int b = 0;
        for (Map.Entry<Aspect, Integer> entry : aspects.entries().entrySet()) {
            int rgb = AspectColor.rgb(entry.getKey());
            int amount = Math.max(0, entry.getValue());
            r += ((rgb >> 16) & 255) * amount;
            g += ((rgb >> 8) & 255) * amount;
            b += (rgb & 255) * amount;
        }
        return ((r / total) << 16) | ((g / total) << 8) | (b / total);
    }

    public void setWater(boolean hasWater) { if (hasWater) fillWater(); else drainWater(); }
    public void fillWater() { waterLevel = MAX_WATER; setChangedAndSync(); }
    public void drainWater() { waterLevel = 0; temperature = 0; boilTicks = 0; setChangedAndSync(); }

    public int flux() { return flux; }
    public void addFlux(int amount) { flux = Math.max(0, flux + amount); setChangedAndSync(); }
    public void clearFlux() { flux = 0; setChangedAndSync(); }

    public void addAspects(AspectList added) {
        aspects.addAll(added);
        if (aspects.totalAmount() > FLUX_OVERLOAD_THRESHOLD) {
            addFlux(2 + Math.max(0, aspects.totalAmount() - FLUX_OVERLOAD_THRESHOLD) / 16);
        }
        setChangedAndSync();
    }

    public void clearAspects() { aspects.clear(); setChangedAndSync(); }


    public boolean consumeWater(int amount) {
        if (amount <= 0) {
            return true;
        }
        if (waterLevel < amount) {
            return false;
        }
        waterLevel -= amount;
        if (waterLevel <= 0) {
            waterLevel = 0;
            temperature = 0;
            boilTicks = 0;
        }
        setChangedAndSync();
        return true;
    }

    public boolean tryAcceptThrownItem(ItemEntity itemEntity) {
        if (level == null || level.isClientSide || itemEntity == null || !itemEntity.isAlive()) {
            return false;
        }
        // TC4 explicitly excluded EntitySpecialItem so freshly crafted output
        // cannot be consumed by the crucible again before the player collects it.
        if (itemEntity instanceof SpecialItemEntity) {
            return false;
        }
        if (!hasWater() || !isBoiling()) {
            return false;
        }
        ItemStack stack = itemEntity.getItem();
        if (stack.isEmpty()) {
            return false;
        }

        AlchemyRecipe catalystRecipe = AlchemyRecipes.findByCatalyst(stack);
        if (catalystRecipe != null && catalystRecipe.canCraft(stack, aspects)) {
            ItemStack result = catalystRecipe.craft(stack, aspects);
            if (result.isEmpty()) {
                return false;
            }
            stack.shrink(1);
            consumeWater(WATER_PER_CRAFT);
            updateThrownStack(itemEntity, stack);
            boolean firstOutput = true;
            while (!result.isEmpty()) {
                int amount = Math.min(result.getCount(), result.getMaxStackSize());
                ItemStack spitout = result.copy();
                spitout.setCount(amount);
                result.shrink(amount);
                SpecialItemEntity output = new SpecialItemEntity(level,
                        worldPosition.getX() + 0.5D, worldPosition.getY() + 0.71D, worldPosition.getZ() + 0.5D, spitout);
                output.setDeltaMovement(firstOutput ? 0.0D : (level.random.nextFloat() - level.random.nextFloat()) * 0.01D,
                        0.1D,
                        firstOutput ? 0.0D : (level.random.nextFloat() - level.random.nextFloat()) * 0.01D);
                output.setPickUpDelay(10);
                level.addFreshEntity(output);
                firstOutput = false;
            }
            playCrucibleCraftFx();
            maybeSpillFlux(false);
            setChangedAndSync();
            return true;
        }

        if (catalystRecipe != null) {
            // TC4 keeps a catalyst intact when the crucible lacks the required essentia.
            return false;
        }

        AspectList dissolved = AspectDatabase.getAspectsForItem(stack);
        if (dissolved.isEmpty()) {
            return false;
        }
        stack.shrink(1);
        addAspects(dissolved);
        consumeWater(WATER_PER_DISSOLVE);
        updateThrownStack(itemEntity, stack);
        playCrucibleDissolveFx();
        maybeSpillFlux(false);
        setChangedAndSync();
        return true;
    }

    private void updateThrownStack(ItemEntity entity, ItemStack stack) {
        if (stack.isEmpty()) {
            entity.discard();
        } else {
            entity.setItem(stack);
            entity.setPickUpDelay(20);
        }
    }

    public boolean maybeSpillFlux(boolean force) {
        if (level == null || level.isClientSide) {
            return false;
        }
        if (!force && flux < 18 && aspects.totalAmount() <= FLUX_OVERLOAD_THRESHOLD) {
            return false;
        }
        if (!force && level.random.nextInt(100) >= Math.min(75, flux + Math.max(0, aspects.totalAmount() - FLUX_OVERLOAD_THRESHOLD))) {
            return false;
        }

        for (int i = 0; i < 14; i++) {
            BlockPos target = worldPosition.offset(level.random.nextInt(7) - 3, level.random.nextInt(3) - 1, level.random.nextInt(7) - 3);
            if (level.isEmptyBlock(target)) {
                level.setBlock(target, ThaumcraftMod.FLUX_GAS.get().defaultBlockState(), 3);
                addFlux(-6);
                return true;
            }
            BlockPos above = target.above();
            if (!level.getBlockState(target).isAir() && level.isEmptyBlock(above)) {
                level.setBlock(above, ThaumcraftMod.FLUX_GOO.get().defaultBlockState(), 3);
                addFlux(-8);
                return true;
            }
        }
        return false;
    }

    private void playCrucibleDissolveFx() {
        if (!(level instanceof ServerLevel serverLevel)) {
            return;
        }
        level.playSound(null, worldPosition, TC4Sounds.event("bubble"), SoundSource.BLOCKS, 0.45F, 0.9F + level.random.nextFloat() * 0.25F);
        serverLevel.sendParticles(ParticleTypes.BUBBLE, worldPosition.getX() + 0.5D, worldPosition.getY() + 0.90D, worldPosition.getZ() + 0.5D, 14, 0.25D, 0.05D, 0.25D, 0.02D);
        serverLevel.sendParticles(ParticleTypes.WITCH, worldPosition.getX() + 0.5D, worldPosition.getY() + 0.92D, worldPosition.getZ() + 0.5D, 7, 0.23D, 0.08D, 0.23D, 0.01D);
    }

    private void playCrucibleCraftFx() {
        if (!(level instanceof ServerLevel serverLevel)) {
            return;
        }
        level.playSound(null, worldPosition, TC4Sounds.event("craftstart"), SoundSource.BLOCKS, 0.70F, 0.95F + level.random.nextFloat() * 0.1F);
        serverLevel.sendParticles(ParticleTypes.ENCHANT, worldPosition.getX() + 0.5D, worldPosition.getY() + 0.95D, worldPosition.getZ() + 0.5D, 48, 0.35D, 0.25D, 0.35D, 0.06D);
        serverLevel.sendParticles(ParticleTypes.WITCH, worldPosition.getX() + 0.5D, worldPosition.getY() + 1.0D, worldPosition.getZ() + 0.5D, 12, 0.25D, 0.15D, 0.25D, 0.02D);
    }

    public String statusText() {
        return "Water: " + waterLevel + "/" + MAX_WATER
                + " | Heat: " + temperature + "/" + MAX_TEMPERATURE
                + " | Heated: " + lastHeated
                + " | Boiling: " + isBoiling()
                + " | Flux: " + flux
                + " | Aspects: ";
    }

    private void tickServer() {
        if (level == null || level.isClientSide) return;
        if (level.getGameTime() % 40L == 0L) {
            bellowsCount = countBellows();
        }
        lastHeated = isHeatSource(level, worldPosition.below());
        if (lastHeated && hasWater()) {
            temperature = Math.min(MAX_TEMPERATURE, temperature + TC4ArcaneBellowsParity.crucibleHeatGain(bellowsCount));
        } else {
            temperature = Math.max(0, temperature - 1);
        }

        if (isBoiling()) {
            boilTicks++;
            if (level instanceof ServerLevel serverLevel && boilTicks % 20 == 0) {
                serverLevel.sendParticles(ParticleTypes.BUBBLE, worldPosition.getX() + 0.5D, worldPosition.getY() + 0.92D, worldPosition.getZ() + 0.5D, 8, 0.22D, 0.05D, 0.22D, 0.02D);
                serverLevel.sendParticles(ParticleTypes.CLOUD, worldPosition.getX() + 0.5D, worldPosition.getY() + 1.05D, worldPosition.getZ() + 0.5D, 3, 0.20D, 0.05D, 0.20D, 0.01D);
            }
            if (boilTicks % 60 == 0) {
                level.playSound(null, worldPosition, TC4Sounds.event("bubble"), SoundSource.BLOCKS, 0.35F, 0.9F + level.random.nextFloat() * 0.2F);
            }
            if (boilTicks % EVAPORATE_INTERVAL_TICKS == 0) {
                consumeWater(1);
            }
            if (aspects.totalAmount() > FLUX_OVERLOAD_THRESHOLD && boilTicks % 100 == 0) {
                addFlux(1);
            }
            if (boilTicks % 100 == 0 && aspects.totalAmount() > 0) {
                degradeBoilingAspect();
            }
            if (flux >= 24 && boilTicks % 80 == 0 && level instanceof ServerLevel serverLevel) {
                serverLevel.sendParticles(ParticleTypes.WITCH, worldPosition.getX() + 0.5D, worldPosition.getY() + 1.0D, worldPosition.getZ() + 0.5D, 10, 0.35D, 0.20D, 0.35D, 0.02D);
                level.playSound(null, worldPosition, TC4Sounds.event("spill"), SoundSource.BLOCKS, 0.45F, 0.85F);
                maybeSpillFlux(false);
            }
        } else {
            boilTicks = Math.max(0, boilTicks - 1);
        }

        if (level.getGameTime() % 20L == 0L) setChangedAndSync(); else setChanged();
    }

    private void degradeBoilingAspect() {
        if (level == null || level.isClientSide || !hasWater()) {
            return;
        }
        Aspect aspect = randomStoredAspect();
        if (aspect == null) {
            return;
        }
        consumeWater(2);
        aspects.remove(aspect, 1);
        if (!aspect.isPrimal()) {
            Aspect component = level.random.nextBoolean() ? aspect.firstComponent() : aspect.secondComponent();
            if (component != null) {
                aspects.add(component, 1);
            }
        } else {
            addFlux(1);
            maybeSpillFlux(false);
        }
        if (level instanceof ServerLevel serverLevel) {
            serverLevel.sendParticles(ParticleTypes.WITCH, worldPosition.getX() + 0.5D, worldPosition.getY() + fluidHeight() + 0.08D, worldPosition.getZ() + 0.5D, 4, 0.18D, 0.03D, 0.18D, 0.005D);
            serverLevel.sendParticles(ParticleTypes.SMOKE, worldPosition.getX() + 0.5D, worldPosition.getY() + fluidHeight() + 0.12D, worldPosition.getZ() + 0.5D, 2, 0.16D, 0.04D, 0.16D, 0.002D);
        }
        setChangedAndSync();
    }

    @Nullable
    private Aspect randomStoredAspect() {
        if (aspects.isEmpty()) {
            return null;
        }
        List<Aspect> weighted = new ArrayList<>();
        for (Map.Entry<Aspect, Integer> entry : aspects.entries().entrySet()) {
            for (int i = 0; i < Math.max(1, entry.getValue()); i++) {
                weighted.add(entry.getKey());
            }
        }
        if (weighted.isEmpty() || level == null) {
            return aspects.firstAspect();
        }
        return weighted.get(level.random.nextInt(weighted.size()));
    }


    /**
     * TileCrucible#getBellows deliberately counted every horizontal metadata-0
     * bellows block, without checking its orientation or redstone state.
     */
    public int countBellows() {
        if (level == null) {
            return 0;
        }
        int count = 0;
        for (Direction direction : Direction.Plane.HORIZONTAL) {
            if (level.getBlockState(worldPosition.relative(direction)).getBlock() instanceof BellowsBlock) {
                count++;
            }
        }
        return count;
    }

    /**
     * TC4 TileCrucible#spillRemnants() port: clears water/aspects and spills flux based on leftover essentia.
     * Called by sneak-right-clicking the crucible with a wand.
     */
    public boolean spillRemnants(@Nullable Player player) {
        if (level == null || level.isClientSide) {
            return false;
        }
        int aspectTotal = aspects.totalAmount();
        if (waterLevel <= 0 && aspectTotal <= 0 && flux <= 0) {
            return false;
        }

        int spillAttempts = Math.max(1, aspectTotal / 2 + flux / 8);
        waterLevel = 0;
        temperature = 0;
        boilTicks = 0;
        aspects.clear();
        flux += Math.max(0, aspectTotal / 2);

        for (int i = 0; i < Math.min(24, spillAttempts); i++) {
            maybeSpillFlux(true);
        }

        if (level instanceof ServerLevel serverLevel) {
            serverLevel.sendParticles(ParticleTypes.SPLASH, worldPosition.getX() + 0.5D, worldPosition.getY() + 0.95D, worldPosition.getZ() + 0.5D, 24, 0.32D, 0.12D, 0.32D, 0.04D);
            serverLevel.sendParticles(ParticleTypes.WITCH, worldPosition.getX() + 0.5D, worldPosition.getY() + 1.0D, worldPosition.getZ() + 0.5D, 18, 0.35D, 0.18D, 0.35D, 0.025D);
        }
        level.playSound(null, worldPosition, TC4Sounds.event("spill"), SoundSource.BLOCKS, 0.75F, 0.9F);
        setChangedAndSync();
        return true;
    }

    public static boolean isHeatSource(Level level, BlockPos pos) {
        BlockState below = level.getBlockState(pos);
        Block block = below.getBlock();
        if (block == Blocks.FIRE || block == Blocks.SOUL_FIRE || block == Blocks.LAVA || block == Blocks.MAGMA_BLOCK) return true;
        if (block instanceof CampfireBlock && below.getValue(CampfireBlock.LIT)) return true;
        return below.is(BlockTags.FIRE) || below.is(BlockTags.CAMPFIRES);
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, CrucibleBlockEntity crucible) {
        crucible.tickServer();
    }

    public void setChangedAndSync() {
        setChanged();
        if (level != null && !level.isClientSide) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), Block.UPDATE_CLIENTS);
        }
    }

    @Nullable
    @Override
    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public CompoundTag getUpdateTag() {
        CompoundTag tag = super.getUpdateTag();
        saveAdditional(tag);
        return tag;
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.putBoolean("HasWater", hasWater());
        tag.putInt("WaterLevel", waterLevel);
        tag.putInt("Temperature", temperature);
        tag.putInt("BoilTicks", boilTicks);
        tag.putInt("Flux", flux);
        tag.putInt("Bellows", bellowsCount);
        tag.putBoolean("LastHeated", lastHeated);
        tag.put("Aspects", aspects.save());
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        waterLevel = tag.contains("WaterLevel") ? tag.getInt("WaterLevel") : (tag.getBoolean("HasWater") ? MAX_WATER : 0);
        temperature = tag.getInt("Temperature");
        boilTicks = tag.getInt("BoilTicks");
        flux = tag.getInt("Flux");
        bellowsCount = tag.getInt("Bellows");
        lastHeated = tag.getBoolean("LastHeated");
        if (tag.contains("Aspects")) aspects.load(tag.getCompound("Aspects"));
    }
}
