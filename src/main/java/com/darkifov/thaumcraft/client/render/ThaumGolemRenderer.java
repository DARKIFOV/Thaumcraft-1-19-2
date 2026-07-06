package com.darkifov.thaumcraft.client.render;

import com.darkifov.thaumcraft.ThaumcraftMod;
import com.darkifov.thaumcraft.entity.ThaumGolemEntity;
import com.darkifov.thaumcraft.golem.GolemDecorationType;
import com.darkifov.thaumcraft.golem.GolemMaterial;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

public class ThaumGolemRenderer extends EntityRenderer<ThaumGolemEntity> {
    public ThaumGolemRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.shadowRadius = 0.35F;
    }

    @Override
    public void render(ThaumGolemEntity entity, float entityYaw, float partialTicks, PoseStack poseStack,
                       MultiBufferSource buffer, int packedLight) {
        BlockState state = materialState(entity.getGolemMaterial());
        float scale = entity.getGolemMaterial() == GolemMaterial.STRAW ? 0.78F : entity.getGolemMaterial() == GolemMaterial.IRON || entity.getGolemMaterial() == GolemMaterial.THAUMIUM ? 0.96F : 0.88F;
        float bob = (float) Math.sin((entity.tickCount + partialTicks) * 0.18F) * 0.025F;
        float armSwing = (float) Math.sin((entity.tickCount + partialTicks) * 0.42F) * 0.08F;

        poseStack.pushPose();
        poseStack.translate(0.0D, bob, 0.0D);
        poseStack.scale(scale, scale, scale);

        renderPart(state, poseStack, buffer, packedLight, -0.24F, 0.20F, -0.18F, 0.48F, 0.56F, 0.36F); // body
        renderPart(state, poseStack, buffer, packedLight, -0.18F, 0.78F, -0.16F, 0.36F, 0.30F, 0.32F); // head
        renderPart(state, poseStack, buffer, packedLight, -0.42F, 0.30F + armSwing, -0.13F, 0.16F, 0.38F, 0.26F); // left arm
        renderPart(state, poseStack, buffer, packedLight, 0.26F, 0.30F - armSwing, -0.13F, 0.16F, 0.38F, 0.26F); // right arm
        renderPart(state, poseStack, buffer, packedLight, -0.18F, 0.00F, -0.12F, 0.14F, 0.22F, 0.24F); // left leg
        renderPart(state, poseStack, buffer, packedLight, 0.04F, 0.00F, -0.12F, 0.14F, 0.22F, 0.24F); // right leg

        if (entity.hasDecoration(GolemDecorationType.TOP_HAT)) {
            renderPart(Blocks.BLACK_WOOL.defaultBlockState(), poseStack, buffer, packedLight, -0.16F, 1.08F, -0.14F, 0.32F, 0.14F, 0.28F);
        }
        if (entity.hasDecoration(GolemDecorationType.FEZ)) {
            renderPart(Blocks.RED_WOOL.defaultBlockState(), poseStack, buffer, packedLight, -0.13F, 1.05F, -0.13F, 0.26F, 0.18F, 0.26F);
        }
        if (entity.hasDecoration(GolemDecorationType.GLASSES) || entity.hasDecoration(GolemDecorationType.VISOR)) {
            renderPart(Blocks.LIGHT_BLUE_STAINED_GLASS.defaultBlockState(), poseStack, buffer, packedLight, -0.19F, 0.88F, -0.18F, 0.38F, 0.05F, 0.04F);
        }
        if (entity.hasDecoration(GolemDecorationType.BOWTIE)) {
            renderPart(Blocks.PURPLE_WOOL.defaultBlockState(), poseStack, buffer, packedLight, -0.10F, 0.70F, -0.20F, 0.20F, 0.06F, 0.05F);
        }
        if (entity.hasDecoration(GolemDecorationType.ARMOR)) {
            renderPart(Blocks.IRON_BLOCK.defaultBlockState(), poseStack, buffer, packedLight, -0.26F, 0.34F, -0.20F, 0.52F, 0.20F, 0.40F);
        }
        if (entity.hasDecoration(GolemDecorationType.WIRELESS_BACKPACK)) {
            renderPart(Blocks.GOLD_BLOCK.defaultBlockState(), poseStack, buffer, packedLight, -0.14F, 0.38F, 0.20F, 0.28F, 0.28F, 0.12F);
        }

        poseStack.popPose();
        super.render(entity, entityYaw, partialTicks, poseStack, buffer, packedLight);
    }

    private void renderPart(BlockState state, PoseStack poseStack, MultiBufferSource buffer, int packedLight,
                            float x, float y, float z, float sx, float sy, float sz) {
        poseStack.pushPose();
        poseStack.translate(x, y, z);
        poseStack.scale(sx, sy, sz);
        Minecraft.getInstance().getBlockRenderer().renderSingleBlock(
                state,
                poseStack,
                buffer,
                packedLight,
                OverlayTexture.NO_OVERLAY
        );
        poseStack.popPose();
    }

    private BlockState materialState(GolemMaterial material) {
        return switch (material) {
            case STRAW -> Blocks.HAY_BLOCK.defaultBlockState();
            case WOOD -> Blocks.OAK_PLANKS.defaultBlockState();
            case TALLOW -> Blocks.BONE_BLOCK.defaultBlockState();
            case CLAY -> Blocks.CLAY.defaultBlockState();
            case FLESH -> Blocks.NETHERRACK.defaultBlockState();
            case STONE -> Blocks.STONE_BRICKS.defaultBlockState();
            case IRON -> Blocks.IRON_BLOCK.defaultBlockState();
            case THAUMIUM -> ThaumcraftMod.ARCANE_STONE_BRICKS.get().defaultBlockState();
        };
    }

    @Override
    public ResourceLocation getTextureLocation(ThaumGolemEntity entity) {
        return InventoryMenu.BLOCK_ATLAS;
    }
}
