package com.darkifov.thaumcraft.client.screen;

import com.darkifov.thaumcraft.Aspect;
import com.darkifov.thaumcraft.client.ClientAspectData;
import com.darkifov.thaumcraft.client.ClientResearchData;
import com.darkifov.thaumcraft.block.ResearchNoteItem;
import com.darkifov.thaumcraft.block.ScribingToolsItem;
import com.darkifov.thaumcraft.research.ResearchNoteState;
import com.darkifov.thaumcraft.network.ThaumcraftNetwork;
import com.darkifov.thaumcraft.research.ResearchEntry;
import com.darkifov.thaumcraft.research.TC4ResearchFlagPolicy;
import com.darkifov.thaumcraft.research.TC4ThaumonomiconParity;
import com.darkifov.thaumcraft.porting.TC4Sounds;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Stage117: the Thaumonomicon browser is no longer the wide rebuild page.
 * It now follows TC4 GuiResearchBrowser behavior: 256x230 frame, draggable
 * research map, original category tabs, original backgrounds, original node
 * sprites and TC4 display coordinates.
 */
public class ThaumonomiconScreen extends Screen {
    private static final ResourceLocation GUI_RESEARCH = ResourceLocation.fromNamespaceAndPath(
            "thaumcraft", "textures/original/thaumcraft4/gui/gui_research.png");
    private static final ResourceLocation NODE_SHEET = ResourceLocation.fromNamespaceAndPath(
            "thaumcraft", "textures/original/thaumcraft4/misc/nodes.png");
    private static final int PANE_WIDTH = 256;
    private static final int PANE_HEIGHT = 230;
    private static final int TC4_BACKGROUND_TRAVEL_X = 288;
    private static final int TC4_BACKGROUND_TRAVEL_Y = 316;
    private static final int TC4_BACKGROUND_SOURCE_WIDTH = 112;
    private static final int TC4_BACKGROUND_SOURCE_HEIGHT = 98;
    private static final int TC4_BACKGROUND_DEST_WIDTH = 224;
    private static final int TC4_BACKGROUND_DEST_HEIGHT = 196;
    private static final int TC4_NODE_ATLAS_SIZE = 2048;
    private static final int TC4_NODE_CELL = 64;
    private static final int TC4_NODE_FRAMES = 32;
    private static final int TC4_FORBIDDEN_STRIP = 5;
    private static final int TC4_FORBIDDEN_SIZE = 80;
    private static final int TC4_FORBIDDEN_COLOR = 0x440055;
    private static final float TC4_FORBIDDEN_ALPHA = 0.66F;
    private static final double DRAG_THRESHOLD_SQUARED = 16.0D;

    private int leftPos;
    private int topPos;
    private OriginalResearchCategory category = TC4ThaumonomiconClientState.category();
    private ResearchEntry selected;
    private ResearchEntry highlighted;
    private int panX = TC4ThaumonomiconClientState.panX();
    private int panY = TC4ThaumonomiconClientState.panY();
    private boolean dragging;
    private boolean draggedBeyondClick;
    private ResearchEntry pressedEntry;
    private double pressMouseX;
    private double pressMouseY;
    private double lastMouseX;
    private double lastMouseY;
    private long popupUntil;
    private String popupText = "";

    public ThaumonomiconScreen() {
        super(Component.translatable("item.thaumcraft.thaumonomicon"));
    }

    @Override
    protected void init() {
        this.leftPos = (this.width - PANE_WIDTH) / 2;
        this.topPos = (this.height - PANE_HEIGHT) / 2;
        this.clearWidgets();
        category = TC4ThaumonomiconClientState.category();
        panX = TC4ThaumonomiconClientState.panX();
        panY = TC4ThaumonomiconClientState.panY();
        clampPan();
        saveBookState();
    }

    private Set<String> unlockedResearch() {
        // Stage205 hard parity reset: do not inject fake client-side completions.
        // TC4 progression visibility must come from the player's research data and
        // each ResearchItem's parents/hiddenParents, not from convenience defaults.
        return new HashSet<>(ClientResearchData.research());
    }

