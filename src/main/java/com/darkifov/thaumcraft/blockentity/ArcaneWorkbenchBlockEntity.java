package com.darkifov.thaumcraft.blockentity;

import com.darkifov.thaumcraft.Aspect;
import com.darkifov.thaumcraft.ThaumcraftMod;
import com.darkifov.thaumcraft.arcane.ArcaneWorkbenchRecipe;
import com.darkifov.thaumcraft.arcane.ArcaneWorkbenchRecipes;
import com.darkifov.thaumcraft.block.WandItem;
import com.darkifov.thaumcraft.data.PlayerThaumData;
import com.darkifov.thaumcraft.menu.ArcaneWorkbenchMenu;
import com.darkifov.thaumcraft.recipe.TC4RecipeRequirementIndex;
import com.darkifov.thaumcraft.porting.TC4Sounds;
import com.darkifov.thaumcraft.wand.WandCraftingRuntime;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.Container;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.Containers;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.sounds.SoundSource;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import net.minecraft.resources.ResourceLocation;

public class ArcaneWorkbenchBlockEntity extends BlockEntity implements Container, MenuProvider {
    /**
     * Stage189: original TC4 TileArcaneWorkbench slot layout.
     * 0..8 = 3x3 crafting grid, 9 = output, 10 = wand.
     * Slot 11 is kept as a hidden Forge 1.19.2 migration adapter only for
     * older Stage135-188 saves that may still contain the temporary catalyst slot.
     */
    public static final int SLOT_INGREDIENT_START = 0;
    public static final int SLOT_INGREDIENT_END = 8;
    public static final int SLOT_OUTPUT = 9;
    public static final int SLOT_WAND = 10;
    public static final int SLOT_LEGACY_CATALYST = 11;
    public static final int SIZE = 12;
    public static final int ORDO_COST = 2;
    private boolean suppressPreviewUpdate = false;

    public static int slotForGrid(int row, int col) {
        return SLOT_INGREDIENT_START + row * 3 + col;
    }

    private final NonNullList<ItemStack> items = NonNullList.withSize(SIZE, ItemStack.EMPTY);

    public ArcaneWorkbenchBlockEntity(BlockPos pos, BlockState state) {
        super(ThaumcraftMod.ARCANE_WORKBENCH_BLOCK_ENTITY.get(), pos, state);
    }

    @Override
    public Component getDisplayName() {
        return Component.literal("Arcane Workbench");
    }

    @Override
    public AbstractContainerMenu createMenu(int containerId, Inventory playerInventory, Player player) {
        return new ArcaneWorkbenchMenu(containerId, playerInventory, this);
    }

    public boolean tryCraft(ArcaneWorkbenchRecipe recipe, Player player) {
        if (recipe == null) {
            player.displayClientMessage(Component.literal("Arcane recipe is missing.").withStyle(ChatFormatting.RED), false);
            return false;
        }

        String requiredResearch = TC4RecipeRequirementIndex.requiredResearchForRuntimeRecipe(recipe.tc4Key(), recipe.research());
        if (!requiredResearch.isBlank() && !PlayerThaumData.hasResearch(player, requiredResearch)) {
            player.displayClientMessage(Component.literal("Research locked: " + requiredResearch).withStyle(ChatFormatting.RED), false);
            return false;
        }

        ItemStack wand = getItem(SLOT_WAND);

        if (!(wand.getItem() instanceof WandItem)) {
            player.displayClientMessage(Component.literal("Put a wand into the wand slot.").withStyle(ChatFormatting.RED), false);
            return false;
        }

        if (!hasArcaneVisCost(wand, recipe, player)) {
            return false;
        }

        if (WandCraftingRuntime.isGeneratedAssembly(recipe)) {
            return tryCraftGeneratedWandAssembly(recipe, player, wand);
        }

        int catalystSlot = findCatalystSlot(recipe);

        if (catalystSlot < 0) {
            player.displayClientMessage(Component.literal("Wrong or missing catalyst in the original TC4 3x3 grid.").withStyle(ChatFormatting.RED), false);
            return false;
        }

        if (!matchesRecipeGrid(recipe, catalystSlot)) {
            player.displayClientMessage(Component.literal("The 3x3 arcane grid does not match the original TC4 recipe layout.").withStyle(ChatFormatting.RED), false);
            return false;
        }

        if (!hasRequiredItems(recipe, catalystSlot)) {
            player.displayClientMessage(Component.literal("Missing ingredient items in the 3x3 arcane grid.").withStyle(ChatFormatting.RED), false);
            return false;
        }

        ItemStack result = recipe.result();

        if (result.isEmpty()) {
            player.displayClientMessage(Component.literal("Recipe result item is missing.").withStyle(ChatFormatting.RED), false);
            return false;
        }

        ItemStack output = getItem(SLOT_OUTPUT);

        if (!output.isEmpty()) {
            if (!ItemStack.isSameItemSameTags(output, result)) {
                player.displayClientMessage(Component.literal("Output slot is blocked.").withStyle(ChatFormatting.RED), false);
                return false;
            }

            if (output.getCount() + result.getCount() > output.getMaxStackSize()) {
                player.displayClientMessage(Component.literal("Output slot is full.").withStyle(ChatFormatting.RED), false);
                return false;
            }
        }

        if (!player.getAbilities().instabuild) {
            consumeArcaneVisCost(wand, recipe);
            consumeSlotPreservingContainer(player, catalystSlot);
            if (!consumePatternIngredients(recipe, catalystSlot, player)) {
                consumeIngredients(recipe.ingredients(), catalystSlot, player);
            }
        }

        if (output.isEmpty()) {
            setItem(SLOT_OUTPUT, result.copy());
        } else {
            output.grow(result.getCount());
        }

        setChanged();
        player.displayClientMessage(Component.literal("Arcane crafting complete: ").append(result.getHoverName()).withStyle(ChatFormatting.GOLD), false);
        return true;
    }

