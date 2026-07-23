package com.darkifov.thaumcraft.block;

import com.darkifov.thaumcraft.Aspect;
import com.darkifov.thaumcraft.ThaumcraftMod;
import com.darkifov.thaumcraft.blockentity.ManaPodBlockEntity;
import com.darkifov.thaumcraft.item.simple.TC4ManaBeanItem;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.TagKey;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.List;

/**
 * Source-oriented port of TC4 {@code BlockManaPod}.
 *
 * <p>The pod hangs below oak, spruce, Greatwood or Silverwood logs in a biome
 * tagged {@code thaumcraft:is_magical}. Its metadata-age contract is retained
 * as an explicit 0..7 block-state property and the stored bean aspect lives in
 * the accompanying block entity.</p>
 */
public final class ManaPodBlock extends BaseEntityBlock {
    public static final IntegerProperty AGE = BlockStateProperties.AGE_7;
    public static final TagKey<net.minecraft.world.level.biome.Biome> MAGICAL_BIOMES = TagKey.create(
            Registry.BIOME_REGISTRY, new ResourceLocation(ThaumcraftMod.MOD_ID, "is_magical"));

    private static final double[] MIN_Y = {12.0D, 10.0D, 8.0D, 6.0D, 5.0D, 4.0D, 3.0D, 2.0D};
    private static final VoxelShape[] SHAPES = new VoxelShape[8];

    static {
        for (int age = 0; age < SHAPES.length; age++) {
            SHAPES[age] = Block.box(4.0D, MIN_Y[age], 4.0D, 12.0D, 16.0D, 12.0D);
        }
    }

    public ManaPodBlock(BlockBehaviour.Properties properties) {
        super(properties);
        registerDefaultState(stateDefinition.any().setValue(AGE, 0));
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPES[state.getValue(AGE)];
    }

    @Override
    public VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPES[state.getValue(AGE)];
    }

    @Override
    public boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
        return isMagicalBiome(level, pos) && isSupportedLog(level.getBlockState(pos.above()));
    }

    public static boolean isMagicalBiome(LevelReader level, BlockPos pos) {
        return level.getBiome(pos).is(MAGICAL_BIOMES);
    }

    public static boolean isSupportedLog(BlockState state) {
        return state.is(Blocks.OAK_LOG)
                || state.is(Blocks.SPRUCE_LOG)
                || state.is(ThaumcraftMod.GREATWOOD_LOG.get())
                || state.is(ThaumcraftMod.SILVERWOOD_LOG.get());
    }

    @Override
    public BlockState updateShape(BlockState state, Direction direction, BlockState neighbour,
                                  LevelAccessor level, BlockPos pos, BlockPos neighbourPos) {
        if (direction == Direction.UP && !state.canSurvive(level, pos)) {
            level.scheduleTick(pos, this, 1);
        }
        return super.updateShape(state, direction, neighbour, level, pos, neighbourPos);
    }

    @Override
    public void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        if (!state.canSurvive(level, pos)) {
            level.destroyBlock(pos, true);
        }
    }

    @Override
    public void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        if (!state.canSurvive(level, pos)) {
            level.destroyBlock(pos, true);
            return;
        }
        if (random.nextInt(30) == 0 && level.getBlockEntity(pos) instanceof ManaPodBlockEntity pod) {
            pod.checkGrowth(random);
        }
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new ManaPodBlockEntity(pos, state);
    }

    @Override
    public List<ItemStack> getDrops(BlockState state, LootContext.Builder builder) {
        int age = state.getValue(AGE);
        if (age < 2) {
            return Collections.emptyList();
        }
        Aspect aspect = Aspect.HERBA;
        BlockEntity blockEntity = builder.getOptionalParameter(LootContextParams.BLOCK_ENTITY);
        if (blockEntity instanceof ManaPodBlockEntity pod && pod.aspect() != null) {
            aspect = pod.aspect();
        }
        int count = age == 7 && builder.getLevel().random.nextFloat() > 0.33F ? 2 : 1;
        ItemStack bean = new ItemStack(ThaumcraftMod.TC4_RESEARCH_ITEMS.get("tc4_mana_bean").get(), count);
        TC4ManaBeanItem.setAspect(bean, aspect);
        return List.of(bean);
    }

    @Override
    public ItemStack getCloneItemStack(BlockGetter level, BlockPos pos, BlockState state) {
        ItemStack bean = new ItemStack(ThaumcraftMod.TC4_RESEARCH_ITEMS.get("tc4_mana_bean").get());
        if (level.getBlockEntity(pos) instanceof ManaPodBlockEntity pod && pod.aspect() != null) {
            TC4ManaBeanItem.setAspect(bean, pod.aspect());
        }
        return bean;
    }

    @Override
    public float getDestroyProgress(BlockState state, Player player, BlockGetter level, BlockPos pos) {
        float base = super.getDestroyProgress(state, player, level, pos);
        return base * Math.max(1.0F, 8.0F - state.getValue(AGE));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(AGE);
    }
}
