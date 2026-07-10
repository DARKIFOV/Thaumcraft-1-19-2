package com.darkifov.thaumcraft.client;

import com.darkifov.thaumcraft.Aspect;
import com.darkifov.thaumcraft.ThaumcraftMod;
import com.darkifov.thaumcraft.block.EssentiaValveBlock;
import com.darkifov.thaumcraft.blockentity.AlchemicalFurnaceBlockEntity;
import com.darkifov.thaumcraft.blockentity.EssentiaJarBlockEntity;
import com.darkifov.thaumcraft.blockentity.EssentiaTubeBlockEntity;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderGuiOverlayEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * v11.62.10: TC4 IGoggles.showIngamePopups parity pass.
 *
 * Original 1.7.10 reference points checked for this pass:
 * - thaumcraft.common.items.armor.ItemGoggles#showIngamePopups returns true.
 * - thaumcraft.client.lib.ClientTickEventsFML binds textures/gui/hud.png for old HUD panels.
 * - TileJarRenderer uses the aspect/amount/filter state as the player-facing jar readout source.
 *
 * This overlay intentionally stops being a permanent modern debug rectangle.  It is only shown
 * when the player is wearing Goggles/Helmet of Revealing, and it uses the old HUD atlas frame,
 * aspect icon and compact TC4-style text near the crosshair.
 */
