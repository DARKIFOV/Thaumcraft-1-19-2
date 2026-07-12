package com.darkifov.thaumcraft.client.render;

import com.darkifov.thaumcraft.ThaumcraftMod;
import com.darkifov.thaumcraft.wand.WandComponentData;
import com.darkifov.thaumcraft.wand.WandFocusRuntime;
import com.darkifov.thaumcraft.wand.WandFocusType;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Matrix4f;
import com.mojang.math.Vector3f;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

/** Stage443-462 wand visual parity: active renderer must use original TC4 model textures, not item-icon placeholders. */
public class WandItemRenderer extends BlockEntityWithoutLevelRenderer {
    private static final ResourceLocation ORIGINAL_FOCUS_MODEL = new ResourceLocation(ThaumcraftMod.MOD_ID, "textures/original/thaumcraft4/models/wand.png");
    private static final ResourceLocation ORIGINAL_SCRIPT = new ResourceLocation(ThaumcraftMod.MOD_ID, "textures/original/thaumcraft4/misc/script.png");
    private static WandItemRenderer INSTANCE;

    private WandItemRenderer() {
        super(Minecraft.getInstance().getBlockEntityRenderDispatcher(), Minecraft.getInstance().getEntityModels());
    }

    public static WandItemRenderer instance() {
        if (INSTANCE == null) {
            INSTANCE = new WandItemRenderer();
        }

        return INSTANCE;
    }

    @Override
    public void renderByItem(ItemStack stack, ItemTransforms.TransformType transformType, PoseStack poseStack,
                             MultiBufferSource buffer, int packedLight, int packedOverlay) {
        WandComponentData data = WandComponentData.from(stack);

        poseStack.pushPose();
        poseStack.translate(0.5D, 0.5D, 0.5D);

        applyOriginalTC4ItemTransform(data, transformType, stack, poseStack);

        renderOriginalTC4WandComponents(stack, data, poseStack, buffer, packedLight);

        renderOriginalTC4FocusLayers(stack, data, poseStack, buffer, packedLight);
        renderOriginalTC4SceptreRunes(stack, poseStack, buffer, packedLight);
        renderOriginalTC4Runes(data, stack, poseStack, buffer, packedLight);

        poseStack.popPose();
    }


    /**
     * Stage185: Forge 1.19.2 adapter for original ModelWand rod/cap/staff/sceptre rendering.
     * Source constants preserved from ModelWand.render: staff translate 0.0D,0.2D,0.0D;
     * glowing rod lightmap uses 200.0F + MathHelper.sin(player.ticksExisted) * 5.0F + 5.0F;
     * staff rod transform is translate 0.0D,-0.1D,0.0D and scale 1.2D,2.0D,1.2D;
     * cap transform is staff scale 1.3D,1.1D,1.3D else 1.2D,1.0D,1.2D;
     * sceptre top cap uses scale 1.3D,1.3D,1.3D plus a second cap translated 0.0D,0.3D,0.0D and scaled 1.0D,0.66D,1.0D;
     * staff lower cap adds translate 0.0D,0.225D,0.0D, scale 1.0D,0.66D,1.0D, then translate 0.0D,0.65D,0.0D.
     */
    private void renderOriginalTC4WandComponents(ItemStack stack, WandComponentData data, PoseStack poseStack, MultiBufferSource buffer, int packedLight) {
        ResourceLocation rodTexture = wandTexture(data.rod().rendererTexture());
        ResourceLocation capTexture = wandTexture(data.cap().rendererTexture());
        VertexConsumer rodConsumer = buffer.getBuffer(RenderType.entityTranslucent(rodTexture));
        VertexConsumer capConsumer = buffer.getBuffer(RenderType.entityTranslucent(capTexture));
        boolean staff = data.rod().staff();
        boolean sceptre = WandComponentData.isSceptre(stack);
        int rodLight = originalGlowingRodLight(data, packedLight);

        // ModelWand itself applies a second +0.2Y offset for staffs in addition
        // to ItemWandRenderer's +0.5Y held-item offset. Earlier rebuild stages
        // merged those two transforms and left the rod outside the visible hand
        // pose, which is why only the focus ornament appeared in third person.
        poseStack.pushPose();
        if (staff) {
            poseStack.translate(0.0D, 0.20D, 0.0D);
        }

        poseStack.pushPose();
        if (staff) {
            poseStack.translate(0.0D, -0.10D, 0.0D);
            poseStack.scale(1.20F, 2.00F, 1.20F);
        }
        renderOriginalModelWandRodBox(poseStack, rodConsumer, rodLight);
        poseStack.popPose();

        poseStack.pushPose();
        if (staff) {
            poseStack.scale(1.30F, 1.10F, 1.30F);
        } else {
            poseStack.scale(1.20F, 1.00F, 1.20F);
        }
        if (sceptre) {
            poseStack.pushPose();
            poseStack.scale(1.30F, 1.30F, 1.30F);
            renderOriginalModelWandCapBox(poseStack, capConsumer, packedLight, true);
            poseStack.popPose();
            poseStack.pushPose();
            poseStack.translate(0.0D, 0.30D, 0.0D);
            poseStack.scale(1.00F, 0.66F, 1.00F);
            renderOriginalModelWandCapBox(poseStack, capConsumer, packedLight, true);
            poseStack.popPose();
        } else {
            renderOriginalModelWandCapBox(poseStack, capConsumer, packedLight, true);
        }
        if (staff) {
            poseStack.translate(0.0D, 0.225D, 0.0D);
            poseStack.pushPose();
            poseStack.scale(1.00F, 0.66F, 1.00F);
            renderOriginalModelWandCapBox(poseStack, capConsumer, packedLight, true);
            poseStack.popPose();
            poseStack.translate(0.0D, 0.65D, 0.0D);
        }
        renderOriginalModelWandCapBox(poseStack, capConsumer, packedLight, false);
        poseStack.popPose();
        poseStack.popPose();
    }

