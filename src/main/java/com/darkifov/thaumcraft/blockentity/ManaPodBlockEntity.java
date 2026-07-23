package com.darkifov.thaumcraft.blockentity;

import com.darkifov.thaumcraft.Aspect;
import com.darkifov.thaumcraft.AspectCombinationRegistry;
import com.darkifov.thaumcraft.AspectList;
import com.darkifov.thaumcraft.ThaumcraftMod;
import com.darkifov.thaumcraft.block.ManaPodBlock;
import com.darkifov.thaumcraft.block.WandItem;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

/** Persistent aspect and the stage-three crossbreeding pass of TC4 TileManaPod. */
public final class ManaPodBlockEntity extends BlockEntity {
    @Nullable private Aspect aspect;

    public ManaPodBlockEntity(BlockPos pos, BlockState state) {
        super(ThaumcraftMod.MANA_POD_BLOCK_ENTITY.get(), pos, state);
    }

    @Nullable
    public Aspect aspect() {
        return aspect;
    }

    /** TC4 IAspectContainer parity: only a fully mature pod exposes its bean aspect. */
    public AspectList exposedAspects() {
        BlockState state = getBlockState();
        if (aspect == null || !state.hasProperty(ManaPodBlock.AGE)
                || state.getValue(ManaPodBlock.AGE) != 7) {
            return new AspectList();
        }
        return new AspectList().add(aspect, 1);
    }

    public void setAspect(@Nullable Aspect aspect) {
        this.aspect = aspect;
        markAndSync();
    }

    public void checkGrowth(RandomSource random) {
        if (level == null || level.isClientSide) {
            return;
        }
        BlockState state = getBlockState();
        if (!state.hasProperty(ManaPodBlock.AGE)) {
            return;
        }

        int age = state.getValue(ManaPodBlock.AGE);
        if (age < 7) {
            age++;
            level.setBlock(worldPosition, state.setValue(ManaPodBlock.AGE, age), Block.UPDATE_ALL);
        }

        boolean changedAspect = false;
        if (age > 2) {
            if (age == 3) {
                changedAspect = chooseCrossbredAspect(random);
            }
            if (aspect == null) {
                Aspect[] primals = WandItem.primalVisAspects();
                aspect = random.nextInt(8) == 0 ? Aspect.HERBA : primals[random.nextInt(primals.length)];
                changedAspect = true;
            }
        }

        if (changedAspect) {
            markAndSync();
        } else {
            setChanged();
        }
    }

    private boolean chooseCrossbredAspect(RandomSource random) {
        EnumMap<Aspect, Integer> nearby = new EnumMap<>(Aspect.class);
        if (aspect != null) {
            nearby.merge(aspect, 1, Integer::sum);
        }
        for (Direction direction : Direction.Plane.HORIZONTAL) {
            if (level.getBlockEntity(worldPosition.relative(direction)) instanceof ManaPodBlockEntity neighbour
                    && neighbour.aspect != null) {
                nearby.merge(neighbour.aspect, 1, Integer::sum);
            }
        }

        if (nearby.size() > 1) {
            Aspect[] distinct = nearby.keySet().toArray(Aspect[]::new);
            List<Aspect> weighted = new ArrayList<>();
            for (int i = 0; i < distinct.length; i++) {
                weighted.add(distinct[i]);
                for (int j = 0; j < distinct.length; j++) {
                    if (i == j) continue;
                    AspectCombinationRegistry.combine(distinct[i], distinct[j]).ifPresent(combo -> {
                        weighted.add(combo);
                        weighted.add(combo);
                    });
                }
            }
            if (!weighted.isEmpty()) {
                Aspect selected = weighted.get(random.nextInt(weighted.size()));
                if (selected != aspect) {
                    aspect = selected;
                    return true;
                }
            }
        }

        if (!nearby.isEmpty() && aspect == null) {
            aspect = nearby.entrySet().stream()
                    .max(Comparator.<Map.Entry<Aspect, Integer>>comparingInt(Map.Entry::getValue)
                            .thenComparingInt(entry -> -entry.getKey().ordinal()))
                    .map(Map.Entry::getKey)
                    .orElse(null);
            return aspect != null;
        }
        return false;
    }

    private void markAndSync() {
        setChanged();
        if (level != null && !level.isClientSide) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), Block.UPDATE_CLIENTS);
        }
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        if (aspect != null) {
            tag.putString("Aspect", aspect.id());
        }
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        aspect = Aspect.byId(tag.getString("Aspect"));
    }

    @Override
    public CompoundTag getUpdateTag() {
        return saveWithoutMetadata();
    }

    @Override
    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public void onDataPacket(Connection connection, ClientboundBlockEntityDataPacket packet) {
        CompoundTag tag = packet.getTag();
        if (tag != null) {
            load(tag);
        }
    }
}
