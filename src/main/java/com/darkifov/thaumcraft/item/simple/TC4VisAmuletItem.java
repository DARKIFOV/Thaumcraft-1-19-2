package com.darkifov.thaumcraft.item.simple;

import com.darkifov.thaumcraft.Aspect;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

import java.text.DecimalFormat;
import java.util.List;

/** Per-primal centivis storage used by the two flattened TC4 vis-amulet variants. */
public class TC4VisAmuletItem extends Item {
    private static final DecimalFormat VIS_FORMAT = new DecimalFormat("#######.##");
    private final int capacity;

    public TC4VisAmuletItem(Properties properties, int capacity) {
        super(properties.stacksTo(1));
        this.capacity = capacity;
    }

    public int capacity() {
        return capacity;
    }

    public int getVis(ItemStack stack, Aspect aspect) {
        return stack.hasTag() && aspect != null ? Math.max(0, stack.getTag().getInt(aspect.id())) : 0;
    }

    public void setVis(ItemStack stack, Aspect aspect, int amount) {
        if (aspect != null && aspect.isPrimal()) {
            stack.getOrCreateTag().putInt(aspect.id(), Math.max(0, Math.min(capacity, amount)));
        }
    }

    public int addRealVis(ItemStack stack, Aspect aspect, int amount) {
        int stored = getVis(stack, aspect);
        int accepted = Math.max(0, Math.min(amount, capacity - stored));
        if (accepted > 0) {
            setVis(stack, aspect, stored + accepted);
        }
        return accepted;
    }

    @Override
    public void appendHoverText(ItemStack stack, Level level, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.literal("Capacity: " + VIS_FORMAT.format(capacity / 100.0F))
                .withStyle(ChatFormatting.GOLD));
        for (Aspect aspect : Aspect.values()) {
            if (!aspect.isPrimal()) continue;
            int amount = getVis(stack, aspect);
            if (amount > 0) {
                tooltip.add(Component.literal(aspect.displayName() + " x " + VIS_FORMAT.format(amount / 100.0F))
                        .withStyle(aspect.color()));
            }
        }
    }
}
