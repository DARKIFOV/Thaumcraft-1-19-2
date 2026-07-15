package com.darkifov.thaumcraft.block;

import com.darkifov.thaumcraft.ThaumcraftMod;
import com.darkifov.thaumcraft.blockentity.TemporaryHoleBlockEntity;
import com.darkifov.thaumcraft.ward.WardedBlockRuntime;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.material.PushReaction;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.level.BlockEvent;

/** TC4 BlockHole + TileHole adapter: passable, invisible, restorable 3x3 tunnel. */
public class TemporaryHoleBlock extends BaseEntityBlock {
    public static final TagKey<Block> PORTABLE_HOLE_BLACKLIST = TagKey.create(
            Registry.BLOCK_REGISTRY, new ResourceLocation(ThaumcraftMod.MOD_ID, "portable_hole_blacklist"));
    public TemporaryHoleBlock(Properties properties) {
        super(properties);
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new TemporaryHoleBlockEntity(pos, state);
    }

    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        if (!(level instanceof ServerLevel)) return null;
        return createTickerHelper(type, ThaumcraftMod.TEMPORARY_HOLE_BLOCK_ENTITY.get(),
                (tickerLevel, tickerPos, tickerState, hole) -> TemporaryHoleBlockEntity.serverTick(
                        (ServerLevel) tickerLevel, tickerPos, tickerState, hole));
    }

    /**
     * Exact createHole transaction boundary: remember first, replace second,
     * initialize the memory tile only after the temporary block exists.
     */
    public static boolean createHole(ServerLevel level, BlockPos pos, int duration, int layers,
                                     Direction clickedFace, Player owner) {
        BlockState old = level.getBlockState(pos);
        if (!canReplace(level, pos, old, owner)) return false;
        if (owner != null) {
            BlockEvent.BreakEvent event = new BlockEvent.BreakEvent(level, pos, old, owner);
            MinecraftForge.EVENT_BUS.post(event);
            if (event.isCanceled()) return false;
        }
        if (!level.setBlock(pos, ThaumcraftMod.TEMPORARY_HOLE.get().defaultBlockState(), Block.UPDATE_ALL)) return false;
        BlockEntity blockEntity = level.getBlockEntity(pos);
        if (!(blockEntity instanceof TemporaryHoleBlockEntity hole)) {
            level.setBlock(pos, old, Block.UPDATE_ALL);
            return false;
        }
        hole.initialize(old, duration, layers, clickedFace, owner == null ? null : owner.getUUID());
        level.sendParticles(ParticleTypes.PORTAL,
                pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D,
                4, 0.25D, 0.25D, 0.25D, 0.02D);
        return true;
    }

    public static boolean canReplace(Level level, BlockPos pos, BlockState state, Player owner) {
        // Forge 1.19.2 equivalent of the legacy block-replaceability check.
        if (state.isAir() || state.getMaterial().isReplaceable() || state.is(Blocks.BEDROCK)
                || state.is(ThaumcraftMod.TEMPORARY_HOLE.get())
                || state.is(PORTABLE_HOLE_BLACKLIST)) return false;
        if (state.getDestroySpeed(level, pos) < 0.0F || state.hasBlockEntity()) return false;
        return owner == null || (level.mayInteract(owner, pos)
                && owner.mayUseItemAt(pos, Direction.UP, permissionStack(owner))
                && WardedBlockRuntime.mayEdit(level, pos, owner));
    }

    private static ItemStack permissionStack(Player owner) {
        ItemStack main = owner.getMainHandItem();
        ItemStack off = owner.getOffhandItem();
        if (main.getItem() instanceof WandItem) return main;
        if (off.getItem() instanceof WandItem) return off;
        return main;
    }

    @Override
    public PushReaction getPistonPushReaction(BlockState state) {
        return PushReaction.BLOCK;
    }

    @Override
    public boolean canEntityDestroy(BlockState state, BlockGetter level, BlockPos pos, Entity entity) {
        return false;
    }

    @Override
    public boolean canDropFromExplosion(BlockState state, BlockGetter level, BlockPos pos, Explosion explosion) {
        return false;
    }

    @Override
    public void onBlockExploded(BlockState state, Level level, BlockPos pos, Explosion explosion) {
        // The temporary memory block must survive until it restores its state.
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        if (random.nextInt(2) == 0) {
            double x = pos.getX() + random.nextDouble();
            double y = pos.getY() + random.nextDouble();
            double z = pos.getZ() + random.nextDouble();
            level.addParticle(ParticleTypes.PORTAL, x, y, z,
                    (random.nextDouble() - 0.5D) * 0.02D,
                    (random.nextDouble() - 0.5D) * 0.02D,
                    (random.nextDouble() - 0.5D) * 0.02D);
        }
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return Shapes.empty();
    }

    @Override
    public VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return Shapes.empty();
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.INVISIBLE;
    }

    @Override
    public boolean propagatesSkylightDown(BlockState state, BlockGetter level, BlockPos pos) {
        return true;
    }

    @Override
    public float getShadeBrightness(BlockState state, BlockGetter level, BlockPos pos) {
        return 1.0F;
    }
}
