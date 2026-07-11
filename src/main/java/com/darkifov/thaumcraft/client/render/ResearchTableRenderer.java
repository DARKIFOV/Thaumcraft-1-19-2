package com.darkifov.thaumcraft.client.render;

import com.darkifov.thaumcraft.ThaumcraftMod;
import com.darkifov.thaumcraft.block.ResearchNoteItem;
import com.darkifov.thaumcraft.block.ResearchTableBlock;
import com.darkifov.thaumcraft.block.ScribingToolsItem;
import com.darkifov.thaumcraft.blockentity.ResearchTableBlockEntity;
import com.darkifov.thaumcraft.research.ResearchNoteState;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Matrix3f;
import com.mojang.math.Matrix4f;
import com.mojang.math.Vector3f;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

/**
 * Forge 1.19.2 transcription of TC4's ModelResearchTable and
 * TileResearchTableRenderer.
 *
 * <p>The previous JSON model was an approximation that duplicated a normal
 * table, sampled unrelated block textures and could not react to the two real
 * inventory slots. This renderer owns the complete world visual, uses the
 * original 128x64 model UV layout (including its 3x restable2 sheet) and keeps the exact 1.7.10 model-box
 * dimensions, while the data and rendering API remain native 1.19.2 Forge.</p>
 */
public final class ResearchTableRenderer implements BlockEntityRenderer<ResearchTableBlockEntity> {
    private static final ResourceLocation TABLE = new ResourceLocation(
            ThaumcraftMod.MOD_ID, "textures/original/thaumcraft4/models/restable.png");
    private static final ResourceLocation SCROLL = new ResourceLocation(
            ThaumcraftMod.MOD_ID, "textures/original/thaumcraft4/models/restable2.png");
    private static final ResourceLocation PARCHMENT = new ResourceLocation(
            ThaumcraftMod.MOD_ID, "textures/original/thaumcraft4/misc/parchment.png");
    private static final ResourceLocation QUILL = new ResourceLocation(
            ThaumcraftMod.MOD_ID, "textures/original/thaumcraft4/blocks/tablequill.png");

    public ResearchTableRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public void render(ResearchTableBlockEntity table, float partialTick, PoseStack poseStack,
                       MultiBufferSource buffer, int packedLight, int packedOverlay) {
        Direction partner = table.getBlockState().hasProperty(ResearchTableBlock.FACING)
                ? table.getBlockState().getValue(ResearchTableBlock.FACING)
                : Direction.EAST;

        poseStack.pushPose();
        // Exact original tile transform: centre of the active half, top at Y=1,
        // and old ModelRenderer's positive Y points down after the 180 degree X turn.
        poseStack.translate(0.5D, 1.0D, 0.5D);
        poseStack.mulPose(Vector3f.XP.rotationDegrees(180.0F));
        poseStack.mulPose(Vector3f.YP.rotationDegrees(rotationFor(partner)));

        VertexConsumer tableConsumer = buffer.getBuffer(RenderType.entityCutoutNoCull(TABLE));
        renderBaseModel(poseStack, tableConsumer, packedLight);

        ItemStack tools = table.getItem(ResearchTableBlockEntity.SLOT_SCRIBING_TOOLS);
        if (tools.getItem() instanceof ScribingToolsItem) {
            renderInkwell(poseStack, tableConsumer, packedLight);
            renderQuill(poseStack, buffer, packedLight);
        }

        renderParchmentStack(poseStack, buffer, packedLight);

        ItemStack note = table.getItem(ResearchTableBlockEntity.SLOT_RESEARCH_NOTE);
        if (note.getItem() instanceof ResearchNoteItem) {
            renderResearchScroll(note, poseStack, buffer, packedLight);
        }
        poseStack.popPose();
    }

    private static float rotationFor(Direction direction) {
        return switch (direction) {
            case NORTH -> 270.0F;
            case SOUTH -> 90.0F;
            case WEST -> 180.0F;
            default -> 0.0F; // EAST is the unrotated original ModelResearchTable orientation.
        };
    }

