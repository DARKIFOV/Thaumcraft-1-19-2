package com.darkifov.thaumcraft.client.render;

import com.darkifov.thaumcraft.AspectList;
import com.darkifov.thaumcraft.AspectStack;
import com.darkifov.thaumcraft.ThaumcraftMod;
import com.darkifov.thaumcraft.aura.AuraNodeModifier;
import com.darkifov.thaumcraft.aura.AuraNodeType;
import com.darkifov.thaumcraft.aura.TC4NodeJarRuntime;
import com.darkifov.thaumcraft.client.TC4AuraNodeHudParity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Matrix3f;
import com.mojang.math.Matrix4f;
import com.mojang.math.Vector3f;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

/** Original-style TC4 node-in-a-jar item renderer. */
public final class NodeJarItemRenderer extends BlockEntityWithoutLevelRenderer {
    private static final ResourceLocation GLASS = new ResourceLocation(ThaumcraftMod.MOD_ID, "textures/block/jar_side.png");
    private static final ResourceLocation LID = new ResourceLocation(ThaumcraftMod.MOD_ID, "textures/block/jar_top.png");
    private static NodeJarItemRenderer INSTANCE;

    private NodeJarItemRenderer() {
        super(Minecraft.getInstance().getBlockEntityRenderDispatcher(), Minecraft.getInstance().getEntityModels());
    }

    public static NodeJarItemRenderer instance() {
        if (INSTANCE == null) {
            INSTANCE = new NodeJarItemRenderer();
        }
        return INSTANCE;
    }

    @Override
    public void renderByItem(ItemStack stack, ItemTransforms.TransformType transformType, PoseStack poseStack,
                             MultiBufferSource buffer, int packedLight, int packedOverlay) {
        poseStack.pushPose();
        poseStack.translate(0.5D, 0.5D, 0.5D);
        applyTransform(transformType, poseStack);
        renderJarShell(poseStack, buffer, packedLight);
        CompoundTag root = stack.getTag();
        if (root != null && root.contains(TC4NodeJarRuntime.TAG_NODE_JAR)) {
            renderContainedNode(root.getCompound(TC4NodeJarRuntime.TAG_NODE_JAR), poseStack, buffer);
        }
        poseStack.popPose();
    }

    private static void applyTransform(ItemTransforms.TransformType type, PoseStack poseStack) {
        if (type == ItemTransforms.TransformType.GUI) {
            poseStack.mulPose(Vector3f.XP.rotationDegrees(25.0F));
            poseStack.mulPose(Vector3f.YP.rotationDegrees(35.0F));
            poseStack.scale(0.82F, 0.82F, 0.82F);
        } else if (type.firstPerson()) {
            poseStack.translate(0.0D, -0.08D, 0.0D);
            poseStack.scale(0.72F, 0.72F, 0.72F);
        } else if (type == ItemTransforms.TransformType.GROUND) {
            poseStack.translate(0.0D, -0.18D, 0.0D);
            poseStack.scale(0.62F, 0.62F, 0.62F);
        } else {
            poseStack.scale(0.70F, 0.70F, 0.70F);
        }
    }

    static void renderJarShell(PoseStack poseStack, MultiBufferSource buffer, int light) {
        VertexConsumer glass = buffer.getBuffer(RenderType.entityTranslucent(GLASS));
        renderCuboid(poseStack, glass, -0.36F, -0.43F, -0.36F, 0.36F, 0.38F, 0.36F,
                light, 1.0F, 1.0F, 1.0F, 0.48F);
        VertexConsumer lid = buffer.getBuffer(RenderType.entityCutoutNoCull(LID));
        renderCuboid(poseStack, lid, -0.30F, 0.37F, -0.30F, 0.30F, 0.48F, 0.30F,
                light, 0.72F, 0.62F, 0.42F, 1.0F);
    }

