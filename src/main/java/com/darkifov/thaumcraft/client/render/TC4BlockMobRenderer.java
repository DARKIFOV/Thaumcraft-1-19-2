package com.darkifov.thaumcraft.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.level.block.state.BlockState;

import java.util.function.Supplier;

/** Lightweight Stage144 renderer for new TC4 parity mobs until model ports are complete. */
public class TC4BlockMobRenderer<T extends Entity> extends EntityRenderer<T> {
    private final Supplier<BlockState> state;
    private final float widthScale;
    private final float heightScale;

    public TC4BlockMobRenderer(EntityRendererProvider.Context context, Supplier<BlockState> state, float widthScale, float heightScale) {
        super(context);
        this.state = state;
        this.widthScale = widthScale;
        this.heightScale = heightScale;
        this.shadowRadius = widthScale * 0.45F;
    }

    @Override
    public void render(T entity, float entityYaw, float partialTicks, PoseStack poseStack, MultiBufferSource buffer, int packedLight) {
        poseStack.pushPose();
        poseStack.translate(-0.5D * widthScale, 0.0D, -0.5D * widthScale);
        poseStack.scale(widthScale, heightScale, widthScale);
        Minecraft.getInstance().getBlockRenderer().renderSingleBlock(state.get(), poseStack, buffer, packedLight, OverlayTexture.NO_OVERLAY);
        poseStack.popPose();
        super.render(entity, entityYaw, partialTicks, poseStack, buffer, packedLight);
    }

    @Override
    public ResourceLocation getTextureLocation(T entity) {
        return InventoryMenu.BLOCK_ATLAS;
    }
}