    private boolean tryCraftGeneratedWandAssembly(ArcaneWorkbenchRecipe recipe, Player player, ItemStack wand) {
        if (!WandCraftingRuntime.matchesGeneratedAssembly(recipe, this, player)) {
            player.displayClientMessage(Component.literal("The 3x3 arcane grid does not match original TC4 wand/sceptre assembly.").withStyle(ChatFormatting.RED), false);
            return false;
        }

        ItemStack result = WandCraftingRuntime.resultFor(recipe, this);
        if (result.isEmpty()) {
            player.displayClientMessage(Component.literal("Wand assembly result item is missing.").withStyle(ChatFormatting.RED), false);
            return false;
        }

        ItemStack output = getItem(SLOT_OUTPUT);
        if (!output.isEmpty()) {
            if (!ItemStack.isSameItemSameTags(output, result)) {
                player.displayClientMessage(Component.literal("Output slot is blocked.").withStyle(ChatFormatting.RED), false);
                return false;
            }
            if (output.getCount() + result.getCount() > output.getMaxStackSize()) {
                player.displayClientMessage(Component.literal("Output slot is full.").withStyle(ChatFormatting.RED), false);
                return false;
            }
        }

        if (!player.getAbilities().instabuild) {
            consumeArcaneVisCost(wand, recipe);
            WandCraftingRuntime.consumeAssemblyItems(recipe, this);
        }

        if (output.isEmpty()) {
            setItem(SLOT_OUTPUT, result.copy());
        } else {
            output.grow(result.getCount());
        }

        setChanged();
        player.displayClientMessage(Component.literal("Original TC4 wand assembly complete: ").append(result.getHoverName()).withStyle(ChatFormatting.GOLD), false);
        return true;
    }

    /**
     * Stage189: original ContainerArcaneWorkbench does not use a recipe browser
     * or a craft button.  Every slot change recomputes the output from the exact
     * 3x3 grid, wand slot, research and vis state, matching
     * ThaumcraftCraftingManager.findMatchingArcaneRecipe.
     */
    public void updateOutputPreview(Player player) {
        if (suppressPreviewUpdate || player == null || level == null || level.isClientSide) {
            return;
        }

        // Stage191: original ContainerArcaneWorkbench first asks vanilla
        // CraftingManager for the 3x3 result.  Only when that result is empty
        // does it ask ThaumcraftCraftingManager for an arcane recipe.
        ItemStack vanillaPreview = previewVanillaCraftingResult(player);
        if (!vanillaPreview.isEmpty()) {
            setOutputPreview(vanillaPreview);
            return;
        }

        // Stage683-702: original TC4 GUI still previews an arcane output even when the wand lacks enough vis.
        // Taking the preview is blocked by Slot#mayPickup / craftFromOutput until the wand can pay.
        // Stage191 audit marker for original two-argument flow: findMatchingArcaneRecipe(player, false)
        ArcaneWorkbenchRecipe recipe = findMatchingArcaneRecipe(player, false, false);
        setOutputPreview(previewResult(recipe));
    }