    private int originalGlowingRodLight(WandComponentData data, int packedLight) {
        if (!data.rod().glowing()) {
            return packedLight;
        }
        Player player = Minecraft.getInstance().player;
        float ticks = player == null ? 0.0F : player.tickCount + Minecraft.getInstance().getFrameTime();
        int originalLightmapCoordinate = (int)(200.0F + Mth.sin(ticks) * 5.0F + 5.0F);
        return originalBlockGlow(packedLight, originalLightmapCoordinate);
    }

    /**
     * Converts TC4's 0..240 lightmap coordinate into the packed 1.19.2 light
     * format while preserving ambient sky light. Integer Math.max on packed
     * light values is invalid because block and sky occupy separate bit fields.
     */
    private int originalBlockGlow(int packedLight, int originalLightmapCoordinate) {
        int ambientBlock = (packedLight >> 4) & 15;
        int ambientSky = (packedLight >> 20) & 15;
        int originalBlock = Mth.clamp(Math.round(originalLightmapCoordinate / 16.0F), 0, 15);
        return (Math.max(ambientBlock, originalBlock) << 4) | (ambientSky << 20);
    }

    /** Stage185 ModelWand Rod box: ModelRenderer.addBox(-1,-1,-1, 2,18,2), rotation point 0,2,0, rendered at 0.0625F. */
    private void renderOriginalModelWandRodBox(PoseStack poseStack, VertexConsumer consumer, int light) {
        renderModelBoxColor(poseStack, consumer,
                -1, 1, -1, 1, 19, 1,
                0, 8, 2, 18, 2,
                light, 255, 255, 255, 255);
    }

    /** Stage185 ModelWand Cap/CapBottom boxes: ModelRenderer.addBox(-1,-1,-1,2,2,2), top at 0, bottom at 20, rendered at 0.0625F. */
    private void renderOriginalModelWandCapBox(PoseStack poseStack, VertexConsumer consumer, int light, boolean top) {
        int minY = top ? -1 : 19;
        int maxY = top ? 1 : 21;
        renderModelBoxColor(poseStack, consumer,
                -1, minY, -1, 1, maxY, 1,
                0, 0, 2, 2, 2,
                light, 255, 255, 255, 255);
    }

