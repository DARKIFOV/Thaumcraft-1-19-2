package com.darkifov.thaumcraft.client.render;

import com.darkifov.thaumcraft.ThaumcraftMod;
import com.darkifov.thaumcraft.blockentity.WardedGlassBlockEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Matrix3f;
import com.mojang.math.Matrix4f;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;

/** Exact TC4 47-tile connected-texture selector for warded glass. */
public final class WardedGlassRenderer implements BlockEntityRenderer<WardedGlassBlockEntity> {
    private static final int[] CONNECTED_TEXTURE_REF = {
            0, 0, 6, 6, 0, 0, 6, 6, 3, 3, 19, 15, 3, 3, 19, 15, 1, 1, 18, 18, 1, 1, 13, 13, 2, 2, 23, 31, 2, 2, 27, 14, 0, 0, 6, 6, 0, 0, 6, 6, 3, 3, 19, 15, 3, 3, 19, 15, 1, 1, 18, 18, 1, 1, 13, 13, 2, 2, 23, 31, 2, 2, 27, 14, 4, 4, 5, 5, 4, 4, 5, 5, 17, 17, 22, 26, 17, 17, 22, 26, 16, 16, 20, 20, 16, 16, 28, 28, 21, 21, 46, 42, 21, 21, 43, 38, 4, 4, 5, 5, 4, 4, 5, 5, 9, 9, 30, 12, 9, 9, 30, 12, 16, 16, 20, 20, 16, 16, 28, 28, 25, 25, 45, 37, 25, 25, 40, 32, 0, 0, 6, 6, 0, 0, 6, 6, 3, 3, 19, 15, 3, 3, 19, 15, 1, 1, 18, 18, 1, 1, 13, 13, 2, 2, 23, 31, 2, 2, 27, 14, 0, 0, 6, 6, 0, 0, 6, 6, 3, 3, 19, 15, 3, 3, 19, 15, 1, 1, 18, 18, 1, 1, 13, 13, 2, 2, 23, 31, 2, 2, 27, 14, 4, 4, 5, 5, 4, 4, 5, 5, 17, 17, 22, 26, 17, 17, 22, 26, 7, 7, 24, 24, 7, 7, 10, 10, 29, 29, 44, 41, 29, 29, 39, 33, 4, 4, 5, 5, 4, 4, 5, 5, 9, 9, 30, 12, 9, 9, 30, 12, 7, 7, 24, 24, 7, 7, 10, 10, 8, 8, 36, 35, 8, 8, 34, 11
    };

    public WardedGlassRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public void render(WardedGlassBlockEntity glass, float partialTick, PoseStack poseStack,
                       MultiBufferSource buffer, int packedLight, int packedOverlay) {
        Level level = glass.getLevel();
        if (level == null) {
            return;
        }
        BlockPos pos = glass.getBlockPos();
        for (Direction face : Direction.values()) {
            if (isConnected(level, pos.relative(face), glass)) {
                continue;
            }
            int texture = textureFor(level, pos, face, glass);
            ResourceLocation location = new ResourceLocation(ThaumcraftMod.MOD_ID,
                    "textures/block/tc4/warded_glass_" + texture + ".png");
            VertexConsumer consumer = buffer.getBuffer(RenderType.entityTranslucent(location));
            renderFace(face, poseStack.last(), consumer, packedLight);
        }
    }

    static int textureFor(Level level, BlockPos pos, Direction face, WardedGlassBlockEntity origin) {
        int mask = 0;
        BlockPos[] offsets = offsets(face);
        for (int bit = 0; bit < offsets.length; bit++) {
            BlockPos delta = offsets[bit];
            if (isConnected(level, pos.offset(delta), origin)) {
                mask |= 1 << bit;
            }
        }
        return CONNECTED_TEXTURE_REF[mask] + 1;
    }

