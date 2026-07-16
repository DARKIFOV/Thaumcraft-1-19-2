package com.darkifov.thaumcraft.block;

import com.darkifov.thaumcraft.client.render.AlembicItemRenderer;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;

import java.util.function.Consumer;

/** Uses the original TC4 alembic OBJ in GUI, hand, ground and frame contexts. */
public final class AlembicBlockItem extends BlockItem {
    public AlembicBlockItem(Block block, Properties properties) {
        super(block, properties);
    }

    @Override
    public void initializeClient(Consumer<IClientItemExtensions> consumer) {
        consumer.accept(new IClientItemExtensions() {
            @Override
            public BlockEntityWithoutLevelRenderer getCustomRenderer() {
                return AlembicItemRenderer.instance();
            }
        });
    }
}
