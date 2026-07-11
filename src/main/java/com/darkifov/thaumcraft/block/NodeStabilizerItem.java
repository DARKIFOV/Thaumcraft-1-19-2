package com.darkifov.thaumcraft.block;

import com.darkifov.thaumcraft.client.render.NodeStabilizerItemRenderer;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;

import java.util.function.Consumer;

/**
 * Block item for the TC4 node stabilizers.
 *
 * <p>The world block is rendered by a block-entity renderer using the original
 * TC4 OBJ mesh. A normal generated BlockItem model would therefore either be
 * invisible or fall back to the old placeholder cuboids. This item explicitly
 * selects the matching custom renderer so inventory, dropped and held views use
 * the same original geometry as the placed machine.</p>
 */
public final class NodeStabilizerItem extends BlockItem {
    private final boolean advanced;

    public NodeStabilizerItem(Block block, Properties properties, boolean advanced) {
        super(block, properties);
        this.advanced = advanced;
    }

    public boolean isAdvanced() {
        return advanced;
    }

    @Override
    public void initializeClient(Consumer<IClientItemExtensions> consumer) {
        consumer.accept(new IClientItemExtensions() {
            @Override
            public BlockEntityWithoutLevelRenderer getCustomRenderer() {
                return NodeStabilizerItemRenderer.instance();
            }
        });
    }
}