    public void craftFromOutput(Player player) {
        if (player == null || level == null || level.isClientSide) {
            return;
        }

        // Stage191: SlotCraftingArcaneWorkbench fires for both vanilla and
        // arcane results.  Vanilla recipes win before arcane recipes and still
        // consume the 3x3 matrix with container item handling.
        if (tryConsumeVanillaCraftingResult(player)) {
            playOriginalCraftSound(0.35F);
            updateOutputPreview(player);
            return;
        }

        ArcaneWorkbenchRecipe recipe = findMatchingArcaneRecipe(player, true, true);
        if (recipe == null) {
            clearOutputPreview();
            return;
        }

        if (!player.getAbilities().instabuild) {
            consumeArcaneVisCost(getItem(SLOT_WAND), recipe);
            consumeOriginalCraftMatrix(player);
        }

        playOriginalCraftSound(0.45F);
        updateOutputPreview(player);
    }


    private void playOriginalCraftSound(float volume) {
        if (level != null && !level.isClientSide) {
            level.playSound(null, worldPosition, TC4Sounds.event("craftstart"), SoundSource.BLOCKS, volume, 0.95F + level.random.nextFloat() * 0.1F);
        }
    }

    private void setOutputPreview(ItemStack preview) {
        suppressPreviewUpdate = true;
        items.set(SLOT_OUTPUT, preview.isEmpty() ? ItemStack.EMPTY : preview.copy());
        suppressPreviewUpdate = false;
        setChanged();
    }

    private ItemStack previewVanillaCraftingResult(Player player) {
        if (level == null) {
            return ItemStack.EMPTY;
        }
        Optional<CraftingRecipe> recipe = findMatchingVanillaCraftingRecipe();
        if (recipe.isEmpty()) {
            return ItemStack.EMPTY;
        }
        ItemStack result = recipe.get().assemble(createCraftingContainer());
        return result.isEmpty() ? ItemStack.EMPTY : result.copy();
    }

    private boolean tryConsumeVanillaCraftingResult(Player player) {
        if (level == null) {
            return false;
        }
        Optional<CraftingRecipe> recipe = findMatchingVanillaCraftingRecipe();
        if (recipe.isEmpty()) {
            return false;
        }
        ItemStack result = recipe.get().assemble(createCraftingContainer());
        if (result.isEmpty()) {
            return false;
        }
        if (!player.getAbilities().instabuild) {
            consumeVanillaCraftingMatrix(recipe.get(), player);
        }
        return true;
    }

    private Optional<CraftingRecipe> findMatchingVanillaCraftingRecipe() {
        if (level == null) {
            return Optional.empty();
        }
        CraftingContainer crafting = createCraftingContainer();
        return level.getRecipeManager().getRecipeFor(RecipeType.CRAFTING, crafting, level);
    }

    private CraftingContainer createCraftingContainer() {
        CraftingContainer crafting = new CraftingContainer(new TC4DummyCraftingMenu(), 3, 3);
        for (int slot = SLOT_INGREDIENT_START; slot <= SLOT_INGREDIENT_END; slot++) {
            crafting.setItem(slot, getItem(slot).copy());
        }
        return crafting;
    }

    private void consumeOriginalCraftMatrix(Player player) {
        for (int slot = SLOT_INGREDIENT_START; slot <= SLOT_INGREDIENT_END; slot++) {
            consumeSlotPreservingContainer(player, slot);
        }
        setChanged();
    }

    private void consumeVanillaCraftingMatrix(CraftingRecipe recipe, Player player) {
        CraftingContainer crafting = createCraftingContainer();
        NonNullList<ItemStack> remainingItems = recipe.getRemainingItems(crafting);

        for (int slot = SLOT_INGREDIENT_START; slot <= SLOT_INGREDIENT_END; slot++) {
            ItemStack stack = getItem(slot);
            if (stack.isEmpty()) {
                continue;
            }

            stack.shrink(1);
            ItemStack remainder = slot < remainingItems.size() ? remainingItems.get(slot) : ItemStack.EMPTY;
            applyCraftingRemainder(player, slot, remainder);

            if (stack.isEmpty() && remainder.isEmpty()) {
                items.set(slot, ItemStack.EMPTY);
            }
        }

        setChanged();
    }

