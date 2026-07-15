package com.darkifov.thaumcraft.client.render;

import com.darkifov.thaumcraft.ThaumcraftMod;
import com.darkifov.thaumcraft.entity.TaintCrawlerEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.SpiderModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.EyesLayer;
import net.minecraft.resources.ResourceLocation;

/** TC4 EntityTaintSpider renderer replacing the former block placeholder. */
public class TaintCrawlerRenderer extends MobRenderer<TaintCrawlerEntity, SpiderModel<TaintCrawlerEntity>> {
    private static final ResourceLocation TEXTURE = new ResourceLocation(ThaumcraftMod.MOD_ID, "textures/models/taint_spider.png");
    private static final ResourceLocation EYES = new ResourceLocation(ThaumcraftMod.MOD_ID, "textures/models/taint_spider_eyes.png");

    public TaintCrawlerRenderer(EntityRendererProvider.Context context) {
        super(context, new SpiderModel<>(context.bakeLayer(ModelLayers.SPIDER)), 0.2F);
        addLayer(new TaintEyesLayer(this));
    }

    @Override protected void scale(TaintCrawlerEntity entity, PoseStack poseStack, float partialTickTime) {
        poseStack.scale(0.4F, 0.4F, 0.4F);
    }

    @Override public ResourceLocation getTextureLocation(TaintCrawlerEntity entity) { return TEXTURE; }

    private static final class TaintEyesLayer extends EyesLayer<TaintCrawlerEntity, SpiderModel<TaintCrawlerEntity>> {
        private TaintEyesLayer(RenderLayerParent<TaintCrawlerEntity, SpiderModel<TaintCrawlerEntity>> parent) { super(parent); }
        @Override public RenderType renderType() { return RenderType.eyes(EYES); }
    }
}