    static void renderContainedNode(CompoundTag nodeTag, PoseStack poseStack, MultiBufferSource buffer) {
        AspectList aspects = new AspectList();
        aspects.load(nodeTag.getCompound("Aspects"));
        AuraNodeType type = AuraNodeType.fromName(nodeTag.getString("NodeType"));
        AuraNodeModifier modifier = AuraNodeModifier.fromName(nodeTag.getString("NodeModifier"));
        if (aspects.isEmpty()) {
            return;
        }

        // ItemNodeRenderer uses the same nanosecond-driven frame as the world
        // renderer. Item NBT must not change animation speed or phase by aspect.
        long nanoTime = System.nanoTime();
        int frame = (int) Math.floorMod(nanoTime / 40_000_000L + 1L,
                TC4AuraNodeHudParity.NODE_SHEET_FRAMES);
        float baseAlpha = 0.50F;
        if (modifier == AuraNodeModifier.BRIGHT) {
            baseAlpha *= 1.50F;
        } else if (modifier == AuraNodeModifier.PALE) {
            baseAlpha *= 0.66F;
        } else if (modifier == AuraNodeModifier.FADING) {
            int viewerTicks = Minecraft.getInstance().player == null ? 0 : Minecraft.getInstance().player.tickCount;
            baseAlpha *= net.minecraft.util.Mth.sin(viewerTicks / 3.0F) * 0.25F + 0.33F;
        }

        int activeAspectCount = 0;
        float average = 0.0F;
        for (AspectStack stack : aspects.all()) {
            if (stack != null && stack.amount() > 0) {
                activeAspectCount++;
                average += stack.amount();
            }
        }
        if (activeAspectCount == 0) {
            return;
        }
        average /= activeAspectCount;

        poseStack.pushPose();
        poseStack.translate(0.0D, -0.10D, 0.0D);
        for (int plane = 0; plane < 3; plane++) {
            poseStack.pushPose();
            if (plane == 1) {
                poseStack.mulPose(Vector3f.YP.rotationDegrees(90.0F));
            } else if (plane == 2) {
                poseStack.mulPose(Vector3f.XP.rotationDegrees(90.0F));
            }

            int count = 0;
            int viewerTicks = Minecraft.getInstance().player == null ? 0 : Minecraft.getInstance().player.tickCount;
            for (AspectStack stack : aspects.all()) {
                if (stack == null || stack.amount() <= 0) {
                    continue;
                }
                float wave = net.minecraft.util.Mth.sin(viewerTicks / Math.max(1.0F, 14.0F - count))
                        * 0.25F + 0.50F;
                float scale = 0.20F + wave * (stack.amount() / 50.0F);
                boolean alphaBlend = stack.aspect().usesAlphaBlend();
                float layerAlpha = baseAlpha * (alphaBlend ? 1.50F : 1.0F) / activeAspectCount;
                renderNodePlane(poseStack, buffer, scale, stack.aspect().argbColor(),
                        layerAlpha, frame, 0, !alphaBlend);
                count++;
            }

            float typeScale = 0.10F + average / 150.0F;
            if (type == AuraNodeType.HUNGRY) {
                typeScale *= 0.75F;
            }
            renderNodePlane(poseStack, buffer, typeScale, 0xFFFFFFFF,
                    baseAlpha, frame, TC4AuraNodeHudParity.stripFor(type),
                    type != AuraNodeType.DARK && type != AuraNodeType.TAINTED);
            poseStack.popPose();
        }
        poseStack.popPose();
    }