    private void applyCraftingRemainder(Player player, int slot, ItemStack remainder) {
        if (remainder == null || remainder.isEmpty()) {
            return;
        }

        ItemStack stack = getItem(slot);
        ItemStack copy = remainder.copy();
        if (stack.isEmpty()) {
            items.set(slot, copy);
            return;
        }

        if (ItemStack.isSameItemSameTags(stack, copy) && stack.getCount() + copy.getCount() <= stack.getMaxStackSize()) {
            stack.grow(copy.getCount());
            return;
        }

        if (player != null && !player.getInventory().add(copy)) {
            Containers.dropItemStack(level, player.getX(), player.getY(), player.getZ(), copy);
        }
    }

    private void consumeSlotPreservingContainer(Player player, int slot) {
        ItemStack stack = getItem(slot);
        if (stack.isEmpty()) {
            return;
        }
        ItemStack remaining = stack.getCraftingRemainingItem();
        stack.shrink(1);
        if (!remaining.isEmpty()) {
            if (stack.isEmpty()) {
                items.set(slot, remaining.copy());
            } else if (player != null && !player.getInventory().add(remaining.copy())) {
                Containers.dropItemStack(level, player.getX(), player.getY(), player.getZ(), remaining.copy());
            }
        }
    }

    private void clearOutputPreview() {
        suppressPreviewUpdate = true;
        items.set(SLOT_OUTPUT, ItemStack.EMPTY);
        suppressPreviewUpdate = false;
        setChanged();
    }

    private ItemStack previewResult(ArcaneWorkbenchRecipe recipe) {
        if (recipe == null) {
            return ItemStack.EMPTY;
        }
        if (WandCraftingRuntime.isGeneratedAssembly(recipe)) {
            return WandCraftingRuntime.resultFor(recipe, this);
        }
        return recipe.result();
    }

    public boolean canTakeOutput(Player player) {
        if (player == null || level == null || getItem(SLOT_OUTPUT).isEmpty()) {
            return false;
        }

        Optional<CraftingRecipe> vanilla = findMatchingVanillaCraftingRecipe();
        if (vanilla.isPresent()) {
            ItemStack vanillaResult = vanilla.get().assemble(createCraftingContainer());
            if (!vanillaResult.isEmpty() && sameItemSameTagsAndCount(vanillaResult, getItem(SLOT_OUTPUT))) {
                return true;
            }
        }

        ArcaneWorkbenchRecipe arcane = findMatchingArcaneRecipe(player, false, true);
        return arcane != null && sameItemSameTagsAndCount(previewResult(arcane), getItem(SLOT_OUTPUT));
    }

    private boolean sameItemSameTagsAndCount(ItemStack expected, ItemStack actual) {
        // v7.62: TC4 SlotCraftingArcaneWorkbench consumes the exact current preview.
        // Do not let an over-stacked stale preview survive a recipe/grid/vis change.
        return !expected.isEmpty()
                && !actual.isEmpty()
                && expected.getCount() == actual.getCount()
                && ItemStack.isSameItemSameTags(expected, actual);
    }

    public ArcaneWorkbenchRecipe findMatchingArcaneRecipe(Player player, boolean message) {
        return findMatchingArcaneRecipe(player, message, true);
    }

    public ArcaneWorkbenchRecipe findMatchingArcaneRecipe(Player player, boolean message, boolean requireVis) {
        ItemStack wand = getItem(SLOT_WAND);
        if (!(wand.getItem() instanceof WandItem)) {
            return null;
        }

        for (ArcaneWorkbenchRecipe recipe : ArcaneWorkbenchRecipes.recipes()) {
            String requiredResearch = TC4RecipeRequirementIndex.requiredResearchForRuntimeRecipe(recipe.tc4Key(), recipe.research());
            if (!requiredResearch.isBlank() && !PlayerThaumData.hasResearch(player, requiredResearch)) {
                continue;
            }

            if (requireVis && !hasArcaneVisCost(wand, recipe, message ? player : null)) {
                continue;
            }

            if (WandCraftingRuntime.isGeneratedAssembly(recipe)) {
                if (WandCraftingRuntime.matchesGeneratedAssembly(recipe, this, player)) {
                    return recipe;
                }
                continue;
            }

            int catalystSlot = findCatalystSlot(recipe);
            if (catalystSlot < 0) {
                continue;
            }
            if (!matchesRecipeGrid(recipe, catalystSlot)) {
                continue;
            }
            if (!hasRequiredItems(recipe, catalystSlot)) {
                continue;
            }
            if (!recipe.result().isEmpty()) {
                return recipe;
            }
        }

        return null;
    }

