package com.darkifov.thaumcraft.item;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

import javax.annotation.Nullable;
import java.util.List;

/**
 * Stage119: concrete 1.19.2 carrier for original TC4 research/recipe components.
 *
 * Each instance represents a de-metadata'd TC4 1.7.10 ItemStack variant that was
 * commonly used as a research icon, recipe output, catalyst or component. Behavior
 * is intentionally ported in later item-specific stages, but the item itself is
 * now registered, textured and usable by Thaumonomicon recipe/research mapping.
 */
public class TC4ResearchComponentItem extends Item {
    private final String originalSource;
    private final String legacyTexture;

    public TC4ResearchComponentItem(Properties properties, String originalSource, String legacyTexture) {
        super(properties);
        this.originalSource = originalSource;
        this.legacyTexture = legacyTexture;
    }

    public String originalSource() {
        return originalSource;
    }

    public String legacyTexture() {
        return legacyTexture;
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.literal("TC4 1.7.10 source: " + originalSource).withStyle(ChatFormatting.DARK_PURPLE));
        tooltip.add(Component.literal("Legacy sprite: " + legacyTexture).withStyle(ChatFormatting.DARK_GRAY));
    }
}
