package com.darkifov.thaumcraft.blockentity;

import com.darkifov.thaumcraft.Aspect;
import com.darkifov.thaumcraft.AspectList;
import com.darkifov.thaumcraft.ThaumcraftMod;
import com.darkifov.thaumcraft.alchemy.AlchemyRecipe;
import com.darkifov.thaumcraft.alchemy.AlchemyRecipes;
import com.darkifov.thaumcraft.block.ThaumatoriumBlock;
import com.darkifov.thaumcraft.essentia.TC4EssentiaNetworkRuntime;
import com.darkifov.thaumcraft.essentia.TC4ItemTransferRuntime;
import com.darkifov.thaumcraft.menu.ThaumatoriumMenu;
import com.darkifov.thaumcraft.porting.TC4Sounds;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.Connection;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.Containers;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

/**
 * Direct TC4 TileThaumatorium transport/crafting adapter.
 *
 * Unlike the old flat-network shortcut, this implementation advertises suction 128,
 * pulls exactly one essentia every five ticks through a directly connected tube,
 * stores recipe essentia internally, obeys heat/redstone, and only completes when
 * the output can be inserted (or safely ejected when no inventory is present).
 */
public class ThaumatoriumBlockEntity extends BlockEntity implements MenuProvider {
    public static final int ORIGINAL_CRAFT_INTERVAL_TICKS = 5;
    public static final int ORIGINAL_SUCTION = 128;
    public static final int ORIGINAL_HEAT_REFRESH_TICKS = 40;

    public static final String NBT_CATALYST = "Catalyst";
    public static final String NBT_PROGRESS = "progress";
    public static final String NBT_LAST_RECIPE = "recipe";
    public static final String NBT_SELECTED_FORMULA = "formula";
    public static final String NBT_MNEMONIC_MATRIX = "brain";

    private final AspectList essentia = new AspectList();
    private ItemStack catalyst = ItemStack.EMPTY;
    private int progress;
    private int counter;
    private String lastRecipe = "";
    private String lastMissing = "";
    private String selectedFormula = "";
    private boolean cachedMnemonicMatrix;
    private boolean heated;
    private Aspect currentSuction;

    public ThaumatoriumBlockEntity(BlockPos pos, BlockState state) {
        super(ThaumcraftMod.THAUMATORIUM_BLOCK_ENTITY.get(), pos, state);
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, ThaumatoriumBlockEntity tile) {
        if (level == null || level.isClientSide) {
            return;
        }
        if (tile.counter == 0 || tile.counter % ORIGINAL_HEAT_REFRESH_TICKS == 0) {
            tile.heated = tile.checkHeat();
            tile.hasMnemonicMatrix();
        }
        tile.counter++;
        tile.progress = tile.counter % ORIGINAL_CRAFT_INTERVAL_TICKS;

        if (tile.catalyst.isEmpty()) {
            tile.currentSuction = null;
            tile.lastMissing = "catalyst";
            return;
        }
        if (!tile.heated) {
            tile.currentSuction = null;
            tile.lastMissing = "heat";
            return;
        }
        if (tile.isPowered()) {
            tile.currentSuction = null;
            tile.lastMissing = "redstone";
            return;
        }
        if (tile.counter % ORIGINAL_CRAFT_INTERVAL_TICKS != 0) {
            return;
        }

        AlchemyRecipe recipe = tile.activeRecipe();
        if (recipe == null) {
            tile.currentSuction = null;
            tile.lastMissing = "formula";
            tile.setChangedAndSync();
            return;
        }

        ItemStack preview = tile.previewResult(recipe);
        if (preview.isEmpty()) {
            tile.currentSuction = null;
            tile.lastMissing = "result";
            tile.setChangedAndSync();
            return;
        }
        if (!tile.outputCanAccept(preview)) {
            tile.currentSuction = null;
            tile.lastMissing = "output";
            tile.setChangedAndSync();
            return;
        }

        tile.currentSuction = tile.firstMissingAspect(recipe);
        if (tile.currentSuction == null) {
            tile.completeRecipe(recipe, preview);
        } else if (!tile.fillOneFromAdjacentTube(tile.currentSuction)) {
            tile.lastMissing = tile.currentSuction.id() + " " + tile.essentia.get(tile.currentSuction)
                    + "/" + recipe.cost().getOrDefault(tile.currentSuction, 0);
            tile.setChangedAndSync();
        }
    }

