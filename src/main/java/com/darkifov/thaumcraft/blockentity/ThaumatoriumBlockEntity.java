package com.darkifov.thaumcraft.blockentity;

import com.darkifov.thaumcraft.Aspect;
import com.darkifov.thaumcraft.AspectList;
import com.darkifov.thaumcraft.ThaumcraftMod;
import com.darkifov.thaumcraft.alchemy.AlchemyRecipe;
import com.darkifov.thaumcraft.alchemy.AlchemyRecipes;
import com.darkifov.thaumcraft.alchemy.TC4ThaumatoriumParity;
import com.darkifov.thaumcraft.block.MnemonicMatrixBlock;
import com.darkifov.thaumcraft.block.ThaumatoriumBlock;
import com.darkifov.thaumcraft.essentia.TC4EssentiaNetworkRuntime;
import com.darkifov.thaumcraft.essentia.TC4ItemTransferRuntime;
import com.darkifov.thaumcraft.menu.ThaumatoriumMenu;
import com.darkifov.thaumcraft.porting.TC4Sounds;
import com.darkifov.thaumcraft.porting.TC4LegacyDuplicateItemMigrator;
import com.darkifov.thaumcraft.porting.TC4LegacyStackMigrationTarget;
import com.darkifov.thaumcraft.recipe.TC4RecipeRequirementIndex;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.Connection;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.WorldlyContainer;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.event.ForgeEventFactory;
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
public class ThaumatoriumBlockEntity extends BlockEntity implements MenuProvider, WorldlyContainer, TC4LegacyStackMigrationTarget {
    public static final int ORIGINAL_CRAFT_INTERVAL_TICKS = TC4ThaumatoriumParity.CRAFT_INTERVAL_TICKS;
    public static final int ORIGINAL_SUCTION = TC4ThaumatoriumParity.SUCTION;
    public static final int ORIGINAL_HEAT_REFRESH_TICKS = TC4ThaumatoriumParity.HEAT_REFRESH_TICKS;

    public static final String NBT_CATALYST = "Catalyst";
    public static final String NBT_PROGRESS = "progress";
    public static final String NBT_LAST_RECIPE = "recipe";
    public static final String NBT_SELECTED_FORMULA = "formula";
    public static final String NBT_MNEMONIC_MATRIX = "brain";
    public static final String NBT_FORMULAS = "recipes";
    public static final String NBT_FORMULA_PLAYERS = "OutputPlayer";
    private static final int[] CATALYST_SLOT = {0};

    private final AspectList essentia = new AspectList();
    private ItemStack catalyst = ItemStack.EMPTY;
    private int progress;
    private int counter;
    private String lastRecipe = "";
    private String lastMissing = "";
    private final List<String> rememberedFormulas = new ArrayList<>();
    private final List<String> formulaPlayers = new ArrayList<>();
    private int currentCraft = -1;
    private boolean cachedMnemonicMatrix;
    private boolean heated;
    private Aspect currentSuction;
    private int venting;

    public ThaumatoriumBlockEntity(BlockPos pos, BlockState state) {
        super(ThaumcraftMod.THAUMATORIUM_BLOCK_ENTITY.get(), pos, state);
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, ThaumatoriumBlockEntity tile) {
        if (level == null || level.isClientSide) {
            return;
        }
        if (tile.counter == 0 || tile.counter % ORIGINAL_HEAT_REFRESH_TICKS == 0) {
            tile.heated = tile.checkHeat();
            tile.refreshRecipeCapacity();
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

    public static void clientTick(Level level, BlockPos pos, BlockState state, ThaumatoriumBlockEntity tile) {
        if (level == null || !level.isClientSide || tile.venting <= 0) {
            return;
        }
        tile.venting--;
        Direction facing = tile.facing();
        double x = pos.getX() + 0.5D + facing.getStepX() * 0.5D
                + (level.random.nextDouble() - 0.5D) * 0.2D;
        double y = pos.getY() + 0.5D + (level.random.nextDouble() - 0.5D) * 0.2D;
        double z = pos.getZ() + 0.5D + facing.getStepZ() * 0.5D
                + (level.random.nextDouble() - 0.5D) * 0.2D;
        level.addParticle(ParticleTypes.POOF, x, y, z,
                facing.getStepX() * 0.25D + (level.random.nextDouble() - 0.5D) * 0.2D,
                (level.random.nextDouble() - 0.5D) * 0.2D,
                facing.getStepZ() * 0.25D + (level.random.nextDouble() - 0.5D) * 0.2D);
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
                BlockState matrix = level.getBlockState(layer.relative(direction));
                if (matrix.is(ThaumcraftMod.MNEMONIC_MATRIX.get())
                        && matrix.hasProperty(MnemonicMatrixBlock.FACING)
                        && matrix.getValue(MnemonicMatrixBlock.FACING) == direction.getOpposite()) {
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
                    BlockState matrix = level.getBlockState(layer.relative(direction));
                    if (matrix.is(ThaumcraftMod.MNEMONIC_MATRIX.get())
                            && matrix.hasProperty(MnemonicMatrixBlock.FACING)
                            && matrix.getValue(MnemonicMatrixBlock.FACING) == direction.getOpposite()) {
                        matrices++;
                    }
                }
            }
        }
        return TC4ThaumatoriumParity.recipeCapacity(matrices);
    }

