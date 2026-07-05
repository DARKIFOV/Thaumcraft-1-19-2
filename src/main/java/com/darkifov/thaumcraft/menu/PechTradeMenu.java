package com.darkifov.thaumcraft.menu;

import com.darkifov.thaumcraft.ThaumcraftMod;
import com.darkifov.thaumcraft.entity.PechEntity;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public class PechTradeMenu extends AbstractContainerMenu {
    private final int pechEntityId;

    public PechTradeMenu(int containerId, Inventory playerInventory, FriendlyByteBuf data) {
        this(containerId, playerInventory, data.readInt());
    }

    public PechTradeMenu(int containerId, Inventory playerInventory, int pechEntityId) {
        super(ThaumcraftMod.PECH_TRADE_MENU.get(), containerId);
        this.pechEntityId = pechEntityId;

        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                addSlot(new Slot(playerInventory, col + row * 9 + 9, 8 + col * 18, 116 + row * 18));
            }
        }

        for (int col = 0; col < 9; col++) {
            addSlot(new Slot(playerInventory, col, 8 + col * 18, 174));
        }
    }

    public int pechEntityId() {
        return pechEntityId;
    }

    @Override
    public boolean stillValid(Player player) {
        Entity entity = player.level.getEntity(pechEntityId);
        return entity instanceof PechEntity && entity.distanceToSqr(player) <= 64.0D;
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        return ItemStack.EMPTY;
    }
}