    private static void renderNodePlane(PoseStack poseStack, MultiBufferSource buffer, float size,
                                        int color, float alpha, int frame, int strip, boolean additive) {
        float cell = TC4AuraNodeHudParity.NODE_SHEET_CELL_UV;
        float u0 = frame * cell;
        float u1 = (frame + 1) * cell;
        float v0 = strip * cell;
        float v1 = (strip + 1) * cell;
        int r = (color >> 16) & 255;
        int g = (color >> 8) & 255;
        int b = color & 255;
        int a = Math.max(1, Math.min(255, Math.round(alpha * 255.0F)));
        Matrix4f matrix = poseStack.last().pose();
        VertexConsumer consumer = buffer.getBuffer(TC4NodeRenderTypes.node(
                TC4AuraNodeHudParity.ORIGINAL_NODES, additive, false));
        vertex(matrix, consumer, -size,-size,0, r,g,b,a, u0,v1, LightTexture.FULL_BRIGHT);
        vertex(matrix, consumer, size,-size,0, r,g,b,a, u1,v1, LightTexture.FULL_BRIGHT);
        vertex(matrix, consumer, size,size,0, r,g,b,a, u1,v0, LightTexture.FULL_BRIGHT);
        vertex(matrix, consumer, -size,size,0, r,g,b,a, u0,v0, LightTexture.FULL_BRIGHT);
    }

    private static void renderCuboid(PoseStack poseStack, VertexConsumer consumer,
                                     float minX,float minY,float minZ,float maxX,float maxY,float maxZ,
                                     int light,float red,float green,float blue,float alpha) {
        PoseStack.Pose pose = poseStack.last();
        Matrix4f matrix = pose.pose();
        Matrix3f normal = pose.normal();
        quad(matrix,normal,consumer,minX,minY,maxZ,maxX,minY,maxZ,maxX,maxY,maxZ,minX,maxY,maxZ,red,green,blue,alpha,light,0,0,1);
        quad(matrix,normal,consumer,maxX,minY,minZ,minX,minY,minZ,minX,maxY,minZ,maxX,maxY,minZ,red,green,blue,alpha,light,0,0,-1);
        quad(matrix,normal,consumer,maxX,minY,maxZ,maxX,minY,minZ,maxX,maxY,minZ,maxX,maxY,maxZ,red,green,blue,alpha,light,1,0,0);
        quad(matrix,normal,consumer,minX,minY,minZ,minX,minY,maxZ,minX,maxY,maxZ,minX,maxY,minZ,red,green,blue,alpha,light,-1,0,0);
        quad(matrix,normal,consumer,minX,maxY,maxZ,maxX,maxY,maxZ,maxX,maxY,minZ,minX,maxY,minZ,red,green,blue,alpha,light,0,1,0);
        quad(matrix,normal,consumer,minX,minY,minZ,maxX,minY,minZ,maxX,minY,maxZ,minX,minY,maxZ,red,green,blue,alpha,light,0,-1,0);
    }

    private static void quad(Matrix4f matrix, Matrix3f normal, VertexConsumer consumer,
                             float x1,float y1,float z1,float x2,float y2,float z2,float x3,float y3,float z3,float x4,float y4,float z4,
                             float r,float g,float b,float a,int light,float nx,float ny,float nz) {
        vertex(matrix,normal,consumer,x1,y1,z1,r,g,b,a,0,1,light,nx,ny,nz);
        vertex(matrix,normal,consumer,x2,y2,z2,r,g,b,a,1,1,light,nx,ny,nz);
        vertex(matrix,normal,consumer,x3,y3,z3,r,g,b,a,1,0,light,nx,ny,nz);
        vertex(matrix,normal,consumer,x4,y4,z4,r,g,b,a,0,0,light,nx,ny,nz);
    }

    private static void vertex(Matrix4f matrix, VertexConsumer consumer,float x,float y,float z,
                               int r,int g,int b,int a,float u,float v,int light) {
        consumer.vertex(matrix,x,y,z).color(r,g,b,a).uv(u,v).overlayCoords(OverlayTexture.NO_OVERLAY)
                .uv2(light).normal(0,0,1).endVertex();
    }

    private static void vertex(Matrix4f matrix, Matrix3f normal, VertexConsumer consumer,float x,float y,float z,
                               float r,float g,float b,float a,float u,float v,int light,float nx,float ny,float nz) {
        consumer.vertex(matrix,x,y,z).color(r,g,b,a).uv(u,v).overlayCoords(OverlayTexture.NO_OVERLAY)
                .uv2(light).normal(normal,nx,ny,nz).endVertex();
    }
}
