package com.darkifov.thaumcraft.block;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

import java.util.List;

/** Cosmetic/research mirror for TC4 fortress mask sprites.
 * Actual TC4 behavior is attached to the fortress helmet NBT tag "mask" by infusion.
 */
public class TC4FortressMaskItem extends Item {
    private final int mask;
    private final String originalSource;
    private final String legacyTexture;

    public TC4FortressMaskItem(Properties properties, int mask, String originalSource, String legacyTexture) {
        super(properties);
        this.mask = mask;
        this.originalSource = originalSource;
        this.legacyTexture = legacyTexture;
    }

    @Override
    public void appendHoverText(ItemStack stack, Level level, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("item.HelmetFortress.mask." + mask).withStyle(ChatFormatting.GOLD));
        tooltip.add(Component.literal("TC4 mask NBT value: " + mask).withStyle(ChatFormatting.DARK_PURPLE));
        tooltip.add(Component.literal(originalSource + " / " + legacyTexture).withStyle(ChatFormatting.GRAY));
    }
}