    private static BlockPos[] offsets(Direction face) {
        return switch (face) {
            case DOWN, UP -> new BlockPos[] {
                    new BlockPos(-1, 0, -1), new BlockPos(0, 0, -1), new BlockPos(1, 0, -1),
                    new BlockPos(-1, 0, 0), new BlockPos(1, 0, 0),
                    new BlockPos(-1, 0, 1), new BlockPos(0, 0, 1), new BlockPos(1, 0, 1)
            };
            case NORTH -> new BlockPos[] {
                    new BlockPos(1, 1, 0), new BlockPos(0, 1, 0), new BlockPos(-1, 1, 0),
                    new BlockPos(1, 0, 0), new BlockPos(-1, 0, 0),
                    new BlockPos(1, -1, 0), new BlockPos(0, -1, 0), new BlockPos(-1, -1, 0)
            };
            case SOUTH -> new BlockPos[] {
                    new BlockPos(-1, 1, 0), new BlockPos(0, 1, 0), new BlockPos(1, 1, 0),
                    new BlockPos(-1, 0, 0), new BlockPos(1, 0, 0),
                    new BlockPos(-1, -1, 0), new BlockPos(0, -1, 0), new BlockPos(1, -1, 0)
            };
            case WEST -> new BlockPos[] {
                    new BlockPos(0, 1, -1), new BlockPos(0, 1, 0), new BlockPos(0, 1, 1),
                    new BlockPos(0, 0, -1), new BlockPos(0, 0, 1),
                    new BlockPos(0, -1, -1), new BlockPos(0, -1, 0), new BlockPos(0, -1, 1)
            };
            case EAST -> new BlockPos[] {
                    new BlockPos(0, 1, 1), new BlockPos(0, 1, 0), new BlockPos(0, 1, -1),
                    new BlockPos(0, 0, 1), new BlockPos(0, 0, -1),
                    new BlockPos(0, -1, 1), new BlockPos(0, -1, 0), new BlockPos(0, -1, -1)
            };
        };
    }

    private static boolean isConnected(Level level, BlockPos pos, WardedGlassBlockEntity origin) {
        if (!level.getBlockState(pos).is(ThaumcraftMod.WARDED_GLASS.get())) {
            return false;
        }
        return level.getBlockEntity(pos) instanceof WardedGlassBlockEntity other
                && other.ownerId().equals(origin.ownerId());
    }

    private static void renderFace(Direction face, PoseStack.Pose pose, VertexConsumer consumer, int light) {
        float e = 0.001F;
        switch (face) {
            case DOWN -> quad(pose, consumer,
                    0, -e, 0, 1, -e, 0, 1, -e, 1, 0, -e, 1,
                    0, -1, 0, light);
            case UP -> quad(pose, consumer,
                    0, 1 + e, 1, 1, 1 + e, 1, 1, 1 + e, 0, 0, 1 + e, 0,
                    0, 1, 0, light);
            case NORTH -> quad(pose, consumer,
                    1, 0, -e, 0, 0, -e, 0, 1, -e, 1, 1, -e,
                    0, 0, -1, light);
            case SOUTH -> quad(pose, consumer,
                    0, 0, 1 + e, 1, 0, 1 + e, 1, 1, 1 + e, 0, 1, 1 + e,
                    0, 0, 1, light);
            case WEST -> quad(pose, consumer,
                    -e, 0, 0, -e, 0, 1, -e, 1, 1, -e, 1, 0,
                    -1, 0, 0, light);
            case EAST -> quad(pose, consumer,
                    1 + e, 0, 1, 1 + e, 0, 0, 1 + e, 1, 0, 1 + e, 1, 1,
                    1, 0, 0, light);
        }
    }

    private static void quad(PoseStack.Pose pose, VertexConsumer consumer,
                             float x1, float y1, float z1,
                             float x2, float y2, float z2,
                             float x3, float y3, float z3,
                             float x4, float y4, float z4,
                             float nx, float ny, float nz, int light) {
        vertex(pose.pose(), pose.normal(), consumer, x1, y1, z1, 0, 1, nx, ny, nz, light);
        vertex(pose.pose(), pose.normal(), consumer, x2, y2, z2, 1, 1, nx, ny, nz, light);
        vertex(pose.pose(), pose.normal(), consumer, x3, y3, z3, 1, 0, nx, ny, nz, light);
        vertex(pose.pose(), pose.normal(), consumer, x4, y4, z4, 0, 0, nx, ny, nz, light);
    }

    private static void vertex(Matrix4f matrix, Matrix3f normal, VertexConsumer consumer,
                               float x, float y, float z, float u, float v,
                               float nx, float ny, float nz, int light) {
        consumer.vertex(matrix, x, y, z)
                .color(255, 255, 255, 255)
                .uv(u, v)
                .overlayCoords(OverlayTexture.NO_OVERLAY)
                .uv2(light)
                .normal(normal, nx, ny, nz)
                .endVertex();
    }
}