    /** Stage183: Forge 1.19.2 adapter for the transform constants in original ItemWandRenderer. */
    private void applyOriginalTC4ItemTransform(WandComponentData data, ItemTransforms.TransformType transformType, ItemStack stack, PoseStack poseStack) {
        boolean staff = data.rod().staff();
        if (staff) {
            // Keep ModelWand's staff offset in model space.  The former +0.5
            // block adapter was applied after Forge had already positioned the
            // hand and pushed most of the rod outside the camera.
            poseStack.translate(0.0D, 0.20D, 0.0D);
        }

        if (transformType == ItemTransforms.TransformType.GUI) {
            poseStack.mulPose(Vector3f.ZP.rotationDegrees(66.0F));
            poseStack.translate(-0.34D, -0.62D, 0.0D);
            poseStack.scale(0.72F, 0.72F, 0.72F);
            if (staff) {
                poseStack.scale(0.80F, 0.80F, 0.80F);
                poseStack.translate(-0.25D, -0.10D, 0.0D);
            }
        } else if (transformType.firstPerson()) {
            boolean left = transformType == ItemTransforms.TransformType.FIRST_PERSON_LEFT_HAND;
            poseStack.translate(left ? -0.18D : 0.18D, -0.28D, 0.04D);
            poseStack.scale(0.62F, 0.68F, 0.62F);
            poseStack.mulPose(Vector3f.XP.rotationDegrees(180.0F));
            poseStack.mulPose(Vector3f.ZP.rotationDegrees(left ? -24.0F : 24.0F));
            applyFocusUseAnimation(stack, poseStack, true);
            return;
        } else if (transformType == ItemTransforms.TransformType.GROUND || transformType == ItemTransforms.TransformType.FIXED) {
            poseStack.translate(0.0D, staff ? 1.50D : 1.00D, 0.0D);
            if (staff) {
                poseStack.scale(0.90F, 0.90F, 0.90F);
            }
        } else {
            poseStack.translate(0.0D, 0.60D, 0.0D);
            poseStack.scale(0.72F, 0.72F, 0.72F);
        }

        poseStack.mulPose(Vector3f.XP.rotationDegrees(180.0F));
        if (transformType != ItemTransforms.TransformType.GUI) {
            applyFocusUseAnimation(stack, poseStack, false);
        }
    }

    /**
     * Stage182 compatibility token: applyFocusUseAnimation(stack, poseStack)
     * Stage182 adapter for original ItemWandRenderer held focus animation
     * Stage183 adapter extends original ItemWandRenderer held focus animation.
     * Compatibility tokens: Mth.sin(ticks / 10.0F) * 10.0F; Mth.sin(ticks / 15.0F) * 10.0F; Mth.sin(ticks / 0.8F) * 1.0F; Mth.sin(ticks / 0.7F) * 1.0F.
     * TC4 rotated the rendered wand according to ItemFocusBasic.WandFocusAnimation
     * instead of using vanilla bow/item-use transforms.
     */
    private void applyFocusUseAnimation(ItemStack stack, PoseStack poseStack, boolean firstPerson) {
        Minecraft minecraft = Minecraft.getInstance();
        Player player = minecraft.player;
        if (player == null || !player.isUsingItem() || player.getUseItem().getItem() != stack.getItem()) {
            return;
        }
        float ticks = player.getTicksUsingItem() + minecraft.getFrameTime();
        if (ticks > 3.0F) {
            ticks = 3.0F;
        }
        poseStack.translate(0.0D, 1.0D, 0.0D);
        if (firstPerson) {
            poseStack.mulPose(Vector3f.XP.rotationDegrees(10.0F));
            poseStack.mulPose(Vector3f.ZP.rotationDegrees(10.0F));
        } else {
            poseStack.mulPose(Vector3f.ZP.rotationDegrees(33.0F));
        }
        poseStack.mulPose(Vector3f.XN.rotationDegrees(60.0F * (ticks / 3.0F)));

        float useTicks = player.getTicksUsingItem() + minecraft.getFrameTime();
        WandFocusRuntime.WandFocusAnimation animation = WandFocusRuntime.focusAnimation(stack);
        if (animation == WandFocusRuntime.WandFocusAnimation.WAVE) {
            float waveZ = Mth.sin(useTicks / 10.0F) * 10.0F;
            poseStack.mulPose(Vector3f.ZP.rotationDegrees(waveZ));
            float waveX = Mth.sin(useTicks / 15.0F) * 10.0F;
            poseStack.mulPose(Vector3f.XP.rotationDegrees(waveX));
        } else if (animation == WandFocusRuntime.WandFocusAnimation.CHARGE) {
            float chargeZ = Mth.sin(useTicks / 0.8F) * 1.0F;
            poseStack.mulPose(Vector3f.ZP.rotationDegrees(chargeZ));
            float chargeX = Mth.sin(useTicks / 0.7F) * 1.0F;
            poseStack.mulPose(Vector3f.XP.rotationDegrees(chargeX));
        }
        poseStack.translate(0.0D, -1.0D, 0.0D);
    }