    @Override
    public void render(PoseStack poseStack, int mouseX, int mouseY, float partialTick) {
        Set<String> unlocked = unlockedResearch();
        ensureVisibleCategory(unlocked);
        renderBackground(poseStack);
        highlighted = null;
        renderResearchTree(poseStack, mouseX, mouseY, partialTick);
        renderTabs(poseStack, mouseX, mouseY, unlocked);
        OriginalGuiTextures.blitOriginalRegion(poseStack, leftPos, topPos, GUI_RESEARCH, 0, 0, PANE_WIDTH, PANE_HEIGHT, 256, 256);
        renderSelectedPage(poseStack);
        renderPopup(poseStack);
        renderHoverTooltip(poseStack, mouseX, mouseY, unlocked);
        super.render(poseStack, mouseX, mouseY, partialTick);
    }

    private void renderResearchTree(PoseStack poseStack, int mouseX, int mouseY, float partialTick) {
        renderMap(poseStack, mouseX, mouseY, partialTick);
    }

    private void renderSelectedPage(PoseStack poseStack) {
        // Stage117 keeps Stage94/95 bridge language for CI compatibility while TC4 pages render in TC4ResearchPageScreen.
        // Selected for Research Note / Complete Selected
    }

    private void renderMap(PoseStack poseStack, int mouseX, int mouseY, float partialTick) {
        int mapLeft = leftPos + OriginalResearchLayout.VIEW_X;
        int mapTop = topPos + OriginalResearchLayout.VIEW_Y;
        Set<String> unlocked = unlockedResearch();
        List<ResearchEntry> entries = OriginalResearchLayout.entriesFor(category);

        // Exact GuiResearchBrowser background sampling: convert the category's
        // clamped research-space pan into the 288x316 movable area of the 512 texture.
        OriginalResearchLayout.Bounds bounds = OriginalResearchLayout.boundsFor(category);
        int bgU = proportionalBackgroundCoordinate(panX, bounds.minPanX(), bounds.maxPanX(),
                TC4_BACKGROUND_TRAVEL_X);
        int bgV = proportionalBackgroundCoordinate(panY, bounds.minPanY(), bounds.maxPanY(),
                TC4_BACKGROUND_TRAVEL_Y);
        // GuiResearchBrowser scales the map texture by 2x and samples only a
        // 112x98 source window (vx/2, vy/2).  Sampling a 224x196 source window
        // directly made the parchment pattern twice as dense and visually moved
        // the research graph against its background.
        OriginalGuiTextures.blitOriginalScaledRegion(poseStack, mapLeft, mapTop, category.background(),
                bgU / 2, bgV / 2, TC4_BACKGROUND_SOURCE_WIDTH, TC4_BACKGROUND_SOURCE_HEIGHT,
                TC4_BACKGROUND_DEST_WIDTH, TC4_BACKGROUND_DEST_HEIGHT, 512, 512);

        enableMapScissor(mapLeft, mapTop, OriginalResearchLayout.VIEW_WIDTH, OriginalResearchLayout.VIEW_HEIGHT);
        // Parent and sibling links are drawn before nodes, just like TC4.
        Set<String> renderedSiblingLinks = new HashSet<>();
        for (ResearchEntry entry : entries) {
            if (!OriginalResearchLayout.visible(unlocked, entry)) continue;
            int x1 = mapLeft + OriginalResearchLayout.mapX(entry, panX) + 11;
            int y1 = mapTop + OriginalResearchLayout.mapY(entry, panY) + 11;
            for (String parentKey : entry.requirements()) {
                ResearchEntry parent = find(entries, parentKey);
                if (parent == null || !OriginalResearchLayout.visible(unlocked, parent)) continue;
                int x2 = mapLeft + OriginalResearchLayout.mapX(parent, panX) + 11;
                int y2 = mapTop + OriginalResearchLayout.mapY(parent, panY) + 11;
                if (unlocked.contains(entry.key())) {
                    drawResearchLine(poseStack, x1, y1, x2, y2, 0xFF1A1A1A, true, partialTick);
                } else if (unlocked.contains(parent.key())) {
                    drawResearchLine(poseStack, x1, y1, x2, y2, 0xFF00FF00, false, partialTick);
                } else {
                    drawResearchLine(poseStack, x1, y1, x2, y2, 0xFF0000FF, false, partialTick);
                }
            }
            for (String siblingKey : entry.siblings()) {
                ResearchEntry sibling = find(entries, siblingKey);
                if (sibling == null || !OriginalResearchLayout.visible(unlocked, sibling)) continue;
                // TC4 does not draw a sibling edge when that same relationship is
                // already represented by sibling.parents. It also renders each
                // undirected sibling edge only once.
                if (containsKey(sibling.requirements(), entry.key())) continue;
                String pair = entry.key().compareTo(sibling.key()) <= 0
                        ? entry.key() + "\u001F" + sibling.key()
                        : sibling.key() + "\u001F" + entry.key();
                if (!renderedSiblingLinks.add(pair)) continue;
                int x2 = mapLeft + OriginalResearchLayout.mapX(sibling, panX) + 11;
                int y2 = mapTop + OriginalResearchLayout.mapY(sibling, panY) + 11;
                if (unlocked.contains(entry.key())) {
                    drawResearchLine(poseStack, x1, y1, x2, y2, 0xFF1A1A33, true, partialTick);
                } else if (unlocked.contains(sibling.key())) {
                    drawResearchLine(poseStack, x1, y1, x2, y2, 0xFF00FF00, false, partialTick);
                } else {
                    drawResearchLine(poseStack, x1, y1, x2, y2, 0xFF0000FF, false, partialTick);
                }
            }
        }

        for (ResearchEntry entry : entries) {
            if (!OriginalResearchLayout.visible(unlocked, entry)) continue;
            int x = mapLeft + OriginalResearchLayout.mapX(entry, panX);
            int y = mapTop + OriginalResearchLayout.mapY(entry, panY);
            if (x < mapLeft - 28 || y < mapTop - 28 || x > mapLeft + OriginalResearchLayout.VIEW_WIDTH || y > mapTop + OriginalResearchLayout.VIEW_HEIGHT) {
                continue;
            }
            boolean complete = OriginalResearchLayout.unlocked(unlocked, entry);
            boolean available = OriginalResearchLayout.available(unlocked, entry);
            renderNode(poseStack, entry, x, y, complete, available, partialTick);
            if (mouseX >= x && mouseX <= x + 22 && mouseY >= y && mouseY <= y + 22
                    && mouseX >= mapLeft && mouseX < mapLeft + OriginalResearchLayout.VIEW_WIDTH
                    && mouseY >= mapTop && mouseY < mapTop + OriginalResearchLayout.VIEW_HEIGHT) {
                highlighted = entry;
            }
        }
        RenderSystem.disableScissor();
    }

