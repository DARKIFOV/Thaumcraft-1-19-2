package com.darkifov.thaumcraft.block;

import com.darkifov.thaumcraft.client.render.AlchemicalCentrifugeItemRenderer;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;

import java.util.function.Consumer;

/** Uses TC4 ModelCentrifuge rather than a generic cuboid item model. */
public final class AlchemicalCentrifugeBlockItem extends BlockItem {
    public AlchemicalCentrifugeBlockItem(Block block, Properties properties) {
        super(block, properties);
    }

    @Override
    public void initializeClient(Consumer<IClientItemExtensions> consumer) {
        consumer.accept(new IClientItemExtensions() {
            @Override
            public BlockEntityWithoutLevelRenderer getCustomRenderer() {
                return AlchemicalCentrifugeItemRenderer.instance();
            }
        });
    }
}