    public ItemStack catalyst() {
        return catalyst;
    }

    public AspectList essentia() {
        return essentia;
    }

    public int progress() {
        return progress;
    }

    public String lastRecipe() {
        return lastRecipe;
    }

    public String lastMissing() {
        return lastMissing;
    }

    public boolean heated() {
        return heated;
    }

    public Aspect currentSuction() {
        return currentSuction;
    }

    public int storedEssentia(Aspect aspect) {
        return aspect == null ? 0 : essentia.get(aspect);
    }

    public int simulateEssentiaAcceptance(Aspect aspect, int amount) {
        if (aspect == null || amount <= 0 || catalyst.isEmpty() || isPowered() || !heated) {
            return 0;
        }
        AlchemyRecipe recipe = activeRecipe();
        if (recipe == null) {
            return 0;
        }
        int missing = Math.max(0, recipe.cost().getOrDefault(aspect, 0) - essentia.get(aspect));
        return Math.min(amount, missing);
    }

    /** Essentia golems use the same bounded recipe buffer as tube suction. */
    public int acceptEssentiaFromGolem(Aspect aspect, int amount) {
        int accepted = simulateEssentiaAcceptance(aspect, amount);
        if (accepted > 0) {
            essentia.add(aspect, accepted);
            lastMissing = "";
            currentSuction = firstMissingAspect(activeRecipe());
            setChangedAndSync();
        }
        return accepted;
    }

    public Direction facing() {
        BlockState state = getBlockState();
        return state.hasProperty(ThaumatoriumBlock.FACING) ? state.getValue(ThaumatoriumBlock.FACING) : Direction.NORTH;
    }

    public boolean isPowered() {
        return level != null && (level.hasNeighborSignal(worldPosition)
                || level.hasNeighborSignal(worldPosition.below())
                || level.hasNeighborSignal(worldPosition.above()));
    }

    private boolean checkHeat() {
        if (level == null) {
            return false;
        }
        BlockState heat = level.getBlockState(worldPosition.below(2));
        return heat.is(Blocks.FIRE)
                || heat.is(Blocks.SOUL_FIRE)
                || heat.is(ThaumcraftMod.NITOR_LIGHT.get())
                || heat.getFluidState().is(FluidTags.LAVA);
    }

    /** Original alchemical construct gains two extra formula memories per mnemonic matrix. */
    public boolean hasMnemonicMatrix() {
        if (level == null) {
            return cachedMnemonicMatrix;
        }
        cachedMnemonicMatrix = false;
        for (int y = 0; y <= 1 && !cachedMnemonicMatrix; y++) {
            BlockPos layer = worldPosition.above(y);
            for (Direction direction : Direction.values()) {
                if (direction == Direction.DOWN || direction == facing()) {
                    continue;
                }
                if (level.getBlockState(layer.relative(direction)).is(ThaumcraftMod.MNEMONIC_MATRIX.get())) {
                    cachedMnemonicMatrix = true;
                    break;
                }
            }
        }
        return cachedMnemonicMatrix;
    }

    public int maxRecipes() {
        int matrices = 0;
        if (level != null) {
            for (int y = 0; y <= 1; y++) {
                BlockPos layer = worldPosition.above(y);
                for (Direction direction : Direction.values()) {
                    if (direction == Direction.DOWN || direction == facing()) {
                        continue;
                    }
                    if (level.getBlockState(layer.relative(direction)).is(ThaumcraftMod.MNEMONIC_MATRIX.get())) {
                        matrices++;
                    }
                }
            }
        }
        return 1 + matrices * 2;
    }

    public List<AlchemyRecipe> visibleFormulaCandidates() {
        if (catalyst.isEmpty()) {
            return Collections.emptyList();
        }
        List<AlchemyRecipe> recipes = new ArrayList<>();
        for (AlchemyRecipe recipe : AlchemyRecipes.recipes()) {
            if (recipe.catalystMatches(catalyst)) {
                recipes.add(recipe);
            }
        }
        recipes.sort(Comparator.comparing(recipe -> recipe.id().toString()));
        int limit = Math.min(maxRecipes(), recipes.size());
        return Collections.unmodifiableList(new ArrayList<>(recipes.subList(0, limit)));
    }