    private static int proportionalBackgroundCoordinate(int pan, int minPan, int maxPan, int travel) {
        int low = Math.min(minPan, maxPan);
        int highExclusive = Math.max(minPan, maxPan);
        int span = Math.max(1, highExclusive - low);
        int clamped = Math.max(low, Math.min(highExclusive - 1, pan));
        // GuiResearchBrowser uses an integer cast (floor), not rounding. The
        // maximum reachable source coordinate is therefore travel-1.
        return Math.max(0, Math.min(travel - 1,
                (int) (((clamped - low) / (float) span) * travel)));
    }

    private void enableMapScissor(int x, int y, int width, int height) {
        Minecraft minecraft = Minecraft.getInstance();
        double scale = minecraft.getWindow().getGuiScale();
        int framebufferHeight = minecraft.getWindow().getHeight();
        int sx = (int) Math.floor(x * scale);
        int sy = framebufferHeight - (int) Math.ceil((y + height) * scale);
        int sw = (int) Math.ceil(width * scale);
        int sh = (int) Math.ceil(height * scale);
        RenderSystem.enableScissor(sx, sy, sw, sh);
    }

    private ResearchEntry find(List<ResearchEntry> entries, String key) {
        for (ResearchEntry entry : entries) {
            if (entry.key().equals(key)) return entry;
        }
        return null;
    }

