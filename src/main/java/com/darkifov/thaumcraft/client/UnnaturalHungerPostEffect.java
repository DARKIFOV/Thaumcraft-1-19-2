package com.darkifov.thaumcraft.client;

import com.darkifov.thaumcraft.ThaumcraftMod;
import com.mojang.blaze3d.pipeline.RenderTarget;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.PostChain;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderGuiEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Dedicated TC4 Unnatural Hunger post chain.
 *
 * <p>The original effect was processed immediately before the HUD and owned a
 * separate shader group, so it could coexist with other post effects. This
 * implementation mirrors that lifecycle instead of taking over
 * {@code GameRenderer}'s single global effect slot.</p>
 */
@Mod.EventBusSubscriber(modid = ThaumcraftMod.MOD_ID, value = Dist.CLIENT,
        bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class UnnaturalHungerPostEffect {
    private static final Logger LOGGER = LoggerFactory.getLogger(UnnaturalHungerPostEffect.class);
    private static final ResourceLocation POST_CHAIN = new ResourceLocation(
            ThaumcraftMod.MOD_ID, "shaders/post/unnatural_hunger.json");

    private static PostChain chain;
    private static int width = -1;
    private static int height = -1;
    private static boolean reloadPending;
    private static boolean loadFailed;

    private UnnaturalHungerPostEffect() {
    }

    /** Called by the client resource reload listener; GL cleanup is deferred. */
    public static void invalidateResources() {
        reloadPending = true;
    }

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) {
            return;
        }

        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player == null
                || !minecraft.player.hasEffect(ThaumcraftMod.UNNATURAL_HUNGER.get())) {
            closeChain();
        }
    }

    /**
     * Forge fires this once before the complete HUD, matching TC4's
     * RenderGameOverlayEvent.Pre(ALL) processing point.
     */
    @SubscribeEvent
    public static void onRenderGuiPre(RenderGuiEvent.Pre event) {
        Minecraft minecraft = Minecraft.getInstance();
        if (reloadPending) {
            closeChain();
            reloadPending = false;
            loadFailed = false;
        }

        if (minecraft.player == null
                || !minecraft.player.hasEffect(ThaumcraftMod.UNNATURAL_HUNGER.get())) {
            closeChain();
            return;
        }

        if (!ensureChain(minecraft)) {
            return;
        }

        int framebufferWidth = event.getWindow().getWidth();
        int framebufferHeight = event.getWindow().getHeight();
        if (framebufferWidth != width || framebufferHeight != height) {
            chain.resize(framebufferWidth, framebufferHeight);
            width = framebufferWidth;
            height = framebufferHeight;
        }

        chain.process(event.getPartialTick());
        RenderTarget mainTarget = minecraft.getMainRenderTarget();
        mainTarget.bindWrite(true);
    }

    private static boolean ensureChain(Minecraft minecraft) {
        if (chain != null) {
            return true;
        }
        if (loadFailed) {
            return false;
        }

        try {
            chain = new PostChain(
                    minecraft.getTextureManager(),
                    minecraft.getResourceManager(),
                    minecraft.getMainRenderTarget(),
                    POST_CHAIN);
            width = -1;
            height = -1;
            return true;
        } catch (Exception exception) {
            loadFailed = true;
            LOGGER.error("Unable to load TC4 Unnatural Hunger post chain {}", POST_CHAIN, exception);
            return false;
        }
    }

    private static void closeChain() {
        if (chain != null) {
            chain.close();
            chain = null;
        }
        width = -1;
        height = -1;
    }
}