    public AlchemyRecipe activeRecipe() {
        List<AlchemyRecipe> candidates = visibleFormulaCandidates();
        if (candidates.isEmpty()) {
            return null;
        }
        if (!selectedFormula.isBlank()) {
            for (AlchemyRecipe recipe : candidates) {
                if (recipe.id().toString().equals(selectedFormula) || recipe.tc4Key().equals(selectedFormula)) {
                    return recipe;
                }
            }
        }
        return candidates.get(0);
    }

    public boolean isSelectedFormula(AlchemyRecipe recipe) {
        AlchemyRecipe active = activeRecipe();
        return active != null && recipe != null && active.id().equals(recipe.id());
    }

    public void cycleFormula() {
        List<AlchemyRecipe> candidates = visibleFormulaCandidates();
        if (candidates.isEmpty()) {
            selectedFormula = "";
            currentSuction = null;
            return;
        }
        int index = selectedFormulaIndex();
        selectedFormula = candidates.get((Math.max(0, index) + 1) % candidates.size()).id().toString();
        currentSuction = null;
        setChangedAndSync();
    }

    public boolean selectFormulaIndex(int requestedIndex) {
        List<AlchemyRecipe> candidates = visibleFormulaCandidates();
        if (requestedIndex < 0 || requestedIndex >= candidates.size()) {
            return false;
        }
        selectedFormula = candidates.get(requestedIndex).id().toString();
        currentSuction = null;
        progress = 0;
        lastMissing = "";
        setChangedAndSync();
        return true;
    }

    public int selectedFormulaIndex() {
        List<AlchemyRecipe> candidates = visibleFormulaCandidates();
        AlchemyRecipe active = activeRecipe();
        if (active == null) {
            return -1;
        }
        for (int i = 0; i < candidates.size(); i++) {
            if (active.id().equals(candidates.get(i).id())) {
                return i;
            }
        }
        return -1;
    }

    public boolean insertCatalyst(ItemStack stack) {
        if (stack == null || stack.isEmpty() || !catalyst.isEmpty()) {
            return false;
        }
        List<AlchemyRecipe> recipes = AlchemyRecipes.recipes().stream().filter(r -> r.catalystMatches(stack)).toList();
        if (recipes.isEmpty()) {
            return false;
        }
        catalyst = stack.copy();
        catalyst.setCount(1);
        selectedFormula = recipes.get(0).id().toString();
        lastRecipe = selectedFormula;
        lastMissing = "";
        currentSuction = null;
        setChangedAndSync();
        return true;
    }

    public ItemStack extractCatalyst() {
        if (catalyst.isEmpty()) {
            return ItemStack.EMPTY;
        }
        ItemStack out = catalyst.copy();
        catalyst = ItemStack.EMPTY;
        progress = 0;
        selectedFormula = "";
        currentSuction = null;
        setChangedAndSync();
        return out;
    }

    public static ThaumatoriumBlockEntity resolveAt(Level level, BlockPos partPos) {
        if (level == null || partPos == null) {
            return null;
        }
        BlockEntity direct = level.getBlockEntity(partPos);
        if (direct instanceof ThaumatoriumBlockEntity thaumatorium) {
            return thaumatorium;
        }
        BlockEntity below = level.getBlockEntity(partPos.below());
        if (below instanceof ThaumatoriumBlockEntity thaumatorium && thaumatorium.worldPosition.above().equals(partPos)) {
            return thaumatorium;
        }
        return null;
    }

    public boolean canConnectAt(BlockPos partPos, Direction sideFromConstruct) {
        if (partPos == null || sideFromConstruct == null) {
            return false;
        }
        boolean upper = partPos.equals(worldPosition.above());
        if (!partPos.equals(worldPosition) && !upper) {
            return false;
        }
        if (sideFromConstruct == facing() || sideFromConstruct == Direction.DOWN) {
            return false;
        }
        return upper || sideFromConstruct != Direction.UP;
    }