    private boolean hasArcaneVisCost(ItemStack wand, ArcaneWorkbenchRecipe recipe, Player player) {
        if (player != null && player.getAbilities().instabuild) {
            return true;
        }
        if (recipe.aspectCost().isEmpty()) {
            int needed = WandItem.modifiedVisCost(wand, Aspect.ORDO, ORDO_COST);
            if (WandItem.getVis(wand, Aspect.ORDO) < needed) {
                if (player != null) {
                    player.displayClientMessage(Component.literal("Not enough Ordo vis in wand. Need Ordo " + needed + " after cap modifier.").withStyle(ChatFormatting.RED), false);
                }
                return false;
            }
            return true;
        }
        for (Map.Entry<Aspect, Integer> entry : recipe.aspectCost().entrySet()) {
            int needed = WandItem.modifiedVisCost(wand, entry.getKey(), entry.getValue());
            if (WandItem.getVis(wand, entry.getKey()) < needed) {
                if (player != null) {
                    player.displayClientMessage(Component.literal("Not enough vis in wand. Need " + entry.getKey().displayName() + " " + needed + " after cap modifier.").withStyle(ChatFormatting.RED), false);
                }
                return false;
            }
        }
        return true;
    }

    private void consumeArcaneVisCost(ItemStack wand, ArcaneWorkbenchRecipe recipe) {
        if (recipe.aspectCost().isEmpty()) {
            WandItem.consumeVisCost(wand, Aspect.ORDO, ORDO_COST);
            return;
        }
        for (Map.Entry<Aspect, Integer> entry : recipe.aspectCost().entrySet()) {
            WandItem.consumeVisCost(wand, entry.getKey(), entry.getValue());
        }
    }

    private int findCatalystSlot(ArcaneWorkbenchRecipe recipe) {
        // Stage189: original TC4 has no separate catalyst slot; the catalyst is
        // one of the nine crafting-grid stacks.  Slot 11 is read only as a
        // save-migration adapter for Stage135-188 worlds.
        for (int slot = SLOT_INGREDIENT_START; slot <= SLOT_INGREDIENT_END; slot++) {
            if (recipe.catalystMatches(getItem(slot))) {
                return slot;
            }
        }

        if (recipe.catalystMatches(getItem(SLOT_LEGACY_CATALYST))) {
            return SLOT_LEGACY_CATALYST;
        }

        return -1;
    }

    private boolean matchesRecipeGrid(ArcaneWorkbenchRecipe recipe, int catalystSlot) {
        if (recipe.pattern().isEmpty()) {
            return true;
        }

        Map<Character, ResourceLocation> symbolMap = recipe.inferredPatternMap();

        if (symbolMap.isEmpty()) {
            return true;
        }

        for (int row = 0; row < 3; row++) {
            String patternRow = row < recipe.pattern().size() ? recipe.pattern().get(row) : "";
            for (int col = 0; col < 3; col++) {
                int slot = SLOT_INGREDIENT_START + row * 3 + col;
                char symbol = col < patternRow.length() ? patternRow.charAt(col) : ' ';
                ItemStack stack = getItem(slot);

                if (symbol == ' ') {
                    if (slot != catalystSlot && !stack.isEmpty()) {
                        return false;
                    }
                    continue;
                }

                ResourceLocation needed = symbolMap.get(symbol);
                if (needed == null) {
                    return true;
                }

                // Stage138: keep the old optional catalyst slot, but only for the
                // symbol inferred as the TC4 catalyst. If the catalyst is in the
                // separate slot, the matching grid position may stay empty.
                if (needed.equals(recipe.catalystItemId()) && catalystSlot == SLOT_LEGACY_CATALYST && stack.isEmpty()) {
                    continue;
                }

                ResourceLocation actual = ForgeRegistries.ITEMS.getKey(stack.getItem());
                if (actual == null || !actual.equals(needed)) {
                    return false;
                }
            }
        }

        return true;
    }

