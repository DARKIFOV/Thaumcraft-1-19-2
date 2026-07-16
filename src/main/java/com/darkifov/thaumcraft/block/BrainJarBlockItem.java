package com.darkifov.thaumcraft.block;

import com.darkifov.thaumcraft.client.render.BrainJarItemRenderer;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;

import java.util.function.Consumer;

/** Renders the real jar, brine and original ModelBrain instead of a flat icon. */
public final class BrainJarBlockItem extends BlockItem {
    public BrainJarBlockItem(Block block, Properties properties) {
        super(block, properties.stacksTo(1));
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
