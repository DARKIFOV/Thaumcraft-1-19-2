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
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.network.chat.Component;
import net.minecraft.world.Container;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import net.minecraft.world.WorldlyContainer;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import java.util.Map;
import java.util.Optional;
import net.minecraft.resources.ResourceLocation;

public class ArcaneWorkbenchBlockEntity extends BlockEntity implements WorldlyContainer, MenuProvider {
    /** Exact TC4 TileMagicWorkbench layout: 0..8 grid, 9 output, 10 wand. */
    public static final int SLOT_INGREDIENT_START = 0;
    public static final int SLOT_INGREDIENT_END = 8;
    public static final int SLOT_OUTPUT = 9;
    public static final int SLOT_WAND = 10;
    public static final int SIZE = 11;
    private static final int LEGACY_STAGE_CATALYST_SLOT = 11;
    private static final int[] AUTOMATION_SLOTS = new int[]{SLOT_WAND};
    private boolean suppressPreviewUpdate = false;
    private ItemStack pendingLegacyCatalyst = ItemStack.EMPTY;

    public static int slotForGrid(int row, int col) {
        return SLOT_INGREDIENT_START + row * 3 + col;
    }

    private final NonNullList<ItemStack> items = NonNullList.withSize(SIZE, ItemStack.EMPTY);

    public ArcaneWorkbenchBlockEntity(BlockPos pos, BlockState state) {
        super(ThaumcraftMod.ARCANE_WORKBENCH_BLOCK_ENTITY.get(), pos, state);
    }

    @Override
    public Component getDisplayName() {
        return Component.translatable("container.arcaneworkbench");
    }

    @Override
    public AbstractContainerMenu createMenu(int containerId, Inventory playerInventory, Player player) {
        return new ArcaneWorkbenchMenu(containerId, playerInventory, this);
    }

    /** Exact BlockTable#onWandRightClick transformation contract from TC4 4.2.3.5. */
    public static InteractionResult transformFromTable(Level level, BlockPos pos, Player player,
                                                       InteractionHand hand, ItemStack wandStack) {
        if (!(wandStack.getItem() instanceof WandItem)) {
            return InteractionResult.PASS;
        }
        if (level.isClientSide) {
            return InteractionResult.SUCCESS;
        }

        boolean staff = WandItem.isStaffStack(wandStack);
        level.setBlock(pos, ThaumcraftMod.ARCANE_WORKBENCH.get().defaultBlockState(), 3);
        if (level.getBlockEntity(pos) instanceof ArcaneWorkbenchBlockEntity workbench) {
            if (!staff) {
                ItemStack installed = wandStack.copy();
                installed.setCount(1);
                workbench.setItem(SLOT_WAND, installed);
                // TC4 removes the selected wand even in creative mode.
                player.setItemInHand(hand, ItemStack.EMPTY);
            }
            workbench.setChanged();
            workbench.syncToClient();
        }
        level.playSound(null, pos, SoundEvents.UI_BUTTON_CLICK, SoundSource.BLOCKS, 0.15F, 0.5F);
        return InteractionResult.CONSUME;
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
            consumeArcaneVisCost(wand, recipe, player);
            consumeMatchedArcaneGrid(player);
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
            consumeArcaneVisCost(wand, recipe, player);
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

        // v11.62.11: strict original TC4 container behaviour. The actual output
        // slot is populated only when a non-staff wand is present and can pay the
        // complete arcane cost. GuiArcaneWorkbench renders a separate dim ghost
        // result plus "Insufficient vis" when the grid matches but the wand cannot
        // pay; that ghost is synchronized by ArcaneWorkbenchMenu and is not a real
        // collectible stack.
        // Original audit marker: findMatchingArcaneRecipe(player, false)
        ArcaneWorkbenchRecipe recipe = findMatchingArcaneRecipeForGrid(player);
        if (recipe == null || !canAffordArcaneRecipe(player, recipe)) {
            clearOutputPreview();
            return;
        }
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
            consumeArcaneVisCost(getItem(SLOT_WAND), recipe, player);
            consumeMatchedArcaneGrid(player);
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
        if (level == null || level.isClientSide) {
            return Optional.empty();
        }

        try {
            CraftingContainer crafting = createCraftingContainer();
            return level.getRecipeManager().getRecipeFor(RecipeType.CRAFTING, crafting, level);
        } catch (NullPointerException | IllegalStateException exception) {
            // v11.62.1 hotfix: integrated server startup can call container/slot refresh
            // before ReloadableServerResources is attached to MinecraftServer.
            // In that short window vanilla preview must be skipped instead of
            // crashing on MinecraftServer#getRecipeManager(). Arcane recipes are
            // handled by our loaded TC4 registry and will refresh after startup.
            return Optional.empty();
        }
    }