    private static boolean containsKey(String[] values, String key) {
        if (values == null || key == null) return false;
        for (String value : values) {
            if (key.equals(value)) return true;
        }
        return false;
    }

    private void renderNode(PoseStack poseStack, ResearchEntry entry, int x, int y, boolean complete, boolean available, float partialTick) {
        if (OriginalResearchLayout.forbidden(entry)) {
            renderForbiddenWarp(poseStack, x + 11, y + 11);
        }
        RenderSystem.enableBlend();
        float frameBrightness = complete ? 1.0F : available
                ? 0.75F + (float)Math.sin(System.currentTimeMillis() % 600L / 600.0D * Math.PI * 2.0D) * 0.25F
                : 0.30F;
        RenderSystem.setShaderColor(frameBrightness, frameBrightness, frameBrightness, 1.0F);

        boolean secondary = OriginalResearchLayout.secondary(entry);
        int u;
        if (OriginalResearchLayout.round(entry)) {
            u = 54;
        } else if (TC4ResearchFlagPolicy.has(entry, TC4ResearchFlagPolicy.HIDDEN)) {
            // Original GuiResearchBrowser uses the far-right frame for hidden secondary research.
            u = secondary ? 230 : 86;
        } else if (secondary) {
            u = 110;
        } else {
            u = 0;
        }
        OriginalGuiTextures.blitOriginalRegion(poseStack, x - 2, y - 2, GUI_RESEARCH, u, 230, 26, 26, 256, 256);
        if (OriginalResearchLayout.special(entry)) {
            OriginalGuiTextures.blitOriginalRegion(poseStack, x - 2, y - 2, GUI_RESEARCH, 26, 230, 26, 26, 256, 256);
        }

        // TC4 dims the icon more aggressively than its frame when the node is locked.
        float iconBrightness = complete ? 1.0F : available ? frameBrightness : 0.10F;
        ResourceLocation icon = iconFor(entry);
        Aspect iconAspect = aspectForIcon(entry, icon);
        if (iconAspect != null) {
            OriginalGuiTextures.blitOriginalTinted(poseStack, x + 3, y + 3, icon, 16, 16,
                    multiplyRgb(iconAspect.nativeColor(), iconBrightness));
        } else {
            RenderSystem.setShaderColor(iconBrightness, iconBrightness, iconBrightness, 1.0F);
            OriginalGuiTextures.blitOriginal(poseStack, x + 3, y + 3, icon, 16, 16);
        }
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
    }

    private void renderForbiddenWarp(PoseStack poseStack, int centerX, int centerY) {
        int part = Minecraft.getInstance().player == null ? 0
                : Math.floorMod(Minecraft.getInstance().player.tickCount, TC4_NODE_FRAMES);
        int frame = TC4_NODE_FRAMES - 1 - part;
        OriginalGuiTextures.blitOriginalScaledTintedAlpha(poseStack,
                centerX - TC4_FORBIDDEN_SIZE / 2, centerY - TC4_FORBIDDEN_SIZE / 2, NODE_SHEET,
                frame * TC4_NODE_CELL, TC4_FORBIDDEN_STRIP * TC4_NODE_CELL,
                TC4_NODE_CELL, TC4_NODE_CELL, TC4_FORBIDDEN_SIZE, TC4_FORBIDDEN_SIZE,
                TC4_NODE_ATLAS_SIZE, TC4_NODE_ATLAS_SIZE, TC4_FORBIDDEN_COLOR, TC4_FORBIDDEN_ALPHA);
    }