    private void refreshRecipeCapacity() {
        int capacity = maxRecipes();
        while (rememberedFormulas.size() > capacity) {
            rememberedFormulas.remove(rememberedFormulas.size() - 1);
            if (formulaPlayers.size() > rememberedFormulas.size()) {
                formulaPlayers.remove(formulaPlayers.size() - 1);
            }
        }
        if (currentCraft >= rememberedFormulas.size()) {
            currentCraft = -1;
        }
    }

    public List<String> rememberedFormulaIds() {
        return Collections.unmodifiableList(rememberedFormulas);
    }

    public boolean isRemembered(AlchemyRecipe recipe) {
        return recipe != null && rememberedFormulas.contains(recipe.id().toString());
    }

    public List<AlchemyRecipe> visibleFormulaCandidates() {
        return visibleFormulaCandidates(null);
    }

    public List<AlchemyRecipe> visibleFormulaCandidates(Player player) {
        List<AlchemyRecipe> recipes = new ArrayList<>();
        for (AlchemyRecipe recipe : AlchemyRecipes.recipes()) {
            boolean remembered = isRemembered(recipe);
            boolean matches = !catalyst.isEmpty() && recipe.catalystMatches(catalyst);
            boolean unlocked = player == null || TC4RecipeRequirementIndex.isRuntimeRecipeUnlocked(
                    player, recipe.tc4Key(), recipe.research());
            if (remembered || (matches && unlocked)) {
                recipes.add(recipe);
            }
        }
        recipes.sort(Comparator.comparing(recipe -> recipe.id().toString()));
        return Collections.unmodifiableList(recipes);
    }

    public AlchemyRecipe activeRecipe() {
        if (catalyst.isEmpty() || rememberedFormulas.isEmpty()) {
            currentCraft = -1;
            return null;
        }
        if (currentCraft >= 0 && currentCraft < rememberedFormulas.size()) {
            AlchemyRecipe current = recipeById(rememberedFormulas.get(currentCraft));
            if (current != null && current.catalystMatches(catalyst)) {
                return current;
            }
        }
        currentCraft = -1;
        for (int i = 0; i < rememberedFormulas.size(); i++) {
            AlchemyRecipe recipe = recipeById(rememberedFormulas.get(i));
            if (recipe != null && recipe.catalystMatches(catalyst)) {
                currentCraft = i;
                return recipe;
            }
        }
        return null;
    }

    private AlchemyRecipe recipeById(String id) {
        if (id == null || id.isBlank()) {
            return null;
        }
        for (AlchemyRecipe recipe : AlchemyRecipes.recipes()) {
            if (recipe.id().toString().equals(id) || recipe.tc4Key().equals(id)) {
                return recipe;
            }
        }
        return null;
    }

    public ItemStack displayedFormulaOutput(long gameTime) {
        if (rememberedFormulas.isEmpty()) {
            return ItemStack.EMPTY;
        }
        int index = (int) Math.floorMod(gameTime / 40L, rememberedFormulas.size());
        AlchemyRecipe recipe = recipeById(rememberedFormulas.get(index));
        if (recipe == null || recipe.resultItemId() == null) {
            return ItemStack.EMPTY;
        }
        net.minecraft.world.item.Item item = ForgeRegistries.ITEMS.getValue(recipe.resultItemId());
        return item == null ? ItemStack.EMPTY : new ItemStack(item);
    }

    public boolean isSelectedFormula(AlchemyRecipe recipe) {
        return isRemembered(recipe);
    }