    private static void renderBaseModel(PoseStack poseStack, VertexConsumer consumer, int light) {
        // ModelResearchTable.java, texture 128x64, mirror=true on every part.
        renderModelBox(poseStack, consumer, 0, 0,
                0, 0, 0, 32, 4, 16,
                -8, 0, -8, 128, 64, light, 0xFFFFFF, 255);
        renderModelBox(poseStack, consumer, 0, 24,
                0, 0, 0, 4, 12, 4,
                -6, 4, -6, 128, 64, light, 0xFFFFFF, 255);
        renderModelBox(poseStack, consumer, 0, 24,
                0, 0, 0, 4, 12, 4,
                -6, 4, 2, 128, 64, light, 0xFFFFFF, 255);
        renderModelBox(poseStack, consumer, 0, 24,
                0, 0, 0, 4, 12, 4,
                18, 4, -6, 128, 64, light, 0xFFFFFF, 255);
        renderModelBox(poseStack, consumer, 0, 24,
                0, 0, 0, 4, 12, 4,
                18, 4, 2, 128, 64, light, 0xFFFFFF, 255);
        renderModelBox(poseStack, consumer, 24, 24,
                0, 0, 0, 24, 4, 4,
                -4, 10, -2, 128, 64, light, 0xFFFFFF, 255);
    }

    private static void renderInkwell(PoseStack poseStack, VertexConsumer consumer, int light) {
        renderModelBox(poseStack, consumer, 0, 44,
                0, 0, 0, 3, 2, 3,
                -6, -2, 3, 128, 64, light, 0xFFFFFF, 230);
    }

    private static void renderQuill(PoseStack poseStack, MultiBufferSource buffer, int light) {
        poseStack.pushPose();
        // TileResearchTableRenderer lines 55-68.
        poseStack.mulPose(Vector3f.YP.rotationDegrees(-90.0F));
        poseStack.mulPose(Vector3f.XP.rotationDegrees(180.0F));
        poseStack.translate(-0.17D, 0.10D, -0.15D);
        poseStack.mulPose(Vector3f.YP.rotationDegrees(15.0F));
        poseStack.scale(0.50F, 0.50F, 0.50F);

        VertexConsumer consumer = buffer.getBuffer(RenderType.entityCutoutNoCull(QUILL));
        renderSpritePrism(poseStack, consumer, light, 0.025F);
        poseStack.popPose();
    }

    private static void renderParchmentStack(PoseStack poseStack, MultiBufferSource buffer, int light) {
        VertexConsumer consumer = buffer.getBuffer(RenderType.entityTranslucent(PARCHMENT));
        for (int layer = 0; layer < 6; layer++) {
            poseStack.pushPose();
            // Exact original stack spacing, tilt and scale.
            poseStack.translate(0.10D, -0.01D - layer * 0.015D, 0.35D);
            poseStack.mulPose(Vector3f.XN.rotationDegrees(90.0F));
            poseStack.mulPose(Vector3f.ZP.rotationDegrees(15.0F + (layer % 3) * 2.0F));
            poseStack.scale(0.50F, 0.60F, 0.60F);
            renderCenteredQuad(poseStack, consumer, light, 0xFFFFFF, 255);
            poseStack.popPose();
        }
    }