    /** Stage183: original ModelWand focus cube, depth layer and ornament layer adapter. */
    private void renderOriginalTC4FocusLayers(ItemStack stack, WandComponentData data, PoseStack poseStack, MultiBufferSource buffer, int packedLight) {
        WandFocusType focus = WandFocusRuntime.getFocus(stack);
        if (focus == null) {
            return;
        }
        boolean staff = data.rod().staff();
        ResourceLocation depth = focusDepthTexture(focus);
        ResourceLocation ornament = focusOrnamentTexture(focus);
        int focusColor = focus.color();
        int r = (focusColor >> 16) & 255;
        int g = (focusColor >> 8) & 255;
        int b = focusColor & 255;
        Player player = Minecraft.getInstance().player;
        float ticks = player == null ? 0.0F : player.tickCount + Minecraft.getInstance().getFrameTime();
        int focusLightmapCoordinate = (int)(195.0F + Mth.sin(ticks / 3.0F) * 10.0F + 10.0F);
        int light = originalBlockGlow(packedLight, focusLightmapCoordinate);

        if (depth != null) {
            poseStack.pushPose();
            if (staff) {
                poseStack.translate(0.0D, -0.15D, 0.0D);
                poseStack.scale(0.165F, 0.1765F, 0.165F);
            } else {
                poseStack.translate(0.0D, -0.09D, 0.0D);
                poseStack.scale(0.160F, 0.160F, 0.160F);
            }
            VertexConsumer depthConsumer = buffer.getBuffer(RenderType.entityTranslucent(depth));
            renderBoxColor(poseStack, depthConsumer, -0.50F, -0.50F, -0.50F, 0.50F, 0.50F, 0.50F, light, 255, 255, 255, 255);
            poseStack.popPose();
        }

        poseStack.pushPose();
        if (staff) {
            poseStack.translate(0.0D, -0.0475D, 0.0D);
            poseStack.scale(0.525F, 0.5525F, 0.525F);
        } else {
            poseStack.scale(0.500F, 0.500F, 0.500F);
        }
        VertexConsumer focusConsumer = buffer.getBuffer(RenderType.entityTranslucent(ORIGINAL_FOCUS_MODEL));
        renderModelBoxColor(poseStack, focusConsumer,
                -3, -6, -3, 3, 0, 3,
                0, 0, 6, 6, 6,
                light, depth != null ? 153 : 242, r, g, b);
        poseStack.popPose();

        if (ornament != null) {
            poseStack.pushPose();
            poseStack.mulPose(Vector3f.XP.rotationDegrees(180.0F));
            VertexConsumer ornamentConsumer = buffer.getBuffer(RenderType.entityTranslucent(ornament));
            renderOrnamentQuad(poseStack, ornamentConsumer, light, 0.0F);
            poseStack.mulPose(Vector3f.YP.rotationDegrees(90.0F));
            renderOrnamentQuad(poseStack, ornamentConsumer, light, 0.0F);
            poseStack.popPose();
        }
    }