    public int suctionAmountAt(BlockPos partPos, Direction sideFromConstruct) {
        return currentSuction != null && canConnectAt(partPos, sideFromConstruct) && !isPowered() ? ORIGINAL_SUCTION : 0;
    }

    public Aspect suctionTypeAt(BlockPos partPos, Direction sideFromConstruct) {
        return suctionAmountAt(partPos, sideFromConstruct) > 0 ? currentSuction : null;
    }

    private Aspect firstMissingAspect(AlchemyRecipe recipe) {
        for (Aspect aspect : Aspect.values()) {
            int required = recipe.cost().getOrDefault(aspect, 0);
            if (required > 0 && essentia.get(aspect) < required) {
                return aspect;
            }
        }
        return null;
    }

    private boolean fillOneFromAdjacentTube(Aspect aspect) {
        if (level == null || aspect == null) {
            return false;
        }
        for (int y = 0; y <= 1; y++) {
            BlockPos part = worldPosition.above(y);
            for (Direction direction : Direction.values()) {
                if (!canConnectAt(part, direction)) {
                    continue;
                }
                BlockEntity adjacent = level.getBlockEntity(part.relative(direction));
                if (!(adjacent instanceof EssentiaTubeBlockEntity tube)) {
                    continue;
                }
                Direction faceFromTube = direction.getOpposite();
                if (!tube.allowsOutputTo(faceFromTube)
                        || tube.getTransportEssentiaAmount(faceFromTube) <= 0
                        || tube.getSuctionAmount(faceFromTube) >= ORIGINAL_SUCTION
                        || ORIGINAL_SUCTION < tube.getMinimumSuction()) {
                    continue;
                }
                Aspect available = tube.getTransportEssentiaType(faceFromTube);
                if (available != aspect) {
                    continue;
                }
                int taken = tube.takeEssentiaOriginal(aspect, 1, faceFromTube);
                if (taken > 0) {
                    essentia.add(aspect, taken);
                    lastMissing = "";
                    setChangedAndSync();
                    return true;
                }
            }
        }
        return false;
    }

    private AspectList recipeCostList(AlchemyRecipe recipe) {
        AspectList paid = new AspectList();
        for (Map.Entry<Aspect, Integer> entry : recipe.cost().entrySet()) {
            paid.add(entry.getKey(), entry.getValue());
        }
        return paid;
    }

    private ItemStack previewResult(AlchemyRecipe recipe) {
        if (recipe == null || catalyst.isEmpty()) {
            return ItemStack.EMPTY;
        }
        ItemStack one = catalyst.copy();
        one.setCount(1);
        return recipe.craft(one, recipeCostList(recipe));
    }

    private boolean outputCanAccept(ItemStack result) {
        if (level == null || result.isEmpty()) {
            return false;
        }
        Direction output = facing();
        BlockPos target = worldPosition.relative(output);
        Direction targetSide = output.getOpposite();
        return !TC4ItemTransferRuntime.hasInventory(level, target, targetSide)
                || TC4ItemTransferRuntime.canInsert(level, target, targetSide, result);
    }

    private boolean completeRecipe(AlchemyRecipe recipe, ItemStack result) {
        AspectList cost = recipeCostList(recipe);
        if (!essentia.containsAll(cost) || catalyst.isEmpty() || !outputCanAccept(result)) {
            return false;
        }
        if (!essentia.removeAll(cost)) {
            return false;
        }
        catalyst.shrink(1);
        if (catalyst.isEmpty()) {
            catalyst = ItemStack.EMPTY;
            selectedFormula = "";
        }

        Direction output = facing();
        BlockPos target = worldPosition.relative(output);
        Direction targetSide = output.getOpposite();
        ItemStack remainder = TC4ItemTransferRuntime.hasInventory(level, target, targetSide)
                ? TC4ItemTransferRuntime.insert(level, target, targetSide, result, false)
                : result.copy();
        if (!remainder.isEmpty()) {
            Containers.dropItemStack(level,
                    worldPosition.getX() + 0.5D + output.getStepX() * 0.66D,
                    worldPosition.getY() + 0.33D,
                    worldPosition.getZ() + 0.5D + output.getStepZ() * 0.66D,
                    remainder);
        }

        ResourceLocation resultId = ForgeRegistries.ITEMS.getKey(result.getItem());
        lastRecipe = recipe.id() + " -> " + (resultId == null ? "unknown" : resultId);
        lastMissing = "";
        currentSuction = null;
        emitOriginalCraftEffects();
        setChangedAndSync();
        return true;
    }