    public boolean toggleFormulaIndex(int requestedIndex, Player player) {
        List<AlchemyRecipe> candidates = visibleFormulaCandidates(player);
        if (requestedIndex < 0 || requestedIndex >= candidates.size()) {
            return false;
        }
        AlchemyRecipe recipe = candidates.get(requestedIndex);
        String id = recipe.id().toString();
        int existing = rememberedFormulas.indexOf(id);
        if (existing >= 0) {
            rememberedFormulas.remove(existing);
            if (existing < formulaPlayers.size()) {
                formulaPlayers.remove(existing);
            }
            currentCraft = -1;
        } else {
            if (rememberedFormulas.size() >= maxRecipes()) {
                return false;
            }
            rememberedFormulas.add(id);
            formulaPlayers.add(player == null ? "" : player.getGameProfile().getName());
        }
        currentSuction = null;
        progress = 0;
        lastMissing = "";
        setChangedAndSync();
        return true;
    }

    /** Compatibility entry point retained for older packets and tests. */
    public boolean selectFormulaIndex(int requestedIndex) {
        return toggleFormulaIndex(requestedIndex, null);
    }

    public int selectedFormulaIndex() {
        AlchemyRecipe active = activeRecipe();
        if (active == null) {
            return -1;
        }
        List<AlchemyRecipe> candidates = visibleFormulaCandidates();
        for (int i = 0; i < candidates.size(); i++) {
            if (active.id().equals(candidates.get(i).id())) {
                return i;
            }
        }
        return -1;
    }

    public void cycleFormula() {
        if (rememberedFormulas.isEmpty()) {
            currentCraft = -1;
            return;
        }
        int start = currentCraft;
        for (int offset = 1; offset <= rememberedFormulas.size(); offset++) {
            int candidate = Math.floorMod(start + offset, rememberedFormulas.size());
            AlchemyRecipe recipe = recipeById(rememberedFormulas.get(candidate));
            if (recipe != null && recipe.catalystMatches(catalyst)) {
                currentCraft = candidate;
                currentSuction = null;
                setChangedAndSync();
                return;
            }
        }
    }

    public boolean insertCatalyst(ItemStack stack) {
        if (stack == null || stack.isEmpty() || !catalyst.isEmpty()) {
            return false;
        }
        catalyst = stack.copy();
        catalyst.setCount(Math.min(catalyst.getCount(), getMaxStackSize()));
        lastMissing = "";
        currentCraft = -1;
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
        currentCraft = -1;
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
        // TileThaumatorium.completeRecipe() replaces its AspectList after a successful match.
        // Normal intake is recipe-bounded, but clearing the entire buffer is important for
        // legacy/migrated NBT that may contain unrelated residual aspects.
        essentia.clear();
        ItemStack consumedCatalyst = catalyst.copy();
        consumedCatalyst.setCount(1);
        catalyst.shrink(1);
        if (catalyst.isEmpty()) {
            catalyst = ItemStack.EMPTY;
        }

        Direction output = facing();
        BlockPos target = worldPosition.relative(output);
        Direction targetSide = output.getOpposite();
        ItemStack remainder = TC4ItemTransferRuntime.hasInventory(level, target, targetSide)
                ? TC4ItemTransferRuntime.insert(level, target, targetSide, result, false)
                : result.copy();
        if (!remainder.isEmpty()) {
            ItemEntity entity = new ItemEntity(level,
                    worldPosition.getX() + 0.5D + output.getStepX() * 0.66D,
                    worldPosition.getY() + 0.33D + output.getOpposite().getStepY(),
                    worldPosition.getZ() + 0.5D + output.getStepZ() * 0.66D,
                    remainder);
            entity.setDeltaMovement(0.075D * output.getStepX(), 0.025D, 0.075D * output.getStepZ());
            level.addFreshEntity(entity);
            level.blockEvent(worldPosition, getBlockState().getBlock(), 0, 0);
        }

        if (level instanceof ServerLevel serverLevel && currentCraft >= 0 && currentCraft < formulaPlayers.size()) {
            String playerName = formulaPlayers.get(currentCraft);
            Player recipePlayer = playerName.isBlank() ? null : serverLevel.getServer().getPlayerList().getPlayerByName(playerName);
            if (recipePlayer != null) {
                ForgeEventFactory.firePlayerCraftingEvent(recipePlayer, result.copy(),
                        new SimpleContainer(consumedCatalyst));
            }
        }
        currentCraft = -1;

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
        serverLevel.playSound(null, worldPosition, net.minecraft.sounds.SoundEvents.FIRE_EXTINGUISH, SoundSource.BLOCKS, 0.25F,
                2.6F + (level.random.nextFloat() - level.random.nextFloat()) * 0.8F);
    }

