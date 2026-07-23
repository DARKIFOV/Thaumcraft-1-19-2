package com.darkifov.thaumcraft.block;

import com.darkifov.thaumcraft.client.render.TallowCandleItemRenderer;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;

import java.util.function.Consumer;

/** Uses TC4's inventory candle geometry: wax body and wick, without world drips. */
public final class TallowCandleBlockItem extends BlockItem {
    public TallowCandleBlockItem(Block block, Properties properties) {
        super(block, properties);
    }

    @Override
    public void initializeClient(Consumer<IClientItemExtensions> consumer) {
        consumer.accept(new IClientItemExtensions() {
            @Override
            public BlockEntityWithoutLevelRenderer getCustomRenderer() {
                return TallowCandleItemRenderer.instance();
            }
        });
    }
}