@Mod.EventBusSubscriber(modid = ThaumcraftMod.MOD_ID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class EssentiaOverlayEvents {
    private EssentiaOverlayEvents() {
    }

    @SubscribeEvent
    public static void onRenderOverlay(RenderGuiOverlayEvent.Post event) {
        Minecraft minecraft = Minecraft.getInstance();

        if (minecraft.level == null || minecraft.player == null || minecraft.hitResult == null || minecraft.options.hideGui) {
            return;
        }

        if (!TC4RevealerHudAdapter.hasIngamePopupRevealer(minecraft.player)) {
            return;
        }

        if (minecraft.hitResult.getType() != HitResult.Type.BLOCK) {
            return;
        }

        BlockPos pos = ((BlockHitResult) minecraft.hitResult).getBlockPos();
        BlockEntity blockEntity = minecraft.level.getBlockEntity(pos);
        PopupData data = popupFor(minecraft, pos, blockEntity);
        if (data == null) {
            return;
        }

        int screenWidth = minecraft.getWindow().getGuiScaledWidth();
        int screenHeight = minecraft.getWindow().getGuiScaledHeight();
        int x = screenWidth / 2 + 16;
        int y = screenHeight / 2 - 33;
        renderOriginalPopup(event.getPoseStack(), minecraft.font, data, x, y);
    }

    private static PopupData popupFor(Minecraft minecraft, BlockPos pos, BlockEntity blockEntity) {
        if (blockEntity instanceof EssentiaJarBlockEntity jar) {
            boolean filtered = minecraft.level.getBlockState(pos).is(ThaumcraftMod.FILTERED_ESSENTIA_JAR.get());
            boolean voidJar = minecraft.level.getBlockState(pos).is(ThaumcraftMod.VOID_ESSENTIA_JAR.get());
            Aspect stored = jar.storedAspect();
            Aspect displayed = stored != null ? stored : jar.filterAspect();
            String title = voidJar ? "Void Jar" : filtered ? "Filtered Jar" : "Warded Jar";
            String amount = stored == null ? "Empty" : jar.amount() + "/" + jar.capacity();
            String filter = jar.filterAspect() == null ? "No filter" : "Filter: " + jar.filterAspect().displayName();
            int suction = jar.originalSuctionAmount(voidJar);
            return new PopupData(title, amount, filter + "  S:" + suction, displayed, jar.amount(), jar.capacity());
        }

        if (blockEntity instanceof EssentiaTubeBlockEntity tube) {
            boolean valve = minecraft.level.getBlockState(pos).is(ThaumcraftMod.ESSENTIA_VALVE.get());
            Aspect type = tube.getTransportEssentiaType(Direction.UP);
            if (type == null) {
                type = tube.aspectFilter();
            }
            String title = valve ? "Essentia Valve" : "Essentia Tube";
            String amount = tube.getTransportEssentiaAmount(Direction.UP) > 0
                    ? tube.getTransportEssentiaAmount(Direction.UP) + " essentia"
                    : "No essentia";
            String suction = "S:" + tube.getSuctionAmount(Direction.UP) + "  Net:" + tube.networkSize()
                    + (valve ? (EssentiaValveBlock.isOpen(minecraft.level, pos) ? "  Open" : "  Closed") : "");
            return new PopupData(title, amount, suction, type, tube.getTransportEssentiaAmount(Direction.UP), 8);
        }

        if (blockEntity instanceof AlchemicalFurnaceBlockEntity furnace) {
            Aspect type = furnace.firstAspect();
            int pct = furnace.burnDuration() <= 0 ? 0 : Math.min(100, furnace.burnProgress() * 100 / furnace.burnDuration());
            return new PopupData("Alchemical Furnace", furnace.aspects().totalAmount() + "/128 essentia",
                    "Fuel " + furnace.fuelTime() + "  Burn " + pct + "%", type,
                    furnace.aspects().totalAmount(), AlchemicalFurnaceBlockEntity.CAPACITY);
        }

        return null;
    }

    private static void renderOriginalPopup(PoseStack poseStack, Font font, PopupData data, int x, int y) {
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderTexture(0, TC4AuraNodeHudParity.ORIGINAL_HUD);

        // Original TC4 HUD atlas pieces, reused instead of a rectangular debug background.
        GuiComponent.blit(poseStack, x - 8, y - 7, TC4AuraNodeHudParity.HUD_RING_U, TC4AuraNodeHudParity.HUD_RING_V,
                TC4AuraNodeHudParity.HUD_RING_W, TC4AuraNodeHudParity.HUD_RING_H, 256, 256);
        GuiComponent.blit(poseStack, x + 38, y - 4, TC4AuraNodeHudParity.HUD_BAR_U, TC4AuraNodeHudParity.HUD_BAR_V,
                TC4AuraNodeHudParity.HUD_BAR_W, TC4AuraNodeHudParity.HUD_BAR_H, 256, 256);

        if (data.aspect != null) {
            drawAspectIcon(poseStack, data.aspect, x + 8, y + 9);
            String count = data.amount <= 0 ? "" : String.valueOf(data.amount);
            if (!count.isEmpty()) {
                font.draw(poseStack, Component.literal(count), x + 26, y + 13, 0xFFEFE6FF);
            }
        } else {
            font.draw(poseStack, Component.literal("—"), x + 14, y + 14, 0xFFBFAFEF);
        }

        int fillHeight = data.capacity <= 0 ? 0 : Math.min(40, Math.max(0, Math.round(data.amount / (float) data.capacity * 40.0F)));
        if (fillHeight > 0 && data.aspect != null) {
            int color = 0xCC000000 | (data.aspect.nativeColor() & 0x00FFFFFF);
            GuiComponent.fill(poseStack, x + 41, y + 35 - fillHeight, x + 46, y + 35, color);
        }

        font.draw(poseStack, Component.literal(data.title).withStyle(ChatFormatting.DARK_PURPLE), x + 54, y + 1, 0xFFE8D4FF);
        font.draw(poseStack, Component.literal(data.line1), x + 54, y + 13, 0xFFD8C9F2);
        font.draw(poseStack, Component.literal(data.line2), x + 54, y + 25, 0xFFBFAFEF);
        RenderSystem.disableBlend();
    }

    private static void drawAspectIcon(PoseStack poseStack, Aspect aspect, int x, int y) {
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderTexture(0, new net.minecraft.resources.ResourceLocation(ThaumcraftMod.MOD_ID, "textures/aspects/" + aspect.id() + ".png"));
        GuiComponent.blit(poseStack, x, y, 0, 0, 16, 16, 16, 16);
    }

    private record PopupData(String title, String line1, String line2, Aspect aspect, int amount, int capacity) {
    }
}
