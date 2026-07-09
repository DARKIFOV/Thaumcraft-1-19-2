package com.darkifov.thaumcraft.blockentity;

import com.darkifov.thaumcraft.Aspect;
import com.darkifov.thaumcraft.AspectList;
import com.darkifov.thaumcraft.ThaumcraftMod;
import com.darkifov.thaumcraft.alchemy.AlchemyRecipe;
import com.darkifov.thaumcraft.alchemy.AlchemyRecipes;
import com.darkifov.thaumcraft.essentia.TC4EssentiaNetworkRuntime;
import com.darkifov.thaumcraft.menu.ThaumatoriumMenu;
import com.darkifov.thaumcraft.porting.TC4Sounds;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.Connection;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.Containers;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

/**
 * Stage503-562 TC4 Thaumatorium adapter.
 * It consumes the same loaded/original alchemy recipes as the crucible path, but draws essentia
 * from tube-connected containers instead of a water crucible. Research/menu selection remains
 * marked adapter until GuiThaumatorium/ContainerThaumatorium are fully ported.
 */
public class ThaumatoriumBlockEntity extends BlockEntity implements MenuProvider {
    public static final int ORIGINAL_CRAFT_INTERVAL_TICKS = 20;
    public static final String NBT_CATALYST = "Catalyst";
    public static final String NBT_PROGRESS = "progress";
    public static final String NBT_LAST_RECIPE = "recipe";
    public static final String NBT_SELECTED_FORMULA = "formula";
    public static final String NBT_MNEMONIC_MATRIX = "brain";

    private ItemStack catalyst = ItemStack.EMPTY;
    private int progress = 0;
    private String lastRecipe = "";
    private String lastMissing = "";
    private String selectedFormula = "";
    private boolean cachedMnemonicMatrix = false;

    public ThaumatoriumBlockEntity(BlockPos pos, BlockState state) {
        super(ThaumcraftMod.THAUMATORIUM_BLOCK_ENTITY.get(), pos, state);
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, ThaumatoriumBlockEntity thaumatorium) {
        if (level == null || level.isClientSide) {
            return;
        }
        if (thaumatorium.catalyst.isEmpty()) {
            thaumatorium.progress = 0;
            return;
        }
        thaumatorium.progress++;
        if (thaumatorium.progress < ORIGINAL_CRAFT_INTERVAL_TICKS) {
            return;
        }
        thaumatorium.progress = 0;
        thaumatorium.tryCraftOnce();
    }

    public ItemStack catalyst() {
        return catalyst;
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

    /** Original alchemical construct only exposes multiple remembered formulas when a mnemonic matrix is installed. */
    public boolean hasMnemonicMatrix() {
        if (level == null) {
            return cachedMnemonicMatrix;
        }
        cachedMnemonicMatrix = level.getBlockState(worldPosition.above()).is(ThaumcraftMod.MNEMONIC_MATRIX.get())
                || level.getBlockState(worldPosition.below()).is(ThaumcraftMod.MNEMONIC_MATRIX.get());
        return cachedMnemonicMatrix;
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
        if (!hasMnemonicMatrix() && recipes.size() > 1) {
            return Collections.singletonList(recipes.get(0));
        }
        return Collections.unmodifiableList(recipes);
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
            return;
        }
        int index = 0;
        for (int i = 0; i < candidates.size(); i++) {
            if (isSelectedFormula(candidates.get(i))) {
                index = i;
                break;
            }
        }
        selectedFormula = candidates.get((index + 1) % candidates.size()).id().toString();
        setChangedAndSync();
    }

    /** Stage543-562: server-side hotzone selection for original GuiThaumatorium formula icons. */
    public boolean selectFormulaIndex(int requestedIndex) {
        List<AlchemyRecipe> candidates = visibleFormulaCandidates();
        if (requestedIndex < 0 || requestedIndex >= candidates.size()) {
            return false;
        }
        selectedFormula = candidates.get(requestedIndex).id().toString();
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
        AlchemyRecipe recipe = AlchemyRecipes.findByCatalyst(stack);
        if (recipe == null) {
            return false;
        }
        catalyst = stack.copy();
        catalyst.setCount(1);
        selectedFormula = recipe.id().toString();
        lastRecipe = recipe.id().toString();
        lastMissing = "";
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
        setChangedAndSync();
        return out;
    }

