package com.darkifov.thaumcraft.menu;

import com.darkifov.thaumcraft.Aspect;
import com.darkifov.thaumcraft.AspectList;
import com.darkifov.thaumcraft.ThaumcraftMod;
import com.darkifov.thaumcraft.blockentity.ResearchTableBlockEntity;
import com.darkifov.thaumcraft.research.ResearchTableInventoryRuntime;
import com.darkifov.thaumcraft.research.TC4ResearchTableParity;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;

public class ResearchTableMenu extends AbstractContainerMenu {
    private final Container table;
    private final BlockPos blockPos;

    public ResearchTableMenu(int containerId, Inventory playerInventory, FriendlyByteBuf data) {
        this(containerId, playerInventory, getContainer(playerInventory, data.readBlockPos()));
    }

    public ResearchTableMenu(int containerId, Inventory playerInventory, Container table) {
        super(ThaumcraftMod.RESEARCH_TABLE_MENU.get(), containerId);
        checkContainerSize(table, ResearchTableBlockEntity.SIZE);
        this.table = table;
        this.blockPos = table instanceof BlockEntity blockEntity ? blockEntity.getBlockPos() : BlockPos.ZERO;
        table.startOpen(playerInventory.player);

        addSlot(new Slot(table, ResearchTableBlockEntity.SLOT_SCRIBING_TOOLS, TC4ResearchTableParity.SLOT_SCRIBING_TOOLS_X, TC4ResearchTableParity.SLOT_SCRIBING_TOOLS_Y) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return ResearchTableInventoryRuntime.isScribingTools(stack);
            }
        });

        addSlot(new Slot(table, ResearchTableBlockEntity.SLOT_RESEARCH_NOTE, TC4ResearchTableParity.SLOT_RESEARCH_NOTE_X, TC4ResearchTableParity.SLOT_RESEARCH_NOTE_Y) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return ResearchTableInventoryRuntime.isResearchNote(stack);
            }
        });

        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                addSlot(new Slot(playerInventory, col + row * 9 + 9, TC4ResearchTableParity.PLAYER_INVENTORY_X + col * 18, TC4ResearchTableParity.PLAYER_INVENTORY_Y + row * 18));
            }
        }

        for (int col = 0; col < 9; col++) {
            addSlot(new Slot(playerInventory, col, TC4ResearchTableParity.PLAYER_INVENTORY_X + col * 18, TC4ResearchTableParity.PLAYER_HOTBAR_Y));
        }
    }

    private static Container getContainer(Inventory playerInventory, BlockPos pos) {
        BlockEntity blockEntity = playerInventory.player.level.getBlockEntity(pos);
        if (blockEntity instanceof ResearchTableBlockEntity table) {
            return table;
        }
        throw new IllegalStateException("Research Table block entity missing at " + pos);
    }

    public BlockPos blockPos() {
        return blockPos;
    }

    public ItemStack tableStack(int slot) {
        return table.getItem(slot);
    }

    public boolean hasInkedTools() {
        return table instanceof ResearchTableBlockEntity researchTable && researchTable.hasInkedScribingTools();
    }

    public boolean consumeInk(int amount, Player player) {
        return table instanceof ResearchTableBlockEntity researchTable && researchTable.consumeInk(amount, player);
    }


    public AspectList tableBonusAspects() {
        if (table instanceof ResearchTableBlockEntity researchTable) {
            return researchTable.bonusAspects();
        }
        return new AspectList();
    }

    public int tableBonusAmount(Aspect aspect) {
        return table instanceof ResearchTableBlockEntity researchTable ? researchTable.bonusAmount(aspect) : 0;
    }

    public boolean consumeBonusAspect(Aspect aspect) {
        return table instanceof ResearchTableBlockEntity researchTable && researchTable.consumeBonusAspect(aspect);
    }

    public void markTableChanged() {
        if (table instanceof ResearchTableBlockEntity researchTable) {
            researchTable.setChanged();
            researchTable.syncToClient();
        } else if (table instanceof BlockEntity blockEntity) {
            blockEntity.setChanged();
        }
    }

    @Override
    public boolean stillValid(Player player) {
        return table.stillValid(player);
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        ItemStack original = ItemStack.EMPTY;
        Slot slot = slots.get(index);
        if (slot != null && slot.hasItem()) {
            ItemStack stack = slot.getItem();
            original = stack.copy();
            if (index < ResearchTableBlockEntity.SIZE) {
                if (!moveItemStackTo(stack, ResearchTableBlockEntity.SIZE, slots.size(), true)) {
                    return ItemStack.EMPTY;
                }
            } else {
                int target = ResearchTableInventoryRuntime.isScribingTools(stack)
                        ? ResearchTableBlockEntity.SLOT_SCRIBING_TOOLS
                        : ResearchTableInventoryRuntime.isResearchNote(stack)
                        ? ResearchTableBlockEntity.SLOT_RESEARCH_NOTE : -1;
                if (target < 0 || !moveItemStackTo(stack, target, target + 1, false)) {
                    return ItemStack.EMPTY;
                }
            }
            if (stack.isEmpty()) {
                slot.set(ItemStack.EMPTY);
            } else {
                slot.setChanged();
            }
            if (stack.getCount() == original.getCount()) {
                return ItemStack.EMPTY;
            }
            slot.onTake(player, stack);
        }
        return original;
    }

    @Override
    public void removed(Player player) {
        super.removed(player);
        table.stopOpen(player);
    }
}