    /**
     * Original ModelWand renders a ten-rune orbit around every sceptre even when
     * the ordinary wand-runes upgrade is absent.  The previous port gated all
     * script geometry behind hasRunes(), leaving sceptres visually incomplete.
     */
    private void renderOriginalTC4SceptreRunes(ItemStack stack, PoseStack poseStack, MultiBufferSource buffer, int packedLight) {
        if (!WandComponentData.isSceptre(stack)) {
            return;
        }
        Player player = Minecraft.getInstance().player;
        float ticks = player == null ? 0.0F : player.tickCount + Minecraft.getInstance().getFrameTime();
        VertexConsumer consumer = buffer.getBuffer(RenderType.eyes(ORIGINAL_SCRIPT));
        int light = originalBlockGlow(packedLight, 200);
        poseStack.pushPose();
        for (int rot = 0; rot < 10; rot++) {
            poseStack.pushPose();
            poseStack.mulPose(Vector3f.YP.rotationDegrees(36.0F * rot + ticks));
            renderRune(poseStack, consumer, 0.16F, -0.01F, -0.125F, rot, light, ticks);
            poseStack.popPose();
        }
        poseStack.popPose();
    }

    /** Stage183: non-invented rune adapter using original misc/script.png. */
    private void renderOriginalTC4Runes(WandComponentData data, ItemStack stack, PoseStack poseStack, MultiBufferSource buffer, int packedLight) {
        if (!data.hasRunes()) {
            return;
        }
        Player player = Minecraft.getInstance().player;
        float ticks = player == null ? 0.0F : player.tickCount + Minecraft.getInstance().getFrameTime();
        VertexConsumer consumer = buffer.getBuffer(RenderType.eyes(ORIGINAL_SCRIPT));
        int light = originalBlockGlow(packedLight, 200);
        poseStack.pushPose();
        for (int rot = 0; rot < 4; rot++) {
            poseStack.mulPose(Vector3f.YP.rotationDegrees(90.0F));
            for (int a = 0; a < 14; a++) {
                int rune = (a + rot * 3) % 16;
                renderRune(poseStack, consumer, 0.36F + a * 0.14F, -0.01F, -0.08F, rune, light, ticks);
            }
        }
        poseStack.popPose();
    }

    private ResourceLocation focusDepthTexture(WandFocusType focus) {
        return switch (focus) {
            case PORTABLE_HOLE -> originalItemTexture("focus_portablehole_depth");
            case WARDING -> originalItemTexture("focus_warding_depth");
            case PRIMAL -> originalItemTexture("focus_primal_depth");
            case PECH_CURSE -> originalItemTexture("focus_pech_depth");
            default -> null;
        };
    }

    private ResourceLocation focusOrnamentTexture(WandFocusType focus) {
        return switch (focus) {
            case EQUAL_TRADE -> originalItemTexture("focus_trade_orn");
            case WARDING -> originalItemTexture("focus_warding_orn");
            case HELLBAT -> originalItemTexture("focus_hellbat_orn");
            default -> null;
        };
    }

    private ResourceLocation originalItemTexture(String name) {
        return new ResourceLocation(ThaumcraftMod.MOD_ID, "textures/original/thaumcraft4/items/" + name + ".png");
    }

    private ResourceLocation wandTexture(String name) {
        return new ResourceLocation(ThaumcraftMod.MOD_ID, "textures/entity/wand/" + name + ".png");
    }

    private void renderOrnamentQuad(PoseStack poseStack, VertexConsumer consumer, int light, float z) {
        poseStack.pushPose();
        poseStack.translate(-0.25F, -0.10F, 0.0275F + z);
        poseStack.scale(0.50F, 0.50F, 0.50F);
        Matrix4f matrix = poseStack.last().pose();
        vertexColor(matrix, consumer, 0.0F, 0.0F, 0.0F, 0.0F, 1.0F, light, 255, 255, 255, 255);
        vertexColor(matrix, consumer, 0.5F, 0.0F, 0.0F, 1.0F, 1.0F, light, 255, 255, 255, 255);
        vertexColor(matrix, consumer, 0.5F, 0.5F, 0.0F, 1.0F, 0.0F, light, 255, 255, 255, 255);
        vertexColor(matrix, consumer, 0.0F, 0.5F, 0.0F, 0.0F, 0.0F, light, 255, 255, 255, 255);
        poseStack.popPose();
    }

