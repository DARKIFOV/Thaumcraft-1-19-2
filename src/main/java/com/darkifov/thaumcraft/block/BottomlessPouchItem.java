package com.darkifov.thaumcraft.block;

import com.darkifov.thaumcraft.menu.BottomlessPouchMenu;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraftforge.network.NetworkHooks;

import java.util.List;

public class BottomlessPouchItem extends Item {
    public static final String TAG_ITEMS = "StoredItems";
    public static final int SLOTS = 27;

    public BottomlessPouchItem(Properties properties) {
        super(properties.stacksTo(1));
    }

    public static ListTag stored(ItemStack stack) {
        return stack.getOrCreateTag().getList(TAG_ITEMS, 10);
    }

    public static int storedCount(ItemStack stack) {
        return stored(stack).size();
    }

    public static boolean store(ItemStack pouch, ItemStack input) {
        if (input.isEmpty() || input.getItem() instanceof BottomlessPouchItem) {
            return false;
        }

        ListTag list = stored(pouch);

        if (list.size() >= SLOTS) {
            return false;
        }

        CompoundTag tag = new CompoundTag();
        input.save(tag);
        list.add(tag);
        pouch.getOrCreateTag().put(TAG_ITEMS, list);
        input.setCount(0);
        return true;
    }

    public static ItemStack takeLast(ItemStack pouch) {
        ListTag list = stored(pouch);

        if (list.isEmpty()) {
            return ItemStack.EMPTY;
        }

        CompoundTag tag = list.getCompound(list.size() - 1);
        list.remove(list.size() - 1);
        pouch.getOrCreateTag().put(TAG_ITEMS, list);
        return ItemStack.of(tag);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack pouch = player.getItemInHand(hand);

        if (!level.isClientSide) {
            if (player.isShiftKeyDown()) {
                ItemStack out = takeLast(pouch);

                if (out.isEmpty()) {
                    player.displayClientMessage(Component.literal("Bottomless Pouch is empty.").withStyle(ChatFormatting.RED), false);
                } else if (!player.getInventory().add(out)) {
                    level.addFreshEntity(new ItemEntity(level, player.getX(), player.getY(), player.getZ(), out));
                }

                player.displayClientMessage(Component.literal("Stored: " + storedCount(pouch) + "/" + SLOTS).withStyle(ChatFormatting.GOLD), false);
            } else if (player instanceof ServerPlayer serverPlayer) {
                boolean mainHand = hand == InteractionHand.MAIN_HAND;
                NetworkHooks.openScreen(
                        serverPlayer,
                        new SimpleMenuProvider(
                                (int id, Inventory inventory, Player menuPlayer) -> new BottomlessPouchMenu(id, inventory, pouch),
                                Component.literal("Bottomless Pouch")
                        ),
                        buffer -> buffer.writeBoolean(mainHand)
                );
            }
        }

        return InteractionResultHolder.success(pouch);
    }

    @Override
    public void appendHoverText(ItemStack stack, Level level, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.literal("Stored: " + storedCount(stack) + "/" + SLOTS).withStyle(ChatFormatting.GOLD));
        tooltip.add(Component.literal("Right-click: open pouch GUI.").withStyle(ChatFormatting.GRAY));
        tooltip.add(Component.literal("Shift-right-click: retrieve last stack quickly.").withStyle(ChatFormatting.GRAY));
    }
}
