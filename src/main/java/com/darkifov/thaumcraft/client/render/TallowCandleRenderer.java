package com.darkifov.thaumcraft.client.render;

import com.darkifov.thaumcraft.ThaumcraftMod;
import com.darkifov.thaumcraft.block.TC4TallowCandleParity;
import com.darkifov.thaumcraft.blockentity.TallowCandleBlockEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Matrix3f;
import com.mojang.math.Matrix4f;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.registries.ForgeRegistries;


/**
 * Direct Forge 1.19.2 renderer for TC4 {@code BlockCandleRenderer}.
 *
 * <p>The wax body and wick use the original atlas sprites. World instances add
 * the exact 1-5 coordinate-seeded wax drips from TC4; item rendering deliberately
 * omits them, matching {@code renderInventoryBlock}.</p>
 */
public final class TallowCandleRenderer implements BlockEntityRenderer<TallowCandleBlockEntity> {
    private static final ResourceLocation WAX = new ResourceLocation(
            ThaumcraftMod.MOD_ID, "block/tc4/candle");
    private static final ResourceLocation WICK = new ResourceLocation(
            ThaumcraftMod.MOD_ID, "block/tc4/candlestub");

    public TallowCandleRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public void render(TallowCandleBlockEntity candle, float partialTick, PoseStack poseStack,
                       MultiBufferSource buffer, int packedLight, int packedOverlay) {
        BlockPos pos = candle.getBlockPos();
        renderCandle(candle.getBlockState().getBlock(), pos, true,
                poseStack, buffer, packedLight, packedOverlay);
    }

    public static void renderItem(ItemStack stack, PoseStack poseStack, MultiBufferSource buffer,
                                  int packedLight, int packedOverlay) {
        Block block = stack.getItem() instanceof net.minecraft.world.item.BlockItem blockItem
                ? blockItem.getBlock()
                : ThaumcraftMod.TALLOW_CANDLE.get();
        renderCandle(block, BlockPos.ZERO, false, poseStack, buffer, packedLight, packedOverlay);
    }

    private static void renderCandle(Block block, BlockPos pos, boolean world,
                                     PoseStack poseStack, MultiBufferSource buffer,
                                     int packedLight, int packedOverlay) {
        TextureAtlasSprite wax = sprite(WAX);
        TextureAtlasSprite wick = sprite(WICK);
        VertexConsumer consumer = buffer.getBuffer(Sheets.cutoutBlockSheet());
        ResourceLocation key = ForgeRegistries.BLOCKS.getKey(block);
        int metadata = TC4TallowCandleParity.legacyMetadata(key == null ? null : key.getPath());
        float red = TC4TallowCandleParity.red(metadata);
        float green = TC4TallowCandleParity.green(metadata);
        float blue = TC4TallowCandleParity.blue(metadata);

        poseStack.pushPose();
        renderBox(poseStack, consumer, wax,
                (float) TC4TallowCandleParity.BODY_MIN, 0.0F, (float) TC4TallowCandleParity.BODY_MIN,
                (float) TC4TallowCandleParity.BODY_MAX, (float) TC4TallowCandleParity.BODY_HEIGHT,
                (float) TC4TallowCandleParity.BODY_MAX,
                packedLight, packedOverlay, red, green, blue);

        if (world) {
            for (TC4TallowCandleParity.Drip drip : TC4TallowCandleParity.drips(
                    pos.getX(), pos.getY(), pos.getZ())) {
                float height = drip.heightPixels() / 16.0F;
                if (drip.xAxis()) {
                    renderBox(poseStack, consumer, wax,
                            (5.0F + drip.location()) / 16.0F, 0.0F,
                            (drip.side() ? 5.0F : 10.0F) / 16.0F,
                            (6.0F + drip.location()) / 16.0F, height,
                            (drip.side() ? 6.0F : 11.0F) / 16.0F,
                            packedLight, packedOverlay, red, green, blue);
                } else {
                    renderBox(poseStack, consumer, wax,
                            (drip.side() ? 5.0F : 10.0F) / 16.0F, 0.0F,
                            (5.0F + drip.location()) / 16.0F,
                            (drip.side() ? 6.0F : 11.0F) / 16.0F, height,
                            (6.0F + drip.location()) / 16.0F,
                            packedLight, packedOverlay, red, green, blue);
                }
            }
        }

        renderBox(poseStack, consumer, wick,
                (float) TC4TallowCandleParity.WICK_MIN, (float) TC4TallowCandleParity.WICK_BOTTOM,
                (float) TC4TallowCandleParity.WICK_MIN,
                (float) TC4TallowCandleParity.WICK_MAX, (float) TC4TallowCandleParity.WICK_TOP,
                (float) TC4TallowCandleParity.WICK_MAX,
                packedLight, packedOverlay, 1.0F, 1.0F, 1.0F);
        poseStack.popPose();
    }

    private static TextureAtlasSprite sprite(ResourceLocation texture) {
        return Minecraft.getInstance().getTextureAtlas(InventoryMenu.BLOCK_ATLAS).apply(texture);
    }

