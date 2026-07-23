package com.darkifov.thaumcraft.block;

import com.darkifov.thaumcraft.client.render.BrainJarItemRenderer;
import com.darkifov.thaumcraft.runic.TC4WarpingGearAdapter;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
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

/** Renders the real jar, brine and original ModelBrain instead of a flat icon. */
public final class BrainJarBlockItem extends BlockItem {
    public BrainJarBlockItem(Block block, Properties properties) {
        super(block, properties);
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, level, tooltip, flag);
        TC4WarpingGearAdapter.appendTooltip(stack, null, tooltip);
    }

    @Override
    public void initializeClient(Consumer<IClientItemExtensions> consumer) {
        consumer.accept(new IClientItemExtensions() {
            @Override
            public BlockEntityWithoutLevelRenderer getCustomRenderer() {
                return BrainJarItemRenderer.instance();
            }
        });
    }
}
