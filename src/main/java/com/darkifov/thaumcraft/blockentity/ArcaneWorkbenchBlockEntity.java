package com.darkifov.thaumcraft.blockentity;

import com.darkifov.thaumcraft.Aspect;
import com.darkifov.thaumcraft.ThaumcraftMod;
import com.darkifov.thaumcraft.arcane.ArcaneWorkbenchRecipe;
import com.darkifov.thaumcraft.block.WandItem;
import com.darkifov.thaumcraft.data.PlayerThaumData;
import com.darkifov.thaumcraft.menu.ArcaneWorkbenchMenu;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.Container;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.resources.ResourceLocation;

public class ArcaneWorkbenchBlockEntity extends BlockEntity implements Container, MenuProvider {
    public static final int SLOT_WAND = 0;
    public static final int SLOT_CATALYST = 1;
    public static final int SLOT_INGREDIENT_START = 2;
    public static final int SLOT_INGREDIENT_END = 10;
    public static final int SLOT_OUTPUT = 11;
    public static final int SIZE = 12;
    public static final int ORDO_COST = 2;

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

        if (!PlayerThaumData.hasResearch(player, recipe.research())) {
            player.displayClientMessage(Component.literal("Research locked: " + recipe.research()).withStyle(ChatFormatting.RED), false);
            return false;
        }

        ItemStack wand = getItem(SLOT_WAND);

        if (!(wand.getItem() instanceof WandItem)) {
            player.displayClientMessage(Component.literal("Put a wand into the wand slot.").withStyle(ChatFormatting.RED), false);
            return false;
        }

        if (WandItem.getVis(wand, Aspect.ORDO) < ORDO_COST && !player.getAbilities().instabuild) {
            player.displayClientMessage(Component.literal("Not enough Ordo vis in wand. Need Ordo " + ORDO_COST + ".").withStyle(ChatFormatting.RED), false);
            return false;
        }

        ItemStack catalyst = getItem(SLOT_CATALYST);

        if (!recipe.catalystMatches(catalyst)) {
            player.displayClientMessage(Component.literal("Wrong or missing catalyst.").withStyle(ChatFormatting.RED), false);
            return false;
        }

        if (!hasIngredients(recipe.ingredients())) {
            player.displayClientMessage(Component.literal("Missing ingredient items in the 3x3 grid.").withStyle(ChatFormatting.RED), false);
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
            WandItem.consumeVis(wand, Aspect.ORDO, ORDO_COST);
            getItem(SLOT_CATALYST).shrink(1);
            consumeIngredients(recipe.ingredients());
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

    private boolean hasIngredients(List<ResourceLocation> ingredients) {
        Map<ResourceLocation, Integer> needed = new HashMap<>();

        for (ResourceLocation id : ingredients) {
            needed.put(id, needed.getOrDefault(id, 0) + 1);
        }

        Map<ResourceLocation, Integer> available = new HashMap<>();

        for (int slot = SLOT_INGREDIENT_START; slot <= SLOT_INGREDIENT_END; slot++) {
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

    private void consumeIngredients(List<ResourceLocation> ingredients) {
        for (ResourceLocation needed : ingredients) {
            for (int slot = SLOT_INGREDIENT_START; slot <= SLOT_INGREDIENT_END; slot++) {
                ItemStack stack = getItem(slot);
                ResourceLocation id = ForgeRegistries.ITEMS.getKey(stack.getItem());

                if (id != null && id.equals(needed)) {
                    stack.shrink(1);
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
            return stack.getItem() instanceof WandItem;
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

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        ContainerHelper.saveAllItems(tag, items);
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);

        for (int i = 0; i < items.size(); i++) {
            items.set(i, ItemStack.EMPTY);
        }

        ContainerHelper.loadAllItems(tag, items);
    }
}