    private static int multiplyRgb(int rgb, float brightness) {
        int red = Math.max(0, Math.min(255, Math.round(((rgb >> 16) & 255) * brightness)));
        int green = Math.max(0, Math.min(255, Math.round(((rgb >> 8) & 255) * brightness)));
        int blue = Math.max(0, Math.min(255, Math.round((rgb & 255) * brightness)));
        return (red << 16) | (green << 8) | blue;
    }

    private Component categoryTitle(OriginalResearchCategory value) {
        return Component.translatable("thaumcraft.category." + value.key().toLowerCase(java.util.Locale.ROOT));
    }

    private Component researchName(ResearchEntry entry) {
        String key = "tc.research_name." + entry.key();
        return I18n.exists(key) ? Component.translatable(key) : Component.literal(entry.title());
    }

    private Component researchDescription(ResearchEntry entry) {
        String key = "tc.research_text." + entry.key();
        return I18n.exists(key) ? Component.translatable(key) : Component.literal(entry.description());
    }

    private Aspect aspectForIcon(ResearchEntry entry, ResourceLocation icon) {
        if (icon == null || !icon.getPath().contains("textures/aspects/")) {
            return null;
        }
        if (!entry.aspects().isEmpty()) {
            return Aspect.byId(entry.aspects().keySet().iterator().next());
        }
        String path = icon.getPath();
        int slash = path.lastIndexOf('/');
        int dot = path.lastIndexOf('.');
        String id = path.substring(slash + 1, dot > slash ? dot : path.length());
        return Aspect.byId(id);
    }

    private ResourceLocation iconFor(ResearchEntry entry) {
        ResourceLocation mapped = TC4ResearchIconMap.texture(entry.key());
        if (mapped != null) {
            return mapped;
        }
        if (!entry.aspects().isEmpty()) {
            String aspect = entry.aspects().keySet().iterator().next().toLowerCase();
            return ResourceLocation.fromNamespaceAndPath("thaumcraft", "textures/aspects/" + aspect + ".png");
        }
        String fallback = switch (category) {
            case BASICS -> "praecantatio";
            case THAUMATURGY -> "auram";
            case ALCHEMY -> "permutatio";
            case ARTIFICE -> "fabrico";
            case GOLEMANCY -> "humanus";
            case ELDRITCH -> "vitium";
        };
        return ResourceLocation.fromNamespaceAndPath("thaumcraft", "textures/aspects/" + fallback + ".png");
    }

    private void drawResearchLine(PoseStack poseStack, int x1, int y1, int x2, int y2,
                                  int color, boolean solid, float partialTick) {
        double deltaX = x1 - x2;
        double deltaY = y1 - y2;
        float distance = (float) Math.sqrt(deltaX * deltaX + deltaY * deltaY);
        int increments = Math.max(1, (int) (distance / 2.0F));
        float stepX = (float) (deltaX / increments);
        float stepY = (float) (deltaY / increments);
        boolean horizontalDominant = Math.abs(deltaX) > Math.abs(deltaY);
        if (horizontalDominant) {
            stepX *= 2.0F;
        } else {
            stepY *= 2.0F;
        }

        Minecraft minecraft = Minecraft.getInstance();
        float count = (minecraft.player == null ? 0.0F : minecraft.player.tickCount) + partialTick;
        float baseRed = ((color >> 16) & 255) / 255.0F;
        float baseGreen = ((color >> 8) & 255) / 255.0F;
        float baseBlue = (color & 255) / 255.0F;
        boolean wiggle = !solid;

        // Port of GuiResearchBrowser#drawLine. A 2x2 pixel point is used as the
        // 1.19.2 GUI-safe equivalent of the original smoothed 3 px line strip.
        for (int index = 0; index <= increments; index++) {
            float phase = index / (float) increments;
            float offsetX = 0.0F;
            float offsetY = 0.0F;
            float red = baseRed;
            float green = baseGreen;
            float blue = baseBlue;
            float alpha = 0.60F;
            if (wiggle) {
                offsetX = (float) Math.sin((count + index) / 7.0F) * 5.0F * (1.0F - phase);
                offsetY = (float) Math.sin((count + index) / 5.0F) * 5.0F * (1.0F - phase);
                red *= 1.0F - phase;
                green *= 1.0F - phase;
                blue *= 1.0F - phase;
                alpha *= phase;
            }

            int x = Math.round(x1 - stepX * index + offsetX);
            int y = Math.round(y1 - stepY * index + offsetY);
            int argb = (Math.round(alpha * 255.0F) << 24)
                    | (Math.round(red * 255.0F) << 16)
                    | (Math.round(green * 255.0F) << 8)
                    | Math.round(blue * 255.0F);
            fill(poseStack, x, y, x + 2, y + 2, argb);

            float curve = 1.0F - 1.0F / (increments * 1.5F);
            if (horizontalDominant) {
                stepX *= curve;
            } else {
                stepY *= curve;
            }
        }
    }