    private CraftingContainer createCraftingContainer() {
        CraftingContainer crafting = new CraftingContainer(new TC4DummyCraftingMenu(), 3, 3);
        for (int slot = SLOT_INGREDIENT_START; slot <= SLOT_INGREDIENT_END; slot++) {
            crafting.setItem(slot, getItem(slot).copy());
        }
        return crafting;
    }

    /** Consumes one item from every occupied 3x3 cell, as TC4's crafting slot does. */
    private void consumeMatchedArcaneGrid(Player player) {
        for (int slot = SLOT_INGREDIENT_START; slot <= SLOT_INGREDIENT_END; slot++) {
            if (!getItem(slot).isEmpty()) {
                consumeSlotPreservingContainer(player, slot);
            }
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

    public ItemStack previewArcaneResult(ArcaneWorkbenchRecipe recipe) {
        return previewResult(recipe);
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

    /**
     * Server-authoritative affordability check used by both the real output slot
     * and the synchronized original-style client ghost. This intentionally still
     * requires a real non-staff wand, matching SlotLimitedByWand in TC4.
     */
    public boolean canAffordArcaneRecipe(Player player, ArcaneWorkbenchRecipe recipe) {
        if (recipe == null) {
            return false;
        }
        ItemStack wand = getItem(SLOT_WAND);
        return wand.getItem() instanceof WandItem
                && !WandItem.isStaffStack(wand)
                && hasArcaneVisCost(wand, recipe, player, false);
    }

    public int baseArcaneCost(ArcaneWorkbenchRecipe recipe, Aspect aspect) {
        if (recipe == null || aspect == null) {
            return 0;
        }
        return Math.max(0, recipe.aspectCost().getOrDefault(aspect, 0));
    }

    public int modifiedArcaneCost(ArcaneWorkbenchRecipe recipe, Aspect aspect) {
        return modifiedArcaneCost(recipe, aspect, null);
    }

    public int modifiedArcaneCost(ArcaneWorkbenchRecipe recipe, Aspect aspect, Player player) {
        int base = baseArcaneCost(recipe, aspect);
        if (base <= 0) {
            return 0;
        }
        ItemStack wand = getItem(SLOT_WAND);
        return wand.getItem() instanceof WandItem
                ? WandItem.modifiedVisCost(wand, player, aspect, base * 100, true)
                : base * 100;
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

    /**
     * Original ThaumcraftCraftingManager-style grid matcher. It deliberately does
     * not require a wand, because GuiArcaneWorkbench must still know which recipe
     * and aspect costs are under the cursor before a wand is inserted.
     */
    public ArcaneWorkbenchRecipe findMatchingArcaneRecipeForGrid(Player player) {
        if (player == null) {
            return null;
        }

        for (ArcaneWorkbenchRecipe recipe : ArcaneWorkbenchRecipes.recipes()) {
            String requiredResearch = TC4RecipeRequirementIndex.requiredResearchForRuntimeRecipe(recipe.tc4Key(), recipe.research());
            if (!requiredResearch.isBlank() && !PlayerThaumData.hasResearch(player, requiredResearch)) {
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

    public ArcaneWorkbenchRecipe findMatchingArcaneRecipe(Player player, boolean message, boolean requireVis) {
        ItemStack wand = getItem(SLOT_WAND);
        if (!(wand.getItem() instanceof WandItem) || WandItem.isStaffStack(wand)) {
            return null;
        }

        ArcaneWorkbenchRecipe recipe = findMatchingArcaneRecipeForGrid(player);
        if (recipe == null) {
            return null;
        }
        if (requireVis && !hasArcaneVisCost(wand, recipe, player, message)) {
            return null;
        }
        return recipe;
    }

    private boolean hasArcaneVisCost(ItemStack wand, ArcaneWorkbenchRecipe recipe, Player player) {
        return hasArcaneVisCost(wand, recipe, player, player != null);
    }

    private boolean hasArcaneVisCost(ItemStack wand, ArcaneWorkbenchRecipe recipe, Player player, boolean message) {
        if (player != null && player.getAbilities().instabuild) {
            return true;
        }
        if (recipe.aspectCost().isEmpty()) {
            return true;
        }
        for (Map.Entry<Aspect, Integer> entry : recipe.aspectCost().entrySet()) {
            int needed = WandItem.modifiedVisCost(wand, player, entry.getKey(), entry.getValue() * 100, true);
            if (WandItem.getVis(wand, entry.getKey()) < needed) {
                if (message && player != null) {
                    player.displayClientMessage(Component.literal("Not enough vis in wand. Need " + entry.getKey().displayName() + " " + WandItem.formatVis(needed) + " after all TC4 modifiers.").withStyle(ChatFormatting.RED), false);
                }
                return false;
            }
        }
        return true;
    }

    private void consumeArcaneVisCost(ItemStack wand, ArcaneWorkbenchRecipe recipe, Player player) {
        if (recipe.aspectCost().isEmpty()) {
            return;
        }
        for (Map.Entry<Aspect, Integer> entry : recipe.aspectCost().entrySet()) {
            WandItem.consumeVisCost(wand, player, entry.getKey(), entry.getValue() * 100, true);
        }
    }

    private int findCatalystSlot(ArcaneWorkbenchRecipe recipe) {
        for (int slot = SLOT_INGREDIENT_START; slot <= SLOT_INGREDIENT_END; slot++) {
            if (recipe.catalystMatches(getItem(slot))) {
                return slot;
            }
        }
        return -1;
    }

    /**
     * Exact 1.7.10 ShapedArcaneRecipe matching: every shaped recipe can be
     * shifted inside the 3x3 grid and is mirrored by default. The previous
     * rebuild only checked the pattern at the top-left corner, so valid TC4
     * layouts such as the two-log Greatwood rod failed in three of four offsets
     * and in their mirrored orientation.
     */
    private boolean matchesRecipeGrid(ArcaneWorkbenchRecipe recipe, int catalystSlot) {
        if (recipe.pattern().isEmpty()) {
            return true;
        }
        return findPatternPlacement(recipe, catalystSlot) != null;
    }

    private PatternPlacement findPatternPlacement(ArcaneWorkbenchRecipe recipe, int catalystSlot) {
        Map<Character, ResourceLocation> symbolMap = recipe.inferredPatternMap();
        if (symbolMap.isEmpty() || recipe.pattern().isEmpty()) {
            return null;
        }

        int height = recipe.pattern().size();
        int width = 0;
        for (String row : recipe.pattern()) {
            width = Math.max(width, row.length());
        }
        if (width <= 0 || height <= 0 || width > 3 || height > 3) {
            return null;
        }

        // Original ShapedArcaneRecipe.matches(): x offset first, then y; normal
        // orientation before the default mirrored orientation.
        for (int offsetX = 0; offsetX <= 3 - width; offsetX++) {
            for (int offsetY = 0; offsetY <= 3 - height; offsetY++) {
                PatternPlacement normal = checkPatternPlacement(recipe, symbolMap, catalystSlot, width, height, offsetX, offsetY, false);
                if (normal != null) {
                    return normal;
                }
                PatternPlacement mirrored = checkPatternPlacement(recipe, symbolMap, catalystSlot, width, height, offsetX, offsetY, true);
                if (mirrored != null) {
                    return mirrored;
                }
            }
        }
        return null;
    }

    private PatternPlacement checkPatternPlacement(
            ArcaneWorkbenchRecipe recipe,
            Map<Character, ResourceLocation> symbolMap,
            int catalystSlot,
            int width,
            int height,
            int offsetX,
            int offsetY,
            boolean mirrored
    ) {
        for (int x = 0; x < 3; x++) {
            for (int y = 0; y < 3; y++) {
                int subX = x - offsetX;
                int subY = y - offsetY;
                char symbol = ' ';

                if (subX >= 0 && subY >= 0 && subX < width && subY < height) {
                    String row = recipe.pattern().get(subY);
                    int patternX = mirrored ? width - subX - 1 : subX;
                    if (patternX >= 0 && patternX < row.length()) {
                        symbol = row.charAt(patternX);
                    }
                }

                int slot = slotForGrid(y, x);
                ItemStack stack = getItem(slot);
                if (symbol == ' ') {
                    if (!stack.isEmpty()) {
                        return null;
                    }
                    continue;
                }

                ResourceLocation needed = symbolMap.get(symbol);
                if (needed == null) {
                    return null;
                }

                if (recipe.ingredientMatches(needed, stack)) {
                    continue;
                }

                return null;
            }
        }

        return new PatternPlacement(offsetX, offsetY, mirrored);
    }

    private record PatternPlacement(int offsetX, int offsetY, boolean mirrored) {
    }

    private boolean hasRequiredItems(ArcaneWorkbenchRecipe recipe, int catalystSlot) {
        if (!recipe.pattern().isEmpty() && !recipe.inferredPatternMap().isEmpty()) {
            // Exact placement matching already verifies every occupied and empty
            // slot, including shifted/mirrored layouts.
            return findPatternPlacement(recipe, catalystSlot) != null;
        }

        return matchesLooseRecipeExactly(recipe, catalystSlot);
    }

    /**
     * Exact port of TC4 ShapelessArcaneRecipe#matches.
     *
     * <p>Every occupied crafting-grid slot represents exactly one recipe input,
     * regardless of that stack's count. One slot containing 64 items therefore
     * cannot satisfy two identical shapeless ingredients. Every occupied slot
     * must be consumed by the recipe, so unrelated extra items reject the match.</p>
     *
     */
    private boolean matchesLooseRecipeExactly(ArcaneWorkbenchRecipe recipe, int catalystSlot) {
        if (recipe == null || catalystSlot < 0) {
            return false;
        }

        boolean[] usedSlots = new boolean[SLOT_INGREDIENT_END - SLOT_INGREDIENT_START + 1];
        if (catalystSlot < SLOT_INGREDIENT_START || catalystSlot > SLOT_INGREDIENT_END) {
            return false;
        }
        ItemStack catalyst = getItem(catalystSlot);
        if (!recipe.catalystMatches(catalyst)) {
            return false;
        }
        usedSlots[catalystSlot - SLOT_INGREDIENT_START] = true;

        for (ResourceLocation needed : recipe.normalizedLooseIngredients()) {
            boolean found = false;
            for (int slot = SLOT_INGREDIENT_START; slot <= SLOT_INGREDIENT_END; slot++) {
                int index = slot - SLOT_INGREDIENT_START;
                if (usedSlots[index]) {
                    continue;
                }
                ItemStack stack = getItem(slot);
                if (!stack.isEmpty() && recipeIngredientMatches(needed, stack)) {
                    usedSlots[index] = true;
                    found = true;
                    break;
                }
            }
            if (!found) {
                return false;
            }
        }

        for (int slot = SLOT_INGREDIENT_START; slot <= SLOT_INGREDIENT_END; slot++) {
            int index = slot - SLOT_INGREDIENT_START;
            if (!getItem(slot).isEmpty() && !usedSlots[index]) {
                return false;
            }
        }
        return true;
    }

    private boolean recipeIngredientMatches(ResourceLocation ingredient, ItemStack stack) {
        return ArcaneWorkbenchRecipe.ingredientMatches(ingredient, stack);
    }

    @Override
    public int getContainerSize() {
        return SIZE;
    }

    @Override
    public boolean isEmpty() {
        for (ItemStack stack : items) {
            if (!stack.isEmpty()) return false;
        }
        return true;
    }

    @Override
    public ItemStack getItem(int slot) {
        return slot >= 0 && slot < items.size() ? items.get(slot) : ItemStack.EMPTY;
    }

    @Override
    public ItemStack removeItem(int slot, int count) {
        ItemStack stack = ContainerHelper.removeItem(items, slot, count);
        if (!stack.isEmpty()) {
            setChanged();
            syncToClient();
        }
        return stack;
    }

    @Override
    public ItemStack removeItemNoUpdate(int slot) {
        return ContainerHelper.takeItem(items, slot);
    }

    @Override
    public void setItem(int slot, ItemStack stack) {
        if (slot < 0 || slot >= items.size()) return;
        items.set(slot, stack);
        if (stack.getCount() > getMaxStackSize()) stack.setCount(getMaxStackSize());
        setChanged();
        syncToClient();
    }

    @Override
    public boolean stillValid(Player player) {
        return !isRemoved() && level != null && level.getBlockEntity(worldPosition) == this
                && player.distanceToSqr(worldPosition.getX() + 0.5D, worldPosition.getY() + 0.5D,
                worldPosition.getZ() + 0.5D) <= 64.0D;
    }

    @Override
    public boolean canPlaceItem(int slot, ItemStack stack) {
        if (slot == SLOT_OUTPUT) return false;
        if (slot == SLOT_WAND) return stack.getItem() instanceof WandItem && !WandItem.isStaffStack(stack);
        return slot >= SLOT_INGREDIENT_START && slot <= SLOT_INGREDIENT_END;
    }

    @Override
    public int[] getSlotsForFace(Direction side) {
        return AUTOMATION_SLOTS.clone();
    }

    @Override
    public boolean canPlaceItemThroughFace(int slot, ItemStack stack, Direction side) {
        return slot == SLOT_WAND && canPlaceItem(slot, stack);
    }

    @Override
    public boolean canTakeItemThroughFace(int slot, ItemStack stack, Direction side) {
        return slot == SLOT_WAND;
    }

    @Override
    public void clearContent() {
        for (int i = 0; i < items.size(); i++) items.set(i, ItemStack.EMPTY);
        setChanged();
        syncToClient();
    }

    public void dropRealContents(Level level, BlockPos pos) {
        for (int slot = 0; slot < items.size(); slot++) {
            if (slot == SLOT_OUTPUT) continue;
            ItemStack stack = items.get(slot);
            if (!stack.isEmpty()) {
                Containers.dropItemStack(level, pos.getX(), pos.getY(), pos.getZ(), stack.copy());
                items.set(slot, ItemStack.EMPTY);
            }
        }
        if (!pendingLegacyCatalyst.isEmpty()) {
            Containers.dropItemStack(level, pos.getX(), pos.getY(), pos.getZ(), pendingLegacyCatalyst.copy());
            pendingLegacyCatalyst = ItemStack.EMPTY;
        }
        clearOutputPreview();
        setChanged();
    }

    /** Original TC4 key and all eleven slots, including the current output preview. */
    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        ListTag inventory = new ListTag();
        for (int slot = 0; slot < items.size(); slot++) {
            ItemStack stack = items.get(slot);
            if (!stack.isEmpty()) {
                CompoundTag entry = new CompoundTag();
                entry.putByte("Slot", (byte) slot);
                stack.save(entry);
                inventory.add(entry);
            }
        }
        tag.put("Inventory", inventory);
        tag.remove("Items");
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        for (int i = 0; i < items.size(); i++) items.set(i, ItemStack.EMPTY);
        pendingLegacyCatalyst = ItemStack.EMPTY;

        if (tag.contains("Inventory", Tag.TAG_LIST)) {
            readInventory(tag.getList("Inventory", Tag.TAG_COMPOUND));
        } else if (tag.contains("Items", Tag.TAG_LIST)) {
            // One-time migration from rebuild versions that used ContainerHelper and slot 11.
            readInventory(tag.getList("Items", Tag.TAG_COMPOUND));
        }
    }

    private void readInventory(ListTag inventory) {
        for (int index = 0; index < inventory.size(); index++) {
            CompoundTag entry = inventory.getCompound(index);
            int slot = entry.getByte("Slot") & 255;
            ItemStack stack = ItemStack.of(entry);
            if (slot >= 0 && slot < SIZE) {
                items.set(slot, stack);
            } else if (slot == LEGACY_STAGE_CATALYST_SLOT && !stack.isEmpty()) {
                pendingLegacyCatalyst = stack;
            }
        }
    }

    @Override
    public void onLoad() {
        super.onLoad();
        if (!pendingLegacyCatalyst.isEmpty() && level != null && !level.isClientSide) {
            ItemStack remainder = pendingLegacyCatalyst.copy();
            for (int slot = SLOT_INGREDIENT_START; slot <= SLOT_INGREDIENT_END && !remainder.isEmpty(); slot++) {
                ItemStack existing = items.get(slot);
                if (existing.isEmpty()) {
                    items.set(slot, remainder.copy());
                    remainder = ItemStack.EMPTY;
                } else if (ItemStack.isSameItemSameTags(existing, remainder)) {
                    int move = Math.min(remainder.getCount(), existing.getMaxStackSize() - existing.getCount());
                    if (move > 0) {
                        existing.grow(move);
                        remainder.shrink(move);
                    }
                }
            }
            if (!remainder.isEmpty()) {
                Containers.dropItemStack(level, worldPosition.getX() + 0.5D, worldPosition.getY() + 1.0D,
                        worldPosition.getZ() + 0.5D, remainder);
            }
            pendingLegacyCatalyst = ItemStack.EMPTY;
            setChanged();
            syncToClient();
        }
    }

    public void syncToClient() {
        if (level != null && !level.isClientSide) {
            BlockState state = getBlockState();
            level.sendBlockUpdated(worldPosition, state, state, 3);
        }
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