    private void emitOriginalCraftEffects() {
        if (!(level instanceof ServerLevel serverLevel)) {
            return;
        }
        serverLevel.playSound(null, worldPosition, TC4Sounds.event("craftstart"), SoundSource.BLOCKS, 0.55F, 1.15F);
        serverLevel.playSound(null, worldPosition, net.minecraft.sounds.SoundEvents.FIRE_EXTINGUISH, SoundSource.BLOCKS, 0.25F,
                2.6F + (level.random.nextFloat() - level.random.nextFloat()) * 0.8F);
        serverLevel.sendParticles(ParticleTypes.WITCH,
                worldPosition.getX() + 0.5D,
                worldPosition.getY() + 1.02D,
                worldPosition.getZ() + 0.5D,
                10, 0.22D, 0.10D, 0.22D, 0.015D);
    }

    public MutableComponent statusComponent() {
        MutableComponent component = Component.literal("Thaumatorium | catalyst="
                + (catalyst.isEmpty() ? "empty" : catalyst.getHoverName().getString())
                + " | formula=" + (selectedFormula.isBlank() ? "auto" : selectedFormula)
                + " | formulaIndex=" + selectedFormulaIndex()
                + " | slots=" + maxRecipes()
                + " | heat=" + heated
                + " | powered=" + isPowered()
                + " | suction=" + (currentSuction == null ? "none" : currentSuction.id() + "@" + ORIGINAL_SUCTION)
                + " | stored=" + essentia.toComponent().getString()
                + " | recipe=" + (lastRecipe.isBlank() ? "none" : lastRecipe)
                + " | progress=" + progress + "/" + ORIGINAL_CRAFT_INTERVAL_TICKS);
        if (!lastMissing.isBlank()) {
            component.append(Component.literal(" | waiting=" + lastMissing));
        }
        component.append(Component.literal(" | tube network=" + (level == null ? 0 : TC4EssentiaNetworkRuntime.networkSize(level, worldPosition))));
        return component;
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
        if (!catalyst.isEmpty()) {
            tag.put(NBT_CATALYST, catalyst.save(new CompoundTag()));
        }
        tag.put("Essentia", essentia.save());
        tag.putInt(NBT_PROGRESS, progress);
        tag.putInt("counter", counter);
        tag.putString(NBT_LAST_RECIPE, lastRecipe);
        tag.putString(NBT_SELECTED_FORMULA, selectedFormula);
        tag.putBoolean(NBT_MNEMONIC_MATRIX, cachedMnemonicMatrix);
        tag.putBoolean("heated", heated);
        if (currentSuction != null) {
            tag.putString("suction", currentSuction.id());
        }
        tag.putString("missing", lastMissing);
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        catalyst = tag.contains(NBT_CATALYST) ? ItemStack.of(tag.getCompound(NBT_CATALYST)) : ItemStack.EMPTY;
        essentia.clear();
        if (tag.contains("Essentia")) {
            essentia.load(tag.getCompound("Essentia"));
        }
        progress = Math.max(0, tag.getInt(NBT_PROGRESS));
        counter = Math.max(0, tag.getInt("counter"));
        lastRecipe = tag.getString(NBT_LAST_RECIPE);
        selectedFormula = tag.getString(NBT_SELECTED_FORMULA);
        cachedMnemonicMatrix = tag.getBoolean(NBT_MNEMONIC_MATRIX);
        heated = tag.getBoolean("heated");
        currentSuction = Aspect.byId(tag.getString("suction"));
        lastMissing = tag.getString("missing");
    }

    @Override
    public Component getDisplayName() {
        return Component.translatable("container.thaumcraft.thaumatorium");
    }

    @Override
    public AbstractContainerMenu createMenu(int containerId, Inventory inventory, Player player) {
        return new ThaumatoriumMenu(containerId, inventory, this);
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