    private void renderRune(PoseStack poseStack, VertexConsumer consumer, float x, float y, float z,
                            int rune, int light, float ticks) {
        poseStack.pushPose();
        poseStack.mulPose(Vector3f.ZP.rotationDegrees(90.0F));
        poseStack.translate(x, y, z);
        Matrix4f matrix = poseStack.last().pose();
        float u0 = 0.0625F * rune;
        float u1 = u0 + 0.0625F;
        float pulse = Mth.sin((ticks + rune * 5.0F) / 10.0F) * 0.2F;
        int alpha = Mth.clamp((int)((pulse + 0.6F) * 255.0F), 0, 255);
        int red = Mth.clamp((int)((Mth.sin((ticks + rune * 5.0F) / 5.0F) * 0.1F + 0.88F) * 255.0F), 0, 255);
        int green = Mth.clamp((int)((Mth.sin((ticks + rune * 5.0F) / 7.0F) * 0.1F + 0.63F) * 255.0F), 0, 255);
        int blue = 51;
        float grow = pulse / 40.0F;
        vertexColor(matrix, consumer, -0.06F - grow, 0.06F + grow, 0.0F, u1, 1.0F, light, alpha, red, green, blue);
        vertexColor(matrix, consumer, 0.06F + grow, 0.06F + grow, 0.0F, u1, 0.0F, light, alpha, red, green, blue);
        vertexColor(matrix, consumer, 0.06F + grow, -0.06F - grow, 0.0F, u0, 0.0F, light, alpha, red, green, blue);
        vertexColor(matrix, consumer, -0.06F - grow, -0.06F - grow, 0.0F, u0, 1.0F, light, alpha, red, green, blue);
        poseStack.popPose();
    }

    /**
     * Reproduces the 1.7.10 ModelRenderer cuboid UV unwrap used by ModelWand.
     * The original textures are addressed on a logical 64x32 atlas even when
     * their PNG is physically 32x32; using 0..1 on every face sampled unrelated
     * transparent pixels and made rods/caps disappear in world renders.
     */
    private void renderModelBoxColor(PoseStack poseStack, VertexConsumer consumer,
                                     int minXPx, int minYPx, int minZPx,
                                     int maxXPx, int maxYPx, int maxZPx,
                                     int textureU, int textureV,
                                     int widthPx, int heightPx, int depthPx,
                                     int light, int alpha, int red, int green, int blue) {
        final float px = 1.0F / 16.0F;
        float minX = minXPx * px;
        float minY = minYPx * px;
        float minZ = minZPx * px;
        float maxX = maxXPx * px;
        float maxY = maxYPx * px;
        float maxZ = maxZPx * px;

        float tw = 64.0F;
        float th = 32.0F;
        float u = textureU;
        float v = textureV;
        float w = widthPx;
        float h = heightPx;
        float d = depthPx;
        Matrix4f matrix = poseStack.last().pose();

        // Standard ModelBox atlas net (mirror=true in original ModelWand).
        modelQuad(matrix, consumer,
                maxX, minY, minZ, maxX, minY, maxZ, maxX, maxY, maxZ, maxX, maxY, minZ,
                (u + d + w + d) / tw, (v + d) / th, (u + d + w) / tw, (v + d + h) / th,
                light, alpha, red, green, blue);
        modelQuad(matrix, consumer,
                minX, minY, maxZ, minX, minY, minZ, minX, maxY, minZ, minX, maxY, maxZ,
                (u + d) / tw, (v + d) / th, u / tw, (v + d + h) / th,
                light, alpha, red, green, blue);
        modelQuad(matrix, consumer,
                minX, minY, minZ, maxX, minY, minZ, maxX, minY, maxZ, minX, minY, maxZ,
                (u + d + w) / tw, v / th, (u + d) / tw, (v + d) / th,
                light, alpha, red, green, blue);
        modelQuad(matrix, consumer,
                minX, maxY, maxZ, maxX, maxY, maxZ, maxX, maxY, minZ, minX, maxY, minZ,
                (u + d + w + w) / tw, v / th, (u + d + w) / tw, (v + d) / th,
                light, alpha, red, green, blue);
        modelQuad(matrix, consumer,
                minX, minY, minZ, minX, maxY, minZ, maxX, maxY, minZ, maxX, minY, minZ,
                (u + d + w) / tw, (v + d) / th, (u + d) / tw, (v + d + h) / th,
                light, alpha, red, green, blue);
        modelQuad(matrix, consumer,
                maxX, minY, maxZ, maxX, maxY, maxZ, minX, maxY, maxZ, minX, minY, maxZ,
                (u + d + w + d + w) / tw, (v + d) / th, (u + d + w + d) / tw, (v + d + h) / th,
                light, alpha, red, green, blue);
    }