    private static void renderBox(PoseStack poseStack, VertexConsumer consumer, TextureAtlasSprite sprite,
                                  float minX, float minY, float minZ,
                                  float maxX, float maxY, float maxZ,
                                  int light, int overlay,
                                  float red, float green, float blue) {
        PoseStack.Pose pose = poseStack.last();
        Matrix4f matrix = pose.pose();
        Matrix3f normal = pose.normal();

        // RenderBlocks 1.7.10 crops every face to the block bounds instead of
        // stretching the entire sprite over each small cuboid. TextureAtlasSprite
        // accepts the same 0..16 interpolation coordinates used by IIcon.
        float uMinX = sprite.getU(minX * 16.0D);
        float uMaxX = sprite.getU(maxX * 16.0D);
        float uMinZ = sprite.getU(minZ * 16.0D);
        float uMaxZ = sprite.getU(maxZ * 16.0D);
        float vTop = sprite.getV((1.0F - maxY) * 16.0D);
        float vBottom = sprite.getV((1.0F - minY) * 16.0D);
        float vMinZ = sprite.getV(minZ * 16.0D);
        float vMaxZ = sprite.getV(maxZ * 16.0D);

        // Up and down: U follows X and V follows Z.
        face(consumer, matrix, normal,
                minX, maxY, minZ, minX, maxY, maxZ, maxX, maxY, maxZ, maxX, maxY, minZ,
                uMinX, vMinZ, uMinX, vMaxZ, uMaxX, vMaxZ, uMaxX, vMinZ,
                light, overlay, red, green, blue, 0.0F, 1.0F, 0.0F);
        face(consumer, matrix, normal,
                minX, minY, maxZ, minX, minY, minZ, maxX, minY, minZ, maxX, minY, maxZ,
                uMinX, vMaxZ, uMinX, vMinZ, uMaxX, vMinZ, uMaxX, vMaxZ,
                light, overlay, red, green, blue, 0.0F, -1.0F, 0.0F);

        // North and south: U follows X and V follows Y with the vanilla vertical flip.
        face(consumer, matrix, normal,
                maxX, minY, minZ, minX, minY, minZ, minX, maxY, minZ, maxX, maxY, minZ,
                uMaxX, vBottom, uMinX, vBottom, uMinX, vTop, uMaxX, vTop,
                light, overlay, red, green, blue, 0.0F, 0.0F, -1.0F);
        face(consumer, matrix, normal,
                minX, minY, maxZ, maxX, minY, maxZ, maxX, maxY, maxZ, minX, maxY, maxZ,
                uMinX, vBottom, uMaxX, vBottom, uMaxX, vTop, uMinX, vTop,
                light, overlay, red, green, blue, 0.0F, 0.0F, 1.0F);

        // West and east: U follows Z and V follows Y.
        face(consumer, matrix, normal,
                minX, minY, minZ, minX, minY, maxZ, minX, maxY, maxZ, minX, maxY, minZ,
                uMinZ, vBottom, uMaxZ, vBottom, uMaxZ, vTop, uMinZ, vTop,
                light, overlay, red, green, blue, -1.0F, 0.0F, 0.0F);
        face(consumer, matrix, normal,
                maxX, minY, maxZ, maxX, minY, minZ, maxX, maxY, minZ, maxX, maxY, maxZ,
                uMaxZ, vBottom, uMinZ, vBottom, uMinZ, vTop, uMaxZ, vTop,
                light, overlay, red, green, blue, 1.0F, 0.0F, 0.0F);
    }

    private static void face(VertexConsumer consumer, Matrix4f matrix, Matrix3f normal,
                             float x1, float y1, float z1,
                             float x2, float y2, float z2,
                             float x3, float y3, float z3,
                             float x4, float y4, float z4,
                             float u1, float v1, float u2, float v2,
                             float u3, float v3, float u4, float v4,
                             int light, int overlay,
                             float red, float green, float blue,
                             float normalX, float normalY, float normalZ) {
        vertex(consumer, matrix, normal, x1, y1, z1, u1, v1, light, overlay, red, green, blue, normalX, normalY, normalZ);
        vertex(consumer, matrix, normal, x2, y2, z2, u2, v2, light, overlay, red, green, blue, normalX, normalY, normalZ);
        vertex(consumer, matrix, normal, x3, y3, z3, u3, v3, light, overlay, red, green, blue, normalX, normalY, normalZ);
        vertex(consumer, matrix, normal, x4, y4, z4, u4, v4, light, overlay, red, green, blue, normalX, normalY, normalZ);
    }

    private static void vertex(VertexConsumer consumer, Matrix4f matrix, Matrix3f normal,
                               float x, float y, float z, float u, float v,
                               int light, int overlay,
                               float red, float green, float blue,
                               float normalX, float normalY, float normalZ) {
        consumer.vertex(matrix, x, y, z)
                .color(Math.round(red * 255.0F), Math.round(green * 255.0F), Math.round(blue * 255.0F), 255)
                .uv(u, v)
                .overlayCoords(overlay == 0 ? OverlayTexture.NO_OVERLAY : overlay)
                .uv2(light)
                .normal(normal, normalX, normalY, normalZ)
                .endVertex();
    }
}
