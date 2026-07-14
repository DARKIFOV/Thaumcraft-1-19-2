package com.darkifov.thaumcraft.block;

import com.darkifov.thaumcraft.client.render.VisChargeRelayItemRenderer;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;

import java.util.function.Consumer;

/** Uses TC4's original relay OBJ in every item transform. */
public final class VisChargeRelayBlockItem extends BlockItem {
    public VisChargeRelayBlockItem(Block block, Properties properties) {
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