    private boolean hasRequiredItems(ArcaneWorkbenchRecipe recipe, int catalystSlot) {
        if (!recipe.pattern().isEmpty() && !recipe.inferredPatternMap().isEmpty()) {
            return hasPatternRequiredItems(recipe, catalystSlot);
        }

        return hasLooseIngredients(recipe.ingredients(), catalystSlot);
    }

    private boolean hasPatternRequiredItems(ArcaneWorkbenchRecipe recipe, int catalystSlot) {
        Map<ResourceLocation, Integer> needed = new HashMap<>();
        Map<Character, ResourceLocation> symbolMap = recipe.inferredPatternMap();

        for (int row = 0; row < 3; row++) {
            String patternRow = row < recipe.pattern().size() ? recipe.pattern().get(row) : "";
            for (int col = 0; col < 3; col++) {
                int slot = SLOT_INGREDIENT_START + row * 3 + col;
                char symbol = col < patternRow.length() ? patternRow.charAt(col) : ' ';
                if (symbol == ' ') {
                    continue;
                }

                ResourceLocation id = symbolMap.get(symbol);
                if (id == null) {
                    return hasLooseIngredients(recipe.ingredients(), catalystSlot);
                }

                if (id.equals(recipe.catalystItemId()) && (slot == catalystSlot || catalystSlot == SLOT_LEGACY_CATALYST)) {
                    continue;
                }

                needed.put(id, needed.getOrDefault(id, 0) + 1);
            }
        }

        Map<ResourceLocation, Integer> available = new HashMap<>();
        for (int slot = SLOT_INGREDIENT_START; slot <= SLOT_INGREDIENT_END; slot++) {
            if (slot == catalystSlot) {
                continue;
            }
            ItemStack stack = getItem(slot);
            ResourceLocation id = ForgeRegistries.ITEMS.getKey(stack.getItem());
            if (id != null && needed.containsKey(id)) {
                available.put(id, available.getOrDefault(id, 0) + stack.getCount());
            }
        }

        for (Map.Entry<ResourceLocation, Integer> entry : needed.entrySet()) {
            if (available.getOrDefault(entry.getKey(), 0) < entry.getValue()) {
                return false;
            }
        }

        return true;
    }

    private boolean hasLooseIngredients(List<ResourceLocation> ingredients, int catalystSlot) {
        Map<ResourceLocation, Integer> needed = new HashMap<>();

        for (ResourceLocation id : ingredients) {
            needed.put(id, needed.getOrDefault(id, 0) + 1);
        }

        Map<ResourceLocation, Integer> available = new HashMap<>();

        for (int slot = SLOT_INGREDIENT_START; slot <= SLOT_INGREDIENT_END; slot++) {
            ItemStack stack = getItem(slot);
            ResourceLocation id = ForgeRegistries.ITEMS.getKey(stack.getItem());

            if (id != null && needed.containsKey(id)) {
                int count = stack.getCount();

                if (slot == catalystSlot) {
                    count = Math.max(0, count - 1);
                }

                if (count > 0) {
                    available.put(id, available.getOrDefault(id, 0) + count);
                }
            }
        }

        for (Map.Entry<ResourceLocation, Integer> entry : needed.entrySet()) {
            if (available.getOrDefault(entry.getKey(), 0) < entry.getValue()) {
                return false;
            }
        }

        return true;
    }

    private boolean consumePatternIngredients(ArcaneWorkbenchRecipe recipe, int catalystSlot, Player player) {
        if (recipe.pattern().isEmpty() || recipe.inferredPatternMap().isEmpty()) {
            return false;
        }

        Map<Character, ResourceLocation> symbolMap = recipe.inferredPatternMap();

        for (int row = 0; row < 3; row++) {
            String patternRow = row < recipe.pattern().size() ? recipe.pattern().get(row) : "";
            for (int col = 0; col < 3; col++) {
                int slot = SLOT_INGREDIENT_START + row * 3 + col;
                char symbol = col < patternRow.length() ? patternRow.charAt(col) : ' ';
                if (symbol == ' ') {
                    continue;
                }

                ResourceLocation id = symbolMap.get(symbol);
                if (id == null) {
                    return false;
                }

                if (id.equals(recipe.catalystItemId()) && (slot == catalystSlot || catalystSlot == SLOT_LEGACY_CATALYST)) {
                    continue;
                }

                consumeSlotPreservingContainer(player, slot);
            }
        }

        return true;
    }