    private static void renderResearchScroll(ItemStack note, PoseStack poseStack,
                                             MultiBufferSource buffer, int light) {
        VertexConsumer consumer = buffer.getBuffer(RenderType.entityCutoutNoCull(SCROLL));

        poseStack.pushPose();
        // ScrollTube: addBox(-21,-0.5,-8, 8,2,2), rotation point (-2,-2,2),
        // and the original model's literal ten-radian Y rotation.
        poseStack.translate(-2.0F / 16.0F, -2.0F / 16.0F, 2.0F / 16.0F);
        poseStack.mulPose(Vector3f.YP.rotationDegrees(572.9578F));
        renderModelBox(poseStack, consumer, 0, 0,
                -21, -0.5F, -8, 8, 2, 2,
                0, 0, 0, 128, 64, light, 0xFFFFFF, 255);
        poseStack.popPose();

        int color = researchColor(note);
        poseStack.pushPose();
        poseStack.scale(1.20F, 1.20F, 1.20F);
        poseStack.translate(-2.0F / 16.0F, -2.0F / 16.0F, 2.0F / 16.0F);
        poseStack.mulPose(Vector3f.YP.rotationDegrees(572.9578F));
        renderModelBox(poseStack, consumer, 0, 4,
                -15.1F, -0.275F, -6.75F, 1, 2, 2,
                0, 0, 0, 128, 64, light, color, 255);
        poseStack.popPose();
    }

    private static int researchColor(ItemStack note) {
        CompoundTag root = ResearchNoteState.root(note);
        int color = root.getInt("color");
        if (color == 0 && note.getTag() != null) {
            color = note.getTag().getInt("color");
        }
        return color == 0 ? 0x999999 : color;
    }

    /**
     * ModelRenderer/ModelBox adapter. Box coordinates and rotation points are in
     * original model pixels; UVs use the logical texture size declared by TC4.
     */
    private static void renderModelBox(PoseStack poseStack, VertexConsumer consumer,
                                       int textureU, int textureV,
                                       float boxX, float boxY, float boxZ,
                                       float width, float height, float depth,
                                       float pivotX, float pivotY, float pivotZ,
                                       float textureWidth, float textureHeight,
                                       int light, int color, int alpha) {
        final float px = 1.0F / 16.0F;
        float minX = (pivotX + boxX) * px;
        float minY = (pivotY + boxY) * px;
        float minZ = (pivotZ + boxZ) * px;
        float maxX = (pivotX + boxX + width) * px;
        float maxY = (pivotY + boxY + height) * px;
        float maxZ = (pivotZ + boxZ + depth) * px;

        float u = textureU;
        float v = textureV;
        float w = width;
        float h = height;
        float d = depth;
        int red = (color >> 16) & 255;
        int green = (color >> 8) & 255;
        int blue = color & 255;
        PoseStack.Pose pose = poseStack.last();

        // Same six-face atlas net used by 1.7.10 ModelBox. ModelResearchTable
        // set mirror=true, so the winding is reversed after assigning UVs.
        modelQuad(pose, consumer,
                maxX, minY, minZ, maxX, maxY, minZ, maxX, maxY, maxZ, maxX, minY, maxZ,
                (u + d + w) / textureWidth, (v + d) / textureHeight,
                (u + d + w + d) / textureWidth, (v + d + h) / textureHeight,
                1, 0, 0, light, red, green, blue, alpha);
        modelQuad(pose, consumer,
                minX, minY, maxZ, minX, maxY, maxZ, minX, maxY, minZ, minX, minY, minZ,
                u / textureWidth, (v + d) / textureHeight,
                (u + d) / textureWidth, (v + d + h) / textureHeight,
                -1, 0, 0, light, red, green, blue, alpha);
        modelQuad(pose, consumer,
                maxX, minY, maxZ, minX, minY, maxZ, minX, minY, minZ, maxX, minY, minZ,
                (u + d) / textureWidth, v / textureHeight,
                (u + d + w) / textureWidth, (v + d) / textureHeight,
                0, -1, 0, light, red, green, blue, alpha);
        modelQuad(pose, consumer,
                maxX, maxY, minZ, minX, maxY, minZ, minX, maxY, maxZ, maxX, maxY, maxZ,
                (u + d + w) / textureWidth, v / textureHeight,
                (u + d + w + w) / textureWidth, (v + d) / textureHeight,
                0, 1, 0, light, red, green, blue, alpha);
        modelQuad(pose, consumer,
                minX, minY, minZ, minX, maxY, minZ, maxX, maxY, minZ, maxX, minY, minZ,
                (u + d) / textureWidth, (v + d) / textureHeight,
                (u + d + w) / textureWidth, (v + d + h) / textureHeight,
                0, 0, -1, light, red, green, blue, alpha);
        modelQuad(pose, consumer,
                maxX, minY, maxZ, maxX, maxY, maxZ, minX, maxY, maxZ, minX, minY, maxZ,
                (u + d + w + d) / textureWidth, (v + d) / textureHeight,
                (u + d + w + d + w) / textureWidth, (v + d + h) / textureHeight,
                0, 0, 1, light, red, green, blue, alpha);
    }