    private void modelQuad(Matrix4f matrix, VertexConsumer consumer,
                           float x1, float y1, float z1,
                           float x2, float y2, float z2,
                           float x3, float y3, float z3,
                           float x4, float y4, float z4,
                           float u0, float v0, float u1, float v1,
                           int light, int alpha, int red, int green, int blue) {
        vertexColor(matrix, consumer, x1, y1, z1, u0, v1, light, alpha, red, green, blue);
        vertexColor(matrix, consumer, x2, y2, z2, u1, v1, light, alpha, red, green, blue);
        vertexColor(matrix, consumer, x3, y3, z3, u1, v0, light, alpha, red, green, blue);
        vertexColor(matrix, consumer, x4, y4, z4, u0, v0, light, alpha, red, green, blue);
    }

    private void renderBox(PoseStack poseStack, VertexConsumer consumer,
                           float minX, float minY, float minZ,
                           float maxX, float maxY, float maxZ,
                           int light, int alpha) {
        renderBoxColor(poseStack, consumer, minX, minY, minZ, maxX, maxY, maxZ, light, alpha, 255, 255, 255);
    }

    private void renderBoxColor(PoseStack poseStack, VertexConsumer consumer,
                                float minX, float minY, float minZ,
                                float maxX, float maxY, float maxZ,
                                int light, int alpha, int red, int green, int blue) {
        Matrix4f matrix = poseStack.last().pose();

        quadColor(matrix, consumer, minX, minY, minZ, maxX, minY, minZ, maxX, maxY, minZ, minX, maxY, minZ, light, alpha, red, green, blue);
        quadColor(matrix, consumer, maxX, minY, maxZ, minX, minY, maxZ, minX, maxY, maxZ, maxX, maxY, maxZ, light, alpha, red, green, blue);
        quadColor(matrix, consumer, maxX, minY, minZ, maxX, minY, maxZ, maxX, maxY, maxZ, maxX, maxY, minZ, light, alpha, red, green, blue);
        quadColor(matrix, consumer, minX, minY, maxZ, minX, minY, minZ, minX, maxY, minZ, minX, maxY, maxZ, light, alpha, red, green, blue);
        quadColor(matrix, consumer, minX, maxY, minZ, maxX, maxY, minZ, maxX, maxY, maxZ, minX, maxY, maxZ, light, alpha, red, green, blue);
        quadColor(matrix, consumer, minX, minY, maxZ, maxX, minY, maxZ, maxX, minY, minZ, minX, minY, minZ, light, alpha, red, green, blue);
    }

    private void quadColor(Matrix4f matrix, VertexConsumer consumer,
                           float x1, float y1, float z1,
                           float x2, float y2, float z2,
                           float x3, float y3, float z3,
                           float x4, float y4, float z4,
                           int light, int alpha, int red, int green, int blue) {
        vertexColor(matrix, consumer, x1, y1, z1, 0.0F, 1.0F, light, alpha, red, green, blue);
        vertexColor(matrix, consumer, x2, y2, z2, 1.0F, 1.0F, light, alpha, red, green, blue);
        vertexColor(matrix, consumer, x3, y3, z3, 1.0F, 0.0F, light, alpha, red, green, blue);
        vertexColor(matrix, consumer, x4, y4, z4, 0.0F, 0.0F, light, alpha, red, green, blue);
    }

    private void vertexColor(Matrix4f matrix, VertexConsumer consumer,
                             float x, float y, float z,
                             float u, float v, int light, int alpha, int red, int green, int blue) {
        consumer.vertex(matrix, x, y, z)
                .color(red, green, blue, alpha)
                .uv(u, v)
                .overlayCoords(OverlayTexture.NO_OVERLAY)
                .uv2(light)
                .normal(0.0F, 0.0F, 1.0F)
                .endVertex();
    }
}
