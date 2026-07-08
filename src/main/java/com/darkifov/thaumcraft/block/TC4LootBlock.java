package com.darkifov.thaumcraft.block;

import com.darkifov.thaumcraft.eldritch.TC4OuterLandsLootAdapter;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * Stage221: 1.19.2 replacement for TC4 BlockLoot.
 * Original behaviour: metadata 0/1/2 controls rarity; breaking drops
 * 1 + md + rand(3) stacks from Utils.generateLoot(md, rand).
 */
public class TC4LootBlock extends Block {
    public static final IntegerProperty VARIANT = IntegerProperty.create("variant", 0, 2);
    private static final VoxelShape URN_SHAPE = Block.box(2.0D, 1.0D, 2.0D, 14.0D, 13.0D, 14.0D);
    private static final VoxelShape CRATE_SHAPE = Block.box(1.0D, 0.0D, 1.0D, 15.0D, 14.0D, 15.0D);

    public enum Kind { URN, CRATE }

    private final Kind kind;

    public TC4LootBlock(Properties properties, Kind kind) {
        super(properties);
        this.kind = kind;
        registerDefaultState(stateDefinition.any().setValue(VARIANT, 0));
    }

    public Kind kind() {
        return kind;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(VARIANT);
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter getter, BlockPos pos, CollisionContext context) {
        return kind == Kind.URN ? URN_SHAPE : CRATE_SHAPE;
    }

    @Override
    public void playerWillDestroy(Level level, BlockPos pos, BlockState state, Player player) {
        if (!level.isClientSide && !player.isCreative()) {
            RandomSource random = level.getRandom();
            int md = state.getValue(VARIANT);
            int q = 1 + md + random.nextInt(3);
            for (int i = 0; i < q; i++) {
                ItemStack loot = TC4OuterLandsLootAdapter.generateLoot(md, random);
                if (!loot.isEmpty()) {
                    popResource(level, pos, loot.copy());
                }
            }
        }
        super.playerWillDestroy(level, pos, state, player);
    }
}
