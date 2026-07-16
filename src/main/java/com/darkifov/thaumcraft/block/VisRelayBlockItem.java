package com.darkifov.thaumcraft.block;

import com.darkifov.thaumcraft.client.render.VisChargeRelayItemRenderer;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;

import java.util.function.Consumer;

/** Renders the relay with TC4's original vis_relay.obj instead of a cube. */
public final class VisRelayBlockItem extends BlockItem {
    public VisRelayBlockItem(Block block, Properties properties) {
        super(block, properties);
    }

    @Override
    public void initializeClient(Consumer<IClientItemExtensions> consumer) {
        consumer.accept(new IClientItemExtensions() {
            @Override
            public BlockEntityWithoutLevelRenderer getCustomRenderer() {
                return VisChargeRelayItemRenderer.instance();
            }
        });
    }
}
