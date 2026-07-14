package com.darkifov.thaumcraft.mirror;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import javax.annotation.Nullable;

/** Dimension-safe replacement for TC4's linkX/linkY/linkZ/linkDim mirror NBT. */
public record MirrorLink(ResourceKey<Level> dimension, BlockPos pos) {
    public static final String TAG_X = "linkX";
    public static final String TAG_Y = "linkY";
    public static final String TAG_Z = "linkZ";
    public static final String TAG_DIMENSION = "linkDim";
    public static final String TAG_DIMENSION_NAME = "dimname";

    public static MirrorLink at(ServerLevel level, BlockPos pos) {
        return new MirrorLink(level.dimension(), pos.immutable());
    }

    public void write(CompoundTag tag) {
        tag.putInt(TAG_X, pos.getX());
        tag.putInt(TAG_Y, pos.getY());
        tag.putInt(TAG_Z, pos.getZ());
        tag.putString(TAG_DIMENSION, dimension.location().toString());
        tag.putString(TAG_DIMENSION_NAME, dimension.location().toString());
    }

    public void write(ItemStack stack) {
        write(stack.getOrCreateTag());
    }

    @Nullable
    public static MirrorLink read(ItemStack stack) {
        return read(stack.getTag());
    }

    @Nullable
    public static MirrorLink read(@Nullable CompoundTag tag) {
        if (tag == null || !tag.contains(TAG_X, Tag.TAG_ANY_NUMERIC)
                || !tag.contains(TAG_Y, Tag.TAG_ANY_NUMERIC)
                || !tag.contains(TAG_Z, Tag.TAG_ANY_NUMERIC)
                || !tag.contains(TAG_DIMENSION)) {
            return null;
        }

        ResourceLocation dimensionId = null;
        if (tag.contains(TAG_DIMENSION, Tag.TAG_STRING)) {
            dimensionId = ResourceLocation.tryParse(tag.getString(TAG_DIMENSION));
        } else if (tag.contains(TAG_DIMENSION, Tag.TAG_ANY_NUMERIC)) {
            dimensionId = legacyDimension(tag.getInt(TAG_DIMENSION));
        }
        if (dimensionId == null) {
            return null;
        }

        ResourceKey<Level> dimension = ResourceKey.create(Registry.DIMENSION_REGISTRY, dimensionId);
        return new MirrorLink(dimension, new BlockPos(tag.getInt(TAG_X), tag.getInt(TAG_Y), tag.getInt(TAG_Z)));
    }

    public static boolean has(ItemStack stack) {
        return read(stack) != null;
    }

    public static void clear(ItemStack stack) {
        CompoundTag tag = stack.getTag();
        if (tag == null) {
            return;
        }
        tag.remove(TAG_X);
        tag.remove(TAG_Y);
        tag.remove(TAG_Z);
        tag.remove(TAG_DIMENSION);
        tag.remove(TAG_DIMENSION_NAME);
        if (tag.isEmpty()) {
            stack.setTag(null);
        }
    }

    @Nullable
    public ServerLevel resolveLevel(ServerLevel origin) {
        return origin.getServer().getLevel(dimension);
    }

    private static ResourceLocation legacyDimension(int legacyId) {
        return switch (legacyId) {
            case -1 -> Level.NETHER.location();
            case 1 -> Level.END.location();
            default -> Level.OVERWORLD.location();
        };
    }
}
