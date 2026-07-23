package com.darkifov.thaumcraft.item;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

import java.util.List;

/** Save-compatible registry alias for a TC4 renderer texture that was never a standalone item. */
public final class TC4LegacyTextureArtifactItem extends Item {
    private final String source;

    public TC4LegacyTextureArtifactItem(Properties properties, String source) {
        super(properties.stacksTo(1));
        this.source = source;
    }

    @Override
    public void appendHoverText(ItemStack stack, Level level, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("tooltip.thaumcraft.legacy_texture_artifact")
                .withStyle(ChatFormatting.DARK_GRAY, ChatFormatting.ITALIC));
        tooltip.add(Component.literal(source).withStyle(ChatFormatting.GRAY));
    }
}
