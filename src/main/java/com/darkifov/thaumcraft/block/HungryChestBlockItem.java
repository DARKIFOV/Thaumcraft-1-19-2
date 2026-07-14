package com.darkifov.thaumcraft.block;

import com.darkifov.thaumcraft.client.render.HungryChestItemRenderer;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;

import java.util.function.Consumer;

/** Uses the animated TC4 chest geometry for inventory, held and dropped views. */
public final class HungryChestBlockItem extends BlockItem {
    public HungryChestBlockItem(Block block, Properties properties) {
        super(block, properties);
    }

    @Override
    public void initializeClient(Consumer<IClientItemExtensions> consumer) {
        consumer.accept(new IClientItemExtensions() {
            @Override
            public BlockEntityWithoutLevelRenderer getCustomRenderer() {
                return HungryChestItemRenderer.instance();
            }
        });
    }
}
