package com.darkifov.thaumcraft.block;

import com.darkifov.thaumcraft.Aspect;
import com.darkifov.thaumcraft.client.render.TC4BannerItemRenderer;
import net.minecraft.ChatFormatting;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;

import java.util.List;
import java.util.function.Consumer;

/** NBT-preserving item form of the TC4 banner, including all 16 creative variants. */
public final class TC4BannerBlockItem extends BlockItem {
    public TC4BannerBlockItem(Block block, Properties properties) {
        super(block, properties);
    }

    public static int getColor(ItemStack stack) {
        CompoundTag tag = stack.getTag();
        if (tag == null || !tag.contains(TC4BannerBlockEntity.TAG_COLOR)) {
            return -1;
        }
        int color = tag.getInt(TC4BannerBlockEntity.TAG_COLOR);
        return color >= 0 && color < 16 ? color : -1;
    }

    public static void setColor(ItemStack stack, int color) {
        if (color < 0 || color >= 16) {
            CompoundTag tag = stack.getTag();
            if (tag != null) {
                tag.remove(TC4BannerBlockEntity.TAG_COLOR);
                if (tag.isEmpty()) stack.setTag(null);
            }
            return;
        }
        stack.getOrCreateTag().putInt(TC4BannerBlockEntity.TAG_COLOR, color);
    }

    public static Aspect getAspect(ItemStack stack) {
        CompoundTag tag = stack.getTag();
        return tag == null ? null : Aspect.byId(tag.getString(TC4BannerBlockEntity.TAG_ASPECT));
    }

    public static void setAspect(ItemStack stack, Aspect aspect) {
        CompoundTag tag = stack.getOrCreateTag();
        if (aspect == null) {
            tag.remove(TC4BannerBlockEntity.TAG_ASPECT);
            if (tag.isEmpty()) stack.setTag(null);
        } else {
            tag.putString(TC4BannerBlockEntity.TAG_ASPECT, aspect.id());
        }
    }

    @Override
    public Component getName(ItemStack stack) {
        int color = getColor(stack);
        return color < 0
                ? Component.translatable("item.thaumcraft.tc4_block_banner.cultist")
                : Component.translatable("item.thaumcraft.tc4_block_banner." + color);
    }

    @Override
    public void appendHoverText(ItemStack stack, Level level, List<Component> tooltip, TooltipFlag flag) {
        Aspect aspect = getAspect(stack);
        if (aspect != null) {
            tooltip.add(Component.translatable("tooltip.thaumcraft.banner.aspect", aspect.displayName())
                    .withStyle(style -> style.withColor(aspect.color())));
        } else if (getColor(stack) >= 0) {
            tooltip.add(Component.translatable("tooltip.thaumcraft.banner.apply_aspect")
                    .withStyle(ChatFormatting.DARK_PURPLE));
        }
    }

    @Override
    public void fillItemCategory(CreativeModeTab category, NonNullList<ItemStack> items) {
        if (!allowedIn(category)) {
            return;
        }
        for (int color = 0; color < 16; color++) {
            ItemStack stack = new ItemStack(this);
            setColor(stack, color);
            items.add(stack);
        }
    }

    @Override
    public void initializeClient(Consumer<IClientItemExtensions> consumer) {
        consumer.accept(new IClientItemExtensions() {
            @Override
            public BlockEntityWithoutLevelRenderer getCustomRenderer() {
                return TC4BannerItemRenderer.instance();
            }
        });
    }
}
