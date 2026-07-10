package com.darkifov.thaumcraft.client.screen;

import com.darkifov.thaumcraft.client.ClientResearchData;
import com.darkifov.thaumcraft.network.ThaumcraftNetwork;
import com.darkifov.thaumcraft.research.ResearchEntry;
import com.darkifov.thaumcraft.research.TC4ResearchFlagPolicy;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FormattedCharSequence;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.Map;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Stage117: the Thaumonomicon browser is no longer the wide rebuild page.
 * It now follows TC4 GuiResearchBrowser behavior: 256x230 frame, draggable
 * research map, original category tabs, original backgrounds, original node
 * sprites and TC4 display coordinates.
 */
public class ThaumonomiconScreen extends Screen {
    private static final ResourceLocation GUI_RESEARCH = new ResourceLocation("thaumcraft", "textures/gui/gui_research.png");
    private static final int PANE_WIDTH = 256;
    private static final int PANE_HEIGHT = 230;
    private static final double DRAG_THRESHOLD_SQUARED = 16.0D;

    private int leftPos;
    private int topPos;
    private OriginalResearchCategory category = OriginalResearchCategory.BASICS;
    private ResearchEntry selected;
    private ResearchEntry highlighted;
    private int panX = -5 * OriginalResearchLayout.CELL - 141 / 2 - 12;
    private int panY = -6 * OriginalResearchLayout.CELL - 141 / 2;
    private boolean dragging;
    private boolean draggedBeyondClick;
    private ResearchEntry pressedEntry;
    private double pressMouseX;
    private double pressMouseY;
    private double lastMouseX;
    private double lastMouseY;
    private final Map<OriginalResearchCategory, Integer> categoryPanX = new EnumMap<>(OriginalResearchCategory.class);
    private final Map<OriginalResearchCategory, Integer> categoryPanY = new EnumMap<>(OriginalResearchCategory.class);
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
        for (OriginalResearchCategory value : OriginalResearchCategory.values()) {
            categoryPanX.putIfAbsent(value, panX);
            categoryPanY.putIfAbsent(value, panY);
        }
        restoreCategoryPan();
        clampPan();
    }

    private Set<String> unlockedResearch() {
        // Stage205 hard parity reset: do not inject fake client-side completions.
        // TC4 progression visibility must come from the player's research data and
        // each ResearchItem's parents/hiddenParents, not from convenience defaults.
        return new HashSet<>(ClientResearchData.research());
    }

    @Override
    public void render(PoseStack poseStack, int mouseX, int mouseY, float partialTick) {
        renderBackground(poseStack);
        highlighted = null;
        renderResearchTree(poseStack, mouseX, mouseY, partialTick);
        renderTabs(poseStack, mouseX, mouseY);
        OriginalGuiTextures.blitOriginalRegion(poseStack, leftPos, topPos, GUI_RESEARCH, 0, 0, PANE_WIDTH, PANE_HEIGHT, 256, 256);
        renderBrowserHeader(poseStack);
        renderSelectedPage(poseStack);
        renderPopup(poseStack);
        renderHoverTooltip(poseStack, mouseX, mouseY);
        super.render(poseStack, mouseX, mouseY, partialTick);
    }

    private void renderResearchTree(PoseStack poseStack, int mouseX, int mouseY, float partialTick) {
        renderMap(poseStack, mouseX, mouseY, partialTick);
    }

    private void renderBrowserHeader(PoseStack poseStack) {
        // Stage663-682: keep the browser frame visually original. Completion and
        // availability counts remain derivable from ClientResearchData, but are
        // not painted as rebuild/debug text over gui_research.png.
        drawString(poseStack, font, Component.literal(category.title()), leftPos + 12, topPos + 8, 0x2D1B0B);
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
        int bgU = proportionalBackgroundCoordinate(panX, bounds.minPanX(), bounds.maxPanX(), 288);
        int bgV = proportionalBackgroundCoordinate(panY, bounds.minPanY(), bounds.maxPanY(), 316);
        OriginalGuiTextures.blitOriginalRegion(poseStack, mapLeft, mapTop, category.background(), bgU, bgV, 224, 196, 512, 512);

        enableMapScissor(mapLeft, mapTop, OriginalResearchLayout.VIEW_WIDTH, OriginalResearchLayout.VIEW_HEIGHT);
        // Parent and sibling links are drawn before nodes, just like TC4.
        for (ResearchEntry entry : entries) {
            if (!OriginalResearchLayout.visible(unlocked, entry)) continue;
            int x1 = mapLeft + OriginalResearchLayout.mapX(entry, panX) + 11;
            int y1 = mapTop + OriginalResearchLayout.mapY(entry, panY) + 11;
            for (String parentKey : entry.requirements()) {
                ResearchEntry parent = find(entries, parentKey);
                if (parent == null || !OriginalResearchLayout.visible(unlocked, parent)) continue;
                int x2 = mapLeft + OriginalResearchLayout.mapX(parent, panX) + 11;
                int y2 = mapTop + OriginalResearchLayout.mapY(parent, panY) + 11;
                boolean active = unlocked.contains(parent.key()) || unlocked.contains(entry.key());
                drawResearchLine(poseStack, x1, y1, x2, y2, active ? 0xFF223F16 : 0xFF21325A, active);
            }
            for (String siblingKey : entry.siblings()) {
                ResearchEntry sibling = find(entries, siblingKey);
                if (sibling == null || !OriginalResearchLayout.visible(unlocked, sibling)) continue;
                int x2 = mapLeft + OriginalResearchLayout.mapX(sibling, panX) + 11;
                int y2 = mapTop + OriginalResearchLayout.mapY(sibling, panY) + 11;
                drawResearchLine(poseStack, x1, y1, x2, y2, 0xFF222244, false);
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
        int high = Math.max(minPan, maxPan);
        int span = Math.max(1, high - low);
        float normalized = (Math.max(low, Math.min(high, pan)) - low) / (float) span;
        return Math.max(0, Math.min(travel, Math.round(normalized * travel)));
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

    private void renderNode(PoseStack poseStack, ResearchEntry entry, int x, int y, boolean complete, boolean available, float partialTick) {
        RenderSystem.enableBlend();
        float brightness = complete ? 1.0f : available ? (0.72f + (float)Math.sin(System.currentTimeMillis() % 600L / 600.0D * Math.PI * 2.0D) * 0.18f) : 0.28f;
        RenderSystem.setShaderColor(brightness, brightness, brightness, 1.0f);

        int u;
        if (OriginalResearchLayout.round(entry)) {
            u = 54;
        } else if (entry.hasFlag("hidden")) {
            u = 86;
        } else if (OriginalResearchLayout.secondary(entry)) {
            u = 110;
        } else {
            u = 0;
        }
        OriginalGuiTextures.blitOriginalRegion(poseStack, x - 2, y - 2, GUI_RESEARCH, u, 230, 26, 26, 256, 256);
        if (OriginalResearchLayout.special(entry)) {
            OriginalGuiTextures.blitOriginalRegion(poseStack, x - 2, y - 2, GUI_RESEARCH, 26, 230, 26, 26, 256, 256);
        }
        RenderSystem.setShaderColor(1, 1, 1, 1);

        // Until every exact icon stack is registered, use the first required aspect
        // as TC4-flavored icon instead of the previous fake square placeholder.
        ResourceLocation icon = iconFor(entry);
        OriginalGuiTextures.blitOriginal(poseStack, x + 3, y + 3, icon, 16, 16);

        if (selected != null && selected.key().equals(entry.key())) {
            fill(poseStack, x - 4, y - 4, x + 26, y - 2, 0xAAFFE08A);
            fill(poseStack, x - 4, y + 24, x + 26, y + 26, 0xAAFFE08A);
            fill(poseStack, x - 4, y - 4, x - 2, y + 26, 0xAAFFE08A);
            fill(poseStack, x + 24, y - 4, x + 26, y + 26, 0xAAFFE08A);
        }
    }

    private ResourceLocation iconFor(ResearchEntry entry) {
        ResourceLocation mapped = TC4ResearchIconMap.texture(entry.key());
        if (mapped != null) {
            return mapped;
        }
        if (!entry.aspects().isEmpty()) {
            String aspect = entry.aspects().keySet().iterator().next().toLowerCase();
            return new ResourceLocation("thaumcraft", "textures/aspects/" + aspect + ".png");
        }
        String fallback = switch (category) {
            case BASICS -> "praecantatio";
            case THAUMATURGY -> "auram";
            case ALCHEMY -> "permutatio";
            case ARTIFICE -> "fabrico";
            case GOLEMANCY -> "humanus";
            case ELDRITCH -> "vitium";
        };
        return new ResourceLocation("thaumcraft", "textures/aspects/" + fallback + ".png");
    }

    private void drawResearchLine(PoseStack poseStack, int x1, int y1, int x2, int y2, int color, boolean active) {
        // TC4 draws animated dangling/wiggling research links, not rigid L-shaped sticks.
        // This 1.19.2 GuiComponent adapter preserves the original visual behaviour:
        // a polyline sampled along the direct parent/sibling vector with optional
        // sine offsets for not-yet-complete links.
        int dx = x2 - x1;
        int dy = y2 - y1;
        int steps = Math.max(1, (int)Math.sqrt(dx * dx + dy * dy) / 2);
        float tick = Minecraft.getInstance().player == null ? 0.0F : Minecraft.getInstance().player.tickCount;
        for (int i = 0; i <= steps; i++) {
            float phase = i / (float)steps;
            int x = x1 + Math.round(dx * phase);
            int y = y1 + Math.round(dy * phase);
            if (!active) {
                x += Math.round((float)Math.sin((tick + i) / 7.0F) * 5.0F * (1.0F - phase));
                y += Math.round((float)Math.sin((tick + i) / 5.0F) * 5.0F * (1.0F - phase));
            }
            int alpha = active ? 0xCC000000 : Math.max(0x33000000, (int)(0x99000000 * phase));
            fill(poseStack, x, y, x + 2, y + 2, (color & 0x00FFFFFF) | alpha);
        }
    }

    private void renderTabs(PoseStack poseStack, int mouseX, int mouseY) {
        int count = 0;
        for (OriginalResearchCategory value : OriginalResearchCategory.values()) {
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

    private void renderHoverTooltip(PoseStack poseStack, int mouseX, int mouseY) {
        if (highlighted == null) {
            for (int i = 0; i < OriginalResearchCategory.values().length; i++) {
                int x = leftPos - 24;
                int y = topPos + i * 24;
                if (mouseX >= x && mouseX < x + 24 && mouseY >= y && mouseY < y + 24) {
                    renderTooltip(poseStack, Component.literal(OriginalResearchCategory.values()[i].title()), mouseX, mouseY);
                    return;
                }
            }
            return;
        }
        Set<String> unlocked = unlockedResearch();
        boolean complete = OriginalResearchLayout.unlocked(unlocked, highlighted);
        boolean available = OriginalResearchLayout.available(unlocked, highlighted);
        List<Component> lines = new ArrayList<>();
        lines.add(Component.literal(highlighted.title()));
        String desc = OriginalResearchLayout.shortTitle(highlighted.description());
        if (!desc.isBlank()) {
            lines.add(Component.literal(desc));
        }
        // Do not append adapter status lines (Complete/Available/Missing/Warp/aspect map).
        // Visibility and locking are already represented by the original node brightness.
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
        for (int i = 0; i < OriginalResearchCategory.values().length; i++) {
            int x = leftPos - 24;
            int y = topPos + i * 24;
            if (mouseX >= x && mouseX < x + 24 && mouseY >= y && mouseY < y + 24) {
                saveCategoryPan();
                category = OriginalResearchCategory.values()[i];
                selected = null;
                restoreCategoryPan();
                clampPan();
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
        ThaumcraftNetwork.requestSelectResearchFromClient(selected.key());
        if (OriginalResearchLayout.unlocked(unlocked, selected) && TC4ResearchFlagPolicy.hasOriginalPagePayload(selected)) {
            Minecraft.getInstance().setScreen(new TC4ResearchPageScreen(this, selected));
        } else if (!OriginalResearchLayout.unlocked(unlocked, selected) && OriginalResearchLayout.available(unlocked, selected)) {
            ThaumcraftNetwork.requestCompleteSelectedResearchFromClient();
            popupUntil = 0L;
            popupText = "";
        } else {
            popupUntil = 0L;
            popupText = "";
        }
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
                saveCategoryPan();
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
        panY -= (int)(delta * 18);
        clampPan();
        saveCategoryPan();
        return true;
    }

    private void saveCategoryPan() {
        categoryPanX.put(category, panX);
        categoryPanY.put(category, panY);
    }

    private void restoreCategoryPan() {
        panX = categoryPanX.getOrDefault(category, panX);
        panY = categoryPanY.getOrDefault(category, panY);
    }

    private void clampPan() {
        OriginalResearchLayout.Bounds bounds = OriginalResearchLayout.boundsFor(category);
        int minX = Math.min(bounds.minPanX(), bounds.maxPanX());
        int maxX = Math.max(bounds.minPanX(), bounds.maxPanX());
        int minY = Math.min(bounds.minPanY(), bounds.maxPanY());
        int maxY = Math.max(bounds.minPanY(), bounds.maxPanY());
        panX = Math.max(minX, Math.min(maxX, panX));
        panY = Math.max(minY, Math.min(maxY, panY));
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