    private boolean tryCraftOnce() {
        if (level == null || catalyst.isEmpty()) {
            return false;
        }
        AlchemyRecipe recipe = activeRecipe();
        if (recipe == null) {
            lastMissing = "formula";
            setChangedAndSync();
            return false;
        }
        for (Map.Entry<Aspect, Integer> entry : recipe.cost().entrySet()) {
            int available = TC4EssentiaNetworkRuntime.available(level, worldPosition, entry.getKey());
            if (available < entry.getValue()) {
                lastMissing = entry.getKey().id() + " " + available + "/" + entry.getValue();
                setChangedAndSync();
                return false;
            }
        }
        AspectList paid = new AspectList();
        for (Map.Entry<Aspect, Integer> entry : recipe.cost().entrySet()) {
            int drained = TC4EssentiaNetworkRuntime.drain(level, worldPosition, entry.getKey(), entry.getValue());
            if (drained < entry.getValue()) {
                // Defensive rollback is intentionally not invented here; this can only happen if another tick races the network.
                paid.add(entry.getKey(), drained);
                lastMissing = entry.getKey().id() + " drain-race";
                setChangedAndSync();
                return false;
            }
            paid.add(entry.getKey(), drained);
        }
        ItemStack result = recipe.craft(catalyst, paid);
        if (result.isEmpty()) {
            lastMissing = "result";
            setChangedAndSync();
            return false;
        }
        catalyst.shrink(1);
        if (catalyst.isEmpty()) {
            catalyst = ItemStack.EMPTY;
            selectedFormula = "";
        }
        ResourceLocation resultId = ForgeRegistries.ITEMS.getKey(result.getItem());
        lastRecipe = recipe.id().toString() + " -> " + (resultId == null ? "unknown" : resultId.toString());
        lastMissing = "";
        Containers.dropItemStack(level, worldPosition.getX() + 0.5D, worldPosition.getY() + 1.1D, worldPosition.getZ() + 0.5D, result);
        emitOriginalCraftEffects();
        setChangedAndSync();
        return true;
    }

    private void emitOriginalCraftEffects() {
        if (!(level instanceof ServerLevel serverLevel)) {
            return;
        }
        serverLevel.playSound(null, worldPosition, TC4Sounds.event("craftstart"), SoundSource.BLOCKS, 0.55F, 1.15F);
        serverLevel.sendParticles(ParticleTypes.WITCH,
                worldPosition.getX() + 0.5D,
                worldPosition.getY() + 1.02D,
                worldPosition.getZ() + 0.5D,
                10,
                0.22D,
                0.10D,
                0.22D,
                0.015D);
    }

    public MutableComponent statusComponent() {
        MutableComponent component = Component.literal("Thaumatorium | catalyst="
                + (catalyst.isEmpty() ? "empty" : catalyst.getHoverName().getString())
                + " | formula=" + (selectedFormula.isBlank() ? "auto" : selectedFormula)
                + " | formulaIndex=" + selectedFormulaIndex()
                + " | matrix=" + hasMnemonicMatrix()
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
        tag.putInt(NBT_PROGRESS, progress);
        tag.putString(NBT_LAST_RECIPE, lastRecipe);
        tag.putString(NBT_SELECTED_FORMULA, selectedFormula);
        tag.putBoolean(NBT_MNEMONIC_MATRIX, cachedMnemonicMatrix);
        tag.putString("missing", lastMissing);
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        catalyst = tag.contains(NBT_CATALYST) ? ItemStack.of(tag.getCompound(NBT_CATALYST)) : ItemStack.EMPTY;
        progress = Math.max(0, tag.getInt(NBT_PROGRESS));
        lastRecipe = tag.getString(NBT_LAST_RECIPE);
        selectedFormula = tag.getString(NBT_SELECTED_FORMULA);
        cachedMnemonicMatrix = tag.getBoolean(NBT_MNEMONIC_MATRIX);
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
