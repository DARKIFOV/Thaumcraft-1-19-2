package com.darkifov.thaumcraft.client;

import com.darkifov.thaumcraft.ThaumcraftMod;
import com.darkifov.thaumcraft.block.WandItem;
import com.darkifov.thaumcraft.item.ElementalShovelItem;
import com.darkifov.thaumcraft.network.ThaumcraftNetwork;
import com.darkifov.thaumcraft.wand.FocusArchitectRuntime;
import com.darkifov.thaumcraft.wand.FocusUpgradeType;
import com.darkifov.thaumcraft.wand.WandComponentData;
import com.darkifov.thaumcraft.wand.WandFocusRuntime;
import com.darkifov.thaumcraft.wand.WandFocusType;
import com.darkifov.thaumcraft.wand.WandManagerRuntime;
import com.darkifov.thaumcraft.ward.WardedBlockRuntime;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.client.event.RenderGuiOverlayEvent;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.List;

/**
 * Stage179 client adapter for original TC4 REHWandHandler/IArchitect preview.
 *
 * The original draws architect block outlines and axis hints client-side while
 * PacketItemKeyToServer key==1 mutates area NBT on the server.  This class keeps
 * that split: G sends RequestWandArchitectTogglePacket, while the overlay reads
 * the same areax/areay/areaz/aread/picked keys for preview only.
 */
@Mod.EventBusSubscriber(modid = ThaumcraftMod.MOD_ID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class ClientWandArchitectEvents {
    private ClientWandArchitectEvents() {
    }

    @SubscribeEvent
    public static void onKeyInput(InputEvent.Key event) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player == null || minecraft.screen != null) {
            return;
        }
        while (ClientWandArchitectKeybinds.KEY_MISC_WAND_TOGGLE.consumeClick()) {
            ItemStack held = heldWand(minecraft.player);
            if (held.isEmpty()) {
                held = heldElementalShovel(minecraft.player);
            }
            if (!held.isEmpty()) {
                ThaumcraftNetwork.requestWandArchitectToggleFromClient();
            }
        }
    }

    @SubscribeEvent
    public static void onRenderGui(RenderGuiOverlayEvent.Post event) {
        Minecraft minecraft = Minecraft.getInstance();
        Preview preview = preview(minecraft);
        if (preview == null) {
            return;
        }

        PoseStack poseStack = event.getPoseStack();
        Font font = minecraft.font;
        int screenWidth = minecraft.getWindow().getGuiScaledWidth();
        int x = screenWidth / 2 - 104;
        int y = 118;
        int h = preview.picked().isEmpty() ? 34 : 46;

        net.minecraft.client.gui.GuiComponent.fill(poseStack, x, y, x + 208, y + h, 0x99100A18);
        font.draw(poseStack, Component.literal(FocusArchitectRuntime.architectStatusLine(preview.wandStack())), x + 8, y + 7, 0xFFE9D7FF);
        font.draw(poseStack, Component.literal("Preview blocks " + preview.blocks().size() + " | axis " + axisLine(preview)), x + 8, y + 20, 0xFFBFAFEF);
        if (!preview.picked().isEmpty()) {
            font.draw(poseStack, Component.literal("Picked: " + preview.picked().getHoverName().getString()), x + 8, y + 33, 0xFFC7E9FF);
        }
    }

    @SubscribeEvent
    public static void onRenderLevel(RenderLevelStageEvent event) {
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_TRANSLUCENT_BLOCKS) {
            return;
        }
        Minecraft minecraft = Minecraft.getInstance();
        Preview preview = preview(minecraft);
        if (preview == null || preview.blocks().isEmpty()) {
            return;
        }

        PoseStack poseStack = event.getPoseStack();
        Vec3 camera = event.getCamera().getPosition();
        poseStack.pushPose();
        poseStack.translate(-camera.x, -camera.y, -camera.z);
        MultiBufferSource.BufferSource buffer = minecraft.renderBuffers().bufferSource();
        VertexConsumer consumer = buffer.getBuffer(RenderType.lines());
        int max = Math.min(preview.blocks().size(), 256);
        for (int i = 0; i < max; i++) {
            BlockPos pos = preview.blocks().get(i);
            AABB box = new AABB(pos).inflate(0.002D);
            LevelRenderer.renderLineBox(poseStack, consumer, box, 0.38F, 0.72F, 1.0F, 0.78F);
        }
        buffer.endBatch(RenderType.lines());
        poseStack.popPose();
    }

    private static Preview preview(Minecraft minecraft) {
        if (minecraft.level == null || minecraft.player == null || minecraft.hitResult == null) {
            return null;
        }
        ItemStack wandStack = heldWand(minecraft.player);
        if (wandStack.isEmpty()) {
            return null;
        }
        WandFocusType focus = WandFocusRuntime.getFocus(wandStack);
        if ((focus != WandFocusType.EQUAL_TRADE && focus != WandFocusType.WARDING)
                || !WandFocusRuntime.focusHasUpgrade(wandStack, FocusUpgradeType.ARCHITECT)) {
            return null;
        }
        if (minecraft.hitResult.getType() != HitResult.Type.BLOCK) {
            return null;
        }
        BlockHitResult hit = (BlockHitResult) minecraft.hitResult;
        boolean removing = focus == WandFocusType.WARDING && WardedBlockRuntime.isWarded(minecraft.level, hit.getBlockPos());
        List<BlockPos> blocks = focus == WandFocusType.EQUAL_TRADE
                ? FocusArchitectRuntime.equalTradeArchitectBlocks(wandStack, minecraft.level, hit, minecraft.player)
                : FocusArchitectRuntime.wardingArchitectBlocks(wandStack, minecraft.level, hit, minecraft.player, removing);
        return new Preview(wandStack, focus, hit, blocks, FocusArchitectRuntime.pickedBlock(wandStack));
    }

    private static ItemStack heldWand(net.minecraft.world.entity.player.Player player) {
        ItemStack main = player.getMainHandItem();
        if (main.getItem() instanceof WandItem) return main;
        ItemStack off = player.getOffhandItem();
        return off.getItem() instanceof WandItem ? off : ItemStack.EMPTY;
    }

    private static ItemStack heldElementalShovel(net.minecraft.world.entity.player.Player player) {
        ItemStack main = player.getMainHandItem();
        if (main.getItem() instanceof ElementalShovelItem) return main;
        ItemStack off = player.getOffhandItem();
        return off.getItem() instanceof ElementalShovelItem ? off : ItemStack.EMPTY;
    }

    private static String axisLine(Preview preview) {
        StringBuilder out = new StringBuilder();
        if (FocusArchitectRuntime.showAxis(preview.wandStack(), preview.focus(), preview.hit().getDirection(), net.minecraft.core.Direction.Axis.X)) {
            out.append('X');
        }
        if (FocusArchitectRuntime.showAxis(preview.wandStack(), preview.focus(), preview.hit().getDirection(), net.minecraft.core.Direction.Axis.Y)) {
            out.append('Y');
        }
        if (FocusArchitectRuntime.showAxis(preview.wandStack(), preview.focus(), preview.hit().getDirection(), net.minecraft.core.Direction.Axis.Z)) {
            out.append('Z');
        }
        return out.length() == 0 ? "none" : out.toString();
    }

    private record Preview(ItemStack wandStack, WandFocusType focus, BlockHitResult hit, List<BlockPos> blocks, ItemStack picked) {
    }
}