    private static void renderSpritePrism(PoseStack poseStack, VertexConsumer consumer,
                                          int light, float thickness) {
        PoseStack.Pose pose = poseStack.last();
        float z0 = -thickness * 0.5F;
        float z1 = thickness * 0.5F;
        // Front/back preserve transparent pixels in the original quill icon.
        modelQuad(pose, consumer, 0, 0, z1, 1, 0, z1, 1, 1, z1, 0, 1, z1,
                0, 0, 1, 1, 0, 0, 1, light, 255, 255, 255, 255);
        modelQuad(pose, consumer, 1, 0, z0, 0, 0, z0, 0, 1, z0, 1, 1, z0,
                0, 0, 1, 1, 0, 0, -1, light, 255, 255, 255, 255);
    }

    private static void renderCenteredQuad(PoseStack poseStack, VertexConsumer consumer,
                                           int light, int color, int alpha) {
        int red = (color >> 16) & 255;
        int green = (color >> 8) & 255;
        int blue = color & 255;
        PoseStack.Pose pose = poseStack.last();
        modelQuad(pose, consumer,
                -0.5F, -0.5F, 0, 0.5F, -0.5F, 0,
                0.5F, 0.5F, 0, -0.5F, 0.5F, 0,
                0, 0, 1, 1, 0, 0, 1, light, red, green, blue, alpha);
    }

    private static void modelQuad(PoseStack.Pose pose, VertexConsumer consumer,
                                  float x1, float y1, float z1,
                                  float x2, float y2, float z2,
                                  float x3, float y3, float z3,
                                  float x4, float y4, float z4,
                                  float u0, float v0, float u1, float v1,
                                  float normalX, float normalY, float normalZ,
                                  int light, int red, int green, int blue, int alpha) {
        vertex(pose.pose(), pose.normal(), consumer, x1, y1, z1, u0, v1,
                normalX, normalY, normalZ, light, red, green, blue, alpha);
        vertex(pose.pose(), pose.normal(), consumer, x2, y2, z2, u1, v1,
                normalX, normalY, normalZ, light, red, green, blue, alpha);
        vertex(pose.pose(), pose.normal(), consumer, x3, y3, z3, u1, v0,
                normalX, normalY, normalZ, light, red, green, blue, alpha);
        vertex(pose.pose(), pose.normal(), consumer, x4, y4, z4, u0, v0,
                normalX, normalY, normalZ, light, red, green, blue, alpha);
    }

    private static void vertex(Matrix4f matrix, Matrix3f normalMatrix, VertexConsumer consumer,
                               float x, float y, float z, float u, float v,
                               float normalX, float normalY, float normalZ,
                               int light, int red, int green, int blue, int alpha) {
        consumer.vertex(matrix, x, y, z)
                .color(red, green, blue, alpha)
                .uv(u, v)
                .overlayCoords(OverlayTexture.NO_OVERLAY)
                .uv2(light)
                .normal(normalMatrix, normalX, normalY, normalZ)
                .endVertex();
    }

    @Override
    public boolean shouldRenderOffScreen(ResearchTableBlockEntity blockEntity) {
        // The original model spans the active and partner table positions.
        return true;
    }
}