    private void consumeIngredients(List<ResourceLocation> ingredients, int catalystSlot, Player player) {
        for (ResourceLocation needed : ingredients) {
            for (int slot = SLOT_INGREDIENT_START; slot <= SLOT_INGREDIENT_END; slot++) {
                if (slot == catalystSlot && getItem(slot).getCount() <= 0) {
                    continue;
                }

                ItemStack stack = getItem(slot);
                ResourceLocation id = ForgeRegistries.ITEMS.getKey(stack.getItem());

                if (id != null && id.equals(needed)) {
                    consumeSlotPreservingContainer(player, slot);
                    break;
                }
            }
        }
    }

    @Override
    public int getContainerSize() {
        return SIZE;
    }

    @Override
    public boolean isEmpty() {
        for (ItemStack stack : items) {
            if (!stack.isEmpty()) {
                return false;
            }
        }

        return true;
    }

    @Override
    public ItemStack getItem(int slot) {
        return items.get(slot);
    }

    @Override
    public ItemStack removeItem(int slot, int count) {
        ItemStack stack = ContainerHelper.removeItem(items, slot, count);

        if (!stack.isEmpty()) {
            setChanged();
        }

        return stack;
    }

    @Override
    public ItemStack removeItemNoUpdate(int slot) {
        return ContainerHelper.takeItem(items, slot);
    }

    @Override
    public void setItem(int slot, ItemStack stack) {
        items.set(slot, stack);

        if (stack.getCount() > getMaxStackSize()) {
            stack.setCount(getMaxStackSize());
        }

        setChanged();
    }

    @Override
    public boolean stillValid(Player player) {
        return !isRemoved()
                && level != null
                && level.getBlockEntity(worldPosition) == this
                && player.distanceToSqr(
                        worldPosition.getX() + 0.5D,
                        worldPosition.getY() + 0.5D,
                        worldPosition.getZ() + 0.5D
                ) <= 64.0D;
    }

    @Override
    public boolean canPlaceItem(int slot, ItemStack stack) {
        if (slot == SLOT_OUTPUT) {
            return false;
        }

        if (slot == SLOT_WAND) {
            return stack.getItem() instanceof WandItem && !WandItem.isStaffStack(stack);
        }

        return true;
    }

    @Override
    public void clearContent() {
        for (int i = 0; i < items.size(); i++) {
            items.set(i, ItemStack.EMPTY);
        }

        setChanged();
    }

    public void dropRealContents(Level level, BlockPos pos) {
        for (int slot = 0; slot < items.size(); slot++) {
            if (slot == SLOT_OUTPUT) {
                continue;
            }
            ItemStack stack = items.get(slot);
            if (!stack.isEmpty()) {
                Containers.dropItemStack(level, pos.getX(), pos.getY(), pos.getZ(), stack.copy());
                items.set(slot, ItemStack.EMPTY);
            }
        }
        clearOutputPreview();
        setChanged();
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        // TC4's output slot is a virtual preview produced by the container.
        // Never serialize it as a real inventory stack, otherwise a stale preview
        // can survive world reloads and become collectible without crafting.
        ItemStack preview = items.get(SLOT_OUTPUT);
        items.set(SLOT_OUTPUT, ItemStack.EMPTY);
        ContainerHelper.saveAllItems(tag, items);
        items.set(SLOT_OUTPUT, preview);
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);

        for (int i = 0; i < items.size(); i++) {
            items.set(i, ItemStack.EMPTY);
        }

        ContainerHelper.loadAllItems(tag, items);
        items.set(SLOT_OUTPUT, ItemStack.EMPTY);
    }
    /**
     * Forge 1.19.2 adapter for the original ContainerDummy used only to ask
     * RecipeManager/CraftingManager for vanilla 3x3 matches.  It has no slots
     * and is never opened by the player.
     */
    private static final class TC4DummyCraftingMenu extends AbstractContainerMenu {
        private TC4DummyCraftingMenu() {
            super(null, -1);
        }

        @Override
        public ItemStack quickMoveStack(Player player, int index) {
            return ItemStack.EMPTY;
        }

        @Override
        public boolean stillValid(Player player) {
            return false;
        }
    }

}