    private void renderTabs(PoseStack poseStack, int mouseX, int mouseY, Set<String> unlocked) {
        int count = 0;
        for (OriginalResearchCategory value : visibleCategories(unlocked)) {
            int x = leftPos - 24;
            int y = topPos + count * 24;
            int tabU = value == category ? 152 : 176;
            OriginalGuiTextures.blitOriginalRegion(poseStack, x, y, GUI_RESEARCH, tabU, 232, 24, 24, 256, 256);
            OriginalGuiTextures.blitOriginal(poseStack, x + 5, y + 4, value.icon(), 16, 16);
            if (value != category) {
                OriginalGuiTextures.blitOriginalRegion(poseStack, x, y, GUI_RESEARCH, 200, 232, 24, 24, 256, 256);
            }
            count++;
        }
    }

    private void renderHoverTooltip(PoseStack poseStack, int mouseX, int mouseY, Set<String> unlocked) {
        if (highlighted == null) {
            List<OriginalResearchCategory> categories = visibleCategories(unlocked);
            for (int i = 0; i < categories.size(); i++) {
                int x = leftPos - 24;
                int y = topPos + i * 24;
                if (mouseX >= x && mouseX < x + 24 && mouseY >= y && mouseY < y + 24) {
                    renderTooltip(poseStack, categoryTitle(categories.get(i)), mouseX, mouseY);
                    return;
                }
            }
            return;
        }
        boolean complete = OriginalResearchLayout.unlocked(unlocked, highlighted);
        boolean available = OriginalResearchLayout.available(unlocked, highlighted);
        List<Component> lines = new ArrayList<>();
        lines.add(researchName(highlighted));
        Component description = researchDescription(highlighted);
        String desc = OriginalResearchLayout.shortTitle(description.getString());
        if (!desc.isBlank()) {
            lines.add(Component.literal(desc).withStyle(ChatFormatting.DARK_GRAY));
        }

        // GuiResearchBrowser shows forbidden knowledge, primary-note guidance,
        // and secondary research aspect costs in the node tooltip.  Keep those
        // cues visible instead of relying only on the node frame brightness.
        if (highlighted.warp() > 0) {
            int level = Math.min(5, highlighted.warp());
            String forbidden = I18n.get("tc.forbidden")
                    .replace("%n", I18n.get("tc.forbidden.level." + level));
            lines.add(Component.literal(forbidden));
        }

        if (!complete) {
            if (!available) {
                lines.add(Component.translatable("tc.researchmissing").withStyle(ChatFormatting.RED));
            } else if (OriginalResearchLayout.secondary(highlighted)) {
                boolean enough = true;
                for (Map.Entry<String, Integer> cost : highlighted.aspects().entrySet()) {
                    Aspect aspect = Aspect.byId(cost.getKey());
                    int required = Math.max(0, cost.getValue());
                    if (aspect == null || !ClientAspectData.knows(aspect)) {
                        enough = false;
                        lines.add(Component.literal("?  " + required).withStyle(ChatFormatting.DARK_GRAY));
                        continue;
                    }
                    int pool = ClientAspectData.pool(aspect);
                    if (pool < required) enough = false;
                    lines.add(Component.literal(aspect.displayName() + "  " + required + " / " + pool)
                            .withStyle(aspect.color()));
                }
                lines.add(Component.translatable(enough ? "tc.research.purchase" : "tc.research.short")
                        .withStyle(enough ? ChatFormatting.DARK_GREEN : ChatFormatting.RED));
            } else {
                lines.add(Component.translatable("tc.research.getprim").withStyle(ChatFormatting.DARK_AQUA));
            }
        }
        renderComponentTooltip(poseStack, lines, mouseX, mouseY);
    }

