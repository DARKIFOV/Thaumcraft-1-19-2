package com.darkifov.thaumcraft.block;

import com.darkifov.thaumcraft.client.render.BellowsItemRenderer;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;

import java.util.function.Consumer;

/** Uses TC4's original five-part ModelBellows in every item display context. */
public final class BellowsBlockItem extends BlockItem {
    public BellowsBlockItem(Block block, Properties properties) {
        super(block, properties);
    }

    @Override
    public void initializeClient(Consumer<IClientItemExtensions> consumer) {
        consumer.accept(new IClientItemExtensions() {
            @Override
            public BlockEntityWithoutLevelRenderer getCustomRenderer() {
                return BellowsItemRenderer.instance();
            }
        });
    }
}
