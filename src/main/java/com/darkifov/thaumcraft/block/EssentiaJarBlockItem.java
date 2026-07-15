package com.darkifov.thaumcraft.block;

import com.darkifov.thaumcraft.Aspect;
import com.darkifov.thaumcraft.AspectList;
import com.darkifov.thaumcraft.blockentity.EssentiaJarBlockEntity;
import com.darkifov.thaumcraft.client.render.EssentiaJarItemRenderer;
import net.minecraft.ChatFormatting;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;

import javax.annotation.Nullable;
import java.util.List;
import java.util.function.Consumer;

/**
 * Filled TC4 jar item. The original ItemJarFilled stored essentia and the label
 * on the stack and used a custom item renderer; a plain BlockItem loses both
 * the visual state and the contents when the block is picked up.
 */
public final class EssentiaJarBlockItem extends BlockItem {
    public static final String BLOCK_ENTITY_TAG = "BlockEntityTag";

    public EssentiaJarBlockItem(Block block, Properties properties) {
        super(block, properties.stacksTo(1));
    }

    @Override
    public void initializeClient(Consumer<IClientItemExtensions> consumer) {
        consumer.accept(new IClientItemExtensions() {
            @Override
            public BlockEntityWithoutLevelRenderer getCustomRenderer() {
                return EssentiaJarItemRenderer.instance();
            }
        });
    }

    /** Reads modern BlockEntityTag and the root-level TC4 ItemJarFilled format. */
    public static CompoundTag readJarData(ItemStack stack) {
        CompoundTag root = stack.getTag();
        if (root == null) {
            return new CompoundTag();
        }
        if (root.contains(BLOCK_ENTITY_TAG, Tag.TAG_COMPOUND)) {
            return root.getCompound(BLOCK_ENTITY_TAG).copy();
        }
        // Legacy port and TC4 migration stacks used Aspects/AspectFilter at root.
        return root.copy();
    }

    public static void writeJarData(ItemStack stack, EssentiaJarBlockEntity jar) {
        CompoundTag data = jar.getUpdateTag();
        if (jar.amount() <= 0 && !jar.hasFilter()) {
            CompoundTag root = stack.getTag();
            if (root != null) {
                root.remove(BLOCK_ENTITY_TAG);
                if (root.isEmpty()) {
                    stack.setTag(null);
                }
            }
            return;
        }
        stack.getOrCreateTag().put(BLOCK_ENTITY_TAG, data);
    }

    public static AspectList itemAspects(ItemStack stack) {
        CompoundTag data = readJarData(stack);
        AspectList result = new AspectList();
        if (data.contains("Aspects", Tag.TAG_COMPOUND)) {
            result.load(data.getCompound("Aspects"));
        } else if (data.contains("Aspect")) {
            Aspect aspect = Aspect.byId(data.getString("Aspect"));
            int amount = Math.max(0, data.getShort("Amount"));
            if (aspect != null && amount > 0) {
                result.add(aspect, amount);
            }
        }
        return result;
    }

    @Nullable
    public static Aspect itemFilter(ItemStack stack) {
        CompoundTag data = readJarData(stack);
        Aspect filter = null;
        if (data.contains("FilterAspect")) {
            filter = Aspect.byId(data.getString("FilterAspect"));
        }
        if (filter == null && data.contains("AspectFilter")) {
            filter = Aspect.byId(data.getString("AspectFilter"));
        }
        return filter;
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, level, tooltip, flag);
        AspectList aspects = itemAspects(stack);
        if (!aspects.isEmpty()) {
            tooltip.add(Component.literal("Essentia: ").withStyle(ChatFormatting.GRAY).append(aspects.toComponent()));
        }
        Aspect filter = itemFilter(stack);
        if (filter != null) {
            tooltip.add(Component.literal("Filter: ").withStyle(ChatFormatting.DARK_PURPLE)
                    .append(Component.literal(filter.displayName()).withStyle(style -> style.withColor(filter.textColor()))));
        }
    }
}