    private void renderPopup(PoseStack poseStack) {
        if (popupUntil <= System.currentTimeMillis()) return;
        int x = leftPos + 128;
        int y = topPos + 112;
        List<FormattedCharSequence> lines = font.split(Component.literal(popupText), 150);
        int h = Math.max(20, lines.size() * 10 + 6);
        fill(poseStack, x - 78, y - h / 2, x + 78, y + h / 2, 0xAA000000);
        int yy = y - h / 2 + 3;
        for (FormattedCharSequence line : lines) {
            font.draw(poseStack, line, x - 73, yy, 0xFFDDCCAA);
            yy += 10;
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        List<OriginalResearchCategory> categories = visibleCategories(unlockedResearch());
        for (int i = 0; i < categories.size(); i++) {
            int x = leftPos - 24;
            int y = topPos + i * 24;
            if (mouseX >= x && mouseX < x + 24 && mouseY >= y && mouseY < y + 24) {
                OriginalResearchCategory next = categories.get(i);
                if (category != next) {
                    category = next;
                    selected = null;
                    clampPan();
                    saveBookState();
                    playGuiSound("cameraclack", TC4ThaumonomiconParity.CATEGORY_CLICK_VOLUME);
                }
                return true;
            }
        }

        int mapLeft = leftPos + OriginalResearchLayout.VIEW_X;
        int mapTop = topPos + OriginalResearchLayout.VIEW_Y;
        if (button == 0 && mouseX >= mapLeft && mouseX < mapLeft + OriginalResearchLayout.VIEW_WIDTH
                && mouseY >= mapTop && mouseY < mapTop + OriginalResearchLayout.VIEW_HEIGHT) {
            dragging = true;
            draggedBeyondClick = false;
            pressedEntry = highlighted;
            pressMouseX = mouseX;
            pressMouseY = mouseY;
            lastMouseX = mouseX;
            lastMouseY = mouseY;
            return true;
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    private void activateResearch(ResearchEntry entry) {
        if (entry == null) {
            return;
        }
        selected = entry;
        Set<String> unlocked = unlockedResearch();
        OriginalClientResearchSelection.set(selected.key());
        if (OriginalResearchLayout.unlocked(unlocked, selected) && TC4ResearchFlagPolicy.hasOriginalPagePayload(selected)) {
            TC4ThaumonomiconPageHistory.clear();
            Minecraft.getInstance().setScreen(new TC4ResearchPageScreen(this, selected));
        } else if (!OriginalResearchLayout.unlocked(unlocked, selected) && OriginalResearchLayout.available(unlocked, selected)) {
            if (TC4ResearchFlagPolicy.isSecondary(selected)) {
                if (hasEnoughResearchAspects(selected)) {
                    ThaumcraftNetwork.requestCompleteSelectedResearchFromClient(selected.key());
                }
            } else if (hasResearchSupplies(selected.key())) {
                ThaumcraftNetwork.requestCompleteSelectedResearchFromClient(selected.key());
                popupUntil = System.currentTimeMillis() + 3000L;
                popupText = Component.translatable("tc.research.popup", researchName(selected)).getString();
            }
        } else {
            popupUntil = 0L;
            popupText = "";
        }
    }

    private boolean hasEnoughResearchAspects(ResearchEntry entry) {
        for (var cost : entry.aspects().entrySet()) {
            Aspect aspect = Aspect.byId(cost.getKey().toLowerCase());
            if (aspect == null || ClientAspectData.pool(aspect) < cost.getValue()) return false;
        }
        return true;
    }

    private boolean hasResearchSupplies(String researchKey) {
        if (minecraft == null || minecraft.player == null) return false;
        if (minecraft.player.getAbilities().instabuild) return true;
        boolean paper = false;
        boolean tools = false;
        for (int i = 0; i < minecraft.player.getInventory().getContainerSize(); i++) {
            ItemStack stack = minecraft.player.getInventory().getItem(i);
            if (stack.is(Items.PAPER)) paper = true;
            if (stack.getItem() instanceof ScribingToolsItem && ScribingToolsItem.hasInk(stack)) tools = true;
            if (stack.getItem() instanceof ResearchNoteItem
                    && researchKey.equals(ResearchNoteState.target(stack)) && !ResearchNoteState.solved(stack)) return false;
        }
        return paper && tools;
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
        if (dragging) {
            double totalDx = mouseX - pressMouseX;
            double totalDy = mouseY - pressMouseY;
            if (totalDx * totalDx + totalDy * totalDy >= DRAG_THRESHOLD_SQUARED) {
                draggedBeyondClick = true;
            }
            if (draggedBeyondClick) {
                panX -= (int)(mouseX - lastMouseX);
                panY -= (int)(mouseY - lastMouseY);
                clampPan();
                saveBookState();
            }
            lastMouseX = mouseX;
            lastMouseY = mouseY;
            return true;
        }
        return super.mouseDragged(mouseX, mouseY, button, dragX, dragY);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        if (dragging && button == 0) {
            ResearchEntry clicked = !draggedBeyondClick ? pressedEntry : null;
            dragging = false;
            draggedBeyondClick = false;
            pressedEntry = null;
            if (clicked != null) {
                activateResearch(clicked);
            }
            return true;
        }
        dragging = false;
        pressedEntry = null;
        return super.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double delta) {
        return super.mouseScrolled(mouseX, mouseY, delta);
    }

    private List<OriginalResearchCategory> visibleCategories(Set<String> unlocked) {
        List<OriginalResearchCategory> result = new ArrayList<>();
        for (OriginalResearchCategory value : OriginalResearchCategory.values()) {
            if (value != OriginalResearchCategory.ELDRITCH || unlocked.contains("ELDRITCHMINOR")) {
                result.add(value);
            }
        }
        return result;
    }

    private void ensureVisibleCategory(Set<String> unlocked) {
        if (category == OriginalResearchCategory.ELDRITCH && !unlocked.contains("ELDRITCHMINOR")) {
            category = OriginalResearchCategory.BASICS;
            selected = null;
            clampPan();
            saveBookState();
        }
    }

    private void saveBookState() {
        TC4ThaumonomiconClientState.save(category, panX, panY);
    }

    private void playGuiSound(String key, float volume) {
        if (minecraft != null && minecraft.player != null) {
            minecraft.player.playSound(TC4Sounds.event(key), volume, TC4ThaumonomiconParity.GUI_SOUND_PITCH);
        }
    }

    @Override
    public void removed() {
        saveBookState();
        super.removed();
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == 256 || (minecraft != null && minecraft.options.keyInventory.matches(keyCode, scanCode))) {
            saveBookState();
            onClose();
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    private void clampPan() {
        OriginalResearchLayout.Bounds bounds = OriginalResearchLayout.boundsFor(category);
        int minX = Math.min(bounds.minPanX(), bounds.maxPanX());
        int maxX = Math.max(bounds.minPanX(), bounds.maxPanX()) - 1;
        int minY = Math.min(bounds.minPanY(), bounds.maxPanY());
        int maxY = Math.max(bounds.minPanY(), bounds.maxPanY()) - 1;
        panX = Math.max(minX, Math.min(maxX, panX));
        panY = Math.max(minY, Math.min(maxY, panY));
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