    public MutableComponent statusComponent() {
        MutableComponent component = Component.literal("Thaumatorium | catalyst="
                + (catalyst.isEmpty() ? "empty" : catalyst.getHoverName().getString())
                + " | formulas=" + rememberedFormulas.size() + "/" + maxRecipes()
                + " | formulaIndex=" + selectedFormulaIndex()
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
    public int migrateLegacyStacks() {
        TC4LegacyDuplicateItemMigrator.MigrationResult result =
                TC4LegacyDuplicateItemMigrator.migrateStackDeepWithStatus(catalyst);
        if (!result.changed()) {
            return 0;
        }
        catalyst = result.stack();
        setChangedAndSync();
        return result.changedStacks();
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
        ListTag formulas = new ListTag();
        for (String id : rememberedFormulas) {
            formulas.add(StringTag.valueOf(id));
        }
        tag.put(NBT_FORMULAS, formulas);
        ListTag players = new ListTag();
        for (String player : formulaPlayers) {
            players.add(StringTag.valueOf(player == null ? "" : player));
        }
        tag.put(NBT_FORMULA_PLAYERS, players);
        tag.putInt("currentCraft", currentCraft);
        tag.putInt("maxrec", maxRecipes());
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
        rememberedFormulas.clear();
        formulaPlayers.clear();
        ListTag formulas = tag.getList(NBT_FORMULAS, Tag.TAG_STRING);
        for (int i = 0; i < formulas.size(); i++) {
            String id = formulas.getString(i);
            if (recipeById(id) != null && !rememberedFormulas.contains(id)) {
                rememberedFormulas.add(id);
            }
        }
        // Migration from the pre-11.64.39 single-formula field.
        if (rememberedFormulas.isEmpty() && tag.contains(NBT_SELECTED_FORMULA, Tag.TAG_STRING)) {
            String migrated = tag.getString(NBT_SELECTED_FORMULA);
            AlchemyRecipe recipe = recipeById(migrated);
            if (recipe != null) {
                rememberedFormulas.add(recipe.id().toString());
            }
        }
        ListTag players = tag.getList(NBT_FORMULA_PLAYERS, Tag.TAG_STRING);
        for (int i = 0; i < rememberedFormulas.size(); i++) {
            formulaPlayers.add(i < players.size() ? players.getString(i) : "");
        }
        currentCraft = tag.contains("currentCraft", Tag.TAG_INT) ? tag.getInt("currentCraft") : -1;
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

    @Override
    public boolean triggerEvent(int id, int type) {
        if (id >= 0 && level != null && level.isClientSide) {
            venting = TC4ThaumatoriumParity.CLIENT_VENT_TICKS;
            return true;
        }
        return super.triggerEvent(id, type);
    }

    @Override
    public int getContainerSize() {
        return 1;
    }

    @Override
    public boolean isEmpty() {
        return catalyst.isEmpty();
    }

    @Override
    public ItemStack getItem(int slot) {
        return slot == 0 ? catalyst : ItemStack.EMPTY;
    }

    @Override
    public ItemStack removeItem(int slot, int amount) {
        if (slot != 0 || catalyst.isEmpty() || amount <= 0) {
            return ItemStack.EMPTY;
        }
        ItemStack removed = catalyst.split(amount);
        if (catalyst.isEmpty()) {
            catalyst = ItemStack.EMPTY;
        }
        currentCraft = -1;
        currentSuction = null;
        setChangedAndSync();
        return removed;
    }

    @Override
    public ItemStack removeItemNoUpdate(int slot) {
        if (slot != 0) {
            return ItemStack.EMPTY;
        }
        ItemStack removed = catalyst;
        catalyst = ItemStack.EMPTY;
        currentCraft = -1;
        currentSuction = null;
        return removed;
    }

    @Override
    public void setItem(int slot, ItemStack stack) {
        if (slot != 0) {
            return;
        }
        catalyst = stack == null ? ItemStack.EMPTY : stack.copy();
        if (!catalyst.isEmpty() && catalyst.getCount() > getMaxStackSize()) {
            catalyst.setCount(getMaxStackSize());
        }
        currentCraft = -1;
        currentSuction = null;
        setChangedAndSync();
    }

    @Override
    public boolean stillValid(Player player) {
        return level != null && level.getBlockEntity(worldPosition) == this;
    }

    @Override
    public void clearContent() {
        catalyst = ItemStack.EMPTY;
        currentCraft = -1;
        currentSuction = null;
        setChangedAndSync();
    }

    @Override
    public boolean canPlaceItem(int slot, ItemStack stack) {
        return slot == 0;
    }

    @Override
    public int[] getSlotsForFace(Direction side) {
        return CATALYST_SLOT;
    }

    @Override
    public boolean canPlaceItemThroughFace(int slot, ItemStack stack, Direction direction) {
        return slot == 0;
    }

    @Override
    public boolean canTakeItemThroughFace(int slot, ItemStack stack, Direction direction) {
        return slot == 0;
    }
}
