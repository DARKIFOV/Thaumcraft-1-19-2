package com.darkifov.thaumcraft.blockentity;

import com.darkifov.thaumcraft.Aspect;
import com.darkifov.thaumcraft.AspectList;
import com.darkifov.thaumcraft.ThaumcraftMod;
import com.darkifov.thaumcraft.block.NodeStabilizerBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;

import java.util.List;

public class AuraNodeBlockEntity extends BlockEntity {
    private static final Aspect[] PRIMARY = new Aspect[]{
            Aspect.AER,
            Aspect.TERRA,
            Aspect.IGNIS,
            Aspect.AQUA,
            Aspect.ORDO,
            Aspect.PERDITIO
    };

    private final AspectList aspects = new AspectList();
    private boolean initialized = false;
    private String nodeType = "NORMAL";

    public AuraNodeBlockEntity(BlockPos pos, BlockState state) {
        super(ThaumcraftMod.AURA_NODE_BLOCK_ENTITY.get(), pos, state);
    }

    public AspectList aspects() {
        return aspects;
    }

    public boolean initialized() {
        return initialized;
    }

    public String nodeType() {
        return nodeType;
    }

    public boolean isStabilized() {
        return level != null && NodeStabilizerBlock.hasStabilizerNearby(level, worldPosition);
    }

    public void initializeFromPosition() {
        if (initialized) {
            return;
        }

        int seed = Math.abs(worldPosition.getX() * 31 + worldPosition.getY() * 17 + worldPosition.getZ() * 13);

        int typeRoll = seed % 20;
        if (typeRoll == 0) {
            nodeType = "PURE";
        } else if (typeRoll == 1) {
            nodeType = "TAINTED";
        } else if (typeRoll == 2) {
            nodeType = "HUNGRY";
        } else {
            nodeType = "NORMAL";
        }

        Aspect first = PRIMARY[seed % PRIMARY.length];
        Aspect second = PRIMARY[(seed / 3 + 2) % PRIMARY.length];
        Aspect third = PRIMARY[(seed / 7 + 4) % PRIMARY.length];

        int bonus = switch (nodeType) {
            case "PURE" -> 16;
            case "TAINTED" -> 8;
            case "HUNGRY" -> 20;
            default -> 0;
        };

        aspects.add(first, 24 + seed % 24 + bonus);
        aspects.add(second, 16 + seed % 16);

        if (seed % 3 == 0 || "HUNGRY".equals(nodeType)) {
            aspects.add(third, 8 + seed % 12);
        }

        if (seed % 5 == 0 || "PURE".equals(nodeType)) {
            aspects.add(Aspect.PRAECANTATIO, 8 + seed % 10);
        }

        if ("TAINTED".equals(nodeType)) {
            aspects.add(Aspect.PERDITIO, 16);
            aspects.add(Aspect.VACUOS, 8);
        }

        if ("HUNGRY".equals(nodeType)) {
            aspects.add(Aspect.VACUOS, 24);
        }

        initialized = true;
        setChangedAndSync();
    }

    public int drainToWand(Aspect aspect, int amount) {
        int removed = aspects.removeUpTo(aspect, amount);

        if (removed > 0) {
            setChangedAndSync();
        }

        return removed;
    }

    public void regenerateSlowly() {
        int max = switch (nodeType) {
            case "PURE" -> 128;
            case "HUNGRY" -> 144;
            case "TAINTED" -> 112;
            default -> 96;
        };

        if (aspects.totalAmount() >= max) {
            return;
        }

        int seed = Math.abs(worldPosition.getX() * 11 + worldPosition.getY() * 19 + worldPosition.getZ() * 23 + (int) (level == null ? 0 : level.getGameTime()));
        Aspect aspect = PRIMARY[seed % PRIMARY.length];

        if ("TAINTED".equals(nodeType) && seed % 3 == 0) {
            aspect = Aspect.PERDITIO;
        }

        if ("PURE".equals(nodeType) && seed % 4 == 0) {
            aspect = Aspect.PRAECANTATIO;
        }

        if ("HUNGRY".equals(nodeType) && seed % 4 == 0) {
            aspect = Aspect.VACUOS;
        }

        aspects.add(aspect, 1);
        setChangedAndSync();
    }

    public void tickNodeEffect(Level level) {
        boolean stabilized = isStabilized();

        if ("TAINTED".equals(nodeType) && !stabilized) {
            if (level.getGameTime() % 200L == 0L) {
                BlockPos target = worldPosition.offset(level.random.nextInt(7) - 3, -1, level.random.nextInt(7) - 3);

                if (!level.isOutsideBuildHeight(target) && !level.getBlockState(target).isAir()) {
                    level.setBlock(target, ThaumcraftMod.TAINTED_SOIL.get().defaultBlockState(), 3);
                }
            }
        }

        if ("PURE".equals(nodeType)) {
            if (level.getGameTime() % 160L == 0L) {
                BlockPos target = worldPosition.offset(level.random.nextInt(7) - 3, -1, level.random.nextInt(7) - 3);

                if (!level.isOutsideBuildHeight(target) && level.getBlockState(target).is(ThaumcraftMod.TAINTED_SOIL.get())) {
                    level.setBlock(target, Blocks.DIRT.defaultBlockState(), 3);
                }
            }
        }

        if ("HUNGRY".equals(nodeType) && !stabilized) {
            if (level.getGameTime() % 20L == 0L) {
                AABB area = new AABB(worldPosition).inflate(6.0D);
                List<ItemEntity> items = level.getEntitiesOfClass(ItemEntity.class, area, item -> item.isAlive() && !item.getItem().isEmpty());

                for (ItemEntity item : items) {
                    double dx = worldPosition.getX() + 0.5D - item.getX();
                    double dy = worldPosition.getY() + 0.5D - item.getY();
                    double dz = worldPosition.getZ() + 0.5D - item.getZ();
                    item.setDeltaMovement(item.getDeltaMovement().add(dx * 0.015D, dy * 0.015D, dz * 0.015D));
                }
            }
        }
    }

    public void setChangedAndSync() {
        setChanged();

        if (level != null) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
        }
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, AuraNodeBlockEntity node) {
        if (!node.initialized()) {
            node.initializeFromPosition();
        }

        if (level.getGameTime() % 400L == 0L) {
            node.regenerateSlowly();
        }

        node.tickNodeEffect(level);
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.putBoolean("Initialized", initialized);
        tag.putString("NodeType", nodeType);
        tag.put("Aspects", aspects.save());
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        initialized = tag.getBoolean("Initialized");
        nodeType = tag.contains("NodeType") ? tag.getString("NodeType") : "NORMAL";
        aspects.clear();
        aspects.load(tag.getCompound("Aspects"));
    }

    @Override
    public CompoundTag getUpdateTag() {
        CompoundTag tag = new CompoundTag();
        saveAdditional(tag);
        return tag;
    }

    @Override
    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public void onDataPacket(Connection connection, ClientboundBlockEntityDataPacket packet) {
        load(packet.getTag());
    }
}
