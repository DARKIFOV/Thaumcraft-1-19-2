package com.darkifov.thaumcraft.block;

import com.darkifov.thaumcraft.client.render.InfusionMatrixItemRenderer;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;

import java.util.function.Consumer;

/** Uses the same eight-piece TC4 geometry and UV unwrap as the world matrix renderer. */
public final class InfusionMatrixBlockItem extends BlockItem {
    public InfusionMatrixBlockItem(Block block, Properties properties) {
        super(block, properties);
    }

    @Override
    public void initializeClient(Consumer<IClientItemExtensions> consumer) {
        consumer.accept(new IClientItemExtensions() {
            @Override
            public BlockEntityWithoutLevelRenderer getCustomRenderer() {
                return InfusionMatrixItemRenderer.instance();
            }
        });
    }
}
