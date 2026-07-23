package com.darkifov.thaumcraft.block;

import com.darkifov.thaumcraft.client.render.NodeTransducerItemRenderer;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;

import java.util.function.Consumer;

/** Uses the same original TC4 OBJ/UV path as the placed node transducer. */
public final class NodeTransducerItem extends BlockItem {
    public NodeTransducerItem(Block block, Properties properties) {
        super(block, properties);
    }

    @Override
    public void initializeClient(Consumer<IClientItemExtensions> consumer) {
        consumer.accept(new IClientItemExtensions() {
            @Override
            public BlockEntityWithoutLevelRenderer getCustomRenderer() {
                return NodeTransducerItemRenderer.instance();
            }
        });
    }
}
